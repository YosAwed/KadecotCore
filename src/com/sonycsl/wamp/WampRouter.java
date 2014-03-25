
package com.sonycsl.wamp;

import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class WampRouter extends WampPeer {

    private int mSessionId = 0;

    private Map<WampMessenger, Integer> mSessionMap = new ConcurrentHashMap<WampMessenger, Integer>();

    public WampRouter() {
    }

    public WampRouter(WampRouter next) {
        super(next);
    }

    private boolean consumeMyMessage(WampMessenger friend, WampMessage msg) {

        if (msg.isHelloMessage()) {
            int sessionId = ++mSessionId;
            mSessionMap.put(friend, sessionId);
            friend.send(WampMessageFactory.createWelcome(sessionId, new JSONObject()));
            return true;
        }

        if (msg.isGoodbyeMessage()) {
            mSessionMap.remove(friend);
            friend.send(WampMessageFactory.createGoodbye(new JSONObject(), "wamp.error"));
            return true;
        }

        // If WAMP is not established, discard message
        if (mSessionMap.get(friend) == null) {
            // TODO: Send Error Messege
            friend.send(WampMessageFactory
                    .createError(msg.getMessageType(), -1, null, "wamp.error"));
        }

        return false;
    }

    @Override
    protected final boolean consumeMessage(WampMessenger friend, WampMessage msg) {
        // Handle Router message
        if (consumeMyMessage(friend, msg)) {
            return true;
        }

        return consumeRoleMessage(friend, msg);

    }

    protected abstract boolean consumeRoleMessage(WampMessenger friend, WampMessage msg);

}
