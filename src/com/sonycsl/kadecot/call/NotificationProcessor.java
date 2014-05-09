/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.call;

import android.content.Context;
import android.util.Log;

import com.sonycsl.kadecot.device.DeviceManager;

import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NotificationProcessor {
    private static final String TAG = NotificationProcessor.class.getSimpleName();

    protected final Context mContext;

    protected final int mPermissionLevel;

    protected DeviceManager mDeviceManager;

    public NotificationProcessor(Context context, int permissionLevel) {
        mContext = context.getApplicationContext();
        mPermissionLevel = permissionLevel;

        mDeviceManager = DeviceManager.getInstance(mContext);
    }

    public void process(final String methodName, final JSONObject params) {
        Log.v(TAG, params.toString());
        try {
            Method method = getClass().getMethod(methodName, new Class[] {
                    JSONObject.class
            });
            method.invoke(this, new Object[] {
                    params
            });
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            // return new ErrorResponse(ErrorResponse.METHOD_NOT_FOUND_CODE, e);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            // return new ErrorResponse(ErrorResponse.METHOD_NOT_FOUND_CODE, e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            // return new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE, e);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            // return new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE, e);
        }
    }

    public void refreshList(JSONObject params) {
        mDeviceManager.refreshDeviceList(mPermissionLevel);
    }

}
