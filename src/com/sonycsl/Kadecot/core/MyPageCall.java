/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.core;

import android.webkit.JavascriptInterface;

public class MyPageCall {

    private final KadecotCoreActivity mKadecot;

    public MyPageCall(KadecotCoreActivity kadecot) {
        mKadecot = kadecot;
    }

    @JavascriptInterface
    public void postMessage(final String message) {

        mKadecot.callJsOnKadecotMyPage("kHAPI.app.onMsgFromApp(null," + message + ")");
        /*
         * StringBuilder builder = new StringBuilder();
         * builder.append("if(\"onMsgFromUserApp\" in window){onMsgFromUserApp("
         * ); builder.append(message); builder.append(");};");
         * mKadecot.callJsOnKadecotMyPage(new String(builder));
         */
    }
}
