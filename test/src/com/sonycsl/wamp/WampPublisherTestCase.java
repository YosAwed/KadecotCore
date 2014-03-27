
package com.sonycsl.wamp;

import com.sonycsl.wamp.mock.WampMockRouter;

import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WampPublisherTestCase extends TestCase {

    private static final class WampTestPublisher extends WampPublisher implements WampTest {

        private CountDownLatch mLatch;
        private WampMessage mMsg;

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
            return mMsg;
        }

        @Override
        protected void onConsumed(WampMessage msg) {
            mMsg = msg;
            if (mLatch != null) {
                mLatch.countDown();
            }
        }
    }

    private static final String TOPIC = "com.myapp.mytopic1";
    private WampTestPublisher mPublisher;
    private WampMockRouter mRouter;

    @Override
    protected void setUp() throws Exception {
        mPublisher = new WampTestPublisher();
        mRouter = new WampMockRouter();
        mPublisher.connect(mRouter);
    }

    public void testPublish() {
        WampTestUtil.broadcastHelloSuccess(mPublisher);
        WampTestUtil.broadcastPublishSuccess(mPublisher, TOPIC);
    }

}
