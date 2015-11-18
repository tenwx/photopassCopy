package com.pictureAir;

import java.util.HashMap;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pictureAir.util.API;
import com.pictureAir.util.AppManager;
import com.pictureAir.util.AppUtil;
import com.pictureAir.util.Common;
import com.pictureAir.util.HttpUtil;
import com.pictureAir.util.Installation;
import com.pictureAir.util.SignAndLoginPhoneNumberUtil;
import com.pictureAir.widget.CustomProgressDialog;
import com.pictureAir.widget.EditTextWithClear;
import com.pictureAir.widget.MyToast;

/** 其他登录 bass */
public class OtherLoginActivity extends Activity implements OnClickListener {
	private static final String TAG = "OtherLoginActivity";
	// 申明控件
	private Button sign, login;
	private EditTextWithClear userName, password;
	
	private TextView forgot;
	private ImageView back;
	private CustomProgressDialog dialog;

	// 申明变量
	private StringBuffer loginUrl = new StringBuffer();// 登录的url
	private String storeIdString = "";// 商店ID字符串
	private String currency = "";// 货币
	private static final int START_OTHER_REGISTER_ACTIVITY = 1;// 启动 其他注册的侧面

	// 申明其他类
	private SharedPreferences sp;
	private Editor editor;
	private MyToast myToast;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_other_login);
		AppManager.getInstance().addActivity(this);// 添加activity
		initView();
	}

	/** 初始化 */
	private void initView() {
		loginUrl.append(Common.BASE_URL).append(Common.LOGIN);// 链接地址
		sp = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);// userInfo
		myToast = new MyToast(OtherLoginActivity.this);// 获取toast
		sign = (Button) findViewById(R.id.sign);
		login = (Button) findViewById(R.id.btnOtherLogin);
		userName = (EditTextWithClear) findViewById(R.id.otherLogin_email);
		password = (EditTextWithClear) findViewById(R.id.otherLogin_password);
		forgot = (TextView) findViewById(R.id.forgot);
		back = (ImageView) findViewById(R.id.login_back);// 返回按键


		back.setOnClickListener(this);
		sign.setOnClickListener(this);
		login.setOnClickListener(this);
		forgot.setOnClickListener(this);

		userName.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					/* 隐藏软键盘 */
					userName.clearFocus();
					password.requestFocus();
					return true;
				}
				return false;
			}
		});
		password.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// TODO Auto-generated method stub
				/* 判断是否是“GO”键 */
				if (actionId == EditorInfo.IME_ACTION_GO) {
					hideInputMethodManager(v);//
					login.performClick(); //
					return true;
				}
				return false;
			}
		});

		initSSMSSDK();// 初始化
	}
	/**
	 * 点击键盘之外，隐藏键盘
	 */
	@Override  
	public boolean dispatchTouchEvent(MotionEvent ev) {
	    if (ev.getAction() == MotionEvent.ACTION_DOWN) {  
	        View v = getCurrentFocus();  
	        if (AppUtil.isShouldHideInput(v, ev)) {  
//	        	if (!password.hasFocus() && !userName.hasFocus()) {
	        		hideInputMethodManager(v);
//				}
	        }  
	        return super.dispatchTouchEvent(ev);  
	    }  
	    // 必不可少，否则所有的组件都不会有TouchEvent了  
	    if (getWindow().superDispatchTouchEvent(ev)) {  
	        return true;  
	    }  
	    return onTouchEvent(ev);  
	}
	//
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case API.SUCCESS:// 登录成功
				API.getcartcount(OtherLoginActivity.this,
						sp.getString(Common.USERINFO_ID, ""), handler);
				break;

			case API.FAILURE:
				String result = msg.obj.toString();
				dialog.dismiss();
				myToast.setTextAndShow(result, Common.TOAST_SHORT_TIME);
				if ("tokenExpired".equals(result)) {
					System.out.println("tokenExpired");
					editor = sp.edit();
					editor.putString(Common.USERINFO_TOKENID, null);
					editor.commit();
				}
				break;

			case API.GET_CART_COUNT_SUCCESS:// 成功获取购物车数量
				Log.d(TAG, "get cart count success---------------");
				API.getPPSByUserId(sp.getString(Common.USERINFO_TOKENID, null),
						handler);// 获取pp列表
				break;

			case API.GET_CART_COUNT_FAILED:// 获取购物车数量失败
				dialog.dismiss();
				myToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
				break;

			case API.GET_PPS_SUCCESS:// 获取pp列表成功
				/**
				 * 获取pp成功之后，需要放入sharedPrefence中
				 */
				JSONObject ppsJsonObject = (JSONObject) msg.obj;
				Log.d(TAG, "pps===" + ppsJsonObject);
				if (ppsJsonObject.has("PPList")) {
					try {
						JSONArray pplists = ppsJsonObject
								.getJSONArray("PPList");
						editor = sp.edit();
						editor.putInt(Common.PP_COUNT, pplists.length());
						editor.commit();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					Log.d(TAG, "pp size == 0");
				}

				API.getStoreIdbyIP("140.206.125.195", handler);
				break;

			case API.GET_PPS_FAILED:// 获取pp列表失败
				dialog.dismiss();
				myToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
				break;

			case API.GET_STOREID_SUCCESS:// 获取storeId成功
				Log.d(TAG, "get storeid success--------------------");
				JSONObject obj = (JSONObject) msg.obj;
				try {
					storeIdString = obj.getString("storeId").toString();
					currency = obj.getString("currency").toString();// 获取货币
					Log.d(TAG, "storeid:" + storeIdString + ";currency:"
							+ currency);
					Editor editor = sp.edit();
					editor.putString(Common.CURRENCY, currency);
					editor.putString(Common.STORE_ID, storeIdString);
					editor.commit();
				} catch (JSONException e) {
					e.printStackTrace();
				}

				loginsuccess();
				break;

			case API.GET_STOREID_FAILED:
				dialog.dismiss();
				myToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
				break;
				
			case START_OTHER_REGISTER_ACTIVITY:

				// 其他注册的按钮//
				System.out.println("other way on click----------");
				startActivity(new Intent(OtherLoginActivity.this,
						OtherRegisterActivity.class));

				break;

			default:
				break;
			}
		}
	};

	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.login_back:
			finish();
			break;
			
		case R.id.btnOtherLogin:
			System.out.println("登录按钮");
			// 登录
			hideInputMethodManager(v);
			if (userName.getText().toString().trim().isEmpty()) {
				myToast.setTextAndShow(R.string.username_null,
						Common.TOAST_SHORT_TIME);
				break;
			}
			if (!AppUtil.isEmail(userName.getText().toString().trim())) {
				myToast.setTextAndShow(R.string.email_error,
						Common.TOAST_SHORT_TIME);
				break;
			}
			if (password.getText().toString().trim().isEmpty()) {
				myToast.setTextAndShow(R.string.pw_null,
						Common.TOAST_SHORT_TIME);
				break;
			}
