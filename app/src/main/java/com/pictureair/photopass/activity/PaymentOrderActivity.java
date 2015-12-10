package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alipay.sdk.app.PayTask;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.paypal.android.sdk.payments.ShippingAddress;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.alipay.PayResult;
import com.pictureair.photopass.entity.OrderInfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.API;
import com.pictureair.photopass.util.AliPayUtil;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.HttpUtil;
import com.pictureair.photopass.util.PaypalUtil;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.MyToast;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import com.pictureair.photopass.wxpay.Constants;
import com.pictureair.photopass.wxpay.MD5;
import com.pictureair.photopass.wxpay.Util;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PaymentOrderActivity extends BaseActivity implements
		OnClickListener {
	private ImageView lrtLayout;
	private TextView sbmtButton;
	private String nameString;
	private String priceString;
	private String introductString;

	private RelativeLayout pickupLayout;
	private RelativeLayout zfbLayout;
	private RelativeLayout ylLayout;
	private RelativeLayout paypalLayout;
	private RelativeLayout wechatLayout;

	private ImageView zfButton;
	private ImageView yhkButton;
	private ImageView paypalButton;
	private ImageView wechatButton;

	private TextView pickupTextView;
	// private TextView addressTextView;

	private static final int RQF_SUCCESS = 1;

	private static final int RQF_CANCEL = 2;

	private static final int RQF_ERROR = 3;

	private static final int INITPAYPAL = 4;

	private int payType;// 支付类型 0 支付宝 1 银联 2 VISA信用卡 3 代付 4 分期 5 自提 6 paypal 7
						// wechat

	private SharedPreferences sPreferences;

	private String orderid = "";

	private MyToast newToast;

	private OrderInfo orderInfo;

	private MyApplication myApplication;

	private boolean needAddress = false;

	private boolean isPaying = false;

	/**
	 * - Set to PayPalConfiguration.ENVIRONMENT_PRODUCTION to move real money.
	 * 
	 * - Set to PayPalConfiguration.ENVIRONMENT_SANDBOX to use your test
	 * credentials from https://developer.paypal.com
	 * 
	 * - Set to PayPalConfiguration.ENVIRONMENT_NO_NETWORK to kick the tires
	 * without communicating to PayPal's servers.
	 */
	private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_NO_NETWORK;

	// note that these credentials will differ between live & sandbox
	// environments.
	private static final String CONFIG_CLIENT_ID = "credential from developer.paypal.com";// 账号注册号之后就会有这个ID

	private static final String TAG = "PaymentOrderActivity";

	private static final int REQUEST_CODE_PAYMENT = 1;
	private static final int REQUEST_CODE_FUTURE_PAYMENT = 2;
	private static final int REQUEST_CODE_PROFILE_SHARING = 3;

	private static PayPalConfiguration config = new PayPalConfiguration()
			.environment(CONFIG_ENVIRONMENT).clientId(CONFIG_CLIENT_ID);

	// -----------微信支付参数----------------//
	PayReq req;
	final IWXAPI msgApi = WXAPIFactory.createWXAPI(this, null);
	Map<String, String> resultunifiedorder;
	StringBuffer sb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment_order);
		findViewById();
		init();
	}

	private void findViewById() {
		lrtLayout = (ImageView) findViewById(R.id.lrt);
		sbmtButton = (TextView) findViewById(R.id.button_smpm);
		// 支付方式选择
		zfButton = (ImageView) findViewById(R.id.imageButton1_zfb);
		yhkButton = (ImageView) findViewById(R.id.imageButton2_yhk);
		paypalButton = (ImageView) findViewById(R.id.imageButton3_paypal);
		wechatButton = (ImageView) findViewById(R.id.imageButton2_weixin);
		zfbLayout = (RelativeLayout) findViewById(R.id.zfb);
		ylLayout = (RelativeLayout) findViewById(R.id.yl);
		paypalLayout = (RelativeLayout) findViewById(R.id.paypal);
		wechatLayout = (RelativeLayout) findViewById(R.id.weixin);
		// 运送方式选择
		pickupLayout = (RelativeLayout) findViewById(R.id.pickup);
		pickupTextView = (TextView) findViewById(R.id.textView4);
		// addressTextView = (TextView)findViewById(R.id.addressTextView);
	}

	private void init() {
		sPreferences = getSharedPreferences(Common.USERINFO_NAME,
				MODE_PRIVATE);
		newToast = new MyToast(this);
		myApplication = (MyApplication) getApplication();

		// 初始化paypal
		InitPayPal();

		// 获取wechat的实例对象
		req = new PayReq();
		sb = new StringBuffer();

		msgApi.registerApp(Constants.APP_ID);

		lrtLayout.setOnClickListener(this);
		sbmtButton.setOnClickListener(this);
		zfButton.setImageResource(R.drawable.sele);
		yhkButton.setImageResource(R.drawable.nosele);
		paypalButton.setImageResource(R.drawable.nosele);
		wechatButton.setImageResource(R.drawable.nosele);

		zfbLayout.setOnClickListener(this);
		ylLayout.setOnClickListener(this);
		paypalLayout.setOnClickListener(this);
		wechatLayout.setOnClickListener(this);

		if (getIntent().getStringExtra("flag") == null) {// 为空，说明是正常流程进入
			nameString = getIntent().getStringExtra("name");// 获取name
			priceString = getIntent().getStringExtra("price");// 获取price
			introductString = getIntent().getStringExtra("introduce");// 获取介绍信息
			orderid = getIntent().getStringExtra("orderId");

			pickupLayout.setOnClickListener(this);
		} else if ("order".equals(getIntent().getStringExtra("flag"))) {// 从订单页面进入
			orderInfo = getIntent().getParcelableExtra("deliveryInfo");
			orderid = orderInfo.orderId;
			// 此处信息，获取比较麻烦，暂时写死
			nameString = "PictureAir";
			priceString = orderInfo.orderTotalPrice + "";
			introductString = "Made by PictureAir";
		}

		needAddress = getIntent().getBooleanExtra("addressType", false);
		if (!needAddress) {// 不需要地址
			// pickupTextView.setVisibility(View.GONE);
			pickupTextView.setText(R.string.noexpress);
		} else {
			pickupTextView.setText(getString(R.string.pick_up_address)
					+ getString(R.string.disney_address));
		}
	}

	/**
	 * 初始化PayPal
	 */
	private void InitPayPal() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, PayPalService.class);
		intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
		startService(intent);
	}

	/**
	 * 支付函数
	 * 
	 * @param orderId
	 *            订单号
	 */
	private void pay(String orderId) {
		// TODO Auto-generated method stub
		if (0 == payType) {// 支付宝支付方式
			try {
				String info = AliPayUtil.getOrderInfo(orderId, nameString,
						introductString);
				System.out.println("info:" + info);
				// 对订单做RSA 签名
				String sign = AliPayUtil.sign(info);
				System.out.println("sign:" + sign);
				// 仅需对sign 做URL编码
				sign = URLEncoder.encode(sign, "UTF-8");
				// 完整的符合支付宝参数规范的订单信息
				final String payInfo = info + "&sign=\"" + sign + "\"&"
						+ AliPayUtil.getSignType();
				Log.i("ExternalPartner", "start pay");
				// start the pay.
				new Thread() {
					public void run() {
						PayTask alipay = new PayTask(PaymentOrderActivity.this);
						// 设置为沙箱模式，不设置默认为线上环境
						// alipay.setSandBox(true);
						// 调用支付接口，获取支付结果
						String result = alipay.pay(payInfo);
						// Result result = new
						// Result(alipay.pay(orderInfo).toString());
						System.out.println("pay info=" + result);
						PayResult payResult = new PayResult(result);
						// String resultInfo = payResult.getResult();

						String resultStatus = payResult.getResultStatus();
						Message msg = new Message();
						if (TextUtils.equals(resultStatus, "9000")) {// 支付成功
							msg.what = RQF_SUCCESS;
						} else if (TextUtils.equals(resultStatus, "6001")) {// 用户中途取消
							msg.what = RQF_CANCEL;
						} else if (TextUtils.equals(resultStatus, "4000")) {// 订单支付失败
							msg.what = RQF_ERROR;
						} else if (TextUtils.equals(resultStatus, "8000")) {// 正在处理中
							msg.what = RQF_SUCCESS;
						} else if (TextUtils.equals(resultStatus, "6002")) {// 网络连接出错
							msg.what = RQF_ERROR;
						}
						mHandler.sendMessage(msg);
					}
				}.start();

			} catch (Exception ex) {
				ex.printStackTrace();
				newToast.setTextAndShow(R.string.pay_failed,
						Common.TOAST_SHORT_TIME);
			}
		} else if (1 == payType) {
			System.out.println("yl");
			mHandler.sendEmptyMessage(RQF_SUCCESS);
		} else if (6 == payType) {
			System.out.println("paypal");
			/*
			 * PAYMENT_INTENT_SALE will cause the payment to complete
			 * immediately. Change PAYMENT_INTENT_SALE to -
			 * PAYMENT_INTENT_AUTHORIZE to only authorize payment and capture
			 * funds later. - PAYMENT_INTENT_ORDER to create a payment for
			 * authorization and capture later via calls from your server.
			 * 
			 * Also, to include additional payment details and an item list, see
			 * getStuffToBuy() below.
			 */
			PayPalPayment thingToBuy = PaypalUtil.getStuffToBuy(
					PayPalPayment.PAYMENT_INTENT_SALE, nameString);
			addAppProvidedShippingAddress(thingToBuy);
			enableShippingAddressRetrieval(thingToBuy, true);
			/*
			 * See getStuffToBuy(..) for examples of some available payment
			 * options.
			 */

			Intent intent = new Intent(this, PaymentActivity.class);

			// send the same configuration for restart resiliency
			intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

			intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);

			startActivityForResult(intent, REQUEST_CODE_PAYMENT);

		} else if (payType == 7) {
			System.out.println("wechat");
			// 调起微信支付
			// 生成prepay_id
			GetPrepayIdTask getPrepayId = new GetPrepayIdTask();
			getPrepayId.execute();
		} else {
			System.out.println("other");
		}
	}

	/*
	 * 添加收货地址到paypal
	 */
	private void addAppProvidedShippingAddress(PayPalPayment paypalPayment) {
		ShippingAddress shippingAddress = new ShippingAddress()
				.recipientName("alieen").line1("abc").city("上海").state("浦东")
				.postalCode("0011").countryCode("US");
		paypalPayment.providedShippingAddress(shippingAddress);
	}

	/*
	 * 是否打开地址检索功能，如果true，则会从paypal账号中获取地址，如果false，不会获取地址
	 */
	private void enableShippingAddressRetrieval(PayPalPayment paypalPayment,
			boolean enable) {
		paypalPayment.enablePayPalShippingAddressesRetrieval(enable);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.lrt:// 返回
			finish();
			break;

		case R.id.button_smpm:// 提交支付
			sbmtButton.setEnabled(false);
			System.out.println("-------------pay on click");
			// 直接调用接口
			pay(orderid);
			break;

		case R.id.zfb:// 选择支付宝支付
			payType = 0;
			zfButton.setImageResource(R.drawable.sele);
			yhkButton.setImageResource(R.drawable.nosele);
			paypalButton.setImageResource(R.drawable.nosele);
			wechatButton.setImageResource(R.drawable.nosele);
			System.out.println("ZFB");
			break;

		case R.id.yl:// 选择银联支付
			payType = 1;
			zfButton.setImageResource(R.drawable.nosele);
			yhkButton.setImageResource(R.drawable.sele);
			paypalButton.setImageResource(R.drawable.nosele);
			wechatButton.setImageResource(R.drawable.nosele);
			System.out.println("YL");
			break;

		case R.id.paypal:// paypal支付
			payType = 6;
			zfButton.setImageResource(R.drawable.nosele);
			yhkButton.setImageResource(R.drawable.nosele);
			paypalButton.setImageResource(R.drawable.sele);
			wechatButton.setImageResource(R.drawable.nosele);
			System.out.println("PAYPAL");
			break;

		case R.id.weixin:// wechat支付
			payType = 7;
			zfButton.setImageResource(R.drawable.nosele);
			yhkButton.setImageResource(R.drawable.nosele);
			paypalButton.setImageResource(R.drawable.nosele);
			wechatButton.setImageResource(R.drawable.sele);
			System.out.println("WECHAT");
			break;

		default:
			break;
		}
	}

	// 支付成功回调函数
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			// paypal支付成功
			PaymentConfirmation confirm = data
					.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
			if (confirm != null) {
				try {
					Log.i("PayPal", confirm.toJSONObject().toString(4));
					Log.i("PayPal", confirm.getPayment().toJSONObject()
							.toString(4));
					/**
					 * TODO: send 'confirm' (and possibly confirm.getPayment()
					 * to your server for verification or consent completion.
					 * See https://developer.paypal.com/webapps/developer/docs/
					 * integration/mobile/verify-mobile-payment/ for more
					 * details.
					 * 
					 * For sample mobile backend interactions, see
					 * https://github
					 * .com/paypal/rest-api-sdk-python/tree/master/
					 * samples/mobile_backend
					 */

				} catch (JSONException e) {
					Log.e("PayPal", "an extremely unlikely failure occurred: ",
							e);
				}
			}
			// String payKey =
			// data.getStringExtra(PayPalActivity.EXTRA_PAY_KEY);
			// System.out.println("paypal pay success"+payKey);
			mHandler.sendEmptyMessage(RQF_SUCCESS);
		} else if (resultCode == RESULT_CANCELED) {
			// paypal取消支付
			System.out.println("paypal pay cancel");
			mHandler.sendEmptyMessage(RQF_CANCEL);
		} else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
			// paypal付款失败
			mHandler.sendEmptyMessage(RQF_ERROR);
		} else if (resultCode == 30) {// 取消添加收货地址

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	// 支付失败处理
	private void ErrorInPayment() {
		// TODO Auto-generated method stub
		newToast.setTextAndShow(R.string.alipay_error, Common.TOAST_SHORT_TIME);
		// myApplication.setIsBuyingPhotoInfo(null);
		myApplication.clearIsBuyingPhotoList();
		myApplication.setRefreshViewAfterBuyBlurPhoto("");
		AppManager.getInstance().killActivity(SubmitOrderActivity.class);
		AppManager.getInstance().killActivity(PreviewProductActivity.class);
		// AppManager.getInstance().killActivity(BlurActivity.class);
		AppManager.getInstance().killActivity(PreviewPhotoActivity.class);
		AppManager.getInstance().killActivity(SelectPhotoActivity.class);
		AppManager.getInstance().killActivity(MakegiftActivity.class);
		finish();
	}

	// 取消操作处理
	private void CancelInPayment() {
		// TODO Auto-generated method stub
		// myApplication.setIsBuyingPhotoInfo(null);
		myApplication.clearIsBuyingPhotoList();
		myApplication.setRefreshViewAfterBuyBlurPhoto("");
		newToast.setTextAndShow(R.string.cancel_deal, Common.TOAST_SHORT_TIME);
		AppManager.getInstance().killActivity(SubmitOrderActivity.class);
		AppManager.getInstance().killActivity(PreviewProductActivity.class);
		// AppManager.getInstance().killActivity(BlurActivity.class);
		AppManager.getInstance().killActivity(SelectPhotoActivity.class);
		AppManager.getInstance().killActivity(PreviewPhotoActivity.class);
		AppManager.getInstance().killActivity(MakegiftActivity.class);
		finish();
	}

	// 成功支付之后的操作
	private void SuccessAfterPayment() {
		// TODO Auto-generated method stub
		System.out.println("start finish expired activity");
		AppManager.getInstance().killActivity(SubmitOrderActivity.class);
		AppManager.getInstance().killActivity(PreviewProductActivity.class);
		// AppManager.getInstance().killActivity(BlurActivity.class);
		AppManager.getInstance().killActivity(PreviewPhotoActivity.class);
		AppManager.getInstance().killActivity(SelectPhotoActivity.class);
		AppManager.getInstance().killActivity(DetailProductActivity.class);
		AppManager.getInstance().killActivity(PPPDetailProductActivity.class);
		AppManager.getInstance().killActivity(MakegiftActivity.class);
		AppManager.getInstance().killActivity(OrderActivity.class);

	}

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			// Result result = new Result((String) msg.obj);
			// System.out.println((String) msg.obj);
			// sbmtButton.setEnabled(true);
			switch (msg.what) {
			case RQF_ERROR:
				Intent intent1 = new Intent(PaymentOrderActivity.this,
						OrderActivity.class);
				startActivity(intent1);
				ErrorInPayment();
				break;
			case RQF_CANCEL:
				Intent intent2 = new Intent(PaymentOrderActivity.this,
						OrderActivity.class);
				startActivity(intent2);
				CancelInPayment();
				break;
			case RQF_SUCCESS:
				System.out.println("RQF_PAY-----------");
				/********* 测试阶段，需要做一个修改订单状态的接口 ************/
				RequestParams requestParams = new RequestParams();
				requestParams.put("orderId", orderid);
				requestParams.put("orderPayStatus", 2);// 修改订单状态，2表示已经支付
				requestParams.put("payType", payType);
				requestParams.put("userId",
						sPreferences.getString(Common.USERINFO_ID, ""));
				System.out.println("orderid" + orderid + "_userId_"
						+ sPreferences.getString(Common.USERINFO_ID, ""));
				HttpUtil.post(Common.BASE_URL + "/shopping/modifyOrderStatus",
						requestParams, new JsonHttpResponseHandler() {
							public void onSuccess(int statusCode,
									Header[] headers, JSONObject response) {
								System.out.println(response + "result");
								try {
									JSONObject resultJsonObject = response
											.getJSONObject("result");
									if ("success".equals(resultJsonObject
											.getString("type"))) {

										if (resultJsonObject.has("pppCode")) {// 判断是否购买ppp，如果购买ppp，则去ppp页面，否则去订单界面
											System.out.println(resultJsonObject
													.getString("pppCode"));
											if (null == resultJsonObject
													.getString("pppCode")
													|| "null"
															.equals(resultJsonObject
																	.getString("pppCode"))) {// 去订单页面
												System.out
														.println("------------------->buy photo");

												// 获取存放的photopass信息
												// PhotoInfo photopassmap =
												// myApplication.getIsBuyingPhotoInfo();
												Intent intent;
												// 以下三种情况要回到清晰图片页面
												if (myApplication
														.getRefreshViewAfterBuyBlurPhoto()
														.equals(Common.FROM_VIEWORSELECTACTIVITY)
														|| myApplication
																.getRefreshViewAfterBuyBlurPhoto()
																.equals(Common.FROM_MYPHOTOPASS)
														|| myApplication
																.getRefreshViewAfterBuyBlurPhoto()
																.equals(Common.FROM_BLUR)) {
													System.out.println("flag is -------------------->"
															+ myApplication
																	.getRefreshViewAfterBuyBlurPhoto());
													myApplication
															.setPhotoIsPaid(true);
													ArrayList<PhotoInfo> photopassArrayList = new ArrayList<PhotoInfo>();
													photopassArrayList
															.addAll(myApplication
																	.getIsBuyingPhotoInfoList());
													// 找出购买的info，并且将购买属性改为1
													photopassArrayList.get(myApplication
															.getIsBuyingIndex()).isPayed = 1;

													intent = new Intent(
															PaymentOrderActivity.this,
															PreviewPhotoActivity.class);
													intent.putExtra("activity",
															"paymentorderactivity");
													intent.putExtra(
															"position",
															myApplication
																	.getIsBuyingIndex()
																	+ "");// 在那个相册中的位置
													intent.putExtra(
															"photoId",
															photopassArrayList
																	.get(myApplication
																			.getIsBuyingIndex()).photoId);
													intent.putExtra("photos",
															photopassArrayList);// 那个相册的全部图片路径
													intent.putExtra(
															"targetphotos",
															myApplication.magicPicList);
													// 清空标记
													myApplication
															.clearIsBuyingPhotoList();

													SuccessAfterPayment();

													if (myApplication
															.getRefreshViewAfterBuyBlurPhoto()
															.equals(Common.FROM_VIEWORSELECTACTIVITY)) {
														myApplication
																.setRefreshViewAfterBuyBlurPhoto(Common.FROM_VIEWORSELECTACTIVITYANDPAYED);
													} else if (myApplication
															.getRefreshViewAfterBuyBlurPhoto()
															.equals(Common.FROM_MYPHOTOPASS)) {
														myApplication
																.setRefreshViewAfterBuyBlurPhoto(Common.FROM_MYPHOTOPASSPAYED);
													} else if (myApplication
															.getRefreshViewAfterBuyBlurPhoto()
															.equals(Common.FROM_BLUR)) {
														myApplication
																.setRefreshViewAfterBuyBlurPhoto("");
													}

												} else {
													// 回到订单页面
													intent = new Intent(
															PaymentOrderActivity.this,
															OrderActivity.class);
													intent.putExtra("flag",
															"two");
													SuccessAfterPayment();
												}

												Editor editor = sPreferences
														.edit();
												editor.putBoolean(
														Common.NEED_FRESH, true);
												editor.commit();
												startActivity(intent);
												finish();
											} else {// 去ppp页面
												System.out
														.println("----------------->buy ppp");
												Editor editor = sPreferences
														.edit();
												editor.putBoolean(
														Common.NEED_FRESH, true);
												editor.commit();
												myApplication
														.setNeedRefreshPPPList(true);
												Intent intent = new Intent(
														PaymentOrderActivity.this,
														MyPPPActivity.class);
												API.PPPlist.clear();
												SuccessAfterPayment();
												startActivity(intent);
												finish();
											}
										} else {// 去订单页面
											System.out
													.println("----------------->buy product");
											SuccessAfterPayment();
											Intent intent = new Intent(
													PaymentOrderActivity.this,
													OrderActivity.class);
											intent.putExtra("flag", "two");
											Editor editor = sPreferences.edit();
											editor.putBoolean(
													Common.NEED_FRESH, true);
											editor.commit();
											startActivity(intent);
											finish();
										}
									}
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							};

							public void onFailure(int statusCode,
									Header[] headers, Throwable throwable,
									JSONObject errorResponse) {
								System.out.println("fail========="
										+ errorResponse);
							};

							public void onStart() {
								System.out.println("start=======");
							};

						});
				break;
			default:
				break;
			}
		};
	};

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	// 生成签名参数
	private String genPackageSign(List<NameValuePair> params) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < params.size(); i++) {
			sb.append(params.get(i).getName());
			sb.append('=');
			sb.append(params.get(i).getValue());
			sb.append('&');
		}
		sb.append("key=");
		sb.append(Constants.API_KEY);

		String packageSign = MD5.getMessageDigest(sb.toString().getBytes())
				.toUpperCase();
		Log.e("orion", packageSign);
		return packageSign;
	}

	private String genAppSign(List<NameValuePair> params) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < params.size(); i++) {
			sb.append(params.get(i).getName());
			sb.append('=');
			sb.append(params.get(i).getValue());
			sb.append('&');
		}
		sb.append("key=");
		sb.append(Constants.API_KEY);

		this.sb.append("sign str\n" + sb.toString() + "\n\n");
		String appSign = MD5.getMessageDigest(sb.toString().getBytes())
				.toUpperCase();
		Log.e("orion", appSign);
		return appSign;
	}

	private String toXml(List<NameValuePair> params) {
		StringBuilder sb = new StringBuilder();
		sb.append("<xml>");
		for (int i = 0; i < params.size(); i++) {
			sb.append("<" + params.get(i).getName() + ">");

			sb.append(params.get(i).getValue());
			sb.append("</" + params.get(i).getName() + ">");
		}
		sb.append("</xml>");

		Log.e("orion", sb.toString());
		return sb.toString();
	}

	private class GetPrepayIdTask extends
			AsyncTask<Void, Void, Map<String, String>> {

		private CustomProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = CustomProgressDialog.show(PaymentOrderActivity.this,
					getString(R.string.getting_access_token), false, null);

		}

		@Override
		protected void onPostExecute(Map<String, String> result) {
			// 生成预付单的结果
			if (dialog != null) {
				dialog.dismiss();
			}
			sb.append("prepay_id\n" + result.get("prepay_id") + "\n\n");

			resultunifiedorder = result;
			Log.e("===============", result.toString());

			// 生成签名参数
			genPayReq();
			// 调用微信支付
			sendPayReq();

		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected Map<String, String> doInBackground(Void... params) {

			String url = String
					.format("https://api.mch.weixin.qq.com/pay/unifiedorder");
			// 生成的支付订单
			String entity = genProductArgs();

			Log.e("orion", entity);
			// 把生成的支付订单post生成预付单
			byte[] buf = Util.httpPost(url, entity);

			String content = new String(buf);
			Log.e("orion", content);
			Map<String, String> xml = decodeXml(content);

			return xml;
		}
	}

	public Map<String, String> decodeXml(String content) {

		try {
			Map<String, String> xml = new HashMap<String, String>();
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(new StringReader(content));
			int event = parser.getEventType();
			while (event != XmlPullParser.END_DOCUMENT) {

				String nodeName = parser.getName();
				switch (event) {
				case XmlPullParser.START_DOCUMENT:

					break;
				case XmlPullParser.START_TAG:

					if ("xml".equals(nodeName) == false) {
						xml.put(nodeName, parser.nextText());
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				}
				event = parser.next();
			}

			return xml;
		} catch (Exception e) {
			Log.e("orion", e.toString());
		}
		return null;

	}

	private String genNonceStr() {
		Random random = new Random();
		return MD5.getMessageDigest(String.valueOf(random.nextInt(10000))
				.getBytes());
	}

	private long genTimeStamp() {
		return System.currentTimeMillis() / 1000;
	}

	private String genOutTradNo() {
		Random random = new Random();
		return MD5.getMessageDigest(String.valueOf(random.nextInt(10000))
				.getBytes());
	}

	// 生成支付订单
	private String genProductArgs() {
		StringBuffer xml = new StringBuffer();

		try {
			String nonceStr = genNonceStr();

			xml.append("</xml>");
			List<NameValuePair> packageParams = new LinkedList<NameValuePair>();
			packageParams
					.add(new BasicNameValuePair("appid", Constants.APP_ID));// 公众账号ID
			packageParams.add(new BasicNameValuePair("body", "weixin"));// 商品描述
			packageParams
					.add(new BasicNameValuePair("mch_id", Constants.MCH_ID));// 商户号
			packageParams.add(new BasicNameValuePair("nonce_str", nonceStr));// 随机字符串
			packageParams.add(new BasicNameValuePair("notify_url",
					"http://121.40.35.3/test"));// 通知地址:接收微信支付异步通知回调地址
			packageParams.add(new BasicNameValuePair("out_trade_no",
					genOutTradNo()));// 商户订单号:商户系统内部的订单号,32个字符内、可包含字母
			packageParams.add(new BasicNameValuePair("spbill_create_ip",
					"127.0.0.1"));// 终端IP:APP和网页支付提交用户端ip，Native支付填调用微信支付API的机器IP
			packageParams.add(new BasicNameValuePair("total_fee", "1"));// 总金额只能为整数,单位是分
			packageParams.add(new BasicNameValuePair("trade_type", "APP"));// 交易类型:取值如下：JSAPI，NATIVE，APP，WAP

			String sign = genPackageSign(packageParams);
			packageParams.add(new BasicNameValuePair("sign", sign));// 签名

			String xmlstring = toXml(packageParams);

			return xmlstring;

		} catch (Exception e) {
			Log.e(TAG, "genProductArgs fail, ex = " + e.getMessage());
			return null;
		}

	}

	private void genPayReq() {

		req.appId = Constants.APP_ID;
		req.partnerId = Constants.MCH_ID;
		req.prepayId = resultunifiedorder.get("prepay_id");// 预支付交易会话标:微信生成的预支付回话标识，用于后续接口调用中使用，该值有效期为2小时
		req.packageValue = "prepay_id=" + resultunifiedorder.get("prepay_id");
		req.nonceStr = genNonceStr();
		req.timeStamp = String.valueOf(genTimeStamp());// 时间戳

		List<NameValuePair> signParams = new LinkedList<NameValuePair>();
		signParams.add(new BasicNameValuePair("appid", req.appId));
		signParams.add(new BasicNameValuePair("noncestr", req.nonceStr));
		signParams.add(new BasicNameValuePair("package", req.packageValue));
		signParams.add(new BasicNameValuePair("partnerid", req.partnerId));
		signParams.add(new BasicNameValuePair("prepayid", req.prepayId));
		signParams.add(new BasicNameValuePair("timestamp", req.timeStamp));

		req.sign = genAppSign(signParams);

		sb.append("sign\n" + req.sign + "\n\n");

		Log.e("orion", signParams.toString());

	}

	// 调用微信支付
	private void sendPayReq() {

		msgApi.registerApp(Constants.APP_ID);
		msgApi.sendReq(req);

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
