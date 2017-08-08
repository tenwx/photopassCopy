package com.pictureair.photopassCopy.fragment;

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
import com.pictureair.photopassCopy.MyApplication;
import com.pictureair.photopassCopy.R;
import com.pictureair.photopassCopy.activity.BaseFragment;
import com.pictureair.photopassCopy.adapter.DiscoverLocationAdapter;
import com.pictureair.photopassCopy.entity.DiscoverLocationItemInfo;
import com.pictureair.photopassCopy.entity.LocationItem;
import com.pictureair.photopassCopy.http.rxhttp.RxSubscribe;
import com.pictureair.photopassCopy.util.ACache;
import com.pictureair.photopassCopy.util.API2;
import com.pictureair.photopassCopy.util.AppUtil;
import com.pictureair.photopassCopy.util.Common;
import com.pictureair.photopassCopy.util.LocationUtil;
import com.pictureair.photopassCopy.util.PictureAirLog;
import com.pictureair.photopassCopy.widget.NoNetWorkOrNoCountView;
import com.trello.rxlifecycle.android.FragmentEvent;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 发现页面，显示各个地点的与当前的距离，可以筛选各个地方，可支持导航
 *
 * @author bauer_bao
 */
public class FragmentPageDiscover extends BaseFragment implements DiscoverLocationAdapter.OnUpdateLocationListener,
        LocationUtil.OnLocationNotificationListener {
    //声明控件
    private ListView discoverListView;
    private NoNetWorkOrNoCountView noNetWorkOrNoCountView;

    //申明类
    private DiscoverLocationAdapter discoverLocationAdapter;
    private SensorManager sensorManager;// 传感器管理器
    private Sensor sensor_orientation;
    private MySensorEventListener mySensorEventListener;// 传感器监听
    private DiscoverLocationItemInfo info;
    private LocationUtil locationUtil;

    //申明变量
    private static final String TAG = "FragmentPageDiscover";
    private float rotate_degree = 0;// x轴的旋转角度
    private ArrayList<DiscoverLocationItemInfo> locationList;
    private NumberFormat distanceFormat;//距离的小数点
    private boolean locationStart = false; //开启实时定位标记
    private int activateLocationCount = 0;//记录总共激活定位的数量
    private int locationActivatedIndex = -1;//记录激活定位的索引值

    private static final int REQUEST_LOCATION_PERMISSION = 2;
    private boolean mIsAskLocationPermission = false;

    private Activity activity;

    private final Handler fragmentPageDiscoverHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case DiscoverLocationAdapter.STOPLOCATION:
                    locationStart = false;
                    break;

                case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_RELOAD://noView的按钮响应重新加载点击事件
                    //重新加载购物车数据
                    PictureAirLog.out("onclick with reload");
                    locationList.clear();
                    getLocationInfo();
                    break;

                default:
                    break;
            }
            return false;
        }
    });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = getActivity();
        View view = inflater.inflate(R.layout.fragment_discover, container, false);
        setImmersiveMode(view);
        PictureAirLog.out("discover on create");

        //初始化控件
        noNetWorkOrNoCountView = (NoNetWorkOrNoCountView) view.findViewById(R.id.discoverNoNetWorkView);
        discoverListView = (ListView) view.findViewById(R.id.discover_listView);

        //声明方向传感器
        mySensorEventListener = new MySensorEventListener();
        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        sensor_orientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        //初始化数据
        locationUtil = new LocationUtil(activity);
        locationStart = true;
        locationList = new ArrayList<>();
        distanceFormat = NumberFormat.getNumberInstance();
        distanceFormat.setMaximumFractionDigits(1);

        //获取数据
        showPWProgressDialog();
        getLocationInfo();
        return view;
    }

    @Override
    public void onStop() {
        if (isVisible()) {
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
        if (isVisible()) {
            PictureAirLog.out(TAG + " truly ==onResume--->discover");
            if (mIsAskLocationPermission) {
                mIsAskLocationPermission = false;
                return;
            }
            locationStart = true;
            requesLocationPermission();
        }
    }

    @Override
    public void onPause() {
        if (isVisible()) {
            PictureAirLog.out("truly pause---->discover");
            locationStart = false;
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        PictureAirLog.d(TAG, "ondestroy===========");
        fragmentPageDiscoverHandler.removeCallbacksAndMessages(null);
        super.onDestroyView();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {//显示发现页面，需要获取location信息与当前的坐标
            PictureAirLog.out("show discover---->");
            //获取数据
            locationStart = true;
            showPWProgressDialog();
            getLocationInfo();
            requesLocationPermission();

        } else {//隐藏发现页面
            PictureAirLog.out("hide discover---->");
            locationStart = false;
            stopService();

        }
    }

    private void getLocationInfo() {
        final String locationCache = ACache.get(activity).getAsString(Common.DISCOVER_LOCATION);
        Observable.just(locationCache)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<String, Observable<JSONObject>>() {
                    @Override
                    public Observable<JSONObject> call(String str) {
                        String time = ACache.get(activity).getAsString(Common.CACHE_LOCATION_TIMER);
                        if (TextUtils.isEmpty(time) || TextUtils.isEmpty(str)) {
                            //超时 或者 没有location数据，需要重新获取
                            //此处不需要直接将location的信息添加时间。因为其他地方没有做为空的处理，做起来也比较麻烦，添加了很多逻辑，因此使用另一个变量来记录时间
                            PictureAirLog.out("start get lcoation info");
                            ACache.get(activity).put(Common.CACHE_LOCATION_TIMER, "time", ACache.TIME_DAY);//记录访问记录，设置一天缓存时间
                            return API2.getLocationInfo(MyApplication.getTokenId())
                                    .map(new Func1<JSONObject, JSONObject>() {
                                        @Override
                                        public JSONObject call(JSONObject jsonObject) {
                                            ACache.get(activity).put(Common.DISCOVER_LOCATION, jsonObject.toString());
                                            return jsonObject;
                                        }
                                    });

                        } else {
                            PictureAirLog.out("not get lcoation info");
                            //直接获取缓存内容
                            return Observable.just(JSONObject.parseObject(str));
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<JSONObject>bindToLifecycle())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        //获取全部的location
                        PictureAirLog.d(TAG, "get location success============" + jsonObject);
                        locationList.clear();
                        if (jsonObject != null) {
                            locationList.addAll(AppUtil.getLocation(activity, jsonObject.toString(), false));
                        }
                        locationUtil.setLocationItemInfos(locationList, FragmentPageDiscover.this);
                        getFavoriteLocationInfo();

                    }

                    @Override
                    public void _onError(int status) {
                        //失败了，取上次的缓存
                        PictureAirLog.out("get location failed");
                        if (TextUtils.isEmpty(locationCache)) {//失败
                            discoverListView.setVisibility(View.GONE);
                            noNetWorkOrNoCountView.setVisibility(View.VISIBLE);
                            noNetWorkOrNoCountView.setResult(R.string.no_network, R.string.click_button_reload, R.string.reload, R.drawable.no_network, fragmentPageDiscoverHandler, true);
                            dismissPWProgressDialog();
                        } else {
                            _onNext(JSONObject.parseObject(locationCache));
                        }
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    /**
     * 获取收藏地点
     */
    private void getFavoriteLocationInfo() {
        PictureAirLog.d("get favorite data");
        API2.getFavoriteLocations(MyApplication.getTokenId())
                .map(new Func1<JSONObject, ArrayList<String>>() {

                    @Override
                    public ArrayList<String> call(JSONObject jsonObject) {
                        ArrayList<String> list = new ArrayList<>();
                        try {
                            JSONArray jsonArray = jsonObject.getJSONArray("favoriteLocations");
                            if (jsonArray != null) {
                                for (int i = 0; i < jsonArray.size(); i++) {
                                    list.add(jsonArray.getString(i));
                                }
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        //设置location的isLove属性
                        for (int i = 0; i < list.size(); i++) {
                            for (int j = 0; j < locationList.size(); j++) {
                                if (list.get(i).equals(locationList.get(j).locationId)) {
                                    locationList.get(j).islove = 1;
                                    break;
                                }
                            }
                        }
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<ArrayList<String>>bindToLifecycle())
                .subscribe(new RxSubscribe<ArrayList<String>>() {
                    @Override
                    public void _onNext(ArrayList<String> strings) {

                    }

                    @Override
                    public void _onError(int status) {
                        waitLocationFinished();
                    }

                    @Override
                    public void onCompleted() {
                        waitLocationFinished();
                    }
                });
    }

    /**
     * 等待地点获取完毕
     */
    private void waitLocationFinished() {
        //开启线程等待第一次定位的结束，如果结束了，就显示出来
        Observable.interval(50, 50, TimeUnit.MILLISECONDS)
                .compose(this.<Long>bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Long aLong) {
                        PictureAirLog.d("wait location");
                        if (locationUtil.locationChanged) {
                            PictureAirLog.d(TAG, "location is ready");
                            discoverListView.setVisibility(View.VISIBLE);
                            noNetWorkOrNoCountView.setVisibility(View.GONE);
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
                            dismissPWProgressDialog();

                            unsubscribe();
                        }

                    }
                });
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

                //定位的时候
                info = locationList.get(position);
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
        Observable.interval(50, 50, TimeUnit.MILLISECONDS)
                .compose(this.<Long>bindUntilEvent(FragmentEvent.STOP))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Long aLong) {
                        updateLocation(position, view);
                        PictureAirLog.d("update location---> " + locationStart + activatedHashMap.get(position + ""));
                        //view.getY() >= 0 && view.getY() <= discoverListView.getHeight() - 100 &&
                        //将图标改为静止状态
                        if (!locationStart || activatedHashMap.get(position + "") != 1) {
                            PictureAirLog.d("stop location");
                            //停止定位
                            item.locationLeadImageView.setImageResource(R.drawable.direction_icon);
                            activateLocationCount--;
                            if (activateLocationCount == 0) {//取消定位
                                sensorManager.unregisterListener(mySensorEventListener);//不使用的时候，取消监听
                                discoverLocationAdapter.disableLocationActivated(position);
                                locationActivatedIndex = -1;
                            }
                            //停止计时循环
                            unsubscribe();
                        }
                    }
                });
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
                if (permissions.length > 0 && grantResults.length > 0) {
                    if (Manifest.permission.ACCESS_COARSE_LOCATION.equalsIgnoreCase(permissions[0]) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        startService();
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}