//			dialog = ProgressDialog.show(this, getString(R.string.loading___),
//					getString(R.string.is_loading), false, true);
			dialog = CustomProgressDialog.show(this, getString(R.string.is_loading), true, null);
			// 登录成功时可把一些后续需要使用到的信息保存起来，比如地点收藏状态，pp和pp+信息等，具体看后台返回的数据决定
			if (null == sp.getString(Common.USERINFO_TOKENID, null)) {
				System.out.println("no tokenid");
				final StringBuffer sb = new StringBuffer();
				sb.append(Common.BASE_URL).append(Common.GET_TOKENID);
				RequestParams params = new RequestParams();
				params.put(Common.TERMINAL, "android");
				params.put(Common.UUID, Installation.id(this));
				HttpUtil.get(sb.toString(), params,
						new JsonHttpResponseHandler() {
							@Override
							public void onStart() {
								super.onStart();
								System.out.println("get tokenid start");
							}

							public void onSuccess(int statusCode,
									Header[] headers, JSONObject response) {
								super.onSuccess(statusCode, headers, response);
								try {
									System.out.println("tokenid==" + response);
									Editor e = sp.edit();
									if (response.has(Common.USERINFO_TOKENID)) {
										System.out
												.println("add tokenid=============");
										e.putString(
												Common.USERINFO_TOKENID,
												response.getString(Common.USERINFO_TOKENID));
									}
									e.commit();
									API.Login(OtherLoginActivity.this, userName
											.getText().toString().trim(),
											password.getText().toString()
													.trim(), handler);
								} catch (JSONException e1) {
									e1.printStackTrace();
								}
							}

							@Override
							public void onFailure(int statusCode,
									Header[] headers, String responseString,
									Throwable throwable) {
								super.onFailure(statusCode, headers,
										responseString, throwable);
								throwable.printStackTrace();
							}
						});
			} else {
				System.out.println("has tokenid");
				API.Login(this, userName.getText().toString().trim(), password
						.getText().toString().trim(), handler);
			}
			break;
		case R.id.sign:
			System.out.println("tap sign");
			sendSMS(0);
			break;

		case R.id.forgot:
			System.out.println("tap forget password");
			sendSMS(1);
			break;
		default:
			break;
		}
	}

	/** 隐藏软键盘 */
	private void hideInputMethodManager(View v) {
		/* 隐藏软键盘 */
		InputMethodManager imm = (InputMethodManager) v.getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm.isActive()) {
			imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
		}
	}

	/**
	 * 登录成功之后的跳转
	 */
	private void loginsuccess() {
		// TODO Auto-generated method stub
		System.out.println("loginsuccess----------------");
		dialog.dismiss();
		// 发送广播
		Log.d(TAG, "start push service");
		Intent intent = new Intent();
		intent.setAction("com.receiver.AlertManagerRecriver");
		sendBroadcast(intent);
		intent = new Intent();
		// 判断是否需要到主界面还是到商品界面
		if (null == getIntent().getStringExtra("activity")) {// 没有标记表示进入主界面
			intent.setClass(OtherLoginActivity.this, MainTabActivity.class);
			startActivity(intent);
		}
		finish();
	}

	/** 初始化发短信 **/
	private void initSSMSSDK() {
		SMSSDK.initSDK(this, Common.APPKEY, Common.APPSECRET);
	}

	/**
	 * 第三方短信验证方法
	 * */
	private void sendSMS(final int type) {
		RegisterPage registerPage = new RegisterPage(type, handler);
		registerPage.setRegisterCallback(new EventHandler() {
			public void afterEvent(int event, int result, Object data) {
				System.out.println("type ---- >" + type + ",result--->"
						+ result);
				// 解析注册结果
				if (result == SMSSDK.RESULT_COMPLETE) {
					@SuppressWarnings("unchecked")
					HashMap<String, Object> phoneMap = (HashMap<String, Object>) data;

					// String country = (String) phoneMap.get("country");//
					String phone = (String) phoneMap.get("phone");
					String pwd = (String) phoneMap.get("pwd");
					// 把手机号发送到服务器判断账号是否存在，存在则跳转到重置密码页面
					System.out.println("type -------->" + type);
					if (type == 0) {
						/* 服务器返回手机号不存在，注册
						 * 将验证都再smssdk中区完成 然后回调上来参数 参数有：phone,pwd 拿到值。就直接注册。
						 * 注册成功：跳转到主页ok
						 */
						System.out.println("phone:" + phone);
						System.out.println("pwd:" + pwd);
						/*
						 *	＊＊＊＊交给SignAndLoginPhoneNumberService类来逻辑处理 注册 并 登录
						 */
//						sendSign(phone, pwd);
						new SignAndLoginPhoneNumberUtil(OtherLoginActivity.this, phone, pwd);

					} else if (type == 1) {
						// 服务器返回手机号存在，修改密码
						Intent intent = new Intent(OtherLoginActivity.this,
								SignOrForgetActivity.class);
						intent.putExtra("phone", phone);
						intent.putExtra("type", type);
						startActivity(intent);
					}
				}
			}
		});
		registerPage.show(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AppManager.getInstance().killActivity(this);
	}

}
