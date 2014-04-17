
package com.sonycsl.test.wamp.mock;

import com.sonycsl.wamp.role.WampRole;

public class MockWampRouter extends MockWampPeer {

    @Override
    protected WampRole getRole() {
        return new MockWampRouterRole();
    }

}
