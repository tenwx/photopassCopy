package com.pictureair.photopass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.entity.CartPhotosInfo;
import com.pictureair.photopass.entity.DealingInfo;
import com.pictureair.photopass.entity.GoodInfoPictures;
import com.pictureair.photopass.entity.GoodsInfo;
import com.pictureair.photopass.eventbus.StoryLoadCompletedEvent;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.GlideUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.CustomTextView;
import com.pictureair.photopass.widget.NoNetWorkOrNoCountView;
import com.pictureair.photopass.widget.PWToast;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by pengwu on 16/9/28.
 * 抢购窗体
 */
public class PanicBuyActivity extends BaseActivity implements View.OnClickListener{

    private RelativeLayout layout_content;
    private ImageView bannerViewDetail;
    private CustomTextView tv_price;
    private Button btn_purchase;
//    private TextView tv_detail1;
    private TextView tv_detail2;
    private TextView tv_detail_title;
    private TextView tv_time_status;
    private TextView tv_hour;
    private TextView tv_min;
    private TextView tv_sec;
    private TextView tv_title;
    private ImageView img_back;
    private NoNetWorkOrNoCountView noNetWorkOrNoCountView;

    private GoodsInfo goodsInfo;
    private String[] photoUrls;


    private int mStartDeal = NO_DEALS;
    private MyCountDownTimer countDownTimer;
    private static final int DEAL_NOT_START = 0;
    private static final int DEALING = 1;
    private static final int NO_DEALS = 2;
    private static final String TAG = "PanicBuyActivity";
    private static final String TIME_ZERO = "00";
    private Date startDate;
    private Date endDate;
    private DealingInfo dealingInfo;
    private static final int COUNT_DOWN_TIME_FINISH = 1111;
    private String currency;
    private PWToast myToast;

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
                    if (endDate != null && currentDate != null && endDate.getTime() >= currentDate.getTime() - goodsInfo.getDealing().getTimeOffset()) {
                        countDownTimer = new MyCountDownTimer(endDate.getTime() - (currentDate.getTime()- goodsInfo.getDealing().getTimeOffset()), 1000, PanicBuyActivity.this);
                        countDownTimer.start();
                        enableBuy();
                        showDealsDetails();
                    }
                } else if (mStartDeal == NO_DEALS) {
                    //活动结束的UI
                    finishBuy(R.string.special_deal_end);
                }
                break;

            case API1.GET_SINGLE_GOODS_SUCCESS:
                goodsInfo = (GoodsInfo) msg.obj;
                if (goodsInfo != null) {
                    PictureAirLog.v(goodsInfo.toString());
                    if (goodsInfo.getPictures() != null && goodsInfo.getPictures().size() > 0) {
                        PictureAirLog.v(TAG, "goodsInfo name: " + goodsInfo.getName());
                        List<GoodInfoPictures> goodInfoPicturesList = new ArrayList<>();
                        goodInfoPicturesList.add(goodsInfo.getPictures().get(1));
//                        bannerViewDetail.findimagepath(goodInfoPicturesList);
                        GlideUtil.load(PanicBuyActivity.this, Common.PHOTO_URL + goodInfoPicturesList.get(0).getUrl(), bannerViewDetail);
                        //封装购物车宣传图
                        photoUrls = new String[goodsInfo.getPictures().size()];
                        for (int i = 0; i < goodsInfo.getPictures().size(); i++) {
                            photoUrls[i] = goodsInfo.getPictures().get(i).getUrl();
                        }
                    }

                    showDealsDetails();
                    try {
                        PictureAirLog.d(TAG, goodsInfo.getDealing().getCurrTimeIntervalStart());
                        PictureAirLog.d(TAG, goodsInfo.getDealing().getCurrTimeIntervalEnd());

                        long localTime = System.currentTimeMillis();
                        Date currentSystemServerDate = AppUtil.getDateLocalFromStr(goodsInfo.getDealing().getCurrTime());//服务器时间转换成手机本地时间,目的是不同时区可以准确计时
                        goodsInfo.getDealing().setTimeOffset(localTime - currentSystemServerDate.getTime());
                        startDate = AppUtil.getDateLocalFromStr(goodsInfo.getDealing().getCurrTimeIntervalStart());
                        endDate = AppUtil.getDateLocalFromStr(goodsInfo.getDealing().getCurrTimeIntervalEnd());
                        PictureAirLog.d(TAG, "formatStartDate " + startDate.toString());
                        PictureAirLog.d(TAG, "formatEndDate " + endDate.toString());

                        Date currentData = getCurrentDate();
                        if (goodsInfo.getDealing().getState() == -2 || goodsInfo.getDealing().getState() == -3) {
                            if (startDate.getTime()  > currentData.getTime() - goodsInfo.getDealing().getTimeOffset()) {
                                mStartDeal = DEAL_NOT_START;
                            }else if (currentData.getTime() - goodsInfo.getDealing().getTimeOffset() <= endDate.getTime()) {
                                mStartDeal = DEALING;
                            }
                        } else if (goodsInfo.getDealing().getState() == 1) {
                            if ( currentData.getTime() - goodsInfo.getDealing().getTimeOffset() >= startDate.getTime() && currentData.getTime() - goodsInfo.getDealing().getTimeOffset() <= endDate.getTime()) {
                                mStartDeal = DEALING;
                            } else {
                                mStartDeal = NO_DEALS;
                            }
                        }
                        if (mStartDeal == DEAL_NOT_START) {
                            countDownTimer = new MyCountDownTimer(startDate.getTime() - (currentData.getTime() - goodsInfo.getDealing().getTimeOffset()), 1000, PanicBuyActivity.this);
                            countDownTimer.start();
                            disableBuy();
                        } else if (mStartDeal == DEALING) {
                            countDownTimer = new MyCountDownTimer(endDate.getTime() - (currentData.getTime() - goodsInfo.getDealing().getTimeOffset()), 1000, PanicBuyActivity.this);
                            countDownTimer.start();
                            enableBuy();
                        } else if (mStartDeal == NO_DEALS) {
                            finishBuy(R.string.special_deal_end);
                        }
                        layout_content.setVisibility(View.VISIBLE);
                        noNetWorkOrNoCountView.setVisibility(View.GONE);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                dismissPWProgressDialog();
                break;

            case API1.GET_SINGLE_GOODS_FAILED:
                layout_content.setVisibility(View.GONE);
                noNetWorkOrNoCountView.setVisibility(View.VISIBLE);
                dismissPWProgressDialog();
                break;

            case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_RELOAD:
                if (dealingInfo != null) {
                    showPWProgressDialog();
                    API1.getSingleGoods(dealingInfo.getDealingUrl(), MyApplication.getInstance().getLanguageType(), panciBuyHandler, false);
                }
                break;

            case API1.UPDATE_SINGLE_GOODS_SUCCESS:
                dismissPWProgressDialog();
                GoodsInfo info = (GoodsInfo) msg.obj;
                int lave = info.getDealing().getLave();
                if (info.getDealing().getPossible() != null && info.getDealing().getPossible()) {
                    if (lave == -1 || lave > 0) {
                        Intent intent = new Intent(PanicBuyActivity.this, SubmitOrderActivity.class);
                        ArrayList<CartItemInfo> orderinfoArrayList = new ArrayList<>();
                        CartItemInfo cartItemInfo = new CartItemInfo();

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
                        cartItemInfo.setGoodsKey(goodsInfo.getGoodsKey());

                        orderinfoArrayList.add(cartItemInfo);
                        intent.putExtra("orderinfo", orderinfoArrayList);
                        intent.putExtra("fromPanicBuy", 1);
                        intent.putExtra("dealingKey", goodsInfo.getDealing().getKey());
                        startActivity(intent);
                    } else {
                        myToast.setTextAndShow(R.string.special_deal_count_enough, Common.TOAST_SHORT_TIME);
                    }
                } else {
                    myToast.setTextAndShow(R.string.special_deal_count_enough, Common.TOAST_SHORT_TIME);
                }
                break;

            case API1.UPDATE_SINGLE_GOODS_FAILED:
                dismissPWProgressDialog();
                myToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
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
        layout_content = (RelativeLayout) findViewById(R.id.special_deal_content_layout);
        bannerViewDetail = (ImageView) findViewById(R.id.special_deal_image);
        bannerViewDetail.measure(0,0);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bannerViewDetail.getLayoutParams();
        params.width = ScreenUtil.getScreenWidth(this);
        params.height = (int) (params.width /4.0 * 3);
        bannerViewDetail.setLayoutParams(params);
        tv_price = (CustomTextView) findViewById(R.id.special_deal_price);
        tv_price.setTypeface(MyApplication.getInstance().getFontBold());
        tv_hour = (TextView) findViewById(R.id.special_deal_hour);
        tv_min = (TextView) findViewById(R.id.special_deal_min);
        tv_sec = (TextView) findViewById(R.id.special_deal_sec);
        tv_detail_title = (TextView) findViewById(R.id.special_deal_detail_title);
//        tv_detail1 = (TextView) findViewById(R.id.special_deal_detail1);
        tv_detail2 = (TextView) findViewById(R.id.special_deal_detail2);
        tv_time_status = (TextView) findViewById(R.id.special_deal_time_status);
        tv_title = (TextView)findViewById(R.id.special_deal_title);
        btn_purchase = (Button) findViewById(R.id.special_deal_purchase);
        img_back = (ImageView) findViewById(R.id.special_deal_back);
        noNetWorkOrNoCountView = (NoNetWorkOrNoCountView) findViewById(R.id.special_deal_NoNetWorkView);
        myToast = new PWToast(this);
        btn_purchase.setOnClickListener(this);
        img_back.setOnClickListener(this);
        showPWProgressDialog();
    }

    private void initData() {
        currency = SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CURRENCY, Common.DEFAULT_CURRENCY);
        tv_hour.setText("00");
        tv_min.setText("00");
        tv_sec.setText("00");
        noNetWorkOrNoCountView.setResult(R.string.no_network, R.string.click_button_reload, R.string.reload, R.drawable.no_network, panciBuyHandler, true);
        dealingInfo = (DealingInfo) getIntent().getSerializableExtra("dealingInfo");
        PictureAirLog.d(dealingInfo.toString());

        if (dealingInfo != null) {
            API1.getSingleGoods(dealingInfo.getDealingUrl(), MyApplication.getInstance().getLanguageType(), panciBuyHandler, false);
        }
    }

    private void enableBuy() {
        btn_purchase.setEnabled(true);
        btn_purchase.setText(R.string.special_deal_buy);
        tv_time_status.setText(R.string.special_deal_end_content);

    }

    private void disableBuy() {
        btn_purchase.setEnabled(false);
        btn_purchase.setText(R.string.special_deal_begin_soon);
        tv_time_status.setText(R.string.special_deal_start_content);
    }

    private void finishBuy(int resourceId) {
        btn_purchase.setEnabled(false);
        btn_purchase.setText(resourceId);
        tv_time_status.setText(R.string.special_deal_already_end_content);
    }

    private void showDealsDetails() {
//        tv_title.setText(goodsInfo.getDealing().getTitle());
        tv_detail_title.setText(goodsInfo.getNameAlias());
//        tv_detail1.setText(goodsInfo.getDescription()+"\n");
        String detail = new String("");
        if (goodsInfo.getCopywriter() != null) {
            String[] spilt =  goodsInfo.getCopywriter().split("\n");
            if (spilt.length > 1) {
                detail = appendEnter(spilt);
            }
        }
        tv_detail2.setText(detail);
        tv_price.setText(currency + goodsInfo.getPrice());
    }

    private String appendEnter(String[] spilt) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < spilt.length; i++) {
            if (i < spilt.length -1) {
                buffer.append(spilt[i]).append("\n").append("\n");
            } else {
                buffer.append(spilt[i]).append("\n");
            }
        }

        return buffer.toString();
    }

    private Date getCurrentDate() {
        Date currentData = new Date(System.currentTimeMillis());
        return currentData;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.special_deal_purchase:
                showPWProgressDialog();
                API1.getSingleGoods(dealingInfo.getDealingUrl(), MyApplication.getInstance().getLanguageType(), panciBuyHandler, true);

                break;
            case R.id.special_deal_back:
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
        if (mStartDeal == NO_DEALS) {
            EventBus.getDefault().postSticky(new StoryLoadCompletedEvent(false, true));
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

    private void setTime(String hour, String min, String sec){
        tv_hour.setText(hour);
        tv_min.setText(min);
        tv_sec.setText(sec);
    }

    private void goNextStatus() {
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

    private static class MyCountDownTimer extends CountDownTimer {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        private WeakReference<PanicBuyActivity> mActivity;
        public MyCountDownTimer(long millisInFuture, long countDownInterval, PanicBuyActivity activity) {
            super(millisInFuture, countDownInterval);
            mActivity = new WeakReference<PanicBuyActivity>(activity);
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

            if (mActivity.get() == null) {
                return;
            }

            String strHour = hh < 10 ? "0" + hh : "" + hh;
            String strMin = mm < 10 ? "0" + mm : "" + mm;
            String strSec = ss < 10 ? "0" + ss : "" + ss;

            mActivity.get().setTime(strHour, strMin, strSec);
        }

        @Override
        public void onFinish() {
            PictureAirLog.e(TAG, "CountDownTime onFinish");
            if (mActivity.get() == null) {
                return;
            }
            mActivity.get().goNextStatus();

        }
    }
}
