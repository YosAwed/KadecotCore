package com.sonycsl.Kadecot.server;

import java.io.IOException;
import java.util.concurrent.Executors;

import com.sonycsl.Kadecot.call.KadecotCall;
import com.sonycsl.Kadecot.call.Notification;
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
	private ServerNetwork mServerNetwork;
	private DeviceManager mDeviceManager;
	private ServerSettings mServerSettings;
	
	private ServerManager(Context context) {
		mContext = context.getApplicationContext();
	}
	
	public static synchronized ServerManager getInstance(Context context) {
		if(sInstance == null) {
			sInstance = new ServerManager(context);
			sInstance.init();
		}
		return sInstance;
	}
	
	protected void init() {
		mDeviceManager = DeviceManager.getInstance(mContext);

		mJSONPServer = KadecotJSONPServer.getInstance(mContext);
		mWebSocketServer = KadecotWebSocketServer.getInstance(mContext);
		mServerSettings = ServerSettings.getInstance(mContext);
		
		mServerNetwork = ServerNetwork.getInstance(mContext);

	}
	
	public void startServer(KadecotServerService service) {
		mKadecotService = service;

		mServerNetwork.startConnectionReceiver();
		
		onChangedServerSettings();
		
	}
	
	public void startHomeNetwork() {

		Executors.newSingleThreadExecutor().execute(new Runnable() {
			@Override
			public void run() {
				mDeviceManager.start();
			}
		
		});
	}
	
	public void stopHomeNetwork() {
		mDeviceManager.stop();
		
		
		changeNotification();

	}

	
	public void stopServer(KadecotServerService service) {
		if(mKadecotService == service) {
			mKadecotService = null;
			mServerNetwork.stopConnectionReceiver();

			stopWebSocketServer();
			stopJSONPServer();
		}
	}
	
	
	public void onChangedServerSettings() {

		if(mServerSettings.isEnabledPersistentMode()) {
			if( mKadecotService != null ) {
				mKadecotService.startForeground() ;	// persistent
			}
		} else {
			if( mKadecotService != null ) {
				mKadecotService.stopForeground() ;	// no persistent
			}
		}
		

		if(mServerSettings.isEnabledJSONPServer() && !isStartedJSONPServer() ) {
			startJSONPServer();
		} else if(!mServerSettings.isEnabledJSONPServer()){
			stopJSONPServer();
		}

		if(mServerSettings.isEnabledWebSocketServer() && !isStartedWebSocketServer() ) {
			startWebSocketServer();
		} else if(!mServerSettings.isEnabledWebSocketServer()){
			stopWebSocketServer();
		}
		
		changeNotification();
		
		KadecotCall.informAll(Notification.ON_NOTIFY_SERVER_SETTINGS, Notification.onNotifyServerSettings(mContext));
	}
	
	public void startWebSocketServer() {
		mWebSocketServer.start();
		changeNotification();

	}
	
	public void stopWebSocketServer() {
		mWebSocketServer.stop();
		changeNotification();

	}
	
	public boolean isStartedWebSocketServer() {
		return mWebSocketServer.isStarted();
	}
	
	public void startJSONPServer() {
		try {
			mJSONPServer.start(31413);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		changeNotification();

	}
	
	public void stopJSONPServer() {
		mJSONPServer.stop();
		changeNotification();

	}
	
	public boolean isStartedJSONPServer() {
		return mJSONPServer.isRunning();
	}
	
	private void changeNotification() {
		if( mKadecotService != null ) {
			mKadecotService.changeNotification();
		}
	}
}
