package com.pictureair.photopass.activity;


import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.EditActivityAdapter;
import com.pictureair.photopass.adapter.FontAdapter;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.editPhoto.BitmapUtils;
import com.pictureair.photopass.editPhoto.EditPhotoUtil;
import com.pictureair.photopass.editPhoto.FontItem;
import com.pictureair.photopass.editPhoto.FontView;
import com.pictureair.photopass.editPhoto.Matrix3;
import com.pictureair.photopass.editPhoto.StickerItem;
import com.pictureair.photopass.editPhoto.StickerView;
import com.pictureair.photopass.entity.DiscoverLocationItemInfo;
import com.pictureair.photopass.entity.FrameOrStikerInfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.filter.Amaro;
import com.pictureair.photopass.filter.BeautifyFilter;
import com.pictureair.photopass.filter.BlurFilter;
import com.pictureair.photopass.filter.EarlyBird;
import com.pictureair.photopass.filter.Filter;
import com.pictureair.photopass.filter.HDRFilter;
import com.pictureair.photopass.filter.LomoFi;
import com.pictureair.photopass.filter.LomoFilter;
import com.pictureair.photopass.filter.NormalFilter;
import com.pictureair.photopass.filter.OldFilter;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.API;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.JsonUtil;
import com.pictureair.photopass.util.LocationUtil;
import com.pictureair.photopass.util.LocationUtil.LocationNotification;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.FontEditDialog;
import com.pictureair.photopass.widget.HorizontalListView;
import com.pictureair.photopass.widget.MyToast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

//显示的时候用压缩过的bitmap，合成的时候，用原始的bitmap
public class EditPhotoActivity extends BaseActivity implements OnClickListener, LocationNotification{
	//视图
	public StickerView mStickerView;// 贴图层View
	public FontView fontView; //文字view
	public Bitmap mainBitmap; //低层显示的bitmap，就是编辑的图片。
	public ImageView mainImage; // 原始图

	private ImageLoader imageLoader;
	private DisplayImageOptions options;

	private ImageView back,btn_left_back; //结束当前页面的back, 取消操作。
	private TextView edit_accessory,titleTextView,preview_save,edit_filter,edit_text,edit_frame;//饰品按钮，标题，保存,滤镜按钮, 文字按钮,边框按钮
	private ImageButton btn_onedit_save,btn_cancel,btn_forward;//保存，返回，前进
	private LinearLayout edittoolsbar,font_bar; // 工具条 和 底部的 旋转 bar
	private RelativeLayout titleBarRelativeLayout;

	private HorizontalListView top_HorizontalListView;  //显示饰品的滑动条
	private FontEditDialog fontEditdialog; //填写字体的dialog

	private CustomProgressDialog dialog;
	private MyToast myToast;

	//适配器
	private EditActivityAdapter eidtAdapter; //通用的适配器
	//对象
	private PhotoInfo photoInfo;
	private String photoURL;

//	private String ssss;

	private Typeface typeface1;
	private Typeface typeface2;
	private Typeface typeface3;
	private Typeface typeface4;
	private FontAdapter fontAdapter;

	private List<Typeface> strings;
	private List<String> fontList;
	//
	private LoadImageTask mLoadImageTask;

	private int imageWidth, imageHeight;// 展示图片控件 宽 高
	//
//	private List<String> stickerPathList = new ArrayList<String>();// 饰品图片路径列表
	private List<String> filterPathList = new ArrayList<String>(); //滤镜图片的路径列表
	//	private List<String> framePathList = new ArrayList<String>(); //相框的缩略图路径。

	private ArrayList<FrameOrStikerInfo> frameInfos = new ArrayList<FrameOrStikerInfo>(); //保存 高清边框的集合。
	private ArrayList<FrameOrStikerInfo> frameFromDBInfos;//来自数据库的数据
	private ArrayList<FrameOrStikerInfo> stikerInfos = new ArrayList<FrameOrStikerInfo>();// 饰品图片路径列表
	private ArrayList<FrameOrStikerInfo> stickerFromDBInfos;//来自数据库的数据
	private ArrayList<DiscoverLocationItemInfo> locationItemInfos;

	public final String STICKERPATH = "sticker";
	public final String FILTERPATH = "";

	private File nameFile; //保存文件的目录
	private File tempFile; //保存文件的临时目录
	private SimpleDateFormat dateFormat;
	// 保存图片路径的集合。
	private ArrayList<String> pathList;
	private int index = -1; // 索引。   控制图片步骤 前进后退。

	private SharedPreferences sharedPreferences;
	private SharedPreferences appPreferences;
	private Editor editor;

	private boolean isOnlinePic = false;
	private Filter filter;
	private Bitmap newImage; // 滤镜处理过的bitmap

	private int editType = 0; //编辑 类型的类别。 0 默认值，1，代表边框，2，代表 滤镜， 3 ，代表饰品，4 代表字体。
	private long time = 0;

	//有关 文字
	private TextView setTextFont,setColor;  //设置字体，设置颜色
	private String fonts[] = null;
	private SimpleAdapter simpleAdapter;
	private String showText;
	private Typeface showTypeface = Typeface.SANS_SERIF;
	private float curTextSize;
	private int showColor = R.color.color16;
	private int colors[] = new int[] { R.drawable.color1, R.drawable.color2,
			R.drawable.color3, R.drawable.color4, R.drawable.color5,
			R.drawable.color6, R.drawable.color7, R.drawable.color8,
			R.drawable.color9, R.drawable.color10, R.drawable.color11,
			R.drawable.color12, R.drawable.color13, R.drawable.color14,
			R.drawable.color15, R.drawable.color16 }; // 显示的颜色图片
	private int color[] = new int[] {
			R.color.color1,R.color.color2,R.color.color3,R.color.color4,R.color.color5,R.color.color6,R.color.color7,R.color.color8,R.color.color9,R.color.color10,R.color.color11,R.color.color12,
			R.color.color13,R.color.color14,R.color.color15,R.color.color16
	}; //实际上的 color 值。
	private GridView setFontGridView; //字体
	private GridView setColorGridView; //文本颜色

