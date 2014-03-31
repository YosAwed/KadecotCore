
package com.sonycsl.wamp;

import com.sonycsl.wamp.mock.WampMockBrokerPeer;
import com.sonycsl.wamp.mock.WampMockPeer;

import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WampSubscriberTestCase extends TestCase {
    private static class WampTestSubscriber extends WampSubscriber implements WampTest {

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

        @Override
        public WampMessage getMessage() {
            return mMsg;
        }

        @Override
        protected void onBroadcast(WampMessage msg) {

        }

        @Override
        public void setConsumed(boolean isConsumed) {
        }

        @Override
        public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            return mLatch.await(timeout, unit);
        }

        @Override
        protected void subscribed(WampSubscribedMessage asSubscribedMessage) {
        }

        @Override
        protected void unsubscribed(WampUnsubscribedMessage asUnsubscribedMessage) {
        }

        @Override
        protected void event(WampEventMessage asUnsubscribedMessage) {
        }

    }

    private static final String TEST_TOPIC = "test.topic";

    private WampTestSubscriber mSubscriber;
    private WampMockPeer mPublisher;
    private WampMockBrokerPeer mRouter;

    @Override
    protected void setUp() throws Exception {
        mSubscriber = new WampTestSubscriber();
        mPublisher = new WampMockPeer();
        mRouter = new WampMockBrokerPeer();
        mSubscriber.connect(mRouter);
        mPublisher.connect(mRouter);
    }

    public void testCtor() {
        assertNotNull(mSubscriber);
        assertNotNull(mPublisher);
        assertNotNull(mRouter);
    }

    public void testSubscribe() {
        WampTestUtil.broadcastHelloSuccess(mSubscriber);
        WampTestUtil.broadcastHelloSuccess(mPublisher);
        WampTestUtil.broadcastSubscribeSuccess(mSubscriber, TEST_TOPIC);
        WampTestUtil.broadcastPublishSuccess(mPublisher, TEST_TOPIC, mSubscriber);
    }
}
