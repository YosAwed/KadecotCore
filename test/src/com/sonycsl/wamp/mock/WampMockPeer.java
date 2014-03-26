
package com.sonycsl.wamp.mock;

import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampMessenger;
import com.sonycsl.wamp.WampPeer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WampMockPeer extends WampPeer {

    private CountDownLatch mLatch;
    private WampMessenger mFriendMessenger;
    private WampMessage mMsg;
    private boolean mIsConsumed = true;

    public WampMockPeer() {
        super();
    }

    public WampMockPeer(WampPeer next) {
        super(next);
    }

    @Override
    protected boolean consumeMessage(WampMessenger friend, WampMessage msg) {
        if (!mIsConsumed) {
            return false;
        }

        mFriendMessenger = friend;
        mMsg = msg;
        if (mLatch != null) {
            mLatch.countDown();
        }
        return true;
    }

    public void setConsumed(boolean isConsumed) {
        mIsConsumed = isConsumed;
    }

    public void setCountDownLatch(CountDownLatch latch) {
        mLatch = latch;
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return mLatch.await(timeout, unit);
    }

    public WampMessenger getFriendMessenger() {
        return mFriendMessenger;
    }

    public WampMessage getMessage() {
        return mMsg;
    }

}
