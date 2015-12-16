package com.pictureair.photopass.adapter;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.nineoldandroids.view.ViewHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.UpdateCallback;
import com.pictureair.photopass.blur.UtilOfDraw;
import com.pictureair.photopass.entity.DiscoverLocationItemInfo;
import com.pictureair.photopass.entity.LocationItem;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.ScreenUtil;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
/**
 * discover页面location的适配器
 * @author bauer_bao
 *
 */
public class DiscoverLocationAdapter extends BaseAdapter {
	private ArrayList<DiscoverLocationItemInfo> list;
	private Handler mHandler;
	private Context context;
	private DiscoverLocationItemInfo info;
	private LayoutInflater layoutInflater;
	//距离的小数点数量
	private NumberFormat distanceFormat;
	//距离和方向
	private AMapLocation mLocation;
	private float x = 0;// x轴的旋转角度
	private UpdateCallback updateCallback;
	private int screenWidth = 0;
	
	public static final int MAGICSHOOT = 101;
	public static final int LOVE = 102;
	public static final int MORE = 103;
	public static final int STOPLOCATION = 104;
	private static final String TAG = "DicoverLocationAdapter";
	
	private ObjectAnimator closeAnimator;
	private SharedPreferences sharedPreferences;
	
	private HashMap<String, Integer> activatedLocationMap;
	
	private DisplayImageOptions options;
	
	private ImageLoader imageLoader;
	
