
package com.sonycsl.test.wamp.message;

import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.message.WampAbortMessage;
import com.sonycsl.wamp.message.WampErrorMessage;
import com.sonycsl.wamp.message.WampGoodbyeMessage;
import com.sonycsl.wamp.message.WampHelloMessage;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.WampWelcomeMessage;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WampMessageFactoryTestCase extends TestCase {
    private static final String REALM = "realm";
    private static final String TOPIC = "topic.test";
    private static final String PROCEDURE = "procedure.test";

    public void testCtor() {
        assertNotNull(new WampMessageFactory());
    }

    public void testCreate() {
        try {
            // create hello message
            JSONArray json = new JSONArray();
            json.put(1);
            json.put(REALM);
            JSONObject role = new JSONObject("{\"roles\":{\"caller\":{}}}");
            json.put(role);
            WampMessage msg = WampMessageFactory.create(json);

            assertTrue(msg.isHelloMessage());

            WampHelloMessage hello = msg.asHelloMessage();

            // check content
            assertTrue(hello.getRealm().equals(REALM));
            assertTrue(hello.getDetails() == role);
        } catch (JSONException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testCreateAbnormal() {
        // no message type
        try {
            JSONArray json = new JSONArray();
            WampMessage msg = WampMessageFactory.create(json);
        } catch (IllegalArgumentException e) {
            return;
        }
        fail();
    }

    public void testCreateHello() {
        try {
            JSONObject role = new JSONObject("{\"roles\":{\"caller\":{}}}");
            WampMessage msg = WampMessageFactory.createHello(REALM, role);

            assertTrue(msg.isHelloMessage());

            WampHelloMessage hello = msg.asHelloMessage();
            assertTrue(hello.getRealm().equals(REALM));
            assertTrue(hello.getDetails() == role);
        } catch (JSONException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testCreateHelloAbnormal() {
        boolean flag = false;

        // details null check
        try {
            WampMessage msg = WampMessageFactory.createHello(REALM, null);
        } catch (IllegalArgumentException e) {
            flag = true;
        }
        if (!flag) {
            fail();
        }

        flag = false;
        // realm null check
        try {
            JSONObject role = new JSONObject("{\"roles\":{\"caller\":{}}}");
            WampMessage msg = WampMessageFactory.createHello(null, role);
        } catch (IllegalArgumentException e) {
            flag = true;
        } catch (JSONException e) {
            e.printStackTrace();
            fail();
        }

        if (!flag) {
            fail();
        }
    }

    public void testCreateWelcome() {
        try {
            JSONObject role = new JSONObject("{\"roles\":{\"dealer\":{}}}");
            int sessionId = 1;
            WampMessage msg = WampMessageFactory.createWelcome(sessionId, role);

            assertTrue(msg.isWelcomeMessage());

            WampWelcomeMessage welcome = msg.asWelcomeMessage();
            assertTrue(welcome.getSession() == sessionId);
            assertTrue(welcome.getDetails() == role);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void testCreateWelcomeAbnormal() {
        // details null check
        try {
            int sessionId = 1;
            WampMessage msg = WampMessageFactory.createWelcome(sessionId, null);
        } catch (IllegalArgumentException e) {
            return;
        }
        fail();
    }

    public void testCreateAbort() {
        JSONObject details = new JSONObject();
        WampMessage msg = WampMessageFactory.createAbort(details, WampError.NO_SUCH_REALM);

        assertTrue(msg.isAbortMessage());

        WampAbortMessage abort = msg.asAbortMessage();
        assertTrue(abort.getDetails() == details);
        assertTrue(abort.getReason().equals(WampError.NO_SUCH_REALM));
    }

    public void testCreateAbortAbnormal() {
        boolean flag = false;
        // details null check
        try {
            WampMessage msg = WampMessageFactory.createAbort(null, WampError.NO_SUCH_REALM);
        } catch (IllegalArgumentException e) {
            flag = true;
        }
        if (!flag) {
            fail();
        }

        flag = false;
        // reason null check
        try {
            WampMessage msg = WampMessageFactory.createAbort(new JSONObject(), null);
        } catch (IllegalArgumentException e) {
            flag = true;
        }
        if (!flag) {
            fail();
        }
    }

    public void testCreateGoodbye() {
        JSONObject details = new JSONObject();
        WampMessage msg = WampMessageFactory.createGoodbye(details, WampError.GOODBYE_AND_OUT);

        assertTrue(msg.isGoodbyeMessage());

        WampGoodbyeMessage goodbye = msg.asGoodbyeMessage();
        assertTrue(goodbye.getDetails() == details);
        assertTrue(goodbye.getReason().equals(WampError.GOODBYE_AND_OUT));
    }

    public void testCreateGoodbyeAbnormal() {
        boolean flag = false;
        // details null check
        try {
            WampMessage msg = WampMessageFactory.createGoodbye(null, WampError.GOODBYE_AND_OUT);
        } catch (IllegalArgumentException e) {
            flag = true;
        }
        if (!flag) {
            fail();
        }

        flag = false;
        // reason null check
        try {
            WampMessage msg = WampMessageFactory.createAbort(new JSONObject(), null);
        } catch (IllegalArgumentException e) {
            flag = true;
        }
        if (!flag) {
            fail();
        }
    }

    public void testCreateError() {
        int requestType = WampMessageType.CALL;
        int requestId = 1;
        JSONObject details = new JSONObject();
        String error = WampError.NO_SUCH_PROCEDURE;
        WampMessage msg;
        WampErrorMessage errorMsg;

        // no arguments and argumentsKw
        msg = WampMessageFactory.createError(requestType, requestId, details, error);

        assertTrue(msg.isErrorMessage());

        errorMsg = msg.asErrorMessage();
        assertTrue(errorMsg.getRequestType() == requestType);
        assertTrue(errorMsg.getRequestId() == requestId);
        assertTrue(errorMsg.getDetails() == details);
        assertTrue(errorMsg.getUri().equals(error));

        // arguments
        JSONArray arguments = new JSONArray();
        msg = WampMessageFactory.createError(requestType, requestId, details, error, arguments);

        assertTrue(msg.isErrorMessage());

        errorMsg = msg.asErrorMessage();
        assertTrue(errorMsg.getRequestType() == requestType);
        assertTrue(errorMsg.getRequestId() == requestId);
        assertTrue(errorMsg.getDetails() == details);
        assertTrue(errorMsg.getUri().equals(error));
        assertTrue(errorMsg.getArguments() == arguments);

        // argumentsKw
        JSONObject argumentsKw = new JSONObject();
        msg = WampMessageFactory.createError(requestType, requestId, details, error, arguments,
                argumentsKw);

        assertTrue(msg.isErrorMessage());

        errorMsg = msg.asErrorMessage();
        assertTrue(errorMsg.getRequestType() == requestType);
        assertTrue(errorMsg.getRequestId() == requestId);
        assertTrue(errorMsg.getDetails() == details);
        assertTrue(errorMsg.getUri().equals(error));
        assertTrue(errorMsg.getArguments() == arguments);
        assertTrue(errorMsg.getArgumentsKw() == argumentsKw);
    }

    public void testCreateErrorAbnormal() {
        boolean flag = false;
        int requestType = WampMessageType.CALL;
        String error = WampError.NO_SUCH_PROCEDURE;
        JSONObject options = new JSONObject();

        // options null
        try {
            WampMessage msg = WampMessageFactory.createError(requestType, 1, null, error);
        } catch (IllegalArgumentException e) {
            flag = true;
        }
        if (!flag) {
            fail();
        }

        // error null
        flag = false;
        try {
            WampMessage msg = WampMessageFactory.createError(requestType, 1, options, null);
        } catch (IllegalArgumentException e) {
            flag = true;
        }
        if (!flag) {
            fail();
        }

        // arguments null
        flag = false;
        try {
            WampMessage msg = WampMessageFactory.createError(requestType, 1, options, error, null);
        } catch (IllegalArgumentException e) {
            flag = true;
        }
        if (!flag) {
            fail();
        }

        // argumentsKw null
        flag = false;
        try {
            WampMessage msg = WampMessageFactory.createError(requestType, 1, options, error,
                    new JSONArray(), null);
        } catch (IllegalArgumentException e) {
            flag = true;
        }
        if (!flag) {
            fail();
        }
    }
}