	//	有关相框
	private ImageView frameImageView;

	private int curFramePosition = 0;

	private PictureAirDbManager pictureAirDbManager;
	private LocationUtil locationUtil;

	private static final String TAG = "EditPhotoActivity";

	private static final int INIT_DATA_FINISHED = 104;
	private static final int LOAD_IMAGE_FINISH = 103;

	private boolean loadingFrame = false;

	private int textWidth;

	//绘制 真是图片显示区域。 控制 饰品 与文字拖动范围
	public static float leftTopX;
	public static float leftTopY;
	public static float rightBottomX;
	public static float rightBottomY;
	int displayBitmapWidth = 0;
	int displayBitmapHeight = 0;
	//end

	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
				case LOAD_IMAGE_FINISH:
					if (dialog.isShowing()) {
						dialog.dismiss();
					}
					break;

				case INIT_DATA_FINISHED:
					if (dialog.isShowing()) {
						dialog.dismiss();
					}
					//开始从网络获取最新数据
					API.getLastContent(appPreferences.getString(Common.GET_LAST_CONTENT_TIME, null), handler);
					break;

				case 0000:
					String text = msg.obj.toString();
					if (text.equals("")) {
						fontEditdialog.dismiss();
					}else{
						showText = text;
						curTextSize = ScreenUtil.getScreenWidth(EditPhotoActivity.this)/2/(showText.length());
						if (showText.length() >= 8) {
							textWidth = ScreenUtil.getScreenWidth(EditPhotoActivity.this);
							curTextSize = ScreenUtil.getScreenWidth(EditPhotoActivity.this)/(showText.length());
						}else if (showText.length() > 20) {
							// 后期 ：  如果字数过长，提示不能输入过长字符
						}
						fontView.addBitImage(EditPhotoUtil.textAsBitmap(text,showTypeface,showColor,curTextSize, textWidth,EditPhotoActivity.this));
						fontEditdialog.dismiss();
					}
					break;

				case 3333:
					fontEditdialog.dismiss();
					break;

				case 1111:
					btn_onedit_save.setVisibility(View.VISIBLE);
					dialog = CustomProgressDialog.show(EditPhotoActivity.this, getString(R.string.dealing), false, null);
					editType = 1;
					curFramePosition = msg.arg1;
					loadframe(curFramePosition);
					break;

				case API.GET_LAST_CONTENT_SUCCESS://获取更新包成功
					Log.d(TAG, "get lastest info success" + msg.obj);
					try {
						com.alibaba.fastjson.JSONObject resultJsonObject = com.alibaba.fastjson.JSONObject.parseObject(msg.obj.toString()) ;
						if (resultJsonObject.containsKey("assets")) {

							pictureAirDbManager.insertFrameAndStickerIntoDB(resultJsonObject.getJSONObject("assets"));

//						if (assetsObject.has("frames")) {
//							JSONArray framesArray = assetsObject.getJSONArray("frames");
//							if (framesArray.length() > 0 ) {
//								Log.d(TAG, "frames length is " + framesArray.length());
//								//开始解析数据，并且将数据写入数据库
//								pictureAirDbManager.insertFrameAndStickerIntoDB(framesArray);
//							}else {
//								Log.d(TAG, "has no any frames");
//							}
//						}
//						if (assetsObject.has("cliparts")) {
//							JSONArray stickersArray = assetsObject.getJSONArray("cliparts");
//							if (stickersArray.length() > 0) {
//								Log.d(TAG, "stickers length is " + stickersArray.length());
//							}else {
//								Log.d(TAG, "has no any stickers");
//							}
//						}

						}

						if (resultJsonObject.containsKey("time")) {
							Log.d(TAG, "lastest time is "+ resultJsonObject.getString("time"));
							Editor editor = appPreferences.edit();
							editor.putString(Common.GET_LAST_CONTENT_TIME, resultJsonObject.getString("time"));
							editor.commit();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					//写入数据库之后，再从数据库拿数据
					frameFromDBInfos = pictureAirDbManager.getLastContentDataFromDB(1);
					for (int i = 0; i < frameFromDBInfos.size(); i++) {
						if (frameFromDBInfos.get(i).locationId.equals("common")) {//通用边框
							frameInfos.add(frameFromDBInfos.get(i));
						}
					}
					//从数据库获取饰品信息
					stickerFromDBInfos = pictureAirDbManager.getLastContentDataFromDB(0);
					for (int j = 0; j < stickerFromDBInfos.size(); j++) {
						if (stickerFromDBInfos.get(j).locationId.equals("common")) {//通用饰品
							stikerInfos.add(stickerFromDBInfos.get(j));
						}
					}
//				frameInfos.addAll(frameFromDBInfos);

					break;

				case API.GET_LAST_CONTENT_FAILED://获取更新包失败

					break;

				default:
					break;
			}
			super.handleMessage(msg);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_photo);

		textWidth = ScreenUtil.getScreenWidth(this)/2;
		initView();
		dialog = CustomProgressDialog.show(EditPhotoActivity.this, getString(R.string.is_loading), false, null);
		new Thread() {
			public void run() {
				//		    addStickerImages(STICKERPATH); //获取资源文件的  饰品   加载饰品资源
				initDate();
			};
		}.start();

