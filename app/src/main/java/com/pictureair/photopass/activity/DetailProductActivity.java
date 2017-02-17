package com.pictureair.photopass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
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
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.GoodInfoPictures;
import com.pictureair.photopass.entity.GoodsInfo;
import com.pictureair.photopass.http.rxhttp.RxSubscribe;
import com.pictureair.photopass.util.API2;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.BannerView_Detail;
import com.pictureair.photopass.widget.PWToast;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;

/**
 * 商品明细类，此页面可以加入购物车
 *
 * @author bauer_bao
 */
public class DetailProductActivity extends BaseActivity implements OnClickListener {
    private final static String TAG = "DetailProductActivity";
    //申明控件
    private ViewGroup animMaskLayout;//动画层
    private ImageView buyImg;// 这是在界面上跑的小图片
    private TextView name, promotionPrice, currencyTextView, detail;
    private ImageView returnLayout;
    private ImageView cartImageView;
    private Button buyButton;
    private Button addtocartButton;
    private TextView cartCountTextView;
    private BannerView_Detail bannerView_Detail;
    private TextView receiveAdress;

    //申明实例类
    private GoodsInfo goodsInfo;
    private PWToast myToast;

    //申明变量
    private int recordcount = 0; //记录数据库中有几条记录

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_product);
        initView();
    }

    private void initView() {
        //find控件
        returnLayout = (ImageView) findViewById(R.id.rt);
        cartImageView = (ImageView) findViewById(R.id.button_bag);
        cartCountTextView = (TextView) findViewById(R.id.textview_cart_count);
        buyButton = (Button) findViewById(R.id.button_buy);
        addtocartButton = (Button) findViewById(R.id.button_cart);
        bannerView_Detail = (BannerView_Detail) findViewById(R.id.bannerview_product_detail);
        name = (TextView) findViewById(R.id.detail_good_name);
        detail = (TextView) findViewById(R.id.product_detail);
        promotionPrice = (TextView) findViewById(R.id.detail_promotion_price);
        currencyTextView = (TextView) findViewById(R.id.detail_currency);
        receiveAdress = (TextView) findViewById(R.id.detail_receive_address);

        buyButton.setTypeface(MyApplication.getInstance().getFontBold());
        addtocartButton.setTypeface(MyApplication.getInstance().getFontBold());

        //绑定监听
        returnLayout.setOnClickListener(this);
        cartCountTextView.setOnClickListener(this);
        cartImageView.setOnClickListener(this);
        buyButton.setOnClickListener(this);
        addtocartButton.setOnClickListener(this);

        //初始化数据
        myToast = new PWToast(this);

        goodsInfo = (GoodsInfo) getIntent().getSerializableExtra("goods");
        if (null != goodsInfo) {
            name.setText(goodsInfo.getNameAlias());
            detail.setText(goodsInfo.getDescription());
        }
        promotionPrice.setTypeface(MyApplication.getInstance().getFontBold());
        currencyTextView.setTypeface(MyApplication.getInstance().getFontBold());
        currencyTextView.setText(Common.DEFAULT_CURRENCY);//SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CURRENCY, Common.DEFAULT_CURRENCY)
        if (null != goodsInfo) {
            promotionPrice.setText(goodsInfo.getPrice() + "");
        }
        //实体商品-自提，数码商品-数码下载
        if (goodsInfo == null) {
            return;
        }
        //根据商品类型判，断是否有自提地址
        if (goodsInfo.getEntityType() == 0) {
            receiveAdress.setText(getString(R.string.address_digital_goods));
        } else {
            receiveAdress.setText(getString(R.string.self_collect));
        }
        if (goodsInfo.getPictures() != null && goodsInfo.getPictures().size() > 0) {
            PictureAirLog.v(TAG, "goodsInfo picture size" + goodsInfo.getPictures().size());
            PictureAirLog.v(TAG, "goodsInfo picture size" + goodsInfo.getPictures().get(0).getUrl());
            PictureAirLog.v(TAG, "goodsInfo picture size" + goodsInfo.getPictures().get(1).getUrl());
            List<GoodInfoPictures> goodInfoPicturesList = new ArrayList<>();
            goodInfoPicturesList.add(goodsInfo.getPictures().get(1));
            bannerView_Detail.findimagepath(goodInfoPicturesList);
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
                intent = new Intent(DetailProductActivity.this, CartActivity.class);
                DetailProductActivity.this.startActivity(intent);
                break;
            case R.id.button_buy:
                //选择照片
                intent = new Intent(DetailProductActivity.this, SelectPhotoActivity.class);
                intent.putExtra("activity", "detailproductactivity");
                intent.putExtra("goodsInfo", goodsInfo);
                PictureAirLog.out("size---->" + goodsInfo.getPictures().size());
                PictureAirLog.out("size---->" + goodsInfo.getPictures().get(0).getUrl());
                startActivity(intent);
                break;

            case R.id.button_cart://加入购物车，会有动画效果
                if (AppUtil.getNetWorkType(MyApplication.getInstance()) == 0) {
                    myToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
                    return;
                }
                //判断tokenId是否为空
                if (MyApplication.getTokenId().isEmpty()) {
                    intent = new Intent(DetailProductActivity.this, LoginActivity.class);
                    intent.putExtra("activity", "detailproductactivity");
                    DetailProductActivity.this.startActivity(intent);
                } else {
                    addtocart();
                }
                break;

            case R.id.rt:
                goBack();
                break;

            default:
                break;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 更新购物车数量
     */
    private void updateCartCount() {
        // TODO Auto-generated method stub
        recordcount = SPUtils.getInt(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
        if (recordcount <= 0) {
            cartCountTextView.setVisibility(View.INVISIBLE);
        } else {
            cartCountTextView.setVisibility(View.VISIBLE);
            cartCountTextView.setText(recordcount + "");
        }
    }

    //返回操作
    private void goBack() {
        if (AppManager.getInstance().checkActivity(MainTabActivity.class)) {//说明有这个界面
            finish();
        } else {//没有这个页面
            Intent intent = new Intent(this, MainTabActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        updateCartCount();
    }

    /**
     * 添加购物车
     */
    private void addtocart() {
        //编辑传入照片的信息
        API2.addToCart(goodsInfo.getGoodsKey(), 1, false, null)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<JSONObject>bindToLifecycle())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        int currentCartCount = SPUtils.getInt(DetailProductActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
                        SPUtils.put(DetailProductActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, currentCartCount + 1);
                        buyImg = new ImageView(DetailProductActivity.this);// buyImg是动画的图片
                        buyImg.setImageResource(R.drawable.addtocart);// 设置buyImg的图片
                        setAnim(buyImg);
                    }

                    @Override
                    public void _onError(int status) {
                        myToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), status), Common.TOAST_SHORT_TIME);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    private void setAnim(final View v) {
        animMaskLayout = null;
        animMaskLayout = createAnimLayout();
        int[] start_location = new int[2];// 一个整型数组，用来存储按钮的在屏幕的X、Y坐标
        start_location[0] = ScreenUtil.getScreenWidth(DetailProductActivity.this) / 2 - Common.CART_WIDTH;//减去的值和图片大小有关系
        start_location[1] = ScreenUtil.getScreenHeight(DetailProductActivity.this) / 2 - Common.CART_HEIGHT;
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
                ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.1f, 1.0f, 0.1f,
                        Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
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
                        int i = SPUtils.getInt(DetailProductActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
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
}
