/*
 * 官网地站:http://www.mob.com
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2014年 mob.com. All rights reserved.
 */
package cn.smssdk.gui;

import static cn.smssdk.framework.utils.R.getBitmapRes;
import static cn.smssdk.framework.utils.R.getIdRes;
import static cn.smssdk.framework.utils.R.getLayoutRes;
import static cn.smssdk.framework.utils.R.getStringRes;
import static cn.smssdk.framework.utils.R.getStyleRes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import cn.smssdk.EventHandler;
import cn.smssdk.OnSendMessageHandler;
import cn.smssdk.R;
import cn.smssdk.SMSSDK;
import cn.smssdk.UserInterruptException;
import cn.smssdk.framework.FakeActivity;

/** 短信注册页面 */
public class RegisterPage extends FakeActivity implements OnClickListener,
TextWatcher {

	// 默认使用中国区号
	private static final String DEFAULT_COUNTRY_ID = "42";
	private EventHandler callback; // 事件回调
	// 国家
	private TextView tvCountry;
	// 手机号码
	private EditTextWithClear etPhoneNum;
	// 国家编号
	private TextView tvCountryNum;
	// 下一步按钮
	private Button btnNext;
	private String currentId; // 当前的id
	private String currentCode; // 当前的代码
	private EventHandler handler;// 事件处理
	// 选择后的国家和区号
	private String countryCode = "86";
	private String countryC = "China";
	// 国家号码规则
	private HashMap<String, String> countryRules;
	private Dialog pd;// 对话框
	private OnSendMessageHandler osmHandler;// 发送消息机制
	private int type;// 类型
	private TextView tv_otherRegistered;// 点击其他方式的注册按钮
	private Handler handler2;
	/**
	 * 这里开始加组件， bass
	 * */
	private static final int RETRY_INTERVAL = 60;// 30秒
	private int time = RETRY_INTERVAL;// 时间变量
	private EventHandler handlerIdentify;
	private EditTextWithClear etIdentifyNum, etPwd, etPwd2;// 验证码输入框,密码，再次输入密码
	// private ImageView ivClear2;// 验证码 的 清除按钮
	private BroadcastReceiver smsReceiver;// 广播接收器
	private int SHOWDIALOGTYPE = 1;// SHOWDIALOGTYPE
	private Button btnSubmit;// 提交按钮

	private MyToast myToast;
	// 用来传递一个值到项目中去。其他注册

	public RegisterPage(int type, Handler handler) {
		this.type = type;
		handler2 = handler;
	}

	/** 第一个参数区号／第二个参数国家 */
	public RegisterPage(String code, String g, Handler handler, int type) {
		countryCode = code;
		countryC = g;
		handler2 = handler;
		this.type = type;
		System.out.println("RegisterPage====取到值" + countryCode + countryC);
		// if (!countryCode.equals("00")) {
		// tvCountryNum.setText("+" + countryCode);
		// tvCountry.setText(country);
		// }
	}

	public void setRegisterCallback(EventHandler callback) {
		this.callback = callback;
	}

	public void setOnSendMessageHandler(OnSendMessageHandler h) {
		osmHandler = h;
	}

	public void show(Context context) {
		super.show(context, null);
	}

	public void onCreate() {
		myToast = new MyToast(activity);
		int resId = getLayoutRes(activity, "smssdk_regist_page");// 主界面
		if (resId > 0) {
			activity.setContentView(resId);
			currentId = DEFAULT_COUNTRY_ID;// 默认使用中国区号

			resId = getIdRes(activity, "ll_back");// 退出按钮
			View llBack = activity.findViewById(resId);// 退出按钮
			resId = getIdRes(activity, "tv_title");// 标题
			TextView tv = (TextView) activity.findViewById(resId);// 标题
			if (type == 0) {// sign
				resId = getStringRes(activity, "smssdk_regist");// 注册

			} else if (type == 1) {// forget
				resId = getStringRes(activity, "smssdk_forget");// 忘记密码
			}
			if (resId > 0) {
				tv.setText(resId);// 设置标题
			}

			resId = getIdRes(activity, "rl_country");// 选择国家的按钮
			View viewCountry = activity.findViewById(resId);
			resId = getIdRes(activity, "btn_next");// 下一步按钮
			btnNext = (Button) activity.findViewById(resId);
			btnNext.setEnabled(false);// 先让按钮变灰，不可点击

			resId = getIdRes(activity, "tv_country");// 国家的name
			tvCountry = (TextView) activity.findViewById(resId);

			String[] country = getCurrentCountry(); // 获取当前国.数组
			// String[] country = SMSSDK.getCountry(currentId);
			if (country != null) {
				currentCode = country[1];// 获取国家的区号，例如：中国 86
				tvCountry.setText(country[0]);// 现实国家的名称，例如：中国
			}
			resId = getIdRes(activity, "tv_country_num"); // 找到放区号的输入框
			tvCountryNum = (TextView) activity.findViewById(resId);
			tvCountryNum.setText("+" + currentCode);// 设置输入框，例如 ＋86

			resId = getIdRes(activity, "et_write_phone");// 找到输入框，输入手机号
			etPhoneNum = (EditTextWithClear) activity.findViewById(resId);
			etPhoneNum.setText("");
			etPhoneNum.addTextChangedListener(this);// 添加文本改变的监听事件
			etPhoneNum.requestFocus();// 点击tab键或enter键焦点自动进入下一个输入框
			// 当输入值的时候，显示清空图标
//			if (etPhoneNum.getText().length() > 0) {
//				btnNext.setEnabled(true);// 如果为不能点击，变灰setEnabled(false)
//				// resId = getIdRes(activity, "iv_clear");
//				// ivClear = (ImageView) activity.findViewById(resId);// 找到清除图标
//				// ivClear.setVisibility(View.VISIBLE);// 显示它
//				resId = getBitmapRes(activity, "smssdk_btn_enable");
//				// if (resId > 0) {
//				// btnNext.setBackgroundResource(resId);
//				// }
//			}

			tv_otherRegistered = (TextView) activity.findViewById(getIdRes(
					activity, "tv_otherRegistered"));
			tv_otherRegistered.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					handler2.sendEmptyMessage(1);// 发送消息到loginActivity中
				}
			});

			// 设置选择后的区号
			tvCountryNum.setText("+" + countryCode);
			tvCountry.setText(countryC + "");

			/*
			 * 新增控件初始化
			 */
			initIdentify();

			resId = getIdRes(activity, "iv_clear");
			//			ivClear = (ImageView) activity.findViewById(resId);// 找到清除图标

			llBack.setOnClickListener(this);
			btnNext.setOnClickListener(this);
			//			ivClear.setOnClickListener(this);
			viewCountry.setOnClickListener(this);// 选择国家的按钮

