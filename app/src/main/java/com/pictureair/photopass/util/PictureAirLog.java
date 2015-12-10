package com.pictureair.photopass.util;

import android.util.Log;

import com.pictureair.photopass.MyApplication;

/**
 * log的封装类
 *
 * @author bauer_bao
 */
public class PictureAirLog {

    // 是否开启debug模式，如果为true，打印log，如果为false，不打印log
    private static boolean DEBUG = true;
    private static final String BASE_TAG = "PhotoPass==>>";

    static {
        DEBUG = MyApplication.DEBUG;
    }

    /**
     * @param tag
     * @param log
     */
    public static void d(String tag, String log) {
        if (DEBUG) {
            Log.d(BASE_TAG + tag, log);
        }
    }

    /**
     * @param tag
     * @param log
     */
    public static void e(String tag, String log) {
        if (DEBUG) {
            Log.e(BASE_TAG + tag, log);
        }
    }

    /**
     * @param tag
     * @param log
     */
    public static void i(String tag, String log) {
        if (DEBUG) {
            Log.i(BASE_TAG + tag, log);
        }
    }

    /**
     * @param tag
     * @param log
     */
    public static void v(String tag, String log) {
        if (DEBUG) {
            Log.v(BASE_TAG + tag, log);
        }
    }

    /**
     * @param tag
     * @param log
     */
    public static void w(String tag, String log) {
        if (DEBUG) {
            Log.w(BASE_TAG + tag, log);
        }
    }

    /**
     * @param log
     */
    public static void out(String log) {
        if (DEBUG) {
			System.out.println(BASE_TAG + ":" + log);
        }
    }

    /**
     * @param log
     */
    public static void err(String log) {
        if (DEBUG) {
            System.err.println(BASE_TAG + log);
        }
    }
}
