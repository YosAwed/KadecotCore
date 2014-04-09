
package com.sonycsl.test.Kadecot.server;

import com.sonycsl.Kadecot.wamp.KadecotDeviceInfo;
import com.sonycsl.Kadecot.wamp.KadecotDeviceObserver;
import com.sonycsl.Kadecot.wamp.KadecotWampTopic;
import com.sonycsl.test.wamp.WampTest;
import com.sonycsl.test.wamp.mock.WampMockBrokerPeer;
import com.sonycsl.test.wamp.mock.WampMockDealerPeer;
import com.sonycsl.test.wamp.mock.WampMockPeer;
import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampMessageFactory;
import com.sonycsl.wamp.WampRouter;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KadecotDeviceObserverTestCase extends TestCase {

    private WampMockPeer mClient;
    private WampRouter mRouter;
    private KadecotDeviceObserver mDeviceObserver;

    @Override
    protected void setUp() {
        mClient = new WampMockPeer();
        mRouter = new WampMockDealerPeer(new WampMockBrokerPeer());
        mDeviceObserver = new KadecotDeviceObserver(mRouter);
    }

    public void testBroadcastDevice() {
        broadcastPublishDevice(mClient, KadecotWampTopic.TOPIC_PRIVATE_DEVICE);
    }

    private static WampMessage broadcastPublishDevice(WampTest publisher, String topic) {
        publisher.setCountDownLatch(new CountDownLatch(1));

        try {
            JSONObject device = new JSONObject().put(KadecotDeviceInfo.DEVICE_STATUS_KEY, 1)
                    .put(KadecotDeviceInfo.DEVICE_PROTOCOL_KEY, "")
                    .put(KadecotDeviceInfo.DEVICE_DEVICENAME_KEY, "")
                    .put(KadecotDeviceInfo.DEVICE_NICKNAME_KEY, "testNickname")
                    .put(KadecotDeviceInfo.DEVICE_PARENT_KEY, "")
                    .put(KadecotDeviceInfo.DEVICE_DEVICETYPE_KEY, "");

            WampMessage msg = WampMessageFactory.createPublish(1, new JSONObject(), topic,
                    new JSONArray(), device);
            publisher.broadcast(msg);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        try {
            TestCase.assertTrue(publisher.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        return publisher.getMessage();

    }
}
