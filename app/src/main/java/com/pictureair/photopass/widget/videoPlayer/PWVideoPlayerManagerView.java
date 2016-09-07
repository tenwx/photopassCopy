package com.pictureair.photopass.widget.videoPlayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.widget.VideoPlayerView;

/**
 * Created by bauer_bao on 16/9/7.
 */
public class PWVideoPlayerManagerView extends RelativeLayout implements View.OnClickListener,
        MediaPlayer.OnErrorListener, VideoPlayerView.MySizeChangeLinstener, SeekBar.OnSeekBarChangeListener,
        VideoPlayerView.myMediapalerPrepared, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    private VideoPlayerView videoPlayerView;
    private TextView loadingTV, hasPlayedTV, durationTV;
    private ImageButton playOrStopButton;
    private LinearLayout controllerBarLL;
    private SeekBar seekBar;

    private Context context;
    private OnVideoPlayerViewEventListener videoPlayerViewEventListener;

    private static final String TAG = PWVideoPlayerManagerView.class.getSimpleName();

    private int playedTime;// 最小化 保存播放时间
    private int screenWidth = 0;
    private int screenHeight = 0;
    private boolean isPaused = false;
    private int videoHeight = 480;
    private int videoWidth = 480;
    private boolean isOnline ;//网络true || 本地false
    private String videoPath;//视频本地路径 || 视频网络地址
    private boolean isPlayFinash = false;//是否播放完毕

    public PWVideoPlayerManagerView(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public PWVideoPlayerManagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    private void initView() {
        inflate(context, R.layout.surfaceview_mediaplaer, this);
        videoPlayerView = (VideoPlayerView) findViewById(R.id.vv);
        loadingTV = (TextView) findViewById(R.id.tv_loding);
        playOrStopButton = (ImageButton) findViewById(R.id.btn_play_or_stop);
        controllerBarLL = (LinearLayout) findViewById(R.id.ll_controler);
        hasPlayedTV = (TextView) findViewById(R.id.has_played);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        durationTV = (TextView) findViewById(R.id.duration);

        controllerBarLL.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);

        videoPlayerView.setOnErrorListener(this);
        videoPlayerView.setMySizeChangeLinstener(this);
        videoPlayerView.setMyMediapalerPrepared(this);
        videoPlayerView.setOnPreparedListener(this);
        videoPlayerView.setOnCompletionListener(this);
    }

    public void initData() {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_controler:
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
    public boolean onError(MediaPlayer mp, int what, int extra) {
        videoPlayerView.stopPlayback();
        return false;
    }

    @Override
    public void doMyThings() {
        PictureAirLog.d(TAG, "===> doMyThings");
//        setVideoScale(SCREEN_DEFAULT);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        PictureAirLog.d(TAG, "===> onProgressChanged");
        if (fromUser) {
            videoPlayerView.seekTo(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        PictureAirLog.d(TAG, "===> onStartTrackingTouch");

//        adVideoHandler.removeMessages(HIDE_CONTROLER);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        PictureAirLog.d(TAG, "===> onStopTrackingTouch");

//        adVideoHandler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);

    }

    @Override
    public void myOnrepared(MediaPlayer mp) {
        PictureAirLog.d(TAG, "===> myOnrepared");
        loadingTV.setVisibility(View.GONE);
//        videoPlayerLL.setEnabled(true);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        PictureAirLog.d(TAG, "===> onPrepared");
//        setVideoScale(SCREEN_DEFAULT);// 按比例（全屏）
        showController();
        int i = videoPlayerView.getDuration();
        PictureAirLog.d("onCompletion", "" + i);
        seekBar.setMax(i);
        i /= 1000;
        int minute = i / 60;
        int second = i % 60;
        minute %= 60;
        durationTV.setText(String.format("%02d:%02d", minute, second));

        videoPlayerView.start();
        playOrStopButton.setVisibility(View.GONE);
        hideControllerDelay();

//        adVideoHandler.sendEmptyMessage(PROGRESS_CHANGED);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        PictureAirLog.d(TAG, "===> onCompletion");

        pausedVideo();
        isPlayFinash = true;
    }

    /**
     * 暂停播放
     */
    private void pausedVideo(){
        isPaused = true;
        videoPlayerView.pause();
        playOrStopButton.setVisibility(View.VISIBLE);
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
        playOrStopButton.setVisibility(View.GONE);
        cancelDelayHide();
        hideControllerDelay();
    }

    private void hideControllerDelay() {
//        adVideoHandler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);
    }

    private void showController() {
        if (!controllerBarLL.isShown()) {
            controllerBarLL.setVisibility(View.VISIBLE);
            playOrStopButton.setVisibility(View.VISIBLE);
        }
    }

    private void cancelDelayHide() {
//        adVideoHandler.removeMessages(HIDE_CONTROLER);
    }

    private void hideController() {
        if (controllerBarLL.isShown()) {
            controllerBarLL.setVisibility(View.GONE);
            playOrStopButton.setVisibility(View.GONE);
        }
    }
}
