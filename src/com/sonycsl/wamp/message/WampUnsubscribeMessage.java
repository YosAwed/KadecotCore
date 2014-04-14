
package com.sonycsl.wamp.message;

public interface WampUnsubscribeMessage {
    public int getRequestId();

    public int getSubscriptionId();
}
