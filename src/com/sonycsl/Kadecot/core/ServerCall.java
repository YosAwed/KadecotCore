
package com.sonycsl.Kadecot.core;

import com.sonycsl.Kadecot.call.KadecotCall;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Kadecot My Page用のJSインターフェース
 */
public class ServerCall {
    @SuppressWarnings("unused")
    private static final String TAG = ServerCall.class.getSimpleName();

    private final ServerCall self = this;

    private final KadecotCoreActivity mKadecot;

    private final KadecotCall mCall;

    public ServerCall(KadecotCoreActivity kadecot, KadecotCall call) {
        mKadecot = kadecot;
        mCall = call;
    }

    public void invoke(final String msg) {

        mKadecot.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                try {
                    mCall.receive(new JSONObject(msg));
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        });
    }

    public void onPageLoadFinished() {
        mKadecot.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mCall.start();

            }

        });
    }
}