		//		initDate();
	}


	private void initView(){
		myToast = new MyToast(this);
		showText = this.getResources().getString(R.string.add_text);
		locationUtil = new LocationUtil(this);
		locationItemInfos = new ArrayList<DiscoverLocationItemInfo>();
		mainImage = (ImageView) findViewById(R.id.main_image);
		// 贴图  view
		mStickerView = (StickerView) findViewById(R.id.sticker_panel);
		fontView = (FontView) findViewById(R.id.font_panel);

		frameImageView = (ImageView) findViewById(R.id.framephoto_imageView1); // 相框
		titleBarRelativeLayout = (RelativeLayout)findViewById(R.id.preview_titlebar);

		back = (ImageView) findViewById(R.id.edit_return);
		edit_accessory = (TextView) findViewById(R.id.edit_accessory);
		titleTextView = (TextView) findViewById(R.id.title_edit);
		edittoolsbar = (LinearLayout) findViewById(R.id.edittoolsbar);
		top_HorizontalListView = (HorizontalListView) findViewById(R.id.horizontalListView);
		preview_save = (TextView) findViewById(R.id.preview_save);
		btn_onedit_save = (ImageButton) findViewById(R.id.btn_onedit_save);
		btn_cancel = (ImageButton) findViewById(R.id.btn_cancel);
		btn_forward = (ImageButton) findViewById(R.id.btn_forward);
		btn_left_back = (ImageView) findViewById(R.id.btn_left_back);
		edit_filter = (TextView) findViewById(R.id.edit_filter);
		edit_text = (TextView) findViewById(R.id.edit_text);
		edit_frame = (TextView) findViewById(R.id.edit_frame);
		font_bar = (LinearLayout) findViewById(R.id.font_bar);
		setFontGridView = (GridView) findViewById(R.id.fontList);
		setTextFont = (TextView) findViewById(R.id.setTextFont);
		setColor = (TextView) findViewById(R.id.setColor);
		setColorGridView = (GridView) findViewById(R.id.colorList);


		edit_frame.setOnClickListener(this);
		setColor.setOnClickListener(this);
		setTextFont.setOnClickListener(this);
		edit_text.setOnClickListener(this);
		edit_filter.setOnClickListener(this);
		btn_forward.setOnClickListener(this);
		btn_cancel.setOnClickListener(this);
		preview_save.setOnClickListener(this);
		btn_onedit_save.setOnClickListener(this);
		btn_left_back.setOnClickListener(this);
		edit_accessory.setOnClickListener(this);
		back.setOnClickListener(this);
	}

	private void initDate(){
		nameFile = new File(Common.PHOTO_SAVE_PATH);

		if (!nameFile.isDirectory()) {
			nameFile.mkdirs();// 创建根目录文件夹
		}
		EditPhotoUtil.deleteTempPic(Common.TEMPPIC_PATH); //每次进入清空temp文件夹。

		tempFile = new File(Common.TEMPPIC_PATH);
		if (!tempFile.isDirectory()) {
			tempFile.mkdirs();// 创建根目录文件夹
		}
		imageLoader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder().cacheInMemory(true).build();
		pictureAirDbManager = new PictureAirDbManager(this);

		filterPathList.add("filter/original.png");
		filterPathList.add("filter/filter1.png");
		filterPathList.add("filter/filter2.png");
		filterPathList.add("filter/filter3.png");
		filterPathList.add("filter/filter4.png");
		filterPathList.add("filter/filter5.png");
		filterPathList.add("filter/filter6.png");

		FrameOrStikerInfo frameInfo = new FrameOrStikerInfo();
		frameInfo.frameThumbnailPath160 = Scheme.ASSETS.wrap("frame/frame_none.png");
		frameInfo.frameOriginalPathLandscape = Scheme.ASSETS.wrap("frame/frame_none.png");
		frameInfo.frameOriginalPathPortrait = Scheme.ASSETS.wrap("frame/frame_none.png");
		frameInfos.add(frameInfo);

		frameInfo = new FrameOrStikerInfo();
		frameInfo.frameThumbnailPath160 = Scheme.ASSETS.wrap("frame/frame_t_1.png");
		frameInfo.frameOriginalPathLandscape = Scheme.ASSETS.wrap("frame/frame_o_1.png");
		frameInfo.frameOriginalPathPortrait = Scheme.ASSETS.wrap("frame/frame_v_1.png");
		frameInfos.add(frameInfo);

		frameInfo = new FrameOrStikerInfo();
		frameInfo.frameThumbnailPath160 = Scheme.ASSETS.wrap("frame/frame_t_2.png");
		frameInfo.frameOriginalPathLandscape = Scheme.ASSETS.wrap("frame/frame_o_2.png");
		frameInfo.frameOriginalPathPortrait = Scheme.ASSETS.wrap("frame/frame_v_2.png");
		frameInfos.add(frameInfo);

		frameInfo = new FrameOrStikerInfo();
		frameInfo.frameThumbnailPath160 = Scheme.ASSETS.wrap("frame/frame_t_3.png");
		frameInfo.frameOriginalPathLandscape = Scheme.ASSETS.wrap("frame/frame_o_3.png");
		frameInfo.frameOriginalPathPortrait = Scheme.ASSETS.wrap("frame/frame_v_3.png");
		frameInfos.add(frameInfo);

		addStickerImages(STICKERPATH); //获取资源文件的  饰品   加载饰品资源

		if (typeface2 == null) {
			System.out.println("-----------> load typeface2");
			typeface2 = Typeface.createFromAsset(getAssets(),
					"fonts/fangzheng.ttf");
		} else {
			System.out.println("---------> need not load typeface2");
		}
		if (typeface3 == null) {
			typeface3 = Typeface.createFromAsset(getAssets(),
					"fonts/gangbi.ttf");
		}
		if (typeface4 == null) {
			typeface4 = Typeface.createFromAsset(getAssets(),
					"fonts/keai.ttf");
		}
		if (typeface1 == null) {
			typeface1 = Typeface.SANS_SERIF;
		}

		dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");

		sharedPreferences = getSharedPreferences("pictureAir", MODE_PRIVATE);
		appPreferences = getSharedPreferences(Common.APP, MODE_PRIVATE);

		pathList = new ArrayList<String>();
		strings = new ArrayList<Typeface>();
		fontList = new ArrayList<String>();

		fonts =  new String[] { getString(R.string.font_default),
				getString(R.string.font_fangzheng),
				getString(R.string.font_pen),
				getString(R.string.font_lovely) };

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		//此处的宽高，决定以后保存图片的速度与图片的效率。
//		imageWidth = (int) ((float) metrics.widthPixels / 1.5);
//		imageHeight = (int) ((float) metrics.heightPixels / 1.5);
		imageWidth = 900;
		imageHeight = 1200;

		photoInfo = getIntent().getParcelableExtra("photo");
		photoURL = photoInfo.photoPathOrURL;
		if (photoInfo.onLine == 1) {
			//网络图片。
			isOnlinePic = true;
			loadOnlineImg(photoURL);
		}else{
			//本地图片
			isOnlinePic = false;
			loadImage(photoURL);
		}
		pathList.add(photoURL);

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (locationItemInfos.size() == 0) {//说明不存在，需要获取所有的location地点信息
			try {
				com.alibaba.fastjson.JSONObject response = com.alibaba.fastjson.JSONObject.parseObject(ACache.get(this).getAsString(Common.LOCATION_INFO));
				com.alibaba.fastjson.JSONArray resultArray = response.getJSONArray("locations");
				for (int i = 0; i < resultArray.size(); i++) {

					DiscoverLocationItemInfo locationInfo = new DiscoverLocationItemInfo();
					JSONObject object = resultArray.getJSONObject(i);
					locationInfo = JsonUtil.getLocation(object);
					locationItemInfos.add(locationInfo);
				}
				locationUtil.setLocationItemInfos(locationItemInfos, this);
			} catch (com.alibaba.fastjson.JSONException e1) {

				e1.printStackTrace();
			}
		}
		locationUtil.startLocation();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		locationUtil.stopLocation();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
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
			case R.id.btn_left_back:
				//如果添加了边框。让边框消失。
				if (editType == 1) {
					if (frameImageView.isShown()) {
						frameImageView.setVisibility(View.INVISIBLE);
					}
				}

				//如果有饰品，饰品消失。
				if (editType == 3) {
					if (mStickerView.isShown()) {
						mStickerView.setVisibility(View.GONE);
						mStickerView.clear();
					}
				}

				//如果添加了滤镜。
				if(editType == 2){
					if (newImage!=null) {
						newImage = null;
//					newImage.recycle();
					}
					mainImage.setImageBitmap(mainBitmap);
				}

				//如果添加了字体
				if (editType == 4) {
					showText = getString(R.string.add_text);
					showColor = R.color.color16;
					showTypeface = Typeface.SANS_SERIF;
					if (fontView.isShown()) {
						fontView.setVisibility(View.GONE);
						fontView.clear();
					}
				}

				exitEditStates(); // 推出编辑状态
				break;

			//编辑边框。
			case R.id.edit_frame:
//			
//			System.out.println(ssss.equals("---"));
//			

				titleTextView.setText(R.string.frames);
				onEditStates();
				eidtAdapter = new EditActivityAdapter(EditPhotoActivity.this,mainBitmap, new ArrayList<String>(),1, frameInfos, handler);
				top_HorizontalListView.setAdapter(eidtAdapter);
				top_HorizontalListView.setOnItemClickListener(null);
//			top_HorizontalListView
//			.setOnItemClickListener(new OnItemClickListener() {
//				//加载边框。
//				@Override
//				public void onItemClick(AdapterView<?> parent,
//						View view, int position, long id) {
//					// TODO Auto-generated method stub
//					curFramePosition = position;
//					//							loadframe(position);
//					editType = 1;
//					dialog = CustomProgressDialog.show(EditPhotoActivity.this, getString(R.string.dealing), false, null);
//					dialog.show();
//					Message msg = handler.obtainMessage();
//					msg.obj = position;
//					msg.what = 1111;
//					handler.sendMessage(msg);
//				}
//			});
				break;
			case R.id.edit_filter:
				onEditStates();
				titleTextView.setText(R.string.magicbrush);
				eidtAdapter = new EditActivityAdapter(EditPhotoActivity.this, mainBitmap, filterPathList, 2, new ArrayList<FrameOrStikerInfo>(), handler);
				top_HorizontalListView.setAdapter(eidtAdapter);
				top_HorizontalListView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
											int position, long arg3) {
						// TODO Auto-generated method stub
						btn_onedit_save.setVisibility(View.VISIBLE);
						editType = 2;
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
						ExcuteFilterTask excuteFilterTask = new ExcuteFilterTask();
						excuteFilterTask.execute(mainBitmap);
					}
				});
				break;
			case R.id.edit_text:
				// 获取 原始图片的模型。
				calRec();
				btn_onedit_save.setVisibility(View.VISIBLE);
				fontView.setVisibility(View.VISIBLE);   //设置了可见
				editType = 4;
				titleTextView.setText(R.string.rotate);
				onEditStates();
				top_HorizontalListView.setVisibility(View.GONE);
				font_bar.setVisibility(View.VISIBLE);

				curTextSize = ScreenUtil.getScreenWidth(EditPhotoActivity.this)/2/(showText.length());
				textWidth = ScreenUtil.getScreenWidth(this)/2;
				fontView.addBitImage(EditPhotoUtil.textAsBitmap(showText,showTypeface ,showColor,curTextSize,textWidth,EditPhotoActivity.this));
				fontView.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						// TODO Auto-generated method stub

						if (event.getAction() == MotionEvent.ACTION_DOWN) {
							time = System.currentTimeMillis();
						}
						if (event.getAction() == MotionEvent.ACTION_UP) {
							if (System.currentTimeMillis() - time < 200) {
								fontEditdialog = new FontEditDialog(EditPhotoActivity.this,showText,handler);
								fontEditdialog.getWindow().setGravity(Gravity.BOTTOM);
								fontEditdialog.setCanceledOnTouchOutside(true);
								fontEditdialog.setCancelable(true);
								fontEditdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
								Window win = fontEditdialog.getWindow();
								win.getDecorView().setPadding(0, 0, 0, 0);
								WindowManager.LayoutParams lp = win.getAttributes();
								lp.width = WindowManager.LayoutParams.MATCH_PARENT;
								lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
								win.setAttributes(lp);
								fontEditdialog.show();
							}
						}
						return false;
					}
				});

				break;
			case R.id.setTextFont:
				System.out.println("-----------> start init text");
				if (typeface2 == null) {
					typeface2 = Typeface.createFromAsset(getAssets(),
							"fonts/fangzheng.ttf");
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
				if (fontAdapter == null) {
					fontAdapter = new FontAdapter(this, fontList, strings);
				}
				setFontGridView.setAdapter(fontAdapter);
				setFontGridView.setNumColumns(fonts.length);
				setFontGridView.setColumnWidth(ScreenUtil.getScreenWidth(this) / fonts.length);
				setFontGridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
				setFontGridView.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
											int position, long id) {
						switch (position) {
							case 0:
								showTypeface = typeface1;
								break;
							case 1:
								showTypeface = typeface2;
								break;
							case 2:
								showTypeface = typeface3;
								break;
							case 3:
								showTypeface = typeface4;
								break;

							default:
								break;
						}
						fontView.setBitmap(EditPhotoUtil.textAsBitmap(showText,showTypeface,showColor,curTextSize,textWidth,EditPhotoActivity.this));
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
				setColorGridView.setColumnWidth(ScreenUtil.getScreenWidth(this) / colors.length);
				setColorGridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
				setColorGridView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
											int position, long arg3) {
						// TODO Auto-generated method stub
						showColor = color[position];
						fontView.setBitmap(EditPhotoUtil.textAsBitmap(showText,showTypeface,showColor,curTextSize,textWidth,EditPhotoActivity.this));
					}
				});
				break;

			case R.id.edit_accessory:
				calRec();
				mStickerView.setVisibility(View.VISIBLE); // 事先让视图可见。
				//饰品编辑
				onEditStates();
				titleTextView.setText(R.string.decoration);
				eidtAdapter = new EditActivityAdapter(EditPhotoActivity.this,mainBitmap, new ArrayList<String>(),3, stikerInfos, handler);
				top_HorizontalListView.setAdapter(eidtAdapter);
				top_HorizontalListView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
											int position, long arg3) {
						// TODO Auto-generated method stub
						btn_onedit_save.setVisibility(View.VISIBLE);
						editType = 3;
						String stickerUrl = "";
						if (stikerInfos.get(position).onLine == 1) {//网络图片
							stickerUrl = Common.PHOTO_URL + stikerInfos.get(position).frameOriginalPathPortrait;
						}else {
							stickerUrl = stikerInfos.get(position).frameOriginalPathPortrait;
						}
						//ImageLoader 加载
						imageLoader.loadImage(stickerUrl, new ImageLoadingListener() {

							@Override
							public void onLoadingStarted(String imageUri, View view) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onLoadingFailed(String imageUri, View view,
														FailReason failReason) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
								// TODO Auto-generated method stub
								mStickerView.addBitImage(loadedImage);
							}

							@Override
							public void onLoadingCancelled(String imageUri, View view) {
								// TODO Auto-generated method stub

							}
						});
