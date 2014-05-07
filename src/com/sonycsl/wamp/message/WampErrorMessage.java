/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp.message;

import org.json.JSONArray;
import org.json.JSONObject;

public interface WampErrorMessage {
    public int getRequestType();

    public int getRequestId();

    public JSONObject getDetails();

    public String getUri();

    public JSONArray getArguments();

    public JSONObject getArgumentsKw();
}
