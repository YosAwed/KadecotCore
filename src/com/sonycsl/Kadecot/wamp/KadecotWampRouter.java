
package com.sonycsl.Kadecot.wamp;

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

    @Override
    protected Set<WampRole> getRouterRoleSet() {
        Set<WampRole> roleSet = new HashSet<WampRole>();
        roleSet.add(new WampBroker());
        roleSet.add(new WampDealer() {
            @Override
            protected JSONObject createInvocationDetails(JSONObject callOptions) {
                return callOptions;
            }
        });
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
    }

}
