/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.plugin;

import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;

public abstract class PostReceiveCallback implements WampPeer.Callback {

    @Override
    public void preConnect(WampPeer connecter, WampPeer connectee) {
    }

    @Override
    public void postConnect(WampPeer connecter, WampPeer connectee) {
    }

    @Override
    public void preTransmit(WampPeer transmitter, WampMessage msg) {
    }

    @Override
    public void postTransmit(WampPeer transmitter, WampMessage msg) {
    }

    @Override
    public void preReceive(WampPeer receiver, WampMessage msg) {
    }

}
