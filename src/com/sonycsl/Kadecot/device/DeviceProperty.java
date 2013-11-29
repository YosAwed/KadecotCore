package com.sonycsl.Kadecot.device;


/**
 * 
 * デバイスのプロパティ
 * 名前と値の組
 * 
 * successとmessageはsetやgetの返答の際に使う．
 * messageはエラーメッセージなどが入る
 *
 */
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
