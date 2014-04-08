
package com.sonycsl.test.Kadecot.wamp;

import com.sonycsl.Kadecot.wamp.KadecotWampSubscriber;
import com.sonycsl.test.Kadecot.wamp.mock.MockWebSocket;
import com.sonycsl.test.wamp.mock.WampMockRouter;

import junit.framework.TestCase;

public class KadecotWampSubscriberTestCase extends TestCase {

    private KadecotWampSubscriber mSubscriber;
    private MockWebSocket mWebSocket;
    private WampMockRouter mRouter;

    @Override
    protected void setUp() throws Exception {
        mWebSocket = new MockWebSocket();
        mSubscriber = new KadecotWampSubscriber(mWebSocket);
        assertNotNull(mWebSocket);
        mRouter = new WampMockRouter();
        assertNotNull(mRouter);
        mSubscriber.connect(mRouter);
    }

    public void testCtor() {
        assertNotNull(mSubscriber);
    }

    public void testHello() {
        KadecotWampClientTestUtil.broadcastHelloSuccess(mSubscriber, mRouter, mWebSocket);
    }

    public void testSubscribe() {
        KadecotWampClientTestUtil.broadcastHelloSuccess(mSubscriber, mRouter, mWebSocket);
        KadecotWampClientTestUtil.broadcastSubscribeSuccess(mSubscriber, mRouter, mWebSocket);
        KadecotWampClientTestUtil.broadcastGoodbyeSuccess(mSubscriber, mRouter, mWebSocket);
    }

    public void testUnsubscribe() {
        KadecotWampClientTestUtil.broadcastHelloSuccess(mSubscriber, mRouter, mWebSocket);
        KadecotWampClientTestUtil.broadcastSubscribeSuccess(mSubscriber, mRouter, mWebSocket);
        KadecotWampClientTestUtil.broadcastUnsubscribeSuccess(mSubscriber, mRouter, mWebSocket);
        KadecotWampClientTestUtil.broadcastGoodbyeSuccess(mSubscriber, mRouter, mWebSocket);
    }

    public void testEvent() {
        KadecotWampClientTestUtil.broadcastHelloSuccess(mSubscriber, mRouter, mWebSocket);
        KadecotWampClientTestUtil.broadcastSubscribeSuccess(mSubscriber, mRouter, mWebSocket);
        KadecotWampClientTestUtil.broadcastEventSuccess(mSubscriber, mRouter, mWebSocket);
        KadecotWampClientTestUtil.broadcastUnsubscribeSuccess(mSubscriber, mRouter, mWebSocket);
        KadecotWampClientTestUtil.broadcastGoodbyeSuccess(mSubscriber, mRouter, mWebSocket);
    }
}
