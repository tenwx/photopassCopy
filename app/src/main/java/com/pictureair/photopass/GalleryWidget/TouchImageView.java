/*
 Copyright (c) 2012 Robert Foss, Roman Truba

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial
 portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.pictureair.photopass.GalleryWidget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import com.pictureair.photopass.util.PictureAirLog;

public class TouchImageView extends ImageView implements View.OnTouchListener, View.OnLongClickListener {
    // These matrices will be used to move and zoom image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    /**
     * 双击间隔时间
     */
    static final long DOUBLE_PRESS_INTERVAL = 600;

    static final float FRICTION = 0.9f;

    // We can be in one of these 4 states
    static final int NONE = 0;//无
    static final int DOWN = 1;//按下模式
    static final int ZOOM = 2;//缩放模式
    static final int TOUCH_CLEAR = 3;//touch clear 模式
    static final int MOVE = 4;//移动模式
    static final int LOSE_FOCUS = 5;//失去焦点模式
    static final int CLICK = 10;
    int mode = NONE;

    float redundantXSpace, redundantYSpace;//初始多余的左边距和上边距
    float right, bottom;//缩放之后，在屏幕外侧的总距离（没有区分上下左右的距离）
    float origWidth, origHeight;//图片在view上显示的宽高
    float bmWidth, bmHeight;//图片的原始宽高
    float width, height;//控件宽高
    PointF last = new PointF();
    PointF mid = new PointF();
    PointF start = new PointF();
    float[] m;
    float matrixX, matrixY;//当前显示的区域位于整个图像区域的坐标

    float saveScale = 1f;
    float minScale = 1f;
    float maxScale = 3f;
    float oldDist = 1f;

    PointF lastDelta = new PointF(0, 0);
    float velocity = 0;//速度

    long lastPressTime = 0, lastDragTime = 0;
    boolean allowInert = false;
    private boolean hasReset = false;

    private Context mContext;
    private Object mScaleDetector;
    private boolean isLongClicking = false;

    private boolean zoomEnable = true;
    private OnTouchClearListener onTouchClearListener;
    private OnLongTouchListener onLongTouchListener;

    public boolean onLeftSide = false, onTopSide = false, onRightSide = false, onBottomSide = false;

    public TouchImageView(Context context) {
        super(context);
        super.setClickable(true);
        this.mContext = context;
        init();
    }

    public TouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setClickable(true);
        this.mContext = context;
        init();
    }

    protected void init() {
        matrix.setTranslate(1f, 1f);
        m = new float[9];
        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);
        mScaleDetector = new ScaleGestureDetector(mContext, new ScaleListener());
        setOnTouchListener(this);
        setOnLongClickListener(this);
    }

    /**
     * 设置是否开启touch-clear
     * @param onTouchClearListener
     */
    public void setOnTouchClearListener(OnTouchClearListener onTouchClearListener) {
        this.onTouchClearListener = onTouchClearListener;
    }

    /**
     * 设置是否开启touch-clear
     * @param onLongTouchListener
     */
    public void setOnLongTouchListener(OnLongTouchListener onLongTouchListener) {
        this.onLongTouchListener = onLongTouchListener;
    }

    /**
     * 设置是否允许双指缩放
     */
    public void disableZoom() {
        zoomEnable = false;
    }

    public void resetScale() {
        fillMatrixXY();
        matrix.postScale(minScale / saveScale, minScale / saveScale, width / 2, height / 2);
        saveScale = minScale;

        calcPadding();
        checkAndSetTranslate(0, 0);
        scaleMatrixToBounds();
        setImageMatrix(matrix);
        invalidate();
    }

    /**
     * 是否可以左右滑动切换tab
     * @return
     */
    public boolean pagerCanScroll() {
        PictureAirLog.out("pager can scroll--->");
        if (mode != NONE) return false;
        return saveScale == minScale;
    }

    /**
     * 是否是不可切换tab的模式
     * @return
     */
    public boolean isNoScrollMode() {
        return (mode == TOUCH_CLEAR || mode == LOSE_FOCUS);
    }

    /**
     * 获取缩放比例
     * @return
     */
    public float getSaveScale() {
        return saveScale;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        PictureAirLog.d("ondraw--->" + allowInert);
        if (!allowInert) return;
        final float deltaX = lastDelta.x * velocity, deltaY = lastDelta.y * velocity;
        if (deltaX > width || deltaY > height) {
            return;
        }
        velocity *= FRICTION;
        if (Math.abs(deltaX) < 0.1 && Math.abs(deltaY) < 0.1) return;
        checkAndSetTranslate(deltaX, deltaY);
        setImageMatrix(matrix);
    }

    private void checkAndSetTranslate(float deltaX, float deltaY) {
        PictureAirLog.d("check set translate---->");
        float scaleWidth = Math.round(origWidth * saveScale);
        float scaleHeight = Math.round(origHeight * saveScale);
        fillMatrixXY();
        if (scaleWidth < width) {
            deltaX = 0;
            if (matrixY + deltaY > 0)
                deltaY = -matrixY;
            else if (matrixY + deltaY < -bottom)
                deltaY = -(matrixY + bottom);
        } else if (scaleHeight < height) {
            deltaY = 0;
            if (matrixX + deltaX > 0)
                deltaX = -matrixX;
            else if (matrixX + deltaX < -right)
                deltaX = -(matrixX + right);
        } else {
            if (matrixX + deltaX > 0)
                deltaX = -matrixX;
            else if (matrixX + deltaX < -right)
                deltaX = -(matrixX + right);

            if (matrixY + deltaY > 0)
                deltaY = -matrixY;
            else if (matrixY + deltaY < -bottom)
                deltaY = -(matrixY + bottom);
        }
        matrix.postTranslate(deltaX, deltaY);
        checkSiding();
    }

    private void checkSiding() {
        PictureAirLog.d("check siding---->");
        fillMatrixXY();
        //Log.d(TAG, "x: " + matrixX + " y: " + matrixY + " left: " + right / 2 + " top:" + bottom / 2);
        float scaleWidth = Math.round(origWidth * saveScale);
        float scaleHeight = Math.round(origHeight * saveScale);
        onLeftSide = onRightSide = onTopSide = onBottomSide = false;
        //滑动到距离边缘10以内，都算到边缘了
        if (-matrixX < 10.0f) onLeftSide = true;
        PictureAirLog.d("GalleryViewPager---> " + String.format("ScaleW: %f; W: %f, MatrixX: %f", scaleWidth, width, matrixX));
        if ((scaleWidth >= width && (matrixX + scaleWidth - width) < 10) ||
                (scaleWidth <= width && -matrixX + scaleWidth <= width)) onRightSide = true;
        if (-matrixY < 10.0f) onTopSide = true;
        if (Math.abs(-matrixY + height - scaleHeight) < 10.0f) onBottomSide = true;
    }

    private void calcPadding() {
        right = width * saveScale - width - (2 * redundantXSpace * saveScale);
        bottom = height * saveScale - height - (2 * redundantYSpace * saveScale);
    }

    private void fillMatrixXY() {
        matrix.getValues(m);
        matrixX = m[Matrix.MTRANS_X];
        matrixY = m[Matrix.MTRANS_Y];
    }

    private void scaleMatrixToBounds() {
        if (Math.abs(matrixX + right / 2) > 0.5f)
            matrix.postTranslate(-(matrixX + right / 2), 0);
        if (Math.abs(matrixY + bottom / 2) > 0.5f)
            matrix.postTranslate(0, -(matrixY + bottom / 2));
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        if (bm != null) {
            bmWidth = bm.getWidth();
            bmHeight = bm.getHeight();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        //Fit to screen.
        float scale;
        float scaleX = width / bmWidth;
        float scaleY = height / bmHeight;
        scale = Math.min(scaleX, scaleY);

        if (saveScale == 1) {
            /*因为图片显示完之后，此控件位置还没定，所以onMeasure还在进行中。如果这个时候saveScale不为1的话，会被onMeasure重置为1，导致
        	 * 双击放大了，又被瞬间重置为初始状态的现象。因此需要判断，如果为1，则进行matrix，如果不为1，不对图像进行matrix处理*/

            matrix.setScale(scale, scale);
            //minScale = scale;
            setImageMatrix(matrix);
            saveScale = 1f;
        }

        // Center the image
        redundantYSpace = height - (scale * bmHeight);
        redundantXSpace = width - (scale * bmWidth);
        redundantYSpace /= (float) 2;
        redundantXSpace /= (float) 2;

        origWidth = width - 2 * redundantXSpace;
        origHeight = height - 2 * redundantYSpace;
        calcPadding();
        if (saveScale == 1) {
            matrix.postTranslate(redundantXSpace, redundantYSpace);
            setImageMatrix(matrix);
        }

    }

    private double distanceBetween(PointF left, PointF right) {
        return Math.sqrt(Math.pow(left.x - right.x, 2) + Math.pow(left.y - right.y, 2));
    }

    /**
     * Determine the space between the first two fingers
     */
    private float spacing(WrapMotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, WrapMotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    @Override
    public boolean onLongClick(View v) {
        if ((mode == DOWN || mode == MOVE) && isLongClicking) {
            isLongClicking = false;
            if (onTouchClearListener != null) {//如果是按下状态，并且是模糊图，才可以转换成touch_clear状态
                mode = TOUCH_CLEAR;
                onTouchClearListener.onTouchClear(last.x, last.y, (int)matrixX, (int)matrixY, saveScale, hasReset, true);
                hasReset = false;
            } else {
                if (onLongTouchListener != null) {
                    onLongTouchListener.onLongTouch();
                    mode = LOSE_FOCUS;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent rawEvent) {
        WrapMotionEvent event = WrapMotionEvent.wrap(rawEvent);
        if (mScaleDetector != null) {
            ((ScaleGestureDetector) mScaleDetector).onTouchEvent(rawEvent);
        }
        fillMatrixXY();
        PointF curr = new PointF(event.getX(), event.getY());

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                PictureAirLog.out("------------>down:" + event.getX() + ":" + event.getY());
                allowInert = false;
                savedMatrix.set(matrix);
                last.set(event.getX(), event.getY());
                start.set(last);
                mode = DOWN;
                isLongClicking = true;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                PictureAirLog.out("------------>action pointer down");
                if (!zoomEnable) {
                    break;
                }
                oldDist = spacing(event);
                //Log.d(TAG, "oldDist=" + oldDist);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    if (mode == TOUCH_CLEAR) {//让touch-clear消失
                        onTouchClearListener.onTouchClear(curr.x, curr.y, (int) matrixX, (int) matrixY, saveScale, hasReset, false);
                    }
                    mode = ZOOM;
                    isLongClicking = false;
                    //Log.d(TAG, "mode=ZOOM");
                }
                break;

            case MotionEvent.ACTION_UP:
                PictureAirLog.out("----------> up");
                if (mode == TOUCH_CLEAR) {//让touch-clear消失
                    onTouchClearListener.onTouchClear(curr.x, curr.y, (int) matrixX, (int) matrixY, saveScale, hasReset, false);
                }
                allowInert = true;
                mode = NONE;

                int xDiff = (int) Math.abs(event.getX() - start.x);
                int yDiff = (int) Math.abs(event.getY() - start.y);

                if (xDiff < CLICK && yDiff < CLICK && zoomEnable) {
                    //Perform scale on double click
                    long pressTime = System.currentTimeMillis();
                    if (pressTime - lastPressTime <= DOUBLE_PRESS_INTERVAL) {
                        if (saveScale == 1) {
                            PictureAirLog.out("----------> saveScale  = 1 ");
                            final float targetScale = maxScale / saveScale;
                            matrix.postScale(targetScale, targetScale, start.x, start.y);
                            saveScale = maxScale;
                        } else {
                            PictureAirLog.out("----------> saveScale != 1");
                            matrix.postScale(minScale / saveScale, minScale / saveScale, width / 2, height / 2);
                            saveScale = minScale;
                        }
                        calcPadding();
                        checkAndSetTranslate(0, 0);
                        lastPressTime = 0;
                    } else {
                        lastPressTime = pressTime;
                    }
                    if (saveScale == minScale) {
                        scaleMatrixToBounds();
                    }
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                PictureAirLog.out("-----------> action pointer up");
                mode = NONE;
                velocity = 0;
                savedMatrix.set(matrix);
                oldDist = spacing(event);
                //Log.d(TAG, "mode=NONE");
                break;

            case MotionEvent.ACTION_MOVE:
                PictureAirLog.out("----------> action move:" + event.getX() + ":" + event.getY() + "mode-->" + mode);
                if (mode == LOSE_FOCUS) {//长按之后，禁止任何touch行为
                    break;
                }

                float deltaX = curr.x - last.x;
                float deltaY = curr.y - last.y;

                if (mode != MOVE && mode != TOUCH_CLEAR) {
                    if (Math.abs(deltaX) > 5 || Math.abs(deltaY) > 5) {//部分机型，比如小米4，move事件有偏差，如果超过这偏差，才算move
                        allowInert = false;
                        isLongClicking = false;
                        mode = MOVE;
                    } else {
                        last.set(curr.x, curr.y);
                        break;
                    }
                }

                if (mode == TOUCH_CLEAR) {
                    onTouchClearListener.onTouchClear(curr.x, curr.y, (int) matrixX, (int) matrixY, saveScale, hasReset, true);
                    hasReset = false;

                } else if (mode == MOVE) {
                    long dragTime = System.currentTimeMillis();

                    velocity = (float) distanceBetween(curr, last) / (dragTime - lastDragTime) * FRICTION;
                    lastDragTime = dragTime;

                    checkAndSetTranslate(deltaX, deltaY);
                    lastDelta.set(deltaX, deltaY);
                }
                last.set(curr.x, curr.y);
                break;
        }

        if (zoomEnable) {
            setImageMatrix(matrix);
            invalidate();
        }
        return false;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mode = ZOOM;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float mScaleFactor = (float) Math.min(Math.max(.95f, detector.getScaleFactor()), 1.05);
            float origScale = saveScale;
            saveScale *= mScaleFactor;
            if (saveScale > maxScale) {
                saveScale = maxScale;
                mScaleFactor = maxScale / origScale;
            } else if (saveScale < minScale) {
                saveScale = minScale;
                mScaleFactor = minScale / origScale;
            }
            right = width * saveScale - width - (2 * redundantXSpace * saveScale);
            bottom = height * saveScale - height - (2 * redundantYSpace * saveScale);
            if (origWidth * saveScale <= width || origHeight * saveScale <= height) {
                matrix.postScale(mScaleFactor, mScaleFactor, width / 2, height / 2);
                if (mScaleFactor < 1) {
                    matrix.getValues(m);
                    float x = m[Matrix.MTRANS_X];
                    float y = m[Matrix.MTRANS_Y];
                    if (mScaleFactor < 1) {
                        if (Math.round(origWidth * saveScale) < width) {
                            if (y < -bottom)
                                matrix.postTranslate(0, -(y + bottom));
                            else if (y > 0)
                                matrix.postTranslate(0, -y);
                        } else {
                            if (x < -right)
                                matrix.postTranslate(-(x + right), 0);
                            else if (x > 0)
                                matrix.postTranslate(-x, 0);
                        }
                    }
                }
            } else {
                matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());
                matrix.getValues(m);
                float x = m[Matrix.MTRANS_X];
                float y = m[Matrix.MTRANS_Y];
                if (mScaleFactor < 1) {
                    if (x < -right)
                        matrix.postTranslate(-(x + right), 0);
                    else if (x > 0)
                        matrix.postTranslate(-x, 0);
                    if (y < -bottom)
                        matrix.postTranslate(0, -(y + bottom));
                    else if (y > 0)
                        matrix.postTranslate(0, -y);
                }
            }
            return true;

        }
    }

    /**
     * 触摸监听
     */
    public interface OnTouchClearListener{
        void onTouchClear(float x, float y, int matrixX, int matrixY, float scale, boolean hasReset, boolean visible);
    }

    /**
     * 长按监听
     */
    public interface OnLongTouchListener{
        void onLongTouch();
    }

    /**
     * 旋转的时候，如果图片有缩放过，需要重置，否则会有再次缩放之后，图片显示位置异常的问题
     */
    public void resetImageView(){
        if (saveScale != 1) {//有缩放过程，需要还原
            PictureAirLog.out("----------> saveScale != 1");
            matrix.postScale(minScale / saveScale, minScale / saveScale, width / 2, height / 2);
            saveScale = minScale;
            calcPadding();
            checkAndSetTranslate(0, 0);
            lastPressTime = 0;
            scaleMatrixToBounds();
        }
        hasReset = true;
    }

    /**
     * 当前显示的区域位于整个图像区域的坐标
     * @return
     */
    public PointF getCurrentPositionPoint() {
        return new PointF(matrixX, matrixY);
    }

}