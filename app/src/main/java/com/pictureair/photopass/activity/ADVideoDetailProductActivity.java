package com.pictureair.photopass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.entity.CartPhotosInfo;
import com.pictureair.photopass.entity.GoodsInfo;
import com.pictureair.photopass.entity.GoodsInfoJson;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.http.rxhttp.RxSubscribe;
import com.pictureair.photopass.http.rxhttp.ServerException;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.API2;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.JsonTools;
import com.pictureair.photopass.util.JsonUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.PWToast;
import com.trello.rxlifecycle.android.ActivityEvent;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by bauer_bao on 16/9/2.
 */
public class ADVideoDetailProductActivity extends BaseActivity implements View.OnClickListener {

    private ImageView backImageView;
    private ImageView cartImageView;
    private ImageView adImageView;
    private TextView cartCountTextView;
    private Button buyPPPBtn, upgradePPP, addPPPToCart;

    private PWToast pwToast;

    private PhotoInfo videoInfo;
    private String tabName;
    private int currentPosition;//记录当前预览照片的索引值

    //加入购物车组件
    private ViewGroup animMaskLayout;//动画层
    private ImageView buyImg;// 这是在界面上跑的小图片

    private final static String TAG = ADVideoDetailProductActivity.class.getSimpleName();

    //商品数据
    private List<GoodsInfo> allGoodsList;//全部商品
    private GoodsInfo pppGoodsInfo;
    private String[] photoUrls;
    private boolean isBuyNow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advideo_detail_product);

        initView();
        initData();
    }

    private void initView() {
        backImageView = (ImageView) findViewById(R.id.rt);
        cartImageView = (ImageView) findViewById(R.id.button_bag);
        cartCountTextView = (TextView) findViewById(R.id.textview_cart_count);
        buyPPPBtn = (Button) findViewById(R.id.animated_photo_buy_ppp_btn);
        upgradePPP = (Button) findViewById(R.id.animated_photo_upgrade_ppp_btn);
        addPPPToCart = (Button) findViewById(R.id.animated_photo_add_cart_btn);
        adImageView = (ImageView) findViewById(R.id.animated_photo_iv);

        backImageView.setOnClickListener(this);
        cartImageView.setOnClickListener(this);
        cartCountTextView.setOnClickListener(this);
        buyPPPBtn.setOnClickListener(this);
        upgradePPP.setOnClickListener(this);
        addPPPToCart.setOnClickListener(this);
    }

    private void initData() {
        pwToast = new PWToast(this);
        videoInfo = (PhotoInfo) getIntent().getExtras().get("videoInfo");
        Bundle bundle = getIntent().getBundleExtra("bundle");
        currentPosition = bundle.getInt("position", 0);
        tabName = bundle.getString("tab");

        if (MyApplication.getInstance().getLanguageType().equals(Common.ENGLISH)) {
            adImageView.setImageResource(R.drawable.animated_ad_en);
        } else {
            adImageView.setImageResource(R.drawable.animated_ad_zh);
        }

        showPWProgressDialog(true);
        getGoods();
    }

    private void getGoods() {
        //从网络获取商品,先检查网络
        Observable.just(AppUtil.getNetWorkType(MyApplication.getInstance()))
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<Integer, Observable<JSONObject>>() {
                    @Override
                    public Observable<JSONObject> call(Integer integer) {
                        if (integer != 0) {
                            String goodsByACache = ACache.get(ADVideoDetailProductActivity.this).getAsString(Common.ALL_GOODS);
                            PictureAirLog.v(TAG, "initData: goodsByACache: " + goodsByACache);
                            if (!TextUtils.isEmpty(goodsByACache)) {
                                return Observable.just(JSONObject.parseObject(goodsByACache));
                            } else {
                                return API2.getGoods()
                                        .map(new Func1<JSONObject, JSONObject>() {
                                            @Override
                                            public JSONObject call(JSONObject jsonObject) {
                                                ACache.get(MyApplication.getInstance()).put(Common.ALL_GOODS, jsonObject.toString(), ACache.TIME_DAY);
                                                return jsonObject;
                                            }
                                        });
                            }
                        } else {
                            return Observable.error(new ServerException(401));
                        }
                    }
                })
                .map(new Func1<JSONObject, JSONObject>() {
                    @Override
                    public JSONObject call(JSONObject jsonObject) {
                        GoodsInfoJson goodsInfoJson = JsonTools.parseObject(jsonObject, GoodsInfoJson.class);//GoodsInfoJson.getString()
                        if (goodsInfoJson != null && goodsInfoJson.getGoods().size() > 0) {
                            allGoodsList = goodsInfoJson.getGoods();
                            PictureAirLog.v(TAG, "goods size: " + allGoodsList.size());
                        }
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
                        return jsonObject;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<JSONObject>bindToLifecycle())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        dismissPWProgressDialog();
                    }

                    @Override
                    public void _onError(int status) {
                        dismissPWProgressDialog();
                        pwToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rt://返回按钮
                finish();
                break;

            case R.id.button_bag://购物车按钮
            case R.id.textview_cart_count:
                if (AppUtil.getNetWorkType(MyApplication.getInstance()) == AppUtil.NETWORKTYPE_INVALID) {
                    pwToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
                    return;
                }
                Intent intent = new Intent(ADVideoDetailProductActivity.this, CartActivity.class);
                startActivity(intent);
                break;

            case R.id.animated_photo_buy_ppp_btn://购买ppp
                if (AppUtil.getNetWorkType(MyApplication.getInstance()) == AppUtil.NETWORKTYPE_INVALID || pppGoodsInfo == null) {
                    pwToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
                    return;
                }
                //购买按钮，需要将当前商品的类型和单价存储起来
                isBuyNow = true;//立即购买
                addtocart();
                break;

            case R.id.animated_photo_upgrade_ppp_btn://使用已存在的ppp升级
                if (AppUtil.getNetWorkType(MyApplication.getInstance()) == AppUtil.NETWORKTYPE_INVALID) { //判断网络情况。
                    pwToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    return;
                }else{
                    showPWProgressDialog();
                    getPPPsByShootDate(videoInfo.getShootDate());
                }
                break;

            case R.id.animated_photo_add_cart_btn://把ppp加入购物车
                if (AppUtil.getNetWorkType(MyApplication.getInstance()) == AppUtil.NETWORKTYPE_INVALID || pppGoodsInfo == null) {
                    pwToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
                    return;
                }
                //加入购物车，会有动画效果,如果没有登录，先提示登录
                isBuyNow = false;
                addtocart();
                break;

            default:
                break;
        }
    }

    private void getPPPsByShootDate(String shootDate) {
        API2.getPPPsByShootDate(shootDate)
                .compose(this.<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        API2.PPPlist = JsonUtil.getPPPSByUserIdNHavedPPP(jsonObject);
                        dismissPWProgressDialog();
                        if (API2.PPPlist.size() > 0) {
                            //将 tabname 存入sp
                            SPUtils.put(ADVideoDetailProductActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, "tabName", tabName);
                            SPUtils.put(ADVideoDetailProductActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, "currentPosition", currentPosition);

                            Intent intent = new Intent(ADVideoDetailProductActivity.this, SelectPPActivity.class);
                            intent.putExtra("photoPassCode", videoInfo.getPhotoPassCode());
                            intent.putExtra("shootTime", videoInfo.getShootDate());
                            startActivity(intent);
                        } else {
                            pwToast.setTextAndShow(R.string.no_ppp_tips, Common.TOAST_SHORT_TIME);
                        }
                    }

                    @Override
                    public void _onError(int status) {
                        dismissPWProgressDialog();
                        pwToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartCount();
    }

    @Override
    protected void onDestroy() {
        if (!MyApplication.getInstance().getBuyPPPStatus().equals(Common.FROM_AD_ACTIVITY_PAYED)) {//如果已经购买完成，则不需要清除数据，否则才会清除
            MyApplication.getInstance().setBuyPPPStatus("");
            //按返回，把状态全部清除
            MyApplication.getInstance().clearIsBuyingPhotoList();
        }
        super.onDestroy();
    }

    /**
     * 更新购物车数量
     */
    private void updateCartCount() {
        // TODO Auto-generated method stub
        int recordcount = SPUtils.getInt(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
        if (recordcount <= 0) {
            cartCountTextView.setVisibility(View.INVISIBLE);
        } else {
            cartCountTextView.setVisibility(View.VISIBLE);
            cartCountTextView.setText(recordcount + "");
        }
    }

    /**
     * 添加购物车
     */
    private void addtocart() {
        showPWProgressDialog();
        //调用addToCart API1
        API2.addToCart(pppGoodsInfo.getGoodsKey(), 1, isBuyNow, null)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<JSONObject>bindToLifecycle())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        int currentCartCount = SPUtils.getInt(ADVideoDetailProductActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
                        SPUtils.put(ADVideoDetailProductActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, currentCartCount + 1);

                        String cartId = jsonObject.getString("cartId");
                        dismissPWProgressDialog();
                        if (isBuyNow) {
                            MyApplication.getInstance().setIsBuyingPhotoInfo(null, null, videoInfo.getPhotoPassCode(), videoInfo.getShootDate());
                            MyApplication.getInstance().setBuyPPPStatus(Common.FROM_AD_ACTIVITY);

                            //生成订单
                            Intent intent = new Intent(ADVideoDetailProductActivity.this, SubmitOrderActivity.class);
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
                            intent.putExtra("orderinfo", orderinfoArrayList);
                            startActivity(intent);
                        } else {
                            buyImg = new ImageView(ADVideoDetailProductActivity.this);// buyImg是动画的图片
                            buyImg.setImageResource(R.drawable.addtocart);// 设置buyImg的图片
                            setAnim(buyImg);
                        }
                    }

                    @Override
                    public void _onError(int status) {
                        dismissPWProgressDialog();
                        pwToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    /**
     * 设置添加购物车动画
     *
     * @param v
     */
    private void setAnim(final View v) {
        animMaskLayout = null;
        animMaskLayout = createAnimLayout();
        int[] start_location = new int[2];// 一个整型数组，用来存储按钮的在屏幕的X、Y坐标
        start_location[0] = ScreenUtil.getScreenWidth(this) / 2 - Common.CART_WIDTH;//减去的值和图片大小有关系
        start_location[1] = ScreenUtil.getScreenHeight(this) / 2 - Common.CART_HEIGHT;
        // 将组件添加到我们的动画层上
        final View view = addViewToAnimLayout(animMaskLayout, v, start_location);
        int[] end_location = new int[2];
        cartCountTextView.getLocationInWindow(end_location);
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
        set.setAnimationListener(new Animation.AnimationListener() {
            // 动画的开始
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            // 动画的结束
            @Override
            public void onAnimationEnd(Animation animation) {
                //x轴的路径动画，匀速
                TranslateAnimation translateAnimationX = new TranslateAnimation(0, endX, 0, 0);
                translateAnimationX.setInterpolator(new LinearInterpolator());
                translateAnimationX.setRepeatCount(0);// 动画重复执行的次数
                //y轴的路径动画，加速
                TranslateAnimation translateAnimationY = new TranslateAnimation(0, 0, 0, endY);
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
                set2.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        v.setVisibility(View.GONE);//控件消失
                        int i = SPUtils.getInt(ADVideoDetailProductActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
                        if (i <= 0) {
                            cartCountTextView.setVisibility(View.INVISIBLE);
                        } else {
                            cartCountTextView.setVisibility(View.VISIBLE);
                            cartCountTextView.setText(i + "");
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
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER;

        animLayout.setLayoutParams(lp);
        animLayout.setBackgroundResource(android.R.color.transparent);
        rootView.addView(animLayout);
        return animLayout;
    }

    private View addViewToAnimLayout(ViewGroup vg, View view, int[] location) {
        int x = location[0];
        int y = location[1];
        vg.addView(view);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = x;
        lp.topMargin = y;
        view.setLayoutParams(lp);
        return view;
    }
}
