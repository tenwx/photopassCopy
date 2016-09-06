package com.pictureair.photopass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.widget.PWToast;

/**
 * Created by bauer_bao on 16/9/2.
 */
public class ADVideoDetailProductActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout videoPlayerLL;
    private ImageView backImageView;
    private ImageView cartImageView;
    private TextView cartCountTextView;
    private Button buyPPPBtn, upgradePPP, addPPPToCart;

    private PWToast pwToast;

    private PhotoInfo photoInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advideo_detail_product);

        initView();
        initData();
    }

    private void initView() {
        videoPlayerLL = (LinearLayout) findViewById(R.id.ll_show);
        backImageView = (ImageView) findViewById(R.id.rt);
        cartImageView = (ImageView) findViewById(R.id.button_bag);
        cartCountTextView = (TextView) findViewById(R.id.textview_cart_count);
        buyPPPBtn = (Button) findViewById(R.id.animated_photo_buy_ppp_btn);
        upgradePPP = (Button) findViewById(R.id.animated_photo_upgrade_ppp_btn);
        addPPPToCart = (Button) findViewById(R.id.animated_photo_add_cart_btn);
        backImageView.setOnClickListener(this);
        cartImageView.setOnClickListener(this);
        cartCountTextView.setOnClickListener(this);
        buyPPPBtn.setOnClickListener(this);
        upgradePPP.setOnClickListener(this);
        addPPPToCart.setOnClickListener(this);
    }

    private void initData() {
        pwToast = new PWToast(this);
        photoInfo = (PhotoInfo) getIntent().getExtras().get("videoInfo");
        PictureAirLog.out("video w -->" + photoInfo.videoWidth + " ---" + photoInfo.videoHeight);
//        ViewGroup.LayoutParams layoutParams = videoPlayerLL.getLayoutParams();
//        layoutParams.height = ScreenUtil.getScreenWidth(this) * photoInfo.videoHeight / photoInfo.videoWidth;
//        PictureAirLog.out("video h--->" + layoutParams.height);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rt://返回按钮
                finish();
                break;

            case R.id.button_bag://购物车按钮
            case R.id.textview_cart_count:
                if (AppUtil.getNetWorkType(MyApplication.getInstance()) == 0) {
                    pwToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
                    return;
                }
                Intent intent = new Intent(ADVideoDetailProductActivity.this, CartActivity.class);
                startActivity(intent);
                break;

            case R.id.animated_photo_buy_ppp_btn://购买ppp
                break;

            case R.id.animated_photo_upgrade_ppp_btn://使用已存在的ppp升级
                break;

            case R.id.animated_photo_add_cart_btn://把ppp加入购物车
                break;

            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        playedTime = videoPlayerView.getCurrentPosition();
//        pausedVideo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartCount();
//        PictureAirLog.e(TAG, "=======>onResume   playedTime:" + playedTime);
//
//        isPaused = true;
//        if (!isPlayFinash){
//            videoPlayerView.seekTo(playedTime);
//            videoPlayerView.start();
//            if (videoPlayerView.isPlaying()) {
//                btnPlayOrStop.setVisibility(View.GONE);
//                hideControllerDelay();
//            }
//            isPaused = false;
//        }
//
//        if (sharePop != null) {
//            PictureAirLog.out("sharePop not null");
//            if (shareType != SharePop.TWITTER) {
//                PictureAirLog.out("dismiss dialog");
//                sharePop.dismissDialog();
//            }
//        }
    }

    @Override
    protected void onDestroy() {
//        videoPlayerHandler.removeMessages(PROGRESS_CHANGED);
//        videoPlayerHandler.removeMessages(HIDE_CONTROLER);
//
//        if (videoPlayerView.isPlaying()) {
//            videoPlayerView.stopPlayback();
//        }
//        videoPlayerHandler.removeCallbacksAndMessages(null);
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
}
