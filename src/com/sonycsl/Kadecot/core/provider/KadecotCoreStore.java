
package com.sonycsl.Kadecot.core.provider;

import android.net.Uri;

public class KadecotCoreStore {

    public static final String AUTHORITY = "com.sonycsl.kadecot.core.provider";

    private static final String CONTENT_AUTHORITY_SLASH = "content://" + AUTHORITY + "/";

    public static final class Devices {

        public static final Uri CONTENT_URI = Uri.parse(CONTENT_AUTHORITY_SLASH + "devices");

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

            public static final String UTC_UPDATED = "utc_updated";

            public static final String LOCAL_UPDATED = "local_updated";
        }
    }

    public static final class Topics {

        public static final Uri CONTENT_URI = Uri.parse(CONTENT_AUTHORITY_SLASH + "topics");

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

        public static final Uri CONTENT_URI = Uri.parse(CONTENT_AUTHORITY_SLASH + "procedures");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/procedures";

        public static final String ENTRY_CONTENT_TYPE = "vnd.android.cursor.item/procedure";

        public interface ProcedureColumns {

            public static final String PROTOCOL = "protocol";

            public static final String NAME = "name";

            public static final String DESCRIPTION = "description";
        }
    }
}
