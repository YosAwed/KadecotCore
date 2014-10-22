
package com.sonycsl.Kadecot.app;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewFragment;

import com.sonycsl.Kadecot.core.R;

public class LicensesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licenses);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new LicensesFragment())
                    .commit();
        }
    }

    public static class LicensesFragment extends WebViewFragment {

        public LicensesFragment() {
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            WebView wv = getWebView();
            wv.setWebViewClient(new WebViewClient());
            wv.loadUrl("file:///android_asset/licenses.html");
        }

    }
}
