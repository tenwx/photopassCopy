package com.pictureworks.android.util;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.baidu.mobstat.StatService;

/**
 * Created by talon on 16/5/16.
 */
public class BaiduMobUtil {

    /**
     * onResume
     * @param context
     */
    public static void onResume(Context context) {
            StatService.onResume(context);
    }
    /**
     * onPause
     * @param context
     */
    public static void onPause(Context context) {
            StatService.onPause(context);
    }


    public static void onFragmentResume(Fragment fragment) {
        StatService.onResume(fragment);
    }


    public static void onFragmentPause(Fragment fragment) {
        StatService.onPause(fragment);
    }

}
