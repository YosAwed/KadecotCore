/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.test.wamp.mock;

import com.sonycsl.test.wamp.Testable;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampRole;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MockWampPeer extends WampPeer implements Testable {

    private CountDownLatch mLatch;
    private WampMessage mMsg;

    public void setCountDownLatch(CountDownLatch latch) {
        mLatch = latch;
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return mLatch.await(timeout, unit);
    }

    public WampMessage getLatestMessage() {
        return mMsg;
    }

    @Override
    protected Set<WampRole> getRoleSet() {
        Set<WampRole> roleSet = new HashSet<WampRole>();
        roleSet.add(new MockWampRole());
        return roleSet;
    }

    @Override
    protected void OnConnected(WampPeer peer) {
    }

    @Override
    protected void OnTransmitted(WampPeer peer, WampMessage msg) {
    }

    @Override
    protected void OnReceived(WampMessage msg) {
        mMsg = msg;
        if (mLatch != null) {
            mLatch.countDown();
        }
    }
}
