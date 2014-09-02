/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.wamp.client.mock;

import com.sonycsl.Kadecot.wamp.client.KadecotWampClient;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.mock.MockWampClientRole;
import com.sonycsl.wamp.role.WampRole;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MockKadecotWampClient extends KadecotWampClient {

    @Override
    public Set<String> getTopicsToSubscribe() {
        return new HashSet<String>();
    }

    @Override
    public Map<String, String> getSubscribableTopics() {
        return new HashMap<String, String>();
    }

    @Override
    public Map<String, String> getRegisterableProcedures() {
        return new HashMap<String, String>();
    }

    @Override
    protected Set<WampRole> getClientRoleSet() {
        Set<WampRole> roleSet = new HashSet<WampRole>();
        roleSet.add(new MockWampClientRole());
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
    }

}
