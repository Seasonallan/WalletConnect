package com.season;

import android.util.Log;

public class L {

    public static boolean log = true;

    public static void e(String tag, String content) {
        if (log) {
            Log.e(tag, content);
        }
    }

    public static void d(String tag, String content) {
        if (log) {
            Log.d(tag, content);
        }
    }
}
