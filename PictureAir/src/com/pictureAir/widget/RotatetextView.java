package com.pictureAir.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

/**
 * A @{code ImageView} which can rotate it's content.
 */
public class RotatetextView extends TextView implements Rotatable {

	private static final String TAG = "RotateImageView";

	private static final int ANIMATION_SPEED = 270; // 270 deg/sec

	private int mCurrentDegree = 0; // [0, 359]
	private int mStartDegree = 0;
	private int mTargetDegree = 0;

	private boolean mClockwise = false, mEnableAnimation = true;

	private long mAnimationStartTime = 0;
	private long mAnimationEndTime = 0;
	private final float DISABLED_ALPHA = 0.4f;
	private boolean mFilterEnabled = true;
	public RotatetextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RotatetextView(Context context) {
		super(context);
	}

	protected int getDegree() {
		return mTargetDegree;
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
		// TODO Auto-generated method stub
		super.onDraw(canvas);
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
		setRotation(-mCurrentDegree);
	}
}