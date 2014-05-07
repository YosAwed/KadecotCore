/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp.message;

import org.json.JSONArray;
import org.json.JSONObject;

public interface WampCallMessage {
    public int getRequestId();

    public JSONObject getOptions();

    public String getProcedure();

    public boolean hasArguments();

    public JSONArray getArguments();

    public boolean hasArgumentsKw();

    public JSONObject getArgumentsKw();
}
