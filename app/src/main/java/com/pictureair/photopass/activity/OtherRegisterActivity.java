package com.pictureair.photopass.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.pictureair.photopass.widget.EditTextWithClear;
import com.pictureair.photopass.widget.MyToast;
import com.pictureair.photopass.widget.wheelview.OnWheelScrollListener;
import com.pictureair.photopass.widget.wheelview.WheelView;
import com.pictureair.photopass.widget.wheelview.adapter.NumericWheelAdapter;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 其他注册实现规则 先调用注册的接口（ 返回成功后再调用修改个人信息的接口 ） 修改个人信息，API.updateProfile这个接口
 * 
 * 如果修改日期颜色。在com.byl.datepicker.wheelview.WheelView中
 * 
 * @author bass
 */
public class OtherRegisterActivity extends BaseActivity implements
		OnClickListener {
	// 声明控件
	private ImageView back;
	private EditTextWithClear etEmail, etPwd, etPwd2, etName, etCounry;
	private TextView etYear, etMonth, etDay;
	private RadioGroup rg;
	private RadioButton rbMan, rbWoman;
	private Button btn_submit_sign;
	private MyToast myToast;
	// 变量
	private String sex = "";// 性别
	private String country = "";
	private String birthday = "";
	private String name = "";

	private SharedPreferences sp;// SharedPreferences
	// 日期探矿选择器
	private LinearLayout ll_brith;
	private LayoutInflater inflater = null;
	private WheelView year;
	private WheelView month;
	private WheelView day;
	private int mYear = 1996;// 初始化
	private int mMonth = 0;
	private int mDay = 1;
	private String mYear_Str = "1996";// 初始化
	private String mMonth_Str = "01";
	private String mDay_Str = "01";
	LinearLayout ll;
	TextView tv1, tv2;
	View view = null;
	boolean isMonthSetted = false, isDaySetted = false;
	private static final int GET_IP_SUCCESS = 2;
	private static final int GET_IP_FAILED = 3;

	/*
	 * 监听性别 性别只能是两种 male 或female
	 */
	private RadioGroup.OnCheckedChangeListener mChangeListener = new RadioGroup.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup rg, int id) {
			if (id == rbMan.getId()) {
				sex = "male";
			} else if (id == rbWoman.getId()) {
				sex = "female";
			}
		}
	};

	// 消息机制
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case API.SUCCESS:// sign成功
				System.out.println("login success-------------注册成功，现在进行保存个人信息");
				System.out.println("swx:" + sex);
				// &&&&&&&&&&&&&& 调用个人信息里的进行添加个人资料
				API.updateProfile(sp.getString(Common.USERINFO_TOKENID, ""),
						name, birthday, sex, country, "", handler);
				// finish();
				break;
			case API.UPDATE_PROFILE_SUCCESS:
				// 获取购物车数量
				API.getcartcount(OtherRegisterActivity.this,
						sp.getString(Common.USERINFO_ID, ""), handler);

				break;
			case API.UPDATE_PROFILE_FAILED:
				/**
				 * 注册成功，但是保存个人信息失败的话，提示注册成功，保存个人信息失败。应该跳转到登录页面，
				 * 并且将信息保存在sharedpreference中
				 */
				myToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
				Intent intent = new Intent(OtherRegisterActivity.this,
						OtherLoginActivity.class);
				startActivity(intent);
				finish();
				break;
			case API.FAILURE:
			case API.GET_CART_COUNT_FAILED:
			case API.GET_PPP_FAILED:
			case API.GET_STOREID_FAILED:

				myToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
				break;

			case API.SIGN_FAILED:
				try {
					JSONObject infoJsonObject = (JSONObject) msg.obj;
					if (infoJsonObject.has("type")) {
						if (infoJsonObject.getString("type").equals(
								"shortPassword")) {
							myToast.setTextAndShow(R.string.pwd_is_short,
									Common.TOAST_SHORT_TIME);

						} else if (infoJsonObject.getString("type").equals(
								"existedEmail")) {
							myToast.setTextAndShow(R.string.email_exist,
									Common.TOAST_SHORT_TIME);

						}
					} else {
						myToast.setTextAndShow(R.string.http_failed,
								Common.TOAST_SHORT_TIME);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;

			case API.GET_CART_COUNT_SUCCESS:
				System.out.println("get cart count success------------");
				API.getPPSByUserId(sp.getString(Common.USERINFO_TOKENID, null),
						handler);// 获取pp列表
				break;

			case GET_IP_SUCCESS:
				API.getStoreIdbyIP(msg.obj.toString(), handler);
				break;

			case API.GET_PPS_SUCCESS:// 获取pp列表成功
				/**
				 * 获取pp成功之后，需要放入sharedPrefence中
				 */
				JSONObject ppsJsonObject = (JSONObject) msg.obj;
				// Log.d(TAG, "pps===" + ppsJsonObject);
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
					// Log.d(TAG, "pp size == 0");
				}
				new Thread() {
					public void run() {
						// String netIP = AppUtil.GetNetIp();
						String netIP = "211.95.27.34";
						System.out.println("netIP-----------> " + netIP);
						if (netIP.equals("")) {// 获取失败
							handler.sendEmptyMessage(GET_IP_FAILED);
						} else {// 获取成功
							Message message = handler.obtainMessage();
							message.what = GET_IP_SUCCESS;
							message.obj = netIP;
							handler.sendMessage(message);
						}

					};
				}.start();
				// API.getStoreIdbyIP("140.206.125.195", handler);
				break;

			case GET_IP_FAILED:
			case API.GET_PPS_FAILED:// 获取pp列表失败
				// dialog.dismiss();
				myToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
				break;

			case API.GET_STOREID_SUCCESS:
				System.out.println("get storeid success----------------");
				JSONObject obj = (JSONObject) msg.obj;
				try {
					Editor editor = sp.edit();
					editor.putString(Common.CURRENCY, obj.getString("currency")
							.toString());
					editor.putString(Common.STORE_ID, obj.getString("storeId")
							.toString());
					editor.commit();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				Intent intent2 = new Intent();
				intent2.setAction("com.receiver.AlertManagerRecriver");
				sendBroadcast(intent2);
				Intent i = new Intent();
				i.setClass(OtherRegisterActivity.this, MainTabActivity.class);
				startActivity(i);
				finish();
				break;

			case API.MODIFY_PWD_FAILED:
				System.out.println("signorfotget------modify pwd failed");
				// 提示错误
				break;

			case API.MODIFY_PWD_SUCCESS:
				System.out.println("signorforget------modify pwd success");
				// 跳转至登录界面
				break;

			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_other_register);

		AppManager.getInstance().addActivity(this);// 添加到activity管理器

		initview();// 初始化
	}

	private void initview() {
		myToast = new MyToast(OtherRegisterActivity.this);
		getDateYMD();
		back = (ImageView) findViewById(R.id.login_back);// 返回按键
		etEmail = (EditTextWithClear) findViewById(R.id.other_sign_email);
		etPwd = (EditTextWithClear) findViewById(R.id.other_sign_password);
		etPwd2 = (EditTextWithClear) findViewById(R.id.other_sign_password2);
		etName = (EditTextWithClear) findViewById(R.id.other_sign_name);
		etCounry = (EditTextWithClear) findViewById(R.id.other_sign_country);
		etYear = (TextView) findViewById(R.id.other_sign_year);
		etMonth = (TextView) findViewById(R.id.other_sign_month);
		etDay = (TextView) findViewById(R.id.other_sign_day);

		btn_submit_sign = (Button) findViewById(R.id.btn_other_sign_submit);

		rg = (RadioGroup) findViewById(R.id.rg_sex);// 获取RadioGroup控件
		rbMan = (RadioButton) findViewById(R.id.rb_btn_man);// 获取RadioButton控件;
		rbWoman = (RadioButton) findViewById(R.id.rb_btn_woman);// 获取RadioButton控件;

		rg.setOnCheckedChangeListener(mChangeListener);// 单选框的改变事件
		// 日期选择器
		ll_brith = (LinearLayout) findViewById(R.id.ll_birth);
		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		ll = (LinearLayout) findViewById(R.id.ll);
		ll.addView(getDataPick());
		ll.setVisibility(View.GONE);

		ll_brith.setOnClickListener(this);
		back.setOnClickListener(this);
		btn_submit_sign.setOnClickListener(this);
		etCounry.setOnClickListener(this);
		etYear.setOnClickListener(this);
		etMonth.setOnClickListener(this);
		etDay.setOnClickListener(this);

		// 点击完成后，隐藏软键盘
		etCounry.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					// 隐藏软键盘
					InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(etCounry.getWindowToken(), 0);
					return true;
				}
				return false;
			}
		});
	}

	/**
	 * 点击键盘之外，隐藏键盘
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			View v = getCurrentFocus();
			if (AppUtil.isShouldHideInput(v, ev)) {
				// if (!password.hasFocus() && !userName.hasFocus()) {
				hideInputMethodManager(v);
				// }
			}
			return super.dispatchTouchEvent(ev);
		}
		// 必不可少，否则所有的组件都不会有TouchEvent了
		if (getWindow().superDispatchTouchEvent(ev)) {
			return true;
		}
		return onTouchEvent(ev);
	}

	/** 隐藏软键盘 */
	private void hideInputMethodManager(View v) {
		/* 隐藏软键盘 */
		InputMethodManager imm = (InputMethodManager) v.getContext()
				.getSystemService(INPUT_METHOD_SERVICE);
		if (imm.isActive()) {
			imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
		}
	}

	@Override
	public void onClick(View v) {

		sp = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);

		switch (v.getId()) {
		case R.id.login_back:
			finish();
			break;

		case R.id.btn_other_sign_submit:
			/*
			 * 1.先提交用户名和密码 2.根据修改用户来提交 个人信息。
			 */
			String email = etEmail.getText().toString().trim();
			String pwd = etPwd.getText().toString().trim();
			String pwd2 = etPwd2.getText().toString().trim();
			String name = etName.getText().toString().trim();
			// String year = etYear.getText().toString();
			// String month = etMonth.getText().toString();
			// String day = etDay.getText().toString();
			country = etCounry.getText().toString();

			if (email.isEmpty()) {
				myToast.setTextAndShow(R.string.email_is_empty,
						Common.TOAST_SHORT_TIME);
			} else if (!AppUtil.isEmail(email)) {
				myToast.setTextAndShow(R.string.email_error,
						Common.TOAST_SHORT_TIME);
			} else if (name.isEmpty()) {
				myToast.setTextAndShow(R.string.name_is_empty,
						Common.TOAST_SHORT_TIME);
			} else {
				// 比较密码合法性
				switch (AppUtil.checkPwd(etPwd.getText().toString(), etPwd2
						.getText().toString())) {
				case AppUtil.PWD_ALL_SAPCE:// 全部为空格
					myToast.setTextAndShow(R.string.pwd_no_all_space,
							Common.TOAST_SHORT_TIME);
					break;

				case AppUtil.PWD_AVAILABLE:// 密码可用
					sign(email, pwd);// 注册 用户名和 密码。成功后将保存个人信息
					break;

				case AppUtil.PWD_EMPTY:// 空
					myToast.setTextAndShow(R.string.pwd_is_empty,
							Common.TOAST_SHORT_TIME);
					break;

				case AppUtil.PWD_INCONSISTENCY:// 不一致
					myToast.setTextAndShow(R.string.pw_is_inconsistency,
							Common.TOAST_SHORT_TIME);
					break;

				case AppUtil.PWD_SHORT:// 小于6位
					myToast.setTextAndShow(R.string.notify_password_hint,
							Common.TOAST_SHORT_TIME);

					break;

				case AppUtil.PWD_HEAD_OR_FOOT_IS_SPACE:// 密码首尾不能为空格
					myToast.setTextAndShow(R.string.pwd_head_or_foot_space,
							Common.TOAST_SHORT_TIME);
					break;

				}
			}
			break;

		case R.id.other_sign_month:
		case R.id.other_sign_day:
		case R.id.other_sign_year:
		case R.id.ll_birth:

			// 弹出出生年月日
			if (ll.getVisibility() == View.GONE) {
				ll.startAnimation(AnimationUtils.loadAnimation(this,
						R.anim.slide_in_from_bottom));
				ll.setVisibility(View.VISIBLE);
			} else {
				ll.startAnimation(AnimationUtils.loadAnimation(this,
						R.anim.slide_out_to_bottom));
				ll.setVisibility(View.GONE);
			}
			etEmail.clearFocus();
			etPwd.clearFocus();
			etPwd2.clearFocus();
			etName.clearFocus();
			etCounry.clearFocus();
			break;

		default:
			break;
		}
	}

	/** 注册 */
	private void sign(final String email, final String pwd) {
		// 注册请求
		if (null == sp.getString(Common.USERINFO_TOKENID, null)) {// 需要重新获取一次tokenid
			System.out.println("no tokenid, need to obtain one");
			final StringBuffer sb = new StringBuffer();
			sb.append(Common.BASE_URL).append(Common.GET_TOKENID);// 获取地址

			RequestParams params = new RequestParams();
			params.put(Common.TERMINAL, "android");
			params.put(Common.UUID, Installation.id(this));

			HttpUtil.get(sb.toString(), params, new JsonHttpResponseHandler() {

				// 成功
				public void onSuccess(int statusCode, Header[] headers,
						JSONObject response) {
					super.onSuccess(statusCode, headers, response);
					try {
						System.out.println("tokenid==" + response);
						Editor e = sp.edit();
						if (response.has(Common.USERINFO_TOKENID)) {
							System.out.println("add tokenid=============");
							e.putString(Common.USERINFO_TOKENID,
									response.getString(Common.USERINFO_TOKENID));
						}
						e.commit();
						API.Sign(OtherRegisterActivity.this, email, pwd,
								handler);
					} catch (JSONException e1) {
						e1.printStackTrace();
					}
				}

				// 失败
				@Override
				public void onFailure(int statusCode, Header[] headers,
						Throwable throwable, JSONObject errorResponse) {
					// TODO Auto-generated method stub
					super.onFailure(statusCode, headers, throwable,
							errorResponse);
					myToast.setTextAndShow(R.string.http_failed,
							Common.TOAST_SHORT_TIME);
				}
			});
		} else {
			API.Sign(this, email, pwd, handler);
		}
	}

	/** 初始化读取日期 */
	private Button btnC, btnSubmit;

	@SuppressLint("InflateParams")
	private View getDataPick() {
		Calendar c = Calendar.getInstance();
		int norYear = c.get(Calendar.YEAR);

		int curYear = mYear;
		int curMonth = mMonth;
		int curDate = mDay;

		view = inflater.inflate(R.layout.wheel_date_picker, null);

		// 得到按钮
		btnC = (Button) view.findViewById(R.id.btn_c_date);
		btnSubmit = (Button) view.findViewById(R.id.btn_s_date);

		btnC.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				ll.startAnimation(AnimationUtils.loadAnimation(
						OtherRegisterActivity.this, R.anim.slide_out_to_bottom));
				ll.setVisibility(View.GONE);
			}
		});
		btnSubmit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				etYear.setText(mYear_Str);
				etMonth.setText(mMonth_Str);
				etDay.setText(mDay_Str);
				ll.startAnimation(AnimationUtils.loadAnimation(
						OtherRegisterActivity.this, R.anim.slide_out_to_bottom));
				ll.setVisibility(View.GONE);
			}
		});

		year = (WheelView) view.findViewById(R.id.year);
		NumericWheelAdapter numericWheelAdapter1 = new NumericWheelAdapter(
				this, 1950, norYear); // 最小
		numericWheelAdapter1.setLabel("");// 这里改变
		year.setViewAdapter(numericWheelAdapter1);
		year.setCyclic(true);// 是否可循环滑动
		year.addScrollingListener(scrollListener);

		month = (WheelView) view.findViewById(R.id.month);
		NumericWheelAdapter numericWheelAdapter2 = new NumericWheelAdapter(
				this, 1, 12, "%02d");
		numericWheelAdapter2.setLabel("");
		month.setViewAdapter(numericWheelAdapter2);
		month.setCyclic(true);
		month.addScrollingListener(scrollListener);

		day = (WheelView) view.findViewById(R.id.day);
		initDay(curYear, curMonth);
		day.setCyclic(true);
		day.addScrollingListener(scrollListener);

		year.setVisibleItems(4);// 设置显示行数
		month.setVisibleItems(4);
		day.setVisibleItems(4);

		year.setCurrentItem(curYear - 1950);
		month.setCurrentItem(curMonth - 1);
		day.setCurrentItem(curDate - 1);

		return view;
	}

	private OnWheelScrollListener scrollListener = new OnWheelScrollListener() {
		@Override
		public void onScrollingStarted(WheelView wheel) {
		}

		@Override
		public void onScrollingFinished(WheelView wheel) {
			int n_year = year.getCurrentItem() + 1950;// 年
			int n_month = month.getCurrentItem() + 1;// 月

			initDay(n_year, n_month);

			birthday = new StringBuilder()
					.append((year.getCurrentItem() + 1950))
					.append("-")
					.append((month.getCurrentItem() + 1) < 10 ? "0"
							+ (month.getCurrentItem() + 1) : (month
							.getCurrentItem() + 1))
					.append("-")
					.append(((day.getCurrentItem() + 1) < 10) ? "0"
							+ (day.getCurrentItem() + 1) : (day
							.getCurrentItem() + 1)).toString();

			mYear_Str = birthday.substring(0, 4);
			mMonth_Str = birthday.substring(5, 7);
			mDay_Str = birthday.substring(8, 10);

		}
	};

	/**
	 */
	private void initDay(int arg1, int arg2) {
		NumericWheelAdapter numericWheelAdapter = new NumericWheelAdapter(this,
				1, AppUtil.getDay(arg1, arg2), "%02d");
		numericWheelAdapter.setLabel("");
		day.setViewAdapter(numericWheelAdapter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AppManager.getInstance().killActivity(this);
	}

	private void getDateYMD() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
		String currentDate = sdf.format(date); // 当期日期
		mYear = Integer.parseInt(currentDate.split("-")[0]);
		mMonth = Integer.parseInt(currentDate.split("-")[1]);
		mDay = Integer.parseInt(currentDate.split("-")[2]);

		mYear_Str = mYear + "";
		mMonth_Str = mMonth + "";
		mDay_Str = mDay + "";
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
}
