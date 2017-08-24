package com.pictureair.hkdlphotopass.widget.videoPlayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pictureair.hkdlphotopass.R;
import com.pictureair.hkdlphotopass.http.rxhttp.HttpCallback;
import com.pictureair.hkdlphotopass.http.rxhttp.RxSubscribe;
import com.pictureair.hkdlphotopass.util.API2;
import com.pictureair.hkdlphotopass.util.AppUtil;
import com.pictureair.hkdlphotopass.util.PictureAirLog;
import com.pictureair.hkdlphotopass.util.ReflectionUtil;
import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.components.RxActivity;

import java.io.File;

import okhttp3.ResponseBody;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Func1;

/**
 * Created by bauer_bao on 16/9/7.
 * 视频播放控件
 */
public class PWVideoPlayerManagerView extends RelativeLayout implements MediaPlayer.OnErrorListener,
        VideoPlayerView.OnVideoEventListenser, SeekBar.OnSeekBarChangeListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener {
    private VideoPlayerView videoPlayerView;
    private TextView loadingTV, hasPlayedTV, durationTV;
    private ImageButton playOrStopButton;
    private LinearLayout controllerBarLL;
    private SeekBar seekBar;
    private ImageView loadingIV;

    private Context context;
    private OnVideoPlayerViewEventListener videoPlayerViewEventListener;
    private Subscription subscription;

    private static final String TAG = PWVideoPlayerManagerView.class.getSimpleName();
    private final static int TIME = 3000;
    public final static int SCREEN_FULL = 0;
    public final static int SCREEN_DEFAULT = 1;
    private final static int PROGRESS_CHANGED = 2;
    private final static int HIDE_CONTROLER = 3;

    private boolean isPaused = true;
    private boolean isOnline ;//网络true || 本地false
    private boolean isPlayFinished = true;//是否播放完毕
    private boolean isReady = false;//视频是否已经准备好

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case PROGRESS_CHANGED:// 进度改变
                    int i = videoPlayerView.getCurrentPosition();
                    PictureAirLog.d(TAG, "change progress----->" + i);
                    seekBar.setProgress(i);
                    setPlayedTv(i);

                    if (!isPaused && !isPlayFinished && isReady) {
                        handler.sendEmptyMessageDelayed(PROGRESS_CHANGED, 200);
                    } else if (isPlayFinished) {
                        seekBar.setProgress(seekBar.getMax());
                    }
                    break;

                case HIDE_CONTROLER:
                    if (isPaused || isPlayFinished) {

                    } else {
                        hideController();
                    }
                    break;

                case API2.DOWNLOAD_FILE_PROGRESS:
                    int progress = msg.arg1;
                    int total = msg.arg2;
                    int currentProgress = 0;
                    if (total != 0) {
                        currentProgress = progress * 100 / total;
                    }

                    int result = ReflectionUtil.getDrawableId(context, "loading_" + (currentProgress / 8));

                    PictureAirLog.d("downloading---> " + currentProgress);

                    loadingIV.setImageResource(result);
                    loadingTV.setText(String.format(context.getString(R.string.animated_photo_loading), currentProgress));
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
        loadingIV = (ImageView) findViewById(R.id.iv_loading);
        playOrStopButton = (ImageButton) findViewById(R.id.btn_play_or_stop);
        controllerBarLL = (LinearLayout) findViewById(R.id.ll_controler);
        hasPlayedTV = (TextView) findViewById(R.id.has_played);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        durationTV = (TextView) findViewById(R.id.duration);

        videoPlayerView.setOnErrorListener(this);
        videoPlayerView.setOnVideoEventListenser(this);
        videoPlayerView.setOnPreparedListener(this);
        videoPlayerView.setOnCompletionListener(this);
        videoPlayerView.setOnBufferingUpdateListener(this);

        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setEnabled(false);
        hideController();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        PictureAirLog.d(TAG, "===> onError");
        videoPlayerViewEventListener.onError();
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
    public void needResumePlayVideo() {
        PictureAirLog.d(TAG, "need resume video");
        resumeVideo();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        PictureAirLog.d(TAG, "===> onProgressChanged" + progress);
        if (fromUser) {
            videoPlayerView.seekTo(progress);
            setPlayedTv(progress);
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
    public void onPrepared(MediaPlayer mp) {
        PictureAirLog.d(TAG, "===> onPrepared");
        loadingIV.setVisibility(GONE);
        loadingTV.setVisibility(View.GONE);
        isReady = true;
        seekBar.setEnabled(true);

        videoPlayerViewEventListener.setVideoScale(SCREEN_DEFAULT);
        showController();
        int i = videoPlayerView.getDuration();
        PictureAirLog.d(TAG, "" + i);
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

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if (isOnline) {
            seekBar.setSecondaryProgress(percent * seekBar.getMax() / 100);
        } else {
            seekBar.setSecondaryProgress(seekBar.getMax());
        }
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
        loadingIV.setVisibility(GONE);
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
        playOrStopButton.setVisibility(isReady ? VISIBLE : GONE);
        cancelDelayHideController();
        showController();
        handler.removeMessages(PROGRESS_CHANGED);
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
        PictureAirLog.d(TAG, "resume video" + isPaused);
        if (isReady && isPaused) {
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
        handler.removeMessages(PROGRESS_CHANGED);

        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    /**
     * 开始播放视频，目前的需求是，先下载完成之后再去播放
     * @param videoPath
     */
    public void startPlayVideo(String videoPath, boolean isOnline) {
        PictureAirLog.d(TAG, "start play video" + videoPath + isOnline);
        this.isOnline = isOnline;
        /**
         * 0.检查文件权限是否同意
         * 1.判断文件是否已经存在，
         * 2.如果存在，直接播放
         * 3.如果不存在，先从网络下载，之后再去播放
         */

        if (!isOnline || TextUtils.isEmpty(videoPath)) {//本地视频，或者路径为空 直接播放
            videoPlayerView.setVideoPath(videoPath);
        } else if (AppUtil.checkPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            String fileName = AppUtil.getReallyFileName(videoPath, 1);
            File file = new File(context.getCacheDir() + File.separator + "video" + File.separator + fileName);
            if (file.exists()) {//文件存在
                PictureAirLog.out("file path-->" + file.toString());
                videoPlayerView.setVideoPath(file.toString());
            } else {//文件不存在，需要从网络下载
                subscription = downloadHeadFile(videoPath, context.getCacheDir() + File.separator + "video" + File.separator, fileName);

            }

        }else{
            loadingIV.setVisibility(GONE);
            loadingTV.setText(R.string.permission_storage_message);
        }
    }

    private Subscription downloadHeadFile(String url, final String folderPath, final String fileName) {

        return API2.downloadHeadFile(url, new HttpCallback() {
            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                handler.obtainMessage(API2.DOWNLOAD_FILE_PROGRESS, (int)bytesWritten, (int)totalSize).sendToTarget();
            }
        }).map(new Func1<ResponseBody, String>() {
            @Override
            public String call(ResponseBody responseBody) {
                try {
                    return AppUtil.writeFile(responseBody, folderPath, fileName);
                } catch (Exception e) {
                    throw Exceptions.propagate(e);
                }
            }
        }).compose(((RxActivity)context).<String>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<String>() {
                    @Override
                    public void _onNext(String s) {
                        PictureAirLog.out("file path-->" + s);
                        videoPlayerView.setVideoPath(s);
                    }

                    @Override
                    public void _onError(int status) {
                        loadingIV.setVisibility(GONE);
                        loadingTV.setText(R.string.http_error_code_401);
                    }

                    @Override
                    public void onCompleted() {
                    }
                });
    }


    /**
     * 显示控制栏
     */
    private void showController() {
        if (!controllerBarLL.isShown()) {
            controllerBarLL.setVisibility(View.VISIBLE);
            playOrStopButton.setVisibility(isReady ? VISIBLE : GONE);
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
        PictureAirLog.d(TAG, "start tracking seekbar--->" + isPaused + isPlayFinished);
        if (isPaused || isPlayFinished) {
            handler.sendEmptyMessage(PROGRESS_CHANGED);
            isPaused = false;
            isPlayFinished = false;
        }
    }

    /**
     * 设置已播放的时间
     * @param time
     */
    private void setPlayedTv(int time) {
        time /= 1000;
        int minute = time / 60;
        int second = time % 60;
        minute %= 60;
        hasPlayedTV.setText(String.format("%02d:%02d", minute, second));
    }
}
