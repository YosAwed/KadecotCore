package com.sonycsl.Kadecot.device;

import com.sonycsl.Kadecot.call.ErrorResponse;

public class AccessException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3590612240967381491L;
	@SuppressWarnings("unused")
	private static final String TAG = AccessException.class.getSimpleName();
	private final AccessException self = this;
	
	protected ErrorResponse mRes;
	
	public AccessException(ErrorResponse res) {
		mRes = res;
	}
	
	public ErrorResponse getErrorResponse() {
		return mRes;
	}

}
