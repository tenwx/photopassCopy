package com.pictureair.photopass.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;

/**
 * 由于Fragment中字体不受chlligraphy框架的影响
 * 所以Fragment中的字体 使用此类
 * Created by bass on 15/12/21.
 */
public class CustomTextView extends TextView {
    private final String TAG = "CustomTextView";

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Typeface typeface;
        if (null == MyApplication.getInstance().typeface){
            PictureAirLog.v(TAG,"Myapplication typeface is null");
            typeface = Typeface.createFromAsset(context.getAssets(), Common.DEFULT_FONT);
        }else {
            PictureAirLog.v(TAG,"Myapplication typeface is not null");
            typeface = MyApplication.getInstance().typeface;
        }
        setTypeface(typeface);
    }
}