/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.wamp.message.impl;

import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.message.WampRegisterMessage;
import com.sonycsl.wamp.util.NullChecker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WampRegisterMessageImpl extends WampAbstractMessage implements WampRegisterMessage {

    private static final int REQUEST_ID_INDEX = 1;
    private static final int OPTIONS_INDEX = 2;
    private static final int PROCEDURE_INDEX = 3;

    public static WampMessage create(int requestId, JSONObject options, String procedure) {
        NullChecker.nullCheck(options, procedure);
        return new WampRegisterMessageImpl(new JSONArray().put(WampMessageType.REGISTER)
                .put(requestId).put(options).put(procedure));
    }

    public WampRegisterMessageImpl(JSONArray msg) {
        super(msg);
    }

    @Override
    public boolean isRegisterMessage() {
        return true;
    }

    @Override
    public WampRegisterMessage asRegisterMessage() {
        return this;
    }

    @Override
    public int getRequestId() {
        try {
            return toJSON().getInt(REQUEST_ID_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no request id");
        }
    }

    @Override
    public JSONObject getOptions() {
        try {
            return toJSON().getJSONObject(OPTIONS_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no option");
        }
    }

    @Override
    public String getProcedure() {
        try {
            return toJSON().getString(PROCEDURE_INDEX);
        } catch (JSONException e) {
            throw new IllegalArgumentException("there is no procedure");
        }
    }

}
