package com.pictureAir.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceActivity.Header;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pictureAir.MainTabActivity;
import com.pictureAir.R;
import com.pictureAir.widget.CustomProgressDialog;
import com.pictureAir.widget.MyToast;

/**
 * 接受loginActicity中传来的手机号和密码
 * 1.首先验证手机号是否注册过
 * 2.进行注册
 * 3.自动登录到主界面
 * */
public class SignAndLoginPhoneNumberUtil {
	private String pwd;
	private String phone;
	private SharedPreferences sp;
	private MyToast newToast;
	private Context context;
	private CustomProgressDialog customProgressDialog;
	public SignAndLoginPhoneNumberUtil(Context c,String phoneStr,String pwdStr) {
		this.context = c;
		this.phone = phoneStr;
		this.pwd = pwdStr;
		
		init();
	}
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case API.SUCCESS://sign成功
				System.out.println("login success-------------");
				API.getcartcount(context,sp.getString(Common.USERINFO_ID, ""),handler);
				break;
				
			case API.FAILURE:
				customProgressDialog.dismiss();
				newToast.setTextAndShow("账号已经被注册", Common.TOAST_SHORT_TIME);
				break;
				
			case API.SIGN_FAILED:
				customProgressDialog.dismiss();
				try {
					JSONObject infoJsonObject = (JSONObject) msg.obj;
					if (infoJsonObject.has("type")) {
						if (infoJsonObject.getString("type").equals("shortPassword")) {
							newToast.setTextAndShow(R.string.pwd_is_short, Common.TOAST_SHORT_TIME);
//						}else {
//							myToast.setTextAndShow(R.string.pwd_is_short, Common.TOAST_SHORT_TIME);
						}else if (infoJsonObject.getString("type").equals("existedEmail")) {
							newToast.setTextAndShow(R.string.email_exist, Common.TOAST_SHORT_TIME);
						}else if (infoJsonObject.getString("type").equals("existedMobile")) {
							newToast.setTextAndShow(R.string.mobile_exist, Common.TOAST_SHORT_TIME);
						}
					}else {
						newToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
				
			case API.GET_CART_COUNT_FAILED:
			case API.GET_PPP_FAILED:
			case API.GET_STOREID_FAILED:
				customProgressDialog.dismiss();
				newToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
				break;
				
			case API.GET_CART_COUNT_SUCCESS:
				System.out.println("get cart count success------------");
				API.getPPSByUserId(sp.getString(Common.USERINFO_TOKENID, null),
						handler);// 获取pp列表
				break;
				
			case API.GET_PPS_SUCCESS:// 获取pp列表成功
				/**
				 * 获取pp成功之后，需要放入sharedPrefence中
				 */
				JSONObject ppsJsonObject = (JSONObject) msg.obj;
//				Log.d(TAG, "pps===" + ppsJsonObject);
				if (ppsJsonObject.has("PPList")) {
					try {
						JSONArray pplists = ppsJsonObject
								.getJSONArray("PPList");
						Editor editor = sp.edit();
						editor.putInt(Common.PP_COUNT, pplists.length());
						editor.commit();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
//					Log.d(TAG, "pp size == 0");
				}

				API.getStoreIdbyIP("140.206.125.195", handler);
				break;

			case API.GET_PPS_FAILED:// 获取pp列表失败
				customProgressDialog.dismiss();
				newToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
				break;
				
			case API.GET_PPP_SUCCESS:
				System.out.println("get ppp success ----------------");
				JSONObject ppplistJsonObject = (JSONObject) msg.obj;
				try {
					JSONArray ppplistArray = ppplistJsonObject.getJSONArray("PPPList");
					if (0!=ppplistArray.length()) {//说明有ppp
						System.out.println("length="+ppplistArray.length());
						Editor editor = sp.edit();
						editor.putInt(Common.PPP_COUNT, ppplistArray.length());
						editor.commit();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				API.getStoreIdbyIP("140.206.125.195", handler);
				break;
				
			case API.GET_STOREID_SUCCESS:
				customProgressDialog.dismiss();
				System.out.println("get storeid success----------------");
				JSONObject obj = (JSONObject) msg.obj;
				try {
					Editor editor = sp.edit();
					editor.putString(Common.CURRENCY, obj.getString("currency").toString());
					editor.putString(Common.STORE_ID, obj.getString("storeId").toString());
					editor.commit();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				Intent i = new Intent();
				i.setAction("com.receiver.AlertManagerRecriver");
				context.sendBroadcast(i);
				
				i = new Intent();
				i.setClass(context, MainTabActivity.class);
				context.startActivity(i);
				
				break;
				
			case API.MODIFY_PWD_FAILED:
				customProgressDialog.dismiss();
				System.out.println("signorfotget------modify pwd failed");
				//提示错误
				break;
				
			case API.MODIFY_PWD_SUCCESS:
				customProgressDialog.dismiss();
				System.out.println("signorforget------modify pwd success");
				//跳转至登录界面
				break;
				
			default:
				break;
			}
		}
	};

	private void init() {
		newToast = new MyToast(context);
		customProgressDialog = CustomProgressDialog.show(context, context.getString(R.string.is_loading), false, null);
		sp = context.getSharedPreferences(Common.USERINFO_NAME, 0);
		
		//获取手机号
		//注册
		// 向服务器发送请求
			if (null == sp.getString(Common.USERINFO_TOKENID, null)) {
				//需要重新获取一次tokenid
				System.out.println("no tokenid, need to obtain one");
				final StringBuffer sb = new StringBuffer();
				sb.append(Common.BASE_URL).append(Common.GET_TOKENID);//获取地址
				
				RequestParams params = new RequestParams();
				params.put(Common.TERMINAL, "android");
				params.put(Common.UUID, Installation.id(context));
				
				HttpUtil.get(sb.toString(), params, new JsonHttpResponseHandler() {
					@Override
					public void onStart() {
						super.onStart();
						System.out.println("get tokenid start");
					}
					@Override
					public void onSuccess(int statusCode, org.apache.http.Header[] headers, JSONObject response) {
						super.onSuccess(statusCode, headers, response);
						try {
							System.out.println("tokenid=="+response);
							Editor e = sp.edit();
							if (response.has(Common.USERINFO_TOKENID)) {
								System.out.println("add tokenid=============");
								e.putString(Common.USERINFO_TOKENID, response.getString(Common.USERINFO_TOKENID));
							}
							e.commit();
							API.Sign(context, phone, pwd , handler);
						} catch (JSONException e1) {
							e1.printStackTrace();
						}
					}

					@Override
					public void onFailure(int statusCode, org.apache.http.Header[] headers, String responseString, Throwable throwable) {
						super.onFailure(statusCode, headers, responseString, throwable);
						throwable.printStackTrace();
						newToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
					}
				});
			}else {
				API.Sign(context, phone, pwd , handler);
			}
		}

}
