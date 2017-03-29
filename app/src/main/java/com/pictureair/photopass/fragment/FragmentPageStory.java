package com.pictureair.photopass.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.AddPPPCodeActivity;
import com.pictureair.photopass.activity.BaseFragment;
import com.pictureair.photopass.activity.EditStoryAlbumActivity;
import com.pictureair.photopass.activity.MipCaptureActivity;
import com.pictureair.photopass.activity.OpinionsActivity;
import com.pictureair.photopass.activity.PanicBuyActivity;
import com.pictureair.photopass.activity.SubmitOrderActivity;
import com.pictureair.photopass.adapter.DailyPPCardRecycleAdapter;
import com.pictureair.photopass.customDialog.PWDialog;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.entity.CartPhotosInfo;
import com.pictureair.photopass.entity.DailyPPCardInfo;
import com.pictureair.photopass.entity.DealingInfo;
import com.pictureair.photopass.entity.DiscoverLocationItemInfo;
import com.pictureair.photopass.entity.GoodsInfo;
import com.pictureair.photopass.entity.GoodsInfoJson;
import com.pictureair.photopass.entity.JsonInfo;
import com.pictureair.photopass.entity.PPinfo;
import com.pictureair.photopass.eventbus.BaseBusEvent;
import com.pictureair.photopass.eventbus.MainTabOnClickEvent;
import com.pictureair.photopass.eventbus.MainTabSwitchEvent;
import com.pictureair.photopass.eventbus.PPDeleteEvent;
import com.pictureair.photopass.eventbus.RedPointControlEvent;
import com.pictureair.photopass.eventbus.ScanInfoEvent;
import com.pictureair.photopass.eventbus.SocketEvent;
import com.pictureair.photopass.eventbus.StoryLoadCompletedEvent;
import com.pictureair.photopass.greendao.PictureAirDbManager;
import com.pictureair.photopass.http.rxhttp.RxSubscribe;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.API2;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.JsonTools;
import com.pictureair.photopass.util.JsonUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.CustomTextView;
import com.pictureair.photopass.widget.NoNetWorkOrNoCountView;
import com.pictureair.photopass.widget.PPPPop;
import com.pictureair.photopass.widget.PWToast;
import com.pictureair.photopass.widget.RecycleTopDividerItemDecoration;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * PhotoPass照片的图片墙，用来显示从服务器返回的照片信息，以及通过magic相机拍摄的图片
 * 可以左右滑动切换不同的相册
 * 可以下拉刷新，获取更多的图片信息
 */
public class FragmentPageStory extends BaseFragment implements OnClickListener, DailyPPCardRecycleAdapter.OnRecyclerViewItemClickListener, PWDialog.OnPWDialogClickListener {
    //声明静态变量
    private static final int REFRESH_ALL_PHOTOS = 1006;

    private static final String TAG = "FragmentPageStory";

    /**
     * 是否需要重新拉取数据
     */
    private boolean needfresh = false;

    /**
     * 从sp中读取的值，如果是true，就要保存起来，一旦失败，就要把这个值给needfresh
     */
    private boolean sharedNeedFresh = false;
    private int screenWidth;
    private boolean hasHidden = false;

    //申明控件
    private ImageView scanIv;
    private RelativeLayout scanLayout;
    private RelativeLayout menuLayout;
    private ScrollView storyNoPpToScanLinearLayout;
    private NoNetWorkOrNoCountView noNetWorkOrNoCountView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PPPPop pppPop;
    private CustomTextView specialDealOnTV;
    private CustomTextView specialDealDetailTV;
    private LinearLayout specialDealLL;
    private RelativeLayout draftLayout;
    private RecyclerView dailyPPCardRV;
    private DailyPPCardRecycleAdapter dailyPPCardRecycleAdapter;
    private ImageView img_float_hide;
    private FloatingActionButton fab;
    private PWToast myToast;
    private PWDialog pwDialog;
    private ImageView scanImgIV;
    private TextView scanTV;
    private CustomTextView buyPPPTV;
    private CustomTextView useDailyPPPTV;
    private CustomTextView usePPPTV;

