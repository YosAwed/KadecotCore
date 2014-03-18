
package com.sonycsl.wamp;

import com.sonycsl.wamp.message.MessageCreater;
import com.sonycsl.wamp.message.MessageType;

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
        int messageType = MessageType.getMessageType(msg);
        switch (messageType) {
            case MessageType.HELLO:
                int sessionId = ++mSessionId;
                mSessionMap.put(friend, sessionId);
                friend.send(MessageCreater.createWelcomeMessage(sessionId, new JSONObject()));
                return true;
            case MessageType.GOODBYE:
                mSessionMap.remove(friend);
                friend.send(MessageCreater.createGoodbyeMessage(new JSONObject(),
                        "wamp.error"));
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
