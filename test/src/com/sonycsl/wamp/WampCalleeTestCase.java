
package com.sonycsl.wamp;

import com.sonycsl.wamp.mock.WampMockPeer;
import com.sonycsl.wamp.mock.WampMockRouter;

import junit.framework.TestCase;

import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WampCalleeTestCase extends TestCase {

    private static class WampTestCallee extends WampCallee implements WampTest {

        private CountDownLatch mLatch;
        private WampMessage mMsg;

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

        public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            return mLatch.await(timeout, unit);
        }

        @Override
        public void setConsumed(boolean isConsumed) {
        }

        @Override
        public WampMessage getMessage() {
            return mMsg;
        }

        @Override
        protected WampMessage onInvocation(String procedure, WampInvocationMessage msg) {
            return WampMessageFactory.createYield(msg.getRequestId(), new JSONObject());
        }

    }

    private static final String PROCEDURE = "com.myapp.myprocedure1";

    private WampTestCallee mCallee;
    private WampMockRouter mRouter;
    private WampMockPeer mCaller;

    @Override
    protected void setUp() throws Exception {
        mCallee = new WampTestCallee();
        mRouter = new WampMockRouter();
        mCaller = new WampMockPeer();
        mCallee.connect(mRouter);
        mCaller.connect(mRouter);
    }

    public void testCtor() {
        assertNotNull(mCallee);
        assertNotNull(mRouter);
    }

    public void testRegister() {
        WampTestUtil.broadcastHelloSuccess(mCallee);
        WampTestUtil.broadcastRegisterSuccess(mCallee, PROCEDURE, mRouter);
    }

    public void testUnregister() {
        WampTestUtil.broadcastHelloSuccess(mCallee);
        WampMessage msg = WampTestUtil.broadcastRegister(mCallee, PROCEDURE, mRouter);
        assertTrue(msg.isRegisteredMessage());
        WampTestUtil.broadcastUnregisterSuccess(mCallee, msg.asRegisteredMessage(), mRouter);
    }

    public void testInvocation() {
        WampTestUtil.broadcastHelloSuccess(mCallee);
        WampMessage msg = WampTestUtil.broadcastRegister(mCallee, PROCEDURE, mRouter);
        assertTrue(msg.isRegisteredMessage());
        WampTestUtil.broadcastInvocationSuccess(mCallee, msg.asRegisteredMessage()
                .getRegistrationId(), mRouter);
    }
}
