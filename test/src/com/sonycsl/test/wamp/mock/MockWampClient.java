
package com.sonycsl.test.wamp.mock;

import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampRole;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MockWampClient extends MockWampPeer {

    private List<WampMessage> mMsgs = new ArrayList<WampMessage>();

    @Override
    protected Set<WampRole> getRoleSet() {
        Set<WampRole> roleSet = new HashSet<WampRole>();
        roleSet.add(new MockWampClientRole());
        return roleSet;
    }

    @Override
    protected void OnConnected(WampPeer peer) {
        super.OnConnected(peer);
    }

    @Override
    protected void OnReceived(WampMessage msg) {
        mMsgs.add(msg);
        super.OnReceived(msg);
    }

    public List<WampMessage> getAllMessages() {
        return mMsgs;
    }

    public void clearMessages() {
        mMsgs.clear();
    }
}
