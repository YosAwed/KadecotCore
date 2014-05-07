/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.device;

import com.sonycsl.kadecot.call.ErrorResponse;

public class AccessException extends Exception {
    /**
	 * 
	 */
    private static final long serialVersionUID = -3590612240967381491L;

    @SuppressWarnings("unused")
    private static final String TAG = AccessException.class.getSimpleName();

    private final AccessException self = this;

    final protected ErrorResponse mErrorResponse;

    public AccessException(ErrorResponse res) {
        mErrorResponse = res;
    }

    public AccessException(int code, String message) {
        mErrorResponse = new ErrorResponse(code, message);
    }

    public AccessException(int code, String message, Object data) {
        mErrorResponse = new ErrorResponse(code, message, data);
    }

    public ErrorResponse getErrorResponse() {
        return mErrorResponse;
    }

}
