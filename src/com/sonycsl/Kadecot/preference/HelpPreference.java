/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.sonycsl.Kadecot.core.R;

public final class HelpPreference {

    private HelpPreference() {
        super();
    }

    public static boolean isChecked(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                context.getString(R.string.preferences_file_name), Context.MODE_PRIVATE);
        return sp.getBoolean(context.getString(R.string.help_preference_key), false);
    }

    public static void set(Context context, boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.preferences_file_name), Context.MODE_PRIVATE);

        if (sharedPreferences.getBoolean(context.getString(R.string.help_preference_key), false) != value) {
            Editor editor = sharedPreferences.edit();
            editor.putBoolean(context.getString(R.string.help_preference_key), value);
            editor.apply();
        }
    }
}