//					}else {
//						Bitmap accessoryBitmap = EditPhotoUtil.getImageFromAssetsFile(EditPhotoActivity.this, stikerInfos.get(position).frameOriginalPathPortrait);
//						mStickerView.addBitImage(accessoryBitmap);
//					}
					}
				});
				break;
			case R.id.btn_onedit_save: //保存到临时目录

				SaveStickersTask task = new SaveStickersTask();
				if (editType == 2) { //滤镜处理过的
					task.execute(newImage);
				}else if(editType == 3 || editType == 4 || editType == 1){
					task.execute(mainBitmap);
				}

				break;
			case R.id.preview_save:
				String url = nameFile + "/" + dateFormat.format(new Date()) + ".JPG";
				EditPhotoUtil.copyFile(pathList.get(index), url);
				scan(url);
				EditPhotoUtil.deleteTempPic(Common.TEMPPIC_PATH);
				break;
			case R.id.btn_forward:
				if (index == -1) {
					index = pathList.size() - 1;
				}

				if (pathList.size() > index + 1) {
					index++;
					loadImage(pathList.get(index));
				}
				check();
				break;
			case R.id.btn_cancel:
				if (index == -1) {
					index = pathList.size() - 1;
				}
				if (index >= 1) {
					index--;
				}

				if (pathList.size() - 2 >= 0) {

					if (index == 0) {
						if (isOnlinePic) {
							loadOnlineImg(pathList.get(index));
						}else{
							loadImage(pathList.get(index));
						}
					}else{
						loadImage(pathList.get(index));
					}
				}
				check();
				break;

			default:
				break;
		}
	}


	// 判断 后退 前进按钮的状态
	private void check() {
		if (index == -1) {
			index = pathList.size() - 1;
		}
		if (index == pathList.size() - 1) {
			btn_forward.setImageResource(R.drawable.forward1);
			btn_forward.setClickable(false);
		} else {
			btn_forward.setImageResource(R.drawable.forward);
			btn_forward.setClickable(true);
		}
		if (index == 0) {
			btn_cancel.setImageResource(R.drawable.cancel1);
			btn_cancel.setClickable(false);
		} else {
			btn_cancel.setImageResource(R.drawable.cancel);
			btn_cancel.setClickable(true);
		}
	}

	// 进入编辑某个效果的状态
	private void onEditStates() {
		titleTextView.setVisibility(View.VISIBLE);
		preview_save.setVisibility(View.GONE);
		btn_forward.setVisibility(View.GONE);
		btn_cancel.setVisibility(View.GONE);
		back.setVisibility(View.GONE);
		btn_left_back.setVisibility(View.VISIBLE);
		top_HorizontalListView.setVisibility(View.VISIBLE);
		edittoolsbar.setVisibility(View.INVISIBLE);
	}

	//退出 编辑状态
	private void exitEditStates(){
		editType = 0;//退出编辑状态，类型初始化。
		titleTextView.setVisibility(View.GONE);
		btn_onedit_save.setVisibility(View.GONE);
		btn_forward.setVisibility(View.VISIBLE);
		btn_cancel.setVisibility(View.VISIBLE);
		back.setVisibility(View.VISIBLE);
		btn_left_back.setVisibility(View.GONE);
		top_HorizontalListView.setVisibility(View.GONE);
		edittoolsbar.setVisibility(View.VISIBLE);
		//字体的编辑条消失。
		font_bar.setVisibility(View.GONE);
		setFontGridView.setVisibility(View.GONE);
		setColorGridView.setVisibility(View.GONE);
	}

	//读取 assets 目录下的图片
	public void addStickerImages(String folderPath) {
		stikerInfos.clear();
		FrameOrStikerInfo frameOrStikerInfo;
		try {
			String[] files =getResources().getAssets()
					.list(folderPath);
			for (String name : files) {
				frameOrStikerInfo = new FrameOrStikerInfo();
				frameOrStikerInfo.frameOriginalPathPortrait = "assets://" + folderPath + File.separator + name;
				frameOrStikerInfo.locationId = "common";
				frameOrStikerInfo.isActive = 1;
				frameOrStikerInfo.onLine = 0;
				stikerInfos.add(frameOrStikerInfo);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 异步载入本地编辑图片
	 *
	 * @param filepath
	 */
	public void loadImage(String filepath) {
		photoURL = filepath;
		if (mLoadImageTask != null) {
			mLoadImageTask.cancel(true);
		}
		mLoadImageTask = new LoadImageTask();
		mLoadImageTask.execute(filepath);
	}

	private final class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
		@Override
		protected Bitmap doInBackground(String... params) {
			return BitmapUtils.loadImageByPath(params[0], imageWidth,
					imageHeight);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			if (mainBitmap != null) {
				mainBitmap.recycle();
				mainBitmap = null;
				System.gc();
			}
			mainBitmap = result;

			if (mainBitmap != null) {
				if(AppUtil.getExifOrientation(photoURL)!=0){
					mainBitmap = AppUtil.rotaingImageView(AppUtil.getExifOrientation(photoURL),mainBitmap);
				}
				mainImage.setImageBitmap(mainBitmap);
			}
			Log.e("bitmap w and h:", mainBitmap.getWidth() + "----"+mainBitmap.getHeight());
			handler.sendEmptyMessage(INIT_DATA_FINISHED);
			//			mainImage.setDisplayType(DisplayType.FIT_TO_SCREEN);
		}
	}

	/**
	 * 加载网络图片
	 *
	 * @param url
	 */
	private void loadOnlineImg(String url){
		/*
		 * 需要使用三级缓存，1.判断SD卡是否存在，2.判断缓存中是否存在，3.从网上下载
		 */
		// 1.获取需要显示文件的文件名
		String fileString = ScreenUtil.getReallyFileName(photoURL);
		// 2、判断文件是否存在sd卡中
		File file = new File(Common.PHOTO_DOWNLOAD_PATH + fileString);
		if (file.exists()) {// 3、如果存在SD卡，则从SD卡获取图片信息
			System.out.println("file exists");
			loadImage(file.toString());
		}else{
			//如果sd卡不存在，判断是否在缓存种。
			System.out.println("file not exists");
			// 获取在缓存中的文件的名字
			final File dirfile = new File(this.getCacheDir() + "/"
					+ photoInfo.photoId+"_ori");
			System.out.println("dirfile = " + dirfile.toString());
			if (dirfile.exists()) {// 5、如果缓存存在，则从缓存中获取图片信息
				System.out.println("cache exists");
				loadImage(dirfile.toString());
			}else{
				//如果缓存不存在，从网上获取。 因为是从前面的页面跳转过来，所以图片都会存在缓存或者sd卡。所以没有必要从网上获取。
			}
		}
	}

	/**
	 * 切换底图Bitmap
	 *
	 * @param newBit
	 */
	public void changeMainBitmap(Bitmap newBit) {
		if (mainBitmap != null) {
			if (!mainBitmap.isRecycled()) {// 回收
				mainBitmap.recycle();
			}
			mainBitmap = newBit;
		} else {
			mainBitmap = newBit;
		}// end if
		mainImage.setImageBitmap(mainBitmap);
	}

	// 扫描SD卡
	private void scan(final String file) {
		// TODO Auto-generated method stub
		MediaScannerConnection.scanFile(this, new String[] { file }, null,
				new MediaScannerConnection.OnScanCompletedListener() {
					@Override
					public void onScanCompleted(String arg0, Uri arg1) {
						// TODO Auto-generated method stub
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


	//保存贴图 滤镜 的异步方法。
	/**
	 * 保存贴图任务
	 *
	 * @author panyi
	 *
	 */

	private final class SaveStickersTask extends
			AsyncTask<Bitmap, Void, Bitmap> {
		private CustomProgressDialog dialog;
		@Override
		protected Bitmap doInBackground(Bitmap... params) {
			// System.out.println("保存贴图!");
			String url = tempFile + "/"
					+ dateFormat.format(new Date()) + ".JPG";
			if (editType == 2) {//滤镜
				EditPhotoUtil.saveBitmap(params[0], url);
				pathList.add(url);
				index = pathList.size() - 1;
				return params[0];
			}else if(editType == 3){//饰品
				//				Matrix touchMatrix = mainImage.getImageViewMatrix();
				Matrix touchMatrix = mainImage.getImageMatrix();
				Bitmap resultBit = Bitmap.createBitmap(params[0]).copy(
						Bitmap.Config.ARGB_8888, true);
				Canvas canvas = new Canvas(resultBit);
				canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));  //抗锯齿
				float[] data = new float[9];
				touchMatrix.getValues(data);// 底部图片变化记录矩阵原始数据
				Matrix3 cal = new Matrix3(data);// 辅助矩阵计算类
				Matrix3 inverseMatrix = cal.inverseMatrix();// 计算逆矩阵
				Matrix m = new Matrix();
				m.setValues(inverseMatrix.getValues());
				LinkedHashMap<Integer, StickerItem> addItems = mStickerView.getBank();
				for (Integer id : addItems.keySet()) {
					StickerItem item = addItems.get(id);
					item.matrix.postConcat(m);// 乘以底部图片变化矩阵
					canvas.drawBitmap(item.bitmap, item.matrix, null);
				}// end for
				EditPhotoUtil.saveBitmap(resultBit, url);
				pathList.add(url);
				index = pathList.size() - 1;
				return resultBit;
			}else if(editType == 4){
				showText = getString(R.string.add_text); //保存之后，初始化原始数据。
				showColor = R.color.color16;
				showTypeface = Typeface.SANS_SERIF;
				//Matrix touchMatrix = mainImage.getImageViewMatrix();
				Matrix touchMatrix = mainImage.getImageMatrix();
				Bitmap resultBit = Bitmap.createBitmap(params[0]).copy(
						Bitmap.Config.ARGB_8888, true);
//				Bitmap resultBit = params[0];
				Canvas canvas = new Canvas(resultBit);
				canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));  //抗锯齿
				float[] data = new float[9];
				touchMatrix.getValues(data);// 底部图片变化记录矩阵原始数据
				Matrix3 cal = new Matrix3(data);// 辅助矩阵计算类
				Matrix3 inverseMatrix = cal.inverseMatrix();// 计算逆矩阵
				Matrix m = new Matrix();
				m.setValues(inverseMatrix.getValues());
				LinkedHashMap<Integer, FontItem> addItems = fontView.getBank();
				for (Integer id : addItems.keySet()) {
					FontItem item = addItems.get(id);
					item.matrix.postConcat(m);// 乘以底部图片变化矩阵
					canvas.drawBitmap(item.bitmap, item.matrix, null);
				}// end for
				//				fontView.currentItem.matrix.postConcat(m);
				//				canvas.drawBitmap(fontView.currentItem.bitmap, fontView.currentItem.matrix, null);
				EditPhotoUtil.saveBitmap(resultBit, url);
				pathList.add(url);
				index = pathList.size() - 1;
				return resultBit;
			}else if(editType == 1){
				Bitmap mainBitmap = params[0];
				Bitmap heBitmap = Bitmap.createBitmap(mainBitmap.getWidth(), mainBitmap.getHeight(),
						Config.ARGB_8888);
//				if (frameImageView.isShown()) {
				//不论边框显示与否，都让他合成。   即使是原图。
				Bitmap frameBitmap;
				if (mainBitmap.getWidth()<mainBitmap.getHeight()) {
					frameBitmap = imageLoader.loadImageSync(frameInfos.get(curFramePosition).frameOriginalPathPortrait);
				}else{
					frameBitmap = imageLoader.loadImageSync(frameInfos.get(curFramePosition).frameOriginalPathLandscape);
				}


				Log.e("＝＝＝＝＝＝＝＝＝＝＝＝＝＝", "frameBitmap width:"+frameBitmap.getWidth()+"_height:"+frameBitmap.getHeight());

				Canvas canvas = new Canvas(heBitmap);
				Paint point = new Paint();
				point.setXfermode(new PorterDuffXfermode(
						android.graphics.PorterDuff.Mode.SRC_OVER));
				Matrix matrix2 = new Matrix();
				matrix2.postScale(
						(float) mainBitmap.getWidth() / (frameBitmap.getWidth()),
						(float) mainBitmap.getHeight() / (frameBitmap.getHeight()));

//										frameBitmap = Bitmap.createBitmap(frameBitmap, 0, 0,
//												frameBitmap.getWidth(), frameBitmap.getHeight(),
//												matrix2, true);

				canvas.drawBitmap(mainBitmap, 0, 0, point);
				canvas.drawBitmap(frameBitmap, matrix2, point);
				matrix2.reset();
				frameBitmap.recycle();
				EditPhotoUtil.saveBitmap(heBitmap, url);
				pathList.add(url);
				index = pathList.size() - 1;
				return heBitmap;
//				}else{
//					.
//					Log.e("应用原图", "没有边框");
//					return heBitmap;  // 不应用边框时，显示原图。
//				}
			}
			return null;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			dialog.dismiss();
		}

		@Override
		protected void onCancelled(Bitmap result) {
			super.onCancelled(result);
			dialog.dismiss();
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			mStickerView.clear();
			fontView.clear();
			frameImageView.setVisibility(View.INVISIBLE);
			changeMainBitmap(result);
			exitEditStates();
			preview_save.setVisibility(View.VISIBLE);
			check();
			dialog.dismiss();

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = CustomProgressDialog.show(EditPhotoActivity.this, getString(R.string.saving), false, null);
			dialog.show();

		}
	}


	/**
	 * 执行滤镜任务
	 *
	 * @author panyi
	 *
	 */
	private final class ExcuteFilterTask extends
			AsyncTask<Bitmap, Void, Bitmap> {
		private CustomProgressDialog dialog;
		@Override
		protected Bitmap doInBackground(Bitmap... params) {
			// System.out.println("保存贴图!");
			if (filter instanceof LomoFi) {
				newImage = ((LomoFi) filter).transform(params[0]);
			} else if (filter instanceof EarlyBird) {
				newImage = ((EarlyBird) filter).transform(params[0],
						getResources());
			} else if (filter instanceof Amaro) {
				newImage = ((Amaro) filter).transform(params[0]);
			} else if (filter instanceof NormalFilter) {
				newImage = ((NormalFilter) filter).transform(params[0]);
			} else if (filter instanceof LomoFilter) {
				newImage = ((LomoFilter) filter).transform(params[0]);
			} else if (filter instanceof BeautifyFilter) {
				newImage = ((BeautifyFilter) filter).transform(params[0]);
			} else if (filter instanceof HDRFilter) {
				newImage = ((HDRFilter) filter).transform(params[0]);
			} else if (filter instanceof OldFilter) {
				newImage = ((OldFilter) filter).transform(params[0]);
			} else if (filter instanceof BlurFilter) {
				newImage = ((BlurFilter) filter).transform(params[0]);
			}
			//           
			return newImage;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			dialog.dismiss();
		}

		@Override
		protected void onCancelled(Bitmap result) {
			super.onCancelled(result);
			dialog.dismiss();
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			mainImage.setImageBitmap(result);
			dialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = CustomProgressDialog.show(EditPhotoActivity.this, getString(R.string.dealing), false, null);
			dialog.show();

		}
	}



	/**
	 * 合成边框方法
	 *  position
	 * @author talon
	 *
	 */
	private void loadframe(int position) {
		if (position != 0) {// 如果不为0，表示有边框
			frameImageView.setVisibility(View.VISIBLE);
			if (frameInfos.get(position).onLine == 1) {//网络图片，这个时候已经下载，所以直接取本地图片路径
				// 判断宽高，加载不同的边框。加载预览边框
				if (mainBitmap.getWidth() < mainBitmap.getHeight()) {
					imageLoader.displayImage("file://" + getFilesDir().toString() + "/frames/frame_portrait_" + ScreenUtil.getReallyFileName(frameInfos.get(position).frameOriginalPathPortrait),
							frameImageView, options, new ImageloaderListener());
				}else{
					imageLoader.displayImage("file://" + getFilesDir().toString() + "/frames/frame_landscape_" + ScreenUtil.getReallyFileName(frameInfos.get(position).frameOriginalPathLandscape),
							frameImageView, options, new ImageloaderListener());
				}
			}else {//本地图片
				// 判断宽高，加载不同的边框。加载预览边框
				if (mainBitmap.getWidth() < mainBitmap.getHeight()) {
					imageLoader.displayImage(frameInfos.get(position).frameOriginalPathPortrait, frameImageView, options, new ImageloaderListener());
				}else{
					imageLoader.displayImage(frameInfos.get(position).frameOriginalPathLandscape, frameImageView, options, new ImageloaderListener());
				}
			}
		} else {// 没有边框
			dialog.dismiss();
			frameImageView.setVisibility(View.INVISIBLE);
		}

	}

	/*
	 * Imageloader 加载监听类，目的是监听加载完毕的事件
	 */
	private class ImageloaderListener implements ImageLoadingListener{
		@Override
		public void onLoadingStarted(String imageUri, View view) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onLoadingFailed(String imageUri, View view,
									FailReason failReason) {
			// TODO Auto-generated method stub
			handler.sendEmptyMessage(LOAD_IMAGE_FINISH);
		}

		@Override
		public void onLoadingComplete(String imageUri, View view,
									  Bitmap loadedImage) {
			// TODO Auto-generated method stub
			handler.sendEmptyMessage(LOAD_IMAGE_FINISH);

		}

		@Override
		public void onLoadingCancelled(String imageUri, View view) {
			// TODO Auto-generated method stub

			handler.sendEmptyMessage(LOAD_IMAGE_FINISH);
		}

	}

	// 没有保存的时候的对话框
	private void createIsSaveDialog() {
		AlertDialog.Builder builder = new Builder(EditPhotoActivity.this);
		builder.setMessage(R.string.exit_hint);
		builder.setTitle("");
		builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				String url = nameFile + "/" + dateFormat.format(new Date()) + ".JPG";
				EditPhotoUtil.copyFile(pathList.get(index), url);
				scan(url);
				EditPhotoUtil.deleteTempPic(Common.TEMPPIC_PATH);
			}
		});
		builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		builder.create().show();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		EditPhotoUtil.deleteTempPic(Common.TEMPPIC_PATH);
		if (mainBitmap != null) {
			mainBitmap.recycle();
			mainBitmap = null;
		}
	}


	@Override
	public void inOrOutPlace(final String locationIds, final boolean in) {//位置改变之后就要改变边框的内容
		// TODO Auto-generated method stub
		Log.d(TAG, "in or out special location..." + locationIds);
		new Thread(){
			public void run() {
				while (!loadingFrame) {//等待边框处理完毕
					if (frameFromDBInfos != null && stickerFromDBInfos != null) {
						loadingFrame = true;
					}
				}
				//1.根据locationIds来判断需要显示或者隐藏的边框
//				frameInfos.addAll(frameFromDBInfos);
				for (int i = 0; i < frameFromDBInfos.size(); i++) {
					System.out.println("locationIds:"+locationIds+":locationId:"+frameFromDBInfos.get(i).locationId);
					if (locationIds.contains(frameFromDBInfos.get(i).locationId)) {//有属于特定地点的边框
						if (in) {//进入
							frameInfos.add(frameFromDBInfos.get(i));
						}else {//离开
							frameInfos.remove(frameFromDBInfos.get(i));
						}
					}
				}
//				stikerInfos.addAll(stickerFromDBInfos);
				for (int j = 0; j < stickerFromDBInfos.size(); j++) {
					System.out.println("locationIds:"+locationIds+":locationId:"+stickerFromDBInfos.get(j).locationId);
					if (locationIds.contains(stickerFromDBInfos.get(j).locationId)) {//有属于特定地点的边框
						if (in) {//进入
							stikerInfos.add(stickerFromDBInfos.get(j));
						}else {//离开
							stikerInfos.remove(stickerFromDBInfos.get(j));
						}
					}
				}
			};
		}.start();
	}

	/**
	 * 计算出 图片真正显示的坐标。
	 */
	private void calRec(){
		if (mainBitmap.getHeight() / (float)mainBitmap.getWidth() > mainImage.getHeight() / (float)mainImage.getWidth()) {//左右会留白
			displayBitmapHeight = mainImage.getHeight();//displayBitmapHeight : ? = bitmapReallyHeight : bitmapReallyWidth
			displayBitmapWidth = (int) (displayBitmapHeight * mainBitmap.getWidth() / (float) mainBitmap.getHeight());
		}else {//上下留白
			displayBitmapWidth = mainImage.getWidth();
			displayBitmapHeight = (int) (displayBitmapWidth * mainBitmap.getHeight() / (float)mainBitmap.getWidth());
		}
		leftTopX = (ScreenUtil.getScreenWidth(this) - displayBitmapWidth) / 2;
		rightBottomX = leftTopX + displayBitmapWidth;
		//leftTopY = 图片上边距＋imageview.getY
		//图片上边距 ＝ （imageview的高 － 图片显示在imageview上的高）／ 2
		leftTopY = (mainImage.getHeight() - displayBitmapHeight) / 2;
		rightBottomY = leftTopY + displayBitmapHeight;
	}

}
