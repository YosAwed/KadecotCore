
package com.sonycsl.test.wamp.mock;

import android.util.Log;

import com.sonycsl.test.wamp.WampTest;
import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampMessageFactory;
import com.sonycsl.wamp.WampRouter;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WampMockDealerPeer extends WampRouter implements WampTest {

    public WampMockDealerPeer() {
    }

    public WampMockDealerPeer(WampRouter router) {
        super(router);
    }

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
        if (msg.isRegisterMessage()) {
            friend.send(WampMessageFactory.createRegistered(msg.asRegisterMessage().getRequestId(),
                    0));
            return true;
        }

        if (msg.isCallMessage()) {
            friend.send(WampMessageFactory.createResult(msg.asCallMessage().getRequestId(),
                    new JSONObject()));
            return true;
        }

        if (msg.isYieldMessage()) {
            return true;
        }

        if (msg.isUnregisterMessage()) {
            friend.send(WampMessageFactory
                    .createUnregistered(msg.asUnregisterMessage().getRequestId()));
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
