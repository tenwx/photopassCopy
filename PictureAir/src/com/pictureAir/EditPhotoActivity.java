package com.pictureAir;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.Header;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.loopj.android.http.BinaryHttpResponseHandler;
import com.pictureAir.adapter.EditActivityAdapter;
import com.pictureAir.adapter.FontAdapter;
import com.pictureAir.entity.PhotoInfo;
import com.pictureAir.filter.Amaro;
import com.pictureAir.filter.BeautifyFilter;
import com.pictureAir.filter.BlurFilter;
import com.pictureAir.filter.EarlyBird;
import com.pictureAir.filter.Filter;
import com.pictureAir.filter.HDRFilter;
import com.pictureAir.filter.LomoFi;
import com.pictureAir.filter.LomoFilter;
import com.pictureAir.filter.NormalFilter;
import com.pictureAir.filter.OldFilter;
import com.pictureAir.util.AppUtil;
import com.pictureAir.util.Common;
import com.pictureAir.util.HttpUtil;
import com.pictureAir.util.ScreenUtil;
import com.pictureAir.widget.HorizontalListView;
import com.pictureAir.widget.MyToast;
import com.pictureAir.widget.TouchView;

public class EditPhotoActivity extends BaseActivity implements OnClickListener {
	private String photoURL;
	private ImageView editImageView; // 原始图
	private ImageView frameImageView; // 相框
	private Bitmap bitmap;
	private ArrayList<String> framelist;// 最新下载的边框的list
	// 编辑的工具条
	private LinearLayout edittoolsbar;
	private RelativeLayout preview_titlebar;
	private int[] pic = { R.drawable.frame_none, R.drawable.frame1_1,
			R.drawable.frame2_2, R.drawable.frame3_3, R.drawable.frame4_4,
			R.drawable.frame5_5, R.drawable.frame6_6, R.drawable.frame7_7,
			R.drawable.frame8_8 };// 缩略图
	private Bitmap frameBitmap;
	private int[] pic2 = { 0, R.drawable.frame1, R.drawable.frame2,
			R.drawable.frame3, R.drawable.frame4, R.drawable.frame5,
			R.drawable.frame6, R.drawable.frame7, R.drawable.frame8 };// 原图
	private int[] pic3 = { R.drawable.decoration1, R.drawable.decoration2,
			R.drawable.decoration4, R.drawable.decoration5,
			R.drawable.decoration6, R.drawable.decoration7,
			R.drawable.decoration8, R.drawable.decoration9,
			R.drawable.decoration10, R.drawable.decoration11,
			R.drawable.decoration12 };// 饰品。
	private int[] pic4 = { R.drawable.original, R.drawable.filter1,
			R.drawable.filter2, R.drawable.filter3, R.drawable.filter4,R.drawable.filter5,
			R.drawable.filter6 };//
	private TextView frameButton; // 相框按钮
	private TextView filterButton; // 滤镜按钮
	private TextView accessoryButton;
	private TextView rotateButton;
	private TextView titleTextView;

	private ArrayList<String> decorationlist;// 最新下载的边框的list

	private RelativeLayout preframe;
	private ImageView back;

	// 图片布局宽高
	public static int fraWidth;
	public static int fraHeight;

	private ArrayList<TouchView> imageViews;
	private int currentpicposition = 0;// 边框选择序号

	// save按钮
	private TextView preview_save;
	private File nameFile;
	private SimpleDateFormat dateFormat;

	private File tempFile;

	private final static int LOADIMAGEFROMSDCARD = 10;
	private final static int LOADFRAME = 11;
	private final static int LOADIMAGEFROMCACHE = 12;
	private final static int TOUCH_VIEW_ON_CLICK = 6;
	private final static String TAG = "EditPhotoActivity";

	private ImageButton btn_cancel; // 返回按钮。撤销
	private ImageButton btn_forward; // 前进

	private EditActivityAdapter eidtAdapter; // 通用的适配器

	private HorizontalListView top_HorizontalListView;
	private ImageView btn_left_back;
	private ImageButton btn_onedit_save;
	private int degree = 0;
	// 保存图片路径的集合。
	private ArrayList<String> pathList;
	private int index = -1; // 索引。

	private LinearLayout rotate_bar; // 底部的 旋转 bar

	private TextView tvLeft90;
	private TextView tvRight90;
	// 是否是通过 left 进入下一个
	boolean isAddText = false;
	boolean isFilter = false;
	private Bitmap curBitmap;
	
	private SharedPreferences sharedPreferences;
	private Editor editor;
	
	private Filter filter;
	private Bitmap newImage;
	
	private List<Typeface> strings;
	private List<String> fontList;
	
	// baker
	private TouchView fontImageView;// 合成图
	private LinearLayout fontAccessory;
	private ViewGroup rootView;
	private EditText fontEditText;
	private Bitmap fontBitmap;
	private String fonts[] = null;
	private SimpleAdapter simpleAdapter;
	private GridView setFontGridView;
	private int colors[] = new int[] { R.drawable.color1, R.drawable.color2,
			R.drawable.color3, R.drawable.color4, R.drawable.color5,
			R.drawable.color6, R.drawable.color7, R.drawable.color8,
			R.drawable.color9, R.drawable.color10, R.drawable.color11,
			R.drawable.color12, R.drawable.color13, R.drawable.color14,
			R.drawable.color15, R.drawable.color16 };
	private GridView setColorGridView;
	
	private PhotoInfo photoInfo;
	private MyToast myToast;
	
	private Typeface typeface1;
	private Typeface typeface2;
	private Typeface typeface3;
	private Typeface typeface4;
	private FontAdapter fontAdapter;
	
