package com.pictureair.photopass.widget;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.mob.tools.utils.UIHandler;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.GlideUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.UmengUtil;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;

import cn.sharesdk.facebook.Facebook;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.instagram.Instagram;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.twitter.Twitter;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;
import cn.sharesdk.wechat.moments.WechatMoments.ShareParams;

import static com.mob.tools.utils.R.getStringRes;

/**
 * 此控件负责photo页面中menu下拉菜单的内容
 */
public class SharePop extends PopupWindow implements OnClickListener, PlatformActionListener, Callback {
    private final String TAG = "SharePop";
    private static final int MSG_ACTION_CCALLBACK = 2;
    private static final int MSG_CANCEL_NOTIFY = 3;
    public static final int TWITTER = 40;
    public static final int SHOW_DIALOG = 41;
    public static final int DISMISS_DIALOG = 42;
    private Context context;
    private LayoutInflater inflater;
    private View defaultView;
    private TextView wechat, wechatMoments, qq, qqzone, sina, facebook, twitter;
    private TextView sharecancel;
    private String imagePath, imageUrl, thumbnailUrl, shareUrl;
    private boolean isOnline;
    private boolean isVideo;
    private int isEncrypted;
    private String sharePlatform;
    private Handler handler;
    private String shareType; // 分享类型，判断是 什么分享平台。 微信：1，qqzone：2，sina：3，twitter

    private String photoId;
    private String shareFileType;//必须为：photo、userInfo、product、video
    public static final String SHARE_PHOTO_TYPE = "photo";
    public static final String SHARE_VIDEO_TYPE = "video";