	public DiscoverLocationAdapter(ArrayList<DiscoverLocationItemInfo> list, Activity context, Handler hander, AMapLocation location, float x) {
		this.list = list;
		this.context = context;
		this.mHandler = hander;
		this.mLocation = location;
		this.x = x;
		distanceFormat = NumberFormat.getNumberInstance();
		distanceFormat.setMaximumFractionDigits(1);
		layoutInflater = LayoutInflater.from(context);
		screenWidth = ScreenUtil.getScreenWidth(context);
		activatedLocationMap = new HashMap<String, Integer>();
		sharedPreferences = context.getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
		options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_discover_loading).
				showImageOnFail(R.drawable.ic_discover_failed).cacheInMemory(true).cacheOnDisk(true).build();
		imageLoader = ImageLoader.getInstance();
	}

	public HashMap<String, Integer> getActivatedLocationMap() {
		return activatedLocationMap;
	}

	public void setActivatedLocationMap(
			HashMap<String, Integer> activatedLocationMap) {
		this.activatedLocationMap = activatedLocationMap;
	}

	public ArrayList<DiscoverLocationItemInfo> getList() {
		return list;
	}

	public void setList(ArrayList<DiscoverLocationItemInfo> list) {
		this.list = list;
	}

	public UpdateCallback getUpdateCallback() {
		return updateCallback;
	}

	public void setUpdateCallback(UpdateCallback updateCallback) {
		this.updateCallback = updateCallback;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	//取消之前激活过的定位
	public void disableLocationActivated(int position){
		activatedLocationMap.put(position + "", 0);
	}
	
	//更新收藏标记
	public void updateIsLove(int position){
		if (list.get(position).islove == 0) {//收藏
			list.get(position).islove = 1;
			
		}else if (list.get(position).islove == 1) {//取消收藏
			list.get(position).islove = 0;
			
		}
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LocationItem viewHolder = null;
		if (null == convertView) {
			convertView = layoutInflater.inflate(R.layout.discover_listview_item, null);
			viewHolder = new LocationItem();
			viewHolder.locationNameTextView = (TextView) convertView.findViewById(R.id.place_name);//地点名称
			viewHolder.makegiftImageView = (ImageView) convertView.findViewById(R.id.discover_makegift);//制作图片按钮
			viewHolder.locationPhotoImageView = (ImageView) convertView.findViewById(R.id.discover_location_photo);//地点默认背景图片
			viewHolder.favoriteImageView = (ImageView) convertView.findViewById(R.id.discover_love);//喜爱按钮
			viewHolder.locationLeadImageView = (ImageView) convertView.findViewById(R.id.discover_direction);//地点导航
			viewHolder.distanceTextView = (TextView) convertView.findViewById(R.id.discover_distance);//距离
			viewHolder.showDetailImageView = (ImageView) convertView.findViewById(R.id.discover_show_detail);//显示详情按钮
			viewHolder.locationDetailLayout = (RelativeLayout) convertView.findViewById(R.id.discover_location_detail_info);//详情layout
//			viewHolder.locationDetailNameTextView = (TextView) convertView.findViewById(R.id.discover_detail_place_name);//详情的地点名称
			viewHolder.locationDetailInfoTextView = (TextView) convertView.findViewById(R.id.discover_place_introduce);//地点的详情介绍
			viewHolder.locationBlurPhotoImageView = (ImageView) convertView.findViewById(R.id.discover_location_blur_photo);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (LocationItem) convertView.getTag();
		}
		//初始化数据
		final View view = convertView;
		info  = list.get(position);
		//设置地点名称
		viewHolder.locationNameTextView.setText(info.place);
//		viewHolder.locationDetailNameTextView.setText(info.place);
		//设置地点详情
		viewHolder.locationDetailInfoTextView.setText(info.placeDetailIntroduce);
		//设置背景图片
//		UniversalImageLoadTool.loadDiscoverImage(info.placeUrl, viewHolder.locationPhotoImageView, options);
		if (viewHolder.locationPhotoImageView.getTag() != null && viewHolder.locationPhotoImageView.getTag().equals(info.placeUrl)) {//直接显示，不需要tag
			System.out.println("不需要加载");
		}else {
			System.out.println("需要加在图片"+viewHolder.locationPhotoImageView.getTag());
			ImageAware imageAware = new ImageViewAware(viewHolder.locationPhotoImageView, false);
			
			imageLoader.displayImage(info.placeUrl, imageAware, options);
			viewHolder.locationPhotoImageView.setTag(info.placeUrl);
		}
		//设置模糊背景图片
//		UniversalImageLoadTool.loadBlurImage(info.placeUrl, viewHolder.locationBlurPhotoImageView, options);
		if (viewHolder.locationBlurPhotoImageView.getTag() != null && viewHolder.locationBlurPhotoImageView.getTag().equals(info.placeUrl)) {
			System.out.println("不需要加载模糊图片");
		}else {
			System.out.println("需要加载模糊图片" + viewHolder.locationBlurPhotoImageView.getTag());
			ImageAware imageAware = new ImageViewAware(viewHolder.locationBlurPhotoImageView, false);
			viewHolder.locationBlurPhotoImageView.setTag(info.placeUrl);
			final ImageView imageView = viewHolder.locationBlurPhotoImageView;
			imageLoader.displayImage(info.placeUrl, imageAware, options, new SimpleImageLoadingListener(){
				@Override
				public void onLoadingComplete(String imageUri, View view,
						Bitmap loadedImage) {
					// TODO Auto-generated method stub
					super.onLoadingComplete(imageUri, view, loadedImage);
					imageView.setImageBitmap(UtilOfDraw.blur(loadedImage));
				}
			});
		}
		//设置导航监听
		final int nPosition = position;
		viewHolder.locationLeadImageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if (null != updateCallback) {
					if (activatedLocationMap.get(nPosition+"") == null || activatedLocationMap.get(nPosition+"") == 0) {//开启定位服务
						activatedLocationMap.put(nPosition+"", 1);
						Log.d(TAG, "start location = 1");
						updateCallback.startLocation(nPosition, view);
					}else {
						
						Log.d(TAG, "stop location");
						activatedLocationMap.put(nPosition+"", 0);
						mHandler.sendEmptyMessage(STOPLOCATION);
					}
				}
			}
		});
		//获得经纬度坐标
		double lat_a = info.latitude;// 纬度
		double lng_a = info.longitude;// 经度
		double lat_b = (mLocation!= null) ? mLocation.getLatitude() : 0;
		double lng_b = (mLocation!= null) ? mLocation.getLongitude() : 0;
