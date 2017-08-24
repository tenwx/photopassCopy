/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pictureair.hkdlphotopass.zxing.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.pictureair.hkdlphotopass.R;
import com.pictureair.hkdlphotopass.util.ScreenUtil;
import com.pictureair.hkdlphotopass.zxing.camera.CameraManager;

import java.util.ArrayList;
import java.util.List;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 */
public final class ViewfinderView extends View {
    private static final String TAG = ViewfinderView.class.getSimpleName();
    /**
     * 刷新界面的时间
     */
    private static final long ANIMATION_DELAY = 10L;
    private static final int CURRENT_POINT_OPACITY = 0xA0;
    private static final int MAX_RESULT_POINTS = 20;
    private static final int POINT_SIZE = 6;

    /**
     * 四个绿色边角对应的长度
     */
    private int screenRate;

    /**
     * 四个绿色边角对应的宽度
     */
    private static final int CORNER_WIDTH = 2;

    /**
     * 中间那条线每次刷新移动的距离
     */
    private static final int SPEEN_DISTANCE = 5;

    /**
     * 画笔对象的引用
     */
    private Paint paint;

    /**
     * 中间滑动线的最顶端位置
     */
    private int slideTop;

    private final int resultPointColor;
    private List<ResultPoint> possibleResultPoints;
    private List<ResultPoint> lastPossibleResultPoints;

    private boolean isFirst;

    private Rect lineRect;

    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //将像素转换成dp
        screenRate = ScreenUtil.getScreenWidth(context) / 6;

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        resultPointColor = ContextCompat.getColor(context, R.color.color3);
        possibleResultPoints = new ArrayList<>(5);
        lastPossibleResultPoints = null;

        lineRect = new Rect();
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (CameraManager.get() == null) {
            return; // not ready yet, early draw before done configuring
        }
        //中间的扫描框，你要修改扫描框的大小，去CameraManager里面修改
        Rect frame = CameraManager.get().getFramingRect();
        Rect previewFrame = CameraManager.get().getFramingRectInPreview();
        if (frame == null || previewFrame == null) {
            return;
        }

        //初始化中间线滑动的最上边和最下边
        if (!isFirst) {
            isFirst = true;
            slideTop = frame.top;
        }

        //画扫描框边上的角，总共8个部分
        paint.setColor(Color.WHITE);
        canvas.drawRect(frame.left, frame.top, frame.left + screenRate,
                frame.top + CORNER_WIDTH, paint);
        canvas.drawRect(frame.left, frame.top, frame.left + CORNER_WIDTH, frame.top
                + screenRate, paint);
        canvas.drawRect(frame.right - screenRate, frame.top, frame.right,
                frame.top + CORNER_WIDTH, paint);
        canvas.drawRect(frame.right - CORNER_WIDTH, frame.top, frame.right, frame.top
                + screenRate, paint);
        canvas.drawRect(frame.left, frame.bottom - CORNER_WIDTH, frame.left
                + screenRate, frame.bottom, paint);
        canvas.drawRect(frame.left, frame.bottom - screenRate,
                frame.left + CORNER_WIDTH, frame.bottom, paint);
        canvas.drawRect(frame.right - screenRate, frame.bottom - CORNER_WIDTH,
                frame.right, frame.bottom, paint);
        canvas.drawRect(frame.right - CORNER_WIDTH, frame.bottom - screenRate,
                frame.right, frame.bottom, paint);


        //绘制中间的线,每次刷新界面，中间的线往下移动SPEEN_DISTANCE
        slideTop += SPEEN_DISTANCE;
        if (slideTop >= frame.bottom) {
            slideTop = frame.top;
        }
        //canvas.drawRect(frame.left + MIDDLE_LINE_PADDING, slideTop - MIDDLE_LINE_WIDTH/2, frame.right - MIDDLE_LINE_PADDING,slideTop + MIDDLE_LINE_WIDTH/2, paint);
        lineRect.setEmpty();
        lineRect.left = frame.left;
        lineRect.right = frame.right;
        lineRect.top = slideTop;
        lineRect.bottom = slideTop + 18;
        canvas.drawBitmap(((BitmapDrawable) (getResources().getDrawable(R.drawable.qrcode_scan_line))).getBitmap(), null, lineRect, paint);

        List<ResultPoint> currentPossible = possibleResultPoints;
        List<ResultPoint> currentLast = lastPossibleResultPoints;
        if (currentPossible.isEmpty()) {
            lastPossibleResultPoints = null;
        } else {
            possibleResultPoints = new ArrayList<>(5);
            lastPossibleResultPoints = currentPossible;
            paint.setAlpha(CURRENT_POINT_OPACITY);
            paint.setColor(resultPointColor);
            synchronized (currentPossible) {
                for (ResultPoint point : currentPossible) {
                    canvas.drawCircle(frame.left + point.getX(), frame.top
                            + point.getY(), POINT_SIZE, paint);
                }
            }
        }

        if (currentLast != null) {
            paint.setAlpha(CURRENT_POINT_OPACITY / 2);
            paint.setColor(resultPointColor);
            synchronized (currentLast) {
                float radius = POINT_SIZE / 2.0f;
                for (ResultPoint point : currentLast) {
                    canvas.drawCircle(frame.left + point.getX(), frame.top
                            + point.getY(), radius, paint);
                }
            }
        }

        //只刷新扫描框的内容，其他地方不刷新
        postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top,
                frame.right, frame.bottom);

    }

    public void drawViewfinder() {
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        List<ResultPoint> points = possibleResultPoints;
        synchronized (points) {
            points.add(point);
            int size = points.size();
            if (size > MAX_RESULT_POINTS) {
                // trim it
                points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
            }
        }
    }

}
