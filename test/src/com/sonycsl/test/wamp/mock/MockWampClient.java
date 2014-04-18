
package com.sonycsl.test.wamp.mock;

import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampRole;

import java.util.ArrayList;
import java.util.List;

public class MockWampClient extends MockWampPeer {

    private List<WampMessage> mMsgs = new ArrayList<WampMessage>();

    @Override
    protected WampRole getRole() {
        return new MockWampClientRole();
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
