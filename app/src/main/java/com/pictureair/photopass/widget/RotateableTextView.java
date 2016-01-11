package com.pictureair.photopass.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 旋转文字。
 * Created by talon on 16/1/11.
 */
public class RotateableTextView extends TextView {

    public RotateableTextView(Context context) {
        super(context);
    }

    public RotateableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public RotateableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        canvas.rotate(90, getMeasuredWidth()/2, getMeasuredHeight() / 2);
        super.onDraw(canvas);
    }




}