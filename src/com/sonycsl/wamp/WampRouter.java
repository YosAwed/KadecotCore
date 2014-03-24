
package com.sonycsl.wamp;

import org.json.JSONArray;
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

    private boolean consumeMyMessage(WampMessenger friend, JSONArray msg) {
        int messageType = WampMessage.extractMessageType(msg);
        switch (messageType) {
            case WampMessage.HELLO:
                int sessionId = ++mSessionId;
                mSessionMap.put(friend, sessionId);
                friend.send(WampMessageFactory.createWelcome(sessionId, new JSONObject())
                        .toJSONArray());
                return true;
            case WampMessage.GOODBYE:
                mSessionMap.remove(friend);
                friend.send(WampMessageFactory.createGoodbye(new JSONObject(), "wamp.error")
                        .toJSONArray());
                return true;
        }

        // If WAMP is not established, discard message
        if (mSessionMap.get(friend) == null) {
            throw new IllegalAccessError("Illegal client access");
        }

        return false;
    }

    @Override
    protected final boolean consumeMessage(WampMessenger friend, JSONArray msg) {
        // Handle Router message
        if (consumeMyMessage(friend, msg)) {
            return true;
        }

        return consumeRoleMessage(friend, msg);

    }

    protected abstract boolean consumeRoleMessage(WampMessenger friend, JSONArray msg);

}
