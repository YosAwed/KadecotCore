
package com.sonycsl.Kadecot.wamp.echonetlite;

import java.util.Locale;

public enum ECHONETLitePropertyName {
    // epc must be lower case

    // common
    OPERATION_STATUS("OperationStatus", "0x80"),

    // HomeAirConditioner
    OPERATION_POWER_SAVING("OperationPowerSaving", "0x8f"),
    OPERATION_MODE_SETTING("OperationModeSetting", "0xb0"),
    AUTOMATIC_TEMPERATURE_CONTROL_SETTING("AutomaticTemperatureControlSetting", "0xb1"),
    NORMAL_HIGHSPEED_SILENT_OPERATION_SETTING("NormalHighspeedSilentOperationSetting", "0xb2"),
    SET_TEMPERATURE_VALUE("SetTemperatureSetting", "0xb3"),
    SET_VALUE_OF_RELATIVE_HUMIDITY_IN_DEHUMIDIFYING_MODE(
            "SetValueOfRelativeHumidityInDehumidifyingMode", "0xb4"),
    SET_TEMPERATURE_VALUE_IN_COOLING_MODE("SetTemperatureValueInCoolingMode", "0xb5"),
    SET_TEMPERATURE_VALUE_IN_HEATING_MODE("SetTemperatureValueInHeatingMode", "0xb6"),

    // Temperature
    MEASURED_TEMPERATURE_VALUE("MeasuredTemperatureValue", "0xe0"),

    UNKNOWN("UNKNOWN", "");

    private String name;
    private String epc;

    private ECHONETLitePropertyName(String name, String epc) {
        this.name = name;
        this.epc = epc;
    }

    public String toString() {
        return name;
    }

    public String getEpc() {
        return epc;
    }

    public static String translate(String paramName) {
        for (ECHONETLitePropertyName elem : ECHONETLitePropertyName.values()) {
            if (paramName.equals(elem.toString())) {
                return elem.getEpc();
            }
        }
        // paramName may be EPC
        return paramName;
    }

    public static ECHONETLitePropertyName getPropertyName(String name) {
        for (ECHONETLitePropertyName element : ECHONETLitePropertyName.values()) {
            if (name.equals(element.toString())) {
                return element;
            }
        }

        return UNKNOWN;
    }

    public static ECHONETLitePropertyName getPropertyNameFromEpc(String epc) {
        String lowerEpc = epc.toLowerCase(Locale.ENGLISH);
        for (ECHONETLitePropertyName element : ECHONETLitePropertyName.values()) {
            if (lowerEpc.equals(element.getEpc())) {
                return element;
            }
        }

        return UNKNOWN;
    }
}
