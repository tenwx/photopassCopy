package com.pictureair.hkdlphotopass.widget.dropview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;


/**
 * 可拖拽消息
 * 执行拖动的实际控件，然后传递给CoverManager，从而控制DropCover显示
 *
 * @author bauer_bao
 */
public class WaterDrop extends RelativeLayout {
    private Paint mPaint = new Paint();
    private DropCover.OnDragCompeteListener mOnDragCompeteListener;
    private boolean startDrag = false;

    public WaterDrop(Context context) {
        super(context);
        init();
    }

    public WaterDrop(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @SuppressLint("NewApi")
    private void init() {
        mPaint.setAntiAlias(true);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        mPaint.setColor(Color.RED);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, mPaint);
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        ViewGroup parent = getScrollableParent();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isShown()) {
                    startDrag = true;
                    if (parent != null)
                        parent.requestDisallowInterceptTouchEvent(true);
                    CoverManager.getInstance().start(this, event.getRawX(), event.getRawY(), mOnDragCompeteListener);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (startDrag) {
                    CoverManager.getInstance().update(event.getRawX(), event.getRawY());
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (startDrag) {
                    startDrag = false;
                    if (parent != null)
                        parent.requestDisallowInterceptTouchEvent(false);
                    CoverManager.getInstance().finish(event.getRawX(), event.getRawY());
                }
                break;
            default:
                break;
        }

        return true;
    }

    private ViewGroup getScrollableParent() {
        View target = this;
        /**
         * 递归的思路
         */
        while (true) {
            View parent;
            try {
                /**
                 * ViewRootImpl cannot be cast to android.view.View
                 */
                parent = (View) target.getParent();
            } catch (Exception e) {
                return null;
            }
            if (parent == null)
                return null;
            if (parent instanceof ListView || parent instanceof ScrollView) {
                return (ViewGroup) parent;
            }
            target = parent;
        }

    }

    public void setOnDragCompeteListener(DropCover.OnDragCompeteListener onDragCompeteListener) {
        mOnDragCompeteListener = onDragCompeteListener;
    }

}
