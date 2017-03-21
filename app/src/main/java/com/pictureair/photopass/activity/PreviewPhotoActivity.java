package com.pictureair.photopass.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.GalleryWidget.GalleryViewPager;
import com.pictureair.photopass.GalleryWidget.PhotoEventListener;
import com.pictureair.photopass.GalleryWidget.UrlPagerAdapter;
import com.pictureair.photopass.GalleryWidget.UrlTouchImageView;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.controller.GetLastestVideoInfoBiz;
import com.pictureair.photopass.controller.GetLastestVideoInfoContract;
import com.pictureair.photopass.controller.GetLastestVideoInfoPresenter;
import com.pictureair.photopass.customDialog.PWDialog;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.entity.CartItemInfoJson;
import com.pictureair.photopass.entity.CartPhotosInfo;
import com.pictureair.photopass.entity.DiscoverLocationItemInfo;
import com.pictureair.photopass.entity.GoodsInfo;
import com.pictureair.photopass.entity.GoodsInfoJson;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.greendao.PictureAirDbManager;
import com.pictureair.photopass.http.rxhttp.RxSubscribe;
import com.pictureair.photopass.service.DownloadService;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.API2;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.JsonTools;
import com.pictureair.photopass.util.JsonUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.util.SettingUtil;
import com.pictureair.photopass.util.UmengUtil;
import com.pictureair.photopass.widget.NoNetWorkOrNoCountView;
import com.pictureair.photopass.widget.PWToast;
import com.pictureair.photopass.widget.SharePop;
import com.trello.rxlifecycle.android.ActivityEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 预览图片，可以进行编辑，分享，下载和制作礼物的操作
 *
 * @author bauer_bao
 */
