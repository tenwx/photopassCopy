package com.pictureworks.android.widget.dropview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;

import com.pictureworks.android.util.ScreenUtil;


/**
 * 实现显示的控件，绘图在此完成
 * @author bauer_bao
 *
 */
public class DropCover extends View implements Callback{

	/**
	 * 最大移动距离
	 */
    private int mMaxDistance;

    /**
     * 开始坐标
     */
    private float mBaseX;
    private float mBaseY;

    /**
     * 当前坐标
     */
    private float mTargetX;
    private float mTargetY;

    private Paint mPaint = new Paint();
    private Triangle triangle = new Triangle();

    /**
     * 原始半径
     */
    private float mRadius = 0;
    
    /**
     * 当前半径
     */
    private float mStrokeWidth = 20;
    
    /**
     * 是否触摸
     */
    private boolean isTouch = true;

    /**
     * 状态栏高度
     */
    private float mStatusBarHeight = 0;
    private OnDragCompeteListener mOnDragCompeteListener;
    
    /**
     * 是否开启消失动画
     */
    private boolean isExplored = false;
    
    /**
     * 触碰状态
     */
    private int touchMode;
    
    private static final int TOUCH_DOWN = 1;
    private static final int TOUCH_MOVE = 2;
    private static final int TOUCH_UP = 3;
    private static final int TOUCH_CANCEL = 4;
    
    private Handler handler;
    

    @SuppressLint("NewApi")
    public DropCover(Context context) {
        super(context);
        setBackgroundColor(Color.TRANSPARENT);
        setFocusable(false);
        setClickable(false);
        
        handler = new Handler(this);
        
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setAntiAlias(true);

        mMaxDistance = ScreenUtil.getScreenHeight(context) / 7;
        mStatusBarHeight = ScreenUtil.getStatusBarHeight(getContext());
        touchMode = TOUCH_UP;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }


    @Override
    protected void onDraw(Canvas canvas) {
    	// TODO Auto-generated method stub
    	super.onDraw(canvas);
    	canvas.drawColor(Color.TRANSPARENT);
        if (isExplored) {//消失动画
        	isExplored = false;
        } else {//触摸
        	if (isTouch) {//触摸中
        		double distance = Math.sqrt(Math.pow(mBaseX - mTargetX, 2) + Math.pow(mBaseY - mTargetY, 2));
        		mStrokeWidth = (float) ((1f - distance / mMaxDistance) * mRadius);
        		// 绘制原来的圆形（触摸移动的时候半径会不断变化）
        		canvas.drawCircle(mBaseX, mBaseY, mStrokeWidth, mPaint);
        		// 绘制手指跟踪的圆形
        		canvas.drawCircle(mTargetX, mTargetY, mRadius, mPaint);
        		if (distance < mMaxDistance) {
        			drawBezier(canvas);
        		}
        	} else {//还原
        		if (mStrokeWidth > 0) {
        			canvas.drawCircle(mBaseX, mBaseY, mStrokeWidth / 2, mPaint);
        		}
        	}
        }
        
        handler.sendEmptyMessage(touchMode);
        
    }
    
    /**
     * 开始拖动
     * @param viewWidth
     * @param viewHeight
     * @param x
     * @param y
     */
    public void start(int viewWidth, int viewHeight, float x, float y) {
    	touchMode = TOUCH_DOWN;
    	mRadius = viewWidth / 2f;
    	mStrokeWidth = mRadius;
        mBaseX = x + viewWidth / 2f;
        mBaseY = y + viewHeight / 2f - mStatusBarHeight;
        mTargetX = mBaseX;
        mTargetY = mBaseY;
        isTouch = true;
        postInvalidate();
    }
    
    /**
     * move the drop
     * 
     * @param x
     * @param y
     */
    public void update(float x, float y) {
    	if (touchMode == TOUCH_DOWN) {
    		mOnDragCompeteListener.onVisible(false);
    		touchMode = TOUCH_MOVE;
		}
    	triangle.deltaX = x - mBaseX;
        triangle.deltaY = -1 * (y - mBaseY); // y轴方向相反，所以需要取反
        double distance = Math.sqrt(triangle.deltaX * triangle.deltaX + triangle.deltaY * triangle.deltaY);
        triangle.hypotenuse = distance;
        
        mTargetX = x;
        mTargetY = y - mStatusBarHeight;
        postInvalidate();
    }

