package com.sonycsl.Kadecot.server;

import java.io.IOException;
import java.util.concurrent.Executors;

import com.sonycsl.Kadecot.call.KadecotCall;
import com.sonycsl.Kadecot.call.Notification;
import com.sonycsl.Kadecot.core.Dbg;
import com.sonycsl.Kadecot.device.DeviceManager;
import com.sonycsl.Kadecot.server.ServerSettings.ExecutionMode;

import android.content.Context;
import android.util.Log;

public class ServerManager {
	@SuppressWarnings("unused")
	private static final String TAG = ServerManager.class.getSimpleName();
	private final ServerManager self = this;
	
	private static ServerManager sInstance = null;
	
	private final Context mContext;
	
	private KadecotServerService mKadecotService;
	
	private KadecotJSONPServer mJSONPServer;
	private KadecotWebSocketServer mWebSocketServer;
	private KadecotSnapServer mSnapServer;
	private ServerNetwork mServerNetwork;
	private DeviceManager mDeviceManager;
	private ServerSettings mServerSettings;

	private static final int mSnapPort = 31414; 
	
	
	private final int STATUS_ON = 0;
	private final int STATUS_HOME_NETWORK_ACTIVE = 1;
	private final int STATUS_OFF = 2;
	
	private int mStatus = STATUS_OFF;

	
	private ServerManager(Context context) {
		mContext = context.getApplicationContext();
	}
	
	public static synchronized ServerManager getInstance(Context context) {
		if(sInstance == null) {
			sInstance = new ServerManager(context);
		}
		return sInstance;
	}
	
	public void startServer(KadecotServerService service) {
		Dbg.print(mStatus);
		mKadecotService = service;
		
		if(mStatus == STATUS_OFF) {
			mStatus = STATUS_ON;
			getNetwork().startWatchingConnection();
		
			onChangedServerSettings();
		}
	}

	
	public void stopServer(KadecotServerService service) {
		Dbg.print(mStatus);

		if(mStatus == STATUS_OFF) {
			return;
		}
		if(mKadecotService == service) {
			mKadecotService = null;
			
			
			getNetwork().stopWatchingConnection();

			//stopWebSocketServer();
			//stopJSONPServer();
			//stopForeground();
			//stopHomeNetwork();
			
			mStatus = STATUS_OFF;
		}
	}
	
	
	
	public void startHomeNetwork() {
		Dbg.print(mStatus);
		if(mStatus != STATUS_ON) {
			return;
		}
		mStatus = STATUS_HOME_NETWORK_ACTIVE;
		Executors.newSingleThreadExecutor().execute(new Runnable(){
			@Override
			public void run() {
				getDeviceManager().start();
			}
		});
		//changeNotification();
		onChangedServerSettings();
	}
	
	public void stopHomeNetwork() {
		Dbg.print(mStatus);
		if(mStatus == STATUS_HOME_NETWORK_ACTIVE) {
			getDeviceManager().stop();
			
			stopWebSocketServer();
			stopJSONPServer();
			stopSnapServer();
			stopForeground();
			
			//changeNotification();
			mStatus = STATUS_ON;
			onChangedServerSettings();
		}

	}
	public void onChangedServerSettings() {
		Dbg.print(mStatus);


		getNetwork().watchConnection();
		
		
		if(getSettings().isEnabledPersistentMode()) {
			startForeground();
		} else {
			stopForeground();
		}
		

		if(getSettings().isEnabledJSONPServer() && !isStartedJSONPServer() ) {
			startJSONPServer();
			// temporary
			startSnapServer();
		} else if(!getSettings().isEnabledJSONPServer()){
			stopJSONPServer();
			stopSnapServer();
		}

		if(getSettings().isEnabledWebSocketServer() && !isStartedWebSocketServer() ) {
			startWebSocketServer();
		} else if(!getSettings().isEnabledWebSocketServer()){
			stopWebSocketServer();
		}
		
		changeNotification();
		
		Notification.informAllOnNotifyServerSettings(mContext);
	}
	
	private void startForeground() {
		Dbg.print(mStatus);
		if(mStatus != STATUS_HOME_NETWORK_ACTIVE) {
			return;
		}

		if( mKadecotService != null ) {
			mKadecotService.startForeground() ;	// persistent
		}
	}
	
	private void stopForeground() {
		Dbg.print(mStatus);

		if( mKadecotService != null ) {
			mKadecotService.stopForeground() ;	// no persistent
		}
	}
	
	private void startWebSocketServer() {
		Dbg.print(mStatus);
		if(mStatus != STATUS_HOME_NETWORK_ACTIVE) {
			return;
		}
		getWebSocketServer().start();

	}
	
	private void stopWebSocketServer() {
		Dbg.print(mStatus);
		getWebSocketServer().stop();
	}
	
	public boolean isStartedWebSocketServer() {
		return getWebSocketServer().isStarted();
	}
	
	private void startJSONPServer() {
		Dbg.print(mStatus);
		if(mStatus != STATUS_HOME_NETWORK_ACTIVE) {
			return;
		}
		try {
			getJSONPServer().start(31413);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		changeNotification();

	}
	
	private void startSnapServer(){
        if (mStatus != STATUS_HOME_NETWORK_ACTIVE) {
            return;
        }
        try {
            getSnapServer().start(mSnapPort);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        changeNotification();
	}
	
	private void stopJSONPServer() {
		Dbg.print(mStatus);
		getJSONPServer().stop();
		changeNotification();
    }

    private void stopSnapServer() {
        Dbg.print(mStatus);
        getSnapServer().stop();
        changeNotification();
    }
	
	public boolean isStartedJSONPServer() {
		return getJSONPServer().isRunning();
	}
	public boolean isStartedSnapServer(){
	    return getSnapServer().isRunning();
	}
	
	private void changeNotification() {
		if( mKadecotService != null ) {
			mKadecotService.changeNotification();
		}
	}
	
	private DeviceManager getDeviceManager() {
		if(mDeviceManager == null) {
			mDeviceManager = DeviceManager.getInstance(mContext);
		}
		return mDeviceManager;
	}
	
	private KadecotJSONPServer getJSONPServer() {
		if(mJSONPServer == null) {
			mJSONPServer = KadecotJSONPServer.getInstance(mContext);
		}
		return mJSONPServer;
	}
	private KadecotSnapServer getSnapServer(){
	    if(mSnapServer == null){
	        mSnapServer = KadecotSnapServer.getInstance(mContext);
	    }
	    return mSnapServer;
	}
	
	
	private KadecotWebSocketServer getWebSocketServer() {
		if(mWebSocketServer == null) {
			mWebSocketServer = KadecotWebSocketServer.getInstance(mContext);
		}
		return mWebSocketServer;
	}
	
	private ServerSettings getSettings() {
		if(mServerSettings == null) {
			mServerSettings = ServerSettings.getInstance(mContext);
		}
		return mServerSettings;
	}
	
	private ServerNetwork getNetwork() {
		if(mServerNetwork == null) {
			mServerNetwork = ServerNetwork.getInstance(mContext);
		}
		return mServerNetwork;
	}
}
