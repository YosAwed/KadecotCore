/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.call;

import org.json.JSONException;
import org.json.JSONObject;

public class Response {
    @SuppressWarnings("unused")
    private static final String TAG = Response.class.getSimpleName();

    private final Response self = this;

    public final Object value;

    protected boolean success;

    public Response(Object value) {
        this.value = value;
        this.success = true;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();

        if (success) {
            obj.put("result", value);
        } else {
            obj.put("error", value);
        }
        return obj;
    }

}
