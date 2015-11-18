package com.pictureAir.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.ThumbnailUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.pictureAir.R;

/**
 * A @{code ImageView} which can rotate it's content.
 */
public class Rotate_RoundAngleView extends ImageView implements Rotatable {

    private static final String TAG = "RotateImageView";

    private static final int ANIMATION_SPEED = 270; // 270 deg/sec

    private int mCurrentDegree = 0; // [0, 359]
    private int mStartDegree = 0;
    private int mTargetDegree = 0;
    
    private Paint paint;  
    private int roundWidth = 5;  
    private int roundHeight = 5;  
    private Paint paint2;

    private boolean mClockwise = false, mEnableAnimation = true;

    private long mAnimationStartTime = 0;
    private long mAnimationEndTime = 0;
    private final float DISABLED_ALPHA = 0.4f;
    private boolean mFilterEnabled = true;
    public Rotate_RoundAngleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public Rotate_RoundAngleView(Context context) {
        super(context);
        init(context, null);
    }

    protected int getDegree() {
        return mTargetDegree;
    }
    private void init(Context context, AttributeSet attrs) {  
        
        if(attrs != null) {     
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundAngleImageView);   
            roundWidth= a.getDimensionPixelSize(R.styleable.RoundAngleImageView_roundWidth, roundWidth);  
            roundHeight= a.getDimensionPixelSize(R.styleable.RoundAngleImageView_roundHeight, roundHeight);  
        }else {  
            float density = context.getResources().getDisplayMetrics().density;  
            roundWidth = (int) (roundWidth*density);  
            roundHeight = (int) (roundHeight*density);  
        }   
          
