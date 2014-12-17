/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.service;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;

import com.sonycsl.Kadecot.preference.DeveloperModePreference;
import com.sonycsl.Kadecot.preference.PersistentModePreference;
import com.sonycsl.Kadecot.preference.WebSocketServerPreference;
import com.sonycsl.Kadecot.wamp.util.WampLocatorCallback;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.transport.ProxyPeer;
import com.sonycsl.wamp.transport.Transportable;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import org.json.JSONArray;
import org.json.JSONException;

public class RequestHandler extends Handler {

    private static final String TAG = RequestHandler.class.getSimpleName();

    private SparseArray<WampPeer> mClients;
    private SparseArray<Message> mConns;
    private WampLocatorCallback mLocator;

    Messenger replyMessenger;

    private Context mContext;

    public RequestHandler(Context context) {
        mClients = new SparseArray<WampPeer>();
        mConns = new SparseArray<Message>();
        mContext = context.getApplicationContext();
    }

    public void setWampLocatorCallback(WampLocatorCallback callback) {
        mLocator = callback;
    }

    @Override
    public void handleMessage(final Message msg) {
        if (msg.what != KadecotService.MSGR_INTERFACE_VERSION) {
            return;
        }

        Bundle bundle = msg.getData();

        if (bundle == null) {
            return;
        }

        Message copyMsg = Message.obtain();
        copyMsg.copyFrom(msg);

        if (bundle.containsKey(KadecotService.MSGR_KEY_CONNECT)) {
            if (msg.replyTo != null) {
                final int replyClientId = WampRequestIdGenerator.getId();
                mConns.put(replyClientId, copyMsg);

                Message reply = Message.obtain(null, copyMsg.what, replyClientId, copyMsg.arg2);
                reply.getData().putBoolean(KadecotService.MSGR_KEY_CONNECT, true);
                try {
                    copyMsg.replyTo.send(reply);
                } catch (RemoteException e) {
                    Log.e(TAG, "Can not reply Connect message because of RemoteException");
                }
            }
        }

        if (mConns.get(copyMsg.arg1) == null) {
            return;
        }

        for (String key : bundle.keySet()) {
            if (KadecotService.MSGR_KEY_REQ_WAMP.equals(key)) {
                receiveWampMsg(bundle, copyMsg.replyTo, copyMsg.what, copyMsg.arg1, copyMsg.arg2);
            } else if (KadecotService.MSGR_KEY_ENABLE_WS.equals(key)) {
                receiveEnableWebsocket(bundle, copyMsg.replyTo, copyMsg.what, copyMsg.arg1,
                        copyMsg.arg2);
            } else if (KadecotService.MSGR_KEY_GET_WS_STATUS.equals(key)) {
                receiveGetWebsocketStatus(bundle, copyMsg.replyTo, copyMsg.what, copyMsg.arg1,
                        copyMsg.arg2);
            } else if (KadecotService.MSGR_KEY_ENABLE_DEVMODE.equals(key)) {
                receiveEnableDevMode(bundle, copyMsg.replyTo, copyMsg.what, copyMsg.arg1,
                        copyMsg.arg2);
            } else if (KadecotService.MSGR_KEY_GET_DEVMODE_STATUS.equals(key)) {
                receiveGetDevMode(bundle, copyMsg.replyTo, copyMsg.what, copyMsg.arg1, copyMsg.arg2);
            }
        }
    }

