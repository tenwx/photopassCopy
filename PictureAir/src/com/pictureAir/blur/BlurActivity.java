package com.pictureAir.blur;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.BinaryHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.pictureAir.MakegiftActivity;
import com.pictureAir.MyApplication;
import com.pictureAir.PPPDetailProductActivity;
import com.pictureAir.R;
import com.pictureAir.SubmitOrderActivity;
import com.pictureAir.entity.CartItemInfo;
import com.pictureAir.entity.PhotoInfo;
import com.pictureAir.service.DownloadService;
import com.pictureAir.util.API;
import com.pictureAir.util.AppManager;
import com.pictureAir.util.Common;
import com.pictureAir.util.HttpsUtil;
import com.pictureAir.util.ScreenUtil;
import com.pictureAir.widget.CustomProgressDialog;
import com.pictureAir.widget.MyToast;
//import com.pictureAir.widget.NewToast;

/** 单张照片浏览页面 */
public class BlurActivity extends Activity implements OnClickListener {
	private ImageLoader imageLoader;
	private CustomProgressDialog progressDialog;// 等待加载视图
	private Bitmap bitmap1 = null;// 原图的模糊图
	private Bitmap bitmap2 = null;// 原图
	private Bitmap bitmap3 = null;// 圆圈图
	private Bitmap bitmap4 = null;// 放大后的模糊图
	private Bitmap bitmap5 = null;// 放大后的清晰图
	private Bitmap resultBitmap = null;// 圆圈图的中心透明渐变
	private Bitmap bm = null;// mask蒙板
	private ImageView image01;// 模糊图的view
	private ImageView image02;// 圆圈的view
	private TextView  edit, share, download, gift;

	private RelativeLayout rl;
	private ImageView back;// 图片显示区域
	private float scaleH;// 图片缩放后的宽
	private float scaleW;// 图片缩放后的高
	private Matrix matrix;
	private boolean flag = false;// 是否是放大的
	private int count = 0;// 点击计数器
	private long fir = 0;// 第一次点击屏幕的时间
	private long sec = 0;// 第二次点击屏幕的时间
	private float downX = 0;// 点击屏幕的X坐标
	private float downY = 0;// 点击屏幕的Y坐标
	private boolean out = false;// 是否移动到屏幕边缘
	private boolean isFirst = false;//第一次进入标记

	private SharedPreferences sharedPreferences;
	private Dialog dia;
	private TextView buy_ppp, cancel, buynow, touchtoclean;
	private SharedPreferences appPreferences;
	private File dirFile;

	private MyToast myToast;
	private MyApplication application;
	/**
	 * 照片已购买情况下
	 * */
	private int mode = 0;// 初始状态
	private static final int MODE_DRAG = 111;
	private static final int MODE_ZOOM = 222;

	private PointF startPoint = new PointF();
	private Matrix currentMatrix = new Matrix();

	private float startDis;
	private PointF midPoint;

	/**
	 * sizeW、sizeH 截图比例 r 圆圈的半径
	 * */
	private int sizeW = 0;
	private int sizeH = 0;
	private int r = 0;

	private PhotoInfo photo;

	private RelativeLayout leadView;
	private Button knowImageView;

	private boolean loadFailed = false;

	@SuppressLint("HandlerLeak")
	/**
	 * x、y用于底图处理 x1、y1用于圆圈处理
	 * */
	int x;
	int y;
	int x1;
	int y1;

	int xx;
	int yy;

