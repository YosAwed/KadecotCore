
package com.sonycsl.wamp.message.impl;

import com.sonycsl.wamp.message.WampAbortMessage;
import com.sonycsl.wamp.message.WampCallMessage;
import com.sonycsl.wamp.message.WampErrorMessage;
import com.sonycsl.wamp.message.WampEventMessage;
import com.sonycsl.wamp.message.WampGoodbyeMessage;
import com.sonycsl.wamp.message.WampHelloMessage;
import com.sonycsl.wamp.message.WampInvocationMessage;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampPublishMessage;
import com.sonycsl.wamp.message.WampPublishedMessage;
import com.sonycsl.wamp.message.WampRegisterMessage;
import com.sonycsl.wamp.message.WampRegisteredMessage;
import com.sonycsl.wamp.message.WampResultMessage;
import com.sonycsl.wamp.message.WampSubscribeMessage;
import com.sonycsl.wamp.message.WampSubscribedMessage;
import com.sonycsl.wamp.message.WampUnregisterMessage;
import com.sonycsl.wamp.message.WampUnregisteredMessage;
import com.sonycsl.wamp.message.WampUnsubscribeMessage;
import com.sonycsl.wamp.message.WampUnsubscribedMessage;
import com.sonycsl.wamp.message.WampWelcomeMessage;
import com.sonycsl.wamp.message.WampYieldMessage;

import org.json.JSONArray;
import org.json.JSONException;

abstract public class WampAbstractMessage implements WampMessage {

    private static final int MESSAGE_TYPE_INDEX = 0;
    private final JSONArray mMsg;

    public WampAbstractMessage(JSONArray msg) {
        mMsg = msg;
    }

    protected final JSONArray toJSON() {
        return mMsg;
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }

    @Override
    public int getMessageType() {
        try {
            return mMsg.getInt(MESSAGE_TYPE_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no message type");
        }
    }

    @Override
    public boolean isHelloMessage() {
        return false;
    }

    @Override
    public boolean isWelcomeMessage() {
        return false;
    }

    @Override
    public boolean isAbortMessage() {
        return false;
    }

    @Override
    public boolean isChallengeMessage() {
        return false;
    }

    @Override
    public boolean isAuthenticateMessage() {
        return false;
    }

    @Override
    public boolean isGoodbyeMessage() {
        return false;
    }

    @Override
    public boolean isHeartbeatMessage() {
        return false;
    }

    @Override
    public boolean isErrorMessage() {
        return false;
    }

    @Override
    public boolean isPublishMessage() {
        return false;
    }

    @Override
    public boolean isPublishedMessage() {
        return false;
    }

    @Override
    public boolean isSubscribeMessage() {
        return false;
    }

    @Override
    public boolean isSubscribedMessage() {
        return false;
    }

    @Override
    public boolean isUnsubscribeMessage() {
        return false;
    }

    @Override
    public boolean isUnsubscribedMessage() {
        return false;
    }

    @Override
    public boolean isEventMessage() {
        return false;
    }

    @Override
    public boolean isCallMessage() {
        return false;
    }

    @Override
    public boolean isCancelMessage() {
        return false;
    }

    @Override
    public boolean isResultMessage() {
        return false;
    }

    @Override
    public boolean isRegisterMessage() {
        return false;
    }

    @Override
    public boolean isRegisteredMessage() {
        return false;
    }

    @Override
    public boolean isUnregisterMessage() {
        return false;
    }

    @Override
    public boolean isUnregisteredMessage() {
        return false;
    }

    @Override
    public boolean isInvocationMessage() {
        return false;
    }

    @Override
    public boolean isInterruptMessage() {
        return false;
    }

    @Override
    public boolean isYieldMessage() {
        return false;
    }

    @Override
    public WampHelloMessage asHelloMessage() {
        return null;
    }

    @Override
    public WampWelcomeMessage asWelcomeMessage() {
        return null;
    }

    @Override
    public WampAbortMessage asAbortMessage() {
        return null;
    }

    @Override
    public WampGoodbyeMessage asGoodbyeMessage() {
        return null;
    }

    @Override
    public WampErrorMessage asErrorMessage() {
        return null;
    }

    @Override
    public WampPublishMessage asPublishMessage() {
        return null;
    }

    @Override
    public WampPublishedMessage asPublishedMessage() {
        return null;
    }

    @Override
    public WampSubscribeMessage asSubscribeMessage() {
        return null;
    }

    @Override
    public WampSubscribedMessage asSubscribedMessage() {
        return null;
    }

    @Override
    public WampUnsubscribeMessage asUnsubscribeMessage() {
        return null;
    }

    @Override
    public WampUnsubscribedMessage asUnsubscribedMessage() {
        return null;
    }

    @Override
    public WampEventMessage asEventMessage() {
        return null;
    }

    @Override
    public WampCallMessage asCallMessage() {
        return null;
    }

    @Override
    public WampResultMessage asResultMessage() {
        return null;
    }

    @Override
    public WampRegisterMessage asRegisterMessage() {
        return null;
    }

    @Override
    public WampRegisteredMessage asRegisteredMessage() {
        return null;
    }

    @Override
    public WampUnregisterMessage asUnregisterMessage() {
        return null;
    }

    @Override
    public WampUnregisteredMessage asUnregisteredMessage() {
        return null;
    }

    @Override
    public WampInvocationMessage asInvocationMessage() {
        return null;
    }

    @Override
    public WampYieldMessage asYieldMessage() {
        return null;
    }

}
