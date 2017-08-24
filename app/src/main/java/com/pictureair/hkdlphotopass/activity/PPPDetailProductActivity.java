package com.pictureair.hkdlphotopass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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

import com.alibaba.fastjson.JSONObject;
import com.pictureair.hkdlphotopass.MyApplication;
import com.pictureair.hkdlphotopass.R;
import com.pictureair.hkdlphotopass.entity.CartItemInfo;
import com.pictureair.hkdlphotopass.entity.CartPhotosInfo;
import com.pictureair.hkdlphotopass.entity.GoodInfoPictures;
import com.pictureair.hkdlphotopass.entity.GoodsInfo;
import com.pictureair.hkdlphotopass.http.rxhttp.RxSubscribe;
import com.pictureair.hkdlphotopass.util.API2;
import com.pictureair.hkdlphotopass.util.AppUtil;
import com.pictureair.hkdlphotopass.util.Common;
import com.pictureair.hkdlphotopass.util.PictureAirLog;
import com.pictureair.hkdlphotopass.util.SPUtils;
import com.pictureair.hkdlphotopass.util.ScreenUtil;
import com.pictureair.hkdlphotopass.widget.BannerView_Detail;
import com.pictureair.hkdlphotopass.widget.PWToast;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;

/**
 * PP+商品明细类
 *
 * @author bauer_bao
 */
public class PPPDetailProductActivity extends BaseActivity implements OnClickListener {
    //申明控件
    private ViewGroup animMaskLayout;//动画层
    private ImageView buyImg;// 这是在界面上跑的小图片
    private TextView nameTextView;
    private TextView promotionPriceTextView;
    private TextView detailTextView;
    private TextView currencyTextView;
    private ImageView returnLayout;
    private ImageView cartImageView;
    private Button buyButton;
    private Button addToCartButton;
    private TextView cartCountTextView;
    private BannerView_Detail bannerViewDetail;
    private TextView shopAddressTextView;

    //申明变量
    private final static String TAG = "PPPDetailProductAct";
    private int recordCount = 0; //记录数据库中有几条记录
    private boolean isBuyNow = false;

    //申明其他类
    private GoodsInfo goodsInfo;
    private PWToast myToast;