    //申明变量
    private MyApplication app;
    private ArrayList<DailyPPCardInfo> dailyPPCardInfoArrayList = new ArrayList<>();
    private ArrayList<DiscoverLocationItemInfo> locationList = new ArrayList<>();
    private ArrayList<PPinfo> pPinfoArrayList = new ArrayList<>();
    private Activity context;
    private DealingInfo dealingInfo;
    private GoodsInfo pppGoodsInfo = null;
    private String[] photoUrls;
    private String cartId = null;

    /**
     * 同步已经购买的照片
     */
    private boolean syncingBoughtPhotos = false;

    private boolean mIsAskStoragePermission = false;

    //申明handler消息回调机制
    private final Handler fragmentPageStoryHandler = new FragmentPageStoryHandler(this);

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
            case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_RELOAD://noView的按钮响应重新加载点击事件
                //重新加载数据
                PictureAirLog.out("onclick with reload");
                showPWProgressDialog();
                getPPList(true, false);
                break;

            case PPPPop.POP_SCAN:
                Intent intent = new Intent(context, MipCaptureActivity.class);
                startActivity(intent);
                if (pppPop.isShowing()) {
                    pppPop.dismiss();
                }
                break;

            case PPPPop.POP_INPUT://进入手动输入页面
                Intent intent2 = new Intent(context, AddPPPCodeActivity.class);
                intent2.putExtra("type", "ppp");//只扫描ppp
                startActivity(intent2);
                if (pppPop.isShowing()) {
                    pppPop.dismiss();
                }
                break;

            case REFRESH_ALL_PHOTOS:
                if (!hasHidden) {
                    showPWProgressDialog();
                    getPPList(false, true);

                } else {
                    fragmentPageStoryHandler.sendEmptyMessageDelayed(REFRESH_ALL_PHOTOS, 500);
                }
                break;
            
            default:
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PictureAirLog.out("story flow ---> on create----->story");
        context = getActivity();
        View view = inflater.inflate(R.layout.fragment_story, container, false);
        setImmersiveMode(view);
        //获取控件
        scanIv = (ImageView) view.findViewById(R.id.story_menu_iv);
        scanLayout = (RelativeLayout) view.findViewById(R.id.story_menu_rl);
        menuLayout = (RelativeLayout) view.findViewById(R.id.story_drawer_rl);
        storyNoPpToScanLinearLayout = (ScrollView) view.findViewById(R.id.story_no_pp_to_scan);
        noNetWorkOrNoCountView = (NoNetWorkOrNoCountView) view.findViewById(R.id.storyNoNetWorkView);
        dailyPPCardRV = (RecyclerView) view.findViewById(R.id.story_fragment_rv);

        scanImgIV = (ImageView) view.findViewById(R.id.story_no_pp_scan);
        scanTV = (TextView) view.findViewById(R.id.story_scan);
        buyPPPTV = (CustomTextView) view.findViewById(R.id.story_buy_ppp);
        useDailyPPPTV = (CustomTextView) view.findViewById(R.id.story_activate_daily_ppp);
        usePPPTV = (CustomTextView) view.findViewById(R.id.story_activate_ppp);
        scanImgIV.setOnClickListener(this);
        scanTV.setOnClickListener(this);
        buyPPPTV.setOnClickListener(this);
        useDailyPPPTV.setOnClickListener(this);
        usePPPTV.setOnClickListener(this);

