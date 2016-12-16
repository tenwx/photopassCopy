package com.pictureair.photopass.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
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

import com.alibaba.fastjson.JSONObject;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.AddPPPCodeActivity;
import com.pictureair.photopass.activity.BaseFragment;
import com.pictureair.photopass.activity.EditStoryAlbumActivity;
import com.pictureair.photopass.activity.GifPlayActivity;
import com.pictureair.photopass.activity.MipCaptureActivity;
import com.pictureair.photopass.activity.OpinionsActivity;
import com.pictureair.photopass.activity.PPPDetailProductActivity;
import com.pictureair.photopass.activity.PanicBuyActivity;
import com.pictureair.photopass.adapter.DailyPPCardRecycleAdapter;
import com.pictureair.photopass.adapter.NoPhotoRecycleAdapter;
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
import com.pictureair.photopass.eventbus.RedPointControlEvent;
import com.pictureair.photopass.eventbus.SocketEvent;
import com.pictureair.photopass.eventbus.StoryLoadCompletedEvent;
import com.pictureair.photopass.greendao.PictureAirDbManager;
import com.pictureair.photopass.http.rxhttp.RxSubscribe;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.API2;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.GlideUtil;
import com.pictureair.photopass.util.JsonTools;
import com.pictureair.photopass.util.JsonUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.util.SettingUtil;
import com.pictureair.photopass.widget.BannerView;
import com.pictureair.photopass.widget.CustomTextView;
import com.pictureair.photopass.widget.NoNetWorkOrNoCountView;
import com.pictureair.photopass.widget.PPPPop;
import com.pictureair.photopass.widget.PWToast;
import com.pictureair.photopass.widget.StoryRecycleDividerItemDecoration;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * PhotoPass照片的图片墙，用来显示从服务器返回的照片信息，以及通过magic相机拍摄的图片
 * 可以左右滑动切换不同的相册
 * 可以下拉刷新，获取更多的图片信息
 */
public class FragmentPageStory extends BaseFragment implements OnClickListener, DailyPPCardRecycleAdapter.OnRecyclerViewItemClickListener {
    //声明静态变量
    private static final int REFRESH = 666;
    private static final int SORT_COMPLETED_ALL = 223;
    private static final int LOAD_PHOTO_FROM_DB = 1003;
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
    private RelativeLayout noPhotoTipRL;
    private RelativeLayout noPhotoViewRL;
    private ImageView noPhotoViewRLCloseIV;
    private RelativeLayout storyNoPpToScanLinearLayout;
    private NoNetWorkOrNoCountView noNetWorkOrNoCountView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PPPPop pppPop;
    private CustomTextView specialDealOnTV;
    private CustomTextView specialDealDetailTV;
    private LinearLayout specialDealLL;
    private BannerView bannerView;
    private RelativeLayout gifRL, scanRL, draftLayout;
    private ImageView gifIV;
    private RecyclerView noPhotoRV, dailyPPCardRV;
    private NoPhotoRecycleAdapter noPhotoRecycleAdapter;
    private DailyPPCardRecycleAdapter dailyPPCardRecycleAdapter;

    //申明类
    private MyApplication app;

    private ArrayList<DailyPPCardInfo> dailyPPCardInfoArrayList = new ArrayList<>();

    private ArrayList<DiscoverLocationItemInfo> locationList = new ArrayList<>();
    private ArrayList<PPinfo> pPinfoArrayList = new ArrayList<>();
    private Activity context;
    private String userId;
    private PWToast myToast;
    private DealingInfo dealingInfo;

    /**
     * 同步已经购买的照片
     */
    private boolean syncingBoughtPhotos = false;

    private SettingUtil settingUtil;

    private boolean mIsAskStoragePermission = false;

    private ImageView img_float_hide;
    private FloatingActionButton fab;

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
            case API1.GET_AD_LOCATIONS_SUCCESS://获取广告地点成功
                PictureAirLog.out("ad location---->" + msg.obj.toString());
                /**
                 * 1.存入数据库
                 * 2.在application中记录结果
                 */
                JSONObject adJsonObject = (JSONObject) msg.obj;
                PictureAirDbManager.insertADLocations(adJsonObject.getJSONArray("locations"));
                app.setGetADLocationSuccess(true);
                break;

