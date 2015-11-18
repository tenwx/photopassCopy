package com.pictureAir.widget;

import static cn.sharesdk.framework.utils.R.getStringRes;

import java.util.HashMap;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.framework.utils.UIHandler;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.moments.WechatMoments;
import cn.sharesdk.wechat.moments.WechatMoments.ShareParams;

import com.pictureAir.R;
import com.pictureAir.util.Common;

/**
 *此控件负责photo页面中menu下拉菜单的内容 
 */
public class SharePop extends PopupWindow implements OnClickListener, PlatformActionListener, Callback {
	public static final int SHARE = 201;
	public static final int GIFT = 202;
	public static final int DOWN = 203;
	public static final int BUY = 204;
	public static final int LOCATION = 205;
	public static final int FAVOURITE = 206;
	private static final int MSG_TOAST = 1;
	private static final int MSG_ACTION_CCALLBACK = 2;
	private static final int MSG_CANCEL_NOTIFY = 3;
	//	private int notifyIcon;
	private Context context;
	private LayoutInflater inflater;
	public View defaultView;
//	private Handler mHandler;
	private ImageButton wechat, qqzone, sina;
	private TextView sharecancel;
//	private PlatformActionListener callback;
	private String imagePath, imageUrl, type;
	private MyToast newToast;
	public SharePop(Context context) {
		super(context);
		this.context = context;
		//		this.mHandler = hander;
		initPopupWindow();
	}

