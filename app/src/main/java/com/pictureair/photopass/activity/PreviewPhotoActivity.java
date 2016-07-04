package com.pictureair.photopass.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.jni.keygenerator.PWJniUtil;
import com.pictureair.photopass.GalleryWidget.GalleryViewPager;
import com.pictureair.photopass.GalleryWidget.UrlPagerAdapter;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.BlurUtil;
import com.pictureair.photopass.customDialog.CustomDialog;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.entity.CartItemInfoJson;
import com.pictureair.photopass.entity.CartPhotosInfo;
import com.pictureair.photopass.entity.DiscoverLocationItemInfo;
import com.pictureair.photopass.entity.GoodsInfo;
import com.pictureair.photopass.entity.GoodsInfoJson;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.service.DownloadService;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.AESKeyHelper;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.HttpCallback;
import com.pictureair.photopass.util.HttpUtil1;
import com.pictureair.photopass.util.JsonTools;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.util.SettingUtil;
import com.pictureair.photopass.util.UmengUtil;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.PWToast;
import com.pictureair.photopass.widget.PictureWorksDialog;
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
@SuppressLint({"FloatMath", "NewApi"})
public class PreviewPhotoActivity extends BaseActivity implements OnClickListener, Handler.Callback {
    private SettingUtil settingUtil;
    //工具条
    private TextView editButton;
    private TextView shareButton;
    private TextView downloadButton;
    private TextView makegiftButton;

    private TextView locationTextView;

    private GalleryViewPager mViewPager;
    private ImageView returnImageView;

    private ImageButton loveImageButton;

    private PWToast newToast;
    private SharePop sharePop;
    private MyApplication myApplication;
    private PictureAirDbManager pictureAirDbManager;
    private PhotoInfo photoInfo;
    private SharedPreferences sharedPreferences;

    private RelativeLayout titleBar;
    private LinearLayout toolsBar, indexBar;
    private static final String TAG = "PreviewPhotoActivity";

    private int shareType = 0;


    //图片显示框架
    private ArrayList<PhotoInfo> photolist;
    private ArrayList<PhotoInfo> targetphotolist;
    private ArrayList<DiscoverLocationItemInfo> locationList = new ArrayList<DiscoverLocationItemInfo>();
    private int currentPosition;//记录当前预览照片的索引值


    private boolean isEdited = false;

    /**
     * 是否已经拿到对象
     */
    private boolean getPhotoInfoSuccess = false;

    /**
     * 是否已经获取图片结束
     */
    private boolean loadPhotoSuccess = false;

    private String tabName;

    /**
     * 是否是横屏模式
     */
    private boolean isLandscape = false;

    //底部切换索引按钮
    private TextView lastPhotoImageView;
    private TextView nextPhotoImageView;
    private TextView currentPhotoIndexTextView;
    private TextView currentPhotoInfoTextView;
    private TextView currentPhotoADTextView;

    private CustomProgressDialog dialog;// 等待加载视图
    private Bitmap oriBlurBmp = null;// 原图的模糊图
    private Bitmap oriClearBmp = null;// 原图
    private Bitmap touchClearBmp = null;// 圆圈图
    private Bitmap zoomBlurBmp = null;// 放大后的模糊图
    private Bitmap zoomClearBmp = null;// 放大后的清晰图
    private Bitmap maskBmp = null;// mask蒙板
    private ImageView image01;// 模糊图的view
    private ImageView image02;// 圆圈的view

    private RelativeLayout photoFraRelativeLayout;
    private RelativeLayout blurFraRelativeLayout;

    private int marginTop = 0;//图片上端与屏幕顶部的距离
    private Matrix matrix;
    private boolean flag = false;// 是否是放大的
    private int count = 0;// 点击计数器
    private long fir = 0;// 第一次点击屏幕的时间
    private long sec = 0;// 第二次点击屏幕的时间
    private float downX = 0;// 点击屏幕的X坐标
    private float downY = 0;// 点击屏幕的Y坐标
    private boolean out = false;// 是否移动到屏幕边缘

    private Dialog dia;
    private TextView buy_ppp, cancel, buynow, use_ppp, touchtoclean;
    private File dirFile;

    private Date date;
    private SimpleDateFormat simpleDateFormat;
    private CartItemInfoJson cartItemInfoJson;//存放意见购买后的购物信息

    /**
     * 照片已购买情况下
     */
    private int mode = 0;// 初始状态
    private static final int MODE_DOWN = 111;
    private static final int MODE_MOVE = 222;
    private static final int MODE_UP = 333;

    private static final int LOAD_FROM_LOCAL = 444;
    private static final int LOAD_FROM_NETWORK = 555;
    private static final int CHECK_FAVORITE = 666;
    private static final int GET_FAVORITE_DATA_DONE = 1000;
    private static final int GET_LOCATION_AD = 777;
    private static final int GET_LOCATION_AD_DONE = 1001;
    private static final int CREATE_BLUR_DIALOG = 888;
    private static final int RESIZE_BLUR_IMAGE = 999;
    private static final int NO_PHOTOS_AND_RETURN = 1002;
    private static final int MAX_SPEED = 6000;

    private CustomDialog customdialog; //  对话框
    private PictureWorksDialog pictureWorksDialog;


    /**
     * 双击放大需要的尺寸，为预览容易的一半
     */
    private int zoomW = 0;
    private int zoomH = 0;

    /**
     * 图片预览容器的宽高
     */
    private float parentPreviewH;
    private float parentPreviewW;

    /**
     * 标准圆半径
     */
    private int originalRadius = 0;

    /**
     * 当前圆的半径
     */
    private int curRadius = 0;

    private RelativeLayout leadView;

    private boolean loadFailed = false;

    private List<GoodsInfo> allGoodsList;//全部商品
    private GoodsInfo pppGoodsInfo;
    private String[] photoUrls;

    /**
     * 当前圆圈的位置
     */
    private int x;
    private int y;
    /**
     * 放大的时候底层图片的位置
     */
    private int x1;
    private int y1;

    /**
     * 当前图片的宽高
     */
    private int curShowBmpWidth = 0;
    private int curShowBmpHeight = 0;

    /**
     * 超出屏幕的时候，每次移动的距离
     */
    private int moveSize = 30;

    private Handler previewPhotoHandler;

    /**
     * 速度监听
     */
    private VelocityTracker vTracker = null;
    private volatile String touchSpeet = "";

    long time = 0;

    /**
     * 处理Message
     *
     * @param msg
     */
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1://移动的时候
                //计算双击变量
                previewPhotoHandler.sendEmptyMessageDelayed(6, 500);
                /**
                 * 重新计算curRadius
                 */
                curRadius = BlurUtil.caculateRadius(curRadius, curShowBmpWidth, curShowBmpHeight);

                PictureAirLog.out("larger bmp h---->" + curShowBmpHeight);
                PictureAirLog.out("larger bmp w---->" + curShowBmpWidth);
                PictureAirLog.out("current radius---->" + curRadius);

                x = (int) (msg.arg1 - curRadius - (parentPreviewW - curShowBmpWidth) / 2);
                y = (int) (msg.arg2 - 2 * curRadius + 20 - (isLandscape ? 0 : marginTop) - (parentPreviewH - curShowBmpHeight) / 2);
                if (x > curShowBmpWidth - 2 * curRadius) {
                    x = curShowBmpWidth - 2 * curRadius;
                    x1 += moveSize;
                    out = true;
                }
                if (x < 0) {
                    x = 0;
                    x1 -= moveSize;
                    out = true;
                }
                if (y > curShowBmpHeight - 2 * curRadius) {
                    y = curShowBmpHeight - 2 * curRadius;
                    y1 += moveSize;
                    out = true;
                }
                if (y < 0) {
                    y = 0;
                    y1 -= moveSize;
                    out = true;
                }

                x1 = BlurUtil.caculateStartCropXOrY(x1, curShowBmpWidth, zoomW, oriBlurBmp.getWidth(), true);
                y1 = BlurUtil.caculateStartCropXOrY(y1, curShowBmpHeight, zoomH, oriBlurBmp.getHeight(), true);

