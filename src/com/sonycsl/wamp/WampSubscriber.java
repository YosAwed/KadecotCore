
package com.sonycsl.wamp;

public abstract class WampSubscriber extends WampClient {

    public WampSubscriber() {
    }

    public WampSubscriber(WampClient next) {
        super(next);
    }

    @Override
    protected final boolean consumeRoleBroadcast(WampMessage msg) {
        if (msg.isSubscribeMessage()) {
            return true;
        }
        if (msg.isUnsubscribeMessage()) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean consumeRoleMessage(WampMessenger friend, WampMessage msg) {
        if (consumeMyMessage(friend, msg)) {
            onConsumed(msg);
            return true;
        }
        return false;
    }

    private boolean consumeMyMessage(WampMessenger friend, WampMessage msg) {
        if (msg.isSubscribedMessage()) {
            /**
             * TODO: verify requestId of subscribed message.
             */
            subscribed(msg.asSubscribedMessage());
            return true;
        }

        if (msg.isUnsubscribedMessage()) {

            /**
             * TODO: verify requestId of unsubscribed message.
             */
            unsubscribed(msg.asUnsubscribedMessage());
            return true;
        }

        if (msg.isEventMessage()) {
            /**
             * TODO: verify subscriptionId of event message.
             */
            event(msg.asEventMessage());
            return true;
        }
        return false;
    }

    abstract protected void subscribed(WampSubscribedMessage msg);

    abstract protected void unsubscribed(WampUnsubscribedMessage msg);

    abstract protected void event(WampEventMessage msg);
}
