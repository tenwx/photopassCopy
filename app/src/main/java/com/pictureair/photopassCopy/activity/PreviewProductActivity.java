package com.pictureair.photopassCopy.activity;

import android.content.Intent;
import android.os.Bundle;
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
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopassCopy.MyApplication;
import com.pictureair.photopassCopy.R;
import com.pictureair.photopassCopy.entity.CartItemInfo;
import com.pictureair.photopassCopy.entity.CartPhotosInfo;
import com.pictureair.photopassCopy.entity.GoodsInfo;
import com.pictureair.photopassCopy.entity.PhotoInfo;
import com.pictureair.photopassCopy.http.rxhttp.RxSubscribe;
import com.pictureair.photopassCopy.util.API2;
import com.pictureair.photopassCopy.util.AppUtil;
import com.pictureair.photopassCopy.util.Common;
import com.pictureair.photopassCopy.util.PictureAirLog;
import com.pictureair.photopassCopy.util.SPUtils;
import com.pictureair.photopassCopy.util.ScreenUtil;
import com.pictureair.photopassCopy.widget.BannerView_PreviewCompositeProduct;
import com.pictureair.photopassCopy.widget.PWToast;

import java.util.ArrayList;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 产品预览，处理商品的合成
 *
 * @author bauer_bao
 */
