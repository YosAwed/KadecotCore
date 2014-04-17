
package com.sonycsl.test.wamp;

import com.sonycsl.wamp.message.WampMessage;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public interface Testable {
    public void setCountDownLatch(CountDownLatch latch);

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException;

    public WampMessage getLatestMessage();

    public void transmit(WampMessage msg);
}
