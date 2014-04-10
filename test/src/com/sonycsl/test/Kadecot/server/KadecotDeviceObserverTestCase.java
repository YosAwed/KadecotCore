
package com.sonycsl.test.Kadecot.server;

import com.sonycsl.Kadecot.wamp.KadecotDeviceInfo;
import com.sonycsl.Kadecot.wamp.KadecotDeviceObserver;
import com.sonycsl.Kadecot.wamp.KadecotWampBroker;
import com.sonycsl.Kadecot.wamp.KadecotWampDealer;
import com.sonycsl.Kadecot.wamp.KadecotWampTopic;
import com.sonycsl.test.wamp.WampTest;
import com.sonycsl.test.wamp.mock.WampMockPeer;
import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampMessageFactory;
import com.sonycsl.wamp.WampMessageType;
import com.sonycsl.wamp.WampResultMessage;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class KadecotDeviceObserverTestCase extends TestCase {

    private WampMockPeer mDevicePublisher;
    private WampMockPeer mCaller;
    private KadecotWampDealer mRouter;

    KadecotDeviceObserver mDeviceObserver;

    @Override
    protected void setUp() {
        mDevicePublisher = new WampMockPeer();
        mCaller = new WampMockPeer();

        mRouter = new KadecotWampDealer(new KadecotWampBroker());

        mDevicePublisher.connect(mRouter);
        mCaller.connect(mRouter);

        mDeviceObserver = new KadecotDeviceObserver(mRouter);
    }

    public void testBroadcastDevice() {
        mDeviceObserver.start();
        broadcastPublishDevice(mDevicePublisher, KadecotWampTopic.TOPIC_PRIVATE_DEVICE,
                createDeviceJson());
        WampMessage msg = broadcastCallDeviceList(mCaller,
                KadecotDeviceObserver.DEVICE_LIST_PROCEDURE);
        assertEquals(WampMessageType.RESULT, msg.getMessageType());

        if (msg.isResultMessage()) {
            WampResultMessage resultMsg = msg.asResultMessage();
            JSONArray expectedDeviceList = new JSONArray().put(createDeviceJson());
            assertEquals(expectedDeviceList.toString(), resultMsg.getArguments().toString());
        }
        mDeviceObserver.stop();
    }

    private static WampMessage broadcastPublishDevice(WampTest publisher, String topic,
            JSONObject device) {
        publisher.setCountDownLatch(new CountDownLatch(1));

        WampMessage msg = WampMessageFactory.createPublish(1, new JSONObject(), topic,
                new JSONArray(), device);
        publisher.broadcast(msg);

        try {
            TestCase.assertTrue(publisher.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        return publisher.getMessage();
    }

    private static JSONObject createDeviceJson() {
        try {
            return new JSONObject().put(KadecotDeviceInfo.DEVICE_STATUS_KEY, 1)
                    .put(KadecotDeviceInfo.DEVICE_PROTOCOL_KEY, "")
                    .put(KadecotDeviceInfo.DEVICE_DEVICENAME_KEY, "")
                    .put(KadecotDeviceInfo.DEVICE_NICKNAME_KEY, "testNickname")
                    .put(KadecotDeviceInfo.DEVICE_PARENT_KEY, "")
                    .put(KadecotDeviceInfo.DEVICE_DEVICETYPE_KEY, "");
        } catch (JSONException e) {
            return null;
        }
    }

    private static WampMessage broadcastCallDeviceList(WampTest caller, String procedure) {
        caller.setCountDownLatch(new CountDownLatch(1));

        caller.broadcast(WampMessageFactory.createCall(1, new JSONObject(),
                procedure));

        try {
            TestCase.assertTrue(caller.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        return caller.getMessage();
    }
}
