
package com.sonycsl.Kadecot.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtils {
    @SuppressWarnings("unused")
    private static final String TAG = FileUtils.class.getSimpleName();

    // private final FileUtils self = this;

    private FileUtils() {
    }

    public static String inputStreamToString(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        StringBuffer sb = new StringBuffer();
        String str;
        while ((str = br.readLine()) != null) {
            sb.append(str + "\n");
        }
        return sb.toString();
    }
}
