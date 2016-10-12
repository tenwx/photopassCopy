package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopass.GalleryWidget.GalleryViewPager;
import com.pictureair.photopass.GalleryWidget.PhotoEventListener;
import com.pictureair.photopass.GalleryWidget.UrlPagerAdapter;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.PhotoDownLoadInfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.widget.PWToast;
import com.pictureair.photopass.widget.SharePop;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by pengwu on 16/7/29.
 */
public class DownloadPhotoPreviewActivity extends BaseActivity implements View.OnClickListener,PhotoEventListener {

    private TextView locationTextView;
    private GalleryViewPager mViewPager;
    private ImageView returnImageView;

    private ImageButton shareImgBtn;

    private PictureAirDbManager pictureAirDbManager;

    private RelativeLayout titleBar;
    private static final String TAG = "PreviewPhotoActivity";

    //图片显示框架
    private ArrayList<PhotoInfo> photolist;
    private int currentPosition;//记录当前预览照片的索引值

    /**
     * 是否是横屏模式
     */
    private boolean isLandscape = false;

    private RelativeLayout photoFraRelativeLayout;

    private SharePop sharePop;

    private int shareType = 0;

    private Handler previewPhotoHandler;

    private PWToast pwToast;

    public static final int NO_PHOTOS = 2323;
    private static final int CHECK_EXIST = 2324;

    @Override
    public void videoClick(int position) {

        PhotoInfo info = photolist.get(position);
        String fileName = AppUtil.getReallyFileName(info.photoPathOrURL,info.isVideo);
        PictureAirLog.e(TAG, "filename=" + fileName);
        File file = new File(Common.PHOTO_DOWNLOAD_PATH + "/" + fileName);
        if (!file.exists()){
            pwToast.setTextAndShow(R.string.photo_download_not_exists,PWToast.LENGTH_SHORT);
        }else {
            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra("from_story", info);
            startActivity(intent);
            overridePendingTransition(R.anim.activity_fadein, R.anim.activity_fadeout);
        }
    }

    @Override
    public void touchClear(boolean visible) {

    }

    private static class PreViewHandler extends Handler{
        private final WeakReference<DownloadPhotoPreviewActivity> mActivity;

        public PreViewHandler(DownloadPhotoPreviewActivity activity){
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

    private void dealHandler(Message msg) {
        switch (msg.what) {
            case CHECK_EXIST://开始检查是否存在
                if (msg.arg1 >= photolist.size() || msg.arg1 != currentPosition) {
                    break;
                }

                PhotoInfo info = photolist.get(msg.arg1);
                File file;
                if (info.isVideo == 1) {//视频
                    String fileName = AppUtil.getReallyFileName(info.photoPathOrURL,info.isVideo);
                    file = new File(Common.PHOTO_DOWNLOAD_PATH + "/" + fileName);
                } else {//照片
                    file = new File(info.photoPathOrURL);
                }

                //更新收藏图标
                if (msg.arg1 == currentPosition && file.exists()) {//数据库查询的数据是true，并且对应的index还是之前的位置
                    shareImgBtn.setVisibility(View.VISIBLE);
                } else {
                    shareImgBtn.setVisibility(View.INVISIBLE);
                }
                break;

            case SharePop.TWITTER:
                shareType = msg.what;
                break;

            case 7:
                mViewPager = (GalleryViewPager) findViewById(R.id.download_preview_viewer);
                UrlPagerAdapter pagerAdapter = new UrlPagerAdapter(DownloadPhotoPreviewActivity.this, photolist,1);
                pagerAdapter.setOnPhotoEventListener(this);
                mViewPager.setOffscreenPageLimit(2);
                mViewPager.setAdapter(pagerAdapter);
                mViewPager.setCurrentItem(currentPosition, true);
                //初始化底部索引按钮
                updateIndexTools(true);

                PictureAirLog.v(TAG, "----------------------->initing...3");

                mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                    @Override
                    public void onPageSelected(int arg0) {
                        //初始化每张图片的love图标
                        PictureAirLog.v(TAG, "----------------------->initing...4");
                        currentPosition = arg0;
                    }

                    @Override
                    public void onPageScrolled(int arg0, float arg1, int arg2) {

                    }

                    @Override
                    public void onPageScrollStateChanged(int arg0) {
                        PictureAirLog.v(TAG, "----------------------->initing...5");
                        if (arg0 == 0) {//结束滑动
                            updateIndexTools(false);
                        }
                    }
                });

                PictureAirLog.v(TAG, "----------------------->initing...6");
                break;
            case NO_PHOTOS://没有图片
                dismissPWProgressDialog();
                finish();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_photo_preview);
        PictureAirLog.out("oncreate start----");
        init();
        PictureAirLog.out("oncreate finish----");
    }

