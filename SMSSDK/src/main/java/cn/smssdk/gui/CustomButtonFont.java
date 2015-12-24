package cn.smssdk.gui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * 用于库中 按钮的字体
 * Created by bass on 15/12/24.
 */
public class CustomButtonFont extends Button {

    private Typeface typeface;

    public CustomButtonFont(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (CustomFontManager.IS_CUSOTM_FONT) {
            if (null == typeface) {
                typeface = Typeface.createFromAsset(context.getAssets(), CustomFontManager.CUSOTM_FONT_NAME);
            }
            setTypeface(typeface);
        }
    }
}
