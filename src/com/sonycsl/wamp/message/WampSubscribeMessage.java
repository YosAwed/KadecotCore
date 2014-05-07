/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp.message;

import org.json.JSONObject;

public interface WampSubscribeMessage {
    public int getRequestId();

    public JSONObject getOptions();

    public String getTopic();
}