	//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_photo);
		init();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		System.out.println("------------> on pause");
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		System.out.println("------------>onresume");
		if (typeface2 == null) {
			System.out.println("-----------> load typeface2");
			typeface2 = Typeface.createFromAsset(getAssets(),
					"fonts/fangzheng.TTF");
		}else {
			System.out.println("---------> need not load typeface2");
		}
		if (typeface3 == null) {
			typeface3 = Typeface.createFromAsset(getAssets(),
					"fonts/gangbi.ttf");
		}
		if (typeface4 == null) {
			typeface4 = Typeface.createFromAsset(getAssets(), "fonts/keai.ttf");
		}
		if (typeface1 == null) {
			typeface1 = Typeface.SANS_SERIF;
		}
	}

	private void init() {
		// 进入编辑页面的时候，先清空 tempPic 文件夹
		deleteTempPic(Common.TEMPPIC_PATH);
		pathList = new ArrayList<String>();
		myToast = new MyToast(this);
		// 创建 pictureAir／temppic文件夹，存放临时文件。 后退一步就存一个。
		tempFile = new File(Common.TEMPPIC_PATH);
		if (!tempFile.isDirectory()) {
			tempFile.mkdirs();// 创建根目录文件夹
		}

		tvLeft90 = (TextView) findViewById(R.id.setTextFont);
		tvRight90 = (TextView) findViewById(R.id.setColor);

		tvLeft90.setOnClickListener(this);
		tvRight90.setOnClickListener(this);

		rotate_bar = (LinearLayout) findViewById(R.id.rotate_bar);
		//
		btn_onedit_save = (ImageButton) findViewById(R.id.btn_onedit_save);
		btn_onedit_save.setOnClickListener(this);

		btn_left_back = (ImageView) findViewById(R.id.btn_left_back);
		btn_left_back.setOnClickListener(this);

		top_HorizontalListView = (HorizontalListView) findViewById(R.id.horizontalListView);

		// top_HorizontalListView.getBackground().setAlpha(200); //底部透明
		btn_cancel = (ImageButton) findViewById(R.id.btn_cancel);

		btn_cancel.setOnClickListener(this);
		// btn_cancel.setVisibility(View.GONE);
		btn_forward = (ImageButton) findViewById(R.id.btn_forward);
		btn_forward.setOnClickListener(this);
		// 画布局
		editImageView = (ImageView) findViewById(R.id.previewphoto_imageView1); // 原始图
		frameImageView = (ImageView) findViewById(R.id.framephoto_imageView1); // 相框
		edittoolsbar = (LinearLayout) findViewById(R.id.edittoolsbar);
		rotateButton = (TextView) findViewById(R.id.edit_rotate);
		preview_save = (TextView) findViewById(R.id.preview_save);
		// preview_save.setVisibility(View.INVISIBLE);
		frameButton = (TextView) findViewById(R.id.edit_frame);
		accessoryButton = (TextView) findViewById(R.id.edit_accessory);
		filterButton = (TextView) findViewById(R.id.edit_filter);
		preframe = (RelativeLayout) findViewById(R.id.preframe);
		back = (ImageView) findViewById(R.id.edit_return);
		titleTextView = (TextView) findViewById(R.id.title_edit);
		titleTextView.setVisibility(View.GONE);
		
		//baker
		fontEditText = (EditText) findViewById(R.id.fontEditText);
		setFontGridView = (GridView) findViewById(R.id.fontList);
		setColorGridView = (GridView) findViewById(R.id.colorList);
		preview_titlebar = (RelativeLayout) findViewById(R.id.preview_titlebar);
		// 创建监听
		frameButton.setOnClickListener(this);
		filterButton.setOnClickListener(this);
		accessoryButton.setOnClickListener(this);
		preview_save.setOnClickListener(this);
		rotateButton.setOnClickListener(this);
		back.setOnClickListener(this);

		
		preframe.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				System.out.println("on touch ------------>");
				if (fontEditText.isShown()) {
					if (fontAccessory != null) {
						fontAccessory.removeAllViews();
					}
					if (rootView != null) {
						rootView.removeView(fontAccessory);
					}
					fontAccessory = new LinearLayout(EditPhotoActivity.this);
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.MATCH_PARENT,
							LinearLayout.LayoutParams.MATCH_PARENT);
					fontAccessory.setLayoutParams(lp);
					fontAccessory.setBackgroundResource(android.R.color.transparent);
					rootView = (ViewGroup) EditPhotoActivity.this.getWindow()
							.getDecorView();
					hideInputMethodManager(v);
					String str = fontEditText.getText().toString();// 获取文本框输入内容
					if (str.isEmpty()) {
						str = getString(R.string.add_text);
					}
					Paint textPaint = new Paint();// 设置画笔
					textPaint.setTextAlign(Paint.Align.LEFT);
					// Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG
					// | Paint.DEV_KERN_TEXT_FLAG);// 设置画笔
					textPaint.setTextSize(fontEditText.getTextSize());// 字体大小
					textPaint.setTypeface(fontEditText.getTypeface());// 字体
					textPaint.setColor(fontEditText.getCurrentTextColor());// 获取当前字体颜色
					Rect bounds = new Rect();
					textPaint.getTextBounds(str, 0, str.length(), bounds);
					int width = (int) textPaint.measureText(str);
					FontMetrics fm = textPaint.getFontMetrics();
					int height = (int) (fm.bottom - fm.top);
					System.out.println("bounds width:"+ bounds.width());
//					int width = bounds.width();
//					int height = bounds.height();
					fontEditText.setVisibility(View.GONE);
					System.out.println("editText width is " + width
							+ "height is " + height);
					System.out.println("top=="+fm.top);
					System.out.println("ascent=="+fm.ascent);
					System.out.println("descent=="+fm.descent);
					System.out.println("bottom=="+fm.bottom);
					System.out.println("paint ascent=="+textPaint.ascent());
					System.out.println("paint descent=="+textPaint.descent());
					fontBitmap = Bitmap.createBitmap(width, height,
							Config.ARGB_8888);
					Canvas canvas = new Canvas(fontBitmap);
					canvas.drawText(str, 0f, height - fm.descent, textPaint);
//					canvas.drawText(str, 0f, height + fm.top - fm.ascent, textPaint);
					
//					LayoutParams params = new LayoutParams(fontBitmap
//							.getWidth(), fontBitmap.getHeight());
					
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
							fontBitmap.getWidth(), fontBitmap.getHeight());
					float textWidth = textPaint.measureText(str);
					int[] l2 = new int[2];
					preview_titlebar.getLocationOnScreen(l2);
					int y1 = l2[1];
					int x = (int) fontEditText.getX();
					int y = (int) fontEditText.getY();
					int x2 = (int) (x + fontEditText.getWidth() / 2 - textWidth / 2);
					int y2 = y + y1 + fontEditText.getHeight()
							+ fontEditText.getPaddingBottom()
							+ fontEditText.getPaddingTop();

					params.setMargins(x2, y2, 0, 0);
					
					
					fontImageView.setLayoutParams(params);
					
					fontImageView.setImageBitmap(fontBitmap);
//					fontImageView.setBackgroundColor(Color.GRAY);
					fontImageView.setClickable(true);
					fontImageView.setFocusable(true);
					System.out.println("editText width is " + params.width
							+ "height is " + params.height);
					fontImageView.setVisibility(View.VISIBLE);
					
					rootView.addView(fontAccessory);
					fontAccessory.addView(fontImageView);
//					fontAccessory.setGravity(Gravity.CENTER);// 居中显示
					
				}
