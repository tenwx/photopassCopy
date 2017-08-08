package com.pictureair.photopassCopy.widget.videoPlayer;

/**
 * Created by bauer_bao on 16/9/7.
 */
public interface OnVideoPlayerViewEventListener {

    void setVideoScale(int flag);//设置视频播放尺寸

    void setControllerVisible(boolean visible);//设置控制栏可见

    void onError();//加载视频失败

}
