/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.wamp;

import com.sonycsl.Kadecot.plugin.KadecotProtocolSetupCallback;
import com.sonycsl.Kadecot.plugin.KadecotWampSetupCallback;
import com.sonycsl.Kadecot.wamp.client.KadecotWampClient;
import com.sonycsl.Kadecot.wamp.router.KadecotWampRouter;
import com.sonycsl.Kadecot.wamp.util.KadecotWampPeerLocator;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampRouter;
import com.sonycsl.wamp.message.WampMessageFactory;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WampTopology {

    private Map<KadecotWampClient, KadecotWampSetupCallback> mWampCallbacks = new HashMap<KadecotWampClient, KadecotWampSetupCallback>();
    private Map<KadecotWampClient, KadecotProtocolSetupCallback> mProtocolCallbacks = new HashMap<KadecotWampClient, KadecotProtocolSetupCallback>();

    public WampTopology() {
        for (KadecotWampClient client : KadecotWampPeerLocator.getSystemClients()) {
            client.connect(KadecotWampPeerLocator.getRouter());
        }
        for (KadecotWampClient client : KadecotWampPeerLocator.getProtocolClients()) {
            client.connect(KadecotWampPeerLocator.getRouter());
        }
    }

    public WampRouter getRouter() {
        return KadecotWampPeerLocator.getRouter();
    }

    public void start() throws InterruptedException {
        final CountDownLatch systemSetup = new CountDownLatch(
                KadecotWampPeerLocator.getSystemClients().length);
        for (KadecotWampClient client : KadecotWampPeerLocator.getSystemClients()) {
            KadecotWampSetupCallback callback = new KadecotWampSetupCallback(
                    client.getTopicsToSubscribe(), client.getRegisterableProcedures()
                            .keySet(),
                    new KadecotWampSetupCallback.OnCompletionListener() {
                        @Override
                        public void onCompletion() {
                            systemSetup.countDown();
                        }
                    });
            mWampCallbacks.put(client, callback);
            client.setCallback(callback);
        }

        for (KadecotWampClient client : KadecotWampPeerLocator.getSystemClients()) {
            client.transmit(WampMessageFactory.createHello(KadecotWampRouter.REALM,
                    new JSONObject()));
        }

        if (!systemSetup.await(5, TimeUnit.SECONDS)) {
            throw new IllegalArgumentException();
        }

        final CountDownLatch wampSetup = new CountDownLatch(
                KadecotWampPeerLocator.getProtocolClients().length);
        for (KadecotWampClient client : KadecotWampPeerLocator.getProtocolClients()) {
            KadecotProtocolSetupCallback protocolCallback = new KadecotProtocolSetupCallback(
                    client.getSubscribableTopics(),
                    client.getRegisterableProcedures(),
                    new KadecotProtocolSetupCallback.OnCompletionListener() {
                        @Override
                        public void onCompletion() {
                        }
                    });
            mProtocolCallbacks.put(client, protocolCallback);
            client.setCallback(protocolCallback);

            KadecotWampSetupCallback wampCallback = new KadecotWampSetupCallback(
                    client.getTopicsToSubscribe(), client.getRegisterableProcedures().keySet(),
                    new KadecotWampSetupCallback.OnCompletionListener() {
                        @Override
                        public void onCompletion() {
                            wampSetup.countDown();
                        }
                    });
            mWampCallbacks.put(client, wampCallback);
            client.setCallback(wampCallback);
        }

        for (KadecotWampClient client : KadecotWampPeerLocator.getProtocolClients()) {
            client.transmit(WampMessageFactory.createHello(KadecotWampRouter.REALM,
                    new JSONObject()));
        }

        if (!wampSetup.await(5, TimeUnit.SECONDS)) {
            throw new IllegalArgumentException();
        }

    }

    public void stop() {
        for (KadecotWampClient client : KadecotWampPeerLocator.getProtocolClients()) {
            client.transmit(WampMessageFactory.createGoodbye(new JSONObject(),
                    WampError.CLOSE_REALM));
        }

        for (KadecotWampClient client : KadecotWampPeerLocator.getSystemClients()) {
            client.transmit(WampMessageFactory.createGoodbye(new JSONObject(),
                    WampError.CLOSE_REALM));
        }

        for (Entry<KadecotWampClient, KadecotWampSetupCallback> entry : mWampCallbacks
                .entrySet()) {
            entry.getKey().removeCallback(entry.getValue());
        }

        for (Entry<KadecotWampClient, KadecotProtocolSetupCallback> entry : mProtocolCallbacks
                .entrySet()) {
            entry.getKey().removeCallback(entry.getValue());
        }
    }

}
