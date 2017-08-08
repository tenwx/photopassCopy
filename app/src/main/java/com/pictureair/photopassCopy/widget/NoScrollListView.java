package com.pictureair.photopassCopy.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

public class NoScrollListView extends ListView{

	public NoScrollListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		setFocusable(false);
	}
	public NoScrollListView(Context context) {   
		super(context); 
		setFocusable(false);
	}   

	public NoScrollListView(Context context, AttributeSet attrs, int defStyle) {   
		super(context, attrs, defStyle);   
		setFocusable(false);
	}

	@Override   
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {   

		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,   
				MeasureSpec.AT_MOST);   
		super.onMeasure(widthMeasureSpec, expandSpec);   
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_MOVE) {
			return true;  //禁止滑动

		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void setFocusable(boolean focusable) {
		// TODO Auto-generated method stub
		super.setFocusable(focusable);
	}
}
