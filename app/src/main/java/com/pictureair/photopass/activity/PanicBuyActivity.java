package com.pictureair.photopass.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.entity.CartPhotosInfo;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.widget.CustomFontManager;
import com.pictureair.photopass.widget.CustomTextView;
import com.pictureair.photopass.widget.NoNetWorkOrNoCountView;

import java.util.ArrayList;

/**
 * Created by pengwu on 16/9/28.
 * 抢购窗体
 */
public class PanicBuyActivity extends BaseActivity implements View.OnClickListener{

    private ImageView previewImage;
    private TextView tv_discount;
    private CustomTextView tv_price;
    private TextView tv_leftCount;
    private Button btn_purchase;
    private TextView tv_time;
    private TextView tv_detail;
    private ImageView img_back;
    private NoNetWorkOrNoCountView noNetWorkOrNoCountView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panic_buy);
        initView();
        initData();
    }

    private void initView() {
        previewImage = (ImageView) findViewById(R.id.panicbuy_image);
        tv_discount = (TextView) findViewById(R.id.panicbuy_dicsount);
        tv_price = (CustomTextView) findViewById(R.id.panicbuy_price);
        tv_leftCount = (TextView) findViewById(R.id.panicbuy_left);
        btn_purchase = (Button) findViewById(R.id.panicbuy_purchase);
        tv_time = (TextView) findViewById(R.id.panicbuy_time);
        tv_detail = (TextView) findViewById(R.id.panicbuy_detail);
        img_back = (ImageView) findViewById(R.id.panicbuy_back);
        noNetWorkOrNoCountView = (NoNetWorkOrNoCountView) findViewById(R.id.panicbuy_NoNetWorkView);
        tv_price.setTypeface(MyApplication.getInstance().getFontBold());
    }

    private void initData() {
        tv_price.setText("￥169");
        String str = String.format(getString(R.string.panic_buy_count_limit),99);
        Spannable spannable = changeTextSizeAndColor(str, 2, 2+"99".length(), 1.5f, "#ff4605");
        tv_leftCount.setText(spannable);
        tv_discount.setText("3.5折【限量抢购，仅限100套】迪士尼乐拍通一卡通");
        tv_time.setText("距离本次开始时间2:00");
        tv_detail.setText("阿拉山口解放啦是否看见爱离开家参加了家里就挖了就发；放假；垃圾睡房垃圾发了；卡了家里；家里；加；老司机发了；数据敷面膜。杀了房间爱拉进来；按时间发；离开房间爱了；上课就发了；刷卡费劲啊拉科技发爱离开房间了；咖啡机案例；咖啡机阿兰卡；司法局啦；咖啡机啊；是否看见爱立方科技");
        enableButton(true);
        btn_purchase.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.panicbuy_purchase:
                Intent intent = new Intent(PanicBuyActivity.this, SubmitOrderActivity.class);
                ArrayList<CartItemInfo> orderinfoArrayList = new ArrayList<>();
                CartItemInfo cartItemInfo = new CartItemInfo();
                cartItemInfo.setCartId("123456");
                cartItemInfo.setProductName("PP+一卡通");
                cartItemInfo.setProductNameAlias("PP+");
                cartItemInfo.setUnitPrice(169);
                cartItemInfo.setEmbedPhotos(new ArrayList<CartPhotosInfo>());
                cartItemInfo.setDescription("真好用啊PP+");
                cartItemInfo.setQty(1);
                cartItemInfo.setStoreId("12345678");
                cartItemInfo.setPictures(new String []{Common.PHOTO_URL});
                cartItemInfo.setPrice(169);
                cartItemInfo.setCartProductType(3);

                orderinfoArrayList.add(cartItemInfo);
                intent.putExtra("orderinfo", orderinfoArrayList);
                startActivity(intent);
                break;
            case R.id.panicbuy_back:
                goBack();
                break;
            default:
                break;
        }
    }

    private Spannable changeTextSizeAndColor(String str, int start, int end, float size,String color) {
        if (TextUtils.isEmpty(str)) return new SpannableString("");

        Spannable span = new SpannableString(str);
        span.setSpan(new RelativeSizeSpan(size), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new ForegroundColorSpan(Color.parseColor(color)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }

    private void enableButton(boolean enable) {
        if (enable) {
            btn_purchase.setEnabled(true);
            btn_purchase.setTextColor(Color.WHITE);
        } else {
            btn_purchase.setEnabled(false);
            btn_purchase.setTextColor(Color.parseColor("#8294aa"));
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
