
package com.sonycsl.wamp;

public interface WampMessage {

    public String toString();

    public int getMessageType();

    public boolean isHelloMessage();

    public boolean isWelcomeMessage();

    public boolean isAbortMessage();

    public boolean isChallengeMessage();

    public boolean isAuthenticateMessage();

    public boolean isGoodbyeMessage();

    public boolean isHeartbeatMessage();

    public boolean isErrorMessage();

    public boolean isPublishMessage();

    public boolean isPublishedMessage();

    public boolean isSubscribeMessage();

    public boolean isSubscribedMessage();

    public boolean isUnsubscribeMessage();

    public boolean isUnsubscribedMessage();

    public boolean isEventMessage();

    public boolean isCallMessage();

    public boolean isCancelMessage();

    public boolean isResuleMessage();

    public boolean isRegisterMessage();

    public boolean isRegisteredMessage();

    public boolean isUnregisterMessage();

    public boolean isUnregisteredMessage();

    public boolean isInvocationMessage();

    public boolean isInterruptMessage();

    public boolean isYieldMessage();

    public WampHelloMessage asHelloMessage();

    public WampWelcomeMessage asWelcomeMessage();

    public WampAbortMessage asAbortMessage();

    public WampGoodbyeMessage asGoodbyeMessage();

    public WampErrorMessage asErrorMessage();

    public WampPublishMessage asPublishMessage();

    public WampPublishedMessage asPublishedMessage();

    public WampSubscribeMessage asSubscribeMessage();

    public WampSubscribedMessage asSubscribedMessage();

    public WampUnsubscribeMessage asUnsubscribeMessage();

    public WampUnsubscribedMessage asUnsubscribedMessage();

    public WampEventMessage asEventMessage();

    public WampCallMessage asCallMessage();

    public WampResultMessage asResultMessage();

    public WampRegisterMessage asRegisterMessage();

    public WampRegisteredMessage asRegisteredMessage();

    public WampUnregisterMessage asUnregisterMessage();

    public WampUnregisteredMessage asUnregisteredMessage();

    public WampInvocationMessage asInvocationMessage();

    public WampYieldMessage asYieldMessage();
}
