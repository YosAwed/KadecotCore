
package com.sonycsl.test.wamp;

import com.sonycsl.test.wamp.mock.MockWampRouter;
import com.sonycsl.wamp.WampClient;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampRole;

import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WampClientSessionTestCase extends TestCase {

    private static class TestWampClient extends WampClient implements Testable {

        private CountDownLatch mLatch;
        private WampMessage mMsg;

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
        protected WampRole getClientRole() {
            return null;
        }

        @Override
        protected void onReceived(WampMessage msg) {
            mMsg = msg;
            if (mLatch != null) {
                mLatch.countDown();
            }
        }
    }

    private TestWampClient mClient;
    private MockWampRouter mRouter;

    @Override
    protected void setUp() {
        mClient = new TestWampClient();
        mRouter = new MockWampRouter();
        mClient.connect(mRouter);
    }

    public void testCtor() {
        assertNotNull(mClient);
        assertNotNull(mRouter);
    }

    public void testHello() {
        WampTestUtil.transmitHelloSuccess(mClient, WampTestParam.REALM, mRouter);
    }

    public void testGoodbye() {
        WampTestUtil.transmitHelloSuccess(mClient, WampTestParam.REALM, mRouter);
        WampTestUtil.transmitGoodbyeSuccess(mClient, WampError.CLOSE_REALM, mRouter);

        WampTestUtil.transmitHelloSuccess(mClient, WampTestParam.REALM, mRouter);
        WampTestUtil.transmitGoodbyeSuccess(mRouter, WampError.CLOSE_REALM, mClient);

        WampTestUtil.transmitHelloSuccess(mClient, WampTestParam.REALM, mRouter);
        WampTestUtil.transmitGoodbyeSuccess(mRouter, WampError.SYSTEM_SHUTDOWN, mClient);
    }
}
