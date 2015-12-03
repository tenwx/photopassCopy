package com.pictureair.photopass.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.activity.BaseFragment;
import com.pictureair.photopass.activity.UpdateCallback;
import com.pictureair.photopass.adapter.DiscoverLocationAdapter;
import com.pictureair.photopass.entity.DiscoverLocationItemInfo;
import com.pictureair.photopass.entity.LocationItem;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.API;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.JsonUtil;
import com.pictureair.photopass.util.LocationUtil;
import com.pictureair.photopass.util.LocationUtil.LocationNotification;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.MyToast;
import com.pictureair.photopass.widget.NoNetWorkOrNoCountView;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
/**
 * 发现页面，显示各个地点的与当前的距离，可以筛选各个地方，可支持导航
 * @author bauer_bao
 *
 */
public class FragmentPageDiscover extends BaseFragment implements UpdateCallback, OnClickListener, LocationNotification{
	
	//声明控件
	private LinearLayout discoverPopularityLinearLayout, discoverDistanceLinearLayout, discoverSelectionLinearLayout, discoverCollectionLinearLayout, discoverTopLinearLayout;
	private ImageView cursorImageView;
	private ListView discoverListView;
	private ImageView moreImageView;
	private ImageView popularityIconImageView, distanceIconImageView, selectionIconImageView, collectionIconImageView;
	private TextView popularityTextView, distanceTextView, selectionTextView, collectionTextView;
	private NoNetWorkOrNoCountView noNetWorkOrNoCountView;
	
	//申明类
	private MyToast myToast;
	private DiscoverLocationAdapter discoverLocationAdapter;
	private SensorManager sensorManager;// 传感器管理器
	private Sensor sensor_orientation;
	private MySensorEventListener mySensorEventListener;// 传感器监听
	private MyApplication app;
	private Thread locationThread;
	private DiscoverLocationItemInfo info;
//	private CustomProgressDialog dialog;
	private SharedPreferences sharedPreferences;
	private LocationUtil locationUtil;
	
	//申明变量
	private static String TAG = "FragmentPageDiscover";
	private static final int CHANGE_LOCATION = 111;
	private static final int STOP_LOCATION = 222;
	private static final int FINISH_LOADING = 333;
	private int currentIndex;//当前选择的tab索引值
	private int offset;//动画条的偏移量
	private float rotate_degree = 0;// x轴的旋转角度
	private ArrayList<DiscoverLocationItemInfo> locationList;
	private ArrayList<String> favoriteList;
	private NumberFormat distanceFormat;//距离的小数点
	private boolean isLoading = false;// 加载线程判断
	private boolean locationStart = false; //开启实时定位标记
	private int activateLocationCount = 0;//记录总共激活定位的数量
	private int locationActivatedIndex = -1;//记录激活定位的索引值
	private boolean showTab = false;
	
