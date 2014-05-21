/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.wamp.echonetlite;

public enum KadecotECHONETLiteProcedure {

    GET("com.sonycsl.kadecot.echonetlite.procedure.get"),
    SET("com.sonycsl.kadecot.echonetlite.procedure.set"),
    NOT_PROCEDURE("not.procedure");

    private String procedure;

    KadecotECHONETLiteProcedure(String procedure) {
        this.procedure = procedure;
    }

    public String getString() {
        return procedure;
    }

    public static KadecotECHONETLiteProcedure getEnum(String procedure) {
        KadecotECHONETLiteProcedure enumArray[] = KadecotECHONETLiteProcedure.values();

        for (KadecotECHONETLiteProcedure enumElement : enumArray) {
            if (procedure.equals(enumElement.getString()))
                return enumElement;
        }

        return NOT_PROCEDURE;
    }
}
