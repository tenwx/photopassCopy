package com.pictureair.hkdlphotopass.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.pictureair.hkdlphotopass.MyApplication;
import com.pictureair.hkdlphotopass.R;
import com.pictureair.jni.ciphermanager.PWJniUtil;
import com.pictureworks.android.customDialog.CustomDialog;
import com.pictureworks.android.db.PictureAirDbManager;
import com.pictureworks.android.entity.PhotoInfo;
import com.pictureair.hkdlphotopass.service.DownloadService;
import com.pictureworks.android.util.AppUtil;
import com.pictureworks.android.util.Common;
import com.pictureair.hkdlphotopass.util.DisneyVideoTool;
import com.pictureworks.android.util.PictureAirLog;
import com.pictureworks.android.util.ScreenUtil;
import com.pictureworks.android.widget.MyToast;
import com.pictureworks.android.widget.SharePop;
import com.pictureworks.android.widget.VideoPlayerView;
import com.pictureworks.android.widget.VideoPlayerView.MySizeChangeLinstener;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;


/**
 * 播放视频（需要优化播放）
 *
 * @author bass
 */
public class VideoPlayerActivity extends BaseActivity implements OnClickListener {
    private final static String TAG = "VideoPlayerActivity";
    private final static int SCREEN_FULL = 0;
    private final static int SCREEN_DEFAULT = 1;
    private static final int UPDATE_UI = 2866;
    private SeekBar seekBar = null;
    private TextView durationTextView = null;
    private TextView playedTextView = null;
    private ImageButton btnPlayOrStop = null;
    private RelativeLayout rlHead, rlBackground;
    private LinearLayout llEnd;
    private ImageView ivIsLove;
    private TextView tvLoding;
    private MyToast myToast;
    private SharePop sharePop;
    private SharedPreferences sharedPreferences;

    private Context context;
    private LinearLayout llControler, llShow, llShare, llDownload;
    private VideoPlayerView videoPlayerView = null;

    private int playedTime;// 最小化 保存播放时间
    private int screenWidth = 0;
    private int screenHeight = 0;
    private final static int TIME = 3000;
    private boolean isControllerShow = true;
    private boolean isPaused = false;
    private final static int PROGRESS_CHANGED = 0;
    private final static int HIDE_CONTROLER = 1;
    private PhotoInfo videoInfo;
    private int mNetWorkType;  //当前网络的状态
    private CustomDialog customdialog; //  对话框
    private PictureAirDbManager pictureAirDbManager;
    //视频实际播放的宽高//4:3默认尺寸
    private int videoHeight = 600;
    private int videoWidth = 800;
    private final int NOT_NETWORK = 111;
    private boolean isOnline ;//网络true || 本地false
    private String videoPath;//视频本地路径 || 视频网络地址
    private int shareType = 0;
    private boolean isPlayFinash = false;//是否播放完毕
    private final Handler videoPlayerHandler = new VideoPlayerHandler(this);

    private static class VideoPlayerHandler extends Handler{
        private final WeakReference<VideoPlayerActivity> mActivity;

