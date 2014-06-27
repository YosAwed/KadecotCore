
package com.sonycsl.test.wamp.role;

import com.sonycsl.wamp.WampError;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.message.WampMessageType;
import com.sonycsl.wamp.role.WampRole;
import com.sonycsl.wamp.role.WampRole.OnReplyListener;

import junit.framework.TestCase;

import org.json.JSONObject;

import java.util.Set;

public class WampRoleTestUtil {
    /**
     * This method check role cannot handle messages. Add message type which
     * role can handle to uncheck arguments.
     * 
     * @param role
     * @param receiver
     * @param uncheck don't check message which uncheck contains
     */
    public static void rxMessageOutOfRole(WampRole role, WampPeer receiver, Set<Integer> uncheck) {
        String realm = "realm";
        int id = 1;
        JSONObject json = new JSONObject();
        String reason = WampError.NO_SUCH_REALM;
        String topic = "topic.test";
        String procedure = "procedure.test";

        OnReplyListener listener = new OnReplyListener() {
            @Override
            public void onReply(WampPeer receiver, WampMessage reply) {
            }
        };

        if (!uncheck.contains(WampMessageType.HELLO)) {
            TestCase.assertFalse(role.resolveRxMessage(receiver,
                    WampMessageFactory.createHello(realm, json),
                    listener));
        }
        if (!uncheck.contains(WampMessageType.WELCOME)) {
            TestCase.assertFalse(role.resolveRxMessage(receiver,
                    WampMessageFactory.createWelcome(id, json),
                    listener));
        }
        if (!uncheck.contains(WampMessageType.ABORT)) {
            TestCase.assertFalse(role.resolveRxMessage(receiver,
                    WampMessageFactory.createAbort(json, reason),
                    listener));
        }
        if (!uncheck.contains(WampMessageType.GOODBYE)) {
            TestCase.assertFalse(role.resolveRxMessage(receiver,
                    WampMessageFactory.createGoodbye(json, reason),
                    listener));
        }
        if (!uncheck.contains(WampMessageType.ERROR)) {
            TestCase.assertFalse(role.resolveRxMessage(receiver,
                    WampMessageFactory.createError(id, id, json, reason), listener));
        }
        if (!uncheck.contains(WampMessageType.PUBLISH)) {
            TestCase.assertFalse(role.resolveRxMessage(receiver,
                    WampMessageFactory.createPublish(id, json, topic), listener));
        }
        if (!uncheck.contains(WampMessageType.PUBLISHED)) {
            TestCase.assertFalse(role.resolveRxMessage(receiver,
                    WampMessageFactory.createPublished(id, id),
                    listener));
        }
        if (!uncheck.contains(WampMessageType.SUBSCRIBE)) {
            TestCase.assertFalse(role.resolveRxMessage(receiver,
                    WampMessageFactory.createSubscribe(id, json, topic), listener));
        }
        if (!uncheck.contains(WampMessageType.SUBSCRIBED)) {
            TestCase.assertFalse(role.resolveRxMessage(receiver,
                    WampMessageFactory.createSubscribed(id, id),
                    listener));
        }
        if (!uncheck.contains(WampMessageType.UNSUBSCRIBE)) {
            TestCase.assertFalse(role.resolveRxMessage(receiver,
                    WampMessageFactory.createUnsubscribe(id, id),
                    listener));
        }
        if (!uncheck.contains(WampMessageType.UNSUBSCRIBED)) {
            TestCase.assertFalse(role.resolveRxMessage(receiver,
                    WampMessageFactory.createUnsubscribed(id),
                    listener));
        }
        if (!uncheck.contains(WampMessageType.EVENT)) {
            TestCase.assertFalse(role.resolveRxMessage(receiver,
                    WampMessageFactory.createEvent(id, id, json),
                    listener));
        }
        if (!uncheck.contains(WampMessageType.CALL)) {
            TestCase.assertFalse(role.resolveRxMessage(receiver,
                    WampMessageFactory.createCall(id, json, procedure), listener));
        }
        if (!uncheck.contains(WampMessageType.RESULT)) {
            TestCase.assertFalse(role.resolveRxMessage(receiver,
                    WampMessageFactory.createResult(id, json),
                    listener));
        }
        if (!uncheck.contains(WampMessageType.REGISTER)) {
            TestCase.assertFalse(role.resolveRxMessage(receiver,
                    WampMessageFactory.createRegister(id, json, procedure), listener));
        }
        if (!uncheck.contains(WampMessageType.REGISTERED)) {
            TestCase.assertFalse(role.resolveRxMessage(receiver,
                    WampMessageFactory.createRegistered(id, id),
                    listener));
        }
        if (!uncheck.contains(WampMessageType.UNREGISTER)) {
            TestCase.assertFalse(role.resolveRxMessage(receiver,
                    WampMessageFactory.createUnregister(id, id),
                    listener));
        }
        if (!uncheck.contains(WampMessageType.UNREGISTERED)) {
            TestCase.assertFalse(role.resolveRxMessage(receiver,
                    WampMessageFactory.createUnregistered(id),
                    listener));
        }
        if (!uncheck.contains(WampMessageType.INVOCATION)) {
            TestCase.assertFalse(role.resolveRxMessage(receiver,
                    WampMessageFactory.createInvocation(id, id, json), listener));
        }
        if (!uncheck.contains(WampMessageType.YIELD)) {
            TestCase.assertFalse(role.resolveRxMessage(receiver,
                    WampMessageFactory.createYield(id, json),
                    listener));
        }
    }

