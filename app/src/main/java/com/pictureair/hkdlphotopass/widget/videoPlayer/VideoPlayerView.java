package com.pictureair.hkdlphotopass.widget.videoPlayer;


import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;

import com.pictureair.hkdlphotopass.util.PictureAirLog;

import java.io.IOException;

/**
 * 播放器，基于MediaPlayer实现
 * 1.setVideoPath
 * 2.cancelDelayHide();
 * 3.hideControllerDelay();
 */
public class VideoPlayerView extends SurfaceView {
    private String TAG = "VideoPlayerView";
    private Context mContext;
    // settable by the client
    private Uri mUri;
    private int mDuration;
    private SurfaceHolder mSurfaceHolder = null;
    private MediaPlayer mMediaPlayer = null;
    private boolean mIsPrepared;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private OnCompletionListener mOnCompletionListener;
    private MediaPlayer.OnPreparedListener mOnPreparedListener;
    private MediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;
    private int mCurrentBufferPercentage;
    private OnErrorListener mOnErrorListener;
    private boolean mStartWhenPrepared;
    private int mSeekWhenPrepared;
    private OnVideoEventListenser mMyChangeLinstener;

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    public void setVideoScale(int width, int height) {
        LayoutParams lp = getLayoutParams();
        lp.height = height;
        lp.width = width;
        setLayoutParams(lp);
    }

    public interface OnVideoEventListenser {
        /**
         * 尺寸改变
         */
        void onSizeChanged();

        /**
         * activity onStop之后，surfaceview 会 destroy，导致回来之后视频黑屏。因此做成继续播放
         */
        void needResumePlayVideo();
    }

    public void setOnVideoEventListenser(OnVideoEventListenser l) {
        mMyChangeLinstener = l;
    }

    public VideoPlayerView(Context context) {
        super(context);
        mContext = context;
        initVideoView();
    }

