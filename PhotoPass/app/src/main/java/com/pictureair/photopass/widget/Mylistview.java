package com.pictureair.photopass.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.ListView;

public class Mylistview extends ListView{
	GestureDetector gestureDetector;
	View.OnTouchListener mGestureListener;
	public Mylistview(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		gestureDetector = new GestureDetector(new YScrollDetector());
	}
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		return super.onInterceptTouchEvent(ev)
				&& gestureDetector.onTouchEvent(ev);
	}
	class YScrollDetector extends SimpleOnGestureListener{
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			// TODO Auto-generated method stub
			if (Math.abs(distanceY) > (Math.abs(distanceX)+10))
			{
				return true;
			}
			return false;
		}
	}
}
