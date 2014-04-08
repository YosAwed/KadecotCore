
package com.sonycsl.test.Kadecot.wamp;

import com.sonycsl.Kadecot.wamp.KadecotWampCaller;
import com.sonycsl.test.wamp.mock.WampMockRouter;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampMessageFactory;
import com.sonycsl.wamp.WampResultMessage;

import junit.framework.TestCase;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.framing.Framedata;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KadecotWampCallerTestCase extends TestCase {

    private static final String TEST_REALM = "realm";
    private static final int REQUEST_ID = 999;
    private static final String GET_DEVICE_LIST = "com.sonycsl.kadecot.procedure.getdevicelist";

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
        broadcastHelloSuccess(mCaller, mRouter, mWebSocket);
    }

    public void testGoodbye() {
        broadcastHelloSuccess(mCaller, mRouter, mWebSocket);
        broadcastGoodbyeSuccess(mCaller, mRouter, mWebSocket);
    }

    public void testCall() {
        broadcastHelloSuccess(mCaller, mRouter, mWebSocket);
        broadcastCallSuccess(mCaller, mRouter, mWebSocket);
        broadcastGoodbyeSuccess(mCaller, mRouter, mWebSocket);
    }

    private static WampMessage broadcastHello(KadecotWampCaller caller, WampMockRouter router,
            MockWebSocket webSocket) {

        router.setCountDownLatch(new CountDownLatch(1));
        webSocket.setCountDownLatch(new CountDownLatch(1));

        caller.broadcast(WampMessageFactory.createHello(TEST_REALM, new JSONObject()));

        try {
            assertTrue(router.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
        assertTrue(router.getMessage().isHelloMessage());

        try {
            assertTrue(webSocket.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
        return webSocket.getMessage();
    }

    private static void broadcastHelloSuccess(KadecotWampCaller caller, WampMockRouter router,
            MockWebSocket webSocket) {
        assertTrue(broadcastHello(caller, router, webSocket).isWelcomeMessage());
    }

    private static WampMessage broadcastGoodbye(KadecotWampCaller caller, WampMockRouter router,
            MockWebSocket webSocket) {

        router.setCountDownLatch(new CountDownLatch(1));
        webSocket.setCountDownLatch(new CountDownLatch(1));

        router.broadcast(WampMessageFactory.createGoodbye(new JSONObject(), WampError.CLOSE_REALM));

        try {
            assertTrue(webSocket.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        WampMessage msg = webSocket.getMessage();
        assertTrue(msg.toString(), msg.isGoodbyeMessage());
        assertEquals(WampError.CLOSE_REALM, msg.asGoodbyeMessage().getReason());

        try {
            assertTrue(router.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        return router.getMessage();
    }

    private static void broadcastGoodbyeSuccess(KadecotWampCaller caller,
            WampMockRouter router, MockWebSocket webSocket) {
        WampMessage msg = broadcastGoodbye(caller, router, webSocket);
        assertTrue(msg.isGoodbyeMessage());
        assertEquals(WampError.GOODBYE_AND_OUT, msg.asGoodbyeMessage().getReason());
    }

    private static WampMessage broadcastCall(KadecotWampCaller caller, WampMockRouter router,
            MockWebSocket webSocket) {

        router.setCountDownLatch(new CountDownLatch(1));
        webSocket.setCountDownLatch(new CountDownLatch(1));

        caller.broadcast(WampMessageFactory.createCall(REQUEST_ID, new JSONObject(),
                GET_DEVICE_LIST));

        try {
            assertTrue(router.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
        assertTrue(router.getMessage().isCallMessage());

        try {
            assertTrue(webSocket.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
        return webSocket.getMessage();
    }

    private static void broadcastCallSuccess(KadecotWampCaller caller, WampMockRouter router,
            MockWebSocket webSocket) {
        WampMessage msg = broadcastCall(caller, router, webSocket);
        assertTrue(msg.isResultMessage());
        WampResultMessage result = msg.asResultMessage();
        assertEquals(REQUEST_ID, result.getRequestId());
    }

    private static class MockWebSocket implements WebSocket {

        private CountDownLatch mLatch;
        private WampMessage mMessage;

        public MockWebSocket() {

        }

        public void setCountDownLatch(CountDownLatch latch) {
            mLatch = latch;
        }

        public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            return mLatch.await(timeout, unit);
        }

        public WampMessage getMessage() {
            return mMessage;
        }

        @Override
        public void close() {
        }

        @Override
        public void close(int arg0) {
        }

        @Override
        public void close(int arg0, String arg1) {
        }

        @Override
        public void closeConnection(int arg0, String arg1) {
        }

        @Override
        public Draft getDraft() {
            return null;
        }

        @Override
        public InetSocketAddress getLocalSocketAddress() {
            return null;
        }

        @Override
        public READYSTATE getReadyState() {
            return null;
        }

        @Override
        public InetSocketAddress getRemoteSocketAddress() {
            return null;
        }

        @Override
        public boolean hasBufferedData() {
            return false;
        }

        @Override
        public boolean isClosed() {
            return false;
        }

        @Override
        public boolean isClosing() {
            return false;
        }

        @Override
        public boolean isConnecting() {
            return false;
        }

        @Override
        public boolean isFlushAndClose() {
            return false;
        }

        @Override
        public boolean isOpen() {
            return false;
        }

        @Override
        public void send(String arg0) throws NotYetConnectedException {
            try {
                mMessage = WampMessageFactory.create(new JSONArray(arg0));
            } catch (JSONException e) {
                fail();
            }

            if (mLatch != null) {
                mLatch.countDown();
            }
        }

        @Override
        public void send(ByteBuffer arg0) throws IllegalArgumentException, NotYetConnectedException {
        }

        @Override
        public void send(byte[] arg0) throws IllegalArgumentException, NotYetConnectedException {
        }

        @Override
        public void sendFrame(Framedata arg0) {
        }

    }
}
