/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.test.mock;

import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampRole;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MockWampPeer extends WampPeer {

    private List<WampMessage> mMsgs = new ArrayList<WampMessage>();

    @Override
    protected Set<WampRole> getRoleSet() {
        Set<WampRole> roleSet = new HashSet<WampRole>();
        roleSet.add(new MockWampRole());
        return roleSet;
    }

    @Override
    protected void onConnected(WampPeer peer) {
    }

    @Override
    protected void onTransmitted(WampPeer peer, WampMessage msg) {
    }

    @Override
    protected void onReceived(WampMessage msg) {
        mMsgs.add(msg);
    }

    public List<WampMessage> getAllMessages() {
        return mMsgs;
    }

    public void clearMessages() {
        mMsgs.clear();
    }
}
