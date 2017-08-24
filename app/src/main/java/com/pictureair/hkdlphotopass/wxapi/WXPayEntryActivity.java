package com.pictureair.hkdlphotopass.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.pictureair.hkdlphotopass.R;
import com.pictureair.hkdlphotopass.util.Common;
import com.pictureair.hkdlphotopass.util.PictureAirLog;
import com.pictureair.hkdlphotopass.util.SPUtils;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import net.sourceforge.simcpux.Constants;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

	private static final String TAG = "MicroMsg.SDKSample.WXPayEntryActivity";

	private IWXAPI api;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pay_result);

		PictureAirLog.out("wxpay activity---> oncreate");
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
		PictureAirLog.d(TAG, "onPay, errCode = " + resp.errCode);
		SPUtils.put(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.WECHAT_PAY_STATUS, resp.errCode);
		finish();
	}
}