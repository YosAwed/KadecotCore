/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.utils;

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