	private Handler handler = new Handler() {
		@SuppressLint("NewApi")
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				sendEmptyMessageDelayed(6, 500);
				x = msg.arg1 - r;
				y = (int) (msg.arg2 - r - ScreenUtil.getScreenHeight(BlurActivity.this) + scaleH);
				if (x > bitmap2.getWidth() - 2 * r) {
					x = bitmap2.getWidth() - 2 * r;
					x1 += 50;
					out = true;
				}
				if (x < 0) {
					x = 0;
					x1 -= 50;
					out = true;
				}
				if (y > bitmap2.getHeight() - 2 * r) {
					y = bitmap2.getHeight() - 2 * r;
					y1 += 50;
					out = true;
				}
				if (y < 0) {
					y = 0;
					y1 -= 50;
					out = true;
				}
				if (x1 < 0) {
					x1 = 0;
				}
				if (x1 > bitmap2.getWidth() - sizeW) {
					x1 = bitmap2.getWidth() - sizeW;
				}

				if (y1 < 0) {
					y1 = 0;
				}
				if (y1 > bitmap2.getHeight() - sizeH) {
					y1 = bitmap2.getHeight() - sizeH;
				}

				/***********之后加的*****************/
				if (x1 > bitmap1.getWidth() - sizeW) {
					x1 = bitmap1.getWidth() - sizeW;
				}
				if (y1 > bitmap1.getHeight() - sizeH) {
					y1 = bitmap1.getHeight() - sizeH;
				}
				/**********之后加的***************/



				if (flag == true) {
					if (out == true) {
						//						System.out.println(x1+"_"+y1+"_"+sizeW+"+"+sizeH+bitmap1.getWidth()+"__"+bitmap1.getHeight()+"+"+bitmap2.getWidth()+"__"+bitmap2.getHeight());

						bitmap4 = Bitmap.createBitmap(bitmap1, x1, y1, sizeW, sizeH);
						bitmap5 = Bitmap.createBitmap(bitmap2, x1, y1, sizeW, sizeH);
						Matrix m = new Matrix();
						m.postScale((float) scaleW / bitmap4.getWidth(), (float) scaleH / bitmap4.getHeight());
						bitmap4 = Bitmap.createBitmap(bitmap4, 0, 0, bitmap4.getWidth(), bitmap4.getHeight(), m, true);
						bitmap5 = Bitmap.createBitmap(bitmap5, 0, 0, bitmap5.getWidth(), bitmap5.getHeight(), m, true);
					}
					image01.setImageBitmap(bitmap4);
					bitmap3 = Bitmap.createBitmap(bitmap5, x, y, 2 * r, 2 * r);
					bitmap3 = Mask(bitmap3);
					bitmap3 = UtilOfDraw.toRoundBitmap(bitmap3);
				} else {
					bitmap3 = Bitmap.createBitmap(bitmap2, x, y, 2 * r, 2 * r);
					bitmap3 = Mask(bitmap3);
					bitmap3 = UtilOfDraw.toRoundBitmap(bitmap3);
				}
				image02.setX(x);
				image02.setY(y);
				image02.setImageBitmap(bitmap3);
				out = false;
				break;
			case 2:
				System.out.println("------->2");
				if (image02 != null) {
					System.out.println("image2 not null and set null");
					image02.setImageBitmap(null);
				}
				if (null != bitmap3){
					bitmap3.recycle();
					System.out.println("bitmap3 recycle-------->");
				}
				break;
			case 3:
				x1 = msg.arg1 - sizeW / 2;
				y1 = msg.arg2 - sizeH / 2;
				System.out.println("current xy = "+x1+"+"+y1);
				if (x1 < 0) {
					x1 = 0;
				}
				if (x1 > bitmap1.getWidth() - sizeW) {
					x1 = bitmap1.getWidth() - sizeW;
				}
				if (y1 < 0) {
					y1 = 0;
				}
				if (y1 > bitmap1.getHeight() - sizeH) {
					y1 = bitmap1.getHeight() - sizeH;
				}
				System.out.println("after currnet xy = "+x1+"_"+y1);
				if (flag == false) {
					bitmap4 = Bitmap.createBitmap(bitmap1, x1, y1, sizeW, sizeH);
					bitmap5 = Bitmap.createBitmap(bitmap2, x1, y1, sizeW, sizeH);
					Matrix m = new Matrix();
					m.postScale((float) scaleW / bitmap4.getWidth(), (float) scaleH / bitmap4.getHeight());
					bitmap4 = Bitmap.createBitmap(bitmap4, 0, 0, bitmap4.getWidth(), bitmap4.getHeight(), m, true);
					bitmap5 = Bitmap.createBitmap(bitmap5, 0, 0, bitmap5.getWidth(), bitmap5.getHeight(), m, true);
					image01.setImageBitmap(bitmap4);
					flag = true;
				} else {
					image01.setImageBitmap(bitmap1);
					flag = false;
				}
				break;
			case 4:
				image01.setImageBitmap(bitmap1);
				flag = false;
				out = false;
				break;
			case API.ADD_TO_CART_SUCCESS:
				JSONObject addcart = (JSONObject) msg.obj;
				System.out.println(addcart);
				String itemidString = "";
				try {
					itemidString = addcart.getString("cartItemId");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


				//将当前购买的照片信息存放到application中
				application.setIsBuyingPhotoInfo(photo);
				if (application.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_MYPHOTOPASS)) {

				}else if (application.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_VIEWORSELECTACTIVITY)) {

				}else {
					application.setRefreshViewAfterBuyBlurPhoto(Common.FROM_BLUR);
				}


