
package com.sonycsl.Kadecot.core;

import android.util.Log;

public class Dbg {
    @SuppressWarnings("unused")
    private static final String TAG = Dbg.class.getSimpleName();

    private final Dbg self = this;

    private Dbg() {
    }

    public static boolean isDebug = true;

    public static void print(Object o) {
        if (isDebug == false) {
            return;
        }
        Throwable t = new Throwable();
        StackTraceElement element = t.getStackTrace()[1];
        String s = element.getClassName();
        String[] ss = s.split("\\.");
        String className = ss[ss.length - 1];
        String methodName = element.getMethodName();
        int lineNumber = element.getLineNumber();

        if (o == null) {
            Log.e("Kadecot", "[Debug]null(class:" + className + ",method:" + methodName + ",line:"
                + lineNumber + ")");
        } else {
            Log.v("Kadecot", "[Debug]" + o.toString() + "(class:" + className + ",method:"
                + methodName + ",line:" + lineNumber + ")");
        }
    }

    public static void print() {
        print("");
    }

}
