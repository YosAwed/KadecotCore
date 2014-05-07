/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp.role;

import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;

public class WampCaller extends WampRole {

    @Override
    public final String getRoleName() {
        return "caller";
    }

    @Override
    public final boolean resolveTxMessageImpl(WampPeer receiver, WampMessage msg) {
        return msg.isCallMessage();
    }

    @Override
    public final boolean resolveRxMessageImpl(WampPeer transmitter, WampMessage msg,
            OnReplyListener listener) {
        return msg.isResultMessage();
    }
}
