package com.pictureair.photopass.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
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
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.widget.CustomTextView;
import com.pictureair.photopass.widget.NoNetWorkOrNoCountView;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by pengwu on 16/9/28.
 * 抢购窗体
 */
public class PanicBuyActivity extends BaseActivity implements View.OnClickListener{

    private ImageView previewImage;
    private CustomTextView tv_price;
    private Button btn_purchase;
    private TextView tv_detail1;
    private TextView tv_detail2;
    private TextView tv_detail_title;
    private TextView tv_time_status;
    private TextView tv_hour;
    private TextView tv_min;
    private TextView tv_sec;

    private ImageView img_back;
    private NoNetWorkOrNoCountView noNetWorkOrNoCountView;
    private int mStartDeal = NO_DEALS;
    private MyCountDownTimer countDownTimer;
    private static final int DEAL_NOT_START = 0;
    private static final int DEALING = 1;
    private static final int NO_DEALS = 2;
    private static final String TAG = "PanicBuyActivity";
    private static final String TIME_ZERO = "00";
    private Date startDate;
    private Date endDate;
    private static final int COUNT_DOWN_TIME_FINISH = 1111;
    private String startTime ;
    private String endTime;
    private int price;
    private String detailTitleCh;
    private String detail1Ch;
    private String detail2Ch;
    private String detailTitleEn;
    private String detail1En;
    private String detail2En;
    private String currency;
    private String goodsKey;

    private final Handler panciBuyHandler = new PanicBuyHandler(this);

    private static class PanicBuyHandler extends Handler{
        private final WeakReference<PanicBuyActivity> mActivity;

