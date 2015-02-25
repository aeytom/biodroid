package de.taytec.biodroid;

import android.util.Log;

/**
 * Created by tay on 21.02.15.
 */
public class BioLog {

    public static boolean debug;

    public static void d(String tag, String msg) {
        if (debug) {
            android.util.Log.d(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        BioLog.e(tag, msg, tr);
    }
}
