/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.wamp.client;

import com.sonycsl.wamp.WampClient;

import java.util.Map;
import java.util.Set;

abstract public class KadecotWampClient extends WampClient {
    abstract public Map<String, String> getSubscribableTopics();

    abstract public Map<String, String> getRegisterableProcedures();

    abstract public Set<String> getTopicsToSubscribe();
}
