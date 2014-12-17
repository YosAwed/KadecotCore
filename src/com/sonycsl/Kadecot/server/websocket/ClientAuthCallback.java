/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.server.websocket;

import java.util.Set;

public interface ClientAuthCallback {
    public boolean isAuthenticated(OpeningHandshake handshake);

    public Set<String> getScopeSet(OpeningHandshake handshake);
}
