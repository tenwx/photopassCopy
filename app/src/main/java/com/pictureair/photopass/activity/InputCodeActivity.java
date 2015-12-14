package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.API;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.HttpUtil;
import com.pictureair.photopass.widget.MyToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

/**
 * 手动输入条码的页面
 * */
public class InputCodeActivity extends BaseActivity implements OnClickListener{
	private Button ok;
	private EditText input;
	private ImageView back;
	private SharedPreferences sp;
	private MyToast newToast;
	private String type;
	private MyApplication application;
	private static int SCAN_SUCCESS = 11;
	private static int SCAN_PPP_SUCCESS = 12;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inputcode);
		newToast = new MyToast(this);
		initview();
	}

	private void initview(){
		sp = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
		back = (ImageView) findViewById(R.id.back);
		ok = (Button) findViewById(R.id.sure);
		input = (EditText) findViewById(R.id.input);
		back.setOnClickListener(this);
		ok.setOnClickListener(this);

		application = (MyApplication) getApplication();
		input.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				if(actionId == EditorInfo.IME_ACTION_GO){ 

					hideInputMethodManager(v);
					System.out.println("done");
					ok.performClick(); 

					return true;  
				}
				return false;
			}
		});

		input.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				System.out.println("up");

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});
	}

	private void hideInputMethodManager(View v) {
		// TODO Auto-generated method stub
		/*隐藏软键盘*/  
		InputMethodManager imm = (InputMethodManager) v  
				.getContext().getSystemService(
						INPUT_METHOD_SERVICE);
		if (imm.isActive()) {  
			imm.hideSoftInputFromWindow(  
					v.getApplicationWindowToken(), 0);  
		} 
	}

	private Handler handler2 = new Handler(){
		public void handleMessage(android.os.Message msg) {

			JSONObject response = null;
			switch (msg.what) {
			case 11://pp
				try {
					response = (JSONObject) msg.obj;
					System.out.println(response+"----------");
					if (response.has("success")) {
						Editor editor = sp.edit();
						editor.putBoolean(Common.NEED_FRESH, true);
						editor.commit();
						newToast.setTextAndShow(R.string.success1, Common.TOAST_SHORT_TIME);
						//						Toast.makeText(MipcaActivityCapture.this, "Success", Toast.LENGTH_SHORT).show();
						finish();
					} else{
						JSONObject errorString = response.getJSONObject("error");
						if (errorString.getString("type").equals("notLogin")) {
							Intent intent = new Intent(InputCodeActivity.this,LoginActivity.class);
							startActivity(intent);
							finish();
						}else {
							if (errorString.getString("type").equals("incomplete")) {
								newToast.setTextAndShow("Incomplete", Common.TOAST_SHORT_TIME);
							}else if(errorString.getString("type").equals("notLogin")){
								newToast.setTextAndShow(R.string.please_login, Common.TOAST_SHORT_TIME);
							}else if(errorString.getString("type").equals("tokenExpired")){
								newToast.setTextAndShow(R.string.Tokenexpired, Common.TOAST_SHORT_TIME);
							}else if (errorString.getString("type").equals("PPHasBind")) {
								newToast.setTextAndShow(R.string.has_used, Common.TOAST_SHORT_TIME);
							}else if (errorString.getString("type").equals("invalidPP")) {
								newToast.setTextAndShow(R.string.invalidPP, Common.TOAST_SHORT_TIME);
							}else if (errorString.getString("type").equals("PPRepeatBound")) {
								newToast.setTextAndShow(R.string.repeatPPbind, Common.TOAST_SHORT_TIME);
							}
							finish();
						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case 12://ppp

				try {
					System.out.println(response+"----------");
					response = (JSONObject) msg.obj;
					//					Log.e("===", "response:"+response);
					if (response.has("success")) {
						//						Log.e("绑定成功", "绑定成功");
						newToast.setTextAndShow(R.string.success1, Common.TOAST_SHORT_TIME);
						//						Toast.makeText(MipcaActivityCapture.this, "Success", Toast.LENGTH_SHORT).show();
						Intent intent = new Intent(InputCodeActivity.this,MyPPPActivity.class);
						API.PPPlist.clear();
						startActivity(intent);
					} else{
						JSONObject errorString = response.getJSONObject("error");
						//						Log.e("errorString", "errorString:"+errorString);
						if (errorString.getString("type").equals("incomplete")) {
							newToast.setTextAndShow("Incomplete", Common.TOAST_SHORT_TIME);
						}else if(errorString.getString("type").equals("errQueryPPP")){
							newToast.setTextAndShow("ErrQueryPPP", Common.TOAST_SHORT_TIME);
						}else if(errorString.getString("type").equals("denyUsePPP")){
							newToast.setTextAndShow("DenyUsePPP", Common.TOAST_SHORT_TIME);
						}else if(errorString.getString("type").equals("PPPHasBind")){
							newToast.setTextAndShow(R.string.has_used, Common.TOAST_SHORT_TIME);
						}else if(errorString.getString("type").equals("invalidPPP")){
							newToast.setTextAndShow(R.string.invalidPPP, Common.TOAST_SHORT_TIME);
						}else if(errorString.getString("type").equals("errSavePPP")){
							newToast.setTextAndShow("ErrSavePPP", Common.TOAST_SHORT_TIME);
						}else if(errorString.getString("type").equals("notLogin")){
							newToast.setTextAndShow(R.string.please_login, Common.TOAST_SHORT_TIME);
						}else if(errorString.getString("type").equals("noPaidForPPP")) {
							newToast.setTextAndShow(R.string.please_pay_first, Common.TOAST_SHORT_TIME);
						}
					}
					finish();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case API.SUCCESS:
				Intent intent = new Intent(InputCodeActivity.this, MyPPPActivity.class);
				API.PPPlist.clear();
				startActivity(intent);
				finish();
				break;
			case API.FAILURE://网络异常
				newToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
				finish();
				break;
			case API.CHECK_CODE_FAILED://返回数据的tip提示，并且结束当前界面
				System.out.println("check code filed");
				response = (JSONObject) msg.obj;
				try {
					if (response.getString("type").equals("incomplete")) {
						//						newToast.setTextAndShow("Incomplete", Common.TOAST_SHORT_TIME);
					}else if (response.getString("type").equals("invalidCode")) {

					}else if (response.getString("type").equals("errQueryUser")) {

					}else if (response.getString("type").equals("PPHasBind")) {

					}else if (response.getString("type").equals("errQueryPPP")) {

					}else if (response.getString("type").equals("noPaidForPPP")) {

					}else if (response.getString("type").equals("invalidPPP")) {

					}else if (response.getString("type").equals("errQueryPhoto")) {

					}
					newToast.setTextAndShow(response.getString("message"), Common.TOAST_SHORT_TIME);
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				finish();
				break;
			case API.CHECK_CODE_SUCCESS://成功返回数据，判断是否已经登录，如果已经登录，则直接绑定，如果没有登录，调转到登录界面，将数据保存起来
				/**
				 * 1.返回数据处理
				 * 2.判断当前是否已经登录
				 * 3.如果已经登录，直接绑定
				 * 4.如果没有登录，将数据保存，并且跳转至登录界面，登录之后进行自动绑定（数据需要存放至公共变量中）
				 */
				response = (JSONObject) msg.obj;
				HashMap<String, String> map = new HashMap<String, String>();
				try {
					if (response.has("codeType")&&"photoPass".equals(response.getString("codeType"))) {
						System.out.println("check pp code success");
						map.put("photoPass", input.getText().toString());
						type = "pp";
					}else if (response.has("codeType")&&"photoPassPlus".equals(response.getString("codeType"))) {
						System.out.println("check pp+ code success");
						map.put("photoPassPlus", input.getText().toString());
						type = "ppp";
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (null == sp.getString(Common.USERINFO_TOKENID, null)) {//没有登录，直接跳转至登录界面，并且保存数据
					//暂时是保持最后一次的数据
					application.clearCodeList();
					application.addObject2CodeList(map);
					newToast.setTextAndShow(R.string.please_login, Common.TOAST_SHORT_TIME);
					Intent intent2 = new Intent(InputCodeActivity.this,LoginActivity.class);
					startActivity(intent2);
					finish();
				}else {//已经登录，直接绑定到用户
					getInfo(input.getText().toString(), type);
				}



				break;
			default:
				break;
			}
		};
	};

	private void getInfo(String code, final String type){
		RequestParams params = new RequestParams();
		System.out.println("scan result="+code+">>"+type);
		params.put(Common.USERINFO_TOKENID, sp.getString(Common.USERINFO_TOKENID, ""));
		String urlString = null;
		if ("pp".equals(type)) {
			if (null!=getIntent().getStringExtra("needbind")&&"false".equals(getIntent().getStringExtra("needbind"))) {//如果是通过pp界面扫描的时候，此处不需要绑定pp到用户
				JSONArray pps = new JSONArray();
				pps.put(code);

				API.bindPPsToPPP(sp.getString(Common.USERINFO_TOKENID, null),pps, getIntent().getStringExtra("binddate"), getIntent().getStringExtra("pppid"), handler2);
				System.out.println("return");
				return;
			}else {//其他界面过来的话，需要绑定到user
				System.out.println("pp");
				params.put(Common.CUSTOMERID, code);
				urlString = Common.BASE_URL+Common.ADD_CODE_TO_USER;
			}
		}else {
			System.out.println("ppp");
			params.put(Common.PPPCode, code);
			urlString = Common.BASE_URL+Common.BIND_PPP_TO_USER;
		}
		System.out.println("return32");

		HttpUtil.get(urlString, params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				if(statusCode == 200){
					if (null != response) {
						Message message = handler2.obtainMessage();
						if ("ppp".equals(type)) {

							message.what = SCAN_PPP_SUCCESS;
						}else if ("pp".equals(type)) {
							message.what = SCAN_SUCCESS;
						}
						message.obj = response;
						handler2.sendMessage(message);
					}else{
						//测试期使用
						//						Toast.makeText(MipcaActivityCapture.this, "出现错误，请重试", Toast.LENGTH_SHORT).show();
						newToast.setTextAndShow(R.string.retry, Common.TOAST_SHORT_TIME);
						finish();
					}
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
			}
		});
	}
	@Override
	public void onClick(View  v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back:
			//			Intent intent = new Intent(this, MipcaActivityCapture.class);
			//			startActivity(intent);
			if (getIntent().getBooleanExtra("needCallBack", false)) {
				setResult(RESULT_CANCELED);
			}
			finish();
			break;
		case R.id.sure:
			if ("".equals(input.getText().toString())) {
				//				Toast.makeText(InputCodeAct.this, R.string.nocontext, Common.TOAST_SHORT_TIME).show();
				newToast.setTextAndShow(R.string.nocontext, Common.TOAST_SHORT_TIME);
			}else if (input.getText().toString().trim().length() != 16) {//长度不一致

				newToast.setTextAndShow(R.string.wrong_length, Common.TOAST_SHORT_TIME);
			}else {
				//如果有键盘显示，把键盘取消掉
				hideInputMethodManager(v);
				if (!getIntent().getBooleanExtra("needCallBack", false)) {//不需要传递回去
					System.out.println("不需要传递回去");
					API.checkCodeAvailable(input.getText().toString().trim().toUpperCase(), handler2);

				}else {//需要传递回去
					System.out.println("需要传递回去");
					Intent intent = new Intent();
					intent.putExtra("code", input.getText().toString().trim().toUpperCase());
					setResult(RESULT_OK, intent);
					finish();
				}
			}
			break;
		default:
			break;
		}
	}
}
