package com.pictureair.photopass.widget;

import android.content.Context;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.jni.ciphermanager.PWJniUtil;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.entity.BaseCheckUpdate;
import com.pictureair.photopass.http.rxhttp.RxSubscribe;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.AESKeyHelper;
import com.pictureair.photopass.util.API2;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SPUtils;
import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.components.RxActivity;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import rx.android.schedulers.AndroidSchedulers;

/**
 * 自动更新检查类
 * @author bauer_bao
 *
 */
public class CheckUpdate implements BaseCheckUpdate{
	private static CheckUpdate checkUpdate;
	private BaseCheckUpdateListener baseCheckListener;
	
	public static CheckUpdate getInstance() {
		if (checkUpdate == null) {
			checkUpdate = new CheckUpdate();
		}
		return checkUpdate;
	}

	public void setBaseCheckListener(BaseCheckUpdateListener listener) {
		this.baseCheckListener = listener;
	}

	@Override
	public void getTokenId(final Context context) {
		API2.getTokenId(context)
				.compose(((RxActivity)context).<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new RxSubscribe<JSONObject>() {
					@Override
					public void _onNext(JSONObject jsonObject) {
						try {
							byte[] key = ACache.get(MyApplication.getInstance()).getAsBinary(Common.USERINFO_SALT);
							if (key == null) {
								ACache.get(context).put(Common.USERINFO_SALT, AESKeyHelper.secureByteRandom());
							}
							SPUtils.put(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_TOKENID,
									AESKeyHelper.encryptString(jsonObject.getString(Common.USERINFO_TOKENID), PWJniUtil.getAESKey(Common.APP_TYPE_SHDRPP, 0)));
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

					@Override
					public void _onError(int status) {
						if (baseCheckListener != null) {
							baseCheckListener.getTokenIdFailed();
						}
					}

					@Override
					public void onCompleted() {
						if (baseCheckListener != null) {
							baseCheckListener.getTokenIdSuccess();
						}
					}
				});
	}

	@Override
	public void checkUpdate(final Context context) {
		// TODO Auto-generated method stub
		if (context == null) {
			if (baseCheckListener != null) {
				baseCheckListener.checkUpdateFailed();
			}
		} else {
			API2.checkUpdate(context)
					.compose(context instanceof RxActivity ? ((RxActivity)context).<JSONObject>bindUntilEvent(ActivityEvent.DESTROY)
							: ((RxAppCompatActivity)context).<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(new RxSubscribe<JSONObject>() {
						@Override
						public void _onNext(JSONObject jsonObject) {
							PictureAirLog.out("update---->" + jsonObject);
							ACache.get(context).put(Common.UPDATE_INFO, jsonObject.toString());
							if (baseCheckListener != null) {
								baseCheckListener.checkUpdateSuccess(jsonObject);
							}
						}

						@Override
						public void _onError(int status) {
							PictureAirLog.out("failed---->" + status);
							if (baseCheckListener != null) {
								baseCheckListener.checkUpdateFailed();
							}
						}

						@Override
						public void onCompleted() {

						}
					});
		}
	}

	public interface BaseCheckUpdateListener {
		void getTokenIdSuccess();
		void getTokenIdFailed();
		void checkUpdateSuccess(JSONObject jsonObject);
		void checkUpdateFailed();
	}
}
