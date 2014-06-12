
package com.sonycsl.Kadecot.wamp;

import com.sonycsl.wamp.WampClient;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampCaller;
import com.sonycsl.wamp.role.WampRole;
import com.sonycsl.wamp.role.WampSubscriber;

import java.util.HashSet;
import java.util.Set;

public class KadecotAppClient extends WampClient {

    private MessageListener mListener;

    public interface MessageListener {
        public void onMessage(WampMessage msg);
    }

    @Override
    protected Set<WampRole> getClientRoleSet() {
        Set<WampRole> roleSet = new HashSet<WampRole>();
        roleSet.add(new WampCaller());
        roleSet.add(new WampSubscriber() {

            @Override
            protected void onEvent(String topic, WampMessage msg) {
            }
        });
        return roleSet;
    }

    public void setOnMessageListener(MessageListener listener) {
        mListener = listener;
    }

    @Override
    protected void onConnected(WampPeer peer) {
    }

    @Override
    protected void onTransmitted(WampPeer peer, WampMessage msg) {
    }

    @Override
    protected void onReceived(WampMessage msg) {
        if (mListener != null) {
            mListener.onMessage(msg);
        }
    }
}
