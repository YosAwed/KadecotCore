package com.sonycsl.Kadecot.server;

import android.os.Binder;

public class ServerBinder extends Binder {
	@SuppressWarnings("unused")
	private static final String TAG = ServerBinder.class.getSimpleName();
	private final ServerBinder self = this;
	
	protected final KadecotServerService mKadecotServer;
	
	public ServerBinder(KadecotServerService kadecotServer) {
		mKadecotServer = kadecotServer;
	}
	
	public void reqStartServer() {
		mKadecotServer.startServer();
	}
	
	public void reqStopServer() {
		if(!mKadecotServer.mForeground) {
			mKadecotServer.stopServer();
		}
	}
	
}
