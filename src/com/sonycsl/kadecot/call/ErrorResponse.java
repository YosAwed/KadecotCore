/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.call;

import org.json.JSONException;
import org.json.JSONObject;

public class ErrorResponse extends Response {

    public static final int PARSE_ERROR_CODE = -32700;

    protected static final String PARSE_ERROR_MSG = "Parse error";

    public static final int INVALID_REQUEST_CODE = -32600;

    protected static final String INVALID_REQUEST_MSG = "Invalid Request";

    public static final int METHOD_NOT_FOUND_CODE = -32601;

    protected static final String METHOD_NOT_FOUND_MSG = "Method not found";

    public static final int INVALID_PARAMS_CODE = -32602;

    protected static final String INVALID_PARAMS_MSG = "Invalid params";

    public static final int INTERNAL_ERROR_CODE = -32603;

    protected static final String INTERNAL_ERROR_MSG = "Internal error";

    public ErrorResponse(int code, String message, Object data) {
        super(getErrorObject(code, message, data));
        this.success = false;
    }

    public ErrorResponse(JSONObject obj) {
        super(obj);
        this.success = false;
    }

    public ErrorResponse(int code, Object data) {
        this(code, getErrorMessage(code), data);
    }

    public ErrorResponse(int code, String message) {
        this(code, message, null);
    }

    public ErrorResponse(int code) {
        this(code, getErrorMessage(code), null);
    }

    public static JSONObject getErrorObject(int code, String message, Object data) {
        JSONObject value = new JSONObject();
        try {
            value.put("code", code);
            value.put("message", message);
            if (data != null) {
                value.put("data", data);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return value;
    }

    public static JSONObject getErrorObject(int code, Object data) {
        return getErrorObject(code, getErrorMessage(code), data);
    }

    public static JSONObject getErrorObject(int code) {
        return getErrorObject(code, getErrorMessage(code), null);
    }

    private static String getErrorMessage(int code) {
        String message = "";
        switch (code) {
            case PARSE_ERROR_CODE:
                message = PARSE_ERROR_MSG;
                break;
            case INVALID_REQUEST_CODE:
                message = INVALID_REQUEST_MSG;
                break;
            case METHOD_NOT_FOUND_CODE:
                message = METHOD_NOT_FOUND_MSG;
                break;
            case INVALID_PARAMS_CODE:
                message = INVALID_PARAMS_MSG;
                break;
            case INTERNAL_ERROR_CODE:
                message = INTERNAL_ERROR_MSG;
                break;
        }
        return message;
    }

}
