package com.pictureair.photopass.widget;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import com.mob.tools.utils.UIHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;
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
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.twitter.Twitter;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;
import cn.sharesdk.wechat.moments.WechatMoments.ShareParams;
import cn.smssdk.gui.CustomProgressDialog;

import static com.mob.tools.utils.R.getStringRes;

/**
 * 此控件负责photo页面中menu下拉菜单的内容
 */
public class SharePop extends PopupWindow implements OnClickListener,
		PlatformActionListener, Callback {
	private final String TAG ="SharePop";
	private static final int MSG_ACTION_CCALLBACK = 2;
	private static final int MSG_CANCEL_NOTIFY = 3;
	public static final int TWITTER = 40;
	private Context context;
	private LayoutInflater inflater;
	private View defaultView;
	private TextView wechat, wechatMoments, qq, qqzone, sina, facebook,
			twitter;
	private TextView sharecancel;
	private String imagePath, imageUrl, shareUrl, type;
	private String sharePlatform;
	private String shareId;
	private Handler handler;
	/**
	 * 打开程序进行分享比较耗时间，所以再点击分享的时候，就显示进度条
	 */
	private CustomProgressDialog dialog;

	private String shareType; // 分享类型，判断是 什么分享平台。 微信：1，qqzone：2，sina：3，twitter

	private String photoId ;
	private String shareFileType;//必须为：photo、userInfo、product、video
	public static final String SHARE_PHOTO_TYPE = "photo";
	public static final String SHARE_VIDEO_TYOE = "video";

	private MyToast myToast;

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
					shareId = shareInfo.getString("shareId");
					PictureAirLog.e(TAG,"拿到了分享链接："+shareUrl);

//					//测试回调代码
//					API1.shareCallBack(shareId, "qq");
//
					startShare(msg.arg1);
					break;

				case API1.GET_SHARE_URL_FAILED:
					//获取url失败，1.通知notify，2、关闭sdk
					if (dialog.isShowing()) {
						dialog.dismiss();
					}
					shareUrl = null;
					shareId = null;
					int resId = getStringRes(context, "http_error_code_401");
					if (resId > 0) {
						showNotification(2000, context.getString(resId));
					}
					ShareSDK.stopSDK();
					break;
			}
		}
	};

	private void initPopupWindow() {
		ShareSDK.initSDK(context);
		myToast = new MyToast(context);
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		defaultView = inflater.inflate(R.layout.share_dialog, null);
		defaultView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		setContentView(defaultView);
		setWidth(LayoutParams.MATCH_PARENT);
		setHeight(LayoutParams.WRAP_CONTENT);
		setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
		setAnimationStyle(R.style.from_bottom_anim);
		setFocusable(true);
		setOutsideTouchable(true);

		wechat = (TextView) defaultView.findViewById(R.id.wechat);
		wechatMoments = (TextView) defaultView
				.findViewById(R.id.wechat_moments);
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
	 * @param imagePath 本地图片路径
	 * @param imageUrl 网络图片url
	 * @param type 判断是否是本地还是网络，类型有“local”“online”
	 * @param photoId id
	 * @param shareFileType 视频还是照片
	 * @param handler
	 */
	public void setshareinfo(String imagePath, String imageUrl, String type, String photoId, String shareFileType, Handler handler) {
		this.imagePath = imagePath;
		this.imageUrl = imageUrl;
		this.type = type;
		this.photoId = photoId;
		this.handler = handler;
		this.shareFileType = shareFileType;
	}


	/**
	 * 微信分享，不支持图文分享，只能分享图片，或者图片以链接的形式分享出去，但是都不能添加文字
	 * 
	 * @param context
	 * @param imagePath
	 *            本地图片路径
	 * @param imageUrl
	 *            网络图片url
	 * @param type
	 *            判断是否是本地还是网络，类型有“local”“online”
	 */
	private void wechatmonentsShare(Context context, String imagePath,
			String imageUrl, String shareUrl, String type) {
		Platform platform = ShareSDK.getPlatform(context, WechatMoments.NAME);
		platform.setPlatformActionListener(this);// 如果没有通过审核，这个监听没有什么作用
		ShareParams shareParams = new ShareParams();
		shareParams.title = context.getString(R.string.share_text);
		// 本地图片可以
		if ("local".equals(type)) {// 本地图片
			shareParams.shareType = Platform.SHARE_IMAGE;// 只分享图片，这个时候不需要url属性。
			shareParams.imagePath = imagePath;
		} else if ("online".equals(type)) {// 网络图片
			shareParams.shareType = Platform.SHARE_WEBPAGE;// 以网页的形式分享图片
			shareParams.imageUrl = imageUrl;
			shareParams.url = shareUrl;// share_webpage的时候需要这个参数
		}
		platform.share(shareParams);
	}

	/**
	 * 微信好友分享，不支持图文分享，只能分享图片，或者图片以链接的形式分享出去，但是都不能添加文字
	 * 
	 * @param context
	 * @param imagePath
	 *            本地图片路径
	 * @param imageUrl
	 *            网络图片url
	 * @param type
	 *            判断是否是本地还是网络，类型有“local”“online”
	 */
	private void wechatFriendsShare(Context context, String imagePath,
			String imageUrl, String shareUrl, String type) {
		Platform platform = ShareSDK.getPlatform(context, Wechat.NAME);
		platform.setPlatformActionListener(this);// 如果没有通过审核，这个监听没有什么作用
		ShareParams shareParams = new ShareParams();
		shareParams.title = context.getString(R.string.share_text);
		// 本地图片可以
		if ("local".equals(type)) {// 本地图片
			shareParams.shareType = Platform.SHARE_IMAGE;// 只分享图片，这个时候不需要url属性。
			shareParams.imagePath = imagePath;
		} else if ("online".equals(type)) {// 网络图片
			shareParams.shareType = Platform.SHARE_WEBPAGE;// 以网页的形式分享图片
			// shareParams.imageUrl = imageUrl;
			shareParams.url = shareUrl;// share_webpage的时候需要这个参数
		}
		platform.share(shareParams);
	}

	/**
	 * QZone分享，是根据qq平台去分享到空间
	 * 
	 * @param context
	 * @param imagePath
	 *            本地图片路径
	 * @param imageUrl
	 *            网络图片url
	 * @param type
	 *            判断是否是本地还是网络，类型有“local”“online”
	 */
	private void qzoneShare(Context context, String imagePath, String imageUrl,
			String shareUrl, String type) {
		Platform platform = ShareSDK.getPlatform(context, QZone.NAME);
		if (platform.isClientValid()) {
			platform.setPlatformActionListener(this);// 如果没有通过审核，这个监听没有什么作用
			cn.sharesdk.tencent.qzone.QZone.ShareParams shareParams = new cn.sharesdk.tencent.qzone.QZone.ShareParams();
			shareParams.title = Common.SHARE_APP_NAME;
			shareParams.text = context.getResources().getString(
					R.string.share_text);
			if ("local".equals(type)) {// 本地图片
				shareParams.imagePath = imagePath;
				shareParams.titleUrl = "http://www.pictureair.com";
				// shareParams.siteUrl = "http://www.pictureair.com";
			} else if ("online".equals(type)) {// 网络图片
				shareParams.imageUrl = imageUrl;
				shareParams.titleUrl = shareUrl;
				shareParams.siteUrl = shareUrl;
			}
			shareParams.site = context.getString(R.string.app_name);
			platform.share(shareParams);
		} else {
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			showNotification(2000,
					context.getString(R.string.share_failure_qzone));
		}
	}

	/**
	 * qq好友分享
	 * 
	 * @param context
	 * @param imagePath
	 * @param imageUrl
	 * @param shareUrl
	 * @param type
	 */
	private void qqShare(Context context, String imagePath, String imageUrl,
			String shareUrl, String type) {
		// TODO Auto-generated method stub
		Platform platform = ShareSDK.getPlatform(context, QQ.NAME);
		if (platform.isClientValid()) {
			platform.setPlatformActionListener(this);// 如果没有通过审核，这个监听没有什么作用
			cn.sharesdk.tencent.qzone.QZone.ShareParams shareParams = new cn.sharesdk.tencent.qzone.QZone.ShareParams();
			shareParams.title = Common.SHARE_APP_NAME;
			shareParams.text = context.getResources().getString(
					R.string.share_text);
			if ("local".equals(type)) {// 本地图片
				shareParams.imagePath = imagePath;
				shareParams.titleUrl = "http://www.pictureair.com";
				// shareParams.siteUrl = "http://www.pictureair.com";
			} else if ("online".equals(type)) {// 网络图片
				shareParams.imageUrl = imageUrl;
				shareParams.titleUrl = shareUrl;
				// shareParams.siteUrl = shareUrl;
			}
			// shareParams.site = context.getString(R.string.app_name);
			platform.share(shareParams);
		} else {
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			showNotification(2000,
					context.getString(R.string.share_failure_qzone));
		}
	}

	/**
	 * sina分享
	 * 
	 * @param context
	 * @param imagePath 本地图片路径
	 * @param imageUrl 网络图片URL
	 * @param shareUrl
	 * @param type 本地还是网络的标记
	 *
	 */
	private void sinaShare(Context context, String imagePath, String imageUrl,
			String shareUrl, String type) {
		Platform platform = ShareSDK.getPlatform(context, SinaWeibo.NAME);

		platform.SSOSetting(false);// 未审核，必须要关闭SSO，true代表关闭。审核过了之后才要设置为false
		platform.setPlatformActionListener(this);// 如果没有通过审核，这个监听没有什么作用
		cn.sharesdk.sina.weibo.SinaWeibo.ShareParams shareParams = new cn.sharesdk.sina.weibo.SinaWeibo.ShareParams();
		shareParams.text = context.getString(R.string.share_text);
		shareParams.setTitleUrl(shareUrl);
		if ("local".equals(type)) {// 本地图片
			shareParams.imagePath = imagePath;
		} else if ("online".equals(type)) {// 网络图片，未审核的不支持网络图片，所以只能把链接分享出来
			shareParams.imageUrl = imageUrl;
			// shareParams.text = imageUrl;
		}
		platform.share(shareParams);
	}

	/**
	 * facebook分享
	 * 
	 * @param context
	 * @param imagePath 本地图片路径
	 * @param imageUrl 网络图片URL
	 * @param shareUrl 文本评论
	 * @param type 本地还是网络的标记
	 */
	private void facebookShare(Context context, String imagePath,
			String imageUrl, String shareUrl, String type) {
		Platform platform = ShareSDK.getPlatform(context, Facebook.NAME);
		if (platform.isClientValid()) {
			platform.setPlatformActionListener(this);// 如果没有通过审核，这个监听没有什么作用
			cn.sharesdk.facebook.Facebook.ShareParams shareParams = new cn.sharesdk.facebook.Facebook.ShareParams();
			shareParams.text = Common.SHARE_APP_NAME;
			if ("local".equals(type)) {// 本地图片
				shareParams.setImagePath(imagePath);
			} else if ("online".equals(type)) {// 网络图片，未审核的不支持网络图片，所以只能把链接分享出来
				shareParams.setImageUrl(imageUrl);
			}
			platform.share(shareParams);
		} else {
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			showNotification(2000,
					context.getString(R.string.share_failure_facebook));
		}
	}

	/**
	 * twitter分享，只能以网页的形式进行分享，同时，本地图片不支持过大的文件
	 * 
	 * @param context
	 * @param imagePath
	 * @param imageUrl
	 * @param type
	 */
	private void twitterShare(Context context, String imagePath,
			String imageUrl, String shareUrl, String type) {
		Platform platform = ShareSDK.getPlatform(context, Twitter.NAME);
		platform.setPlatformActionListener(this);
		cn.sharesdk.twitter.Twitter.ShareParams shareParams = new cn.sharesdk.twitter.Twitter.ShareParams();
		if ("local".equals(type)) {
			shareParams.text = context.getString(R.string.share_text);
			shareParams.setImagePath(imagePath);
		} else if ("online".equals(type)) {
			shareParams.text = shareUrl;
		}
		platform.share(shareParams);
	}

	/**
	 * 开始分享
	 * @param id
	 */
	private void startShare(int id){
		UmengUtil.onEvent(context, shareType);
		switch (id) {
			case R.id.wechat_moments:
				shareType = Common.EVENT_ONCLICK_SHARE_WECHAT_MOMENTS;
				sharePlatform = "wechat moments";
				wechatmonentsShare(context, imagePath, imageUrl, shareUrl, type);
				break;

			case R.id.wechat:
				shareType = Common.EVENT_ONCLICK_SHARE_WECHAT;
				sharePlatform = "wechat";
				wechatFriendsShare(context, imagePath, imageUrl, shareUrl, type);
				break;

			case R.id.qq:
				shareType = Common.EVENT_ONCLICK_SHARE_QQ;
				sharePlatform = "qq";
				if (type.equals("local")) {// 本地
					createThumbNail(id);
				} else {
					qqShare(context, imagePath, imageUrl, shareUrl, type);
				}
				break;

			case R.id.qqzone:
				shareType = Common.EVENT_ONCLICK_SHARE_QQZONE;
				sharePlatform = "qzone";
				qzoneShare(context, imagePath, imageUrl, shareUrl, type);
				break;

			case R.id.sina:
				shareType = Common.EVENT_ONCLICK_SHARE_SINA_WEIBO;
				sharePlatform = "sina";
				if (type.equals("local")) {// 本地
					createThumbNail(id);
				} else {
					sinaShare(context, imagePath, imageUrl, shareUrl, type);
				}
				break;

			case R.id.facebook:
				PictureAirLog.out("fb on click");
				shareType = Common.EVENT_ONCLICK_SHARE_FACEBOOK;
				sharePlatform = "facebook";
				facebookShare(context, imagePath, imageUrl, shareUrl, type);
				break;

			case R.id.twitter:
				shareType = Common.EVENT_ONCLICK_SHARE_TWITTER;
				sharePlatform = "twitter";
				handler.sendEmptyMessage(TWITTER);
				if (type.equals("local")) {// 本地
					createThumbNail(id);
				} else {
					twitterShare(context, imagePath, imageUrl, shareUrl, type);
				}
				break;

			default:
				break;
		}
	}

	@Override
	public void onClick(View v) {
		// 显示进度条，等待app打开
		dialog = CustomProgressDialog.show(context, null, false, null);
		switch (v.getId()) {
			case R.id.wechat_moments:
			case R.id.wechat:
			case R.id.qq:
			case R.id.qqzone:
			case R.id.sina:
			case R.id.facebook:
			case R.id.twitter:
				PictureAirLog.out("share on click--->");
				if (!Common.DEBUG) {//release版本
					dialog.dismiss();
					myToast.setTextAndShow(R.string.share_not_open, Common.TOAST_SHORT_TIME);
				} else {
					if ("local".equals(type)) {//开始分享
						PictureAirLog.out("local");
						startShare(v.getId());
					} else if ("online".equals(type)) {//网络，需要获取shareURL
						PictureAirLog.out("online");
						API1.getShareUrl(photoId, shareFileType, v.getId(), mHandler);
					}
				}
				break;

			case R.id.share_cancel:
				dialog.dismiss();
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
		new Thread() {
			public void run() {
				ImageLoader.getInstance().loadImage("file:///" + imagePath,
						new ImageLoadingListener() {

							@Override
							public void onLoadingStarted(String imageUri,
									View view) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onLoadingFailed(String imageUri,
									View view, FailReason failReason) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onLoadingComplete(String imageUri,
									View view, Bitmap loadedImage) {
								// TODO Auto-generated method stub
								ByteArrayOutputStream baos = new ByteArrayOutputStream();
								loadedImage.compress(
										Bitmap.CompressFormat.JPEG, 30, baos);
								byte[] datas = baos.toByteArray();
								File shareFile = new File(Common.SHARE_PATH);
								if (!shareFile.exists()) {
									shareFile.mkdirs();
								}
								shareFile = new File(Common.SHARE_PATH
										+ ScreenUtil
												.getReallyFileName(imagePath,0));
								if (shareFile.exists()) {

								} else {

									BufferedOutputStream stream = null;
									try {
										shareFile.createNewFile();
										FileOutputStream fStream = new FileOutputStream(
												shareFile);
										stream = new BufferedOutputStream(
												fStream);
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
									twitterShare(context, shareFile.toString(),
											imageUrl, shareUrl, type);
									break;

								case R.id.sina:
									sinaShare(context, shareFile.toString(), imageUrl,
											shareUrl, type);
									break;

								case R.id.qq:
									qqShare(context, shareFile.toString(), imageUrl,
											shareUrl, type);
									break;

								default:
									break;
								}
							}

							@Override
							public void onLoadingCancelled(String imageUri,
									View view) {
								// TODO Auto-generated method stub

							}
						});
			};
		}.start();
	}

	/**
	 * 将开始程序的对话框消失掉
	 */
	public void dismissDialog() {
		if (dialog != null && dialog.isShowing()) {
			PictureAirLog.out("share pop dismiss");
			dialog.dismiss();
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_ACTION_CCALLBACK: {
			switch (msg.arg1) {
			case 1: {
				// 成功
				API1.shareCallBack(shareId, sharePlatform);
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
						|| "WechatTimelineNotSupportedException"
								.equals(expName)
						|| "WechatFavoriteNotSupportedException"
								.equals(expName)) {
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
				// PictureAirLog.out("3333");
				int resId = getStringRes(context, "share_canceled");
				if (resId > 0) {
					// PictureAirLog.out("3333"+context.getString(resId));
					showNotification(2000, context.getString(resId));
				}
				ShareSDK.stopSDK();
			}
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
		if (dialog.isShowing()) {
			dialog.dismiss();
		}
		Message msg = new Message();
		msg.what = MSG_ACTION_CCALLBACK;
		msg.arg1 = 3;
		msg.arg2 = arg1;
		msg.obj = arg0;
		UIHandler.sendMessage(msg, this);
	}

	@Override
	public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {
		if (dialog.isShowing()) {
			dialog.dismiss();
		}
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
		if (dialog.isShowing()) {
			dialog.dismiss();
		}
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
		}  else if (typeName.equals("QZone")) {
			eventName = Common.EVENT_SHARE_QQZONE_FINISH;

		}
		else if (typeName.equals("QZone")) {
			eventName = Common.EVENT_SHARE_QQZONE_FINISH;

		} else if (typeName.equals("Wechat")) {
			eventName = Common.EVENT_SHARE_WECHAT_FINISH;

		} else if (typeName.equals("WechatMoments")) {
			eventName = Common.EVENT_SHARE_WECHAT_MOMENTS_FINISH;

		} else if (typeName.equals("Facebook")) {
			eventName = Common.EVENT_SHARE_FACEBOOK_FINISH;

		} else if (typeName.equals("Twitter")) {
			eventName = Common.EVENT_SHARE_TWITTER_FINISH;
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
			NotificationManager nm = (NotificationManager) app
					.getSystemService(Context.NOTIFICATION_SERVICE);
			final int id = Integer.MAX_VALUE / 13 + 1;
			nm.cancel(id);

			PendingIntent pi = PendingIntent.getActivity(app, 0, new Intent(), 0);
			Notification notification = new NotificationCompat.Builder(app).
					setSmallIcon(R.drawable.pp_icon).setAutoCancel(true).setContentTitle(context.getString(R.string.app_name))
					.setContentText(text).setWhen(System.currentTimeMillis()).setTicker(text).setContentIntent(pi).build();
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