    /**
     * finish drag event and start explosion
     * 
     * @param x
     * @param y
     */
    public void finish(float x, float y) {
        double distance = Math.sqrt(Math.pow(mBaseX - mTargetX, 2) + Math.pow(mBaseY - mTargetY, 2));
        if (distance > mMaxDistance) {//消失
        	touchMode = TOUCH_UP;
            if (mOnDragCompeteListener != null)
                mOnDragCompeteListener.onDrag(x, y - mStatusBarHeight);
            isExplored = true;
            clearDatas();
            clearViews();
            postInvalidate();
        } else {//还原
        	startRollBackAnimation(500/*ms*/);
        }
        isTouch = false;
    }
    
    /**
     * reset datas
     */
    public void clearDatas() {
        mBaseX = -1;
        mBaseY = -1;
        mTargetX = -1;
        mTargetY = -1;
        isExplored = false;
    }

    /**
     * remove DropCover
     */
    public void clearViews() {
        if (getParent() != null) {
            CoverManager.getInstance().getWindowManager().removeView(this);
        }
    }
    
    /**
     * 绘制贝塞尔曲线
     * @param canvas
     */
    private void drawBezier(Canvas canvas) {
        Path mPath = new Path();
            double sin = triangle.deltaY / triangle.hypotenuse;
            double cos = triangle.deltaX / triangle.hypotenuse;

            // A点
            mPath.moveTo(
                    (float) (mBaseX - mStrokeWidth * sin),
                    (float) (mBaseY - mStrokeWidth * cos)
            );
            // B点
            mPath.lineTo(
                    (float) (mBaseX + mStrokeWidth * sin),
                    (float) (mBaseY + mStrokeWidth * cos)
            );
            // C点
            mPath.quadTo(
                    (mBaseX + mTargetX) / 2, (mBaseY + mTargetY) / 2,
                    (float) (mTargetX + mRadius * sin), (float) (mTargetY + mRadius * cos)
            );
            // D点
            mPath.lineTo(
                    (float) (mTargetX - mRadius * sin),
                    (float) (mTargetY - mRadius * cos)
            );
            // A点
            mPath.quadTo(
                    (mBaseX + mTargetX) / 2, (mBaseY + mTargetY) / 2,
                    (float) (mBaseX - mStrokeWidth * sin), (float) (mBaseY - mStrokeWidth * cos)
            );
            canvas.drawPath(mPath, mPaint);
    }

    public void setOnDragCompeteListener(OnDragCompeteListener onDragCompeteListener) {
        mOnDragCompeteListener = onDragCompeteListener;
    }

    /**
     * 计算四个坐标的三角边关系
     */
    class Triangle {
        double deltaX;
        double deltaY;
        double hypotenuse;
    }
    
    /**
     * 回调接口
     * @author bauer_bao
     *
     */
    public interface OnDragCompeteListener {
        void onDrag(float endX, float endY);
        void onVisible(boolean visible);
    }
    
    /**
     * 还原回滚动画
     * @param duration
     */
    private void startRollBackAnimation(long duration) {
        ValueAnimator rollBackAnim = ValueAnimator.ofFloat(mStrokeWidth, mRadius * 2);
        rollBackAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                mStrokeWidth = (int) value;
                postInvalidate();
            }
        });
        rollBackAnim.setInterpolator(new BounceInterpolator()); // 反弹效果
        rollBackAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                touchMode = TOUCH_CANCEL;
                mOnDragCompeteListener.onVisible(true);
            }
        });
        rollBackAnim.setDuration(duration);
        rollBackAnim.start();
    }

	@Override
	public boolean handleMessage(Message msg) {
		if (touchMode == TOUCH_UP) {//结束
			mOnDragCompeteListener = null;
		} else if (touchMode == TOUCH_CANCEL) {//取消
			mOnDragCompeteListener = null;
			clearDatas();
			clearAnimation();
			clearViews();
		}
		return false;
	}
}