				Intent intent = new Intent(BlurActivity.this, SubmitOrderActivity.class);
				ArrayList<CartItemInfo> orderinfo = new ArrayList<CartItemInfo>();
				CartItemInfo cartItemInfo = new CartItemInfo();
				cartItemInfo.cart_productName = Common.PHOTOPASS_NAME;
				cartItemInfo.cart_originalPrice = (double)59;
				cartItemInfo.cart_photoUrls = null;
				cartItemInfo.cart_productIntroduce = Common.PHOTOPASS_DESCRPITION;
				cartItemInfo.cart_productImageUrl = photo.photoThumbnail;
				cartItemInfo.cart_quantity = 1;
				cartItemInfo.isSelect = true;
				cartItemInfo.hasPhoto = false;
				cartItemInfo.cart_id = itemidString;
				cartItemInfo.cart_storeId = sharedPreferences.getString(Common.STORE_ID, "");
				cartItemInfo.cart_productId = "";
				cartItemInfo.cart_productType = 2;
				orderinfo.add(cartItemInfo);
				Editor editor = sharedPreferences.edit();

				editor.putInt(Common.CART_COUNT, sharedPreferences.getInt(Common.CART_COUNT, 0)+1);
				editor.commit();
				intent.putParcelableArrayListExtra("orderinfo", orderinfo);
				startActivity(intent);
				break;

			case 6:
				if (count > 0) {
					count = 0;
				}
				break;
			default:
				break;
			}

			// System.gc();
			super.handleMessage(msg);
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		photo = getIntent().getParcelableExtra("photo");
//		System.out.println(photo._id+"========id========");
		setContentView(R.layout.activity_blur);
		AppManager.getInstance().addActivity(this);
		myToast = new MyToast(this);
		application = (MyApplication)getApplication();
		image01 = (ImageView) findViewById(R.id.img01);
		leadView = (RelativeLayout)findViewById(R.id.blur_lead_view);
		knowImageView = (Button)findViewById(R.id.leadknow);
		touchtoclean = (TextView)findViewById(R.id.textview_blur);
		rl = (RelativeLayout) findViewById(R.id.rl);
		image01.setVisibility(View.INVISIBLE);
//		imageLoader = ImageLoader.getInstance();
//		imageLoader.displayImage(photo.photoThumbnail, image01);
		sharedPreferences = getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
		appPreferences = getSharedPreferences(Common.APP, Context.MODE_PRIVATE);