	//声明handler消息回调机制
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			//成功获取地址，需要获取已收藏的地址
			case API.GET_LOCATION_SUCCESS:
				//获取全部的location
				Log.d(TAG, "get location success============"+ msg.obj);
				try {
					JSONObject response = new JSONObject(msg.obj.toString());
					JSONArray resultArray = response.getJSONArray("locations");
					for (int i = 0; i < resultArray.length(); i++) {
						DiscoverLocationItemInfo locationInfo = new DiscoverLocationItemInfo();
						JSONObject object = resultArray.getJSONObject(i);
						locationInfo = JsonUtil.getLocation(object);
						locationList.add(locationInfo);
					}
					locationUtil.setLocationItemInfos(locationList, FragmentPageDiscover.this);
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
				API.getFavoriteLocations(sharedPreferences.getString(Common.USERINFO_TOKENID, null), handler);
				break;
				
			case API.GET_FAVORITE_LOCATION_FAILED:
			case API.GET_LOCATION_FAILED://获取地址失败	
				noNetWorkOrNoCountView.setVisibility(View.VISIBLE);
				noNetWorkOrNoCountView.setResult(R.string.no_network, R.string.click_button_reload, R.string.reload, R.drawable.no_network, handler, true);
				discoverListView.setVisibility(View.GONE);
//				myToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
//				if(dialog.isShowing()){
//					dialog.dismiss();
//				}
				break;
				
			case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_RELOAD://noView的按钮响应重新加载点击事件
				//重新加载购物车数据
				System.out.println("onclick with reload");
				locationList.clear();
				favoriteList.clear();
//				dialog = CustomProgressDialog.show(getActivity(), getString(R.string.is_loading), false, null);
				API.getLocationInfo(getActivity(),handler);//获取所有的location
//				if (ACache.get(getActivity()).getAsString(Common.LOCATION_INFO) == null) {//地址获取失败
//					API.getLocationInfo(getActivity(),handler);//获取所有的location
//				}else {//地址获取成功，但是照片获取失败
//					Message message = handler.obtainMessage();
//					message.what = API.GET_LOCATION_SUCCESS;
//					message.obj = ACache.get(getActivity()).getAsString(Common.LOCATION_INFO);
//					handler.sendMessage(message);
//				}
				break;
				
			//获取收藏地址成功
			case API.GET_FAVORITE_LOCATION_SUCCESS:
				Log.d(TAG, "get favorite success");
				try {
					JSONObject favoriteJsonObject = (JSONObject) msg.obj;
					JSONArray jsonArray = favoriteJsonObject.getJSONArray("favoriteLocations");
					for (int i = 0; i < jsonArray.length(); i++) {
						favoriteList.add(jsonArray.getString(i));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//设置location的isLove属性
				for (int i = 0; i < favoriteList.size(); i++) {
					for (int j = 0; j < locationList.size(); j++) {
						if (favoriteList.get(i).equals(locationList.get(j).locationId)) {
							locationList.get(j).islove = 1;
							break;
						}
					}
				}
				//开启线程等待第一次定位的结束，如果结束了，就显示出来
				new Thread(){
					public void run() {
						while (!isLoading) {
							if (locationUtil.mapLocation != null) {
								Looper.prepare();
								isLoading = true;
								Log.d(TAG, "location is ready");
								discoverLocationAdapter = new DiscoverLocationAdapter(locationList, getActivity(), handler, locationUtil.mapLocation, rotate_degree);
								discoverLocationAdapter.setUpdateCallback(FragmentPageDiscover.this);
								handler.sendEmptyMessage(FINISH_LOADING);
							}
						}
					};
				}.start();
				break;
				
			//加载完毕之后
			case FINISH_LOADING:
				discoverListView.setVisibility(View.VISIBLE);
				noNetWorkOrNoCountView.setVisibility(View.GONE);
				discoverListView.setAdapter(discoverLocationAdapter);
				discoverListView.setOnScrollListener(new DiscoverOnScrollListener());
//				if(dialog.isShowing()){
//					dialog.dismiss();
//				}
				break;
				
			//定位的时候
			case CHANGE_LOCATION:
				info = locationList.get(msg.arg1);
				LocationItem item = (LocationItem) msg.obj;
				final double lat_a = info.latitude;// 纬度,目标地点不会变化
				final double lng_a = info.longitude;// 经度
				double lat_b = locationUtil.mapLocation.getLatitude();//获取当前app经纬度
				double lng_b = locationUtil.mapLocation.getLongitude();
				double distance = Math.round(AppUtil.getDistance(lng_a, lat_a, lng_b, lat_b));
//				double distance = Math.round((double) AppUtil.gps2m(lat_a, lng_a, lat_b, lng_b));
				// 距离
				item.distanceTextView.setText(AppUtil.getSmartDistance(distance, distanceFormat));
				double d = -AppUtil.gps2d(lat_a, lng_a, lat_b, lng_b);
				// 角度
				item.locationLeadImageView.setRotation((float) d - rotate_degree);
				break;
				
			//停止定位
			case STOP_LOCATION:
				LocationItem item2 = (LocationItem) msg.obj;
				item2.locationLeadImageView.setImageResource(R.drawable.direction_icon);
				break;
				
			case DiscoverLocationAdapter.MAGICSHOOT:
				Log.d(TAG, "magic shoot on click");
				break;
				
			case DiscoverLocationAdapter.STOPLOCATION:
				locationStart = false;
				break;
				
			case API.EDIT_FAVORITE_LOCATION_SUCCESS:
				try {
					JSONObject jsonObject = (JSONObject) msg.obj;
					if (jsonObject.has("success") && jsonObject.getBoolean("success")) {
						discoverLocationAdapter.updateIsLove(msg.arg1);
					}else {
						myToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
				
				//收藏地点失败
			case API.EDIT_FAVORITE_LOCATION_FAILED:
				myToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
				break;
				
			default:
				break;
			}
		};
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_discover, null);
		
		//初始化控件
		discoverPopularityLinearLayout = (LinearLayout)view.findViewById(R.id.discover_linearlayout_popularity);
		discoverDistanceLinearLayout = (LinearLayout)view.findViewById(R.id.discover_linearlayout_distance);
		discoverSelectionLinearLayout = (LinearLayout)view.findViewById(R.id.discover_linearlayout_selection);
		discoverCollectionLinearLayout = (LinearLayout)view.findViewById(R.id.discover_linearlayout_collection);
		discoverTopLinearLayout = (LinearLayout)view.findViewById(R.id.discover_top);
		noNetWorkOrNoCountView = (NoNetWorkOrNoCountView)view.findViewById(R.id.discoverNoNetWorkView);
		cursorImageView = (ImageView)view.findViewById(R.id.discover_cursor);
		discoverListView = (ListView)view.findViewById(R.id.discover_listView);
		moreImageView = (ImageView)view.findViewById(R.id.discover_more);
		popularityIconImageView = (ImageView)view.findViewById(R.id.discover_imageview_popularity);
		distanceIconImageView = (ImageView)view.findViewById(R.id.discover_imageview_distance);
		selectionIconImageView = (ImageView)view.findViewById(R.id.discover_imageview_selection);
		collectionIconImageView = (ImageView)view.findViewById(R.id.discover_imageview_collection);
		popularityTextView = (TextView)view.findViewById(R.id.discover_textview_popularity);
		distanceTextView = (TextView)view.findViewById(R.id.discover_textview_distance);
		selectionTextView = (TextView)view.findViewById(R.id.discover_textview_selection);
		collectionTextView = (TextView)view.findViewById(R.id.discover_textview_collection);
		
		//声明方向传感器
		mySensorEventListener = new MySensorEventListener();
		sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
		sensor_orientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		
		//绑定监听
		discoverPopularityLinearLayout.setOnClickListener(new LeadingTabOnClickListener(0));
		discoverDistanceLinearLayout.setOnClickListener(new LeadingTabOnClickListener(1));
		discoverSelectionLinearLayout.setOnClickListener(new LeadingTabOnClickListener(2));
		discoverCollectionLinearLayout.setOnClickListener(new LeadingTabOnClickListener(3));
		moreImageView.setOnClickListener(this);
		   
		//初始化数据
		changeClickEffect(0);
		offset = ScreenUtil.getScreenWidth(getActivity()) / 4;//偏移量
		myToast = new MyToast(getActivity());
		locationUtil = new LocationUtil(getActivity());
		app = (MyApplication) getActivity().getApplication();
		locationStart = true;
		startService();
		locationList = new ArrayList<DiscoverLocationItemInfo>();
		favoriteList = new ArrayList<String>();
		distanceFormat = NumberFormat.getNumberInstance();
		distanceFormat.setMaximumFractionDigits(1);
		sharedPreferences = getActivity().getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
		
		//获取数据
//		dialog = CustomProgressDialog.show(getActivity(), getString(R.string.is_loading), false, null);
		getLocationData();
		
		return view;
	}
	
	//获取地点数据
	private void getLocationData() {
		if (ACache.get(getActivity()).getAsString(Common.LOCATION_INFO)==null) {
			API.getLocationInfo(getActivity(),handler);//获取所有的location
		}else {
			Message message = handler.obtainMessage();
			message.what = API.GET_LOCATION_SUCCESS;
			message.obj = ACache.get(getActivity()).getAsString(Common.LOCATION_INFO);
			handler.sendMessage(message);
		}
	}
	
	@Override
	public void onStop() {
		Log.d(TAG, "stop============");
		stopService();
		super.onStop();

	}
	
	@Override
	public void onResume() {
		MobclickAgent.onPageStart("FragmentPageDiscover"); //统计页面
		isLoading = false;
		locationStart = true;
		startService();
		super.onResume();
	}
	
	@Override
	public void onPause() {
		 MobclickAgent.onPageEnd("FragmentPageDiscover"); 
		locationStart = false;
//		if(dialog.isShowing()){
//			dialog.dismiss();
//		}
		super.onPause();
	}
	
	@Override
	public void onDestroyView() {
		Log.d(TAG, "ondestroy===========");
		locationStart = false;
		isLoading = false;
		super.onDestroyView();
	}
	
	//顶部导航栏的点击事件类
	private class LeadingTabOnClickListener implements OnClickListener{
		private int currentTab;
		public LeadingTabOnClickListener(int cur) {
			currentTab = cur;
		}
		
		@Override
		public void onClick(View v) {
			Animation animation = new TranslateAnimation(offset * currentIndex, offset * currentTab, 0, 0);
			currentIndex = currentTab;
			animation.setFillAfter(true);
			animation.setDuration(300);
			cursorImageView.startAnimation(animation);
			changeClickEffect(currentTab);
		}
	}
	
	private void changeClickEffect(int cur){
		switch (cur) {
		case 0:
			popularityIconImageView.setImageResource(R.drawable.discover_collection_sele);
			distanceIconImageView.setImageResource(R.drawable.discover_collection_nor);
			selectionIconImageView.setImageResource(R.drawable.discover_collection_nor);
			collectionIconImageView.setImageResource(R.drawable.discover_collection_nor);
			popularityTextView.setTextColor(getResources().getColor(R.color.blue));
			distanceTextView.setTextColor(getResources().getColor(R.color.gray));
			selectionTextView.setTextColor(getResources().getColor(R.color.gray));
			collectionTextView.setTextColor(getResources().getColor(R.color.gray));
			break;
			
		case 1:
			popularityIconImageView.setImageResource(R.drawable.discover_collection_nor);
			distanceIconImageView.setImageResource(R.drawable.discover_collection_sele);
			selectionIconImageView.setImageResource(R.drawable.discover_collection_nor);
			collectionIconImageView.setImageResource(R.drawable.discover_collection_nor);
			popularityTextView.setTextColor(getResources().getColor(R.color.gray));
			distanceTextView.setTextColor(getResources().getColor(R.color.blue));
			selectionTextView.setTextColor(getResources().getColor(R.color.gray));
			collectionTextView.setTextColor(getResources().getColor(R.color.gray));
			break;
			
		case 2:
			popularityIconImageView.setImageResource(R.drawable.discover_collection_nor);
			distanceIconImageView.setImageResource(R.drawable.discover_collection_nor);
			selectionIconImageView.setImageResource(R.drawable.discover_collection_sele);
			collectionIconImageView.setImageResource(R.drawable.discover_collection_nor);
			popularityTextView.setTextColor(getResources().getColor(R.color.gray));
			distanceTextView.setTextColor(getResources().getColor(R.color.gray));
			selectionTextView.setTextColor(getResources().getColor(R.color.blue));
			collectionTextView.setTextColor(getResources().getColor(R.color.gray));
			break;
			
		case 3:
			popularityIconImageView.setImageResource(R.drawable.discover_collection_nor);
			distanceIconImageView.setImageResource(R.drawable.discover_collection_nor);
			selectionIconImageView.setImageResource(R.drawable.discover_collection_nor);
			collectionIconImageView.setImageResource(R.drawable.discover_collection_sele);
			popularityTextView.setTextColor(getResources().getColor(R.color.gray));
			distanceTextView.setTextColor(getResources().getColor(R.color.gray));
			selectionTextView.setTextColor(getResources().getColor(R.color.gray));
			collectionTextView.setTextColor(getResources().getColor(R.color.blue));
			break;

		default:
			break;
		}
	}
	
	/**
	 * 开启定位服务
	 * */
	private void startService() {
		locationUtil.startLocation();
	}
	
	/**
	 * 关闭定位服务
	 * */
	private void stopService() {
		Log.d(TAG, "stop location------->");
		locationUtil.stopLocation();
	}
	
	/**
	 * 局部更新listView
	 * */
	private void updateLocation(int position, View view) {
		int firstVisiblePosition = discoverListView.getFirstVisiblePosition();
		int lastVisiblePosition = discoverListView.getLastVisiblePosition();
		if (position + 1 >= firstVisiblePosition && position - 1 <= lastVisiblePosition) {
			if (view.getTag() instanceof LocationItem && locationStart) {
				LocationItem item = (LocationItem) view.getTag();
				Message msg = new Message();
				msg.obj = item;
				msg.arg1 = position;
				msg.what = CHANGE_LOCATION;
				handler.sendMessage(msg);
			}
		}
	}
	
	//可以得到传感器实时测量出来的变化值
	private class MySensorEventListener implements SensorEventListener {
		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (Sensor.TYPE_ORIENTATION == event.sensor.getType()) {
				rotate_degree = event.values[0];
//				System.out.println("--------->sensor changed");
			}
		}
	}

