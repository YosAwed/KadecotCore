
package com.sonycsl.Kadecot.device;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.sonycsl.Kadecot.core.R;

import org.json.JSONArray;
import org.json.JSONException;

public class DeviceNotification {
    @SuppressWarnings("unused")
    private static final String TAG = DeviceNotification.class.getSimpleName();

    private final DeviceNotification self = this;

    protected static final int ERROR_NOTIFICATION_ID = 137;

    public static final String INTENT_ACTION_LAUNCH_FROM_DEVICE_ERROR_NOTIFICATION =
            "com.sonycsl.Kadecot.LAUNCH_FROM_DEVICE_ERROR_NOTIFICATION";

    protected final Context mContext;

    protected Notification mNotification = null;

    protected int mNotificationId = 0;

    public DeviceNotification(Context context) {
        mContext = context;
    }

    public DeviceNotification buildEchoErrorNotification(String nickname, JSONArray errorValue)
            throws JSONException {

        mNotification = new Notification(R.drawable.icon, "ERROR", System.currentTimeMillis());
        // notice.flags |= Notification.FLAG_NO_CLEAR;

        PackageManager pm = mContext.getPackageManager();
        // Intent intent =
        // pm.getLaunchIntentForPackage("com.sonycsl.ARMoekaden");
        Intent intent = new Intent(INTENT_ACTION_LAUNCH_FROM_DEVICE_ERROR_NOTIFICATION);
        // intent.setAction(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        byte val0 = (byte) errorValue.getInt(0);
        byte val1 = (byte) errorValue.getInt(1);
        String errorInfo = "0x" + String.format("%02x", val0) + String.format("%02x", val1);
        String contentText = "[" + nickname + "]" + errorInfo;
        mNotification.setLatestEventInfo(mContext, "ERROR", contentText, pendIntent);

        mNotificationId = ERROR_NOTIFICATION_ID;

        return this;
    }

    public void show() {
        if (mNotification != null) {
            NotificationManager nm =
                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(mNotificationId, mNotification);
        }
    }

}
