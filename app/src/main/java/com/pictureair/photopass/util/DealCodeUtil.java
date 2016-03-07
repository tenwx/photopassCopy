package com.pictureair.photopass.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSONArray;
import com.loopj.android.http.RequestParams;

import cn.smssdk.gui.MyToast;

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
	private MyToast myToast;
	private String dealWay;
	private String needBind;
	private String bindData;
	private String pppId;
	private SharedPreferences sharedPreferences;

	/**
	 * code处理失败
	 */
	public static final int DEAL_CODE_FAILED = 2;
	/**
	 * code处理成功
	 */
	public static final int DEAL_CODE_SUCCESS = 3;

	private int id = 0;
	
	
	private Handler handler2 = new Handler(){
		public void handleMessage(Message msg){
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

					if (dealWay != null) {//如果从ppp页面过来，需要返回错误类型数据，并且需要跳转到对应的activity
						handler.obtainMessage(DEAL_CODE_FAILED, id).sendToTarget();
					}else {//弹出错误提示
						myToast.setTextAndShow(id, Common.TOAST_SHORT_TIME);
						handler.sendEmptyMessage(DEAL_CODE_FAILED);
					}
					break;

				case API1.CHECK_CODE_SUCCESS://检查code成功
					PictureAirLog.out("----------->check code success");
					if ("photoPass".equals(msg.obj.toString())) {
						codeType = "pp";
					} else if ("photoPassPlus".equals(msg.obj.toString())) {
						codeType = "ppp";
					}
					if (dealWay != null) {//如果从ppp页面进来，卡的类型不一致，直接返回，退出
						System.out.println("--------->need call back");
						Bundle bundle = new Bundle();
						if (!dealWay.equals(codeType)) {//卡类型不一致
							bundle.putInt("status", 1);
							bundle.putString("result", "notSame");
							handler.obtainMessage(DEAL_CODE_SUCCESS, bundle).sendToTarget();

						} else {//类型一致。如果是ppp的话，直接绑定，如果是pp的话，提示并返回，让用户去确认
							if (codeType.equals("ppp")) {
								getInfo(code, codeType);
							} else {//如果是pp，返回信息
								bundle.putInt("status", 2);
								bundle.putString("result", code);
								bundle.putBoolean("hasBind", false);
								handler.obtainMessage(DEAL_CODE_SUCCESS, bundle).sendToTarget();
							}
						}
					} else {//其他页面
						getInfo(code, codeType);
					}
					break;

				case API1.BIND_PP_FAILURE://绑定pp失败
				case API1.ADD_SCANE_CODE_FAIED://绑定失败
					PictureAirLog.out("error code --->" + msg.arg1);
					myToast.setTextAndShow(ReflectionUtil.getStringId(context, msg.arg1), Common.TOAST_SHORT_TIME);
					handler.sendEmptyMessage(DEAL_CODE_FAILED);
					break;

				case API1.BIND_PP_SUCCESS://绑定pp成功
					Bundle bundle = new Bundle();
					bundle.putInt("status", 3);
					handler.obtainMessage(DEAL_CODE_SUCCESS, bundle).sendToTarget();
					break;

				case API1.ADD_PP_CODE_TO_USER_SUCCESS://绑定pp成功
					Bundle bundle2 = new Bundle();
					bundle2.putInt("status", 4);
					handler.obtainMessage(DEAL_CODE_SUCCESS, bundle2).sendToTarget();
					break;

				case API1.ADD_PPP_CODE_TO_USER_SUCCESS://绑定ppp成功
					Bundle bundle3 = new Bundle();
					PictureAirLog.out("add ppp code to user success--->" + dealWay);
					if (dealWay != null) {//从ppp进入
						bundle3.putInt("status", 5);
						bundle3.putString("result", "pppOK");
						PictureAirLog.out("scan ppp ok 555");
						handler.obtainMessage(DEAL_CODE_SUCCESS, bundle3).sendToTarget();
					}else {
						bundle3.putInt("status", 3);
						PictureAirLog.out("scan ppp ok 333");
						handler.obtainMessage(DEAL_CODE_SUCCESS, bundle3).sendToTarget();
					}
					break;

				default:
					break;
			}
		};
	};
	
	
	public DealCodeUtil(Context context, Intent intent, Handler handler) {
		this.context = context;
		this.handler = handler;
		myToast = new MyToast(context);
		dealWay = intent.getStringExtra("type");
		needBind = intent.getStringExtra("needbind");
		bindData = intent.getStringExtra("binddate");
		pppId = intent.getStringExtra("pppid");
		sharedPreferences = context.getSharedPreferences(Common.USERINFO_NAME,Context.MODE_PRIVATE);
	}
	
	/**
	 * 处理二维码
	 * @param code
	 */
	public void startDealCode(final String code){
		this.code = code;
		API1.checkCodeAvailable(code, sharedPreferences.getString(Common.USERINFO_TOKENID, ""), handler2);
	}

	private void getInfo(String code, final String type){
		RequestParams params = new RequestParams();
		PictureAirLog.out("scan result=" + code + ">>" + type);
		params.put(Common.USERINFO_TOKENID, sharedPreferences.getString(Common.USERINFO_TOKENID, ""));
		String urlString = null;
		if ("pp".equals(type)) {
			if (null != needBind && "false".equals(needBind)) {//如果是通过pp界面扫描的时候，此处不需要绑定pp到用户
				JSONArray pps = new JSONArray();
				pps.add(code);

				API1.bindPPsToPPP(sharedPreferences.getString(Common.USERINFO_TOKENID, null),pps, bindData, pppId, handler2);
				PictureAirLog.out("return");
				return;
			}else {//其他界面过来的话，需要绑定到user
				PictureAirLog.out("pp");
				params.put(Common.CUSTOMERID, code);
				urlString = Common.BASE_URL_TEST+Common.ADD_CODE_TO_USER;
			}
		}else {
			PictureAirLog.out("ppp");
			params.put(Common.PPPCode, code);
			urlString = Common.BASE_URL_TEST+Common.BIND_PPP_TO_USER;
		}

		API1.addScanCodeToUser(urlString, params, type, handler2);

	}
	

}
