
package com.sonycsl.test.wamp.message.impl;

import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.impl.WampAbstractMessage;

import junit.framework.TestCase;

import org.json.JSONArray;

public class WampAbstractMessageTestCase extends TestCase {
    private class WampBaseMessage extends WampAbstractMessage {
        public WampBaseMessage(JSONArray msg) {
            super(msg);
        }
    }

    public void testCtor() {
        JSONArray json = new JSONArray();
        json.put(WampMessageType.HELLO);
        WampMessage msg = new WampBaseMessage(json);
        assertNotNull(msg);
    }

    public void testIsMethods() {
        JSONArray json = new JSONArray();
        json.put(WampMessageType.HELLO);
        WampMessage msg = new WampBaseMessage(json);
        assertFalse(msg.isHelloMessage());
        assertFalse(msg.isWelcomeMessage());
        assertFalse(msg.isAbortMessage());
        assertFalse(msg.isChallengeMessage());
        assertFalse(msg.isAuthenticateMessage());
        assertFalse(msg.isGoodbyeMessage());
        assertFalse(msg.isHeartbeatMessage());
        assertFalse(msg.isErrorMessage());
        assertFalse(msg.isPublishMessage());
        assertFalse(msg.isPublishedMessage());
        assertFalse(msg.isSubscribeMessage());
        assertFalse(msg.isSubscribedMessage());
        assertFalse(msg.isUnsubscribeMessage());
        assertFalse(msg.isUnsubscribedMessage());
        assertFalse(msg.isEventMessage());
        assertFalse(msg.isCallMessage());
        assertFalse(msg.isCancelMessage());
        assertFalse(msg.isResultMessage());
        assertFalse(msg.isRegisterMessage());
        assertFalse(msg.isRegisteredMessage());
        assertFalse(msg.isUnregisterMessage());
        assertFalse(msg.isUnregisteredMessage());
        assertFalse(msg.isInvocationMessage());
        assertFalse(msg.isInterruptMessage());
        assertFalse(msg.isYieldMessage());
    }

    public void testAsMethods() {
        JSONArray json = new JSONArray();
        json.put(WampMessageType.HELLO);
        WampMessage msg = new WampBaseMessage(json);
        assertNull(msg.asHelloMessage());
        assertNull(msg.asWelcomeMessage());
        assertNull(msg.asAbortMessage());
        assertNull(msg.asGoodbyeMessage());
        assertNull(msg.asErrorMessage());
        assertNull(msg.asPublishMessage());
        assertNull(msg.asPublishedMessage());
        assertNull(msg.asSubscribeMessage());
        assertNull(msg.asSubscribedMessage());
        assertNull(msg.asUnsubscribeMessage());
        assertNull(msg.asUnsubscribedMessage());
        assertNull(msg.asEventMessage());
        assertNull(msg.asCallMessage());
        assertNull(msg.asResultMessage());
        assertNull(msg.asRegisterMessage());
        assertNull(msg.asRegisteredMessage());
        assertNull(msg.asUnregisterMessage());
        assertNull(msg.asUnregisteredMessage());
        assertNull(msg.asInvocationMessage());
        assertNull(msg.asYieldMessage());
    }
}