            case LOAD_PHOTO_FROM_DB:
                if (dailyPPCardInfoArrayList.size() == 0 || needfresh) {
                    //数据为0，需要从网上下载
                    PictureAirLog.out("photolist size = 0");
                    //判断是否之前有成功获取过
                    PictureAirLog.out("story flow ---> start get photo from net");
                    getLocationPhotos(false);
                } else {
                    //有数据，直接显示
                    fragmentPageStoryHandler.sendEmptyMessage(SORT_COMPLETED_ALL);
                }
                break;

            case REFRESH_ALL_PHOTOS:
                if (!hasHidden) {
                    showPWProgressDialog();
                    getLocationPhotos(true);
                    API1.getPPSByUserId(fragmentPageStoryHandler);

                } else {
                    fragmentPageStoryHandler.sendEmptyMessageDelayed(REFRESH_ALL_PHOTOS, 500);
                }
                break;

            case API1.GET_SOCKET_DATA_SUCCESS://手动刷新成功
                //获取推送成功，后面逻辑按照之前走
                PictureAirLog.e(TAG, "GET_SOCKET_DATA_SUCCESS: " + msg.obj.toString());
                JSONObject jsonObject = (JSONObject) msg.obj;
                if (jsonObject.size() > 0) {
                    JsonUtil.dealGetSocketData(MyApplication.getInstance().getApplicationContext(), jsonObject.toString(), true, null);
                }
                break;

            case REFRESH://开始刷新
                PictureAirLog.d(TAG, "the index of refreshing is " + msg.arg1);
                API1.getSocketData(fragmentPageStoryHandler);//手动拉取socket信息
                API1.getPPSByUserId(fragmentPageStoryHandler);
                getLocationPhotos(true);
                showLeadView();
                break;

            case SORT_COMPLETED_ALL:
                PictureAirLog.out("story flow ---> sort data done");
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

            case API1.GET_PPS_SUCCESS:// 获取pp列表成功
                pPinfoArrayList.clear();
                pPinfoArrayList.addAll(JsonUtil.getPPList((JSONObject) msg.obj));
                if (noPhotoViewRL.isShown()) {//有卡无图页面，需要刷新列表
                    if (noPhotoRecycleAdapter != null) {
                        noPhotoRecycleAdapter.notifyDataSetChanged();
                    }
                }
                //通知首页更新PP列表
                EventBus.getDefault().post(new MainTabSwitchEvent(MainTabSwitchEvent.DRAGER_VIEW_UPDATE, pPinfoArrayList));
                break;