    public SharePop(Context context) {
        super(context);
        this.context = context;
        initPopupWindow();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case API1.GET_SHARE_URL_SUCCESS:
                    //拿到shareUrl
                    JSONObject shareInfo = JSONObject.parseObject(msg.obj.toString());
                    shareUrl = shareInfo.getString("shareUrl");
                    PictureAirLog.d(TAG, "tokenid----->" + MyApplication.getTokenId());
                    PictureAirLog.e(TAG, "拿到了分享链接：" + shareUrl);

                    if (isOnline && !isVideo && (msg.arg1 == R.id.wechat || msg.arg1 == R.id.wechat_moments || msg.arg1 == R.id.sina) && isEncrypted == 1) {//如果是微信分享，并且分享的是网络图片，并且有加密
                        API1.getNewPhotosInfo(MyApplication.getTokenId(), photoId, msg.arg1, mHandler);

                    } else {
                        startShare(msg.arg1);

                    }
                    break;

                case API1.GET_SHORT_URL_SUCCESS://拿到了短链接
                    JSONObject shortUrlInfo = (JSONObject)msg.obj;
                    if (shortUrlInfo.containsKey("shortUrl")) {
                        shareUrl = shortUrlInfo.getString("shortUrl");
                    }
                    PictureAirLog.out("result--->" + shareUrl);
                    startShare(msg.arg1);
                    break;

                case API1.GET_NEW_PHOTOS_INFO_SUCCESS:
                    PhotoInfo photoInfo = (PhotoInfo) msg.obj;
                    imageUrl = photoInfo.getPhotoThumbnail_1024();
                    startShare(msg.arg1);
                    break;

                case API1.GET_SHORT_URL_FAILED:
                case API1.GET_NEW_PHOTOS_INFO_FAILED:
                case API1.GET_SHARE_URL_FAILED:
                    PictureAirLog.d(TAG, "error--" + msg.arg1);
                    //获取url失败，1.通知notify，2、关闭sdk
                    dismissDialog();
                    shareUrl = null;
                    int resId = getStringRes(context, "http_error_code_401");
                    if (resId > 0) {
                        showNotification(2000, context.getString(resId));
                    }
                    ShareSDK.stopSDK();
                    break;
                default:
                    break;
            }
        }
    };

    private void initPopupWindow() {
        ShareSDK.initSDK(context);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        defaultView = inflater.inflate(R.layout.share_dialog, null);
        defaultView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        setContentView(defaultView);
        setWidth(LayoutParams.MATCH_PARENT);
        setHeight(LayoutParams.WRAP_CONTENT);
        setBackgroundDrawable(new ColorDrawable(context.getResources().getColor(android.R.color.transparent)));
        setAnimationStyle(R.style.from_bottom_anim);
        setFocusable(true);
        setOutsideTouchable(true);

        wechat = (TextView) defaultView.findViewById(R.id.wechat);
        wechatMoments = (TextView) defaultView.findViewById(R.id.wechat_moments);
        qq = (TextView) defaultView.findViewById(R.id.qq);
        qqzone = (TextView) defaultView.findViewById(R.id.qqzone);
        sina = (TextView) defaultView.findViewById(R.id.sina);
        facebook = (TextView) defaultView.findViewById(R.id.facebook);
        twitter = (TextView) defaultView.findViewById(R.id.twitter);
        sharecancel = (TextView) defaultView.findViewById(R.id.share_cancel);

        wechat.setOnClickListener(this);
        wechatMoments.setOnClickListener(this);
        qq.setOnClickListener(this);
        qqzone.setOnClickListener(this);
        sina.setOnClickListener(this);
        twitter.setOnClickListener(this);
        facebook.setOnClickListener(this);
        sharecancel.setOnClickListener(this);

    }

    @Override
    public void dismiss() {
        // TODO Auto-generated method stub
        super.dismiss();
    }

    /**
     * 设置需要分享的信息
     *
     * @param photoInfo
     * @param handler
     */
    public void setshareinfo(PhotoInfo photoInfo, Handler handler) {
        isOnline = photoInfo.getIsOnLine() == 1;
        isVideo = photoInfo.getIsVideo() == 1;
        if (isOnline || isVideo) {//网络或者视频
            imagePath = null;
            imageUrl = photoInfo.getPhotoThumbnail_1024();
            thumbnailUrl = photoInfo.getPhotoThumbnail_128();
            photoId = photoInfo.getPhotoId();
            isEncrypted = photoInfo.getIsEnImage();

        } else {//本地
            imagePath = photoInfo.getPhotoOriginalURL();
            imageUrl = null;
            thumbnailUrl = null;
            photoId = null;
            isEncrypted = 0;
        }

        if (isVideo) {
            shareFileType = SHARE_VIDEO_TYPE;
        } else {
            shareFileType = SHARE_PHOTO_TYPE;
        }

        PictureAirLog.d(TAG, "imagePath" + imagePath);
        PictureAirLog.d(TAG, "imageurl" + imageUrl);
        PictureAirLog.d(TAG, "thumbnail" + thumbnailUrl);
        this.handler = handler;
    }


    /**
     * 微信分享，不支持图文分享，只能分享图片，或者图片以链接的形式分享出去，但是都不能添加文字
     *
     * @param context
     * @param imagePath 本地图片路径
     * @param imageUrl  网络图片url
     * @param isOnline      判断是否是本地还是网络，类型有“local”“online”
     */
    private void wechatmonentsShare(Context context, String imagePath, String thumbnailUrl,
                                    String imageUrl, String shareUrl, boolean isOnline) {
        Platform platform = ShareSDK.getPlatform(context, WechatMoments.NAME);
        platform.setPlatformActionListener(this);// 如果没有通过审核，这个监听没有什么作用
        ShareParams shareParams = new ShareParams();
        shareParams.title = context.getString(R.string.share_app_name);
        if (isVideo) {
            shareParams.shareType = Platform.SHARE_VIDEO;
            shareParams.imageUrl = thumbnailUrl;
            shareParams.url = imageUrl;// share_webpage的时候需要这个参数
        } else {
            // 本地图片可以
            if (!isOnline) {// 本地图片
                shareParams.shareType = Platform.SHARE_IMAGE;// 只分享图片，这个时候不需要url属性。
                shareParams.imagePath = imagePath;
            } else {// 网络图片
                shareParams.shareType = Platform.SHARE_IMAGE;// 如果以网页的形式分享图片，则用SHARE_WEBPAGE
                shareParams.imageUrl = imageUrl;
            }
        }
        platform.share(shareParams);
    }

    /**
     * 微信好友分享，不支持图文分享，只能分享图片，或者图片以链接的形式分享出去，但是都不能添加文字
     *
     * @param context
     * @param imagePath 本地图片路径
     * @param imageUrl  网络图片url
     * @param isOnline      判断是否是本地还是网络，类型有“local”“online”
     */
    private void wechatFriendsShare(Context context, String imagePath, String thumbnailUrl,
                                    String imageUrl, String shareUrl, boolean isOnline) {
        Platform platform = ShareSDK.getPlatform(context, Wechat.NAME);
        platform.setPlatformActionListener(this);// 如果没有通过审核，这个监听没有什么作用
        ShareParams shareParams = new ShareParams();
        shareParams.title = context.getString(R.string.share_app_name);
        if (isVideo) {
            // 如果以网页的形式分享图片，则用SHARE_WEBPAGE
            shareParams.shareType = Platform.SHARE_VIDEO;
            shareParams.imageUrl = thumbnailUrl;
            shareParams.url = imageUrl;// share_webpage的时候需要这个参数
            shareParams.text = context.getResources().getString(R.string.share_text);
        } else {
            // 本地图片可以
            if (!isOnline) {// 本地图片
                shareParams.shareType = Platform.SHARE_IMAGE;// 只分享图片，这个时候不需要url属性。
                shareParams.imagePath = imagePath;
            } else {// 网络图片
                shareParams.shareType = Platform.SHARE_IMAGE;// 如果以网页的形式分享图片，则用SHARE_WEBPAGE
                shareParams.imageUrl = imageUrl;
            }
        }
        platform.share(shareParams);
    }

    /**
     * QZone分享，是根据qq平台去分享到空间
     *
     * @param context
     * @param imagePath 本地图片路径
     * @param imageUrl  网络图片url
     * @param isOnline      判断是否是本地还是网络，类型有“local”“online”
     */
    private void qzoneShare(Context context, String imagePath, String thumbnailUrl, String imageUrl,
                            String shareUrl, boolean isOnline) {
        Platform platform = ShareSDK.getPlatform(context, QZone.NAME);
        if (platform.isClientValid()) {
            platform.setPlatformActionListener(this);// 如果没有通过审核，这个监听没有什么作用
            cn.sharesdk.tencent.qzone.QZone.ShareParams shareParams = new cn.sharesdk.tencent.qzone.QZone.ShareParams();
            shareParams.title = context.getString(R.string.share_app_name);
            shareParams.text = context.getResources().getString(R.string.share_text);
            if (isVideo) {
                shareParams.imageUrl = thumbnailUrl;
                shareParams.titleUrl = imageUrl;
                shareParams.siteUrl = imageUrl;
            } else {
                if (!isOnline) {// 本地图片
                    shareParams.setImagePath(imagePath);
                    shareParams.setTitleUrl("http://www.disneyphotopass.com.cn");
                } else {// 网络图片
                    shareParams.imageUrl = thumbnailUrl;
                    shareParams.titleUrl = shareUrl;
                    shareParams.siteUrl = shareUrl;
                }
            }
            shareParams.site = context.getString(R.string.share_app_name);
            platform.share(shareParams);
        } else {
            dismissDialog();
            showNotification(2000, context.getString(R.string.share_failure_qzone));
        }
    }

    /**
     * qq好友分享
     *
     * @param context
     * @param imagePath
     * @param imageUrl
     * @param shareUrl
     * @param isOnline
     */
    private void qqShare(Context context, String imagePath, String thumbnailUrl, String imageUrl,
                         String shareUrl, boolean isOnline) {
        // TODO Auto-generated method stub
        Platform platform = ShareSDK.getPlatform(context, QQ.NAME);
        if (platform.isClientValid()) {
            platform.setPlatformActionListener(this);// 如果没有通过审核，这个监听没有什么作用
            cn.sharesdk.tencent.qzone.QZone.ShareParams shareParams = new cn.sharesdk.tencent.qzone.QZone.ShareParams();
            shareParams.title = context.getString(R.string.share_app_name);
            shareParams.text = context.getResources().getString(R.string.share_text);
            if (isVideo) {
                PictureAirLog.d(TAG, "VIDEO");
                shareParams.imageUrl = thumbnailUrl;
                shareParams.titleUrl = imageUrl;
            } else {
                if (!isOnline) {// 本地图片
                    PictureAirLog.d(TAG, "LOCAL");
                    shareParams.setImagePath(imagePath);
                    shareParams.setTitleUrl("http://www.disneyphotopass.com.cn");
                } else {// 网络图片
                    PictureAirLog.d(TAG, "PHOTO");
                    shareParams.imageUrl = thumbnailUrl;
                    shareParams.titleUrl = shareUrl;
                }
            }
            platform.share(shareParams);
        } else {
            dismissDialog();
            showNotification(2000, context.getString(R.string.share_failure_qzone));
        }
    }

    /**
     * sina分享
     *
     * @param context
     * @param imagePath 本地图片路径
     * @param imageUrl  网络图片URL
     * @param shareUrl
     * @param isOnline      本地还是网络的标记
     */
    private void sinaShare(Context context, String imagePath, String thumbnailUrl, String imageUrl,
                           String shareUrl, boolean isOnline) {
        Platform platform = ShareSDK.getPlatform(context, SinaWeibo.NAME);
//        platform.SSOSetting(false);// 未审核，必须要关闭SSO，true代表关闭。审核过了之后才要设置为false，新版本，貌似已经不需要这个了
        platform.setPlatformActionListener(this);// 如果没有通过审核，这个监听没有什么作用
        cn.sharesdk.sina.weibo.SinaWeibo.ShareParams shareParams = new cn.sharesdk.sina.weibo.SinaWeibo.ShareParams();

        if (isVideo) {
            shareParams.text = context.getString(R.string.share_text) + shareUrl;
            shareParams.setImageUrl(thumbnailUrl);
        } else {
            if (!isOnline) {// 本地图片
                shareParams.text = context.getString(R.string.share_text);
                shareParams.setImagePath(imagePath);
            } else {// 网络图片
                shareParams.text = context.getString(R.string.share_text);
                shareParams.setImageUrl(imageUrl);
            }
        }
        platform.share(shareParams);
    }

    /**
     * facebook分享
     *
     * @param context
     * @param imagePath 本地图片路径
     * @param imageUrl  网络图片URL
     * @param shareUrl  文本评论
     * @param isOnline      本地还是网络的标记
     */
    private void facebookShare(Context context, String imagePath,  String thumbnailUrl,
                               String imageUrl, String shareUrl, boolean isOnline) {
        Platform platform = ShareSDK.getPlatform(context, Facebook.NAME);
        if (platform.isClientValid()) {
            platform.setPlatformActionListener(this);// 如果没有通过审核，这个监听没有什么作用
            cn.sharesdk.facebook.Facebook.ShareParams shareParams = new cn.sharesdk.facebook.Facebook.ShareParams();
            shareParams.text = context.getString(R.string.share_app_name);
            if (!isOnline) {// 本地图片
                shareParams.setImagePath(imagePath);
            } else {// 网络图片，未审核的不支持网络图片，所以只能把链接分享出来
                shareParams.setImageUrl(thumbnailUrl);
            }
            platform.share(shareParams);
        } else {
            dismissDialog();
            showNotification(2000, context.getString(R.string.share_failure_facebook));
        }
    }

    /**
     * twitter分享，只能以网页的形式进行分享，同时，本地图片不支持过大的文件
     *
     * @param context
     * @param imagePath
     * @param imageUrl
     * @param isOnline
     */
    private void twitterShare(Context context, String imagePath, String thumbnailUrl,
                              String imageUrl, String shareUrl, boolean isOnline) {
        Platform platform = ShareSDK.getPlatform(context, Twitter.NAME);
        platform.setPlatformActionListener(this);
        cn.sharesdk.twitter.Twitter.ShareParams shareParams = new cn.sharesdk.twitter.Twitter.ShareParams();
        if (!isOnline) {
            shareParams.text = context.getString(R.string.share_text);
            shareParams.setImagePath(imagePath);
        } else {
            shareParams.text = shareUrl;
        }
        platform.share(shareParams);
    }

    /**
     * instagram
     * 分享图文	 ImagePath
     * imageUrl
     * 分享视频	 FilePath(/sdcard/视屏文件)
     *
     * @param context
     * @param imagePath 本地图片路径
     * @param imageUrl  网络图片url
     * @param isOnline      判断是否是本地还是网络，类型有“local”“online”
     */
    private void instagramShare(Context context, String imagePath, String thumbnailUrl, String imageUrl,
                                String shareUrl, boolean isOnline) {
        PictureAirLog.i(TAG, "---> instagram分享");
        Platform platform = ShareSDK.getPlatform(context, Instagram.NAME);
        if (platform.isClientValid()) {
            PictureAirLog.i(TAG, "---> instagram分享---用户已经安装客户端");
            platform.setPlatformActionListener(this);// 如果没有通过审核，这个监听没有什么作用
            cn.sharesdk.instagram.Instagram.ShareParams shareParams = new cn.sharesdk.instagram.Instagram.ShareParams();
            shareParams.text = context.getResources().getString(
                    R.string.share_text);
            if (!isOnline) {// 本地图片
                shareParams.setImagePath(imagePath);
            } else {// 网络图片
                shareParams.imageUrl = thumbnailUrl;
            }
            platform.share(shareParams);
        } else {
            PictureAirLog.i(TAG, "---> instagram分享---用户没有安装客户端");
            dismissDialog();
            showNotification(2000, context.getString(R.string.share_failure_instagram));
        }
    }


    /**
     * 开始分享
     * @param id
     */
    private void startShare(int id) {

        switch (id) {
            case R.id.wechat_moments:
                shareType = Common.EVENT_ONCLICK_SHARE_WECHAT_MOMENTS;
                sharePlatform = "wechat moments";
                wechatmonentsShare(context, imagePath, thumbnailUrl, imageUrl, shareUrl, isOnline);
                break;

            case R.id.wechat:
                shareType = Common.EVENT_ONCLICK_SHARE_WECHAT;
                sharePlatform = "wechat";
                wechatFriendsShare(context, imagePath, thumbnailUrl, imageUrl, shareUrl, isOnline);
                break;

            case R.id.qq:
                shareType = Common.EVENT_ONCLICK_SHARE_QQ;
                sharePlatform = "qq";
                if (!isOnline && !isVideo) {// 本地
                    createThumbNail(id);
                } else {
                    qqShare(context, imagePath, thumbnailUrl, imageUrl, shareUrl, isOnline);
                }
                break;

            case R.id.qqzone:
                shareType = Common.EVENT_ONCLICK_SHARE_QQZONE;
                sharePlatform = "qzone";
                if (!isOnline && !isVideo) {
                    createThumbNail(id);
                } else {
                    qzoneShare(context, imagePath, thumbnailUrl, imageUrl, shareUrl, isOnline);
                }
                break;

            case R.id.sina:
                shareType = Common.EVENT_ONCLICK_SHARE_SINA_WEIBO;
                sharePlatform = "sina";
                if (!isOnline && !isVideo) {// 本地图片
                    createThumbNail(id);
                } else {
                    sinaShare(context, imagePath, thumbnailUrl, imageUrl, shareUrl, isOnline);
                }
                break;

            case R.id.facebook:
                PictureAirLog.d(TAG, "fb on click");
                shareType = Common.EVENT_ONCLICK_SHARE_FACEBOOK;
                sharePlatform = "facebook";
                facebookShare(context, imagePath, thumbnailUrl, imageUrl, shareUrl, isOnline);
                break;

            case R.id.twitter:
                shareType = Common.EVENT_ONCLICK_SHARE_TWITTER;
                sharePlatform = "twitter";
                handler.sendEmptyMessage(TWITTER);
                if (!isOnline && !isVideo) {// 本地
                    createThumbNail(id);
                } else {
                    twitterShare(context, imagePath, thumbnailUrl, imageUrl, shareUrl, isOnline);
                }
                break;

            default:
                break;
        }
        //友盟统计分享。
        UmengUtil.onEvent(context, shareType);
    }

    @Override
    public void onClick(View v) {
        // 显示进度条，等待app打开
        handler.sendEmptyMessage(SHOW_DIALOG);
        switch (v.getId()) {
            case R.id.wechat_moments:
            case R.id.wechat:
            case R.id.qq:
            case R.id.qqzone:
            case R.id.facebook:
            case R.id.twitter:
                PictureAirLog.d(TAG, "share on click--->");
                if (!isOnline || isVideo) {//本地图片或者视频，都直接开始分享
                    PictureAirLog.d(TAG, "local or video");
                    startShare(v.getId());
                } else {//网络图片，需要获取shareURL
                    PictureAirLog.d(TAG, "online get share url");
                    API1.getShareUrl(photoId, shareFileType, v.getId(), mHandler);
                }
                break;

            case R.id.sina:
                PictureAirLog.d(TAG, "share on click--->");
                if (isVideo) {//视频需要直接获取短连接
                    API1.getShortUrl(imageUrl, v.getId(), mHandler);

                } else if (!isOnline) {//本地图片，直接开始分享
                    PictureAirLog.d(TAG, "local");
                    startShare(v.getId());

                } else {//网络图片，需要获取shareURL
                    PictureAirLog.d(TAG, "online get share url");
                    API1.getShareUrl(photoId, shareFileType, v.getId(), mHandler);

                }
                break;

            case R.id.share_cancel:
                dismissDialog();
                ShareSDK.stopSDK();
                break;

            default:
                break;
        }
        if (isShowing()) {
            dismiss();
        }
    }

    private void createThumbNail(final int id) {
        // TODO Auto-generated method stub
        PictureAirLog.d(TAG, "share pop---->start create thumbnail");
        GlideUtil.load(context, GlideUtil.getFileUrl(imagePath), new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap loadedImage, GlideAnimation<? super Bitmap> glideAnimation) {
                PictureAirLog.d(TAG, "share pop---->get the bitmap");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                loadedImage.compress(Bitmap.CompressFormat.JPEG, 20, baos);
                byte[] datas = baos.toByteArray();
                File shareFile = new File(Common.SHARE_PATH);
                if (!shareFile.exists()) {
                    shareFile.mkdirs();
                }
                shareFile = new File(Common.SHARE_PATH + AppUtil.getReallyFileName(imagePath, 0));
                if (!shareFile.exists()) {
                    PictureAirLog.d(TAG, "share pop---->start deal thumbnail");
                    BufferedOutputStream stream = null;
                    try {
                        shareFile.createNewFile();
                        FileOutputStream fStream = new FileOutputStream(shareFile);
                        stream = new BufferedOutputStream(fStream);
                        stream.write(datas);
                    } catch (Exception e) {
                        // TODO: handle exception
                    } finally {
                        if (stream != null) {
                            try {
                                stream.close();
                            } catch (Exception e2) {
                                // TODO: handle exception
                            }
                        }
                    }
                }

                switch (id) {
                    case R.id.twitter:
                        // 生成缩略图成功， 需要开始分享
                        twitterShare(context, shareFile.toString(), thumbnailUrl, imageUrl, shareUrl, isOnline);
                        break;

                    case R.id.sina:
                        sinaShare(context, shareFile.toString(), thumbnailUrl, imageUrl, shareUrl, isOnline);
                        break;

                    case R.id.qq:
                        qqShare(context, shareFile.toString(), thumbnailUrl, imageUrl, shareUrl, isOnline);
                        break;

                    case R.id.qqzone:
                        qzoneShare(context, shareFile.toString(), thumbnailUrl, imageUrl, shareUrl, isOnline);
                        break;

                    default:
                        break;
                }
            }
        });
    }

    /**
     * 将开始程序的对话框消失掉
     */
    private void dismissDialog() {
        handler.sendEmptyMessage(DISMISS_DIALOG);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_ACTION_CCALLBACK: {
                switch (msg.arg1) {
                    case 1: {
                        // 成功
                        int resId = getStringRes(context, "share_completed");
                        if (resId > 0) {
                            showNotification(2000, context.getString(resId));
                        }
                        ShareSDK.stopSDK();
                    }
                    break;
                    case 2: {
                        // 失败
                        String expName = msg.obj.getClass().getSimpleName();
                        if ("WechatClientNotExistException".equals(expName)
                                || "WechatTimelineNotSupportedException".equals(expName)
                                || "WechatFavoriteNotSupportedException".equals(expName)) {
                            int resId = getStringRes(context, "share_failure_wechat");
                            if (resId > 0) {
                                showNotification(2000, context.getString(resId));
                            }
                        } else if ("QQClientNotExistException".equals(expName)) {
                            int resId = getStringRes(context, "share_failure_qzone");
                            if (resId > 0) {
                                showNotification(2000, context.getString(resId));
                            }
                        } else {
                            int resId = getStringRes(context, "share_failed");
                            if (resId > 0) {
                                showNotification(2000, context.getString(resId));
                            }
                        }
                        ShareSDK.stopSDK();
                    }
                    break;

                    case 3: {
                        // 取消
                        // PictureAirLog.d("3333");
                        int resId = getStringRes(context, "share_canceled");
                        if (resId > 0) {
                            // PictureAirLog.d("3333"+context.getString(resId));
                            showNotification(2000, context.getString(resId));
                        }
                        ShareSDK.stopSDK();
                    }
                    break;
                    default:
                        break;
                }
            }
            break;
            case MSG_CANCEL_NOTIFY: {
                NotificationManager nm = (NotificationManager) msg.obj;
                if (nm != null) {
                    nm.cancel(msg.arg1);
                }
            }
            break;
        }
        return false;
    }

    @Override
    public void onCancel(Platform arg0, int arg1) {
        dismissDialog();
        Message msg = new Message();
        msg.what = MSG_ACTION_CCALLBACK;
        msg.arg1 = 3;
        msg.arg2 = arg1;
        msg.obj = arg0;
        UIHandler.sendMessage(msg, this);
    }

    @Override
    public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {
        dismissDialog();
        Message msg = new Message();
        msg.what = MSG_ACTION_CCALLBACK;
        msg.arg1 = 1;
        msg.arg2 = arg1;
        msg.obj = arg0;

        UIHandler.sendMessage(msg, this);
        //统计分享成功
        onEventUmeng(arg0);
        if (sharePlatform.equals("twitter")) {
            arg0.removeAccount(true);
        }
    }

    @Override
    public void onError(Platform arg0, int arg1, Throwable arg2) {
        // TODO Auto-generated method stub
        // System.out.println("error");
        arg2.printStackTrace();
        dismissDialog();
        Message msg = new Message();
        msg.what = MSG_ACTION_CCALLBACK;
        msg.arg1 = 2;
        msg.arg2 = arg1;
        msg.obj = arg2;
        UIHandler.sendMessage(msg, this);

        // 分享失败的统计
        ShareSDK.logDemoEvent(4, arg0);
    }

    /**
     * 分享完成后统计分享数据
     *
     * @param platform
     */
    private void onEventUmeng(Platform platform) {
        if (platform == null) {
            return;
        }
        String typeName = platform.getName();
        String eventName = null;
        if (typeName.equals("SinaWeibo")) {
            eventName = Common.EVENT_SHARE_SINA_WEIBO_FINISH;
        } else if (typeName.equals("QZone")) {
            eventName = Common.EVENT_SHARE_QQZONE_FINISH;

        } else if (typeName.equals("QZone")) {
            eventName = Common.EVENT_SHARE_QQZONE_FINISH;

        } else if (typeName.equals("Wechat")) {
            eventName = Common.EVENT_SHARE_WECHAT_FINISH;

        } else if (typeName.equals("WechatMoments")) {
            eventName = Common.EVENT_SHARE_WECHAT_MOMENTS_FINISH;

        } else if (typeName.equals("Facebook")) {
            eventName = Common.EVENT_SHARE_FACEBOOK_FINISH;

        } else if (typeName.equals("Twitter")) {
            eventName = Common.EVENT_SHARE_TWITTER_FINISH;
        } else if (typeName.equals("Qq")) {
            eventName = Common.EVENT_SHARE_QQ_FINISH;
        }

        // 分享完成统计事件
        if (!TextUtils.isEmpty(eventName)) {
            UmengUtil.onEvent(context, eventName);
        }

    }

    // 在状态栏提示分享操作
    private void showNotification(long cancelTime, String text) {
        try {
            Context app = context.getApplicationContext();
            NotificationManager nm = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
            final int id = Integer.MAX_VALUE / 13 + 1;
            nm.cancel(id);

//			PendingIntent pi = PendingIntent.getActivity(app, 0, new Intent(), 0);
            Notification notification = new NotificationCompat.Builder(app)
                    .setSmallIcon(AppUtil.getNotificationIcon())
                    .setAutoCancel(true)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(text)
                    .setWhen(System.currentTimeMillis())
                    .setTicker(text)
                    .build();//.setContentIntent(pi)
            notification.flags = Notification.FLAG_AUTO_CANCEL;//通知栏可以自动删除
            nm.notify(id, notification);

            if (cancelTime > 0) {
                Message msg = new Message();
                msg.what = MSG_CANCEL_NOTIFY;
                msg.obj = nm;
                msg.arg1 = id;
                UIHandler.sendMessageDelayed(msg, cancelTime, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
