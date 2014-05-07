/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.call;

public class CannotProcessRequestException extends RuntimeException {
    /**
	 * 
	 */
    private static final long serialVersionUID = -5633026610439739490L;

    @SuppressWarnings("unused")
    private static final String TAG = CannotProcessRequestException.class.getSimpleName();

    private final CannotProcessRequestException self = this;

    final protected ErrorResponse mErrorResponse;

    public CannotProcessRequestException(ErrorResponse res) {
        mErrorResponse = res;
    }

    public CannotProcessRequestException(int code, String message) {
        mErrorResponse = new ErrorResponse(code, message);
    }

    public CannotProcessRequestException(int code, String message, Object data) {
        mErrorResponse = new ErrorResponse(code, message, data);
    }

    public ErrorResponse getErrorResponse() {
        return mErrorResponse;
    }

}
