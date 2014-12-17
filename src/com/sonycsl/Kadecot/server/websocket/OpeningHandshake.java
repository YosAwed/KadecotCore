/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.server.websocket;

import org.apache.http.NameValuePair;

import java.util.List;

public interface OpeningHandshake {
    public String getHost();

    public String getUpgrade();

    public String getConnection();

    public String getSecWebSocketKey();

    public String getOrigin();

    public String getSecWebSocketProtocol();

    public String getSecWebSocketVersion();

    public String getFieldValue(String field);

    public List<NameValuePair> getParameters();

    public String getParameter(String name);
}