                PictureAirLog.out("oriblur---?" + oriBlurBmp.getWidth() + " ---  " + oriBlurBmp.getHeight());
                PictureAirLog.out("x1---" + x1 + " y1---  " + y1);
                PictureAirLog.out("zoomW---" + zoomW);

                if (flag) {//放大
                    if (out) {//超出屏幕
                        cropNewBmt();
                    }
                    image01.setImageBitmap(zoomBlurBmp);
                }

                touchClearBmp = Bitmap.createBitmap(flag ? zoomClearBmp : oriClearBmp, x, y, 2 * curRadius, 2 * curRadius);
                touchClearBmp = BlurUtil.doMask(touchClearBmp, maskBmp);
                touchClearBmp = BlurUtil.toRoundBitmap(touchClearBmp);

                if (!image02.isShown()) {
                    image02.setVisibility(View.VISIBLE);
                }
                image02.setX(x);
                image02.setY(y);
                image02.setImageBitmap(touchClearBmp);
                out = false;
                break;

            case 2://取消移动的时候
                if (image02.isShown()) {
                    image02.setVisibility(View.GONE);
                }

                if (null != touchClearBmp) {
                    touchClearBmp.recycle();
                    touchClearBmp = null;
                }

                long thisTime = System.currentTimeMillis();
                if (null != msg.obj && !msg.obj.equals("") && thisTime - time > 1000){
                    time = System.currentTimeMillis();
                    if (msg.obj.equals("indexLast")){
                        indexLast();
                    }else{
                        indexNext();
                    }
                    touchSpeet = "";
                }else{
                    PictureAirLog.out("the obj : "+msg.obj);
                }
                break;

            case 3://双击放大
                x1 = msg.arg1 - zoomW / 2;
                y1 = msg.arg2 - zoomH / 2;
                PictureAirLog.v(TAG, "current xy = " + x1 + "+" + y1);
                x1 = BlurUtil.caculateStartCropXOrY(x1, curShowBmpWidth, zoomW, oriBlurBmp.getWidth(), false);
                y1 = BlurUtil.caculateStartCropXOrY(y1, curShowBmpHeight, zoomH, oriBlurBmp.getHeight(), false);
                PictureAirLog.v(TAG, "after currnet xy = " + x1 + "_" + y1);

                if (!flag) {
                    cropNewBmt();
                    image01.setImageBitmap(zoomBlurBmp);
                    curShowBmpWidth = zoomClearBmp.getWidth();
                    curShowBmpHeight = zoomClearBmp.getHeight();
                    flag = true;
                } else {
                    image01.setImageBitmap(oriBlurBmp);
                    flag = false;
                    curShowBmpWidth = oriClearBmp.getWidth();
                    curShowBmpHeight = oriClearBmp.getHeight();
                }
                curRadius = originalRadius;
                PictureAirLog.out("larger bmp h after zoom---->" + curShowBmpHeight);
                PictureAirLog.out("larger bmp w after zoom---->" + curShowBmpWidth);
                break;

            case SharePop.TWITTER:
                shareType = msg.what;
                break;

            case API1.BUY_PHOTO_SUCCESS:
                if (dialog.isShowing()) {
                    PictureAirLog.out("dismiss--->buy photo success");
                    dialog.dismiss();
                }
                cartItemInfoJson = JsonTools.parseObject((JSONObject) msg.obj, CartItemInfoJson.class);//CartItemInfoJson.getString()
                PictureAirLog.v(TAG, "BUY_PHOTO_SUCCESS" + cartItemInfoJson.toString());
                //将当前购买的照片信息存放到application中
                myApplication.setIsBuyingPhotoInfo(photolist.get(currentPosition).photoId, tabName);
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
                Editor editor = sharedPreferences.edit();
                editor.putInt(Common.CART_COUNT, sharedPreferences.getInt(Common.CART_COUNT, 0) + 1);
                editor.commit();
                intent.putExtra("orderinfo", orderinfo);
//                intent.putExtra("isBack", "1");//取消付款后是否回到当前页面
                startActivity(intent);
                break;

            case API1.BUY_PHOTO_FAILED:
                //购买失败
                if (dialog.isShowing()) {
                    PictureAirLog.out("dismiss--->by foto fild");
                    dialog.dismiss();
                }
                newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);
                break;

            case API1.GET_GOODS_SUCCESS:
                GoodsInfoJson goodsInfoJson = JsonTools.parseObject(msg.obj.toString(), GoodsInfoJson.class);//GoodsInfoJson.getString()
                if (goodsInfoJson != null && goodsInfoJson.getGoods().size() > 0) {
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
                if (dialog.isShowing()) {
                    PictureAirLog.out("dismiss--->get gds fild");
                    dialog.dismiss();
                }
                newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);
                break;

            case API1.ADD_TO_CART_FAILED:
                if (dialog.isShowing()) {
                    PictureAirLog.out("dismiss--->add to crt fild");
                    dialog.dismiss();
                }
                newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);

                break;

            case API1.ADD_TO_CART_SUCCESS:
                if (dialog.isShowing()) {
                    PictureAirLog.out("dismiss--->add cart succe");
                    dialog.dismiss();
                }
                JSONObject jsonObject = (JSONObject) msg.obj;
                editor = sharedPreferences.edit();
                editor.putInt(Common.CART_COUNT, sharedPreferences.getInt(Common.CART_COUNT, 0) + 1);
                editor.commit();
                String cartId = jsonObject.getString("cartId");

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
//                intent1.putExtra("isBack", "1");//取消付款后是否回到当前页面
                startActivity(intent1);
                break;

            case 6://计算双击方法事件
                if (count > 0) {
                    count = 0;
                }
                break;

            case 7://操作比较耗时，会影响oncreate绘制
                getPhotoInfoSuccess = true;
                mViewPager = (GalleryViewPager) findViewById(R.id.viewer);
                UrlPagerAdapter pagerAdapter = new UrlPagerAdapter(PreviewPhotoActivity.this, photolist);
                mViewPager.setOffscreenPageLimit(2);
                mViewPager.setAdapter(pagerAdapter);
                mViewPager.setCurrentItem(currentPosition, true);
                //初始化底部索引按钮
                updateIndexTools(true);

                PictureAirLog.v(TAG, "----------------------->initing...3");

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
                        //				PictureAirLog.v(TAG,"--------onPageScrollStateChanged-------"+arg0);

                        PictureAirLog.v(TAG, "----------------------->initing...5");
                        if (arg0 == 0) {//结束滑动
                            //					PictureAirLog.v(TAG,"--------scroll end-------");
                            updateIndexTools(false);//只能写在这里，不能写在onPageSelected，不然出现切换回来之后，显示错乱
                            setUmengPhotoSlide();//统计滑动图片次数
                        }
                    }
                });

                PictureAirLog.v(TAG, "----------------------->initing...6");
