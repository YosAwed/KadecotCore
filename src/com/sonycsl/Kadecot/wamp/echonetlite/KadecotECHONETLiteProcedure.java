/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.wamp.echonetlite;

public enum KadecotECHONETLiteProcedure {

    CHANGE_NICK_NAME("com.sonycsl.Kadecot.echonetlite.procedure.changeNickname"),
    DELETE_DEVICE_DATA("com.sonycsl.Kadecot.echonetlite.procedure.deleteDeviceData"),
    DELETE_INACTIVE_DEVICES("com.sonycsl.Kadecot.echonetlite.procedure.deleteinactivedevices"),
    GET("com.sonycsl.Kadecot.echonetlite.procedure.get"),
    SET("com.sonycsl.Kadecot.echonetlite.procedure.set"),
    GET_DEVICE_LIST("com.sonycsl.Kadecot.echonetlite.procedure.getdevicelist"),
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
