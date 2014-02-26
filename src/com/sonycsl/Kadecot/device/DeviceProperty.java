
package com.sonycsl.Kadecot.device;

/**
 * デバイスのプロパティ 名前と値の組 successとmessageはsetやgetの返答の際に使う． messageはエラーメッセージなどが入る
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

    public DeviceProperty(String name) {
        this.name = name;
        this.value = null;
    }

    public DeviceProperty(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public DeviceProperty(String name, Object value, boolean success) {
        this.name = name;
        this.value = value;
        this.success = success;
    }
}
