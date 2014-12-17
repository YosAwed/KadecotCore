/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.sonycsl.Kadecot.core.R;

public class AccountSettingsPreference {

    private AccountSettingsPreference() {

    }

    public static void setName(Context context, String name) {
        set(context, R.string.account_name_preference_key, name);
    }

    public static String getName(Context context) {
        return get(context, R.string.account_name_preference_key);
    }

    public static void setPass(Context context, String name) {
        set(context, R.string.account_pass_preference_key, name);
    }

    public static String getPass(Context context) {
        return get(context, R.string.account_pass_preference_key);
    }

    private static void set(Context context, int resId, String name) {
        SharedPreferences sp = context.getSharedPreferences(
                context.getString(R.string.preferences_file_name), Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putString(context.getString(resId), name);
        editor.apply();
    }

    private static String get(Context context, int resId) {
        SharedPreferences sp = context.getSharedPreferences(
                context.getString(R.string.preferences_file_name), Context.MODE_PRIVATE);
        return sp.getString(context.getString(resId), "");
    }
}
