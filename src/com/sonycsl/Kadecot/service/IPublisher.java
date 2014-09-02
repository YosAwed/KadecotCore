/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.service;

import com.sonycsl.wamp.message.WampMessage;

import org.json.JSONObject;

public interface IPublisher {

    public interface OnPublishedListener {
        public void onPublished(int publicationId);

        public void onError(WampMessage reply);
    }

    public void publish(String topic, JSONObject argsKw, OnPublishedListener listener);
}
