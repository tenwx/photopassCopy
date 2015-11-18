package com.pictureAir;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.OnZoomChangeListener;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.transition.ChangeBounds;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.pictureAir.adapter.FrameGridViewAdapter;
import com.pictureAir.util.AppManager;
import com.pictureAir.util.AppUtil;
import com.pictureAir.util.Common;
import com.pictureAir.util.ScreenUtil;
import com.pictureAir.widget.MyToast;
import com.pictureAir.widget.RotateView;
import com.pictureAir.widget.RotatetextView;

/**
 * 拍照界面
 * 
 * @author bauer_bao
 * 
 */
@SuppressLint("NewApi")
public class CameraActivity extends Activity implements SurfaceHolder.Callback, OnClickListener, OnZoomChangeListener {
	private SurfaceView mySurfaceView = null;
	private SurfaceHolder mySurfaceHolder = null;
	private ImageButton recordButton;
	private RotateView flashButton;
	private RotateView switchButton;
	private RotateView timeButton;
	private RotatetextView timetextView;
	private RotateView viewButton;
	private RotateView returnButton;
	private RotateView magicButton;
	private Camera mycamera = null;
	private Camera.Parameters myParameters;
	private boolean isview = false;// 相机是否在预览中
	private static int DELAY_TIME_0S = 0;
	private static int DELAY_TIME_3S = 1;
	private static int DELAY_TIME_10S = 2;
	private int time_flag = DELAY_TIME_0S;
	private boolean taking_flag = false;// 是否在拍摄中的标志
	private Bitmap bitmap = null;
	private RotatetextView textView_time;
	private PopupWindow framePopupWindow;
	private int currentpicposition = 0;// 边框选择序号
	private View frameView_popwindow;
	private GridView framegridview;
	private FrameGridViewAdapter frameGridViewAdapter;
	private ArrayList<String> framelist;// 最新下载的边框的list
	private int[] pic = { R.drawable.frame_none, R.drawable.frame1_1,
			R.drawable.frame2_2, R.drawable.frame3_3, R.drawable.frame4_4,
			R.drawable.frame5_5, R.drawable.frame6_6, R.drawable.frame7_7,
			R.drawable.frame8_8 };// 缩略图
	private int[] pic2 = { 0, R.drawable.frame1, R.drawable.frame2,
			R.drawable.frame3, R.drawable.frame4, R.drawable.frame5,
			R.drawable.frame6, R.drawable.frame7, R.drawable.frame8 };// 原图
	int cntSave = 0;
	int x, y;
	int myzoommax;
	int zoom = 0;
	int time;
	private int degree = 0;
	private int mTargetZoomValue;
	private static final int ZOOM_STOPPED = 0;
	private static final int ZOOM_START = 1;
	private static final int ZOOM_STOPPING = 2;
	private int mZoomState = ZOOM_STOPPED;
	private final ZoomListener mZoomListener = new ZoomListener();
	
	private int cameraPosition = 1; // 0代表前置摄像头，1代表后置摄像头
	
	private int camera_flash = 1;// 1代表off，2代表on，3代表auto
	private Camera.AutoFocusCallback myAutoFocusCallback;
	private SimpleDateFormat dateFormat;
	private File nameFile;
	private ImageView frame_imageView;
	private Bitmap pictBitmap;
	private Bitmap heBitmap;
	private Bitmap frameBitmap = null;
	private SensorManager mSensorManager = null;
	private Sensor mSensor = null;
	private float axis_x, axis_y, axis_z;
	private int lastzoom = 0;

	private MyToast newToast;

	// 获取手机牌子和型号
	private String mobiletype;
	private String mobilemodle;

	// 存放最后一张的拍摄照片路径
	private SharedPreferences sharedPreferences;
	private SharedPreferences.Editor editor;
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
 
	// 记录下触摸屏幕的坐标
	float fx;
	float fy;

	float x1 = 0;
	float y1 = 0;
	float z1 = 0;

	private View view_focus = null;

	private boolean isfocus = false;

	private RelativeLayout relativeLayout;

	private double nLenStart = 0;
	// 滑动的距离。
	private int progressInt = 0;
	// 是否 
	private boolean isMask = false;

	//纪录时间，两秒之内，触摸对焦之后不允许
	private long timeLong;

	private String lastPhotoUrl = "";
	
	//	记录屏幕是横屏还是竖屏   竖屏 1， 横屏 0。
	private int screenType = 1;
 
	private ImageView magicShow;
	private ImageView decoration;
	private int showX = 0;
	private int showY = 0;
	private LinearLayout titlebar;
	private ViewGroup anim_mask_layout;//动画层
	//判断 是否是 magicShot 模式
	private boolean isMagicShot = false;
	
	//用于解决 点击相册 再返回camera，还有动画的bug
	private boolean isCurActivity = true;
	
	//播放音效。
	SoundPool soundPool;
	HashMap<Integer,Integer> hashMap;
	int currentStreamId;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("oncreate--------");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		initSoundPool();
		
		isCurActivity = true;
		AppManager.getInstance().addActivity(this);
		int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;

		Window myWindow = this.getWindow();
		myWindow.setFlags(flag, flag);// 设置为全屏
		setContentView(R.layout.camera_activity);

//		DisplayMetrics metrics = new DisplayMetrics();
//		getWindowManager().getDefaultDisplay().getMetrics(metrics);// 获取屏幕宽高
//		x = metrics.widthPixels;// 获取宽度
//		y = metrics.heightPixels;
		x = ScreenUtil.getScreenWidth(this);
		y = ScreenUtil.getScreenHeight(this);
		
		Log.v("utils", "max mem in mb:" + (Runtime.getRuntime().maxMemory() / 1024 / 1024));// 获取分配给app的内存
		// 获取手机牌子和型号
		mobiletype = android.os.Build.MANUFACTURER;
		mobilemodle = android.os.Build.MODEL;
		System.out.println("mobiletype and mobile modle=" + mobiletype + "_" + mobilemodle);

		imageLoader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder().displayer(new RoundedBitmapDisplayer(10)).considerExifParams(true).build();//设置圆角图片
		newToast = new MyToast(CameraActivity.this);

		mySurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
		mySurfaceHolder = mySurfaceView.getHolder();
		mySurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);

		titlebar = (LinearLayout) findViewById(R.id.titlebar);
		recordButton = (ImageButton) findViewById(R.id.imagebutton_record);
		flashButton = (RotateView) findViewById(R.id.imageButton_flash);
		switchButton = (RotateView) findViewById(R.id.imageButton_switch);
		returnButton = (RotateView) findViewById(R.id.camera_return);
		timeButton = (RotateView) findViewById(R.id.imageButton_time);
		returnButton.setOnClickListener(this);
		flashButton.setOnClickListener(this);
		switchButton.setOnClickListener(this);
		timeButton.setOnClickListener(this);
		timetextView = (RotatetextView) findViewById(R.id.textview_time);
		timetextView.setText("0'S");
		view_focus = findViewById(R.id.view_focus);
		relativeLayout = (RelativeLayout) findViewById(R.id.previewview);
		magicShow = (ImageView) findViewById(R.id.magic_show);
		// 获取最新的framelist数量
		framelist = new ArrayList<String>();
		for (int i = 0; i < pic.length; i++) {
			framelist.add(String.valueOf(pic[i]));
		}

		textView_time = (RotatetextView) findViewById(R.id.textView_time);
		frame_imageView = (ImageView) findViewById(R.id.imageView1);
		frame_imageView.setLayoutParams(new RelativeLayout.LayoutParams(x, (int) (x * 4 / 3)));
		mySurfaceHolder.addCallback(this);
		mySurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		viewButton = (RotateView) findViewById(R.id.button2);
		viewButton.setOnClickListener(this);
		magicButton = (RotateView) findViewById(R.id.imagebutton_magic);
		magicButton.setOnClickListener(this);

		frameView_popwindow = getLayoutInflater().inflate(R.layout.popupwindow_gridview, null);
		framePopupWindow = new PopupWindow(frameView_popwindow, LayoutParams.MATCH_PARENT, ScreenUtil.getScreenWidth(this) / 3);
		framePopupWindow.setFocusable(true);// 设置能够获得焦点
		framePopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));// 此代码和上一条代码两者结合，实现能够点击popupwindow外面将popupwindow关闭

		framegridview = (GridView) frameView_popwindow.findViewById(R.id.horizontal_gridView1);

		sharedPreferences = getSharedPreferences("pictureAir", MODE_PRIVATE);
		editor = sharedPreferences.edit();

		dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
		nameFile = new File(Common.PHOTO_SAVE_PATH);
		nameFile.mkdirs();// 创建根目录文件夹
		// 自动对焦的回调函数  聚焦
		myAutoFocusCallback = new Camera.AutoFocusCallback() {

			public void onAutoFocus(boolean success, Camera camera) {// 调用一次自动对焦
				// TODO Auto-generated method stub
				// 如果是这种模式的话，先改变这种模式。
				// Log.e(" myParameters.getFocusMode() ",
				// "myParameters.getFocusMode() :"+myParameters.getFocusMode());
				// Log.e(" Parameters.FLASH_MODE_ON ",
				// "Parameters.FLASH_MODE_ON :"+Parameters.FLASH_MODE_ON);
				// if(myParameters.getFocusMode().equals(Parameters.FLASH_MODE_ON)){

				//				if (!taking_flag) {
				//				if(time > 1){
				if (success)  {
					// mode = MODE.FOCUSED;
					view_focus.setBackgroundResource(R.drawable.ic_focus_focused);
				} else {
					// mode = MODE.FOCUSFAIL;
					view_focus.setBackgroundResource(R.drawable.ic_focus_failed);
				}
				//					if (!taking_flag) {
				setFocusView();
				x1 = 0;
				y1 = 0;
				z1 = 0;
				//					}
				//						
				isfocus = false;
				////				}
				////				}
				if(camera_flash == 2){
					myParameters.setFlashMode(Parameters.FLASH_MODE_ON);
					mycamera.setParameters(myParameters);
				}
			}
		};
		// 触摸对焦的实现
		mySurfaceView.setOnTouchListener(new OnTouchListener() {// 触摸对焦

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// 手势识别 。放大与缩小
				int nCnt = event.getPointerCount();

				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					// Log.e("ACTION_DOWN:",
					// "================================");
					isMask = false;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					// mode = MODE.FOCUSING;
					if (!isMask) {
						Log.e(" log ", " time : "+time);
						int width = view_focus.getWidth();
						int height = view_focus.getHeight();
						view_focus.setVisibility(View.VISIBLE);
						view_focus.setBackgroundResource(R.drawable.ic_focus_focusing);
						view_focus.setX(event.getX() - (width / 2));
						view_focus.setY(event.getY() - (height / 2));
						//							if (!taking_flag) {
						isfocus = true;
						//取消
						handler2.removeCallbacks(runnable2); 
						// 纪录时间，
						timeLong = System.currentTimeMillis();
						focusOnTouch(event);
					}
				} else if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN && 2 == nCnt) {
					isMask = true;
					int xlen = Math.abs((int) event.getX(0) - (int) event.getX(1));
					int ylen = Math.abs((int) event.getY(0) - (int) event.getY(1));

					nLenStart = Math.sqrt((double) xlen * xlen + (double) ylen * ylen);
					lastzoom = (int) Math.floor(Math.abs(nLenStart * myzoommax / 500));
				} else if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE && 2 == nCnt) {
					isMask = true;
					int xlen = Math.abs((int) event.getX(0) - (int) event.getX(1));
					int ylen = Math.abs((int) event.getY(0) - (int) event.getY(1));

					double nLenEnd = Math.sqrt((double) xlen * xlen + (double) ylen * ylen);
					caculateBig((int) Math.floor(Math.abs(nLenEnd * myzoommax / 500)));
				}

				return true;
			}
		});
		// 拍照
		recordButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if (!taking_flag) {
					System.out.println("ok");
					if (time_flag == DELAY_TIME_3S) {// 如果延迟开关打开，则开始计时，计时结束之后再拍照
						System.out.println("delay 3s");
						time = 3;
						handler.postDelayed(runnable, 0);
					} else if (time_flag == DELAY_TIME_10S) {
						handler.postDelayed(runnable_10, 0);
						time = 10;
						System.out.println("delay 10s");
					} else {// 否则，直接拍照
						change();
						handler2.removeCallbacks(runnable4);
						handler2.removeCallbacks(runnable2);
						Log.e("直接拍照", "："+System.currentTimeMillis());
						mycamera.takePicture(null, null, myjpegCallback);
						handler2.postDelayed(runnable5, 500);
					}
										
					taking_flag = true;
				} else {
					System.out.println("fail");
					newToast.setTextAndShow(R.string.is_taking, Common.TOAST_SHORT_TIME);
				}

			}
		});
	}

	private final class ZoomListener implements android.hardware.Camera.OnZoomChangeListener {
		public void onZoomChange(int value, boolean stopped, android.hardware.Camera camera) {
			Log.v("camera", "Zoom changed: value=" + value + ". stopped=" + stopped);
			zoom = value;
			// Keep mParameters up to date. We do not getParameter again in
			// takePicture. If we do not do this, wrong zoom value will be set.
			myParameters.setZoom(value);
			// We only care if the zoom is stopped. mZooming is set to true when
			// we start smooth zoom.
			if (stopped && mZoomState != ZOOM_STOPPED) {
				if (value != mTargetZoomValue) {
					mycamera.startSmoothZoom(mTargetZoomValue);
					mZoomState = ZOOM_START;
				} else {
					mZoomState = ZOOM_STOPPED;
				}
			}
		}
	}

	private void initializeZoom() {
		if (!myParameters.isZoomSupported())
			return;

		// mGestureDetector = new GestureDetector(this, new
		// ZoomGestureListener());

		mycamera.setZoomChangeListener(mZoomListener);
	}

	ShutterCallback myShutterCallback = new ShutterCallback() {

		@Override
		public void onShutter() {
			// TODO Auto-generated method stub

		}
	};

	// 回调函数，合成图片保存图片都在这里处理
	PictureCallback myjpegCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			handler2.removeCallbacks(runnable4);
			if (null != data) {
				File photoFile = new File(nameFile + "/"
						+ dateFormat.format(new Date()) + ".JPG");
				mycamera.stopPreview();
				if(isMagicShot){
					//消失掉  magicShow 的模版，动画继续
					magicShow.setVisibility(View.GONE);
					
					decoration = new ImageView(CameraActivity.this);// buyImg是动画的图片
					Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.decorationtest); 
					decoration.setImageResource(R.drawable.decorationtest);
					
					showX = x*90/584-bitmap.getWidth()/2;  //减去的50和饰品的高度有关
					showY = (magicShow.getHeight()*4/7)+(relativeLayout.getHeight()-magicShow.getHeight())/2+titlebar.getHeight()-bitmap.getHeight();
                    if(isCurActivity){
                    	setAnim(decoration,showX,showY, bitmap);
                    }
					
					//原始的 饰品
//					Bitmap decorationBitmap = BitmapFactory.decodeResource(
//							getResources(), R.drawable.decoration13);
					int decorationWidth = bitmap.getWidth();
					int decorationHeight = bitmap.getHeight();
					Log.e("decorationWidth :", " :"+decorationWidth+"_:"+decorationHeight);
					
					pictBitmap = BitmapFactory.decodeByteArray(data, 0,
							data.length);// data是字节数据，将其解析成位图
					
					 
					 change1(pictBitmap.getWidth(),pictBitmap.getHeight());
					 //  获取的是新的 pictBitmap 的高度
					 int picBitmapWidth = pictBitmap.getWidth();
					 int picBitmapHeight = pictBitmap.getHeight();
					 Matrix matrix = new Matrix();
					 //计算出 饰品的缩放比例。
				     matrix.postScale((float)((float)picBitmapWidth*decorationWidth/x)/decorationWidth, (float)((float)picBitmapHeight*decorationHeight/((float)x*4/3))/decorationHeight);    

				     Bitmap newDecorationBitmap = Bitmap.createBitmap(bitmap, 0, 0, decorationWidth,    
				    		 decorationHeight, matrix, true);   
				     
					 // 接下来图片合成
				     
					 Bitmap newbmp = Bitmap.createBitmap(picBitmapWidth, picBitmapHeight, Config.ARGB_8888); 
					 
					 int drawX = picBitmapWidth*showX/x + picBitmapWidth*45/relativeLayout.getWidth();  // 加上的数字和 图片有关，45与 20 
					 int drawY = picBitmapHeight*(showY-titlebar.getHeight())/(x*4/3) + picBitmapHeight*20/relativeLayout.getHeight();
					 Log.e(" == ", "picBitmapWidth:"+picBitmapWidth);
					 Log.e("==", "drawX:"+drawX+"_drawY:"+drawY);
					 Canvas cv = new Canvas(newbmp);
					 cv.drawBitmap(pictBitmap, 0, 0, null);
					 pictBitmap.recycle();
					 cv.drawBitmap(newDecorationBitmap, drawX, drawY, null);
					 newDecorationBitmap.recycle();
					 cv.save(Canvas.ALL_SAVE_FLAG);//保存   
			         cv.restore();//存储
			         // 保存图片到sdcard
						if (null != newbmp) {
							System.out.println(photoFile.toString());
							FileOutputStream fileOutputStream2 = null;
							try {
								fileOutputStream2 = new FileOutputStream(
										photoFile);
								BufferedOutputStream bos = new BufferedOutputStream(
										fileOutputStream2);
								newbmp.compress(Bitmap.CompressFormat.JPEG,
										100, bos);
								bos.flush();
								bos.close();
								fileOutputStream2.close();
							} catch (IOException e) {
								// TODO: handle exception
							}
						}
						newbmp.recycle();
				}else{
					FileOutputStream outputStream;

					try {
						outputStream = new FileOutputStream(photoFile);
						outputStream.write(data); // 写入sd卡中
						outputStream.close(); // 关闭输出流
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				System.gc();// 通知系统强制回收内存
				// 记录最后一次拍摄的图片URL
				editor.putString(Common.LAST_PHOTO_URL, photoFile.toString());
				editor.commit();

				UpdateLastPhoto(photoFile.toString());
				if (!isMagicShot) {
					mycamera.startPreview();
					taking_flag = false;
				}
//				mycamera.startPreview();
//				taking_flag = false;
				((MyApplication)getApplication()).scanMagicFinish = false;
				scan(photoFile.toString());
			} else {
				System.out.println("take picture failed");
				newToast.setTextAndShow(R.string.take_photo_failed, Common.TOAST_SHORT_TIME);
			}
		}
	};

	/**
	 * //主动让媒体库去更新最新文件
	 * 
	 * @param file
	 *            需要扫描的文件
	 */
	private void scan(final String file) {
		// TODO Auto-generated method stub
		MediaScannerConnection.scanFile(this, new String[] { file },
				new String[] { "image/*" },
				new MediaScannerConnection.OnScanCompletedListener() {
			public void onScanCompleted(String path, Uri uri) {
				System.out.println("okfads");
			}
		});
	}

	public static void scanPhotos(String filePath, Context context) {
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		Uri uri = Uri.fromFile(new File(filePath));
		intent.setData(uri);
		context.sendBroadcast(intent);
	}

	/**
	 * 更新预览图
	 * 
	 * @param URL
	 *            预览图的最新的URL
	 */
	private void UpdateLastPhoto(String URL) {
		System.out.println("view image url is "+ URL);
		if (URL.equals("")) {
			viewButton.setImageResource(R.drawable.camera_view_empty);
		}else {
			imageLoader.displayImage("file://" + URL, viewButton, options);
		}
	}

	// 重力感应 的监听，负责图标的动态旋转
	SensorEventListener lsn = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			axis_x = event.values[SensorManager.DATA_X];
			axis_y = event.values[SensorManager.DATA_Y];
			axis_z = event.values[SensorManager.DATA_Z];

			if (x1 == 0 && y1 == 0 && z1 == 0) {
				x1 = axis_x;
				y1 = axis_y;
				z1 = axis_z;

			}
			if (Math.sqrt((axis_x - x1) * (axis_x - x1) + (axis_y - y1)
					* (axis_y - y1) + (axis_z - z1) * (axis_z - z1)) > 1) {
				if (cameraPosition == 1) {

					if (!isfocus) {
						if(System.currentTimeMillis() - timeLong > 4000){
							System.out.println("need do focus");
							if(camera_flash == 2){
								myParameters.setFlashMode(Parameters.FLASH_MODE_OFF);
								mycamera.setParameters(myParameters);
							}
							if(!taking_flag){
								handler2.postDelayed(runnable2, 2000);
							}
						
							isfocus = true;
						}
					}
				}

			}
			if (axis_y > Math.abs(axis_x)) {// 手机竖直的时候,先判断方向，再判断是不是处于临界点
				if (axis_y > Math.abs(axis_x) + 1) {// 通过+1，区分临界点
					screenType = 1;
					dorotate(0);
					degree = 0;
					// System.out.println("degree = "+degree);
				}
			} else if (axis_x > Math.abs(axis_y)) {// 向左旋转了90的状态
				if (axis_x > Math.abs(axis_y) + 1) {
					screenType = 0;
					dorotate(270);
					degree = 270;
					// System.out.println("degree = "+degree);
				}
			} else if ((axis_y < axis_x && axis_x < 0)
					|| (Math.abs(axis_y) > axis_x && axis_x > 0)) {// 手机倒立的状态
				screenType = 3;
				if ((axis_y < axis_x - 1 && axis_x < 0)
						|| (Math.abs(axis_y) > axis_x + 1 && axis_x > 0)) {
					dorotate(180);
					degree = 180;
					// System.out.println("degree = "+degree);
				}
			} else if ((axis_x < axis_y && axis_y < 0)
					|| (Math.abs(axis_x) > axis_y && axis_y > 0)) {// 向右旋转了90的状态
				screenType = 2;
				if ((axis_x < axis_y - 1 && axis_y < 0)
						|| (Math.abs(axis_x) > axis_y + 1 && axis_y > 0)) {
					dorotate(90);
					degree = 90;
					// System.out.println("degree = "+degree);
				}
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}
	};

	// 倒计时，并且拍照
	Handler handler = new Handler();
	Runnable runnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (time == 0) {// 如果为0，则开始拍照，并且将时间重置为3s
				if (isview && mycamera != null) {
					// taking_flag = true;
					handler2.removeCallbacks(runnable4);
					handler2.removeCallbacks(runnable2);
					mycamera.takePicture(null, null, myjpegCallback);
					handler2.postDelayed(runnable5, 500);
					//					view_focus.setVisibility(View.GONE);
					time = 3;

				}
				textView_time.setVisibility(View.GONE);
			} else {// 如果不为0.实现倒计时
				textView_time.setVisibility(View.VISIBLE);
				textView_time.setText("" + time);
				System.out.println(time);
				time--;

				handler.postDelayed(this, 1000);
			}
		}
	};
	Runnable runnable_10 = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub

			if (time == 0) {// 如果为0，则开始拍照，并且将时间重置为3s
				if (isview && mycamera != null) {
					handler2.removeCallbacks(runnable4);
					handler2.removeCallbacks(runnable2);
					mycamera.takePicture(null, null,
							myjpegCallback);
					handler2.postDelayed(runnable5, 500);
					//					view_focus.setVisibility(View.GONE);
					time = 10;
					taking_flag = true;
				}
				textView_time.setVisibility(View.GONE);
			} else {// 如果不为0.实现倒计时
				textView_time.setVisibility(View.VISIBLE);
				textView_time.setText("" + time);
				System.out.println(time);
				time--;
				handler.postDelayed(this, 1000);
			}
		}
	};

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent;
		switch (v.getId()) {
		case R.id.imageButton_switch:// 前置后置转换键
			int cameraCount = 0;
			CameraInfo cameraInfo = new CameraInfo();
			cameraCount = Camera.getNumberOfCameras();// 得到摄像头的个数
			for (int i = 0; i < cameraCount; i++) {
				Camera.getCameraInfo(i, cameraInfo);// 得到每一个摄像头的信息
				if (cameraPosition == 1) {
					// 现在是后置，变更为前置
					if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {// 代表摄像头的方位，CAMERA_FACING_FRONT前置
						// CAMERA_FACING_BACK后置
						mycamera.stopPreview();//  停掉原来摄像头的预览
						mycamera.release();// 释放资源
						mycamera = null;// 取消原来摄像头
						System.out.println("--------------");
						mycamera = Camera.open(i);// 打开当前选中的摄像头
						
						Log.e("iiiiiiiiiiiiii", "i 1"+i);
						
						isview = false;
						initcamera(270, i);// 因为前置本来拍出来的就是镜像，所以要旋转270°
						cameraPosition = 0;
						// seekBar.setProgress(0);//将变焦杆置0
						break;
					}
				} else {
					// 现在是前置， 变更为后置
					if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {// 代表摄像头的方位，CAMERA_FACING_FRONT前置
						// CAMERA_FACING_BACK后置
						mycamera.stopPreview();// 停掉原来摄像头的预览
						mycamera.release();// 释放资源
						mycamera = null;// 取消原来摄像头
						mycamera = Camera.open(i);// 打开当前选中的摄像头
						Log.e("iiiiiiiiiiiiii", "i 2"+i);
						System.out.println("==============");
						isview = false;
						initcamera(90, i);
						cameraPosition = 1;
						// seekBar.setProgress(0);//将变焦杆置0
						break;
					}
				}
			}
			break;
		case R.id.imageButton_flash:// 闪光灯 1代表off，2代表on，3代表auto
			if (camera_flash == 1) {

				flashButton.setImageResource(R.drawable.camera_flash_on);
				flashButton.setScaleType(ScaleType.CENTER_INSIDE);
				// mycamera.stopPreview();
				myParameters.setFlashMode(Parameters.FLASH_MODE_ON);
				Log.e("Parameters.FLASH_MODE_ON ",
						"Parameters.FLASH_MODE_ON : "
								+ Parameters.FLASH_MODE_ON);
				Log.e("myParameters ",
						"myParameters : " + myParameters.getFlashMode());

				mycamera.setParameters(myParameters);
				// mycamera.startPreview();
				camera_flash = 2;
				zoom += 1;

				// flashButton.setText("on");
			} else if (camera_flash == 2) {
				flashButton.setImageResource(R.drawable.camera_flash_auto);
				flashButton.setScaleType(ScaleType.CENTER_INSIDE);
				// mycamera.stopPreview();
				myParameters.setFlashMode(Parameters.FLASH_MODE_AUTO);
				mycamera.setParameters(myParameters);
				// mycamera.startPreview();
				camera_flash = 3;
				zoom += 1;

				// flashButton.setText("auto");
			} else if (camera_flash == 3) {
				flashButton.setImageResource(R.drawable.camera_flash_off);
				flashButton.setScaleType(ScaleType.CENTER_INSIDE);
				// mycamera.stopPreview();
				myParameters.setFlashMode(Parameters.FLASH_MODE_OFF);
				mycamera.setParameters(myParameters);
				// mycamera.startPreview();
				camera_flash = 1;
				zoom += 1;

				// flashButton.setText("off");
			}
			break;
		case R.id.imageButton_time:// 倒计时按钮
			System.out.println("time");

			if (time_flag == DELAY_TIME_0S) {
				//				time = 3;
				time_flag = DELAY_TIME_3S;
				timetextView.setText("3'S");
				System.out.println("3s");
			} else if (time_flag == DELAY_TIME_3S) {
				//				time = 10;
				time_flag = DELAY_TIME_10S;
				timetextView.setText("10'S");
				System.out.println("10s");

			} else {
				//				time = 0;
				time_flag = DELAY_TIME_0S;
				timetextView.setText("0'S");
				System.out.println("0s");
			}
			break;
		case R.id.camera_return:// 返回按钮
			System.out.println("return");
			// intent = new Intent(this, MainTabActivity.class);
			//
			// startActivity(intent);
			finish();
			break;
		case R.id.button2:// 图片预览按钮
			if (taking_flag) {
				newToast.setTextAndShow(R.string.is_taking, Common.TOAST_SHORT_TIME);
			}else{
			isCurActivity = false;
			//取消当前的倒计时操作
			view_focus.setVisibility(View.GONE);
			timetextView.setText("0'S");
			time = 0;
			time_flag = DELAY_TIME_0S;
			taking_flag = false;
			textView_time.setVisibility(View.GONE);
			handler.removeCallbacks(runnable);
			handler.removeCallbacks(runnable_10);

			intent = new Intent(this, ViewPhotoActivity.class);
			//			intent = new Intent(this, ViewOrSelectPhotoActivity.class);
			// intent2.putExtra("activity", "cameraactivity");
			// CameraActivity.this.finish();
			CameraActivity.this.startActivity(intent);
			}
			break;
		case R.id.imagebutton_magic:// 设置 magic
			if(isMagicShot){
				magicShow.setVisibility(View.GONE);
				magicButton.setImageResource(R.drawable.magic_btn);
				isMagicShot = false;
			}else{
				magicShow.setVisibility(View.VISIBLE);
				magicButton.setImageResource(R.drawable.magic_btn_sele);
				isMagicShot = true;
			}
			
//			int[] loaction = new int[2];
//			findViewById(R.id.bottombar).getLocationOnScreen(loaction);// 获取底部菜单栏的坐标
//
//			// 加载边框
//			frameGridViewAdapter = new FrameGridViewAdapter(
//					CameraActivity.this, framelist, pic.length);
//			framegridview.setAdapter(frameGridViewAdapter);
//			int frameWidth = ScreenUtil.getScreenWidth(this);
//
//			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//					framelist.size() * (frameWidth / 4 + 5) + 10,
//					LinearLayout.LayoutParams.WRAP_CONTENT);// 设置gridview的宽和高
//			framegridview.setLayoutParams(params);
//
//			framegridview.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
//			framegridview.setNumColumns(framelist.size());
//			framegridview.setHorizontalSpacing(10);
//			framegridview.setColumnWidth(frameWidth / 4 - 5);
//			framegridview.setOnItemClickListener(new OnItemClickListener() {
//
//				@Override
//				public void onItemClick(AdapterView<?> parent, View view,
//						int position, long id) {
//					// TODO Auto-generated method stub
//					currentpicposition = position;
//					System.out.println("positon:" + currentpicposition);
//					// popupWindow.showAtLocation(findViewById(R.id.button2_frame),
//					// Gravity.CENTER, 0, 0);
//					loadframe(currentpicposition);
//				}
//			});
//			framePopupWindow.showAtLocation(findViewById(R.id.bottombar),
//					Gravity.NO_GRAVITY, loaction[0], loaction[1]
//							- framePopupWindow.getHeight());// 显示在控件的上方
			break;
		default:
			break;
		}
	}

	/**
	 * 加载边框函数
	 */
	private void loadframe(final int positon) {
		if (positon != 0) {

			new Thread(){
				public void run() {
					bitmap = BitmapFactory.decodeResource(getResources(), pic2[positon]);
					Matrix m = new Matrix();
					// 缩放尺寸，保持和边框一致
					m.postScale((float) x / (bitmap.getWidth()), (float) x / (bitmap.getWidth()));

					bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
					handler3.sendEmptyMessage(1111);
				};
			}.start();

		} else {
			frame_imageView.setVisibility(View.INVISIBLE);
		}

		if (framePopupWindow.isShowing()) {
			framePopupWindow.dismiss();
		}
	}

	Handler handler3 = new Handler(){
		public void handleMessage(android.os.Message msg) {
			// 加载预览边框
			frame_imageView.setImageBitmap(bitmap);
			frame_imageView.setVisibility(View.VISIBLE);
			System.out.println(bitmap.getWidth() + "+" + bitmap.getHeight());
		};
	};

	// surfaceview创建的时候
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		System.out.println("surfacecreated===============");
		//判断。
		if (cameraPosition == 1) {
			initcamera(90, 0);
		}else{
			initcamera(270, 1);
		}
		initializeZoom();
		// mycamera.startPreview();
		mySurfaceView.setLayoutParams(new RelativeLayout.LayoutParams(x, (int) (x * 4 / 3)));// 设置surfaceview的高度，实现4：3