        public PanicBuyHandler(PanicBuyActivity activity) {
            mActivity = new WeakReference<PanicBuyActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivity.get() == null) {
                return;
            }
            mActivity.get().dealHandler(msg);


        }
    }

    private void dealHandler(Message msg) {
        switch (msg.what) {

            case COUNT_DOWN_TIME_FINISH:
                if (mStartDeal == DEALING) {
                    Date currentDate = getCurrentDate();
                    if (endDate != null && currentDate != null && endDate.getTime() >= currentDate.getTime()) {
                        countDownTimer = new MyCountDownTimer(endDate.getTime() - currentDate.getTime(), 1000);
                        countDownTimer.start();
                        enableBuy();
                        showDealsDetails();
                    }
                } else if (mStartDeal == NO_DEALS) {
                    //活动结束的UI
                }
                break;

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panic_buy);
        initView();
        initData();
    }

    private void initView() {
        previewImage = (ImageView) findViewById(R.id.panicbuy_image);
        tv_price = (CustomTextView) findViewById(R.id.panicbuy_price);
        tv_price.setTypeface(MyApplication.getInstance().getFontBold());
        tv_hour = (TextView) findViewById(R.id.panicbuy_hour);
        tv_min = (TextView) findViewById(R.id.panicbuy_min);
        tv_sec = (TextView) findViewById(R.id.panicbuy_sec);
        tv_detail_title = (TextView) findViewById(R.id.panicbuy_detail_title);
        tv_detail1 = (TextView) findViewById(R.id.panicbuy_detail1);
        tv_detail2 = (TextView) findViewById(R.id.panicbuy_detail2);
        tv_time_status = (TextView) findViewById(R.id.panicbuy_time_status);
        btn_purchase = (Button) findViewById(R.id.panicbuy_purchase);
        img_back = (ImageView) findViewById(R.id.panicbuy_back);
        noNetWorkOrNoCountView = (NoNetWorkOrNoCountView) findViewById(R.id.panicbuy_NoNetWorkView);
        tv_price.setTypeface(MyApplication.getInstance().getFontBold());
        btn_purchase.setOnClickListener(this);
    }

    private void initData() {
        price = 169;
        goodsKey = "1234556778";
        currency = SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CURRENCY, Common.DEFAULT_CURRENCY);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/mm/dd HH:mm:ss", Locale.getDefault());
        detailTitleCh = "单拼迪士尼乐拍通";
        detail1Ch = "您即可获得绑定至同一张迪士尼乐拍通卡内的所有数码照片!";
        detail2Ch = "-获得绑定至同一天所使用的一张迪士尼乐拍通卡内的所有数码照片\n-无限次预览,下载和分享所有数码照片\n-下载上海迪士尼度假区官方数码纪念照";

        detailTitleEn = "Single Disney PhotoPass+";
        detail1En = "Unlock and download all your digital photos from ONE Disney PhotoPass card!";
        detail2En = "-Get all digital photos from one(1) Disney PhotoPass card used in a single day.\n -Enjoy unlimited digital photo downloads.\n -Download ShanghaiDisney Resort digital souvenir photos";

        startTime = "2016/10/18 9:55:00";
        endTime = "2016/10/18 13:56:00";

        try {
            startDate = simpleDateFormat.parse(startTime);
            endDate = simpleDateFormat.parse(endTime);
            Date currentData = getCurrentDate();

            if (startDate.getTime() > currentData.getTime()) {
                mStartDeal = DEAL_NOT_START;
            } else {
                if (currentData.getTime() <= endDate.getTime()) {
                    mStartDeal = DEALING;
                } else {
                    mStartDeal = NO_DEALS;
                }
            }
            if (mStartDeal == DEAL_NOT_START) {
                countDownTimer = new MyCountDownTimer(startDate.getTime() - currentData.getTime(), 1000);
                countDownTimer.start();
                disableBuy();
                showDealsDetails();

            } else if (mStartDeal == DEALING) {
                countDownTimer = new MyCountDownTimer(endDate.getTime() - currentData.getTime(), 1000);
                countDownTimer.start();
                enableBuy();
                showDealsDetails();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

    private void enableBuy() {
        btn_purchase.setEnabled(true);
        btn_purchase.setText(R.string.panic_buy_buy_now);
        tv_price.setText(currency + price);
        tv_time_status.setText(R.string.panic_buy_end_content);

    }

    private void disableBuy() {
        btn_purchase.setEnabled(false);
        btn_purchase.setText(R.string.panic_buy_begin_soon);
        tv_price.setText(currency + price);
        tv_time_status.setText(R.string.panic_buy_start_content);
    }

    private void showDealsDetails() {
        String language = MyApplication.getInstance().getLanguageType();
        if (Common.ENGLISH.equals(language)) {
            tv_detail_title.setText(detailTitleEn);
            tv_detail1.setText(detail1En);
            tv_detail2.setText(detail2En);
        } else if (Common.SIMPLE_CHINESE.equals(language)) {
            tv_detail_title.setText(detailTitleCh);
            tv_detail1.setText(detail1Ch);
            tv_detail2.setText(detail2Ch);
        }
    }

    private Date getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/mm/dd HH:mm:ss",Locale.getDefault());
        Date currentData = null;
        try {
            currentData = sdf.parse(sdf.format(new Date()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return currentData;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.panicbuy_purchase:
                Intent intent = new Intent(PanicBuyActivity.this, SubmitOrderActivity.class);
                ArrayList<CartItemInfo> orderinfoArrayList = new ArrayList<>();
                CartItemInfo cartItemInfo = new CartItemInfo();
                cartItemInfo.setProductName("PhotoPass+");
                cartItemInfo.setProductNameAlias("乐拍通一卡通");
                cartItemInfo.setEmbedPhotos(new ArrayList<CartPhotosInfo>());
                cartItemInfo.setUnitPrice(price);
                cartItemInfo.setQty(1);
                cartItemInfo.setPrice(price);
                cartItemInfo.setEntityType(0);
                cartItemInfo.setGoodsKey(goodsKey);
                orderinfoArrayList.add(cartItemInfo);
                intent.putExtra("orderinfo", orderinfoArrayList);
                intent.putExtra("fromPanicBuy",1);
                startActivity(intent);
                break;
            case R.id.panicbuy_back:
                goBack();
                break;
            default:
                break;
        }
    }

    //返回操作
    private void goBack() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
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

    private class MyCountDownTimer extends CountDownTimer {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            PictureAirLog.e(TAG, "CountDownTime onTick" + millisUntilFinished);
            int sec = 1000;
            int min = 60 * 1000;
            int hour = 60 * 60 * 1000;
            long hh = millisUntilFinished / hour;
            long mm = (millisUntilFinished - hh * hour) / min;
            long ss = (millisUntilFinished - hh * hour - mm * min) / sec;

            String strHour = hh < 10 ? "0" + hh : "" + hh;
            String strMin = mm < 10 ? "0" + mm : "" + mm;
            String strSec = ss < 10 ? "0" + ss : "" + ss;

            tv_hour.setText(strHour);
            tv_min.setText(strMin);
            tv_sec.setText(strSec);
        }

        @Override
        public void onFinish() {
            PictureAirLog.e(TAG, "CountDownTime onFinish");
            tv_hour.setText(TIME_ZERO);
            tv_min.setText(TIME_ZERO);
            tv_sec.setText(TIME_ZERO);
            if (mStartDeal == DEAL_NOT_START) {
                mStartDeal = DEALING;
            } else if (mStartDeal == DEALING) {
                mStartDeal = NO_DEALS;
            }
            if (panciBuyHandler != null) {
                panciBuyHandler.sendEmptyMessage(COUNT_DOWN_TIME_FINISH);
            }
        }
    }
}
