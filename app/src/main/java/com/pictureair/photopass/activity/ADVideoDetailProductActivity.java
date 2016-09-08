package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.PWToast;
import com.pictureair.photopass.widget.videoPlayer.OnVideoPlayerViewEventListener;
import com.pictureair.photopass.widget.videoPlayer.PWVideoPlayerManagerView;

import java.io.File;

/**
 * Created by bauer_bao on 16/9/2.
 */
public class ADVideoDetailProductActivity extends BaseActivity implements View.OnClickListener, OnVideoPlayerViewEventListener {

    private LinearLayout animatedPhotoBackgroundLL, animatedPhotoBottomBtnsLL;
    private RelativeLayout animatedPhotoTopBarRl;
    private ImageView backImageView;
    private ImageView cartImageView;
    private TextView cartCountTextView, animatedPhotoIntroduce;
    private Button buyPPPBtn, upgradePPP, addPPPToCart;

    private PWToast pwToast;

    private PhotoInfo videoInfo;

    //视频组件
    private PWVideoPlayerManagerView pwVideoPlayerManagerView;

    private final static String TAG = ADVideoDetailProductActivity.class.getSimpleName();

    private int screenWidth = 0;
    private int screenHeight = 0;
    private boolean isOnline ;//网络true || 本地false
    private String videoPath;//视频本地路径 || 视频网络地址

    private Handler adVideoHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {

                default:
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advideo_detail_product);

        initView();
        initData();

        Configuration cf = getResources().getConfiguration();
        int ori = cf.orientation;
        adjustScreenUI(ori == cf.ORIENTATION_LANDSCAPE);

        if (isOnline && AppUtil.getNetWorkType(this) == AppUtil.NETWORKTYPE_INVALID) {
            pwVideoPlayerManagerView.setLoadingText(R.string.no_network);
        } else {
            pwVideoPlayerManagerView.startPlayVideo(videoPath, isOnline);// 开始播放视频
        }
    }

    private void initView() {
        backImageView = (ImageView) findViewById(R.id.rt);
        cartImageView = (ImageView) findViewById(R.id.button_bag);
        cartCountTextView = (TextView) findViewById(R.id.textview_cart_count);
        buyPPPBtn = (Button) findViewById(R.id.animated_photo_buy_ppp_btn);
        upgradePPP = (Button) findViewById(R.id.animated_photo_upgrade_ppp_btn);
        addPPPToCart = (Button) findViewById(R.id.animated_photo_add_cart_btn);
        animatedPhotoTopBarRl = (RelativeLayout) findViewById(R.id.animated_photo_top_rl);
        animatedPhotoIntroduce = (TextView) findViewById(R.id.animated_photo_product_detail);
        animatedPhotoBottomBtnsLL = (LinearLayout) findViewById(R.id.animated_photo_bottom_btns_ll);
        animatedPhotoBackgroundLL = (LinearLayout) findViewById(R.id.animated_photo_backtground_ll);
        pwVideoPlayerManagerView = (PWVideoPlayerManagerView) findViewById(R.id.animated_photo_pwvideo_pmv);

        backImageView.setOnClickListener(this);
        cartImageView.setOnClickListener(this);
        cartCountTextView.setOnClickListener(this);
        buyPPPBtn.setOnClickListener(this);
        upgradePPP.setOnClickListener(this);
        addPPPToCart.setOnClickListener(this);
        pwVideoPlayerManagerView.setOnClickListener(this);
        pwVideoPlayerManagerView.setOnVideoPlayerViewEventListener(this);
    }

    private void initData() {
        pwToast = new PWToast(this);
        videoInfo = (PhotoInfo) getIntent().getExtras().get("videoInfo");

        getScreenSize();
        getReallyVideoUrl();
        setVideoResolution(true);
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

            case R.id.animated_photo_pwvideo_pmv:
                PictureAirLog.out("click video");
                // 单次处理
                if (pwVideoPlayerManagerView.isPaused()) {
                    pwVideoPlayerManagerView.playVideo();
                } else {
                    pwVideoPlayerManagerView.pausedVideo();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        getScreenSize();
        adjustScreenUI(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE);//调整UI
        setPausedOrPlay();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pwVideoPlayerManagerView.pausedVideo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartCount();
        pwVideoPlayerManagerView.resumeVideo();
    }

    @Override
    protected void onDestroy() {
        pwVideoPlayerManagerView.stopVideo();
        adVideoHandler.removeCallbacksAndMessages(null);
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

    private void getReallyVideoUrl(){
        String fileName = AppUtil.getReallyFileName(videoInfo.photoThumbnail_1024, 1);
        PictureAirLog.e(TAG, "filename=" + fileName);
        File filedir = new File(Common.PHOTO_DOWNLOAD_PATH);
        filedir.mkdirs();
        final File file = new File(filedir + "/" + fileName);
        if (!file.exists()) {
            videoPath = videoInfo.photoThumbnail_1024;
            PictureAirLog.v(TAG, " 网络播放:"+videoPath);
            isOnline = true;
        } else {
            PictureAirLog.v(TAG, " 本地播放");
            videoPath = file.getPath();
            isOnline = false;
        }
    }

    /**
     * 获取屏幕宽高
     */
    private void getScreenSize() {
        screenHeight = ScreenUtil.getScreenHeight(this);
        screenWidth = ScreenUtil.getScreenWidth(this);
    }


    @Override
    public void setVideoScale(int flag) {
        pwVideoPlayerManagerView.setVideoScale(screenWidth, screenHeight);
        switch (flag) {
            case PWVideoPlayerManagerView.SCREEN_FULL:
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                break;

            case PWVideoPlayerManagerView.SCREEN_DEFAULT:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                break;
        }
    }

    /**
     * 调整ui
     * @param isLandscape 是不是横屏模式
     */
    private void adjustScreenUI(boolean isLandscape) {
        animatedPhotoTopBarRl.setVisibility(isLandscape ? View.GONE : View.VISIBLE);
        animatedPhotoIntroduce.setVisibility(isLandscape ? View.GONE : View.VISIBLE);
        animatedPhotoBottomBtnsLL.setVisibility(isLandscape ? View.GONE : View.VISIBLE);
        animatedPhotoBackgroundLL.setBackgroundColor(ContextCompat.getColor(this, isLandscape ? R.color.black : R.color.gray_light));
        setVideoResolution(!isLandscape);
        setVideoScale(isLandscape ? PWVideoPlayerManagerView.SCREEN_FULL : PWVideoPlayerManagerView.SCREEN_DEFAULT);
    }

    /**
     * 设置视频显示尺寸
     *
     * @param isVertical  竖屏？横屏
     */
    private void setVideoResolution(boolean isVertical) {
        ViewGroup.LayoutParams layoutParams = pwVideoPlayerManagerView.getLayoutParams();
        if (isVertical) {//竖屏
            layoutParams.width = ScreenUtil.getScreenWidth(this);
            layoutParams.height = layoutParams.width * videoInfo.videoHeight / videoInfo.videoWidth;
        } else {//横屏
            layoutParams.height = ScreenUtil.getScreenHeight(this);
            layoutParams.width = layoutParams.height * videoInfo.videoWidth / videoInfo.videoHeight;
        }
        pwVideoPlayerManagerView.setLayoutParams(layoutParams);
    }

    private void setPausedOrPlay() {
        if (pwVideoPlayerManagerView.isPaused()) {
            pwVideoPlayerManagerView.pausedVideo();
        } else {
            pwVideoPlayerManagerView.playVideo();
        }
    }
}
