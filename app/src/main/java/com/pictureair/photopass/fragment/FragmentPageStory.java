package com.pictureair.photopass.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.BaseFragment;
import com.pictureair.photopass.activity.MipCaptureActivity;
import com.pictureair.photopass.activity.MyPPPActivity;
import com.pictureair.photopass.adapter.FragmentAdapter;
import com.pictureair.photopass.customDialog.CustomDialog;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.DiscoverLocationItemInfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.entity.PhotoItemInfo;
import com.pictureair.photopass.eventbus.BaseBusEvent;
import com.pictureair.photopass.eventbus.MainTabSwitchEvent;
import com.pictureair.photopass.eventbus.RedPointControlEvent;
import com.pictureair.photopass.eventbus.SocketEvent;
import com.pictureair.photopass.eventbus.StoryFragmentEvent;
import com.pictureair.photopass.eventbus.StoryRefreshEvent;
import com.pictureair.photopass.eventbus.StoryRefreshOnClickEvent;
import com.pictureair.photopass.service.DownloadService;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.DisneyVideoTool;
import com.pictureair.photopass.util.JsonUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.util.SettingUtil;
import com.pictureair.photopass.widget.MyToast;
import com.pictureair.photopass.widget.NoNetWorkOrNoCountView;
import com.pictureair.photopass.widget.viewpagerindicator.TabPageIndicator;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import cn.smssdk.gui.CustomProgressDialog;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

/**
 * PhotoPass照片的图片墙，用来显示从服务器返回的照片信息，以及通过magic相机拍摄的图片
 * 可以左右滑动切换不同的相册
 * 可以下拉刷新，获取更多的图片信息
 */
public class FragmentPageStory extends BaseFragment implements OnClickListener, ViewPager.OnPageChangeListener{
    //声明静态变量
    private static final int DEAL_ALL_PHOTO_DATA_DONE = 444;
    private static final int DEAL_REFRESH_PHOTO_DATA_DONE = 555;
    private static final int LOAD_COMPLETED = 111;
    private static final int REFRESH = 666;
    private static final int REFRESH_LOCAL_PHOTOS = 777;
    private static final int SORT_COMPLETED_ALL = 223;
    private static final int SORT_COMPLETED_REFRESH = 224;
    private static final int DEAL_ALL_VIDEO_DATA_DONE = 888;
    private static final int DEAL_REFRESH_VIDEO_DATA_DONE = 999;
    private static final int DEAL_FAVORITE_DATA_SUCCESS = 1000;
    private static final int SYNC_BOUGHT_PHOTOS = 1001;
    private static final int SYNC_BOUGHT_PHOTOS_DEAL_DATA_DONE = 1002;
    private static final int LOAD_PHOTO_FROM_DB = 1003;

    private static final String TAG = "FragmentPageStory";
    private String[] titleStrings;

    //申明变量
    private int refreshDataCount = 0;//记录刷新数据的数量
    private int refreshVideoDataCount = 0;//记录刷新
    /**
     * 是否需要重新拉取数据
     */
    private boolean needfresh = false;
    /**
     * 从sp中读取的值，如果是true，就要保存起来，一旦失败，就要把这个值给needfresh
     */
    private boolean sharedNeedFresh = false;
    private int screenWidth;
    private boolean isLoading = false;
    private boolean scanMagicPhotoNeedCallBack;//记录是否需要重新扫描本地照片
    private boolean noPhotoViewStateRefresh = false;//无图的时候进行的刷新
    private int ppPhotoCount;

    //申明控件
    private ImageView more;
    private ImageView scanLayout;
    private LinearLayout noPhotoView;
    private RelativeLayout scanRelativeLayout;
    private CustomProgressDialog dialog;// 加载等待
    private ViewPager storyViewPager;
    private LinearLayout storyNoPpToScanLinearLayout;
    private ImageView storyNoPpScanImageView;
    private ImageView storyNoPhotoToDiscoverImageView;
    private NoNetWorkOrNoCountView noNetWorkOrNoCountView;
    private SwipeRefreshLayout swipeRefreshLayout;

    //申明类
    private MyApplication app;
    private ArrayList<PhotoItemInfo> photoPassItemInfoList;
    private ArrayList<PhotoItemInfo> magicItemInfoList;
    private ArrayList<PhotoItemInfo> boughtItemInfoList = new ArrayList<>();// 所有已经购买的图片的信息
    private ArrayList<PhotoItemInfo> allItemInfoList = new ArrayList<>();// 所有的图片信息

    private ArrayList<PhotoInfo> photoPassPicList = new ArrayList<>();// 所有的从服务器返回的photopass图片的信息
    private ArrayList<PhotoInfo> photoPassVideoList = new ArrayList<>();// 所有的从服务器返回的photopass图片的信息
    private ArrayList<PhotoInfo> targetMagicPhotoList = new ArrayList<>();

    private ArrayList<PhotoInfo> allPhotoList, pictureAirPhotoList, magicPhotoList, boughtPhotoList, favouritePhotoList, localPhotoList;
    private ArrayList<DiscoverLocationItemInfo> locationList = new ArrayList<DiscoverLocationItemInfo>();
    private List<Fragment> fragments;
    private FragmentAdapter fragmentAdapter;
    private Context context;
    private SimpleDateFormat sdf;
    private SharedPreferences sharedPreferences;
    private MyToast myToast;
    private PhotoInfo selectPhotoItemInfo;
    private ScanPhotosThread scanPhotosThread;
    private PictureAirDbManager pictureAirDbManager;
    private boolean getPhotoInfoDone = false;
    private boolean getVideoInfoDone = false;

    /**
     * 同步已经购买的照片
     */
    private boolean syncingBoughtPhotos = false;

    private SettingUtil settingUtil;
    private LinearLayout storyLeadBarLinearLayout;
    private TabPageIndicator indicator;

    //申明handler消息回调机制

