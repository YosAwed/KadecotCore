package com.sonycsl.Kadecot.device.echo;

import java.util.HashMap;

import com.sonycsl.echo.eoj.device.airconditioner.*;
import com.sonycsl.echo.eoj.device.audiovisual.*;
import com.sonycsl.echo.eoj.device.cookinghousehold.*;
import com.sonycsl.echo.eoj.device.health.*;
import com.sonycsl.echo.eoj.device.housingfacilities.*;
import com.sonycsl.echo.eoj.device.managementoperation.*;
import com.sonycsl.echo.eoj.device.sensor.*;

public class EchoDeviceUtils {
	@SuppressWarnings("unused")
	private static final String TAG = EchoDeviceUtils.class.getSimpleName();
	//private final EchoUtils self = this;
	private static final HashMap<Short, String> sClassNameMap = new HashMap<Short, String>();
	
	static{
		sClassNameMap.put(ActivityAmountSensor.ECHO_CLASS_CODE, ActivityAmountSensor.class.getSimpleName());
		sClassNameMap.put(AirPollutionSensor.ECHO_CLASS_CODE, AirPollutionSensor.class.getSimpleName());
		sClassNameMap.put(AirSpeedSensor.ECHO_CLASS_CODE, AirSpeedSensor.class.getSimpleName());
		sClassNameMap.put(BathHeatingStatusSensor.ECHO_CLASS_CODE, BathHeatingStatusSensor.class.getSimpleName());
		sClassNameMap.put(BathWaterLevelSensor.ECHO_CLASS_CODE, BathWaterLevelSensor.class.getSimpleName());
		sClassNameMap.put(BedPresenceSensor.ECHO_CLASS_CODE, BedPresenceSensor.class.getSimpleName());
		sClassNameMap.put(CallSensor.ECHO_CLASS_CODE, CallSensor.class.getSimpleName());
		sClassNameMap.put(CigaretteSmokeSensor.ECHO_CLASS_CODE, CigaretteSmokeSensor.class.getSimpleName());
		sClassNameMap.put(CO2Sensor.ECHO_CLASS_CODE, CO2Sensor.class.getSimpleName());
		sClassNameMap.put(CondensationSensor.ECHO_CLASS_CODE, CondensationSensor.class.getSimpleName());
		sClassNameMap.put(CrimePreventionSensor.ECHO_CLASS_CODE, CrimePreventionSensor.class.getSimpleName());
		sClassNameMap.put(CurrentValueSensor.ECHO_CLASS_CODE, CurrentValueSensor.class.getSimpleName());
		sClassNameMap.put(DifferentialPressureSensor.ECHO_CLASS_CODE, DifferentialPressureSensor.class.getSimpleName());
		sClassNameMap.put(EarthquakeSensor.ECHO_CLASS_CODE, EarthquakeSensor.class.getSimpleName());
		sClassNameMap.put(ElectricEnergySensor.ECHO_CLASS_CODE, ElectricEnergySensor.class.getSimpleName());
		sClassNameMap.put(ElectricLeakSensor.ECHO_CLASS_CODE, ElectricLeakSensor.class.getSimpleName());
		sClassNameMap.put(EmergencyButton.ECHO_CLASS_CODE, EmergencyButton.class.getSimpleName());
		sClassNameMap.put(FireSensor.ECHO_CLASS_CODE, FireSensor.class.getSimpleName());
		sClassNameMap.put(FirstAidSensor.ECHO_CLASS_CODE, FirstAidSensor.class.getSimpleName());
		sClassNameMap.put(FlameSensor.ECHO_CLASS_CODE, FlameSensor.class.getSimpleName());
		sClassNameMap.put(GasLeakSensor.ECHO_CLASS_CODE, GasLeakSensor.class.getSimpleName());
		sClassNameMap.put(GasSensor.ECHO_CLASS_CODE, GasSensor.class.getSimpleName());
		sClassNameMap.put(HumanBodyLocationSensor.ECHO_CLASS_CODE, HumanBodyLocationSensor.class.getSimpleName());
		sClassNameMap.put(HumanDetectionSensor.ECHO_CLASS_CODE, HumanDetectionSensor.class.getSimpleName());
		sClassNameMap.put(HumiditySensor.ECHO_CLASS_CODE, HumiditySensor.class.getSimpleName());
		sClassNameMap.put(IlluminanceSensor.ECHO_CLASS_CODE, IlluminanceSensor.class.getSimpleName());
		sClassNameMap.put(MailingSensor.ECHO_CLASS_CODE, MailingSensor.class.getSimpleName());
		sClassNameMap.put(MicromotionSensor.ECHO_CLASS_CODE, MicromotionSensor.class.getSimpleName());
		sClassNameMap.put(OdorSensor.ECHO_CLASS_CODE, OdorSensor.class.getSimpleName());
		sClassNameMap.put(OpenCloseSensor.ECHO_CLASS_CODE, OpenCloseSensor.class.getSimpleName());
		sClassNameMap.put(OxygenSensor.ECHO_CLASS_CODE, OxygenSensor.class.getSimpleName());
		sClassNameMap.put(PassageSensor.ECHO_CLASS_CODE, PassageSensor.class.getSimpleName());
		sClassNameMap.put(RainSensor.ECHO_CLASS_CODE, RainSensor.class.getSimpleName());
		sClassNameMap.put(SnowSensor.ECHO_CLASS_CODE, SnowSensor.class.getSimpleName());
		sClassNameMap.put(SoundSensor.ECHO_CLASS_CODE, SoundSensor.class.getSimpleName());
		sClassNameMap.put(TemperatureSensor.ECHO_CLASS_CODE, TemperatureSensor.class.getSimpleName());
		sClassNameMap.put(VisitorSensor.ECHO_CLASS_CODE, VisitorSensor.class.getSimpleName());
		sClassNameMap.put(VOCSensor.ECHO_CLASS_CODE, VOCSensor.class.getSimpleName());
		sClassNameMap.put(WaterFlowRateSensor.ECHO_CLASS_CODE, WaterFlowRateSensor.class.getSimpleName());
		sClassNameMap.put(WaterLeakSensor.ECHO_CLASS_CODE, WaterLeakSensor.class.getSimpleName());
		sClassNameMap.put(WaterLevelSensor.ECHO_CLASS_CODE, WaterLevelSensor.class.getSimpleName());
		sClassNameMap.put(WaterOverflowSensor.ECHO_CLASS_CODE, WaterOverflowSensor.class.getSimpleName());
		sClassNameMap.put(WeightSensor.ECHO_CLASS_CODE, WeightSensor.class.getSimpleName());
		sClassNameMap.put(AirCleaner.ECHO_CLASS_CODE, AirCleaner.class.getSimpleName());
		sClassNameMap.put(AirConditionerVentilationFan.ECHO_CLASS_CODE, AirConditionerVentilationFan.class.getSimpleName());
		sClassNameMap.put(ElectricHeater.ECHO_CLASS_CODE, ElectricHeater.class.getSimpleName());
		sClassNameMap.put(FanHeater.ECHO_CLASS_CODE, FanHeater.class.getSimpleName());
		sClassNameMap.put(HomeAirConditioner.ECHO_CLASS_CODE, HomeAirConditioner.class.getSimpleName());
		sClassNameMap.put(Humidifier.ECHO_CLASS_CODE, Humidifier.class.getSimpleName());
		sClassNameMap.put(PackageTypeCommercialAirConditionerIndoorUnit.ECHO_CLASS_CODE, PackageTypeCommercialAirConditionerIndoorUnit.class.getSimpleName());
		sClassNameMap.put(PackageTypeCommercialAirConditionerOutdoorUnit.ECHO_CLASS_CODE, PackageTypeCommercialAirConditionerOutdoorUnit.class.getSimpleName());
		sClassNameMap.put(VentilationFan.ECHO_CLASS_CODE, VentilationFan.class.getSimpleName());
		sClassNameMap.put(BathroomHeaterAndDryer.ECHO_CLASS_CODE, BathroomHeaterAndDryer.class.getSimpleName());
		sClassNameMap.put(Battery.ECHO_CLASS_CODE, Battery.class.getSimpleName());
		sClassNameMap.put(Buzzer.ECHO_CLASS_CODE, Buzzer.class.getSimpleName());
		sClassNameMap.put(ColdOrHotWaterHeatSourceEquipment.ECHO_CLASS_CODE, ColdOrHotWaterHeatSourceEquipment.class.getSimpleName());
		sClassNameMap.put(ElectricallyOperatedShade.ECHO_CLASS_CODE, ElectricallyOperatedShade.class.getSimpleName());
		sClassNameMap.put(ElectricLock.ECHO_CLASS_CODE, ElectricLock.class.getSimpleName());
		sClassNameMap.put(ElectricShutter.ECHO_CLASS_CODE, ElectricShutter.class.getSimpleName());
		sClassNameMap.put(ElectricStormWindow.ECHO_CLASS_CODE, ElectricStormWindow.class.getSimpleName());
		sClassNameMap.put(ElectricToiletSeat.ECHO_CLASS_CODE, ElectricToiletSeat.class.getSimpleName());
		sClassNameMap.put(ElectricWaterHeater.ECHO_CLASS_CODE, ElectricWaterHeater.class.getSimpleName());
		sClassNameMap.put(FloorHeater.ECHO_CLASS_CODE, FloorHeater.class.getSimpleName());
		sClassNameMap.put(FuelCell.ECHO_CLASS_CODE, FuelCell.class.getSimpleName());
		sClassNameMap.put(GasMeter.ECHO_CLASS_CODE, GasMeter.class.getSimpleName());
		sClassNameMap.put(GeneralLighting.ECHO_CLASS_CODE, GeneralLighting.class.getSimpleName());
		sClassNameMap.put(HouseholdSolarPowerGeneration.ECHO_CLASS_CODE, HouseholdSolarPowerGeneration.class.getSimpleName());
		sClassNameMap.put(InstantaneousWaterHeater.ECHO_CLASS_CODE, InstantaneousWaterHeater.class.getSimpleName());
		sClassNameMap.put(LPGasMeter.ECHO_CLASS_CODE, LPGasMeter.class.getSimpleName());
		sClassNameMap.put(PowerDistributionBoardMetering.ECHO_CLASS_CODE, PowerDistributionBoardMetering.class.getSimpleName());
		sClassNameMap.put(SmartElectricEnergyMeter.ECHO_CLASS_CODE, SmartElectricEnergyMeter.class.getSimpleName());
		sClassNameMap.put(SmartGasMeter.ECHO_CLASS_CODE, SmartGasMeter.class.getSimpleName());
		sClassNameMap.put(Sprinkler.ECHO_CLASS_CODE, Sprinkler.class.getSimpleName());
		sClassNameMap.put(WaterFlowmeter.ECHO_CLASS_CODE, WaterFlowmeter.class.getSimpleName());
		sClassNameMap.put(WattHourMeter.ECHO_CLASS_CODE, WattHourMeter.class.getSimpleName());
		sClassNameMap.put(ClothesDryer.ECHO_CLASS_CODE, ClothesDryer.class.getSimpleName());
		sClassNameMap.put(CombinationMicrowaveOven.ECHO_CLASS_CODE, CombinationMicrowaveOven.class.getSimpleName());
		sClassNameMap.put(CookingHeater.ECHO_CLASS_CODE, CookingHeater.class.getSimpleName());
		sClassNameMap.put(ElectricHotWaterPot.ECHO_CLASS_CODE, ElectricHotWaterPot.class.getSimpleName());
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
	
	private EchoDeviceUtils(){
		
	}

	
	public static String getClassName(short echoClassCode) {
		String s = sClassNameMap.get(echoClassCode);
		if(s == null) return "UnknownDevice";
		return s;
	}
	
	public static void putClassName(short echoClassCode, String name) {
		sClassNameMap.put(echoClassCode, name);
	}
}
