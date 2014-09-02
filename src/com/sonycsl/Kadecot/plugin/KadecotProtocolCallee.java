/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.plugin;

import com.sonycsl.Kadecot.provider.KadecotCoreStore;
import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.message.WampInvocationMessage;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.role.WampCallee;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

public abstract class KadecotProtocolCallee extends WampCallee {

    Set<String> mProcedureSet;

    public KadecotProtocolCallee(Set<String> procedureSet) {
        mProcedureSet = procedureSet;
    }

    @Override
    protected WampMessage invocation(String procedure, WampMessage msg) {
        if (!msg.isInvocationMessage()) {
            throw new IllegalArgumentException("IllegalMessage:" + msg);
        }

        WampInvocationMessage invocMsg = msg.asInvocationMessage();

        if (!isMyProcedure(procedure)) {
            WampMessageFactory.createError(msg.getMessageType(), invocMsg.getRequestId(),
                    new JSONObject(), WampError.NO_SUCH_PROCEDURE);
        }

        try {
            invocMsg.getDetails().getLong(KadecotCoreStore.Devices.DeviceColumns.DEVICE_ID);
        } catch (JSONException e) {
            e.printStackTrace();
            return WampMessageFactory.createError(msg.getMessageType(), invocMsg.getRequestId(),
                    new JSONObject(), WampError.INVALID_ARGUMENT);
        }

        return resolveInvocationMsg(procedure, invocMsg);
    };

    protected boolean isMyProcedure(String procedure) {
        return mProcedureSet.contains(procedure);
    }

    abstract protected WampMessage resolveInvocationMsg(String procedure,
            WampInvocationMessage invocMsg);
}
