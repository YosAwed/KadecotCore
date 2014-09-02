/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import com.sonycsl.Kadecot.core.R;
import com.sonycsl.Kadecot.preference.KadecotServicePreference;

public final class WebAppLauncher {

    private WebAppLauncher() {
        super();
    }

    private static void startBrowser(Context context, String url) {
        Uri uri = Uri.parse(url);
        context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    public static void launch(final Context context, final String url) {
        if (KadecotServicePreference.isPersistentModeEnabled(context)) {
            startBrowser(context, url);
            return;
        }

        new AlertDialog.Builder(context).setTitle(R.string.title_background_unavailable)
                .setMessage(R.string.background_confirmation)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        KadecotServicePreference.setPersistentModeEnabled(context, true);
                        startBrowser(context, url);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create().show();
    }
}
