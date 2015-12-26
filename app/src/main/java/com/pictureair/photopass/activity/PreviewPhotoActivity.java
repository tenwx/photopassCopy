package com.pictureair.photopass.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.pictureair.photopass.GalleryWidget.GalleryViewPager;
import com.pictureair.photopass.GalleryWidget.UrlPagerAdapter;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.blur.UtilOfDraw;
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
import com.pictureair.photopass.util.HttpUtil;
import com.pictureair.photopass.util.JsonTools;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.util.SettingUtil;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.MyToast;
import com.pictureair.photopass.widget.SharePop;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.crypto.NoSuchPaddingException;

import cz.msebera.android.httpclient.Header;

/**
 * 预览图片，可以进行编辑，分享，下载和制作礼物的操作
 *
 * @author bauer_bao
 */
@SuppressLint({"FloatMath", "NewApi"})
public class PreviewPhotoActivity extends BaseActivity implements OnClickListener {
    private SettingUtil settingUtil;
    private String s;
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
    //	private String userId;
    private static final String TAG = "PreviewPhotoActivity";

    private int shareType = 0;

    private TextView textView;

    //图片显示框架
    private ArrayList<PhotoInfo> photolist;
    private ArrayList<PhotoInfo> targetphotolist;
    private int currentPosition;//记录当前预览照片的索引值
    //	private int flag;

    //图片布局宽高
    public static int fraWidth;
    public static int fraHeight;

    private boolean isEdited = false;
    //	private boolean

    //底部切换索引按钮
    private ImageView lastPhotoImageView;
    private ImageView nextPhotoImageView;
    private TextView currentPhotoIndexTextView;
    private TextView currentPhotoInfoTextView;

    private CustomProgressDialog progressDialog;// 等待加载视图
    private Bitmap bitmap1 = null;// 原图的模糊图
    private Bitmap bitmap2 = null;// 原图
    private Bitmap bitmap3 = null;// 圆圈图
    private Bitmap bitmap4 = null;// 放大后的模糊图
    private Bitmap bitmap5 = null;// 放大后的清晰图
    private Bitmap resultBitmap = null;// 圆圈图的中心透明渐变
    private Bitmap bm = null;// mask蒙板
    private ImageView image01;// 模糊图的view
    private ImageView image02;// 圆圈的view

    private RelativeLayout photoFraRelativeLayout;
    private RelativeLayout blurFraRelativeLayout;
    //	private ImageView back;// 图片显示区域
    private float scaleH;// 图片缩放后的宽
    private float scaleW;// 图片缩放后的高
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

    //	private MyApplication application;

    /**
     * 照片已购买情况下
     */
    private int mode = 0;// 初始状态
    private static final int MODE_DOWN = 111;
    private static final int MODE_MOVE = 222;
    private static final int MODE_UP = 333;

    private int mNetWorkType;  //当前网络的状态
    private CustomDialog customdialog; //  对话框

    //	private PointF startPoint = new PointF();
    //	private Matrix currentMatrix = new Matrix();

    //	private float startDis;
    //	private PointF midPoint;

    /**
     * sizeW、sizeH 截图比例 r 圆圈的半径
     */
    private int sizeW = 0;
    private int sizeH = 0;
    private int r = 0;

    private RelativeLayout leadView;
    private Button knowImageView;

    private boolean loadFailed = false;

    private List<GoodsInfo1> allGoodsList;//全部商品
    private GoodsInfo1 pppGoodsInfo;
    private String[] photoUrls;

    /**
     * x、y用于底图处理 x1、y1用于圆圈处理
     */
    int x;
    int y;
    int x1;
    int y1;

    int xx;
    int yy;

