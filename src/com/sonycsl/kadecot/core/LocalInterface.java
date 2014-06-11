
package com.sonycsl.kadecot.core;

import android.util.Log;
import android.webkit.JavascriptInterface;

import com.sonycsl.kadecot.wamp.KadecotAppClientWrapper;
import com.sonycsl.kadecot.wamp.KadecotAppClientWrapper.WampCallListener;
import com.sonycsl.kadecot.wamp.KadecotAppClientWrapper.WampSubscribeListener;
import com.sonycsl.kadecot.wamp.KadecotAppClientWrapper.WampUnsubscribeListener;
import com.sonycsl.kadecot.wamp.KadecotAppProxy;
import com.sonycsl.kadecot.wamp.KadecotWampRouter;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.util.JsonEscapeUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeoutException;

public class LocalInterface {

    private static final String TAG = LocalInterface.class.getSimpleName();

    private static final String LOCALHOST = "localhost";
    private static final String WEBSOCKET_PORT = "41314";

    private final KadecotCoreActivity mKadecot;

    KadecotAppProxy mProxy;
    KadecotAppClientWrapper mClient;

    public LocalInterface(KadecotCoreActivity kadecot) {
        mKadecot = kadecot;
        mProxy = new KadecotAppProxy();
        mClient = new KadecotAppClientWrapper();

        mClient.connect(mProxy);
    }

    public void open() throws InterruptedException, TimeoutException {
        mProxy.open(LOCALHOST, WEBSOCKET_PORT);
        mClient.hello(KadecotWampRouter.REALM);
    }

    public void close() {
        mClient.goodbye(WampError.CLOSE_REALM);
        mProxy.close();
    }

    @JavascriptInterface
    public void call(String procedure, String options, String paramsKw,
            final String resultListener, final String errorListener) {
        Log.d(TAG, "call, procedure=" + procedure + ", options=" + options + ", paramsKw="
                + paramsKw + ", resultListener=" + resultListener + ", errorListener="
                + errorListener);

        if (!mProxy.isOpen()) {
            try {
                open();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG, "CALL, Can not open WebSocket");
                return;
            } catch (TimeoutException e) {
                e.printStackTrace();
                Log.e(TAG, "CALL, Can not open WebSocket");
                return;
            }
        }

        try {
            mClient.call(procedure, new JSONObject(options), new JSONObject(paramsKw),
                    new WampCallListener() {

                        @Override
                        public void onResult(JSONObject details, JSONObject argumentsKw) {
                            mKadecot.callJsOnKadecotMyPage(resultListener
                                    + "(JSON.parse(\"[]\"), JSON.parse("
                                    + JsonEscapeUtil.escapeSlash(argumentsKw)
                                    + ", true));");
                        }

                        @Override
                        public void onError(JSONObject details, String error) {
                            mKadecot.callJsOnKadecotMyPage(errorListener
                                    + "(JSON.parse(\"[]\"), "
                                    + "JSON.parse(\"{\\\"error\\\":\\\"" + error + "\\\"}\")"
                                    + ");");
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void subscribe(String topic, String options, final String subscribedListener,
            final String subscribeErrorListener, final String eventListener) {
        if (!mProxy.isOpen()) {
            try {
                open();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG, "CALL, Can not open WebSocket");
                return;
            } catch (TimeoutException e) {
                e.printStackTrace();
                Log.e(TAG, "CALL, Can not open WebSocket");
                return;
            }
        }

        try {
            mClient.subscribe(topic, new JSONObject(options), new WampSubscribeListener() {

                @Override
                public void onSubscribed(int subscriptionId) {
                    mKadecot.callJsOnKadecotMyPage(subscribedListener + "(" + subscriptionId + ");");
                }

                @Override
                public void onError(JSONObject details, String error) {
                    mKadecot.callJsOnKadecotMyPage(subscribeErrorListener
                            + "(JSON.parse(\"[]\"), "
                            + "JSON.parse(\"{\\\"error\\\":\\\"" + error + "\\\"}\")"
                            + ");");
                }

                @Override
                public void onEvent(JSONObject details, JSONObject argumentsKw) {
                    mKadecot.callJsOnKadecotMyPage(eventListener
                            + "(JSON.parse(\"[]\"), JSON.parse("
                            + JsonEscapeUtil.escapeSlash(argumentsKw) + "));");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void subscribe(int subscriptionId, final String unsubscribedListener,
            final String unsubscribeErrorListener) {

        if (!mProxy.isOpen()) {
            try {
                open();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG, "CALL, Can not open WebSocket");
                return;
            } catch (TimeoutException e) {
                e.printStackTrace();
                Log.e(TAG, "CALL, Can not open WebSocket");
                return;
            }
        }

        mClient.unsubscribe(subscriptionId, new WampUnsubscribeListener() {

            @Override
            public void onUnsubscribed() {
                mKadecot.callJsOnKadecotMyPage(unsubscribedListener + "();");
            }

            @Override
            public void onError(JSONObject details, String error) {
                mKadecot.callJsOnKadecotMyPage(unsubscribeErrorListener + "(" + error + ");");
            }
        });
    }
}