public class PreviewProductActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = "PreviewProductActivity";
    private ImageView returnButton;
    private Button buynowButton;
    private Button addtocartButton;
    private ImageView shoppingcartButton;
    private ViewGroup anim_mask_layout;//动画层
    private ImageView buyImg;// 这是在界面上跑的小图片
    private ArrayList<PhotoInfo> list;

    private int recordcount = 0; //记录数据库中有几条记录
    private TextView counTextView;

    private BannerView_PreviewCompositeProduct bannerView_Preview;
    private boolean isbuynow = false;

    private int viewWidth;
    private int viewHeight;

    private GoodsInfo goodsInfo;//存放商品信息
    private String picUrl = "";

    private PWToast newToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        newToast = new PWToast(this);
        //获取从selectphotoactivity传递过来的信息
        goodsInfo = (GoodsInfo) getIntent().getSerializableExtra("goodsInfo");
        list = (ArrayList<PhotoInfo>) getIntent().getSerializableExtra("photopath");
        PictureAirLog.v(TAG, "goodsInfo name" + goodsInfo.getName());
        PictureAirLog.v(TAG, "list size" + list.size());
        init();
    }

    @Override
    public void recreate() {
        super.recreate();
    }

    private void init() {
        returnButton = (ImageView) findViewById(R.id.imageButton1_back);
        returnButton.setOnClickListener(this);
        buynowButton = (Button) findViewById(R.id.button_buy_now);
        buynowButton.setOnClickListener(this);
        addtocartButton = (Button) findViewById(R.id.button_add_to_cart);
        addtocartButton.setOnClickListener(this);
        shoppingcartButton = (ImageView) findViewById(R.id.button1_cart);
        counTextView = (TextView) findViewById(R.id.textview_cart_count);
        counTextView.setOnClickListener(this);
        shoppingcartButton.setOnClickListener(this);
        buynowButton.setTypeface(MyApplication.getInstance().getFontBold());
        addtocartButton.setTypeface(MyApplication.getInstance().getFontBold());

        recordcount = SPUtils.getInt(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
        if (recordcount <= 0) {
            counTextView.setVisibility(View.INVISIBLE);
        } else {
            counTextView.setVisibility(View.VISIBLE);
            counTextView.setText(recordcount + "");
        }
        bannerView_Preview = (BannerView_PreviewCompositeProduct) findViewById(R.id.bannerview_preview_detail);

        if (goodsInfo.getPictures() != null && goodsInfo.getPictures().size() > 0) {
            picUrl = goodsInfo.getPictures().get(0).getUrl();
        }

        ViewTreeObserver viewTreeObserver = bannerView_Preview.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                bannerView_Preview.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                viewHeight = bannerView_Preview.getHeight();
                viewWidth = bannerView_Preview.getWidth();
                PictureAirLog.v(TAG, "-------------->" + goodsInfo.getEntityType());
                PictureAirLog.v(TAG, "name-------------->" + goodsInfo.getName());
                if (goodsInfo.getName().equals("canvas")) {
                    //1.画布，商品宽 355
                    //		 商品高 258
                    //       左边留白 20
                    //		 上边留白 12
                    //		 预览图片宽 355-20-19 = 316
                    //		 预览图片高 258-12-19 = 227
                    bannerView_Preview.initImageList(list, picUrl, viewWidth, viewHeight,
                            355, 258, 20, 12, 316, 227, 0, 0, 0, "canvas");//设置bannerview的图片
//				}else if (getName().equals("iphone5Case")) {
//					//2.手机后盖，商品宽 480
//					//		 商品高 946
//					//       左边留白 0
//					//		 上边留白 0
//					//		 预览图片宽 480
//					//		 预览图片高 946
//                    bannerView_Preview.initImageList(list, picUrl, viewWidth, viewHeight,
//                            480, 946, 0, 0, 480, 946, 0, R.drawable.iphone_case_mask_bottom, R.drawable.iphone_case_mask_top, "iphone5Case");//设置bannerview的图片
                } else if (goodsInfo.getName().equals("4R Print")) {
                    //4.4r相框，商品宽 180
                    //		 商品高 120
                    //       左边留白 7
                    //		 上边留白 7
                    //		 预览图片宽 180-7-7 = 166
                    //		 预览图片高 120-7-7 = 106
                    bannerView_Preview.initImageList(list, picUrl, viewWidth, viewHeight,
                            180, 120, 7, 7, 166, 106, 0, 0, 0, "4R Print");//设置bannerview的图片
                } else if (goodsInfo.getName().equals(Common.GOOD_NAME_6R) ||
                        goodsInfo.getName().equals(Common.GOOD_NAME_COOK)) {
                    //5.6r相框，商品宽 240
                    //		 商品高 180
                    //       左边留白 10
                    //		 上边留白 14
                    //		 预览图片宽 240-10-10 = 220
                    //		 预览图片高 180-14-14 = 152
                    bannerView_Preview.initImageList(list, picUrl, viewWidth, viewHeight,
                            240, 180, 10, 14, 220, 152, 0, 0, 0, goodsInfo.getNameAlias());//设置bannerview的图片
                } else if (goodsInfo.getName().equals("keyChain")) {
                    //6.钥匙圈，商品宽 205
                    //		 商品高 89
                    //       左边留白 88
                    //		 上边留白 18
                    //		 预览图片宽 205-88-21 = 205 - 109 = 96
                    //		 预览图片高 89-18-16 = 55
                    bannerView_Preview.initImageList(list, picUrl, viewWidth, viewHeight,
                            205, 89, 88, 18, 96, 55, 0.15f, 0, 0, "keyChain");//设置bannerview的图片
//				}else if (goodsInfo.getName().equals("mug")) {
//					//7.杯子，商品宽 185
//					//		 商品高 160
//					//       左边留白 10
//					//		 上边留白 12
//					//		 预览图片宽 185-10-61 = 114
//					//		 预览图片高 160-12-34 = 114
//                    bannerView_Preview.initImageList(list, picUrl, viewWidth, viewHeight,
//                            185, 160, 10, 12, 114, 114, 0, 0, R.drawable.mug_mask_top, "mug");//设置bannerview的图片
                } else if (goodsInfo.getName().equals(Common.GOOD_NAME_SINGLE_DIGITAL)) {
                    //3.数码商品，商品宽 300
                    //		 商品高 217
                    //       左边留白 22
                    //		 上边留白 26
                    //		 预览图片宽 300-22-21 = 257
                    //		 预览图片高 217-26-25 = 166
                    bannerView_Preview.initImageList(list, picUrl, viewWidth, viewHeight,
                            300, 217, 22, 26, 257, 166, 0, 0, 0, Common.GOOD_NAME_SINGLE_DIGITAL);//设置bannerview的图片
                } else {
                    //3.数码商品，商品宽 300
                    //		 商品高 217
                    //       左边留白 22
                    //		 上边留白 26
                    //		 预览图片宽 300-22-21 = 257
                    //		 预览图片高 217-26-25 = 166
                    bannerView_Preview.initImageList(list, picUrl, viewWidth, viewHeight,
                            300, 217, 22, 26, 257, 166, 0, 0, 0, Common.GOOD_NAME_SINGLE_DIGITAL);//设置bannerview的图片
                }

            }
        });

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        recordcount = SPUtils.getInt(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
        if (recordcount <= 0) {
            counTextView.setVisibility(View.INVISIBLE);
        } else {
            counTextView.setVisibility(View.VISIBLE);
            counTextView.setText(recordcount + "");
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        Intent intent;
        switch (v.getId()) {
            case R.id.imageButton1_back:
                finish();
                break;

            case R.id.button_buy_now:
                isbuynow = true;//buy now
                addCartAndBuy();
                break;

            case R.id.button_add_to_cart://先要上传选择的图片，然后再加入购物车
                isbuynow = false;//add to cart
                addCartAndBuy();
                break;

            case R.id.textview_cart_count:
            case R.id.button1_cart:
                //检查网络
                if (AppUtil.getNetWorkType(PreviewProductActivity.this) == AppUtil.NETWORKTYPE_INVALID) {
                    newToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
                    return;
                }
                intent = new Intent(this, CartActivity.class);
                PreviewProductActivity.this.startActivity(intent);
                break;

            default:
                break;
        }
    }

    /**
     * 加入购物车
     */
    private void addCartAndBuy() {
        //检查网络
        if (AppUtil.getNetWorkType(PreviewProductActivity.this) == AppUtil.NETWORKTYPE_INVALID) {
            newToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
            return;
        }
        showPWProgressDialog();
        Observable.just(list)
                .subscribeOn(Schedulers.io())
                .map(new Func1<ArrayList<PhotoInfo>, ArrayList<PhotoInfo>>() {
                    @Override
                    public ArrayList<PhotoInfo> call(ArrayList<PhotoInfo> arrayList) {
                        for (int i = 0; i < arrayList.size(); i++) {
                            //服务器上获取的图片，只需要将photoid获取就行
                            PhotoInfo info = arrayList.get(i);
                            info.setPhotoOriginalURL(info.getPhotoThumbnail_512());
                        }
                        return arrayList;
                    }
                })
                .flatMap(new Func1<ArrayList<PhotoInfo>, Observable<JSONObject>>() {
                    @Override
                    public Observable<JSONObject> call(ArrayList<PhotoInfo> photoInfos) {
                        //开始加入购物车
                        if (goodsInfo.getName().equals(Common.GOOD_NAME_SINGLE_DIGITAL)) {//需要批量加入
                            JSONArray goodsArray = new JSONArray();
                            JSONObject goodsObject;
                            for (int i = 0; i < photoInfos.size(); i++) {
                                goodsObject = new JSONObject();
                                goodsObject.put(Common.GOODS_KEY, goodsInfo.getGoodsKey());
                                goodsObject.put(Common.QTY, 1);
                                goodsObject.put(Common.IS_JUST_BUY, isbuynow);

                                JSONArray embedPhotos = new JSONArray();
                                JSONObject embedPhoto = new JSONObject();
                                embedPhoto.put(Common.PHOTO_ID, photoInfos.get(i).getPhotoId());
                                embedPhotos.add(embedPhoto);
                                goodsObject.put(Common.EMBEDPHOTOS, embedPhotos);
                                goodsArray.add(goodsObject);
                            }
                            PictureAirLog.out("goods----->" + goodsArray.toJSONString());
                            return API2.batchAddToCarts(MyApplication.getTokenId(), goodsArray.toJSONString());

                        } else {//正常加入购物车
                            //编辑传入照片的信息
                            JSONArray embedPhotos = new JSONArray();//放入图片的图片id数组
                            for (int i = 0; i < photoInfos.size(); i++) {
                                JSONObject photoid = new JSONObject();
                                photoid.put("photoId", photoInfos.get(i).getPhotoId());
                                embedPhotos.add(photoid);
                            }
                            PictureAirLog.v(TAG, embedPhotos.toString());
                            return API2.addToCart(goodsInfo.getGoodsKey(), 1, isbuynow, embedPhotos);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<JSONObject>bindToLifecycle())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        PictureAirLog.d("add to cart ---> " + jsonObject);
                        if (jsonObject.containsKey("cartItemIds")) {
                            //批量加入购物车成功
                            addCartSuccess(jsonObject.getJSONArray("cartItemIds"), true);
                        } else if (jsonObject.containsKey("cartId")) {
                            //加入购物车成功
                            addCartSuccess(jsonObject, false);
                        } else {
                            _onError(401);
                        }
                    }

                    @Override
                    public void _onError(int status) {
                        dismissPWProgressDialog();
                        newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    /**
     * 处理购物车
     * @param result 返回结果
     * @param isBatch 是否是批量处理
     */
    private void addCartSuccess(Object result, boolean isBatch){
        dismissPWProgressDialog();
        PictureAirLog.out("result---->" + result);
        JSONArray addcartArray;

        if (isBatch) {
            addcartArray = (JSONArray) result;
        } else {
            JSONObject addcart = (JSONObject) result;
            addcartArray = new JSONArray();
            addcartArray.add(addcart.getString("cartId"));
        }

        int currentCartCount = SPUtils.getInt(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
        SPUtils.put(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, currentCartCount + addcartArray.size());

        if (isbuynow) {//获取订单信息，传送到下一界面
            Intent intent = new Intent(PreviewProductActivity.this, SubmitOrderActivity.class);
            ArrayList<CartItemInfo> orderinfoArrayList = new ArrayList<>();

            for (int i = 0; i < addcartArray.size(); i++) {
                CartItemInfo cartItemInfo = new CartItemInfo();
                cartItemInfo.setProductName(goodsInfo.getName());
                cartItemInfo.setProductNameAlias(goodsInfo.getNameAlias());
                cartItemInfo.setEntityType(goodsInfo.getEntityType());
                cartItemInfo.setUnitPrice(goodsInfo.getPrice());
                cartItemInfo.setPrice(goodsInfo.getPrice() * 1);
                cartItemInfo.setCartProductType(1);
                cartItemInfo.setDescription(goodsInfo.getDescription());
                cartItemInfo.setQty(1);
                cartItemInfo.setStoreId(goodsInfo.getStoreId());
                cartItemInfo.setGoodsKey(goodsInfo.getGoodsKey());
                cartItemInfo.setCartId(addcartArray.getString(i));//会返回此数据
                if (goodsInfo.getPictures() != null && goodsInfo.getPictures().size() > 0) {
                    String[] cartProductImageUrl = new String[goodsInfo.getPictures().size()];
                    for (int j = 0; j < goodsInfo.getPictures().size(); j++) {
                        cartProductImageUrl[j] = goodsInfo.getPictures().get(j).getUrl();
                        PictureAirLog.out("imageurl--->" + goodsInfo.getPictures().get(j).getUrl());
                    }
                    cartItemInfo.setPictures(cartProductImageUrl);
                }
                CartPhotosInfo cartPhotosInfo;
                ArrayList<CartPhotosInfo> listAfterUploaded = new ArrayList<>();
                if (isBatch) {
                    cartPhotosInfo = new CartPhotosInfo();
                    if (i < list.size()) {
                        cartPhotosInfo.setPhotoUrl(list.get(i).getPhotoOriginalURL());
                        cartPhotosInfo.setPhotoId(list.get(i).getPhotoId());
                        cartPhotosInfo.setIsEncrypted(list.get(i).getIsEnImage());
                    }
                    listAfterUploaded.add(cartPhotosInfo);
                } else {
                    for (int j = 0; j < list.size(); j++) {
                        cartPhotosInfo = new CartPhotosInfo();
                        cartPhotosInfo.setPhotoUrl(list.get(j).getPhotoOriginalURL());
                        cartPhotosInfo.setPhotoId(list.get(j).getPhotoId());
                        cartPhotosInfo.setIsEncrypted(list.get(j).getIsEnImage());
                        listAfterUploaded.add(cartPhotosInfo);
                    }
                }
                cartItemInfo.setEmbedPhotos(listAfterUploaded);
                orderinfoArrayList.add(cartItemInfo);
            }

            intent.putExtra("orderinfo", orderinfoArrayList);
            intent.putExtra("activity", "previewproduct");
            PreviewProductActivity.this.startActivity(intent);
        } else {
            buyImg = new ImageView(PreviewProductActivity.this);// buyImg是动画的图片
            buyImg.setImageResource(R.drawable.addtocart);// 设置buyImg的图片
            setAnim(buyImg);// 开始执行动画
        }
    }

    //添加试图到动画图层
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

    //创建动画图层
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

    private void setAnim(final View v) {
        anim_mask_layout = null;
        anim_mask_layout = createAnimLayout();
        int[] start_location = new int[2];// 一个整型数组，用来存储按钮的在屏幕的X、Y坐标
        start_location[0] = ScreenUtil.getScreenWidth(PreviewProductActivity.this) / 2 - Common.CART_WIDTH;//减去的值和图片大小有关系
        start_location[1] = ScreenUtil.getScreenHeight(PreviewProductActivity.this) / 2 - Common.CART_HEIGHT;
        final View view = addViewToAnimLayout(anim_mask_layout, v,
                start_location);
        int[] end_location = new int[2];// 这是用来存储动画结束位置的X、Y坐标
        counTextView.getLocationInWindow(end_location);// shopCart是那个购物车
        // 计算位移
        final int endX = end_location[0] - start_location[0];// 动画位移的X坐标
        final int endY = end_location[1] - start_location[1];// 动画位移的y坐标
        //设置放大动画
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setInterpolator(new LinearInterpolator());//匀速
        scaleAnimation.setRepeatCount(0);//不重复
        scaleAnimation.setFillAfter(true);//停在最后动画

        AnimationSet set = new AnimationSet(false);
        set.setFillAfter(false);
        set.addAnimation(scaleAnimation);
        set.setDuration(500);//动画整个时间
        view.startAnimation(set);//开始动画
        // 动画监听事件
        set.setAnimationListener(new AnimationListener() {
            // 动画的开始
            @Override
            public void onAnimationStart(Animation animation) {
                //					v.setVisibility(View.VISIBLE);
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
                        int i = SPUtils.getInt(PreviewProductActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
                        if (i <= 0) {
                            counTextView.setVisibility(View.INVISIBLE);
                        } else {
                            counTextView.setVisibility(View.VISIBLE);
                            counTextView.setText(i + "");
                        }
                    }
                });
            }
        });

    }
}