
package com.sonycsl.wamp.util;

public class NullChecker {
    public static void nullCheck(Object... objs) throws IllegalArgumentException {
        for (Object obj : objs) {
            if (obj == null) {
                throw new IllegalArgumentException("null argument");
            }
        }
    }
}
