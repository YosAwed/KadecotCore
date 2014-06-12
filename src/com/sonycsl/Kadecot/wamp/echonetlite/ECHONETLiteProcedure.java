/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.wamp.echonetlite;

public enum ECHONETLiteProcedure {

    GET("com.sonycsl.kadecot.echonetlite.procedure.get"),
    SET("com.sonycsl.kadecot.echonetlite.procedure.set"),
    NOT_PROCEDURE("not.procedure");

    private String procedure;

    ECHONETLiteProcedure(String procedure) {
        this.procedure = procedure;
    }

    public String toString() {
        return procedure;
    }

    public static ECHONETLiteProcedure getEnum(String procedure) {
        ECHONETLiteProcedure enumArray[] = ECHONETLiteProcedure.values();

        for (ECHONETLiteProcedure enumElement : enumArray) {
            if (procedure.equals(enumElement.toString()))
                return enumElement;
        }

        return NOT_PROCEDURE;
    }
}
