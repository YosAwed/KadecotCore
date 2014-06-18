/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp.role;

import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampPublishMessage;
import com.sonycsl.wamp.message.WampPublishedMessage;
import com.sonycsl.wamp.util.DoubleKeyMap;

public class WampPublisher extends WampRole {

    private DoubleKeyMap<WampPeer, Integer, WampMessage> mPubs = new DoubleKeyMap<WampPeer, Integer, WampMessage>();

    @Override
    public final String getRoleName() {
        return "publisher";
    }

    @Override
    public boolean resolveTxMessageImpl(WampPeer receiver, WampMessage msg) {
        if (!msg.isPublishMessage()) {
            return false;
        }
        mPubs.put(receiver, msg.asPublishMessage().getRequestId(), msg);
        return true;
    }

    @Override
    public boolean resolveRxMessageImpl(WampPeer transmitter, WampMessage msg,
            OnReplyListener listener) {
        if (!msg.isPublishedMessage()) {
            return false;
        }

        WampPublishedMessage publishedMsg = msg.asPublishedMessage();

        if (!mPubs.containsKey(transmitter, publishedMsg.getRequestId())) {
            return false;
        }

        WampPublishMessage request = mPubs.get(transmitter, publishedMsg.getRequestId())
                .asPublishMessage();
        WampPublishedMessage response = msg.asPublishedMessage();

        if (request.getRequestId() != response.getRequestId()) {
            return false;
        }

        mPubs.remove(transmitter, publishedMsg.getRequestId());

        return true;
    }
}
