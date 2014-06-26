
package com.sonycsl.Kadecot.wamp.echonetlite;

import android.util.SparseArray;

import java.util.Collection;
import java.util.HashSet;

public final class ECHONETLiteTopicGenerator {

    // TODO: raw フォルダに json ファイルをおいてすべての EPC Name を生成する
    private static final String PREFIX = ECHONETLiteClient.BASE_URI + ".topic";
    private static final SparseArray<String> CLASS_NAMES;
    private static final SparseArray<SparseArray<String>> EPCS;

    private static SparseArray<String> createSuperClassEpcNames() {
        SparseArray<String> epcNames = new SparseArray<String>();
        epcNames.put(0x80, "OperationStatus");
        epcNames.put(0x81, "InstallationLocation");

        epcNames.put(0x82, "StandardVersionInformation");
        epcNames.put(0x83, "IdentificationNumber");
        epcNames.put(0x84, "MeasuredInstantaneousPowerConsumption");
        epcNames.put(0x85, "MeasuredCumulativePowerConsumption");
        epcNames.put(0x86, "ManufacturesFaultCode");
        epcNames.put(0x87, "CurrentLimitSetting");

        epcNames.put(0x88, "FaultStatus");

        epcNames.put(0x89, "FaultDescription");
        epcNames.put(0x8A, "ManifacturerCode");
        epcNames.put(0x8B, "BusinessFacilityCode");
        epcNames.put(0x8C, "ProductCode");
        epcNames.put(0x8D, "ProductionNumber");
        epcNames.put(0x8E, "ProductionDate");
        epcNames.put(0x8F, "PowerSavingOperationSetting");
        epcNames.put(0x93, "RemoteControlSetting");
        epcNames.put(0x97, "CurrentTimeSetting");
        epcNames.put(0x98, "CurrentDateSetting");
        epcNames.put(0x99, "PowerLimitSetting");
        epcNames.put(0x9A, "CumulativeOperatingTime");
        epcNames.put(0x9B, "SetMPropertyMap");
        epcNames.put(0x9C, "GetMPropertyMap");
        epcNames.put(0x9D, "StatusChangeAnnouncementPropertyMap");
        epcNames.put(0x9E, "SetPropertyMap");
        epcNames.put(0x9F, "GetPropertyMap");

        return epcNames;
    }

    static {
        CLASS_NAMES = new SparseArray<String>();
        EPCS = new SparseArray<SparseArray<String>>();
        SparseArray<String> epcNames;

        /* HomeAirConditioner 0x01, 0x30 */
        CLASS_NAMES.put(0x0130, "HomeAirConditioner");
        epcNames = createSuperClassEpcNames();
        epcNames.put(0x80, "OperationStatus");
        epcNames.put(0x8F, "OperationPowerSaving");
        epcNames.put(0xB0, "OperationModeSetting");
        epcNames.put(0xB1, "AutomaticTemperatureControlSetting");
        epcNames.put(0xB2, "NormalHighSpeedSilentOperationSetting");
        epcNames.put(0xB3, "SetTemperatureValue");
        epcNames.put(0xB4, "SetValueOfRerativeHumidityInDehumidifyingMode");
        epcNames.put(0xB5, "SetTemperatureValueInCoolingMode");
        epcNames.put(0xB6, "SetTemperatureValueInHeatingMode");
        epcNames.put(0xB7, "SetTemperatureValueInDehumidifyingMode");
        epcNames.put(0xB8, "RatedPowerConsumption");
        epcNames.put(0xB9, "MeasuredValueOfCurrentConsumption");
        epcNames.put(0xBA, "MeasuredValueOfRoomRelativeHumidify");
        epcNames.put(0xBB, "MeasuredValueOfRoomTemperature");
        epcNames.put(0xBC, "SetTemperatureValueOfUserRemoteControl");
        epcNames.put(0xBD, "MeasuredCooledAirTemperature");
        epcNames.put(0xBE, "MeasuredOutdoorAirTemperature");
        epcNames.put(0xBF, "RelativeTemperatureSetting");
        epcNames.put(0xA0, "AirFlowRateSetting");
        epcNames.put(0xA1, "AutomaticControlOfAirFlowDirectionSetting");
        epcNames.put(0xA3, "AutomaticSwingOfAirFlowSetting");
        epcNames.put(0xA4, "AirFlowDirectionVerticalSetting");
        epcNames.put(0xA5, "AirFlowDirectionHorizontalSetting");
        epcNames.put(0xAA, "SpecialState");
        epcNames.put(0xAB, "NonPriorityState");
        epcNames.put(0xC0, "VentilationFunctionSetting");
        epcNames.put(0xC1, "HumidifierFunctionSetting");
        epcNames.put(0xC2, "VentilationAirFlowRateSetting");
        epcNames.put(0xC4, "DegreeOfHumidificationSetting");
        epcNames.put(0xC6, "MountedAirCleaningMethod");
        epcNames.put(0xC7, "AirPurifierFunctionSetting");
        epcNames.put(0xC8, "MountedAirRefreshMethod");
        epcNames.put(0xC9, "AirRefresherFunctionSetting");
        epcNames.put(0xCA, "MountedSelfCleaningMethod");
        epcNames.put(0xCB, "SelfCleaningFunctionSetting");
        epcNames.put(0xCC, "SpecialFunctionSetting");
        epcNames.put(0xCD, "OperationStatusOfComponents");
        epcNames.put(0xCE, "ThermostatSettingOverrideFunction");
        epcNames.put(0xCF, "AirPurificationModeSetting");
        epcNames.put(0x90, "OnTimerBasedReservationSetting");
        epcNames.put(0x91, "OnTimerTimeSetting");
        epcNames.put(0x92, "OnTimerRelativeTimeSetting");
        epcNames.put(0x94, "OffTimerBasedReservationSetting");
        epcNames.put(0x95, "OffTimerTimeSetting");
        epcNames.put(0x96, "OffTimerRelativeTimeSetting");
        EPCS.put(0x0130, epcNames);
    }

    static String getTopic(short classCode, String propertyName) {
        int epc = Integer.decode(propertyName);

        if (CLASS_NAMES.get(classCode) == null || EPCS.get(classCode) == null
                || EPCS.get(classCode).get(epc) == null) {
            return PREFIX + "." + Integer.toHexString(classCode) + "." + propertyName;
        }

        return PREFIX + "." + CLASS_NAMES.get(classCode) + "." + EPCS.get(classCode).get(epc);
    }

    static Collection<String> getTopics() {
        Collection<String> topics = new HashSet<String>();
        for (int classi = 0; classi < CLASS_NAMES.size(); classi++) {
            int className = CLASS_NAMES.keyAt(classi);
            for (int epci = 0; epci < EPCS.size(); epci++) {
                SparseArray<String> epc = EPCS.get(EPCS.keyAt(epci));
                for (int epcni = 0; epcni < epc.size(); epcni++) {
                    topics.add(getTopic((short) className, String.valueOf(epc.keyAt(epcni))));
                }
            }
        }
        return topics;
    }
}