    private final Handler fragmentPageStoryHandler = new FragmentPageStoryHandler(this);

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        PictureAirLog.out("change tap--->" + i);
        app.fragmentStoryLastSelectedTab = i;
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    private static class FragmentPageStoryHandler extends Handler {
        private final WeakReference<FragmentPageStory> mActivity;

        public FragmentPageStoryHandler(FragmentPageStory activity) {
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
            case API1.GET_AD_LOCATIONS_SUCCESS://获取广告地点成功
                PictureAirLog.out("ad location---->" + msg.obj.toString());
                /**
                 * 1.存入数据库
                 * 2.在application中记录结果
                 */
                JSONObject adJsonObject = (JSONObject) msg.obj;
                pictureAirDbManager.insertADLocations(adJsonObject.getJSONArray("locations"));
                app.setGetADLocationSuccess(true);
                break;

            case API1.GET_ALL_PHOTOS_BY_CONDITIONS_FAILED://获取全部照片失败
                getPhotoInfoDone = true;
                if (getPhotoInfoDone && getVideoInfoDone) {
                    finishLoad(true);
                }
                break;

            case API1.GET_ALL_VIDEO_LIST_FAILED://获取全部视频失败
                getVideoInfoDone = true;
                if (getPhotoInfoDone && getVideoInfoDone) {
                    finishLoad(true);
                }
                break;

            case API1.GET_ALL_LOCATION_FAILED://获取地址信息失败
                finishLoad(true);
                break;

            case API1.GET_ALL_LOCATION_SUCCESS://成功获取地点信息
                PictureAirLog.d(TAG, "---------->get location success");
                try {
                    JSONObject response = JSONObject.parseObject(msg.obj.toString());
                    JSONArray resultArray = response.getJSONArray("locations");
                    for (int i = 0; i < resultArray.size(); i++) {
                        JSONObject object = resultArray.getJSONObject(i);
                        DiscoverLocationItemInfo locationInfo = JsonUtil.getLocation(object);
                        locationList.add(locationInfo);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //检查数据库是否有数据，如果有数据，直接显示，如果没有数据，从网络获取
                photoPassPicList.clear();
                photoPassVideoList.clear();

                if (!needfresh) {//如果需要刷新数据的话，就不需要从数据库中获取数据
                    //  如果PP中的照片大于 10 张，并且账户中没有PP＋。就提示购买PP+
                    if (settingUtil.isFirstPP10(sharedPreferences.getString(Common.USERINFO_ID, ""))) {
                        //第一次 PP数量到 10 。
                        API1.getPPPSByUserId(MyApplication.getTokenId(), fragmentPageStoryHandler);
                    }

                    PictureAirLog.d(TAG, "---------> load data from databases");
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            loadDataFromDataBase();
                            fragmentPageStoryHandler.sendEmptyMessage(LOAD_PHOTO_FROM_DB);
                        }
                    }.start();
                } else {
                    fragmentPageStoryHandler.sendEmptyMessage(LOAD_PHOTO_FROM_DB);
                }
                break;

            case LOAD_PHOTO_FROM_DB:
                if (photoPassPicList.size() == 0 || needfresh) {
                    //数据为0，需要从网上下载
                    PictureAirLog.out("photolist size = 0");
                    //判断是否之前有成功获取过
                    API1.getPhotosByConditions(MyApplication.getTokenId(), fragmentPageStoryHandler, null);//获取全部图片
                    API1.getVideoList(null, fragmentPageStoryHandler);//获取全部视频信息
                } else {
                    PictureAirLog.out("photolist size = " + photoPassPicList.size());
                    //有数据，直接显示
                    photoPassItemInfoList.clear();
                    allItemInfoList.clear();
                    boughtItemInfoList.clear();
                    try {
                        getData();
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                break;

            case API1.GET_REFRESH_PHOTOS_BY_CONDITIONS_FAILED://获取刷新失败
                PictureAirLog.out("get photo refresh failed------>");
                getPhotoInfoDone = true;
                if (getPhotoInfoDone && getVideoInfoDone) {
                    finishLoad(false);
                }
                break;

            case API1.GET_REFRESH_VIDEO_LIST_FAILED://获取刷新失败
                PictureAirLog.out("get video refresh failed------>");
                getVideoInfoDone = true;
                if (getPhotoInfoDone && getVideoInfoDone) {
                    finishLoad(false);
                }
                break;

            case API1.GET_ALL_PHOTOS_BY_CONDITIONS_SUCCESS://获取照片成功

                PictureAirLog.d(TAG, "--------->get photo success");
                saveJsonToSQLite((JSONObject) msg.obj, true, false);
                break;

            case API1.GET_ALL_VIDEO_LIST_SUCCESS://获取视频成功

                PictureAirLog.d(TAG, "--->get video success");
                saveJsonToSQLite((JSONObject) msg.obj, true, true);
                break;

            case API1.GET_REFRESH_PHOTOS_BY_CONDITIONS_SUCCESS://获取刷新的推送图片
//                    app.setPushPhotoCount(0);
                PictureAirLog.d(TAG, "deal refresh photos-------");
                saveJsonToSQLite((JSONObject) msg.obj, false, false);
                break;

            case API1.GET_REFRESH_VIDEO_LIST_SUCCESS://获取刷新的视频成功
                PictureAirLog.d(TAG, "--->get refresh video success");
                saveJsonToSQLite((JSONObject) msg.obj, false, true);
                break;

            case API1.GET_SOCKET_DATA_SUCCESS://手动刷新成功
                //获取推送成功，后面逻辑按照之前走
                PictureAirLog.e(TAG, "GET_SOCKET_DATA_SUCCESS: " + msg.obj.toString());
                JSONObject jsonObject = (JSONObject) msg.obj;
                if (jsonObject.size() > 0) {
                    JsonUtil.dealGetSocketData(getActivity(), jsonObject.toString(), true, null, sharedPreferences);
                }
                break;

            case REFRESH_LOCAL_PHOTOS://刷新处理本地照片
                PictureAirLog.d(TAG, "scan local photos success");
                dealLocalRefreshedData();
                break;

            case REFRESH://开始刷新
                PictureAirLog.d(TAG, "the index of refreshing is " + msg.arg1);
                API1.getPhotosByConditions(MyApplication.getTokenId(), fragmentPageStoryHandler, sharedPreferences.getString(Common.LAST_UPDATE_PHOTO_TIME, null));//获取更新信息
                API1.getVideoList(sharedPreferences.getString(Common.LAST_UPDATE_VIDEO_TIME, null), fragmentPageStoryHandler);//获取最新视频信息
                API1.getSocketData(fragmentPageStoryHandler);//手动拉取socket信息
                break;

            case DEAL_ALL_PHOTO_DATA_DONE://处理照片成功
                app.setPushPhotoCount(0);//清空推送消息的数量
                getPhotoInfoDone = true;
                getDataFinish();
                break;

            case DEAL_ALL_VIDEO_DATA_DONE://处理全部视频成功
                app.setPushViedoCount(0);
                getVideoInfoDone = true;
                getDataFinish();
                break;

            case DEAL_REFRESH_PHOTO_DATA_DONE://处理刷新照片成功
                app.setPushPhotoCount(0);
                getPhotoInfoDone = true;
                PictureAirLog.d(TAG, "deal refresh photos done");
                Editor editor = sharedPreferences.edit();// 获取编辑器
                editor.putInt("photoCount", 0);
                editor.commit();// 提交修改
                getRefreshDataFinish();
                break;

            case DEAL_REFRESH_VIDEO_DATA_DONE://处理刷新视频成功
                app.setPushViedoCount(0);
                getVideoInfoDone = true;
                PictureAirLog.out("deal refresh video done");
                getRefreshDataFinish();
                break;

            case DEAL_FAVORITE_DATA_SUCCESS://处理收藏图片成功
                EventBus.getDefault().post(new StoryFragmentEvent(favouritePhotoList, targetMagicPhotoList, 4));
                break;

            case SORT_COMPLETED_REFRESH:
                //刷新广告地点
                app.setGetADLocationSuccess(false);
                API1.getADLocations(fragmentPageStoryHandler);

                if (noPhotoViewStateRefresh) {//无图页的刷新
                    showViewPager();
                    noPhotoViewStateRefresh = false;
                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setEnabled(false);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                } else {//有图片页的刷新
                    EventBus.getDefault().post(new StoryFragmentEvent(allPhotoList, targetMagicPhotoList, 0));
                    EventBus.getDefault().post(new StoryFragmentEvent(pictureAirPhotoList, targetMagicPhotoList, 1));
                    EventBus.getDefault().post(new StoryFragmentEvent(magicPhotoList, targetMagicPhotoList, 2));
                    EventBus.getDefault().post(new StoryFragmentEvent(boughtPhotoList, targetMagicPhotoList, 3));
                    EventBus.getDefault().post(new StoryFragmentEvent(favouritePhotoList, targetMagicPhotoList, 4));
                }
                break;

            case LOAD_COMPLETED:
                //没有成功获取广告信息
                if (!app.isGetADLocationSuccess()) {
                    PictureAirLog.out("start get ad location");
                    API1.getADLocations(fragmentPageStoryHandler);
                } else {
                    PictureAirLog.out("ad location has got already");
                }

                sortData(true);
                break;

            case SORT_COMPLETED_ALL:
                if (syncingBoughtPhotos) {//同步购买照片操作
                    syncingBoughtPhotos = false;
                    EventBus.getDefault().post(new StoryFragmentEvent(allPhotoList, targetMagicPhotoList, 0));
                    EventBus.getDefault().post(new StoryFragmentEvent(pictureAirPhotoList, targetMagicPhotoList, 1));
                    EventBus.getDefault().post(new StoryFragmentEvent(magicPhotoList, targetMagicPhotoList, 2));
                    EventBus.getDefault().post(new StoryFragmentEvent(boughtPhotoList, targetMagicPhotoList, 3));
                    EventBus.getDefault().post(new StoryFragmentEvent(favouritePhotoList, targetMagicPhotoList, 4));
                } else {
                    scanMagicPhotoNeedCallBack = true;
                    fragments = new ArrayList<>();
                    fragments.clear();
                    showViewPager();
                    noNetWorkOrNoCountView.setVisibility(View.GONE);//无网络状态的View设置为不可见
                    if (sharedNeedFresh) {
                        sharedNeedFresh = false;
                    }
                }

                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                break;

            case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_RELOAD://noView的按钮响应重新加载点击事件
                //重新加载数据
                PictureAirLog.out("onclick with reload");
                if (!dialog.isShowing()){
                    dialog.show();
                }
                if (ACache.get(getActivity()).getAsString(Common.LOCATION_INFO) == null) {//地址获取失败
                    API1.getLocationInfo(getActivity(), MyApplication.getTokenId(), fragmentPageStoryHandler);//获取所有的location
                } else {//地址获取成功，但是照片获取失败
                    Message message = fragmentPageStoryHandler.obtainMessage();
                    message.what = API1.GET_ALL_LOCATION_SUCCESS;
                    message.obj = ACache.get(getActivity()).getAsString(Common.LOCATION_INFO);
                    fragmentPageStoryHandler.sendMessage(message);
                }
                break;

            case API1.GET_PPP_SUCCESS:
                if (ppPhotoCount >= 10 && API1.PPPlist.size() == 0) {
                    new CustomDialog(context, R.string.pp_first_up10_msg, R.string.pp_first_up10_no_msg, R.string.pp_first_up10_yes_msg, new CustomDialog.MyDialogInterface() {

                        @Override
                        public void yes() {
                            // TODO Auto-generated method stub // 去升级：购买AirPass+页面. 由于失去了airPass详情的界面。故此处，跳转到了airPass＋的界面。
                            Intent intent = new Intent();
                            intent.setClass(getActivity(),
                                    MyPPPActivity.class);
                            startActivity(intent);
                        }

                        @Override
                        public void no() {
                            // TODO Auto-generated method stub // 考虑下：不做操作

                        }
                    });
                    settingUtil.insertSettingFirstPP10Status(sharedPreferences.getString(Common.USERINFO_ID, ""));
                } else if (API1.PPPlist.size() > 0) {
                    settingUtil.insertSettingFirstPP10Status(sharedPreferences.getString(Common.USERINFO_ID, ""));
                }
                break;

            case SYNC_BOUGHT_PHOTOS://同步已购买图片
                /**
                 * 1.重新从数据库获取一遍数据
                 * 2.更新页面
                 */
                if (!dialog.isShowing()){
                    dialog.show();
                }

                new Thread(){
                    @Override
                    public void run() {
                        synchronized (this) {
                            photoPassPicList.clear();
                            photoPassVideoList.clear();
                            loadDataFromDataBase();
                            photoPassItemInfoList.clear();
                            allItemInfoList.clear();
                            boughtItemInfoList.clear();
                            fragmentPageStoryHandler.sendEmptyMessage(SYNC_BOUGHT_PHOTOS_DEAL_DATA_DONE);
                        }
                    }
                }.start();
                break;

            case SYNC_BOUGHT_PHOTOS_DEAL_DATA_DONE:
                try {
                    getData();
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;

            default:
                break;
        }
    }


    /**
     * 全部处理完成之后会调用
     */
    private void finishLoad(boolean setVisibile) {
        //将video和photo标记清空
        getVideoInfoDone = false;
        getPhotoInfoDone = false;
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        if (setVisibile) {
            storyNoPpToScanLinearLayout.setVisibility(View.GONE);
            noNetWorkOrNoCountView.setVisibility(View.VISIBLE);
            if (sharedNeedFresh) {
                needfresh = sharedNeedFresh;
            }
            noNetWorkOrNoCountView.setResult(R.string.no_network, R.string.click_button_reload, R.string.reload, R.drawable.no_network, fragmentPageStoryHandler, true);

        } else {//刷新失败
            myToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
            EventBus.getDefault().post(new StoryRefreshEvent(app.fragmentStoryLastSelectedTab, StoryRefreshEvent.STOP_REFRESH));
        }
    }

    /**
     * 保存到数据库成功之后
     */
    private void getDataFinish() {
        if (getPhotoInfoDone && getVideoInfoDone) {
            getPhotoInfoDone = false;
            getVideoInfoDone = false;
            photoPassItemInfoList.clear();
            allItemInfoList.clear();
            boughtItemInfoList.clear();
            try {
                getData();
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * 保存json之后，处理对应的刷新数据
     */
    private void getRefreshDataFinish() {
        if (getPhotoInfoDone && getVideoInfoDone) {
            getPhotoInfoDone = false;
            getVideoInfoDone = false;
            if (refreshDataCount > 0 || refreshVideoDataCount > 0) {
                PictureAirLog.out("getrefreshdata");
                getrefreshdata();
                sortData(false);

            } else {
                PictureAirLog.out("nomore");
                myToast.setTextAndShow(R.string.nomore, Common.TOAST_SHORT_TIME);
                fragmentPageStoryHandler.sendEmptyMessage(SORT_COMPLETED_REFRESH);
            }
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            EventBus.getDefault().post(new RedPointControlEvent(false));
        }
    }

    /**
     * 解析服务器返回的数据
     *
     * @param jsonObject json对象
     * @param isAll      布尔值，是否是获取全部数据
     * @param isVideo    是否是视频数据
     */
    private void saveJsonToSQLite(final JSONObject jsonObject, final boolean isAll, final boolean isVideo) {
        PictureAirLog.out("start save json");
        new Thread() {
            public void run() {
                synchronized (this) {
                    PictureAirLog.out("start save json in thread");
                    if (isAll) {//获取全部数据，需要先清空数据库，反之，插入到后面
                        if (isVideo) {
                            PictureAirLog.d(TAG, "delete all video data from table");
                            pictureAirDbManager.deleteAllInfoFromTable(Common.PHOTOPASS_INFO_TABLE, true);
                        } else {
                            PictureAirLog.d(TAG, "delete all data from table");
                            pictureAirDbManager.deleteAllInfoFromTable(Common.PHOTOPASS_INFO_TABLE, false);
                        }
                    } else {
                        PictureAirLog.d(TAG, "need not delete all data");
                    }
                    final JSONArray responseArray = jsonObject.getJSONArray(isVideo ? "videoList" : "photos");

                    String updatetimeString = jsonObject.getString(isVideo ? "t" : "time");
                    PictureAirLog.out("updatetime:" + updatetimeString + "new data count = " + responseArray.size());

                    if (isAll || responseArray.size() > 0) {//说明全部获取，需要记录时间；如果刷新的话，有数据的时候，才记录时间，否则不记录时间
                        //需要存储这个时间
                        Editor editor = sharedPreferences.edit();
                        if (isVideo) {
                            editor.putString(Common.LAST_UPDATE_VIDEO_TIME, updatetimeString);
                        } else {
                            editor.putString(Common.LAST_UPDATE_PHOTO_TIME, updatetimeString);
                        }
                        editor.commit();
                    }

                    if (isAll) {//如果全部获取，需要清除原有的数据
                        if (isVideo) {
                            photoPassVideoList.clear();
                        } else {
                            photoPassPicList.clear();
                        }
                    }
                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ArrayList<PhotoInfo> resultPhotoList = pictureAirDbManager.insertPhotoInfoIntoPhotoPassInfo(responseArray, isVideo, isAll);
                    if (isVideo) {
                        PictureAirLog.out("-----------------> finish insert video data into database");
                        if (!isAll) {
                            refreshVideoDataCount = resultPhotoList.size();
                            PictureAirLog.d(TAG, "------refresh count ----->" + refreshVideoDataCount);
                        }
                        photoPassVideoList.addAll(resultPhotoList);
                    } else {
                        PictureAirLog.out("-----------------> finish insert photo data into database");
                        if (!isAll) {
                            refreshDataCount = resultPhotoList.size();
                            PictureAirLog.d(TAG, "------refresh count ----->" + refreshDataCount);
                            downLoadPhoto(resultPhotoList);
                        }
                        photoPassPicList.addAll(resultPhotoList);
                    }

                    //通知已经处理完毕
                    if (isAll) {
                        fragmentPageStoryHandler.sendEmptyMessage(isVideo ? DEAL_ALL_VIDEO_DATA_DONE : DEAL_ALL_PHOTO_DATA_DONE);

                    } else {
                        fragmentPageStoryHandler.sendEmptyMessage(isVideo ? DEAL_REFRESH_VIDEO_DATA_DONE : DEAL_REFRESH_PHOTO_DATA_DONE);
                    }
                }
            }

            ;
        }.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PictureAirLog.out("on create----->");
        View view = inflater.inflate(R.layout.fragment_story, null);
        titleStrings = new String[] {getActivity().getResources().getString(R.string.story_tab_all),
                getActivity().getResources().getString(R.string.story_tab_photopass),
                getActivity().getResources().getString(R.string.story_tab_magic),
                getActivity().getResources().getString(R.string.story_tab_bought),
                getActivity().getResources().getString(R.string.story_tab_favorite) };
        //获取控件
        more = (ImageView) view.findViewById(R.id.story_more);
        scanRelativeLayout = (RelativeLayout) view.findViewById(R.id.storyScanRelativeLayout);
        scanLayout = (ImageView) view.findViewById(R.id.story_scan);
        storyNoPpToScanLinearLayout = (LinearLayout) view.findViewById(R.id.story_no_pp_to_scan);
        storyLeadBarLinearLayout = (LinearLayout) view.findViewById(R.id.story_lead_bar);
        storyNoPpScanImageView = (ImageView) view.findViewById(R.id.story_no_pp_scan);
        storyViewPager = (ViewPager) view.findViewById(R.id.story_viewPager);
        noNetWorkOrNoCountView = (NoNetWorkOrNoCountView) view.findViewById(R.id.storyNoNetWorkView);
        noPhotoView = (LinearLayout) view.findViewById(R.id.no_photo_view_relativelayout);
        storyNoPhotoToDiscoverImageView = (ImageView) view.findViewById(R.id.story_to_discover);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.story_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        swipeRefreshLayout.setEnabled(false);

        indicator = (TabPageIndicator) view.findViewById(R.id.indicator);
        indicator.setOnPageChangeListener(this);
        //初始化控件
        PictureAirLog.out("dialog-----> in story");
        dialog = CustomProgressDialog.show(getActivity(), getString(R.string.is_loading), false, null);
        context = getActivity();
        pictureAirDbManager = new PictureAirDbManager(getActivity());
        settingUtil = new SettingUtil(pictureAirDbManager);
        app = (MyApplication) getActivity().getApplication();
        PictureAirLog.out("current tap---->" + app.fragmentStoryLastSelectedTab);
        indicator.setmSelectedTabIndex(app.fragmentStoryLastSelectedTab);
        sharedPreferences = getActivity().getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
        photoPassItemInfoList = new ArrayList<>();
        magicItemInfoList = new ArrayList<>();

        allPhotoList = new ArrayList<>();
        pictureAirPhotoList = new ArrayList<>();
        magicPhotoList = new ArrayList<>();
        boughtPhotoList = new ArrayList<>();
        favouritePhotoList = new ArrayList<>();
        storyNoPpScanImageView.setOnClickListener(this);
        scanLayout.setOnClickListener(this);
        scanRelativeLayout.setOnClickListener(this);
        storyNoPhotoToDiscoverImageView.setOnClickListener(this);
        more.setOnClickListener(this);
        //初始化数据
        scanMagicPhotoNeedCallBack = false;
        myToast = new MyToast(getActivity());
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        locationList.clear();
        screenWidth = ScreenUtil.getScreenWidth(FragmentPageStory.this.getActivity());
        PictureAirLog.d(TAG, "screen width = " + screenWidth);
        //获取sp中的值
        needfresh = sharedPreferences.getBoolean(Common.NEED_FRESH, false);
        //如果不是在story获取推送，需要从application中获取，并且全部刷新
        if (app.getPushPhotoCount() + app.getPushViedoCount() > 0) {
            if (!needfresh) {
                needfresh = true;
            }
        }
        sharedNeedFresh = needfresh;
        if (needfresh) {//如果一开始就需要全部刷新，
            Editor editor = sharedPreferences.edit();
            editor.putBoolean(Common.NEED_FRESH, false);
            editor.commit();
        }
        //获取API
        isLoading = true;
        //获取地点信息
        if (ACache.get(getActivity()).getAsString(Common.LOCATION_INFO) == null) {
            API1.getLocationInfo(getActivity(), app.getTokenId(), fragmentPageStoryHandler);//获取所有的location
        } else {
            Message message = fragmentPageStoryHandler.obtainMessage();
            message.what = API1.GET_ALL_LOCATION_SUCCESS;
            message.obj = ACache.get(getActivity()).getAsString(Common.LOCATION_INFO);
            fragmentPageStoryHandler.sendMessageDelayed(message, 200);
        }

        return view;
    }


    /**
     * 排序
     */
    private void sortData(final boolean isAll) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                allPhotoList.clear();
                pictureAirPhotoList.clear();
                magicPhotoList.clear();
                boughtPhotoList.clear();
//                favouritePhotoList.clear();

                allPhotoList.addAll(AppUtil.startSortForPinnedListView(allItemInfoList));
                pictureAirPhotoList.addAll(AppUtil.startSortForPinnedListView(photoPassItemInfoList));
                magicPhotoList.addAll(AppUtil.startSortForPinnedListView(magicItemInfoList));
                boughtPhotoList.addAll(AppUtil.startSortForPinnedListView(boughtItemInfoList));
//                favouritePhotoList.addAll(AppUtil.startSortForPinnedListView(favouritePictureList));

                if (isAll) {
                    fragmentPageStoryHandler.sendEmptyMessage(SORT_COMPLETED_ALL);
                } else {
                    fragmentPageStoryHandler.sendEmptyMessage(SORT_COMPLETED_REFRESH);
                }
            }
        }.start();

    }


    /**
     * 控制控件的隐藏或者显示
     */
    private void showViewPager() {
        if (allItemInfoList != null && allItemInfoList.size() > 0) {//有图片
            PictureAirLog.out("viewpager---->has photos");
            //隐藏没有pp的情况
            storyNoPpToScanLinearLayout.setVisibility(View.GONE);
            //隐藏空图的情况
            noPhotoView.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.GONE);
            //显示有pp的情况
            storyLeadBarLinearLayout.setVisibility(View.VISIBLE);

            fragments.add(StoryFragment.getInstance(allPhotoList, targetMagicPhotoList, 0, fragmentPageStoryHandler));
            fragments.add(StoryFragment.getInstance(pictureAirPhotoList, targetMagicPhotoList, 1, fragmentPageStoryHandler));
            fragments.add(StoryFragment.getInstance(magicPhotoList, targetMagicPhotoList, 2, fragmentPageStoryHandler));
            fragments.add(StoryFragment.getInstance(boughtPhotoList, targetMagicPhotoList, 3, fragmentPageStoryHandler));
            fragments.add(StoryFragment.getInstance(favouritePhotoList, targetMagicPhotoList, 4, fragmentPageStoryHandler));
            fragmentAdapter = new FragmentAdapter(getChildFragmentManager(), fragments, titleStrings);
            storyViewPager.setAdapter(fragmentAdapter);

            indicator.setViewPager(storyViewPager);
            indicator.setVisibility(View.VISIBLE);
            storyViewPager.setVisibility(View.VISIBLE);
            storyViewPager.setOffscreenPageLimit(2);
            storyViewPager.setCurrentItem(app.fragmentStoryLastSelectedTab);
//                setTitleBarTextColor(app.fragmentStoryLastSelectedTab);
            EventBus.getDefault().post(new StoryFragmentEvent(allPhotoList, targetMagicPhotoList, 0));
            EventBus.getDefault().post(new StoryFragmentEvent(pictureAirPhotoList, targetMagicPhotoList, 1));
            EventBus.getDefault().post(new StoryFragmentEvent(magicPhotoList, targetMagicPhotoList, 2));
            EventBus.getDefault().post(new StoryFragmentEvent(boughtPhotoList, targetMagicPhotoList, 3));
            EventBus.getDefault().post(new StoryFragmentEvent(favouritePhotoList, targetMagicPhotoList, 4));
            if (app.getPushPhotoCount() + app.getPushViedoCount() == 0){
                PictureAirLog.out("need gone the badgeview");
                PictureAirLog.out("photocount---->" + app.getPushPhotoCount());
                PictureAirLog.out("video count---->" + app.getPushViedoCount());
                EventBus.getDefault().post(new RedPointControlEvent(false));
            }
        } else {//没有图片

            //判断是否应该显示左上角红点

            if (sharedPreferences.getInt(Common.PP_COUNT, 0) < 2) {//没有扫描过
                PictureAirLog.out("viewpager---->has not scan pp");
                //显示没有pp的情况
                storyNoPpToScanLinearLayout.setVisibility(View.VISIBLE);
                noPhotoView.setVisibility(View.GONE);
                swipeRefreshLayout.setVisibility(View.VISIBLE);

                //需要设置为不可见，不然会报空指针异常
                storyLeadBarLinearLayout.setVisibility(View.INVISIBLE);
                storyViewPager.setVisibility(View.INVISIBLE);
            } else {//有扫描过
                PictureAirLog.out("viewpager---->no photos");
                storyNoPpToScanLinearLayout.setVisibility(View.GONE);

                //显示空图的情况
                noPhotoView.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setVisibility(View.VISIBLE);

                //需要设置为不可见，不然会报空指针异常
                storyLeadBarLinearLayout.setVisibility(View.INVISIBLE);
                storyViewPager.setVisibility(View.INVISIBLE);
            }
        }
    }


    //扫描图片线程类
    private class ScanPhotosThread extends Thread {
        private boolean needCallBck;//onResume的时候才会检查是否有刷新数据

        public ScanPhotosThread(boolean needCallBck) {
            // TODO Auto-generated constructor stub
            this.needCallBck = needCallBck;
        }

        @Override
        public void run() {
            PictureAirLog.out("------->run");
            if (!app.scanMagicFinish) {
                if (!AppUtil.hasSDCard()) {//如果SD卡不存在
                    app.scanMagicFinish = true;
                } else {
                    if (needCallBck) {
                        PictureAirLog.d(TAG, "need remove local data first");
                        allItemInfoList.removeAll(magicItemInfoList);
                        magicItemInfoList.clear();
                    }
                }
                targetMagicPhotoList.clear();
                targetMagicPhotoList.addAll(AppUtil.getLocalPhotos(getActivity(), Common.PHOTO_SAVE_PATH, Common.ALBUM_MAGIC));
                Collections.sort(targetMagicPhotoList);
                app.scanMagicFinish = true;
            }
            if (needCallBck) {//是刷新数据操作，需要通知adatper更新数据
                fragmentPageStoryHandler.sendEmptyMessage(REFRESH_LOCAL_PHOTOS);
            }
        }
    }



    //检查更新本地照片
    private void checkLocalPhotos(String filePath) {
        File file = new File(filePath);
        File[] files = file.listFiles();
        Date date;
        //判断是否为空
        if (localPhotoList == null) {
            localPhotoList = new ArrayList<>();
        }
        localPhotoList.clear();
        PhotoInfo localPhotoInfo;
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().endsWith(".JPG") || files[i].getName().endsWith(".jpg")) {
                if (files[i].length() > 0) {//扫描到文件
                    PictureAirLog.out("scan local photo");
                    localPhotoInfo = new PhotoInfo();
                    localPhotoInfo.photoPathOrURL = files[i].getPath();
                    localPhotoInfo.lastModify = files[i].lastModified();
                    date = new Date(localPhotoInfo.lastModify);
                    localPhotoInfo.shootOn = sdf.format(date);
                    localPhotoInfo.shootTime = localPhotoInfo.shootOn.substring(0, 10);
                    localPhotoInfo.isChecked = 0;
                    localPhotoInfo.isSelected = 0;
                    localPhotoInfo.showMask = 0;
                    localPhotoInfo.locationName = getString(R.string.story_tab_magic);
                    localPhotoInfo.isPayed = 1;
                    localPhotoInfo.onLine = 0;
                    localPhotoInfo.isVideo = 0;
                    localPhotoInfo.isHasPreset = 0;
                    localPhotoList.add(localPhotoInfo);
                }
            }
        }
        PictureAirLog.out("rescan count--->" + localPhotoList.size() + ", old count--->" + targetMagicPhotoList.size());
        if (localPhotoList.size() != targetMagicPhotoList.size()) {//发现数据不一致，需要更新
            PictureAirLog.d(TAG, "local photos has update");
            allItemInfoList.removeAll(magicItemInfoList);
            magicItemInfoList.clear();
            targetMagicPhotoList.clear();
            targetMagicPhotoList.addAll(localPhotoList);
            Collections.sort(targetMagicPhotoList);

            Iterator<PhotoInfo> iterator = favouritePhotoList.iterator();
            while (iterator.hasNext()) {
                PhotoInfo info = iterator.next();
                if (info.onLine == 0) {
                    file = new File(info.photoPathOrURL);
                    if (!file.exists()) {
                        iterator.remove();
                    }
                }
            }
            fragmentPageStoryHandler.sendEmptyMessage(REFRESH_LOCAL_PHOTOS);
        }
    }

