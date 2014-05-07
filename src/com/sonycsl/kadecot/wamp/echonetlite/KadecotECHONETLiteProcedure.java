/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.wamp.echonetlite;

public enum KadecotECHONETLiteProcedure {

    CHANGE_NICK_NAME("com.sonycsl.kadecot.echonetlite.procedure.changeNickname"),
    DELETE_DEVICE_DATA("com.sonycsl.kadecot.echonetlite.procedure.deleteDeviceData"),
    DELETE_INACTIVE_DEVICES("com.sonycsl.kadecot.echonetlite.procedure.deleteinactivedevices"),
    GET("com.sonycsl.kadecot.echonetlite.procedure.get"),
    SET("com.sonycsl.kadecot.echonetlite.procedure.set"),
    GET_DEVICE_LIST("com.sonycsl.kadecot.echonetlite.procedure.getdevicelist"),
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
