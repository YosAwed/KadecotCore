
package com.sonycsl.Kadecot.core;

public class ExitApp {

    private KadecotCoreActivity mActivity;

    public ExitApp(KadecotCoreActivity activity) {
        mActivity = activity;
    }

    public void exitActivity() {

        mActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mActivity.finish();
            }

        });
    }
}