    private String[] photoUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_product);
        //初始化控件
        returnLayout = (ImageView) findViewById(R.id.rt);
        cartImageView = (ImageView) findViewById(R.id.button_bag);
        cartCountTextView = (TextView) findViewById(R.id.textview_cart_count);
        nameTextView = (TextView) findViewById(R.id.detail_good_name);
        bannerViewDetail = (BannerView_Detail) findViewById(R.id.bannerview_product_detail);
        detailTextView = (TextView) findViewById(R.id.product_detail);
        currencyTextView = (TextView) findViewById(R.id.detail_currency);
        promotionPriceTextView = (TextView) findViewById(R.id.detail_promotion_price);
        shopAddressTextView = (TextView) findViewById(R.id.detail_receive_address);
        buyButton = (Button) findViewById(R.id.button_buy);
        addToCartButton = (Button) findViewById(R.id.button_cart);
        buyButton.setTypeface(MyApplication.getInstance().getFontBold());
        addToCartButton.setTypeface(MyApplication.getInstance().getFontBold());

        addToCartButton.setVisibility(View.VISIBLE);
        //绑定监听
        returnLayout.setOnClickListener(this);
        cartCountTextView.setOnClickListener(this);
        cartImageView.setOnClickListener(this);
        buyButton.setOnClickListener(this);
        addToCartButton.setOnClickListener(this);

        //初始数据
        myToast = new PWToast(this);
        //获取传过来的值
        goodsInfo = (GoodsInfo) getIntent().getSerializableExtra("goods");
        nameTextView.setText(goodsInfo.getNameAlias());

        promotionPriceTextView.setTypeface(MyApplication.getInstance().getFontBold());
        currencyTextView.setTypeface(MyApplication.getInstance().getFontBold());
        currencyTextView.setText(Common.DEFAULT_CURRENCY);//SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CURRENCY, Common.DEFAULT_CURRENCY)
        promotionPriceTextView.setText(goodsInfo.getPrice() + "");

        detailTextView.setText(goodsInfo.getDescription());

        shopAddressTextView.setText(getString(R.string.address_digital_goods));
        if (goodsInfo.getPictures() != null && goodsInfo.getPictures().size() > 0) {
            PictureAirLog.v(TAG, "goodsInfo name: " + goodsInfo.getName());
            List<GoodInfoPictures> goodInfoPicturesList = new ArrayList<>();
            goodInfoPicturesList.add(goodsInfo.getPictures().get(1));
            bannerViewDetail.findimagepath(goodInfoPicturesList);
            //封装购物车宣传图
            photoUrls = new String[goodsInfo.getPictures().size()];
            for (int i = 0; i < goodsInfo.getPictures().size(); i++) {
                photoUrls[i] = goodsInfo.getPictures().get(i).getUrl();
            }
        }
        buyButton.setText(R.string.buy_good);
    }


    @Override
    protected void onResume() {
        super.onResume();
        recordCount = SPUtils.getInt(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
        if (recordCount <= 0) {
            cartCountTextView.setVisibility(View.INVISIBLE);
        } else {
            cartCountTextView.setVisibility(View.VISIBLE);
            cartCountTextView.setText(recordCount + "");
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.textview_cart_count:
            case R.id.button_bag:
                if (AppUtil.getNetWorkType(MyApplication.getInstance()) == 0) {
                    myToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
                    return;
                }
                intent = new Intent(this, CartActivity.class);
                startActivity(intent);
                break;

            case R.id.button_buy:
                if (AppUtil.getNetWorkType(MyApplication.getInstance()) == 0) {
                    myToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
                    return;
                }
                //购买按钮，需要将当前商品的类型和单价存储起来
                isBuyNow = true;//立即购买
                addtocart();
                break;

            case R.id.button_cart:
                if (AppUtil.getNetWorkType(MyApplication.getInstance()) == 0) {
                    myToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
                    return;
                }
                //加入购物车，会有动画效果,如果没有登录，先提示登录
                isBuyNow = false;
                addtocart();
                break;

            case R.id.rt:
                finish();
                break;

            default:
                break;
        }
    }

    /**
     * 添加购物车
     */
    private void addtocart() {
        showPWProgressDialog();
        //调用addToCart API1
        API2.addToCart(goodsInfo.getGoodsKey(), 1, isBuyNow, null)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<JSONObject>bindToLifecycle())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        int currentCartCount = SPUtils.getInt(PPPDetailProductActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
                        SPUtils.put(PPPDetailProductActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, currentCartCount + 1);

                        String cartId = jsonObject.getString("cartId");
                        dismissPWProgressDialog();
                        if (isBuyNow) {
                            //生成订单
                            Intent intent = new Intent(PPPDetailProductActivity.this, SubmitOrderActivity.class);
                            ArrayList<CartItemInfo> orderinfoArrayList = new ArrayList<>();
                            CartItemInfo cartItemInfo = new CartItemInfo();
                            cartItemInfo.setCartId(cartId);
                            cartItemInfo.setProductName(goodsInfo.getName());
                            cartItemInfo.setProductNameAlias(goodsInfo.getNameAlias());
                            cartItemInfo.setUnitPrice(goodsInfo.getPrice());
                            cartItemInfo.setEmbedPhotos(new ArrayList<CartPhotosInfo>());
                            cartItemInfo.setDescription(goodsInfo.getDescription());
                            cartItemInfo.setQty(1);
                            cartItemInfo.setStoreId(goodsInfo.getStoreId());
                            cartItemInfo.setPictures(photoUrls);
                            cartItemInfo.setPrice(goodsInfo.getPrice());
                            cartItemInfo.setCartProductType(3);

                            orderinfoArrayList.add(cartItemInfo);
                            intent.putExtra("orderinfo", orderinfoArrayList);
                            startActivity(intent);
                        } else {
                            buyImg = new ImageView(PPPDetailProductActivity.this);// buyImg是动画的图片
                            buyImg.setImageResource(R.drawable.addtocart);// 设置buyImg的图片
                            setAnim(buyImg);
                        }
                    }

                    @Override
                    public void _onError(int status) {
                        dismissPWProgressDialog();
                        myToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
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
        start_location[0] = ScreenUtil.getScreenWidth(PPPDetailProductActivity.this) / 2 - Common.CART_WIDTH;//减去的值和图片大小有关系
        start_location[1] = ScreenUtil.getScreenHeight(PPPDetailProductActivity.this) / 2 - Common.CART_HEIGHT;
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
        set.setAnimationListener(new AnimationListener() {
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

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        v.setVisibility(View.GONE);//控件消失
                        int i = SPUtils.getInt(PPPDetailProductActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
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
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER;


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
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }
}
