package com.pictureair.photopass.util;

import android.content.Context;

/**
 * Created by bauer_bao on 15/12/10.
 */
public class ReflectionUtil {
    private static final String BASE_CODE_STRING = "http_error_code_";
    /**
     * 获取layout Id
     * @param paramContext
     * @param paramString
     * @return
     */
    public static int getLayoutId(Context paramContext, String paramString) {
        return paramContext.getResources().getIdentifier(paramString, "layout",
                paramContext.getPackageName());
    }

    /**
     * 获取String Id
     * @param paramContext
     * @param paramString
     * @return
     */
    public static int getStringId(Context paramContext, int paramString) {
        if (paramContext == null) {
            return 401;
        }
        if (paramString < 5000 || (paramString > 5050 && paramString < 6000) || paramString > 6177){
            return 401;
        }
        return paramContext.getResources().getIdentifier(BASE_CODE_STRING + paramString, "string",
                paramContext.getPackageName());
    }

    /**
     * 获取图片资源ID
     * @param paramContext
     * @param paramString
     * @return
     */
    public static int getDrawableId(Context paramContext, String paramString) {
        return paramContext.getResources().getIdentifier(paramString,
                "drawable", paramContext.getPackageName());
    }

    /**
     * 获取style id
     * @param paramContext
     * @param paramString
     * @return
     */
    public static int getStyleId(Context paramContext, String paramString) {
        return paramContext.getResources().getIdentifier(paramString,
                "style", paramContext.getPackageName());
    }

    /**
     * 获取ID
     * @param paramContext
     * @param paramString
     * @return
     */
    public static int getId(Context paramContext, String paramString) {
        return paramContext.getResources().getIdentifier(paramString,
                "id", paramContext.getPackageName());
    }

    /**
     * 获取颜色ID
     * @param paramContext
     * @param paramString
     * @return
     */
    public static int getColorId(Context paramContext, String paramString) {
        return paramContext.getResources().getIdentifier(paramString,
                "color", paramContext.getPackageName());
    }

    /**
     * 获取array ID
     * @param paramContext
     * @param paramString
     * @return
     */
    public static int getArrayId(Context paramContext, String paramString) {
        return paramContext.getResources().getIdentifier(paramString,
                "array", paramContext.getPackageName());
    }
}
