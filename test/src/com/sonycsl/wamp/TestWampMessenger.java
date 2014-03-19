/*
 * Copyright (C) Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp;

import com.sonycsl.wamp.WampMessenger;

import org.json.JSONArray;

import java.util.concurrent.CountDownLatch;

public class TestWampMessenger implements WampMessenger {
    private CountDownLatch mLatch;
    private JSONArray mMsg;

    public TestWampMessenger() {
    }

    @Override
    public void send(JSONArray msg) {
        mLatch.countDown();
        mMsg = msg;
    }

    public void setCountDownLatch(CountDownLatch latch) {
        mLatch = latch;
    }

    public JSONArray getRecievedMessage() {
        return mMsg;
    }
}
