
package com.sonycsl.test.Kadecot.wamp.echonetlite;

import com.sonycsl.Kadecot.wamp.echonetlite.ECHONETLiteManager;
import com.sonycsl.Kadecot.wamp.echonetlite.ECHONETLiteWampSubscriber;
import com.sonycsl.Kadecot.wamp.echonetlite.ECHONETLiteWampSubscriber.OnTopicListener;
import com.sonycsl.Kadecot.wamp.provider.KadecotProviderClient;
import com.sonycsl.test.mock.MockWampClient;
import com.sonycsl.wamp.message.WampMessageFactory;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ECHONETLiteWampSubscriberTestCase extends TestCase {

    public static final class LatchTopicListener implements OnTopicListener {

        private CountDownLatch mLatch;
        private String mLastTopic;

        @Override
        public void onTopicStopped(String topic) {
            mLastTopic = topic;
            mLatch.countDown();
        }

        @Override
        public void onTopicStarted(String topic) {
            mLastTopic = topic;
            mLatch.countDown();
        }

        public void setCountDownLatch(CountDownLatch latch) {
            mLatch = latch;
        }

        public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            return mLatch.await(timeout, unit);
        }

        public String getLastTopic() {
            return mLastTopic;
        }
    }

    private ECHONETLiteWampSubscriber mSubscriber;
    private MockWampClient mClient;
    private LatchTopicListener mListener;

    @Override
    protected void setUp() throws Exception {
        mListener = new LatchTopicListener();
        mSubscriber = new ECHONETLiteWampSubscriber(ECHONETLiteManager.getInstance(), mListener);
        mClient = new MockWampClient();
    }

    public void testCtor() {
        assertNotNull(mSubscriber);
    }

    public void testOnTopicStarted() {
        mSubscriber.resolveTxMessage(mClient, WampMessageFactory.createSubscribe(1,
                new JSONObject(), KadecotProviderClient.Topic.START.getUri()));
        mSubscriber.resolveRxMessage(mClient, WampMessageFactory.createSubscribed(1, 2), null);
        mListener.setCountDownLatch(new CountDownLatch(1));
        try {
            mSubscriber.resolveRxMessage(
                    mClient,
                    WampMessageFactory.createEvent(
                            2,
                            1,
                            new JSONObject(),
                            new JSONArray(),
                            new JSONObject().put("topic",
                                    KadecotProviderClient.Topic.START.getUri())),
                    null);
        } catch (JSONException e) {
            e.printStackTrace();
            fail();
        }

        try {
            assertTrue(mListener.await(1, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }

        assertEquals(KadecotProviderClient.Topic.START.getUri(), mListener.getLastTopic());
    }

    public void testOnTopicStopped() {
        mSubscriber.resolveTxMessage(mClient, WampMessageFactory.createSubscribe(1,
                new JSONObject(), KadecotProviderClient.Topic.STOP.getUri()));
        mSubscriber.resolveRxMessage(mClient, WampMessageFactory.createSubscribed(1, 2), null);
        mListener.setCountDownLatch(new CountDownLatch(1));
        try {
            mSubscriber.resolveRxMessage(
                    mClient,
                    WampMessageFactory.createEvent(
                            2,
                            1,
                            new JSONObject(),
                            new JSONArray(),
                            new JSONObject().put("topic",
                                    KadecotProviderClient.Topic.STOP.getUri())),
                    null);
        } catch (JSONException e) {
            e.printStackTrace();
            fail();
        }

        try {
            assertTrue(mListener.await(1, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }

        assertEquals(KadecotProviderClient.Topic.STOP.getUri(), mListener.getLastTopic());
    }

}