//		magicShow.setLayoutParams(new RelativeLayout.LayoutParams(x, (int) (x * 4 / 3)));

		// 默认加载第一张，暂时没有图片，需要图片
		loadframe(currentpicposition);
	}

	// surfaceview改变的时候
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		System.out.println("surfacechanged===========");
	}

	// surface销毁的时候，释放资源
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		System.out.println("surfacedestroyed--------");

		handler2.removeCallbacks(runnable2);
		handler2.removeCallbacks(runnable3);

		mycamera.stopPreview();
		mycamera.release();
		mycamera = null;
		isview = false;
		if (currentpicposition != 0) {
			bitmap.recycle();
			bitmap = null;
		}

	}

	// 预览图缩放变化的时候
	@Override
	public void onZoomChange(int zoomValue, boolean stopped, Camera camera) {
		// TODO Auto-generated method stub
		// onz
		myParameters.setZoom(zoom);
		mycamera.setParameters(myParameters);
		System.out.println(zoom);
		System.out.println(zoomValue);

	}

	/**
	 * 控件的旋转
	 * @param degree 需要旋转的角度
	 */
	private void dorotate(int degree) {
		returnButton.setOrientation(degree, true);
		flashButton.setOrientation(degree, true);
		switchButton.setOrientation(degree, true);
		timeButton.setOrientation(degree, true);
		magicButton.setOrientation(degree, true);
		viewButton.setOrientation(degree, true);
		timetextView.setOrientation(degree, true);
		textView_time.setOrientation(degree, true);
	}

	/**
	 * 初始化camera
	 */
	public void initcamera(int rotation, int cameraId) {
		Log.e("＝＝＝＝", "cameraId :"+cameraId);
		CameraInfo info = new CameraInfo();
		Camera.getCameraInfo(cameraId, info);// 得到每一个摄像头的信息

		int result = 0;
		if (mycamera == null && !isview) {
//			mycamera = Camera.open(1);
//			if (cameraPosition == 0) {
				mycamera = Camera.open(cameraId);// 打开相机
//			}else{
//				mycamera = Camera.open(0);// 打开相机
//			}
		}
		if (mycamera != null && !isview) {
			try {
				myParameters = mycamera.getParameters();
				if (!myParameters.isZoomSupported()) {
					System.out.println("zoom---false");
				} else {
					System.out.println("zoom---true");
				}
				if (!myParameters.isSmoothZoomSupported()) {
					System.out.println("smoothzoom---false");
				} else {
					System.out.println("smoothzoom----true");
				}
				// 获取可支持的最大放大尺寸
				myzoommax = myParameters.getMaxZoom();
				System.out.println("zoommax" + myzoommax);
				myParameters.setPictureFormat(PixelFormat.JPEG);
				myParameters.set("jpeg-quality", 100);// 设置图片质量，我支持的手机是从0-100，可能就是这个范围，没有仔细研究过
				// 获取手机可支持的preview尺寸
				List<Size> previewsizes = myParameters.getSupportedPreviewSizes();// 获取系统可支持的预览尺寸
				List<Size> photoSizes = myParameters.getSupportedPictureSizes();// 获取系统可支持的图片尺寸
				int previewsize_width = 1;
				int previewsize_height = 1;
				int picturesize_width = 1;
				int picturesize_height = 1;
				for (Size size : previewsizes) {// 设置为4：3的预览尺寸,有些手机遍历出来的结果从大到小，有的手机从小到大，所以只要一个最大的尺寸
					Log.d("previewSize", "width:" + size.width + " height "
							+ size.height);// 查询所有的预览尺寸
					if (size.width / 4 == size.height / 3) {
						if (size.width > previewsize_width) {
							previewsize_width = size.width;
							previewsize_height = size.height;
						}
					}
				}
				for (Size size : photoSizes) {// 设置为4：3图片尺寸，原理同上
					Log.d("PictureSizes", "width:" + size.width + " height "
							+ size.height);// 查询所有的图片尺寸
					if (size.width / 4 == size.height / 3) {
						Log.d("PictureSizes", "width:" + size.width
								+ " height " + size.height);// 查询所有4:3的图片尺寸
						if (size.width > picturesize_width) {
							picturesize_width = size.width;
							picturesize_height = size.height;
						}
					}
				}

				
				myParameters.setPreviewSize(previewsize_width,
						previewsize_height);
				Log.d("previewSize", "SET width:" + previewsize_width
						+ " height " + previewsize_height);
				myParameters.setPictureSize(picturesize_width,
						picturesize_height);
				Log.d("setPictureSize", "SET width:" + picturesize_width
						+ " height " + picturesize_height);

				myParameters.set("rotation", rotation);// 旋转角度

				if (cameraId == 0) {

					List<String> focuseMode = (myParameters
							.getSupportedFocusModes());
					for (int i = 0; i < focuseMode.size(); i++) {
						Log.i("tag", focuseMode.get(i));
						if (focuseMode.get(i).contains("continuous")) {
							myParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
						} else {
							myParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
						}
					}
				}
				if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					result = (info.orientation) % 360;
					result = (360 - result) % 360;
				}else {
					result = (info.orientation + 360) % 360;
				}
				System.out.println("-------------->result------------->"+result);
				mycamera.setDisplayOrientation(result);// 显示方向旋转角度
				mycamera.setParameters(myParameters);
				mycamera.setPreviewDisplay(mySurfaceHolder);
				mycamera.startPreview();// 开始预览
				isview = true;
				if (cameraId == 0) {
					mycamera.cancelAutoFocus();// 取消当前的自动对焦，保证下一次对焦的开始
					mycamera.autoFocus(myAutoFocusCallback);// 自动对焦
				}
				isfocus = true;
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		if (mycamera != null && isview) {
			isfocus = true;
			mycamera.autoFocus(myAutoFocusCallback);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//测试返回时 cameraposition是多少。
		isCurActivity = true;
		Log.e("＝＝＝＝", "position:"+cameraPosition);
//		if (decoration!=null) {
//			decoration.clearAnimation();
//		}
		
		//更新 最新照片信息。
		lastPhotoUrl = sharedPreferences.getString(Common.LAST_PHOTO_URL, "");
		File thumbnialFile = new File(lastPhotoUrl);
		if (lastPhotoUrl.equals("") || !thumbnialFile.exists()) {//变量中没有这个字段，查找文件
			
			lastPhotoUrl = AppUtil.findLatestPic();
			//将找到的路径存放在sharedprefrence中
			editor.putString(Common.LAST_PHOTO_URL, lastPhotoUrl);
			editor.commit();
		}
		
		UpdateLastPhoto(lastPhotoUrl);

		if (null == mSensorManager) {
			mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
			mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			mSensorManager.registerListener(lsn, mSensor, SensorManager.SENSOR_DELAY_UI);
		}
		System.out.println("resume------");
	}

	@Override
	protected void onDestroy() {// 因为推出的时候，还是会执行surfaceviewdestroy，所以不需要在这里释放
		// TODO Auto-generated method stub
		super.onDestroy();

		AppManager.getInstance().killActivity(this);
		System.out.println("destroy--------");
		soundPool.stop(currentStreamId);
		soundPool.release();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		System.out.println("pause-------");
		if (decoration != null) {//清除动画
			decoration.clearAnimation();
		}
		mSensorManager.unregisterListener(lsn);
		mSensorManager = null;
	}

	/**
	 * 设置焦点和测光区域
	 * 
	 * @param event
	 */
	public void focusOnTouch(MotionEvent event) {
		//		if (!taking_flag) {
		int[] location = new int[2];
		relativeLayout.getLocationOnScreen(location);

		Rect focusRect = calculateTapArea(view_focus.getWidth(),
				view_focus.getHeight(), 1f, event.getRawX(),
				event.getRawY(), location[0],
				location[0] + relativeLayout.getWidth(), location[1],
				location[1] + relativeLayout.getHeight());
		Rect meteringRect = calculateTapArea(view_focus.getWidth(),
				view_focus.getHeight(), 1.5f, event.getRawX(),
				event.getRawY(), location[0],
				location[0] + relativeLayout.getWidth(), location[1],
				location[1] + relativeLayout.getHeight());

		Camera.Parameters parameters = mycamera.getParameters();
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

		// System.out.println("CustomCameraView getMaxNumFocusAreas = " +
		// parameters.getMaxNumFocusAreas());
		if (parameters.getMaxNumFocusAreas() > 0) {
			List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
			focusAreas.add(new Camera.Area(focusRect, 1000));
			parameters.setFocusAreas(focusAreas);
		}

		// System.out.println("CustomCameraView getMaxNumMeteringAreas = " +
		// parameters.getMaxNumMeteringAreas());
		if (parameters.getMaxNumMeteringAreas() > 0) {
			List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
			meteringAreas.add(new Camera.Area(meteringRect, 1000));

			parameters.setMeteringAreas(meteringAreas);
		}

		try {
			mycamera.setParameters(parameters);
		} catch (Exception e) {

		}
		//			if (!taking_flag) {
		if(taking_flag){
			handler2.postDelayed(runnable4, 1000);
		}else{
			handler2.postDelayed(runnable4, 500);
		}

	}

	public Rect calculateTapArea(int focusWidth, int focusHeight,
			float areaMultiple, float x, float y, int previewleft,
			int previewRight, int previewTop, int previewBottom) {
		int areaWidth = (int) (focusWidth * areaMultiple);
		int areaHeight = (int) (focusHeight * areaMultiple);
		int centerX = (previewleft + previewRight) / 2;
		int centerY = (previewTop + previewBottom) / 2;
		double unitx = ((double) previewRight - (double) previewleft) / 2000;
		double unity = ((double) previewBottom - (double) previewTop) / 2000;
		int left = clamp((int) (((x - areaWidth / 2) - centerX) / unitx), -1000, 1000);
		int top = clamp((int) (((y - areaHeight / 2) - centerY) / unity), -1000, 1000);
		int right = clamp((int) (left + areaWidth / unitx), -1000, 1000);
		int bottom = clamp((int) (top + areaHeight / unity), -1000, 1000);

		return new Rect(left, top, right, bottom);
	}

	public int clamp(int x, int min, int max) {
		if (x > max)
			return max;
		if (x < min)
			return min;
		return x;
	}

	private void setFocusView() {
		handler2.postDelayed(runnable3, 1000);
	}

	/**
	 * 放大缩小。放大范围为0-myzoommax，假设允许的手势距离为0-500
	 * 
	 * @param arg 需要放大缩小的比例
	 */
	private void caculateBig(int arg) {
		// 大于放大尺寸的时候 不做操作、
		System.out.println(arg + "+++++++++++++" + lastzoom);
		progressInt = progressInt + arg - lastzoom;
		if (arg > lastzoom) {// 放大
			progressInt = progressInt > myzoommax ? myzoommax : progressInt;
		} else {// 缩小
			progressInt = progressInt < 0 ? 0 : progressInt;
		}
		System.out.println("zoom value:" + progressInt);
		setZoom(progressInt);
		lastzoom = arg;
	}

	// 放大或者缩小的具体方法
	private void setZoom(int i) {
		myParameters.setZoom(i);
		mycamera.setParameters(myParameters);
	}

	// 定时操作。
	Handler handler2 = new Handler();
	Runnable runnable2 = new Runnable() {
		@Override
		public void run() {
			// 要做的事情
			int width = ScreenUtil.getScreenWidth(CameraActivity.this); // 屏幕宽度（像素）
			int height = ScreenUtil.getScreenHeight(CameraActivity.this); // 屏幕高度（像素）
			view_focus.setX((width - view_focus.getWidth()) / 2);
			view_focus.setY(height / 2 - view_focus.getHeight());
			view_focus.setBackgroundResource(R.drawable.ic_focus_focusing);
			view_focus.setVisibility(View.VISIBLE);

			Camera.Parameters parameters = mycamera.getParameters();
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			try {
				mycamera.setParameters(parameters);
			} catch (Exception e) {
			}

			mycamera.autoFocus(myAutoFocusCallback);
		}
	};

	// 定时操作。
	Runnable runnable3 = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			// 要做的事情
			view_focus.setBackgroundDrawable(null);
			mycamera.cancelAutoFocus();// 取消一切对焦的动作
		}
	};

	//  延迟 一秒聚焦。
	Runnable runnable4 = new Runnable() {
		@Override
		public void run() {
			// 要做的事情
			if (!taking_flag) {
				mycamera.autoFocus(myAutoFocusCallback);

			}
		}
	};

	Runnable runnable5 = new Runnable() {
		@Override
		public void run() {
			// 要做的事情
			view_focus.setVisibility(View.GONE);
		}
	};

	// 用于判断手机型号 和 竖屏or横屏
	private void change() {
		// 其实这部分代码适合几乎所有的手机，显示图片问题用ImageLoader内容解决。
		if (screenType == 0) {
			myParameters.set("rotation", 0);
		} else if(screenType == 1){
			if (cameraPosition == 0) {
				myParameters.set("rotation", 270);// 旋转角度
			} else {
				myParameters.set("rotation", 90);// 旋转角度
			}

		}else if(screenType == 2){
			myParameters.set("rotation", 180);
		}else if(screenType == 3){
			if (cameraPosition == 0) {
				myParameters.set("rotation", 90);
			}else{
				myParameters.set("rotation", 270);
			}

		}
		mycamera.setParameters(myParameters);
	}
	
	//data转化为bitmap后显示方向不对问题。
	private void change1(int width,int height){
		
		if (screenType == 1) {
				if (cameraPosition == 0) {
					pictBitmap = rotate(pictBitmap, 270,width,height);// 旋转角度
				} else {
					pictBitmap = rotate(pictBitmap, 90,width,height);// 旋转角度
				}
		} else if (screenType == 0) {
			myParameters.set("rotation", 0);
		} else if (screenType == 2) {
			pictBitmap = rotate(pictBitmap, 180,width,height);
		} else if (screenType == 3) {
			if (cameraPosition == 0) {
				pictBitmap = rotate(pictBitmap, 90,width,height);
			} else {
				pictBitmap = rotate(pictBitmap, 270,width,height);
			}
		}
	}
	
	
	
	
    //设置动画。
	private void setAnim(final View v, int endLocationX, int endLocationY, final Bitmap animBitmap){
		PlaySound(1,0);
		
		anim_mask_layout = null;
		anim_mask_layout = createAnimLayout();
		int[] start_location = new int[2];// 一个整型数组，用来存储按钮的在屏幕的X、Y坐标
		start_location[0] = 0;//减去的值和图片大小有关系
		start_location[1] = 0;
		final View view = addViewToAnimLayout(anim_mask_layout, v,
				start_location);
		int[] end_location = new int[2];// 这是用来存储动画结束位置的X、Y坐标
		end_location[0] = endLocationX;
		end_location[1] = endLocationY;
		// 计算位移
		final int endX = end_location[0] - start_location[0]+45;// 动画位移的X坐标    加的数字和图片有关系  正常图片不需要,合成时候需要根据 数据判断 比例
		final int endY = end_location[1] - start_location[1]+20;// 动画位移的y坐标    加的数字和图片有关系  正常图片不需要
		//设置放大动画
		ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		
		scaleAnimation.setInterpolator(new LinearInterpolator());//匀速
		scaleAnimation.setRepeatCount(0);//不重复
		scaleAnimation.setFillAfter(true);//停在最后动画
		AnimationSet set = new AnimationSet(false);
		set.setFillAfter(false);
		set.addAnimation(scaleAnimation);
		set.setDuration(500);//动画整个时间
//		set.set
		view.startAnimation(set);//开始动画
		// 动画监听事件
				set.setAnimationListener(new AnimationListener() {
					// 动画的开始
					@Override
					public void onAnimationStart(Animation animation) {
						//					v.setVisibility(View.VISIBLE);
						
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub
					}

					// 动画的结束
					@Override
					public void onAnimationEnd(Animation animation) {
						//x轴的路径动画，匀速
						TranslateAnimation translateAnimationX = new TranslateAnimation(0,
								endX, 0, 0);
						translateAnimationX.setInterpolator(new LinearInterpolator());
						translateAnimationX.setRepeatCount(0);// 动画重复执行的次数
						//y轴的路径动画，加速
						TranslateAnimation translateAnimationY = new TranslateAnimation(0, 0,
								0, endY);
						translateAnimationY.setInterpolator(new AccelerateInterpolator());
						translateAnimationY.setRepeatCount(0);// 动画重复执行的次数
//						ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f);
						AnimationSet set2 = new AnimationSet(false);
//						set2.addAnimation(scaleAnimation);
						set2.addAnimation(translateAnimationY);
						set2.addAnimation(translateAnimationX);

						set2.setFillAfter(true);
						set2.setStartOffset(200);//等待时间
						set2.setDuration(800);// 动画的执行时间
						view.startAnimation(set2);
						set2.setAnimationListener(new AnimationListener() {

							@Override
							public void onAnimationStart(Animation animation) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onAnimationRepeat(Animation animation) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onAnimationEnd(Animation animation) {
								// TODO Auto-generated method stub
								
								
							   //延迟两秒  camera 再次预览
						       new Handler().postDelayed(new Runnable() {
							    public void run() {
								  // execute the task
							      soundPool.stop(currentStreamId);
							      v.clearAnimation();
							      isMagicShot = false; //延迟两秒之后  取消掉Magic shoot 模式
							      magicButton.setImageResource(R.drawable.magic_btn);
							      
								   if (mycamera != null) {
									mycamera.startPreview();
								   }
								  taking_flag = false;
								  animBitmap.recycle();
								  v.setVisibility(View.GONE);
							    }
						       }, 2000);
						       
						       
						       
							}
						});
						}
				});
	}

	//创建动画图层
		private ViewGroup createAnimLayout() {
			ViewGroup rootView = (ViewGroup) this.getWindow().getDecorView();
			LinearLayout animLayout = new LinearLayout(this);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.MATCH_PARENT);
			animLayout.setLayoutParams(lp);
			animLayout.setBackgroundResource(android.R.color.transparent);
			rootView.addView(animLayout);
			return animLayout;
		}
		//添加试图到动画图层
		private View addViewToAnimLayout( ViewGroup vg, final View view,
				int[] location) {
			int x = location[0];
			int y = location[1];
			vg.addView(view);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			lp.leftMargin = x;
			lp.topMargin = y;
			view.setLayoutParams(lp);
			return view;
		}
		
		private  Bitmap rotate(Bitmap in, int angle,int width,int height) {
			 Matrix mat = new Matrix();
			if(width>height){
				mat.postRotate(angle);
			}
			if(cameraPosition == 0){
				mat.postScale(-1, 1);
			}
		    
		    return Bitmap.createBitmap(in, 0, 0, in.getWidth(), in.getHeight(), mat, true);
		}
		
		
				
		  protected void PlaySound(int sound, int loop) {
				// TODO Auto-generated method stub
				AudioManager audioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
				float streamVolumeCurrent = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
				float streamVolumeMax=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
				float volume=streamVolumeCurrent/streamVolumeMax;
				currentStreamId=soundPool.play(hashMap.get(sound), volume, volume, 1, loop, 1.0f);
				
			}

			private void initSoundPool() {
				// TODO Auto-generated method stub
				soundPool=new SoundPool(4,AudioManager.STREAM_MUSIC,0);
				hashMap=new HashMap<Integer,Integer>();
				hashMap.put(1, soundPool.load(this,R.raw.bling,1));
			}

		 		
}