
package com.sonycsl.wamp;

import java.util.concurrent.CountDownLatch;

public class TestEchoWampPeer extends WampPeer {

    private CountDownLatch mLatch;
    private WampMessenger mFriendMessenger;
    private WampMessage mMsg;
    private boolean mIsConsumed = true;

    public TestEchoWampPeer() {
        super();
    }

    public TestEchoWampPeer(WampPeer next) {
        super(next);
    }

    @Override
    protected boolean consumeMessage(WampMessenger friend, WampMessage msg) {
        if (!mIsConsumed) {
            return false;
        }

        mFriendMessenger = friend;
        mMsg = msg;
        mFriendMessenger.send(msg);
        mLatch.countDown();
        return true;
    }

    public void setConsumed(boolean isConsumed) {
        mIsConsumed = isConsumed;
    }

    public void setCountDownLatch(CountDownLatch latch) {
        mLatch = latch;
    }

    public WampMessenger getFriendMessenger() {
        return mFriendMessenger;
    }

    public WampMessage getMessage() {
        return mMsg;
    }

}
