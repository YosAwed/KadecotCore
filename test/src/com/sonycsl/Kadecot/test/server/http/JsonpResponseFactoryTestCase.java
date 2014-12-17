/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.test.server.http;

import com.sonycsl.Kadecot.server.http.mock.MockHTTPSession;
import com.sonycsl.Kadecot.server.http.response.JsonpReponseFactory;
import com.sonycsl.Kadecot.wamp.client.KadecotAppClientWrapper;
import com.sonycsl.Kadecot.wamp.client.KadecotAppClientWrapper.WampWelcomeListener;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.mock.MockWampPeer;
import com.sonycsl.wamp.util.TestableCallback;

import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class JsonpResponseFactoryTestCase extends TestCase {

    private MockWampPeer mMock;
    private JsonpReponseFactory mFactory;
    private KadecotAppClientWrapper mClient;

    private static final String SERVICE_VERSION = "/v1";
    private static final String SLASH_DEVICES = "/devices";

    protected void setUp() throws Exception {
        mMock = new MockWampPeer();
        mClient = new KadecotAppClientWrapper();

        mClient.connect(mMock);
        mFactory = new JsonpReponseFactory(mClient);
    }

    public void testCtor() {
        assertNotNull(mMock);
        assertNotNull(mClient);
        assertNotNull(mFactory);
    }

    public void testDeviceList() {
        final CountDownLatch welcomeLatch = new CountDownLatch(1);
        mClient.hello("realm", new WampWelcomeListener() {

            @Override
            public void onWelcome(int session, JSONObject details) {
                welcomeLatch.countDown();
            }
        });

        try {
            assertTrue(welcomeLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
        final TestableCallback callback = new TestableCallback();
        callback.setTargetMessageType(WampMessageType.CALL, new CountDownLatch(1));
        mMock.setCallback(callback);

        JSONObject json = null;

        try {
            JSONArray array = new JSONArray().put(new JSONObject().put("protocol", "scalarwebapi")
                    .put("deviceId", 1));
            json = new JSONObject().put("deviceList", array);
        } catch (JSONException e1) {
            fail();
        }

        final String deviceListStr = json.toString();

        final CountDownLatch deviceListLatch = new CountDownLatch(1);
        new Thread(new Runnable() {

            @Override
            public void run() {
                MockHTTPSession session = new MockHTTPSession(Method.GET, "/jsonp"
                        + SERVICE_VERSION + SLASH_DEVICES);
                Response response = mFactory.create(session, "/jsonp");
                try {
                    assertEquals(deviceListStr, inputStreamToString(response.getData()));
                } catch (IOException e) {
                    fail();
                }

                deviceListLatch.countDown();
            }
        }).start();

        try {
            TestCase.assertTrue(callback.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        mMock.transmit(WampMessageFactory.createResult(callback.getTargetMessage().asCallMessage()
                .getRequestId(), new JSONObject(), new JSONArray(), json));

        try {
            TestCase.assertTrue(deviceListLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

    }

    public void testJsonp() {
        final CountDownLatch welcomeLatch = new CountDownLatch(1);
        mClient.hello("realm", new WampWelcomeListener() {

            @Override
            public void onWelcome(int session, JSONObject details) {
                welcomeLatch.countDown();
            }
        });

        try {
            assertTrue(welcomeLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
        final TestableCallback callback = new TestableCallback();
        callback.setTargetMessageType(WampMessageType.CALL, new CountDownLatch(1));
        mMock.setCallback(callback);

        JSONObject json = null;

        try {
            JSONArray array = new JSONArray().put(new JSONObject().put("protocol", "scalarwebapi")
                    .put("deviceId", 1));
            json = new JSONObject().put("deviceList", array);
        } catch (JSONException e1) {
            fail();
        }

        final String deviceListStr = json.toString();

        final CountDownLatch deviceListLatch = new CountDownLatch(1);
        new Thread(new Runnable() {

            @Override
            public void run() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("callback", "callback");
                MockHTTPSession session = new MockHTTPSession(Method.GET, "/jsonp"
                        + SERVICE_VERSION + SLASH_DEVICES);
                session.setParams(params);
                Response response = mFactory.create(session, "/jsonp");
                try {
                    assertEquals("callback(" + deviceListStr + ")",
                            inputStreamToString(response.getData()));
                } catch (IOException e) {
                    fail();
                }

                deviceListLatch.countDown();
            }
        }).start();

        try {
            TestCase.assertTrue(callback.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        mMock.transmit(WampMessageFactory.createResult(callback.getTargetMessage().asCallMessage()
                .getRequestId(), new JSONObject(), new JSONArray(), json));

        try {
            TestCase.assertTrue(deviceListLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }
    }

    public void testGetProcedureList() {
        final CountDownLatch welcomeLatch = new CountDownLatch(1);
        mClient.hello("realm", new WampWelcomeListener() {

            @Override
            public void onWelcome(int session, JSONObject details) {
                welcomeLatch.countDown();
            }
        });

        try {
            assertTrue(welcomeLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
        final TestableCallback callback = new TestableCallback();
        callback.setTargetMessageType(WampMessageType.CALL, new CountDownLatch(1));
        mMock.setCallback(callback);

        JSONObject deviceListJson = null;

        try {
            JSONArray array = new JSONArray().put(new JSONObject().put("protocol", "scalarwebapi")
                    .put("deviceId", 1));
            deviceListJson = new JSONObject().put("deviceList", array);
        } catch (JSONException e1) {
            fail();
        }

        JSONObject procListJson = null;
        try {
            JSONArray array = new JSONArray()
                    .put(new JSONObject().put("procedure",
                            "com.sonycsl.kadecot.echonetlite.procedure.get").put("description", ""))
                    .put(new JSONObject().put("procedure",
                            "com.sonycsl.kadecot.echonetlite.procedure.set").put("description", ""));
            procListJson = new JSONObject().put("procedureList", array);
        } catch (JSONException e1) {
            fail();
        }

        final CountDownLatch procListLatch = new CountDownLatch(1);
        new Thread(new Runnable() {

            @Override
            public void run() {
                MockHTTPSession session = new MockHTTPSession(Method.GET, "/jsonp"
                        + SERVICE_VERSION + SLASH_DEVICES + "/1");
                Response response = mFactory.create(session, "/jsonp");
                JSONObject procListJson = null;
                try {
                    JSONArray array = new JSONArray()
                            .put(new JSONObject().put("procedure", "get").put("description", ""))
                            .put(new JSONObject().put("procedure", "set").put("description", ""));
                    procListJson = new JSONObject().put("procedureList", array);
                } catch (JSONException e1) {
                    fail();
                }
                try {
                    assertEquals(procListJson.toString(), inputStreamToString(response.getData()));
                } catch (IOException e) {
                    fail();
                }

                procListLatch.countDown();
            }
        }).start();

        try {
            TestCase.assertTrue(callback.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        mMock.transmit(WampMessageFactory.createResult(callback.getTargetMessage().asCallMessage()
                .getRequestId(), new JSONObject(), new JSONArray(), deviceListJson));

        callback.setTargetMessageType(WampMessageType.CALL, new CountDownLatch(1));
        mMock.setCallback(callback);

        try {
            TestCase.assertTrue(callback.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        mMock.transmit(WampMessageFactory.createResult(callback.getTargetMessage().asCallMessage()
                .getRequestId(), new JSONObject(), new JSONArray(), procListJson));

        try {
            TestCase.assertTrue(procListLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

    }

    public void testCallProcedure() {
        final CountDownLatch welcomeLatch = new CountDownLatch(1);
        mClient.hello("realm", new WampWelcomeListener() {

            @Override
            public void onWelcome(int session, JSONObject details) {
                welcomeLatch.countDown();
            }
        });

        try {
            assertTrue(welcomeLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
        final TestableCallback callback = new TestableCallback();
        callback.setTargetMessageType(WampMessageType.CALL, new CountDownLatch(1));
        mMock.setCallback(callback);

        JSONObject deviceListJson = null;

        try {
            JSONArray array = new JSONArray().put(new JSONObject().put("protocol", "scalarwebapi")
                    .put("deviceId", 1));
            deviceListJson = new JSONObject().put("deviceList", array);
        } catch (JSONException e1) {
            fail();
        }

        final String procedure = "appControl";
        JSONObject paramsJson = null;
        try {
            paramsJson = new JSONObject().put("test", "test");
        } catch (JSONException e1) {
            fail();
        }

        final String paramsJsonStr = paramsJson.toString();

        JSONObject result = null;
        try {
            result = new JSONObject().put("result", "result");
        } catch (JSONException e1) {
            fail();
        }

        final String resultStr = result.toString();

        final CountDownLatch procListLatch = new CountDownLatch(1);
        new Thread(new Runnable() {

            @Override
            public void run() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("procedure", procedure);
                params.put("params", paramsJsonStr);
                MockHTTPSession session = new MockHTTPSession(Method.GET, "/jsonp"
                        + SERVICE_VERSION + SLASH_DEVICES + "/1");
                session.setParams(params);
                Response response = mFactory.create(session, "/jsonp");
                try {
                    assertEquals(resultStr, inputStreamToString(response.getData()));
                } catch (IOException e) {
                    fail();
                }

                procListLatch.countDown();
            }
        }).start();

        try {
            TestCase.assertTrue(callback.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        mMock.transmit(WampMessageFactory.createResult(callback.getTargetMessage().asCallMessage()
                .getRequestId(), new JSONObject(), new JSONArray(), deviceListJson));

        callback.setTargetMessageType(WampMessageType.CALL, new CountDownLatch(1));
        mMock.setCallback(callback);

        try {
            TestCase.assertTrue(callback.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

        mMock.transmit(WampMessageFactory.createResult(callback.getTargetMessage().asCallMessage()
                .getRequestId(), new JSONObject(), new JSONArray(), result));

        try {
            TestCase.assertTrue(procListLatch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            TestCase.fail();
        }

    }

    private static String inputStreamToString(InputStream in) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        StringBuffer buf = new StringBuffer();
        String str;
        while ((str = br.readLine()) != null) {
            buf.append(str);
        }
        return buf.toString();
    }

}
