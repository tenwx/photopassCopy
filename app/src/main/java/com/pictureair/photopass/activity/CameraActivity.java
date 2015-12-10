package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.EditActivityAdapter;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.CameraUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.MyToast;
import com.pictureair.photopass.widget.RotateView;
import com.pictureair.photopass.widget.RotatetextView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 相机界面
 * 
 * @author talon
 */
public class CameraActivity extends BaseActivity implements OnClickListener,
		SurfaceHolder.Callback, OnTouchListener {
	private static final String TAG = "CameraActivity";
	private SurfaceView mySurfaceView = null;
	private SurfaceHolder mySurfaceHolder = null;
	private LinearLayout titlebar; // 顶部栏。
	private ImageButton recordButton; // 录像按钮
	private RotateView viewButton, flashButton, switchButton, timeButton,
			returnButton, magicButton;// 预览图片，闪光灯，前后置切换，倒计时，返回，magic。
	private RotatetextView timeTextTitle, timeTextCenter; // 顶部数字标志，中间倒计时标志。
	View view_focus; // 聚焦显示的图片
	private RelativeLayout relativeLayout, cameraTab; // 预览界面的Relative ,
														// Camera界面底部tab
	private int screenWidth, screenHeight, myzoommax, camera_flash = 1,
			cameraPosition = 1, lastzoom, progressInt; // 屏幕宽度，屏幕高度,相机可缩放值，相机闪光灯的标志／1代表off／2代表on／3代表auto,
														// 0代表前置摄像头/1代表后置摄像头,放大缩小焦距,放大的距离
	private float x1 = 0, y1 = 0, z1 = 0;
	// 加载图片的框架
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	private MyToast newToast;
	// 存放最后一张的拍摄照片路径
	private SharedPreferences sharedPreferences;
	private SharedPreferences.Editor editor;
	// 目标文件夹。
	private SimpleDateFormat dateFormat;
	private File nameFile;
	private ArrayList<String> framelist;// 相机预览饰品List 。 放在assets 里面
	private boolean isview = false;// 相机是否在预览中
	private boolean isfocus = false;// 是否聚焦中
	private boolean openSuccess = false;// 打开相机是否成功
	private Camera.Parameters myParameters; // 相机的属性
	private Camera.AutoFocusCallback myAutoFocusCallback; // 自动聚焦
	private Camera mycamera = null; // camera 对象
	// 焦距
	private static final int ZOOM_STOPPED = 0;
	private static final int ZOOM_START = 1;
	private int zoom = 0, time, mTargetZoomValue, mZoomState = ZOOM_STOPPED;
	private final ZoomListener mZoomListener = new ZoomListener();
	// 是否
	private boolean isMask = false, taking_flag = false; // ???, 是否在拍摄中
	private long timeLong;// 纪录时间，两秒之内，触摸对焦之后不允许
	private String lastPhotoUrl = ""; // 纪录最后一张拍摄照片的地址。用于显示右下角按钮
	private int time_flag = CameraUtil.DELAY_TIME_0S;// 定时拍照 变量
	// 饰品显示 与 隐藏 动画实现。
	TranslateAnimation mHiddenAction;
	TranslateAnimation mShowAction;
	private EditActivityAdapter eidtAdapter; // 相机 饰品的适配器。与编辑图片的是 同一个。
	// 传感器
	private SensorManager mSensorManager = null;
	private Sensor mSensor = null;
	private int ro; // 纪录手机旋转的角度。
	File photoFile; // 保存文件
	private boolean isSupportContinuous = false; // 判断手机是否支持 Continus聚焦模式
	// magic shoot 的组件。
	// 判断 是否是 magicShot 模式
	private boolean isMagicShot = false;
	private ImageView magicShow; // 显示 虚线引导图
	private ImageView decoration;
	private int showX = 0;
	private int showY = 0;
	private boolean isCurActivity = true;// 用于解决 点击相册 再返回camera，还有动画的bug。
											// 判断是否是当前CameraActivity。
	private Bitmap pictBitmap; // 拍照后的二进制转换成Bitmap
	private ViewGroup anim_mask_layout;// 动画层

	// 播放音效。
	SoundPool soundPool;
	HashMap<Integer, Integer> hashMap;
	int currentStreamId;
	private Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ScreenUtil.setFullScreen(this);
		setContentView(R.layout.activity_camera);
		isCurActivity = true;
		initView();
		initData();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		isCurActivity = true;
		// 更新 最新照片信息。
		lastPhotoUrl = sharedPreferences.getString(Common.LAST_PHOTO_URL, "");
		File thumbnialFile = new File(lastPhotoUrl);
		if (lastPhotoUrl.equals("") || !thumbnialFile.exists()) {// 变量中没有这个字段，查找文件
			lastPhotoUrl = AppUtil.findLatestPic();
			// 将找到的路径存放在sharedprefrence中
			editor.putString(Common.LAST_PHOTO_URL, lastPhotoUrl);
			editor.commit();
		}
		UpdateLastPhoto(lastPhotoUrl);

		if (null == mSensorManager) {
			mSensorManager = (SensorManager) this
					.getSystemService(SENSOR_SERVICE);
			mSensor = mSensorManager
					.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			mSensorManager.registerListener(lsn, mSensor,
					SensorManager.SENSOR_DELAY_UI);
		}

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mSensorManager.unregisterListener(lsn);
		mSensorManager = null;
	}

	@Override
	protected void onDestroy() {// 因为推出的时候，还是会执行surfaceviewdestroy，所以不需要在这里释放
		// TODO Auto-generated method stub
		super.onDestroy();
		AppManager.getInstance().killActivity(this);
		soundPool.stop(currentStreamId);
		soundPool.release();
	}

	/**
	 * 初始化 界面
	 */
	private void initView() {
		titlebar = (LinearLayout) findViewById(R.id.titlebar);
		recordButton = (ImageButton) findViewById(R.id.imagebutton_record);
		flashButton = (RotateView) findViewById(R.id.imageButton_flash);
		switchButton = (RotateView) findViewById(R.id.imageButton_switch);
		returnButton = (RotateView) findViewById(R.id.camera_return);
		timeButton = (RotateView) findViewById(R.id.imageButton_time);
		timeTextTitle = (RotatetextView) findViewById(R.id.textView_time_title);
		timeTextCenter = (RotatetextView) findViewById(R.id.textView_time_center);

		view_focus = findViewById(R.id.view_focus);
		relativeLayout = (RelativeLayout) findViewById(R.id.previewview);

		mySurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
		mySurfaceHolder = mySurfaceView.getHolder();
		mySurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
		mySurfaceView.setOnTouchListener(this);
		mySurfaceHolder.addCallback(this);
		mySurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		viewButton = (RotateView) findViewById(R.id.button2);

		viewButton.setOnClickListener(this);
		magicButton = (RotateView) findViewById(R.id.imagebutton_magic);
		cameraTab = (RelativeLayout) findViewById(R.id.camera_tab);

		magicShow = (ImageView) findViewById(R.id.magic_show);

		magicButton.setOnClickListener(this);
		recordButton.setOnClickListener(this);
		returnButton.setOnClickListener(this);
		flashButton.setOnClickListener(this);
		switchButton.setOnClickListener(this);
		timeButton.setOnClickListener(this);

	}

	/**
	 * 初始化 数据
	 */
	private void initData() {
		screenWidth = ScreenUtil.getScreenWidth(this);
		screenHeight = ScreenUtil.getScreenHeight(this);
		timeTextTitle.setText("0s");
		imageLoader = ImageLoader.getInstance();
		// options = new DisplayImageOptions.Builder().displayer(new
		// RoundedBitmapDisplayer(10)).considerExifParams(true).build();//设置圆角图片
		options = new DisplayImageOptions.Builder().considerExifParams(true)
				.build();// 设置圆角图片
		newToast = new MyToast(CameraActivity.this);
		// 设置 tab 的高度。
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
				screenWidth, Common.TAB_HEIGHT);
		rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		cameraTab.setLayoutParams(rlp);
		// 设置底部栏目 和 首页tab一样 高。
		LayoutParams layoutParams = recordButton.getLayoutParams();
		layoutParams.width = ScreenUtil.dip2px(this, 40);
		layoutParams.height = layoutParams.width;
		recordButton.setLayoutParams(layoutParams);

		sharedPreferences = getSharedPreferences("pictureAir", MODE_PRIVATE);
		editor = sharedPreferences.edit();

		dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
		nameFile = new File(Common.PHOTO_SAVE_PATH);
		nameFile.mkdirs();// 创建根目录文件夹

		initSoundPool();

		// 自动对焦的回调函数 聚焦
		myAutoFocusCallback = new Camera.AutoFocusCallback() {
			public void onAutoFocus(boolean success, Camera camera) {// 调用一次自动对焦
				// TODO Auto-generated method stub
				if (success) {
					if (cameraPosition == 1) {
						if (isSupportContinuous) {
							myParameters
									.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
							camera.setParameters(myParameters);
						}
					}
					// camera.startPreview();
					view_focus
							.setBackgroundResource(R.drawable.ic_focus_focused);
				} else {
					view_focus
							.setBackgroundResource(R.drawable.ic_focus_failed);
				}
				setFocusView();
				x1 = 0;
				y1 = 0;
				z1 = 0;
				isfocus = false;
				if (camera_flash == 2) {
					myParameters.setFlashMode(Parameters.FLASH_MODE_ON);
					mycamera.setParameters(myParameters);
				}
			}
		};
		// 添加饰品。
		framelist = new ArrayList<String>();
		framelist.add(Scheme.ASSETS.wrap("magic/magic1.png"));

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.imageButton_switch:
			switchCameraPosition();
			break;
		case R.id.imageButton_flash:// 闪光灯 1代表off，2代表on，3代表auto
			switchCameraFlash();
			break;
		case R.id.imageButton_time:// 倒计时按钮
			if (time_flag == CameraUtil.DELAY_TIME_0S) {
				time_flag = CameraUtil.DELAY_TIME_3S;
				timeTextTitle.setText("3s");
			} else if (time_flag == CameraUtil.DELAY_TIME_3S) {
				time_flag = CameraUtil.DELAY_TIME_10S;
				timeTextTitle.setText("10s");
			} else {
				time_flag = CameraUtil.DELAY_TIME_0S;
				timeTextTitle.setText("0s");
			}
			break;
		case R.id.camera_return:// 返回按钮
			finish();
			break;
		case R.id.button2:// 图片预览按钮
			isCurActivity = false;
			goViewPhotoActivity();
			break;
		case R.id.imagebutton_magic:
			// 操作 magicShort
			if (isMagicShot) {
				magicShow.setVisibility(View.GONE);
				magicButton.setImageResource(R.drawable.magic_btn);
				isMagicShot = false;
			} else {
				magicShow.setVisibility(View.VISIBLE);
				magicButton.setImageResource(R.drawable.magic_btn_sele);
				isMagicShot = true;
			}

			break;
		case R.id.imagebutton_record:
			if (!taking_flag) {
				if (time_flag == CameraUtil.DELAY_TIME_3S) {// 如果延迟开关打开，则开始计时，计时结束之后再拍照
					// System.out.println("delay 3s");
					time = 3;
					handler.postDelayed(runnable, 0);
				} else if (time_flag == CameraUtil.DELAY_TIME_10S) {
					handler.postDelayed(runnable_10, 0);
					time = 10;
					// System.out.println("delay 10s");
				} else {// 否则，直接拍照
					setCameraRotation();// 设置Camera 角度属性。
					handler.removeCallbacks(runnable4);
					handler.removeCallbacks(runnable2);
					mycamera.takePicture(null, null, myjpegCallback);
					handler.postDelayed(runnable5, 500);
				}
				taking_flag = true;
			} else {
				// System.out.println("fail");
				// newToast.setTextAndShow(R.string.please_wait,
				// Common.TOAST_SHORT_TIME);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		// 判断。
		if (cameraPosition == 1) {
			openSuccess = initcamera(90, 0);
		} else {
			openSuccess = initcamera(270, 1);
		}
		if (openSuccess) {
			initializeZoom();
			mySurfaceView.setLayoutParams(new RelativeLayout.LayoutParams(
					screenWidth, (int) (screenWidth * 4 / 3)));// 设置surfaceview的高度，实现4：3
		} else {
			finish();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		handler.removeCallbacks(runnable2);
		handler.removeCallbacks(runnable3);
		handler.removeCallbacks(runnable4);
		if (mycamera != null) {
			mycamera.stopPreview();
			mycamera.release();
			mycamera = null;
			isview = false;
		}
	}

	// 回调函数，合成图片保存图片都在这里处理
	PictureCallback myjpegCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] picData, Camera camera) {
			handler.removeCallbacks(runnable4);
			if (null != picData) {
				photoFile = new File(nameFile + "/"
						+ dateFormat.format(new Date()) + ".JPG");
				mycamera.stopPreview();

				// magic模式拍照。有饰品的时候，合成饰品，没有饰品，就正常拍摄。
				if (isMagicShot) {
					// 消失掉 magicShow 的模版，动画继续
					magicShow.setVisibility(View.GONE);

					decoration = new ImageView(CameraActivity.this);// buyImg是动画的图片
					Bitmap bitmap = BitmapFactory.decodeResource(
							getResources(), R.drawable.decorationtest);
					decoration.setImageResource(R.drawable.decorationtest);

					showX = screenWidth * 90 / 584 - bitmap.getWidth() / 2; // 减去的50和饰品的高度有关
					showY = (magicShow.getHeight() * 4 / 7)
							+ (relativeLayout.getHeight() - magicShow
									.getHeight()) / 2 + titlebar.getHeight()
							- bitmap.getHeight();
					if (isCurActivity) {
						setAnim(decoration, showX, showY, bitmap);
					}

					// 原始的 饰品
					// Bitmap decorationBitmap = BitmapFactory.decodeResource(
					// getResources(), R.drawable.decoration13);
					int decorationWidth = bitmap.getWidth();
					int decorationHeight = bitmap.getHeight();

					pictBitmap = BitmapFactory.decodeByteArray(picData, 0,
							picData.length);// data是字节数据，将其解析成位图

					// change1(pictBitmap.getWidth(),pictBitmap.getHeight());
					// 获取的是新的 pictBitmap 的高度
					int picBitmapWidth = pictBitmap.getWidth();
					int picBitmapHeight = pictBitmap.getHeight();
					Matrix matrix = new Matrix();
					// 计算出 饰品的缩放比例。
					matrix.postScale((float) ((float) picBitmapWidth
							* decorationWidth / screenWidth)
							/ decorationWidth, (float) ((float) picBitmapHeight
							* decorationHeight / ((float) screenWidth * 4 / 3))
							/ decorationHeight);

					Bitmap newDecorationBitmap = Bitmap.createBitmap(bitmap, 0,
							0, decorationWidth, decorationHeight, matrix, true);

					// 接下来图片合成

					Bitmap newbmp = Bitmap.createBitmap(picBitmapWidth,
							picBitmapHeight, Config.ARGB_8888);

					int drawX = picBitmapWidth * showX / screenWidth
							+ picBitmapWidth * 45 / relativeLayout.getWidth(); // 加上的数字和
																				// 图片有关，45与
																				// 20
					int drawY = picBitmapHeight
							* (showY - titlebar.getHeight())
							/ (screenWidth * 4 / 3) + picBitmapHeight * 20
							/ relativeLayout.getHeight();
					Canvas cv = new Canvas(newbmp);
					cv.drawBitmap(pictBitmap, 0, 0, null);
					pictBitmap.recycle();
					cv.drawBitmap(newDecorationBitmap, drawX, drawY, null);
					newDecorationBitmap.recycle();
					cv.save(Canvas.ALL_SAVE_FLAG);// 保存
					cv.restore();// 存储
					// 保存图片到sdcard
					if (null != newbmp) {
						System.out.println(photoFile.toString());
						FileOutputStream fileOutputStream2 = null;
						try {
							fileOutputStream2 = new FileOutputStream(photoFile);
							BufferedOutputStream bos = new BufferedOutputStream(
									fileOutputStream2);
							newbmp.compress(Bitmap.CompressFormat.JPEG, 100,
									bos);
							bos.flush();
							bos.close();
							fileOutputStream2.close();
						} catch (IOException e) {
							// TODO: handle exception
						}
					}
					newbmp.recycle();

				} else {
					// 正常模式拍照
					CameraUtil.outputPhotoForStream(photoFile, picData);
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
				((MyApplication) getApplication()).scanMagicFinish = false;
				CameraUtil.scan(photoFile.toString(), CameraActivity.this);

			} else {
				// 数据为空。拍照失败。
				newToast.setTextAndShow(R.string.take_photo_failed,
						Common.TOAST_SHORT_TIME);
			}
		}
	};

	// ****************************************************************************************************************
	// ***********************此处之后，都是实现方法，可不看。 流程在上面
	// ****************************************************
	// ****************************************************************************************************************

	private void initializeZoom() {
		if (!myParameters.isZoomSupported())
			return;
		mycamera.setZoomChangeListener(mZoomListener);
	}

	/*
	 * 缩放类 的实现
	 */
	private final class ZoomListener implements
			android.hardware.Camera.OnZoomChangeListener {
		public void onZoomChange(int value, boolean stopped,
				android.hardware.Camera camera) {
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

	/**
	 * 初始化camera
	 */
	public boolean initcamera(int rotation, int cameraId) {
		CameraInfo info = new CameraInfo();
		Camera.getCameraInfo(cameraId, info);// 得到每一个摄像头的信息

		int result = 0;
		if (mycamera == null && !isview) {
			try {
				mycamera = Camera.open(cameraId);// 打开相机
			} catch (Exception e) {
				// TODO: handle exception
				// System.out.println("无法打开相机");
				newToast.setTextAndShow(R.string.camera_closed,
						Common.TOAST_SHORT_TIME);
				PictureAirLog.v(TAG,"Exception e : " + e.toString());
				return false;
			}
		}
		if (mycamera != null && !isview) {
			try {
				myParameters = mycamera.getParameters();
				// 获取可支持的最大放大尺寸
				myzoommax = myParameters.getMaxZoom();
				// System.out.println("zoommax" + myzoommax);
				myParameters.setPictureFormat(PixelFormat.JPEG);
				myParameters.set("jpeg-quality", 90);// 设置图片质量，我支持的手机是从0-100，可能就是这个范围，没有仔细研究过
				// 获取手机可支持的preview尺寸
				List<Size> previewsizes = myParameters
						.getSupportedPreviewSizes();// 获取系统可支持的预览尺寸
				List<Size> photoSizes = myParameters.getSupportedPictureSizes();// 获取系统可支持的图片尺寸
				int previewsize_width = 1;
				int previewsize_height = 1;
				int picturesize_width = 1;
				int picturesize_height = 1;
				for (Size size : previewsizes) {// 设置为4：3的预览尺寸,有些手机遍历出来的结果从大到小，有的手机从小到大，所以只要一个最大的尺寸
					if (size.width / 4 == size.height / 3) {
						if (size.width > previewsize_width) {
							previewsize_width = size.width;
							previewsize_height = size.height;
						}
					}
				}
				for (Size size : photoSizes) {// 设置为4：3图片尺寸，原理同上
					if (size.width / 4 == size.height / 3) {
						if (size.width > picturesize_width) {
							picturesize_width = size.width;
							picturesize_height = size.height;
						}
					}
				}

				myParameters.setPreviewSize(previewsize_width,
						previewsize_height);
				myParameters.setPictureSize(picturesize_width,
						picturesize_height);

				myParameters.set("rotation", rotation);// 旋转角度

				if (cameraId == 0) {

					List<String> focuseMode = (myParameters
							.getSupportedFocusModes());
					for (int i = 0; i < focuseMode.size(); i++) {
						// PictureAirLog.e("  === ", focuseMode.get(i));
						if (focuseMode.get(i).equals("continuous-picture")) {
							myParameters
									.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
							PictureAirLog.v("FOCUS_MODE_CONTINUOUS_PICTURE", "FOCUS_MODE_CONTINUOUS_PICTURE");
							isSupportContinuous = true;
							break;
						} else {
							myParameters
									.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
							PictureAirLog.v("FOCUS_MODE_AUTO", "FOCUS_MODE_AUTO");
							isSupportContinuous = false;
						}
					}
				}
				if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					result = (info.orientation) % 360;
					result = (360 - result) % 360;
				} else {
					result = (info.orientation + 360) % 360;
				}
				// System.out.println("-------------->result------------->"+result);
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
		return true;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		// 手势识别 。放大与缩小
		int nCnt = event.getPointerCount();
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// Log.e("ACTION_DOWN:",
			// "================================");
			isMask = false;
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			// mode = MODE.FOCUSING;
			if (!isMask) {
				int width = view_focus.getWidth();
				int height = view_focus.getHeight();
				view_focus.setVisibility(View.VISIBLE);
				view_focus.setBackgroundResource(R.drawable.ic_focus_focusing);
				view_focus.setX(event.getX() - (width / 2));
				view_focus.setY(event.getY() - (height / 2));
				isfocus = true;
				// 取消
				handler.removeCallbacks(runnable2);
				// 纪录时间，
				timeLong = System.currentTimeMillis();
				if (mycamera != null) {
					CameraUtil.focusOnTouch(event, relativeLayout, view_focus,
							mycamera);
				}
				if (taking_flag) {
					handler.postDelayed(runnable4, 1000);
				} else {
					handler.postDelayed(runnable4, 500);
				}
			}
		} else if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN
				&& 2 == nCnt) {
			isMask = true;
			int xlen = Math.abs((int) event.getX(0) - (int) event.getX(1));
			int ylen = Math.abs((int) event.getY(0) - (int) event.getY(1));

			double nLenStart = Math.sqrt((double) xlen * xlen + (double) ylen
					* ylen);
			lastzoom = (int) Math.floor(Math.abs(nLenStart * myzoommax / 500));
		} else if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE
				&& 2 == nCnt) {
			isMask = true;
			int xlen = Math.abs((int) event.getX(0) - (int) event.getX(1));
			int ylen = Math.abs((int) event.getY(0) - (int) event.getY(1));

			double nLenEnd = Math.sqrt((double) xlen * xlen + (double) ylen
					* ylen);
			caculateBig((int) Math.floor(Math.abs(nLenEnd * myzoommax / 500)));
		}
		return true;
	}

	/**
	 * 放大缩小。放大范围为0-myzoommax，假设允许的手势距离为0-500
	 * 
	 * @param arg
	 *            需要放大缩小的比例
	 */
	private void caculateBig(int arg) {
		// 大于放大尺寸的时候 不做操作、
		progressInt = progressInt + arg - lastzoom;
		if (arg > lastzoom) {// 放大
			progressInt = progressInt > myzoommax ? myzoommax : progressInt;
		} else {// 缩小
			progressInt = progressInt < 0 ? 0 : progressInt;
		}
		// setZoom(progressInt);
		myParameters.setZoom(progressInt);
		mycamera.setParameters(myParameters);
		lastzoom = arg;
	}

	Runnable runnable5 = new Runnable() {
		@Override
		public void run() {
			// 要做的事情
			view_focus.setVisibility(View.GONE);
		}
	};
	Runnable runnable2 = new Runnable() {
		@Override
		public void run() {
			// 要做的事情
			view_focus.setX((screenWidth - view_focus.getWidth()) / 2);
			view_focus.setY(screenHeight / 2 - view_focus.getHeight());
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
	// 延迟 一秒聚焦。
	Runnable runnable4 = new Runnable() {
		@Override
		public void run() {
			// 要做的事情
			if (!taking_flag) {
				mycamera.autoFocus(myAutoFocusCallback);

			}
		}
	};

	/**
	 * / * 更新预览图
	 * 
	 * @param URL
	 *            预览图的最新的URL
	 */
	private void UpdateLastPhoto(String URL) {
		// System.out.println("view image url is "+ URL);
		if (URL.equals("")) {
			viewButton.setImageResource(R.drawable.camera_view_empty);
		} else {
			imageLoader.displayImage("file://" + URL, viewButton, options);
		}
	}

	/*
	 * 切换前后摄像头。
	 */
	private void switchCameraPosition() {
		int cameraCount = 0;
		CameraInfo cameraInfo = new CameraInfo();
		cameraCount = Camera.getNumberOfCameras();// 得到摄像头的个数
		for (int i = 0; i < cameraCount; i++) {
			Camera.getCameraInfo(i, cameraInfo);// 得到每一个摄像头的信息
			if (cameraPosition == 1) {
				// 现在是后置，变更为前置
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {// 代表摄像头的方位，CAMERA_FACING_FRONT前置
					// CAMERA_FACING_BACK后置
					mycamera.stopPreview();// 停掉原来摄像头的预览
					mycamera.release();// 释放资源
					mycamera = null;// 取消原来摄像头
					// System.out.println("--------------");
					mycamera = Camera.open(i);// 打开当前选中的摄像头

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
					isview = false;
					initcamera(90, i);
					cameraPosition = 1;
					// seekBar.setProgress(0);//将变焦杆置0
					break;
				}
			}
		}

	}

	/*
	 * 切换闪光灯的开关
	 */
	private void switchCameraFlash() {
		if (camera_flash == 1) {
			flashButton.setImageResource(R.drawable.camera_flash_on);
			flashButton.setScaleType(ScaleType.CENTER_INSIDE);
			myParameters.setFlashMode(Parameters.FLASH_MODE_ON);
			mycamera.setParameters(myParameters);
			// mycamera.startPreview();
			camera_flash = 2;
			zoom += 1;

		} else if (camera_flash == 2) {
			flashButton.setImageResource(R.drawable.camera_flash_auto);
			flashButton.setScaleType(ScaleType.CENTER_INSIDE);
			// mycamera.stopPreview();
			myParameters.setFlashMode(Parameters.FLASH_MODE_AUTO);
			mycamera.setParameters(myParameters);
			// mycamera.startPreview();
			camera_flash = 3;
			zoom += 1;

		} else if (camera_flash == 3) {
			flashButton.setImageResource(R.drawable.camera_flash_off);
			flashButton.setScaleType(ScaleType.CENTER_INSIDE);
			myParameters.setFlashMode(Parameters.FLASH_MODE_OFF);
			mycamera.setParameters(myParameters);
			camera_flash = 1;
			zoom += 1;
		}

	}

	private void goViewPhotoActivity() {
		if (taking_flag) {
			newToast.setTextAndShow(R.string.connecting,
					Common.TOAST_SHORT_TIME);
		} else {
			// 取消当前的倒计时操作
			view_focus.setVisibility(View.GONE);
			timeTextTitle.setText("0s");
			time = 0;
			time_flag = CameraUtil.DELAY_TIME_0S;
			taking_flag = false;
			// timetextView.setVisibility(View.GONE);
			handler.removeCallbacks(runnable);
			handler.removeCallbacks(runnable_10);

			Intent intent = new Intent(this, ViewPhotoActivity.class);
			startActivity(intent);
		}
	}

	Runnable runnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (time == 0) {// 如果为0，则开始拍照，并且将时间重置为3s
				if (isview && mycamera != null) {
					// taking_flag = true;
					setCameraRotation();// 设置Camera 角度属性。
					handler.removeCallbacks(runnable4);
					handler.removeCallbacks(runnable2);
					mycamera.takePicture(null, null, myjpegCallback);
					handler.postDelayed(runnable5, 500);
					// view_focus.setVisibility(View.GONE);
					time = 3;
				}
				timeTextCenter.setVisibility(View.GONE);
			} else {// 如果不为0.实现倒计时
				timeTextCenter.setVisibility(View.VISIBLE);
				timeTextCenter.setText("" + time);
				// System.out.println(time);
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
					setCameraRotation();// 设置Camera 角度属性。
					handler.removeCallbacks(runnable4);
					handler.removeCallbacks(runnable2);
					mycamera.takePicture(null, null, myjpegCallback);
					handler.postDelayed(runnable5, 500);
					// view_focus.setVisibility(View.GONE);
					time = 10;
					taking_flag = true;
				}
				timeTextCenter.setVisibility(View.GONE);
			} else {// 如果不为0.实现倒计时
				timeTextCenter.setVisibility(View.VISIBLE);
				timeTextCenter.setText("" + time);
				time--;
				handler.postDelayed(this, 1000);
			}
		}
	};

	// 定时操作。 让聚焦图片消失。并且取消聚焦。
	Runnable runnable3 = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			// 要做的事情
			view_focus.setBackgroundDrawable(null);
			mycamera.cancelAutoFocus();// 取消一切对焦的动作
		}
	};

	private void setFocusView() {
		handler.postDelayed(runnable3, 1000);
	}

	/*
	 * 重力感应传感器，用于旋转图标 和 生成正确角度的照片。
	 */
	// 重力感应 的监听，负责图标的动态旋转
	SensorEventListener lsn = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			float axis_x, axis_y, axis_z;
			axis_x = event.values[SensorManager.DATA_X];
			axis_y = event.values[SensorManager.DATA_Y];
			axis_z = event.values[SensorManager.DATA_Z];

			// PictureAirLog.e("====", "width:"+new
			// MatrixImageView(getApplicationContext()).mImageWidth);
			if (x1 == 0 && y1 == 0 && z1 == 0) {
				x1 = axis_x;
				y1 = axis_y;
				z1 = axis_z;

			}
			// 曾经 根据重力感应对焦。
			// if (Math.sqrt((axis_x - x1) * (axis_x - x1) + (axis_y - y1)
			// * (axis_y - y1) + (axis_z - z1) * (axis_z - z1)) > 1) {
			// if (cameraPosition == 1) {
			// if (!isfocus) {
			// if(System.currentTimeMillis() - timeLong > 4000){
			// System.out.println("need do focus");
			// if(camera_flash == 2){
			// myParameters.setFlashMode(Parameters.FLASH_MODE_OFF);
			// mycamera.setParameters(myParameters);
			// }
			// if(!taking_flag){
			// handler2.postDelayed(runnable2, 2000);
			// }
			//
			// isfocus = true;
			// }
			// }
			// }
			//
			// }
			if (axis_y > Math.abs(axis_x)) {// 手机竖直的时候,先判断方向，再判断是不是处于临界点
				if (axis_y > Math.abs(axis_x) + 1) {// 通过+1，区分临界点
					// screenType = 1;
					dorotate(0);
					ro = 90;
				}
			} else if (axis_x > Math.abs(axis_y)) {// 向左旋转了90的状态
				if (axis_x > Math.abs(axis_y) + 1) {
					// screenType = 0;
					dorotate(270);
					ro = 0;

				}
			} else if ((axis_y < axis_x && axis_x < 0)
					|| (Math.abs(axis_y) > axis_x && axis_x > 0)) {// 手机倒立的状态
				// screenType = 3;
				if ((axis_y < axis_x - 1 && axis_x < 0)
						|| (Math.abs(axis_y) > axis_x + 1 && axis_x > 0)) {
					dorotate(180);
					ro = 270;
				}
			} else if ((axis_x < axis_y && axis_y < 0)
					|| (Math.abs(axis_x) > axis_y && axis_y > 0)) {// 向右旋转了90的状态
				// screenType = 2;
				if ((axis_x < axis_y - 1 && axis_y < 0)
						|| (Math.abs(axis_x) > axis_y + 1 && axis_y > 0)) {
					dorotate(90);
					ro = 180;
				}
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}
	};

	/**
	 * 控件的旋转
	 * 
	 * @param degree
	 *            需要旋转的角度
	 */
	private void dorotate(int degree) {
		returnButton.setOrientation(degree, true);
		flashButton.setOrientation(degree, true);
		switchButton.setOrientation(degree, true);
		timeButton.setOrientation(degree, true);
		magicButton.setOrientation(degree, true);
		viewButton.setOrientation(degree, true);
		timeTextCenter.setOrientation(degree, true);
		timeTextTitle.setOrientation(degree, true);
	}

	/**
	 * 设置Camara角度，极其重要。 在拍照前调用。
	 */
	private void setCameraRotation() {
		if (isMagicShot) {
			// 如果 照片电影模式存在。就不设置相机的属性。 非常重要。
			myParameters.set("jpeg-quality", 50);
			mycamera.setParameters(myParameters);
		} else {
			if (cameraPosition == 1) {
				ro = ro % 360;
			} else {
				ro = (360 - ro) % 360;
			}
			myParameters.set("rotation", ro);
			mycamera.setParameters(myParameters);
		}
	}

	/*
	 * 根据角度旋转Bitmap，flag 为 是否打开镜像开关。
	 */
	private Bitmap rotate(Bitmap bitmap, int oritation, boolean flag) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Matrix mtx = new Matrix();
		if (flag) {
			mtx.postScale(-1, 1);
		}
		mtx.postRotate(oritation);
		return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
	}

	// 设置动画。
	private void setAnim(final View v, int endLocationX, int endLocationY,
			final Bitmap animBitmap) {
		PlaySound(1, 0);
		anim_mask_layout = null;
		anim_mask_layout = createAnimLayout();
		int[] start_location = new int[2];// 一个整型数组，用来存储按钮的在屏幕的X、Y坐标
		start_location[0] = 0;// 减去的值和图片大小有关系
		start_location[1] = 0;
		final View view = addViewToAnimLayout(anim_mask_layout, v,
				start_location);
		int[] end_location = new int[2];// 这是用来存储动画结束位置的X、Y坐标
		end_location[0] = endLocationX;
		end_location[1] = endLocationY;
		// 计算位移
		final int endX = end_location[0] - start_location[0] + 45;// 动画位移的X坐标
																	// 加的数字和图片有关系
																	// 正常图片不需要,合成时候需要根据
																	// 数据判断 比例
		final int endY = end_location[1] - start_location[1] + 20;// 动画位移的y坐标
																	// 加的数字和图片有关系
																	// 正常图片不需要
		// 设置放大动画
		ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f,
				1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);

		scaleAnimation.setInterpolator(new LinearInterpolator());// 匀速
		scaleAnimation.setRepeatCount(0);// 不重复
		scaleAnimation.setFillAfter(true);// 停在最后动画
		AnimationSet set = new AnimationSet(false);
		set.setFillAfter(false);
		set.addAnimation(scaleAnimation);
		set.setDuration(500);// 动画整个时间
		// set.set
		view.startAnimation(set);// 开始动画
		// 动画监听事件
		set.setAnimationListener(new AnimationListener() {
			// 动画的开始
			@Override
			public void onAnimationStart(Animation animation) {
				// v.setVisibility(View.VISIBLE);

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
			}

			// 动画的结束
			@Override
			public void onAnimationEnd(Animation animation) {
				// x轴的路径动画，匀速
				TranslateAnimation translateAnimationX = new TranslateAnimation(
						0, endX, 0, 0);
				translateAnimationX.setInterpolator(new LinearInterpolator());
				translateAnimationX.setRepeatCount(0);// 动画重复执行的次数
				// y轴的路径动画，加速
				TranslateAnimation translateAnimationY = new TranslateAnimation(
						0, 0, 0, endY);
				translateAnimationY
						.setInterpolator(new AccelerateInterpolator());
				translateAnimationY.setRepeatCount(0);// 动画重复执行的次数
				// ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f,
				// 1.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f,
				// Animation.RELATIVE_TO_SELF, 1.0f);
				AnimationSet set2 = new AnimationSet(false);
				// set2.addAnimation(scaleAnimation);
				set2.addAnimation(translateAnimationY);
				set2.addAnimation(translateAnimationX);

				set2.setFillAfter(true);
				set2.setStartOffset(200);// 等待时间
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

						// 延迟两秒 camera 再次预览
						new Handler().postDelayed(new Runnable() {
							public void run() {
								// execute the task
								soundPool.stop(currentStreamId);
								v.clearAnimation();
								isMagicShot = false; // 延迟两秒之后 取消掉Magic shoot 模式
								magicButton
										.setImageResource(R.drawable.magic_btn);

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

	// 创建动画图层
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

	// 添加试图到动画图层
	private View addViewToAnimLayout(ViewGroup vg, final View view,
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

	protected void PlaySound(int sound, int loop) {
		// TODO Auto-generated method stub
		AudioManager audioManager = (AudioManager) this
				.getSystemService(AUDIO_SERVICE);
		float streamVolumeCurrent = audioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		float streamVolumeMax = audioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		float volume = streamVolumeCurrent / streamVolumeMax;
		currentStreamId = soundPool.play(hashMap.get(sound), volume, volume, 1,
				loop, 1.0f);

	}

	private void initSoundPool() {
		// TODO Auto-generated method stub
		soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
		hashMap = new HashMap<Integer, Integer>();
		hashMap.put(1, soundPool.load(this, R.raw.bling, 1));
	}

}