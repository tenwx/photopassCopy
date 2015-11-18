package com.pictureAir.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.EditText;

public class LineEditText extends EditText{
	private Paint mPaint;

	public LineEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(Color.BLUE);
	}
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		canvas.drawLine(0,this.getHeight()-1,  this.getWidth()-1, this.getHeight()-1, mPaint);
		canvas.drawLine(0,this.getHeight()-1,  0, this.getHeight()-5, mPaint);
		canvas.drawLine(this.getWidth()-1, this.getHeight()-1,  this.getWidth()-1, this.getHeight()-5, mPaint);
	}
}
