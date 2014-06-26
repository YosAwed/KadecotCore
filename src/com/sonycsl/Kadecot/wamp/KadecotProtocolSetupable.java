
package com.sonycsl.Kadecot.wamp;

import java.util.Map;

public interface KadecotProtocolSetupable {

    public Map<String, String> getSubscribableTopics();

    public Map<String, String> getRegisterableProcedures();
}
