/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.wamp.util;

import com.sonycsl.wamp.WampPeer;

public interface WampLocatorCallback {
    public void locate(WampPeer peer);
}
