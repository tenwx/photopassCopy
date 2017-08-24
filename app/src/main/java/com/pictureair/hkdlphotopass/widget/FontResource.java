package com.pictureair.photopass.widget;

import android.content.Context;
import android.graphics.Typeface;

/**
 * 字体资源
 * Created by bass on 16/3/22.
 */
public class FontResource {
    private static FontResource instance;

    private Typeface typeface;//初始化字体
    private Typeface typefaceBold;

    public static FontResource getInstance() {
        if (null == instance) {
            instance = new FontResource();
        }
        return instance;
    }

    private FontResource() {
    }

    public void initFout(Context context) {
        loadingFout(context);
        loadingBoldFout(context);
    }

    /**
     * 加载正常字体
     */
    public Typeface loadingFout(Context context) {
        if (null == typeface) {
            typeface = Typeface.createFromAsset(context.getAssets(), CustomFontManager.CUSOTM_FONT_NAME);//初始化字体
        }
        return typeface;
    }

    /**
     * 加载粗体
     */
    public Typeface loadingBoldFout(Context context) {
        if (null == typefaceBold) {
            typefaceBold = Typeface.createFromAsset(context.getAssets(), CustomFontManager.CUSOTM_FONT_BOLD_NAME);
        }
        return typefaceBold;
    }
}