//			smsReceiver = new SMSReceiver(new SMSSDK.VerifyCodeReadListener() {
//				@Override
//				public void onReadVerifyCode(final String verifyCode) {
//					runOnUIThread(new Runnable() {
//						@Override
//						public void run() {
//							etIdentifyNum.setText(verifyCode);
//						}
//					});
//				}
//			});
//			activity.registerReceiver(smsReceiver, new IntentFilter(
//					"android.provider.Telephony.SMS_RECEIVED"));

			handler = new EventHandler() {
				@SuppressWarnings("unchecked")
				public void afterEvent(final int event, final int result,
						final Object data) {

					if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
						/** 提交验证码 */
						afterSubmit(result, data);//
					} else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
						/** 获取验证码成功后的执行动作 */
						afterGet(result, data);
					}

					runOnUIThread(new Runnable() {
						public void run() {
							if (pd != null && pd.isShowing()) {
								pd.dismiss();
							}
							// 回调完成
							if (result == SMSSDK.RESULT_COMPLETE) {
								if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {
									// 请求支持国家列表
									onCountryListGot((ArrayList<HashMap<String, Object>>) data);
								} else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
									// 请求验证码后，跳转到验证码填写页面（请求验证码）
									// afterVerificationCodeRequested();
								}
							} else {
								if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE
										&& data != null
										&& (data instanceof UserInterruptException)) {
									// 由于此处是开发者自己决定要中断发送的，因此什么都不用做
									return;
								}

								// 根据服务器返回的网络错误，给toast提示
								try {
									((Throwable) data).printStackTrace();
									Throwable throwable = (Throwable) data;

									JSONObject object = new JSONObject(
											throwable.getMessage());
									String des = object.optString("detail");
									if (!TextUtils.isEmpty(des)) {
										myToast.setTextAndShow(des, 100);
//										Toast.makeText(activity, des,
//												Toast.LENGTH_SHORT).show();
										return;
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
								// 如果木有找到资源，默认提示
								int resId = getStringRes(activity,
										"smssdk_network_error");
								if (resId > 0) {
									myToast.setTextAndShow(resId, 100);
//									Toast.makeText(activity, resId,
//											Toast.LENGTH_SHORT).show();
								}
							}
						}
					});
				}
			};
		}

	}
	
	private String[] getCurrentCountry() {
		String mcc = getMCC();
		String[] country = null;
		if (!TextUtils.isEmpty(mcc)) {
			country = SMSSDK.getCountryByMCC(mcc);
		}

		if (country == null) {
			Log.w("SMSSDK", "no country found by MCC: " + mcc);
			country = SMSSDK.getCountry(DEFAULT_COUNTRY_ID);
		}
		return country;
	}

	private String getMCC() {
		TelephonyManager tm = (TelephonyManager) activity
				.getSystemService(Context.TELEPHONY_SERVICE);
		// 返回当前手机注册的网络运营商所在国家的MCC+MNC. 如果没注册到网络就为空.
		String networkOperator = tm.getNetworkOperator();

		// 返回SIM卡运营商所在国家的MCC+MNC. 5位或6位. 如果没有SIM卡返回空
		String simOperator = tm.getSimOperator();

		String mcc = null;
		if (!TextUtils.isEmpty(networkOperator)
				&& networkOperator.length() >= 5) {
			mcc = networkOperator.substring(0, 3);
		}

		if (TextUtils.isEmpty(mcc)) {
			if (!TextUtils.isEmpty(simOperator) && simOperator.length() >= 5) {
				mcc = simOperator.substring(0, 3);
			}
		}

		return mcc;
	}

	/** 注册回调接口 */
	public void onResume() {
		SMSSDK.registerEventHandler(handler);
	}

	/** 注销回调接口 */
	public void onPause() {

		SMSSDK.unregisterEventHandler(handler);
	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}

	private void onChangedEditTestiSempet() {
		if (etPhoneNum.getText().toString().length() > 0
				&& etIdentifyNum.getText().toString().length() > 0
				&& etPwd.getText().toString().length() > 0
				&& etPwd2.getText().toString().length() > 0) {

			int rresId = getBitmapRes(activity, "button_blue");
			if (rresId > 0) {
				btnSubmit.setBackgroundResource(rresId);
				btnSubmit.setTextColor(Color.WHITE);
				btnSubmit.setEnabled(true);
			}
		} else {
			int rresId = getBitmapRes(activity, "button_gray");
			if (rresId > 0) {
				btnSubmit.setBackgroundResource(rresId);
				btnSubmit.setTextColor(activity.getResources().getColor(R.color.gray3));
				btnSubmit.setEnabled(false);
			}
		}
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
		onChangedEditTestiSempet();
		if (s.length() > 0) {
			btnNext.setEnabled(true);
			//			ivClear.setVisibility(View.VISIBLE);
		} else {
			btnNext.setEnabled(false);
			//			ivClear.setVisibility(View.GONE);
		}
	}

	public void afterTextChanged(Editable s) {

	}

	public void onClick(View v) {
		int id = v.getId();
		int id_ll_back = getIdRes(activity, "ll_back");
		int id_rl_country = getIdRes(activity, "rl_country");
		int id_btn_next = getIdRes(activity, "btn_next");
		int id_iv_clear = getIdRes(activity, "iv_clear");
		int id_btnSubmit = getIdRes(activity, "sure");

		if (id == id_btnSubmit) {
			/**
			 * 提交按钮， 1. 先验证验证码 2. 成功后，将手机号 区号 传入到LoginActivity中。
			 * ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
			 */
			System.out.println("注册按钮");
			// 提交验证码
			String verificationCode = etIdentifyNum.getText().toString().trim();// 得到验证码
			String phone = etPhoneNum.getText().toString();

			System.out.println("提交按钮————————输入的验证码为 ：" + verificationCode);
			System.out.println("提交按钮————————手机号为 ：" + phone);
			System.out.println("提交按钮————————区号 ：" + countryCode);

			if (!TextUtils.isEmpty(countryCode)) {
				if (pd != null && pd.isShowing()) {
					pd.dismiss();
				}
				pd = CommonDialog.ProgressDialog(activity);
				if (pd != null) {
					pd.show();
				}
				// 提交 区号，手机号，验证码
				SMSSDK.submitVerificationCode(countryCode, phone,
						verificationCode);
			} else {
				int resId = getStringRes(activity, "smssdk_write_identify_code");// 填写验证码
				if (resId > 0) {
					myToast.setTextAndShow(resId, 100);
//					Toast.makeText(getContext(), resId, Toast.LENGTH_SHORT)
//					.show();
				}
			}
			// 验证验证码＝＝＝＝＝＝完毕

		} else if (id == id_ll_back) {
			finish();
		} else if (id == id_rl_country) {
			// 自定义的国家列表
			// handler2.sendEmptyMessage(2);
			// finish();

			CountryPage countryPage = new CountryPage();
			countryPage.setCountryId(currentId);// 当前的id
			countryPage.setCountryRuls(countryRules);// 国家号码的规则
			countryPage.showForResult(activity, null, this);
		} else if (id == id_btn_next) {
			System.out.println("请求发送验证码");
			// 请求发送短信验证码
			if (countryRules == null || countryRules.size() <= 0) {
				if (pd != null && pd.isShowing()) {
					pd.dismiss();
				}
				pd = CommonDialog.ProgressDialog(activity);
				if (pd != null) {
					pd.show();
				}

				SMSSDK.getSupportedCountries();
			} else {
				String phone = etPhoneNum.getText().toString().trim()
						.replaceAll("\\s*", "");
				String code = tvCountryNum.getText().toString().trim();

				checkPhoneNum(phone, code);// 检查电话号码
			}
		} else if (id == id_iv_clear) {
			// 清除电话号码输入框
			etPhoneNum.getText().clear();
		}
	}

	/**
	 * 获取验证码成功后,的执行动作 倒计时
	 * 
	 * @param result
	 * @param data
	 */
	private void afterGet(final int result, final Object data) {

		System.out.println("获取验证码成功后,的执行动作————————afterGet");

		runOnUIThread(new Runnable() {
			public void run() {
				if (pd != null && pd.isShowing()) {
					pd.dismiss();
				}

				if (result == SMSSDK.RESULT_COMPLETE) {
//					int resId = getStringRes(activity,
//							"smssdk_virificaition_code_sent");
//					if (resId > 0) {
//						myToast.setTextAndShow(resId, 100);
////						Toast.makeText(activity, resId, Toast.LENGTH_SHORT)
////						.show();
//					}
					// resId = getStringRes(activity, "smssdk_receive_msg");
					// if (resId > 0) {
					// // String unReceive = getContext().getString(resId,
					// // time);
					// // tvUnreceiveIdentify.setText(Html.fromHtml(unReceive));
					// }
					time = RETRY_INTERVAL;
					countDown();// 倒计时
					// 999;
				} else {
					((Throwable) data).printStackTrace();
					Throwable throwable = (Throwable) data;
					// 根据服务器返回的网络错误，给toast提示
					try {
						JSONObject object = new JSONObject(
								throwable.getMessage());
						String des = object.optString("detail");
						if (!TextUtils.isEmpty(des)) {
							myToast.setTextAndShow(des, 100);
//							Toast.makeText(activity, des, Toast.LENGTH_SHORT)
//							.show();
							return;
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					// / 如果木有找到资源，默认提示
					int resId = getStringRes(activity, "smssdk_network_error");
					if (resId > 0) {
						myToast.setTextAndShow(resId, 100);
//						Toast.makeText(activity, resId, Toast.LENGTH_SHORT)
//						.show();
					}
				}
			}
		});
	}

	/**
	 * 提交验证码成功后的执行事件
	 * 
	 * @param result
	 *            ：获得 SMSSDK 的相关信息，测试 参数为－1
	 * @param data
	 *            ：
	 */
	public void afterSubmit(final int result, final Object data) {
		System.out.println("提交验证码成功后的执行事件————————afterSubmit:参数1:" + result);
		System.out.println("提交验证码成功后的执行事件————————afterSubmit:参数2:"
				+ data.toString());

		runOnUIThread(new Runnable() {
			@SuppressWarnings("unchecked")
			@SuppressLint("NewApi")
			public void run() {
				if (pd != null && pd.isShowing()) {
					pd.dismiss();
				}

				if (!etPwd.getText().toString().isEmpty()
						&& !etPwd2.getText().toString().isEmpty()) {
					if (etPwd.getText().toString()
							.equals(etPwd2.getText().toString())) {

						if (result == SMSSDK.RESULT_COMPLETE) {
							// 当验证码成功的时候
							String pwd = etPwd.getText().toString();
							// 结果_完整
							HashMap<String, Object> res = new HashMap<String, Object>();
							res.put("res", true);
							res.put("phone", data);
							res.put("pwd", pwd);
							// 再需要获取密码
							// 验证码校验返回
							HashMap<String, Object> phoneMap = (HashMap<String, Object>) res
									.get("phone");
							phoneMap.put("pwd", pwd);
							// 32；
//							int resId = getStringRes(activity,
//									"smssdk_your_ccount_is_verified");
//							if (resId > 0) {
//								myToast.setTextAndShow(resId, 100);
////								Toast.makeText(activity, resId,
////										Toast.LENGTH_SHORT).show();
//							}

							if (callback != null) {
								callback.afterEvent(
										SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE,
										SMSSDK.RESULT_COMPLETE, phoneMap);
							}
							System.out.println("验证成功");
							// -------------------------------------------------------------------完成回调－－－－－－－－－－－－－－-----
							finish();
						} else {
							((Throwable) data).printStackTrace();
							// 验证码不正确
							int resId = getStringRes(activity,
									"smssdk_virificaition_code_wrong");
							if (resId > 0) {
								myToast.setTextAndShow(resId, 100);
//								Toast.makeText(activity, resId,
//										Toast.LENGTH_SHORT).show();
							}
						}
					} else {
						// 密码不一致
						etPwd.setText("");
						etPwd2.setText("");
						int resId = getStringRes(activity, "smssdk_pwd_no");
						if (resId > 0) {
							myToast.setTextAndShow(resId, 100);
//							Toast.makeText(activity, resId, Toast.LENGTH_SHORT)
//							.show();
						}
					}

				} else {
					// 密码不能为空
					int resId = getStringRes(activity, "smssdk_pwd_is_empty");
					if (resId > 0) {
						myToast.setTextAndShow(resId, 100);
//						Toast.makeText(activity, resId, Toast.LENGTH_SHORT)
//						.show();
					}
				}

			}
		});
	}

	private void countDown() {
		runOnUIThread(new Runnable() {
			public void run() {
				time--;
				if (time == 0) {
					// 倒计时完毕后

					int resId = getStringRes(activity, "smssdk_send_code2");// 再次发送验证码
					if (resId > 0) {
						btnNext.setText(resId);
					}

					resId = getBitmapRes(activity, "button_green");
					if (resId > 0) {
						btnNext.setBackgroundResource(resId);
					}

					btnNext.setEnabled(true);

					time = RETRY_INTERVAL;// time30秒
				} else {
					int resId = getStringRes(activity, "smssdk_receive_msg");// 倒计时
					// n
					// 秒
					if (resId > 0) {
						String unReceive = getContext().getString(resId, time);

						resId = getBitmapRes(activity, "button_gray");
						if (resId > 0) {
							btnNext.setBackgroundResource(resId);
						}

						btnNext.setText(Html.fromHtml(unReceive));
					}
					btnNext.setEnabled(false);
					runOnUIThread(this, 1000);
				}
			}
		}, 1000);
	}

	/** 请求支持国家列表 */
	private void onCountryListGot(ArrayList<HashMap<String, Object>> countries) {
		// 解析国家列表
		for (HashMap<String, Object> country : countries) {
			String code = (String) country.get("zone");// 区
			String rule = (String) country.get("rule");// 规则

			// 如果是空的自动返回
			if (TextUtils.isEmpty(code) || TextUtils.isEmpty(rule)) {
				continue;
			}

			if (countryRules == null) {
				countryRules = new HashMap<String, String>();
			}
			countryRules.put(code, rule);
		}
		// 检查手机号码
		String phone = etPhoneNum.getText().toString().trim()
				.replaceAll("\\s*", "");
		String code = tvCountryNum.getText().toString().trim();
		checkPhoneNum(phone, code);// 检查手机号码
	}

	/** 分割电话号码 */
	private String splitPhoneNum(String phone) {
		StringBuilder builder = new StringBuilder(phone);
		builder.reverse();
		for (int i = 4, len = builder.length(); i < len; i += 5) {
			builder.insert(i, ' ');
		}
		builder.reverse();
		return builder.toString();
	}

	/**
	 * 点击发送验证码过后 检查电话号码
	 */
	private void checkPhoneNum(String phone, String code) {
		System.out.println("phone:" + phone + ",code:" + code);
		// 如果手机号的开头有“＋”号，就删除掉
		if (code.startsWith("+")) {
			code = code.substring(1);
		}
		System.out.println("phone:" + phone + ",code:" + code);

		// 如果手机号为空，就提示手机号不能为空
		if (TextUtils.isEmpty(phone)) {
			int resId = getStringRes(activity, "smssdk_write_mobile_phone");
			if (resId > 0) {
				myToast.setTextAndShow(resId, 100);
//				Toast.makeText(getContext(), resId, Toast.LENGTH_SHORT).show();
			}
			return;
		}
		String rule = countryRules.get(code);
		System.out.println("rule:" + rule);
		Pattern p = Pattern.compile(rule);
		Matcher m = p.matcher(phone);
		int resId = 0;
		// 如果手机号验证不通过。则弹出“请填写正确的手机号码”
		if (!m.matches()) {
			resId = getStringRes(activity, "smssdk_write_right_mobile_phone");
			if (resId > 0) {
				myToast.setTextAndShow(resId, 100);
//				Toast.makeText(getContext(), resId, Toast.LENGTH_SHORT).show();
			}
			return;
		}
		System.out.println("手机号验证成功phone:" + phone + ",code:" + code);
		// 提示框：是否发送验证码
		showDialog(phone, code);
	}

	/** 是否请求发送验证码，对话框 */
	public void showDialog(final String phone, final String code) {
		int resId = getStyleRes(activity, "CommonDialog");// 加载对话框 类
		if (resId > 0) {
			final String phoneNum = "+" + code + " " + splitPhoneNum(phone);// 分割电话号码
			final Dialog dialog = new Dialog(getContext(), resId);
			resId = getLayoutRes(activity, "smssdk_send_msg_dialog");// 加载一个xml对话框
			if (resId > 0) {
				dialog.setContentView(resId);
				resId = getIdRes(activity, "tv_phone");
				((TextView) dialog.findViewById(resId)).setText(phoneNum);
				resId = getIdRes(activity, "tv_dialog_hint");// 提示语
				TextView tv = (TextView) dialog.findViewById(resId);
				resId = getStringRes(activity, "smssdk_make_sure_mobile_detail");// 我们将发送～等这些提示语
				if (resId > 0) {
					String text = getContext().getString(resId);
					tv.setText(Html.fromHtml(text));
				}
				resId = getIdRes(activity, "btn_dialog_ok");
				// 选择发送短信
				if (resId > 0) {
					((Button) dialog.findViewById(resId))
					.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							// 跳转到验证码页面
							dialog.dismiss();

							if (pd != null && pd.isShowing()) {
								pd.dismiss();
							}
							pd = CommonDialog.ProgressDialog(activity);
							if (pd != null) {
								pd.show();
							}
							Log.e("verification phone ==>>", phone);

							// 开始发送短信
							System.out.println("提示框，确定按钮触动，确认号码："
									+ code + "===" + phone.trim());
							// 86===18771711071 验证的时候 没有＋号
							SMSSDK.getVerificationCode(code,
									phone.trim(), osmHandler);
						}
					});
				}
				// 取消发送短信
				resId = getIdRes(activity, "btn_dialog_cancel");
				if (resId > 0) {
					((Button) dialog.findViewById(resId))
					.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
				}
				dialog.setCanceledOnTouchOutside(true);// 可以在提示框外面触摸取消
				dialog.show();
			}
		}
	}

	/**
	 * 这个方法写入验证码输入界面的组件，第二次修改的界面 note：这个 btnSubmit 提交按钮是提交验证码的按钮 1.需要合并 ‘注册’ 按钮
	 * 当点击‘注册’按钮的时候，先验证验证码。再进行保存密码
	 */
	private void initIdentify() {

		// 提交按钮
		int reId2 = getIdRes(activity, "sure");
		btnSubmit = (Button) activity.findViewById(reId2);
		btnSubmit.setOnClickListener(this);
		btnSubmit.setEnabled(false);
		// 输入验证码
		reId2 = getIdRes(activity, "et_put_identify");
		etIdentifyNum = (EditTextWithClear) activity.findViewById(reId2);
		etIdentifyNum.addTextChangedListener(new MyTextWatcher());
		// 第一个密码输入框
		reId2 = getIdRes(activity, "pwd");
		etPwd = (EditTextWithClear) activity.findViewById(reId2);
		etPwd.addTextChangedListener(new MyTextWatcher());
		// 第二个密码输入框
		reId2 = getIdRes(activity, "pwd_again");
		etPwd2 = (EditTextWithClear) activity.findViewById(reId2);
		etPwd2.addTextChangedListener(new MyTextWatcher());
	}

	private class MyTextWatcher implements TextWatcher{

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
			onChangedEditTestiSempet();
		}

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub

		}

	}

	@SuppressWarnings("unchecked")
	public void onResult(HashMap<String, Object> data) {
		if (data != null) {
			int page = (Integer) data.get("page");
			if (page == 1) {
				// 国家列表返回
				currentId = (String) data.get("id");
				countryRules = (HashMap<String, String>) data.get("rules");
				String[] country = SMSSDK.getCountry(currentId);
				if (countryC != null) {

					currentCode = country[1];
					tvCountryNum.setText("+" + currentCode);
					tvCountry.setText(country[0]);
					// bass自定义的国家列表
					// tvCountry.setText("" + country);
					// tvCountryNum.setText("" + countryCode);
				}
			}
		}
	}

}
