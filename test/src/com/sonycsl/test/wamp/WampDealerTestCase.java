
package com.sonycsl.test.wamp;

import com.sonycsl.test.wamp.mock.WampMockCalleePeer;
import com.sonycsl.test.wamp.mock.WampMockPeer;
import com.sonycsl.wamp.WampDealer;
import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampRegisteredMessage;

import junit.framework.TestCase;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WampDealerTestCase extends TestCase {
    private static class TestWampDealer extends WampDealer implements WampTest {

        private CountDownLatch mLatch;

        private Queue<WampMessage> mMessageQueue = new LinkedList<WampMessage>();

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

    }

    private TestWampDealer mDealer;
    private WampMockPeer mFriendPeer1;
    private WampMockPeer mFriendPeer2;
    private WampMockCalleePeer mFriendPeer3;

    private static final String TEST_PROCEDURE = "test.procedure";

    @Override
    protected void setUp() {
        mDealer = new TestWampDealer();
        mFriendPeer1 = new WampMockPeer();
        mFriendPeer2 = new WampMockPeer();
        mFriendPeer3 = new WampMockCalleePeer();
        mDealer.connect(mFriendPeer1);
        mDealer.connect(mFriendPeer2);
        mDealer.connect(mFriendPeer3);
    }

    public void testCtor() {
        assertNotNull(mDealer);
        assertNotNull(mFriendPeer1);
        assertNotNull(mFriendPeer2);
        assertNotNull(mFriendPeer3);
    }

    public void testRegister() {
        WampTestUtil.broadcastHelloSuccess(mFriendPeer1);
        WampTestUtil.broadcastRegisterSuccess(mFriendPeer1, TEST_PROCEDURE, mDealer);
    }

    // public void testRegisterWithoutHello() {
    // assertTrue(WampTestUtil.broadcastRegister(mFriendPeer1, TEST_PROCEDURE,
    // mDealer)
    // .isErrorMessage());
    // }

    public void testRegisterWithTwoClient() {
        WampTestUtil.broadcastHelloSuccess(mFriendPeer1);
        WampTestUtil.broadcastHelloSuccess(mFriendPeer2);
        WampTestUtil.broadcastRegisterSuccess(mFriendPeer1, TEST_PROCEDURE, mDealer);
        WampTestUtil.broadcastRegisterSuccess(mFriendPeer2, TEST_PROCEDURE, mDealer);
    }

    public void testUnregister() {
        WampTestUtil.broadcastHelloSuccess(mFriendPeer1);
        WampTestUtil.broadcastHelloSuccess(mFriendPeer2);
        WampMessage msg = WampTestUtil.broadcastRegister(mFriendPeer1, TEST_PROCEDURE, mDealer);
        assertTrue(msg.isRegisteredMessage());

        WampRegisteredMessage registeredMsg = msg.asRegisteredMessage();

        assertTrue(WampTestUtil.broadcastUnregister(mFriendPeer2, registeredMsg, mDealer)
                .isErrorMessage());
        WampTestUtil.broadcastUnregisterSuccess(mFriendPeer1, registeredMsg, mDealer);
    }

    public void testCall() {
        WampTestUtil.broadcastHelloSuccess(mFriendPeer1);
        WampTestUtil.broadcastHelloSuccess(mFriendPeer3);
        WampTestUtil.broadcastRegisterSuccess(mFriendPeer3, TEST_PROCEDURE, mDealer);
        WampTestUtil.broadcastCallSuccess(mFriendPeer1, TEST_PROCEDURE, mDealer, mFriendPeer3);
    }
}