	public void initPopupWindow() {
		ShareSDK.initSDK(context);
		newToast = new MyToast(context);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		defaultView = inflater.inflate(R.layout.share_dialog, null);
		defaultView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		setContentView(defaultView);
		setWidth(LayoutParams.MATCH_PARENT);
		setHeight(LayoutParams.WRAP_CONTENT);
		setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
		setAnimationStyle(R.style.from_bottom_anim);
		setFocusable(true);
		setOutsideTouchable(true);

		wechat = (ImageButton)defaultView.findViewById(R.id.wechat);
		qqzone = (ImageButton)defaultView.findViewById(R.id.qqzone);
		sina = (ImageButton)defaultView.findViewById(R.id.sina);
		sharecancel = (TextView)defaultView.findViewById(R.id.share_cancel);

		wechat.setOnClickListener(this);
		qqzone.setOnClickListener(this);
		sina.setOnClickListener(this);
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
	 * @param imageUrl  网络图片url
	 * @param type 判断是否是本地还是网络，类型有“local”“online”
	 */
	public void setshareinfo(String imagePath, String imageUrl, String type){
		this.imagePath = imagePath;
		this.imageUrl = imageUrl;
		this.type = type;
		//		System.out.println(this.imagePath+"+"+this.imageUrl+"_"+this.type);
	}

	/**
	 * 微信分享，不支持图文分享，只能分享图片，或者图片以链接的形式分享出去，但是都不能添加文字
	 * @param context
	 * @param imagePath 本地图片路径
	 * @param imageUrl  网络图片url
	 * @param type 判断是否是本地还是网络，类型有“local”“online”
	 */
	public void wechatmonentsShare(Context context, String imagePath, String imageUrl, String type){
		System.out.println("sharing info "+ imagePath+"_"+imageUrl+"_"+type);
		Platform platform = ShareSDK.getPlatform(context,WechatMoments.NAME);
		platform.setPlatformActionListener(this);//如果没有通过审核，这个监听没有什么作用
		ShareParams shareParams = new ShareParams();
		shareParams.title = "PhotoPass Share";
//		shareParams.text = "PhotoPass";
		//本地图片可以
		shareParams.shareType = Platform.SHARE_WEBPAGE;//以网页的形式分享图片
						shareParams.shareType = Platform.SHARE_IMAGE;//只分享图片，这个时候不需要url属性。
		if ("local".equals(type)) {//本地图片
			shareParams.imagePath = imagePath;
		}else if ("online".equals(type)) {//网络图片
//			shareParams.imageUrl = "http://140.206.125.194:4000/location/defaultPhotos/winnie.jpg";
			shareParams.imageUrl = imageUrl;
		}
//		shareParams.url = "http://www.pictureair.com";//share_webpage的时候需要这个参数
		platform.share(shareParams);
	}
	/**
	 * QZone分享
	 * @param context
	 * @param imagePath 本地图片路径
	 * @param imageUrl  网络图片url
	 * @param type 判断是否是本地还是网络，类型有“local”“online”
	 */
	public void qzoneShare(Context context, String imagePath, String imageUrl, String type){
		System.out.println("sharing info "+ imagePath+"_"+imageUrl+"_"+type);
		Platform platform = ShareSDK.getPlatform(context,QZone.NAME);
		platform.setPlatformActionListener(this);//如果没有通过审核，这个监听没有什么作用
		cn.sharesdk.tencent.qzone.QZone.ShareParams shareParams = new cn.sharesdk.tencent.qzone.QZone.ShareParams();
		shareParams.title = "Shanghai Disney Resort";
		shareParams.titleUrl = "http://www.pictureair.com";
		shareParams.text = "I would like to share my wonderful experience at Shanghai Disney Resort with you.";
		if ("local".equals(type)) {//本地图片
			shareParams.imagePath = imagePath;
		}else if ("online".equals(type)) {//网络图片
			shareParams.imageUrl = imageUrl;
		}
		shareParams.site = context.getString(R.string.app_name);
		shareParams.siteUrl = "http://www.pictureair.com";
		platform.share(shareParams);
	}

	/**
	 * sina分享
	 * @param context
	 * @param imagePath 本地图片路径
	 * @param imageUrl 网络图片URL
	 * @param type 本地还是网络的标记
	 * @param commend 文本评论
	 */
	public void sinaShare(Context context, String imagePath, String imageUrl, String type){
		System.out.println("sharing info "+ imagePath+"_"+imageUrl+"_"+type);
		Platform platform = ShareSDK.getPlatform(context,SinaWeibo.NAME);
		platform.SSOSetting(true);//未审核，必须要关闭SSO，true代表关闭。审核过了之后才要设置为false
//		platform.authorize();
		platform.setPlatformActionListener(this);//如果没有通过审核，这个监听没有什么作用
		cn.sharesdk.sina.weibo.SinaWeibo.ShareParams shareParams = new cn.sharesdk.sina.weibo.SinaWeibo.ShareParams();
		shareParams.text = "PictureAir";
		if ("local".equals(type)) {//本地图片
			shareParams.imagePath = imagePath;
		}else if ("online".equals(type)) {//网络图片，未审核的不支持网络图片，所以只能把链接分享出来
//			shareParams.imageUrl = imageUrl;
			shareParams.text = imageUrl;
			
		}
		platform.share(shareParams);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		//		Message msg = mHandler.obtainMessage();
		switch (v.getId()) {
		case R.id.wechat:
			System.out.println("wechat share");
			wechatmonentsShare(context, imagePath, imageUrl, type);
			break;
		case R.id.share_cancel:
			break;
		case R.id.qqzone:
			qzoneShare(context, imagePath, imageUrl, type);
			break;
		case R.id.sina:
			sinaShare(context, imagePath, imageUrl, type);
//			newToast.setTextAndShow("This function doesn't open", Common.TOAST_SHORT_TIME);
			
			break;
		default:
			break;
		}
		if(isShowing()){
			dismiss();
			ShareSDK.stopSDK();
		}

	}

	@Override
	public boolean handleMessage(Message msg) {
		switch(msg.what) {
		case MSG_TOAST: {
			String text = String.valueOf(msg.obj);
//			Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
			newToast.setTextAndShow(text, Common.TOAST_SHORT_TIME);
		}
		break;
		case MSG_ACTION_CCALLBACK: {
			switch (msg.arg1) {
			case 1: {
				// 成功
				int resId = getStringRes(context, "share_completed");
				if (resId > 0) {
					showNotification(2000, context.getString(resId));
				}
			}
			break;
			case 2: {
				// 失败
				String expName = msg.obj.getClass().getSimpleName();
				if ("WechatClientNotExistException".equals(expName)
						|| "WechatTimelineNotSupportedException".equals(expName)
						|| "WechatFavoriteNotSupportedException".equals(expName)) {
					int resId = getStringRes(context, "wechat_client_inavailable");
					if (resId > 0) {
						showNotification(2000, context.getString(resId));
					}
				} else if ("GooglePlusClientNotExistException".equals(expName)) {
					int resId = getStringRes(context, "google_plus_client_inavailable");
					if (resId > 0) {
						showNotification(2000, context.getString(resId));
					}
				} else if ("QQClientNotExistException".equals(expName)) {
					int resId = getStringRes(context, "qq_client_inavailable");
					if (resId > 0) {
						showNotification(2000, context.getString(resId));
					}
				} else if ("YixinClientNotExistException".equals(expName)
						|| "YixinTimelineNotSupportedException".equals(expName)) {
					int resId = getStringRes(context, "yixin_client_inavailable");
					if (resId > 0) {
						showNotification(2000, context.getString(resId));
					}
				} else if ("KakaoTalkClientNotExistException".equals(expName)) {
					int resId = getStringRes(context, "kakaotalk_client_inavailable");
					if (resId > 0) {
						showNotification(2000, context.getString(resId));
					}
				}else if ("KakaoStoryClientNotExistException".equals(expName)) {
					int resId = getStringRes(context, "kakaostory_client_inavailable");
					if (resId > 0) {
						showNotification(2000, context.getString(resId));
					}
				}else {
					int resId = getStringRes(context, "share_failed");
					if (resId > 0) {
						showNotification(2000, context.getString(resId));
					}
				}
			}
			break;
			case 3: {
				// 取消
				int resId = getStringRes(context, "share_canceled");
				if (resId > 0) {
					showNotification(2000, context.getString(resId));
				}
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
		// TODO Auto-generated method stub
		System.out.println("cancel");
		Message msg = new Message();
		msg.what = MSG_ACTION_CCALLBACK;
		msg.arg1 = 3;
		msg.arg2 = arg1;
		msg.obj = arg0;
		UIHandler.sendMessage(msg, this);
	}

	@Override
	public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {
		// TODO Auto-generated method stub
		System.out.println("complete");
		Message msg = new Message();
		msg.what = MSG_ACTION_CCALLBACK;
		msg.arg1 = 1;
		msg.arg2 = arg1;
		msg.obj = arg0;
		UIHandler.sendMessage(msg, this);
	}

	@Override
	public void onError(Platform arg0, int arg1, Throwable arg2) {
		// TODO Auto-generated method stub
		System.out.println("error");
		arg2.printStackTrace();
		Message msg = new Message();
		msg.what = MSG_ACTION_CCALLBACK;
		msg.arg1 = 2;
		msg.arg2 = arg1;
		msg.obj = arg2;
		UIHandler.sendMessage(msg, this);

		// 分享失败的统计
		ShareSDK.logDemoEvent(4, arg0);
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
//				PendingIntent pi = PendingIntent.getActivity(MainActivity.this, 0, new Intent(MainActivity.this,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT);
//				notificationBuilder.setContentIntent(pi);
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
