
package com.sonycsl.test.util;

import com.sonycsl.test.mock.MockWampRole;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampRole;

import java.util.HashSet;
import java.util.Set;

public class TestablePeer extends WampPeer {

    @Override
    public void setCallback(WampPeer.Callback callback) {
        if (!(callback instanceof TestableCallback)) {
            throw new IllegalArgumentException();
        }

        super.setCallback(callback);
    }

    @Override
    public TestableCallback getCallback() {
        return (TestableCallback) super.getCallback();
    }

    @Override
    protected Set<WampRole> getRoleSet() {
        Set<WampRole> roleSet = new HashSet<WampRole>();
        roleSet.add(new MockWampRole());
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
