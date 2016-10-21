package com.pictureair.photopass.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.BaseLazyFragment;
import com.pictureair.photopass.adapter.DiscoverLocationAdapter;
import com.pictureair.photopass.entity.DiscoverLocationItemInfo;
import com.pictureair.photopass.entity.LocationItem;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.LocationUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.widget.NoNetWorkOrNoCountView;
import com.pictureair.photopass.widget.PWToast;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 发现页面，显示各个地点的与当前的距离，可以筛选各个地方，可支持导航
 *
 * @author bauer_bao
 */
public class FragmentPageDiscover extends BaseLazyFragment implements DiscoverLocationAdapter.OnUpdateLocationListener,
        LocationUtil.OnLocationNotificationListener {
    //声明控件
    private ListView discoverListView;
    private NoNetWorkOrNoCountView noNetWorkOrNoCountView;
    private View view;

    //申明类
    private PWToast myToast;
    private DiscoverLocationAdapter discoverLocationAdapter;
    private SensorManager sensorManager;// 传感器管理器
    private Sensor sensor_orientation;
    private MySensorEventListener mySensorEventListener;// 传感器监听
    private MyApplication app;
    private Thread locationThread;
    private DiscoverLocationItemInfo info;
    private LocationUtil locationUtil;

    //申明变量
    private static final String TAG = "FragmentPageDiscover";
    private static final int CHANGE_LOCATION = 111;
    private static final int STOP_LOCATION = 222;
    private static final int FINISH_LOADING = 333;
    private static final int WAIT_LOCATION = 444;
    private float rotate_degree = 0;// x轴的旋转角度
    private ArrayList<DiscoverLocationItemInfo> locationList;
    private ArrayList<String> favoriteList;
    private NumberFormat distanceFormat;//距离的小数点
    private boolean isLoading = false;// 加载线程判断
    private boolean locationStart = false; //开启实时定位标记
    private int activateLocationCount = 0;//记录总共激活定位的数量
    private int locationActivatedIndex = -1;//记录激活定位的索引值
    // 标志fragment是否初始化完成
    private boolean isPrepared;
    private boolean isInited;
    private int id = 0;

    private static final int REQUEST_LOCATION_PERMISSION = 2;
    private boolean mIsAskLocationPermission = false;

    private Activity activity;

    private final Handler fragmentPageDiscoverHandler = new FragmentPageDiscoverHandler(this);

    private static class FragmentPageDiscoverHandler extends Handler {
        private final WeakReference<FragmentPageDiscover> mActivity;

        public FragmentPageDiscoverHandler(FragmentPageDiscover activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivity.get() == null) {
                return;
            }
            mActivity.get().dealHandler(msg);
        }
    }

    /**
     * 处理Message
     *
     * @param msg
     */
    private void dealHandler(Message msg) {
        switch (msg.what) {
            case API1.GET_ALL_LOCATION_FAILED:
                PictureAirLog.out("get location failed");
                String locationCache = ACache.get(activity).getAsString(Common.DISCOVER_LOCATION);
                fragmentPageDiscoverHandler.obtainMessage(API1.GET_ALL_LOCATION_SUCCESS, locationCache).sendToTarget();
                break;

            case API1.GET_ALL_LOCATION_SUCCESS:
                //获取全部的location
                PictureAirLog.d(TAG, "get location success============" + msg.obj);
                locationList.clear();
                if (msg.obj != null) {
                    locationList.addAll(
                            AppUtil.getLocation(
                                    activity,
                                    msg.obj.toString(),
                                    false));
                }
                locationUtil.setLocationItemInfos(locationList, FragmentPageDiscover.this);
                API1.getFavoriteLocations(MyApplication.getTokenId(), fragmentPageDiscoverHandler);
                break;

            case API1.GET_FAVORITE_LOCATION_FAILED:
                fragmentPageDiscoverHandler.sendEmptyMessage(WAIT_LOCATION);
                break;

            //获取收藏地址成功
            case API1.GET_FAVORITE_LOCATION_SUCCESS:
                PictureAirLog.d(TAG, "get favorite success");
                try {
                    JSONObject favoriteJsonObject = JSONObject.parseObject(msg.obj.toString());
                    JSONArray jsonArray = favoriteJsonObject.getJSONArray("favoriteLocations");
                    if (jsonArray != null) {
                        for (int i = 0; i < jsonArray.size(); i++) {
                            favoriteList.add(jsonArray.getString(i));
                        }
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
                fragmentPageDiscoverHandler.sendEmptyMessage(WAIT_LOCATION);
                break;

            case WAIT_LOCATION:
                //开启线程等待第一次定位的结束，如果结束了，就显示出来
                new Thread() {
                    public void run() {
                        while (!isLoading) {
                            if (locationUtil.locationChanged) {
                                Looper.prepare();
                                isLoading = true;
                                PictureAirLog.d(TAG, "location is ready");
                                fragmentPageDiscoverHandler.sendEmptyMessage(FINISH_LOADING);
                            }
                        }
                    }

                    ;
                }.start();
                break;

            //加载完毕之后
            case FINISH_LOADING:
                if (discoverLocationAdapter == null) {
                    PictureAirLog.out("discover adapter is null");
                    discoverLocationAdapter = new DiscoverLocationAdapter(locationList, activity, fragmentPageDiscoverHandler, locationUtil.mapLocation, rotate_degree);
                    discoverLocationAdapter.setOnUpdateLocationListener(FragmentPageDiscover.this);
                    discoverListView.setAdapter(discoverLocationAdapter);
                    discoverListView.setOnScrollListener(new DiscoverOnScrollListener());
                } else {
                    PictureAirLog.out("discover adapter is not null");
                    discoverLocationAdapter.notifyDataSetChanged();
                }
                discoverListView.setVisibility(View.VISIBLE);
                noNetWorkOrNoCountView.setVisibility(View.GONE);
                dismissPWProgressDialog();
                break;

            //定位的时候
            case CHANGE_LOCATION:
                info = locationList.get(msg.arg1);
                LocationItem item = (LocationItem) msg.obj;
                final double lat_a = info.latitude;// 纬度,目标地点不会变化
                final double lng_a = info.longitude;// 经度
                double lat_b = (locationUtil.mapLocation != null) ? locationUtil.mapLocation.getLatitude() : 0;//获取当前app经纬度
                double lng_b = (locationUtil.mapLocation != null) ? locationUtil.mapLocation.getLongitude() : 0;
                double distance = Math.round(AppUtil.getDistance(lng_a, lat_a, lng_b, lat_b));
                // 距离
                item.distanceTextView.setText(AppUtil.getSmartDistance(distance, distanceFormat));
                double d = AppUtil.gps2d(lat_a, lng_a, lat_b, lng_b);
                PictureAirLog.out("degree----->" + rotate_degree + "; d---> " + d);
                // 角度
                item.locationLeadImageView.setRotation((float) d - rotate_degree);
                break;

            //停止定位
            case STOP_LOCATION:
                LocationItem item2 = (LocationItem) msg.obj;
                item2.locationLeadImageView.setImageResource(R.drawable.direction_icon);
                break;

            case DiscoverLocationAdapter.MAGICSHOOT:
                PictureAirLog.d(TAG, "magic shoot on click");
                break;

            case DiscoverLocationAdapter.STOPLOCATION:
                locationStart = false;
                break;

            case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_RELOAD://noView的按钮响应重新加载点击事件
                //重新加载购物车数据
                PictureAirLog.out("onclick with reload");
                locationList.clear();
                favoriteList.clear();
                API1.getLocationInfo(activity, MyApplication.getTokenId(), fragmentPageDiscoverHandler);//获取所有的location
                break;

            //收藏地点失败
            case API1.EDIT_FAVORITE_LOCATION_FAILED:
                id = ReflectionUtil.getStringId(activity, msg.arg1);
                myToast.setTextAndShow(id, Common.TOAST_SHORT_TIME);
                break;

            case API1.EDIT_FAVORITE_LOCATION_SUCCESS:
                discoverLocationAdapter.updateIsLove(msg.arg1);
                break;
            default:
                break;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            activity = getActivity();
            view = inflater.inflate(R.layout.fragment_discover, null);
            PictureAirLog.out("discover on create");
            isPrepared = true;
            onLazyLoad();
        }

        return view;
    }

    @Override
    protected void onLazyLoad() {
        if (!isPrepared || !isVisible || isInited) {
            return;
        }
        isInited = true;
        PictureAirLog.out("onLazyLoad");
        //初始化控件
        noNetWorkOrNoCountView = (NoNetWorkOrNoCountView) view.findViewById(R.id.discoverNoNetWorkView);
        discoverListView = (ListView) view.findViewById(R.id.discover_listView);

        //声明方向传感器
        mySensorEventListener = new MySensorEventListener();
        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        sensor_orientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        //初始化数据
        myToast = new PWToast(activity);
        locationUtil = new LocationUtil(activity);
        app = (MyApplication) activity.getApplication();
        locationStart = true;
        locationList = new ArrayList<>();
        favoriteList = new ArrayList<>();
        distanceFormat = NumberFormat.getNumberInstance();
        distanceFormat.setMaximumFractionDigits(1);

        //获取数据
        showPWProgressDialog();
        getLocationInfo();

        onResume();//第一次resume在activity运行的时候就执行了，导致真正到了发现页面之后，第一次的resume不会执行
    }

    @Override
    public void onStop() {
        if (isPrepared && isVisible) {
            PictureAirLog.d(TAG, "stop============");
            stopService();
        } else {
            PictureAirLog.out("discover need not stop");
        }
        super.onStop();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (isPrepared && isVisible) {
            PictureAirLog.out(TAG + " truly ==onResume--->discover");
            if (mIsAskLocationPermission) {
                mIsAskLocationPermission = false;
                return;
            }
            isLoading = false;
            locationStart = true;
            requesLocationPermission();
        }
    }

    @Override
    public void onPause() {
        if (isPrepared && isVisible) {
            PictureAirLog.out("truly pause---->discover");
            locationStart = false;
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        PictureAirLog.d("ondestroy===========");
        fragmentPageDiscoverHandler.removeCallbacksAndMessages(null);
        super.onDestroyView();
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        if (isPrepared && isVisible) {
            //显示发现页面，需要获取location信息与当前的坐标
            PictureAirLog.out("show discover---->");
            //获取数据
            isLoading = false;
            locationStart = true;
            showPWProgressDialog();
            getLocationInfo();
            requesLocationPermission();
        }
    }

    @Override
    protected void onInvisible() {
        super.onInvisible();
        if (isPrepared && !isVisible) {
            //隐藏发现页面
            PictureAirLog.out("hide discover---->");
            locationStart = false;
            isLoading = false;
            stopService();
        }
    }

    private void getLocationInfo() {
        String expireTime = ACache.get(activity).getAsString(Common.CACHE_LOCATION_TIMER);
        String locationCache = ACache.get(activity).getAsString(Common.DISCOVER_LOCATION);

        if (TextUtils.isEmpty(expireTime) || TextUtils.isEmpty(locationCache)) {
            //超时 或者 没有location数据，需要重新获取
            //此处不需要直接将location的信息添加时间。因为其他地方没有做为空的处理，做起来也比较麻烦，添加了很多逻辑，因此使用另一个变量来记录时间
            PictureAirLog.out("start get lcoation info");
            API1.getLocationInfo(activity, app.getTokenId(), fragmentPageDiscoverHandler);//获取所有的location
            ACache.get(activity).put(Common.CACHE_LOCATION_TIMER, "time", ACache.TIME_DAY);//记录访问记录，设置一天缓存时间
        } else {
            PictureAirLog.out("not get lcoation info");
            //直接获取缓存内容
            fragmentPageDiscoverHandler.obtainMessage(API1.GET_ALL_LOCATION_SUCCESS, locationCache).sendToTarget();
        }
    }

    /**
     * 开启定位服务
     */
    private void startService() {
        locationUtil.startLocation();
    }

    /**
     * 关闭定位服务
     */
    private void stopService() {
        PictureAirLog.d(TAG, "stop location------->");
        locationUtil.stopLocation();
    }

    /**
     * 局部更新listView
     */
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
                fragmentPageDiscoverHandler.sendMessage(msg);
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
//				PictureAirLog.out("--------->sensor changed");
            }
        }
    }

    //listView滚动监听，判断当前定位激活的位置是否超出屏幕
    private class DiscoverOnScrollListener implements OnScrollListener {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            discoverLocationAdapter.setFirstVisibleCount(firstVisibleItem);
            discoverLocationAdapter.setLastVisibleCount(firstVisibleItem + visibleItemCount);
            if (locationActivatedIndex != -1) {//说明有激活的position
                if (locationActivatedIndex < firstVisibleItem || locationActivatedIndex > discoverListView.getLastVisiblePosition()) {//超出屏幕
//					PictureAirLog.d(TAG, "out of window------->");
                    locationStart = false;
                }
            }
        }
    }

    @Override
    public void startLocation(final int position, final View view) {
        PictureAirLog.d(TAG, "start location---------->");
        //获取已经激活定位的arrylist
        final HashMap<String, Integer> activatedHashMap = discoverLocationAdapter.getActivatedLocationMap();
        PictureAirLog.d(TAG, "start location---------->" + activatedHashMap.get(position + ""));
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
        if (activateLocationCount == 0) {
            //SENSOR_DELAY_UI采样频率是中等的，比normal要快一点，比game和fast都慢。如果定位觉得不准，是因为没有进行校正。目前校正只能通过画8字手动校准
            sensorManager.registerListener(mySensorEventListener, sensor_orientation, SensorManager.SENSOR_DELAY_UI);//点击定位的时候，注册监听
        }
        activateLocationCount++;
        locationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                do {
                    updateLocation(position, view);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                while (locationStart && activatedHashMap.get(position + "") == 1);//view.getY() >= 0 && view.getY() <= discoverListView.getHeight() - 100 &&
                //将图标改为静止状态
                Message msg = new Message();
                msg.obj = item;
                msg.arg1 = position;
                msg.what = STOP_LOCATION;

                fragmentPageDiscoverHandler.sendMessage(msg);
                activateLocationCount--;
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
    public void inOrOutPlace(String locationIds, boolean in) {
        // TODO Auto-generated method stub
        PictureAirLog.out("in or out special location......");
    }

    private void requesLocationPermission() {
        PictureAirLog.out("request location permission");
        if (!AppUtil.checkPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
            PictureAirLog.out("1111111");
            mIsAskLocationPermission = true;
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }
        PictureAirLog.out("22222222");
        startService();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                PictureAirLog.out("33333333333");
                if (Manifest.permission.ACCESS_COARSE_LOCATION.equalsIgnoreCase(permissions[0]) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startService();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}