//				}
				return false;
			}
		});
		
		// 边框浮窗
		framelist = new ArrayList<String>();
		for (int i = 0; i < pic.length; i++) {
			framelist.add(String.valueOf(pic[i]));
		}

		// 初始化数据
		photoInfo = getIntent().getParcelableExtra("photo");
		photoURL = photoInfo.photoPathOrURL;
		sharedPreferences = getSharedPreferences("pictureAir", MODE_PRIVATE);

		pathList.add(photoURL);

		nameFile = new File(Common.PHOTO_SAVE_PATH);
		if (!nameFile.isDirectory()) {
			nameFile.mkdirs();// 创建根目录文件夹
		}
		dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
		imageViews = new ArrayList<TouchView>();
		decorationlist = new ArrayList<String>();
		for (int i = 0; i < pic3.length; i++) {
			decorationlist.add(String.valueOf(pic3[i]));
		}
		strings = new ArrayList<Typeface>();
		fontList = new ArrayList<String>();
		
		fonts =  new String[] { getString(R.string.font_default),
				getString(R.string.font_fangzheng),
				getString(R.string.font_pen),
				getString(R.string.font_lovely) };
		
		// 加载图片
		if (photoInfo.onLine == 1) {// 网络图片
			System.out.println("editPhoto=" + "pp");
			/*
			 * 需要使用三级缓存，1.判断SD卡是否存在，2.判断缓存中是否存在，3.从网上下载
			 */
			// 1.获取需要显示文件的文件名
			String fileString = ScreenUtil.getReallyFileName(photoURL);
			// 2、判断文件是否存在sd卡中
			File file = new File(Common.PHOTO_DOWNLOAD_PATH + fileString);
			if (file.exists()) {// 3、如果存在SD卡，则从SD卡获取图片信息
				System.out.println("file exists");
				loadFileFromSDCard(file.toString(), LOADIMAGEFROMSDCARD);

			} else {// 4、如果SD卡不存在，判断是否在缓存中
				System.out.println("file not exists");
				// 获取在缓存中的文件的名字
				final File dirfile = new File(this.getCacheDir() + "/"
						+ photoInfo.photoId+"_ori");
				System.out.println("dirfile = " + dirfile.toString());
				if (dirfile.exists()) {// 5、如果缓存存在，则从缓存中获取图片信息
					System.out.println("cache exists");

					loadFileFromSDCard(dirfile.toString(), LOADIMAGEFROMCACHE);

				} else {// 6.如果缓存不存在，从网络获取图片信息，
					System.out.println("cache not exist");
					HttpUtil.get(photoURL, new BinaryHttpResponseHandler() {
						@Override
						public void onSuccess(int arg0, Header[] arg1,
								byte[] arg2) {
							// TODO Auto-generated method stub
							bitmap = BitmapFactory.decodeByteArray(arg2, 0,
									arg2.length);
							fraWidth = ScreenUtil
									.getScreenWidth(EditPhotoActivity.this);
							fraHeight = preframe.getHeight();
							bitmap = zoomBitmap(bitmap.getWidth(),
									bitmap.getHeight(), fraWidth, fraHeight,
									bitmap);
							if (bitmap != null) {
								editImageView.setImageBitmap(bitmap);
							}

							// 7.将网络获取的图片信息存放到缓存
							BufferedOutputStream stream = null;
							try {
								System.out.println(dirfile.toString());
								FileOutputStream fsStream = new FileOutputStream(
										dirfile);
								stream = new BufferedOutputStream(fsStream);
								stream.write(arg2);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} finally {
								try {
									if (stream != null) {
										stream.flush();
										stream.close();
									}
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								System.out.println("done");
							}
						}

						@Override
						public void onFailure(int arg0, Header[] arg1,
								byte[] arg2, Throwable arg3) {
							System.out.println(arg3.toString());
						}

					});
				}
			}

		} else {// 本地图片
			System.out.println("editPhoto=" + "not pp");
			loadFileFromSDCard(photoURL, LOADIMAGEFROMSDCARD);
		}

	}

	/**
	 * 加载本地图片
	 * 
	 * @param url
	 *            图片文件的url
	 * @param type
	 *            LOADIMAGEFROMSDCARD，SD卡文件，LOADIMAGEFROMCACHE，缓存文件
	 */
	private void loadFileFromSDCard(final String url, final int type) {
		// 创建一个画图监听，如果布局画完，就通知处理显示本地图片
		ViewTreeObserver viewTreeObserver = preframe.getViewTreeObserver();
		viewTreeObserver
				.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {
						// TODO Auto-generated method stub
						preframe.getViewTreeObserver()
								.removeGlobalOnLayoutListener(this);
						// 获取预览框大小
						fraWidth = ScreenUtil
								.getScreenWidth(EditPhotoActivity.this);
						fraHeight = preframe.getHeight();
						Message message = handler.obtainMessage();
						message.what = type;
						message.obj = url;
						handler.sendMessage(message);
					}
				});
	}
	
	/**
	 * 点击键盘之外，隐藏键盘
	 */
	@Override  
	public boolean dispatchTouchEvent(MotionEvent ev) {
	    if (ev.getAction() == MotionEvent.ACTION_DOWN) {  
	        View v = getCurrentFocus();  
	        if (AppUtil.isShouldHideInput(v, ev)) {  
//	        	if (!password.hasFocus() && !userName.hasFocus()) {
	        		hideInputMethodManager(v);
//				}
	        }  
	        return super.dispatchTouchEvent(ev);  
	    }  
	    // 必不可少，否则所有的组件都不会有TouchEvent了  
	    if (getWindow().superDispatchTouchEvent(ev)) {  
	        return true;  
	    }  
	    return onTouchEvent(ev);  
	} 
	
	private void hideInputMethodManager(View v) {
		/* 隐藏软键盘 */
		InputMethodManager imm = (InputMethodManager) v.getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm.isActive()) {
			imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
		}
	}

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case LOADIMAGEFROMSDCARD:// 加载本地的原始图片
				String urlString = msg.obj.toString();
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(urlString, options);
				int width = options.outWidth;
				int height = options.outHeight;
				System.out.println(width + "{" + height);

				System.out.println(fraWidth + "?" + fraHeight);
				bitmap = BitmapFactory.decodeFile(urlString);
				bitmap = zoomBitmap(width, height, fraWidth, fraHeight, bitmap);
				if (bitmap != null) {
					
					if(AppUtil.getExifOrientation(urlString)!=0){
						bitmap = AppUtil.rotaingImageView(AppUtil.getExifOrientation(urlString),bitmap);
					}
					editImageView.setImageBitmap(bitmap);
				}
				break;

			case LOADFRAME:// 加载边框
				loadframe(currentpicposition,
						((BitmapDrawable) editImageView.getDrawable())
								.getBitmap().getWidth(),
						((BitmapDrawable) editImageView.getDrawable())
								.getBitmap().getHeight());
				break;

			case LOADIMAGEFROMCACHE:// 加载缓存中的图片
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int len = 0;
				FileInputStream inStream;
				try {
					inStream = new FileInputStream(new File(msg.obj.toString()));
					while ((len = inStream.read(buffer)) != -1) {
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
				bitmap = BitmapFactory.decodeByteArray(arg2, 0, arg2.length);
				bitmap = zoomBitmap(bitmap.getWidth(), bitmap.getHeight(),
						fraWidth, fraHeight, bitmap);
				if (bitmap != null) {
					editImageView.setImageBitmap(bitmap);
				}
				break;

			case TOUCH_VIEW_ON_CLICK:
				Log.d(TAG, "touchView onclick--------->");
				if (fontImageView != null) {
					Log.d(TAG, "need edit text--------->");
					fontImageView.setVisibility(View.GONE);
					int[] location = new int[2];
					fontImageView.getLocationOnScreen(location);
					int x = location[0];
					int y = location[1];
					int[] l2 = new int[2];
					preview_titlebar.getLocationOnScreen(l2);
					int y1 = l2[1];
					TextPaint textPaint = fontEditText.getPaint();
					float textWidth = textPaint.measureText((fontEditText
							.getText().toString().isEmpty()) ? getString(R.string.add_text) : fontEditText.getText().toString());
					fontEditText.setX(x - fontEditText.getWidth() / 2
							+ textWidth / 2);
					fontEditText.setY(y - y1 - fontEditText.getHeight()
							- fontEditText.getPaddingBottom()
							- fontEditText.getPaddingTop());
					fontEditText.setVisibility(View.VISIBLE);
					fontEditText.setFocusable(true);
					
					fontEditText.setFocusableInTouchMode(true);
					fontEditText.requestFocus();
					fontEditText.findFocus();
				}
				break;
				
			default:
				break;
			}
		};
	};

	/**
	 * 将获取的bitmap进行压缩
	 * 
	 * @param bitW
	 *            bitmap的宽
	 * @param bitH
	 *            bitmap的高
	 * @param requestW
	 *            需要预览的宽
	 * @param requestH
	 *            需要预览的高
	 * @param bitmap
	 *            传入的bitmap
	 * @return
	 */
	private Bitmap zoomBitmap(int bitW, int bitH, int requestW, int requestH,
			Bitmap bitmap) {
		Matrix m = new Matrix();
		if (bitW > requestW || bitH > requestH) {
			final double widthRatio = (float) bitW / (float) requestW;
			final double heightRatio = (float) bitH / (float) requestH;

			if (widthRatio > heightRatio) {// 图片是横的
				m.postScale((float) requestW / bitW, (float) requestW / bitW);
			} else {// 图片是竖着的
				m.postScale((float) requestH / bitH, (float) requestH / bitH);
			}
			System.out.println("size=" + "_" + widthRatio + "_" + heightRatio);
		}

		Bitmap b = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), m, true);
		return b;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.edit_frame:// 边框按钮
			titleTextView.setText(R.string.frames);
			onEditStates();
			
			eidtAdapter = new EditActivityAdapter(EditPhotoActivity.this,bitmap, pic,1);
			top_HorizontalListView.setAdapter(eidtAdapter);
			top_HorizontalListView
					.setOnItemClickListener(new OnItemClickListener() {
						// 推荐商品的点击效果，如果点击，则重载当前界面
						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {
							// TODO Auto-generated method stub
							currentpicposition = position;
							handler.sendEmptyMessage(LOADFRAME);
							// preview_save.setVisibility(View.VISIBLE);
						}
					});
			break;

		case R.id.edit_filter:// 滤镜按钮
			titleTextView.setText(R.string.magicbrush);
			onEditStates();
			curBitmap = ((BitmapDrawable) editImageView
					.getDrawable()).getBitmap();
			eidtAdapter = new EditActivityAdapter(EditPhotoActivity.this,bitmap, pic4,2);
			top_HorizontalListView.setAdapter(eidtAdapter);
			
			top_HorizontalListView
					.setOnItemClickListener(new OnItemClickListener() {
						// 推荐商品的点击效果，如果点击，则重载当前界面
						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {
							switch (position) {
							case 0:
								filter = new NormalFilter();
								
								break;
							case 1:
								filter = new LomoFilter();
								break;
							case 2:
								// 流年效果
								filter = new Amaro();
								break;
							case 3:
								// 自然美肤效果
								filter = new BeautifyFilter();
								break;
							case 4:
								// HDR 效果
								filter = new HDRFilter();
								break;
							 case 5:
							 // 自然美肤效果
								 filter = new BlurFilter();
							 break;
							case 6:
								// 怀旧效果
								filter = new OldFilter();
								break;
							default:
								break;
							}
						    test();
							isFilter = true;
						}
					});

			break;

		case R.id.edit_accessory:// 饰品按钮
			titleTextView.setText(R.string.decoration);
			onEditStates();

			eidtAdapter = new EditActivityAdapter(EditPhotoActivity.this,bitmap,pic3,
					3);
			top_HorizontalListView.setAdapter(eidtAdapter);
			top_HorizontalListView
					.setOnItemClickListener(new OnItemClickListener() {
						// 推荐商品的点击效果，如果点击，则重载当前界面
						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {
							TouchView imageView = new TouchView(
									EditPhotoActivity.this,
									ScreenUtil.getScreenWidth(EditPhotoActivity.this),
									ScreenUtil.getScreenHeight(EditPhotoActivity.this),
									((BitmapDrawable) editImageView.getDrawable()).getBitmap().getWidth(),
									((BitmapDrawable) editImageView.getDrawable()).getBitmap().getHeight(), fraWidth, fraHeight,
									edittoolsbar.getHeight(), handler);

							ViewGroup rootView = (ViewGroup) EditPhotoActivity.this
									.getWindow().getDecorView();

							LinearLayout accessory = new LinearLayout(
									EditPhotoActivity.this);
							LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
									LinearLayout.LayoutParams.MATCH_PARENT,
									LinearLayout.LayoutParams.MATCH_PARENT);
							accessory.setLayoutParams(lp);
							accessory
									.setBackgroundResource(android.R.color.transparent);
							rootView.addView(accessory);
							Bitmap accessoryBitmap = BitmapFactory
									.decodeResource(getResources(), Integer
											.valueOf(decorationlist
													.get(position)));
							imageView.setImageBitmap(accessoryBitmap);
							imageView.setClickable(true);

							if (imageViews.size() == 1) {
								// 移除 之前的一个饰品 并不显示。
								imageViews.get(0).setVisibility(View.GONE);
								imageViews.remove(0);
							} else {

							}
							imageViews.add(imageView);
							accessory.addView(imageView);
							accessory.setGravity(Gravity.CENTER);// 居中显示
							Log.e("imageViews size", "==:" + imageViews.size());

						}
					});

			break;

		case R.id.edit_rotate:// 旋转按钮，每次旋转后，都要更新饰品可移动范围
			fontImageView = new TouchView(EditPhotoActivity.this,
					ScreenUtil.getScreenWidth(EditPhotoActivity.this),
					ScreenUtil.getScreenHeight(EditPhotoActivity.this),
					((BitmapDrawable) editImageView.getDrawable()).getBitmap()
							.getWidth(),
					((BitmapDrawable) editImageView.getDrawable()).getBitmap()
							.getHeight(), fraWidth, fraHeight,
					edittoolsbar.getHeight(), handler);
			
			titleTextView.setText(R.string.rotate);
			onEditStates();
			// trends();
			top_HorizontalListView.setVisibility(View.GONE);
			rotate_bar.setVisibility(View.VISIBLE);
			fontEditText.setVisibility(View.VISIBLE);
			fontEditText.setText("");
			fontEditText.setFocusable(true);
			fontEditText.setFocusableInTouchMode(true);
			fontEditText.requestFocus();
			fontEditText.findFocus();
			fontImageView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					System.out.println("touch view on click");
					fontEditText.setFocusable(true);
					fontEditText.setFocusableInTouchMode(true);
					fontEditText.requestFocus();
					fontEditText.findFocus();
					fontEditText.setVisibility(View.VISIBLE);
					fontImageView.setVisibility(View.GONE);
				}
			});
			
			break;

		case R.id.preview_save:
		    
			bitmap = ((BitmapDrawable) editImageView.getDrawable()).getBitmap();

			FileOutputStream fileOutputStream3 = null;
			try {
				String urlString = nameFile + "/"
						+ dateFormat.format(new Date()) + ".JPG";
				System.out.println("------->"+urlString);
				fileOutputStream3 = new FileOutputStream(new File(urlString));// 创建一个新的文件
				BufferedOutputStream bos = new BufferedOutputStream(
						fileOutputStream3);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
				bos.flush();
				bos.close();
				fileOutputStream3.close();
				scan(urlString);
				
			} catch (IOException e) {
			}
			bitmap.recycle();
			// 清空 pictureAir/temppic 的内容
			deleteTempPic(Common.TEMPPIC_PATH);
			break;

		case R.id.edit_return:
			// 判断 是否 需要保存图片
			if (tempFile.exists() && tempFile.isDirectory()) {
				if (tempFile.list().length > 0) {
					// 提示是否需要保存图片。
					createIsSaveDialog();
				} else {
					finish();
				}
			} else {
				finish();
			}

			break;
		case R.id.btn_cancel:
			// 后退一步
			// init();
			
			if (index == -1) {
				index = pathList.size() - 1;
			}
			if (index >= 1) {
				index--;
			}

			if (pathList.size() - 2 >= 0) {
				
				if (index == 0) {
					showUrlPic(pathList.get(index));
				} else {
					loadTempPic(pathList.get(index));
				}
			}
			check();
			break;
		case R.id.btn_forward:
			if (index == -1) {

				index = pathList.size() - 1;
			}

			if (pathList.size() > index + 1) {
				index++;
				loadTempPic(pathList.get(index));
			}
			check();
			break;
		case R.id.btn_left_back:
			if (fontImageView != null) {
				fontImageView.setVisibility(View.GONE);
				fontImageView = null;
			}
			if (fontEditText != null) {
				fontEditText.setVisibility(View.GONE);
				setColorGridView.setVisibility(View.GONE);
				setFontGridView.setVisibility(View.GONE);
				super.onResume();
			}
			hideInputMethodManager(v);
			// 移除饰品。
			if (imageViews.size() > 0) {

				for (int i = 0; i < imageViews.size(); i++) {
					Log.e("i ==", "i == " + i);
					imageViews.get(i).setVisibility(View.GONE);
					imageViews.remove(i);
				}

			}
			// 移除边框
			if (frameImageView.isShown()) {
				frameImageView.setVisibility(View.INVISIBLE);
			}
			
			//如果加了滤镜
			if(isFilter){
				editImageView.setImageBitmap(curBitmap);
			}
			isFilter = false;

			check();
			// 回到上个步骤。
			if (pathList.size() > 1) {
				preview_save.setVisibility(View.VISIBLE);
			}
			rotate_bar.setVisibility(View.GONE);
			titleTextView.setVisibility(View.GONE);
			btn_onedit_save.setVisibility(View.GONE);
			btn_forward.setVisibility(View.VISIBLE);
			btn_cancel.setVisibility(View.VISIBLE);
			back.setVisibility(View.VISIBLE);
			btn_left_back.setVisibility(View.GONE);
			top_HorizontalListView.setVisibility(View.GONE);
			edittoolsbar.setVisibility(View.VISIBLE);
			break;
		case R.id.btn_onedit_save:
			// 保存临时文件 ， 并显示
			isFilter = false;
			if (fontImageView != null && fontEditText.getText().toString().isEmpty()) {
				myToast.setTextAndShow(R.string.add_new_text, Common.TOAST_SHORT_TIME);
				return;
			}
			degree = 0;
			bitmap = ((BitmapDrawable) editImageView.getDrawable()).getBitmap();
			hideInputMethodManager(v);//
			if (bitmap != null) {
				System.out.println("editImageView is not null");
				int photowidth = bitmap.getWidth();
				int photoheight = bitmap.getHeight();
				Bitmap heBitmap = Bitmap.createBitmap(photowidth, photoheight,
						Config.ARGB_8888);
				Canvas canvas = new Canvas(heBitmap);
				Paint point = new Paint();
				point.setXfermode(new PorterDuffXfermode(
						android.graphics.PorterDuff.Mode.SRC_OVER));
				// 将原图画在画布上
				Matrix matrix2 = new Matrix();
				canvas.drawBitmap(bitmap, matrix2, point);
				
				//合成  文字 与 图片  开始
				if (fontImageView != null && fontImageView.isShown()) {
					
					int x = fontImageView.getLeft();
					int y = fontImageView.getTop();
					Bitmap bitmap3 = ((BitmapDrawable) fontImageView.getDrawable())
							.getBitmap();
					int w = bitmap3.getWidth();
					int h = bitmap3.getHeight();
					int w2 = fontImageView.getWidth();
					int h2 = fontImageView.getHeight();
					matrix2.postScale((float) w2 / w, (float) h2 / h);
					bitmap3 = Bitmap.createBitmap(bitmap3, 0, 0, w, h, matrix2,
							true);// 将图片压缩到和边框一样
					
					canvas.drawBitmap(
							bitmap3,
							x - (fraWidth - bitmap.getWidth()) / 2,
							y
							- ScreenUtil.getScreenHeight(this)
							+ (fraHeight + edittoolsbar.getHeight() * 2 + bitmap
									.getHeight()) / 2, point);
					fontImageView.setVisibility(View.GONE);
					fontImageView = null;
					fontEditText.setVisibility(View.GONE);
					setFontGridView.setVisibility(View.GONE);
					setColorGridView.setVisibility(View.GONE);
				}
				
				//结束
				
				// 添加配件饰品图片
				for (int i = 0; i < imageViews.size(); i++) {
					// 获取imageview在屏幕上的坐标点
					int currentX = imageViews.get(i).getLeft();
					int currentY = imageViews.get(i).getTop();
					// 获取imageview的bitmap
					Bitmap bitmap2 = ((BitmapDrawable) imageViews.get(i)
							.getDrawable()).getBitmap();
					// 获取imageview原始图片的宽高
					int orignalw = bitmap2.getWidth();
					int orignalh = bitmap2.getHeight();
					// 获取imageview现在图片的宽高
					int currentw = imageViews.get(i).getWidth();
					int currenth = imageViews.get(i).getHeight();
					System.out.println("bitmap2 is not null" + orignalw + "_"
							+ orignalh + "_" + currentw + "_" + currenth);
					// 获取配件的原始宽高和当前宽高，配置matrix进行缩放
					matrix2.postScale((float) currentw / orignalw,
							(float) currenth / orignalh);
					bitmap2 = Bitmap.createBitmap(bitmap2, 0, 0, orignalw,
							orignalh, matrix2, true);// 将图片压缩到和边框一样

					canvas.drawBitmap(
							bitmap2,
							currentX - (fraWidth - bitmap.getWidth()) / 2,
							currentY
									- ScreenUtil.getScreenHeight(this)
									+ (fraHeight + edittoolsbar.getHeight() * 2 + bitmap
											.getHeight()) / 2, point);

					imageViews.get(i).setVisibility(View.GONE);

					bitmap2.recycle();
					matrix2.reset();
				}
				while (!imageViews.isEmpty()) {
					System.out.println("delete------");
					imageViews.remove(0);

				}
				bitmap.recycle();
				bitmap = null;
				if (frameImageView.isShown()) {

					// 添加边框图片
					Log.d("正在合成边框", "...");

					matrix2.postScale(
							(float) photowidth / (frameBitmap.getWidth()),
							(float) photoheight / (frameBitmap.getHeight()));
					frameBitmap = Bitmap.createBitmap(frameBitmap, 0, 0,
							frameBitmap.getWidth(), frameBitmap.getHeight(),
							matrix2, true);
					canvas.drawBitmap(frameBitmap, 0, 0, point);
					matrix2.reset();
					frameBitmap.recycle();
					frameImageView.setVisibility(View.INVISIBLE);
				} else {

				}
				if (null != heBitmap) {
					FileOutputStream fileOutputStream2 = null;
					try {
						String urlString = tempFile + "/"
								+ dateFormat.format(new Date()) + ".JPG";
						System.out.println(urlString);

						fileOutputStream2 = new FileOutputStream(new File(
								urlString));// 创建一个新的文件
						BufferedOutputStream bos = new BufferedOutputStream(
								fileOutputStream2);
						heBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
						bos.flush();
						bos.close();
						fileOutputStream2.close();
						// 显示处理过后的图片。
						Log.e("=== ", " urlString :" + urlString);
						// showUrlPic(urlString);
						pathList.add(urlString);
						loadFileFromSDCard(urlString, LOADIMAGEFROMSDCARD);
					} catch (IOException e) {
					}
				}
				heBitmap.recycle();
			}else {
				System.out.println("editImageView is null");
			}
			index = pathList.size() - 1;

			check();
			rotate_bar.setVisibility(View.GONE);
			titleTextView.setVisibility(View.GONE);
			preview_save.setVisibility(View.VISIBLE);
			btn_onedit_save.setVisibility(View.GONE);
			btn_forward.setVisibility(View.VISIBLE);
			btn_cancel.setVisibility(View.VISIBLE);
			back.setVisibility(View.VISIBLE);
			btn_left_back.setVisibility(View.GONE);
			top_HorizontalListView.setVisibility(View.GONE);
			edittoolsbar.setVisibility(View.VISIBLE);
			break;

		// 旋转
		case R.id.setTextFont:
			System.out.println("-----------> start init text");
			if (typeface2 == null) {
				typeface2 = Typeface.createFromAsset(getAssets(),
						"fonts/fangzheng.TTF");
			}
			if (typeface3 == null) {
				typeface3 = Typeface.createFromAsset(getAssets(),
						"fonts/gangbi.ttf");
			}
			if (typeface4 == null) {
				typeface4 = Typeface.createFromAsset(getAssets(), "fonts/keai.ttf");
			}
			if (typeface1 == null) {
				typeface1 = Typeface.SANS_SERIF;
			}

			strings.clear();
			strings.add(typeface1);
			strings.add(typeface2);
			strings.add(typeface3);
			strings.add(typeface4);
			
