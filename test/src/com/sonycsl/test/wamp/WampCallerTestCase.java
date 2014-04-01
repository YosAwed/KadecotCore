
package com.sonycsl.test.wamp;

import com.sonycsl.test.wamp.mock.WampMockCalleePeer;
import com.sonycsl.test.wamp.mock.WampMockDealerPeer;
import com.sonycsl.wamp.WampCaller;
import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampResultMessage;

import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WampCallerTestCase extends TestCase {

    private static class WampTestCaller extends WampCaller implements WampTest {

        private CountDownLatch mLatch;
        private WampMessage mMsg;

        @Override
        protected void result(WampResultMessage msg) {
        }

        @Override
        protected void onConsumed(WampMessage msg) {
            mMsg = msg;
            if (mLatch != null) {
                mLatch.countDown();
            }
        }

        public void setCountDownLatch(CountDownLatch latch) {
            mLatch = latch;
        }

        @Override
        public WampMessage getMessage() {
            return mMsg;
        }

        @Override
        public void setConsumed(boolean isConsumed) {
        }

        @Override
        public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            return mLatch.await(timeout, unit);
        }

    }

    private static final String TEST_PROCEDURE = "test.procedure";

    private WampTestCaller mCaller;
    private WampMockCalleePeer mCallee;
    private WampMockDealerPeer mRouter;

    @Override
    protected void setUp() throws Exception {
        mCaller = new WampTestCaller();
        mCallee = new WampMockCalleePeer();
        mRouter = new WampMockDealerPeer();
        mCaller.connect(mRouter);
        mCallee.connect(mRouter);
    }

    public void testCtor() {
        assertNotNull(mCaller);
        assertNotNull(mCallee);
        assertNotNull(mRouter);
    }

    public void testCall() {
        WampTestUtil.broadcastHelloSuccess(mCaller);
        WampTestUtil.broadcastHelloSuccess(mCallee);
        WampTestUtil.broadcastRegisterSuccess(mCallee, TEST_PROCEDURE, mRouter);
        WampTestUtil.broadcastCallSuccess(mCaller, TEST_PROCEDURE, mRouter, mCallee);
    }
}
