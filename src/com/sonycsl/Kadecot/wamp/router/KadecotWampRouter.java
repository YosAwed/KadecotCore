/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.wamp.router;

import android.content.ContentResolver;

import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.WampRouter;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampBroker;
import com.sonycsl.wamp.role.WampDealer;
import com.sonycsl.wamp.role.WampRole;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class KadecotWampRouter extends WampRouter {

    public static final String REALM = "realm";

    private final ContentResolver mResolver;

    public KadecotWampRouter(ContentResolver resolver) {
        super();
        mResolver = resolver;
    }

    @Override
    protected Set<WampRole> getRouterRoleSet() {
        Set<WampRole> roleSet = new HashSet<WampRole>();
        roleSet.add(new WampBroker(new KadecotTopicResolver(mResolver)) {
            @Override
            protected JSONObject createEventDetails(JSONObject publishOptions) {
                return publishOptions;
            }

        });
        roleSet.add(new WampDealer() {
            @Override
            protected JSONObject createInvocationDetails(JSONObject callOptions) {
                return callOptions;
            }
        });
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
