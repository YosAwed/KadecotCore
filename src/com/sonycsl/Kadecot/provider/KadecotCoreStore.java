/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.provider;

import android.graphics.Bitmap;
import android.net.Uri;

public class KadecotCoreStore {

    private static final String CONTENT_AUTHORITY_SLASH_ON_DISK = "content://"
            + OnDiskProvider.AUTHORITY + "/";

    public static final class Devices {

        public static final Uri CONTENT_URI = Uri
                .parse(CONTENT_AUTHORITY_SLASH_ON_DISK + "devices");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/devices";

        public static final String ENTRY_CONTENT_TYPE = "vnd.android.cursor.item/device";

        public interface DeviceColumns {

            public static final String DEVICE_ID = "deviceId";

            public static final String PROTOCOL = "protocol";

            public static final String UUID = "uuid";

            public static final String DEVICE_TYPE = "deviceType";

            public static final String DESCRIPTION = "description";

            public static final String STATUS = "status";

            public static final String NICKNAME = "nickname";

            public static final String IP_ADDR = "ip_addr";

            public static final String BSSID = "bssid";

            public static final String LOCATION = "location";

            public static final String SUB_LOCATION = "sub_location";

            public static final String UTC_UPDATED = "utc_updated";

            public static final String LOCAL_UPDATED = "local_updated";
        }
    }

    public static final class Topics {

        public static final Uri CONTENT_URI = Uri.parse(CONTENT_AUTHORITY_SLASH_ON_DISK
                + "topics");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/topics";

        public static final String ENTRY_CONTENT_TYPE = "vnd.android.cursor.item/topic";

        public interface TopicColumns {

            public static final String PROTOCOL = "protocol";

            public static final String NAME = "name";

            public static final String DESCRIPTION = "description";

            public static final String REFERENCE_COUNT = "referenceCount";
        }
    }

    public static final class Procedures {

        public static final Uri CONTENT_URI = Uri.parse(CONTENT_AUTHORITY_SLASH_ON_DISK
                + "procedures");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/procedures";

        public static final String ENTRY_CONTENT_TYPE = "vnd.android.cursor.item/procedure";

        public interface ProcedureColumns {

            public static final String PROTOCOL = "protocol";

            public static final String NAME = "name";

            public static final String DESCRIPTION = "description";
        }
    }

    public static final class AccessPoints {

        public static final Uri CONTENT_URI = Uri.parse(CONTENT_AUTHORITY_SLASH_ON_DISK
                + "accesspoints");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/accesspoints";

        public static final String ENTRY_CONTENT_TYPE = "vnd.android.cursor.item/accesspoint";

        public interface AccessPointColumns {

            public static final String SSID = "ssid";

            public static final String BSSID = "bssid";
        }
    }

    public static final class Handshakes {

        public static final Uri CONTENT_URI = Uri.parse(CONTENT_AUTHORITY_SLASH_ON_DISK
                + "handshakes");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/handshakes";

        public static final String ENTRY_CONTENT_TYPE = "vnd.android.cursor.item/handshake";

        public interface HandshakeColumns {
            public static final String UTC = "utc";

            public static final String LOCALTIME = "localtime";

            public static final String ORIGIN = "origin";

            public static final String STATUS = "status";
        }
    }

    private static final String CONTENT_AUTHORITY_SLASH_IN_MEMORY = "content://"
            + InMemoryProvider.AUTHORITY + "/";

    public static final class Protocols {

        public static final Uri CONTENT_URI = Uri.parse(CONTENT_AUTHORITY_SLASH_IN_MEMORY
                + "protocols");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/protocols";

        public static final String ENTRY_CONTENT_TYPE = "vnd.android.cursor.item/protocols";

        public interface ProtocolColumns {

            public static final String PROTOCOL = "protocol";

            public static final String PACKAGE_NAME = "package";

            public static final String ACTIVITY_NAME = "activityName";
        }
    }

    public static final class DeviceTypes {

        public static final Uri CONTENT_URI = Uri.parse(CONTENT_AUTHORITY_SLASH_IN_MEMORY
                + "deviceTypes");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/deviceTypes";

        public static final String ENTRY_CONTENT_TYPE = "vnd.android.cursor.item/deviceTypes";

        public interface DeviceTypeColumns {

            public static final String DEVICE_TYPE = "deviceType";

            public static final String PROTOCOL = "protocol";

            public static final String ICON = "icon";
        }
    }

    public static class ProtocolData {

        private String mProtocol;
        private String mPackageName;
        private String mActivityName;

        public ProtocolData(String protocol, String packageName, String activityName) {
            mProtocol = protocol;
            mPackageName = packageName;
            mActivityName = activityName;
        }

        public String getProtocol() {
            return mProtocol;
        }

        public String getPackageName() {
            return mPackageName;
        }

        public String getActivityName() {
            return mActivityName;
        }
    }

    public static class DeviceTypeData {

        private String mDeviceType;
        private String mProtocol;
        private Bitmap mIcon;

        public DeviceTypeData(String deviceType, String protocol, Bitmap icon) {
            mDeviceType = deviceType;
            mProtocol = protocol;
            mIcon = icon;
        }

        public String getDeviceType() {
            return mDeviceType;
        }

        public String getProtocol() {
            return mProtocol;
        }

        public Bitmap getIcon() {
            return mIcon;
        }
    }

}
