package me.ele.trojan.log;

import android.util.Log;

/**
 * Created by allen on 17/6/29.
 */

public class Logger {
    private static final String TAG = "Trojan";
    private static boolean sLogOn = false;

    public static void setLog(boolean on) {
        sLogOn = on;
    }

    public static void e(String msg) {
        if (sLogOn) {
            Log.e(TAG, msg);
        }
    }

    public static void i(String msg) {
        if (sLogOn) {
            Log.i(TAG, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (sLogOn) {
            Log.e(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (sLogOn) {
            Log.e(tag, msg);
        }
    }

}
