package com.pictureair.photopass.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.ImageButton;

public class MyImageButton extends ImageButton{
	private Paint mPaint;
	public MyImageButton(Context context) {
		super(context);
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
