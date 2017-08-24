package com.pictureair.hkdlphotopass.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;


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
            Typeface typeface = FontResource.getInstance().loadingFout(context);
            setTypeface(typeface);
        }
    }
}