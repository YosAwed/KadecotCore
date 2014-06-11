
package com.sonycsl.kadecot.wamp;

import java.util.HashSet;
import java.util.Set;

public final class KadecotWampPeerLocator {

    private static KadecotWampPeerLocator instance = new KadecotWampPeerLocator();

    public static void load(KadecotWampPeerLocator locator) {
        instance = locator;
    }

    public static KadecotWampClient[] getClients() {
        return instance.mClients.toArray(new KadecotWampClient[instance.mClients.size()]);
    }

    public static KadecotWampRouter getRouter() {
        return instance.mRouter;
    }

    private KadecotWampRouter mRouter;
    private Set<KadecotWampClient> mClients;

    public KadecotWampPeerLocator() {
        mClients = new HashSet<KadecotWampClient>();
    }

    public void loadClient(KadecotWampClient client) {
        mClients.add(client);
    }

    public void setRouter(KadecotWampRouter router) {
        mRouter = router;
    }
}
