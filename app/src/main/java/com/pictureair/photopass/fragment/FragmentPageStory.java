package com.pictureair.photopass.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.BaseFragment;
import com.pictureair.photopass.activity.MainTabActivity;
import com.pictureair.photopass.activity.MipCaptureActivity;
import com.pictureair.photopass.activity.MyPPPActivity;
import com.pictureair.photopass.activity.PPPDetailProductActivity;
import com.pictureair.photopass.adapter.FragmentAdapter;
import com.pictureair.photopass.customDialog.CustomDialog;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.DiscoverLocationItemInfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.entity.PhotoItemInfo;
import com.pictureair.photopass.entity.StoryFragmentEvent;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.DisneyVideoTool;
import com.pictureair.photopass.util.JsonUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.util.SettingUtil;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.MyToast;
import com.pictureair.photopass.widget.NoNetWorkOrNoCountView;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * PhotoPass照片的图片墙，用来显示从服务器返回的照片信息，以及通过magic相机拍摄的图片
 * 可以左右滑动切换不同的相册
 * 可以下拉刷新，获取更多的图片信息
 */
public class FragmentPageStory extends BaseFragment implements OnClickListener {
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

    private static String TAG = "FragmentPageStory";

    //申明变量
    private int refreshDataCount = 0;//记录刷新数据的数量
    private int refreshVideoDataCount = 0;//记录刷新
    private boolean needfresh = false;
    private int screenWidth;
    private boolean isLoading = false;
    private boolean scanMagicPhotoNeedCallBack;//记录是否需要重新扫描本地照片
    private int ppPhotoCount;

    //申明控件
    private ImageView more;
    private ImageView scanLayout;
    private LinearLayout noPhotoView;
    private RelativeLayout scanRelativeLayout;
    private static CustomProgressDialog dialog;// 加载等待
    private ImageView cursorImageView;
    private TextView storyTabAllTextView, storyTabPhotopassTextView, storyTabMagicTextView, storyTabBoughtTextView, storyTabFavoriteTextView;
    private static ViewPager storyViewPager;
    private LinearLayout storyNoPpToScanLinearLayout, storyLeadBarLinearLayout, storyCursorLinearLayout;
    private ImageView storyNoPpScanImageView;
    private NoNetWorkOrNoCountView noNetWorkOrNoCountView;
    private CustomDialog customdialog; //  对话框

    //申明类
    private MyApplication app;
    private ArrayList<PhotoItemInfo> photoPassPictureList;
    private ArrayList<PhotoItemInfo> magicPicList;
    private ArrayList<PhotoItemInfo> favouritePictureList;

    private ArrayList<PhotoInfo> allPhotoList, pictureAirPhotoList, magicPhotoList, boughtPhotoList, favouritePhotoList;
    private ArrayList<DiscoverLocationItemInfo> locationList = new ArrayList<DiscoverLocationItemInfo>();
    private List<Fragment> fragments;
    private FragmentAdapter fragmentAdapter;
    public static Context context;
    private SimpleDateFormat sdf;
    private static SharedPreferences sharedPreferences;
    private MyToast myToast;
    private PhotoInfo selectPhotoItemInfo;
    private ScanPhotosThread scanPhotosThread;
    private PictureAirDbManager pictureAirDbManager;
    private boolean getPhotoInfoDone = false;
    private boolean getVideoInfoDone = false;

    private SettingUtil settingUtil;
    //申明handler消息回调机制
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
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
                    PictureAirLog.d(TAG, "---------->get location success" + msg.obj.toString());
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
                    if (app.photoPassPicList == null) {
                        app.photoPassPicList = new ArrayList<>();
                    }
                    app.photoPassPicList.clear();
                    if (app.photoPassVideoList == null) {
                        app.photoPassVideoList = new ArrayList<>();
                    }
                    app.photoPassVideoList.clear();

