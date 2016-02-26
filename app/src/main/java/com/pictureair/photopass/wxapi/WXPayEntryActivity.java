package com.pictureair.photopass.wxapi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pay_result);

		api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);

		api.handleIntent(getIntent(), this);
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
			PictureAirLog.err("init sp" + sharedPreferences);
		}
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(Common.WECHAT_PAY_STATUS, resp.errCode);
		PictureAirLog.out("====resp.errCode=====" + resp.errCode);
		editor.commit();
		finish();
	}
}