        paint = new Paint();  
        paint.setColor(Color.WHITE);  
        paint.setAntiAlias(true);  
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));  
          
        paint2 = new Paint();  
        paint2.setXfermode(null);  
    }  
      
    @Override  
    public void draw(Canvas canvas) {  
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);  
        Canvas canvas2 = new Canvas(bitmap);  
        super.draw(canvas2);  
        drawLiftUp(canvas2);  
        drawRightUp(canvas2);  
        drawLiftDown(canvas2);  
        drawRightDown(canvas2);  
        canvas.drawBitmap(bitmap, 0, 0, paint2);  
        bitmap.recycle();  
    }  
      
    private void drawLiftUp(Canvas canvas) {  
        Path path = new Path();  
        path.moveTo(0, roundHeight);  
        path.lineTo(0, 0);  
        path.lineTo(roundWidth, 0);  
        path.arcTo(new RectF(  
                0,   
                0,   
                roundWidth*2,   
                roundHeight*2),   
                -90,   
                -90);  
        path.close();  
        canvas.drawPath(path, paint);  
    }  
      
    private void drawLiftDown(Canvas canvas) {  
        Path path = new Path();  
        path.moveTo(0, getHeight()-roundHeight);  
        path.lineTo(0, getHeight());  
        path.lineTo(roundWidth, getHeight());  
        path.arcTo(new RectF(  
                0,   
                getHeight()-roundHeight*2,   
                0+roundWidth*2,   
                getHeight()),  
                90,   
                90);  
        path.close();  
        canvas.drawPath(path, paint);  
    }  
      
    private void drawRightDown(Canvas canvas) {  
        Path path = new Path();  
        path.moveTo(getWidth()-roundWidth, getHeight());  
        path.lineTo(getWidth(), getHeight());  
        path.lineTo(getWidth(), getHeight()-roundHeight);  
        path.arcTo(new RectF(  
                getWidth()-roundWidth*2,   
                getHeight()-roundHeight*2,   
                getWidth(),   
                getHeight()), 0, 90);  
        path.close();  
        canvas.drawPath(path, paint);  
    }  
      
    private void drawRightUp(Canvas canvas) {  
        Path path = new Path();  
        path.moveTo(getWidth(), roundHeight);  
        path.lineTo(getWidth(), 0);  
        path.lineTo(getWidth()-roundWidth, 0);  
        path.arcTo(new RectF(  
                getWidth()-roundWidth*2,   
                0,   
                getWidth(),   
                0+roundHeight*2),   
                -90,   
                90);  
        path.close();  
        canvas.drawPath(path, paint);  
    }
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mFilterEnabled) {
            if (enabled) {
                setAlpha(1.0f);
            } else {
                setAlpha(DISABLED_ALPHA);
            }
        }
    }

    public void enableFilter(boolean enabled) {
        mFilterEnabled = enabled;
    }
    // Rotate the view counter-clockwise
    @Override
    public void setOrientation(int degree, boolean animation) {
        mEnableAnimation = animation;
        // make sure in the range of [0, 359]
        degree = degree >= 0 ? degree % 360 : degree % 360 + 360;
        if (degree == mTargetDegree) return;

        mTargetDegree = degree;
        if (mEnableAnimation) {
            mStartDegree = mCurrentDegree;
            mAnimationStartTime = AnimationUtils.currentAnimationTimeMillis();

            int diff = mTargetDegree - mCurrentDegree;
            diff = diff >= 0 ? diff : 360 + diff; // make it in range [0, 359]

            // Make it in range [-179, 180]. That's the shorted distance between the
            // two angles
            diff = diff > 180 ? diff - 360 : diff;

            mClockwise = diff >= 0;
            mAnimationEndTime = mAnimationStartTime
                    + Math.abs(diff) * 1000 / ANIMATION_SPEED;
        } else {
            mCurrentDegree = mTargetDegree;
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            Log.e(TAG, "drawable == null, return");
            return;
        }

        Rect bounds = drawable.getBounds();
        int w = bounds.right - bounds.left;
        int h = bounds.bottom - bounds.top;

        if (w == 0 || h == 0) {
            Log.e(TAG, "w == 0 || h == 0, return");
            return; // nothing to draw
        }

        if (mCurrentDegree != mTargetDegree) {
            long time = AnimationUtils.currentAnimationTimeMillis();
            if (time < mAnimationEndTime) {
                int deltaTime = (int)(time - mAnimationStartTime);
                int degree = mStartDegree + ANIMATION_SPEED
                        * (mClockwise ? deltaTime : -deltaTime) / 1000;
                degree = degree >= 0 ? degree % 360 : degree % 360 + 360;
                mCurrentDegree = degree;
                invalidate();
            } else {
                mCurrentDegree = mTargetDegree;
            }
        }

        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getPaddingRight();
        int bottom = getPaddingBottom();
        int width = getWidth() - left - right;
        int height = getHeight() - top - bottom;

        int saveCount = canvas.getSaveCount();

        // Scale down the image first if required.
        if ((getScaleType() == ImageView.ScaleType.FIT_CENTER) &&
                ((width < w) || (height < h))) {
            float ratio = Math.min((float) width / w, (float) height / h);
            canvas.scale(ratio, ratio, width / 2.0f, height / 2.0f);
        }
        canvas.translate(left + width / 2, top + height / 2);
        canvas.rotate(-mCurrentDegree);
        canvas.translate(-w / 2, -h / 2);
        drawable.draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    private Bitmap mThumb;
    private Drawable[] mThumbs;
    private TransitionDrawable mThumbTransition;

    public void setBitmap(Bitmap bitmap) {
        // Make sure uri and original are consistently both null or both
        // non-null.
        if (bitmap == null) {
            mThumb = null;
            mThumbs = null;
            setImageDrawable(null);
            setVisibility(GONE);
            return;
        }

        LayoutParams param = getLayoutParams();
        final int miniThumbWidth = param.width
                - getPaddingLeft() - getPaddingRight();
        final int miniThumbHeight = param.height
                - getPaddingTop() - getPaddingBottom();
        mThumb = ThumbnailUtils.extractThumbnail(
                bitmap, miniThumbWidth, miniThumbHeight);
        Drawable drawable;
        if (mThumbs == null || !mEnableAnimation) {
            mThumbs = new Drawable[2];
            mThumbs[1] = new BitmapDrawable(getContext().getResources(), mThumb);
            setImageDrawable(mThumbs[1]);
        } else {
            mThumbs[0] = mThumbs[1];
            mThumbs[1] = new BitmapDrawable(getContext().getResources(), mThumb);
            mThumbTransition = new TransitionDrawable(mThumbs);
            setImageDrawable(mThumbTransition);
            mThumbTransition.startTransition(500);
        }
        setVisibility(VISIBLE);
    }
}