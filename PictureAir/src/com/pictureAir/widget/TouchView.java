package com.pictureAir.widget;

import android.R.integer;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
/**
 * 
 * @author bauer_bao
 *
 */

@SuppressLint("NewApi") public class TouchView extends ImageView
{
	static final int NONE = 0;
	static final int DRAG = 1;	   //拖拽模式
	static final int ZOOM = 2;     //缩放模式
	static final int BIGGER = 3;   //放大
	static final int SMALLER = 4;  //缩小
	static final int DOWN =5;
	static final int ON_CLICK = 6;//点击的消息回调
	private int mode = NONE;	   //无
	

	private float beforeLenght;   //之前长度
	private float afterLenght;    //之后长度
	private float scale = 0.04f;  //每次缩放比例
	
	private int type;

	//    private int screenW;
	//    private int screenH;

	/*记录控件坐标*/
	private int start_x;
	private int start_y;
	private int stop_x ;
	private int stop_y ;

	private int screenWidth, screenHeight, bitmapw, bitmaph, fraw, frah,toolheight;
	private Handler handler;

	private TranslateAnimation trans; //移动的动画效果

	public TouchView(Context context,int screenWidth,int screenHeight,int bitmapw,int bitmaph, int fraw,int frah,int toolheight, Handler handler)
	{
		super(context);
		this.setPadding(0, 0, 0, 0);
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.bitmapw = bitmapw;
		this.bitmaph = bitmaph;
		this.fraw = fraw;
		this.frah = frah;
		this.toolheight = toolheight;
		this.handler = handler;
		System.out.println(screenWidth+"{"+screenHeight+"}"+bitmapw+"{"+bitmaph+"}"+fraw+"{"+frah);
	}
	public void changevalue(int bitmapw,int bitmaph) {
		this.bitmapw = bitmapw;
		this.bitmaph = bitmaph;
	}
	/**
	 * 计算两手指之间的间距
	 */
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}

	/**
	 * ontouch监听
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{	
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			System.out.println("down");
			type = MotionEvent.ACTION_DOWN;
			mode = DRAG;
			stop_x = (int) event.getRawX();
			stop_y = (int) event.getRawY();
			start_x = (int) event.getX();
			start_y = stop_y - this.getTop();
			if(event.getPointerCount()==2)
				beforeLenght = spacing(event);
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			type = MotionEvent.ACTION_POINTER_DOWN;
			System.out.println("ponter down");
			if (spacing(event) > 10f) {
				mode = ZOOM;
				beforeLenght = spacing(event);
			}
			break;
		case MotionEvent.ACTION_UP:
			System.out.println("up");
			/*手指离开的时候*/
			int disX = 0;
			int disY = 0;
			if(getHeight()<=screenHeight || this.getTop()<0)
			{
				if(this.getTop()<0 )
				{
					int dis = getTop();
					this.layout(this.getLeft(), 0, this.getRight(), 0 + this.getHeight());
					disY = dis - getTop();
				}
				else if(this.getBottom()>screenHeight)
				{
					disY = getHeight()- screenHeight+getTop();
					this.layout(this.getLeft(), screenHeight-getHeight(), this.getRight(), screenHeight);
				}
			}
			if(getWidth()<=screenWidth)
			{
				if(this.getLeft()<0)
				{
					disX = getLeft();
					this.layout(0, this.getTop(), 0+getWidth(), this.getBottom());
				}
				else if(this.getRight()>screenWidth)
				{
					disX = getWidth()-screenWidth+getLeft();
					this.layout(screenWidth-getWidth(), this.getTop(), screenWidth, this.getBottom());
				}
			}
			if(disX!=0 || disY!=0)
			{
				trans = new TranslateAnimation(disX, 0, disY, 0);
				trans.setDuration(500);
				this.startAnimation(trans);
			}
			mode = NONE;
			if (type == MotionEvent.ACTION_DOWN) {
				type = MotionEvent.ACTION_UP;
				handler.sendEmptyMessage(ON_CLICK);
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			System.out.println("pointer up");
			mode = NONE;
			type = MotionEvent.ACTION_POINTER_UP;
			break;
		case MotionEvent.ACTION_MOVE:
			System.out.println("move");
			
			/*移动模式*/
			if (mode == DRAG) {
				int currentx = (int)event.getRawX()-stop_x;
				int currenty = (int)event.getRawY()-stop_y;
//				System.out.println("currentx = "+ currentx + "currenty = "+ currenty);
				if (Math.abs(currentx) > 5 || Math.abs(currenty) > 5) {
					type = MotionEvent.ACTION_MOVE;
				}else {
//					System.out.println("小于5-------》");
					
				}
//				System.out.println("x---->"+event.getRawX() + "y----->"+event.getRawY());
				
				int left = this.getLeft()+currentx;
				int top = this.getTop()+currenty;
				int right = this.getRight()+currentx;
				int bottom = this.getBottom()+currenty;
				//判断图片可以拖动的边界范围
				int l = (fraw-bitmapw)/2;
				int r = (screenWidth+bitmapw)/2;
				int t = screenHeight-(frah+bitmaph+toolheight*2)/2;
				int b = screenHeight-(frah-bitmaph+toolheight*2)/2;
				if(left < l){  
					left = l;  
					right = left + this.getWidth();  
				}                     
				if(right > r){  
					right = r;  
					left = right - this.getWidth();  
				}                     
				if(top < t){  
					top = t;  
					bottom = top + this.getHeight();  
				}                     
				if(bottom > b){  
					bottom = b;  
					top = bottom - this.getHeight();  
				} 
				this.layout(left, top, right, bottom);
				stop_x = (int)event.getRawX();
				stop_y = (int)event.getRawY();

			} 
			/*缩放模式*/
			else if (mode == ZOOM) {
				type = MotionEvent.ACTION_MOVE;
				if(spacing(event)>10f)
				{
					afterLenght = spacing(event);
					float gapLenght = afterLenght - beforeLenght;                     
					if(gapLenght == 0) {  
						break;
					}
					else if(Math.abs(gapLenght)>5f)
					{
						if(gapLenght>0) { 
							this.setScale(scale,BIGGER);   
						}else {  
							this.setScale(scale,SMALLER);   
						}                             
						beforeLenght = afterLenght; 
					}
				}
			}
			break;
		}
		return true;	
	}
	//	public int getleft(){
	//		System.out.println(this.getLeft());
	//		return this.getleft();
	//				
	//	}
	//	public int gettop(){
	//		System.out.println(this.getTop());
	//		return this.gettop();
	//				
	//	}
	/**
	 * 缩放实现
	 */
	private void setScale(float temp,int flag) {   

		if(flag==BIGGER) { 
			this.setFrame(this.getLeft()-(int)(temp*this.getWidth()),    
					this.getTop()-(int)(temp*this.getHeight()),    
					this.getRight()+(int)(temp*this.getWidth()),    
					this.getBottom()+(int)(temp*this.getHeight()));      
		}else if(flag==SMALLER){   
			this.setFrame(this.getLeft()+(int)(temp*this.getWidth()),    
					this.getTop()+(int)(temp*this.getHeight()),    
					this.getRight()-(int)(temp*this.getWidth()),    
					this.getBottom()-(int)(temp*this.getHeight()));   
		}   
	}

	/**
	 * 设定控件当前位置
	 */
	private void setPosition(int left,int top,int right,int bottom) {  
		this.layout(left,top,right,bottom);           	
	}

}
