/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.sonycsl.Kadecot.core.R;

public final class KadecotServicePreference {

    private KadecotServicePreference() {
        super();
    }

    public static void setPersistentModeEnabled(Context context, boolean enabled) {
        SharedPreferences sp = context.getSharedPreferences(
                context.getString(R.string.preferences_file_name), Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putBoolean(context.getString(R.string.persistent_mode_preference_key), enabled);
        editor.apply();
    }

    public static boolean isPersistentModeEnabled(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                context.getString(R.string.preferences_file_name), Context.MODE_PRIVATE);
        return sp.getBoolean(context.getString(R.string.persistent_mode_preference_key), false);
    }

}
