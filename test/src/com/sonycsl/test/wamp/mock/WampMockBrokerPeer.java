
package com.sonycsl.test.wamp.mock;

import com.sonycsl.test.wamp.WampTest;
import com.sonycsl.wamp.WampRouter;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WampMockBrokerPeer extends WampRouter implements WampTest {

    private CountDownLatch mLatch;

    private Queue<WampMessage> mMessageQueue = new LinkedList<WampMessage>();

    @Override
    protected boolean consumeRoleMessage(WampMessenger friend, WampMessage msg) {
        if (consumeMyMessage(friend, msg)) {
            mMessageQueue.add(msg);
            if (mLatch != null) {
                mLatch.countDown();
            }
            return true;
        }
        return false;
    }

    private boolean consumeMyMessage(WampMessenger friend, WampMessage msg) {
        if (msg.isSubscribeMessage()) {
            friend.send(WampMessageFactory.createSubscribed(
                    msg.asSubscribeMessage().getRequestId(), 0));
            return true;
        }

        if (msg.isPublishMessage()) {
            friend.send(WampMessageFactory.createPublished(0, 0));
            return true;
        }

        if (msg.isUnsubscribedMessage()) {
            friend.send(WampMessageFactory
                    .createUnsubscribed(msg.asUnregisterMessage().getRequestId()));
            return true;
        }
        return false;
    }

    @Override
    public void setConsumed(boolean isConsumed) {
    }

    @Override
    public void setCountDownLatch(CountDownLatch latch) {
        mLatch = latch;
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return mLatch.await(timeout, unit);
    }

    @Override
    public WampMessage getMessage() {
        return mMessageQueue.remove();
    }

    @Override
    protected void onConsumed(WampMessage msg) {
    }

    @Override
    protected boolean consumeRoleBroadcast(WampMessage msg) {
        return true;
    }
}
