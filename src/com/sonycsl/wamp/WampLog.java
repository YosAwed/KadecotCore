
package com.sonycsl.wamp;

public final class WampLog {

    private static void log(String name, String tag, String msg) {
        try {
            Class.forName("android.util.Log").getMethod(name, String.class, String.class)
                    .invoke(null, tag, msg);
        } catch (Exception e) {
            if (name.equals("e")) {
                System.err.println(tag + ": " + msg);
                return;
            }

            if (name.equals("d")) {
                System.out.println(tag + ": " + msg);
                return;
            }
        }
    }

    public static void e(String tag, String msg) {
        log("e", tag, msg);
    }

    public static void d(String tag, String msg) {
        log("d", tag, msg);
    }
}