//		dialog = ProgressDialog.show(this, getString(R.string.loading___), getString(R.string.is_loading), false, false);
//		progressDialogPop = new CustomProgressBarPop(this, findViewById(R.id.parentAll));
//		progressDialogPop.show();
		progressDialog = CustomProgressDialog.show(this, getString(R.string.is_loading), false, null);
		final Handler mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch (msg.what) {
				case 0:
					if (null != bitmap2) {
						System.out.println("bitmap 2 not null");
						progressDialog.dismiss();
						init();
					}else {
						System.out.println("bitmap2 null-->");
						progressDialog.dismiss();
						loadFailed = true;
						myToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
						init();
					}
					break;
				case 1:
					ByteArrayOutputStream outStream = new ByteArrayOutputStream();  
					byte[] buffer = new byte[1024];  
					int len = 0;  
					FileInputStream inStream;
					try {
						inStream = new FileInputStream(dirFile);
						while( (len = inStream.read(buffer))!= -1){  
							outStream.write(buffer, 0, len);  
						}  
						outStream.close();  
						inStream.close();  
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					byte[] arg2 = outStream.toByteArray();
					bitmap2 = BitmapFactory.decodeByteArray(arg2, 0, arg2.length);
					if (null != bitmap2) {
						progressDialog.dismiss();
						init();
					}
					break;
				default:
					break;
				}
			}
		};
		dirFile = new File(getApplicationContext().getCacheDir()+"/"+photo.photoId);//创建一个以ID为名字的文件，放入到app缓存文件下
		System.out.println(dirFile.toString());
		System.out.println("photo URL ------->"+photo.photoThumbnail_1024);
		if (dirFile.exists()) {//如果文件存在
			System.out.println("file exists");
			//创建一个画图的监听，等到这个控件画好之后，触发监听函数，同时移除对应的监听
			ViewTreeObserver viewTreeObserver = rl.getViewTreeObserver();
			viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

				@Override
				public void onGlobalLayout() {
					// TODO Auto-generated method stub
					rl.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//					progressDialogPop.show();
					mHandler.sendEmptyMessage(1);
				}
			});
		}else {//如果文件不存在，下载文件到缓存
			System.out.println("file is not exist");
			HttpsUtil.get(photo.photoThumbnail_1024, new BinaryHttpResponseHandler() {
//				@Override
//				public void onStart() {
//					// TODO Auto-generated method stub
//					super.onStart();
//					progressDialogPop.show();
//				}
				@Override
				public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
					// TODO Auto-generated method stub
					BufferedOutputStream stream = null;
					try {
						System.out.println(dirFile.toString());
						FileOutputStream fsStream = new FileOutputStream(dirFile);
						stream = new BufferedOutputStream(fsStream);
						stream.write(arg2);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally{
						try {
							if(stream != null){
								stream.flush();
								stream.close();
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.out.println("done");
					}
					bitmap2 = BitmapFactory.decodeByteArray(arg2, 0, arg2.length);
					mHandler.sendEmptyMessage(0);
				}

				@Override
				public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					// TODO Auto-generated method stub
					System.out.println(arg3.toString());
					mHandler.sendEmptyMessage(0);
				}
//				@Override
//				public void onProgress(int bytesWritten, int totalSize) {
//					// TODO Auto-generated method stub
//					super.onProgress(bytesWritten, totalSize);
//					//进度条的展示
//					progressDialogPop.setProgress(bytesWritten, totalSize);
//				}
			});
		}



		if (!sharedPreferences.getString(Common.USERINFO_ID, "").equals( appPreferences.getString(Common.BLUR_LEAD, ""))) {//第一次进入
			leadView.setVisibility(View.VISIBLE);
			knowImageView.setOnClickListener(this);
			Editor editor = appPreferences.edit();
			//此处有问题，以后有时间再改。问题描述：此方法每次只能记录一个用户。
			editor.putString(Common.BLUR_LEAD, sharedPreferences.getString(Common.USERINFO_ID, ""));
			editor.commit();
			isFirst = true;
		}
	}

	/** 根据照片的购买情况确定布局和显示模式 */
	@SuppressLint("NewApi")
	private void init() {
		back = (ImageView) findViewById(R.id.back);
		edit = (TextView) findViewById(R.id.edit);
		share = (TextView) findViewById(R.id.share);
		download = (TextView) findViewById(R.id.download);
		gift = (TextView) findViewById(R.id.gift);

		if (!isFirst) {
			back.setOnClickListener(this);
			edit.setOnClickListener(this);
			share.setOnClickListener(this);
			download.setOnClickListener(this);
			gift.setOnClickListener(this);
		}

		if (!loadFailed) {//加载成功
			int w = bitmap2.getWidth();
			int h = bitmap2.getHeight();
			System.out.println("bitmap2 width, height"+w+"?"+h);
			scaleW = ScreenUtil.getScreenWidth(this);
			scaleH = rl.getHeight();
			if(w/h > scaleW/scaleH){
				bitmap2 = UtilOfDraw.rotaingImageView(90, bitmap2);
				w = bitmap2.getWidth();
				h = bitmap2.getHeight();
				//			Toast.makeText(this, "进入横屏模式", 0).show();
				//			newToast.setTextAndShow("Into landscape mode", Common.TOAST_SHORT_TIME);
				System.out.println("landscape+"+w+"+"+h);
				scaleH = rl.getHeight();
				scaleW = scaleH * w/h;
				if(scaleW > ScreenUtil.getScreenWidth(this)){
					scaleW = ScreenUtil.getScreenWidth(this);
					scaleH = scaleW * h/w;
				}
			}
			float sw = scaleW / w;
			float sh = scaleH / h;
			matrix = new Matrix();
			matrix.postScale(sw, sh);
			bitmap2 = Bitmap.createBitmap(bitmap2, 0, 0, w, h, matrix, true);
			System.out.println(bitmap2.getWidth()+"----"+bitmap2.getHeight());
			sizeW = (int) (scaleW / 2);
			sizeH = (int) (scaleH / 2);
			if (photo.isPayed == 0) {// 未购买的照片
				image02 = (ImageView) findViewById(R.id.img02);
				dia = new Dialog(this, R.style.dialogTans);
				Window window = dia.getWindow();
				window.setGravity(Gravity.CENTER);
//				window.setWindowAnimations(R.style.from_bottom_anim);
				dia.setCanceledOnTouchOutside(true);
				View view = View.inflate(this, R.layout.tans_dialog, null);
				view.setMinimumWidth(ScreenUtil.getScreenWidth(this));
				dia.setContentView(view);
				buy_ppp = (TextView) dia.findViewById(R.id.buy_ppp);
				cancel = (TextView) dia.findViewById(R.id.cancel);
				buynow = (TextView) dia.findViewById(R.id.buynow);
				buynow.setOnClickListener(this);
				buy_ppp.setOnClickListener(this);
				cancel.setOnClickListener(this);
				bm = BitmapFactory.decodeResource(getResources(), R.drawable.round_meitu_1).copy(Config.ARGB_8888, true);
				bitmap1 = UtilOfDraw.blur(bitmap2);
				System.out.println("bitmap1 = "+bitmap1.getWidth()+"_"+bitmap1.getHeight());
				//-------------------------//
				image01.setBackgroundColor(Color.RED);
				
				image01.setImageBitmap(bitmap1);
				System.out.println("---------->"+image01.getWidth()+"_____"+image01.getHeight());
				
				r = (int) (ScreenUtil.getScreenWidth(this) / 3);
			} else {
				image01.setImageBitmap(bitmap2);
				image01.setScaleType(ImageView.ScaleType.MATRIX);
				matrix.setScale(rl.getWidth() / bitmap2.getWidth(), rl.getHeight() / bitmap2.getHeight());
				image01.setImageMatrix(matrix);
			}
			image01.setVisibility(View.VISIBLE);
			yy = (rl.getHeight() - bitmap2.getHeight()) / 2;
			xx = (rl.getWidth() - bitmap2.getWidth()) / 2;

		}else {
			touchtoclean.setText(R.string.failed);
		}
	}

	@SuppressLint("NewApi")
	public boolean onTouchEvent(MotionEvent event) {
		if (!loadFailed) {
			if (photo.isPayed == 0) {// 未购买状态
				Message msg = handler.obtainMessage();
				msg.arg1 = (int) event.getX();
				msg.arg2 = (int) event.getY();
				switch (event.getAction()) {

				case MotionEvent.ACTION_DOWN:
					System.out.println("-------->down");
					downX = event.getX();
					downY = event.getY();
					count++;
					touchtoclean.setVisibility(View.INVISIBLE);
					if (count == 1) {
						System.out.println("---->1");
						fir = System.currentTimeMillis();
						msg.what = 1;
					}
					if (count == 2) {
						System.out.println("------>2");
						sec = System.currentTimeMillis();
						if (sec - fir < 500) {// 双击放大
							msg.what = 3;
							count = 0;
							fir = 0;
							sec = 0;
						}else {
							count = 0;
						}
					}else {
						System.out.println("------->"+count);
					}

					break;
				case MotionEvent.ACTION_MOVE:
					System.out.println("----->move");
					float moveX = event.getX() - downX;
					float moveY = event.getY() - downY;
					if (moveX != 0 | moveY != 0) {
						msg.what = 1;
						count = 0;
						fir = 0;
						sec = 0;
					}
					break;
				case MotionEvent.ACTION_UP:
					System.out.println("up");
					msg.what = 2;
					touchtoclean.setVisibility(View.VISIBLE);
					break;
				}
				handler.sendMessage(msg);

			} else {// 已购买状态
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				// 手指压下屏幕
				case MotionEvent.ACTION_DOWN:
					// 记录ImageView当前的移动位置
					currentMatrix.set(image01.getImageMatrix());
					startPoint.set(event.getX(), event.getY());
					mode = MODE_DRAG;
					count++;
					if (count == 1) {
						fir = System.currentTimeMillis();
					}
					if (count == 2) {
						sec = System.currentTimeMillis();
						if (sec - fir < 500) {
							System.out.println(123);
							matrix.setScale(rl.getWidth() / image01.getWidth(), rl.getHeight() / image01.getHeight());
							mode = 0;
						}
						count = 0;
						fir = 0;
						sec = 0;
					}
					break;
				case MotionEvent.ACTION_MOVE:
					// 拖拉图片
					if (mode == MODE_DRAG) {
						float dx = event.getX() - startPoint.x; // 得到x轴的移动距离
						float dy = event.getY() - startPoint.y; // 得到y轴的移动距离
						// 在没有移动之前的位置上进行移动
						if (dx != 0 || dy != 0) {
							matrix.set(currentMatrix);
							matrix.postTranslate(dx, dy);
							count = 0;
							fir = 0;
							sec = 0;
						}
					}
					// 放大缩小图片
					else if (mode == MODE_ZOOM) {
						float endDis = distance(event);// 结束距离
						if (endDis > 10f) { // 两个手指并拢在一起的时候像素大于10
							float scale = endDis / startDis;// 得到缩放倍数
							matrix.set(currentMatrix);
							matrix.postScale(scale, scale, midPoint.x, midPoint.y);
							count = 0;
							fir = 0;
							sec = 0;
						}
					}
					break;
					// 手指离开屏幕
				case MotionEvent.ACTION_UP:
					// 当触点离开屏幕，但是屏幕上还有触点(手指)
				case MotionEvent.ACTION_POINTER_UP:
					mode = 0;
					break;
					// 当屏幕上已经有触点(手指)，再有一个触点压下屏幕
				case MotionEvent.ACTION_POINTER_DOWN:
					mode = MODE_ZOOM;
					startDis = distance(event);
					if (startDis > 10f) { // 两个手指并拢在一起的时候像素大于10
						midPoint = mid(event);
						// 记录当前ImageView的缩放倍数
						currentMatrix.set(image01.getImageMatrix());
					}
					break;
				}
				image01.setImageMatrix(matrix);
			}
		}
		return super.onTouchEvent(event);
	}

	/** 计算两个手指间的距离 */
	@SuppressWarnings("deprecation")
	private float distance(MotionEvent event) {
		try {
			float dx = event.getX(1) - event.getX(0);
			float dy = event.getY(1) - event.getY(0);
			/** 使用勾股定理返回两点之间的距离 */
			return (float) Math.sqrt(dx * dx + dy * dy);
		} catch (IllegalArgumentException e) {
			// TODO: handle exception
		}
		return 0;
	}

	/** 计算两个手指间的中间点 */
	private PointF mid(MotionEvent event) {
		float midX = (event.getX(1) + event.getX(0)) / 2;
		float midY = (event.getY(1) + event.getY(0)) / 2;
		return new PointF(midX, midY);
	}

	@Override
	public void onBackPressed() {
		if (flag == true && photo.isPayed == 0) {
			handler.sendEmptyMessage(4);
		} else {
			BlurActivity.this.finish();
			//			AppManager.getInstance().killTopActivity();
		}
	}

	/**
	 * 制作周边渐变模糊
	 * 1.将正方形内切圆以外的部分进行全透明处理
	 * 2.将蒙版图片设置成全黑，边缘透明，中心不透明 的渐变效果
	 * 3.将截取的图片的透明值设为完全透明
	 * 4.将截图和蒙版进行合成，分两点，1）透明值合成，截图的全透明和蒙版的透明进行或运算，得到的是蒙版的透明值
	 *     						2）颜色值的合成，截图的颜色值和蒙版的颜色值（蒙版只有黑色）进行或运算，得到合成后的颜色值
	 *     						因为是以十六进制表示，所以高位表示透明值，低位表示颜色值
	 * @param b Bitmap对象
	 * @return resultBitmap 合成之后的bitmap对象
	 */
	private Bitmap Mask(Bitmap b) {
		//如果resultBitmap已经存在，则不需要重新创建一个bitmap
		if (resultBitmap == null)
			//创建一个新的bitmap，三个参数依次是宽，高，config
			resultBitmap = Bitmap.createBitmap(b.getWidth(), b.getHeight(), Bitmap.Config.ARGB_8888);
		int w = bm.getWidth();//获取mask蒙板的宽
		int h = bm.getHeight();//获取高
		float sw = (float) b.getWidth() / w;
		float sh = (float) b.getHeight() / h;
		//matrix为android自带的图片处理的一个类（矩阵）
		matrix.reset();//初始化
		matrix.postScale(sw, sh);//设置缩放的比例
		//将mask蒙板缩放到和截图一样大小
		bm = Bitmap.createBitmap(bm, 0, 0, w, h, matrix, true);
		//创建数组
		int[] pixels_b = new int[b.getWidth() * b.getHeight()];
		int[] pixels_bm = new int[bm.getWidth() * bm.getHeight()];
		//得到传入参数的像素值，并且放入pixels_b中
		b.getPixels(pixels_b, 0, b.getWidth(), 0, 0, b.getWidth(), b.getWidth());
		//得到mask蒙板的像素值，并且放入pixels_bm中
		bm.getPixels(pixels_bm, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm.getWidth());
		//遍历mask蒙板数组，图片全黑部分转化为全透明，其他地方和截取的图片进行合成
		for (int i = 0; i < pixels_bm.length; i++) {
			if (pixels_bm[i] == 0xff000000) {//ff000000为不透明的黑色
				//因为在截取图片的时候，只能截取方块，所以圆以外的部分做全透明处理
				pixels_b[i] = 0;//全透明的黑色
				//			} else if (pixels_b[i] == 0) {
			} else {
				pixels_bm[i] &= 0xff000000;//全部变成000000，但是透明度不变
				pixels_bm[i] = 0xff000000 - pixels_bm[i];//颜色不变，透明度翻转，这两步相当于把蒙版的透明度翻转，颜色值全部变为黑色
				pixels_b[i] &= 0x00ffffff;//颜色值不变，但是透明度全部变成完全透明，相当于将截取到的图片设为完全透明
				pixels_b[i] |= pixels_bm[i];//将蒙版和截图进行合成，分两块，一块是透明度的合成，一块是颜色值的合成
				//透明度的合成，截图完全透明，|操作，取的时蒙版的透明度
				//颜色值的合成，截图的颜色值和蒙版的颜色值进行|操作
			}
		}
		resultBitmap.setPixels(pixels_b, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm.getHeight());
		return resultBitmap;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent;
		switch (v.getId()) {
		case R.id.back:

			finish();
			break;
		case R.id.edit://如果已经购买，跳转到编辑界面进行编辑
			if (photo.isPayed == 1) {

			} else{
				if (loadFailed) {
					myToast.setTextAndShow(R.string.reloadphoto, Common.TOAST_SHORT_TIME);
				}else {
					dia.show();
				}

			}
			break;
		case R.id.gift://跳转到makegift
			if (loadFailed) {
				myToast.setTextAndShow(R.string.reloadphoto, Common.TOAST_SHORT_TIME);
			}else {
				intent = new Intent(this, MakegiftActivity.class);
//				intent.putExtra("albums", Common.ALBUM_PHOTOPASS);
//				intent.putExtra("thumbnailURL", photo.photoThumbnail_512);
//				intent.putExtra("photoId", photo.photoId);
				intent.putExtra("selectPhoto", photo);
				startActivity(intent);
				dia.dismiss();

			}
			break;
		case R.id.share:
			if (photo.isPayed == 1){
				dia.dismiss();
				//				ShowShare.showShare(this, photo);
			}else{
				if (loadFailed) {
					myToast.setTextAndShow(R.string.reloadphoto, Common.TOAST_SHORT_TIME);
				}else {
					dia.show();
				}

			}
			break;
		case R.id.download:
			if (photo.isPayed == 1) {
				ArrayList<String> list = new ArrayList<String>();
				list.add(photo.photoPathOrURL);
				Intent service = new Intent(this, DownloadService.class);
				Bundle bundle = new Bundle();
				bundle.putStringArrayList("photos", list);
				service.putExtras(bundle);
				startService(service);
				dia.dismiss();
			} else{
				if (loadFailed) {
					myToast.setTextAndShow(R.string.reloadphoto, Common.TOAST_SHORT_TIME);
				}else {
					dia.show();
				}

			}
			break;
		case R.id.cancel:
			dia.dismiss();
			break;
		case R.id.buynow:
			//编辑传入照片的信息
			JSONArray embedphotos = new JSONArray();//放入图片的json数组
			JSONObject embedphoto = new JSONObject();
			JSONArray photoids = new JSONArray();//放入图片的图片id数组
			JSONObject photoid = new JSONObject();
			try {
				//如果是多张照片，此处需要for循环处理
				photoid.put("photoId", "");
				photoid.put("photoUrl", "");
				photoids.put(photoid);
				embedphoto.put("photosIds", photoids);
				embedphoto.put("svg", "");
				embedphotos.put(embedphoto);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			API.addtocart(sharedPreferences.getString(Common.USERINFO_ID, ""), sharedPreferences.getString(Common.STORE_ID, ""), "", 1, 
					Common.PHOTOPASS_NAME, photo.photoId, Double.valueOf(59), photo.photoThumbnail,
					Common.PHOTOPASS_DESCRPITION, "", Double.valueOf(59), handler, embedphotos, true);
			dia.dismiss();
			break;
		case R.id.buy_ppp:
			intent = new Intent(this, PPPDetailProductActivity.class);
			intent.putExtra("activity", "bluractivity");
			//			Editor editor = sharedPreferences.edit();
			//			editor.putString(Common.AUTO_BIND_PP, photo._id);
			//			editor.commit();
			startActivity(intent);
			dia.dismiss();
			break;
		case R.id.leadknow:
			System.out.println("know");
			leadView.setVisibility(View.GONE);
			back.setOnClickListener(this);
			edit.setOnClickListener(this);
			share.setOnClickListener(this);
			download.setOnClickListener(this);
			gift.setOnClickListener(this);
			break;
		default:
			break;
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//如果手指在上面的时候，如果同时休眠，在唤醒之后，页面上有个清晰圈
		//需要通知handler释放清晰圈
		handler.sendEmptyMessage(2);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		System.out.println("----------->"+application.getRefreshViewAfterBuyBlurPhoto());
		if (application.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_MYPHOTOPASSPAYED)) {

		}else if (application.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_VIEWORSELECTACTIVITYANDPAYED)) {

		}else if (application.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_BLURPAYED)) {

		}else {
			application.setRefreshViewAfterBuyBlurPhoto("");
		}
		//按返回，把状态全部清除
		application.setIsBuyingPhotoInfo(null);

		AppManager.getInstance().killActivity(this);
	}
}