//                judgeBuyOnePhoto(); // 根据需求， 取消了购买的流程 的同步流程
                break;

            case LOAD_FROM_NETWORK:
                //添加模糊
                if (dialog.isShowing()) {
                    PictureAirLog.out("dismiss--->network");
                    dialog.dismiss();
                }
                if (null != oriClearBmp) {
                    PictureAirLog.v(TAG, "bitmap 2 not null");
                    initBlur();
                } else {
                    PictureAirLog.v(TAG, "oriClearBmp null-->");
                    loadFailed = true;
                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                }
                PictureAirLog.out("set enable in network");
                lastPhotoImageView.setEnabled(true);
                nextPhotoImageView.setEnabled(true);
                break;

            case LOAD_FROM_LOCAL:
                byte[] arg2 = null;
                try {
                    arg2 = AESKeyHelper.decrypt(dirFile.toString(), PWJniUtil.getAESKey(Common.APP_TYPE_SHDRPP));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (null != arg2)
                    oriClearBmp = BitmapFactory.decodeByteArray(arg2, 0, arg2.length);
                if (dialog.isShowing()) {
                    PictureAirLog.out("dismiss--->local");
                    dialog.dismiss();
                }
                if (null != oriClearBmp) {
                    initBlur();
                }
                PictureAirLog.out("set enable in local");
                lastPhotoImageView.setEnabled(true);
                nextPhotoImageView.setEnabled(true);
                loadPhotoSuccess = true;
                break;

            case GET_LOCATION_AD:
                currentPhotoADTextView.setVisibility(View.GONE);
                final int oldPositon = msg.arg1;
                if (myApplication.isGetADLocationSuccess()) {
                    //从数据库中查找
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String adStr = pictureAirDbManager.getADByLocationId(photoInfo.locationId, MyApplication.getInstance().getLanguageType());
                            previewPhotoHandler.obtainMessage(GET_LOCATION_AD_DONE, oldPositon, 0, adStr).sendToTarget();
                        }
                    }).start();

                } else {
                    //从网络获取
                    API1.getADLocations(oldPositon, previewPhotoHandler);
                }
                if (dialog.isShowing()) {
                    PictureAirLog.out("dismiss--->ad");
                    dialog.dismiss();
                }
                break;

            case GET_LOCATION_AD_DONE:
                if (msg.arg1 == currentPosition && !msg.obj.toString().equals("")) {//如果获取的对应索引值，依旧是当期的索引值，则显示广告
                    PictureAirLog.out("current position need show ad");
                    currentPhotoADTextView.setVisibility(View.VISIBLE);
                    currentPhotoADTextView.setText(msg.obj.toString());
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
                        String adString = pictureAirDbManager.insertADLocations(adJsonObject.getJSONArray("locations"),
                            photoInfo.locationId, MyApplication.getInstance().getLanguageType());
                        previewPhotoHandler.obtainMessage(GET_LOCATION_AD_DONE, oldPosition1, 0, adString).sendToTarget();
                    }
                }).start();
                break;

            case API1.GET_AD_LOCATIONS_FAILED:
                break;

            case CHECK_FAVORITE://开始获取收藏信息
                final int oldPosition = msg.arg1;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        previewPhotoHandler.obtainMessage(GET_FAVORITE_DATA_DONE, oldPosition, 0,
                                pictureAirDbManager.checkLovePhoto(photoInfo, sharedPreferences.getString(Common.USERINFO_ID, ""))).sendToTarget();
                    }
                }).start();
                break;

            case GET_FAVORITE_DATA_DONE://获取数据成功
                //更新收藏图标
                if (Boolean.valueOf(msg.obj.toString()) && msg.arg1 == currentPosition) {//数据库查询的数据是true，并且对应的index还是之前的位置
                    PictureAirLog.out("current postion and is favorite");
                    photoInfo.isLove = 1;
                    loveImageButton.setImageResource(R.drawable.discover_like);
                } else {
                    PictureAirLog.out("not the favorite");
                    loveImageButton.setImageResource(R.drawable.discover_no_like);
                }
                break;

            case CREATE_BLUR_DIALOG:
                createBlurDialog();
                break;

            case RESIZE_BLUR_IMAGE:
                if (getPhotoInfoSuccess && loadPhotoSuccess) {
                    if (photoInfo.onLine == 1 && photoInfo.isPayed == 0) {//模糊图需要重新修改大小
                        if (null != oriClearBmp) {
                            resizeBlurImage();
                        }
                    }
                } else {
                    previewPhotoHandler.sendEmptyMessageDelayed(RESIZE_BLUR_IMAGE, 200);
                }
                break;

            case API1.GET_PPPS_BY_SHOOTDATE_SUCCESS:  //根据已有PP＋升级
                if (API1.PPPlist.size() > 0) {

//                    if (AppManager.getInstance().checkActivity(MyPPActivity.class)){ //如果存在MyPPActivity，就把这个类杀掉。
//                        AppManager.getInstance().killActivity(MyPPActivity.class);
//                    }
                    //将 tabname 存入sp
                    SharedPreferences.Editor editor1 = sharedPreferences.edit();  //设置需要刷新
                    editor1.putString("tabName", tabName);
                    editor1.putInt("currentPosition", currentPosition);
                    editor1.commit();

                    dia.dismiss();

                    intent = new Intent(PreviewPhotoActivity.this, SelectPPActivity.class);
                    intent.putExtra("photoPassCode",photoInfo.photoPassCode);
                    intent.putExtra("shootTime",photoInfo.shootTime);
//                    intent.putExtra("isUseHavedPPP", true);
                    startActivity(intent);
                } else {
                    newToast.setTextAndShow(R.string.no_ppp_tips, Common.TOAST_SHORT_TIME);
                }
                break;

            case API1.GET_PPPS_BY_SHOOTDATE_FAILED:
                newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);
                break;

            case NO_PHOTOS_AND_RETURN://没有图片
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
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
        previewPhotoHandler = new Handler(this);
        pictureAirDbManager = new PictureAirDbManager(this);
        settingUtil = new SettingUtil(pictureAirDbManager);
        newToast = new PWToast(this);
        sharePop = new SharePop(this);
        matrix = new Matrix();
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        PictureAirLog.out("oncreate----->2");
        dialog = CustomProgressDialog.create(this, getString(R.string.is_loading), false, null);
        sharedPreferences = getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, MODE_PRIVATE);
        returnImageView = (ImageView) findViewById(R.id.button1_shop_rt);

        locationTextView = (TextView) findViewById(R.id.preview_location);
        editButton = (TextView) findViewById(R.id.preview_edit);
        shareButton = (TextView) findViewById(R.id.preview_share);
        downloadButton = (TextView) findViewById(R.id.preview_download);
        makegiftButton = (TextView) findViewById(R.id.preview_makegift);
        loveImageButton = (ImageButton) findViewById(R.id.preview_love);

        lastPhotoImageView = (TextView) findViewById(R.id.index_last);
        nextPhotoImageView = (TextView) findViewById(R.id.index_next);
        currentPhotoInfoTextView = (TextView) findViewById(R.id.index_time);
        currentPhotoIndexTextView = (TextView) findViewById(R.id.current_index);
        currentPhotoADTextView = (TextView) findViewById(R.id.preview_photo_ad_intro_tv);

        image01 = (ImageView) findViewById(R.id.img01);
        leadView = (RelativeLayout) findViewById(R.id.blur_lead_view);
        touchtoclean = (TextView) findViewById(R.id.textview_blur);
        blurFraRelativeLayout = (RelativeLayout) findViewById(R.id.blur_photo_relativelayout);
        photoFraRelativeLayout = (RelativeLayout) findViewById(R.id.fra_layout);

        titleBar = (RelativeLayout) findViewById(R.id.preview_titlebar);
        toolsBar = (LinearLayout) findViewById(R.id.toolsbar);
        indexBar = (LinearLayout) findViewById(R.id.index_bar);

        image02 = (ImageView) findViewById(R.id.img02);

        previewPhotoHandler.sendEmptyMessage(CREATE_BLUR_DIALOG);

        myApplication = (MyApplication) getApplication();

        returnImageView.setOnClickListener(this);
        editButton.setOnClickListener(this);
        shareButton.setOnClickListener(this);
        downloadButton.setOnClickListener(this);
        makegiftButton.setOnClickListener(this);
        loveImageButton.setOnClickListener(this);

        lastPhotoImageView.setOnClickListener(this);
        nextPhotoImageView.setOnClickListener(this);
        PictureAirLog.v(TAG, "----------------------->initing...1");

        Configuration cf = getResources().getConfiguration();
        int ori = cf.orientation;
        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
            isLandscape = true;
            landscapeOrientation();
        }
        originalRadius = 120;
        curRadius = originalRadius;
        dialog.show();
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
                                pictureAirDbManager, simpleDateFormat.format(new Date(cacheTime)),
                                simpleDateFormat, MyApplication.getInstance().getLanguageType()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                } else if (tabName.equals("photopass")) {//获取pp图片
                    locationList.addAll(AppUtil.getLocation(PreviewPhotoActivity.this, ACache.get(PreviewPhotoActivity.this).getAsString(Common.DISCOVER_LOCATION), true));
                    try {
                        photolist.addAll(AppUtil.getSortedPhotoPassPhotos(locationList, pictureAirDbManager,
                                simpleDateFormat.format(new Date(cacheTime)), simpleDateFormat, MyApplication.getInstance().getLanguageType(), false));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                } else if (tabName.equals("local")) {//获取本地图片
                    photolist.addAll(targetphotolist);

                } else if (tabName.equals("bought")) {//获取已经购买的图片
                    locationList.addAll(AppUtil.getLocation(PreviewPhotoActivity.this, ACache.get(PreviewPhotoActivity.this).getAsString(Common.DISCOVER_LOCATION), true));
                    try {
                        photolist.addAll(AppUtil.getSortedPhotoPassPhotos(locationList, pictureAirDbManager,
                                simpleDateFormat.format(new Date(cacheTime)), simpleDateFormat, MyApplication.getInstance().getLanguageType(), true));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                } else if (tabName.equals("favourite")) {//获取收藏图片
                    locationList.addAll(AppUtil.getLocation(PreviewPhotoActivity.this, ACache.get(PreviewPhotoActivity.this).getAsString(Common.DISCOVER_LOCATION), true));
                    photolist.addAll(AppUtil.insterSortFavouritePhotos(
                            pictureAirDbManager.getFavoritePhotoInfoListFromDB(PreviewPhotoActivity.this, sharedPreferences.getString(Common.USERINFO_ID, ""), simpleDateFormat.format(new Date(cacheTime)), locationList, MyApplication.getInstance().getLanguageType())));

                } else if (tabName.equals("editStory")){//编辑PP照片页面
                    String ppCode = bundle.getString("ppCode");
                    locationList.addAll(AppUtil.getLocation(PreviewPhotoActivity.this, ACache.get(PreviewPhotoActivity.this).getAsString(Common.DISCOVER_LOCATION), true));
                    photolist.addAll(AppUtil.insterSortFavouritePhotos(
                            pictureAirDbManager.getPhotoInfosByPPCode(ppCode, locationList, MyApplication.getInstance().getLanguageType())));

                } else {//获取列表图片
                    ArrayList<PhotoInfo> temp = bundle.getParcelableArrayList("photos");//获取图片路径list
                    if (temp != null) {
                        photolist.addAll(temp);
                    }
                }

                if (currentPosition == -1) {//购买图片后返回
                    String photoId = bundle.getString("photoId", "");
                    PictureAirLog.out("photoid--->" + photoId);
                    for (int i = 0; i < photolist.size(); i++) {
                        PictureAirLog.out("photoinfo.photoid----->" + photolist.get(i).photoId);
                        if (TextUtils.isEmpty(photolist.get(i).photoId)) {//本地图片，没有PhotoId，需要过滤

                        } else if (photolist.get(i).photoId.equals(photoId)){
                            photolist.get(i).isPayed = 1;
                            currentPosition = i;
                            break;
                        }
                    }
                }

                if (currentPosition == -2) {//绑定PP后返回
                    String ppsStr = bundle.getString("ppsStr");
                    refreshPP(photolist,ppsStr);
                    currentPosition = sharedPreferences.getInt("currentPosition",0);
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
                PhotoInfo currentPhotoInfo = photolist.get(currentPosition);

                PictureAirLog.out("photolist size ---->" + photolist.size());
                Iterator<PhotoInfo> photoInfoIterator = photolist.iterator();
                while (photoInfoIterator.hasNext()) {
                    PhotoInfo info = photoInfoIterator.next();
                    if (info.isVideo == 1) {
                        photoInfoIterator.remove();
                    }
                }
                PictureAirLog.out("photolist size ---->" + photolist.size());
                PictureAirLog.out("currentPosition ---->" + currentPosition);
                currentPosition = photolist.indexOf(currentPhotoInfo);
                PictureAirLog.out("currentPosition ---->" + currentPosition);
                PictureAirLog.v(TAG, "photo size is " + photolist.size());
                PictureAirLog.v(TAG, "thumbnail is " + photolist.get(currentPosition).photoThumbnail);
                PictureAirLog.v(TAG, "thumbnail 512 is " + photolist.get(currentPosition).photoThumbnail_512);
                PictureAirLog.v(TAG, "thumbnail 1024 is " + photolist.get(currentPosition).photoThumbnail_1024);
                PictureAirLog.v(TAG, "original is " + photolist.get(currentPosition).photoPathOrURL);
                PictureAirLog.v(TAG, "----------------------->initing...2");
                previewPhotoHandler.sendEmptyMessage(7);
            }
        }.start();
    }

    private void createBlurDialog() {
        dia = new Dialog(this, R.style.dialogTans);
        Window window = dia.getWindow();
        window.setGravity(Gravity.CENTER);
        //		window.setWindowAnimations(R.style.from_bottom_anim);
        dia.setCanceledOnTouchOutside(true);
        View view = View.inflate(this, R.layout.tans_dialog, null);
        dia.setContentView(view);
        WindowManager.LayoutParams layoutParams = dia.getWindow().getAttributes();
        layoutParams.width = ScreenUtil.getScreenWidth(this);
        dia.getWindow().setAttributes(layoutParams);

        buy_ppp = (TextView) dia.findViewById(R.id.buy_ppp);
        cancel = (TextView) dia.findViewById(R.id.cancel);
        buynow = (TextView) dia.findViewById(R.id.buynow);
        use_ppp = (TextView) dia.findViewById(R.id.use_ppp);
        buynow.setOnClickListener(this);
        buy_ppp.setOnClickListener(this);
        cancel.setOnClickListener(this);
        use_ppp.setOnClickListener(this);
    }

    /**
     * 加载网络图片
     *
     * @param loadPhotoInfo 需要加载的网络图片
     */
    private void loadPhotoPassPhoto(PhotoInfo loadPhotoInfo, boolean isOnCreate) {
        // TODO Auto-generated method stub
        loadPhotoSuccess = false;
        dirFile = new File(getApplicationContext().getCacheDir() + "/" + loadPhotoInfo.photoId);//创建一个以ID为名字的文件，放入到app缓存文件下
        PictureAirLog.v(TAG, dirFile.toString());
        PictureAirLog.v(TAG, "photo URL ------->" + loadPhotoInfo.photoThumbnail_1024);
        if (dirFile.exists()) {//如果文件存在
            PictureAirLog.v(TAG, "file exists");
            if (isOnCreate) {
                //创建一个画图的监听，等到这个控件画好之后，触发监听函数，同时移除对应的监听
                ViewTreeObserver viewTreeObserver = photoFraRelativeLayout.getViewTreeObserver();
                viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        photoFraRelativeLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        previewPhotoHandler.sendEmptyMessageDelayed(LOAD_FROM_LOCAL, 200);
                    }
                });

            } else {
                previewPhotoHandler.sendEmptyMessageDelayed(LOAD_FROM_LOCAL, 200);
            }
        } else {//如果文件不存在，下载文件到缓存
            PictureAirLog.v(TAG, "file is not exist");
            HttpUtil1.asyncDownloadBinaryData(loadPhotoInfo.photoThumbnail_1024, new HttpCallback() {
                @Override
                public void onSuccess(byte[] binaryData) {
                    super.onSuccess(binaryData);
                    try {
                        AESKeyHelper.encrypt(binaryData, dirFile.toString(), PWJniUtil.getAESKey(Common.APP_TYPE_SHDRPP));
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    oriClearBmp = BitmapFactory.decodeByteArray(binaryData, 0, binaryData.length);
                    previewPhotoHandler.sendEmptyMessage(LOAD_FROM_NETWORK);
                    loadPhotoSuccess = true;
                }

                @Override
                public void onFailure(int status) {
                    super.onFailure(status);
                    previewPhotoHandler.sendEmptyMessage(LOAD_FROM_NETWORK);
                    loadPhotoSuccess = true;
                }
            });
        }
    }

    /**
     * 更新底部索引工具
     */
    private void updateIndexTools(boolean isOnCreate) {
        PictureAirLog.v(TAG, "updateIndexTools-------->" + currentPosition);
        //初始化图片收藏按钮，需要判断isLove=1或者是否在数据库中
        if (isEdited) {
            photoInfo = targetphotolist.get(currentPosition);
        } else {//编辑前
            photoInfo = photolist.get(currentPosition);
        }
        previewPhotoHandler.obtainMessage(CHECK_FAVORITE, currentPosition, 0).sendToTarget();

        //更新title地点名称
        locationTextView.setText(photoInfo.locationName);

        //更新序列号
        currentPhotoIndexTextView.setText(String.format(getString(R.string.photo_index), currentPosition + 1, isEdited ? targetphotolist.size() : photolist.size()));
        currentPhotoInfoTextView.setText(photoInfo.shootOn.substring(0, 16));
        //更新上一张下一张按钮
        if (currentPosition == 0) {
            lastPhotoImageView.setVisibility(View.INVISIBLE);
        } else {
            lastPhotoImageView.setVisibility(View.VISIBLE);
        }
        if (currentPosition == (isEdited ? targetphotolist.size() - 1 : photolist.size() - 1)) {
            nextPhotoImageView.setVisibility(View.INVISIBLE);
        } else {
            nextPhotoImageView.setVisibility(View.VISIBLE);
        }

        //如果是未购买图片，判断是否是第一次进入，如果是，则显示引导图层
        if (photoInfo.isPayed == 0 && photoInfo.onLine == 1) {//未购买的图片
            PictureAirLog.v(TAG, "need show blur view");
            image01.setVisibility(View.INVISIBLE);
            if (!dialog.isShowing()) {
                dialog.show();
            }
            touchtoclean.setVisibility(View.VISIBLE);
            blurFraRelativeLayout.setVisibility(View.VISIBLE);
            currentPhotoADTextView.setVisibility(View.GONE);
            loadPhotoPassPhoto(photoInfo, isOnCreate);
        } else if (photoInfo.isPayed == 1 && photoInfo.onLine == 1) {
            previewPhotoHandler.obtainMessage(GET_LOCATION_AD, currentPosition, 0).sendToTarget();
            PictureAirLog.out("set enable in get ad");
            lastPhotoImageView.setEnabled(true);
            nextPhotoImageView.setEnabled(true);
        } else {
            currentPhotoADTextView.setVisibility(View.GONE);
            PictureAirLog.out("set enable in other conditions");
            lastPhotoImageView.setEnabled(true);
            nextPhotoImageView.setEnabled(true);
            if (dialog.isShowing()) {
                PictureAirLog.out("dismiss--->other");
                dialog.dismiss();
            }
        }

        if (isLandscape) {//横屏模式
            if (mViewPager != null) {
                mViewPager.setBackgroundColor(Color.BLACK);
            }
            touchtoclean.setTextColor(getResources().getColor(R.color.white));
            touchtoclean.setShadowLayer(2, 2, 2, getResources().getColor(R.color.pp_dark_blue));
        } else {
            touchtoclean.setTextColor(getResources().getColor(R.color.pp_dark_blue));
            touchtoclean.setShadowLayer(2, 2, 2, getResources().getColor(R.color.transparent));
        }
    }

    private long touchDownTime = 0;
    private long touchUpTime = 0;

    public boolean onTouchEvent(MotionEvent event) {
        PictureAirLog.out("the-----onTouchEvent");
        if (!loadFailed) {
            if (photoInfo.isPayed == 0 && photoInfo.onLine == 1) {// 未购买状态
                if (event.getY() < marginTop || event.getY() > parentPreviewH + marginTop) {
                    if (mode != MODE_MOVE) {
                        Message msg = previewPhotoHandler.obtainMessage();
                        msg.what = 2;
                        touchtoclean.setVisibility(View.VISIBLE);
                        previewPhotoHandler.sendMessage(msg);
                        return super.onTouchEvent(event);
                    }
                }

                Message msg = previewPhotoHandler.obtainMessage();
                msg.arg1 = (int) event.getX();
                msg.arg2 = (int) event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touchDownTime = System.currentTimeMillis();
                        mode = MODE_DOWN;
                        PictureAirLog.v(TAG, "-------->downY---" + event.getY());
                        downX = event.getX();
                        downY = event.getY();

                        count++;
                        touchtoclean.setVisibility(View.INVISIBLE);
                        if (count == 1) {
                            fir = System.currentTimeMillis();
                            msg.what = 1;
                        }
                        if (count == 2) {
                            sec = System.currentTimeMillis();
                            if (sec - fir < 500) {// 双击放大
                                msg.what = 3;
                                count = 0;
                                fir = 0;
                                sec = 0;
                            } else {
                                count = 0;
                            }
                        }

                        /** 测速 */
                        if(vTracker == null){
                            vTracker = VelocityTracker.obtain();
                        }else{
                            vTracker.clear();
                        }
                        vTracker.addMovement(event);

                        break;
                    case MotionEvent.ACTION_MOVE:
                        mode = MODE_MOVE;
                        PictureAirLog.v(TAG, "----->move");
                        float moveX = event.getX() - downX;
                        float moveY = event.getY() - downY;
                        if (moveX != 0 | moveY != 0) {
                            msg.what = 1;
                            count = 0;
                            fir = 0;
                            sec = 0;
                        }

                        if (vTracker != null) {
                            vTracker.addMovement(event);
                            vTracker.computeCurrentVelocity(1000);
                            PictureAirLog.out("vTracker----> the x velocity is "+vTracker.getXVelocity());
                            PictureAirLog.out("vTracker----> the y velocity is "+vTracker.getYVelocity());
                            if (vTracker.getXVelocity() > MAX_SPEED){
                                touchSpeet = "indexLast";
                                PictureAirLog.out("vTracker----> the -----<");
                            } else if (vTracker.getXVelocity() < -MAX_SPEED){
                                touchSpeet = "indexNext";
                                PictureAirLog.out("vTracker----> the ----->");
                            }
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        touchUpTime = System.currentTimeMillis();

                        if (vTracker != null) {
                            vTracker.clear();
                            vTracker.recycle();
                            vTracker = null;
                        }

                        mode = MODE_UP;
                        PictureAirLog.v(TAG, "up");
                        if (touchUpTime - touchDownTime < 200){
                            msg.obj = touchSpeet;
                        } else {
                            touchSpeet = "";
                        }
                        msg.what = 2;
                        touchtoclean.setVisibility(View.VISIBLE);
                        break;
                }
                previewPhotoHandler.sendMessage(msg);
            }
        }
        return true;
    }

    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.button1_shop_rt:
                if (leadView.isShown()) {
                    return;
                }
                finish();
                break;

            case R.id.preview_love://收藏按钮的操作
                if (leadView.isShown()) {
                    return;
                }
                if (isEdited) {
                    photoInfo = targetphotolist.get(mViewPager.getCurrentItem());
                } else {//编辑前
                    photoInfo = photolist.get(mViewPager.getCurrentItem());
                }
                if (photoInfo == null) {
                    return;
                }
                if (photoInfo.isLove == 1) {
                    PictureAirLog.d(TAG, "cancel love");
                    pictureAirDbManager.setPictureLove(photoInfo, sharedPreferences.getString(Common.USERINFO_ID, ""), false);
                    photoInfo.isLove = 0;
                    loveImageButton.setImageResource(R.drawable.discover_no_like);
                } else {
                    PictureAirLog.d(TAG, "add love");
                    pictureAirDbManager.setPictureLove(photoInfo, sharedPreferences.getString(Common.USERINFO_ID, ""), true);
                    photoInfo.isLove = 1;
                    loveImageButton.setImageResource(R.drawable.discover_like);
                }
                myApplication.needScanFavoritePhotos = true;
                break;

            case R.id.preview_edit://编辑
                if (leadView.isShown()) {
                    return;
                }
                if (photoInfo == null) {
                    return;
                }
                if (photoInfo.isPayed == 1) {
                    if (photoInfo.isHasPreset == 0) { // 如果没有模版，就去执行编辑操作。 如果有模版就弹出提示。
                        intent = new Intent(this, EditPhotoActivity.class);
                        if (isEdited) {//已经编辑过，取targetlist中的值
                            intent.putExtra("photo", targetphotolist.get(mViewPager.getCurrentItem()));
                        } else {//没有编辑，取正常的值
                            intent.putExtra("photo", photolist.get(mViewPager.getCurrentItem()));
                        }
                        startActivityForResult(intent, 1);
                    } else {
                        if (pictureWorksDialog == null) {
                            pictureWorksDialog = new PictureWorksDialog(PreviewPhotoActivity.this, null,
                                    getString(R.string.photo_cannot_edit_content), null,
                                    getString(R.string.photo_cannot_edit_yes), true, previewPhotoHandler);
                        }
                        pictureWorksDialog.show();
                    }
                } else {
                    if (loadFailed) {
                        newToast.setTextAndShow(R.string.reloadphoto, Common.TOAST_SHORT_TIME);
                    } else {
                        dia.show();
                    }

                }
                break;

            case R.id.preview_share:
                if (leadView.isShown()) {
                    return;
                }
                if (photoInfo == null) {
                    return;
                }
                if (photoInfo.isPayed == 1) {
                    dia.dismiss();
                    PictureAirLog.v(TAG, "start share=" + photolist.get(mViewPager.getCurrentItem()).photoPathOrURL);
                    if (isEdited) {//编辑后
                        sharePop.setshareinfo(targetphotolist.get(mViewPager.getCurrentItem()).photoPathOrURL, null, "local", null, SharePop.SHARE_PHOTO_TYPE, previewPhotoHandler);
                    } else {//编辑前
                        //判断图片是本地还是网路图片
                        if (photoInfo.onLine == 1) {//网络图片
                            sharePop.setshareinfo(null, photolist.get(mViewPager.getCurrentItem()).photoThumbnail_1024,
                                    "online", photolist.get(mViewPager.getCurrentItem()).photoId, SharePop.SHARE_PHOTO_TYPE, previewPhotoHandler);
                        } else {
                            sharePop.setshareinfo(photolist.get(mViewPager.getCurrentItem()).photoPathOrURL, null, "local", null, SharePop.SHARE_PHOTO_TYPE, previewPhotoHandler);
                        }

                    }
                    sharePop.showAtLocation(v, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                } else {
                    if (loadFailed) {
                        newToast.setTextAndShow(R.string.reloadphoto, Common.TOAST_SHORT_TIME);
                    } else {
                        dia.show();
                    }

                }
                break;

            case R.id.preview_download://下载,如果不是pp的照片，提示不需要下载，如果是pp的照片，并且没有支付，提示购买，如果已经购买，如果没有下载，则下载，否则提示已经下载
                if (leadView.isShown()) {
                    return;
                }
                if (AppUtil.getNetWorkType(PreviewPhotoActivity.this) == AppUtil.NETWORKTYPE_INVALID) {
                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    return;
                }
                if (photoInfo == null) {
                    return;
                }
                if (photoInfo.isPayed == 1) {
                    if (isEdited) {//编辑后
                        newToast.setTextAndShow(R.string.neednotdownload, Common.TOAST_SHORT_TIME);
                    } else {//编辑前
                        if (photoInfo.onLine == 1) {//是pp的照片
                            judgeOnePhotoDownloadFlow();
                        } else {
                            newToast.setTextAndShow(R.string.neednotdownload, Common.TOAST_SHORT_TIME);
                        }

                    }

                } else {
                    if (loadFailed) {
                        newToast.setTextAndShow(R.string.reloadphoto, Common.TOAST_SHORT_TIME);
                    } else {
                        dia.show();
                    }

                }

                break;

            case R.id.preview_makegift:
                if (leadView.isShown()) {
                    return;
                }

                if (photoInfo == null) {
                    return;
                }

                if (photoInfo.onLine == 0) {
                    newToast.setTextAndShow(R.string.local_photo_not_support_makegift, Common.TOAST_SHORT_TIME);
                    return;
                }

                if (photoInfo.locationId.equals("photoSouvenirs")) {//排除纪念照的照片
                    newToast.setTextAndShow(R.string.not_support_makegift, Common.TOAST_SHORT_TIME);
                    return;
                }

                if (photoInfo.isPayed == 0 && loadFailed) {
                    newToast.setTextAndShow(R.string.reloadphoto, Common.TOAST_SHORT_TIME);
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

            case R.id.buynow:
                if (AppUtil.getNetWorkType(PreviewPhotoActivity.this) == AppUtil.NETWORKTYPE_INVALID) {
                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    dia.dismiss();
                    return;
                }
                if (photoInfo == null) {
                    return;
                }
                if (!dialog.isShowing()) {
                    dialog.show();
                }
                API1.buyPhoto(photoInfo.photoId, previewPhotoHandler);
                dia.dismiss();
                break;

            case R.id.buy_ppp:
                //直接购买PP+
                if (AppUtil.getNetWorkType(PreviewPhotoActivity.this) == AppUtil.NETWORKTYPE_INVALID) {
                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    dia.dismiss();
                    return;
                }
                if (!dialog.isShowing()) {
                    dialog.show();
                }
                //获取商品
                getALlGoods();
                dia.dismiss();
                break;
            case R.id.use_ppp:
                if (photoInfo == null) {
                    return;
                }
                if (AppUtil.getNetWorkType(PreviewPhotoActivity.this) == AppUtil.NETWORKTYPE_INVALID) { //判断网络情况。
                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    dia.dismiss();
                    return;
                }else{
                    API1.getPPPsByShootDate(previewPhotoHandler, photoInfo.shootTime);
                }
                break;

            case R.id.leadknow:
            case R.id.blur_lead_view:
                PictureAirLog.v(TAG, "know");
                leadView.setVisibility(View.GONE);
                break;

            case R.id.index_last://上一张
                indexLast();
                break;

            case R.id.index_next://下一张
                indexNext();
                break;

            default:
                break;
        }
    }

    /**
     * 下一张
     */
    private void indexNext(){
        PictureAirLog.v(TAG, "--------->next");
        lastPhotoImageView.setEnabled(false);
        nextPhotoImageView.setEnabled(false);
        changeTab(true);
        setUmengPhotoSlide();//统计滑动图片次数
    }

    /**
     * 上一张
     */
    private void indexLast(){
        PictureAirLog.v(TAG, "--------->last");
        lastPhotoImageView.setEnabled(false);
        nextPhotoImageView.setEnabled(false);
        changeTab(false);
        setUmengPhotoSlide();//统计滑动图片次数
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

    /**
     * 左右滑动切换图片
     */
    private void changeTab(boolean next) {
        if (leadView.isShown()) {
            PictureAirLog.v(TAG, "leadView is shown----->");
            return;
        }
        if (next) {
            PictureAirLog.v(TAG, "--------->next");
            if (currentPosition < (isEdited ? targetphotolist.size() - 1 : photolist.size() - 1)) {
                currentPosition++;
            } else {
                return;
            }
        } else {
            PictureAirLog.v(TAG, "--------->last");
            if (currentPosition > 0) {
                currentPosition--;
            } else {
                return;
            }
        }
        mViewPager.setVisibility(View.VISIBLE);
        touchtoclean.setVisibility(View.GONE);
        blurFraRelativeLayout.setVisibility(View.GONE);
        mViewPager.setCurrentItem(currentPosition);
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
                selectPhotoItemInfo.photoPathOrURL = data.getStringExtra("photoUrl");
                File file = new File(selectPhotoItemInfo.photoPathOrURL);
                selectPhotoItemInfo.lastModify = file.lastModified();
                date = new Date(selectPhotoItemInfo.lastModify);
                selectPhotoItemInfo.shootOn = simpleDateFormat.format(date);
                selectPhotoItemInfo.shootTime = selectPhotoItemInfo.shootOn.substring(0, 10);
                selectPhotoItemInfo.isChecked = 0;
                selectPhotoItemInfo.isSelected = 0;
                selectPhotoItemInfo.showMask = 0;
                selectPhotoItemInfo.locationName = getString(R.string.story_tab_magic);
                //					selectPhotoItemInfo.albumName = albumName;
                selectPhotoItemInfo.onLine = 0;
                selectPhotoItemInfo.isUploaded = 0;
                selectPhotoItemInfo.isPayed = 1;
                selectPhotoItemInfo.isVideo = 0;
                selectPhotoItemInfo.isHasPreset = 0;

                //2.将新图片插入到targetList中
                targetphotolist.add(0, selectPhotoItemInfo);
                //3.修改viewPager中的值为targetList
                mViewPager.setAdapter(new UrlPagerAdapter(this, targetphotolist));
                mViewPager.setCurrentItem(0, true);
                currentPosition = 0;
                //4.更新底部工具栏
                isEdited = true;

                updateIndexTools(false);

//                currentPhotoIndexTextView.setText(String.format(getString(R.string.photo_index), currentPosition + 1, isEdited ? targetphotolist.size() : photolist.size()));
//                currentPhotoInfoTextView.setText(selectPhotoItemInfo.shootOn);
//                PictureAirLog.v(TAG, "targetphotolist size is " + targetphotolist.size());


//                fsa;
                myApplication.setneedScanPhoto(true);
                myApplication.scanMagicFinish = false;
                //				flag = 0;
                //5,更新 标题栏。
//                locationTextView.setText(R.string.magic_location);
            }
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        //如果手指在上面的时候，如果同时休眠，在唤醒之后，页面上有个清晰圈
        //需要通知handler释放清晰圈
        if (photoInfo != null && photoInfo.isPayed == 0 && photoInfo.onLine == 1) {
            previewPhotoHandler.sendEmptyMessage(2);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PictureAirLog.v(TAG, "----------->" + myApplication.getRefreshViewAfterBuyBlurPhoto());
        if (photoInfo != null && photoInfo.isPayed == 0 && photoInfo.onLine == 1) {
            if (myApplication.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_MYPHOTOPASSPAYED)) {

            } else if (myApplication.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_VIEWORSELECTACTIVITYANDPAYED)) {

            } else if (myApplication.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_PREVIEW_PHOTO_ACTIVITY_PAY)) {

            } else {
                myApplication.setRefreshViewAfterBuyBlurPhoto("");
            }
            //按返回，把状态全部清除
            myApplication.clearIsBuyingPhotoList();
        }
        previewPhotoHandler.removeCallbacksAndMessages(null);

        if (oriBlurBmp != null) {
            oriBlurBmp.recycle();
            oriBlurBmp = null;
        }
        if (oriClearBmp != null) {
            oriClearBmp.recycle();
            oriClearBmp = null;
        }
        if (touchClearBmp != null) {
            touchClearBmp.recycle();
            touchClearBmp = null;
        }
        if (zoomBlurBmp != null) {
            zoomBlurBmp.recycle();
            zoomBlurBmp = null;
        }
        if (zoomClearBmp != null) {
            zoomClearBmp.recycle();
            zoomClearBmp = null;
        }
        if (maskBmp != null) {
            maskBmp.recycle();
            maskBmp = null;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            PictureAirLog.out("landscape----->");
            landscapeOrientation();
        } else {
            PictureAirLog.out("portrait----->");
            portraitOrientation();
        }

        previewPhotoHandler.sendEmptyMessage(RESIZE_BLUR_IMAGE);

        if (dia != null) {
            WindowManager.LayoutParams layoutParams = dia.getWindow().getAttributes();
            layoutParams.width = ScreenUtil.getScreenWidth(this);
            dia.getWindow().setAttributes(layoutParams);
        }
        super.onConfigurationChanged(newConfig);

        String language = MyApplication.getInstance().getLanguageType();
        PictureAirLog.out("language------>" + language);
        Configuration config = getResources().getConfiguration();
        if (!language.equals("")) {//语言不为空
            if (language.equals(Common.ENGLISH)) {
                config.locale = Locale.US;
            } else if (language.equals(Common.SIMPLE_CHINESE)) {
                config.locale = Locale.SIMPLIFIED_CHINESE;
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
        titleBar.setVisibility(View.VISIBLE);
        toolsBar.setVisibility(View.VISIBLE);
        indexBar.setVisibility(View.VISIBLE);
        if (mViewPager != null) {
            mViewPager.setBackgroundColor(getResources().getColor(R.color.pp_light_gray_background));
        }
        blurFraRelativeLayout.setBackgroundColor(getResources().getColor(R.color.pp_light_gray_background));
        photoFraRelativeLayout.setBackgroundColor(getResources().getColor(R.color.pp_light_gray_background));
        image01.setBackgroundColor(getResources().getColor(R.color.pp_light_gray_background));
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        touchtoclean.setTextColor(getResources().getColor(R.color.pp_dark_blue));
        touchtoclean.setShadowLayer(2, 2, 2, getResources().getColor(R.color.transparent));
    }

    /**
     * 横屏模式
     */
    private void landscapeOrientation() {
        isLandscape = true;
        if (sharePop.isShowing()) {
            sharePop.dismiss();
        }
        if (mViewPager != null) {
            mViewPager.setBackgroundColor(Color.BLACK);
        }
        blurFraRelativeLayout.setBackgroundColor(Color.BLACK);
        photoFraRelativeLayout.setBackgroundColor(Color.BLACK);
        image01.setBackgroundColor(Color.BLACK);
        titleBar.setVisibility(View.GONE);
        toolsBar.setVisibility(View.GONE);
        indexBar.setVisibility(View.GONE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        touchtoclean.setTextColor(getResources().getColor(R.color.white));
        touchtoclean.setShadowLayer(2, 2, 2, getResources().getColor(R.color.pp_dark_blue));
    }


    /**
     * 根据照片的购买情况确定布局和显示模式
     */
    private void initBlur() {
        PictureAirLog.v(TAG, "initBlur " + currentPosition + "___" + mViewPager.getCurrentItem());
        if (!loadFailed) {//加载成功
            createOriginalClearBit(true);
            PictureAirLog.v(TAG, "ori clear bitmap" + oriClearBmp.getWidth() + "----" + oriClearBmp.getHeight());
            zoomW = (int) (parentPreviewW / 2);
            zoomH = (int) (parentPreviewH / 2);
            if (photoInfo.isPayed == 0) {// 未购买的照片
                if (maskBmp != null) {
                    maskBmp.recycle();
                }
                maskBmp = BitmapFactory.decodeResource(getResources(), R.drawable.round_meitu_1).copy(Config.ARGB_8888, true);
                oriBlurBmp = BlurUtil.blur(oriClearBmp);//添加模糊度
                PictureAirLog.v(TAG, "oriBlurBmp = " + oriBlurBmp.getWidth() + "_" + oriBlurBmp.getHeight());
                image01.setImageBitmap(oriBlurBmp);
            }
            image01.setVisibility(View.VISIBLE);

        } else {
            touchtoclean.setText(R.string.http_error_code_401);
        }
        mViewPager.setVisibility(View.GONE);
        curShowBmpWidth = oriClearBmp.getWidth();
        curShowBmpHeight = oriClearBmp.getHeight();
        curRadius = originalRadius;

        if (flag) {//放大模式
            flag = false;
            if (zoomBlurBmp != null) {
                zoomBlurBmp.recycle();
            }
            if (zoomClearBmp != null) {
                zoomClearBmp.recycle();
            }
        }
        PictureAirLog.out("larger bmp h after init---->" + curShowBmpHeight);
        PictureAirLog.out("larger bmp w after init---->" + curShowBmpWidth);
    }

    /**
     * 根据照片的购买情况确定布局和显示模式
     */
    private void resizeBlurImage() {
        PictureAirLog.v(TAG, "initBlur " + currentPosition + "___" + mViewPager.getCurrentItem());
        createOriginalClearBit(false);
        PictureAirLog.v(TAG, "oriClearBmp--->" + oriClearBmp.getWidth() + "----" + oriClearBmp.getHeight());
        zoomW = (int) (parentPreviewW / 2);
        zoomH = (int) (parentPreviewH / 2);
        PictureAirLog.v(TAG, "size---->" + zoomW + "___" + zoomH);

        oriBlurBmp = BlurUtil.blur(oriClearBmp);//添加模糊度
        PictureAirLog.v(TAG, "oriBlurBmp = " + oriBlurBmp.getWidth() + "_" + oriBlurBmp.getHeight());

        image01.setImageBitmap(oriBlurBmp);
        curShowBmpWidth = oriClearBmp.getWidth();
        curShowBmpHeight = oriClearBmp.getHeight();
        if (flag) {//放大模式
            flag = false;
            if (zoomBlurBmp != null) {
                zoomBlurBmp.recycle();
            }

            if (zoomClearBmp != null) {
                zoomClearBmp.recycle();
            }
        }
        curRadius = originalRadius;
        PictureAirLog.out("larger bmp h after resize---->" + curShowBmpHeight);
        PictureAirLog.out("larger bmp w after resize---->" + curShowBmpWidth);
    }

    private void createOriginalClearBit(boolean isInit) {
        int w = oriClearBmp.getWidth();
        int h = oriClearBmp.getHeight();
        PictureAirLog.v(TAG, "oriClearBmp width, height" + w + "?" + h);
        parentPreviewW = ScreenUtil.getScreenWidth(this);

        if (isLandscape) {
            parentPreviewH = ScreenUtil.getScreenHeight(this);
        } else {
            parentPreviewH = ScreenUtil.getScreenHeight(this) - ScreenUtil.getStatusBarHeight(this) - toolsBar.getHeight() - indexBar.getHeight() - titleBar.getHeight();
        }
        PictureAirLog.v(TAG, "screen width, height" + parentPreviewW + "?" + ScreenUtil.getScreenHeight(this));

        if (isInit) {
            int[] location = new int[2];
            photoFraRelativeLayout.getLocationOnScreen(location);//获取控件在屏幕上的坐标
            marginTop = location[1];
            PictureAirLog.v(TAG, "------------>photoFraRelativeLayout height is " + photoFraRelativeLayout.getHeight());
        }

        float sw = 0f;
        if (h / (float) w > parentPreviewH / parentPreviewW) {//左右留白
            sw = parentPreviewH / (float) h;
        } else {//上下留白
            sw = parentPreviewW / (float) w;
        }

        matrix.reset();
        matrix.postScale(sw, sw);
        if (w > 0 && h > 0) {
            oriClearBmp = Bitmap.createBitmap(oriClearBmp, 0, 0, w, h, matrix, true);
        }
    }

    private void cropNewBmt() {
        zoomBlurBmp = Bitmap.createBitmap(oriBlurBmp, x1, y1, Math.min(zoomW, oriBlurBmp.getWidth()), Math.min(zoomH, oriBlurBmp.getHeight()));
        zoomClearBmp = Bitmap.createBitmap(oriClearBmp, x1, y1, Math.min(zoomW, oriClearBmp.getWidth()), Math.min(zoomH, oriClearBmp.getHeight()));
        PictureAirLog.out("crop--->" + Math.min(zoomW, oriBlurBmp.getWidth()) + "--->" + Math.min(zoomH, oriBlurBmp.getHeight()));
        matrix.reset();
        float sw;
        if (zoomBlurBmp.getHeight() / (float)zoomBlurBmp.getWidth() > parentPreviewH / parentPreviewW) {//左右留白
            sw = parentPreviewH / (float) zoomBlurBmp.getHeight();
        } else {//上下留白
            sw = parentPreviewW / (float)zoomBlurBmp.getWidth();
        }
        matrix.postScale(sw, sw);
        zoomBlurBmp = Bitmap.createBitmap(zoomBlurBmp, 0, 0, zoomBlurBmp.getWidth(), zoomBlurBmp.getHeight(), matrix, true);
        zoomClearBmp = Bitmap.createBitmap(zoomClearBmp, 0, 0, zoomClearBmp.getWidth(), zoomClearBmp.getHeight(), matrix, true);
    }

    //直接下载
    private void downloadPic() {
        ArrayList<PhotoInfo> list = new ArrayList<PhotoInfo>();
        list.add(photolist.get(mViewPager.getCurrentItem()));
        Intent intent = new Intent(PreviewPhotoActivity.this,
                DownloadService.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("photos", list);
        intent.putExtras(bundle);
        startService(intent);
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

    // 判断 是否第一次提示 同步更新。，并弹出相应的tips  暂时取消这个设置。
//    private void judgeBuyOnePhoto() {
//        if (myApplication.isPhotoIsPaid()) {// 如果是 购买之后跳转过来的。
//            if (settingUtil.isFirstTipsSyns(sharedPreferences.getString(
//                    Common.USERINFO_ID, ""))) {
//                if (settingUtil.isAutoUpdate(sharedPreferences.getString(
//                        Common.USERINFO_ID, ""))) {
//                    if (AppUtil.getNetWorkType(PreviewPhotoActivity.this) == AppUtil.NETWORKTYPE_WIFI) {
//                        downloadPic();
//                    }
//                } else {
//                    new CustomDialog(PreviewPhotoActivity.this,
//                            R.string.first_tips_syns_msg1,
//                            R.string.first_tips_syns_no_msg1,
//                            R.string.first_tips_syns_yes_msg1,
//                            new CustomDialog.MyDialogInterface() {
//                                @Override
//                                public void yes() {
//                                    // TODO Auto-generated method stub
//                                    // //同步更新：下载单张照片，并且修改设置。
//                                    settingUtil.insertSettingAutoUpdateStatus(sharedPreferences.getString(Common.USERINFO_ID, ""));
//                                    if (AppUtil.getNetWorkType(PreviewPhotoActivity.this) == AppUtil.NETWORKTYPE_WIFI) {
//                                        downloadPic();
//                                    }
//                                }
//
//                                @Override
//                                public void no() {
//                                    // TODO Auto-generated method stub // 取消；不操作
//                                    settingUtil.deleteSettingAutoUpdateStatus(sharedPreferences.getString(Common.USERINFO_ID, ""));
//                                }
//                            });
//                }
//                settingUtil.insertSettingFirstTipsSynsStatus(sharedPreferences
//                        .getString(Common.USERINFO_ID, ""));
//            } else {
//                if (settingUtil.isAutoUpdate(sharedPreferences.getString(
//                        Common.USERINFO_ID, ""))) {
//                    if (AppUtil.getNetWorkType(PreviewPhotoActivity.this) == AppUtil.NETWORKTYPE_WIFI) {
//                        downloadPic();
//                    }
//                }
//            }
//
//        } else {
//
//        }
//        myApplication.setPhotoIsPaid(false); // 保持 不是购买的状态。
//    }

    /**
     * tips 1，网络下载流程。
     */
    private void judgeOnePhotoDownloadFlow() { // 如果当前是wifi，无弹窗提示。如果不是wifi，则提示。
        if (AppUtil.getNetWorkType(PreviewPhotoActivity.this) == AppUtil.NETWORKTYPE_WIFI) {
            downloadPic();
        } else {
            // 判断用户是否设置过 “仅wifi” 的选项。
            if (settingUtil.isOnlyWifiDownload(sharedPreferences.getString(
                    Common.USERINFO_ID, ""))) {
                customdialog = new CustomDialog(PreviewPhotoActivity.this,
                        R.string.one_photo_download_msg1,
                        R.string.one_photo_download_no_msg1,
                        R.string.one_photo_download_yes_msg1,
                        new CustomDialog.MyDialogInterface() {

                            @Override
                            public void yes() {
                                // TODO Auto-generated method stub
                                // //去更改：跳转到设置界面。
                                Intent intent = new Intent(
                                        PreviewPhotoActivity.this,
                                        SettingActivity.class);
                                startActivity(intent);
                            }

                            @Override
                            public void no() {
                                // TODO Auto-generated method stub // 考虑下：弹窗消失
                            }
                        });
            } else {
                customdialog = new CustomDialog(PreviewPhotoActivity.this,
                        R.string.one_photo_download_msg2,
                        R.string.one_photo_download_no_msg2,
                        R.string.one_photo_download_yes_msg2,
                        new CustomDialog.MyDialogInterface() {

                            @Override
                            public void yes() {
                                // TODO Auto-generated method stub //继续下载：继续下载
                                downloadPic();
                            }

                            @Override
                            public void no() {
                                // TODO Auto-generated method stub // 停止下载：弹窗消失
                            }
                        });
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
                if (photolist.get(i).photoPassCode != null) {
                    if (photolist.get(i).photoPassCode.replace(",","").equals(jsonObject.getString("code"))) {
                        if (photolist.get(i).shootOn.contains(jsonObject.getString("bindDate"))) {
                            photolist.get(i).isPayed = 1;
                        }
                    }
                }
            }
        }
    }


}