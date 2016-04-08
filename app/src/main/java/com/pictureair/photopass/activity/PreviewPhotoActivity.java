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
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
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

import com.alibaba.fastjson.JSONObject;
import com.pictureair.jni.keygenerator.PWJniUtil;
import com.pictureair.photopass.GalleryWidget.GalleryViewPager;
import com.pictureair.photopass.GalleryWidget.UrlPagerAdapter;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.blur.BlurUtil;
import com.pictureair.photopass.customDialog.CustomDialog;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.CartItemInfo1;
import com.pictureair.photopass.entity.CartItemInfoJson;
import com.pictureair.photopass.entity.CartPhotosInfo1;
import com.pictureair.photopass.entity.GoodsInfo1;
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
import com.pictureair.photopass.widget.MyToast;
import com.pictureair.photopass.widget.SharePop;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import cn.smssdk.gui.CustomProgressDialog;

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

    private MyToast newToast;
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
    private int currentPosition;//记录当前预览照片的索引值


    private boolean isEdited = false;

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

    private CustomProgressDialog progressDialog;// 等待加载视图
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
    private boolean isFirst = false;//第一次进入标记

    private Dialog dia;
    private TextView buy_ppp, cancel, buynow, touchtoclean;
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

    private CustomDialog customdialog; //  对话框


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

    private List<GoodsInfo1> allGoodsList;//全部商品
    private GoodsInfo1 pppGoodsInfo;
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
    private int curShowBmpWidth = 0 ;
    private int curShowBmpHeight = 0;

    /**
     * 超出屏幕的时候，每次移动的距离
     */
    private int moveSize = 30;

    private Handler previewPhotoHandler;

    /**
     * 处理Message
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
                if (curRadius > curShowBmpWidth / 2) {
                    curRadius = curShowBmpWidth / 2;
                }
                if (curRadius > curShowBmpHeight / 2) {
                    curRadius = curShowBmpHeight / 2;
                }

                PictureAirLog.out("larger bmp h---->" + curShowBmpHeight);
                PictureAirLog.out("larger bmp w---->" + curShowBmpWidth);
                PictureAirLog.out("current radius---->" + curRadius);

                x = (int) (msg.arg1 - curRadius - (parentPreviewW - curShowBmpWidth) / 2);
                y = (int) (msg.arg2 - curRadius - (isLandscape ? 0 : marginTop) - (parentPreviewH - curShowBmpHeight) / 2);
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

                if (x1 > curShowBmpWidth - Math.min(curShowBmpWidth, zoomW)) {
                    x1 = curShowBmpWidth - Math.min(curShowBmpWidth, zoomW);
                }

                if (y1 > curShowBmpHeight - Math.min(curShowBmpHeight, zoomH)) {
                    y1 = curShowBmpHeight - Math.min(curShowBmpHeight, zoomH);
                }

                PictureAirLog.out("bit1 w-" + oriBlurBmp.getWidth() + "h--" + oriBlurBmp.getHeight());
                PictureAirLog.out("bit2 w-" + oriClearBmp.getWidth() + "h--" + oriClearBmp.getHeight());

                if (x1 > oriBlurBmp.getWidth() - Math.min(zoomW, oriBlurBmp.getWidth())) {
                    x1 = oriBlurBmp.getWidth() - Math.min(zoomW, oriBlurBmp.getWidth());
                }
                if (y1 > oriBlurBmp.getHeight() - Math.min(zoomH, oriBlurBmp.getHeight())) {
                    y1 = oriBlurBmp.getHeight() - Math.min(zoomH, oriBlurBmp.getHeight());
                }

                if (x1 < 0) {
                    x1 = 0;
                }
                if (y1 < 0) {
                    y1 = 0;
                }

                PictureAirLog.out("oriblur---?" + oriBlurBmp.getWidth() + " ---  " + oriBlurBmp.getHeight());
                PictureAirLog.out("x1---" + x1 + " y1---  " + y1);
                PictureAirLog.out("zoomW---" + zoomW);

                if (flag) {//放大
                    if (out) {//超出屏幕
                        zoomBlurBmp = Bitmap.createBitmap(oriBlurBmp, x1, y1, Math.min(zoomW, oriBlurBmp.getWidth()), Math.min(zoomH, oriBlurBmp.getHeight()));
                        zoomClearBmp = Bitmap.createBitmap(oriClearBmp, x1, y1, Math.min(zoomW, oriClearBmp.getWidth()), Math.min(zoomH, oriClearBmp.getHeight()));
                        matrix.reset();
                        matrix.postScale(parentPreviewW / zoomBlurBmp.getWidth(), parentPreviewH / zoomBlurBmp.getHeight());
                        zoomBlurBmp = Bitmap.createBitmap(zoomBlurBmp, 0, 0, zoomBlurBmp.getWidth(), zoomBlurBmp.getHeight(), matrix, true);
                        zoomClearBmp = Bitmap.createBitmap(zoomClearBmp, 0, 0, zoomClearBmp.getWidth(), zoomClearBmp.getHeight(), matrix, true);
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
                break;

            case 3://双击放大
                x1 = msg.arg1 - zoomW / 2;
                y1 = msg.arg2 - zoomH / 2;
                PictureAirLog.v(TAG, "current xy = " + x1 + "+" + y1);
                if (x1 > oriBlurBmp.getWidth() - Math.min(zoomW, oriBlurBmp.getWidth())) {
                    x1 = oriBlurBmp.getWidth() - Math.min(zoomW, oriBlurBmp.getWidth());
                }
                if (x1 < 0) {
                    x1 = 0;
                }
                if (y1 > oriBlurBmp.getHeight() - Math.min(zoomH, oriBlurBmp.getHeight())) {
                    y1 = oriBlurBmp.getHeight() - Math.min(zoomH, oriBlurBmp.getHeight());
                }
                if (y1 < 0) {
                    y1 = 0;
                }
                PictureAirLog.v(TAG, "after currnet xy = " + x1 + "_" + y1);
                if (!flag) {
                    zoomBlurBmp = Bitmap.createBitmap(oriBlurBmp, x1, y1, Math.min(zoomW, oriBlurBmp.getWidth()), Math.min(zoomH, oriBlurBmp.getHeight()));
                    zoomClearBmp = Bitmap.createBitmap(oriClearBmp, x1, y1, Math.min(zoomW, oriClearBmp.getWidth()), Math.min(zoomH, oriClearBmp.getHeight()));
                    matrix.reset();
                    matrix.postScale(parentPreviewW / zoomBlurBmp.getWidth(), parentPreviewH / zoomBlurBmp.getHeight());
                    zoomBlurBmp = Bitmap.createBitmap(zoomBlurBmp, 0, 0, zoomBlurBmp.getWidth(), zoomBlurBmp.getHeight(), matrix, true);
                    zoomClearBmp = Bitmap.createBitmap(zoomClearBmp, 0, 0, zoomClearBmp.getWidth(), zoomClearBmp.getHeight(), matrix, true);
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
                progressDialog.dismiss();
                cartItemInfoJson = JsonTools.parseObject((JSONObject) msg.obj, CartItemInfoJson.class);//CartItemInfoJson.getString()
                PictureAirLog.v(TAG, "BUY_PHOTO_SUCCESS" + cartItemInfoJson.toString());
                //将当前购买的照片信息存放到application中
                myApplication.setIsBuyingPhotoInfo(photolist, currentPosition);
                if (myApplication.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_MYPHOTOPASS)) {
                } else if (myApplication.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_VIEWORSELECTACTIVITY)) {
                } else {
                    myApplication.setRefreshViewAfterBuyBlurPhoto(Common.FROM_PREVIEW_PHOTO_ACTIVITY);
                }
                List<CartItemInfo1> cartItemInfo1List = cartItemInfoJson.getItems();
                Intent intent = new Intent(PreviewPhotoActivity.this, SubmitOrderActivity.class);
                ArrayList<CartItemInfo1> orderinfo = new ArrayList<>();
                CartItemInfo1 cartItemInfo = cartItemInfo1List.get(0);
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
                progressDialog.dismiss();
                newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);

                break;
            case API1.GET_GOODS_SUCCESS:
                GoodsInfoJson goodsInfoJson = JsonTools.parseObject(msg.obj.toString(), GoodsInfoJson.class);//GoodsInfoJson.getString()
                if (goodsInfoJson != null && goodsInfoJson.getGoods().size() > 0) {
                    allGoodsList = goodsInfoJson.getGoods();
                    PictureAirLog.v(TAG, "goods size: " + allGoodsList.size());
                    //获取PP+
                    for (GoodsInfo1 goodsInfo : allGoodsList) {
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
                    //将数据保存到缓存中
                    if (ACache.get(MyApplication.getInstance()).getAsString(Common.ALL_GOODS) == null || ACache.get(MyApplication.getInstance()).getAsString(Common.ALL_GOODS).equals("")) {
                        ACache.get(MyApplication.getInstance()).put(Common.ALL_GOODS, msg.obj.toString(), ACache.GOODS_ADDRESS_ACACHE_TIME);
                    }
                }
                break;

            case API1.GET_GOODS_FAILED:
                progressDialog.dismiss();
                newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);
                break;

            case API1.ADD_TO_CART_FAILED:
                progressDialog.dismiss();
                newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);

                break;

            case API1.ADD_TO_CART_SUCCESS:
                progressDialog.dismiss();
                JSONObject jsonObject = (JSONObject) msg.obj;
                editor = sharedPreferences.edit();
                editor.putInt(Common.CART_COUNT, sharedPreferences.getInt(Common.CART_COUNT, 0) + 1);
                editor.commit();
                String cartId = jsonObject.getString("cartId");

                //生成订单
                Intent intent1 = new Intent(PreviewPhotoActivity.this, SubmitOrderActivity.class);
                ArrayList<CartItemInfo1> orderinfoArrayList = new ArrayList<>();
                CartItemInfo1 cartItemInfo1 = new CartItemInfo1();
                cartItemInfo1.setCartId(cartId);
                cartItemInfo1.setProductName(pppGoodsInfo.getName());
                cartItemInfo1.setProductNameAlias(pppGoodsInfo.getNameAlias());
                cartItemInfo1.setUnitPrice(pppGoodsInfo.getPrice());
                cartItemInfo1.setEmbedPhotos(new ArrayList<CartPhotosInfo1>());
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
                if (null != oriClearBmp) {
                    PictureAirLog.v(TAG, "bitmap 2 not null");
                    progressDialog.dismiss();
                    initBlur();
                } else {
                    PictureAirLog.v(TAG, "oriClearBmp null-->");
                    progressDialog.dismiss();
                    loadFailed = true;
                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    initBlur();
                }
                break;

            case LOAD_FROM_LOCAL:
                byte[] arg2 = null;
                try {
                    arg2 = AESKeyHelper.decrypt(dirFile.toString(), PWJniUtil.getAESKey(Common.APP_TYPE_SHDRPP));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                oriClearBmp = BitmapFactory.decodeByteArray(arg2, 0, arg2.length);
                if (null != oriClearBmp) {
                    progressDialog.dismiss();
                    initBlur();
                }
                break;

            case API1.GET_AD_LOCATIONS_SUCCESS:
                PictureAirLog.out("ad location---->" + msg.obj.toString());
                /**
                 * 1.存入数据库
                 * 2.在application中记录结果
                 */
                JSONObject adJsonObject = JSONObject.parseObject(msg.obj.toString());
                String adString = pictureAirDbManager.insertADLocations(adJsonObject.getJSONArray("locations"),
                        photoInfo.locationId, MyApplication.getInstance().getLanguageType());

                if (!adString.equals("")) {
                    currentPhotoADTextView.setVisibility(View.VISIBLE);
                    currentPhotoADTextView.setText(adString);
                }
                myApplication.setGetADLocationSuccess(true);
                break;

            case API1.GET_AD_LOCATIONS_FAILED:
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
        init();//初始化UI
