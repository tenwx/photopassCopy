package com.pictureAir.fragment;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureAir.BaseFragment;
import com.pictureAir.EditStoryAlbumActivity;
import com.pictureAir.MainTabActivity;
import com.pictureAir.MipCaptureActivity;
import com.pictureAir.MyApplication;
import com.pictureAir.R;
import com.pictureAir.adapter.StoryViewPagerAdapter;
import com.pictureAir.db.PhotoInfoDBHelper;
import com.pictureAir.db.SQLiteHelperFactory;
import com.pictureAir.entity.DiscoverLocationItemInfo;
import com.pictureAir.entity.PhotoInfo;
import com.pictureAir.entity.PhotoItemInfo;
import com.pictureAir.util.ACache;
import com.pictureAir.util.API;
import com.pictureAir.util.Common;
import com.pictureAir.util.JsonUtil;
import com.pictureAir.util.ScreenUtil;
import com.pictureAir.util.UmengUtil;
import com.pictureAir.widget.CustomProgressDialog;
import com.pictureAir.widget.MyToast;
import com.pictureAir.widget.StoryMenuPop;
import com.pictureAir.widget.XListView;

/**
 * PhotoPass照片的图片墙，用来显示从服务器返回的照片信息，以及通过magic相机拍摄的图片 可以左右滑动切换不同的相册
 * 可以下拉刷新，获取更多的图片信息
 * */
public class FragmentPageStory extends BaseFragment implements OnClickListener {
	// 声明静态变量
	private static final int LOADING = 333;
	private static final int DEAL_ALL_PHOTO_DATA_DONE = 444;
	private static final int DEAL_REFRESH_PHOTO_DATA_DONE = 555;
	// private static final int DEAL_DATA_COMPLETED = 222;
	private static final int LOAD_COMPLETED = 111;
	private static final int REFRESH = 666;
	private static final int REFRESH_LOCAL_PHOTOS = 777;

	private static String TAG = "FragmentPageStory";

	// 申明变量
	private int position;
	private int refreshDataCount = 0;// 记录刷新数据的数量
	private boolean needfresh = false;
	private int screenWidth;
	// private int currentIndex = 0;//记录当前viewpager的索引值
	// private boolean scanMagicFinish = false;
	private boolean isLoading = false;
	private boolean scanMagicPhotoNeedCallBack;// 记录是否需要重新扫描本地照片
	// private static HashMap<String, Integer> refreshTabMap = new
	// HashMap<String, Integer>();
	private static int refreshTap = 0;

	// 申明控件
	private ImageView more;
	private ImageView scanLayout;
	private RelativeLayout scanRelativeLayout;
	private ImageView redIconImageView;
	private static XListView mListView;
	private static CustomProgressDialog dialog;// 加载等待
	private ImageView cursorImageView;
	private TextView storyTabAllTextView, storyTabPhotopassTextView,
			storyTabMagicTextView, storyTabBoughtTextView;
	private static ViewPager storyViewPager;
	private LinearLayout storyNoPpToScanLinearLayout, storyLeadBarLinearLayout,
			storyCursorLinearLayout;
	private ImageView storyNoPpScanImageView;
	private StoryMenuPop storyMenuPop;

	// 申明类
	private MyApplication app;
	private ArrayList<PhotoItemInfo> photoPassPictureList;
	private ArrayList<PhotoItemInfo> magicPicList;
	private ArrayList<DiscoverLocationItemInfo> locationList = new ArrayList<DiscoverLocationItemInfo>();
	private ArrayList<View> listViews;
	private static StoryViewPagerAdapter storyViewPagerAdapter;
	public static Context context;
	private SimpleDateFormat sdf;
	private static SharedPreferences sharedPreferences;
	private SQLiteOpenHelper dbHelper;
	private SQLiteDatabase db;
	private MyToast myToast;
	private PhotoInfo selectPhotoItemInfo;
	private ScanPhotosThread scanPhotosThread;
	// private BadgeView scanBadgeView;

	// 申明handler消息回调机制
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REFRESH_LOCAL_PHOTOS:// 刷新处理本地照片
				Log.d(TAG, "scan local photos success");
				dealLocalRefreshedData();
				break;

			case REFRESH:// 开始刷新
				Log.d(TAG, "the index of refreshing is " + msg.arg1);
				API.getPhotosByConditions(sharedPreferences.getString(
						Common.USERINFO_TOKENID, null), handler,
						sharedPreferences.getString(
								Common.LAST_UPDATE_PHOTO_TIME, null));// 获取更新信息
				// refreshTabMap.put("refreshTab", msg.arg1);
				refreshTap = msg.arg1;
				break;

			case API.GET_REFRESH_PHOTOS_SUCCESS:// 获取刷新的推送图片
				// fasdfas;
				app.setPushPhotoCount(0);
				Log.d(TAG, "deal refresh photos-------");
				saveJsonToSQLite((JSONObject) msg.obj, false);
				break;

			case API.GET_REFRESH_PHOTOS_FAILED:// 获取刷新失败
				if (dialog.isShowing()) {
					dialog.dismiss();
				}
				storyViewPagerAdapter.stopRefresh(refreshTap);
				myToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
				break;

			case StoryMenuPop.EDIT:
				if (sharedPreferences.getInt(Common.PP_COUNT, 0) < 2) {// 没有照片，提示先添加pp
					myToast.setTextAndShow(R.string.add_pp_first,
							Common.TOAST_SHORT_TIME);
					return;
				}
				Intent intent = new Intent(getActivity(),
						EditStoryAlbumActivity.class);
				Bundle b = new Bundle();
				switch (storyViewPager.getCurrentItem()) {
				case 0:
					b.putParcelableArrayList("photos", app.allPicList);
					break;

				case 1:
					b.putParcelableArrayList("photos", photoPassPictureList);
					break;

				case 2:
					b.putParcelableArrayList("photos", magicPicList);
					break;

				case 3:
					b.putParcelableArrayList("photos", app.boughtPicList);
					break;

				default:
					break;
				}
				intent.putExtra("photos", b);
				getActivity().startActivity(intent);
				break;

			case API.GET_PHOTOS_SUCCESS:// 获取照片成功
				Log.d(TAG, "--------->get photo success");
				saveJsonToSQLite((JSONObject) msg.obj, true);
				break;

