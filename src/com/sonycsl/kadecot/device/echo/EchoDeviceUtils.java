/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.device.echo;

import com.sonycsl.echo.eoj.device.airconditioner.AirCleaner;
import com.sonycsl.echo.eoj.device.airconditioner.AirConditionerVentilationFan;
import com.sonycsl.echo.eoj.device.airconditioner.ElectricHeater;
import com.sonycsl.echo.eoj.device.airconditioner.FanHeater;
import com.sonycsl.echo.eoj.device.airconditioner.HomeAirConditioner;
import com.sonycsl.echo.eoj.device.airconditioner.Humidifier;
import com.sonycsl.echo.eoj.device.airconditioner.PackageTypeCommercialAirConditionerIndoorUnit;
import com.sonycsl.echo.eoj.device.airconditioner.PackageTypeCommercialAirConditionerOutdoorUnit;
import com.sonycsl.echo.eoj.device.airconditioner.VentilationFan;
import com.sonycsl.echo.eoj.device.audiovisual.Display;
import com.sonycsl.echo.eoj.device.audiovisual.Television;
import com.sonycsl.echo.eoj.device.cookinghousehold.ClothesDryer;
import com.sonycsl.echo.eoj.device.cookinghousehold.CombinationMicrowaveOven;
import com.sonycsl.echo.eoj.device.cookinghousehold.CookingHeater;
import com.sonycsl.echo.eoj.device.cookinghousehold.ElectricHotWaterPot;
import com.sonycsl.echo.eoj.device.cookinghousehold.Refrigerator;
import com.sonycsl.echo.eoj.device.cookinghousehold.RiceCooker;
import com.sonycsl.echo.eoj.device.cookinghousehold.WasherAndDryer;
import com.sonycsl.echo.eoj.device.cookinghousehold.WashingMachine;
import com.sonycsl.echo.eoj.device.health.Weighing;
import com.sonycsl.echo.eoj.device.housingfacilities.BathroomHeaterAndDryer;
import com.sonycsl.echo.eoj.device.housingfacilities.Battery;
import com.sonycsl.echo.eoj.device.housingfacilities.Buzzer;
import com.sonycsl.echo.eoj.device.housingfacilities.ColdOrHotWaterHeatSourceEquipment;
import com.sonycsl.echo.eoj.device.housingfacilities.ElectricLock;
import com.sonycsl.echo.eoj.device.housingfacilities.ElectricShutter;
import com.sonycsl.echo.eoj.device.housingfacilities.ElectricStormWindow;
import com.sonycsl.echo.eoj.device.housingfacilities.ElectricToiletSeat;
import com.sonycsl.echo.eoj.device.housingfacilities.ElectricWaterHeater;
import com.sonycsl.echo.eoj.device.housingfacilities.ElectricallyOperatedShade;
import com.sonycsl.echo.eoj.device.housingfacilities.FloorHeater;
import com.sonycsl.echo.eoj.device.housingfacilities.FuelCell;
import com.sonycsl.echo.eoj.device.housingfacilities.GasMeter;
import com.sonycsl.echo.eoj.device.housingfacilities.GeneralLighting;
import com.sonycsl.echo.eoj.device.housingfacilities.HouseholdSolarPowerGeneration;
import com.sonycsl.echo.eoj.device.housingfacilities.InstantaneousWaterHeater;
import com.sonycsl.echo.eoj.device.housingfacilities.LPGasMeter;
import com.sonycsl.echo.eoj.device.housingfacilities.PowerDistributionBoardMetering;
import com.sonycsl.echo.eoj.device.housingfacilities.SmartElectricEnergyMeter;
import com.sonycsl.echo.eoj.device.housingfacilities.SmartGasMeter;
import com.sonycsl.echo.eoj.device.housingfacilities.Sprinkler;
import com.sonycsl.echo.eoj.device.housingfacilities.WaterFlowmeter;
import com.sonycsl.echo.eoj.device.housingfacilities.WattHourMeter;
import com.sonycsl.echo.eoj.device.managementoperation.Controller;
import com.sonycsl.echo.eoj.device.managementoperation.Switch;
import com.sonycsl.echo.eoj.device.sensor.ActivityAmountSensor;
import com.sonycsl.echo.eoj.device.sensor.AirPollutionSensor;
import com.sonycsl.echo.eoj.device.sensor.AirSpeedSensor;
import com.sonycsl.echo.eoj.device.sensor.BathHeatingStatusSensor;
import com.sonycsl.echo.eoj.device.sensor.BathWaterLevelSensor;
import com.sonycsl.echo.eoj.device.sensor.BedPresenceSensor;
import com.sonycsl.echo.eoj.device.sensor.CO2Sensor;
import com.sonycsl.echo.eoj.device.sensor.CallSensor;
import com.sonycsl.echo.eoj.device.sensor.CigaretteSmokeSensor;
import com.sonycsl.echo.eoj.device.sensor.CondensationSensor;
import com.sonycsl.echo.eoj.device.sensor.CrimePreventionSensor;
import com.sonycsl.echo.eoj.device.sensor.CurrentValueSensor;
import com.sonycsl.echo.eoj.device.sensor.DifferentialPressureSensor;
import com.sonycsl.echo.eoj.device.sensor.EarthquakeSensor;
import com.sonycsl.echo.eoj.device.sensor.ElectricEnergySensor;
import com.sonycsl.echo.eoj.device.sensor.ElectricLeakSensor;
import com.sonycsl.echo.eoj.device.sensor.EmergencyButton;
import com.sonycsl.echo.eoj.device.sensor.FireSensor;
import com.sonycsl.echo.eoj.device.sensor.FirstAidSensor;
import com.sonycsl.echo.eoj.device.sensor.FlameSensor;
import com.sonycsl.echo.eoj.device.sensor.GasLeakSensor;
import com.sonycsl.echo.eoj.device.sensor.GasSensor;
import com.sonycsl.echo.eoj.device.sensor.HumanBodyLocationSensor;
import com.sonycsl.echo.eoj.device.sensor.HumanDetectionSensor;
import com.sonycsl.echo.eoj.device.sensor.HumiditySensor;
import com.sonycsl.echo.eoj.device.sensor.IlluminanceSensor;
import com.sonycsl.echo.eoj.device.sensor.MailingSensor;
import com.sonycsl.echo.eoj.device.sensor.MicromotionSensor;
import com.sonycsl.echo.eoj.device.sensor.OdorSensor;
import com.sonycsl.echo.eoj.device.sensor.OpenCloseSensor;
import com.sonycsl.echo.eoj.device.sensor.OxygenSensor;
import com.sonycsl.echo.eoj.device.sensor.PassageSensor;
import com.sonycsl.echo.eoj.device.sensor.RainSensor;
import com.sonycsl.echo.eoj.device.sensor.SnowSensor;
import com.sonycsl.echo.eoj.device.sensor.SoundSensor;
import com.sonycsl.echo.eoj.device.sensor.TemperatureSensor;
import com.sonycsl.echo.eoj.device.sensor.VOCSensor;
import com.sonycsl.echo.eoj.device.sensor.VisitorSensor;
import com.sonycsl.echo.eoj.device.sensor.WaterFlowRateSensor;
import com.sonycsl.echo.eoj.device.sensor.WaterLeakSensor;
import com.sonycsl.echo.eoj.device.sensor.WaterLevelSensor;
import com.sonycsl.echo.eoj.device.sensor.WaterOverflowSensor;
import com.sonycsl.echo.eoj.device.sensor.WeightSensor;

