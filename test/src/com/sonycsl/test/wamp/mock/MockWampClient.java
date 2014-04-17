
package com.sonycsl.test.wamp.mock;

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
    protected void onReceived(WampMessage msg) {
        mMsgs.add(msg);
        super.onReceived(msg);
    }

    public List<WampMessage> getAllMessages() {
        return mMsgs;
    }

    public void clearMessages() {
        mMsgs.clear();
    }
}
