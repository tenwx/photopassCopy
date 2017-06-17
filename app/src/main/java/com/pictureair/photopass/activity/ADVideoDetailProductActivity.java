package com.pictureair.photopass.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
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
    private TextView cartCountTextView, buyTV;
    private RelativeLayout buyPPPRL, usePPPRL, useDailyPPPRL, buyPhotoRL;
    private TextView buyPPPNameTV, buyPPPIntroTV, buyPPPPriceTV;

    private PWToast pwToast;
    private BottomSheetDialog sheetDialog;
    private BottomSheetBehavior bottomSheetBehavior;
    private View buyPhotoRootView;

    private PhotoInfo videoInfo;
    private String ppCode;
    private String tabName;
    private int currentPosition;//记录当前预览照片的索引值

    private final static String TAG = ADVideoDetailProductActivity.class.getSimpleName();

    //商品数据
    private List<GoodsInfo> allGoodsList;//全部商品
    private GoodsInfo pppGoodsInfo;
    private String[] photoUrls;

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
        buyTV = (TextView) findViewById(R.id.animated_photo_buy_btn);

        sheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogStyle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//解决弹出对话框之后，状态栏的沉浸式效果消失了
            sheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        backImageView.setOnClickListener(this);
        cartImageView.setOnClickListener(this);
        cartCountTextView.setOnClickListener(this);
        buyTV.setOnClickListener(this);
    }

    private void initData() {
        pwToast = new PWToast(this);
        videoInfo = (PhotoInfo) getIntent().getExtras().get("videoInfo");
        Bundle bundle = getIntent().getBundleExtra("bundle");
        currentPosition = bundle.getInt("position", 0);
        tabName = bundle.getString("tab");
        ppCode = bundle.getString("ppCode");

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
                        String goodsByACache = ACache.get(ADVideoDetailProductActivity.this).getAsString(Common.ALL_GOODS);
                        PictureAirLog.v(TAG, "initData: goodsByACache: " + goodsByACache);
                        if (!TextUtils.isEmpty(goodsByACache)) {
                            return Observable.just(JSONObject.parseObject(goodsByACache));
                        } else {
                            if (integer != 0) {
                                return API2.getGoods()
                                        .map(new Func1<JSONObject, JSONObject>() {
                                            @Override
                                            public JSONObject call(JSONObject jsonObject) {
                                                ACache.get(MyApplication.getInstance()).put(Common.ALL_GOODS, jsonObject.toString(), ACache.TIME_DAY);
                                                return jsonObject;
                                            }
                                        });
                            } else {
                                return Observable.error(new ServerException(401));
                            }
                        }
                    }
                })
                .map(new Func1<JSONObject, JSONObject>() {
                    @Override
                    public JSONObject call(JSONObject jsonObject) {
                        GoodsInfoJson goodsInfoJson = JsonTools.parseObject(jsonObject, GoodsInfoJson.class);//GoodsInfoJson.getString()
                        if (goodsInfoJson != null && goodsInfoJson.getGoods().size() > 0) {
                            allGoodsList = goodsInfoJson.getGoods();
                        } else {
                            allGoodsList = new ArrayList<>();
                        }
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

            case R.id.preview_blur_dialog_buy_ppp_ll://购买ppp
                if (AppUtil.getNetWorkType(MyApplication.getInstance()) == AppUtil.NETWORKTYPE_INVALID || pppGoodsInfo == null) {
                    pwToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
                    return;
                }
                if (sheetDialog.isShowing()) {
                    sheetDialog.dismiss();
                }
                //购买按钮，需要将当前商品的类型和单价存储起来
                addtocart();
                break;

            case R.id.preview_blur_dialog_upgrade_photo_ll://使用已存在的ppp升级
                if (AppUtil.getNetWorkType(MyApplication.getInstance()) == AppUtil.NETWORKTYPE_INVALID) { //判断网络情况。
                    pwToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                }else{
                    if (sheetDialog.isShowing()) {
                        sheetDialog.dismiss();
                    }
                    showPWProgressDialog();
                    getPPPsByShootDate(videoInfo.getShootDate(), false);
                }
                break;

            case R.id.preview_blur_dialog_upgrade_daily_photo_ll://使用已存在的daily ppp升级
                if (AppUtil.getNetWorkType(MyApplication.getInstance()) == AppUtil.NETWORKTYPE_INVALID) { //判断网络情况。
                    pwToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                }else{
                    if (sheetDialog.isShowing()) {
                        sheetDialog.dismiss();
                    }
                    showPWProgressDialog();
                    getPPPsByShootDate(videoInfo.getShootDate(), true);
                }
                break;

            case R.id.animated_photo_buy_btn:
                showSheetDialog();
                break;

            default:
                break;
        }
    }

    /**
     * 显示dialog
     */
    private void showSheetDialog() {
        if (buyPhotoRootView == null) {
            buyPhotoRootView = LayoutInflater.from(this).inflate(R.layout.dialog_preview_buy_blur, null);
            buyPPPRL = (RelativeLayout) buyPhotoRootView.findViewById(R.id.preview_blur_dialog_buy_ppp_ll);
            buyPPPNameTV = (TextView) buyPhotoRootView.findViewById(R.id.preview_blur_dialog_buy_ppp_tv);
            buyPPPIntroTV = (TextView) buyPhotoRootView.findViewById(R.id.preview_blur_dialog_buy_ppp_intro_tv);
            buyPPPPriceTV = (TextView) buyPhotoRootView.findViewById(R.id.preview_blur_dialog_buy_ppp_price_tv);
            usePPPRL = (RelativeLayout) buyPhotoRootView.findViewById(R.id.preview_blur_dialog_upgrade_photo_ll);
            useDailyPPPRL = (RelativeLayout) buyPhotoRootView.findViewById(R.id.preview_blur_dialog_upgrade_daily_photo_ll);
            buyPhotoRL = (RelativeLayout) buyPhotoRootView.findViewById(R.id.preview_blur_dialog_buy_photo_ll);
            buyPhotoRL.setVisibility(View.GONE);
            buyPPPRL.setOnClickListener(this);
            usePPPRL.setOnClickListener(this);
            useDailyPPPRL.setOnClickListener(this);
        } else {//需要把view的父控件的子view全部清除，此处为什么是FrameLayout，是从源码得知
            ((FrameLayout) buyPhotoRootView.getParent()).removeAllViews();
        }
        sheetDialog.setContentView(buyPhotoRootView);
        initSheetDialog();

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
        if (allGoodsList != null) {
            for (GoodsInfo good : allGoodsList) {
                if (good != null && good.getName().equals(Common.GOOD_NAME_PPP)) {//ppp
                    pppGoodsInfo = good;
                    buyPPPNameTV.setText(good.getNameAlias());
                    PictureAirLog.d("----> " + good.getPrice());
                    buyPPPPriceTV.setText(Common.DEFAULT_CURRENCY + good.getPrice());
                    buyPPPIntroTV.setText(good.getDescription());
                    break;
                }
            }
        }
    }

    private void getPPPsByShootDate(String shootDate, final boolean isDaily) {
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

                            JSONArray pps = new JSONArray();
                            JSONObject ppJB = new JSONObject();
                            ppJB.put("code", ppCode);
                            ppJB.put("bindDate", videoInfo.getShootDate());
                            pps.add(ppJB);

                            Intent intent = new Intent(ADVideoDetailProductActivity.this, MyPPPActivity.class);
                            intent.putExtra("ppsStr", pps.toString());
                            intent.putExtra("isUseHavedPPP", true);
                            intent.putExtra("dailyppp", isDaily);
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
        API2.addToCart(pppGoodsInfo.getGoodsKey(), 1, true, null)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<JSONObject>bindToLifecycle())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        int currentCartCount = SPUtils.getInt(ADVideoDetailProductActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
                        SPUtils.put(ADVideoDetailProductActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, currentCartCount + 1);

                        String cartId = jsonObject.getString("cartId");
                        dismissPWProgressDialog();
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

}
