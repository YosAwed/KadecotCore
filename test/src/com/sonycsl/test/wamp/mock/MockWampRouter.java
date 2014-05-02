
package com.sonycsl.test.wamp.mock;

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
