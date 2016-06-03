package com.pictureworks.android.util;

import android.util.Log;

import com.pictureworks.android.BuildConfig;


/**
 * log的封装类
 *
 * @author bauer_bao
 */
public class PictureAirLog {

    // 是否开启debug模式，如果为true，打印log，如果为false，不打印log
    private static final String BASE_TAG = "PhotoPass==>>";

    /**
     * @param tag
     * @param log
     */
    public static void d(String tag, String log) {
        if (BuildConfig.LOG_DEBUG) {
            Log.d(BASE_TAG + tag, log + "");
        }
    }

    /**
     * @param tag
     * @param log
     */
    public static void e(String tag, String log) {
        if (BuildConfig.LOG_DEBUG) {
            Log.e(BASE_TAG + tag, log + "");
        }
    }

    /**
     * @param tag
     * @param log
     */
    public static void i(String tag, String log) {
        if (BuildConfig.LOG_DEBUG) {
            Log.i(BASE_TAG + tag, log + "");
        }
    }

    /**
     * @param tag
     * @param log
     */
    public static void v(String tag, String log) {
        if (BuildConfig.LOG_DEBUG) {
            Log.v(BASE_TAG + tag, log + "");
        }
    }

    /**
     * @param tag
     * @param log
     */
    public static void w(String tag, String log) {
        if (BuildConfig.LOG_DEBUG) {
            Log.w(BASE_TAG + tag, log + "");
        }
    }

    /**
     * @param log
     */
    public static void out(String log) {
        if (BuildConfig.LOG_DEBUG) {
			System.out.println(BASE_TAG + ":" + log);
        }
    }

    /**
     * @param log
     */
    public static void err(String log) {
        if (BuildConfig.LOG_DEBUG) {
            System.err.println(BASE_TAG + log);
        }
    }
}
