
package com.sonycsl.test.wamp.mock;

import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.role.WampCallee;

import org.json.JSONObject;

public class MockWampCallee extends WampCallee {

    @Override
    protected WampMessage invocation(String procedure, WampMessage msg) {
        return WampMessageFactory.createYield(msg.asInvocationMessage().getRequestId(),
                new JSONObject());
    }
}
