package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
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
import com.pictureair.photopass.widget.VideoPlayerView;

import java.io.File;

/**
 * Created by bauer_bao on 16/9/2.
 */
public class ADVideoDetailProductActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout videoPlayerLL, animatedPhotoBackgroundLL, animatedPhotoBottomBtnsLL;
    private RelativeLayout animatedPhotoTopBarRl;
    private ImageView backImageView;
    private ImageView cartImageView;
    private TextView cartCountTextView, animatedPhotoIntroduce;
    private Button buyPPPBtn, upgradePPP, addPPPToCart;

    private PWToast pwToast;

    private PhotoInfo videoInfo;

    //视频组件
    private SeekBar seekBar;
    private VideoPlayerView videoPlayerView;
    private TextView durationTextView;
    private TextView playedTextView;
    private ImageButton btnPlayOrStop;
    private LinearLayout llControler;
    private TextView tvLoding;

    private final static int PROGRESS_CHANGED = 0;
    private final static int HIDE_CONTROLER = 1;
    private final static int SCREEN_FULL = 0;
    private final static int SCREEN_DEFAULT = 1;
    private final static int TIME = 3000;
    private static final int UPDATE_UI = 2866;
    private final int NOT_NETWORK = 111;
    private final static String TAG = ADVideoDetailProductActivity.class.getSimpleName();

    private int playedTime;// 最小化 保存播放时间
    private int screenWidth = 0;
    private int screenHeight = 0;
    private boolean isControllerShow = true;
    private boolean isPaused = false;
    //视频实际播放的宽高//4:3默认尺寸
    private int videoHeight = 600;
    private int videoWidth = 800;
    private boolean isOnline ;//网络true || 本地false
    private String videoPath;//视频本地路径 || 视频网络地址
    private boolean isPlayFinash = false;//是否播放完毕

    private Handler adVideoHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case PROGRESS_CHANGED:// 进度改变
                    int i = videoPlayerView.getCurrentPosition();
                    seekBar.setProgress(i);
                    if (isOnline) {
                        int j = videoPlayerView.getBufferPercentage();
                        seekBar.setSecondaryProgress(j * seekBar.getMax() / 100);
                    } else {
                        seekBar.setSecondaryProgress(0);
                    }

                    i /= 1000;
                    int minute = i / 60;
                    int second = i % 60;
                    minute %= 60;

                    playedTextView.setText(String.format("%02d:%02d", minute, second));
                    adVideoHandler.sendEmptyMessageDelayed(PROGRESS_CHANGED, 100);
                    break;

                case HIDE_CONTROLER:
                    hideController();
                    break;

                case UPDATE_UI:
                    Configuration cf = getResources().getConfiguration();
                    int ori = cf.orientation;
                    if (ori == cf.ORIENTATION_LANDSCAPE) {
                        crossScreen();
                    } else if (ori == cf.ORIENTATION_PORTRAIT) {
                        verticalScreen();
                    }
                    break;

                case NOT_NETWORK:
                    tvLoding.setText(R.string.no_network);
                    break;

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

        myOnError();// 播放错误
        myVVSizeChangeLinstener();// 屏幕大小发生改变的情况
        getScreenSize();// 给底下菜单的布局
        initSeekBarEvent();// 进度条调整
        initVVEvent();// VideoPlayerView类的回调

        adVideoHandler.sendEmptyMessage(UPDATE_UI);
        hideController();
        getNetwork();
    }

    private void initView() {
        videoPlayerLL = (LinearLayout) findViewById(R.id.ll_show);
        backImageView = (ImageView) findViewById(R.id.rt);
        cartImageView = (ImageView) findViewById(R.id.button_bag);
        cartCountTextView = (TextView) findViewById(R.id.textview_cart_count);
        buyPPPBtn = (Button) findViewById(R.id.animated_photo_buy_ppp_btn);
        upgradePPP = (Button) findViewById(R.id.animated_photo_upgrade_ppp_btn);
        addPPPToCart = (Button) findViewById(R.id.animated_photo_add_cart_btn);
        animatedPhotoTopBarRl = (RelativeLayout) findViewById(R.id.animated_photo_top_rl);
        animatedPhotoIntroduce = (TextView) findViewById(R.id.animated_photo_product_detail);
        animatedPhotoBottomBtnsLL = (LinearLayout) findViewById(R.id.animated_photo_bottom_btns_ll);

        //视频组件
        llControler = (LinearLayout) findViewById(R.id.ll_controler);
        durationTextView = (TextView) findViewById(R.id.duration);
        playedTextView = (TextView) findViewById(R.id.has_played);
        btnPlayOrStop = (ImageButton) findViewById(R.id.btn_play_or_stop);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        videoPlayerView = (VideoPlayerView) findViewById(R.id.vv);
        animatedPhotoBackgroundLL = (LinearLayout) findViewById(R.id.animated_photo_backtground_ll);
        tvLoding = (TextView) findViewById(R.id.tv_loding);

        backImageView.setOnClickListener(this);
        cartImageView.setOnClickListener(this);
        cartCountTextView.setOnClickListener(this);
        buyPPPBtn.setOnClickListener(this);
        upgradePPP.setOnClickListener(this);
        addPPPToCart.setOnClickListener(this);
        videoPlayerLL.setOnClickListener(this);
    }

    private void initData() {
        pwToast = new PWToast(this);
        videoInfo = (PhotoInfo) getIntent().getExtras().get("videoInfo");

        //测试数据
        videoInfo.videoWidth = 480;
        videoInfo.videoHeight = 480;
        videoInfo.photoThumbnail_1024 = "http://192.168.8.3/media/44ac0bc36a4eb8ae015c6d1789dbf32be2045a08b7b69bf61accac881130641d";

        getIsOnline();

        PictureAirLog.out("video w -->" + videoInfo.videoWidth + " ---" + videoInfo.videoHeight);
        ViewGroup.LayoutParams layoutParams = videoPlayerLL.getLayoutParams();
        layoutParams.height = ScreenUtil.getScreenWidth(this) * videoInfo.videoHeight / videoInfo.videoWidth;
        PictureAirLog.out("video h--->" + layoutParams.height);

        //视频组件
        btnPlayOrStop.setImageResource(R.drawable.play);
        btnPlayOrStop.setAlpha(0xBB);
        btnPlayOrStop.setVisibility(View.GONE);
    }

    private void getNetwork() {
        if (!isOnline) {
            startVideo();
        } else {
            if (AppUtil.getNetWorkType(this) == AppUtil.NETWORKTYPE_INVALID) {
                adVideoHandler.sendEmptyMessage(NOT_NETWORK);
            } else {
                startVideo();// 开始播放视频
            }
        }
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

            case R.id.ll_show:
                // 单次处理
                if (isPaused) {
                    playVideo();
                } else {
                    pausedVideo();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        getScreenSize();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            crossScreen();//横屏计算大小
        } else {
            verticalScreen();//竖屏计算大小
        }
        isPausedOrPlay();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPause() {
        super.onPause();
        playedTime = videoPlayerView.getCurrentPosition();
        pausedVideo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartCount();
        isPaused = true;
        if (!isPlayFinash){
            videoPlayerView.seekTo(playedTime);
            videoPlayerView.start();
            if (videoPlayerView.isPlaying()) {
                btnPlayOrStop.setVisibility(View.GONE);
                hideControllerDelay();
            }
            isPaused = false;
        }
    }

    @Override
    protected void onDestroy() {
        adVideoHandler.removeMessages(PROGRESS_CHANGED);
        adVideoHandler.removeMessages(HIDE_CONTROLER);

        if (videoPlayerView.isPlaying()) {
            videoPlayerView.stopPlayback();
        }
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

    private void getIsOnline(){
        String fileName = AppUtil.getReallyFileName(videoInfo.photoThumbnail_1024, 1);
        PictureAirLog.e(TAG, "filename=" + fileName);
        File filedir = new File(Common.PHOTO_DOWNLOAD_PATH);
        filedir.mkdirs();
        final File file = new File(filedir + "/" + fileName);
        if (!file.exists()) {
            videoPath = videoInfo.photoThumbnail_1024;
//            videoPath = Common.PHOTO_URL + videoInfo.photoThumbnail_1024;
            PictureAirLog.v(TAG, " 网络播放:"+videoPath);
            isOnline = true;
        } else {
            PictureAirLog.v(TAG, " 本地播放");
            videoPath = file.getPath();
            isOnline = false;
        }
    }

    private void startVideo() {
        isPlayFinash = false;
        videoPlayerView.setVideoPath(videoPath);
        cancelDelayHide();
        hideControllerDelay();
    }

    private void initVVEvent() {
        videoPlayerView.setMyMediapalerPrepared(new VideoPlayerView.myMediapalerPrepared() {
            @Override
            public void myOnrepared(MediaPlayer mp) {
                PictureAirLog.d(TAG, "===> myOnrepared");
                tvLoding.setVisibility(View.GONE);
                videoPlayerLL.setEnabled(true);
            }
        });
        videoPlayerView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer arg0) {
                PictureAirLog.d(TAG, "===> onPrepared");
                setVideoScale(SCREEN_DEFAULT);// 按比例（全屏）
                if (isControllerShow) {
                    showController();
                }
                int i = videoPlayerView.getDuration();
                PictureAirLog.d("onCompletion", "" + i);
                seekBar.setMax(i);
                i /= 1000;
                int minute = i / 60;
                int second = i % 60;
                minute %= 60;
                durationTextView.setText(String.format("%02d:%02d", minute, second));

                videoPlayerView.start();
                btnPlayOrStop.setVisibility(View.GONE);
                hideControllerDelay();

                adVideoHandler.sendEmptyMessage(PROGRESS_CHANGED);
            }
        });

        videoPlayerView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer arg0) {
                PictureAirLog.d(TAG, "===> onCompletion");

                pausedVideo();
                isPlayFinash = true;
            }
        });
    }

    private void initSeekBarEvent() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
                PictureAirLog.d(TAG, "===> onProgressChanged");
                if (fromUser) {
                    videoPlayerView.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
                PictureAirLog.d(TAG, "===> onStartTrackingTouch");

                adVideoHandler.removeMessages(HIDE_CONTROLER);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                PictureAirLog.d(TAG, "===> onStopTrackingTouch");

                adVideoHandler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);
            }
        });
    }

    private void myVVSizeChangeLinstener() {
        videoPlayerView.setMySizeChangeLinstener(new VideoPlayerView.MySizeChangeLinstener() {
            @Override
            public void doMyThings() {
                PictureAirLog.d(TAG, "===> doMyThings");

                setVideoScale(SCREEN_DEFAULT);
            }
        });
    }

    private void myOnError() {
        videoPlayerView.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                PictureAirLog.d(TAG, "===> onError");

                videoPlayerView.stopPlayback();
                return false;
            }

        });
    }

    /**
     * 获取屏幕宽高
     */
    private void getScreenSize() {
        screenHeight = ScreenUtil.getScreenHeight(this);
        screenWidth = ScreenUtil.getScreenWidth(this);
    }

    private void hideControllerDelay() {
        adVideoHandler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);
    }

    private void hideController() {
        if (llControler.getVisibility() == View.VISIBLE) {
            llControler.setVisibility(View.GONE);
            btnPlayOrStop.setVisibility(View.GONE);
        }
    }

    private void showController() {
        llControler.setVisibility(View.VISIBLE);
        btnPlayOrStop.setVisibility(View.VISIBLE);
        isControllerShow = true;
    }

    private void cancelDelayHide() {
        adVideoHandler.removeMessages(HIDE_CONTROLER);
    }

    // 设置可以全屏
    private void setVideoScale(int flag) {
        switch (flag) {
            case SCREEN_FULL:
                videoPlayerView.setVideoScale(screenWidth, screenHeight);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                break;

            case SCREEN_DEFAULT:
                videoPlayerView.setVideoScale(screenWidth, screenHeight);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                break;
        }
    }

    private void crossScreen() {
        animatedPhotoTopBarRl.setVisibility(View.GONE);
        animatedPhotoIntroduce.setVisibility(View.GONE);
        animatedPhotoBottomBtnsLL.setVisibility(View.GONE);
        animatedPhotoBackgroundLL.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
        setVideoResolution(false, videoWidth, videoHeight);
        setVideoScale(SCREEN_FULL);
    }

    private void verticalScreen() {
        animatedPhotoTopBarRl.setVisibility(View.VISIBLE);
        animatedPhotoIntroduce.setVisibility(View.VISIBLE);
        animatedPhotoBottomBtnsLL.setVisibility(View.VISIBLE);
        animatedPhotoBackgroundLL.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_light));
        setVideoResolution(true, videoWidth, videoHeight);
        setVideoScale(SCREEN_DEFAULT);
    }

    /**
     * 设置视频显示尺寸
     *
     * @param isVertical  竖屏？横屏
     * @param videoWidth  视频宽
     * @param videoHeight 视频高
     */
    private void setVideoResolution(boolean isVertical, int videoWidth, int videoHeight) {
        ViewGroup.LayoutParams layoutParams = videoPlayerLL.getLayoutParams();
        if (isVertical) {//竖屏
            layoutParams.width = ScreenUtil.getScreenWidth(this);
            layoutParams.height = layoutParams.width * videoHeight / videoWidth;
        } else {//横屏
            layoutParams.height = ScreenUtil.getScreenHeight(this);
            layoutParams.width = layoutParams.height * videoWidth / videoHeight;
        }
        videoPlayerLL.setLayoutParams(layoutParams);
    }

    private void isPausedOrPlay() {
        if (isPaused) {
            pausedVideo();
        } else {
            playVideo();
        }
    }

    /**
     * 暂停播放
     */
    private void pausedVideo(){
        isPaused = true;
        videoPlayerView.pause();
        btnPlayOrStop.setVisibility(View.VISIBLE);
        cancelDelayHide();
        showController();
    }

    /**
     * 播放视频
     */
    private void playVideo(){
        isPaused = false;
        isPlayFinash = false;
        videoPlayerView.start();
        btnPlayOrStop.setVisibility(View.GONE);
        cancelDelayHide();
        hideControllerDelay();
    }
}