    /**
     * 检查数据库是否有数据
     */
    private void loadDataFromDataBase() {
        PictureAirLog.out("load data from database");
        long cacheTime = System.currentTimeMillis() - PictureAirDbManager.CACHE_DAY * PictureAirDbManager.DAY_TIME;
        ArrayList<PhotoInfo> resultPhotoArrayList = pictureAirDbManager.getAllPhotoFromPhotoPassInfo(false, sdf.format(new Date(cacheTime)));
        ArrayList<PhotoInfo> resultVideoArrayList = pictureAirDbManager.getAllPhotoFromPhotoPassInfo(true, sdf.format(new Date(cacheTime)));
        PictureAirLog.out("photo from db ---->" + resultPhotoArrayList.size());
        PictureAirLog.out("video from db ---->" + resultVideoArrayList.size());
        ppPhotoCount = resultPhotoArrayList.size();
        photoPassPicList.addAll(resultPhotoArrayList);
        photoPassVideoList.addAll(resultVideoArrayList);
    }

    @Override
    public void onResume() {
        PictureAirLog.out(TAG + "  ==onResume");
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        if (sharedPreferences.getBoolean(Common.NEED_FRESH, false)) {
            PictureAirLog.out("need refresh");
            app.needScanFavoritePhotos = false;//防止会重复执行，所以此处改为false
            Editor editor = sharedPreferences.edit();
            editor.putBoolean(Common.NEED_FRESH, false);
            editor.commit();
            if (!dialog.isShowing()) {
                dialog.show();
            }
            API1.getPhotosByConditions(MyApplication.getTokenId(), fragmentPageStoryHandler, null);//获取全部图片
            API1.getVideoList(null, fragmentPageStoryHandler);//获取全部视频信息
            EventBus.getDefault().post(new RedPointControlEvent(false));
        }
        if (!app.scanMagicFinish) {//app内的正常流程
            PictureAirLog.out("need scan local photos");
            if (!dialog.isShowing()) {
                dialog.show();
            }
            scanPhotosThread = new ScanPhotosThread(scanMagicPhotoNeedCallBack);
            scanPhotosThread.start();
        } else {//检查本地文件夹，属于外部原因的检查
            PictureAirLog.out("need check local photos");
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    checkLocalPhotos(Common.PHOTO_SAVE_PATH);
                }
            }.start();
        }

        if (app.needScanFavoritePhotos) {//需要扫描收藏图片
            app.needScanFavoritePhotos = false;
            favouritePhotoList.clear();
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    long cacheTime = System.currentTimeMillis() - PictureAirDbManager.CACHE_DAY * PictureAirDbManager.DAY_TIME;
                    favouritePhotoList.addAll(AppUtil.insterSortFavouritePhotos(
                            pictureAirDbManager.getFavoritePhotoInfoListFromDB(sharedPreferences.getString(Common.USERINFO_ID, ""), sdf.format(new Date(cacheTime)))));
                    fragmentPageStoryHandler.sendEmptyMessage(DEAL_FAVORITE_DATA_SUCCESS);

                }
            }.start();
        }
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        PictureAirLog.out("story-----> detach");
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PictureAirLog.out("story-----> destroy");
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        fragmentPageStoryHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 数据组合排序
     * 遍历已经存在了的图片资源，分成四大类，all，photopass，magic，bought
     *
     * @throws ParseException
     */
    private void getData() throws ParseException {
        //遍历所有photopass信息
        PhotoItemInfo photoItemInfo;
        boolean clone_contains = false;
        Date date1;
        Date date2;
        //处理网络图片
        for (int l = 0; l < photoPassPicList.size(); l++) {
            PhotoInfo info = photoPassPicList.get(l);
            PictureAirLog.d(TAG, "scan photo list:"+l);
            //先挑选出相同的locationid信息
            for (int i = 0; i < locationList.size(); i++) {
                PictureAirLog.d(TAG, "scan location:"+i);
                if (info.locationId.equals(locationList.get(i).locationId) || locationList.get(i).locationIds.contains(info.locationId)) {
                    PictureAirLog.d(TAG, "find the location");
                    //如果locationid一样，需要判断是否已经存在此item，如果有，在按照时间分类，没有，新建一个item
                    for (int j = 0; j < photoPassItemInfoList.size(); j++) {
                        PictureAirLog.d(TAG, "weather already exists:"+j);
                        if (info.shootTime.equals(photoPassItemInfoList.get(j).shootTime)
                                && (info.locationId.equals(photoPassItemInfoList.get(j).locationId) || photoPassItemInfoList.get(j).locationIds.contains(info.locationId))) {
                            PictureAirLog.d(TAG, "photo location id " + info.locationId + "____" + info.shootTime);
                            PictureAirLog.d(TAG, "location id:" + locationList.get(i).locationId + "___" + locationList.get(i).locationIds);
                            PictureAirLog.d(TAG, "location id:" + photoPassItemInfoList.get(j).locationId + "___" + photoPassItemInfoList.get(j).locationIds);
                            PictureAirLog.d(TAG, "already exist");
                            info.locationName = photoPassItemInfoList.get(j).place;
                            photoPassItemInfoList.get(j).list.add(info);
                            date1 = sdf.parse(info.shootOn);
                            date2 = sdf.parse(photoPassItemInfoList.get(j).shootOn);
                            if (date1.after(date2)) {
                                photoPassItemInfoList.get(j).shootOn = info.shootOn;
                            }
                            clone_contains = true;
                            addToBoughtList(info, i, photoPassItemInfoList.get(j).locationIds);
                            break;
                        }
                    }
                    if (!clone_contains) {
                        //初始化item的信息
                        PictureAirLog.d(TAG, "not exist");
                        photoItemInfo = new PhotoItemInfo();
                        photoItemInfo.locationId = locationList.get(i).locationId;
                        photoItemInfo.locationIds = locationList.get(i).locationIds.toString();
                        photoItemInfo.shootTime = info.shootTime;
                        if (MyApplication.getInstance().getLanguageType().equals(Common.SIMPLE_CHINESE)) {
                            photoItemInfo.place = locationList.get(i).placeCHName;
                            info.locationName = locationList.get(i).placeCHName;

                        } else {
                            photoItemInfo.place = locationList.get(i).placeENName;
                            info.locationName = locationList.get(i).placeENName;

                        }
                        photoItemInfo.list.add(info);
                        photoItemInfo.placeUrl = locationList.get(i).placeUrl;
                        photoItemInfo.latitude = locationList.get(i).latitude;
                        photoItemInfo.longitude = locationList.get(i).longitude;
                        photoItemInfo.islove = 0;
                        photoItemInfo.shootOn = info.shootOn;
                        photoPassItemInfoList.add(photoItemInfo);
                        addToBoughtList(info, i, photoItemInfo.locationIds);
                    } else {
                        clone_contains = false;
                    }
                    break;
                }
            }
        }

        //处理视频信息
        for (int i = 0; i < photoPassVideoList.size(); i++) {
            PhotoInfo info = photoPassVideoList.get(i);
//            PictureAirLog.out("video shoot time is " + info.shootOn);
            for (int j = 0; j < photoPassItemInfoList.size(); j++) {
//                PictureAirLog.out("j-->" + j + ", info shootTime-->" + info.shootTime + ", picList-->" + photoPassItemInfoList.get(j).shootTime);
                if (info.shootTime.equals(photoPassItemInfoList.get(j).shootTime)) {
//                    PictureAirLog.out("j-->" + j + ", info.isVideo-->" + info.isVideo + ", picList-->" + photoPassItemInfoList.get(j).list.get(0).isVideo);
                    if (info.isVideo == photoPassItemInfoList.get(j).list.get(0).isVideo) {
                        photoPassItemInfoList.get(j).list.add(info);
                        date1 = sdf.parse(info.shootOn);
                        date2 = sdf.parse(photoPassItemInfoList.get(j).shootOn);
//                        PictureAirLog.out("date--->" + date1 + ";2-->" + date2);
                        if (date1.after(date2)) {
                            photoPassItemInfoList.get(j).shootOn = info.shootOn;
                        }
                        clone_contains = true;
                        break;
                    }
                }
            }
            //判断是否需要new
            if (!clone_contains) {//如果之前没有找到，说明需要new
                photoItemInfo = new PhotoItemInfo();
//                PictureAirLog.out("need new shootTime:" + info.shootTime);
                photoItemInfo.shootTime = info.shootTime;
                photoItemInfo.place = getString(R.string.video_location);
                photoItemInfo.list.add(info);
                photoItemInfo.shootOn = info.shootOn;
                photoPassItemInfoList.add(photoItemInfo);
            } else {
                clone_contains = false;
            }
        }

        //如果网络图片先处理完，magic相册还没处理完的话，需要等待magic处理完
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (isLoading) {
                    //如果magic相册已经处理完毕
//                    PictureAirLog.d(TAG, "waiting for scanning local photo completed");
                    if (app.scanMagicFinish) {
                        isLoading = false;
                    }
                }
                try {
                    getMagicData();
                    //将magic和photopass列表放入all中
                    Collections.sort(photoPassItemInfoList);
                    allItemInfoList.addAll(photoPassItemInfoList);
                    allItemInfoList.addAll(magicItemInfoList);
                    Collections.sort(allItemInfoList);//对all进行排序
                    favouritePhotoList.clear();
                    long cacheTime = System.currentTimeMillis() - PictureAirDbManager.CACHE_DAY * PictureAirDbManager.DAY_TIME;
                    favouritePhotoList.addAll(AppUtil.insterSortFavouritePhotos(
                            pictureAirDbManager.getFavoritePhotoInfoListFromDB(sharedPreferences.getString(Common.USERINFO_ID, ""), sdf.format(new Date(cacheTime)))));
                    PictureAirLog.out("location is ready----->" + favouritePhotoList.size());
                    fragmentPageStoryHandler.sendEmptyMessage(LOAD_COMPLETED);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                PictureAirLog.d(TAG, "------>completed");
            }
        }).start();

    }

    //处理本地照片的刷新数据
    private void dealLocalRefreshedData() {
        // TODO Auto-generated method stub
        PictureAirLog.d(TAG, "dealLocalRefreshedData");
        try {
            getMagicData();
            //将magic和photopass列表放入all中
            if (!allItemInfoList.containsAll(photoPassItemInfoList)) {
                PictureAirLog.out("all lIst 不包含photopasspicturelist");
                allItemInfoList.addAll(photoPassItemInfoList);
            }
            allItemInfoList.addAll(magicItemInfoList);
            Collections.sort(allItemInfoList);//对all进行排序
            PictureAirLog.out("location is ready");
            fragmentPageStoryHandler.sendEmptyMessage(LOAD_COMPLETED);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        PictureAirLog.d(TAG, "------>completed");
    }


    /**
     * 添加到已购买的列表
     *
     * @param info
     */
    private void addToBoughtList(PhotoInfo info, int position, String locationIds) {
        PhotoItemInfo photoItemInfo;
        boolean isContains = false;
        //判断是否已经购买
        if (info.isPayed == 1) {//已购买状态，需要将图片放到bought列表中
            PictureAirLog.d(TAG, "add to bought list");
            for (int j = 0; j < boughtItemInfoList.size(); j++) {
                PictureAirLog.d(TAG, "检查之前的是否存在");

                if (info.shootTime.equals(boughtItemInfoList.get(j).shootTime) &&
                        (info.locationId.equals(boughtItemInfoList.get(j).locationId) || boughtItemInfoList.get(j).locationIds.contains(info.locationId))) {
                    PictureAirLog.d(TAG, "已经存在于bought列表");
//                    info.locationName = boughtItemInfoList.get(j).place;
                    boughtItemInfoList.get(j).list.add(info);
                    isContains = true;
                    break;
                }
            }
            if (!isContains) {//没有
                PictureAirLog.d(TAG, "不存在于之前的已购买的列表");
                //初始化item的信息
                photoItemInfo = new PhotoItemInfo();
                photoItemInfo.locationId = info.locationId;
                photoItemInfo.locationIds = locationIds;
                photoItemInfo.shootTime = info.shootTime;
                if (MyApplication.getInstance().getLanguageType().equals(Common.SIMPLE_CHINESE)) {
                    photoItemInfo.place = locationList.get(position).placeCHName;

                } else {
                    photoItemInfo.place = locationList.get(position).placeENName;

                }
                photoItemInfo.list.add(info);
                photoItemInfo.placeUrl = locationList.get(position).placeUrl;
                photoItemInfo.latitude = locationList.get(position).latitude;
                photoItemInfo.longitude = locationList.get(position).longitude;
                photoItemInfo.islove = 0;
                photoItemInfo.shootOn = info.shootOn;
                boughtItemInfoList.add(photoItemInfo);
            } else {
                isContains = false;
            }
        } else {

        }
    }

    /**
     * 添加到已购买的列表
     *
     * @param info
     */
    private void addRefreshDataToBoughtList(PhotoInfo info, String locationIds, String placeName, String placeUrl, double latitude, double longitude) {
        PhotoItemInfo photoItemInfo;
        boolean isContains = false;
        //判断是否已经购买
        if (info.isPayed == 1) {//已购买状态，需要将图片放到bought列表中
            PictureAirLog.d(TAG, "add to bought list");
            for (int j = 0; j < boughtItemInfoList.size(); j++) {
                PictureAirLog.d(TAG, "检查之前的是否存在");

                if (info.shootTime.equals(boughtItemInfoList.get(j).shootTime) &&
                        (info.locationId.equals(boughtItemInfoList.get(j).locationId) || boughtItemInfoList.get(j).locationIds.contains(info.locationId))) {
                    PictureAirLog.d(TAG, "已经存在于bought列表");

                    //比较时间，按照时间排序
                    for (int i = 0; i < boughtItemInfoList.get(j).list.size(); i++) {
                        try {
                            Date date1 = sdf.parse(boughtItemInfoList.get(j).list.get(i).shootOn);
                            Date date2 = sdf.parse(info.shootOn);//获取列表中的时间
                            Date date3 = sdf.parse(boughtItemInfoList.get(j).shootOn);
                            info.locationName = boughtItemInfoList.get(j).place;

                            if (date2.after(date1)) {//需要添加的时间是最新的，显示在最前面
                                PictureAirLog.out("the lastest time, need add");
                                boughtItemInfoList.get(j).list.add(i, info);
                                if (date2.after(date3)) {//当前时间date3之后
                                    boughtItemInfoList.get(j).shootOn = info.shootOn;//更新shootOn的时间
                                }
                                break;
                            } else {
                                if (i == (boughtItemInfoList.get(j).list.size() - 1)) {//如果已经在最后一张了，直接添加在最后面
                                    PictureAirLog.out("the last position, need add");
                                    boughtItemInfoList.get(j).list.add(info);
                                    if (date2.after(date3)) {//当前时间date3之后
                                        boughtItemInfoList.get(j).shootOn = info.shootOn;//更新shootOn的时间
                                    }
                                    break;
                                } else {

                                    PictureAirLog.out("scan next------>");
                                }
                            }
                        } catch (ParseException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }//获取列表中的时间
                    }
                    isContains = true;
                    break;
                }
            }
            if (!isContains) {//没有
                PictureAirLog.d(TAG, "不存在于之前的已购买的列表");
                //初始化item的信息
                photoItemInfo = new PhotoItemInfo();
                photoItemInfo.locationId = info.locationId;
                photoItemInfo.locationIds = locationIds;
                photoItemInfo.shootTime = info.shootTime;
//                if (MyApplication.getInstance().getLanguageType().equals(Common.SIMPLE_CHINESE)) {
//                    photoItemInfo.place = locationList.get(position).placeCHName;
//
//                } else {
                    photoItemInfo.place = placeName;

//                }
                photoItemInfo.list.add(info);
                photoItemInfo.placeUrl = placeUrl;
                photoItemInfo.latitude = latitude;
                photoItemInfo.longitude = longitude;
                photoItemInfo.islove = 0;
                photoItemInfo.shootOn = info.shootOn;
                boughtItemInfoList.add(photoItemInfo);
            } else {
                isContains = false;
            }
        } else {

        }
    }

    /**
     * 遍历所有magic图片信息
     * 1.判断现有列表是否已经存在
     * 2.如果存在，直接添加在item列表后面，并且将shootOn的值更新为最大的
     * 3.如果不存在，新建item
     *
     * @throws ParseException
     */
    private void getMagicData() throws ParseException {
        PictureAirLog.d(TAG, "----------->get magic photos" + targetMagicPhotoList.size() + "____" + magicItemInfoList.size());
        magicItemInfoList.clear();//添加之前，先清除，防止添加pp/pp+造成数据重复添加
        magicItemInfoList.addAll(AppUtil.getMagicItemInfoList(getActivity(), sdf, targetMagicPhotoList));
//        PhotoItemInfo photoItemInfo;
//        boolean clone_contains = false;
//        Date date1;
//        Date date2;
//        for (int i = 0; i < targetMagicPhotoList.size(); i++) {
//            PictureAirLog.out("photo shoot time is " + targetMagicPhotoList.get(i).shootOn);
//            for (int j = 0; j < magicItemInfoList.size(); j++) {
//                if (targetMagicPhotoList.get(i).shootTime.equals(magicItemInfoList.get(j).shootTime)) {
//                    magicItemInfoList.get(j).list.add(targetMagicPhotoList.get(i));
//                    date1 = sdf.parse(targetMagicPhotoList.get(i).shootOn);
//                    date2 = sdf.parse(magicItemInfoList.get(j).shootOn);
//                    if (date1.after(date2)) {
//                        magicItemInfoList.get(j).shootOn = targetMagicPhotoList.get(i).shootOn;
//                    }
//                    clone_contains = true;
//                    break;
//                }
//            }
//            //判断是否需要new
//            if (!clone_contains) {//如果之前没有找到，说明需要new
//                photoItemInfo = new PhotoItemInfo();
//                PictureAirLog.out("shootTime:" + targetMagicPhotoList.get(i).shootTime);
//                photoItemInfo.shootTime = targetMagicPhotoList.get(i).shootTime;
//                photoItemInfo.place = getString(R.string.story_tab_magic);
//                photoItemInfo.list.add(targetMagicPhotoList.get(i));
//                photoItemInfo.shootOn = targetMagicPhotoList.get(i).shootOn;
//                magicItemInfoList.add(photoItemInfo);
//            } else {
//                clone_contains = false;
//            }
//        }
    }

    /**
     * 刷新数据的处理
     * 1.对所有获取的信息按照locationid和shoottime分类，放入一个新的photoiteminfo中
     * 2.加入list中，判断新的locationid是否在无photo的locationid中，如果有，需要重新排列list
     * 3.并且更新adapter
     */
    private void getrefreshdata() {
        PictureAirLog.e("getdata", "refreshdata");
        //根据数量，加入新的item
        PictureAirLog.out("all update data=" + photoPassPicList.size());
        PictureAirLog.out("all update video data=" + photoPassVideoList.size());
        PhotoItemInfo itemInfo;
        boolean findLocation = false;
        //先清除之前旧的列表
        allItemInfoList.removeAll(photoPassItemInfoList);

        //将图片按照location加载到list中去
        for (int l = photoPassPicList.size() - refreshDataCount; l < photoPassPicList.size(); l++) {//遍历所要添加的图片list
            PictureAirLog.out("遍历照片");
            PhotoInfo info = photoPassPicList.get(l);
            //查找list_clone有图片的item，如果找到locationid，在判断是否有同一天的photos，如果有同一天的，add进去，如果没有，新建一个项
            for (int j = 0; j < photoPassItemInfoList.size(); j++) {//遍历list，查找locationid一样的内容
                PictureAirLog.out("遍历地址");
                PhotoItemInfo p = photoPassItemInfoList.get(j);
                if (p.locationId == null) {//此item为视频，直接跳过
                    continue;
                }
                if (info.locationId.equals(p.locationId) || p.locationIds.contains(info.locationId)) {//如果locationId和photo的locationid一样
                    PictureAirLog.out("location一样");
                    findLocation = true;
                    if (info.shootTime.equals(p.shootTime)) {//如果shoottime一致，则插入到列表中
                        PictureAirLog.out("shootTime一致，直接插入列表");
                        //比较时间，按照时间排序
                        for (int i = 0; i < p.list.size(); i++) {
                            try {
                                //									PictureAirLog.out("date1--->"+p.list.get(i).shootOn);
                                //									PictureAirLog.out("date2--->"+info.shootOn);
                                Date date1 = sdf.parse(p.list.get(i).shootOn);
                                Date date2 = sdf.parse(info.shootOn);//获取列表中的时间
                                Date date3 = sdf.parse(p.shootOn);
                                info.locationName = p.place;

                                //									PictureAirLog.out("date1--->"+date1);
                                //									PictureAirLog.out("date2--->"+date2);
                                if (date2.after(date1)) {//需要添加的时间是最新的，显示在最前面
                                    PictureAirLog.out("the lastest time, need add");
                                    p.list.add(i, info);
                                    PictureAirLog.out("size->" + p.list.size());
                                    if (date2.after(date3)) {//当前时间date3之后
                                        p.shootOn = info.shootOn;//更新shootOn的时间
                                    }
                                    addRefreshDataToBoughtList(info, p.locationIds, p.place, p.placeUrl, p.latitude, p.longitude);
                                    break;
                                } else {
                                    if (i == (p.list.size() - 1)) {//如果已经在最后一张了，直接添加在最后面
                                        PictureAirLog.out("the last position, need add");
                                        p.list.add(info);
                                        PictureAirLog.out("size->" + p.list.size());
                                        if (date2.after(date3)) {//当前时间date3之后
                                            p.shootOn = info.shootOn;//更新shootOn的时间
                                        }
                                        addRefreshDataToBoughtList(info, p.locationIds, p.place, p.placeUrl, p.latitude, p.longitude);
                                        break;
                                    } else {

                                        PictureAirLog.out("scan next------>");
                                    }
                                }
                            } catch (ParseException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }//获取列表中的时间
                        }
                        //							PictureAirLog.out("after add new photo------>");
                        //记录当前的列表的索引
                        //							needmove = j;
                    } else {//时间不一致，新建列表
                        PictureAirLog.out("时间不一致，新建列表");
                        itemInfo = new PhotoItemInfo();
                        itemInfo.locationId = p.locationId;
                        itemInfo.locationIds = p.locationIds;
                        itemInfo.shootTime = info.shootTime;
                        itemInfo.place = p.place;
                        info.locationName = p.place;
                        itemInfo.list.add(0, info);
                        PictureAirLog.out("size->" + itemInfo.list.size());
                        itemInfo.placeUrl = p.placeUrl;
                        itemInfo.latitude = p.latitude;
                        itemInfo.longitude = p.longitude;
                        itemInfo.shootOn = info.shootOn;
                        //							itemInfo.gps = p.gps;
                        itemInfo.islove = p.islove;
                        photoPassItemInfoList.add(0, itemInfo);//放置到列表的顶部

                        addRefreshDataToBoughtList(info, p.locationIds, p.place, p.placeUrl, p.latitude, p.longitude);
                    }
                    //					}
                    break;
                }
            }
            if (findLocation) {//如果之前已经找到了对应的位置
                PictureAirLog.out("找到位置");
                findLocation = false;
            } else {//如果之前没有找到对应的位置，遍历地址列表，需要新建一个item，并且放入到最上方
                for (int k = 0; k < locationList.size(); k++) {
                    PictureAirLog.out("没有找到位置，遍历location");
                    if (info.locationId.equals(locationList.get(k).locationId) || locationList.get(k).locationIds.contains(info.locationId)) {
                        PictureAirLog.out("找到其他的location");
                        itemInfo = new PhotoItemInfo();
                        itemInfo.locationId = locationList.get(k).locationId;
                        itemInfo.locationIds = locationList.get(k).locationIds.toString();
                        itemInfo.shootTime = info.shootTime;
                        if (MyApplication.getInstance().getLanguageType().equals(Common.SIMPLE_CHINESE)) {
                            itemInfo.place = locationList.get(k).placeCHName;
                            info.locationName = locationList.get(k).placeCHName;

                        } else {
                            itemInfo.place = locationList.get(k).placeENName;
                            info.locationName = locationList.get(k).placeENName;

                        }
                        itemInfo.list.add(info);
                        itemInfo.placeUrl = locationList.get(k).placeUrl;
                        itemInfo.latitude = locationList.get(k).latitude;
                        itemInfo.longitude = locationList.get(k).longitude;
                        itemInfo.islove = 0;
                        itemInfo.shootOn = info.shootOn;
                        photoPassItemInfoList.add(0, itemInfo);
                        addRefreshDataToBoughtList(info, itemInfo.locationIds, itemInfo.place, itemInfo.placeUrl, itemInfo.latitude, itemInfo.longitude);
                        break;
                    }

                }
            }
        }
        refreshDataCount = 0;

        //将视频加载到list中去
        for (int l = photoPassVideoList.size() - refreshVideoDataCount; l < photoPassVideoList.size(); l++) {//遍历所要添加的图片list
            PictureAirLog.out("遍历照片");
            PhotoInfo info = photoPassVideoList.get(l);
            for (int j = 0; j < photoPassItemInfoList.size(); j++) {//遍历list，查找locationid一样的内容
                PhotoItemInfo p = photoPassItemInfoList.get(j);
                if (info.shootTime.equals(p.shootTime) && info.isVideo == p.list.get(0).isVideo) {
                    findLocation = true;
                    for (int i = 0; i < p.list.size(); i++) {
                        try {
                            Date date1 = sdf.parse(p.list.get(i).shootOn);
                            Date date2 = sdf.parse(info.shootOn);//获取列表中的时间
                            Date date3 = sdf.parse(p.shootOn);
                            if (date2.after(date1)) {//需要添加的时间是最新的，显示在最前面
                                PictureAirLog.out("the lastest time, need add");
                                p.list.add(i, info);
                                PictureAirLog.out("size->" + p.list.size());
                                if (date2.after(date3)) {//当前时间date3之后
                                    p.shootOn = info.shootOn;//更新shootOn的时间
                                }
                                break;
                            } else {
                                if (i == (p.list.size() - 1)) {//如果已经在最后一张了，直接添加在最后面
                                    PictureAirLog.out("the last position, need add");
                                    p.list.add(info);
                                    PictureAirLog.out("size->" + p.list.size());
                                    if (date2.after(date3)) {//当前时间date3之后
                                        p.shootOn = info.shootOn;//更新shootOn的时间
                                    }
                                    break;
                                } else {
                                    PictureAirLog.out("scan next------>");
                                }
                            }
                        } catch (ParseException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }//获取列表中的时间
                    }
                }
            }
            if (findLocation) {//如果之前已经找到了对应的位置
                PictureAirLog.out("找到位置");
                findLocation = false;
            } else {//如果之前没有找到对应的位置，遍历地址列表，需要新建一个item，并且放入到最上方
                itemInfo = new PhotoItemInfo();
                itemInfo.shootTime = info.shootTime;
                itemInfo.shootOn = info.shootOn;
                itemInfo.place = getString(R.string.video_location);
                itemInfo.list.add(info);
                photoPassItemInfoList.add(0, itemInfo);
            }
        }
        refreshVideoDataCount = 0;

        allItemInfoList.addAll(photoPassItemInfoList);
        PictureAirLog.out("start-----------> all sort");
        Collections.sort(allItemInfoList);//对all进行排序
        PictureAirLog.out("start-----------> photoPass sort");
        Collections.sort(photoPassItemInfoList);
    }


    @SuppressLint("NewApi")
    @Override
    public void onClick(View v) {
        Intent i = null;
        switch (v.getId()) {
            //扫描按钮
            case R.id.story_scan:
            case R.id.storyScanRelativeLayout:
            case R.id.story_no_pp_scan:
                i = new Intent(getActivity(), MipCaptureActivity.class);
                startActivity(i);
                break;

            case R.id.story_more:
                // Disney Video
                DisneyVideoTool.getIsOneGoToDisneyVideoPage(context);
                // storyMenuPop.showAsDropDown(v);
                break;

            case R.id.story_to_discover://跳转到Discover页面
                PictureAirLog.out("Onclick---->Discover");
                EventBus.getDefault().post(new MainTabSwitchEvent(MainTabSwitchEvent.DISCOVER_TAB));
                break;

            default:
                break;
        }

    }

    /**
     * 检测story按钮的点击事件，点击了执行刷新操作
     *
     * @param baseBusEvent
     */
    @Subscribe
    public void onUserEvent(BaseBusEvent baseBusEvent) {
        if (baseBusEvent instanceof StoryRefreshOnClickEvent) {
            StoryRefreshOnClickEvent storyRefreshOnClickEvent = (StoryRefreshOnClickEvent) baseBusEvent;
            if (storyRefreshOnClickEvent.isStoryTabClick()) {//通知页面开始刷新
                if (noPhotoView.isShown() || storyNoPpToScanLinearLayout.isShown()) {
                    PictureAirLog.out("do refresh when noPhotoView is showing");
                    if (!swipeRefreshLayout.isRefreshing()) {
                        noPhotoViewStateRefresh = true;
                        swipeRefreshLayout.setEnabled(true);
                        swipeRefreshLayout.setRefreshing(true);
                        Message message = fragmentPageStoryHandler.obtainMessage();
                        message.what = REFRESH;
                        fragmentPageStoryHandler.sendMessage(message);
                    }
                } else {
                    PictureAirLog.out("do refresh when noPhotoView is not showing");
                    EventBus.getDefault().post(new StoryRefreshEvent(app.fragmentStoryLastSelectedTab, StoryRefreshEvent.START_REFRESH));

                }
            }
            EventBus.getDefault().removeStickyEvent(storyRefreshOnClickEvent);
        }
        if (baseBusEvent instanceof SocketEvent) {
            SocketEvent socketEvent = (SocketEvent) baseBusEvent;
            if (!noPhotoView.isShown() && !syncingBoughtPhotos) {//延迟2秒，防止多次执行导致app异常
                syncingBoughtPhotos = true;
                PictureAirLog.out("start sync------->");
                fragmentPageStoryHandler.sendEmptyMessageDelayed(SYNC_BOUGHT_PHOTOS, 2000);
            } else {
                PictureAirLog.out("still waiting sync");
            }
            //刷新列表
            EventBus.getDefault().removeStickyEvent(socketEvent);
        }
    }


    /**
     * 下载方法
     * @param lists
     */
    private void downLoadPhoto(ArrayList<PhotoInfo> lists) {
        if (settingUtil.isAutoUpdate(sharedPreferences.getString(Common.USERINFO_ID, ""))) {
            for (int i = 0; i < lists.size(); i++) {
                if (lists.get(i).isPayed == 1) {
                    download(lists);
                }
            }
        }
    }

    private void download(ArrayList<PhotoInfo> arrayList) {
        if (arrayList.size() > 0) {
            Intent intent = new Intent(getContext(),
                    DownloadService.class);
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("photos", arrayList);
            intent.putExtras(bundle);
            getContext().startService(intent);
        }
    }


}