//		double distance = Math.round((double) AppUtil.gps2m(lat_a, lng_a, lat_b, lng_b));
		double distance = Math.round(AppUtil.getDistance(lng_a, lat_a, lng_b, lat_b));
		System.out.println("distance--in adapter--------->"+distance);
		viewHolder.distanceTextView.setText(AppUtil.getSmartDistance(distance, distanceFormat));
		//获取旋转角度
		double d = -AppUtil.gps2d(lat_a, lng_a, lat_b, lng_b);
		viewHolder.locationLeadImageView.setRotation((float) d - x);
		//设置喜爱按钮的监听
		viewHolder.favoriteImageView.setOnClickListener(new OnItemChildClickListener(LOVE, position, viewHolder));
		//设置喜爱按钮的初始化数据
		if (info.islove == 1) {
			viewHolder.favoriteImageView.setImageResource(R.drawable.discover_like);
		} else {
			viewHolder.favoriteImageView.setImageResource(R.drawable.discover_no_like);
		}
		//设置制作按钮的监听
		viewHolder.makegiftImageView.setOnClickListener(new OnItemChildClickListener(MAGICSHOOT, position, viewHolder));
		//设置显示详情的按钮监听
		viewHolder.showDetailImageView.setOnClickListener(new OnItemChildClickListener(MORE, position, viewHolder));
		//初始化详情地址的可视状态
		if (info.showDetail == 1) {
			viewHolder.showDetailImageView.setImageResource(R.drawable.discover_show_detail);
			viewHolder.locationDetailLayout.setVisibility(View.VISIBLE);
		}else {
			viewHolder.showDetailImageView.setImageResource(R.drawable.discover_hide_detail);
			viewHolder.locationDetailLayout.setVisibility(View.GONE);
			ViewHelper.setTranslationX(viewHolder.locationPhotoImageView, 0);
		}
		
		return convertView;
	}
	
	/**适配器只做布局和数据加载，后续操作逻辑由fragmentPage1完成*/
	private class OnItemChildClickListener implements OnClickListener {
		private int clickIndex;// 点击了哪个控件
		private int position;// 点击了第几条
		private LocationItem locationItem;

		public OnItemChildClickListener(int clickIndex, int position, LocationItem locationItem) {
			this.clickIndex = clickIndex;
			this.position = position;
			this.locationItem = locationItem;
		}

		@Override
		public void onClick(View v) {
			if (position < 0) {
				return;
			}
			if (clickIndex == MORE) {//显示详情操作
				if (list.get(position).showDetail == 1) {//关闭详情
					list.get(position).showDetail = 0;
					locationItem.showDetailImageView.setImageResource(R.drawable.discover_hide_detail);
					closeAnimator = ObjectAnimator.ofFloat(locationItem.locationDetailLayout, "translationX", 0, screenWidth).setDuration(500);
					closeAnimator.addListener(new AnimatorListener() {
						
						@Override
						public void onAnimationStart(Animator animation) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void onAnimationRepeat(Animator animation) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void onAnimationEnd(Animator animation) {
							// TODO Auto-generated method stub
							locationItem.locationDetailLayout.setVisibility(View.GONE);
						}
						
						@Override
						public void onAnimationCancel(Animator animation) {
							// TODO Auto-generated method stub
							
						}
					});
					closeAnimator.start();
					ObjectAnimator.ofFloat(locationItem.locationPhotoImageView, "translationX", -screenWidth, 0).setDuration(500).start();
				}else {//打开详情
					locationItem.locationDetailLayout.setVisibility(View.VISIBLE);
					list.get(position).showDetail = 1;
					locationItem.showDetailImageView.setImageResource(R.drawable.discover_show_detail);
					ObjectAnimator.ofFloat(locationItem.locationPhotoImageView, "translationX", 0, -screenWidth).setDuration(500).start();
					ObjectAnimator.ofFloat(locationItem.locationDetailLayout, "translationX", screenWidth, 0).setDuration(500).start();
				}
			}else if (clickIndex == LOVE) {
				mHandler.sendEmptyMessage(STOPLOCATION);
				if (list.get(position).islove == 1) {
					Log.d(TAG, "is love need remove");
					API1.editFavoriteLocations(sharedPreferences.getString(Common.USERINFO_TOKENID, null), list.get(position).locationId, "remove", position, mHandler);
				}else {
					Log.d(TAG, "not love need add");
					API1.editFavoriteLocations(sharedPreferences.getString(Common.USERINFO_TOKENID, null), list.get(position).locationId, "add", position, mHandler);
				}
			}else {
				final Message msg = new Message();
				msg.what = clickIndex;
				msg.arg1 = position;
				msg.obj = v;
				mHandler.sendMessage(msg);
				
			}
		}
	}

	
}
