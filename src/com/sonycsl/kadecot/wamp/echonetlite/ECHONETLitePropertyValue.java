
package com.sonycsl.kadecot.wamp.echonetlite;

public enum ECHONETLitePropertyValue {
    ON("0x30"),
    OFF("0x31"),
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
