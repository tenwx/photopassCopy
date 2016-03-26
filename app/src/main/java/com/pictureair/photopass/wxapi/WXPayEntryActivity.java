package com.pictureair.photopass.wxapi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.pictureair.photopass.R;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import net.sourceforge.simcpux.Constants;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

	private static final String TAG = "MicroMsg.SDKSample.WXPayEntryActivity";

	private IWXAPI api;

	private SharedPreferences sharedPreferences;

	private boolean launchFinish = false;

	private static final int DEAL_PAY_RESULE = 111;

	private Handler handler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
				case DEAL_PAY_RESULE:
					if (launchFinish) {
						launchFinish = false;
						finish();
					} else {
						handler.sendEmptyMessageDelayed(DEAL_PAY_RESULE, 500);
					}
					break;

				default:
					break;
			}
			return false;
		}
	});

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pay_result);

		PictureAirLog.out("wxpay activity---> oncreate");
		api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);

		api.handleIntent(getIntent(), this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		launchFinish = true;
		PictureAirLog.out("wxpay activity---> onresume");
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {

	}

	// 返回结果
	@Override
	public void onResp(BaseResp resp) {
		PictureAirLog.d(TAG, "onPayFinish, errCode = " + resp.errCode);
		if (sharedPreferences == null) {
			sharedPreferences = getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
		}
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(Common.WECHAT_PAY_STATUS, resp.errCode);
		PictureAirLog.out("====resp.errCode=====" + resp.errCode);
		editor.commit();
		handler.sendEmptyMessage(DEAL_PAY_RESULE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		PictureAirLog.out("wx pay activity---->ondestroy");
	}
}