/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.test.wamp.mock;

import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampRole;

public class MockWampRole extends WampRole {

    @Override
    public String getRoleName() {
        return "mock";
    }

    @Override
    protected boolean resolveTxMessageImpl(WampPeer receiver, WampMessage msg) {
        return true;
    }

    @Override
    protected boolean resolveRxMessageImpl(WampPeer transmitter, WampMessage msg,
            OnReplyListener listener) {
        return true;
    }

}