public class PreviewPhotoActivity extends BaseActivity implements OnClickListener, Handler.Callback,
        PWDialog.OnPWDialogClickListener, PhotoEventListener, GetLastestVideoInfoContract.View {
    private SettingUtil settingUtil;

    private TextView locationTextView;

    private GalleryViewPager mViewPager;
    private UrlPagerAdapter pagerAdapter;
    private ImageView returnImageView;
    private ImageButton moreImgBtn;

    private PWToast newToast;
    private SharePop sharePop;
    private boolean isShareDialogShowing = false;
    private MyApplication myApplication;
    private PhotoInfo photoInfo;
    private BottomSheetDialog sheetDialog;
    private View editRootView, buyPhotoRootView;
    private BottomSheetBehavior bottomSheetBehavior;

    private RelativeLayout titleBar;
    private static final String TAG = "PreviewPhotoActivity";

    private int shareType = 0;

    private int sheetDialogType;

    //图片显示框架
    private ArrayList<PhotoInfo> photolist;
    private ArrayList<DiscoverLocationItemInfo> locationList = new ArrayList<>();
    private int currentPosition;//记录当前预览照片的索引值

    private String tabName, currentPPCode;

    /**
     * 模糊图购买对话框控件
     */
    private TextView buyPhotoPriceTV, buyPhotoIntroTV, buyPPPPriceTV, buyPPPIntroTV, buyPhotoNameTV, buyPPPNameTV;
    private RelativeLayout buyPhotoRL, buyPPPRL, usePPPRL, useDailyPPPRL;

    /**
     * 是否是横屏模式
     */
    private boolean isLandscape = false;

    private RelativeLayout photoFraRelativeLayout;

    private TextView editTV, shareTV, downloadTV, makeGiftTV;

    private static final int GET_LOCATION_AD = 777;
    private static final int NO_PHOTOS_AND_RETURN = 1002;

    private static final int LOCAL_PHOTO_EDIT_DIALOG = 1003;
    private static final int FRAME_PHOTO_EDIT_DIALOG = 1004;
    private static final int GO_SETTING_DIALOG = 1005;
    private static final int DOWNLOAD_DIALOG = 1006;
    private static final int GO_DOWNLOAD_ACTIVITY_DIALOG = 1007;
    private static final int VIDEO_STILL_MAKING_DIALOG = 1008;
    private static final int BUY_BLUR_PHOTO_SHEET_DIALOG = 1009;
    private static final int EDIT_PHOTO_SHEET_DIALOG = 1010;
    private static final int SAVE_PHOTO_TIP_DIALOG = 1011;

    private PWDialog pictureWorksDialog;

    private List<GoodsInfo> allGoodsList = new ArrayList<>();
    private GoodsInfo pppGoodsInfo;
    private String[] photoUrls;
    private String cartId = null;

    private Handler previewPhotoHandler;

    //点击视频播放的处理对象
    private GetLastestVideoInfoPresenter lastestVideoInfoPresenter;

    /**是否为预览纪念照的状态*/
    private boolean isSouvenir;
    private NoNetWorkOrNoCountView netWorkOrNoCountView;
    /**没有纪念照时的布局*/
    private LinearLayout noSouvenirLayout;

    /**
     * 处理Message
     *
     * @param msg
     */
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case SharePop.TWITTER:
                shareType = msg.what;
                break;

            case SharePop.DISMISS_DIALOG:
                isShareDialogShowing = false;
                dismissPWProgressDialog();
                break;

            case SharePop.SHOW_DIALOG:
                isShareDialogShowing = true;
                showPWProgressDialog(null);
                break;

            case 7://操作比较耗时，会影响oncreate绘制
                mViewPager = (GalleryViewPager) findViewById(R.id.viewer);
                pagerAdapter = new UrlPagerAdapter(PreviewPhotoActivity.this, photolist, 0, true);
                pagerAdapter.setOnPhotoEventListener(this);
                mViewPager.setOffscreenPageLimit(2);
                mViewPager.setAdapter(pagerAdapter);
                mViewPager.setCurrentItem(currentPosition, true);
                //初始化底部索引按钮
                updateIndexTools();

                mViewPager.addOnPageChangeListener(new OnPageChangeListener() {

                    @Override
                    public void onPageSelected(int arg0) {
                        //初始化每张图片的love图标
                        PictureAirLog.v(TAG, "----------------------->initing...4");
                        currentPosition = arg0;
                    }

                    @Override
                    public void onPageScrolled(int arg0, float arg1, int arg2) {
                    }

                    @Override
                    public void onPageScrollStateChanged(int arg0) {
                        // TODO Auto-generated method stub

                        PictureAirLog.v(TAG, "----------------------->initing...5");
                        if (arg0 == 0) {//结束滑动
                            updateIndexTools();//只能写在这里，不能写在onPageSelected，不然出现切换回来之后，显示错乱
                            setUmengPhotoSlide();//统计滑动图片次数
                        }
                    }
                });
                break;

            case GET_LOCATION_AD:
                PictureAirLog.d("start get location info from net");
                final int oldPositon = msg.arg1;
                if (myApplication.isGetADLocationSuccess()) {
                    //从数据库中查找
                    Observable.just(photoInfo.getLocationId())
                            .subscribeOn(Schedulers.io())
                            .map(new Func1<String, String>() {

                                @Override
                                public String call(String str) {
                                    return PictureAirDbManager.getADByLocationId(str, MyApplication.getInstance().getLanguageType());

                                }
                            })
                            .observeOn(AndroidSchedulers.mainThread())
                            .compose(this.<String>bindToLifecycle())
                            .subscribe(new Subscriber<String>() {
                                @Override
                                public void onCompleted() {

                                }

                                @Override
                                public void onError(Throwable e) {

                                }

                                @Override
                                public void onNext(String s) {
                                    setADtext(s, oldPositon);
                                }
                            });

                } else {
                    //从网络获取
                    API2.getADLocations()
                            .map(new Func1<JSONObject, String>() {
                                @Override
                                public String call(JSONObject jsonObject) {
                                    PictureAirLog.out("ad location----> " + jsonObject.toString());
                                    /**
                                     * 1.存入数据库
                                     * 2.在application中记录结果
                                     */
                                    String adString = PictureAirDbManager.insertADLocations(jsonObject.getJSONArray("locations"),
                                            photoInfo.getLocationId(), MyApplication.getInstance().getLanguageType());
                                    myApplication.setGetADLocationSuccess(true);
                                    return adString;
                                }
                            })
                            .observeOn(AndroidSchedulers.mainThread())
                            .compose(this.<String>bindToLifecycle())
                            .subscribe(new RxSubscribe<String>() {
                                @Override
                                public void _onNext(String str) {
                                    setADtext(str, oldPositon);
                                }

                                @Override
                                public void _onError(int status) {

                                }

                                @Override
                                public void onCompleted() {

                                }
                            });
                }
                dismissPWProgressDialog();
                break;

            case NO_PHOTOS_AND_RETURN://没有图片
                dismissPWProgressDialog();
                finish();
                break;

            case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_RELOAD:
                getSouvenirPhotos();
                break;

            default:
                break;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_photo);
        PictureAirLog.out("oncreate start----");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        init();//初始化UI
        PictureAirLog.out("oncreate finish----");
    }

    private void init() {
        // TODO Auto-generated method stub
        pictureWorksDialog = new PWDialog(this)
                .setOnPWDialogClickListener(this)
                .pwDialogCreate();
        sheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogStyle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//解决弹出对话框之后，状态栏的沉浸式效果消失了
            sheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        previewPhotoHandler = new Handler(this);
        settingUtil = new SettingUtil();
        newToast = new PWToast(this);
        sharePop = new SharePop(this);
        PictureAirLog.out("oncreate----->2");
        returnImageView = (ImageView) findViewById(R.id.button1_shop_rt);
        moreImgBtn = (ImageButton) findViewById(R.id.preview_more);
        locationTextView = (TextView) findViewById(R.id.preview_location);
        photoFraRelativeLayout = (RelativeLayout) findViewById(R.id.fra_layout);
        titleBar = (RelativeLayout) findViewById(R.id.preview_titlebar);
        noSouvenirLayout = (LinearLayout) findViewById(R.id.preivew_no_souvenir_layout);
        netWorkOrNoCountView = (NoNetWorkOrNoCountView) findViewById(R.id.nonetwork_view);

        myApplication = (MyApplication) getApplication();

        returnImageView.setOnClickListener(this);
        moreImgBtn.setOnClickListener(this);

        Configuration cf = getResources().getConfiguration();
        int ori = cf.orientation;
        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
            isLandscape = true;
            landscapeOrientation();
        }

        isSouvenir = getIntent().getBooleanExtra("souvenir", false);
        showPWProgressDialog(R.string.is_loading);
        if (!isSouvenir) {
            getPreviewPhotos();
        } else {
            getSouvenirPhotos();
        }
    }

    /**
     * 设置广告文案
     * @param str
     * @param oldP
     */
    private void setADtext(String str, int oldP){
        if (oldP == currentPosition) {
            //如果获取的对应索引值，依旧是当期的索引值，则显示广告
            UrlTouchImageView imageView = (UrlTouchImageView)mViewPager.findViewById(currentPosition);
            if (imageView != null) {
                imageView.setADText(str);
            }
        }
    }

    /**
     *
     * 获取纪念照
     * 如果没有纪念照则显示没有照片的页面
     * 如果有纪念照则保存一次照片数据，后续如果网络获取纪念照失败，则直接读取保存的数据
     * */
    private void getSouvenirPhotos() {
        String userPPCode = SPUtils.getString(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_USER_PP, "");
        API2.getSouvenirPhotos(MyApplication.getTokenId(), userPPCode)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        if (jsonObject != null) {
                            JSONArray responseArray = jsonObject.getJSONArray("photos");
                            photolist = new ArrayList<>();
                            if (responseArray.size() > 0) {
                                ACache.get(PreviewPhotoActivity.this).put(Common.SOUVENIR, responseArray);
                                noSouvenirLayout.setVisibility(View.GONE);
                                netWorkOrNoCountView.setVisibility(View.GONE);
                                photoFraRelativeLayout.setVisibility(View.VISIBLE);
                                for (int i = 0; i < responseArray.size(); i++) {
                                    PhotoInfo photoInfo = JsonUtil.getPhoto(responseArray.getJSONObject(i));
                                    if (photoInfo != null) {
                                        photoInfo.setId(1L);
                                        photolist.add(photoInfo);
                                    }
                                }
                                currentPosition = 0;
                                previewPhotoHandler.sendEmptyMessage(7);
                            } else {

                                netWorkOrNoCountView.setVisibility(View.GONE);
                                photoFraRelativeLayout.setVisibility(View.GONE);
                                noSouvenirLayout.setVisibility(View.VISIBLE);
                                locationTextView.setText(R.string.souvenir_photo);
                                dismissPWProgressDialog();
                            }
                        } else {
                            netWorkOrNoCountView.setVisibility(View.GONE);
                            photoFraRelativeLayout.setVisibility(View.GONE);
                            noSouvenirLayout.setVisibility(View.VISIBLE);
                            locationTextView.setText(R.string.souvenir_photo);
                            dismissPWProgressDialog();
                        }
                    }

                    @Override
                    public void _onError(int status) {
                        Object object = ACache.get(PreviewPhotoActivity.this).getAsObject(Common.SOUVENIR);
                        if (object != null) {
                            if (object instanceof JSONArray) {
                                photolist = new ArrayList<>();
                                noSouvenirLayout.setVisibility(View.GONE);
                                netWorkOrNoCountView.setVisibility(View.GONE);
                                photoFraRelativeLayout.setVisibility(View.VISIBLE);
                                JSONArray jsonArray = (JSONArray) object;
                                for (int i = 0; i < jsonArray.size(); i++) {
                                    PhotoInfo photoInfo = JsonUtil.getPhoto(jsonArray.getJSONObject(i));
                                    if (photoInfo != null) {
                                        photoInfo.setId(1L);
                                        photolist.add(photoInfo);
                                    }
                                }
                                currentPosition = 0;
                                previewPhotoHandler.sendEmptyMessage(7);
                            }
                        } else {
                            noSouvenirLayout.setVisibility(View.GONE);
                            photoFraRelativeLayout.setVisibility(View.GONE);
                            netWorkOrNoCountView.setVisibility(View.VISIBLE);
                            locationTextView.setText(R.string.souvenir_photo);
                            netWorkOrNoCountView.setResult(R.string.no_network, R.string.click_button_reload, R.string.reload, R.drawable.no_network, previewPhotoHandler, true);
                            dismissPWProgressDialog();
                        }
                    }

                    @Override
                    public void onCompleted() {

                    }
                });//获取全部图片

    }

    private void getPreviewPhotos() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                //获取intent传递过来的信息
                photolist = new ArrayList<>();
                Bundle bundle = getIntent().getBundleExtra("bundle");
                currentPosition = bundle.getInt("position", 0);
                PictureAirLog.out("currentposition---->" + currentPosition);
                tabName = bundle.getString("tab");
                PictureAirLog.out("tabName--->" + tabName);
                if (tabName.equals("editStory")){//编辑PP照片页面
                    currentPPCode = bundle.getString("ppCode");
                    String shootDate = bundle.getString("shootDate");
                    locationList.addAll(AppUtil.getLocation(PreviewPhotoActivity.this, ACache.get(PreviewPhotoActivity.this).getAsString(Common.DISCOVER_LOCATION), true));
                    photolist.addAll(AppUtil.insertSortFavouritePhotos(
                            PictureAirDbManager.getPhotoInfosByPPCode(currentPPCode, shootDate, locationList, MyApplication.getInstance().getLanguageType()), false));

                } else {//获取列表图片， other，不需要根据photoid重新找到地点
                    ArrayList<PhotoInfo> temp = bundle.getParcelableArrayList("photos");//获取图片路径list
                    if (temp != null) {
                        photolist.addAll(temp);
                    }
                }

                if (currentPosition == -1) {//购买图片后返回
                    String photoId = bundle.getString("photoId", "");
                    PictureAirLog.out("photoid--->" + photoId);
                    for (int i = 0; i < photolist.size(); i++) {
                        PictureAirLog.out("photoinfo.photoid----->" + photolist.get(i).getPhotoId());
                        if (TextUtils.isEmpty(photolist.get(i).getPhotoId())) {//本地图片，没有PhotoId，需要过滤

                        } else if (photolist.get(i).getPhotoId().equals(photoId)){
                            photolist.get(i).setIsPaid(1);
                            currentPosition = i;
                            break;
                        }
                    }
                } else if (currentPosition == -2) {//绑定PP后返回
                    String ppsStr = bundle.getString("ppsStr");
                    refreshPP(photolist,ppsStr);
                    currentPosition = SPUtils.getInt(PreviewPhotoActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, "currentPosition",0);
                } else {//其他情况，需要根据photoid找到应对的position位置，再显示
                    if (tabName.equals("all") ||
                            tabName.equals("photopass") ||
                            tabName.equals("bought") ||
                            tabName.equals("editStory")) {
                        String photoId = bundle.getString("photoId", "");
                        for (int i = 0; i < photolist.size(); i++) {
                            if (photolist.get(i).getPhotoId().equals(photoId)){
                                currentPosition = i;
                                break;
                            }
                        }
                    }
                }

                if (currentPosition < 0) {
                    currentPosition = 0;
                }
                if (photolist.size() == 0) {
                    /**
                     * 图片为空的情况。
                     * 如果收到删除数据推送，本地数据处理完毕，
                     * 但是用户正好点进图片预览，这个时候会出现为空的情况，需要finish
                     */
                    PictureAirLog.out("no photos need return");
                    previewPhotoHandler.sendEmptyMessage(NO_PHOTOS_AND_RETURN);
                    return;
                }
                if (currentPosition > photolist.size() - 1) {//出现的情况就是，刚点进去，同步删除的处理将数据库删除了，需要取最后一个数据
                    currentPosition = photolist.size() - 1;
                }
                PhotoInfo currentPhotoInfo = photolist.get(currentPosition);

                PictureAirLog.out("photolist size ---->" + photolist.size());
                Iterator<PhotoInfo> photoInfoIterator = photolist.iterator();
                while (photoInfoIterator.hasNext()) {
                    PhotoInfo info = photoInfoIterator.next();
                    if (info.getIsVideo() == 1 && info.getIsPaid() == 0) {
                        photoInfoIterator.remove();
                    }
                }
                PictureAirLog.out("photolist size ---->" + photolist.size());
                PictureAirLog.out("currentPosition ---->" + currentPosition);
                currentPosition = photolist.indexOf(currentPhotoInfo);
                PictureAirLog.out("photoid--->" + photolist.get(currentPosition).getPhotoId());
                PictureAirLog.out("currentPosition ---->" + currentPosition);
                PictureAirLog.v(TAG, "photo size is " + photolist.size());
                PictureAirLog.v(TAG, "thumbnail is " + photolist.get(currentPosition).getPhotoThumbnail_128());
                PictureAirLog.v(TAG, "thumbnail 512 is " + photolist.get(currentPosition).getPhotoThumbnail_512());
                PictureAirLog.v(TAG, "thumbnail 1024 is " + photolist.get(currentPosition).getPhotoThumbnail_1024());
                PictureAirLog.v(TAG, "original is " + photolist.get(currentPosition).getPhotoOriginalURL());
                PictureAirLog.v(TAG, "----------------------->initing...2");
                previewPhotoHandler.sendEmptyMessage(7);
            }
        }.start();
    }

    /**
     * 准备开始弹出对话框
     * @param type
     */
    private void prepareShowSheetDialog(int type){
        if (type == BUY_BLUR_PHOTO_SHEET_DIALOG && allGoodsList.size() == 0) {//需要获取shop数据, 需要重新获取商品数据
            showPWProgressDialog();
            getGoods(type);
        } else {
            showSheetDialog(type);
        }
    }

    /**
     * 显示对话框
     * @param type
     */
    private void showSheetDialog(int type) {
        if (sheetDialogType != type) {//需要更新UI
            sheetDialogType = type;
            if (type == BUY_BLUR_PHOTO_SHEET_DIALOG) {//购买模糊图的弹框
                if (buyPhotoRootView == null) {
                    buyPhotoRootView = LayoutInflater.from(this).inflate(R.layout.dialog_preview_buy_blur, null);
                    buyPhotoRL = (RelativeLayout) buyPhotoRootView.findViewById(R.id.preview_blur_dialog_buy_photo_ll);
                    buyPhotoNameTV = (TextView) buyPhotoRootView.findViewById(R.id.preview_blur_dialog_buy_photo_tv);
                    buyPhotoIntroTV = (TextView) buyPhotoRootView.findViewById(R.id.preview_blur_dialog_buy_photo_intro_tv);
                    buyPhotoPriceTV = (TextView) buyPhotoRootView.findViewById(R.id.preview_blur_dialog_buy_photo_price_tv);
                    buyPPPRL = (RelativeLayout) buyPhotoRootView.findViewById(R.id.preview_blur_dialog_buy_ppp_ll);
                    buyPPPNameTV = (TextView) buyPhotoRootView.findViewById(R.id.preview_blur_dialog_buy_ppp_tv);
                    buyPPPIntroTV = (TextView) buyPhotoRootView.findViewById(R.id.preview_blur_dialog_buy_ppp_intro_tv);
                    buyPPPPriceTV = (TextView) buyPhotoRootView.findViewById(R.id.preview_blur_dialog_buy_ppp_price_tv);
                    usePPPRL = (RelativeLayout) buyPhotoRootView.findViewById(R.id.preview_blur_dialog_upgrade_photo_ll);
                    useDailyPPPRL = (RelativeLayout) buyPhotoRootView.findViewById(R.id.preview_blur_dialog_upgrade_daily_photo_ll);
                    buyPhotoRL.setOnClickListener(this);
                    buyPPPRL.setOnClickListener(this);
                    usePPPRL.setOnClickListener(this);
                    useDailyPPPRL.setOnClickListener(this);
                } else {//需要把view的父控件的子view全部清除，此处为什么是FrameLayout，是从源码得知
                    ((FrameLayout) buyPhotoRootView.getParent()).removeAllViews();
                }
                sheetDialog.setContentView(buyPhotoRootView);
                initSheetDialog();

            } else if (type == EDIT_PHOTO_SHEET_DIALOG) {//编辑清晰图的弹框
                if (editRootView == null) {
                    editRootView = View.inflate(this, R.layout.preview_photo_dialog, null);
                    editTV = (TextView) editRootView.findViewById(R.id.preview_edit);
                    shareTV = (TextView) editRootView.findViewById(R.id.preview_share);
                    downloadTV = (TextView) editRootView.findViewById(R.id.preview_download);
                    makeGiftTV = (TextView) editRootView.findViewById(R.id.preview_makegift);
                    editTV.setOnClickListener(this);
                    shareTV.setOnClickListener(this);
                    downloadTV.setOnClickListener(this);
                    makeGiftTV.setOnClickListener(this);
                } else {
                    ((FrameLayout) editRootView.getParent()).removeAllViews();
                }

                sheetDialog.setContentView(editRootView);

            }
        }
        if (type == EDIT_PHOTO_SHEET_DIALOG) {//如果是编辑对话框，需要区分视频和照片
            if (photoInfo.getIsVideo() == 1) {
                editTV.setVisibility(View.GONE);
                makeGiftTV.setVisibility(View.GONE);
            } else {
                editTV.setVisibility(View.VISIBLE);
                makeGiftTV.setVisibility(View.VISIBLE);
            }
        }
        if (bottomSheetBehavior == null) {
            //解决弹出之后，如果用手势把对话框消失，则再次弹出的时候，只有阴影，对话框不会弹出的问题
            View view1 = sheetDialog.getDelegate().findViewById(android.support.design.R.id.design_bottom_sheet);
            bottomSheetBehavior = BottomSheetBehavior.from(view1);
            bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        sheetDialog.dismiss();
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                }
            });
        }
        sheetDialog.show();
    }

    /**
     * 初始化数据
     */
    private void initSheetDialog() {
        for (GoodsInfo good: allGoodsList) {
            if (good.getName().equals(Common.GOOD_NAME_PPP)) {//ppp
                pppGoodsInfo = good;
                buyPPPNameTV.setText(good.getNameAlias());
                PictureAirLog.d("----> " + good.getPrice());
                buyPPPPriceTV.setText(Common.DEFAULT_CURRENCY + good.getPrice());
                buyPPPIntroTV.setText(good.getDescription());

            } else if (good.getName().equals(Common.GOOD_NAME_SINGLE_DIGITAL)) {//数码照片
                buyPhotoNameTV.setText(good.getNameAlias());
                PictureAirLog.d("----> " + good.getPrice());
                buyPhotoPriceTV.setText(Common.DEFAULT_CURRENCY + good.getPrice());
                buyPhotoIntroTV.setText(good.getDescription());

            }
        }
    }

    /**
     * 更新底部索引工具
     */
    private void updateIndexTools() {
        PictureAirLog.v(TAG, "updateIndexTools-------->" + currentPosition);
        //初始化图片收藏按钮，需要判断isLove=1或者是否在数据库中
        photoInfo = photolist.get(currentPosition);

        if (!isSouvenir) {

            //更新title地点名称
            locationTextView.setText(photoInfo.getLocationName());

            //如果是未购买图片，判断是否是第一次进入，如果是，则显示引导图层
            if (photoInfo.getIsPaid() == 0 && photoInfo.getIsOnLine() == 1) {//未购买的图片
                PictureAirLog.v(TAG, "need show blur view");
                dismissPWProgressDialog();
            } else if (photoInfo.getIsPaid() == 1 && photoInfo.getIsOnLine() == 1) {
                previewPhotoHandler.obtainMessage(GET_LOCATION_AD, currentPosition, 0).sendToTarget();
                PictureAirLog.out("set enable in get ad");
            } else {
                PictureAirLog.out("set enable in other conditions");
                dismissPWProgressDialog();
            }
        } else {
            locationTextView.setText(R.string.souvenir_photo);
            dismissPWProgressDialog();
        }

        if (isLandscape) {//横屏模式
            if (mViewPager != null) {
                mViewPager.setBackgroundColor(Color.BLACK);
            }
        }
    }

    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.preview_more:
                prepareShowSheetDialog(EDIT_PHOTO_SHEET_DIALOG);
                break;

            case R.id.button1_shop_rt:
                finish();
                break;

            case R.id.preview_edit://编辑
                if (sheetDialog.isShowing()) {
                    sheetDialog.dismiss();
                }
                if (photoInfo == null) {
                    return;
                }
                if (photoInfo.getIsOnLine() == 0) {
                    pictureWorksDialog.setPWDialogId(LOCAL_PHOTO_EDIT_DIALOG)
                            .setPWDialogMessage(R.string.local_photo_cannot_edit_content)
                            .setPWDialogNegativeButton(null)
                            .setPWDialogPositiveButton(R.string.photo_cannot_edit_yes)
                            .pwDilogShow();
                    return;
                }
                if (photoInfo.getIsPaid() == 1) {
                    if (photoInfo.getIsPreset() == 0) { // 如果没有模版，就去执行编辑操作。 如果有模版就弹出提示。
                        intent = new Intent(this, EditPhotoActivity.class);
                        intent.putExtra("photo", photolist.get(mViewPager.getCurrentItem()));
                        startActivityForResult(intent, 1);
                    } else {
                        pictureWorksDialog.setPWDialogId(FRAME_PHOTO_EDIT_DIALOG)
                                .setPWDialogMessage(R.string.photo_cannot_edit_content)
                                .setPWDialogNegativeButton(null)
                                .setPWDialogPositiveButton(R.string.photo_cannot_edit_yes)
                                .pwDilogShow();
                    }
                } else {
                    prepareShowSheetDialog(BUY_BLUR_PHOTO_SHEET_DIALOG);
                }
                break;

            case R.id.preview_share:
                if (sheetDialog.isShowing()) {
                    sheetDialog.dismiss();
                }
                if (photoInfo == null) {
                    return;
                }
                if (photoInfo.getIsPaid() == 1) {
                    if (sheetDialog.isShowing()) {
                        sheetDialog.dismiss();
                    }
                    if (mViewPager.getCurrentItem() >= photolist.size()) {
                        return;
                    }
                    PictureAirLog.v(TAG, "start share=" + photolist.get(mViewPager.getCurrentItem()).getPhotoOriginalURL());
                    sharePop.setShareInfo(photolist.get(mViewPager.getCurrentItem()), false, previewPhotoHandler);
                    sharePop.showAtLocation(v, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                } else {
                    prepareShowSheetDialog(BUY_BLUR_PHOTO_SHEET_DIALOG);
                }
                break;

            case R.id.preview_download://下载,如果不是pp的照片，提示不需要下载，如果是pp的照片，并且没有支付，提示购买，如果已经购买，如果没有下载，则下载，否则提示已经下载
                if (sheetDialog.isShowing()) {
                    sheetDialog.dismiss();
                }
                if (AppUtil.getNetWorkType(PreviewPhotoActivity.this) == AppUtil.NETWORKTYPE_INVALID) {
                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    return;
                }
                if (photoInfo == null) {
                    return;
                }
                if (photoInfo.getIsPaid() == 1) {
                    if (photoInfo.getIsOnLine() == 1) {//是pp的照片
                        judgeOnePhotoDownloadFlow();
                    } else {
                        newToast.setTextAndShow(R.string.neednotdownload, Common.TOAST_SHORT_TIME);
                    }

                } else {
                    prepareShowSheetDialog(BUY_BLUR_PHOTO_SHEET_DIALOG);
                }

                break;

            case R.id.preview_makegift:
                if (sheetDialog.isShowing()) {
                    sheetDialog.dismiss();
                }
                if (photoInfo == null) {
                    return;
                }

                if (photoInfo.getIsOnLine() == 0) {
                    newToast.setTextAndShow(R.string.local_photo_not_support_makegift, Common.TOAST_SHORT_TIME);
                    return;
                }

                if (photoInfo.getLocationId().equals("photoSouvenirs")) {//排除纪念照的照片
                    newToast.setTextAndShow(R.string.not_support_makegift, Common.TOAST_SHORT_TIME);
                    return;
                }

                if (AppUtil.getNetWorkType(PreviewPhotoActivity.this) == AppUtil.NETWORKTYPE_INVALID) {
                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    return;
                }

                PictureAirLog.v(TAG, "makegift");
                intent = new Intent(this, MakegiftActivity.class);
                intent.putExtra("selectPhoto", photolist.get(mViewPager.getCurrentItem()));
                startActivity(intent);
                if (sheetDialog.isShowing()) {
                    sheetDialog.dismiss();
                }
                break;

            case R.id.preview_blur_dialog_buy_photo_ll:
                if (sheetDialog.isShowing()) {
                    sheetDialog.dismiss();
                }
                if (AppUtil.getNetWorkType(PreviewPhotoActivity.this) == AppUtil.NETWORKTYPE_INVALID) {
                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    if (sheetDialog.isShowing()) {
                        sheetDialog.dismiss();
                    }
                    return;
                }
                if (photoInfo == null) {
                    return;
                }
                showPWProgressDialog();
                buyPhoto(photoInfo.getPhotoId());
                break;

            case R.id.preview_blur_dialog_buy_ppp_ll:
                if (sheetDialog.isShowing()) {
                    sheetDialog.dismiss();
                }
                //直接购买PP+
                if (AppUtil.getNetWorkType(PreviewPhotoActivity.this) == AppUtil.NETWORKTYPE_INVALID) {
                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    if (sheetDialog.isShowing()) {
                        sheetDialog.dismiss();
                    }
                    return;
                }
                showPWProgressDialog();
                //获取商品
                buyPPP();
                break;

            case R.id.preview_blur_dialog_upgrade_photo_ll:
                if (sheetDialog.isShowing()) {
                    sheetDialog.dismiss();
                }
                if (photoInfo == null) {
                    return;
                }
                if (AppUtil.getNetWorkType(PreviewPhotoActivity.this) == AppUtil.NETWORKTYPE_INVALID) { //判断网络情况。
                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    if (sheetDialog.isShowing()) {
                        sheetDialog.dismiss();
                    }
                    return;
                }else{
                    getPPPsByShootDate(photoInfo.getShootDate(), false);
                }
                break;

            case R.id.preview_blur_dialog_upgrade_daily_photo_ll:
                if (sheetDialog.isShowing()) {
                    sheetDialog.dismiss();
                }
                if (photoInfo == null) {
                    return;
                }
                if (AppUtil.getNetWorkType(PreviewPhotoActivity.this) == AppUtil.NETWORKTYPE_INVALID) { //判断网络情况。
                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    if (sheetDialog.isShowing()) {
                        sheetDialog.dismiss();
                    }
                    return;
                }else{
                    getPPPsByShootDate(photoInfo.getShootDate(), true);
                }
                break;

            default:
                break;
        }
    }

    /**
     * 购买照片
     * @param photoId
     */
    private void buyPhoto(String photoId) {
        API2.buyPhoto(photoId)
                .map(new Func1<JSONObject, CartItemInfoJson>() {
                    @Override
                    public CartItemInfoJson call(JSONObject jsonObject) {
                        return JsonTools.parseObject(jsonObject, CartItemInfoJson.class);//CartItemInfoJson.getString()
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<CartItemInfoJson>bindToLifecycle())
                .subscribe(new RxSubscribe<CartItemInfoJson>() {
                    @Override
                    public void _onNext(CartItemInfoJson cartItemInfoJson) {
                        dismissPWProgressDialog();
                        if (cartItemInfoJson.getItems().size() == 0) {
                            return;
                        }
                        PictureAirLog.v(TAG, "BUY_PHOTO_SUCCESS" + cartItemInfoJson.toString());
                        //将当前购买的照片信息存放到application中
                        myApplication.setIsBuyingPhotoInfo(photolist.get(currentPosition).getPhotoId(), tabName, null, null);
                        if (myApplication.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_MYPHOTOPASS)) {
                        } else if (myApplication.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_VIEWORSELECTACTIVITY)) {
                        } else {
                            myApplication.setRefreshViewAfterBuyBlurPhoto(Common.FROM_PREVIEW_PHOTO_ACTIVITY);
                        }
                        List<CartItemInfo> cartItemInfoList = cartItemInfoJson.getItems();
                        Intent intent = new Intent(PreviewPhotoActivity.this, SubmitOrderActivity.class);
                        ArrayList<CartItemInfo> orderinfo = new ArrayList<>();
                        CartItemInfo cartItemInfo = cartItemInfoList.get(0);
                        cartItemInfo.setCartProductType(2);
                        orderinfo.add(cartItemInfo);
                        int curCarts = SPUtils.getInt(PreviewPhotoActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
                        SPUtils.put(PreviewPhotoActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, curCarts + 1);
                        intent.putExtra("orderinfo", orderinfo);
                        startActivity(intent);
                    }

                    @Override
                    public void _onError(int status) {
                        //购买失败
                        dismissPWProgressDialog();
                        newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), status), Common.TOAST_SHORT_TIME);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    private void getPPPsByShootDate(String shootDate, final boolean isDaily) {
        API2.getPPPsByShootDate(shootDate)
                .compose(this.<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        API2.PPPlist = JsonUtil.getPPPSByUserIdNHavedPPP(jsonObject);
                        if (API2.PPPlist.size() > 0) {
                            //将 tabname 存入sp
                            SPUtils.put(PreviewPhotoActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, "tabName", tabName);
                            SPUtils.put(PreviewPhotoActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, "currentPosition", currentPosition);

                            if (sheetDialog.isShowing()) {
                                sheetDialog.dismiss();
                            }

                            JSONArray pps = new JSONArray();
                            JSONObject ppJB = new JSONObject();
                            ppJB.put("code", currentPPCode);
                            ppJB.put("bindDate", photoInfo.getShootDate());
                            pps.add(ppJB);

                            Intent intent = new Intent(PreviewPhotoActivity.this, MyPPPActivity.class);
                            intent.putExtra("ppsStr", pps.toString());
                            intent.putExtra("isUseHavedPPP", true);
                            intent.putExtra("dailyppp", isDaily);
                            startActivity(intent);
                        } else {
                            newToast.setTextAndShow(R.string.no_ppp_tips, Common.TOAST_SHORT_TIME);
                        }
                    }

                    @Override
                    public void _onError(int status) {
                        newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), status), Common.TOAST_SHORT_TIME);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    /**
     * 购买ppp
     */
    private void buyPPP() {
        photoUrls = new String[pppGoodsInfo.getPictures().size()];
        for (int i = 0; i < pppGoodsInfo.getPictures().size(); i++) {
            photoUrls[i] = pppGoodsInfo.getPictures().get(i).getUrl();
        }

        //加入购物车
        API2.addToCart(pppGoodsInfo.getGoodsKey(), 1, true, null)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<JSONObject>bindToLifecycle())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        PictureAirLog.d("add to cart success--> " + jsonObject);
                        int currentCartCount = SPUtils.getInt(PreviewPhotoActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
                        SPUtils.put(PreviewPhotoActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, currentCartCount + 1);
                        cartId = jsonObject.getString("cartId");
                        PictureAirLog.d("cartid--> " + cartId);
                    }

                    @Override
                    public void _onError(int status) {
                        dismissPWProgressDialog();
                        newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), status), Common.TOAST_SHORT_TIME);
                    }

                    @Override
                    public void onCompleted() {
                        dismissPWProgressDialog();

                        myApplication.setIsBuyingPhotoInfo(null, null, photolist.get(currentPosition).getPhotoPassCode(), photolist.get(currentPosition).getShootDate());
                        myApplication.setBuyPPPStatus(Common.FROM_PREVIEW_PPP_ACTIVITY);

                        //生成订单
                        Intent intent1 = new Intent(PreviewPhotoActivity.this, SubmitOrderActivity.class);
                        ArrayList<CartItemInfo> orderinfoArrayList = new ArrayList<>();
                        CartItemInfo cartItemInfo1 = new CartItemInfo();
                        cartItemInfo1.setCartId(cartId);
                        cartItemInfo1.setProductName(pppGoodsInfo.getName());
                        cartItemInfo1.setProductNameAlias(pppGoodsInfo.getNameAlias());
                        cartItemInfo1.setUnitPrice(pppGoodsInfo.getPrice());
                        cartItemInfo1.setEmbedPhotos(new ArrayList<CartPhotosInfo>());
                        cartItemInfo1.setDescription(pppGoodsInfo.getDescription());
                        cartItemInfo1.setQty(1);
                        cartItemInfo1.setStoreId(pppGoodsInfo.getStoreId());
                        cartItemInfo1.setPictures(photoUrls);
                        cartItemInfo1.setPrice(pppGoodsInfo.getPrice());
                        cartItemInfo1.setCartProductType(3);

                        orderinfoArrayList.add(cartItemInfo1);
                        intent1.putExtra("orderinfo", orderinfoArrayList);
                        startActivity(intent1);
                    }
                });

    }

    /**
     * 获取商品
     */
    private void getGoods(final int type) {
        if (AppUtil.getNetWorkType(MyApplication.getInstance()) == 0) {
            newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
            dismissPWProgressDialog();
            return;
        }
        //从缓层中获取数据
        Observable.just(ACache.get(MyApplication.getInstance()).getAsString(Common.ALL_GOODS))
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<String, Observable<JSONObject>>() {
                    @Override
                    public Observable<JSONObject> call(String s) {
                        if (!TextUtils.isEmpty(s)) {
                            PictureAirLog.d("goods is not null  " + s);
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
                .map(new Func1<JSONObject, GoodsInfoJson>() {
                    @Override
                    public GoodsInfoJson call(JSONObject jsonObject) {
                        PictureAirLog.d("parse goods json");
                        return JsonTools.parseObject(jsonObject.toString(), GoodsInfoJson.class);//GoodsInfoJson.getString()
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<GoodsInfoJson>bindToLifecycle())
                .subscribe(new RxSubscribe<GoodsInfoJson>() {
                    @Override
                    public void _onNext(GoodsInfoJson goodsInfoJson) {
                        allGoodsList.clear();
                        if (goodsInfoJson != null && goodsInfoJson.getGoods().size() > 0) {
                            allGoodsList.addAll(goodsInfoJson.getGoods());
                        }
                        PictureAirLog.v(TAG, "goods size: " + allGoodsList.size());
                        dismissPWProgressDialog();
                        showSheetDialog(type);
                    }

                    @Override
                    public void _onError(int status) {
                        dismissPWProgressDialog();
                        newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), status), Common.TOAST_SHORT_TIME);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        //如果手指在上面的时候，如果同时休眠，在唤醒之后，页面上有个清晰圈
        //需要通知handler释放清晰圈
        if (photoInfo != null && photoInfo.getIsPaid() == 0 && photoInfo.getIsOnLine() == 1) {
            previewPhotoHandler.sendEmptyMessage(2);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PictureAirLog.v(TAG, "----------->" + myApplication.getRefreshViewAfterBuyBlurPhoto());
        if (photoInfo != null && photoInfo.getIsPaid() == 0 && photoInfo.getIsOnLine() == 1) {
            if (myApplication.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_MYPHOTOPASSPAYED)) {

            } else if (myApplication.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_VIEWORSELECTACTIVITYANDPAYED)) {

            } else if (myApplication.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_PREVIEW_PHOTO_ACTIVITY_PAY)) {

            } else {
                myApplication.setRefreshViewAfterBuyBlurPhoto("");
            }

            if (!myApplication.getBuyPPPStatus().equals(Common.FROM_PREVIEW_PPP_ACTIVITY_PAYED)) {//如果已经购买完成，则不需要清除数据，否则才会清除
                myApplication.setBuyPPPStatus("");
                //按返回，把状态全部清除
                myApplication.clearIsBuyingPhotoList();
            }

        }
        previewPhotoHandler.removeCallbacksAndMessages(null);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (mViewPager != null) {
            mViewPager.resetImageView();
        }
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            PictureAirLog.out("landscape----->");
            landscapeOrientation();
        } else {
            PictureAirLog.out("portrait----->");
            portraitOrientation();
        }

        if (pictureWorksDialog != null) {
            pictureWorksDialog.autoFitScreen();
        }
        super.onConfigurationChanged(newConfig);

        String language = MyApplication.getInstance().getLanguageType();
        PictureAirLog.out("language------>" + language);
        Configuration config = getResources().getConfiguration();
        if (!language.equals("")) {//语言不为空
            if (language.equals(Common.ENGLISH)) {
                if (Build.VERSION.SDK_INT < 24) {
                    config.locale = Locale.US;
                } else {
                    config.setLocale(Locale.US);
                }
            } else if (language.equals(Common.SIMPLE_CHINESE)) {
                if (Build.VERSION.SDK_INT < 24) {
                    config.locale = Locale.SIMPLIFIED_CHINESE;
                } else {
                    config.setLocale(Locale.SIMPLIFIED_CHINESE);
                }
            }
        }
        PictureAirLog.out("new config---->" + config.locale);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        getResources().updateConfiguration(config, displayMetrics);
        PictureAirLog.out("update configuration done");

    }

    /**
     * 垂直模式
     */
    private void portraitOrientation() {
        isLandscape = false;
        setFullScreenMode(false);
    }

    /**
     * 横屏模式
     */
    private void landscapeOrientation() {
        isLandscape = true;
        if (sharePop.isShowing()) {
            sharePop.dismiss();
        }
        setFullScreenMode(true);
    }

    /**
     * 旋转手机
     * @param fullScreenMode
     */
    private void setFullScreenMode(boolean fullScreenMode) {
        titleBar.setVisibility(fullScreenMode ? View.GONE : View.VISIBLE);
        if (mViewPager != null) {
            mViewPager.setBackgroundColor(fullScreenMode ? Color.BLACK : ContextCompat.getColor(this, R.color.pp_light_gray_background));
            //默认初始化5个view，所以需要修改这5个view
            for (int i = currentPosition - 2; i < currentPosition + 3; i++) {
                if (i < 0) {
                    continue;
                } else {
                    UrlTouchImageView urlTouchImageView = (UrlTouchImageView)mViewPager.findViewById(i);
                    if (urlTouchImageView != null) {
                        PictureAirLog.d("set full screen mode---->");
                        urlTouchImageView.setFullScreenMode(fullScreenMode);
                    }
                }
            }
        }

        if (pagerAdapter != null) {
            pagerAdapter.setFullScreenMode(fullScreenMode);
        }

        if (fullScreenMode) {
            photoFraRelativeLayout.setBackgroundColor(Color.BLACK);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            photoFraRelativeLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.pp_light_gray_background));
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    //直接下载
    private void downloadPic() {
        ArrayList<PhotoInfo> list = new ArrayList<>();
        list.add(photolist.get(mViewPager.getCurrentItem()));
        Intent intent = new Intent(PreviewPhotoActivity.this, DownloadService.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("photos", list);
        bundle.putInt("prepareDownloadCount",list.size());//该参数用于传递下载的图片数量
        intent.putExtras(bundle);
        startService(intent);

        //弹框提示，可以进去下载管理页面
        pictureWorksDialog.setPWDialogId(GO_DOWNLOAD_ACTIVITY_DIALOG)
                .setPWDialogMessage(R.string.edit_story_addto_downloadlist)
                .setPWDialogNegativeButton(null)
                .setPWDialogPositiveButton(R.string.reset_pwd_ok)
                .pwDilogShow();
    }


    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (sharePop != null) {
            PictureAirLog.out("sharePop not null");
            if (shareType != SharePop.TWITTER && isShareDialogShowing) {
                PictureAirLog.out("dismiss dialog");
                dismissPWProgressDialog();
            }
        }
    }

    /**
     * tips 1，网络下载流程。
     */
    private void judgeOnePhotoDownloadFlow() { // 如果当前是wifi，无弹窗提示。如果不是wifi，则提示。
        if (!AppUtil.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            newToast.setTextAndShow(R.string.permission_storage_message, Common.TOAST_SHORT_TIME);
            return;
        }
        if (AppUtil.getNetWorkType(PreviewPhotoActivity.this) == AppUtil.NETWORKTYPE_WIFI) {
            downloadPic();
        } else {
            // 判断用户是否设置过 “仅wifi” 的选项。
            if (settingUtil.isOnlyWifiDownload(SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ID, ""))) {
                pictureWorksDialog.setPWDialogId(GO_SETTING_DIALOG)
                        .setPWDialogMessage(R.string.one_photo_download_msg1)
                        .setPWDialogNegativeButton(R.string.one_photo_download_no_msg1)
                        .setPWDialogPositiveButton(R.string.one_photo_download_yes_msg1)
                        .pwDilogShow();
            } else {
                pictureWorksDialog.setPWDialogId(DOWNLOAD_DIALOG)
                        .setPWDialogMessage(R.string.one_photo_download_msg2)
                        .setPWDialogNegativeButton(R.string.one_photo_download_no_msg2)
                        .setPWDialogPositiveButton(R.string.one_photo_download_yes_msg2)
                        .pwDilogShow();
            }
        }
    }

    private void setUmengPhotoSlide() {
        UmengUtil.onEvent(PreviewPhotoActivity.this, Common.EVENT_PHOTO_SLIDE);
    }

    /**
     * 更新同一组PP, PP 卡号相同，日期相同的更新。
     * @param photolist
     * @param ppsStr    //ppsStr:[{"bindDate":"2016-05-04","code":"SHDRF22A2PWFH4N6"}]
     */
    private void refreshPP(List<PhotoInfo> photolist, String ppsStr) {
        JSONArray ppsArray = JSONArray.parseArray(ppsStr);
        JSONObject jsonObject = (JSONObject) ppsArray.get(0);
        if (photolist != null) {
            for (int i = 0; i < photolist.size(); i++) {
                if (photolist.get(i).getPhotoPassCode() != null) {
                    if (photolist.get(i).getPhotoPassCode().replace(",","").equals(jsonObject.getString("code"))) {
                        if (photolist.get(i).getStrShootOn().contains(jsonObject.getString("bindDate"))) {
                            photolist.get(i).setIsPaid(1);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onPWDialogButtonClicked(int which, int dialogId) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            switch (dialogId) {
                case GO_SETTING_DIALOG:
                    //去更改：跳转到设置界面。
                    Intent intent = new Intent(PreviewPhotoActivity.this, SettingActivity.class);
                    startActivity(intent);
                    break;

                case DOWNLOAD_DIALOG:
                    downloadPic();
                    break;

                case GO_DOWNLOAD_ACTIVITY_DIALOG:
                    AppManager.getInstance().killActivity(LoadManageActivity.class);
                    Intent i = new Intent(PreviewPhotoActivity.this, LoadManageActivity.class);
                    startActivity(i);
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public void videoClick(int position) {
        PictureAirLog.out("start check the video info -->" + position);
        /**
         * 1.检查数据库，是否需要重新获取数据
         * 2.获取最新的视频信息
         * 3.储存最新信息
         * 4.跳转或者弹框提示
         */
        showPWProgressDialog(R.string.is_loading);

        if (lastestVideoInfoPresenter == null) {
            lastestVideoInfoPresenter = new GetLastestVideoInfoPresenter(this, this);
        }

        lastestVideoInfoPresenter.videoInfoClick(photolist.get(position).getPhotoId(), MyApplication.getTokenId(), position);
    }

    @Override
    public void buyClick(int position) {
        PictureAirLog.d("buy---> " + position);
        prepareShowSheetDialog(BUY_BLUR_PHOTO_SHEET_DIALOG);
    }

    @Override
    public void longClick(int position) {
        PictureAirLog.d("long click--->");
        if (!sheetDialog.isShowing()) {
            prepareShowSheetDialog(EDIT_PHOTO_SHEET_DIALOG);
        }
    }

    @Override
    public void getNewInfoDone(int dealStatus, int position, PhotoInfo photoInfo, boolean checkByNetwork) {
        dismissPWProgressDialog();

        switch (dealStatus) {
            case GetLastestVideoInfoBiz.NETWORK_ERROR://网络问题
                newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                break;

            case GetLastestVideoInfoBiz.VIDEO_MAKING://依旧在制作中
                pictureWorksDialog.setPWDialogId(VIDEO_STILL_MAKING_DIALOG)
                        .setPWDialogMessage(R.string.magic_in_the_making)
                        .setPWDialogNegativeButton(null)
                        .setPWDialogPositiveButton(R.string.button_ok)
                        .pwDilogShow();
                break;

            case GetLastestVideoInfoBiz.VIDEO_FINISHED://已经制作完成
                //list拿错数据
                PictureAirLog.d("result--->" + checkByNetwork);
                PhotoInfo info;
                if (checkByNetwork) {
                    info = photoInfo;
                } else {
                    info = photolist.get(position);
                }

                Intent intent = new Intent(this, VideoPlayerActivity.class);
                intent.putExtra("from_story", info);
                startActivity(intent);
                overridePendingTransition(R.anim.activity_fadein, R.anim.activity_fadeout);
                break;

            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == 11) {
                //保存完图片的处理
                PictureAirLog.v(TAG, "save success");
                //1.获取新图片的数据，生成一个新的对象
                pictureWorksDialog.setPWDialogId(SAVE_PHOTO_TIP_DIALOG)
                        .setPWDialogMessage(R.string.sea_photo_in_camera)
                        .setPWDialogNegativeButton(null)
                        .setPWDialogPositiveButton(R.string.button_ok)
                        .pwDilogShow();
            }
        }
    }
}