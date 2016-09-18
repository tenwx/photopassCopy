package com.pictureair.photopass.widget.videoPlayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.util.PictureAirLog;

/**
 * Created by bauer_bao on 16/9/7.
 * 视频播放控件
 */
public class PWVideoPlayerManagerView extends RelativeLayout implements MediaPlayer.OnErrorListener,
        VideoPlayerView.OnVideoSizeChangedListenser, SeekBar.OnSeekBarChangeListener,
        VideoPlayerView.OnVideoPlayerViewPreparedListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    private VideoPlayerView videoPlayerView;
    private TextView loadingTV, hasPlayedTV, durationTV;
    private ImageButton playOrStopButton;
    private LinearLayout controllerBarLL;
    private SeekBar seekBar;

    private Context context;
    private OnVideoPlayerViewEventListener videoPlayerViewEventListener;

    private static final String TAG = PWVideoPlayerManagerView.class.getSimpleName();
    private final static int TIME = 3000;
    public final static int SCREEN_FULL = 0;
    public final static int SCREEN_DEFAULT = 1;
    private final static int PROGRESS_CHANGED = 2;
    private final static int HIDE_CONTROLER = 3;

    private int playedTime;// 最小化 保存播放时间
    private boolean isPaused = true;
    private boolean isOnline ;//网络true || 本地false
    private boolean isPlayFinished = true;//是否播放完毕

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case PROGRESS_CHANGED:// 进度改变
                    PictureAirLog.out("change progress");
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

                    hasPlayedTV.setText(String.format("%02d:%02d", minute, second));

                    if (!isPaused && !isPlayFinished) {
                        handler.sendEmptyMessageDelayed(PROGRESS_CHANGED, 100);
                    } else if (isPlayFinished) {
                        seekBar.setProgress(seekBar.getMax());
                    }
                    break;

                case HIDE_CONTROLER:
                    hideController();
                    break;

                default:
                    break;
            }
            return false;
        }
    });

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

        videoPlayerView.setOnErrorListener(this);
        videoPlayerView.setOnVideoSizeChangedListenser(this);
        videoPlayerView.setOnVideoPlayerViewPreparedListener(this);
        videoPlayerView.setOnPreparedListener(this);
        videoPlayerView.setOnCompletionListener(this);

        seekBar.setOnSeekBarChangeListener(this);

        setEnabled(false);
        hideController();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        PictureAirLog.d(TAG, "===> onError");
        videoPlayerView.stopPlayback();
        handler.removeMessages(PROGRESS_CHANGED);
        return false;
    }

    @Override
    public void onSizeChanged() {
        PictureAirLog.d(TAG, "===> onSizeChanged");
        videoPlayerViewEventListener.setVideoScale(SCREEN_DEFAULT);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        PictureAirLog.d(TAG, "===> onProgressChanged");
        if (fromUser) {
            PictureAirLog.out("test--->" + progress);
            playedTime = progress;
            videoPlayerView.seekTo(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        handler.removeMessages(HIDE_CONTROLER);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        handler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);
    }

    @Override
    public void onVideoPlayerViewPrepared(MediaPlayer mp) {
        PictureAirLog.d(TAG, "===> onVideoPlayerViewPrepared");
        loadingTV.setVisibility(View.GONE);
        setEnabled(true);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        PictureAirLog.d(TAG, "===> onPrepared");
        videoPlayerViewEventListener.setVideoScale(SCREEN_DEFAULT);
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
        startTrackingSeekbar();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        PictureAirLog.d(TAG, "===> onCompletion");
        pausedVideo();
        isPlayFinished = true;
        seekBar.setProgress(seekBar.getMax());
    }

    /**
     * 设置监听
     * @param videoPlayerViewEventListener
     */
    public void setOnVideoPlayerViewEventListener(OnVideoPlayerViewEventListener videoPlayerViewEventListener) {
        this.videoPlayerViewEventListener = videoPlayerViewEventListener;
    }

    /**
     * 设置视频播放尺寸
     * @param width
     * @param height
     */
    public void setVideoScale(int width, int height) {
        videoPlayerView.setVideoScale(width, height);
    }

    /**
     * 设置loading的文案
     * @param strId
     */
    public void setLoadingText(int strId) {
        loadingTV.setText(strId);
    }

    /**
     * 获取当前播放的时间
     * @return
     */
    public int getCurrentIndexTime() {
        return videoPlayerView.getCurrentPosition();
    }

    /**
     * 暂停播放
     */
    public void pausedVideo(){
        isPaused = true;
        videoPlayerView.pause();
        playedTime = videoPlayerView.getCurrentPosition();
        playOrStopButton.setVisibility(View.VISIBLE);
        cancelDelayHideController();
        showController();

        PictureAirLog.out("test--->" + playedTime);
    }

    /**
     * 是否在暂停中
     * @return
     */
    public boolean isPaused() {
        return isPaused;
    }

    /**
     * 继续播放视频
     */
    public void resumeVideo() {
        PictureAirLog.out("resume video");
        if (isPaused) {
            if (isPlayFinished) {
                playedTime = 0;
            }
            videoPlayerView.seekTo(playedTime);
            videoPlayerView.start();
            if (videoPlayerView.isPlaying()) {
                playOrStopButton.setVisibility(View.GONE);
                cancelDelayHideController();
                hideControllerDelay();
            }
            startTrackingSeekbar();
            isPaused = false;
        }
    }

    /**
     * 结束播放视频
     */
    public void stopVideo() {
        if (videoPlayerView.isPlaying()) {
            videoPlayerView.stopPlayback();
        }
        isPaused = true;
    }

    /**
     * 开始播放视频
     * @param videoPath
     */
    public void startPlayVideo(String videoPath, boolean isOnline) {
        PictureAirLog.out("start play video");
        this.isOnline = isOnline;
        startTrackingSeekbar();
        videoPlayerView.setVideoPath(videoPath);
        cancelDelayHideController();
        hideControllerDelay();
    }

    /**
     * 显示控制栏
     */
    private void showController() {
        if (!controllerBarLL.isShown()) {
            controllerBarLL.setVisibility(View.VISIBLE);
            playOrStopButton.setVisibility(View.VISIBLE);
            videoPlayerViewEventListener.setControllerVisible(true);
        }
    }

    /**
     * 取消隐藏控制栏
     */
    private void cancelDelayHideController() {
        handler.removeMessages(HIDE_CONTROLER);
    }

    /**
     * 隐藏控制栏
     */
    private void hideControllerDelay() {
        handler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);
    }

    /**
     * 隐藏控制栏
     */
    private void hideController() {
        if (controllerBarLL.isShown()) {
            controllerBarLL.setVisibility(View.GONE);
            playOrStopButton.setVisibility(View.GONE);
            videoPlayerViewEventListener.setControllerVisible(false);
        }
    }

    /**
     * 开始进度
     */
    private void startTrackingSeekbar() {
        //暂停了，或者停止播放了，才开始播放
        PictureAirLog.out("start tracking seekbar--->" + isPaused + isPlayFinished);
        if (isPaused || isPlayFinished) {
            handler.sendEmptyMessage(PROGRESS_CHANGED);
            isPaused = false;
            isPlayFinished = false;
        }
    }
}
