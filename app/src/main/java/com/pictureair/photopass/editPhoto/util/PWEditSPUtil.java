package com.pictureair.photopass.editPhoto.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences 的管理类
 * 方便以后把编辑图片以jar包形式导入
 * Created by talon on 16/7/19.
 */
public class PWEditSPUtil {

    public static final String FILE_NAME = "pw_edit_photo_data";
    public static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;


    public static void setValue(Context context, String key, Object obj) {

        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        }

        if (editor == null) {
            editor = sharedPreferences.edit();
        }

        if (obj instanceof Boolean) {
            editor.putBoolean(key, (Boolean) obj);
        } else if (obj instanceof String) {
            editor.putString(key, (String) obj);
        } else if (obj instanceof Integer) {
            editor.putInt(key, (Integer) obj);
        } else if (obj instanceof Float) {
            editor.putFloat(key, (Float) obj);
        } else if (obj instanceof Long) {
            editor.putLong(key, (Long) obj);
        }
        editor.apply();
    }


    public static Object getValue(Context context, String key, Object defaultObj) {

        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        }
        Object obj = null;
        if (defaultObj instanceof Boolean) {
            obj = sharedPreferences.getBoolean(key, (Boolean)defaultObj);
        } else if (defaultObj instanceof String) {
            obj = sharedPreferences.getString(key, (String)defaultObj);
        } else if (defaultObj instanceof Integer) {
            obj = sharedPreferences.getInt(key, (Integer)defaultObj);
        } else if (defaultObj instanceof Float) {
            obj = sharedPreferences.getFloat(key, (Float)defaultObj);
        } else if (defaultObj instanceof Long) {
            obj = sharedPreferences.getLong(key, (Long)defaultObj);
        }

        return obj;
    }

}
