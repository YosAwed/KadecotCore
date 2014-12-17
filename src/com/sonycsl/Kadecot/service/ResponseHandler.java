
package com.sonycsl.Kadecot.service;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

public class ResponseHandler extends Handler {

    private WeakReference<MessageReceiver> mRef;

    public ResponseHandler(MessageReceiver callback) {
        super();
        mRef = new WeakReference<MessageReceiver>(callback);
    }

    @Override
    public void handleMessage(Message msg) {
        MessageReceiver callback = mRef.get();
        if (callback == null) {
            return;
        }
        callback.onReceive(msg);
    }

}
