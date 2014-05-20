
package com.sonycsl.kadecot.server;

import com.sonycsl.kadecot.wamp.KadecotDeviceObserver;
import com.sonycsl.kadecot.wamp.KadecotTopicTimer;
import com.sonycsl.kadecot.wamp.KadecotWampTopic;
import com.sonycsl.wamp.WampClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class KadecotWampClientLocator {

    private static KadecotWampClientLocator instance = new KadecotWampClientLocator();

    public static void load(KadecotWampClientLocator locator) {
        instance = locator;
    }

    public static WampClient[] getClients() {
        return instance.mClients.toArray(new WampClient[instance.mClients.size()]);
    }

    private List<WampClient> mClients;

    public KadecotWampClientLocator() {
        mClients = new ArrayList<WampClient>();

        mClients.add(new KadecotDeviceObserver());
        mClients.add(new KadecotTopicTimer(KadecotWampTopic.TOPIC_PRIVATE_SEARCH, 5,
                TimeUnit.SECONDS));
    }

    public void loadClient(WampClient client) {
        mClients.add(client);
    }

}
