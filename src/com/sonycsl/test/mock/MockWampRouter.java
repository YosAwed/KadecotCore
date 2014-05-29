/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.test.mock;

import com.sonycsl.wamp.role.WampRole;

import java.util.HashSet;
import java.util.Set;

public class MockWampRouter extends MockWampPeer {

    @Override
    protected Set<WampRole> getRoleSet() {
        Set<WampRole> roleSet = new HashSet<WampRole>();
        roleSet.add(new MockWampRouterRole());
        return roleSet;
    }

}
