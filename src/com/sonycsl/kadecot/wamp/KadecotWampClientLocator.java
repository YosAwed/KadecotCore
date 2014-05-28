
package com.sonycsl.kadecot.wamp;

import java.util.HashSet;
import java.util.Set;

public final class KadecotWampClientLocator {

    private static KadecotWampClientLocator instance = new KadecotWampClientLocator();

    public static void load(KadecotWampClientLocator locator) {
        instance = locator;
    }

    public static KadecotWampClient[] getClients() {
        return instance.mClients.toArray(new KadecotWampClient[instance.mClients.size()]);
    }

    private Set<KadecotWampClient> mClients;

    public KadecotWampClientLocator() {
        mClients = new HashSet<KadecotWampClient>();
    }

    public void loadClient(KadecotWampClient client) {
        mClients.add(client);
    }

}
