
package com.sonycsl.wamp.util;

import org.json.JSONObject;

public class JsonEscapeUtil {

    public static String escapeSlash(JSONObject object) {
        StringBuilder builder = new StringBuilder();
        builder.append("\"");
        String jsonObjStr = object.toString().replaceAll("\\\\",
                "\\\\\\\\");
        jsonObjStr = jsonObjStr.replaceAll("\\\"", "\\\\\"");
        builder.append(jsonObjStr);
        builder.append("\"");
        return builder.toString();
    }
}
