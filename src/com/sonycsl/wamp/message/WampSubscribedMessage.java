
package com.sonycsl.wamp.message;

public interface WampSubscribedMessage {
    public int getRequestId();

    public int getSubscriptionId();
}
