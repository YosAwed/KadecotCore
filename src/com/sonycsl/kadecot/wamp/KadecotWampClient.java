
package com.sonycsl.kadecot.wamp;

import com.sonycsl.wamp.WampClient;

import java.util.Set;

abstract public class KadecotWampClient extends WampClient {

    abstract public Set<String> getSubscribableTopics();

    abstract public Set<String> getRegisterableProcedures();
}
