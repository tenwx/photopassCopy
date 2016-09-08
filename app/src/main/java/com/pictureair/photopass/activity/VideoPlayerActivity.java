package com.pictureair.photopass.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.customDialog.PWDialog;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.service.DownloadService;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.PWToast;
import com.pictureair.photopass.widget.SharePop;
import com.pictureair.photopass.widget.videoPlayer.OnVideoPlayerViewEventListener;
import com.pictureair.photopass.widget.videoPlayer.PWVideoPlayerManagerView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;


/**
 * 播放视频（需要优化播放）
 *
 * @author bass
 */
public class VideoPlayerActivity extends BaseActivity implements OnClickListener,
        PWDialog.OnPWDialogClickListener, OnVideoPlayerViewEventListener {
    private final static String TAG = "VideoPlayerActivity";
    private final static int SCREEN_FULL = 0;
    private final static int SCREEN_DEFAULT = 1;
    private static final int UPDATE_UI = 2866;
    private RelativeLayout rlHead, rlBackground;
    private LinearLayout llEnd;
    private ImageView ivIsLove;
    private TextView videoDateTV;
    private PWVideoPlayerManagerView pwVideoPlayerManagerView;
    private PWToast myToast;
    private SharePop sharePop;

    private Context context;
    private LinearLayout llShare, llDownload;

    private int screenWidth = 0;
    private int screenHeight = 0;
    private PhotoInfo videoInfo;
    private int mNetWorkType;  //当前网络的状态
    private PWDialog pwDialog; //  对话框
    private PictureAirDbManager pictureAirDbManager;
    private boolean isOnline ;//网络true || 本地false
    private String videoPath;//视频本地路径 || 视频网络地址
    private int shareType = 0;
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

            case UPDATE_UI:
                Configuration cf = context.getResources().getConfiguration();
                int ori = cf.orientation;
                adjustScreenUI(ori == cf.ORIENTATION_LANDSCAPE);
                //更新收藏图标
                if (videoInfo.isLove == 1 || pictureAirDbManager.checkLovePhoto(videoInfo, SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ID, ""))) {
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
        initListener();// 所有按钮的事件˙
        getScreenSize();
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
        sharePop = new SharePop(context);
        pictureAirDbManager = new PictureAirDbManager(context);
        myToast = new PWToast(context);
        setTopLeftValueAndShow(R.drawable.back_white,true);
        String place = videoInfo.locationName;
        if (place == null || place.equals("null")) {
            place = "";
        }
        setTopTitleShow(place);
        ivIsLove = getTopRightImageView();
        rlBackground = (RelativeLayout) findViewById(R.id.rl_background);
        llShare = (LinearLayout) findViewById(R.id.ll_share);
        llDownload = (LinearLayout) findViewById(R.id.ll_download);

        rlHead = (RelativeLayout) findViewById(R.id.head);
        llEnd = (LinearLayout) findViewById(R.id.ll_end);
        pwVideoPlayerManagerView = (PWVideoPlayerManagerView) findViewById(R.id.video_player_pmv);

        videoDateTV = (TextView) findViewById(R.id.video_date);
        videoDateTV.setText(videoInfo.shootOn);

        videoPlayerHandler.sendEmptyMessage(UPDATE_UI);
    }

    private void initListener() {
        pwVideoPlayerManagerView.setOnClickListener(this);
        pwVideoPlayerManagerView.setOnVideoPlayerViewEventListener(this);
        llShare.setOnClickListener(this);
        llDownload.setOnClickListener(this);
    }

    private void getReallyVideoUrl(){
        String fileName = AppUtil.getReallyFileName(videoInfo.photoThumbnail_1024,1);
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
        screenHeight = ScreenUtil.getScreenHeight(context);
        screenWidth = ScreenUtil.getScreenWidth(context);
    }

    // 设置可以全屏
    @Override
    public void setVideoScale(int flag) {
        pwVideoPlayerManagerView.setVideoScale(screenWidth, screenHeight);
        switch (flag) {
            case SCREEN_FULL:
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                break;

            case SCREEN_DEFAULT:
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
        rlHead.setVisibility(isLandscape ? View.GONE : View.VISIBLE);
        llEnd.setVisibility(isLandscape ? View.GONE : View.VISIBLE);
        videoDateTV.setVisibility(isLandscape ? View.GONE : View.VISIBLE);
        rlBackground.setBackgroundColor(ContextCompat.getColor(this, isLandscape ? R.color.black : R.color.gray_light));
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

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.ll_share:
                sharePop.setshareinfo(null, videoInfo.shareURL, videoInfo.shareURL, "online", videoInfo.photoId, SharePop.SHARE_VIDEO_TYOE, 0, videoPlayerHandler);
                sharePop.showAtLocation(v, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                break;

            case R.id.ll_download:
                downloadVideo();
                break;

            case R.id.video_player_pmv:
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
            if (pwDialog == null) {
                pwDialog = new PWDialog(context)
                        .setPWDialogMessage(R.string.dialog_download_message)
                        .setPWDialogNegativeButton(R.string.dialog_cancel)
                        .setPWDialogPositiveButton(R.string.dialog_ok)
                        .setOnPWDialogClickListener(this)
                        .pwDialogCreate();
            }
            pwDialog.pwDilogShow();
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
            pictureAirDbManager.setPictureLove(videoInfo, SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ID, ""), false);
            videoInfo.isLove = 0;
            ivIsLove.setImageResource(R.drawable.discover_no_like);
        } else {
            PictureAirLog.d(TAG, "add love");
            pictureAirDbManager.setPictureLove(videoInfo, SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ID, ""), true);
            videoInfo.isLove = 1;
            ivIsLove.setImageResource(R.drawable.discover_like);
        }
        ((MyApplication) getApplication()).needScanFavoritePhotos = true;
        return true;
    }

    @Override
    protected void onPause() {
        pwVideoPlayerManagerView.pausedVideo();
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (sharePop != null) {
            PictureAirLog.out("sharePop not null");
            if (shareType != SharePop.TWITTER) {
                PictureAirLog.out("dismiss dialog");
                sharePop.dismissDialog();
            }
        }

        pwVideoPlayerManagerView.resumeVideo();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        pwVideoPlayerManagerView.stopVideo();
        videoPlayerHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void onPWDialogButtonClicked(int which, int dialogId) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            downloadPic();
        }
    }
}
