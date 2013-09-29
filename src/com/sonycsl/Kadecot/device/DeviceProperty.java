package com.sonycsl.Kadecot.device;

public class DeviceProperty {
	@SuppressWarnings("unused")
	private static final String TAG = DeviceProperty.class.getSimpleName();
	private final DeviceProperty self = this;
	
	public String name;
	public Object value;

	
	// result
	public boolean success;
	public Object message;
}