    private void init() {
        // TODO Auto-generated method stub
        previewPhotoHandler = new PreViewHandler(this);
        pictureAirDbManager = new PictureAirDbManager(this);
        sharePop = new SharePop(this);
        PictureAirLog.out("oncreate----->2");
        returnImageView = (ImageView) findViewById(R.id.download_preview_back);
        locationTextView = (TextView) findViewById(R.id.download_preview_title);
        photoFraRelativeLayout = (RelativeLayout) findViewById(R.id.download_preview_fra_layout);
        titleBar = (RelativeLayout) findViewById(R.id.download_preview_titlebar);
        shareImgBtn = (ImageButton) findViewById(R.id.download_preview_share);
        pwToast = new PWToast(MyApplication.getInstance());
        shareImgBtn.setOnClickListener(this);
        returnImageView.setOnClickListener(this);
        PictureAirLog.v(TAG, "----------------------->initing...1");

        Configuration cf = getResources().getConfiguration();
        int ori = cf.orientation;
        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
            isLandscape = true;
            landscapeOrientation();
        }
        showPWProgressDialog();
        getPreviewPhotos(getIntent());
    }

    private void getPreviewPhotos(Intent intent) {
        final Intent intent1 = intent;
        new Thread(){
            @Override
            public void run() {
                Bundle bundle = intent1.getExtras();
                currentPosition = bundle.getInt("position", 0);
                photolist = new ArrayList<PhotoInfo>();

                String userId = SPUtils.getString(DownloadPhotoPreviewActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ID, "");
                List<PhotoDownLoadInfo> photos = pictureAirDbManager.getPhotosOrderByTime(userId,"true");
                if (photos != null && photos.size() >0) {
                    for (int i=0;i<photos.size();i++) {
                        PhotoDownLoadInfo downLoadInfo = photos.get(i);

                        PhotoInfo photoInfo = new PhotoInfo();
                        photoInfo.photoId = downLoadInfo.getPhotoId();

                        String loadUrl;
                        //如果文件名过长导致无法保存，会将文件名进行MD5，数据库中保存在FailedTime字段，此处是对这种情况的特殊处理
                        if (TextUtils.isEmpty(downLoadInfo.getFailedTime())){
                            String fileName = AppUtil.getReallyFileName(downLoadInfo.getUrl(), downLoadInfo.getIsVideo());
                            loadUrl = Common.PHOTO_DOWNLOAD_PATH + fileName;
                        }else{
                            loadUrl = downLoadInfo.getFailedTime();
                        }
                        if (downLoadInfo.getIsVideo() == 0) {
                            photoInfo.photoPathOrURL = loadUrl;
                        }else{
                            photoInfo.photoPathOrURL = downLoadInfo.getUrl();
                        }
                        photoInfo.photoThumbnail = downLoadInfo.getPreviewUrl();
                        photoInfo.photoThumbnail_512 = downLoadInfo.getPhotoThumbnail_512();
                        photoInfo.photoThumbnail_1024 = downLoadInfo.getPhotoThumbnail_1024();
                        photoInfo.photoPassCode = "";
                        photoInfo.locationId = "";
                        photoInfo.locationName = "";
                        photoInfo.locationCountry = "";
                        photoInfo.shootOn = downLoadInfo.getShootTime();
                        photoInfo.shootTime = "";
                        photoInfo.shareURL = "";
                        photoInfo.isPayed = 1;
                        photoInfo.isVideo = downLoadInfo.getIsVideo();
                        photoInfo.isLove = 0;
                        photoInfo.fileSize = 0;
                        photoInfo.videoHeight = downLoadInfo.getVideoHeight();
                        photoInfo.videoWidth = downLoadInfo.getVideoWidth();
                        photoInfo.isHasPreset = 0;
                        photoInfo.isEncrypted = 1;
                        photoInfo.onLine = 1;//因为都是网络图片，也都有保存photoid，本地图还要压缩，并且上传，因此就以网络图处理
                        photoInfo.isChecked = 0;
                        photoInfo.isSelected = 0;
                        photoInfo.isUploaded = 0;
                        photoInfo.showMask = 0;
                        photoInfo.lastModify = 0l;
                        photoInfo.index = "";
                        photoInfo.isRefreshInfo = 0;

                        photolist.add(photoInfo);
                    }
                }

                if (photolist.size() == 0) {
                    /**
                     * 图片为空的情况。
                     * 如果收到删除数据推送，本地数据处理完毕，
                     * 但是用户正好点进图片预览，这个时候会出现为空的情况，需要finish
                     */
                    PictureAirLog.out("no photos need return");
                    previewPhotoHandler.sendEmptyMessage(NO_PHOTOS);
                    return;
                }

                if (currentPosition == -1) {
                    String url = bundle.getString("path","");
                    currentPosition = 0;
                    if (!TextUtils.isEmpty(url)){
                        String path = Common.PHOTO_DOWNLOAD_PATH+AppUtil.getReallyFileName(url,0);
                        for (int i = 0; i < photolist.size(); i++) {
                            if (path.equals(photolist.get(i).photoPathOrURL)){
                                currentPosition = i;
                            }
                        }
                    }
                }

                previewPhotoHandler.sendEmptyMessage(7);
            }
        }.start();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        PictureAirLog.e("DownloadPhotoPreviewActivity","onNewIntent");
        showPWProgressDialog();
        getPreviewPhotos(intent);
    }

    /**
     * 垂直模式
     */
    private void portraitOrientation() {
        isLandscape = false;
        titleBar.setVisibility(View.VISIBLE);
        if (mViewPager != null) {
            mViewPager.setBackgroundColor(getResources().getColor(R.color.pp_light_gray_background));
        }
        photoFraRelativeLayout.setBackgroundColor(getResources().getColor(R.color.pp_light_gray_background));
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 横屏模式
     */
    private void landscapeOrientation() {
        isLandscape = true;
        if (mViewPager != null) {
            mViewPager.setBackgroundColor(Color.BLACK);
        }
        photoFraRelativeLayout.setBackgroundColor(Color.BLACK);
        titleBar.setVisibility(View.GONE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mViewPager.resetImageView();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            PictureAirLog.out("landscape----->");
            landscapeOrientation();
        } else {
            PictureAirLog.out("portrait----->");
            portraitOrientation();
        }

        super.onConfigurationChanged(newConfig);

        String language = MyApplication.getInstance().getLanguageType();
        PictureAirLog.out("language------>" + language);
        Configuration config = getResources().getConfiguration();
        if (!language.equals("")) {//语言不为空
            if (language.equals(Common.ENGLISH)) {
                config.locale = Locale.US;
            } else if (language.equals(Common.SIMPLE_CHINESE)) {
                config.locale = Locale.SIMPLIFIED_CHINESE;
            }
        }
        PictureAirLog.out("new config---->" + config.locale);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        getResources().updateConfiguration(config, displayMetrics);
        PictureAirLog.out("update configuration done");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            doBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //退出app进行的判断，判断是否是栈中的唯一一个app，如果是，启动主页
    private void doBack() {
        // TODO Auto-generated method stub
        if (AppManager.getInstance().getActivityCount() == 1) {//一个activity的时候
            Intent intent = new Intent(this, MainTabActivity.class);
            startActivity(intent);
        }
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.download_preview_back:
                doBack();
                break;

            case R.id.download_preview_share:
                sharePop.setshareinfo(photolist.get(mViewPager.getCurrentItem()), previewPhotoHandler);
                sharePop.showAtLocation(v, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                break;

            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sharePop != null) {
            PictureAirLog.out("sharePop not null");
            if (shareType != SharePop.TWITTER) {
                PictureAirLog.out("dismiss dialog");
                sharePop.dismissDialog();
            }
        }
    }

    /**
     * 更新底部索引工具
     */
    private void updateIndexTools(boolean isOnCreate) {
        PictureAirLog.v(TAG, "updateIndexTools-------->" + currentPosition);
        previewPhotoHandler.obtainMessage(CHECK_EXIST, currentPosition, 0).sendToTarget();
        //更新序列号
        locationTextView.setText(String.format(getString(R.string.photo_index), currentPosition + 1,photolist.size()));

        PictureAirLog.out("set enable in other conditions");
        dismissPWProgressDialog();

        if (isLandscape) {//横屏模式
            if (mViewPager != null) {
                mViewPager.setBackgroundColor(Color.BLACK);
            }
        }
    }
}