			case DEAL_ALL_PHOTO_DATA_DONE:// 处理照片成功
				app.setPushPhotoCount(0);// 清空推送消息的数量
				photoPassPictureList.clear();
				app.allPicList.clear();
				app.boughtPicList.clear();
				try {
					getData();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;

			case DEAL_REFRESH_PHOTO_DATA_DONE:// 处理刷新照片成功
				Log.d(TAG, "deal refresh photos done");
				Editor editor = sharedPreferences.edit();// 获取编辑器
				editor.putInt("photoCount", 0);
				editor.commit();// 提交修改
				if (refreshDataCount > 0) {
					System.out.println("getrefreshdata");
					getrefreshdata();
					refreshDataCount = 0;
				} else {
					System.out.println("nomore");
					myToast.setTextAndShow(R.string.nomore,
							Common.TOAST_SHORT_TIME);
				}
				if (dialog.isShowing()) {
					dialog.dismiss();
				}
				// app.setPushPhotoCount(0);
				if (MainTabActivity.maintabbadgeView.isShown()) {
					MainTabActivity.maintabbadgeView.hide();
				}
				storyViewPagerAdapter.stopRefresh(refreshTap);
				break;

			case API.GET_LOCATION_SUCCESS:// 成功获取地点信息
				Log.d(TAG,
						"---------->get location success" + msg.obj.toString());
				try {
					JSONObject response = new JSONObject(msg.obj.toString());
					JSONArray resultArray = response.getJSONArray("locations");
					for (int i = 0; i < resultArray.length(); i++) {
						DiscoverLocationItemInfo locationInfo = new DiscoverLocationItemInfo();
						JSONObject object = resultArray.getJSONObject(i);
						locationInfo = JsonUtil.getLocation(object);
						// Log.d(TAG, locationInfo.locationId);
						locationList.add(locationInfo);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				// 检查数据库是否有数据，如果有数据，直接显示，如果没有数据，从网络获取
				app.photoPassPicList.clear();

				if (!needfresh) {// 如果需要刷新数据的话，就不需要从数据库中获取数据
					Log.d(TAG, "---------> load data from databases");
					loadDataFromDataBase();
				}
				if (app.photoPassPicList.size() == 0 || needfresh) {
					// 数据为0，需要从网上下载
					System.out.println("photolist size = 0");
					// 判断是否之前有成功获取过
					API.getPhotosByConditions(sharedPreferences.getString(
							Common.USERINFO_TOKENID, null), handler, null);// 获取全部图片
				} else {
					System.out.println("photolist size = "
							+ app.photoPassPicList.size());
					// 有数据，直接显示
					photoPassPictureList.clear();
					app.allPicList.clear();
					app.boughtPicList.clear();
					try {
						getData();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				break;

			// case DEAL_DATA_COMPLETED:
			// System.out.println("location is ready list size "+
			// photoPassPictureList.size());
			// // if(storyViewPagerAdapter == null){
			// storyViewPagerAdapter = new StoryViewPagerAdapter(context,
			// listViews, allPicList, photoPassPictureList, magicPicList,
			// boughtPicList);
			// storyViewPager.setAdapter(storyViewPagerAdapter);
			// storyViewPager.setOnPageChangeListener(new
			// MyOnPageChangeListener());
			// storyViewPager.setCurrentItem(app.fragmentStoryLastSelectedTab);
			// Animation animation = new TranslateAnimation(0, screenWidth / 4 *
			// app.fragmentStoryLastSelectedTab, 0, 0);
			// animation.setFillAfter(true);
			// animation.setDuration(300);
			// cursorImageView.startAnimation(animation);
			//
			//
			// // }else{
			// // storyViewPager.setAdapter(storyViewPagerAdapter);
			// // storyViewPager.setOnPageChangeListener(new
			// MyOnPageChangeListener());
			// //// storyViewPager.setCurrentItem(0);
			// // storyViewPagerAdapter.notifyDataSetChanged();
			// // }
			// if(dialog.isShowing()){
			// dialog.dismiss();
			// }
			// break;
			case API.GET_PHOTOS_FAILED:
			case API.GET_LOCATION_FAILED:
				if (dialog.isShowing()) {
					dialog.dismiss();
				}
				myToast.setTextAndShow(R.string.http_failed,
						Common.TOAST_SHORT_TIME);
				break;

			case LOAD_COMPLETED:
				// storyViewPagerAdapter.refreshData(picList);
				scanMagicPhotoNeedCallBack = true;
				// if (storyViewPagerAdapter == null) {
				storyViewPagerAdapter = new StoryViewPagerAdapter(context,
						listViews, app.allPicList, photoPassPictureList,
						magicPicList, app.boughtPicList, handler);
				storyViewPager.setAdapter(storyViewPagerAdapter);
				storyViewPager
						.setOnPageChangeListener(new MyOnPageChangeListener());
				System.out.println("------------> adapter is null ");
				// }else {
				// System.out.println("0------------> adapter is not null");
				// storyViewPagerAdapter.notifyDataSetChanged();
				// }
				storyViewPager.setCurrentItem(app.fragmentStoryLastSelectedTab);
				if (sharedPreferences.getInt(Common.PP_COUNT, 0) > 1) {
					showViewPager(true);
				}

				Animation animation = new TranslateAnimation(0, screenWidth / 4
						* app.fragmentStoryLastSelectedTab, 0, 0);
				// app.fragmentStoryLastSelectedTab = arg0;
				animation.setFillAfter(true);
				animation.setDuration(300);
				cursorImageView.startAnimation(animation);
				if (dialog.isShowing()) {
					dialog.dismiss();
				}
				// 关闭定位服务
				// stopService();
				break;

			default:
				break;
			}
		};
	};

	/**
	 * 解析服务器返回的数据
	 * 
	 * @param jsonObject
	 *            json对象
	 * @param isAll
	 *            布尔值，是否是获取全部数据
	 * 
	 */
	private void saveJsonToSQLite(JSONObject jsonObject, final boolean isAll) {
		db = dbHelper.getWritableDatabase(Common.SQLCIPHER_KEY);
		if (isAll) {// 获取全部数据，需要先清空数据库，反之，插入到后面
			Log.d(TAG, "delete all data from table");
			db.execSQL("delete from " + Common.PHOTOPASS_INFO_TABLE);
		} else {
			Log.d(TAG, "need not delete all data");
		}
		Log.d(TAG, "saveJsonToSQLite json" + jsonObject.toString());
		try {
			final JSONArray responseArray = jsonObject.getJSONArray("photos");
			String updatetimeString = jsonObject.getString("time");
			System.out.println("updatetime:" + updatetimeString
					+ "new data count = " + responseArray.length());

			if (isAll || responseArray.length() > 0) {// 说明全部获取，需要记录时间；如果刷新的话，有数据的时候，才记录时间，否则不记录时间
				// 需要存储这个时间
				Editor editor = sharedPreferences.edit();
				editor.putString(Common.LAST_UPDATE_PHOTO_TIME,
						updatetimeString);
				editor.commit();
			}

			if (isAll) {// 如果全部获取，需要清除原有的数据
				app.photoPassPicList.clear();
			} else {// 刷新最新照片，获取刷新数据的数量
				refreshDataCount = responseArray.length();
				Log.d(TAG, "------refresh count ----->" + refreshDataCount);
			}

			new Thread() {
				public void run() {
					for (int i = 0; i < responseArray.length(); i++) {
						try {
							JSONObject object = responseArray.getJSONObject(i);
							final PhotoInfo photo = JsonUtil.getPhoto(db,
									object);
							app.photoPassPicList.add(photo);
							// 暂时无法实现 服务端没有返回图片总数量
							// 将获取到的图片数量统计到友盟中

							// 将数据插入到数据库
							db.execSQL("insert into "
									+ Common.PHOTOPASS_INFO_TABLE
									+ " values(null,?,?,?,?,?,?,?,?,?,?,?)",
									new String[] { photo.photoId,
											photo.photoPassCode,
											photo.shootTime,
											photo.photoPathOrURL,
											photo.photoThumbnail,
											photo.photoThumbnail_512,
											photo.photoThumbnail_1024,
											photo.locationId, photo.shootOn,
											0 + "", photo.isPayed + "" });
							// System.out.println("current thread-->"+Thread.currentThread().getName());
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					db.close();
					// 通知已经处理完毕
					if (isAll) {
						handler.sendEmptyMessage(DEAL_ALL_PHOTO_DATA_DONE);

					} else {
						handler.sendEmptyMessage(DEAL_REFRESH_PHOTO_DATA_DONE);
					}
					// 友盟数据统计1，有多少人有图片2，总共有多少张图片
					Umeng();

				};
			}.start();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 友盟数据统计
	 */
	public void Umeng() {
		System.out.println("Umeng ==>>");
		if (MyApplication.getInstance().photoPassPicList != null
				&& MyApplication.getInstance().photoPassPicList.size() > 0) {
			// 统计用户是否有图片、 提交友盟统计
			// 1. 统计多少人有图片
			String isHave = sharedPreferences
					.getString(Common.IS_HAVE_PIC, "0");
			Editor editor = sharedPreferences.edit();
			// 判断之前是否添加过 1- 添加过，不需要再统计；0-没有，需要统计
			if (isHave.equals("0")) {
				// 之前没有添加该用户
				Map<String, String> map = new HashMap<String, String>();
				String name = sharedPreferences.getString(Common.USERINFO_ID, "");
				map.put("UserName", name);// 添加用户名称属性
				System.out.println("Umeng ==>>" + "onEvent1");
				UmengUtil.onEvent(context,
						Common.EVENT_CONTAIN_PICTURE_PEOPLES, map);

				// 保存统计记录
				editor.putString(Common.IS_HAVE_PIC, "1");
				editor.commit();
			}

			// 2. 统计所有人所有图片
			int size_last = sharedPreferences.getInt(Common.PICTURE_COUNT, 0);
			// 获取增量
			int duration = MyApplication.getInstance().photoPassPicList.size()
					- size_last;
			if (duration != 0) {
				// 之前没有添加该用户
				Map<String, String> map = new HashMap<String, String>();
				map.put("UserName",
						sharedPreferences.getString(Common.USERINFO_ID, ""));// 添加用户名称属性
				UmengUtil.onEvent(context, Common.EVENT_TOTAL_PICTURES, map,
						duration);
				System.out.println("Umeng ==>>" + "onEvent2");
				// 保存统计记录
				editor.putInt(Common.PICTURE_COUNT,
						MyApplication.getInstance().photoPassPicList.size());
				editor.commit();
			}

		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_story, null);

		// 获取控件
		more = (ImageView) view.findViewById(R.id.story_more);
		scanRelativeLayout = (RelativeLayout) view
				.findViewById(R.id.storyScanRelativeLayout);
		scanLayout = (ImageView) view.findViewById(R.id.story_scan);
		redIconImageView = (ImageView) view.findViewById(R.id.scan_red_point);
		storyNoPpToScanLinearLayout = (LinearLayout) view
				.findViewById(R.id.story_no_pp_to_scan);
		storyLeadBarLinearLayout = (LinearLayout) view
				.findViewById(R.id.story_lead_bar);
		storyCursorLinearLayout = (LinearLayout) view
				.findViewById(R.id.story_cursor_layout);
		storyNoPpScanImageView = (ImageView) view
				.findViewById(R.id.story_no_pp_scan);
		storyTabAllTextView = (TextView) view.findViewById(R.id.story_tab_all);
		storyTabPhotopassTextView = (TextView) view
				.findViewById(R.id.story_tab_photopass);
		storyTabMagicTextView = (TextView) view
				.findViewById(R.id.story_tab_magic);
		storyTabBoughtTextView = (TextView) view
				.findViewById(R.id.story_tab_bought);
		cursorImageView = (ImageView) view.findViewById(R.id.story_cursor);
		storyViewPager = (ViewPager) view.findViewById(R.id.story_viewPager);

		// 初始化控件
		context = getActivity();
		sharedPreferences = getActivity().getSharedPreferences(
				Common.USERINFO_NAME, Context.MODE_PRIVATE);
		// allPicList = new ArrayList<PhotoItemInfo>();
		photoPassPictureList = new ArrayList<PhotoItemInfo>();
		magicPicList = new ArrayList<PhotoItemInfo>();
		// boughtPicList = new ArrayList<PhotoItemInfo>();
		storyMenuPop = new StoryMenuPop(getActivity(), handler);
		ShowHasPPCondition(
				(sharedPreferences.getInt(Common.PP_COUNT, 0) < 2) ? false
						: true, view);
		scanLayout.setOnClickListener(this);
		scanRelativeLayout.setOnClickListener(this);
		more.setOnClickListener(this);

		// 初始化数据
		scanMagicPhotoNeedCallBack = false;
		// refreshTabMap.put("refreshTab", 0);
		myToast = new MyToast(getActivity());
		app = (MyApplication) getActivity().getApplication();
		dbHelper = SQLiteHelperFactory.create(context);
//		dbHelper = new PhotoInfoDBHelper(context, Common.PHOTOPASS_INFO_NAME,
//				Common.PHOTOPASS_INFO_VERSION);
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		locationList.clear();
		screenWidth = ScreenUtil.getScreenWidth(FragmentPageStory.this
				.getActivity());
		Log.d(TAG, "screen width = " + screenWidth);
		needfresh = sharedPreferences.getBoolean(Common.NEED_FRESH, false);
		if (needfresh) {// 如果一开始就需要全部刷新，
			Editor editor = sharedPreferences.edit();
			editor.putBoolean(Common.NEED_FRESH, false);
			editor.commit();
		}
		// 获取API
		dialog = CustomProgressDialog.show(getActivity(),
				getString(R.string.is_loading), false, null);
		// dialog = new CustomProgressDialog(getActivity(),
		// getString(R.string.is_loading), false);
		// dialog.show();
		// dialog.setMessage(getString(R.string.is_loading));
		// dialog = ProgressDialog.show(context, getString(R.string.loading___),
		// getString(R.string.is_loading), false, false);
		isLoading = true;
		// 获取地点信息
		if (ACache.get(getActivity()).getAsString(Common.LOCATION_INFO) == null) {
			API.getLocationInfo(getActivity(), handler);// 获取所有的location
		} else {
			Message message = handler.obtainMessage();
			message.what = API.GET_LOCATION_SUCCESS;
			message.obj = ACache.get(getActivity()).getAsString(
					Common.LOCATION_INFO);
			handler.sendMessage(message);
		}
		// //开启扫描图片的线程
		// scanPhotosThread = new ScanPhotosThread(false);
		// scanPhotosThread.start();

		return view;
	}

	/**
	 * 根据是否有pp，来显示对应的UI布局
	 * 
	 * @param hasScanedPP
	 *            是否扫描过pp
	 * @param view
	 */
	private void ShowHasPPCondition(boolean hasPP, View view) {
		// 绑定监听
		storyTabAllTextView.setOnClickListener(new viewPagerOnClickListener(0));
		storyTabPhotopassTextView
				.setOnClickListener(new viewPagerOnClickListener(1));
		storyTabMagicTextView
				.setOnClickListener(new viewPagerOnClickListener(2));
		storyTabBoughtTextView.setOnClickListener(new viewPagerOnClickListener(
				3));
		// 初始化storyViewPage的信息
		LayoutInflater mInflater = getActivity().getLayoutInflater();
		listViews = new ArrayList<View>();
		listViews.add(mInflater.inflate(R.layout.story_pinned_list, null));
		listViews.add(mInflater.inflate(R.layout.story_pinned_list, null));
		listViews.add(mInflater.inflate(R.layout.story_pinned_list, null));
		listViews.add(mInflater.inflate(R.layout.story_pinned_list, null));
		storyNoPpScanImageView.setOnClickListener(this);
		// scanBadgeView = new BadgeView(getActivity(), scanLayout);
		// scanBadgeView.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
		// scanBadgeView.setTextSize(2);
		// scanBadgeView.setBackgroundResource(R.drawable.notificaitonpoint);
		showViewPager(hasPP);
	}

	/**
	 * 控制控件的隐藏或者显示
	 * 
	 * @param show
	 */
	private void showViewPager(boolean show) {
		if (show) {// 如果有扫过pp
			// 隐藏没有pp的情况
			storyNoPpToScanLinearLayout.setVisibility(View.GONE);
			// 显示有pp的情况
			storyLeadBarLinearLayout.setVisibility(View.VISIBLE);
			storyCursorLinearLayout.setVisibility(View.VISIBLE);
			storyViewPager.setVisibility(View.VISIBLE);

			redIconImageView.setVisibility(View.INVISIBLE);
		} else {// 没有扫过pp
			redIconImageView.setVisibility(View.VISIBLE);
			// scanBadgeView.show();
		}
	}

	// 选项卡点击事件监听
	private class viewPagerOnClickListener implements OnClickListener {
		private int index = 0;

		public viewPagerOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			storyViewPager.setCurrentItem(index);
		}

	}

	/**
	 * 页卡切换监听
	 */
	private class MyOnPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageSelected(int arg0) {
			System.out.println(arg0 + " selected-----------");
			Animation animation = new TranslateAnimation(screenWidth / 4
					* app.fragmentStoryLastSelectedTab, screenWidth / 4 * arg0,
					0, 0);
			app.fragmentStoryLastSelectedTab = arg0;
			animation.setFillAfter(true);
			animation.setDuration(300);
			cursorImageView.startAnimation(animation);
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}

	// 扫描图片线程类
	private class ScanPhotosThread extends Thread {
		private boolean needCallBck;// onResume的时候才会检查是否有刷新数据

		public ScanPhotosThread(boolean needCallBck) {
			// TODO Auto-generated constructor stub
			this.needCallBck = needCallBck;
		}

		@Override
		public void run() {
			System.out.println("------->run");
			if (!app.scanMagicFinish) {
				ScanPhotos(Common.PHOTO_SAVE_PATH, Common.ALBUM_MAGIC,
						needCallBck);
				Collections.sort(app.magicPicList);
				app.scanMagicFinish = true;
			}
			if (needCallBck) {// 是刷新数据操作，需要通知adatper更新数据
				handler.sendEmptyMessage(REFRESH_LOCAL_PHOTOS);
			}
		}
	}

	// 获取Magic的照片
	private void ScanPhotos(String filePath, String albumName,
			boolean needCallBck) {
		System.out.println("---------->scan" + albumName);
		if (!Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {// 如果SD卡不存在
			app.scanMagicFinish = true;
			return;
		}
		File file = new File(filePath);
		if (!file.exists()) {// 如果文件不存在，创建文件夹
			file.mkdirs();
			return;
		}
		File[] files = file.listFiles();
		Date date;
		if (needCallBck) {
			Log.d(TAG, "need remove local data first");
			app.allPicList.removeAll(magicPicList);
			magicPicList.clear();
		}
		app.magicPicList.clear();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().endsWith(".JPG")
					|| files[i].getName().endsWith(".jpg")) {
				if (files[i].length() > 0) {// 扫描到文件
					selectPhotoItemInfo = new PhotoInfo();
					selectPhotoItemInfo.photoPathOrURL = files[i].getPath();
					selectPhotoItemInfo.lastModify = files[i].lastModified();
					date = new Date(selectPhotoItemInfo.lastModify);
					selectPhotoItemInfo.shootOn = sdf.format(date);
					selectPhotoItemInfo.shootTime = selectPhotoItemInfo.shootOn
							.substring(0, 10);
					selectPhotoItemInfo.isChecked = 0;
					selectPhotoItemInfo.isSelected = 0;
					selectPhotoItemInfo.showMask = 0;
					// selectPhotoItemInfo.albumName = albumName;
					selectPhotoItemInfo.isPayed = 1;
					selectPhotoItemInfo.onLine = 0;
					app.magicPicList.add(selectPhotoItemInfo);
					System.out.println("magic url =========>"
							+ selectPhotoItemInfo.photoPathOrURL);
				}
			}
		}
	}

	/**
	 * 检查数据库是否有数据
	 */
	private void loadDataFromDataBase() {
		// TODO Auto-generated method stub
		System.out.println("load data from database");
		boolean hasPhoto = false;
		// dbHelper = new PhotoInfoDBHelper(context, Common.PHOTOPASS_INFO_NAME,
		// Common.PHOTOPASS_INFO_VERSION);
		db = dbHelper.getReadableDatabase(Common.SQLCIPHER_KEY);
		// 查询photo表的信息
		System.out.println("start query");
		Cursor cursor = db.rawQuery("select * from "
				+ Common.PHOTOPASS_INFO_TABLE + " order by shootOn desc", null);
		PhotoInfo photoInfo;
		if (!cursor.moveToFirst()) {// 判断是否photo数据
			System.out.println("move to first failed---");
			// cursor.close();
			// db.close();
			hasPhoto = false;
		} else {
			hasPhoto = true;
		}
		if (hasPhoto) {
			do {
				photoInfo = new PhotoInfo();
				System.out.println("load data from database = "
						+ cursor.getInt(0));
				photoInfo.photoId = cursor.getString(1);// photoId
				photoInfo.photoPassCode = cursor.getString(2);// photopassCode
				photoInfo.shootTime = cursor.getString(3);// shootTime
				photoInfo.photoPathOrURL = cursor.getString(4);// originalUrl
				photoInfo.photoThumbnail = cursor.getString(5);// previewUrl
				photoInfo.photoThumbnail_512 = cursor.getString(6);// previewUrl_512
				photoInfo.photoThumbnail_1024 = cursor.getString(7);// previewUrl_1024
				photoInfo.locationId = cursor.getString(8);// locationId
				photoInfo.shootOn = cursor.getString(9);// shootOn
				photoInfo.isLove = Integer.valueOf(cursor.getString(10));// islove
				photoInfo.isPayed = Integer.valueOf(cursor.getString(11));// ispay
				photoInfo.onLine = 1;
				photoInfo.isChecked = 0;
				photoInfo.isSelected = 0;
				photoInfo.isUploaded = 0;
				photoInfo.showMask = 0;
				photoInfo.lastModify = 0l;
				photoInfo.index = "";
				// photoInfo.albumName = "";
				app.photoPassPicList.add(photoInfo);

				// 将获取到的图片数量统计到友盟中
			} while (cursor.moveToNext());
		}
		System.out.println("get data from database");
		// 查询favorite表的信息
		// cursor = db.rawQuery("select * from "+Common.FAVORITE_INFO_TABLE,
		// null);
		// cursor =
		// db.rawQuery("select * from "+Common.FAVORITE_INFO_TABLE+" where userId=?",
		// new String[]{sharedPreferences.getString(Common.USERINFO_ID, null)});
		// // String userIdString =
		// sharedPreferences.getString(Common.USERINFO_ID, null);
		// //
		// System.out.println("get favorite data==========="+sharedPreferences.getString(Common.USERINFO_ID,
		// null));
		// favoriteList.clear();
		// if (!cursor.moveToFirst()) {//判断是否有数据
		// System.out.println("no location data");
		// cursor.close();
		// db.close();
		// return;
		// }
		// System.out.println("favorite data has been cleared");
		// do {
		// System.out.println("load data from database = "+ cursor.getInt(0));
		// System.out.println("favoritelist locationid ======"+cursor.getString(1));
		// // if (userIdString.equals(cursor.getString(2))) {
		// favoriteList.add(cursor.getString(1));//locationid
		// // }
		// System.out.println("favorite size = "+favoriteList.size());
		// } while (cursor.moveToNext());
		//
		cursor.close();
		db.close();
	}

	@Override
	public void onResume() {
		System.out.println("on resume-----------");
		if (sharedPreferences.getBoolean(Common.NEED_FRESH, false)) {
			System.out.println("need refresh");
			Editor editor = sharedPreferences.edit();
			editor.putBoolean(Common.NEED_FRESH, false);
			editor.commit();
			dialog.show();
			// app.allPicList.removeAll(magicPicList);//避免扫描pp之后，magic的图片会重复
			API.getPhotosByConditions(
					sharedPreferences.getString(Common.USERINFO_TOKENID, null),
					handler, null);// 获取全部图片
			if (MainTabActivity.maintabbadgeView.isShown()) {
				MainTabActivity.maintabbadgeView.hide();
			}
			if (redIconImageView.isShown()) {
				redIconImageView.setVisibility(View.GONE);
			}
			// //开启扫描图片的线程
			// scanPhotosThread = new ScanPhotosThread(false);
		}
		// else {
		// 开启扫描图片的线程
		if (!app.scanMagicFinish) {
			dialog.show();
			scanPhotosThread = new ScanPhotosThread(scanMagicPhotoNeedCallBack);
			scanPhotosThread.start();
		} else {
			Collections.sort(app.magicPicList);
		}
		// }
		super.onResume();
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		// storyViewPagerAdapter = null;
		// storyViewPager = null;
		super.onDestroyView();
	}

	/**
	 * 数据组合排序 遍历已经存在了的图片资源，分成四大类，all，photopass，magic，bought
	 * 
	 * @throws ParseException
	 * */
	private void getData() throws ParseException {
		// 遍历所有photopass信息
		PhotoItemInfo photoItemInfo;
		boolean clone_contains = false;
		Date date1;
		Date date2;
		for (int l = 0; l < app.photoPassPicList.size(); l++) {
			PhotoInfo info = app.photoPassPicList.get(l);
			// Log.d(TAG, "scan photo list:"+l);
			// 先挑选出相同的locationid信息
			for (int i = 0; i < locationList.size(); i++) {
				// Log.d(TAG, "scan location:"+i);
				if (info.locationId.equals(locationList.get(i).locationId)
						|| locationList.get(i).locationIds
								.contains(info.locationId)) {
					// Log.d(TAG, "find the location");
					// 如果locationid一样，需要判断是否已经存在此item，如果有，在按照时间分类，没有，新建一个item
					for (int j = 0; j < photoPassPictureList.size(); j++) {
						// Log.d(TAG, "weather already exists:"+j);
						if (info.shootTime
								.equals(photoPassPictureList.get(j).shootTime)
								&& (info.locationId.equals(photoPassPictureList
										.get(j).locationId) || photoPassPictureList
										.get(j).locationIds
										.contains(info.locationId))) {
							// Log.d(TAG, "photo location id "+ info.locationId
							// + "____"+ info.shootTime);
							// Log.d(TAG,
							// "location id:"+locationList.get(i).locationId
							// +"___"+ locationList.get(i).locationIds);
							// Log.d(TAG,
							// "location id:"+photoPassPictureList.get(j).locationId
							// +"___"+ photoPassPictureList.get(j).locationIds);
							// Log.d(TAG, "already exist");
							photoPassPictureList.get(j).list.add(info);
							date1 = sdf.parse(info.shootOn);
							date2 = sdf
									.parse(photoPassPictureList.get(j).shootOn);
							if (date1.after(date2)) {
								photoPassPictureList.get(j).shootOn = info.shootOn;
							}
							clone_contains = true;
							addToBoughtList(info, i);
							break;
						}
					}
					if (!clone_contains) {
						// 初始化item的信息
						// Log.d(TAG, "not exist");
						photoItemInfo = new PhotoItemInfo();
						photoItemInfo.locationId = locationList.get(i).locationId;
						photoItemInfo.locationIds = locationList.get(i).locationIds
								.toString();
						photoItemInfo.shootTime = info.shootTime;
						photoItemInfo.place = locationList.get(i).place;
						photoItemInfo.list.add(info);
						photoItemInfo.placeUrl = locationList.get(i).placeUrl;
						photoItemInfo.latitude = locationList.get(i).latitude;
						photoItemInfo.longitude = locationList.get(i).longitude;
						photoItemInfo.islove = 0;
						photoItemInfo.shootOn = info.shootOn;
						photoPassPictureList.add(photoItemInfo);
						addToBoughtList(info, i);
					} else {
						clone_contains = false;
					}
					break;
				}
			}
		}

		// 如果网络图片先处理完，magic相册还没处理完的话，需要等待magic处理完
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (isLoading) {
					// 如果magic相册已经处理完毕
					Log.d(TAG, "waiting for scanning local photo completed");
					if (app.scanMagicFinish) {
						isLoading = false;
					}
				}
				try {
					getMagicData();
					// 将magic和photopass列表放入all中
					app.allPicList.addAll(photoPassPictureList);
					app.allPicList.addAll(magicPicList);
					Collections.sort(app.allPicList);// 对all进行排序
					System.out.println("location is ready");
					handler.sendEmptyMessage(LOAD_COMPLETED);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Log.d(TAG, "------>completed");
			}
		}).start();

		// /**
		// * 初始化favorite地址信息
		// * 1.遍历favorite信息
		// * 2.遍历list信息
		// * 3.判断是否有图片，如果有图片，直接结束当前循环
		// * 4.如果没有图片，判断locationid是否一致
		// * 5.如果一致，并且没有photo信息的，设置favorite信息，并且把那一项移动到没有photo的顶部，结束当前循环
		// */
		// int temp = 0;//记录有图和没图的临界点
		// //1.遍历favorite信息
		// System.out.println("favoriteList size = "+favoriteList.size());
		// for (int j = 0; j < favoriteList.size(); j++) {
		// //2.遍历list信息，从临界点开始
		// for (int k = 0; k < list_clone.size(); k++) {
		// //3.判断是否有图片，如果有图片，直接结束当前循环
		// PhotoItemInfo info = list_clone.get(k);
		// if (null!=info.list&&info.list.size()>0) {//有图片
		// temp = k;
		// }else {//没有图片
		// //4.如果没有图片，判断locationid是否一致
		// if (info.locationId.equals(favoriteList.get(j))) {
		// //5.如果一致，并且没有photo信息的，把那一项移动到没有photo的顶部，结束当前循环
		// info.islove = 1;
		// System.out.println("find the islove location==========");
		//
		// //移动列表位置，从当前位置移动到temp位置
		// if (app.photoPassPicList.size()>0) {//如果有照片，则需要加1；如果没有照片，不需要加1
		// temp++;
		// }
		// list_clone.add(temp, info);//复制当前对象到临界点的位置
		// list_clone.remove(k+1);//删除原有的位置（需要+1）
		// break;
		// }
		// }
		// }
		// }
	}

	// 处理本地照片的刷新数据
	private void dealLocalRefreshedData() {
		// TODO Auto-generated method stub
		Log.d(TAG, "dealLocalRefreshedData");
		try {
			getMagicData();
			// 将magic和photopass列表放入all中
			if (!app.allPicList.containsAll(photoPassPictureList)) {
				System.out.println("all lIst 不包含photopasspicturelist");
				app.allPicList.addAll(photoPassPictureList);
			}
			app.allPicList.addAll(magicPicList);
			Collections.sort(app.allPicList);// 对all进行排序
			System.out.println("location is ready");
			handler.sendEmptyMessage(LOAD_COMPLETED);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d(TAG, "------>completed");
	}

	/**
	 * 添加到已购买的列表
	 * 
	 * @param info
	 */
	private void addToBoughtList(PhotoInfo info, int position) {
		PhotoItemInfo photoItemInfo;
		boolean isContains = false;
		// 判断是否已经购买
		if (info.isPayed == 1) {// 已购买状态，需要将图片放到bought列表中
			// Log.d(TAG, "add to bought list");
			for (int j = 0; j < app.boughtPicList.size(); j++) {
				// Log.d(TAG, "检查之前的是否存在");
				if (info.shootTime.equals(app.boughtPicList.get(j).shootTime)
						&& info.locationId
								.equals(app.boughtPicList.get(j).locationId)) {
					// Log.d(TAG, "已经存在于bought列表");
					app.boughtPicList.get(j).list.add(info);
					isContains = true;
					break;
				}
			}
			if (!isContains) {// 没有
				// Log.d(TAG, "不存在于之前的已购买的列表");
				// 初始化item的信息
				photoItemInfo = new PhotoItemInfo();
				photoItemInfo.locationId = info.locationId;
				photoItemInfo.shootTime = info.shootTime;
				photoItemInfo.place = locationList.get(position).place;
				photoItemInfo.list.add(info);
				photoItemInfo.placeUrl = locationList.get(position).placeUrl;
				photoItemInfo.latitude = locationList.get(position).latitude;
				photoItemInfo.longitude = locationList.get(position).longitude;
				photoItemInfo.islove = 0;
				photoItemInfo.shootOn = info.shootOn;
				app.boughtPicList.add(photoItemInfo);
			} else {
				isContains = false;
			}
		} else {
			// Log.d(TAG, "not add to bought list");

		}
	}

	/**
	 * 遍历所有magic图片信息 1.判断现有列表是否已经存在 2.如果存在，直接添加在item列表后面，并且将shootOn的值更新为最大的
	 * 3.如果不存在，新建item
	 * 
	 * @throws ParseException
	 */
	private void getMagicData() throws ParseException {
		Log.d(TAG, "----------->get magic photos" + app.magicPicList.size()
				+ "____" + magicPicList.size());
		PhotoItemInfo photoItemInfo;
		boolean clone_contains = false;
		Date date1;
		Date date2;
		magicPicList.clear();// 添加之前，先清除，防止添加pp/pp+造成数据重复添加
		for (int i = 0; i < app.magicPicList.size(); i++) {
			System.out.println("photo shoot time is "
					+ app.magicPicList.get(i).shootOn);
			for (int j = 0; j < magicPicList.size(); j++) {
				if (app.magicPicList.get(i).shootTime.equals(magicPicList
						.get(j).shootTime)) {
					magicPicList.get(j).list.add(app.magicPicList.get(i));
					date1 = sdf.parse(app.magicPicList.get(i).shootOn);
					date2 = sdf.parse(magicPicList.get(j).shootOn);
					if (date1.after(date2)) {
						magicPicList.get(j).shootOn = app.magicPicList.get(i).shootOn;
					}
					clone_contains = true;
					break;
				}
			}
			// 判断是否需要new
			if (!clone_contains) {// 如果之前没有找到，说明需要new
				photoItemInfo = new PhotoItemInfo();
				System.out.println("shootTime:"
						+ app.magicPicList.get(i).shootTime);
				photoItemInfo.shootTime = app.magicPicList.get(i).shootTime;
				photoItemInfo.place = getString(R.string.magic_location);
				photoItemInfo.list.add(app.magicPicList.get(i));
				photoItemInfo.shootOn = app.magicPicList.get(i).shootOn;
				magicPicList.add(photoItemInfo);
			} else {
				clone_contains = false;
			}
		}
	}

	/**
	 * 1.遍历list，判断有没有图片，有，next 2.判断是不是favorite，有，next 3.判断是不是大于或者等于对比值，是，next
	 * 4.找到位置
	 * 
	 * @param locationId
	 * @return
	 */
	// private int getTargetPosition(String locationId) {
	// // TODO Auto-generated method stub
	// for (int i = 0; i < list.size(); i++) {
	// if (null!=list.get(i).list&&list.get(i).list.size()>0) {//是不是有图片
	//
	// }else if (list.get(i).islove == 1) {//是不是favorite
	//
	// }else if
	// (Integer.valueOf(locationId)>=Integer.valueOf(list.get(i).locationId))
	// {//判断locationId的大小，决定需要插入的顺序，如果大于或者等于，说明目标地址在后面
	//
	// }else {//找到位置
	// return i;
	// }
	// }
	//
	// return list.size()-1;
	// }
	/**
	 * 刷新数据的处理 1.对所有获取的信息按照locationid和shoottime分类，放入一个新的photoiteminfo中
	 * 2.加入list中，判断新的locationid是否在无photo的locationid中，如果有，需要重新排列list
	 * 3.并且更新adapter
	 * */
	private void getrefreshdata() {
		Log.e("getdata", "refreshdata");
		// 根据数量，加入新的item
		System.err.println("all update data=" + app.photoPassPicList.size());
		PhotoItemInfo itemInfo;
		// int needmove = -1;//移动，如果有图片的location，会需要移动到顶部
		boolean findLocation = false;
		// 先清除之前旧的列表
		app.allPicList.removeAll(photoPassPictureList);
		// app.allPicList.addAll(magicPicList);

		// 将图片按照location加载到list中去
		for (int l = app.photoPassPicList.size() - refreshDataCount; l < app.photoPassPicList
				.size(); l++) {// 遍历所要添加的图片list
			System.out.println("遍历照片");
			PhotoInfo info = app.photoPassPicList.get(l);
			// 查找list_clone有图片的item，如果找到locationid，在判断是否有同一天的photos，如果有同一天的，add进去，如果没有，新建一个项
			for (int j = 0; j < photoPassPictureList.size(); j++) {// 遍历list，查找locationid一样的内容
				System.out.println("遍历地址");
				PhotoItemInfo p = photoPassPictureList.get(j);
				if (info.locationId.equals(p.locationId)
						|| p.locationIds.contains(info.locationId)) {// 如果locationId和photo的locationid一样
					System.out.println("location一样");
					findLocation = true;
					if (info.shootTime.equals(p.shootTime)) {// 如果shoottime一致，则插入到列表中
						System.out.println("shootTime一致，直接插入列表");
						// 比较时间，按照时间排序
						for (int i = 0; i < p.list.size(); i++) {
							try {
								// System.out.println("date1--->"+p.list.get(i).shootOn);
								// System.out.println("date2--->"+info.shootOn);

								Date date1 = sdf.parse(p.list.get(i).shootOn);
								Date date2 = sdf.parse(info.shootOn);// 获取列表中的时间

								// System.out.println("date1--->"+date1);
								// System.out.println("date2--->"+date2);
								if (date2.after(date1)) {// 需要添加的时间是最新的，显示在最前面
									System.out
											.println("the lastest time, need add");
									p.list.add(i, info);
									System.out
											.println("size->" + p.list.size());
									p.shootOn = info.shootOn;// 更新shootOn的时间
									break;
								} else {
									if (i == (p.list.size() - 1)) {// 如果已经在最后一张了，直接添加在最后面
										System.out
												.println("the last position, need add");
										p.list.add(info);
										System.out.println("size->"
												+ p.list.size());
										p.shootOn = info.shootOn;// 更新shootOn的时间
										break;
									} else {

										System.out.println("scan next------>");
									}
								}
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}// 获取列表中的时间
						}
						// System.out.println("after add new photo------>");
						// 记录当前的列表的索引
						// needmove = j;
					} else {// 时间不一致，新建列表
						System.out.println("时间不一致，新建列表");
						itemInfo = new PhotoItemInfo();
						itemInfo.locationId = p.locationId;
						itemInfo.locationIds = p.locationIds;
						itemInfo.shootTime = info.shootTime;
						itemInfo.place = p.place;
						itemInfo.list.add(0, info);
						System.out.println("size->" + itemInfo.list.size());
						itemInfo.placeUrl = p.placeUrl;
						itemInfo.latitude = p.latitude;
						itemInfo.longitude = p.longitude;
						itemInfo.shootOn = info.shootOn;
						// itemInfo.gps = p.gps;
						itemInfo.islove = p.islove;
						photoPassPictureList.add(0, itemInfo);// 放置到列表的顶部
					}
					// }
					break;
				}
			}
			if (findLocation) {// 如果之前已经找到了对应的位置
				System.out.println("找到位置");
				findLocation = false;
			} else {// 如果之前没有找到对应的位置，遍历地址列表，需要新建一个item，并且放入到最上方
				for (int k = 0; k < locationList.size(); k++) {
					System.out.println("没有找到位置，遍历location");
					if (info.locationId.equals(locationList.get(k).locationId)
							|| locationList.get(k).locationIds
									.contains(info.locationId)) {
						System.out.println("找到其他的location");
						itemInfo = new PhotoItemInfo();
						itemInfo.locationId = locationList.get(k).locationId;
						itemInfo.locationIds = locationList.get(k).locationIds
								.toString();
						itemInfo.shootTime = info.shootTime;
						itemInfo.place = locationList.get(k).place;
						itemInfo.list.add(info);
						itemInfo.placeUrl = locationList.get(k).placeUrl;
						itemInfo.latitude = locationList.get(k).latitude;
						itemInfo.longitude = locationList.get(k).longitude;
						itemInfo.islove = 0;
						itemInfo.shootOn = info.shootOn;
						photoPassPictureList.add(0, itemInfo);
						break;
					}

				}
			}
			// 处理列表的置顶操作
			// if (needmove != -1 && needmove != 0)
			// {//如果不为默认值，或者不为第0项（不在顶端），说明需要置顶操作
			// System.out.println("删除操作");
			// PhotoItemInfo moveInfo = photoPassPictureList.get(needmove);
			// photoPassPictureList.remove(needmove);//先删除对应项
			// photoPassPictureList.add(0, moveInfo);//置顶
			// needmove = -1;//重置标记
			// }
		}
		app.allPicList.addAll(photoPassPictureList);
		System.out.println("start-----------> all sort");
		Collections.sort(app.allPicList);// 对all进行排序
		System.out.println("start-----------> photoPass sort");
		Collections.sort(photoPassPictureList);
		// app.allPicList.addAll(magicPicList);
	}

	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		Intent i = null;
		switch (v.getId()) {
		// 扫描按钮
		case R.id.story_scan:
		case R.id.storyScanRelativeLayout:
		case R.id.story_no_pp_scan:
			System.out.println("scan");
			i = new Intent(getActivity(), MipCaptureActivity.class);
			startActivity(i);
			break;

		case R.id.story_more:
			storyMenuPop.showAsDropDown(v);
			break;

		default:
			break;
		}

	}

	/**
	 * 刷新所有数据，相当于重新访问一次API
	 * */
	// @Override
	// public void onRefresh() {
	//
	// //刷新之后清除通知栏并清除红色数字。
	// // if (MainTabActivity.maintabbadgeView.isShown()) {//只获取最新数据
	// System.out.println("get new info");
	// //
	// API.getPhotosByConditions(sharedPreferences.getString(Common.USERINFO_TOKENID,
	// null),mHandler,sharedPreferences.getString(Common.LAST_UPDATE_PHOTO_TIME,
	// null));//获取更新信息
	// System.out.println("refreshing in callback");
	// if (MainTabActivity.maintabbadgeView.isShown()) {
	// MainTabActivity.maintabbadgeView.hide();
	// }
	// app.setPushPhotoCount(0);
	// NotificationManager mgr = (NotificationManager)
	// context.getSystemService(Context.NOTIFICATION_SERVICE);
	// mgr.cancel(0);
	// // }else {//获取全部数据
	// // System.out.println("refresh");
	// // isLoading = false;
	// // API.getPhotosByConditions(getActivity(),mHandler,null);//刷新全部数据
	// // }
	// }

	// 执行下拉刷新
	public static void doRefresh() {
		// dialog.show();
		if (sharedPreferences.getInt(Common.PP_COUNT, 0) > 1) {
			storyViewPagerAdapter.startRefresh();
		}
		// System.out.println("refresh in funchting");

	}

	public static int getCurrentViewPager() {
		return storyViewPager.getCurrentItem();
	}

	// /**
	// * 加载更多,每次加载10条，每次加载更多，就多加载10条
	// */
	// @Override
	// public void onLoadMore() {
	// }

	/**
	 * 关闭定位服务
	 * */
	private void stopService() {
		Log.d(TAG, "stop service");
		new Thread() {
			public void run() {
				while (app.mLocation != null && app.getLocationState()) {
					if (app.mLocationClient.isStarted()) {
						Log.d(TAG, "------>location is running");
						app.disableLocation(true);
					}
				}
			};
		}.start();
	}

}
