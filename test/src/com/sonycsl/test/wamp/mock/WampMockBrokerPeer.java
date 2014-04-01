
package com.sonycsl.test.wamp.mock;

import com.sonycsl.test.wamp.WampTest;
import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampMessageFactory;
import com.sonycsl.wamp.WampRouter;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WampMockBrokerPeer extends WampRouter implements WampTest {

    private WampMessenger mSubscriber;

    private CountDownLatch mLatch;

    private Queue<WampMessage> mMessageQueue = new LinkedList<WampMessage>();

    @Override
    protected boolean consumeRoleMessage(WampMessenger friend, WampMessage msg) {
        if (consumeMyMessage(friend, msg)) {
            mMessageQueue.add(msg);
            return true;
        }
        return false;
    }

    private boolean consumeMyMessage(WampMessenger friend, WampMessage msg) {
        if (msg.isSubscribeMessage()) {
            friend.send(WampMessageFactory.createSubscribed(
                    msg.asSubscribeMessage().getRequestId(), 0));
            mSubscriber = friend;
            return true;
        }

        if (msg.isPublishMessage()) {
            friend.send(WampMessageFactory.createPublished(0, 0));
            mSubscriber.send(WampMessageFactory.createEvent(0, 0, new JSONObject()));
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
        if (msg.isCallMessage() || msg.isYieldMessage() || msg.isRegisterMessage()
                || msg.isUnregisterMessage()) {
            mMessageQueue.add(msg);
        }
        if (mLatch != null) {
            mLatch.countDown();
        }
    }

    @Override
    protected boolean consumeRoleBroadcast(WampMessage msg) {
        return true;
    }
}
