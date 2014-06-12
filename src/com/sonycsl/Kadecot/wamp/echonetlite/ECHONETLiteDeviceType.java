/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.wamp.echonetlite;

// TODO: collect class code
// TODO: add all echo devices
public enum ECHONETLiteDeviceType {
    CONTROLLER((short) 1535, "Controller"),
    GENERAL_LIGHTING((short) 656, "GeneralLighting"),
    HOME_AIR_CONDITIONER((short) 304, "HomeAirConditioner"),
    TEMPERATURE_SENSOR((short) 17, "TemperatureSensor"),
    RAIN_SENSOR((short) 19, "RainSensor"),
    HUMIDITY_SENSOR((short) 18, "HumiditySensor"),
    AIR_SPEED_SENSOR((short) 31, "AirSpeedSensor"),
    SNOW_SENSOR((short) 44, "SnowSensor"),

    UNKNOWN((short) 0, "unknown");

    short classCode;
    String name;

    private ECHONETLiteDeviceType(short cCode, String name) {
        this.classCode = cCode;
        this.name = name;
    }

    public short getClassCode() {
        return classCode;
    }

    public String getName() {
        return name;
    }

    public static ECHONETLiteDeviceType getType(short cCode) {
        for (ECHONETLiteDeviceType type : ECHONETLiteDeviceType.values()) {
            if (cCode == type.getClassCode()) {
                return type;
            }
        }

        return UNKNOWN;
    }

    public static ECHONETLiteDeviceType getType(String name) {
        for (ECHONETLiteDeviceType type : ECHONETLiteDeviceType.values()) {
            if (name.equals(type.getName())) {
                return type;
            }
        }

        return UNKNOWN;
    }

}
