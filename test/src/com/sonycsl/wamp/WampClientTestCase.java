
package com.sonycsl.wamp;

import com.sonycsl.wamp.mock.WampMockRouter;

import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WampClientTestCase extends TestCase {

    private static class WampTestClient extends WampClient implements WampTest {

        private CountDownLatch mLatch;
        private WampMessenger mFriendMessenger;
        private WampMessage mMsg;
        private boolean mIsConsumed = true;

        @Override
        protected boolean consumeRoleMessage(WampMessenger friend, WampMessage msg) {
            return mIsConsumed;
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

        @Override
        protected void onBroadcast(WampMessage msg) {
        }
    }

    private WampTestClient mClient;
    private WampMockRouter mRouter;

    @Override
    protected void setUp() {
        mClient = new WampTestClient();
        mRouter = new WampMockRouter();
        mClient.connect(mRouter);
    }

    public void testCtor() {
        assertNotNull(mClient);
        assertNotNull(mRouter);
    }

    public void testHello() {
        WampTestUtil.broadcastHelloSuccess(mClient);
    }

    public void testGoodbye() {
        WampTestUtil.broadcastHelloSuccess(mClient);
        WampTestUtil.broadcastGoodbyeSuccess(mRouter, mClient);
    }
}