        specialDealLL = (LinearLayout) view.findViewById(R.id.special_deal_ll);
        specialDealDetailTV = (CustomTextView) view.findViewById(R.id.special_deal_detail_tv);
        specialDealOnTV = (CustomTextView) view.findViewById(R.id.special_deal_on);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.story_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                PictureAirLog.out("start refresh");
                getRefreshData();
            }
        });

        draftLayout= (RelativeLayout) view.findViewById(R.id.draft_layout);
        draftLayout.setVisibility(View.GONE);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        img_float_hide = (ImageView) view.findViewById(R.id.float_hide);
        fab.setOnClickListener(this);
        img_float_hide.setOnClickListener(this);
        String currentLanguage = SPUtils.getString(context, Common.SHARED_PREFERENCE_APP, Common.LANGUAGE_TYPE, Common.ENGLISH);
        if ("zh".equals(currentLanguage)) {
            fab.setIcon(R.drawable.float_customer);
        } else if ("en".equals(currentLanguage)){
            fab.setIcon(R.drawable.float_customer);
        }
        //初始化控件
        PictureAirLog.out("dialog-----> in story");
        showPWProgressDialog();
        app = (MyApplication) context.getApplication();
        PictureAirLog.out("current tap---->" + app.fragmentStoryLastSelectedTab);

        scanLayout.setOnClickListener(this);
        menuLayout.setOnClickListener(this);
        specialDealLL.setOnClickListener(this);
        specialDealOnTV.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
        //初始化数据
        myToast = new PWToast(getActivity());
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
        //获取全部的pp
        getPPList(true, false);
        return view;
    }

    /**
     * 获取pp列表
     * @param needGetLocationData 是否需要继续获取location等信息
     */
    private void getPPList(final boolean needGetLocationData, final boolean refresh) {
        PictureAirLog.d("get pp list ----> " + needGetLocationData);
        API2.getPPSByUserId().compose(this.<JSONObject>bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        pPinfoArrayList.clear();
                        pPinfoArrayList.addAll(JsonUtil.getPPList(jsonObject));
                    }

                    @Override
                    public void _onError(int status) {
                        if (refresh) {//如果是刷新操作，需要获取照片信息
                            getLocationPhotos(refresh);
                        } else if (needGetLocationData) {//不是刷新操作，需要获取地点信息
                            getLocationData(false);
                        }
                    }

                    @Override
                    public void onCompleted() {
                        if (refresh) {
                            getLocationPhotos(refresh);
                        } else if (needGetLocationData) {
                            getLocationData(false);
                        }

                        //通知首页更新PP列表
                        EventBus.getDefault().post(new MainTabSwitchEvent(MainTabSwitchEvent.DRAGER_VIEW_UPDATE, pPinfoArrayList));

                    }
                });
    }

    /**
     * 获取地点信息
     * @param isRefresh 是否是下拉刷新操作
     */
    private void getLocationData(final boolean isRefresh) {
        PictureAirLog.d("loadData start");
        //先获取缓存的数据
        Observable.just(ACache.get(getActivity()).getAsString(Common.DISCOVER_LOCATION))
                .flatMap(new Func1<String, Observable<JSONObject>>() {
                    @Override
                    public Observable<JSONObject> call(final String s) {
                        if (!TextUtils.isEmpty(s)) {//如果不为空，把缓存数据传递下去
                            PictureAirLog.d("load data---> cache is not empty");
                            return Observable.just(JSONObject.parseObject(s));

                        } else {//如果为空，请求服务器，并将数据缓存起来
                            PictureAirLog.d("load data---> cache is empty, need get from net");
                            return API2.getLocationInfo(MyApplication.getTokenId())
                                    .map(new Func1<JSONObject, JSONObject>() {
                                        @Override
                                        public JSONObject call(JSONObject jsonObject) {
                                            ACache.get(context).put(Common.DISCOVER_LOCATION, jsonObject.toString());
                                            return jsonObject;
                                        }
                                    });

                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .map(new Func1<JSONObject, ArrayList<DiscoverLocationItemInfo>>() {
                    @Override
                    public ArrayList<DiscoverLocationItemInfo> call(JSONObject jsonObject) {
                        PictureAirLog.d(TAG, "load data ---> get location success" + jsonObject.toString());
                        locationList.clear();
                        locationList.addAll(AppUtil.getLocation(context, jsonObject.toString(), true));
                        return locationList;
                    }
                })
                //开始从数据库获取一卡一天的数据
                .map(new Func1<ArrayList<DiscoverLocationItemInfo>, ArrayList<DailyPPCardInfo>>() {
                    @Override
                    public ArrayList<DailyPPCardInfo> call(ArrayList<DiscoverLocationItemInfo> discoverLocationItemInfos) {
                        ArrayList<DailyPPCardInfo> list = new ArrayList<>();
                        PictureAirLog.d("need refresh---> " + needfresh);
                        if (!needfresh) {//不需要刷新
                            PictureAirLog.d(TAG, "---------> load data from databases");
                            ArrayList<JsonInfo> jsonInfos = new ArrayList<>();
                            jsonInfos.addAll(PictureAirDbManager.getJsonInfos(JsonInfo.JSON_LOCATION_PHOTO_TYPE));
                            list.addAll(JsonUtil.getDailyPPCardInfoList(jsonInfos, discoverLocationItemInfos, MyApplication.getInstance().getLanguageType()));
                        }
                        return list;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<ArrayList<DailyPPCardInfo>>bindToLifecycle())
                .subscribe(new RxSubscribe<ArrayList<DailyPPCardInfo>>() {
                    @Override
                    public void _onNext(ArrayList<DailyPPCardInfo> dailyPPCardInfos) {
                        dailyPPCardInfoArrayList.clear();
                        dailyPPCardInfoArrayList.addAll(dailyPPCardInfos);
                    }

                    @Override
                    public void _onError(int status) {
                        loadError(true);
                    }

                    @Override
                    public void onCompleted() {
                        if (dailyPPCardInfoArrayList.size() == 0 || needfresh) {
                            //数据为0，需要从网上下载，判断是否之前有成功获取过
                            PictureAirLog.out("story flow ---> start get photo from net");
                            getLocationPhotos(false);

                        } else {
                            PictureAirLog.out("story flow ---> show data");
                            //有数据，直接显示
                            //清空推送消息的数量
                            app.setPushPhotoCount(0);
                            if (!isRefresh) {//全部加载
                                //获取广告信息
                                if (!app.isGetADLocationSuccess()) {
                                    PictureAirLog.out("start get ad location");
                                    getADInfo();
                                } else {
                                    PictureAirLog.out("ad location has got already");
                                }

                                if (syncingBoughtPhotos) {//同步购买照片操作
                                    syncingBoughtPhotos = false;
                                    if (dailyPPCardRecycleAdapter != null) {
                                        dailyPPCardRecycleAdapter.notifyDataSetChanged();
                                    }
                                } else {
                                    showViewPager();
                                    noNetWorkOrNoCountView.setVisibility(View.GONE);//无网络状态的View设置为不可见
                                    if (sharedNeedFresh) {
                                        sharedNeedFresh = false;
                                    }
                                }
                                dismissPWProgressDialog();
                            }
                        }

                    }
                });
    }

    /**
     * 获取一卡一天的数据列表
     * @param isRefresh isrefresh
     */
    private void getLocationPhotos(final boolean isRefresh) {
        API2.getLocationPhoto(MyApplication.getTokenId())
                .map(new Func1<JSONObject, ArrayList<DailyPPCardInfo>>() {
                    @Override
                    public ArrayList<DailyPPCardInfo> call(JSONObject jsonObject) {
                        //更新数据库
                        if (jsonObject.containsKey("locationP")) {
                            PictureAirDbManager.updateJsonInfos(jsonObject.getJSONArray("locationP"), JsonInfo.JSON_LOCATION_PHOTO_TYPE);

                        }
                        return JsonUtil.getDailyPPCardInfoList(jsonObject, locationList, MyApplication.getInstance().getLanguageType());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<ArrayList<DailyPPCardInfo>>bindToLifecycle())
                .subscribe(new RxSubscribe<ArrayList<DailyPPCardInfo>>() {
                    @Override
                    public void _onNext(ArrayList<DailyPPCardInfo> strings) {
                        dailyPPCardInfoArrayList.clear();
                        dailyPPCardInfoArrayList.addAll(strings);
                    }

                    @Override
                    public void _onError(int status) {
                        loadError(!isRefresh);
                    }

                    @Override
                    public void onCompleted() {
                        //清空推送消息的数量
                        app.setPushPhotoCount(0);

                        if (isRefresh) {
                            SPUtils.put(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, "photoCount", 0);

                            //刷新广告地点
                            app.setGetADLocationSuccess(false);
                            getADInfo();

                            showViewPager();
                            if (swipeRefreshLayout.isRefreshing()) {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                            dismissPWProgressDialog();
                        } else {
                            //获取广告信息
                            if (!app.isGetADLocationSuccess()) {
                                PictureAirLog.out("start get ad location");
                                getADInfo();
                            } else {
                                PictureAirLog.out("ad location has got already");
                            }

                            if (syncingBoughtPhotos) {//同步购买照片操作
                                syncingBoughtPhotos = false;
                                if (dailyPPCardRecycleAdapter != null) {
                                    dailyPPCardRecycleAdapter.notifyDataSetChanged();
                                }
                            } else {
                                showViewPager();
                                noNetWorkOrNoCountView.setVisibility(View.GONE);//无网络状态的View设置为不可见
                                if (sharedNeedFresh) {
                                    sharedNeedFresh = false;
                                }
                            }
                            dismissPWProgressDialog();
                        }

                    }
                });
    }

    /**
     * 开始刷新
     */
    private void getRefreshData() {
        PictureAirLog.d(TAG, "start refreshing");
        //获取socket推送信息
        API2.getSocketData()
                .compose(this.<JSONObject>bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        //获取推送成功，后面逻辑按照之前走
                        PictureAirLog.e(TAG, "GET_SOCKET_DATA_SUCCESS: " + jsonObject.toString());
                        if (jsonObject.size() > 0) {
                            JsonUtil.dealGetSocketData(MyApplication.getInstance().getApplicationContext(), jsonObject.toString(), true, null);
                        }
                    }

                    @Override
                    public void _onError(int status) {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });

        getPPList(false, true);
        showLeadView();
    }

    /**
     * 全部处理完成之后会调用
     */
    private void loadError(boolean setVisibile) {
        //将video和photo标记清空
        dismissPWProgressDialog();
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
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
        }
    }

    /**
     * 获取广告视频
     */
    private void getADInfo() {
        PictureAirLog.d("get ad info");
        API2.getADLocations()
                .doOnNext(new Action1<JSONObject>() {
                    @Override
                    public void call(JSONObject jsonObject) {
                        PictureAirLog.out("ad location---->" + jsonObject.toString());
                        /**
                         * 1.存入数据库
                         * 2.在application中记录结果
                         */
                        PictureAirDbManager.insertADLocations(jsonObject.getJSONArray("locations"));
                        app.setGetADLocationSuccess(true);
                    }
                })
                .compose(this.<JSONObject>bindToLifecycle())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {

                    }

                    @Override
                    public void _onError(int status) {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    /**
     * 控制控件的隐藏或者显示
     */
    private void showViewPager() {
        PictureAirLog.out("story flow ---> show view");
        if (dailyPPCardInfoArrayList != null && dailyPPCardInfoArrayList.size() > 0) {//有卡
            PictureAirLog.out("viewpager---->has photos");
            //隐藏没有pp的情况
            storyNoPpToScanLinearLayout.setVisibility(View.GONE);
            //显示有pp的情况
            dailyPPCardRV.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setEnabled(true);

            if (dailyPPCardRecycleAdapter == null) {
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                dailyPPCardRecycleAdapter = new DailyPPCardRecycleAdapter(context, dailyPPCardInfoArrayList);
                dailyPPCardRecycleAdapter.setOnItemClickListener(this);
                dailyPPCardRV.addItemDecoration(new RecycleTopDividerItemDecoration(ScreenUtil.dip2px(context, 10)));
                dailyPPCardRV.setLayoutManager(linearLayoutManager);
                dailyPPCardRV.setAdapter(dailyPPCardRecycleAdapter);
            } else {
                dailyPPCardRecycleAdapter.notifyDataSetChanged();
            }

            if (app.getPushPhotoCount() == 0) {
                PictureAirLog.out("need gone the badgeview");
                PictureAirLog.out("photocount---->" + app.getPushPhotoCount());
                EventBus.getDefault().post(new RedPointControlEvent(false));
            }
        } else {//没有卡
            swipeRefreshLayout.setEnabled(false);
            //没有扫描过
            PictureAirLog.out("viewpager---->has not scan pp");
            //显示没有pp的情况
            storyNoPpToScanLinearLayout.setVisibility(View.VISIBLE);
        }
        PictureAirLog.out("story flow ---> show view done");
        showLeadView();
    }

    private void showLeadView(){
        if (TextUtils.isEmpty(SPUtils.getString(MyApplication.getInstance(), Common.SHARED_PREFERENCE_APP, Common.STORY_LEAD_VIEW, null))) {
            EventBus.getDefault().post(new StoryLoadCompletedEvent(true, specialDealLL.isShown()));
        } else {
            EventBus.getDefault().post(new StoryLoadCompletedEvent(false, specialDealLL.isShown()));
        }
    }

    private void refreshPPlist() {
        if (SPUtils.getBoolean(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.NEED_FRESH, false)) {
            PictureAirLog.out("need refresh");
            app.needScanFavoritePhotos = false;//防止会重复执行，所以此处改为false
            SPUtils.put(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.NEED_FRESH, false);
            showPWProgressDialog();
            //加卡，删卡，需要更新pplist信息，删图片，需要更新locationPhoto信息，因此此处全部处理
            getPPList(false, true);
            EventBus.getDefault().post(new RedPointControlEvent(false));
        }
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
        refreshPPlist();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (hasHidden) {
            PictureAirLog.out("bu ke jian");
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
            if (app.getPushPhotoCount() > 0) {
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

    @Override
    public void onClick(View v) {
        Intent i;
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

            //侧边栏按钮
            case R.id.story_drawer_rl:
                EventBus.getDefault().post(new MainTabSwitchEvent(MainTabSwitchEvent.DRAGER_VIEW));
                break;

            case R.id.story_no_pp_scan:
            case R.id.story_scan:
            case R.id.story_activate_daily_ppp:
                i = new Intent(context, MipCaptureActivity.class);
                startActivity(i);
                break;

            case R.id.story_activate_ppp://添加ppp
                i = new Intent(context, AddPPPCodeActivity.class);
                i.putExtra("type", "ppp");//只扫描ppp
                startActivity(i);
                break;

            case R.id.story_buy_ppp:
                buyClick(0);
                break;

            case R.id.special_deal_ll:
                //抢单点击事件，即可进入抢单活动页面
                PictureAirLog.d("deal url---> " + dealingInfo.getDealingUrl() + "tokenid-->" + MyApplication.getTokenId());
                i = new Intent(context, PanicBuyActivity.class);
                i.putExtra("dealingInfo", dealingInfo);
                startActivity(i);
                break;

            case R.id.fab:
                i = new Intent(context, OpinionsActivity.class);
                startActivity(i);
                break;

            case R.id.float_hide:
                draftLayout.setVisibility(View.GONE);
                break;

            default:
                break;
        }

    }

    @Override
    public void downloadClick(int position) {
        PictureAirLog.d("download click-->" + " :" + position);
        itemClick(position);
    }

    @Override
    public void buyClick(int position) {
        PictureAirLog.d("buy click---> " + position);
        if (pwDialog == null) {
            pwDialog = new PWDialog(context)
                    .setOnPWDialogClickListener(this)
                    .setPWDialogMessage(R.string.story_buy_dialog)
                    .setPWDialogNegativeButton(R.string.update_ppp_cancel)
                    .setPWDialogPositiveButton(R.string.update_ppp_ok)
                    .pwDialogCreate();
        }
        pwDialog.pwDilogShow();
    }

    @Override
    public void itemClick(int position) {
        PictureAirLog.d("item click-->" + dailyPPCardInfoArrayList.get(position).getPpCode());
        if (dailyPPCardInfoArrayList.get(position).getPhotoCount() == 0) {//空卡，不可点击
            return;
        }
        //进入相册
        Intent i = new Intent(context, EditStoryAlbumActivity.class);
        i.putExtra("ppCode", dailyPPCardInfoArrayList.get(position).getPpCode());
        i.putExtra("shootDate", dailyPPCardInfoArrayList.get(position).getShootDate());
        i.putExtra("activated", dailyPPCardInfoArrayList.get(position).getActivated());
        i.putExtra("photoCount", dailyPPCardInfoArrayList.get(position).getPhotoCount());
        startActivity(i);
    }

    @Override
    public void onPWDialogButtonClicked(int which, int dialogId) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                showPWProgressDialog();
                //获取商品（以后从缓存中取）
                getGoods();
                break;
        }
    }

    private void clickToRefresh() {
        PictureAirLog.out("do refresh when noPhotoView is showing");
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setEnabled(true);
            swipeRefreshLayout.setRefreshing(true);
            getRefreshData();
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
            if (storyRefreshOnClickEvent.getEventType() == MainTabOnClickEvent.STORY_TAB_CLICK_EVENT) {//通知页面开始刷新
                clickToRefresh();

            } else if (storyRefreshOnClickEvent.getEventType() == MainTabOnClickEvent.SPECIAL_DEAL_EVENT) {//抢单流程
                dealingInfo = storyRefreshOnClickEvent.getDealingInfo();

                if (storyRefreshOnClickEvent.isShowSpecialDealBar()) {
                    specialDealLL.setVisibility(View.VISIBLE);
                    specialDealDetailTV.setText(dealingInfo.getDetails());
                } else {
                    specialDealLL.setVisibility(View.GONE);
                }

                if (storyRefreshOnClickEvent.isSpecialDealBuyClick()) {//如果是点了购买按钮，需要执行购买流程
                    specialDealLL.performClick();
                }
            }
            EventBus.getDefault().removeStickyEvent(storyRefreshOnClickEvent);
        }
        if (baseBusEvent instanceof SocketEvent) {
            SocketEvent socketEvent = (SocketEvent) baseBusEvent;
            if (socketEvent.getType() == SocketEvent.SOCKET_REFRESH_PHOTO) {//刷新全部图片
                if (!syncingBoughtPhotos) {
                    syncingBoughtPhotos = true;
                    fragmentPageStoryHandler.sendEmptyMessageDelayed(REFRESH_ALL_PHOTOS, 500);
                }

            } else if (!syncingBoughtPhotos) {//延迟2秒，防止多次执行导致app异常
                syncingBoughtPhotos = true;
                PictureAirLog.out("start sync------->");
                fragmentPageStoryHandler.sendEmptyMessageDelayed(REFRESH_ALL_PHOTOS, 2000);

            } else {
                PictureAirLog.out("still waiting sync");
            }
            //刷新列表
            EventBus.getDefault().removeStickyEvent(socketEvent);
        } else if (baseBusEvent instanceof PPDeleteEvent) {
            final PPDeleteEvent ppDeleteEvent = (PPDeleteEvent) baseBusEvent;
            ArrayList<PPinfo> list = new ArrayList<>();
            list.addAll(ppDeleteEvent.getPpList());
            /**
             * 1.删除数据库中一卡一天的信息
             * 2.刷新标记也需要从数据库中删除
             * 3.如果是有卡无图，需要删除列表信息
             * 4.如果是有卡有图，需要删除列表信息
             */
            Observable.from(list)
                    .subscribeOn(Schedulers.io())
                    .map(new Func1<PPinfo, Object>() {

                        @Override
                        public Object call(PPinfo info) {
                            PictureAirDbManager.deleteJsonInfosByTypeAndString(JsonInfo.JSON_LOCATION_PHOTO_TYPE, "PPCode: \"" + info.getPpCode() + "\"");
                            PictureAirDbManager.deleteJsonInfosByTypeAndString(JsonInfo.DAILY_PP_REFRESH_ALL_TYPE, info.getPpCode());
//                            //有卡无图
//                            Iterator<PPinfo> iterator = pPinfoArrayList.iterator();
//                            while (iterator.hasNext()) {
//                                PPinfo pPinfo = iterator.next();
//                                if (pPinfo.getPpCode().equals(info.getPpCode())) {
//                                    iterator.remove();
//                                }
//                            }

                            //有卡有图
                            Iterator<DailyPPCardInfo> iterator2 = dailyPPCardInfoArrayList.iterator();
                            while (iterator2.hasNext()) {
                                DailyPPCardInfo dailyPPCardInfo = iterator2.next();
                                if (dailyPPCardInfo.getPpCode().equals(info.getPpCode())) {
                                    iterator2.remove();
                                }
                            }
                            return null;
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(this.bindToLifecycle())
                    .subscribe(new RxSubscribe<Object>() {

                        @Override
                        public void onCompleted() {
                            if (dailyPPCardRecycleAdapter != null) {
                                dailyPPCardRecycleAdapter.notifyDataSetChanged();
                            }
                            EventBus.getDefault().removeStickyEvent(ppDeleteEvent);
                            showViewPager();
                        }

                        @Override
                        public void _onNext(Object o) {

                        }

                        @Override
                        public void _onError(int status) {

                        }
                    });
        } else if (baseBusEvent instanceof ScanInfoEvent){
            //为了解决从一日通页面添加pp卡 切换回来之后不刷新列表的问题
            final ScanInfoEvent scanInfoEvent = (ScanInfoEvent) baseBusEvent;
            refreshPPlist();
            EventBus.getDefault().removeStickyEvent(scanInfoEvent);
        }
    }

    /**
     * 初始化数据
     */
    private void getGoods() {
        if (AppUtil.getNetWorkType(MyApplication.getInstance()) == 0) {
            myToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
            dismissPWProgressDialog();
            return;
        }
        //从缓层中获取数据
        Observable.just(ACache.get(getActivity()).getAsString(Common.ALL_GOODS))
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<String, Observable<JSONObject>>() {
                    @Override
                    public Observable<JSONObject> call(String s) {
                        if (!TextUtils.isEmpty(s)) {
                            PictureAirLog.d("goods is not null");
                            return Observable.just(JSONObject.parseObject(s));
                        } else {
                            PictureAirLog.d("goods is null");
                            //从网络获取商品,先检查网络
                            return API2.getGoods()
                                    .map(new Func1<JSONObject, JSONObject>() {
                                        @Override
                                        public JSONObject call(JSONObject jsonObject) {
                                            ACache.get(MyApplication.getInstance()).put(Common.ALL_GOODS, jsonObject.toString(), ACache.TIME_DAY);
                                            return jsonObject;
                                        }
                                    });
                        }
                    }
                })
                //解析json
                .map(new Func1<JSONObject, GoodsInfo>() {
                    @Override
                    public GoodsInfo call(JSONObject jsonObject) {
                        PictureAirLog.d("parse goods json");
                        List<GoodsInfo> allGoodsList1 = new ArrayList<>();
                        GoodsInfoJson goodsInfoJson = JsonTools.parseObject(jsonObject.toString(), GoodsInfoJson.class);//GoodsInfoJson.getString()
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
                        photoUrls = new String[pppGoodsInfo.getPictures().size()];
                        for (int i = 0; i < pppGoodsInfo.getPictures().size(); i++) {
                            photoUrls[i] = pppGoodsInfo.getPictures().get(i).getUrl();
                        }
                        return pppGoodsInfo;
                    }
                })
                //加入购物车
                .flatMap(new Func1<GoodsInfo, Observable<JSONObject>>() {
                    @Override
                    public Observable<JSONObject> call(GoodsInfo goodsInfo) {
                        PictureAirLog.d("start add to goods key:" + goodsInfo.getGoodsKey());
                        //调用addToCart API1
                        return API2.addToCart(goodsInfo.getGoodsKey(), 1, true, null);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<JSONObject>bindToLifecycle())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        PictureAirLog.d("add to cart success--> " + jsonObject);

                        int currentCartCount = SPUtils.getInt(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
                        SPUtils.put(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, currentCartCount + 1);
                        cartId = jsonObject.getString("cartId");
                        PictureAirLog.d("cartid--> " + cartId);

                    }

                    @Override
                    public void _onError(int status) {
                        dismissPWProgressDialog();
                        myToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), status), Common.TOAST_SHORT_TIME);
                    }

                    @Override
                    public void onCompleted() {
                        dismissPWProgressDialog();
                        //生成订单
                        Intent intent3 = new Intent(context, SubmitOrderActivity.class);
                        ArrayList<CartItemInfo> orderinfoArrayList = new ArrayList<>();
                        CartItemInfo cartItemInfo = new CartItemInfo();
                        cartItemInfo.setCartId(cartId);
                        cartItemInfo.setProductName(pppGoodsInfo.getName());
                        cartItemInfo.setProductNameAlias(pppGoodsInfo.getNameAlias());
                        cartItemInfo.setUnitPrice(pppGoodsInfo.getPrice());
                        cartItemInfo.setEmbedPhotos(new ArrayList<CartPhotosInfo>());
                        cartItemInfo.setDescription(pppGoodsInfo.getDescription());
                        cartItemInfo.setQty(1);
                        cartItemInfo.setStoreId(pppGoodsInfo.getStoreId());
                        cartItemInfo.setPictures(photoUrls);
                        cartItemInfo.setPrice(pppGoodsInfo.getPrice());
                        cartItemInfo.setCartProductType(3);

                        orderinfoArrayList.add(cartItemInfo);
                        intent3.putExtra("orderinfo", orderinfoArrayList);
                        startActivity(intent3);
                    }
                });

    }
}
