
package com.sonycsl.Kadecot.device;

/**
 * Deviceの情報・状態 主にDatabaseに登録されていない情報
 */
public class DeviceInfo {
    @SuppressWarnings("unused")
    private static final String TAG = DeviceInfo.class.getSimpleName();

    private final DeviceInfo self = this;

    public final boolean active;

    public final String deviceName;

    public final String deviceType;

    public final String parent;

    public DeviceInfo(boolean active, String deviceName, String deviceType, String parent) {
        this.active = active;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.parent = parent;
    }

}
