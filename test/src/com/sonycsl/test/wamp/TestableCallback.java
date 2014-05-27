
package com.sonycsl.test.wamp;

import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestableCallback implements WampPeer.Callback {

    private CountDownLatch mLatch;
    private int mType;
    private WampMessage mMsg;

    public void setTargetMessageType(int type, CountDownLatch latch) {
        mType = type;
        mLatch = latch;
    }

    public WampMessage getTargetMessage() {
        return mMsg;
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return mLatch.await(timeout, unit);
    }

    @Override
    public void preConnect(WampPeer connecter, WampPeer connectee) {
    }

    @Override
    public void postConnect(WampPeer connecter, WampPeer connectee) {
    }

    @Override
    public void preTransmit(WampPeer transmitter, WampMessage msg) {
    }

    @Override
    public void postTransmit(WampPeer transmitter, WampMessage msg) {
    }

    @Override
    public void preReceive(WampPeer receiver, WampMessage msg) {
    }

    @Override
    public void postReceive(WampPeer receiver, WampMessage msg) {
        if (mType != msg.getMessageType()) {
            return;
        }

        mMsg = msg;
        if (mLatch != null) {
            mLatch.countDown();
        }
    }
}
