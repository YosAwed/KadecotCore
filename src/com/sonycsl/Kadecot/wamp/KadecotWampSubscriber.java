
package com.sonycsl.Kadecot.wamp;

import com.sonycsl.wamp.WampClient;
import com.sonycsl.wamp.WampEventMessage;
import com.sonycsl.wamp.WampMessage;
import com.sonycsl.wamp.WampSubscribedMessage;
import com.sonycsl.wamp.WampSubscriber;
import com.sonycsl.wamp.WampUnsubscribedMessage;

public class KadecotWampSubscriber extends WampSubscriber {

    public KadecotWampSubscriber() {
    }

    public KadecotWampSubscriber(WampClient next) {
        super(next);
    }

    @Override
    protected void subscribed(WampSubscribedMessage asSubscribedMessage) {
    }

    @Override
    protected void unsubscribed(WampUnsubscribedMessage asUnsubscribedMessage) {
    }

    @Override
    protected void event(WampEventMessage asUnsubscribedMessage) {
    }

    @Override
    protected void onConsumed(WampMessage msg) {
    }

    @Override
    protected boolean consumeRoleBroadcast(WampMessage msg) {
        return false;
    }

}
