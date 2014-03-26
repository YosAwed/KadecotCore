
package com.sonycsl.wamp;

import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

abstract public class WampClient extends WampPeer {

    private final Map<WampMessenger, Integer> mSessionMap;

    public WampClient() {
        super();
        mSessionMap = new ConcurrentHashMap<WampMessenger, Integer>();
    }

    public WampClient(WampClient next) {
        super(next);
        mSessionMap = new ConcurrentHashMap<WampMessenger, Integer>();
    }

    @Override
    protected final boolean consumeMessage(WampMessenger friend, WampMessage msg) {
        if (consumeMyMessage(friend, msg)) {
            onConsumed(msg);
            return true;
        }

        return consumeRoleMessage(friend, msg);
    }

    private boolean consumeMyMessage(WampMessenger friend, WampMessage msg) {

        if (msg.isWelcomeMessage()) {
            final WampWelcomeMessage welcome = msg.asWelcomeMessage();
            mSessionMap.put(friend, Integer.valueOf(welcome.getSession()));
            return true;
        }

        if (msg.isAbortMessage() || msg.isErrorMessage()) {
            /* delegate */
            return false;
        }

        if (mSessionMap.get(friend) == null) {
            throw new IllegalStateException("session closed");
        }

        if (msg.isGoodbyeMessage()) {
            mSessionMap.remove(friend);
            String reason = msg.asGoodbyeMessage().getReason();
            if (reason.equals(WampError.SYSTEM_SHUTDOWN) || reason.equals(WampError.CLOSE_REALM)) {
                friend.send(WampMessageFactory.createGoodbye(new JSONObject(),
                        WampError.GOODBYE_AND_OUT));
            }
            return true;
        }

        if (msg.isChallengeMessage()) {
            /*
             * TODO future support
             */
            return false;
        }

        if (msg.isHeartbeatMessage()) {
            /*
             * TODO future support
             */
            return false;
        }

        return false;
    }

    abstract protected boolean consumeRoleMessage(WampMessenger friend, WampMessage msg);

    abstract protected void onConsumed(WampMessage msg);
}
