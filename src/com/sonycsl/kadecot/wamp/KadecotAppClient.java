
package com.sonycsl.kadecot.wamp;

import com.sonycsl.wamp.WampClient;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampCaller;
import com.sonycsl.wamp.role.WampRole;
import com.sonycsl.wamp.role.WampSubscriber;

import java.util.HashSet;
import java.util.Set;

public class KadecotAppClient extends WampClient {

    private static final String TAG = KadecotAppClient.class.getSimpleName();

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
    protected void OnConnected(WampPeer peer) {
    }

    @Override
    protected void OnTransmitted(WampPeer peer, WampMessage msg) {
    }

    @Override
    protected void OnReceived(WampMessage msg) {
        if (mListener != null) {
            mListener.onMessage(msg);
        }
    }
}
