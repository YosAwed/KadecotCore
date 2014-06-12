
package com.sonycsl.Kadecot.wamp.echonetlite;

public enum ECHONETLitePropertyValue {
    // common
    ON("0x30"),
    OFF("0x31"),

    // aircon //
    // Operation power-saving
    POWER_SAVING_MODE("0x41"),
    NORMAL_MODE("0x42"),
    // Operation mode setting
    AUTOMATIC("0x41"),
    COOLING("0x42"),
    HEATING("0x43"),
    DEHUMIDIFICATION("0x44"),
    AIR_CIRCULATOR("0x45"),
    OTHER("0x40"),
    // Automatic temperature control setting
    // AUTOMATIC("0x41"), // already exists
    NON_AUTOMATIC("0x42"),
    // Normal/highspeed/silent operation setting
    NORMAL_OPERATION("0x41"),
    HIGH_SPEED_OPERRATION("0x42"),
    SILENT_OPERATION("0x43"),

    // other
    UNKNOWN("unknown");

    private String value;

    ECHONETLitePropertyValue(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }

    public static ECHONETLitePropertyValue getPropertyValue(String value) {
        for (ECHONETLitePropertyValue element : ECHONETLitePropertyValue.values()) {
            if (value.equals(element.toString())) {
                return element;
            }
        }

        return UNKNOWN;
    }
}
