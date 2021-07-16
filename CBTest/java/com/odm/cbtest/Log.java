package com.odm.cbtest;

public class Log {

    private static final String TAG = "CBTest";

    public static void d(String msg) {
        android.util.Log.d(TAG, msg);
    }

    public static void e(String msg) {
        android.util.Log.e(TAG, msg);
    }

    public static void d(String tag, String msg) {
        android.util.Log.d(TAG, tag + msg);
    }

    public static void e(String tag, String msg) {
        android.util.Log.e(TAG, tag + msg);
    }
}