	//listView滚动监听，判断当前定位激活的位置是否超出屏幕
	private class DiscoverOnScrollListener implements OnScrollListener{

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			if (locationActivatedIndex != -1) {//说明有激活的position
				if (locationActivatedIndex < firstVisibleItem || locationActivatedIndex > discoverListView.getLastVisiblePosition()) {//超出屏幕
//					Log.d(TAG, "out of window------->");
					locationStart = false;
				}
			}
		}
		
	}
	
	@Override
	public void startLocation(final int position, final View view) {
		Log.d(TAG, "start location---------->");
		//获取已经激活定位的arrylist
		final HashMap<String, Integer> activatedHashMap = discoverLocationAdapter.getActivatedLocationMap();
		Log.d(TAG, "start location---------->"+ activatedHashMap.get(position+ ""));
		if (locationActivatedIndex != -1) {//说明之前有item激活过定位服务
			//将arraylist中已激活过的值改为0；
			discoverLocationAdapter.disableLocationActivated(locationActivatedIndex);
		}
		//修改当前激活索引值
		locationActivatedIndex = position;
		if (!locationStart) {
			locationStart = true;
		}
		
		final LocationItem item = (LocationItem) view.getTag();
		item.locationLeadImageView.setImageResource(R.drawable.direction_icon_sele);
		if (activateLocationCount == 0 ) {
			//SENSOR_DELAY_UI采样频率是中等的，比normal要快一点，比game和fast都慢。如果定位觉得不准，是因为没有进行校正。目前校正只能通过画8字手动校准
			sensorManager.registerListener(mySensorEventListener, sensor_orientation, SensorManager.SENSOR_DELAY_UI);//点击定位的时候，注册监听
		}
		activateLocationCount ++;
		locationThread = new Thread(new Runnable() {
			@Override
			public void run() {
				do {
					updateLocation(position, view);
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} while (locationStart && activatedHashMap.get(position+"") == 1);//view.getY() >= 0 && view.getY() <= discoverListView.getHeight() - 100 && 
				//将图标改为静止状态
				Message msg = new Message();
				msg.obj = item;
				msg.arg1 = position;
				msg.what = STOP_LOCATION;
				
				handler.sendMessage(msg);
				activateLocationCount --;
				if (activateLocationCount == 0) {//取消定位
					sensorManager.unregisterListener(mySensorEventListener);//不使用的时候，取消监听
					discoverLocationAdapter.disableLocationActivated(position);
					locationActivatedIndex = -1;
				}
			}
		});
		locationThread.start();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.discover_more:
			if (!showTab) {
				discoverTopLinearLayout.setVisibility(View.VISIBLE);
				showTab = true;
			}else {
				discoverTopLinearLayout.setVisibility(View.GONE);
				showTab = false;
			}
			break;

		default:
			break;
		}
		
	}

	@Override
	public void inOrOutPlace(String locationIds, boolean in) {
		// TODO Auto-generated method stub
		System.out.println("in or out special location......");
	}

	
}