
package com.sonycsl.test.Kadecot.wamp;

import com.sonycsl.Kadecot.wamp.KadecotWampCaller;
import com.sonycsl.test.Kadecot.wamp.mock.MockWebSocket;
import com.sonycsl.test.wamp.mock.WampMockRouter;

import junit.framework.TestCase;

public class KadecotWampCallerTestCase extends TestCase {

    private KadecotWampCaller mCaller;
    private MockWebSocket mWebSocket;
    private WampMockRouter mRouter;

    @Override
    protected void setUp() throws Exception {
        mWebSocket = new MockWebSocket();
        mCaller = new KadecotWampCaller(mWebSocket);
        assertNotNull(mWebSocket);
        mRouter = new WampMockRouter();
        assertNotNull(mRouter);
        mCaller.connect(mRouter);
    }

    public void testCtor() {
        assertNotNull(mCaller);
    }

    public void testHello() {
        KadecotWampClientTestUtil.broadcastHelloSuccess(mCaller, mRouter, mWebSocket);
    }

    public void testGoodbye() {
        KadecotWampClientTestUtil.broadcastHelloSuccess(mCaller, mRouter, mWebSocket);
        KadecotWampClientTestUtil.broadcastGoodbyeSuccess(mCaller, mRouter, mWebSocket);
    }

    public void testCall() {
        KadecotWampClientTestUtil.broadcastHelloSuccess(mCaller, mRouter, mWebSocket);
        KadecotWampClientTestUtil.broadcastCallSuccess(mCaller, mRouter, mWebSocket);
        KadecotWampClientTestUtil.broadcastGoodbyeSuccess(mCaller, mRouter, mWebSocket);
    }
}