            case API1.GET_PPS_FAILED:// 获取pp列表失败, 不做任何操作
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
     * 获取一卡一天的数据列表
     * @param isRefresh
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
                .subscribe(new RxSubscribe<ArrayList<DailyPPCardInfo>>() {
                    @Override
                    public void _onNext(ArrayList<DailyPPCardInfo> strings) {
                        dailyPPCardInfoArrayList.clear();
                        dailyPPCardInfoArrayList.addAll(strings);
                    }

                    @Override
                    public void _onError(int status) {
                        finishLoad(!isRefresh);
                    }

                    @Override
                    public void onCompleted() {
                        //清空推送消息的数量
                        app.setPushPhotoCount(0);

                        if (isRefresh) {
                            SPUtils.put(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, "photoCount", 0);

                            //刷新广告地点
                            app.setGetADLocationSuccess(false);
                            API1.getADLocations(0, fragmentPageStoryHandler);

                            showViewPager();
                            if (swipeRefreshLayout.isRefreshing()) {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                            dismissPWProgressDialog();
                        } else {
                            //获取广告信息
                            if (!app.isGetADLocationSuccess()) {
                                PictureAirLog.out("start get ad location");
                                API1.getADLocations(0, fragmentPageStoryHandler);
                            } else {
                                PictureAirLog.out("ad location has got already");
                            }

                            fragmentPageStoryHandler.sendEmptyMessage(SORT_COMPLETED_ALL);
                        }

                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PictureAirLog.out("story flow ---> on create----->story");
        context = getActivity();
        View view = inflater.inflate(R.layout.fragment_story, null);
        //获取控件
        scanIv = (ImageView) view.findViewById(R.id.story_menu_iv);
        scanLayout = (RelativeLayout) view.findViewById(R.id.story_menu_rl);
        menuLayout = (RelativeLayout) view.findViewById(R.id.story_drawer_rl);
        storyNoPpToScanLinearLayout = (RelativeLayout) view.findViewById(R.id.story_no_pp_to_scan);
        noNetWorkOrNoCountView = (NoNetWorkOrNoCountView) view.findViewById(R.id.storyNoNetWorkView);
        noPhotoTipRL = (RelativeLayout) view.findViewById(R.id.story_no_photo_rl);
        noPhotoViewRL = (RelativeLayout) view.findViewById(R.id.no_photo_view_relativelayout);
        noPhotoViewRLCloseIV = (ImageView) view.findViewById(R.id.story_no_photo_close_iv);
        noPhotoRV = (RecyclerView) view.findViewById(R.id.story_no_photo_rv);
        dailyPPCardRV = (RecyclerView) view.findViewById(R.id.story_fragment_rv);
        specialDealLL = (LinearLayout) view.findViewById(R.id.special_deal_ll);
        specialDealDetailTV = (CustomTextView) view.findViewById(R.id.special_deal_detail_tv);
        specialDealOnTV = (CustomTextView) view.findViewById(R.id.special_deal_on);
        bannerView = (BannerView) view.findViewById(R.id.story_banner);
        bannerView.setPhotos(JsonUtil.getBannerPhotosList(context, null));
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.story_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                PictureAirLog.out("start refresh");
                Message message = fragmentPageStoryHandler.obtainMessage();
                message.what = REFRESH;
                fragmentPageStoryHandler.sendMessage(message);
            }
        });
        gifRL = (RelativeLayout) view.findViewById(R.id.story_gif_rl);
        gifIV = (ImageView) view.findViewById(R.id.story_gif_iv);
        scanRL = (RelativeLayout) view.findViewById(R.id.story_scan_rl);
        gifRL.setOnClickListener(this);
        scanRL.setOnClickListener(this);

        draftLayout= (RelativeLayout) view.findViewById(R.id.draft_layout);
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
        settingUtil = new SettingUtil();
        app = (MyApplication) context.getApplication();
        PictureAirLog.out("current tap---->" + app.fragmentStoryLastSelectedTab);
        userId = SPUtils.getString(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ID, "");

        scanLayout.setOnClickListener(this);
        menuLayout.setOnClickListener(this);
        specialDealLL.setOnClickListener(this);
        noPhotoViewRLCloseIV.setOnClickListener(this);
        specialDealOnTV.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
        //初始化数据
        GlideUtil.loadGifAsImage(context, GlideUtil.getDrawableUrl(context, R.drawable.story_pp_intro), gifIV);
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
        //获取API
        PictureAirLog.out("story flow ---> get location info");
        Observable.concat(acache, API2.getLocationInfo(context, app.getTokenId())
                .map(new Func1<JSONObject, JSONObject>() {
                    @Override
                    public JSONObject call(JSONObject jsonObject) {
                        ACache.get(context).put(Common.DISCOVER_LOCATION, jsonObject.toString());
                        return jsonObject;
                    }
                }))
                .first()
                .subscribeOn(Schedulers.io())

                .compose(this.<JSONObject>bindToLifecycle())
                .map(new Func1<JSONObject, ArrayList<DiscoverLocationItemInfo>>() {
                    @Override
                    public ArrayList<DiscoverLocationItemInfo> call(JSONObject jsonObject) {
                        PictureAirLog.d(TAG, "story flow ---> get location success" + jsonObject.toString());
                        locationList.clear();
                        locationList.addAll(AppUtil.getLocation(context, jsonObject.toString(), true));
                        return locationList;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<ArrayList<DiscoverLocationItemInfo>>() {
                    @Override
                    public void _onNext(ArrayList<DiscoverLocationItemInfo> discoverLocationItemInfos) {

                    }

                    @Override
                    public void _onError(int status) {
                        finishLoad(true);
                    }

                    @Override
                    public void onCompleted() {
                        API1.getPPSByUserId(fragmentPageStoryHandler);
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
                                    ArrayList<JsonInfo> jsonInfos = new ArrayList<>();
                                    jsonInfos.addAll(PictureAirDbManager.getJsonInfos(JsonInfo.JSON_LOCATION_PHOTO_TYPE));

                                    dailyPPCardInfoArrayList.addAll(JsonUtil.getDailyPPCardInfoList(jsonInfos, locationList, MyApplication.getInstance().getLanguageType()));
                                    fragmentPageStoryHandler.sendEmptyMessage(LOAD_PHOTO_FROM_DB);
                                }
                            }.start();
                        } else {
                            fragmentPageStoryHandler.sendEmptyMessage(LOAD_PHOTO_FROM_DB);
                        }
                    }
                });

