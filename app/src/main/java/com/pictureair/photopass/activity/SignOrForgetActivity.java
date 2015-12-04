package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.API;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.HttpUtil;
import com.pictureair.photopass.util.Installation;
import com.pictureair.photopass.widget.MyToast;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 注册和修改密码的页面，前提都是通过了手机号码验证才会来到这个页面， 根据intent传递过来的type值判断是注册还是修改密码 type == 0
 * 为注册，type == 1为修改密码
 * PS：暂时因为注册和修改密码界面几乎一样，所以放在一个activity中完成，但后续如果改变注册页面布局的话最好和修改密码分开处理，使业务逻辑清晰化
 * */
public class SignOrForgetActivity extends BaseActivity implements OnClickListener {
	private EditText username, pwd1, pwd2;
	private Button sure;
	private ImageView back;
	private String phone;
	private TextView title;
	private SharedPreferences sp;
	private MyToast newToast;
	private int type;// 判断跳转来自注册还是密码修改 0：注册；1：修改密码；
	private static final int GET_IP_SUCCESS = 3;
	private static final int GET_IP_FAILED = 4;
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case API.SUCCESS://sign成功
				System.out.println("login success-------------");
				API.getcartcount(SignOrForgetActivity.this,sp.getString(Common.USERINFO_ID, ""),handler);
				break;
				
			case API.FAILURE:
			case API.GET_CART_COUNT_FAILED:
			case API.GET_PPP_FAILED:
			case API.GET_STOREID_FAILED:
				newToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
				break;
				
			case API.SIGN_FAILED:
				try {
					JSONObject infoJsonObject = (JSONObject) msg.obj;
					if (infoJsonObject.has("type")) {
						if (infoJsonObject.getString("type").equals("shortPassword")) {
							newToast.setTextAndShow(R.string.pwd_is_short, Common.TOAST_SHORT_TIME);
//						}else {
//							myToast.setTextAndShow(R.string.pwd_is_short, Common.TOAST_SHORT_TIME);
						}else if (infoJsonObject.getString("type").equals("existedEmail")) {
							newToast.setTextAndShow(R.string.email_exist, Common.TOAST_SHORT_TIME);
							
						}
					}else {
						newToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
				
			case API.GET_CART_COUNT_SUCCESS:
				System.out.println("get cart count success------------");
				API.getPPSByUserId(sp.getString(Common.USERINFO_TOKENID, null), handler);
				break;
				
			case GET_IP_SUCCESS:
				API.getStoreIdbyIP(msg.obj.toString(), handler);
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
				new Thread(){
					public void run() {
						String netIP = AppUtil.GetNetIp();
						System.out.println("netIP-----------> "+ netIP);
						if (netIP.equals("")) {//获取失败
							handler.sendEmptyMessage(GET_IP_FAILED);
						}else {//获取成功
							Message message = handler.obtainMessage();
							message.what = GET_IP_SUCCESS;
							message.obj = netIP;
							handler.sendMessage(message);
						}
						
					};
				}.start();
//				API.getStoreIdbyIP("140.206.125.195", handler);
				break;
				
			case GET_IP_FAILED:
			case API.GET_PPS_FAILED:// 获取pp列表失败
//				dialog.dismiss();
				newToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
				break;
				
//			case API.GET_PPP_SUCCESS:
//				System.out.println("get ppp success ----------------");
//				JSONObject ppplistJsonObject = (JSONObject) msg.obj;
//				try {
//					JSONArray ppplistArray = ppplistJsonObject.getJSONArray("PPPList");
//					if (0!=ppplistArray.length()) {//说明有ppp
//						System.out.println("length="+ppplistArray.length());
//						Editor editor = sp.edit();
//						editor.putInt(Common.PPP_COUNT, ppplistArray.length());
//						editor.commit();
//					}
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				API.getStoreIdbyIP("140.206.125.195", handler);
//				break;
				
			case API.GET_STOREID_SUCCESS:
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
				i.setClass(SignOrForgetActivity.this, MainTabActivity.class);
				finish();
				startActivity(i);
				break;
				
			case API.MODIFY_PWD_FAILED:
				System.out.println("signorfotget------modify pwd failed");
				//提示错误
				break;
				
			case API.MODIFY_PWD_SUCCESS:
				System.out.println("signorforget------modify pwd success");
				//跳转至登录界面
				break;
				
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign);
		initView();
	}
	
