
package com.sonycsl.Kadecot.plugin;

public class DeviceData {

    private String mProtocol;
    private String mUuid;
    private String mDeviceType;
    private String mDescription;
    private boolean mStatus;
    private String mNetworkAddress;
    private String mNickname;
    private String mLocation;

    private DeviceData(Builder builder) {
        mProtocol = builder.mProtocol;
        mUuid = builder.mUuid;
        mDeviceType = builder.mDeviceType;
        mDescription = builder.mDescription;
        mStatus = builder.mStatus;
        mNetworkAddress = builder.mNetworkAddress;
        mNickname = builder.mNickname;
        mLocation = builder.mLocation;
    }

    public String getProtocol() {
        return mProtocol;
    }

    public String getUuid() {
        return mUuid;
    }

    public String getDeviceType() {
        return mDeviceType;
    }

    public String getDescription() {
        return mDescription;
    }

    public boolean getStatus() {
        return mStatus;
    }

    public String getNetworkAddress() {
        return mNetworkAddress;
    }

    public String getNickname() {
        return mNickname;
    }

    public String getLocation() {
        return mLocation;
    }

    public static class Builder {

        private String mProtocol;
        private String mUuid;
        private String mDeviceType;
        private String mDescription;
        private boolean mStatus;
        private String mNetworkAddress;
        private String mNickname;
        private String mLocation;

        public Builder(String protocol, String uuid, String deviceType,
                String description, boolean status, String networkAddress) {
            mProtocol = protocol;
            mUuid = uuid;
            mDeviceType = deviceType;
            mDescription = description;
            mStatus = status;
            mNetworkAddress = networkAddress;
        }

        public Builder setNickname(String nickname) {
            mNickname = nickname;
            return this;
        }

        public Builder setLocation(String location) {
            mLocation = location;
            return this;
        }

        public DeviceData build() {
            return new DeviceData(this);
        }
    }

}