//        judge();//判断 照片是否购买，并弹出相应的tips
    }

    private void init() {
        // TODO Auto-generated method stub
        previewPhotoHandler = new Handler(this);
        pictureAirDbManager = new PictureAirDbManager(this);
        settingUtil = new SettingUtil(pictureAirDbManager);
        newToast = new MyToast(this);
        sharePop = new SharePop(this);
        matrix = new Matrix();
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sharedPreferences = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
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

//        view.setMinimumWidth(ScreenUtil.getScreenWidth(this));
        buy_ppp = (TextView) dia.findViewById(R.id.buy_ppp);
        cancel = (TextView) dia.findViewById(R.id.cancel);
        buynow = (TextView) dia.findViewById(R.id.buynow);
        buynow.setOnClickListener(this);
        buy_ppp.setOnClickListener(this);
        cancel.setOnClickListener(this);

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
        PictureAirLog.v(TAG, "photoid ===== " + getIntent().getStringExtra("photoId"));

        //获取intent传递过来的信息
        photolist = new ArrayList<>();
        photolist.addAll((ArrayList<PhotoInfo>) getIntent().getSerializableExtra("photos"));//获取图片路径list
        targetphotolist = (ArrayList<PhotoInfo>) getIntent().getSerializableExtra("targetphotos");
        currentPosition = getIntent().getIntExtra("position", 0);

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
        Configuration cf = getResources().getConfiguration();
        int ori = cf.orientation;
        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
            isLandscape = true;
            landscapeOrientation();
            originalRadius = (ScreenUtil.getScreenHeight(PreviewPhotoActivity.this) / 3);
        } else {
            originalRadius = (ScreenUtil.getScreenWidth(PreviewPhotoActivity.this) / 3);
        }
        curRadius = originalRadius;
        previewPhotoHandler.sendEmptyMessage(7);
    }

    /**
     * 加载网络图片
     *
     * @param loadPhotoInfo 需要加载的网络图片
     */
    private void loadPhotoPassPhoto(PhotoInfo loadPhotoInfo, boolean isOnCreate) {
        // TODO Auto-generated method stub
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
                        previewPhotoHandler.sendEmptyMessage(LOAD_FROM_LOCAL);
                    }
                });

            } else {

                previewPhotoHandler.sendEmptyMessage(LOAD_FROM_LOCAL);
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
                }

                @Override
                public void onFailure(int status) {
                    super.onFailure(status);
                    previewPhotoHandler.sendEmptyMessage(LOAD_FROM_NETWORK);

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
        //更新收藏图标
        if (photoInfo.isLove == 1 || pictureAirDbManager.checkLovePhoto(photoInfo, sharedPreferences.getString(Common.USERINFO_ID, ""))) {
            photoInfo.isLove = 1;
            loveImageButton.setImageResource(R.drawable.discover_like);
        } else {
            loveImageButton.setImageResource(R.drawable.discover_no_like);
        }

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
        if (currentPosition == (isEdited ? targetphotolist.size() - 1
                : photolist.size() - 1)) {
            nextPhotoImageView.setVisibility(View.INVISIBLE);
        } else {
            nextPhotoImageView.setVisibility(View.VISIBLE);
        }
        lastPhotoImageView.setEnabled(true);
        nextPhotoImageView.setEnabled(true);

        //如果是未购买图片，判断是否是第一次进入，如果是，则显示引导图层
        if (photoInfo.isPayed == 0 && photoInfo.onLine == 1) {//未购买的图片
            PictureAirLog.v(TAG, "need show blur view");
            image01.setVisibility(View.INVISIBLE);
            progressDialog = CustomProgressDialog.show(this, getString(R.string.is_loading), false, null);
            touchtoclean.setVisibility(View.VISIBLE);
            blurFraRelativeLayout.setVisibility(View.VISIBLE);
            currentPhotoADTextView.setVisibility(View.GONE);
            loadPhotoPassPhoto(photoInfo, isOnCreate);
//            if (!isFirst) {
//                if (pictureAirDbManager.checkFirstTimeStartActivity("blurActivity", sharedPreferences.getString(Common.USERINFO_ID, ""))) {//第一次进入
//                    PictureAirLog.v(TAG, "new user");
//                    leadView.setVisibility(View.VISIBLE);
//                    leadView.setOnClickListener(this);
//                    isFirst = true;
//                }
//            }
        } else if (photoInfo.isPayed == 1 && photoInfo.onLine == 1) {
            currentPhotoADTextView.setVisibility(View.GONE);
            if (myApplication.isGetADLocationSuccess()) {
                //从数据库中查找
                String adString = pictureAirDbManager.getADByLocationId(photoInfo.locationId, MyApplication.getInstance().getLanguageType());
                if (!adString.equals("")) {
                    currentPhotoADTextView.setVisibility(View.VISIBLE);
                    currentPhotoADTextView.setText(adString);
                }
            } else {
                //从网络获取
                API1.getADLocations(previewPhotoHandler);
            }
        } else {
            currentPhotoADTextView.setVisibility(View.GONE);
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


    /**
     * 根据照片的购买情况确定布局和显示模式
     */
    private void initBlur() {
        PictureAirLog.v(TAG, "initBlur " + currentPosition + "___" + mViewPager.getCurrentItem());
        if (!loadFailed) {//加载成功
            int w = oriClearBmp.getWidth();
            int h = oriClearBmp.getHeight();
            PictureAirLog.v(TAG, "oriClearBmp width, height" + w + "?" + h);
            parentPreviewW = ScreenUtil.getScreenWidth(this);
            parentPreviewH = photoFraRelativeLayout.getHeight();
            int[] location = new int[2];
            photoFraRelativeLayout.getLocationOnScreen(location);//获取控件在屏幕上的坐标
            marginTop = location[1];
            PictureAirLog.v(TAG, "------------>photoFraRelativeLayout height is " + photoFraRelativeLayout.getHeight());
            float sw = 0f;
            if (h / (float) w > parentPreviewH / parentPreviewW) {//左右留白
                sw = parentPreviewH / (float) h;
            } else {//上下留白
                sw = parentPreviewW / (float) w;
            }
            matrix.reset();
            matrix.postScale(sw, sw);
            oriClearBmp = Bitmap.createBitmap(oriClearBmp, 0, 0, w, h, matrix, true);
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

    public boolean onTouchEvent(MotionEvent event) {
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
                        break;
                    case MotionEvent.ACTION_UP:
                        mode = MODE_UP;
                        PictureAirLog.v(TAG, "up");
                        msg.what = 2;
                        touchtoclean.setVisibility(View.VISIBLE);
                        break;
                }
                previewPhotoHandler.sendMessage(msg);

            }
        }
        return super.onTouchEvent(event);
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
                if (photoInfo.isLove == 1) {
                    Log.d(TAG, "cancel love");
                    pictureAirDbManager.setPictureLove(photoInfo, sharedPreferences.getString(Common.USERINFO_ID, ""), false);
                    photoInfo.isLove = 0;
                    loveImageButton.setImageResource(R.drawable.discover_no_like);
                } else {
                    Log.d(TAG, "add love");
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
                if (photoInfo.isPayed == 1) {
                    if (photoInfo.isHasPreset == 0){ // 如果没有模版，就去执行编辑操作。 如果有模版就弹出提示。
                        intent = new Intent(this, EditPhotoActivity.class);
                        if (isEdited) {//已经编辑过，取targetlist中的值
                            intent.putExtra("photo", targetphotolist.get(mViewPager.getCurrentItem()));
                        } else {//没有编辑，取正常的值
                            intent.putExtra("photo", photolist.get(mViewPager.getCurrentItem()));
                        }
                        startActivityForResult(intent, 1);
                    }else{

//                        new PictureWorksDialog(PreviewPhotoActivity.this,getString(R.string.photo_cannot_edit_title),getString(R.string.photo_cannot_edit_content),getString(R.string.photo_cannot_edit_no),getString(R.string.photo_cannot_edit_yes),true,null).show();
//                        newToast.setTextAndShow("这张照片不能编辑", Common.TOAST_SHORT_TIME);
                        customdialog = new CustomDialog(PreviewPhotoActivity.this,
                                R.string.photo_cannot_edit_content,
                                R.string.photo_cannot_edit_no,
                                R.string.photo_cannot_edit_yes,
                                new CustomDialog.MyDialogInterface() {

                                    @Override
                                    public void yes() {
                                        // TODO Auto-generated method stub
                                    }

                                    @Override
                                    public void no() {
                                        // TODO Auto-generated method stub // 考虑下：弹窗消失
                                    }
                                });
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
                if (photoInfo.isPayed == 1) {
                    dia.dismiss();
                    PictureAirLog.v(TAG, "start share=" + photolist.get(mViewPager.getCurrentItem()).photoPathOrURL);
                    if (isEdited) {//编辑后
                        sharePop.setshareinfo(targetphotolist.get(mViewPager.getCurrentItem()).photoPathOrURL, null, "local", null, SharePop.SHARE_PHOTO_TYPE, previewPhotoHandler);
                    } else {//编辑前
                        //判断图片是本地还是网路图片
                        if (photoInfo.onLine == 1) {//网络图片
                            sharePop.setshareinfo(null, photolist.get(mViewPager.getCurrentItem()).photoPathOrURL,
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
                if (photoInfo.isPayed == 1) {
                    if (isEdited) {//编辑后
                        newToast.setTextAndShow(R.string.neednotdownload, Common.TOAST_SHORT_TIME);
                    } else {//编辑前
                        if (photoInfo.onLine == 1) {//是pp的照片
                            judgeOnePhotoDownloadFlow();
//                            downLoadPhotos();
                            //						ArrayList<PhotoInfo> list = new ArrayList<PhotoInfo>();
                            //						list.add(photolist.get(mViewPager.getCurrentItem()));
                            //						intent = new Intent(this, DownloadService.class);
                            //						Bundle bundle = new Bundle();
                            //						bundle.putParcelableArrayList("photos", list);
                            //						intent.putExtras(bundle);
                            //						startService(intent);
                            //						PictureAirLog.v(TAG,"start");
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

                if (photoInfo.onLine == 0) {
                    newToast.setTextAndShow(R.string.local_photo_not_support_makegift, Common.TOAST_SHORT_TIME);
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
                progressDialog = CustomProgressDialog.show(this, getString(R.string.is_loading), false, null);
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
                progressDialog = CustomProgressDialog.show(this, getString(R.string.is_loading), false, null);
                //获取商品
                getALlGoods();
                dia.dismiss();
                break;
            case R.id.leadknow:
            case R.id.blur_lead_view:
                PictureAirLog.v(TAG, "know");
                leadView.setVisibility(View.GONE);
                break;

            case R.id.index_last://上一张
                PictureAirLog.v(TAG, "--------->last");
                lastPhotoImageView.setEnabled(false);
                nextPhotoImageView.setEnabled(false);
                changeTab(false);
                setUmengPhotoSlide();//统计滑动图片次数
                break;

            case R.id.index_next://下一张
                PictureAirLog.v(TAG, "--------->next");
                changeTab(true);
                lastPhotoImageView.setEnabled(false);
                nextPhotoImageView.setEnabled(false);
                setUmengPhotoSlide();//统计滑动图片次数
                break;

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
            currentPosition++;
        } else {
            PictureAirLog.v(TAG, "--------->last");
            currentPosition--;
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
        if (photoInfo.isPayed == 0 && photoInfo.onLine == 1) {
            previewPhotoHandler.sendEmptyMessage(2);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PictureAirLog.v(TAG, "----------->" + myApplication.getRefreshViewAfterBuyBlurPhoto());
        if (photoInfo.isPayed == 0 && photoInfo.onLine == 1) {
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

        if (oriBlurBmp != null){
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

        if (photoInfo.onLine == 1 && photoInfo.isPayed == 0) {//模糊图需要重新修改大小
            resizeBlurImage();
        }

        if (dia.isShowing()) {
            WindowManager.LayoutParams layoutParams = dia.getWindow().getAttributes();
            layoutParams.width = ScreenUtil.getScreenWidth(this);
            dia.getWindow().setAttributes(layoutParams);
        }
        super.onConfigurationChanged(newConfig);
    }

    /**
     * 垂直模式
     */
    private void portraitOrientation(){
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
    private void landscapeOrientation(){
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
    private void resizeBlurImage() {
        PictureAirLog.v(TAG, "initBlur " + currentPosition + "___" + mViewPager.getCurrentItem());
        int w = oriClearBmp.getWidth();
        int h = oriClearBmp.getHeight();
        PictureAirLog.v(TAG, "oriClearBmp width, height" + w + "?" + h);
        parentPreviewW = ScreenUtil.getScreenWidth(this);
        parentPreviewH = photoFraRelativeLayout.getHeight();//如果切换屏幕的时候，这个数值依旧是旋转屏幕之前的数值
        if (parentPreviewH > ScreenUtil.getScreenHeight(this)) {//如果是切换到横屏的时候，如果超过屏幕高，则使用屏幕的高
            parentPreviewH = ScreenUtil.getScreenHeight(this);
        }
        PictureAirLog.v(TAG, "screen width, height" + parentPreviewW + "?" + ScreenUtil.getScreenHeight(this));
        PictureAirLog.v(TAG, "scale width, height" + parentPreviewW + "?" + parentPreviewH);
        float sw = 0f;
        if (h / (float) w > parentPreviewH / (float) parentPreviewW) {//左右留白
            sw = parentPreviewH / (float) h;
        } else {//上下留白
            sw = parentPreviewW / (float) w;
        }

        matrix.reset();
        matrix.postScale(sw, sw);
        oriClearBmp = Bitmap.createBitmap(oriClearBmp, 0, 0, w, h, matrix, true);
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
            zoomBlurBmp.recycle();
            zoomClearBmp.recycle();
        }
        curRadius = originalRadius;
        PictureAirLog.out("larger bmp h after resize---->" + curShowBmpHeight);
        PictureAirLog.out("larger bmp w after resize---->" + curShowBmpWidth);
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


    private void setUmengPhotoSlide(){
        UmengUtil.onEvent(PreviewPhotoActivity.this, Common.EVENT_PHOTO_SLIDE);
    }

}