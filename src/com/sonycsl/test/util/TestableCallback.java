
package com.sonycsl.test.util;

import com.sonycsl.kadecot.wamp.KadecotWampClientSetupCallback;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestableCallback extends KadecotWampClientSetupCallback {

    public TestableCallback() {
        super(new HashSet<String>(), new HashSet<String>(), new OnCompletionListener() {
            @Override
            public void onCompletion() {
            }
        });
    }

    public TestableCallback(Set<String> topics, Set<String> procedures,
            OnCompletionListener listener) {
        super(topics, procedures, listener);
    }

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
    public void postReceive(WampPeer receiver, WampMessage msg) {

        if (mType != msg.getMessageType()) {
            super.postReceive(receiver, msg);
            return;
        }

        mMsg = msg;
        if (mLatch != null) {
            mLatch.countDown();
        }

        super.postReceive(receiver, msg);
    }
}
