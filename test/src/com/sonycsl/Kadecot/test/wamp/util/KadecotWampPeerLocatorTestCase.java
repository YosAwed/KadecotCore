/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.test.wamp.util;

import android.test.mock.MockContentResolver;

import com.sonycsl.Kadecot.wamp.client.KadecotWampClient;
import com.sonycsl.Kadecot.wamp.client.mock.MockKadecotWampClient;
import com.sonycsl.Kadecot.wamp.router.mock.MockKadecotWampRouter;
import com.sonycsl.Kadecot.wamp.util.KadecotWampPeerLocator;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class KadecotWampPeerLocatorTestCase extends TestCase {

    private MockKadecotWampRouter mRouter;
    private MockKadecotWampClient mClient1;
    private MockKadecotWampClient mClient2;

    private KadecotWampPeerLocator mLocator;

    @Override
    protected void setUp() {
        mRouter = new MockKadecotWampRouter(new MockContentResolver());
        mClient1 = new MockKadecotWampClient();
        mClient2 = new MockKadecotWampClient();

        mLocator = new KadecotWampPeerLocator();
    }

    public void testConstructor() {
        assertNotNull(mRouter);
        assertNotNull(mClient1);
        assertNotNull(mClient2);
        assertNotNull(mLocator);
    }

    public void testSetRouter() {
        mLocator.setRouter(mRouter);
        KadecotWampPeerLocator.load(mLocator);
        assertEquals(mRouter, KadecotWampPeerLocator.getRouter());
    }

    public void testSetSystemClient() {
        mLocator.loadSystemClient(mClient1);
        mLocator.loadSystemClient(mClient2);
        List<KadecotWampClient> clientList = new ArrayList<KadecotWampClient>();
        clientList.add(mClient1);
        clientList.add(mClient2);

        KadecotWampPeerLocator.load(mLocator);

        for (KadecotWampClient client : KadecotWampPeerLocator.getSystemClients()) {
            assertTrue(clientList.contains(client));
        }
    }

    public void testSetProtocolClient() {
        mLocator.loadProtocolClient(mClient1);
        mLocator.loadProtocolClient(mClient2);
        List<KadecotWampClient> clientList = new ArrayList<KadecotWampClient>();
        clientList.add(mClient1);
        clientList.add(mClient2);

        KadecotWampPeerLocator.load(mLocator);

        for (KadecotWampClient client : KadecotWampPeerLocator.getProtocolClients()) {
            assertTrue(clientList.contains(client));
        }
    }
}
