
package com.sonycsl.wamp;

import com.sonycsl.wamp.mock.WampMockRouter;

import junit.framework.TestCase;

import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WampClientTestCase extends TestCase {

    private static class WampTestClient extends WampClient {

        private CountDownLatch mLatch;
        private WampMessenger mFriendMessenger;
        private WampMessage mMsg;
        private boolean mIsConsumed = true;

        @Override
        protected boolean consumeRoleMessage(WampMessenger friend, WampMessage msg) {
            return false;
        }

        @Override
        protected void onConsumed(WampMessage msg) {
            mMsg = msg;
            if (mLatch != null) {
                mLatch.countDown();
            }
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

    private WampTestClient mClient;
    private WampMockRouter mFriend;

    @Override
    protected void setUp() {
        mClient = new WampTestClient();
        mFriend = new WampMockRouter();
        mClient.connect(mFriend);
    }

    public void testCtor() {
        assertNotNull(mClient);
        assertNotNull(mFriend);
    }

    public void testHello() {
        WampMessage msg = WampMessageFactory.createHello("realm", new JSONObject());

        mFriend.setCountDownLatch(new CountDownLatch(1));
        mClient.setCountDownLatch(new CountDownLatch(1));
        mClient.broadcast(msg);

        try {
            assertTrue(mFriend.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
        assertTrue(mFriend.getMessage().isHelloMessage());

        try {
            assertTrue(mClient.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
        assertTrue(mClient.getMessage().isWelcomeMessage());
    }

    public void testGoodbye() {
        testHello();

        WampMessage msg = WampMessageFactory.createGoodbye(new JSONObject(), WampError.CLOSE_REALM);

        mFriend.setCountDownLatch(new CountDownLatch(1));
        mClient.setCountDownLatch(new CountDownLatch(1));

        mFriend.broadcast(msg);

        try {
            assertTrue(mClient.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
        assertTrue(mClient.getMessage().isGoodbyeMessage());
        assertEquals(WampError.CLOSE_REALM, mClient.getMessage().asGoodbyeMessage().getReason());

        try {
            assertTrue(mFriend.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
        assertTrue(mFriend.getMessage().isGoodbyeMessage());
        assertEquals(WampError.GOODBYE_AND_OUT, mFriend.getMessage().asGoodbyeMessage().getReason());
    }
}
