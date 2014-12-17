/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.sonycsl.Kadecot.core.R;

public class WebViewDialogFragment extends DialogFragment {

    private static final String URL_KEY = "url";
    private static final String TITLE_KEY = "title";
    private DialogInterface.OnClickListener mListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface arg0, int arg1) {
        }
    };

    public static WebViewDialogFragment getInstance(String title, String url) {
        WebViewDialogFragment fragment = new WebViewDialogFragment();
        Bundle args = new Bundle();
        args.putString(URL_KEY, url);
        args.putString(TITLE_KEY, title);
        fragment.setArguments(args);
        return fragment;
    }

    public WebViewDialogFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof DialogInterface.OnClickListener) {
            mListener = (DialogInterface.OnClickListener) activity;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View content = inflater.inflate(R.layout.dialog_webview, null);

        final WebView webview = (WebView) content.findViewById(R.id.webview);
        final TextView loading = (TextView) content.findViewById(R.id.loading);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(getArguments().getString(TITLE_KEY))
                .setView(content)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton(android.R.string.ok, mListener).create();
        dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        loading.setVisibility(View.VISIBLE);
        webview.setVisibility(View.INVISIBLE);

        webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                loading.setVisibility(View.INVISIBLE);
                webview.setVisibility(View.VISIBLE);
            }
        });
        webview.loadUrl(getArguments().getString(URL_KEY));

        return dialog;
    }
}