                    if (!needfresh) {//如果需要刷新数据的话，就不需要从数据库中获取数据
                        PictureAirLog.d(TAG, "---------> load data from databases");
                        loadDataFromDataBase();

                        //  如果PP中的照片大于 10 张，并且账户中没有PP＋。就提示购买PP+
                        if (settingUtil.isFirstPP10(sharedPreferences.getString(Common.USERINFO_ID, ""))) {
                            //第一次 PP数量到 10 。
                            API1.getPPPSByUserId(sharedPreferences.getString(Common.USERINFO_TOKENID, null), handler);
                        }
                    }
                    if (app.photoPassPicList.size() == 0 || needfresh) {
                        //数据为0，需要从网上下载
                        System.out.println("photolist size = 0");
                        //判断是否之前有成功获取过
                        API1.getPhotosByConditions(sharedPreferences.getString(Common.USERINFO_TOKENID, null), handler, null);//获取全部图片
                        API1.getVideoList(null, handler);//获取全部视频信息
                    } else {
                        PictureAirLog.out("photolist size = " + app.photoPassPicList.size());
                        //有数据，直接显示
                        photoPassPictureList.clear();
                        app.allPicList.clear();
                        app.boughtPicList.clear();
                        favouritePictureList.clear();
                        try {
                            getData();
                        } catch (ParseException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    break;

                case API1.GET_REFRESH_PHOTOS_BY_CONDITIONS_FAILED://获取刷新失败
                    getPhotoInfoDone = true;
                    if (getPhotoInfoDone && getVideoInfoDone) {
                        finishLoad(false);
                    }
                    break;

                case API1.GET_REFRESH_VIDEO_LIST_FAILED://获取刷新失败
                    getVideoInfoDone = true;
                    if (getPhotoInfoDone && getVideoInfoDone) {
                        finishLoad(false);
                    }
                    break;

                case API1.GET_ALL_PHOTOS_BY_CONDITIONS_SUCCESS://获取照片成功

                    PictureAirLog.d(TAG, "--------->get photo success");
                    saveJsonToSQLite(JSONObject.parseObject(msg.obj.toString()), true);
                    break;

                case API1.GET_ALL_VIDEO_LIST_SUCCESS://获取视频成功

                    PictureAirLog.d(TAG, "--->get video success");
                    saveVideoJsonToSQLite(JSONObject.parseObject(msg.obj.toString()), true);
                    break;

                case API1.GET_REFRESH_PHOTOS_BY_CONDITIONS_SUCCESS://获取刷新的推送图片
//                    app.setPushPhotoCount(0);
                    PictureAirLog.d(TAG, "deal refresh photos-------");
                    saveJsonToSQLite(JSONObject.parseObject(msg.obj.toString()), false);
                    break;

                case API1.GET_REFRESH_VIDEO_LIST_SUCCESS://获取刷新的视频成功
                    PictureAirLog.d(TAG, "--->get refresh video success");
                    saveVideoJsonToSQLite(JSONObject.parseObject(msg.obj.toString()), false);
                    break;

                case REFRESH_LOCAL_PHOTOS://刷新处理本地照片
                    PictureAirLog.d(TAG, "scan local photos success");
                    dealLocalRefreshedData();
                    break;

                case REFRESH://开始刷新
                    PictureAirLog.d(TAG, "the index of refreshing is " + msg.arg1);
                    API1.getPhotosByConditions(sharedPreferences.getString(Common.USERINFO_TOKENID, null), handler, sharedPreferences.getString(Common.LAST_UPDATE_PHOTO_TIME, null));//获取更新信息
                    API1.getVideoList(sharedPreferences.getString(Common.LAST_UPDATE_VIDEO_TIME, null), handler);//获取全部视频信息
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

                case SORT_COMPLETED_REFRESH:
                    EventBus.getDefault().post(new StoryFragmentEvent(allPhotoList, app.magicPicList, 0));
                    EventBus.getDefault().post(new StoryFragmentEvent(pictureAirPhotoList, app.magicPicList, 1));
                    EventBus.getDefault().post(new StoryFragmentEvent(magicPhotoList, app.magicPicList, 2));
                    EventBus.getDefault().post(new StoryFragmentEvent(boughtPhotoList, app.magicPicList, 3));
                    EventBus.getDefault().post(new StoryFragmentEvent(favouritePhotoList, app.magicPicList, 4));
                    break;

                case LOAD_COMPLETED:
                    sortData(true);
                    break;

                case SORT_COMPLETED_ALL:
                    scanMagicPhotoNeedCallBack = true;
                    fragments = new ArrayList<>();
                    fragments.clear();

                    showViewPager();
                    noNetWorkOrNoCountView.setVisibility(View.GONE);//无网络状态的View设置为不可见
                    Animation animation = new TranslateAnimation(0, screenWidth / 5 * app.fragmentStoryLastSelectedTab, 0, 0);
                    animation.setFillAfter(true);
                    animation.setDuration(300);
                    cursorImageView.startAnimation(animation);
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    break;

                case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_RELOAD://noView的按钮响应重新加载点击事件
                    //重新加载购物车数据
                    System.out.println("onclick with reload");
                    dialog = CustomProgressDialog.show(getActivity(), getString(R.string.is_loading), false, null);
                    if (ACache.get(getActivity()).getAsString(Common.LOCATION_INFO) == null) {//地址获取失败
                        API1.getLocationInfo(getActivity(), MyApplication.getTokenId(), handler);//获取所有的location
                    } else {//地址获取成功，但是照片获取失败
                        Message message = handler.obtainMessage();
                        message.what = API1.GET_ALL_LOCATION_SUCCESS;
                        message.obj = ACache.get(getActivity()).getAsString(Common.LOCATION_INFO);
                        handler.sendMessage(message);
                    }
                    break;

                case API1.GET_PPP_SUCCESS:
                    if (ppPhotoCount >=10 && API1.PPPlist.size() == 0) {
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
                    }else if( API1.PPPlist.size() >0){
                        settingUtil.insertSettingFirstPP10Status(sharedPreferences.getString(Common.USERINFO_ID, ""));
                    }
                    break;

                default:
                    break;
            }
        }

