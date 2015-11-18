package com.pictureAir.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.view.View;

import com.pictureAir.R;
import com.pictureAir.util.ScreenUtil;

public class DashedLineView extends View {      
    private int dashColor = 0;
    public DashedLineView(Context context, AttributeSet attrs) {      
        super(context, attrs);                
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.DashLine, 0, 0);
        dashColor = a.getResourceId(R.styleable.DashLine_dashColor, R.color.gray_light3);
        a.recycle();
    }      
      
    @Override      
    protected void onDraw(Canvas canvas) {      
        // TODO Auto-generated method stub      
        super.onDraw(canvas);              
        Paint paint = new Paint();   
        paint.setStyle(Paint.Style.STROKE);      
        paint.setColor(getResources().getColor(dashColor));      
        Path path = new Path();   
        path.moveTo(0, 1);      
        path.lineTo(ScreenUtil.getScreenWidth(getContext()),1);            
        PathEffect effects = new DashPathEffect(new float[]{5,6,5,6},1);      
        paint.setPathEffect(effects);      
        canvas.drawPath(path, paint);      
    }   
}  
