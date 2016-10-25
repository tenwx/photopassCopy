package com.pictureair.photopass.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.BaseFragment;
import com.pictureair.photopass.activity.InputCodeActivity;
import com.pictureair.photopass.activity.MipCaptureActivity;
import com.pictureair.photopass.activity.MyPPPActivity;
import com.pictureair.photopass.activity.PPPDetailProductActivity;
import com.pictureair.photopass.activity.PanicBuyActivity;
import com.pictureair.photopass.adapter.FragmentAdapter;
import com.pictureair.photopass.customDialog.PWDialog;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.DealingInfo;
import com.pictureair.photopass.entity.DiscoverLocationItemInfo;
import com.pictureair.photopass.entity.GoodsInfo;
import com.pictureair.photopass.entity.GoodsInfoJson;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.entity.PhotoItemInfo;
import com.pictureair.photopass.eventbus.BaseBusEvent;
import com.pictureair.photopass.eventbus.MainTabOnClickEvent;
import com.pictureair.photopass.eventbus.MainTabSwitchEvent;
import com.pictureair.photopass.eventbus.RedPointControlEvent;
import com.pictureair.photopass.eventbus.SocketEvent;
import com.pictureair.photopass.eventbus.StoryFragmentEvent;
import com.pictureair.photopass.eventbus.StoryLoadCompletedEvent;
import com.pictureair.photopass.eventbus.StoryRefreshEvent;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.JsonTools;
import com.pictureair.photopass.util.JsonUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.util.SettingUtil;
import com.pictureair.photopass.widget.CustomTextView;
import com.pictureair.photopass.widget.NoNetWorkOrNoCountView;
import com.pictureair.photopass.widget.PPPPop;
import com.pictureair.photopass.widget.PWToast;
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
    private static final int GET_REFRESH_DATA_DONE = 1004;

    private static final String TAG = "FragmentPageStory";
    private String[] titleStrings;

    //申明变量
    private int refreshDataCount = 0;//记录刷新数据的数量
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
    private boolean isOnCreate = false;
    private boolean scanMagicPhotoNeedCallBack;//记录是否需要重新扫描本地照片
    private boolean noPhotoViewStateRefresh = false;//无图的时候进行的刷新
    private int ppPhotoCount;
    private boolean hasHidden = false;

    //申明控件
    private ImageView scanIv;
    private RelativeLayout scanLayout;
    private LinearLayout noPhotoView;
    private ViewPager storyViewPager;
    private RelativeLayout storyNoPpToScanLinearLayout;
    private ImageView storyNoPpScanImageView;
    private ImageView storyNoPhotoToDiscoverImageView;
    private NoNetWorkOrNoCountView noNetWorkOrNoCountView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PPPPop pppPop;
    private CustomTextView buyPPP;
    private CustomTextView activatePPP;
    private CustomTextView storyTopTip;
    private CustomTextView storyScan;
    private CustomTextView storyDiscover;
    private CustomTextView storyNoPhotoTip;
    private CustomTextView specialDealOnTV;
    private LinearLayout specialDealLL;

    //申明类
    private MyApplication app;
    private ArrayList<PhotoItemInfo> photoPassItemInfoList;
    private ArrayList<PhotoItemInfo> magicItemInfoList;
    private ArrayList<PhotoItemInfo> boughtItemInfoList = new ArrayList<>();// 所有已经购买的图片的信息
    private ArrayList<PhotoItemInfo> allItemInfoList = new ArrayList<>();// 所有的图片信息

    private ArrayList<PhotoInfo> photoPassPicList = new ArrayList<>();// 所有的从服务器返回的photopass图片的信息
    private ArrayList<PhotoInfo> targetMagicPhotoList = new ArrayList<>();

    private ArrayList<PhotoInfo> allPhotoList, pictureAirPhotoList, magicPhotoList, boughtPhotoList, favouritePhotoList, localPhotoList;
    private ArrayList<DiscoverLocationItemInfo> locationList = new ArrayList<>();
    private List<Fragment> fragments = new ArrayList<>();
    private FragmentAdapter fragmentAdapter;
    private Activity context;
    private SimpleDateFormat sdf;
    private String userId;
    private PWToast myToast;
    private ScanPhotosThread scanPhotosThread;
    private PictureAirDbManager pictureAirDbManager;
    private PWDialog pwDialog;
    private DealingInfo dealingInfo;
    private boolean getPhotoInfoDone = false;

    /**
     * 同步已经购买的照片
     */
    private boolean syncingBoughtPhotos = false;

    private SettingUtil settingUtil;
    private LinearLayout storyLeadBarLinearLayout;
    private TabPageIndicator indicator;

    private static final int REQUEST_STORAGE_PERMISSION = 1;
    private boolean mIsAskStoragePermission = false;

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
                finishLoad(true);
                break;

            case API1.GET_ALL_LOCATION_FAILED://获取地址信息失败
                finishLoad(true);
                break;

            case API1.GET_ALL_LOCATION_SUCCESS://成功获取地点信息
                PictureAirLog.d(TAG, "---------->get location success");
                if (context == null) {
                    break;
                }
                locationList.clear();
                locationList.addAll(AppUtil.getLocation(context, msg.obj.toString(), true));
                //检查数据库是否有数据，如果有数据，直接显示，如果没有数据，从网络获取
                photoPassPicList.clear();

                if (!needfresh) {//如果需要刷新数据的话，就不需要从数据库中获取数据
                    //  如果PP中的照片大于 10 张，并且账户中没有PP＋。就提示购买PP+
                    if (settingUtil.isFirstPP10(userId)) {
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
                    API1.getPhotosByConditions(MyApplication.getTokenId(), fragmentPageStoryHandler, null, null);//获取全部图片
                } else {
                    PictureAirLog.out("photolist size = " + photoPassPicList.size());
                    //有数据，直接显示
                    photoPassItemInfoList.clear();
                    allItemInfoList.clear();
                    boughtItemInfoList.clear();
                    getData();
                }
                break;

            case API1.GET_REFRESH_PHOTOS_BY_CONDITIONS_FAILED://获取刷新失败
                PictureAirLog.out("get photo refresh failed------>");
                getPhotoInfoDone = true;
                finishLoad(false);
                break;

            case API1.GET_ALL_PHOTOS_BY_CONDITIONS_SUCCESS://获取照片成功

                PictureAirLog.d(TAG, "--------->get photo success");
                saveJsonToSQLite((JSONObject) msg.obj, true);
                break;

            case API1.GET_REFRESH_PHOTOS_BY_CONDITIONS_SUCCESS://获取刷新的推送图片
//                    app.setPushPhotoCount(0);
                PictureAirLog.d(TAG, "deal refresh photos-------");
                saveJsonToSQLite((JSONObject) msg.obj, false);
                break;

            case API1.GET_SOCKET_DATA_SUCCESS://手动刷新成功
                //获取推送成功，后面逻辑按照之前走
                PictureAirLog.e(TAG, "GET_SOCKET_DATA_SUCCESS: " + msg.obj.toString());
                JSONObject jsonObject = (JSONObject) msg.obj;
                if (jsonObject.size() > 0) {
                    JsonUtil.dealGetSocketData(MyApplication.getInstance().getApplicationContext(), jsonObject.toString(), true, null);
                }
                break;

            case REFRESH_LOCAL_PHOTOS://刷新处理本地照片
                PictureAirLog.d(TAG, "scan local photos success");
                if (!isOnCreate) {//当不在oncreate的时候，需要刷新本地图片
                    dealLocalRefreshedData();
                }
                break;

            case REFRESH://开始刷新
                PictureAirLog.d(TAG, "the index of refreshing is " + msg.arg1);
                API1.getPhotosByConditions(MyApplication.getTokenId(), fragmentPageStoryHandler,
                        SPUtils.getString(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.LAST_UPDATE_PHOTO_TIME, null),
                        null);//获取更新信息
                API1.getSocketData(fragmentPageStoryHandler);//手动拉取socket信息
                showLeadView();
                break;

            case DEAL_ALL_PHOTO_DATA_DONE://处理照片成功
                app.setPushPhotoCount(0);//清空推送消息的数量
                getPhotoInfoDone = true;
                getDataFinish();
                break;

            case DEAL_ALL_VIDEO_DATA_DONE://处理全部视频成功
                app.setPushViedoCount(0);
                getDataFinish();
                break;

            case DEAL_REFRESH_PHOTO_DATA_DONE://处理刷新照片成功
                app.setPushPhotoCount(0);
                getPhotoInfoDone = true;
                PictureAirLog.d(TAG, "deal refresh photos done");
                SPUtils.put(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, "photoCount", 0);
                getRefreshDataFinish();
                break;

            case DEAL_REFRESH_VIDEO_DATA_DONE://处理刷新视频成功
                app.setPushViedoCount(0);
                PictureAirLog.out("deal refresh video done");
                getRefreshDataFinish();
                break;

            case DEAL_FAVORITE_DATA_SUCCESS://处理收藏图片成功
                EventBus.getDefault().post(new StoryFragmentEvent(favouritePhotoList, targetMagicPhotoList, 4));
                break;

            case GET_REFRESH_DATA_DONE://处理刷新数据成功
                PictureAirLog.out("start sortdata");
                sortData(false);
                break;

            case SORT_COMPLETED_REFRESH:
                //刷新广告地点
                app.setGetADLocationSuccess(false);
                API1.getADLocations(0, fragmentPageStoryHandler);

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
                    API1.getADLocations(0, fragmentPageStoryHandler);
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
                    showViewPager();
                    noNetWorkOrNoCountView.setVisibility(View.GONE);//无网络状态的View设置为不可见
                    if (sharedNeedFresh) {
                        sharedNeedFresh = false;
                    }
                }

                dismissPWProgressDialog();
                break;

            case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_RELOAD://noView的按钮响应重新加载点击事件
                //重新加载数据
                PictureAirLog.out("onclick with reload");
                showPWProgressDialog();
                if (TextUtils.isEmpty(ACache.get(getActivity()).getAsString(Common.DISCOVER_LOCATION))) {//地址获取失败
                    API1.getLocationInfo(context, MyApplication.getTokenId(), fragmentPageStoryHandler);//获取所有的location
                } else {//地址获取成功，但是照片获取失败
                    Message message = fragmentPageStoryHandler.obtainMessage();
                    message.what = API1.GET_ALL_LOCATION_SUCCESS;
                    message.obj = ACache.get(getActivity()).getAsString(Common.DISCOVER_LOCATION);
                    fragmentPageStoryHandler.sendMessage(message);
                }
                break;

            case API1.GET_PPP_SUCCESS:
                if (ppPhotoCount >= 10 && API1.PPPlist.size() == 0 && context != null) {
                    if (pwDialog == null) {
                        pwDialog = new PWDialog(context)
                                .setPWDialogMessage(R.string.pp_first_up10_msg)
                                .setPWDialogNegativeButton(R.string.pp_first_up10_no_msg)
                                .setPWDialogPositiveButton(R.string.pp_first_up10_yes_msg)
                                .setOnPWDialogClickListener(new PWDialog.OnPWDialogClickListener() {
                                    @Override
                                    public void onPWDialogButtonClicked(int which, int dialogId) {
                                        if (which == DialogInterface.BUTTON_POSITIVE) {
                                            // 去升级：购买AirPass+页面. 由于失去了airPass详情的界面。故此处，跳转到了airPass＋的界面。
                                            Intent intent = new Intent();
                                            intent.setClass(context, MyPPPActivity.class);
                                            startActivity(intent);
                                        }
                                    }
                                })
                                .pwDialogCreate();
                    }
                    pwDialog.pwDilogShow();
                    settingUtil.insertSettingFirstPP10Status(userId);
                } else if (API1.PPPlist.size() > 0) {
                    settingUtil.insertSettingFirstPP10Status(userId);
                }
                break;

            case SYNC_BOUGHT_PHOTOS://同步已购买图片
                /**
                 * 1.重新从数据库获取一遍数据
                 * 2.更新页面
                 */
                showPWProgressDialog();

                new Thread(){
                    @Override
                    public void run() {
                        synchronized (this) {
                            photoPassPicList.clear();
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
                getData();
                break;

            case PPPPop.POP_SCAN:
                Intent intent = new Intent(context, MipCaptureActivity.class);
                startActivity(intent);
                if (pppPop.isShowing()) {
                    pppPop.dismiss();
                }
                break;

            case PPPPop.POP_INPUT://进入手动输入页面
                Intent intent2 = new Intent(context, InputCodeActivity.class);
                startActivity(intent2);
                if (pppPop.isShowing()) {
                    pppPop.dismiss();
                }
                break;

            case API1.GET_GOODS_SUCCESS:
                if (context == null) {
                    break;
                }
                List<GoodsInfo> allGoodsList1 = new ArrayList<>();
                GoodsInfo pppGoodsInfo = null;
                GoodsInfoJson goodsInfoJson = JsonTools.parseObject(msg.obj.toString(), GoodsInfoJson.class);//GoodsInfoJson.getString()
                if (goodsInfoJson != null && goodsInfoJson.getGoods().size() > 0) {
                    allGoodsList1.addAll(goodsInfoJson.getGoods());
                }
                //获取PP+
                for (GoodsInfo goodsInfo : allGoodsList1) {
                    if (goodsInfo.getName().equals(Common.GOOD_NAME_PPP)) {
                        pppGoodsInfo = goodsInfo;
                        break;
                    }
                }
                dismissPWProgressDialog();
                //跳转到PP+详情页面
                Intent intent3 = new Intent(context, PPPDetailProductActivity.class);
                intent3.putExtra("goods", pppGoodsInfo);
                startActivity(intent3);
                break;

            case API1.GET_GOODS_FAILED:
                dismissPWProgressDialog();
                myToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);
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
        getPhotoInfoDone = false;
        dismissPWProgressDialog();
        if (setVisibile) {
            storyNoPpToScanLinearLayout.setVisibility(View.GONE);
            noNetWorkOrNoCountView.setVisibility(View.VISIBLE);
            if (sharedNeedFresh) {
                needfresh = sharedNeedFresh;
            }
            noNetWorkOrNoCountView.setResult(R.string.no_network, R.string.click_button_reload, R.string.reload, R.drawable.no_network, fragmentPageStoryHandler, true);
            showLeadView();
        } else {//刷新失败
            myToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
            EventBus.getDefault().post(new StoryRefreshEvent(app.fragmentStoryLastSelectedTab, StoryRefreshEvent.STOP_REFRESH));
        }
    }

    /**
     * 保存到数据库成功之后
     */
    private void getDataFinish() {
        if (getPhotoInfoDone) {
            getPhotoInfoDone = false;
            photoPassItemInfoList.clear();
            allItemInfoList.clear();
            boughtItemInfoList.clear();
            getData();
        }
    }

    /**
     * 保存json之后，处理对应的刷新数据
     */
    private void getRefreshDataFinish() {
        if (getPhotoInfoDone) {
            getPhotoInfoDone = false;
            if (refreshDataCount > 0) {
                PictureAirLog.out("getrefreshdata");
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        synchronized (this) {
                            getrefreshdata();
                            fragmentPageStoryHandler.sendEmptyMessage(GET_REFRESH_DATA_DONE);
                        }
                    }
                }.start();

            } else {
                PictureAirLog.out("nomore");
                myToast.setTextAndShow(R.string.nomore, Common.TOAST_SHORT_TIME);
                fragmentPageStoryHandler.sendEmptyMessage(SORT_COMPLETED_REFRESH);
            }
            dismissPWProgressDialog();
            EventBus.getDefault().post(new RedPointControlEvent(false));
        }
    }

    /**
     * 解析服务器返回的数据
     *
     * @param jsonObject json对象
     * @param isAll      布尔值，是否是获取全部数据
     */
    private void saveJsonToSQLite(final JSONObject jsonObject, final boolean isAll) {
        PictureAirLog.out("start save json");
        new Thread() {
            public void run() {
                synchronized (this) {
                    PictureAirLog.out("start save json in thread");
                    if (isAll) {//获取全部数据，需要先清空数据库，反之，插入到后面
                        PictureAirLog.d(TAG, "delete all data from table");
                        pictureAirDbManager.deleteAllInfoFromTable(Common.PHOTOPASS_INFO_TABLE);
                    } else {
                        PictureAirLog.d(TAG, "need not delete all data");
                    }
                    final JSONArray responseArray = jsonObject.getJSONArray("photos");

                    String updatetimeString = jsonObject.getString("time");
                    PictureAirLog.out("updatetime:" + updatetimeString + "new data count = " + responseArray.size());

                    if (isAll || responseArray.size() > 0) {//说明全部获取，需要记录时间；如果刷新的话，有数据的时候，才记录时间，否则不记录时间
                        //需要存储这个时间
                        SPUtils.put(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.LAST_UPDATE_PHOTO_TIME, updatetimeString);
                    }

                    if (isAll) {//如果全部获取，需要清除原有的数据
                        photoPassPicList.clear();
                    }
                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ArrayList<PhotoInfo> resultPhotoList = pictureAirDbManager.insertPhotoInfoIntoPhotoPassInfo(responseArray, isAll);
                    PictureAirLog.out("-----------------> finish insert photo data into database");
                    if (!isAll) {
                        refreshDataCount = resultPhotoList.size();
                        PictureAirLog.d(TAG, "------refresh count ----->" + refreshDataCount);
                    }
                    photoPassPicList.addAll(resultPhotoList);

                    //通知已经处理完毕
                    if (isAll) {
                        fragmentPageStoryHandler.sendEmptyMessage(DEAL_ALL_PHOTO_DATA_DONE);

                    } else {
                        fragmentPageStoryHandler.sendEmptyMessage(DEAL_REFRESH_PHOTO_DATA_DONE);
                    }
                }
            }

            ;
        }.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PictureAirLog.out("on create----->story");
        context = getActivity();
        isOnCreate = true;
        View view = inflater.inflate(R.layout.fragment_story, null);
        titleStrings = new String[] {context.getResources().getString(R.string.story_tab_all),
                context.getResources().getString(R.string.story_tab_photopass),
                context.getResources().getString(R.string.story_tab_magic),
                context.getResources().getString(R.string.story_tab_bought),
                context.getResources().getString(R.string.story_tab_favorite) };
        //获取控件
        scanIv = (ImageView) view.findViewById(R.id.story_menu_iv);
        scanLayout = (RelativeLayout) view.findViewById(R.id.story_menu_rl);
        storyNoPpToScanLinearLayout = (RelativeLayout) view.findViewById(R.id.story_no_pp_to_scan);
        storyLeadBarLinearLayout = (LinearLayout) view.findViewById(R.id.story_lead_bar);
        storyNoPpScanImageView = (ImageView) view.findViewById(R.id.story_no_pp_scan);
        storyViewPager = (ViewPager) view.findViewById(R.id.story_viewPager);
        noNetWorkOrNoCountView = (NoNetWorkOrNoCountView) view.findViewById(R.id.storyNoNetWorkView);
        noPhotoView = (LinearLayout) view.findViewById(R.id.no_photo_view_relativelayout);
        storyNoPhotoToDiscoverImageView = (ImageView) view.findViewById(R.id.story_to_discover);
        buyPPP = (CustomTextView) view.findViewById(R.id.story_buy_ppp);
        activatePPP = (CustomTextView) view.findViewById(R.id.story_activate_ppp);
        storyScan = (CustomTextView) view.findViewById(R.id.story_scan);
        storyTopTip = (CustomTextView) view.findViewById(R.id.story_top_tip);
        storyDiscover = (CustomTextView) view.findViewById(R.id.story_discover);
        storyNoPhotoTip = (CustomTextView) view.findViewById(R.id.story_no_photo_tip);
        specialDealLL = (LinearLayout) view.findViewById(R.id.special_deal_ll);
        specialDealOnTV = (CustomTextView) view.findViewById(R.id.special_deal_on);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.story_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        swipeRefreshLayout.setEnabled(false);

        indicator = (TabPageIndicator) view.findViewById(R.id.indicator);
        indicator.setOnPageChangeListener(this);
        //初始化控件
        PictureAirLog.out("dialog-----> in story");
        showPWProgressDialog();
        pictureAirDbManager = new PictureAirDbManager(getActivity());
        settingUtil = new SettingUtil(pictureAirDbManager);
        app = (MyApplication) context.getApplication();
        PictureAirLog.out("current tap---->" + app.fragmentStoryLastSelectedTab);
        indicator.setmSelectedTabIndex(app.fragmentStoryLastSelectedTab);
        userId = SPUtils.getString(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ID, "");
        photoPassItemInfoList = new ArrayList<>();
        magicItemInfoList = new ArrayList<>();

        allPhotoList = new ArrayList<>();
        pictureAirPhotoList = new ArrayList<>();
        magicPhotoList = new ArrayList<>();
        boughtPhotoList = new ArrayList<>();
        favouritePhotoList = new ArrayList<>();
        storyNoPpScanImageView.setOnClickListener(this);
        scanLayout.setOnClickListener(this);
        storyNoPhotoToDiscoverImageView.setOnClickListener(this);
        buyPPP.setOnClickListener(this);
        activatePPP.setOnClickListener(this);
        specialDealLL.setOnClickListener(this);
        buyPPP.setTypeface(app.getFontBold());
        activatePPP.setTypeface(app.getFontBold());
        storyTopTip.setTypeface(app.getFontBold());
        storyScan.setTypeface(app.getFontBold());
        storyDiscover.setTypeface(app.getFontBold());
        storyNoPhotoTip.setTypeface(app.getFontBold());
        specialDealOnTV.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
        //初始化数据
        scanMagicPhotoNeedCallBack = false;
        myToast = new PWToast(getActivity());
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        screenWidth = ScreenUtil.getScreenWidth(FragmentPageStory.this.getActivity());
        PictureAirLog.d(TAG, "screen width = " + screenWidth);
        //获取sp中的值
        needfresh = SPUtils.getBoolean(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.NEED_FRESH, false);
        //如果不是在story获取推送，需要从application中获取，并且全部刷新
        if (app.getPushPhotoCount() + app.getPushViedoCount() > 0) {
            if (!needfresh) {
                needfresh = true;
            }
        }
        sharedNeedFresh = needfresh;
        if (needfresh) {//如果一开始就需要全部刷新，
            SPUtils.put(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.NEED_FRESH, false);
        }
        //获取API
        isLoading = true;
        //获取地点信息
        if (TextUtils.isEmpty(ACache.get(getActivity()).getAsString(Common.DISCOVER_LOCATION))) {
            API1.getLocationInfo(context, app.getTokenId(), fragmentPageStoryHandler);//获取所有的location
        } else {
            Message message = fragmentPageStoryHandler.obtainMessage();
            message.what = API1.GET_ALL_LOCATION_SUCCESS;
            message.obj = ACache.get(getActivity()).getAsString(Common.DISCOVER_LOCATION);
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
//            more.setVisibility(View.VISIBLE);
            //隐藏没有pp的情况
            storyNoPpToScanLinearLayout.setVisibility(View.GONE);
            //隐藏空图的情况
            noPhotoView.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.GONE);
            //显示有pp的情况
            storyLeadBarLinearLayout.setVisibility(View.VISIBLE);

            fragments.clear();
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
//            more.setVisibility(View.INVISIBLE);
            if (SPUtils.getInt(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.PP_COUNT, 0) < 2) {//没有扫描过
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
        showLeadView();
        isOnCreate = false;
    }

    private void showLeadView(){
        if (TextUtils.isEmpty(SPUtils.getString(MyApplication.getInstance(), Common.SHARED_PREFERENCE_APP, Common.STORY_LEAD_VIEW, null))) {
            EventBus.getDefault().post(new StoryLoadCompletedEvent(true));
        } else {
            EventBus.getDefault().post(new StoryLoadCompletedEvent(false));
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
                if (context != null) {
                    targetMagicPhotoList.addAll(AppUtil.getLocalPhotos(context, Common.PHOTO_SAVE_PATH, Common.ALBUM_MAGIC));
                }
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
        if (files != null) {
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
                        localPhotoInfo.isEncrypted = 0;
                        localPhotoInfo.isRefreshInfo = 0;
                        localPhotoList.add(localPhotoInfo);
                    }
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
        PictureAirLog.out("photo from db ---->" + resultPhotoArrayList.size());
        ppPhotoCount = resultPhotoArrayList.size();
        photoPassPicList.addAll(resultPhotoArrayList);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (hasHidden) {
            PictureAirLog.out("bu ke jian");
            return;
        }
        PictureAirLog.out(TAG + "truely onresume---->story");
        if (mIsAskStoragePermission) {
            mIsAskStoragePermission = false;
            return;
        }
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        if (SPUtils.getBoolean(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.NEED_FRESH, false)) {
            PictureAirLog.out("need refresh");
            app.needScanFavoritePhotos = false;//防止会重复执行，所以此处改为false
            SPUtils.put(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.NEED_FRESH, false);
            showPWProgressDialog();
            API1.getPhotosByConditions(MyApplication.getTokenId(), fragmentPageStoryHandler, null, null);//获取全部图片
            EventBus.getDefault().post(new RedPointControlEvent(false));
        }
        if (!app.scanMagicFinish) {//app内的正常流程
            PictureAirLog.out("need scan local photos");
            showPWProgressDialog();
            requesPermission();
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
                    if (context != null) {
                        favouritePhotoList.addAll(AppUtil.insertSortFavouritePhotos(
                                pictureAirDbManager.getFavoritePhotoInfoListFromDB(context, userId, sdf.format(new Date(cacheTime)), locationList, app.getLanguageType())));
                    }
                    fragmentPageStoryHandler.sendEmptyMessage(DEAL_FAVORITE_DATA_SUCCESS);

                }
            }.start();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        PictureAirLog.out("story-----> detach");
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        context = null;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PictureAirLog.out("story-----> destroy");
        fragmentPageStoryHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        hasHidden = hidden;
        PictureAirLog.out("hidden--->" + hidden);
        if (!hasHidden) {
            if (app.getPushPhotoCount() + app.getPushViedoCount() > 0) {
                PictureAirLog.out("hidden--->开始刷新");
                //刷新操作
                clickToRefresh();
            } else {
                PictureAirLog.out("hidden--->不需要刷新");

            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //不需要super
    }

    /**
     * 数据组合排序
     * 遍历已经存在了的图片资源，分成四大类，all，photopass，magic，bought
     *
     * @throws ParseException
     */
    private void getData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    //遍历所有photopass信息
                    PhotoItemInfo photoItemInfo;
                    boolean clone_contains = false;
                    Date date1;
                    Date date2;
                    //处理网络图片
                    for (int l = 0; l < photoPassPicList.size(); l++) {
                        PhotoInfo info = photoPassPicList.get(l);

                        int resultPosition = AppUtil.findPositionInLocationList(info, locationList);
                        if (resultPosition == -1) {//如果没有找到，说明是其他地点的照片
                            resultPosition = locationList.size() - 1;
                            info.locationId = "others";
                        }
                        if (resultPosition < 0 ) {
                            resultPosition = 0;
                        }

//                                PictureAirLog.d(TAG, "find the location");
                        //如果locationid一样，需要判断是否已经存在此item，如果有，在按照时间分类，没有，新建一个item
                        for (int j = 0; j < photoPassItemInfoList.size(); j++) {
//                                    PictureAirLog.d(TAG, "weather already exists:" + j);
                            if (info.shootTime.equals(photoPassItemInfoList.get(j).shootTime)
                                    && (info.locationId.equals(photoPassItemInfoList.get(j).locationId) ||
                                        photoPassItemInfoList.get(j).locationIds.contains(info.locationId))) {
//                                        PictureAirLog.d(TAG, "photo location id " + info.locationId + "____" + info.shootTime);
//                                        PictureAirLog.d(TAG, "location id:" + locationList.get(i).locationId + "___" + locationList.get(i).locationIds);
//                                        PictureAirLog.d(TAG, "location id:" + photoPassItemInfoList.get(j).locationId + "___" + photoPassItemInfoList.get(j).locationIds);
//                                        PictureAirLog.d(TAG, "already exist");
                                info.locationName = photoPassItemInfoList.get(j).place;
                                photoPassItemInfoList.get(j).list.add(info);
                                date1 = sdf.parse(info.shootOn);
                                date2 = sdf.parse(photoPassItemInfoList.get(j).shootOn);
                                if (date1.after(date2)) {
                                    photoPassItemInfoList.get(j).shootOn = info.shootOn;
                                }
                                clone_contains = true;
                                addToBoughtList(info, resultPosition, photoPassItemInfoList.get(j).locationIds);
                                break;
                            }
                        }
                        if (!clone_contains) {
                            //初始化item的信息
                            PictureAirLog.d(TAG, "not exist");
                            photoItemInfo = new PhotoItemInfo();
                            photoItemInfo.locationId = locationList.get(resultPosition).locationId;
//                            if (isOther) {//把属于other的地点拼接到后面
//                                photoItemInfo.locationIds = locationList.get(resultPosition).locationIds.toString() + info.locationId;
//                            } else {
                                photoItemInfo.locationIds = locationList.get(resultPosition).locationIds.toString();
//                            }
                            photoItemInfo.shootTime = info.shootTime;
                            if (MyApplication.getInstance().getLanguageType().equals(Common.SIMPLE_CHINESE)) {
                                photoItemInfo.place = locationList.get(resultPosition).placeCHName;
                                info.locationName = locationList.get(resultPosition).placeCHName;

                            } else {
                                photoItemInfo.place = locationList.get(resultPosition).placeENName;
                                info.locationName = locationList.get(resultPosition).placeENName;

                            }
                            photoItemInfo.list.add(info);
                            photoItemInfo.placeUrl = locationList.get(resultPosition).placeUrl;
                            photoItemInfo.latitude = locationList.get(resultPosition).latitude;
                            photoItemInfo.longitude = locationList.get(resultPosition).longitude;
                            photoItemInfo.islove = 0;
                            photoItemInfo.shootOn = info.shootOn;
                            photoPassItemInfoList.add(photoItemInfo);
                            addToBoughtList(info, resultPosition, photoItemInfo.locationIds);
                        } else {
                            clone_contains = false;
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                } finally {
                }

                //如果网络图片先处理完，magic相册还没处理完的话，需要等待magic处理完
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
                    if (context != null) {
                        favouritePhotoList.addAll(AppUtil.insertSortFavouritePhotos(
                                pictureAirDbManager.getFavoritePhotoInfoListFromDB(context, userId, sdf.format(new Date(cacheTime)), locationList, app.getLanguageType())));

                    }
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
//            PictureAirLog.d(TAG, "add to bought list");
            for (int j = 0; j < boughtItemInfoList.size(); j++) {
//                PictureAirLog.d(TAG, "检查之前的是否存在");

                if (info.shootTime.equals(boughtItemInfoList.get(j).shootTime) &&
                        (info.locationId.equals(boughtItemInfoList.get(j).locationId) || boughtItemInfoList.get(j).locationIds.contains(info.locationId))) {
//                    PictureAirLog.d(TAG, "已经存在于bought列表");
//                    info.locationName = boughtItemInfoList.get(j).place;
                    boughtItemInfoList.get(j).list.add(info);
                    isContains = true;
                    break;
                }
            }
            if (!isContains) {//没有
//                PictureAirLog.d(TAG, "不存在于之前的已购买的列表");
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
            PictureAirLog.d(TAG, "add to bought list" + info.locationId);
            for (int j = 0; j < boughtItemInfoList.size(); j++) {
                PictureAirLog.d(TAG, "检查之前的是否存在");

                if (info.shootTime.equals(boughtItemInfoList.get(j).shootTime) &&
                        (info.locationId.equals(boughtItemInfoList.get(j).locationId) || boughtItemInfoList.get(j).locationIds.contains(info.locationId))) {
                    PictureAirLog.d(TAG, "已经存在于bought列表");

                    //比较时间，按照时间排序
                    for (int i = 0; i < boughtItemInfoList.get(j).list.size(); i++) {
                        if (info.isRefreshInfo == 1) {//如果需要刷新旧数据，需要遍历比对photoid，并且替换对应信息
                            if (info.photoId.equals(boughtItemInfoList.get(j).list.get(i).photoId)) {
                                boughtItemInfoList.get(j).list.get(i).photoPathOrURL = info.photoPathOrURL;
                                boughtItemInfoList.get(j).list.get(i).photoThumbnail = info.photoThumbnail;
                                boughtItemInfoList.get(j).list.get(i).photoThumbnail_512 = info.photoThumbnail_512;
                                boughtItemInfoList.get(j).list.get(i).photoThumbnail_1024 = info.photoThumbnail_1024;
                                break;
                            }
                        } else {
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
                                    }
                                }
                            } catch (ParseException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }//获取列表中的时间
                        }
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
                photoItemInfo.place = placeName;
                photoItemInfo.list.add(info);
                photoItemInfo.placeUrl = placeUrl;
                photoItemInfo.latitude = latitude;
                photoItemInfo.longitude = longitude;
                photoItemInfo.islove = 0;
                photoItemInfo.shootOn = info.shootOn;
                boughtItemInfoList.add(photoItemInfo);
            }
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
        if (context != null) {
            magicItemInfoList.addAll(AppUtil.getMagicItemInfoList(context, sdf, targetMagicPhotoList));
        }
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
        PictureAirLog.out("refreshdatacount---->" + refreshDataCount);
        PhotoItemInfo itemInfo;
        boolean findLocation = false;
        //先清除之前旧的列表
        allItemInfoList.removeAll(photoPassItemInfoList);

        //将图片按照location加载到list中去
        for (int l = photoPassPicList.size() - refreshDataCount; l < photoPassPicList.size() && l >= 0; l++) {//遍历所要添加的图片list
            PictureAirLog.out("遍历照片");
            PhotoInfo info = photoPassPicList.get(l);

            //先检查locationid，是否数据其他地点的照片
            int resultPosition = AppUtil.findPositionInLocationList(info, locationList);
            if (resultPosition == -1) {//如果没有找到，说明是其他地点的照片
                resultPosition = locationList.size() - 1;
                info.locationId = "others";
            }

            if (resultPosition < 0 ) {
                resultPosition = 0;
            }

            //查找list_clone有图片的item，如果找到locationid，在判断是否有同一天的photos，如果有同一天的，add进去，如果没有，新建一个项
            for (int j = 0; j < photoPassItemInfoList.size(); j++) {//遍历list，查找locationid一样的内容
                PictureAirLog.out("遍历地址");
                PhotoItemInfo p = photoPassItemInfoList.get(j);
                if (p.locationId == null) {
                    p.locationId = "others";
                }

                if (info.isRefreshInfo == 1) {//需要更新lisa中的数据
                    findLocation = true;
                    if ((info.locationId.equals(p.locationId) || p.locationIds.contains(info.locationId)) && info.shootTime.equals(p.shootTime)) {//地点和日期一致，比对photoid，替换url
                        for (int i = 0; i < p.list.size(); i++) {
                            if (p.list.get(i).photoId.equals(info.photoId)) {
                                p.list.get(i).photoThumbnail_1024 = info.photoThumbnail_1024;
                                p.list.get(i).photoThumbnail_512 = info.photoThumbnail_512;
                                p.list.get(i).photoThumbnail = info.photoThumbnail;
                                p.list.get(i).photoPathOrURL = info.photoPathOrURL;
                                addRefreshDataToBoughtList(info, p.locationIds, p.place, p.placeUrl, p.latitude, p.longitude);
                                break;
                            }
                        }
                        break;
                    }
                } else {
                    if (info.locationId.equals(p.locationId) || p.locationIds.contains(info.locationId)) {//如果locationId和photo的locationid一样
                        PictureAirLog.out("location一样");
                        findLocation = true;
                        if (info.shootTime.equals(p.shootTime)) {//如果shoottime一致，则插入到列表中
                            PictureAirLog.out("shootTime一致，直接插入列表");
                            //比较时间，按照时间排序
                            for (int i = 0; i < p.list.size(); i++) {
                                try {
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
                        break;
                    }
                }
            }
            if (findLocation) {//如果之前已经找到了对应的位置
                PictureAirLog.out("找到位置");
                findLocation = false;
            } else {//如果之前没有找到对应的位置，遍历地址列表，需要新建一个item，并且放入到最上方
                PictureAirLog.out("找到其他的location");
                itemInfo = new PhotoItemInfo();
                itemInfo.locationId = locationList.get(resultPosition).locationId;
                itemInfo.locationIds = locationList.get(resultPosition).locationIds.toString();
                itemInfo.shootTime = info.shootTime;
                if (MyApplication.getInstance().getLanguageType().equals(Common.SIMPLE_CHINESE)) {
                    itemInfo.place = locationList.get(resultPosition).placeCHName;
                    info.locationName = locationList.get(resultPosition).placeCHName;

                } else {
                    itemInfo.place = locationList.get(resultPosition).placeENName;
                    info.locationName = locationList.get(resultPosition).placeENName;

                }
                itemInfo.list.add(info);
                itemInfo.placeUrl = locationList.get(resultPosition).placeUrl;
                itemInfo.latitude = locationList.get(resultPosition).latitude;
                itemInfo.longitude = locationList.get(resultPosition).longitude;
                itemInfo.islove = 0;
                itemInfo.shootOn = info.shootOn;
                photoPassItemInfoList.add(0, itemInfo);
                addRefreshDataToBoughtList(info, itemInfo.locationIds, itemInfo.place, itemInfo.placeUrl, itemInfo.latitude, itemInfo.longitude);
            }
        }
        refreshDataCount = 0;

        allItemInfoList.addAll(photoPassItemInfoList);
        Collections.sort(allItemInfoList);//对all进行排序
        Collections.sort(photoPassItemInfoList);
    }


    @SuppressLint("NewApi")
    @Override
    public void onClick(View v) {
        Intent i = null;
        switch (v.getId()) {
            //扫描按钮
            case R.id.story_menu_rl:
                if (pppPop == null ) {
                    pppPop = new PPPPop(context, fragmentPageStoryHandler, PPPPop.MENU_TYPE_STORY);
                }

                int[] location = new int[2];
                scanIv.getLocationOnScreen(location);
                pppPop.showAsDropDown(scanIv, 0, ScreenUtil.dip2px(context, 15) - 10);
                break;

            case R.id.story_activate_ppp:
                i = new Intent(context, MipCaptureActivity.class);
                i.putExtra("mode", "ocr");//默认ocr扫描
                startActivity(i);
                break;

            case R.id.story_no_pp_scan:
                i = new Intent(context, MipCaptureActivity.class);
                startActivity(i);
                break;

            case R.id.story_to_discover://跳转到Discover页面
                PictureAirLog.out("Onclick---->Discover");
                EventBus.getDefault().post(new MainTabSwitchEvent(MainTabSwitchEvent.DISCOVER_TAB));
                break;

            case R.id.story_buy_ppp:
                //购买PP+，先获取商品 然后进入商品详情
                showPWProgressDialog();
                //获取商品（以后从缓存中取）
                getGoods();
                break;

            case R.id.special_deal_ll:
                //抢单点击事件，即可进入抢单活动页面
                PictureAirLog.d("deal url---> " + dealingInfo.getDealingUrl() + "tokenid-->" + MyApplication.getTokenId());
                Intent intent = new Intent(context, PanicBuyActivity.class);
                intent.putExtra("dealingInfo", dealingInfo);
                startActivity(intent);
                break;

            default:
                break;
        }

    }

    private void clickToRefresh() {
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

    /**
     * 检测story按钮的点击事件，点击了执行刷新操作
     *
     * @param baseBusEvent
     */
    @Subscribe
    public void onUserEvent(BaseBusEvent baseBusEvent) {
        if (baseBusEvent instanceof MainTabOnClickEvent) {
            MainTabOnClickEvent storyRefreshOnClickEvent = (MainTabOnClickEvent) baseBusEvent;
            if (storyRefreshOnClickEvent.isStoryTabClick()) {//通知页面开始刷新
                clickToRefresh();
            }
            if (storyRefreshOnClickEvent.isShowSpecialDealBar()) {//显示顶部抢购栏
                dealingInfo = storyRefreshOnClickEvent.getDealingInfo();
                specialDealLL.setVisibility(View.VISIBLE);
                if (storyRefreshOnClickEvent.isSpecialDealBuyClick()) {//如果是点了购买按钮，需要执行购买流程
                    specialDealLL.performClick();
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

    private void requesPermission() {
        if (!AppUtil.checkPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            mIsAskStoragePermission = true;
            ActivityCompat.requestPermissions(context,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
            return;
        }
        scanPhotosThread = new ScanPhotosThread(scanMagicPhotoNeedCallBack);
        scanPhotosThread.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION:
                scanPhotosThread = new ScanPhotosThread(scanMagicPhotoNeedCallBack);
                scanPhotosThread.start();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * 初始化数据
     */
    private void getGoods() {
        //从缓层中获取数据
        String goodsByACache = ACache.get(getActivity()).getAsString(Common.ALL_GOODS);
        if (goodsByACache != null && !goodsByACache.equals("")) {
            fragmentPageStoryHandler.obtainMessage(API1.GET_GOODS_SUCCESS, goodsByACache).sendToTarget();
        } else {
            //从网络获取商品,先检查网络
            if (AppUtil.getNetWorkType(MyApplication.getInstance()) != 0) {
                API1.getGoods(fragmentPageStoryHandler);
            } else {
                myToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
            }
        }
    }
}