	private void initView() {
		AppManager.getInstance().addActivity(this);
		
		phone = getIntent().getStringExtra("phone");//获取手机号
		type = getIntent().getIntExtra("type", 0);//注册
		System.out.println("phone------->"+phone+",type---->"+type);
		newToast = new MyToast(this);
		title = (TextView) findViewById(R.id.title);//获取标题名字
		sure = (Button) findViewById(R.id.sure);//
		if (type == 0){//sign
			title.setText(R.string.sign);
			sure.setText(R.string.sign);
		}
		else if (type == 1){//forget
			title.setText(R.string.forget_pwd);
			sure.setText(R.string.confirm);
		}
		username = (EditText) findViewById(R.id.username);
		username.setText(phone);
		pwd1 = (EditText) findViewById(R.id.pwd);
		pwd2 = (EditText) findViewById(R.id.pwd_again);
		back = (ImageView) findViewById(R.id.sign_back);
		back.setOnClickListener(this);
		sure.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		sp = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
		switch (v.getId()) {
		case R.id.sign_back:
			finish();
			break;
		case R.id.sure:
			String p1 = pwd1.getText().toString().trim();
			final String p2 = pwd2.getText().toString().trim();
			//显示不能为空密码
			if (p1.isEmpty()) {
				newToast.setTextAndShow(R.string.pw_null, Common.TOAST_SHORT_TIME);
				break;
			}
			//显示密码不一致
			if (p2.isEmpty() || !p2.equals(p1)) {
				newToast.setTextAndShow(R.string.pw_is_inconsistency, Common.TOAST_SHORT_TIME);
				break;
			}
			// 向服务器发送请求
			if (type == 0) {
				// 注册请求
				if (null == sp.getString(Common.USERINFO_TOKENID, null)) {//需要重新获取一次tokenid
					System.out.println("no tokenid, need to obtain one");
					final StringBuffer sb = new StringBuffer();
					sb.append(Common.BASE_URL).append(Common.GET_TOKENID);//获取地址
					
					RequestParams params = new RequestParams();
					params.put(Common.TERMINAL, "android");
					params.put(Common.UUID, Installation.id(this));
					
					HttpUtil.get(sb.toString(), params, new JsonHttpResponseHandler() {
						@Override
						public void onStart() {
							super.onStart();
							System.out.println("get tokenid start");
						}
						public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
							super.onSuccess(statusCode, headers, response);
							try {
								System.out.println("tokenid=="+response);
								Editor e = sp.edit();
								if (response.has(Common.USERINFO_TOKENID)) {
									System.out.println("add tokenid=============");
									e.putString(Common.USERINFO_TOKENID, response.getString(Common.USERINFO_TOKENID));
								}
								e.commit();
								API.Sign(SignOrForgetActivity.this, phone, p2 , handler);
							} catch (JSONException e1) {
								e1.printStackTrace();
							}
						}

						@Override
						public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
							super.onFailure(statusCode, headers, responseString, throwable);
							throwable.printStackTrace();
							newToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
						}
					});
				}else {
					API.Sign(this, phone, p2 , handler);
				}
			} else if (type == 1) {
				// 修改密码请求
				// 注册请求
				newToast.setTextAndShow(R.string.not_open, Common.TOAST_SHORT_TIME);
//				if (null == sp.getString(Common.USERINFO_TOKENID, null)) {//需要重新获取一次tokenid
//					System.out.println("no tokenid, need to obtain one");
//					final StringBuffer sb = new StringBuffer();
//					sb.append(Common.BASE_URL).append(Common.GET_TOKENID);
//					RequestParams params = new RequestParams();
//					params.put(Common.TERMINAL, "android");
//					params.put(Common.UUID, Installation.id(this));
//					HttpUtil.get(sb.toString(), params, new JsonHttpResponseHandler() {
//						@Override
//						public void onStart() {
//							// TODO Auto-generated method stub
//							super.onStart();
//							System.out.println("get tokenid start");
//						}
//						public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//							// TODO Auto-generated method stub
//							super.onSuccess(statusCode, headers, response);
//							try {
//								System.out.println("tokenid=="+response);
//								Editor e = sp.edit();
//								if (response.has(Common.USERINFO_TOKENID)) {
//									System.out.println("add tokenid=============");
//									e.putString(Common.USERINFO_TOKENID, response.getString(Common.USERINFO_TOKENID));
//								}
//								e.commit();
//								API.modifyPwd(SignOrForgetActivity.this, null, p2, "forget", handler);
//							} catch (JSONException e1) {
//								// TODO Auto-generated catch block
//								e1.printStackTrace();
//							}
//						}
//
//						@Override
//						public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//							// TODO Auto-generated method stub
//							super.onFailure(statusCode, headers, responseString, throwable);
//							throwable.printStackTrace();
//							newToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
//						}
//					});
//				}else {
//					API.modifyPwd(this, null, p2, "forget", handler);
//				}
				
			}
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		AppManager.getInstance().killActivity(this);
	}
}