    private void receiveWampMsg(Bundle bundle, Messenger replyMsgr, final int protocolVersion,
            final int clientId, final int requestId) {
        WampPeer peer = mClients.get(clientId);
        if (peer == null) {
            final ProxyPeer proxy = new ProxyPeer(new Transportable<WampMessage>() {

                @Override
                public void send(WampMessage data) {
                    Message reply = Message.obtain(null, protocolVersion, clientId,
                            requestId);
                    reply.getData()
                            .putString(KadecotService.MSGR_KEY_REQ_WAMP, data.toString());
                    try {
                        mConns.get(clientId).replyTo.send(reply);
                    } catch (RemoteException e) {
                        Log.e(TAG,
                                "Can not Send Wamp message because of RemoteException");
                    }
                }
            });
            mLocator.locate(proxy);
            mClients.put(clientId, proxy);
            peer = proxy;
        }

        final String wampString = bundle.getString(KadecotService.MSGR_KEY_REQ_WAMP);
        try {
            peer.transmit(WampMessageFactory.create(new JSONArray(wampString)));
        } catch (JSONException e) {
            Log.e(TAG, "Not a WAMP message");
        }

    }

    private void receiveEnableWebsocket(Bundle bundle, Messenger replyMsgr,
            final int protocolVersion,
            final int clientId, final int requestId) {
        if (bundle.getBoolean(KadecotService.MSGR_KEY_ENABLE_WS)) {
            WebSocketServerPreference.setEnabled(mContext, true);
            PersistentModePreference.setEnabled(mContext, true);
        } else {
            WebSocketServerPreference.setEnabled(mContext, false);
            PersistentModePreference.setEnabled(mContext, false);
        }
    }

    private void receiveGetWebsocketStatus(Bundle bundle, Messenger replyMsgr,
            final int protocolVersion,
            final int clientId, final int requestId) {
        Message response = Message.obtain(null, protocolVersion, clientId, requestId);
        response.getData().putBoolean(KadecotService.MSGR_KEY_GET_WS_STATUS,
                WebSocketServerPreference.isEnabled(mContext));
        try {
            mConns.get(clientId).replyTo.send(response);
        } catch (RemoteException e) {
            Log.e(TAG, "Can not reply GetWebSocketStatus message because of RemoteException");
        }

    }

    private void receiveEnableDevMode(Bundle bundle, Messenger replyMsgr,
            final int protocolVersion,
            final int clientId, final int requestId) {
        if (bundle.getBoolean(KadecotService.MSGR_KEY_ENABLE_DEVMODE)) {
            DeveloperModePreference.setEnabled(mContext, true);
        } else {
            DeveloperModePreference.setEnabled(mContext, false);
        }
    }

    private void receiveGetDevMode(Bundle bundle, Messenger replyMsgr,
            final int protocolVersion,
            final int clientId, final int requestId) {
        Message response = Message.obtain(null, protocolVersion, clientId, requestId);
        response.getData().putBoolean(KadecotService.MSGR_KEY_GET_DEVMODE_STATUS,
                DeveloperModePreference.isEnabled(mContext));
        try {
            mConns.get(clientId).replyTo.send(response);
        } catch (RemoteException e) {
            Log.e(TAG, "Can not reply GetDevMode Response message because of RemoteException");
        }
    }

    public void sendWebsocketStatus(boolean enable) {
        for (int msgi = 0; msgi < mConns.size(); msgi++) {
            Message cpConnMsg = Message.obtain();
            cpConnMsg.copyFrom(mConns.valueAt(msgi));

            Message msg = Message.obtain(null, cpConnMsg.what, cpConnMsg.arg1,
                    WampRequestIdGenerator.getId());
            msg.getData().putBoolean(KadecotService.MSGR_KEY_WS_STATUS, enable);
            try {
                cpConnMsg.replyTo.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "Can not Send Web socket status because of RemoteException");
            }
        }
    }

    public void sendDeveloperModeStatus(boolean enable) {
        for (int msgi = 0; msgi < mConns.size(); msgi++) {
            Message cpConnMsg = Message.obtain();
            cpConnMsg.copyFrom(mConns.valueAt(msgi));

            Message msg = Message.obtain(null, cpConnMsg.what, cpConnMsg.arg1,
                    WampRequestIdGenerator.getId());
            msg.getData().putBoolean(KadecotService.MSGR_KEY_DEVMODE_STATUS, enable);
            try {
                cpConnMsg.replyTo.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "Can not Send developer mode status because of RemoteException");
            }
        }
    }
}
