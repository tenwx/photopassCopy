package com.pictureair.photopass.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.loopj.android.http.RequestParams;
import com.pictureair.jni.keygenerator.PWJniUtil;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.CouponInfo;
import com.pictureair.photopass.widget.PWToast;


/**
 * 扫码的管理类
 * @author bauer_bao
 *
 */
public class DealCodeUtil {
	private Context context;
	private String code;
	private Handler handler;
	private String codeType;
	private PWToast myToast;
	private String dealWay;
	private boolean isInputAct;
	private SharedPreferences sharedPreferences;

	/**
	 * code处理失败
	 */
	public static final int DEAL_CODE_FAILED = 2;
	/**
	 * code处理成功
	 */
	public static final int DEAL_CODE_SUCCESS = 3;

	/**
	 * 需要返回信息
	 */
	public static final int STATE_RETURN_MSG = 1;

	/**
	 * 绑定pp成功，不需要返回信息
	 */
	public static final int STATE_ADD_PP_TO_USER_NOT_RETURN_SUCCESS = 4;

	/**
	 * 正常流程进入，并且绑定ppp或者coupon成功，不需要将结果返回
	 */
	public static final int STATE_ADD_CODE_TO_USER_NOT_RETURN_SUCCESS = 3;

	private int id = 0;
	
	
	private Handler handler2 = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
				case API1.CHECK_CODE_FAILED:
					PictureAirLog.out("check code failed");
					switch (msg.arg1) {
						case 6136://无效的条码类型
							id = ReflectionUtil.getStringId(context, msg.arg1);
							break;

						case 6055://已经被绑定过
							id = ReflectionUtil.getStringId(context, msg.arg1);
							break;

						case 6049://ppp未付费
							id = ReflectionUtil.getStringId(context, msg.arg1);
							break;

						case 6048://无效的ppp
							id = ReflectionUtil.getStringId(context, msg.arg1);
							break;

						case 6057://ppp已经被绑定
							id = ReflectionUtil.getStringId(context, msg.arg1);
							break;

						default://网络请求失败
							id = ReflectionUtil.getStringId(context, msg.arg1);
							break;
					}

					if (dealWay != null && (dealWay.equals("ppp") || dealWay.equals("pp")) && //如果从ppp页面或者pp页面过来，需要返回错误类型数据，并且需要跳转到对应的activity
							!isInputAct) {//如果不是手动输入页面
						handler.obtainMessage(DEAL_CODE_FAILED, id).sendToTarget();
					}else {//弹出错误提示
						if (dealWay != null && dealWay.equals("coupon")) {
							id = R.string.incorrect_coupon;
						}
						myToast.setTextAndShow(id, Common.TOAST_SHORT_TIME);
						handler.sendEmptyMessage(DEAL_CODE_FAILED);
					}
					break;

				case API1.CHECK_CODE_SUCCESS://检查code成功
					PictureAirLog.out("----------->check code success" + msg.obj);
					if (msg.obj == null || "invalid".equals(msg.obj.toString())) {
						checkCodeAvailableFailed();
						break;
					} else if ("photoPass".equals(msg.obj.toString())) {
						codeType = "pp";
					} else if ("photoPassPlus".equals(msg.obj.toString())) {
						codeType = "ppp";
					} else if ("coupon".equals(msg.obj.toString())) {
						codeType = "coupon";
					}

					if (TextUtils.isEmpty(codeType)) {//为空
						checkCodeAvailableFailed();
						break;
					}

					PictureAirLog.out("codetype--->" + codeType + " dealway--->" + dealWay);
					if (dealWay != null) {//如果从ppp、pp或者coupon页面进来，卡的类型不一致，直接返回，退出，一致，则添加
						if (!dealWay.equals(codeType)) {//类型不一致
							if (dealWay.equals("ppp") || dealWay.equals("pp")) {//ppp或者pp
								PictureAirLog.out("--------->need call back");
								Bundle bundle = new Bundle();
								bundle.putInt("status", STATE_RETURN_MSG);
								bundle.putString("result", "notSame");
								handler.obtainMessage(DEAL_CODE_SUCCESS, bundle).sendToTarget();
							} else {//coupon
								id = R.string.incorrect_coupon;
								myToast.setTextAndShow(id, Common.TOAST_SHORT_TIME);
								handler.sendEmptyMessage(DEAL_CODE_FAILED);
								break;
							}
						} else {//卡类型一致
							getInfo(code, codeType);
						}
					} else {//故事页面
						if (codeType.equals("coupon")) {//故事页面扫了coupon，提示无效的卡
							myToast.setTextAndShow(R.string.http_error_code_6136, Common.TOAST_SHORT_TIME);
							handler.sendEmptyMessage(DEAL_CODE_FAILED);
						} else {
							getInfo(code, codeType);
						}
					}
					break;

