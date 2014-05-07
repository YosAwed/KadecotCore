/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.core;

import android.webkit.WebView;

public class UserApp {
    @SuppressWarnings("unused")
    private static final String TAG = UserApp.class.getSimpleName();

    private final UserApp self = this;

    private final KadecotCoreActivity mKadecot;

    public UserApp(KadecotCoreActivity kadecot) {
        mKadecot = kadecot;
    }

    /**
     * KadecotMyPageからAppViewにメッセージを送る
     * 
     * @param message
     */
    public void postMessage(final String message) {
        mKadecot.callJsOnAppView("kadecot._wa.onMsgFromServer(null," + message + ");");
    }

    public void openAppView(final String url) {
        mKadecot.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mKadecot.getAppView().setVisibility(WebView.VISIBLE);
                mKadecot.loadUrlOnAppView(url);
            }

        });
    }

    public void closeAppView() {
        mKadecot.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mKadecot.getAppView().setVisibility(WebView.INVISIBLE);
                mKadecot.loadUrlOnAppView("");
            }

        });
    }

}
