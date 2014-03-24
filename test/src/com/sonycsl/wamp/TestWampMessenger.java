/*
 * Copyright (C) Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp;

import org.json.JSONArray;

import java.util.concurrent.CountDownLatch;

public class TestWampMessenger implements WampMessenger {
    private CountDownLatch mLatch;
    private JSONArray mMsg;

    public TestWampMessenger() {
    }

    @Override
    public void send(JSONArray msg) {
        mMsg = msg;
        if (mLatch != null) {
            mLatch.countDown();
        }
    }

    public void setCountDownLatch(CountDownLatch latch) {
        mLatch = latch;
    }

    public JSONArray getReceivedMessage() {
        return mMsg;
    }
}
