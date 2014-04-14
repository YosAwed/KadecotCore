
package com.sonycsl.test.Kadecot.wamp.mock;

import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;

import junit.framework.TestCase;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.framing.Framedata;
import org.json.JSONArray;
import org.json.JSONException;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MockWebSocket implements WebSocket {

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
            TestCase.fail();
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