				case API1.ADD_SCANE_CODE_FAIED://绑定失败
					PictureAirLog.out("error code --->" + msg.arg1);
					myToast.setTextAndShow(ReflectionUtil.getStringId(context, msg.arg1), Common.TOAST_SHORT_TIME);
					handler.sendEmptyMessage(DEAL_CODE_FAILED);
					break;

				case API1.ADD_PP_CODE_TO_USER_SUCCESS://绑定pp成功
					Bundle bundle2 = new Bundle();
					if (dealWay != null && dealWay.equals("pp")) {//从pp进入
						bundle2.putInt("status", STATE_RETURN_MSG);
						bundle2.putString("result", "ppOK" + code);
						PictureAirLog.out("scan pp ok 2222");
						handler.obtainMessage(DEAL_CODE_SUCCESS, bundle2).sendToTarget();

					}else {
						bundle2.putInt("status", STATE_ADD_PP_TO_USER_NOT_RETURN_SUCCESS);
						PictureAirLog.out("scan pp ok 444");
						handler.obtainMessage(DEAL_CODE_SUCCESS, bundle2).sendToTarget();

					}
					break;

				case API1.ADD_PPP_CODE_TO_USER_SUCCESS://绑定ppp或coupon成功
					Bundle bundle3 = new Bundle();
					PictureAirLog.out("add ppp code or coupon to user success--->" + dealWay);
					if (msg.obj == null) {//ppp
						if (dealWay != null && dealWay.equals("ppp")) {//从ppp进入
							bundle3.putInt("status", STATE_RETURN_MSG);
							bundle3.putString("result", "pppOK");
							PictureAirLog.out("scan ppp ok 555");
							handler.obtainMessage(DEAL_CODE_SUCCESS, bundle3).sendToTarget();

						}else {
							bundle3.putInt("status", STATE_ADD_CODE_TO_USER_NOT_RETURN_SUCCESS);
							PictureAirLog.out("scan ppp ok 333");
							handler.obtainMessage(DEAL_CODE_SUCCESS, bundle3).sendToTarget();

						}
					} else {//coupon
						//1.ppp,不会出现
						//2.scan，直接绑定，也可以返回数据，scan页面也不会处理
						//3.coupon，直接绑定，并且返回数据
						//解析json
						PictureAirLog.out("coupon---->" + msg.obj.toString());
						CouponInfo couponInfo = JsonUtil.getCouponInfo(((JSONObject) msg.obj).getJSONObject("PPP"));
						//将对象放入bundle3中
						bundle3.putInt("status", STATE_ADD_CODE_TO_USER_NOT_RETURN_SUCCESS);
						PictureAirLog.out("coupon---->" + couponInfo == null ? " null " : "not null");
						bundle3.putSerializable("coupon", couponInfo);
						PictureAirLog.out("scan coupon ok");
						handler.obtainMessage(DEAL_CODE_SUCCESS, bundle3).sendToTarget();
					}
					break;

				default:
					break;
			}
			return false;
		}
	});
	
	
	public DealCodeUtil(Context context, Intent intent, boolean isInputAct, Handler handler) {
		this.context = context;
		this.handler = handler;
		myToast = new PWToast(context);
		dealWay = intent.getStringExtra("type");
		this.isInputAct = isInputAct;
		sharedPreferences = context.getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME,Context.MODE_PRIVATE);
	}

	/**
	 * 检查code失败的情况
	 */
	private void checkCodeAvailableFailed(){
		if (dealWay != null && dealWay.equals("coupon")) {
			id = R.string.incorrect_coupon;
		} else {
			id = R.string.http_error_code_6136;
		}
		myToast.setTextAndShow(id, Common.TOAST_SHORT_TIME);
		handler.sendEmptyMessage(DEAL_CODE_FAILED);
	}
	
	/**
	 * 处理二维码
	 * @param code
	 */
	public void startDealCode(final String code){
		this.code = code;
		API1.checkCodeAvailable(code, AESKeyHelper.decryptString(sharedPreferences.getString(Common.USERINFO_TOKENID, ""), PWJniUtil.getAESKey(Common.APP_TYPE_SHDRPP)), handler2);
	}

	private void getInfo(String code, final String type){
		RequestParams params = new RequestParams();
		PictureAirLog.out("scan result=" + code + ">>" + type);
		params.put(Common.USERINFO_TOKENID, AESKeyHelper.decryptString(sharedPreferences.getString(Common.USERINFO_TOKENID, ""), PWJniUtil.getAESKey(Common.APP_TYPE_SHDRPP)));
		String urlString;
		if ("pp".equals(type)) {
			PictureAirLog.out("pp");
			params.put(Common.CUSTOMERID, code);
			urlString = Common.BASE_URL_TEST + Common.ADD_CODE_TO_USER;

		}else {
			PictureAirLog.out("ppp or coupon");
			params.put(Common.PPPCode, code);
			urlString = Common.BASE_URL_TEST+Common.BIND_PPP_TO_USER;

		}

		API1.addScanCodeToUser(urlString, params, type, handler2);
	}

}
