/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.wamp.util;

import com.sonycsl.Kadecot.wamp.client.KadecotWampClient;
import com.sonycsl.Kadecot.wamp.router.KadecotWampRouter;

import java.util.HashSet;
import java.util.Set;

public final class KadecotWampPeerLocator {

    private static KadecotWampPeerLocator instance = new KadecotWampPeerLocator();

    public static void load(KadecotWampPeerLocator locator) {
        instance = locator;
    }

    public static KadecotWampClient[] getSystemClients() {
        return instance.mSystemClients
                .toArray(new KadecotWampClient[instance.mSystemClients.size()]);
    }

    public static KadecotWampClient[] getProtocolClients() {
        return instance.mProtocolClients.toArray(new KadecotWampClient[instance.mProtocolClients
                .size()]);
    }

    public static KadecotWampRouter getRouter() {
        return instance.mRouter;
    }

    private KadecotWampRouter mRouter;
    private Set<KadecotWampClient> mSystemClients;
    private Set<KadecotWampClient> mProtocolClients;

    public KadecotWampPeerLocator() {
        mSystemClients = new HashSet<KadecotWampClient>();
        mProtocolClients = new HashSet<KadecotWampClient>();
    }

    public void loadSystemClient(KadecotWampClient client) {
        mSystemClients.add(client);
    }

    public void loadProtocolClient(KadecotWampClient client) {
        mProtocolClients.add(client);
    }

    public void setRouter(KadecotWampRouter router) {
        mRouter = router;
    }
}
