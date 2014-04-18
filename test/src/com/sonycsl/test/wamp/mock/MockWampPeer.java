
package com.sonycsl.test.wamp.mock;

import com.sonycsl.test.wamp.Testable;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampRole;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MockWampPeer extends WampPeer implements Testable {

    private CountDownLatch mLatch;
    private WampMessage mMsg;

    public MockWampPeer() {
        super();
    }

    public void setCountDownLatch(CountDownLatch latch) {
        mLatch = latch;
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return mLatch.await(timeout, unit);
    }

    public WampMessage getLatestMessage() {
        return mMsg;
    }

    @Override
    protected WampRole getRole() {
        return new MockWampRole();
    }

    @Override
    protected void OnConnected(WampPeer peer) {
    }

    @Override
    protected void OnReceived(WampMessage msg) {
        mMsg = msg;
        if (mLatch != null) {
            mLatch.countDown();
        }
    }
}
