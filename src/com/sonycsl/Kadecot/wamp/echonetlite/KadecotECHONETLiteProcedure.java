
package com.sonycsl.Kadecot.wamp.echonetlite;

public enum KadecotECHONETLiteProcedure {

    GET("com.sonycsl.Kadecot.echonetlite.procedure.get"),
    SET("com.sonycsl.Kadecot.echonetlite.procedure.set");

    private String procedure;

    KadecotECHONETLiteProcedure(String procedure) {
        this.procedure = procedure;
    }

    public String getString() {
        return procedure;
    }
}
