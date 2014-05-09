/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.server;

import android.os.Binder;

public class ServerBinder extends Binder {

    protected final KadecotServerService mKadecotServer;

    public ServerBinder(KadecotServerService kadecotServer) {
        mKadecotServer = kadecotServer;
    }

    public void reqStartServer() {
        mKadecotServer.startServer();
    }

    public void reqStopServer() {
        if (!mKadecotServer.mForeground) {
            mKadecotServer.stopServer();
        }
    }

}
