/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.sonycsl.Kadecot.core.R;
import com.sonycsl.Kadecot.preference.EulaPreference;

public class EulaActivity extends Activity {

    public static String EULA_LABEL = "EULA";
    private Activity self = this;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_eula);

        WebView webView = (WebView) findViewById(R.id.eura_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        SharedPreferences sharedPreferences = getSharedPreferences(
                getString(R.string.preferences_file_name), Context.MODE_PRIVATE);

        FinishInterface finish = new FinishInterface() {
            @Override
            public void finish() {
                self.finish();
            }
        };

        OpenBrowserInterface openBrowser = new OpenBrowserInterface() {
            @Override
            public void openBrowser(Intent intent) {
                startActivity(intent);
            }
        };

        JsObject js = new JsObject(sharedPreferences, finish, openBrowser);

        webView.addJavascriptInterface(js, "andjs");
        webView.loadUrl("file:///android_asset/agreement.html");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && !EulaPreference.isAgreed(this)) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public interface FinishInterface {
        public void finish();
    }

    public interface OpenBrowserInterface {
        public void openBrowser(Intent intent);
    }

    private static final class JsObject {
        private SharedPreferences mPreference;
        private FinishInterface mFinish;
        private OpenBrowserInterface mOpenBrowser;

        public JsObject(SharedPreferences preference, FinishInterface finishInterface,
                OpenBrowserInterface broadcastInterface) {
            mPreference = preference;
            mFinish = finishInterface;
            mOpenBrowser = broadcastInterface;
        }

        @JavascriptInterface
        public void agree() {
            Editor editor = mPreference.edit();
            editor.putBoolean(EULA_LABEL, true);
            editor.apply();
            mFinish.finish();
        }

        @JavascriptInterface
        public void openKadecotNet() {
            Uri uri = Uri.parse("http://kadecot.net");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            mOpenBrowser.openBrowser(intent);
        }

        @JavascriptInterface
        public void openTutorial() {
            Uri uri = Uri.parse("http://kadecot.net/tutorial");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            mOpenBrowser.openBrowser(intent);
        }
    }
}
