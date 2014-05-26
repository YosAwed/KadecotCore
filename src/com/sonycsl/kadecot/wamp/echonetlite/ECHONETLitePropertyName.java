
package com.sonycsl.kadecot.wamp.echonetlite;

public enum ECHONETLitePropertyName {
    OPERATION_STATUS("OperationStatus"),
    UNKNOWN("UNKNOWN");

    private String name;

    private ECHONETLitePropertyName(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    public static String translate(String paramName) {
        if (paramName.equals(OPERATION_STATUS.toString())) {
            return "0x80";
        } else {
            // paramName may be EPC
            return paramName;
        }
    }

    public static ECHONETLitePropertyName getPropertyName(String name) {
        for (ECHONETLitePropertyName element : ECHONETLitePropertyName.values()) {
            if (name.equals(element.toString())) {
                return element;
            }
        }

        return UNKNOWN;
    }
}
