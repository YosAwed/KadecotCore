/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.sonycsl.Kadecot.core.R;
import com.sonycsl.Kadecot.preference.AccountSettingsPreference;

public class HelpDialogFragment extends DialogFragment {
    public static final String HELP_URL = "http://app.kadecot.net/android_start_tutorial/index.html";

    public HelpDialogFragment() {
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View content = inflater.inflate(R.layout.dialog_help, null);

        final WebView webview = (WebView) content.findViewById(R.id.help_view);
        final TextView loading = (TextView) content.findViewById(R.id.help_loading);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_help)
                .setView(content)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showAccountSettingsIfNeeded();
                    }
                }).create();
        dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        loading.setVisibility(View.VISIBLE);
        webview.setVisibility(View.INVISIBLE);

        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                loading.setVisibility(View.INVISIBLE);
                webview.setVisibility(View.VISIBLE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && url.startsWith("http")) {
                    view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } else {
                    return false;
                }
            }
        });
        webview.loadUrl(HELP_URL);

        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        showAccountSettingsIfNeeded();
    }

    private void showAccountSettingsIfNeeded() {
        Context context = getActivity();
        if (context == null) {
            return;
        }

        if (!AccountSettingsPreference.getName(getActivity()).equals("")) {
            return;
        }

        final String tag = AccountSettingsDialogFragment.class.getSimpleName();
        Fragment fragment = getFragmentManager().findFragmentByTag(tag);
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        if (fragment != null) {
            ft.remove(fragment);
        }

        fragment = AccountSettingsDialogFragment.getInstance(getActivity().getString(
                R.string.title_please_setup_your_kadecot_account));
        ft.add(fragment, tag);
        ft.commitAllowingStateLoss();
    }
}
