package com.pictureair.photopass.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;

/**
 * Created by pengwu on 16/12/9.
 * 可拖动的layout
 */

public class DraftLayout extends RelativeLayout{

    private int screenWidth;
    private int screenHeight;
    private int lastX, lastY;
    private int originX, originY;
    private int oriHeight;

    public DraftLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        screenWidth = ScreenUtil.getScreenWidth(context);
        screenHeight = ScreenUtil.getScreenHeight(context) - ScreenUtil.getStatusBarHeight(context) - ScreenUtil.dip2px(context, 65);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        int ea = event.getAction();
        switch (ea) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) event.getRawX();// 获取触摸事件触摸位置的原始X坐标
                lastY = (int) event.getRawY();
                originX = lastX;
                originY = lastY;
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = (int) event.getRawX() - lastX;
                int dy = (int) event.getRawY() - lastY;
                int l = getLeft() + dx;
                int b = getBottom() + dy;
                int r = getRight() + dx;
                int t = getTop() + dy;
                // 下面判断移动是否超出屏幕
                if (l < 0) {
                    l = 0;
                    r = l + getWidth();
                }
                if (t < 0) {
                    t = 0;
                    b = t + oriHeight;
                }
                if (r > screenWidth) {
                    r = screenWidth;
                    l = r - getWidth();
                }
                if (b > screenHeight) {
                    b = screenHeight;
                    t = b - oriHeight;
                }
                layout(l, t, r, b);
                //必须设置，否则整个页面如果滑动，layout会回到起始位置
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(getWidth(), oriHeight);
                layoutParams.leftMargin = l;
                layoutParams.topMargin = t;
                setLayoutParams(layoutParams);
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
                postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
                int distance = (int) event.getRawX() - originX + (int)event.getRawY() - originY;
                PictureAirLog.e("DIstance",distance+"");
                if (Math.abs(distance)<20) {
                    //当变化太小的时候什么都不做 OnClick执行
                }else {
                    return true;
                }
                break;
        }
        return false;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (oriHeight == 0) {
            oriHeight = b - t;
        }
    }
}
