package com.pictureair.photopass.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.videoPlayer.OnVideoPlayerViewEventListener;
import com.pictureair.photopass.widget.videoPlayer.PWVideoPlayerManagerView;

import java.io.File;


/**
 * 播放视频
 *
 * @author bass
 */
public class VideoPlayerActivity extends BaseActivity implements OnClickListener, OnVideoPlayerViewEventListener {
    private final static String TAG = "VideoPlayerActivity";
    private PWVideoPlayerManagerView pwVideoPlayerManagerView;
    private ImageView backImageView;

    private PhotoInfo videoInfo;
    private boolean isOnline ;//网络true || 本地false
    private String videoPath;//视频本地路径 || 视频网络地址

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtil.setFullScreen(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_player);
        initView();
        startPlayVideo();
    }

    private void startPlayVideo() {
        if (isOnline && AppUtil.getNetWorkType(this) == AppUtil.NETWORKTYPE_INVALID) {
            pwVideoPlayerManagerView.setLoadingText(R.string.no_network);
        } else {
            pwVideoPlayerManagerView.startPlayVideo(videoPath, isOnline);// 开始播放视频
        }
    }

    private void initView() {
        videoInfo = (PhotoInfo) getIntent().getExtras().get("from_story");
        getReallyVideoUrl();//读取网络视频还是本地
        pwVideoPlayerManagerView = (PWVideoPlayerManagerView) findViewById(R.id.video_player_pmv);
        pwVideoPlayerManagerView.setOnClickListener(this);
        pwVideoPlayerManagerView.setOnVideoPlayerViewEventListener(this);

        backImageView = (ImageView) findViewById(R.id.video_player_back_imv);
        backImageView.setOnClickListener(this);

        Configuration cf = getResources().getConfiguration();
        adjustScreenUI(cf.orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    private void getReallyVideoUrl(){
        String fileName = AppUtil.getReallyFileName(videoInfo.getPhotoOriginalURL(),1);
        PictureAirLog.e(TAG, "filename=" + fileName);
        File filedir = new File(Common.PHOTO_DOWNLOAD_PATH);
        filedir.mkdirs();
        final File file = new File(filedir + "/" + fileName);
        if (!file.exists()) {
            videoPath = videoInfo.getPhotoThumbnail_1024();
            PictureAirLog.v(TAG, " 网络播放:"+videoPath);
            isOnline = true;
        } else {
            PictureAirLog.v(TAG, " 本地播放");
            videoPath = file.getPath();
            isOnline = false;
        }
        if (videoInfo.getVideoHeight() == 0) {
            videoInfo.setVideoHeight(300);
        }
        if (videoInfo.getVideoWidth() == 0) {
            videoInfo.setVideoWidth(400);
        }
    }

    // 设置可以全屏
    @Override
    public void setVideoScale(int flag) {
    }

    @Override
    public void setControllerVisible(boolean visible) {
        backImageView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onError() {
        setControllerVisible(true);
        pwVideoPlayerManagerView.setLoadingText(R.string.http_error_code_401);
    }

    /**
     * 当横竖屏切换的时候会直接调用onCreate方法中的 onConfigurationChanged方法
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            adjustScreenUI(true);
        } else {
            adjustScreenUI(false);
        }
        setPausedOrPlay();
        super.onConfigurationChanged(newConfig);
    }

    /**
     * 调整ui
     * @param isLandscape 是不是横屏模式
     */
    private void adjustScreenUI(boolean isLandscape) {
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
            layoutParams.height = layoutParams.width * videoInfo.getVideoHeight() / videoInfo.getVideoWidth();
        } else {//横屏
            layoutParams.height = ScreenUtil.getScreenHeight(this);
            layoutParams.width = layoutParams.height * videoInfo.getVideoWidth() / videoInfo.getVideoHeight();
        }
        pwVideoPlayerManagerView.setVideoScale(layoutParams.width, layoutParams.height);
        pwVideoPlayerManagerView.setLayoutParams(layoutParams);
    }

    private void setPausedOrPlay() {
        if (pwVideoPlayerManagerView.isPaused()) {
            pwVideoPlayerManagerView.pausedVideo();
        } else {
            pwVideoPlayerManagerView.resumeVideo();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_player_pmv:
                // 单次处理
                if (pwVideoPlayerManagerView.isPaused()) {
                    pwVideoPlayerManagerView.resumeVideo();
                } else {
                    pwVideoPlayerManagerView.pausedVideo();
                }
                break;

            case R.id.video_player_back_imv:
                exitActivity();
                break;

            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        pwVideoPlayerManagerView.pausedVideo();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        pwVideoPlayerManagerView.stopVideo();
        super.onDestroy();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitActivity();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exitActivity() {
        finish();
        overridePendingTransition(R.anim.activity_fadein, R.anim.activity_fadeout);
    }
}