    public VideoPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
        initVideoView();
    }

    public VideoPlayerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initVideoView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private void initVideoView() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        getHolder().addCallback(mSHCallback);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
    }

    /**
     * 首先设置路径
     *
     * @param path
     */
    public void setVideoPath(String path) {
        if (path == null) {
            path = "";
        }
        setVideoURI(Uri.parse(path));
    }

    private void setVideoURI(Uri uri) {
        mUri = uri;
        mStartWhenPrepared = false;
        mSeekWhenPrepared = 0;
        requestLayout();
        invalidate();
        //防止surface先准备好，导致uri设置之后不会播放的问题
        openVideo();
    }

    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void openVideo() {
        PictureAirLog.d(TAG, "open video");
        if (mUri == null || mSurfaceHolder == null) {
            return;
        }
        PictureAirLog.out("start play video");
        //暂停其他音乐
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        mContext.sendBroadcast(i);
        if (mMediaPlayer == null) {//第一次进入，需要创建
            try {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setOnPreparedListener(mPreparedListener);
                mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
                mIsPrepared = false;
                mDuration = -1;
                mMediaPlayer.setOnCompletionListener(mCompletionListener);
                mMediaPlayer.setOnErrorListener(mErrorListener);
                mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
                mCurrentBufferPercentage = 0;
                mMediaPlayer.setDataSource(mContext, mUri);
                mMediaPlayer.setDisplay(mSurfaceHolder);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setScreenOnWhilePlaying(true);
                mMediaPlayer.prepareAsync();
            } catch (IOException ex) {
                PictureAirLog.d(TAG, "Unable to open content: " + mUri + ex);
            } catch (IllegalArgumentException ex) {
                PictureAirLog.d(TAG, "Unable to open content: " + mUri + ex);
            }
        } else {//没有destroy的情况下，再次进入
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMyChangeLinstener.needResumePlayVideo();
        }
    }

    MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
            new MediaPlayer.OnVideoSizeChangedListener() {

                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

                    PictureAirLog.d(TAG, "===> onVideoSizeChanged");

                    mVideoWidth = mp.getVideoWidth();
                    mVideoHeight = mp.getVideoHeight();

                    if (mMyChangeLinstener != null) {
                        mMyChangeLinstener.onSizeChanged();
                    }

                    if (mVideoWidth != 0 && mVideoHeight != 0) {
                        getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                    }
                }
            };

    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            PictureAirLog.d(TAG, "===> onPrepared");
            mIsPrepared = true;
            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                    if (mSeekWhenPrepared != 0) {
                        mMediaPlayer.seekTo(mSeekWhenPrepared);
                        mSeekWhenPrepared = 0;
                    }
                    if (mStartWhenPrepared) {
                        mStartWhenPrepared = false;
                    }
                }
            } else {
                if (mSeekWhenPrepared != 0) {
                    mMediaPlayer.seekTo(mSeekWhenPrepared);
                    mSeekWhenPrepared = 0;
                }
                if (mStartWhenPrepared) {
                    mStartWhenPrepared = false;
                }
            }
        }
    };

    private OnCompletionListener mCompletionListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            if (mOnCompletionListener != null) {
                mOnCompletionListener.onCompletion(mMediaPlayer);
            }
        }
    };

    private OnErrorListener mErrorListener = new OnErrorListener() {
        public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
            PictureAirLog.d(TAG, "===> Error: " + framework_err + "," + impl_err);
            if (mOnErrorListener != null) {
                mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err);
            }

            return true;
        }
    };

    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            PictureAirLog.d(TAG, "===> onBufferingUpdate: " + percent);
            mOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
            mCurrentBufferPercentage = percent;
        }
    };

    public void setOnBufferingUpdateListener(MediaPlayer.OnBufferingUpdateListener l) {
        mOnBufferingUpdateListener = l;
    }

    /**
     * Register a callback to be invoked when the media file
     * is loaded and ready to go.
     *
     * @param l The callback that will be run
     */
    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    /**
     * Register a callback to be invoked when the end of a media file
     * has been reached during playback.
     *
     * @param l The callback that will be run
     */
    public void setOnCompletionListener(OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    /**
     * Register a callback to be invoked when an error occurs
     * during playback or setup.  If no listener is specified,
     * or if the listener returned false, VideoView will inform
     * the user of any errors.
     *
     * @param l The callback that will be run
     */
    public void setOnErrorListener(OnErrorListener l) {
        mOnErrorListener = l;
    }

    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            PictureAirLog.d(TAG, "===> surfaceChanged " + format + " ---" + w + " ===" + h);
            mSurfaceWidth = w;
            mSurfaceHeight = h;
        }

        public void surfaceCreated(SurfaceHolder holder) {
            PictureAirLog.d(TAG, "===> surfaceCreated");
            mSurfaceHolder = holder;
            openVideo();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            PictureAirLog.d(TAG, "===> surfaceDestroyed");
            mSurfaceHolder = null;
        }
    };

    public void start() {
        PictureAirLog.d(TAG, " start play video");
        if (mMediaPlayer != null && mIsPrepared) {
            mMediaPlayer.start();
            mStartWhenPrepared = false;
        } else {
            mStartWhenPrepared = true;
        }
    }

    public void pause() {
        if (mMediaPlayer != null && mIsPrepared) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }
        mStartWhenPrepared = false;
    }

    public int getDuration() {
        if (mMediaPlayer != null && mIsPrepared) {
            if (mDuration > 0) {
                return mDuration;
            }
            mDuration = mMediaPlayer.getDuration();
            return mDuration;
        }
        mDuration = -1;
        return mDuration;
    }

    public int getCurrentPosition() {
        if (mMediaPlayer != null && mIsPrepared) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void seekTo(int msec) {
        PictureAirLog.d(TAG, "seekTo :" + msec);
        if (mMediaPlayer != null && mIsPrepared) {
            mMediaPlayer.seekTo(msec);
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    public boolean isPlaying() {
        if (mMediaPlayer != null && mIsPrepared) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }
}