//					new Typeface[] { typeface1, typeface2,
//					typeface3, typeface4 };
				
			if (setColorGridView != null) {
				setColorGridView.setVisibility(View.GONE);
			}
			setFontGridView.setVisibility(View.VISIBLE);
			
			
			fontList.clear();
			for (int i = 0; i < fonts.length; i++) {
				fontList.add(fonts[i]);
			}
			fontAdapter = new FontAdapter(this, fontList, strings);
			setFontGridView.setAdapter(fontAdapter);
			setFontGridView.setNumColumns(fonts.length);
			setFontGridView.setColumnWidth(fraWidth / fonts.length);
			setFontGridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
			setFontGridView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					switch (position) {
					case 0:
						fontEditText.setTypeface(typeface1);
						break;
					case 1:
						fontEditText.setTypeface(typeface2);
						break;
					case 2:
						fontEditText.setTypeface(typeface3);
						break;
					case 3:
						fontEditText.setTypeface(typeface4);
						break;
					default:
						break;
					}

				}
			});
			System.out.println("------------> finish init text");
			break;

		case R.id.setColor:
			
			if (setFontGridView != null) {
				setFontGridView.setVisibility(View.GONE);
			}
			setColorGridView.setVisibility(View.VISIBLE);
			ArrayList<HashMap<String, Object>> colorList = new ArrayList<HashMap<String, Object>>();
			HashMap<String, Object> map;
			for (int i = 0; i < colors.length; i++) {
				map = new HashMap<String, Object>();
				map.put("colors", colors[i]);
				colorList.add(map);
			}

			simpleAdapter = new SimpleAdapter(EditPhotoActivity.this,
					colorList, R.layout.color_list, new String[] { "colors" },
					new int[] { R.id.color });
			setColorGridView.setAdapter(simpleAdapter);
			setColorGridView.setNumColumns(colors.length);
			setColorGridView.setColumnWidth(fraWidth / colors.length);
			setColorGridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
			setColorGridView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					switch (position) {
					case 0:
						fontEditText.setTextColor(fontEditText.getResources()
								.getColor(R.color.color1));
						fontEditText.setHintTextColor(getResources().getColor(R.color.color1));
						break;
					case 1:
						fontEditText.setTextColor(fontEditText.getResources()
								.getColor(R.color.color2));
						fontEditText.setHintTextColor(getResources().getColor(R.color.color2));
						break;
					case 2:
						fontEditText.setTextColor(fontEditText.getResources()
								.getColor(R.color.color3));
						fontEditText.setHintTextColor(getResources().getColor(R.color.color3));
						break;
					case 3:
						fontEditText.setTextColor(fontEditText.getResources()
								.getColor(R.color.color4));
						fontEditText.setHintTextColor(getResources().getColor(R.color.color4));
						break;
					case 4:
						fontEditText.setTextColor(fontEditText.getResources()
								.getColor(R.color.color5));
						fontEditText.setHintTextColor(getResources().getColor(R.color.color5));
						break;
					case 5:
						fontEditText.setTextColor(fontEditText.getResources()
								.getColor(R.color.color6));
						fontEditText.setHintTextColor(getResources().getColor(R.color.color6));
						break;
					case 6:
						fontEditText.setTextColor(fontEditText.getResources()
								.getColor(R.color.color7));
						fontEditText.setHintTextColor(getResources().getColor(R.color.color7));
						break;
					case 7:
						fontEditText.setTextColor(fontEditText.getResources()
								.getColor(R.color.color8));
						fontEditText.setHintTextColor(getResources().getColor(R.color.color8));
						break;
					case 8:
						fontEditText.setTextColor(fontEditText.getResources()
								.getColor(R.color.color9));
						fontEditText.setHintTextColor(getResources().getColor(R.color.color9));
						break;
					case 9:
						fontEditText.setTextColor(fontEditText.getResources()
								.getColor(R.color.color10));
						fontEditText.setHintTextColor(getResources().getColor(R.color.color10));
						break;
					case 10:
						fontEditText.setTextColor(fontEditText.getResources()
								.getColor(R.color.color11));
						fontEditText.setHintTextColor(getResources().getColor(R.color.color11));
						break;
					case 11:
						fontEditText.setTextColor(fontEditText.getResources()
								.getColor(R.color.color12));
						fontEditText.setHintTextColor(getResources().getColor(R.color.color12));
						break;
					case 12:
						fontEditText.setTextColor(fontEditText.getResources()
								.getColor(R.color.color13));
						fontEditText.setHintTextColor(getResources().getColor(R.color.color13));
						break;
					case 13:
						fontEditText.setTextColor(fontEditText.getResources()
								.getColor(R.color.color14));
						fontEditText.setHintTextColor(getResources().getColor(R.color.color14));
						break;
					case 14:
						fontEditText.setTextColor(fontEditText.getResources()
								.getColor(R.color.color15));
						fontEditText.setHintTextColor(getResources().getColor(R.color.color15));
						break;
					case 15:
						fontEditText.setTextColor(fontEditText.getResources()
								.getColor(R.color.color16));
						fontEditText.setHintTextColor(getResources().getColor(R.color.color16));
						break;
					default:
						break;
					}
				}
			});
			
			
			break;

		default:
			break;
		}
	}

	/**
	 * 加载边框函数
	 * 
	 * @param positon
	 *            选择边框的索引值
	 * @param bitmapwidth
	 *            需要匹配的图片宽
	 * @param bitmapheight
	 *            需要匹配的图片高
	 */
	private void loadframe(int positon, int bitmapwidth, int bitmapheight) {
		if (positon != 0) {// 如果不为0，表示有边框
			Log.d("loadframe", bitmapwidth + "_" + bitmapheight);
			// 加载预览边框
			frameBitmap = BitmapFactory.decodeResource(getResources(),
					pic2[positon]);
			Matrix m = new Matrix();
			// 缩放尺寸，保持和边框一致
			m.postScale((float) bitmapwidth / (frameBitmap.getWidth()),
					(float) bitmapheight / (frameBitmap.getHeight()));

			frameBitmap = Bitmap.createBitmap(frameBitmap, 0, 0,
					frameBitmap.getWidth(), frameBitmap.getHeight(), m, true);
			frameImageView.setImageBitmap(frameBitmap);
			frameImageView.setVisibility(View.VISIBLE);
		} else {// 没有边框
			frameImageView.setVisibility(View.INVISIBLE);
		}

	}

	// 扫描SD卡
	private void scan(final String file) {
		// TODO Auto-generated method stub
		MediaScannerConnection.scanFile(this, new String[] { file }, null,
				new MediaScannerConnection.OnScanCompletedListener() {
					public void onScanCompleted(String path, Uri uri) {
						editor = sharedPreferences.edit();
						editor.putString(Common.LAST_PHOTO_URL, file);
						editor.commit();
						// 可以添加一些返回的数据过去，还有扫描最好放在返回去之后。
						Intent intent = new Intent();
						intent.putExtra("photoUrl", file);
						setResult(11, intent);
						System.out.println("set result--------->");
						finish();
					}
				});
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		deleteTempPic(Common.TEMPPIC_PATH);
		if (bitmap != null) {
			bitmap.recycle();
			bitmap = null;
		}
	}

	// 进入编辑某个效果的状态
	private void onEditStates() {
		titleTextView.setVisibility(View.VISIBLE);
		preview_save.setVisibility(View.GONE);
		btn_onedit_save.setVisibility(View.VISIBLE);
		btn_forward.setVisibility(View.GONE);
		btn_cancel.setVisibility(View.GONE);
		back.setVisibility(View.GONE);
		btn_left_back.setVisibility(View.VISIBLE);
		top_HorizontalListView.setVisibility(View.VISIBLE);
		edittoolsbar.setVisibility(View.INVISIBLE);
	}

	// 显示 指定 路径的图片
	private void showUrlPic(String path) {
		if (photoInfo.onLine == 1) {// 网络图片
			System.out.println("editPhoto=" + "pp");
			/*
			 * 需要使用三级缓存，1.判断SD卡是否存在，2.判断缓存中是否存在，3.从网上下载
			 */
			// 1.获取需要显示文件的文件名
			String fileString = ScreenUtil.getReallyFileName(path);
			// 2、判断文件是否存在sd卡中
			File file = new File(Common.PHOTO_DOWNLOAD_PATH + fileString);
			if (file.exists()) {// 3、如果存在SD卡，则从SD卡获取图片信息
				System.out.println("file exists");
				loadFileFromSDCard(file.toString(), LOADIMAGEFROMSDCARD);

			} else {// 4、如果SD卡不存在，判断是否在缓存中
				System.out.println("file not exists");
				// 获取在缓存中的文件的名字
				final File dirfile = new File(this.getCacheDir() + "/"
						+ photoInfo.photoId+"_ori");
				System.out.println("dirfile = " + dirfile.toString());
				if (dirfile.exists()) {// 5、如果缓存存在，则从缓存中获取图片信息
					System.out.println("cache exists");
					loadFileFromSDCard(dirfile.toString(), LOADIMAGEFROMCACHE);

				} else {// 6.如果缓存不存在，从网络获取图片信息，
					System.out.println("cache not exist");
					HttpUtil.get(path, new BinaryHttpResponseHandler() {
						@Override
						public void onSuccess(int arg0, Header[] arg1,
								byte[] arg2) {
							// TODO Auto-generated method stub
							bitmap = BitmapFactory.decodeByteArray(arg2, 0,
									arg2.length);
							fraWidth = ScreenUtil
									.getScreenWidth(EditPhotoActivity.this);
							fraHeight = preframe.getHeight();
							bitmap = zoomBitmap(bitmap.getWidth(),
									bitmap.getHeight(), fraWidth, fraHeight,
									bitmap);
							if (bitmap != null) {
								editImageView.setImageBitmap(bitmap);
							}

							// 7.将网络获取的图片信息存放到缓存
							BufferedOutputStream stream = null;
							try {
								System.out.println(dirfile.toString());
								FileOutputStream fsStream = new FileOutputStream(
										dirfile);
								stream = new BufferedOutputStream(fsStream);
								stream.write(arg2);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} finally {
								try {
									if (stream != null) {
										stream.flush();
										stream.close();
									}
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								System.out.println("done");
							}
						}

						@Override
						public void onFailure(int arg0, Header[] arg1,
								byte[] arg2, Throwable arg3) {
							System.out.println(arg3.toString());
						}

					});
				}
			}

		} else {// 本地图片
			System.out.println("editPhoto=" + "not pp");
			loadFileFromSDCard(path, LOADIMAGEFROMSDCARD);
		}
	}

	// 清空文件夹中的 所有文件。
	private void deleteTempPic(String path) {
		File file = new File(path);
		DeleteFile(file);
	}

	private void DeleteFile(File file) {
		if (file.exists() == false) {
			return;
		} else {
			if (file.isFile()) {
				file.delete();
				return;
			}
			if (file.isDirectory()) {
				File[] childFile = file.listFiles();
				if (childFile == null || childFile.length == 0) {
					file.delete();
					return;
				}
				for (File f : childFile) {
					DeleteFile(f);
				}
				// file.delete();
			}
		}
	}

	// 判断是否可以点击。
	private void check() {
		Log.e("index ", "index :" + index);
		if (index == -1) {
			index = pathList.size() - 1;
		}

		if (index == pathList.size() - 1) {
			btn_forward.setImageResource(R.drawable.forward1);
		} else {
			btn_forward.setImageResource(R.drawable.forward);
		}
		if (index == 0) {
			btn_cancel.setImageResource(R.drawable.cancel1);
		} else {
			btn_cancel.setImageResource(R.drawable.cancel);
		}
	}

	// 没有保存的时候的对话框
	private void createIsSaveDialog() {
		AlertDialog.Builder builder = new Builder(EditPhotoActivity.this);
		builder.setMessage("是否保存？");
		builder.setTitle("提示");
		builder.setPositiveButton("是", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				bitmap = ((BitmapDrawable) editImageView.getDrawable())
						.getBitmap();

				FileOutputStream fileOutputStream3 = null;
				try {
					String urlString = nameFile + "/"
							+ dateFormat.format(new Date()) + ".JPG";
					System.out.println(urlString);
					fileOutputStream3 = new FileOutputStream(
							new File(urlString));// 创建一个新的文件
					BufferedOutputStream bos = new BufferedOutputStream(
							fileOutputStream3);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
					bos.flush();
					bos.close();
					fileOutputStream3.close();
					scan(urlString);
				} catch (IOException e) {
				}
				bitmap.recycle();
				// 清空 pictureAir/temppic 的内容
				deleteTempPic(Common.TEMPPIC_PATH);
			}
		});
		builder.setNegativeButton("否", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		builder.create().show();
	}

	
	private void test(){
		 
		 editImageView.setWillNotDraw(true);
         Thread thread = new Thread(){
             public void run() {
            	
            	 if(filter instanceof LomoFi){
            		 newImage = ((LomoFi) filter).transform(curBitmap);
            	 }else if(filter instanceof EarlyBird){
            		 newImage = ((EarlyBird) filter).transform(curBitmap,getResources());
            	 }else if(filter instanceof Amaro){
            		 newImage = ((Amaro) filter).transform(curBitmap);
            	 }else if(filter instanceof NormalFilter){
            		 newImage = ((NormalFilter) filter).transform(curBitmap);
            	 }else if(filter instanceof LomoFilter){
            		 newImage = ((LomoFilter) filter).transform(curBitmap);
            	 }else if(filter instanceof BeautifyFilter){
            		 newImage = ((BeautifyFilter) filter).transform(curBitmap);
            	 }else if(filter instanceof HDRFilter){
            		 newImage = ((HDRFilter) filter).transform(curBitmap);
            	 }else if(filter instanceof OldFilter){
            		 newImage = ((OldFilter) filter).transform(curBitmap);
            	 }else if(filter instanceof BlurFilter){
            		 newImage = ((BlurFilter) filter).transform(curBitmap);
            	 }
                
                 EditPhotoActivity.this.runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                     	editImageView.setImageBitmap(newImage);
                     	editImageView.setWillNotDraw(false);
                     	editImageView.postInvalidate();
                     }
                 });
             }
         };
         thread.setDaemon(true);
         thread.start();
	}
	
	public void loadTempPic(String urlString){
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(urlString, options);
		int width = options.outWidth;
		int height = options.outHeight;
		bitmap = BitmapFactory.decodeFile(urlString);
		bitmap = zoomBitmap(width, height, fraWidth, fraHeight, bitmap);
		if (bitmap != null) {
			if(AppUtil.getExifOrientation(urlString)!=0){
				bitmap = AppUtil.rotaingImageView(AppUtil.getExifOrientation(urlString),bitmap);
			}
			editImageView.setImageBitmap(bitmap);
		}
	}
	
}