        public VideoPlayerHandler(VideoPlayerActivity activity){
            mActivity = new WeakReference<>(activity);
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

    /**
     * 处理Message
     * @param msg
     */
    private void dealHandler(Message msg) {
        switch (msg.what) {
            case SharePop.TWITTER:
                shareType = msg.what;
                break;

            case NOT_NETWORK:
                tvLoding.setText(R.string.no_network);
                break;

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
                int hour = minute / 60;
                int second = i % 60;
                minute %= 60;
                // playedTextView.setText(String.format("%02d:%02d:%02d", hour,
                // minute, second));

                playedTextView.setText(String.format("%02d:%02d", minute,
                        second));
                videoPlayerHandler.sendEmptyMessageDelayed(PROGRESS_CHANGED, 100);
                break;
            case HIDE_CONTROLER:
                hideController();
                break;

            case UPDATE_UI:
                Configuration cf = context.getResources().getConfiguration();
                int ori = cf.orientation;
                if (ori == cf.ORIENTATION_LANDSCAPE) {
                    crossScreen();
                } else if (ori == cf.ORIENTATION_PORTRAIT) {
                    verticalScreen();
                }
                //更新收藏图标
                if (videoInfo.isLove == 1 || pictureAirDbManager.checkLovePhoto(videoInfo, sharedPreferences.getString(Common.USERINFO_ID, ""))) {
                    videoInfo.isLove = 1;
                    ivIsLove.setImageResource(R.drawable.discover_like);
                } else {
                    videoInfo.isLove = 0;
                    ivIsLove.setImageResource(R.drawable.discover_no_like);
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        context = this;
        initView();
        initBtnEvent();// 所有按钮的事件˙
        myOnError();// 播放错误
        myVVSizeChangeLinstener();// 屏幕大小发生改变的情况
        getScreenSize();// 给底下菜单的布局
        initSeekBarEvent();// 进度条调整
        initVVEvent();// VideoPlayerView类的回调
        getNetwork();
    }

    private void getNetwork() {
        if (!isOnline) {
            startVideo();
        } else {
            if (AppUtil.getNetWorkType(context) == AppUtil.NETWORKTYPE_INVALID) {
                videoPlayerHandler.sendEmptyMessage(NOT_NETWORK);
            } else {
                startVideo();// 开始播放视频
            }
        }
    }

    private void initView() {
        videoInfo = (PhotoInfo) getIntent().getExtras().get(DisneyVideoTool.FROM_STORY);
        if (0 != videoInfo.videoWidth || 0 != videoInfo.videoHeight) {
            this.videoWidth = videoInfo.videoWidth;
            this.videoHeight = videoInfo.videoHeight;

            //TEST
//            this.videoWidth = 1280;
//            this.videoHeight = 720;
        }
        getIsOnline();//读取网络视频还是本地
        sharePop = new SharePop(context, MyApplication.getTokenId());
        pictureAirDbManager = new PictureAirDbManager(context, PWJniUtil.getSqlCipherKey(Common.APP_TYPE_HKDLPP));
        sharedPreferences = getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, MODE_PRIVATE);
        myToast = new MyToast(context);
        setTopLeftValueAndShow(R.drawable.back_white,true);
        setTopTitleShow(R.string.my_disney_story);
        ivIsLove = getTopRightImageView();
        rlBackground = (RelativeLayout) findViewById(R.id.rl_background);
        tvLoding = (TextView) findViewById(R.id.tv_loding);
        llShare = (LinearLayout) findViewById(R.id.ll_share);
        llDownload = (LinearLayout) findViewById(R.id.ll_download);

        rlHead = (RelativeLayout) findViewById(R.id.head);
        llEnd = (LinearLayout) findViewById(R.id.ll_end);
        llShow = (LinearLayout) findViewById(R.id.ll_show);
        llShow.setEnabled(false);

        // 控制栏的
        llControler = (LinearLayout) findViewById(R.id.ll_controler);
        durationTextView = (TextView) findViewById(R.id.duration);
        playedTextView = (TextView) findViewById(R.id.has_played);

        btnPlayOrStop = (ImageButton) findViewById(R.id.btn_play_or_stop);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        videoPlayerView = (VideoPlayerView) findViewById(R.id.vv);

        btnPlayOrStop.setImageResource(R.drawable.play);
        btnPlayOrStop.setAlpha(0xBB);
        btnPlayOrStop.setVisibility(View.GONE);
        videoPlayerHandler.sendEmptyMessage(UPDATE_UI);
        hideController();
    }

    private void initBtnEvent() {
        llShow.setOnClickListener(this);
        llShare.setOnClickListener(this);
        llDownload.setOnClickListener(this);
    }

    private void getIsOnline(){
        String fileName = ScreenUtil.getReallyFileName(videoInfo.photoPathOrURL,1);
        PictureAirLog.e(TAG, "filename=" + fileName);
        File filedir = new File(Common.PHOTO_DOWNLOAD_PATH);
        filedir.mkdirs();
        final File file = new File(filedir + "/" + fileName);
        if (!file.exists()) {
            videoPath = Common.PHOTO_URL+videoInfo.photoPathOrURL;
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
//        videoPath = "http://m.fallback.wdjcdn.com/baobab/14468618701471.mp4";
        videoPlayerView.setVideoPath(videoPath);
        cancelDelayHide();
        hideControllerDelay();
    }

    private void initVVEvent() {
        videoPlayerView.setMyMediapalerPrepared(new VideoPlayerView.myMediapalerPrepared() {
            @Override
            public void myOnrepared(MediaPlayer mp) {
                PictureAirLog.e(TAG, "===> myOnrepared");
                tvLoding.setVisibility(View.GONE);
                llShow.setEnabled(true);
//                isLoading = false;
            }
        });
        videoPlayerView.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer arg0) {
                PictureAirLog.e(TAG, "===> onPrepared");
                setVideoScale(SCREEN_DEFAULT);// 按比例（全屏）
                // setVideoScale(SCREEN_FULL);//全屏（会改变视频尺寸）
                if (isControllerShow) {
                    showController();
                }
                int i = videoPlayerView.getDuration();
                PictureAirLog.d("onCompletion", "" + i);
                seekBar.setMax(i);
                i /= 1000;
                int minute = i / 60;
                int hour = minute / 60;
                int second = i % 60;
                minute %= 60;
                durationTextView.setText(String.format("%02d:%02d", minute,
                        second));
                // durationTextView.setText(String.format("%02d:%02d:%02d",
                // hour, minute, second));

                videoPlayerView.start();
                btnPlayOrStop.setVisibility(View.GONE);
                hideControllerDelay();

                videoPlayerHandler.sendEmptyMessage(PROGRESS_CHANGED);
            }
        });

        videoPlayerView.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer arg0) {
                PictureAirLog.e(TAG, "===> onCompletion");

                pausedVideo();
                isPlayFinash = true;
            }
        });
    }

    private void initSeekBarEvent() {
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekbar, int progress,
                                          boolean fromUser) {
                PictureAirLog.e(TAG, "===> onProgressChanged");
                if (fromUser) {
//                    if (!isLoading) {
                    videoPlayerView.seekTo(progress);
//                    }
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
                PictureAirLog.e(TAG, "===> onStartTrackingTouch");

                videoPlayerHandler.removeMessages(HIDE_CONTROLER);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                PictureAirLog.e(TAG, "===> onStopTrackingTouch");

                videoPlayerHandler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);
            }
        });
    }

    private void myVVSizeChangeLinstener() {
        videoPlayerView.setMySizeChangeLinstener(new MySizeChangeLinstener() {
            @Override
            public void doMyThings() {
                PictureAirLog.e(TAG, "===> doMyThings");

                setVideoScale(SCREEN_DEFAULT);
            }
        });
    }

    private void myOnError() {
        videoPlayerView.setOnErrorListener(new OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                PictureAirLog.e(TAG, "===> onError");

                videoPlayerView.stopPlayback();
                return false;
            }

        });
    }

    /**
     * 获取屏幕宽高
     */
    private void getScreenSize() {
        screenHeight = ScreenUtil.getScreenHeight(context);
        screenWidth = ScreenUtil.getScreenWidth(context);
    }

    private void hideControllerDelay() {
        videoPlayerHandler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);
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
        videoPlayerHandler.removeMessages(HIDE_CONTROLER);
    }

    // 设置可以全屏
    private void setVideoScale(int flag) {
        switch (flag) {
            case SCREEN_FULL:
                videoPlayerView.setVideoScale(screenWidth, screenHeight);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                break;

            case SCREEN_DEFAULT:
                int videoWidth = videoPlayerView.getVideoWidth();
                int videoHeight = videoPlayerView.getVideoHeight();
                int mWidth = screenWidth;
                int mHeight = screenHeight - 25;
                if (videoWidth > 0 && videoHeight > 0) {
                    if (videoWidth * mHeight > mWidth * videoHeight) {
                        mHeight = mWidth * videoHeight / videoWidth;
                    } else if (videoWidth * mHeight < mWidth * videoHeight) {
                        mWidth = mHeight * videoWidth / videoHeight;
                    } else {

                    }
                }
                videoPlayerView.setVideoScale(screenWidth, screenHeight);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                break;
        }
    }

    /**
     * 当横竖屏切换的时候会直接调用onCreate方法中的 onConfigurationChanged方法
     */

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        getScreenSize();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            sharePop.dismiss();
            crossScreen();//横屏计算大小
        } else {
            verticalScreen();//竖屏计算大小
        }
        isPausedOrPlay();
        super.onConfigurationChanged(newConfig);
    }

    private void crossScreen() {
        rlHead.setVisibility(View.GONE);
        llEnd.setVisibility(View.GONE);
        rlBackground.setBackgroundColor(getResources().getColor(R.color.black));
        setVideoResolution(false, videoWidth, videoHeight);
        setVideoScale(SCREEN_FULL);
    }

    private void verticalScreen() {
        rlHead.setVisibility(View.VISIBLE);
        llEnd.setVisibility(View.VISIBLE);
        rlBackground.setBackgroundColor(getResources().getColor(R.color.gray_light));
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
        ViewGroup.LayoutParams layoutParams = llShow.getLayoutParams();
        if (isVertical) {//竖屏
            layoutParams.width = ScreenUtil.getScreenWidth(this);
            layoutParams.height = layoutParams.width * videoHeight / videoWidth;
        } else {//横屏
            layoutParams.height = ScreenUtil.getScreenHeight(this);
            layoutParams.width = layoutParams.height * videoWidth / videoHeight;
        }
        llShow.setLayoutParams(layoutParams);
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

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.ll_share:
                sharePop.setshareinfo(null, videoInfo.shareURL, "online", videoInfo.photoId, SharePop.SHARE_VIDEO_TYOE, videoPlayerHandler);
                sharePop.showAtLocation(v, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                break;
            case R.id.ll_download:
                downloadVideo();
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
    public void TopViewClick(View view) {
        super.TopViewClick(view);
        switch (view.getId()) {
            case R.id.topLeftView:
                finish();
                break;
            case R.id.topRightView:
                isLoveEvent();
                break;
            default:
                break;
        }
    }

    /**
     * 下载视频
     * 1.检查是否有网络
     * 2.查看本地是否存在
     * 3.查看网络类型 遵守 下载规则
     * 4.开始下载
     */
    private void downloadVideo() {
        if (AppUtil.getNetWorkType(context) == AppUtil.NETWORKTYPE_INVALID) {
            myToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
            return;
        }
        if (isOnline) {
            downLoadPhotos();
        } else {
            myToast.setTextAndShow(R.string.neednotdownload_video, Common.TOAST_SHORT_TIME);
        }
    }

    //判断网络类型  并做操作。
    public void downLoadPhotos() {
        mNetWorkType = AppUtil.getNetWorkType(getApplicationContext());
        if (mNetWorkType == AppUtil.NETWORKTYPE_MOBILE) {
            //如果是手机流量 ，弹出对话狂
            customdialog = new CustomDialog.Builder(context)
                    .setMessage(getResources().getString(R.string.dialog_download_message))
                    .setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            customdialog.dismiss();
                        }
                    })
                    .setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            downloadPic();
                            customdialog.dismiss();
                        }
                    })
                    .setCancelable(false)
                    .create();
            customdialog.show();
        } else if (mNetWorkType == AppUtil.NETWORKTYPE_WIFI) {
            downloadPic();
        } else {
            // 网络不可用
        }
    }

    //直接下载
    private void downloadPic() {
        ArrayList<PhotoInfo> list = new ArrayList<PhotoInfo>();
        list.add(videoInfo);
        Intent intent = new Intent(context, DownloadService.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("photos", list);
        intent.putExtras(bundle);
        startService(intent);
    }

    /**
     * 收藏事件
     *
     * @return
     */
    public boolean isLoveEvent() {
        if (videoInfo.isLove == 1) {
            PictureAirLog.d(TAG, "cancel love");
            pictureAirDbManager.setPictureLove(videoInfo, sharedPreferences.getString(Common.USERINFO_ID, ""), false);
            videoInfo.isLove = 0;
            ivIsLove.setImageResource(R.drawable.discover_no_like);
        } else {
            PictureAirLog.d(TAG, "add love");
            pictureAirDbManager.setPictureLove(videoInfo, sharedPreferences.getString(Common.USERINFO_ID, ""), true);
            videoInfo.isLove = 1;
            ivIsLove.setImageResource(R.drawable.discover_like);
        }
        ((MyApplication) getApplication()).needScanFavoritePhotos = true;
        return true;
    }

    @Override
    protected void onPause() {
        playedTime = videoPlayerView.getCurrentPosition();
        pausedVideo();

        PictureAirLog.e(TAG, "=======>onPause   playedTime:" + playedTime);
        super.onPause();
    }

    @Override
    protected void onResume() {
        PictureAirLog.e(TAG, "=======>onResume   playedTime:" + playedTime);

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

        if (sharePop != null) {
            PictureAirLog.out("sharePop not null");
            if (shareType != SharePop.TWITTER) {
                PictureAirLog.out("dismiss dialog");
                sharePop.dismissDialog();
            }
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        videoPlayerHandler.removeMessages(PROGRESS_CHANGED);
        videoPlayerHandler.removeMessages(HIDE_CONTROLER);

        if (videoPlayerView.isPlaying()) {
            videoPlayerView.stopPlayback();
        }
        videoPlayerHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

}
