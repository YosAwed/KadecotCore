
package com.sonycsl.Kadecot.core;

public class MyPageCall {
    @SuppressWarnings("unused")
    private static final String TAG = MyPageCall.class.getSimpleName();

    private final MyPageCall self = this;

    private final KadecotCoreActivity mKadecot;

    public MyPageCall(KadecotCoreActivity kadecot) {
        mKadecot = kadecot;
    }

    public void postMessage(final String message) {

        mKadecot.callJsOnKadecotMyPage("kHAPI.app.onMsgFromApp(null," + message + ")");
        /*
         * StringBuilder builder = new StringBuilder();
         * builder.append("if(\"onMsgFromUserApp\" in window){onMsgFromUserApp(");
         * builder.append(message); builder.append(");};"); mKadecot.callJsOnKadecotMyPage(new
         * String(builder));
         */
    }
}