    /**
     * This method check role cannot handle messages. Add message type which
     * role can handle to uncheck arguments.
     * 
     * @param role
     * @param receiver
     * @param uncheck don't check message which uncheck contains
     */
    public static void txMessageOutOfRole(WampRole role, WampPeer receiver, Set<Integer> uncheck) {
        String realm = "realm";
        int id = 1;
        JSONObject json = new JSONObject();
        String reason = WampError.NO_SUCH_REALM;
        String topic = "topic.test";
        String procedure = "procedure.test";

        if (!uncheck.contains(WampMessageType.HELLO)) {
            TestCase.assertFalse(role.resolveTxMessage(receiver,
                    WampMessageFactory.createHello(realm, json)));
        }
        if (!uncheck.contains(WampMessageType.WELCOME)) {
            TestCase.assertFalse(role.resolveTxMessage(receiver,
                    WampMessageFactory.createWelcome(id, json)));
        }
        if (!uncheck.contains(WampMessageType.ABORT)) {
            TestCase.assertFalse(role.resolveTxMessage(receiver,
                    WampMessageFactory.createAbort(json, reason)));
        }
        if (!uncheck.contains(WampMessageType.GOODBYE)) {
            TestCase.assertFalse(role.resolveTxMessage(receiver,
                    WampMessageFactory.createGoodbye(json, reason)));
        }
        if (!uncheck.contains(WampMessageType.ERROR)) {
            TestCase.assertFalse(role.resolveTxMessage(receiver,
                    WampMessageFactory.createError(id, id, json, reason)));
        }
        if (!uncheck.contains(WampMessageType.PUBLISH)) {
            TestCase.assertFalse(role.resolveTxMessage(receiver,
                    WampMessageFactory.createPublish(id, json, topic)));
        }
        if (!uncheck.contains(WampMessageType.PUBLISHED)) {
            TestCase.assertFalse(role.resolveTxMessage(receiver,
                    WampMessageFactory.createPublished(id, id)));
        }
        if (!uncheck.contains(WampMessageType.SUBSCRIBE)) {
            TestCase.assertFalse(role.resolveTxMessage(receiver,
                    WampMessageFactory.createSubscribe(id, json, topic)));
        }
        if (!uncheck.contains(WampMessageType.SUBSCRIBED)) {
            TestCase.assertFalse(role.resolveTxMessage(receiver,
                    WampMessageFactory.createSubscribed(id, id)));
        }
        if (!uncheck.contains(WampMessageType.UNSUBSCRIBE)) {
            TestCase.assertFalse(role.resolveTxMessage(receiver,
                    WampMessageFactory.createUnsubscribe(id, id)));
        }
        if (!uncheck.contains(WampMessageType.UNSUBSCRIBED)) {
            TestCase.assertFalse(role.resolveTxMessage(receiver,
                    WampMessageFactory.createUnsubscribed(id)));
        }
        if (!uncheck.contains(WampMessageType.EVENT)) {
            TestCase.assertFalse(role.resolveTxMessage(receiver,
                    WampMessageFactory.createEvent(id, id, json)));
        }
        if (!uncheck.contains(WampMessageType.CALL)) {
            TestCase.assertFalse(role.resolveTxMessage(receiver,
                    WampMessageFactory.createCall(id, json, procedure)));
        }
        if (!uncheck.contains(WampMessageType.RESULT)) {
            TestCase.assertFalse(role.resolveTxMessage(receiver,
                    WampMessageFactory.createResult(id, json)));
        }
        if (!uncheck.contains(WampMessageType.REGISTER)) {
            TestCase.assertFalse(role.resolveTxMessage(receiver,
                    WampMessageFactory.createRegister(id, json, procedure)));
        }
        if (!uncheck.contains(WampMessageType.REGISTERED)) {
            TestCase.assertFalse(role.resolveTxMessage(receiver,
                    WampMessageFactory.createRegistered(id, id)));
        }
        if (!uncheck.contains(WampMessageType.UNREGISTER)) {
            TestCase.assertFalse(role.resolveTxMessage(receiver,
                    WampMessageFactory.createUnregister(id, id)));
        }
        if (!uncheck.contains(WampMessageType.UNREGISTERED)) {
            TestCase.assertFalse(role.resolveTxMessage(receiver,
                    WampMessageFactory.createUnregistered(id)));
        }
        if (!uncheck.contains(WampMessageType.INVOCATION)) {
            TestCase.assertFalse(role.resolveTxMessage(receiver,
                    WampMessageFactory.createInvocation(id, id, json)));
        }
        if (!uncheck.contains(WampMessageType.YIELD)) {
            TestCase.assertFalse(role.resolveTxMessage(receiver,
                    WampMessageFactory.createYield(id, json)));
        }
    }
}
