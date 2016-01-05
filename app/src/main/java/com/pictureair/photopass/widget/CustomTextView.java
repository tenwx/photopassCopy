package com.pictureair.photopass.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.PictureAirLog;

import cn.smssdk.gui.CustomFontManager;

/**
 * 由于Fragment中字体不受chlligraphy框架的影响
 * 所以Fragment中的字体 使用此类
 * Created by bass on 15/12/21.
 */
public class CustomTextView extends TextView {
    private final String TAG = "CustomTextView";

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomTextView(Context context,AttributeSet attrs,int i) {
        super(context, attrs, i);
        init(context);
    }

    private void init(Context context) {
        if (CustomFontManager.IS_CUSOTM_FONT) {
            Typeface typeface;
            if (null == MyApplication.getInstance().typeface) {
                PictureAirLog.v(TAG, "Myapplication typeface is null");
                typeface = Typeface.createFromAsset(context.getAssets(), CustomFontManager.CUSOTM_FONT_NAME);
            } else {
                PictureAirLog.v(TAG, "Myapplication typeface is not null");
                typeface = MyApplication.getInstance().typeface;
            }
            setTypeface(typeface);
        }
    }
}