import java.util.HashMap;

public class EchoDeviceUtils {
    @SuppressWarnings("unused")
    private static final String TAG = EchoDeviceUtils.class.getSimpleName();

    // private final EchoUtils self = this;
    private static final HashMap<Short, String> sClassNameMap = new HashMap<Short, String>();

    static {
        sClassNameMap.put(ActivityAmountSensor.ECHO_CLASS_CODE, ActivityAmountSensor.class
                .getSimpleName());
        sClassNameMap.put(AirPollutionSensor.ECHO_CLASS_CODE, AirPollutionSensor.class
                .getSimpleName());
        sClassNameMap.put(AirSpeedSensor.ECHO_CLASS_CODE, AirSpeedSensor.class.getSimpleName());
        sClassNameMap.put(BathHeatingStatusSensor.ECHO_CLASS_CODE, BathHeatingStatusSensor.class
                .getSimpleName());
        sClassNameMap.put(BathWaterLevelSensor.ECHO_CLASS_CODE, BathWaterLevelSensor.class
                .getSimpleName());
        sClassNameMap.put(BedPresenceSensor.ECHO_CLASS_CODE, BedPresenceSensor.class
                .getSimpleName());
        sClassNameMap.put(CallSensor.ECHO_CLASS_CODE, CallSensor.class.getSimpleName());
        sClassNameMap.put(CigaretteSmokeSensor.ECHO_CLASS_CODE, CigaretteSmokeSensor.class
                .getSimpleName());
        sClassNameMap.put(CO2Sensor.ECHO_CLASS_CODE, CO2Sensor.class.getSimpleName());
        sClassNameMap.put(CondensationSensor.ECHO_CLASS_CODE, CondensationSensor.class
                .getSimpleName());
        sClassNameMap.put(CrimePreventionSensor.ECHO_CLASS_CODE, CrimePreventionSensor.class
                .getSimpleName());
        sClassNameMap.put(CurrentValueSensor.ECHO_CLASS_CODE, CurrentValueSensor.class
                .getSimpleName());
        sClassNameMap.put(DifferentialPressureSensor.ECHO_CLASS_CODE,
                DifferentialPressureSensor.class.getSimpleName());
        sClassNameMap.put(EarthquakeSensor.ECHO_CLASS_CODE, EarthquakeSensor.class.getSimpleName());
        sClassNameMap.put(ElectricEnergySensor.ECHO_CLASS_CODE, ElectricEnergySensor.class
                .getSimpleName());
        sClassNameMap.put(ElectricLeakSensor.ECHO_CLASS_CODE, ElectricLeakSensor.class
                .getSimpleName());
        sClassNameMap.put(EmergencyButton.ECHO_CLASS_CODE, EmergencyButton.class.getSimpleName());
        sClassNameMap.put(FireSensor.ECHO_CLASS_CODE, FireSensor.class.getSimpleName());
        sClassNameMap.put(FirstAidSensor.ECHO_CLASS_CODE, FirstAidSensor.class.getSimpleName());
        sClassNameMap.put(FlameSensor.ECHO_CLASS_CODE, FlameSensor.class.getSimpleName());
        sClassNameMap.put(GasLeakSensor.ECHO_CLASS_CODE, GasLeakSensor.class.getSimpleName());
        sClassNameMap.put(GasSensor.ECHO_CLASS_CODE, GasSensor.class.getSimpleName());
        sClassNameMap.put(HumanBodyLocationSensor.ECHO_CLASS_CODE, HumanBodyLocationSensor.class
                .getSimpleName());
        sClassNameMap.put(HumanDetectionSensor.ECHO_CLASS_CODE, HumanDetectionSensor.class
                .getSimpleName());
        sClassNameMap.put(HumiditySensor.ECHO_CLASS_CODE, HumiditySensor.class.getSimpleName());
        sClassNameMap.put(IlluminanceSensor.ECHO_CLASS_CODE, IlluminanceSensor.class
                .getSimpleName());
        sClassNameMap.put(MailingSensor.ECHO_CLASS_CODE, MailingSensor.class.getSimpleName());
        sClassNameMap.put(MicromotionSensor.ECHO_CLASS_CODE, MicromotionSensor.class
                .getSimpleName());
        sClassNameMap.put(OdorSensor.ECHO_CLASS_CODE, OdorSensor.class.getSimpleName());
        sClassNameMap.put(OpenCloseSensor.ECHO_CLASS_CODE, OpenCloseSensor.class.getSimpleName());
        sClassNameMap.put(OxygenSensor.ECHO_CLASS_CODE, OxygenSensor.class.getSimpleName());
        sClassNameMap.put(PassageSensor.ECHO_CLASS_CODE, PassageSensor.class.getSimpleName());
        sClassNameMap.put(RainSensor.ECHO_CLASS_CODE, RainSensor.class.getSimpleName());
        sClassNameMap.put(SnowSensor.ECHO_CLASS_CODE, SnowSensor.class.getSimpleName());
        sClassNameMap.put(SoundSensor.ECHO_CLASS_CODE, SoundSensor.class.getSimpleName());
        sClassNameMap.put(TemperatureSensor.ECHO_CLASS_CODE, TemperatureSensor.class
                .getSimpleName());
        sClassNameMap.put(VisitorSensor.ECHO_CLASS_CODE, VisitorSensor.class.getSimpleName());
        sClassNameMap.put(VOCSensor.ECHO_CLASS_CODE, VOCSensor.class.getSimpleName());
        sClassNameMap.put(WaterFlowRateSensor.ECHO_CLASS_CODE, WaterFlowRateSensor.class
                .getSimpleName());
        sClassNameMap.put(WaterLeakSensor.ECHO_CLASS_CODE, WaterLeakSensor.class.getSimpleName());
        sClassNameMap.put(WaterLevelSensor.ECHO_CLASS_CODE, WaterLevelSensor.class.getSimpleName());
        sClassNameMap.put(WaterOverflowSensor.ECHO_CLASS_CODE, WaterOverflowSensor.class
                .getSimpleName());
        sClassNameMap.put(WeightSensor.ECHO_CLASS_CODE, WeightSensor.class.getSimpleName());
        sClassNameMap.put(AirCleaner.ECHO_CLASS_CODE, AirCleaner.class.getSimpleName());
        sClassNameMap.put(AirConditionerVentilationFan.ECHO_CLASS_CODE,
                AirConditionerVentilationFan.class.getSimpleName());
        sClassNameMap.put(ElectricHeater.ECHO_CLASS_CODE, ElectricHeater.class.getSimpleName());
        sClassNameMap.put(FanHeater.ECHO_CLASS_CODE, FanHeater.class.getSimpleName());
        sClassNameMap.put(HomeAirConditioner.ECHO_CLASS_CODE, HomeAirConditioner.class
                .getSimpleName());
        sClassNameMap.put(Humidifier.ECHO_CLASS_CODE, Humidifier.class.getSimpleName());
        sClassNameMap.put(PackageTypeCommercialAirConditionerIndoorUnit.ECHO_CLASS_CODE,
                PackageTypeCommercialAirConditionerIndoorUnit.class.getSimpleName());
        sClassNameMap.put(PackageTypeCommercialAirConditionerOutdoorUnit.ECHO_CLASS_CODE,
                PackageTypeCommercialAirConditionerOutdoorUnit.class.getSimpleName());
        sClassNameMap.put(VentilationFan.ECHO_CLASS_CODE, VentilationFan.class.getSimpleName());
        sClassNameMap.put(BathroomHeaterAndDryer.ECHO_CLASS_CODE, BathroomHeaterAndDryer.class
                .getSimpleName());
        sClassNameMap.put(Battery.ECHO_CLASS_CODE, Battery.class.getSimpleName());
        sClassNameMap.put(Buzzer.ECHO_CLASS_CODE, Buzzer.class.getSimpleName());
        sClassNameMap.put(ColdOrHotWaterHeatSourceEquipment.ECHO_CLASS_CODE,
                ColdOrHotWaterHeatSourceEquipment.class.getSimpleName());
        sClassNameMap.put(ElectricallyOperatedShade.ECHO_CLASS_CODE,
                ElectricallyOperatedShade.class.getSimpleName());
        sClassNameMap.put(ElectricLock.ECHO_CLASS_CODE, ElectricLock.class.getSimpleName());
        sClassNameMap.put(ElectricShutter.ECHO_CLASS_CODE, ElectricShutter.class.getSimpleName());
        sClassNameMap.put(ElectricStormWindow.ECHO_CLASS_CODE, ElectricStormWindow.class
                .getSimpleName());
        sClassNameMap.put(ElectricToiletSeat.ECHO_CLASS_CODE, ElectricToiletSeat.class
                .getSimpleName());
        sClassNameMap.put(ElectricWaterHeater.ECHO_CLASS_CODE, ElectricWaterHeater.class
                .getSimpleName());
        sClassNameMap.put(FloorHeater.ECHO_CLASS_CODE, FloorHeater.class.getSimpleName());
        sClassNameMap.put(FuelCell.ECHO_CLASS_CODE, FuelCell.class.getSimpleName());
        sClassNameMap.put(GasMeter.ECHO_CLASS_CODE, GasMeter.class.getSimpleName());
        sClassNameMap.put(GeneralLighting.ECHO_CLASS_CODE, GeneralLighting.class.getSimpleName());
        sClassNameMap.put(HouseholdSolarPowerGeneration.ECHO_CLASS_CODE,
                HouseholdSolarPowerGeneration.class.getSimpleName());
        sClassNameMap.put(InstantaneousWaterHeater.ECHO_CLASS_CODE, InstantaneousWaterHeater.class
                .getSimpleName());
        sClassNameMap.put(LPGasMeter.ECHO_CLASS_CODE, LPGasMeter.class.getSimpleName());
        sClassNameMap.put(PowerDistributionBoardMetering.ECHO_CLASS_CODE,
                PowerDistributionBoardMetering.class.getSimpleName());
        sClassNameMap.put(SmartElectricEnergyMeter.ECHO_CLASS_CODE, SmartElectricEnergyMeter.class
                .getSimpleName());
        sClassNameMap.put(SmartGasMeter.ECHO_CLASS_CODE, SmartGasMeter.class.getSimpleName());
        sClassNameMap.put(Sprinkler.ECHO_CLASS_CODE, Sprinkler.class.getSimpleName());
        sClassNameMap.put(WaterFlowmeter.ECHO_CLASS_CODE, WaterFlowmeter.class.getSimpleName());
        sClassNameMap.put(WattHourMeter.ECHO_CLASS_CODE, WattHourMeter.class.getSimpleName());
        sClassNameMap.put(ClothesDryer.ECHO_CLASS_CODE, ClothesDryer.class.getSimpleName());
        sClassNameMap.put(CombinationMicrowaveOven.ECHO_CLASS_CODE, CombinationMicrowaveOven.class
                .getSimpleName());
        sClassNameMap.put(CookingHeater.ECHO_CLASS_CODE, CookingHeater.class.getSimpleName());
        sClassNameMap.put(ElectricHotWaterPot.ECHO_CLASS_CODE, ElectricHotWaterPot.class
                .getSimpleName());
        sClassNameMap.put(Refrigerator.ECHO_CLASS_CODE, Refrigerator.class.getSimpleName());
        sClassNameMap.put(RiceCooker.ECHO_CLASS_CODE, RiceCooker.class.getSimpleName());
        sClassNameMap.put(WasherAndDryer.ECHO_CLASS_CODE, WasherAndDryer.class.getSimpleName());
        sClassNameMap.put(WashingMachine.ECHO_CLASS_CODE, WashingMachine.class.getSimpleName());
        sClassNameMap.put(Weighing.ECHO_CLASS_CODE, Weighing.class.getSimpleName());
        sClassNameMap.put(Controller.ECHO_CLASS_CODE, Controller.class.getSimpleName());
        sClassNameMap.put(Switch.ECHO_CLASS_CODE, Switch.class.getSimpleName());
        sClassNameMap.put(Display.ECHO_CLASS_CODE, Display.class.getSimpleName());
        sClassNameMap.put(Television.ECHO_CLASS_CODE, Television.class.getSimpleName());
    }

    private EchoDeviceUtils() {

    }

    public static String getClassName(short echoClassCode) {
        String s = sClassNameMap.get(echoClassCode);
        if (s == null)
            return "UnknownDevice";
        return s;
    }

    public static void putClassName(short echoClassCode, String name) {
        sClassNameMap.put(echoClassCode, name);
    }
}
