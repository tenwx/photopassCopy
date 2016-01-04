package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.loopj.android.http.RequestParams;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.MakegiftGoodsAdapter;
import com.pictureair.photopass.entity.CartItemInfo1;
import com.pictureair.photopass.entity.CartPhotosInfo1;
import com.pictureair.photopass.entity.GoodsInfo1;
import com.pictureair.photopass.entity.GoodsInfoJson;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.JsonTools;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.BannerView_PreviewCompositeProduct;
import com.pictureair.photopass.widget.CustomProgressBarPop;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.MyToast;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class MakegiftActivity extends BaseActivity implements OnClickListener {
    //选择商品的horizontalscrollview的popupwindow
    private ListView goodsGridView;
    private MakegiftGoodsAdapter mAdapter;
    private List<GoodsInfo1> allList;
    private List<GoodsInfo1> originalGoodsList;
    private GoodsInfo1 goodsInfo;

    private ImageView returnLayout;
    private ImageView cartButton;
    private TextView selectButton;
    private Button buyButton;
    private Button addtocartButton;
    private TextView cartcountTextView, currencytextview;
    private ImageView buttonSelectproduct;

    private ViewGroup anim_mask_layout;//动画层
    private ImageView buyImg;// 这是在界面上跑的小图片
    private SharedPreferences sp;
    private String tokenId;
    private Editor editor;
    private boolean isbuynow = false;
    private BannerView_PreviewCompositeProduct bannerView_Makegift;

    private String photourl;
    private String idString;
    private ArrayList<PhotoInfo> photoList;
    private ArrayList<CartPhotosInfo1> photoListAfterUpload;//图片上传之后的list
    //选择商品popupwindow
    private PopupWindow selproductPopupWindow;
    private View selproductView_popwindow;

    private ImageView addphotoButton;

    private int count = 1;//图片的数量
    private int upload_index = 0;
    private TextView priceTextView;
    private TextView introduceTextView;
    private int recordcount;

    private CustomProgressDialog progressDialog;
    private CustomProgressBarPop progressBarPop;


    private MyToast newToast;

    private int previewViewWidth;
    private int previewViewHeight;

    private final static int WAIT_DRAW_FINISH = 111;
    private LinearLayout productNameLl;

    private final static String TAG = "MakegiftAct";

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case API1.GET_GOODS_SUCCESS://成功获取商品
                    allList.clear();
                    PictureAirLog.v(TAG, "GET_GOODS_SUCCESS---->" + msg.obj.toString());
                    GoodsInfoJson goodsInfoJson = JsonTools.parseObject(msg.obj.toString(), GoodsInfoJson.class);//GoodsInfoJson.getString()
                    if (goodsInfoJson != null && goodsInfoJson.getGoods().size() > 0) {
                        originalGoodsList = goodsInfoJson.getGoods();
                        PictureAirLog.v(TAG, "goods size: " + allList.size());
                    }

                    for (int i = 0; i < originalGoodsList.size(); i++) {
                        if (!Common.GOOD_NAME_PPP.equals(originalGoodsList.get(i).getName()) &&
                                !Common.GOOD_NAME_SINGLE_DIGITAL.equals(originalGoodsList.get(i).getName())) {
                            allList.add(originalGoodsList.get(i));
                        }
                    }

                    goodsInfo = allList.get(0);

                    priceTextView.setText(allList.get(0).getPrice() + "");
                    introduceTextView.setText(allList.get(0).getDescription());
                    Message message = handler.obtainMessage();
                    message.what = WAIT_DRAW_FINISH;
                    handler.sendMessageDelayed(message, 500);

                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    //将数据保存到缓存中
                    if (ACache.get(MyApplication.getInstance()).getAsString(Common.ALL_GOODS) != null && !ACache.get(MyApplication.getInstance()).getAsString(Common.ALL_GOODS).equals("")) {
                        ACache.get(MyApplication.getInstance()).put(Common.ALL_GOODS, msg.obj.toString(), ACache.GOODS_ADDRESS_ACACHE_TIME);
                    }

                    break;

                case API1.GET_GOODS_FAILED://获取商品失败
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    newToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
                    break;

                case API1.UPLOAD_PHOTO_SUCCESS:
                    System.out.println(msg.obj.toString() + "uploadphotosuccess");
                    if (!"start".equals(msg.obj.toString())) {//说明是调用接口之后返回的数据，需要更新photoId和photoURL
                        JSONObject result = JSONObject.parseObject(msg.obj.toString());
                        String photoUrlString = null;
                        String photoIdString = null;
                        try {
                            photoUrlString = result.getString("photoUrl");
                            photoIdString = result.getString("photoId");
                            System.out.println(photoUrlString + "_" + photoIdString);
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        PhotoInfo info = photoList.get(upload_index - 1);
                        info.isUploaded = 1;
                        info.photoId = photoIdString;
                        info.photoPathOrURL = photoUrlString;
                        photoList.set(upload_index - 1, info);
                    }
                    if (upload_index < photoList.size()) {
                        if (photoList.get(upload_index).onLine == 0) {//需要将图片上传
                            if (photoList.get(upload_index).isUploaded == 1) {//已经上传过了
                                System.out.println("has already uploaded");
                                PhotoInfo selectPhotoItemInfo = photoList.get(upload_index);
                                selectPhotoItemInfo.photoId = photoList.get(upload_index).photoId;
                                selectPhotoItemInfo.photoPathOrURL = photoList.get(upload_index).photoPathOrURL;
                                photoList.set(upload_index, selectPhotoItemInfo);
                                handler.obtainMessage(API1.UPLOAD_PHOTO_SUCCESS, "start").sendToTarget();
                            } else {//还没有上传
                                System.out.println("not uploaded, starting upload");
                                String photourl = photoList.get(upload_index).photoPathOrURL;
                                System.out.println("上传的图片URL" + photourl);
                                // 需要上传选择的图片
                                RequestParams params = new RequestParams();
                                try {
                                    params.put("file", new File(photourl), "application/octet-stream");
                                    params.put(Common.USERINFO_TOKENID, tokenId);
                                    System.out.println(tokenId + "tokenid");
                                    API1.SetPhoto(params, handler, upload_index, progressBarPop);
                                } catch (FileNotFoundException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                        } else {//服务器上获取的图片，只需要将photoid获取就行
                            PhotoInfo info = photoList.get(upload_index);
                            info.photoId = photoList.get(upload_index).photoId;
                            info.photoPathOrURL = photoList.get(upload_index).photoThumbnail_512;
                            photoList.set(upload_index, info);
                            handler.obtainMessage(API1.UPLOAD_PHOTO_SUCCESS, "start").sendToTarget();
                        }
                        upload_index++;
                    } else {//开始加入购物车
                        upload_index = 0;
                        //编辑传入照片的信息
                        JSONArray embedPhotos = new JSONArray();//放入图片的图片id数组
                        for (int i = 0; i < photoList.size(); i++) {
                            JSONObject photoid = new JSONObject();
                            photoid.put("photoId", photoList.get(i).photoId);
                            embedPhotos.add(photoid);
                        }
                        PictureAirLog.v(TAG, embedPhotos.toString());
                        PictureAirLog.out(embedPhotos.toString());
                        API1.addToCart(goodsInfo.getGoodsKey(), 1, false, embedPhotos, handler);
                    }
                    break;

                case API1.ADD_TO_CART_FAILED:
                case API1.UPLOAD_PHOTO_FAILED:
                    if (progressBarPop.isShowing()) {
                        progressBarPop.dismiss();
                    }
                    upload_index = 0;
                    newToast.setTextAndShow(R.string.upload_failed, Common.TOAST_SHORT_TIME);
                    break;

                case WAIT_DRAW_FINISH:
                    //此处，如果数据已经返回，但是控件还没有画好的话，会显示不出来。需要做判断
                    if (previewViewWidth != 0 && previewViewHeight != 0) {//onCreate已经执行完，显示图片
                        System.out.println("--------->ok");
                        setProductImage(goodsInfo.getName(), (goodsInfo.getPictures().size() > 0) ? goodsInfo.getPictures().get(0).getUrl() : "");
                    } else {//onCreate还没执行完，需要等待
                        System.out.println("---------->not ok, waiting.....");
                        handler.sendMessageDelayed(msg, 500);
                    }
                    break;

                case API1.ADD_TO_CART_SUCCESS:
                    if (progressBarPop.isShowing()) {
                        progressBarPop.dismiss();
                    }
                    JSONObject addcart = JSONObject.parseObject(msg.obj.toString());
                    PictureAirLog.v(TAG, "addtocart==" + addcart);
                    editor = sp.edit();
                    editor.putInt(Common.CART_COUNT, sp.getInt(Common.CART_COUNT, 0) + 1);
                    editor.commit();
                    String itemidString = addcart.getString("cartId");
                    if (isbuynow) {//获取订单信息，传送到下一界面
                        Intent intent = new Intent(MakegiftActivity.this, SubmitOrderActivity.class);
                        ArrayList<CartItemInfo1> orderinfoArrayList = new ArrayList<>();
                        CartItemInfo1 cartItemInfo = new CartItemInfo1();
                        cartItemInfo.setProductName(goodsInfo.getName());
                        cartItemInfo.setPrice(goodsInfo.getPrice() * 1);
                        cartItemInfo.setUnitPrice(goodsInfo.getPrice());
                        cartItemInfo.setEntityType(goodsInfo.getEntityType());
                        cartItemInfo.setCartProductType(1);
                        photoListAfterUpload.clear();
                        for (int i = 0; i < photoList.size(); i++) {
                            CartPhotosInfo1 cartPhotosInfo = new CartPhotosInfo1();
                            cartPhotosInfo.setPhotoUrl(photoList.get(i).photoPathOrURL);
                            cartPhotosInfo.setPhotoId(photoList.get(i).photoId);
                            photoListAfterUpload.add(cartPhotosInfo);
                        }

                        cartItemInfo.setEmbedPhotos(photoListAfterUpload);
                        cartItemInfo.setDescription(goodsInfo.getDescription());
                        cartItemInfo.setQty(1);
                        cartItemInfo.setCartId(itemidString);//会返回此数据
                        cartItemInfo.setStoreId(goodsInfo.getStoreId());
                        if (goodsInfo.getPictures() != null && goodsInfo.getPictures().size() > 0) {
                            String[] cartProductImageUrl = new String[goodsInfo.getPictures().size()];
                            for (int i = 0; i < goodsInfo.getPictures().size(); i++) {
                                cartProductImageUrl[i] = goodsInfo.getPictures().get(i).getUrl();
                            }
                            cartItemInfo.setPictures(cartProductImageUrl);
                        }
                        cartItemInfo.setGoodsKey(goodsInfo.getGoodsKey());
                        orderinfoArrayList.add(cartItemInfo);
                        intent.putExtra("orderinfo", orderinfoArrayList);
                        intent.putExtra("activity", "previewproduct");
                        MakegiftActivity.this.startActivity(intent);
                    } else {
                        buyImg = new ImageView(MakegiftActivity.this);// buyImg是动画的图片
                        buyImg.setImageResource(R.drawable.addtocart);// 设置buyImg的图片
                        setAnim(buyImg);// 开始执行动画
                    }
                    break;


                default:
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_makegift);
        init();
    }

    private void init() {
        sp = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);

        tokenId = sp.getString(Common.USERINFO_TOKENID, null);
        newToast = new MyToast(this);
        currencytextview = (TextView) findViewById(R.id.textView2);
        currencytextview.setText(sp.getString(Common.CURRENCY, Common.DEFAULT_CURRENCY));
        priceTextView = (TextView) findViewById(R.id.textview_productprice);
        introduceTextView = (TextView) findViewById(R.id.product_detail);
        addphotoButton = (ImageView) findViewById(R.id.addimage);
        addphotoButton.setOnClickListener(this);
        returnLayout = (ImageView) findViewById(R.id.rt);
        returnLayout.setOnClickListener(this);
        cartButton = (ImageView) findViewById(R.id.button_cart);
        selectButton = (TextView) findViewById(R.id.product_name_tv);
        productNameLl = (LinearLayout) findViewById(R.id.product_name_ll);
        buyButton = (Button) findViewById(R.id.button_buy);
        addtocartButton = (Button) findViewById(R.id.button_addtocart);
        buyButton.setTypeface(MyApplication.getInstance().getFontBold());
        addtocartButton.setTypeface(MyApplication.getInstance().getFontBold());
        buttonSelectproduct = (ImageView) findViewById(R.id.button_selectproduct);
        cartButton.setOnClickListener(this);
        progressBarPop = new CustomProgressBarPop(this, findViewById(R.id.makegift_relativate), CustomProgressBarPop.TYPE_UPLOAD);
//        selectButton.setOnClickListener(this);

        productNameLl.setOnClickListener(this);
        buyButton.setOnClickListener(this);
        addtocartButton.setOnClickListener(this);
        cartcountTextView = (TextView) findViewById(R.id.textview_cart_count);
        cartcountTextView.setOnClickListener(this);
        progressDialog = CustomProgressDialog.show(this, getString(R.string.is_loading), true, null);
        allList = new ArrayList<>();
        originalGoodsList = new ArrayList<>();
        //从缓层中获取数据
        String goodsByACache = ACache.get(this).getAsString(Common.ALL_GOODS);
        if (goodsByACache == null ||  goodsByACache.equals("")) {
            API1.getGoods(handler);
        } else {
            Message message = handler.obtainMessage();
            message.what = API1.GET_GOODS_SUCCESS;
            message.obj = goodsByACache;
            handler.sendMessage(message);
        }
        recordcount = sp.getInt(Common.CART_COUNT, 0);
        if (recordcount <= 0) {
            cartcountTextView.setVisibility(View.INVISIBLE);
        } else {
            cartcountTextView.setVisibility(View.VISIBLE);
            cartcountTextView.setText(recordcount + "");
        }
        photoList = new ArrayList<PhotoInfo>();
        photoListAfterUpload = new ArrayList<>();
        PhotoInfo itemInfo = new PhotoInfo();
        itemInfo = getIntent().getParcelableExtra("selectPhoto");
//		if (Common.ALBUM_PHOTOPASS.equals(getIntent().getStringExtra("albums"))) {//网络图片
//			photourl = getIntent().getStringExtra("thumbnailURL");//获取照片路径
//			idString = getIntent().getStringExtra("photoId");
//			itemInfo.photoThumbnail_512 = photourl;
//			itemInfo.photoId = idString;
//			itemInfo.albumName = Common.ALBUM_PHOTOPASS;
//			System.out.println(photourl+"____"+idString);
//		}else {//本地图片
//			photourl = getIntent().getStringExtra("photopath");//获取照片路径
////			idString = getIntent().getStringExtra("id");
//			itemInfo.photoPathOrURL = photourl;
//			itemInfo.albumName = "notphotopass";
//			System.out.println(photourl+"____"+idString);
//		}
        photoList.add(itemInfo);
        bannerView_Makegift = (BannerView_PreviewCompositeProduct) findViewById(R.id.bannerview_makegift_detail);
        ViewTreeObserver viewTreeObserver = bannerView_Makegift.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                bannerView_Makegift.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                previewViewWidth = bannerView_Makegift.getWidth();
                previewViewHeight = bannerView_Makegift.getHeight();
            }
        });
        //选择商品的popupwindow
        selproductView_popwindow = getLayoutInflater().inflate(R.layout.popupwindow_selectproduct, null);
        selproductPopupWindow = new PopupWindow(selproductView_popwindow, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        selproductPopupWindow.setFocusable(false);//设置能够获得焦点
        selproductPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//此代码和上一条代码两者结合，实现能够点击popupwindow外面将popupwindow关闭
        goodsGridView = (ListView) selproductView_popwindow.findViewById(R.id.id_horizontalScrollView);
        mAdapter = new MakegiftGoodsAdapter(MakegiftActivity.this, allList);

        goodsGridView.setAdapter(mAdapter);
        goodsGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                if (allList.get(position).getName().equals("Album")) {
                    System.out.println("album");
                    buyButton.setText("Buy (" + count + ")");
                    addphotoButton.setVisibility(View.VISIBLE);
                } else {
                    if (addphotoButton.isShown()) {
                        addphotoButton.setVisibility(View.INVISIBLE);
                    }
                }
                goodsInfo = allList.get(position);
                priceTextView.setText(goodsInfo.getPrice() + "");
                introduceTextView.setText(goodsInfo.getDescription());
                setProductImage(goodsInfo.getName(), (goodsInfo.getPictures().size() > 0) ? goodsInfo.getPictures().get(0).getUrl() : "");
                gonePopupwindow();
            }
        });

        selproductView_popwindow.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                gonePopupwindow();
                return false;
            }
        });
    }

    /**
     * 头部三角符号 朝上（pop显示的时候）
     */
    private void buttonSelectproductShowUp() {
        setRotatingAnimation(180f);
    }

    /**
     * 头部三角符号 朝下（pop显示的时候）
     */
    private void buttonSelectproductShowDown() {
        setRotatingAnimation(360f);
    }

    /**
     * 设置旋转动画
     */
    private void setRotatingAnimation(float value) {
        RotateAnimation ra;
        ra = new RotateAnimation(0f, value, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setRepeatCount(1);//设置重复次数
        ra.setDuration(10);//设置动画持续时间
        ra.setFillAfter(true);//动画执行完后是否停留在执行完的状态
        buttonSelectproduct.startAnimation(ra);
    }

    /**
     * popwindow消失
     */
    private void gonePopupwindow() {
        if (selproductPopupWindow.isShowing()) {
            selproductPopupWindow.dismiss();
            buttonSelectproductShowDown();
        }
    }

    /**
     * 设置makeGift的背景预览图
     *
     * @param productName 商品名字
     * @param productURL  商品预览图的背景图片URL
     */
    private void setProductImage(String productName, String productURL) {
        System.out.println("------->" + productURL);
        if (productName.equals("canvas")) {
            //1.画布，商品宽 355
            //		 商品高 258
            //       左边留白 20
            //		 上边留白 12
            //		 预览图片宽 355-20-19 = 316
            //		 预览图片高 258-12-19 = 227
            bannerView_Makegift.changeimagepath(photoList, productURL, previewViewWidth, previewViewHeight, 355, 258, 20, 12, 316, 227, 0, 0, 0, "canvas");//设置bannerview的图片
//		}else if (productName.equals("iphone5Case")) {
//			//2.手机后盖，商品宽 480
//			//		 商品高 946
//			//       左边留白 0
//			//		 上边留白 0
//			//		 预览图片宽 480
//			//		 预览图片高 946
//			bannerView_Makegift.changeimagepath(photoList, productURL, previewViewWidth, previewViewHeight, 480, 946, 0, 0, 480, 946, 0, R.drawable.iphone_case_mask_bottom, R.drawable.iphone_case_mask_top, "iphone5Case");//设置bannerview的图片
        } else if (productName.equals(Common.GOOD_NAME_SINGLE_DIGITAL)) {
            //3.数码商品，商品宽 300
            //		 商品高 217
            //       左边留白 22
            //		 上边留白 26
            //		 预览图片宽 300-22-21 = 257
            //		 预览图片高 217-26-25 = 166
            bannerView_Makegift.changeimagepath(photoList, productURL, previewViewWidth, previewViewHeight, 300, 217, 22, 26, 257, 166, 0, 0, 0, Common.GOOD_NAME_SINGLE_DIGITAL);//设置bannerview的图片
        } else if (productName.equals("4R Print")) {
            //4.4r相框，商品宽 180
            //		 商品高 120
            //       左边留白 7
            //		 上边留白 7
            //		 预览图片宽 180-7-7 = 166
            //		 预览图片高 120-7-7 = 106
            bannerView_Makegift.changeimagepath(photoList, productURL, previewViewWidth, previewViewHeight, 180, 120, 7, 7, 166, 106, 0, 0, 0, "4R Print");//设置bannerview的图片
        } else if (productName.equals(Common.GOOD_NAME_6R) || productName.equals(Common.GOOD_NAME_COOK)) {
            //5.6r相框，商品宽 240
            //		 商品高 180
            //       左边留白 10
            //		 上边留白 14
            //		 预览图片宽 240-10-10 = 220
            //		 预览图片高 180-14-14 = 152
            bannerView_Makegift.changeimagepath(photoList, productURL, previewViewWidth, previewViewHeight, 240, 180, 10, 14, 220, 152, 0, 0, 0, productName);//设置bannerview的图片
        } else if (productName.equals("keyChain")) {
            //6.钥匙圈，商品宽 205
            //		 商品高 89
            //       左边留白 88
            //		 上边留白 18
            //		 预览图片宽 205-88-21 = 205 - 109 = 96
            //		 预览图片高 89-18-16 = 55
            bannerView_Makegift.changeimagepath(photoList, productURL, previewViewWidth, previewViewHeight, 205, 89, 88, 18, 96, 55, 0.15f, 0, 0, "keyChain");//设置bannerview的图片
//		}else if (productName.equals("mug")) {
//			//7.杯子，商品宽 185
//			//		 商品高 160
//			//       左边留白 10
//			//		 上边留白 12
//			//		 预览图片宽 185-10-61 = 114
//			//		 预览图片高 160-12-34 = 114
//			bannerView_Makegift.changeimagepath(photoList, productURL, previewViewWidth, previewViewHeight, 185, 160, 10, 12, 114, 114, 0, 0, R.drawable.mug_mask_top, "mug");//设置bannerview的图片
        } else {
            //3.数码商品，商品宽 300
            //		 商品高 217
            //       左边留白 22
            //		 上边留白 26
            //		 预览图片宽 300-22-21 = 257
            //		 预览图片高 217-26-25 = 166
            bannerView_Makegift.changeimagepath(photoList, productURL, previewViewWidth, previewViewHeight, 300, 217, 22, 26, 257, 166, 0, 0, 0, Common.GOOD_NAME_SINGLE_DIGITAL);//设置bannerview的图片
        }
    }


    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        recordcount = sp.getInt(Common.CART_COUNT, 0);
        if (recordcount <= 0) {
            cartcountTextView.setVisibility(View.INVISIBLE);
        } else {
            cartcountTextView.setVisibility(View.VISIBLE);
            cartcountTextView.setText(recordcount + "");
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        Intent intent;
        switch (v.getId()) {
            case R.id.rt://返回按钮
                finish();
                break;

            case R.id.textview_cart_count:
            case R.id.button_cart:
                intent = new Intent(this, CartActivity.class);
                startActivity(intent);
                break;

            case R.id.product_name_ll:
                PictureAirLog.out("选择商品");
                if (selproductPopupWindow.isShowing()) {
                    selproductPopupWindow.dismiss();
                    buttonSelectproductShowDown();
                } else {
                    mAdapter.notifyDataSetChanged();
                    selproductPopupWindow.showAsDropDown(findViewById(R.id.product_name_tv));
                    buttonSelectproductShowUp();
                }
                break;

            case R.id.button_buy:
                if (goodsInfo == null) {
                    newToast.setTextAndShow(R.string.http_error_code_5005, Toast.LENGTH_SHORT);
                    return;
                }
                if (null != sp.getString(Common.USERINFO_ID, null)) {
                    Message message = handler.obtainMessage();
                    message.what = API1.UPLOAD_PHOTO_SUCCESS;
                    isbuynow = true;//buy now
                    message.obj = "start";
                    handler.sendMessage(message);
                    progressBarPop.show(0);
                } else {
                    intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                }
                break;

            case R.id.button_addtocart://加入购物车因为还有个上传图片的过程，先上传图片，获取photo返回数据之后再调用购物车API。所以整个动画流程中，有一部分需要进度条
                if (goodsInfo == null) {
                    newToast.setTextAndShow(R.string.http_error_code_5005, Toast.LENGTH_SHORT);
                    return;
                }
                if (null != sp.getString(Common.USERINFO_ID, null)) {
                    Message message = handler.obtainMessage();
                    message.what = API1.UPLOAD_PHOTO_SUCCESS;
                    isbuynow = false;//add to cart
                    message.obj = "start";
                    handler.sendMessage(message);
                    progressBarPop.show(0);
                } else {
                    intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                }
                break;

            case R.id.addimage://添加图片按钮
                System.out.println("addimage");
                intent = new Intent(this, SelectPhotoActivity1.class);
                intent.putExtra("photopath", photoList);
                intent.putExtra("activity", "makegiftactivity");
                startActivityForResult(intent, 1);
                break;

            default:
                break;

        }
    }

    //回调函数处理新添加的图片
    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 20) {//成功返回
            //do update operation here
            ArrayList<PhotoInfo> list = (ArrayList<PhotoInfo>) data.getSerializableExtra("photopath");
            PhotoInfo info;
            for (int i = 0; i < list.size(); i++) {
                info = new PhotoInfo();
                info.photoPathOrURL = list.get(i).photoPathOrURL;
//				info.Id = list.get(i).Id;
                photoList.add(info);
                System.out.println("i=" + i);
            }
            System.out.println("photolist:" + photoList.size());
            addphotoButton.setVisibility(View.INVISIBLE);

            buyButton.setText("Buy (" + photoList.size() + ")");
            //设置显示的图片，但是暂时没有这个商品，可以留着以后写
//			bannerView_Makegift.findimagepath(photoList);//设置bannerview的图片
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
     * 设置动画
     */
    private void setAnim(final View v) {
        anim_mask_layout = null;
        anim_mask_layout = createAnimLayout();
        int[] start_location = new int[2];// 一个整型数组，用来存储按钮的在屏幕的X、Y坐标
        start_location[0] = ScreenUtil.getScreenWidth(this) / 2 - Common.CART_WIDTH;//减去的值和图片大小有关系
        start_location[1] = ScreenUtil.getScreenHeight(this) / 2 - Common.CART_HEIGHT;
        // 将组件添加到我们的动画层上
        final View view = addViewToAnimLayout(anim_mask_layout, v, start_location);
        int[] end_location = new int[2];
        cartcountTextView.getLocationInWindow(end_location);
        // 计算位移
        final int endX = end_location[0] - start_location[0];
        final int endY = end_location[1] - start_location[1];

        ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setInterpolator(new LinearInterpolator());//匀速
        scaleAnimation.setRepeatCount(0);//不重复
        scaleAnimation.setFillAfter(true);//停在最后动画
        AnimationSet set = new AnimationSet(false);
        set.setFillAfter(false);
        set.addAnimation(scaleAnimation);
        set.setDuration(500);//动画整个时间
        view.startAnimation(set);//开始动画
        set.setAnimationListener(new AnimationListener() {
            // 动画的开始
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
            }

            // 动画的结束
            @Override
            public void onAnimationEnd(Animation animation) {
                //x轴的路径动画，匀速
                TranslateAnimation translateAnimationX = new TranslateAnimation(0,
                        endX, 0, 0);
                translateAnimationX.setInterpolator(new LinearInterpolator());
                translateAnimationX.setRepeatCount(0);// 动画重复执行的次数
                //y轴的路径动画，加速
                TranslateAnimation translateAnimationY = new TranslateAnimation(0, 0,
                        0, endY);
                translateAnimationY.setInterpolator(new AccelerateInterpolator());
                translateAnimationY.setRepeatCount(0);// 动画重复执行的次数
                ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.1f, 1.0f, 0.1f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
                AnimationSet set2 = new AnimationSet(false);
                //要先添加形状的，后添加位移的，不然动画效果不能达到要求
                set2.addAnimation(scaleAnimation);
                set2.addAnimation(translateAnimationY);
                set2.addAnimation(translateAnimationX);

                set2.setFillAfter(false);
                set2.setStartOffset(200);//等待时间
                set2.setDuration(800);// 动画的执行时间
                view.startAnimation(set2);
                set2.setAnimationListener(new AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // TODO Auto-generated method stub
                        v.setVisibility(View.GONE);//控件消失
                        int i = sp.getInt(Common.CART_COUNT, 0);
                        if (i <= 0) {
                            cartcountTextView.setVisibility(View.INVISIBLE);
                        } else {
                            cartcountTextView.setVisibility(View.VISIBLE);
                            cartcountTextView.setText(i + "");
                        }
                    }
                });
            }
        });
    }

    private ViewGroup createAnimLayout() {
        ViewGroup rootView = (ViewGroup) this.getWindow().getDecorView();
        LinearLayout animLayout = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        animLayout.setLayoutParams(lp);
        animLayout.setBackgroundResource(android.R.color.transparent);
        rootView.addView(animLayout);
        return animLayout;
    }

    private View addViewToAnimLayout(ViewGroup vg, final View view,
                                     int[] location) {
        int x = location[0];
        int y = location[1];
        vg.addView(view);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = x;
        lp.topMargin = y;
        view.setLayoutParams(lp);
        return view;
    }

    @Override
    protected void onPause() {
        super.onPause();
        gonePopupwindow();
    }
}