    private Handler handler = new Handler() {
        @SuppressLint("NewApi")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    sendEmptyMessageDelayed(6, 500);
                    x = msg.arg1 - r;
                    y = (int) (msg.arg2 - r - ScreenUtil.getScreenHeight(PreviewPhotoActivity.this) + scaleH);
                    if (x > bitmap2.getWidth() - 2 * r) {
                        x = bitmap2.getWidth() - 2 * r;
                        x1 += 50;
                        out = true;
                    }
                    if (x < 0) {
                        x = 0;
                        x1 -= 50;
                        out = true;
                    }
                    if (y > bitmap2.getHeight() - 2 * r) {
                        y = bitmap2.getHeight() - 2 * r;
                        y1 += 50;
                        out = true;
                    }
                    if (y < 0) {
                        y = 0;
                        y1 -= 50;
                        out = true;
                    }
                    if (x1 < 0) {
                        x1 = 0;
                    }
                    if (x1 > bitmap2.getWidth() - sizeW) {
                        x1 = bitmap2.getWidth() - sizeW;
                    }

                    if (y1 < 0) {
                        y1 = 0;
                    }
                    if (y1 > bitmap2.getHeight() - sizeH) {
                        y1 = bitmap2.getHeight() - sizeH;
                    }

                    /***********之后加的*****************/
                    if (x1 > bitmap1.getWidth() - sizeW) {
                        x1 = bitmap1.getWidth() - sizeW;
                    }
                    if (y1 > bitmap1.getHeight() - sizeH) {
                        y1 = bitmap1.getHeight() - sizeH;
                    }
                    /**********之后加的***************/


                    if (flag == true) {
                        if (out == true) {
                            //						PictureAirLog.v(TAG,x1+"_"+y1+"_"+sizeW+"+"+sizeH+bitmap1.getWidth()+"__"+bitmap1.getHeight()+"+"+bitmap2.getWidth()+"__"+bitmap2.getHeight());

                            bitmap4 = Bitmap.createBitmap(bitmap1, x1, y1, sizeW, sizeH);
                            bitmap5 = Bitmap.createBitmap(bitmap2, x1, y1, sizeW, sizeH);
                            Matrix m = new Matrix();
                            m.postScale((float) scaleW / bitmap4.getWidth(), (float) scaleH / bitmap4.getHeight());
                            bitmap4 = Bitmap.createBitmap(bitmap4, 0, 0, bitmap4.getWidth(), bitmap4.getHeight(), m, true);
                            bitmap5 = Bitmap.createBitmap(bitmap5, 0, 0, bitmap5.getWidth(), bitmap5.getHeight(), m, true);
                        }
                        image01.setImageBitmap(bitmap4);
                        bitmap3 = Bitmap.createBitmap(bitmap5, x, y, 2 * r, 2 * r);
                        bitmap3 = Mask(bitmap3);
                        bitmap3 = UtilOfDraw.toRoundBitmap(bitmap3);
                    } else {
                        bitmap3 = Bitmap.createBitmap(bitmap2, x, y, 2 * r, 2 * r);
                        bitmap3 = Mask(bitmap3);
                        bitmap3 = UtilOfDraw.toRoundBitmap(bitmap3);
                    }
                    if (!image02.isShown()) {
                        image02.setVisibility(View.VISIBLE);
                    }
                    image02.setX(x);
                    image02.setY(y);
                    image02.setImageBitmap(bitmap3);
                    out = false;
                    break;
                case 2:
                    PictureAirLog.v(TAG, "------->2");
                    //				if (image02 != null) {
                    //					PictureAirLog.v(TAG,"image2 not null and set null");
                    //					image02.setImageBitmap(null);
                    //				}
                    if (image02.isShown()) {
                        PictureAirLog.v(TAG, "image2 is shown");
                        image02.setVisibility(View.GONE);
                    } else {
                        PictureAirLog.v(TAG, "image2 is not shown");
                    }

                    if (null != bitmap3) {
                        bitmap3.recycle();
                        bitmap3 = null;
                        PictureAirLog.v(TAG, "bitmap3 recycle-------->");
                    }
                    break;
                case 3:
                    x1 = msg.arg1 - sizeW / 2;
                    y1 = msg.arg2 - sizeH / 2;
                    PictureAirLog.v(TAG, "current xy = " + x1 + "+" + y1);
                    if (x1 < 0) {
                        x1 = 0;
                    }
                    if (x1 > bitmap1.getWidth() - sizeW) {
                        x1 = bitmap1.getWidth() - sizeW;
                    }
                    if (y1 < 0) {
                        y1 = 0;
                    }
                    if (y1 > bitmap1.getHeight() - sizeH) {
                        y1 = bitmap1.getHeight() - sizeH;
                    }
                    PictureAirLog.v(TAG, "after currnet xy = " + x1 + "_" + y1);
                    if (flag == false) {
                        bitmap4 = Bitmap.createBitmap(bitmap1, x1, y1, sizeW, sizeH);
                        bitmap5 = Bitmap.createBitmap(bitmap2, x1, y1, sizeW, sizeH);
                        Matrix m = new Matrix();
                        m.postScale((float) scaleW / bitmap4.getWidth(), (float) scaleH / bitmap4.getHeight());
                        bitmap4 = Bitmap.createBitmap(bitmap4, 0, 0, bitmap4.getWidth(), bitmap4.getHeight(), m, true);
                        bitmap5 = Bitmap.createBitmap(bitmap5, 0, 0, bitmap5.getWidth(), bitmap5.getHeight(), m, true);
                        image01.setImageBitmap(bitmap4);
                        flag = true;
                    } else {
                        image01.setImageBitmap(bitmap1);
                        flag = false;
                    }
                    break;
                case 4:
                    image01.setImageBitmap(bitmap1);
                    flag = false;
                    out = false;
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
                        myApplication.setRefreshViewAfterBuyBlurPhoto(Common.FROM_BLUR);
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
                        API1.addToCart(pppGoodsInfo.getGoodsKey(), 1, true, null, handler);
                        //将数据保存到缓存中
                        if (ACache.get(MyApplication.getInstance()).getAsString(Common.ALL_GOODS) != null && !ACache.get(MyApplication.getInstance()).getAsString(Common.ALL_GOODS).equals("")) {
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
                    startActivity(intent1);
                    break;

                case 6:
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
                    r = (int) (ScreenUtil.getScreenWidth(PreviewPhotoActivity.this) / 3);

                    mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

                        @Override
                        public void onPageSelected(int arg0) {
                            //初始化每张图片的love图标
                            PictureAirLog.v(TAG, "----------------------->initing...4");
                            //				PictureAirLog.v(TAG,"viewPager pageSelected--------->"+ arg0 + "____" + mViewPager.getCurrentItem()+"____"+currentPosition);
                            currentPosition = arg0;
                            //				PictureAirLog.v(TAG,"viewPager pageSelected--------->"+ arg0 + "____" + mViewPager.getCurrentItem()+"____"+currentPosition);

                            //				if (fasdflag) {
                            //					updateIndexTools(false);//只能写在这里，不能写在onPageSelected，不然出现切换回来之后，显示错乱
                            //				}
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
                            }
                        }
                    });

                    PictureAirLog.v(TAG, "----------------------->initing...6");
                    judgeBuyOnePhoto();
                    break;
                default:
                    break;
            }

            // System.gc();
            super.handleMessage(msg);
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    //添加模糊
                    if (null != bitmap2) {
                        PictureAirLog.v(TAG, "bitmap 2 not null");
                        progressDialog.dismiss();
                        initBlur();
                    } else {
                        PictureAirLog.v(TAG, "bitmap2 null-->");
                        progressDialog.dismiss();
                        loadFailed = true;
                        newToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
                        initBlur();
                    }
                    break;
                case 1:
//                    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
//                    byte[] buffer = new byte[1024];
//                    int len = 0;
//                    FileInputStream inStream;
//                    try {
//                        inStream = new FileInputStream(dirFile);
//                        while ((len = inStream.read(buffer)) != -1) {
//                            outStream.write(buffer, 0, len);
//                        }
//                        outStream.close();
//                        inStream.close();
//                    } catch (FileNotFoundException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
                    byte[] arg2 = null;
                    try {
                        arg2 = AESKeyHelper.decrypt(dirFile.toString(), Common.AES_ENCRYPTION_KEY);
                    } catch (InvalidKeyException | NoSuchAlgorithmException
                            | NoSuchPaddingException | IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }


//                        byte[] arg2 = outStream.toByteArray();
                    bitmap2 = BitmapFactory.decodeByteArray(arg2, 0, arg2.length);
                    if (null != bitmap2) {
                        progressDialog.dismiss();
                        initBlur();
                    }
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_photo);
        init();//初始化UI
        judge();//判断 照片是否购买，并弹出相应的tips
    }

    private void init() {
        // TODO Auto-generated method stub
        settingUtil = new SettingUtil(this);
        newToast = new MyToast(this);
        sharePop = new SharePop(this);
        pictureAirDbManager = new PictureAirDbManager(this);
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sharedPreferences = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
        returnImageView = (ImageView) findViewById(R.id.button1_shop_rt);

        locationTextView = (TextView) findViewById(R.id.preview_location);
        editButton = (TextView) findViewById(R.id.preview_edit);
        shareButton = (TextView) findViewById(R.id.preview_share);
        downloadButton = (TextView) findViewById(R.id.preview_download);
        makegiftButton = (TextView) findViewById(R.id.preview_makegift);
        loveImageButton = (ImageButton) findViewById(R.id.preview_love);

        lastPhotoImageView = (ImageView) findViewById(R.id.index_last);
        nextPhotoImageView = (ImageView) findViewById(R.id.index_next);
        currentPhotoInfoTextView = (TextView) findViewById(R.id.index_time);
        currentPhotoIndexTextView = (TextView) findViewById(R.id.current_index);

        image01 = (ImageView) findViewById(R.id.img01);
        leadView = (RelativeLayout) findViewById(R.id.blur_lead_view);
        knowImageView = (Button) findViewById(R.id.leadknow);
        touchtoclean = (TextView) findViewById(R.id.textview_blur);
        blurFraRelativeLayout = (RelativeLayout) findViewById(R.id.blur_photo_relativelayout);
        photoFraRelativeLayout = (RelativeLayout) findViewById(R.id.fra_layout);

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
        handler.sendEmptyMessage(7);

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
                        mHandler.sendEmptyMessage(1);
                    }
                });

            } else {

                mHandler.sendEmptyMessage(1);
            }
        } else {//如果文件不存在，下载文件到缓存
            PictureAirLog.v(TAG, "file is not exist");
            HttpUtil.get(loadPhotoInfo.photoThumbnail_1024, new BinaryHttpResponseHandler() {

                @Override
                public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
//                    // TODO Auto-generated method stub
//                    BufferedOutputStream stream = null;
//                    try {
//                        PictureAirLog.v(TAG,dirFile.toString());
//                        FileOutputStream fsStream = new FileOutputStream(dirFile);
//                        stream = new BufferedOutputStream(fsStream);
//                        stream.write(arg2);
//                    } catch (Exception e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    } finally {
//                        try {
//                            if (stream != null) {
//                                stream.flush();
//                                stream.close();
//                            }
//                        } catch (IOException e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        }
//                        PictureAirLog.v(TAG,"done");
//                    }
                    try {
                        AESKeyHelper.encrypt(arg2, dirFile.toString(), Common.AES_ENCRYPTION_KEY);
                    } catch (InvalidKeyException
                            | NoSuchAlgorithmException
                            | NoSuchPaddingException | IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    bitmap2 = BitmapFactory.decodeByteArray(arg2, 0, arg2.length);
                    mHandler.sendEmptyMessage(0);
                }

                @Override
                public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                    // TODO Auto-generated method stub
                    PictureAirLog.v(TAG, arg3.toString());
                    mHandler.sendEmptyMessage(0);
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
        if (photoInfo.isLove == 1 || pictureAirDbManager.checkLovePhoto(photoInfo.photoId, sharedPreferences.getString(Common.USERINFO_ID, ""), photoInfo.photoPathOrURL)) {
            photoInfo.isLove = 1;
            loveImageButton.setImageResource(R.drawable.discover_like);
        } else {
            loveImageButton.setImageResource(R.drawable.discover_no_like);
        }
        //更新序列号
        currentPhotoIndexTextView.setText(String.format(getString(R.string.photo_index), currentPosition + 1, isEdited ? targetphotolist.size() : photolist.size()));
        currentPhotoInfoTextView.setText(photoInfo.shootOn);


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

        //更新title地点名称
        //		locationTextView.setText(getString(R.string.story_tab_magic));
        locationTextView.setText(photoInfo.locationName);
        //如果是未购买图片，判断是否是第一次进入，如果是，则显示引导图层
        if (photoInfo.isPayed == 0 && photoInfo.onLine == 1) {//未购买的图片
            PictureAirLog.v(TAG, "need show blur view");
            image01.setVisibility(View.INVISIBLE);
            progressDialog = CustomProgressDialog.show(this, getString(R.string.is_loading), false, null);
            touchtoclean.setVisibility(View.VISIBLE);
            blurFraRelativeLayout.setVisibility(View.VISIBLE);
            //			mViewPager.setVisibility(View.GONE);
            loadPhotoPassPhoto(photoInfo, isOnCreate);
            if (!isFirst) {
                if (pictureAirDbManager.checkFirstTimeStartActivity("blurActivity", sharedPreferences.getString(Common.USERINFO_ID, ""))) {//第一次进入
                    PictureAirLog.v(TAG, "new user");
                    leadView.setVisibility(View.VISIBLE);
//                    knowImageView.setOnClickListener(this);
                    leadView.setOnClickListener(this);
                    isFirst = true;
                }
            }
        }
        lastPhotoImageView.setEnabled(true);
        nextPhotoImageView.setEnabled(true);
    }


    /**
     * 根据照片的购买情况确定布局和显示模式
     */
    private void initBlur() {
        PictureAirLog.v(TAG, "initBlur " + currentPosition + "___" + mViewPager.getCurrentItem());
        if (!loadFailed) {//加载成功
            int w = bitmap2.getWidth();
            int h = bitmap2.getHeight();
            PictureAirLog.v(TAG, "bitmap2 width, height" + w + "?" + h);
            scaleW = ScreenUtil.getScreenWidth(this);
            scaleH = photoFraRelativeLayout.getHeight();
            int[] location = new int[2];
            photoFraRelativeLayout.getLocationOnScreen(location);//获取控件在屏幕上的坐标
            marginTop = location[1];
            PictureAirLog.v(TAG, "------------>photoFraRelativeLayout height is " + photoFraRelativeLayout.getHeight());
            //			if(w/h > scaleW/scaleH){
            if (w > h) {//进入横屏模式
                bitmap2 = UtilOfDraw.rotaingImageView(90, bitmap2);
                w = bitmap2.getWidth();
                h = bitmap2.getHeight();
                //			Toast.makeText(this, "进入横屏模式", 0).show();
                //			newToast.setTextAndShow("Into landscape mode", Common.TOAST_SHORT_TIME);
                PictureAirLog.v(TAG, "landscape+" + w + "+" + h);
                scaleH = photoFraRelativeLayout.getHeight();
                scaleW = scaleH * w / (float) h;
                if (scaleW > ScreenUtil.getScreenWidth(this)) {
                    scaleW = ScreenUtil.getScreenWidth(this);
                    scaleH = scaleW * h / (float) w;
                }
            }
            float sw = 0f;
            if (h / (float) w > scaleH / (float) scaleW) {//左右留白
                sw = scaleH / (float) h;
            } else {//上下留白
                sw = scaleW / (float) w;
            }
            matrix = new Matrix();
            matrix.postScale(sw, sw);
            bitmap2 = Bitmap.createBitmap(bitmap2, 0, 0, w, h, matrix, true);
            PictureAirLog.v(TAG, bitmap2.getWidth() + "----" + bitmap2.getHeight());
            sizeW = (int) (scaleW / 2);
            sizeH = (int) (scaleH / 2);
            if (photoInfo.isPayed == 0) {// 未购买的照片

                bm = BitmapFactory.decodeResource(getResources(), R.drawable.round_meitu_1).copy(Config.ARGB_8888, true);
                bitmap1 = UtilOfDraw.blur(bitmap2);//添加模糊度
                PictureAirLog.v(TAG, "bitmap1 = " + bitmap1.getWidth() + "_" + bitmap1.getHeight());
                image01.setImageBitmap(bitmap1);
                PictureAirLog.v(TAG, "---------->" + image01.getWidth() + "_____" + image01.getHeight());
            }
            //			else {
            //				image01.setImageBitmap(bitmap2);
            //				image01.setScaleType(ImageView.ScaleType.MATRIX);
            //				matrix.setScale(rl.getWidth() / bitmap2.getWidth(), rl.getHeight() / bitmap2.getHeight());
            //				image01.setImageMatrix(matrix);
            //			}
            image01.setVisibility(View.VISIBLE);
            yy = (photoFraRelativeLayout.getHeight() - bitmap2.getHeight()) / 2;
            xx = (photoFraRelativeLayout.getWidth() - bitmap2.getWidth()) / 2;

        } else {
            touchtoclean.setText(R.string.failed);
        }
        mViewPager.setVisibility(View.GONE);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!loadFailed) {
            if (photoInfo.isPayed == 0 && photoInfo.onLine == 1) {// 未购买状态
                if (event.getY() < marginTop || event.getY() > scaleH + marginTop) {
                    if (mode == MODE_MOVE) {

                    } else {
                        PictureAirLog.v(TAG, "out of photo range");
                        //				PictureAirLog.v(TAG,"up");
                        //				fads；
                        Message msg = handler.obtainMessage();
                        msg.what = 2;
                        touchtoclean.setVisibility(View.VISIBLE);
                        handler.sendMessage(msg);
                        return super.onTouchEvent(event);
                    }
                }

                Message msg = handler.obtainMessage();
                msg.arg1 = (int) event.getX();
                msg.arg2 = (int) event.getY();

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        mode = MODE_DOWN;
                        PictureAirLog.v(TAG, "-------->down");
                        downX = event.getX();
                        downY = event.getY();

                        //					if (downY < marginTop || downY > scaleH + marginTop) {
                        //						PictureAirLog.v(TAG,"out of photo range");
                        ////						PictureAirLog.v(TAG,"up");
                        ////						Message msg = handler.obtainMessage();
                        ////						msg.what = 2;
                        ////						touchtoclean.setVisibility(View.VISIBLE);
                        ////						handler.sendMessage(msg);
                        //						return super.onTouchEvent(event);
                        //					}


                        count++;
                        touchtoclean.setVisibility(View.INVISIBLE);
                        if (count == 1) {
                            PictureAirLog.v(TAG, "---->1");
                            fir = System.currentTimeMillis();
                            msg.what = 1;
                        }
                        if (count == 2) {
                            PictureAirLog.v(TAG, "------>2");
                            sec = System.currentTimeMillis();
                            if (sec - fir < 500) {// 双击放大
                                msg.what = 3;
                                count = 0;
                                fir = 0;
                                sec = 0;
                            } else {
                                count = 0;
                            }
                        } else {
                            PictureAirLog.v(TAG, "------->" + count);
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
                handler.sendMessage(msg);

            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (flag == true && photoInfo.isPayed == 0) {
            handler.sendEmptyMessage(4);
        } else {
            PreviewPhotoActivity.this.finish();
        }
    }

    /**
     * 制作周边渐变模糊
     * 1.将正方形内切圆以外的部分进行全透明处理
     * 2.将蒙版图片设置成全黑，边缘透明，中心不透明 的渐变效果
     * 3.将截取的图片的透明值设为完全透明
     * 4.将截图和蒙版进行合成，分两点，1）透明值合成，截图的全透明和蒙版的透明进行或运算，得到的是蒙版的透明值
     * 2）颜色值的合成，截图的颜色值和蒙版的颜色值（蒙版只有黑色）进行或运算，得到合成后的颜色值
     * 因为是以十六进制表示，所以高位表示透明值，低位表示颜色值
     *
     * @param b Bitmap对象
     * @return resultBitmap 合成之后的bitmap对象
     */
    private Bitmap Mask(Bitmap b) {
        //如果resultBitmap已经存在，则不需要重新创建一个bitmap
        if (resultBitmap == null)
            //创建一个新的bitmap，三个参数依次是宽，高，config
            resultBitmap = Bitmap.createBitmap(b.getWidth(), b.getHeight(), Bitmap.Config.ARGB_8888);
        int w = bm.getWidth();//获取mask蒙板的宽
        int h = bm.getHeight();//获取高
        float sw = (float) b.getWidth() / w;
        float sh = (float) b.getHeight() / h;
        //matrix为android自带的图片处理的一个类（矩阵）
        matrix.reset();//初始化
        matrix.postScale(sw, sh);//设置缩放的比例
        //将mask蒙板缩放到和截图一样大小
        bm = Bitmap.createBitmap(bm, 0, 0, w, h, matrix, true);
        //创建数组
        int[] pixels_b = new int[b.getWidth() * b.getHeight()];
        int[] pixels_bm = new int[bm.getWidth() * bm.getHeight()];
        //得到传入参数的像素值，并且放入pixels_b中
        b.getPixels(pixels_b, 0, b.getWidth(), 0, 0, b.getWidth(), b.getWidth());
        //得到mask蒙板的像素值，并且放入pixels_bm中
        bm.getPixels(pixels_bm, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm.getWidth());
        //遍历mask蒙板数组，图片全黑部分转化为全透明，其他地方和截取的图片进行合成
        for (int i = 0; i < pixels_bm.length; i++) {
            if (pixels_bm[i] == 0xff000000) {//ff000000为不透明的黑色
                //因为在截取图片的时候，只能截取方块，所以圆以外的部分做全透明处理
                pixels_b[i] = 0;//全透明的黑色
                //			} else if (pixels_b[i] == 0) {
            } else {
                pixels_bm[i] &= 0xff000000;//全部变成000000，但是透明度不变
                pixels_bm[i] = 0xff000000 - pixels_bm[i];//颜色不变，透明度翻转，这两步相当于把蒙版的透明度翻转，颜色值全部变为黑色
                pixels_b[i] &= 0x00ffffff;//颜色值不变，但是透明度全部变成完全透明，相当于将截取到的图片设为完全透明
                pixels_b[i] |= pixels_bm[i];//将蒙版和截图进行合成，分两块，一块是透明度的合成，一块是颜色值的合成
                //透明度的合成，截图完全透明，|操作，取的时蒙版的透明度
                //颜色值的合成，截图的颜色值和蒙版的颜色值进行|操作
            }
        }
        resultBitmap.setPixels(pixels_b, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm.getHeight());
        return resultBitmap;
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
                    pictureAirDbManager.setPictureLove(photoInfo.photoId, sharedPreferences.getString(Common.USERINFO_ID, ""), photoInfo.photoPathOrURL, false);
                    photoInfo.isLove = 0;
                    loveImageButton.setImageResource(R.drawable.discover_no_like);
                } else {
                    Log.d(TAG, "add love");
                    pictureAirDbManager.setPictureLove(photoInfo.photoId, sharedPreferences.getString(Common.USERINFO_ID, ""), photoInfo.photoPathOrURL, true);
                    photoInfo.isLove = 1;
                    loveImageButton.setImageResource(R.drawable.discover_like);
                }
                break;

            case R.id.preview_edit://编辑
                if (leadView.isShown()) {
                    return;
                }
                if (photoInfo.isPayed == 1) {
                    intent = new Intent(this, EditPhotoActivity.class);
                    if (isEdited) {//已经编辑过，取targetlist中的值
                        intent.putExtra("photo", targetphotolist.get(mViewPager.getCurrentItem()));
                    } else {//没有编辑，取正常的值
                        intent.putExtra("photo", photolist.get(mViewPager.getCurrentItem()));
                    }
                    startActivityForResult(intent, 1);

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
                        sharePop.setshareinfo(targetphotolist.get(mViewPager.getCurrentItem()).photoPathOrURL, null, "local", null, SharePop.SHARE_PHOTO_TYPE, handler);
                    } else {//编辑前
                        //判断图片是本地还是网路图片
                        if (photoInfo.onLine == 1) {//网络图片
                            sharePop.setshareinfo(null, photolist.get(mViewPager.getCurrentItem()).photoPathOrURL,
                                    "online", photolist.get(mViewPager.getCurrentItem()).photoId, SharePop.SHARE_PHOTO_TYPE, handler);
                        } else {
                            sharePop.setshareinfo(photolist.get(mViewPager.getCurrentItem()).photoPathOrURL, null, "local", null, SharePop.SHARE_PHOTO_TYPE, handler);
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
                    newToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
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
                    newToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
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
                    newToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
                    dia.dismiss();
                    return;
                }
                progressDialog = CustomProgressDialog.show(this, getString(R.string.is_loading), false, null);
                API1.buyPhoto(photoInfo.photoId, handler);
                dia.dismiss();
                break;

            case R.id.buy_ppp:
                //直接购买PP+
                if (AppUtil.getNetWorkType(PreviewPhotoActivity.this) == AppUtil.NETWORKTYPE_INVALID) {
                    newToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
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
                break;

            case R.id.index_next://下一张
                PictureAirLog.v(TAG, "--------->next");
                changeTab(true);
                lastPhotoImageView.setEnabled(false);
                nextPhotoImageView.setEnabled(false);
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
            handler.obtainMessage(API1.GET_GOODS_SUCCESS, goodsByACache).sendToTarget();
        } else {
            //从网络获取商品,先检查网络
            if (AppUtil.getNetWorkType(MyApplication.getInstance()) != 0) {
                API1.getGoods(handler);
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
                selectPhotoItemInfo.locationName = getString(R.string.magic_location);
                //					selectPhotoItemInfo.albumName = albumName;
                selectPhotoItemInfo.onLine = 0;
                selectPhotoItemInfo.isUploaded = 0;
                selectPhotoItemInfo.isPayed = 1;
                selectPhotoItemInfo.isVideo = 0;

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
            handler.sendEmptyMessage(2);
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        PictureAirLog.v(TAG, "----------->" + myApplication.getRefreshViewAfterBuyBlurPhoto());
        if (photoInfo.isPayed == 0 && photoInfo.onLine == 1) {
            if (myApplication.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_MYPHOTOPASSPAYED)) {

            } else if (myApplication.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_VIEWORSELECTACTIVITYANDPAYED)) {

            } else if (myApplication.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_BLURPAYED)) {

            } else {
                myApplication.setRefreshViewAfterBuyBlurPhoto("");
            }
            //按返回，把状态全部清除
            myApplication.clearIsBuyingPhotoList();
//			myApplication.setIsBuyingPhotoInfo(null);
        }

    }

    //判断网络类型  并做操作。
    public void downLoadPhotos() {
        mNetWorkType = AppUtil.getNetWorkType(getApplicationContext());
        if (mNetWorkType == AppUtil.NETWORKTYPE_MOBILE) {
            //如果是手机流量 ，弹出对话狂
            customdialog = new CustomDialog.Builder(PreviewPhotoActivity.this)
                    .setMessage(getResources().getString(R.string.dialog_download_message))
                    .setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            // TODO Auto-generated method stub
                            customdialog.dismiss();
                        }
                    })
                    .setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            // TODO Auto-generated method stub
                            ArrayList<PhotoInfo> list = new ArrayList<PhotoInfo>();
                            list.add(photolist.get(mViewPager.getCurrentItem()));
                            Intent intent = new Intent(PreviewPhotoActivity.this, DownloadService.class);
                            Bundle bundle = new Bundle();
                            bundle.putParcelableArrayList("photos", list);
                            intent.putExtras(bundle);
                            startService(intent);
                            customdialog.dismiss();
                        }
                    })
                    .setCancelable(false)
                    .create();
            customdialog.show();
        } else if (mNetWorkType == AppUtil.NETWORKTYPE_WIFI) {
            //如果是 wifi ，直接下载
//			ArrayList<PhotoInfo> list = new ArrayList<PhotoInfo>();
//			list.add(photolist.get(mViewPager.getCurrentItem()));
//			Intent intent = new Intent(this, DownloadService.class);
//			Bundle bundle = new Bundle();
//			bundle.putParcelableArrayList("photos", list);
//			intent.putExtras(bundle);
//			startService(intent);
            downloadPic();

//			private void downloadPic() {
//				ArrayList<PhotoInfo> list = new ArrayList<PhotoInfo>();
//				list.add(photolist.get(mViewPager.getCurrentItem()));
//				Intent intent = new Intent(PreviewPhotoActivity.this,
//						DownloadService.class);
//				Bundle bundle = new Bundle();
//				bundle.putParcelableArrayList("photos", list);
//				intent.putExtras(bundle);
//				startService(intent);
//			}
        } else {
            // 网络不可用
        }
    }

    //判断 照片是否购买，并弹出相应的tips
    private void judge() {

        if (myApplication.isPhotoIsPaid()) {// 如果是 购买之后跳转过来的。
            boolean wifiFlag = pictureAirDbManager.checkFirstBuyPhoto(Common.SETTING_WIFI, sharedPreferences.getString(Common.USERINFO_ID, ""));
            boolean syncFlag = pictureAirDbManager.checkFirstBuyPhoto(Common.SETTING_SYNC, sharedPreferences.getString(Common.USERINFO_ID, ""));
            boolean notFirstGoBuyOnePhotoFlag = pictureAirDbManager.checkFirstBuyPhoto(Common.SETTING_NOT_FIRST_BUY_ONE_PHOTO, sharedPreferences.getString(Common.USERINFO_ID, ""));  //不是第一次。
            if (!notFirstGoBuyOnePhotoFlag) {
                customdialog = new CustomDialog.Builder(PreviewPhotoActivity.this)
                        .setMessage(getResources().getString(R.string.dialog_sync_message))
                        .setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                // TODO Auto-generated method stub
                                customdialog.dismiss();
                            }
                        })
                        .setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                // TODO Auto-generated method stub
                                //判断网络状态
                                pictureAirDbManager.insertSettingStatus(Common.SETTING_SYNC, sharedPreferences.getString(Common.USERINFO_ID, ""));
                                customdialog.dismiss();
                                mNetWorkType = AppUtil.getNetWorkType(getApplicationContext());
                                if (mNetWorkType == AppUtil.NETWORKTYPE_MOBILE) {
                                    //如果是数据流量的话。
                                    customdialog = new CustomDialog.Builder(PreviewPhotoActivity.this)
                                            .setMessage(getResources().getString(R.string.dialog_sync_download))
                                            .setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface arg0, int arg1) {
                                                    // TODO Auto-generated method stub
                                                    downloadPic();
                                                    customdialog.dismiss();
                                                    // 不需要的话，流量wifi 都可以下载。
                                                }
                                            })
                                            .setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface arg0, int arg1) {
                                                    // TODO Auto-generated method stub
                                                    customdialog.dismiss();
                                                    // 仅wifi下载。 设置
                                                    pictureAirDbManager.insertSettingStatus(Common.SETTING_WIFI, sharedPreferences.getString(Common.USERINFO_ID, ""));
                                                }
                                            })
                                            .setCancelable(false)
                                            .create();
                                    customdialog.show();

                                } else if (mNetWorkType == AppUtil.NETWORKTYPE_WIFI) {
                                    //如果是 wifi ，直接下载
                                    downloadPic();
                                } else {
                                    // 网络不可用
                                }
                            }
                        })
                        .setCancelable(false)
                        .create();
                customdialog.show();
                pictureAirDbManager.insertSettingStatus(Common.SETTING_NOT_FIRST_BUY_ONE_PHOTO, sharedPreferences.getString(Common.USERINFO_ID, ""));

            } else {
                if (syncFlag) {
                    downloadPic();
                }
            }

        } else {

        }
        myApplication.setPhotoIsPaid(false); // 保持 不是购买的状态。
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

    // 判断 是否第一次提示 同步更新。，并弹出相应的tips
    private void judgeBuyOnePhoto() {
        if (myApplication.isPhotoIsPaid()) {// 如果是 购买之后跳转过来的。
            if (settingUtil.isFirstTipsSyns(sharedPreferences.getString(
                    Common.USERINFO_ID, ""))) {
                if (settingUtil.isAutoUpdate(sharedPreferences.getString(
                        Common.USERINFO_ID, ""))) {
                    if (AppUtil.getNetWorkType(PreviewPhotoActivity.this) == AppUtil.NETWORKTYPE_WIFI) {
                        downloadPic();
                    }
                } else {
                    new CustomDialog(PreviewPhotoActivity.this,
                            R.string.first_tips_syns_msg1,
                            R.string.first_tips_syns_no_msg1,
                            R.string.first_tips_syns_yes_msg1,
                            new CustomDialog.MyDialogInterface() {
                                @Override
                                public void yes() {
                                    // TODO Auto-generated method stub
                                    // //同步更新：下载单张照片，并且修改设置。
                                    settingUtil
                                            .insertSettingAutoUpdateStatus(sharedPreferences
                                                    .getString(
                                                            Common.USERINFO_ID,
                                                            ""));
                                    if (AppUtil
                                            .getNetWorkType(PreviewPhotoActivity.this) == AppUtil.NETWORKTYPE_WIFI) {
                                        downloadPic();
                                    }
                                }

                                @Override
                                public void no() {
                                    // TODO Auto-generated method stub // 取消；不操作
                                    settingUtil
                                            .deleteSettingAutoUpdateStatus(sharedPreferences
                                                    .getString(
                                                            Common.USERINFO_ID,
                                                            ""));
                                }
                            });
                }
                settingUtil.insertSettingFirstTipsSynsStatus(sharedPreferences
                        .getString(Common.USERINFO_ID, ""));
            } else {
                if (settingUtil.isAutoUpdate(sharedPreferences.getString(
                        Common.USERINFO_ID, ""))) {
                    if (AppUtil.getNetWorkType(PreviewPhotoActivity.this) == AppUtil.NETWORKTYPE_WIFI) {
                        downloadPic();
                    }
                }
            }

        } else {

        }
        myApplication.setPhotoIsPaid(false); // 保持 不是购买的状态。
    }

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

}