        ;
    };

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

        }
        noNetWorkOrNoCountView.setResult(R.string.no_network, R.string.click_button_reload, R.string.reload, R.drawable.no_network, handler, true);
    }

    /**
     * 保存到数据库成功之后
     */
    private void getDataFinish() {
        if (getPhotoInfoDone && getVideoInfoDone) {
            getPhotoInfoDone = false;
            getVideoInfoDone = false;
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
    }

    /**
     * 保存json之后，处理对应的刷新数据
     */
    private void getRefreshDataFinish() {
        if (getPhotoInfoDone && getVideoInfoDone) {
            getPhotoInfoDone = false;
            getVideoInfoDone = false;
            if (refreshDataCount > 0 || refreshVideoDataCount > 0) {
                System.out.println("getrefreshdata");
                refreshDataCount = 0;
                refreshVideoDataCount = 0;
                getrefreshdata();

            } else {
                System.out.println("nomore");
                myToast.setTextAndShow(R.string.nomore, Common.TOAST_SHORT_TIME);
            }
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (MainTabActivity.maintabbadgeView.isShown()) {
                MainTabActivity.maintabbadgeView.hide();
            }
            sortData(false);
        }
    }

    /**
     * 解析服务器返回的数据
     *
     * @param jsonObject json对象
     * @param isAll      布尔值，是否是获取全部数据
     */
    private void saveJsonToSQLite(JSONObject jsonObject, final boolean isAll) {

        if (isAll) {//获取全部数据，需要先清空数据库，反之，插入到后面
            PictureAirLog.d(TAG, "delete all data from table");
            pictureAirDbManager.deleteAllInfoFromTable(Common.PHOTOPASS_INFO_TABLE, false);
        } else {
            PictureAirLog.d(TAG, "need not delete all data");
        }

        try {
            final JSONArray responseArray = jsonObject.getJSONArray("photos");

            String updatetimeString = jsonObject.getString("time");
            System.out.println("updatetime:" + updatetimeString + "new data count = " + responseArray.size());

            if (isAll || responseArray.size() > 0) {//说明全部获取，需要记录时间；如果刷新的话，有数据的时候，才记录时间，否则不记录时间
                //需要存储这个时间
                Editor editor = sharedPreferences.edit();
                editor.putString(Common.LAST_UPDATE_PHOTO_TIME, updatetimeString);
                editor.commit();
            }

            if (isAll) {//如果全部获取，需要清除原有的数据
                app.photoPassPicList.clear();
            } else {//刷新最新照片，获取刷新数据的数量
                refreshDataCount = responseArray.size();
                PictureAirLog.d(TAG, "------refresh count ----->" + refreshDataCount);
            }

            new Thread() {
                public void run() {
                    System.out.println("-----------------> start insert data into database");
                    app.photoPassPicList.addAll(pictureAirDbManager.insertPhotoInfoIntoPhotoPassInfo(responseArray));
                    //通知已经处理完毕
                    if (isAll) {
                        handler.sendEmptyMessage(DEAL_ALL_PHOTO_DATA_DONE);

                    } else {
                        handler.sendEmptyMessage(DEAL_REFRESH_PHOTO_DATA_DONE);
                    }

                }

                ;
            }.start();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 解析服务器返回的数据
     *
     * @param jsonObject json对象
     * @param isAll      布尔值，是否是获取全部数据
     */
    private void saveVideoJsonToSQLite(JSONObject jsonObject, final boolean isAll) {

        if (isAll) {//获取全部数据，需要先清空数据库，反之，插入到后面
            PictureAirLog.d(TAG, "delete all video data from table");
            pictureAirDbManager.deleteAllInfoFromTable(Common.PHOTOPASS_INFO_TABLE, true);
        } else {
            PictureAirLog.d(TAG, "need not delete all video data");
        }

        try {
            final JSONArray responseArray = jsonObject.getJSONArray("videoList");

            String updatetimeString = jsonObject.getString("t");
            System.out.println("updatetime:" + updatetimeString + "new data count = " + responseArray.size());

            if (isAll || responseArray.size() > 0) {//说明全部获取，需要记录时间；如果刷新的话，有数据的时候，才记录时间，否则不记录时间
                //需要存储这个时间
                Editor editor = sharedPreferences.edit();
                editor.putString(Common.LAST_UPDATE_VIDEO_TIME, updatetimeString);
                editor.commit();
            }

            if (isAll) {//如果全部获取，需要清除原有的数据
                app.photoPassVideoList.clear();
            } else {//刷新最新照片，获取刷新数据的数量
                refreshVideoDataCount = responseArray.size();
                PictureAirLog.d(TAG, "------refresh count ----->" + refreshVideoDataCount);
            }

            new Thread() {
                public void run() {
                    System.out.println("-----------------> start insert data into database");
                    app.photoPassVideoList.addAll(pictureAirDbManager.insertVideoInfoIntoPhotoPassInfo(responseArray));
                    //通知已经处理完毕
                    if (isAll) {
                        handler.sendEmptyMessage(DEAL_ALL_VIDEO_DATA_DONE);

                    } else {
                        handler.sendEmptyMessage(DEAL_REFRESH_VIDEO_DATA_DONE);
                    }

                }

                ;
            }.start();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_story, null);

        //获取控件
        more = (ImageView) view.findViewById(R.id.story_more);
        scanRelativeLayout = (RelativeLayout) view.findViewById(R.id.storyScanRelativeLayout);
        scanLayout = (ImageView) view.findViewById(R.id.story_scan);
        storyNoPpToScanLinearLayout = (LinearLayout) view.findViewById(R.id.story_no_pp_to_scan);
        storyLeadBarLinearLayout = (LinearLayout) view.findViewById(R.id.story_lead_bar);
        storyCursorLinearLayout = (LinearLayout) view.findViewById(R.id.story_cursor_layout);
        storyNoPpScanImageView = (ImageView) view.findViewById(R.id.story_no_pp_scan);
        storyTabAllTextView = (TextView) view.findViewById(R.id.story_tab_all);
        storyTabPhotopassTextView = (TextView) view.findViewById(R.id.story_tab_photopass);
        storyTabMagicTextView = (TextView) view.findViewById(R.id.story_tab_magic);
        storyTabBoughtTextView = (TextView) view.findViewById(R.id.story_tab_bought);
        storyTabFavoriteTextView = (TextView) view.findViewById(R.id.story_tab_favourite);
        cursorImageView = (ImageView) view.findViewById(R.id.story_cursor);
        storyViewPager = (ViewPager) view.findViewById(R.id.story_viewPager);
        noNetWorkOrNoCountView = (NoNetWorkOrNoCountView) view.findViewById(R.id.storyNoNetWorkView);
        noPhotoView = (LinearLayout) view.findViewById(R.id.no_photo_view_relativelayout);

        //初始化控件
        context = getActivity();
        settingUtil = new SettingUtil(context);
        app = (MyApplication) getActivity().getApplication();
        pictureAirDbManager = new PictureAirDbManager(getActivity());
        sharedPreferences = getActivity().getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
        photoPassPictureList = new ArrayList<>();
        favouritePictureList = new ArrayList<>();
        magicPicList = new ArrayList<>();

        allPhotoList = new ArrayList<>();
        pictureAirPhotoList = new ArrayList<>();
        magicPhotoList = new ArrayList<>();
        boughtPhotoList = new ArrayList<>();
        favouritePhotoList = new ArrayList<>();
        //绑定监听
        storyTabAllTextView.setOnClickListener(new viewPagerOnClickListener(0));
        storyTabPhotopassTextView.setOnClickListener(new viewPagerOnClickListener(1));
        storyTabMagicTextView.setOnClickListener(new viewPagerOnClickListener(2));
        storyTabBoughtTextView.setOnClickListener(new viewPagerOnClickListener(3));
        storyTabFavoriteTextView.setOnClickListener(new viewPagerOnClickListener(4));
        storyNoPpScanImageView.setOnClickListener(this);


        scanLayout.setOnClickListener(this);
        scanRelativeLayout.setOnClickListener(this);
        more.setOnClickListener(this);

        //初始化数据
        scanMagicPhotoNeedCallBack = false;
        myToast = new MyToast(getActivity());
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        locationList.clear();
        screenWidth = ScreenUtil.getScreenWidth(FragmentPageStory.this.getActivity());
        PictureAirLog.d(TAG, "screen width = " + screenWidth);
        needfresh = sharedPreferences.getBoolean(Common.NEED_FRESH, false);
        if (needfresh) {//如果一开始就需要全部刷新，
            Editor editor = sharedPreferences.edit();
            editor.putBoolean(Common.NEED_FRESH, false);
            editor.commit();
        }
        //获取API
        dialog = CustomProgressDialog.show(getActivity(), getString(R.string.is_loading), false, null);
        isLoading = true;
        //获取地点信息
        if (ACache.get(getActivity()).getAsString(Common.LOCATION_INFO) == null) {
            API1.getLocationInfo(getActivity(), app.getTokenId(), handler);//获取所有的location
        } else {
            Message message = handler.obtainMessage();
            message.what = API1.GET_ALL_LOCATION_SUCCESS;
            message.obj = ACache.get(getActivity()).getAsString(Common.LOCATION_INFO);
            handler.sendMessage(message);
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
                favouritePhotoList.clear();


                allPhotoList.addAll(AppUtil.startSortForPinnedListView(app.allPicList));
                pictureAirPhotoList.addAll(AppUtil.startSortForPinnedListView(photoPassPictureList));
                magicPhotoList.addAll(AppUtil.startSortForPinnedListView(magicPicList));
                boughtPhotoList.addAll(AppUtil.startSortForPinnedListView(app.boughtPicList));
                favouritePhotoList.addAll(AppUtil.startSortForPinnedListView(favouritePictureList));

                if (isAll) {
                    handler.sendEmptyMessage(SORT_COMPLETED_ALL);
                } else {
                    handler.sendEmptyMessage(SORT_COMPLETED_REFRESH);
                }
            }
        }.start();

    }


    /**
     * 控制控件的隐藏或者显示
     */
    private void showViewPager() {

        //判断是否应该显示左上角红点

        if (sharedPreferences.getInt(Common.PP_COUNT, 0) < 2) {//没有扫描过
            PictureAirLog.out("viewpager---->has not scan pp");
            //显示没有pp的情况
            storyNoPpToScanLinearLayout.setVisibility(View.VISIBLE);

            noPhotoView.setVisibility(View.GONE);

            //需要设置为不可见，不然会报空指针异常
            storyLeadBarLinearLayout.setVisibility(View.INVISIBLE);
            storyCursorLinearLayout.setVisibility(View.INVISIBLE);
            storyViewPager.setVisibility(View.INVISIBLE);
        } else {//有扫描过

            if (app.allPicList != null && app.allPicList.size() > 0) {//有图片
                PictureAirLog.out("viewpager---->has photos");
                //隐藏没有pp的情况
                storyNoPpToScanLinearLayout.setVisibility(View.GONE);
                //隐藏空图的情况
                noPhotoView.setVisibility(View.GONE);
                //显示有pp的情况
                storyLeadBarLinearLayout.setVisibility(View.VISIBLE);
                storyCursorLinearLayout.setVisibility(View.VISIBLE);
                storyViewPager.setVisibility(View.VISIBLE);

                fragments.add(StoryFragment.getInstance(allPhotoList, app.magicPicList, 0, handler));
                fragments.add(StoryFragment.getInstance(pictureAirPhotoList, app.magicPicList, 1, handler));
                fragments.add(StoryFragment.getInstance(magicPhotoList, app.magicPicList, 2, handler));
                fragments.add(StoryFragment.getInstance(boughtPhotoList, app.magicPicList, 3, handler));
                fragments.add(StoryFragment.getInstance(favouritePhotoList, app.magicPicList, 4, handler));
                fragmentAdapter = new FragmentAdapter(getChildFragmentManager(), fragments);
                storyViewPager.setAdapter(fragmentAdapter);
                storyViewPager.setOffscreenPageLimit(2);
                storyViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
                storyViewPager.setCurrentItem(app.fragmentStoryLastSelectedTab);
                setTitleBarTextColor(app.fragmentStoryLastSelectedTab);
                EventBus.getDefault().post(new StoryFragmentEvent(allPhotoList, app.magicPicList, 0));
                EventBus.getDefault().post(new StoryFragmentEvent(pictureAirPhotoList, app.magicPicList, 1));
                EventBus.getDefault().post(new StoryFragmentEvent(magicPhotoList, app.magicPicList, 2));
                EventBus.getDefault().post(new StoryFragmentEvent(boughtPhotoList, app.magicPicList, 3));
                EventBus.getDefault().post(new StoryFragmentEvent(boughtPhotoList, app.magicPicList, 4));


            } else {//没有图片
                PictureAirLog.out("viewpager---->no photos");
                storyNoPpToScanLinearLayout.setVisibility(View.GONE);

                //显示空图的情况
                noPhotoView.setVisibility(View.VISIBLE);

                //需要设置为不可见，不然会报空指针异常
                storyLeadBarLinearLayout.setVisibility(View.INVISIBLE);
                storyCursorLinearLayout.setVisibility(View.INVISIBLE);
                storyViewPager.setVisibility(View.INVISIBLE);
            }
        }
    }

    //选项卡点击事件监听
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
            Animation animation = new TranslateAnimation(screenWidth / 5 * app.fragmentStoryLastSelectedTab, screenWidth / 5 * arg0, 0, 0);
            app.fragmentStoryLastSelectedTab = arg0;
            setTitleBarTextColor(arg0);
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

    private void setTitleBarTextColor(int index) {
        switch (index) {
            case 0:
                storyTabAllTextView.setTextColor(getActivity().getResources().getColor(R.color.pp_blue));
                storyTabPhotopassTextView.setTextColor(getActivity().getResources().getColor(R.color.pp_dark_blue));
                storyTabMagicTextView.setTextColor(getActivity().getResources().getColor(R.color.pp_dark_blue));
                storyTabBoughtTextView.setTextColor(getActivity().getResources().getColor(R.color.pp_dark_blue));
                storyTabFavoriteTextView.setTextColor(getActivity().getResources().getColor(R.color.pp_dark_blue));
                break;

            case 1:
                storyTabAllTextView.setTextColor(getActivity().getResources().getColor(R.color.pp_dark_blue));
                storyTabPhotopassTextView.setTextColor(getActivity().getResources().getColor(R.color.pp_blue));
                storyTabMagicTextView.setTextColor(getActivity().getResources().getColor(R.color.pp_dark_blue));
                storyTabBoughtTextView.setTextColor(getActivity().getResources().getColor(R.color.pp_dark_blue));
                storyTabFavoriteTextView.setTextColor(getActivity().getResources().getColor(R.color.pp_dark_blue));
                break;

            case 2:
                storyTabAllTextView.setTextColor(getActivity().getResources().getColor(R.color.pp_dark_blue));
                storyTabPhotopassTextView.setTextColor(getActivity().getResources().getColor(R.color.pp_dark_blue));
                storyTabMagicTextView.setTextColor(getActivity().getResources().getColor(R.color.pp_blue));
                storyTabBoughtTextView.setTextColor(getActivity().getResources().getColor(R.color.pp_dark_blue));
                storyTabFavoriteTextView.setTextColor(getActivity().getResources().getColor(R.color.pp_dark_blue));
                break;

            case 3:
                storyTabAllTextView.setTextColor(getActivity().getResources().getColor(R.color.pp_dark_blue));
                storyTabPhotopassTextView.setTextColor(getActivity().getResources().getColor(R.color.pp_dark_blue));
                storyTabMagicTextView.setTextColor(getActivity().getResources().getColor(R.color.pp_dark_blue));
                storyTabBoughtTextView.setTextColor(getActivity().getResources().getColor(R.color.pp_blue));
                storyTabFavoriteTextView.setTextColor(getActivity().getResources().getColor(R.color.pp_dark_blue));
                break;

            case 4:
                storyTabAllTextView.setTextColor(getActivity().getResources().getColor(R.color.pp_dark_blue));
                storyTabPhotopassTextView.setTextColor(getActivity().getResources().getColor(R.color.pp_dark_blue));
                storyTabMagicTextView.setTextColor(getActivity().getResources().getColor(R.color.pp_dark_blue));
                storyTabBoughtTextView.setTextColor(getActivity().getResources().getColor(R.color.pp_dark_blue));
                storyTabFavoriteTextView.setTextColor(getActivity().getResources().getColor(R.color.pp_blue));
                break;
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
            System.out.println("------->run");
            if (!app.scanMagicFinish) {
                ScanPhotos(Common.PHOTO_SAVE_PATH, Common.ALBUM_MAGIC, needCallBck);
                Collections.sort(app.magicPicList);
                app.scanMagicFinish = true;
            }
            if (needCallBck) {//是刷新数据操作，需要通知adatper更新数据
                handler.sendEmptyMessage(REFRESH_LOCAL_PHOTOS);
            }
        }
    }

    //获取Magic的照片
    private void ScanPhotos(String filePath, String albumName, boolean needCallBck) {
        System.out.println("---------->scan" + albumName);
        if (!Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {//如果SD卡不存在
            app.scanMagicFinish = true;
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {//如果文件不存在，创建文件夹
            file.mkdirs();
            return;
        }
        File[] files = file.listFiles();
        Date date;
        if (needCallBck) {
            PictureAirLog.d(TAG, "need remove local data first");
            app.allPicList.removeAll(magicPicList);
            magicPicList.clear();
        }
        //判断是否为空
        if (app.magicPicList == null) {
            app.magicPicList = new ArrayList<PhotoInfo>();
        }
        app.magicPicList.clear();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().endsWith(".JPG") || files[i].getName().endsWith(".jpg")) {
                if (files[i].length() > 0) {//扫描到文件
                    selectPhotoItemInfo = new PhotoInfo();
                    selectPhotoItemInfo.photoPathOrURL = files[i].getPath();
                    selectPhotoItemInfo.lastModify = files[i].lastModified();
                    date = new Date(selectPhotoItemInfo.lastModify);
                    selectPhotoItemInfo.shootOn = sdf.format(date);
                    selectPhotoItemInfo.shootTime = selectPhotoItemInfo.shootOn.substring(0, 10);
                    selectPhotoItemInfo.isChecked = 0;
                    selectPhotoItemInfo.isSelected = 0;
                    selectPhotoItemInfo.showMask = 0;
                    selectPhotoItemInfo.locationName = getString(R.string.magic_location);
                    selectPhotoItemInfo.isPayed = 1;
                    selectPhotoItemInfo.onLine = 0;
                    selectPhotoItemInfo.isVideo = 0;
                    app.magicPicList.add(selectPhotoItemInfo);
                    System.out.println("magic url =========>" + selectPhotoItemInfo.photoPathOrURL);
                }
            }
        }
    }

    /**
     * 检查数据库是否有数据
     */
    private void loadDataFromDataBase() {
        System.out.println("load data from database");
        ArrayList<PhotoInfo> resultPhotoArrayList = pictureAirDbManager.getAllPhotoFromPhotoPassInfo(false);
        ArrayList<PhotoInfo> resultVideoArrayList = pictureAirDbManager.getAllPhotoFromPhotoPassInfo(true);
        ppPhotoCount = resultPhotoArrayList.size();
        app.photoPassPicList.addAll(resultPhotoArrayList);
        app.photoPassVideoList.addAll(resultVideoArrayList);
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
            API1.getPhotosByConditions(sharedPreferences.getString(Common.USERINFO_TOKENID, null), handler, null);//获取全部图片
            API1.getVideoList(null, handler);//获取全部视频信息
            if (MainTabActivity.maintabbadgeView.isShown()) {
                MainTabActivity.maintabbadgeView.hide();
            }
        }
        if (!app.scanMagicFinish) {
            dialog.show();
            scanPhotosThread = new ScanPhotosThread(scanMagicPhotoNeedCallBack);
            scanPhotosThread.start();
        } else {
            Collections.sort(app.magicPicList);
        }
        super.onResume();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
        for (int l = 0; l < app.photoPassPicList.size(); l++) {
            PhotoInfo info = app.photoPassPicList.get(l);
            //			PictureAirLog.d(TAG, "scan photo list:"+l);
            //先挑选出相同的locationid信息
            for (int i = 0; i < locationList.size(); i++) {
                //				PictureAirLog.d(TAG, "scan location:"+i);
                if (info.locationId.equals(locationList.get(i).locationId) || locationList.get(i).locationIds.contains(info.locationId)) {
                    //					PictureAirLog.d(TAG, "find the location");
                    //如果locationid一样，需要判断是否已经存在此item，如果有，在按照时间分类，没有，新建一个item
                    for (int j = 0; j < photoPassPictureList.size(); j++) {
                        //						PictureAirLog.d(TAG, "weather already exists:"+j);
                        if (info.shootTime.equals(photoPassPictureList.get(j).shootTime)
                                && (info.locationId.equals(photoPassPictureList.get(j).locationId) || photoPassPictureList.get(j).locationIds.contains(info.locationId))) {
                            //							PictureAirLog.d(TAG, "photo location id "+ info.locationId + "____"+ info.shootTime);
                            //							PictureAirLog.d(TAG, "location id:"+locationList.get(i).locationId +"___"+ locationList.get(i).locationIds);
                            //							PictureAirLog.d(TAG, "location id:"+photoPassPictureList.get(j).locationId +"___"+ photoPassPictureList.get(j).locationIds);
                            //							PictureAirLog.d(TAG, "already exist");
                            info.locationName = photoPassPictureList.get(j).place;
                            photoPassPictureList.get(j).list.add(info);
                            date1 = sdf.parse(info.shootOn);
                            date2 = sdf.parse(photoPassPictureList.get(j).shootOn);
                            if (date1.after(date2)) {
                                photoPassPictureList.get(j).shootOn = info.shootOn;
                            }
                            clone_contains = true;
                            addToBoughtList(info, i);
                            break;
                        }
                    }
                    if (!clone_contains) {
                        //初始化item的信息
                        //						PictureAirLog.d(TAG, "not exist");
                        photoItemInfo = new PhotoItemInfo();
                        photoItemInfo.locationId = locationList.get(i).locationId;
                        photoItemInfo.locationIds = locationList.get(i).locationIds.toString();
                        photoItemInfo.shootTime = info.shootTime;
                        photoItemInfo.place = locationList.get(i).place;
                        info.locationName = locationList.get(i).place;
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

        //处理视频信息
        for (int i = 0; i < app.photoPassVideoList.size(); i++) {
            PhotoInfo info = app.photoPassVideoList.get(i);
            PictureAirLog.out("video shoot time is " + info.shootOn);
            for (int j = 0; j < photoPassPictureList.size(); j++) {
                PictureAirLog.out("j-->" + j + ", info shootTime-->" + info.shootTime + ", picList-->" + photoPassPictureList.get(j).shootTime);
                if (info.shootTime.equals(photoPassPictureList.get(j).shootTime)) {
                    PictureAirLog.out("j-->" + j + ", info.isVideo-->" + info.isVideo + ", picList-->" + photoPassPictureList.get(j).list.get(0).isVideo);
                    if (info.isVideo == photoPassPictureList.get(j).list.get(0).isVideo) {
                        photoPassPictureList.get(j).list.add(info);
                        date1 = sdf.parse(info.shootOn);
                        date2 = sdf.parse(photoPassPictureList.get(j).shootOn);
                        PictureAirLog.out("date--->" + date1 + ";2-->" + date2);
                        if (date1.after(date2)) {
                            photoPassPictureList.get(j).shootOn = info.shootOn;
                        }
                        clone_contains = true;
                        break;
                    }
                }
            }
            //判断是否需要new
            if (!clone_contains) {//如果之前没有找到，说明需要new
                photoItemInfo = new PhotoItemInfo();
                PictureAirLog.out("need new shootTime:" + info.shootTime);
                photoItemInfo.shootTime = info.shootTime;
                photoItemInfo.place = getString(R.string.video_location);
                photoItemInfo.list.add(info);
                photoItemInfo.shootOn = info.shootOn;
                photoPassPictureList.add(photoItemInfo);
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
                    PictureAirLog.d(TAG, "waiting for scanning local photo completed");
                    if (app.scanMagicFinish) {
                        isLoading = false;
                    }
                }
                try {
                    getMagicData();
                    //将magic和photopass列表放入all中
                    Collections.sort(photoPassPictureList);
                    app.allPicList.addAll(photoPassPictureList);
                    app.allPicList.addAll(magicPicList);
                    Collections.sort(app.allPicList);//对all进行排序
                    ArrayList<PhotoItemInfo> originalPhotoList = new ArrayList<>();
                    originalPhotoList.addAll(app.allPicList);
                    favouritePictureList.addAll(pictureAirDbManager.checkLovePhotos(originalPhotoList, sharedPreferences.getString(Common.USERINFO_ID, "")));
                    System.out.println("location is ready");
                    handler.sendEmptyMessage(LOAD_COMPLETED);
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
            if (!app.allPicList.containsAll(photoPassPictureList)) {
                System.out.println("all lIst 不包含photopasspicturelist");
                app.allPicList.addAll(photoPassPictureList);
            }
            app.allPicList.addAll(magicPicList);
            Collections.sort(app.allPicList);//对all进行排序
            System.out.println("location is ready");
            handler.sendEmptyMessage(LOAD_COMPLETED);
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
    private void addToBoughtList(PhotoInfo info, int position) {
        PhotoItemInfo photoItemInfo;
        boolean isContains = false;
        //判断是否已经购买
        if (info.isPayed == 1) {//已购买状态，需要将图片放到bought列表中
            //			PictureAirLog.d(TAG, "add to bought list");
            for (int j = 0; j < app.boughtPicList.size(); j++) {
                //				PictureAirLog.d(TAG, "检查之前的是否存在");
                if (info.shootTime.equals(app.boughtPicList.get(j).shootTime) && info.locationId.equals(app.boughtPicList.get(j).locationId)) {
                    //					PictureAirLog.d(TAG, "已经存在于bought列表");
                    info.locationName = app.boughtPicList.get(j).place;
                    app.boughtPicList.get(j).list.add(info);
                    isContains = true;
                    break;
                }
            }
            if (!isContains) {//没有
                //				PictureAirLog.d(TAG, "不存在于之前的已购买的列表");
                //初始化item的信息
                photoItemInfo = new PhotoItemInfo();
                photoItemInfo.locationId = info.locationId;
                photoItemInfo.shootTime = info.shootTime;
                photoItemInfo.place = locationList.get(position).place;
                info.locationName = locationList.get(position).place;
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
            //			PictureAirLog.d(TAG, "not add to bought list");

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
        PictureAirLog.d(TAG, "----------->get magic photos" + app.magicPicList.size() + "____" + magicPicList.size());
        PhotoItemInfo photoItemInfo;
        boolean clone_contains = false;
        Date date1;
        Date date2;
        magicPicList.clear();//添加之前，先清除，防止添加pp/pp+造成数据重复添加
        for (int i = 0; i < app.magicPicList.size(); i++) {
            System.out.println("photo shoot time is " + app.magicPicList.get(i).shootOn);
            for (int j = 0; j < magicPicList.size(); j++) {
                if (app.magicPicList.get(i).shootTime.equals(magicPicList.get(j).shootTime)) {
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
            //判断是否需要new
            if (!clone_contains) {//如果之前没有找到，说明需要new
                photoItemInfo = new PhotoItemInfo();
                System.out.println("shootTime:" + app.magicPicList.get(i).shootTime);
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
     * 刷新数据的处理
     * 1.对所有获取的信息按照locationid和shoottime分类，放入一个新的photoiteminfo中
     * 2.加入list中，判断新的locationid是否在无photo的locationid中，如果有，需要重新排列list
     * 3.并且更新adapter
     */
    private void getrefreshdata() {
        PictureAirLog.e("getdata", "refreshdata");
        //根据数量，加入新的item
        System.err.println("all update data=" + app.photoPassPicList.size());
        System.err.println("all update video data=" + app.photoPassVideoList.size());
        PhotoItemInfo itemInfo;
        boolean findLocation = false;
        //先清除之前旧的列表
        app.allPicList.removeAll(photoPassPictureList);

        //将图片按照location加载到list中去
        for (int l = app.photoPassPicList.size() - refreshDataCount; l < app.photoPassPicList.size(); l++) {//遍历所要添加的图片list
            System.out.println("遍历照片");
            PhotoInfo info = app.photoPassPicList.get(l);
            //查找list_clone有图片的item，如果找到locationid，在判断是否有同一天的photos，如果有同一天的，add进去，如果没有，新建一个项
            for (int j = 0; j < photoPassPictureList.size(); j++) {//遍历list，查找locationid一样的内容
                System.out.println("遍历地址");
                PhotoItemInfo p = photoPassPictureList.get(j);
                if (info.locationId.equals(p.locationId) || p.locationIds.contains(info.locationId)) {//如果locationId和photo的locationid一样
                    System.out.println("location一样");
                    findLocation = true;
                    if (info.shootTime.equals(p.shootTime)) {//如果shoottime一致，则插入到列表中
                        System.out.println("shootTime一致，直接插入列表");
                        //比较时间，按照时间排序
                        for (int i = 0; i < p.list.size(); i++) {
                            try {
                                //									System.out.println("date1--->"+p.list.get(i).shootOn);
                                //									System.out.println("date2--->"+info.shootOn);
                                Date date1 = sdf.parse(p.list.get(i).shootOn);
                                Date date2 = sdf.parse(info.shootOn);//获取列表中的时间
                                info.locationName = p.place;

                                //									System.out.println("date1--->"+date1);
                                //									System.out.println("date2--->"+date2);
                                if (date2.after(date1)) {//需要添加的时间是最新的，显示在最前面
                                    System.out.println("the lastest time, need add");
                                    p.list.add(i, info);
                                    System.out.println("size->" + p.list.size());
                                    p.shootOn = info.shootOn;//更新shootOn的时间
                                    break;
                                } else {
                                    if (i == (p.list.size() - 1)) {//如果已经在最后一张了，直接添加在最后面
                                        System.out.println("the last position, need add");
                                        p.list.add(info);
                                        System.out.println("size->" + p.list.size());
                                        p.shootOn = info.shootOn;//更新shootOn的时间
                                        break;
                                    } else {

                                        System.out.println("scan next------>");
                                    }
                                }
                            } catch (ParseException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }//获取列表中的时间
                        }
                        //							System.out.println("after add new photo------>");
                        //记录当前的列表的索引
                        //							needmove = j;
                    } else {//时间不一致，新建列表
                        System.out.println("时间不一致，新建列表");
                        itemInfo = new PhotoItemInfo();
                        itemInfo.locationId = p.locationId;
                        itemInfo.locationIds = p.locationIds;
                        itemInfo.shootTime = info.shootTime;
                        itemInfo.place = p.place;
                        info.locationName = p.place;
                        itemInfo.list.add(0, info);
                        System.out.println("size->" + itemInfo.list.size());
                        itemInfo.placeUrl = p.placeUrl;
                        itemInfo.latitude = p.latitude;
                        itemInfo.longitude = p.longitude;
                        itemInfo.shootOn = info.shootOn;
                        //							itemInfo.gps = p.gps;
                        itemInfo.islove = p.islove;
                        photoPassPictureList.add(0, itemInfo);//放置到列表的顶部
                    }
                    //					}
                    break;
                }
            }
            if (findLocation) {//如果之前已经找到了对应的位置
                System.out.println("找到位置");
                findLocation = false;
            } else {//如果之前没有找到对应的位置，遍历地址列表，需要新建一个item，并且放入到最上方
                for (int k = 0; k < locationList.size(); k++) {
                    System.out.println("没有找到位置，遍历location");
                    if (info.locationId.equals(locationList.get(k).locationId) || locationList.get(k).locationIds.contains(info.locationId)) {
                        System.out.println("找到其他的location");
                        itemInfo = new PhotoItemInfo();
                        itemInfo.locationId = locationList.get(k).locationId;
                        itemInfo.locationIds = locationList.get(k).locationIds.toString();
                        itemInfo.shootTime = info.shootTime;
                        itemInfo.place = locationList.get(k).place;
                        info.locationName = locationList.get(k).place;
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
        }

        //将视频加载到list中去
        for (int l = app.photoPassVideoList.size() - refreshVideoDataCount; l < app.photoPassVideoList.size(); l++) {//遍历所要添加的图片list
            System.out.println("遍历照片");
            PhotoInfo info = app.photoPassVideoList.get(l);
            for (int j = 0; j < photoPassPictureList.size(); j++) {//遍历list，查找locationid一样的内容
                PhotoItemInfo p = photoPassPictureList.get(j);
                if (info.shootTime.equals(p.shootTime) && info.isVideo == p.list.get(0).isVideo) {
                    findLocation = true;
                    for (int i = 0; i < p.list.size(); i++) {
                        try {
                            Date date1 = sdf.parse(p.list.get(i).shootOn);
                            Date date2 = sdf.parse(info.shootOn);//获取列表中的时间
                            if (date2.after(date1)) {//需要添加的时间是最新的，显示在最前面
                                PictureAirLog.out("the lastest time, need add");
                                p.list.add(i, info);
                                PictureAirLog.out("size->" + p.list.size());
                                p.shootOn = info.shootOn;//更新shootOn的时间
                                break;
                            } else {
                                if (i == (p.list.size() - 1)) {//如果已经在最后一张了，直接添加在最后面
                                    PictureAirLog.out("the last position, need add");
                                    p.list.add(info);
                                    PictureAirLog.out("size->" + p.list.size());
                                    p.shootOn = info.shootOn;//更新shootOn的时间
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
                System.out.println("找到位置");
                findLocation = false;
            } else {//如果之前没有找到对应的位置，遍历地址列表，需要新建一个item，并且放入到最上方
                itemInfo = new PhotoItemInfo();
                itemInfo.shootTime = info.shootTime;
                itemInfo.shootOn = info.shootOn;
                itemInfo.place = getString(R.string.video_location);
                itemInfo.list.add(info);
                photoPassPictureList.add(0, itemInfo);
            }
        }
        app.allPicList.addAll(photoPassPictureList);
        PictureAirLog.out("start-----------> all sort");
        Collections.sort(app.allPicList);//对all进行排序
        PictureAirLog.out("start-----------> photoPass sort");
        Collections.sort(photoPassPictureList);
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

            default:
                break;
        }

    }

    //执行下拉刷新
    public static void doRefresh() {
        System.out.println("do refresh----->");
//		if (sharedPreferences.getInt(Common.PP_COUNT, 0) > 1) {
//			storyViewPagerAdapter.startRefresh();
//		}
    }

}