        return view;
    }

    private Observable<JSONObject> acache = Observable.create(new Observable.OnSubscribe<JSONObject>() {
        @Override
        public void call(Subscriber<? super JSONObject> subscriber) {
            String locationInfo = ACache.get(getActivity()).getAsString(Common.DISCOVER_LOCATION);
            if (!TextUtils.isEmpty(locationInfo)) {
                subscriber.onNext(JSONObject.parseObject(locationInfo));
            } else {
                subscriber.onCompleted();
            }
        }
    });

    /**
     * 控制控件的隐藏或者显示
     */
    private void showViewPager() {
        PictureAirLog.out("story flow ---> show view");
        if (dailyPPCardInfoArrayList != null && dailyPPCardInfoArrayList.size() > 0) {//有图片
            PictureAirLog.out("viewpager---->has photos");
            //隐藏没有pp的情况
            storyNoPpToScanLinearLayout.setVisibility(View.GONE);
            //隐藏空图的情况
            noPhotoViewRL.setVisibility(View.GONE);
            //显示有pp的情况
            dailyPPCardRV.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setEnabled(true);

            if (dailyPPCardRecycleAdapter == null) {
                GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2);
                dailyPPCardRecycleAdapter = new DailyPPCardRecycleAdapter(context, dailyPPCardInfoArrayList);
                dailyPPCardRecycleAdapter.setOnItemClickListener(this);
                dailyPPCardRV.addItemDecoration(new StoryRecycleDividerItemDecoration(ScreenUtil.dip2px(context, 2)));
                dailyPPCardRV.setLayoutManager(gridLayoutManager);
                dailyPPCardRV.setAdapter(dailyPPCardRecycleAdapter);
            } else {
                dailyPPCardRecycleAdapter.notifyDataSetChanged();
            }

            if (app.getPushPhotoCount() + app.getPushViedoCount() == 0){
                PictureAirLog.out("need gone the badgeview");
                PictureAirLog.out("photocount---->" + app.getPushPhotoCount());
                PictureAirLog.out("video count---->" + app.getPushViedoCount());
                EventBus.getDefault().post(new RedPointControlEvent(false));
            }
        } else {//没有图片
            swipeRefreshLayout.setEnabled(false);
            //判断是否应该显示左上角红点
            if (SPUtils.getInt(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.PP_COUNT, 0) < 2) {//没有扫描过
                PictureAirLog.out("viewpager---->has not scan pp");
                //获取banner数据
                API2.getBannerPhotos(MyApplication.getTokenId())
                        .map(new Func1<JSONObject, ArrayList<String>>() {

                            @Override
                            public ArrayList<String> call(JSONObject jsonObject) {
                                return JsonUtil.getBannerPhotosList(context, jsonObject);
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new RxSubscribe<ArrayList<String>>() {
                            @Override
                            public void _onNext(ArrayList<String> strings) {
                                bannerView.setPhotos(strings);

                            }

                            @Override
                            public void _onError(int status) {
                                bannerView.bannerStartPlay();

                            }

                            @Override
                            public void onCompleted() {
                                bannerView.bannerStartPlay();

                            }
                        });
                //显示没有pp的情况
                storyNoPpToScanLinearLayout.setVisibility(View.VISIBLE);
                noPhotoViewRL.setVisibility(View.GONE);

            } else {//有扫描过
                PictureAirLog.out("viewpager---->no photos");
                storyNoPpToScanLinearLayout.setVisibility(View.GONE);

                //显示空图的情况
                noPhotoViewRL.setVisibility(View.VISIBLE);

                if (noPhotoRecycleAdapter == null) {
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                    noPhotoRecycleAdapter = new NoPhotoRecycleAdapter(context, pPinfoArrayList);
                    noPhotoRV.setLayoutManager(linearLayoutManager);
                    noPhotoRV.setAdapter(noPhotoRecycleAdapter);
                } else {
                    noPhotoRecycleAdapter.notifyDataSetChanged();
                }
            }
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

    @Override
    public void onResume() {
        super.onResume();
        if (hasHidden) {
            PictureAirLog.out("bu ke jian");
            return;
        }
        PictureAirLog.out(TAG + "truely onresume---->story");
        if (bannerView != null) {
            bannerView.bannerStartPlay();
        }

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
            API1.getPPSByUserId(fragmentPageStoryHandler);
            EventBus.getDefault().post(new RedPointControlEvent(false));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (hasHidden) {
            PictureAirLog.out("bu ke jian");
            return;
        }
        if (bannerView != null) {
            bannerView.bannerStopPlay();
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

            //侧边栏按钮
            case R.id.story_drawer_rl:
                EventBus.getDefault().post(new MainTabSwitchEvent(MainTabSwitchEvent.DRAGER_VIEW));
                break;

            case R.id.story_scan_rl:
                i = new Intent(context, MipCaptureActivity.class);
                startActivity(i);
                break;

            case R.id.special_deal_ll:
                //抢单点击事件，即可进入抢单活动页面
                PictureAirLog.d("deal url---> " + dealingInfo.getDealingUrl() + "tokenid-->" + MyApplication.getTokenId());
                i = new Intent(context, PanicBuyActivity.class);
                i.putExtra("dealingInfo", dealingInfo);
                startActivity(i);
                break;

            case R.id.story_gif_rl:
                //进入gif播放页面
                i = new Intent(context, GifPlayActivity.class);
                startActivity(i);
                context.overridePendingTransition(R.anim.activity_fadein, R.anim.activity_fadeout);
                break;

            case R.id.fab:

                Intent intent = new Intent(context, OpinionsActivity.class);
                startActivity(intent);
                break;

            case R.id.float_hide:
                draftLayout.setVisibility(View.GONE);
            case R.id.story_no_photo_close_iv:
                noPhotoTipRL.setVisibility(View.GONE);
                break;

            default:
                break;
        }

    }

    @Override
    public void downloadClick(int position) {
        PictureAirLog.d("download click-->" + " :" + position);

    }

    @Override
    public void buyClick(int position) {
        showPWProgressDialog();
        //获取商品（以后从缓存中取）
        getGoods();

    }

    @Override
    public void previewClick(int position) {
        PictureAirLog.d("preview click-->" + position);

    }

    @Override
    public void itemClick(int position) {
        //进入相册
        Intent i = new Intent(context, EditStoryAlbumActivity.class);
        i.putExtra("ppCode", dailyPPCardInfoArrayList.get(position).getPpCode());
        i.putExtra("shootDate", dailyPPCardInfoArrayList.get(position).getShootDate());
        i.putExtra("activated", dailyPPCardInfoArrayList.get(position).getActivated());
        i.putExtra("photoCount", dailyPPCardInfoArrayList.get(position).getPhotoCount());
        startActivity(i);
    }

    private void clickToRefresh() {
        PictureAirLog.out("do refresh when noPhotoView is showing");
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setEnabled(true);
            swipeRefreshLayout.setRefreshing(true);
            Message message = fragmentPageStoryHandler.obtainMessage();
            message.what = REFRESH;
            fragmentPageStoryHandler.sendMessage(message);
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

            } else if (!noPhotoViewRL.isShown() && !syncingBoughtPhotos) {//延迟2秒，防止多次执行导致app异常
                syncingBoughtPhotos = true;
                PictureAirLog.out("start sync------->");
                fragmentPageStoryHandler.sendEmptyMessageDelayed(REFRESH_ALL_PHOTOS, 2000);

            } else {
                PictureAirLog.out("still waiting sync");
            }
            //刷新列表
            EventBus.getDefault().removeStickyEvent(socketEvent);
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
