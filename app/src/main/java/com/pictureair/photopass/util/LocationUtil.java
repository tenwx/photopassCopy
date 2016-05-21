package com.pictureair.photopass.util;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.pictureair.photopass.entity.DiscoverLocationItemInfo;

import java.util.ArrayList;

/**
 * 定位封装类
 * @author bauer_bao
 *
 */
public class LocationUtil implements AMapLocationListener{

	private AMapLocationClient locationClient = null;
	private AMapLocationClientOption locationOption = null;
	public AMapLocation mapLocation;
	private Context context;
	private double latitude, longitude;//定位的经纬度
	private boolean isScanFence = false;
	private ArrayList<DiscoverLocationItemInfo> locationItemInfos;//所有的地点集合
	private int currentNotificationPosition = -1;//记录已经进入的地点索引值
	private static final String TAG = "LocationUtil";
	private static final int radius = 40;//指定范围的半径
	/**
	 * 判断是否成功获取定位信息
	 */
	public boolean locationChanged = false;
	private OnLocationNotificationListener onLocationNotificationListener;

	public LocationUtil(Context context){
		this.context = context;
		initLocation();
	}

	public void setLocationItemInfos(ArrayList<DiscoverLocationItemInfo> locationItemInfos, OnLocationNotificationListener onLocationNotificationListener) {
		this.locationItemInfos = locationItemInfos;
		this.onLocationNotificationListener = onLocationNotificationListener;
	}

	private void initLocation() {
		PictureAirLog.d(TAG, "----------->init location");

		if (locationClient != null) {
			PictureAirLog.d(TAG, "------->has started");
			return;
		}
		locationChanged = false;

		locationClient = new AMapLocationClient(context);
		locationOption = new AMapLocationClientOption();
		// 设置定位模式为高精度模式
		locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
		// 设置定位监听
		locationClient.setLocationListener(this);
		/**
		 * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
		 * 注意：只有在高精度模式下的单次定位有效，其他方式无效
		 */
		locationOption.setGpsFirst(true);
		// 设置发送定位请求的时间间隔,最小值为1000，如果小于1000，按照1000算
		locationOption.setInterval(2000);
		// 设置定位参数
		locationClient.setLocationOption(locationOption);
		// 启动定位
		locationClient.startLocation();
	}

	public void startLocation() {
		PictureAirLog.d(TAG, "----------->start location");
		initLocation();
	}

	public void stopLocation() {
		PictureAirLog.d(TAG, "----------->stop location");

		if (null != locationClient) {
			// 停止定位
			locationClient.stopLocation();
			/**
			 * 如果AMapLocationClient是在当前Activity实例化的，
			 * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
			 */
			locationClient.onDestroy();
			locationClient = null;
			locationOption = null;
		}
	}

	@Override
	public void onLocationChanged(AMapLocation arg0) {
		// TODO Auto-generated method stub
		PictureAirLog.out("map" + arg0);
		locationChanged = true;
		if (arg0 != null && arg0.getErrorCode() == 0) {
			latitude = arg0.getLatitude();
			longitude = arg0.getLongitude();
			mapLocation = arg0;
			PictureAirLog.d(TAG, "latitude --->"+ latitude+",longitude--->"+longitude);
			//每次改动location，都需要判断当前是否在扫描定位通知中，如果不是，则启动线程，开始扫描
			if (!isScanFence && locationItemInfos != null && locationItemInfos.size() > 0) {
				scanFence();
			}
		}

	}

	public interface OnLocationNotificationListener{
		/**
		 * 进入和离开的函数
		 * @param locationIds location集合
		 * @param in 是否进入
		 */
		void inOrOutPlace(String locationIds, boolean in);
	}
	
//	//进入或者离开通知区域
//	private void inOrOutPlace(String locationIds) {
//		// TODO Auto-generated method stub
//		PictureAirLog.d(TAG, "in or out of place");
//	}

	/**
	 * 扫描指定地点
	 * 1.开启线程处理
	 * 2.判断之前是否有进入通知地点，如果有，直接判断那个地点是否已经离开；
	 * 3.如果没有，说明没有进入任何一个点，需要遍历全部
	 * 4.判断距离是否超过指定半径，如果超过，说明在地点外，如果没有超过，说明进入指定区域
	 */
	private void scanFence() {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				isScanFence = true;
				if (currentNotificationPosition != -1) {//说明之前已经能够进入
					if (AppUtil.getDistance(locationItemInfos.get(currentNotificationPosition).longitude, 
							locationItemInfos.get(currentNotificationPosition).latitude, 
							mapLocation.getLongitude(), mapLocation.getLatitude()) > radius) {//超过半径，离开后需要继续扫描其他地点，判断是否进入了其他区域
						onLocationNotificationListener.inOrOutPlace(locationItemInfos.get(currentNotificationPosition).locationIds, false);
						currentNotificationPosition = -1;
						PictureAirLog.d(TAG, "out of location notification");
					}else {//说明还在当前区域中，无需做任何操作
						PictureAirLog.d(TAG, "still in location notification");
						isScanFence = false;
						return;
					}
				}
				//判断距离是否超过一定距离，如果超过一定距离，直接跳过定位通知判断，如果在一定距离之内，则开始定位通知
				for (int i = 0; i < locationItemInfos.size(); i++) {//循环遍历所有的location，判断距离是否在指定半径内
					if (AppUtil.getDistance(locationItemInfos.get(i).longitude, 
							locationItemInfos.get(i).latitude, 
							mapLocation.getLongitude(), mapLocation.getLatitude()) > radius) {//超过半径
						PictureAirLog.d(TAG, "skip location notification");
					}else {//进入通知区域后，其他区域不需要再计算，这样的问题，有可能有重叠区域无法准确判断
						PictureAirLog.d(TAG, "in location notification");
						currentNotificationPosition = i;
						onLocationNotificationListener.inOrOutPlace(locationItemInfos.get(currentNotificationPosition).locationIds, true);
						break;
					}
				}

				isScanFence = false;


			}
		}).start();
	}
}
