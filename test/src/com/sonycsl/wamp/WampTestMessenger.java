/*
 * Copyright (C) Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp;

import java.util.concurrent.CountDownLatch;

public class WampTestMessenger implements WampMessenger {
    private CountDownLatch mLatch;
    private WampMessage mMsg;

    public WampTestMessenger() {
    }

    @Override
    public void send(WampMessage msg) {
        mMsg = msg;
        if (mLatch != null) {
            mLatch.countDown();
        }
    }

    public void setCountDownLatch(CountDownLatch latch) {
        mLatch = latch;
    }

    public WampMessage getRecievedMessage() {
        return mMsg;
    }
}
