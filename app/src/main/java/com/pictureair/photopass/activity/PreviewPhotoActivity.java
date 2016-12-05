package com.pictureair.photopass.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
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
import com.pictureair.photopass.controller.GetLastestVideoInfoPresenter;
import com.pictureair.photopass.controller.IGetLastestVideoInfoView;
import com.pictureair.photopass.customDialog.PWDialog;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.entity.CartItemInfoJson;
import com.pictureair.photopass.entity.CartPhotosInfo;
import com.pictureair.photopass.entity.DiscoverLocationItemInfo;
import com.pictureair.photopass.entity.GoodsInfo;
import com.pictureair.photopass.entity.GoodsInfoJson;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.greendao.PictureAirDbManager;
import com.pictureair.photopass.service.DownloadService;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.JsonTools;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.util.SettingUtil;
import com.pictureair.photopass.util.UmengUtil;
import com.pictureair.photopass.widget.PWToast;
import com.pictureair.photopass.widget.SharePop;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * 预览图片，可以进行编辑，分享，下载和制作礼物的操作
 *
 * @author bauer_bao
 */
public class PreviewPhotoActivity extends BaseActivity implements OnClickListener, Handler.Callback,
        PWDialog.OnPWDialogClickListener, PWDialog.OnCustomerViewCallBack, PhotoEventListener, IGetLastestVideoInfoView {
    private SettingUtil settingUtil;

    private TextView locationTextView;

    private GalleryViewPager mViewPager;
    private UrlPagerAdapter pagerAdapter;
    private ImageView returnImageView;

    private PWToast newToast;
    private SharePop sharePop;
    private MyApplication myApplication;
    private PhotoInfo photoInfo;

    private RelativeLayout titleBar;
    private static final String TAG = "PreviewPhotoActivity";

    private int shareType = 0;

    //图片显示框架
    private ArrayList<PhotoInfo> photolist;
    private ArrayList<PhotoInfo> targetphotolist;
    private ArrayList<DiscoverLocationItemInfo> locationList = new ArrayList<DiscoverLocationItemInfo>();
    private int currentPosition;//记录当前预览照片的索引值

    private boolean isEdited = false;
    private String tabName;

    /**
     * 模糊图购买对话框控件
     */
    private static final int BUY_PHOTO = 201;
    private static final int BUY_PPP = 202;
    private static final int BUY_PHOTO_USE_CURRENT_PPP = 203;
    private int selectedGoods;
    private ImageView buyPhotoIV, buyPPPIV, upgradePPIV, closeDialogIV;
    private TextView buyPhotoPriceTV, buyPhotoIntroTV, buyPPPPriceTV, buyPPPIntroTV;
    private Button confirmToBuyBtn;

    /**
     * 是否是横屏模式
     */
    private boolean isLandscape = false;

    private RelativeLayout photoFraRelativeLayout;

    private Dialog dia;
    private TextView editTV, shareTV, downloadTV, makeGiftTV;
    private View cancelView;

    private Date date;
    private SimpleDateFormat simpleDateFormat;
    private CartItemInfoJson cartItemInfoJson;//存放意见购买后的购物信息

    private static final int GET_LOCATION_AD = 777;
    private static final int GET_LOCATION_AD_DONE = 1001;
    private static final int CREATE_BLUR_DIALOG = 888;
    private static final int NO_PHOTOS_AND_RETURN = 1002;

    private static final int LOCAL_PHOTO_EDIT_DIALOG = 1003;
    private static final int FRAME_PHOTO_EDIT_DIALOG = 1004;
    private static final int GO_SETTING_DIALOG = 1005;
    private static final int DOWNLOAD_DIALOG = 1006;
    private static final int GO_DOWNLOAD_ACTIVITY_DIALOG = 1007;
    private static final int VIDEO_STILL_MAKING_DIALOG = 1008;
    private static final int BUY_BLUR_PHOTO_DIALOG = 1009;

    private PWDialog pictureWorksDialog, buyPhotoDialog;

    private List<GoodsInfo> allGoodsList;//全部商品
    private GoodsInfo pppGoodsInfo;
    private String[] photoUrls;

    private Handler previewPhotoHandler;

    //点击视频播放的处理对象
    private GetLastestVideoInfoPresenter lastestVideoInfoPresenter;

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

            case API1.BUY_PHOTO_SUCCESS:
                dismissPWProgressDialog();
                cartItemInfoJson = JsonTools.parseObject((JSONObject) msg.obj, CartItemInfoJson.class);//CartItemInfoJson.getString()
                if (cartItemInfoJson.getItems().size() == 0) {
                    break;
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
                int curCarts = SPUtils.getInt(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
                SPUtils.put(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, curCarts + 1);
                intent.putExtra("orderinfo", orderinfo);
                startActivity(intent);
                break;

            case API1.BUY_PHOTO_FAILED:
                //购买失败
                dismissPWProgressDialog();
                newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);
                break;

            case API1.GET_GOODS_SUCCESS:
                GoodsInfoJson goodsInfoJson = JsonTools.parseObject(msg.obj.toString(), GoodsInfoJson.class);//GoodsInfoJson.getString()
                if (goodsInfoJson != null && goodsInfoJson.getGoods() != null && goodsInfoJson.getGoods().size() > 0) {
                    allGoodsList = goodsInfoJson.getGoods();
                    PictureAirLog.v(TAG, "goods size: " + allGoodsList.size());
                    //获取PP+
                    for (GoodsInfo goodsInfo : allGoodsList) {
                        if (goodsInfo.getName().equals(Common.GOOD_NAME_PPP)) {
                            pppGoodsInfo = goodsInfo;
                            //封装购物车宣传图
                            photoUrls = new String[goodsInfo.getPictures().size()];
                            for (int i = 0; i < goodsInfo.getPictures().size(); i++) {
                                photoUrls[i] = goodsInfo.getPictures().get(i).getUrl();
                            }
                            break;
                        }
                    }
                    API1.addToCart(pppGoodsInfo.getGoodsKey(), 1, true, null, previewPhotoHandler);
                }
                break;

            case API1.GET_GOODS_FAILED:
                dismissPWProgressDialog();
                newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);
                break;

            case API1.ADD_TO_CART_FAILED:
                dismissPWProgressDialog();
                newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);

                break;

            case API1.ADD_TO_CART_SUCCESS:
                dismissPWProgressDialog();
                JSONObject jsonObject = (JSONObject) msg.obj;
                int currentCartCount = SPUtils.getInt(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
                SPUtils.put(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, currentCartCount + 1);
                String cartId = jsonObject.getString("cartId");

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

                mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

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
                final int oldPositon = msg.arg1;
                if (myApplication.isGetADLocationSuccess()) {
                    //从数据库中查找
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String adStr = PictureAirDbManager.getADByLocationId(photoInfo.getLocationId(), MyApplication.getInstance().getLanguageType());
                            previewPhotoHandler.obtainMessage(GET_LOCATION_AD_DONE, oldPositon, 0, adStr).sendToTarget();
                        }
                    }).start();

                } else {
                    //从网络获取
                    API1.getADLocations(oldPositon, previewPhotoHandler);
                }
                dismissPWProgressDialog();
                break;

            case GET_LOCATION_AD_DONE:
                if (msg.arg1 == currentPosition) {
                    //如果获取的对应索引值，依旧是当期的索引值，则显示广告
                    UrlTouchImageView imageView = (UrlTouchImageView)mViewPager.findViewById(currentPosition);
                    if (imageView != null) {
                        imageView.setADText(msg.obj.toString());
                    }
                }
                break;

            case API1.GET_AD_LOCATIONS_SUCCESS:
                PictureAirLog.out("ad location---->" + msg.obj.toString());
                final int oldPosition1 = msg.arg1;
                final JSONObject adJsonObject = JSONObject.parseObject(msg.obj.toString());
                myApplication.setGetADLocationSuccess(true);
                /**
                 * 1.存入数据库
                 * 2.在application中记录结果
                 */
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String adString = PictureAirDbManager.insertADLocations(adJsonObject.getJSONArray("locations"),
                            photoInfo.getLocationId(), MyApplication.getInstance().getLanguageType());
                        previewPhotoHandler.obtainMessage(GET_LOCATION_AD_DONE, oldPosition1, 0, adString).sendToTarget();
                    }
                }).start();
                break;

            case API1.GET_AD_LOCATIONS_FAILED:
                break;

            case CREATE_BLUR_DIALOG:
                createBlurDialog();
                break;

            case API1.GET_PPPS_BY_SHOOTDATE_SUCCESS:  //根据已有PP＋升级
                if (API1.PPPlist.size() > 0) {
                    //将 tabname 存入sp
                    SPUtils.put(this, Common.SHARED_PREFERENCE_USERINFO_NAME, "tabName", tabName);
                    SPUtils.put(this, Common.SHARED_PREFERENCE_USERINFO_NAME, "currentPosition", currentPosition);

                    dia.dismiss();

                    intent = new Intent(PreviewPhotoActivity.this, SelectPPActivity.class);
                    intent.putExtra("photoPassCode",photoInfo.getPhotoPassCode());
                    intent.putExtra("shootTime",photoInfo.getShootDate());
                    startActivity(intent);
                } else {
                    newToast.setTextAndShow(R.string.no_ppp_tips, Common.TOAST_SHORT_TIME);
                }
                break;

            case API1.GET_PPPS_BY_SHOOTDATE_FAILED:
                newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);
                break;

            case NO_PHOTOS_AND_RETURN://没有图片
                dismissPWProgressDialog();
                finish();
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
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE );
        init();//初始化UI
        PictureAirLog.out("oncreate finish----");
    }

    private void init() {
        // TODO Auto-generated method stub
        pictureWorksDialog = new PWDialog(this)
                .setOnPWDialogClickListener(this)
                .pwDialogCreate();
        previewPhotoHandler = new Handler(this);
        settingUtil = new SettingUtil();
        newToast = new PWToast(this);
        sharePop = new SharePop(this);
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        PictureAirLog.out("oncreate----->2");
        returnImageView = (ImageView) findViewById(R.id.button1_shop_rt);

        locationTextView = (TextView) findViewById(R.id.preview_location);

        photoFraRelativeLayout = (RelativeLayout) findViewById(R.id.fra_layout);

        titleBar = (RelativeLayout) findViewById(R.id.preview_titlebar);

        previewPhotoHandler.sendEmptyMessage(CREATE_BLUR_DIALOG);

        myApplication = (MyApplication) getApplication();

        returnImageView.setOnClickListener(this);

        Configuration cf = getResources().getConfiguration();
        int ori = cf.orientation;
        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
            isLandscape = true;
            landscapeOrientation();
        }

        showPWProgressDialog();
        getPreviewPhotos();
    }

    private void getPreviewPhotos() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                //获取本地图片
                targetphotolist = new ArrayList<>();
                targetphotolist.addAll(AppUtil.getLocalPhotos(PreviewPhotoActivity.this, Common.PHOTO_SAVE_PATH, Common.ALBUM_MAGIC));
                Collections.sort(targetphotolist);

                //获取intent传递过来的信息
                photolist = new ArrayList<>();
                Bundle bundle = getIntent().getBundleExtra("bundle");
                currentPosition = bundle.getInt("position", 0);
                PictureAirLog.out("currentposition---->" + currentPosition);
                tabName = bundle.getString("tab");
                PictureAirLog.out("tabName--->" + tabName);
                long cacheTime = System.currentTimeMillis() - PictureAirDbManager.CACHE_DAY * PictureAirDbManager.DAY_TIME;

                if (tabName.equals("all")) {//获取全部照片
                    locationList.addAll(AppUtil.getLocation(PreviewPhotoActivity.this, ACache.get(PreviewPhotoActivity.this).getAsString(Common.DISCOVER_LOCATION), true));
                    try {
                        photolist.addAll(AppUtil.getSortedAllPhotos(PreviewPhotoActivity.this, locationList, targetphotolist,
                                simpleDateFormat.format(new Date(cacheTime)),
                                simpleDateFormat, MyApplication.getInstance().getLanguageType(), false));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                } else if (tabName.equals("photopass")) {//获取pp图片
                    locationList.addAll(AppUtil.getLocation(PreviewPhotoActivity.this, ACache.get(PreviewPhotoActivity.this).getAsString(Common.DISCOVER_LOCATION), true));
                    try {
                        photolist.addAll(AppUtil.getSortedPhotoPassPhotos(locationList,
                                simpleDateFormat.format(new Date(cacheTime)), simpleDateFormat, MyApplication.getInstance().getLanguageType(), false, false));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                } else if (tabName.equals("local")) {//获取本地图片
                    photolist.addAll(targetphotolist);

                } else if (tabName.equals("bought")) {//获取已经购买的图片
                    locationList.addAll(AppUtil.getLocation(PreviewPhotoActivity.this, ACache.get(PreviewPhotoActivity.this).getAsString(Common.DISCOVER_LOCATION), true));
                    try {
                        photolist.addAll(AppUtil.getSortedPhotoPassPhotos(locationList,
                                simpleDateFormat.format(new Date(cacheTime)), simpleDateFormat, MyApplication.getInstance().getLanguageType(), true, false));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                } else if (tabName.equals("favourite")) {//获取收藏图片

                } else if (tabName.equals("editStory")){//编辑PP照片页面
                    String ppCode = bundle.getString("ppCode");
                    locationList.addAll(AppUtil.getLocation(PreviewPhotoActivity.this, ACache.get(PreviewPhotoActivity.this).getAsString(Common.DISCOVER_LOCATION), true));
                    photolist.addAll(AppUtil.insertSortFavouritePhotos(
                            PictureAirDbManager.getPhotoInfosByPPCode(ppCode, locationList, MyApplication.getInstance().getLanguageType()), false));

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

    private void createBlurDialog() {
        dia = new Dialog(this, R.style.dialogTans);
        Window window = dia.getWindow();
        window.setGravity(Gravity.BOTTOM);
        //		window.setWindowAnimations(R.style.from_bottom_anim);
        dia.setCanceledOnTouchOutside(true);
        View view = View.inflate(this, R.layout.preview_photo_dialog, null);
        dia.setContentView(view);
        WindowManager.LayoutParams layoutParams = dia.getWindow().getAttributes();
        layoutParams.width = ScreenUtil.getScreenWidth(this);
        dia.getWindow().setAttributes(layoutParams);

        cancelView = dia.findViewById(R.id.space_view);
        editTV = (TextView) dia.findViewById(R.id.preview_edit);
        shareTV = (TextView) dia.findViewById(R.id.preview_share);
        downloadTV = (TextView) dia.findViewById(R.id.preview_download);
        makeGiftTV = (TextView) dia.findViewById(R.id.preview_makegift);
        cancelView.setOnClickListener(this);
        editTV.setOnClickListener(this);
        shareTV.setOnClickListener(this);
        downloadTV.setOnClickListener(this);
        makeGiftTV.setOnClickListener(this);
    }

    /**
     * 更新底部索引工具
     */
    private void updateIndexTools() {
        PictureAirLog.v(TAG, "updateIndexTools-------->" + currentPosition);
        //初始化图片收藏按钮，需要判断isLove=1或者是否在数据库中
        if (isEdited) {
            photoInfo = targetphotolist.get(currentPosition);
        } else {//编辑前
            photoInfo = photolist.get(currentPosition);
        }

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

        if (isLandscape) {//横屏模式
            if (mViewPager != null) {
                mViewPager.setBackgroundColor(Color.BLACK);
            }
        }
    }

    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.button1_shop_rt:
                finish();
                break;

            case R.id.preview_love://收藏按钮的操作
                if (isEdited) {
                    photoInfo = targetphotolist.get(mViewPager.getCurrentItem());
                } else {//编辑前
                    photoInfo = photolist.get(mViewPager.getCurrentItem());
                }
                if (photoInfo == null) {
                    return;
                }
                myApplication.needScanFavoritePhotos = true;
                break;

            case R.id.preview_edit://编辑
                if (dia.isShowing()) {
                    dia.dismiss();
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
                        if (isEdited) {//已经编辑过，取targetlist中的值
                            intent.putExtra("photo", targetphotolist.get(mViewPager.getCurrentItem()));
                        } else {//没有编辑，取正常的值
                            intent.putExtra("photo", photolist.get(mViewPager.getCurrentItem()));
                        }
                        startActivityForResult(intent, 1);
                    } else {
                        pictureWorksDialog.setPWDialogId(FRAME_PHOTO_EDIT_DIALOG)
                                .setPWDialogMessage(R.string.photo_cannot_edit_content)
                                .setPWDialogNegativeButton(null)
                                .setPWDialogPositiveButton(R.string.photo_cannot_edit_yes)
                                .pwDilogShow();
                    }
                } else {
                    dia.show();
                }
                break;

            case R.id.preview_share:
                if (dia.isShowing()) {
                    dia.dismiss();
                }
                if (photoInfo == null) {
                    return;
                }
                if (photoInfo.getIsPaid() == 1) {
                    dia.dismiss();
                    if (mViewPager.getCurrentItem() >= photolist.size()) {
                        return;
                    }
                    PictureAirLog.v(TAG, "start share=" + photolist.get(mViewPager.getCurrentItem()).getPhotoOriginalURL());
                    if (isEdited) {//编辑后
                        sharePop.setshareinfo(targetphotolist.get(mViewPager.getCurrentItem()), previewPhotoHandler);
                    } else {//编辑前
                        sharePop.setshareinfo(photolist.get(mViewPager.getCurrentItem()), previewPhotoHandler);
                    }
                    sharePop.showAtLocation(v, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                } else {
                    dia.show();
                }
                break;

            case R.id.preview_download://下载,如果不是pp的照片，提示不需要下载，如果是pp的照片，并且没有支付，提示购买，如果已经购买，如果没有下载，则下载，否则提示已经下载
                if (dia.isShowing()) {
                    dia.dismiss();
                }
                if (AppUtil.getNetWorkType(PreviewPhotoActivity.this) == AppUtil.NETWORKTYPE_INVALID) {
                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    return;
                }
                if (photoInfo == null) {
                    return;
                }
                if (photoInfo.getIsPaid() == 1) {
                    if (isEdited) {//编辑后
                        newToast.setTextAndShow(R.string.neednotdownload, Common.TOAST_SHORT_TIME);
                    } else {//编辑前
                        if (photoInfo.getIsOnLine() == 1) {//是pp的照片
                            judgeOnePhotoDownloadFlow();
                        } else {
                            newToast.setTextAndShow(R.string.neednotdownload, Common.TOAST_SHORT_TIME);
                        }
                    }

                } else {
                    dia.show();
                }

                break;

            case R.id.preview_makegift:
                if (dia.isShowing()) {
                    dia.dismiss();
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
                //判断是否已经被编辑过
                if (isEdited) {//已经被编辑过，那么取得是targetList中的值
                    intent.putExtra("selectPhoto", targetphotolist.get(mViewPager.getCurrentItem()));
                } else {//没有编辑过，直接获取之前的值
                    intent.putExtra("selectPhoto", photolist.get(mViewPager.getCurrentItem()));
                }
                startActivity(intent);
                if (dia != null && dia.isShowing()) {
                    dia.dismiss();
                }
                break;

            case R.id.cancel:
                dia.dismiss();
                break;

//            case buynow:
//                if (AppUtil.getNetWorkType(PreviewPhotoActivity.this) == AppUtil.NETWORKTYPE_INVALID) {
//                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
//                    dia.dismiss();
//                    return;
//                }
//                if (photoInfo == null) {
//                    return;
//                }
//                showPWProgressDialog();
//                API1.buyPhoto(photoInfo.getPhotoId(), previewPhotoHandler);
//                dia.dismiss();
//                break;

            case R.id.space_view:
                if (dia != null && dia.isShowing()) {
                    dia.dismiss();

                }
                break;

//            case buy_ppp:
//                //直接购买PP+
//                if (AppUtil.getNetWorkType(PreviewPhotoActivity.this) == AppUtil.NETWORKTYPE_INVALID) {
//                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
//                    dia.dismiss();
//                    return;
//                }
//                showPWProgressDialog();
//                //获取商品
//                getALlGoods();
//                dia.dismiss();
//                break;
//            case R.id.use_ppp:
//                if (photoInfo == null) {
//                    return;
//                }
//                if (AppUtil.getNetWorkType(PreviewPhotoActivity.this) == AppUtil.NETWORKTYPE_INVALID) { //判断网络情况。
//                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
//                    dia.dismiss();
//                    return;
//                }else{
//                    API1.getPPPsByShootDate(previewPhotoHandler, photoInfo.getShootDate());
//                }
//                break;

            default:
                break;
        }
    }

    /**
     * 初始化数据
     */
    public void getALlGoods() {
        //从缓层中获取数据
        String goodsByACache = ACache.get(MyApplication.getInstance()).getAsString(Common.ALL_GOODS);
        if (goodsByACache != null && !goodsByACache.equals("")) {
            previewPhotoHandler.obtainMessage(API1.GET_GOODS_SUCCESS, goodsByACache).sendToTarget();
        } else {
            //从网络获取商品,先检查网络
            if (AppUtil.getNetWorkType(MyApplication.getInstance()) != 0) {
                API1.getGoods(previewPhotoHandler);
            } else {
                //提醒检查网络
                newToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
            }
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
                PhotoInfo selectPhotoItemInfo = new PhotoInfo();
                selectPhotoItemInfo.setPhotoOriginalURL(data.getStringExtra("photoUrl"));
                File file = new File(selectPhotoItemInfo.getPhotoOriginalURL());
                date = new Date(file.lastModified());
                selectPhotoItemInfo.setPhotoId(selectPhotoItemInfo.getPhotoOriginalURL());
                selectPhotoItemInfo.setStrShootOn(simpleDateFormat.format(date));
                selectPhotoItemInfo.setShootDate(selectPhotoItemInfo.getStrShootOn().substring(0, 10));
                selectPhotoItemInfo.setLocationName(getString(R.string.story_tab_magic));
                selectPhotoItemInfo.setIsPaid(1);

                //2.将新图片插入到targetList中
                targetphotolist.add(0, selectPhotoItemInfo);
                //3.修改viewPager中的值为targetList
                pagerAdapter = new UrlPagerAdapter(this, targetphotolist, 0, true);
                mViewPager.setAdapter(pagerAdapter);
                mViewPager.setCurrentItem(0, true);
                currentPosition = 0;
                //4.更新底部工具栏
                isEdited = true;

                updateIndexTools();

                myApplication.setneedScanPhoto(true);
                myApplication.scanMagicFinish = false;
            }
        }
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

        if (dia != null) {
            WindowManager.LayoutParams layoutParams = dia.getWindow().getAttributes();
            layoutParams.width = ScreenUtil.getScreenWidth(this);
            dia.getWindow().setAttributes(layoutParams);
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
            mViewPager.setBackgroundColor(fullScreenMode ? Color.BLACK : getResources().getColor(R.color.pp_light_gray_background));
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
            photoFraRelativeLayout.setBackgroundColor(getResources().getColor(R.color.pp_light_gray_background));
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    //直接下载
    private void downloadPic() {
        ArrayList<PhotoInfo> list = new ArrayList<PhotoInfo>();
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
            if (shareType != SharePop.TWITTER) {
                PictureAirLog.out("dismiss dialog");
                sharePop.dismissDialog();
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
    public void initCustomerView(View view, int dialogId) {
        if (dialogId == BUY_BLUR_PHOTO_DIALOG) {
            closeDialogIV = (ImageView) view.findViewById(R.id.preview_blur_dialog_close);
            buyPhotoIV = (ImageView) view.findViewById(R.id.preview_blur_dialog_buy_photo_select_iv);
            buyPhotoPriceTV = (TextView) view.findViewById(R.id.preview_blur_dialog_buy_photo_price_tv);
            buyPhotoIntroTV = (TextView) view.findViewById(R.id.preview_blur_dialog_buy_photo_intro_tv);
            buyPhotoIntroTV.setVisibility(View.GONE);
            buyPPPIV = (ImageView) view.findViewById(R.id.preview_blur_dialog_buy_ppp_select_iv);
            buyPPPPriceTV = (TextView) view.findViewById(R.id.preview_blur_dialog_buy_ppp_price_tv);
            buyPPPIntroTV = (TextView) view.findViewById(R.id.preview_blur_dialog_buy_ppp_intro_tv);
            buyPPPIntroTV.setVisibility(View.GONE);
            upgradePPIV = (ImageView) view.findViewById(R.id.preview_blur_dialog_upgrade_photo_select_iv);
            confirmToBuyBtn = (Button) view.findViewById(R.id.preview_blur_dialog_buy_btn);

            closeDialogIV.setOnClickListener(this);
            buyPhotoIV.setOnClickListener(this);
            buyPPPIV.setOnClickListener(this);
            upgradePPIV.setOnClickListener(this);
            confirmToBuyBtn.setOnClickListener(this);
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
            lastestVideoInfoPresenter = new GetLastestVideoInfoPresenter(this, this, MyApplication.getTokenId());
        }

        lastestVideoInfoPresenter.videoInfoClick(photolist.get(position).getPhotoId(), position);
    }

    @Override
    public void buyClick(int position) {
        PictureAirLog.d("buy---> " + position);
        if (buyPhotoDialog == null) {
            buyPhotoDialog = new PWDialog(this, BUY_BLUR_PHOTO_DIALOG)
                    .setPWDialogNegativeButton(null)
                    .setPWDialogPositiveButton(null)
                    .setPWDialogBackgroundColor(R.color.transparent)
                    .setPWDialogContentView(R.layout.dialog_preview_buy_blur, this)
                    .pwDialogCreate();
        }
        buyPhotoDialog.pwDilogShow();
    }

    @Override
    public void longClick(int position) {
        PictureAirLog.d("long click--->");
        if (dia != null && !dia.isShowing()) {
            if (photoInfo.getIsVideo() == 1) {
                editTV.setVisibility(View.GONE);
                makeGiftTV.setVisibility(View.GONE);
            } else {
                editTV.setVisibility(View.VISIBLE);
                makeGiftTV.setVisibility(View.VISIBLE);
            }
            dia.show();

        }
    }

    @Override
    public void getNewInfoDone(int dealStatus, int position, PhotoInfo photoInfo, boolean checkByNetwork) {
        dismissPWProgressDialog();

        switch (dealStatus) {
            case GetLastestVideoInfoPresenter.NETWORK_ERROR://网络问题
                newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                break;

            case GetLastestVideoInfoPresenter.VIDEO_MAKING://依旧在制作中
                pictureWorksDialog.setPWDialogId(VIDEO_STILL_MAKING_DIALOG)
                        .setPWDialogMessage(R.string.magic_in_the_making)
                        .setPWDialogNegativeButton(null)
                        .setPWDialogPositiveButton(R.string.button_ok)
                        .pwDilogShow();
                break;

            case GetLastestVideoInfoPresenter.VIDEO_FINISHED://已经制作完成
                //list拿错数据
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
}