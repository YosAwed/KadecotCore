
package com.sonycsl.Kadecot.call;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.json.JSONArray;

import android.content.Context;
import android.util.Log;

import com.sonycsl.Kadecot.device.DeviceManager;

public class NotificationProcessor {
    @SuppressWarnings("unused")
    private static final String TAG = NotificationProcessor.class.getSimpleName();

    private final NotificationProcessor self = this;

    protected final Context mContext;

    protected final int mPermissionLevel;

    protected DeviceManager mDeviceManager;

    public NotificationProcessor(Context context, int permissionLevel) {
        mContext = context.getApplicationContext();
        mPermissionLevel = permissionLevel;

        mDeviceManager = DeviceManager.getInstance(mContext);
    }

    public void process(final String methodName, final JSONArray params) {
        Log.v(TAG, params.toString());
        try {
            Method method = getClass().getMethod(methodName, new Class[] {
                    JSONArray.class
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

    public void refreshList(JSONArray params) {
        mDeviceManager.refreshList(mPermissionLevel);
    }

}
