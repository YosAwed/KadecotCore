
package com.sonycsl.test.wamp.mock;

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

public class WampMockMessage implements WampMessage {

    @Override
    public int getMessageType() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isHelloMessage() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isWelcomeMessage() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isAbortMessage() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isChallengeMessage() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isAuthenticateMessage() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isGoodbyeMessage() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isHeartbeatMessage() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isErrorMessage() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isPublishMessage() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isPublishedMessage() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSubscribeMessage() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSubscribedMessage() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isUnsubscribeMessage() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isUnsubscribedMessage() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEventMessage() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isCallMessage() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isCancelMessage() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isResultMessage() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRegisterMessage() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRegisteredMessage() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isUnregisterMessage() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isUnregisteredMessage() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInvocationMessage() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInterruptMessage() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isYieldMessage() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public WampHelloMessage asHelloMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WampWelcomeMessage asWelcomeMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WampAbortMessage asAbortMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WampGoodbyeMessage asGoodbyeMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WampErrorMessage asErrorMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WampPublishMessage asPublishMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WampPublishedMessage asPublishedMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WampSubscribeMessage asSubscribeMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WampSubscribedMessage asSubscribedMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WampUnsubscribeMessage asUnsubscribeMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WampUnsubscribedMessage asUnsubscribedMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WampEventMessage asEventMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WampCallMessage asCallMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WampResultMessage asResultMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WampRegisterMessage asRegisterMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WampRegisteredMessage asRegisteredMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WampUnregisterMessage asUnregisterMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WampUnregisteredMessage asUnregisteredMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WampInvocationMessage asInvocationMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WampYieldMessage asYieldMessage() {
        // TODO Auto-generated method stub
        return null;
    }

}
