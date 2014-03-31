
package com.sonycsl.test.wamp;

import com.sonycsl.wamp.WampMessage;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public interface WampTest {

    public void broadcast(WampMessage msg);

    public void setConsumed(boolean isConsumed);

    public void setCountDownLatch(CountDownLatch latch);

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException;

    public WampMessage getMessage();
}
