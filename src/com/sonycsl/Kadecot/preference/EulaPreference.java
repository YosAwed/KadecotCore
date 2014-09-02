/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.sonycsl.Kadecot.app.EulaActivity;
import com.sonycsl.Kadecot.core.R;

public final class EulaPreference {

    private EulaPreference() {
        super();
    }

    public static boolean isAgreed(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                context.getString(R.string.preferences_file_name), Context.MODE_PRIVATE);
        return sp.getBoolean(EulaActivity.EULA_LABEL, false);
    }
}
