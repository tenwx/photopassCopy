package com.pictureAir;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
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
import com.pictureAir.alipay.PayResult;
import com.pictureAir.entity.OrderInfo;
import com.pictureAir.entity.PhotoInfo;
import com.pictureAir.util.API;
import com.pictureAir.util.AliPayUtil;
import com.pictureAir.util.AppManager;
import com.pictureAir.util.Common;
import com.pictureAir.util.HttpUtil;
import com.pictureAir.util.PaypalUtil;
import com.pictureAir.weChatPay.Constants;
import com.pictureAir.weChatPay.Util;
import com.pictureAir.widget.CustomProgressDialog;
import com.pictureAir.widget.MyToast;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.umeng.analytics.MobclickAgent;

public class PaymentOrderActivity extends Activity implements OnClickListener{
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
//	private TextView addressTextView;

	private static final int RQF_SUCCESS = 1;

	private static final int RQF_CANCEL = 2;

	private static final int RQF_ERROR = 3;

	private static final int INITPAYPAL = 4;

	private int payType;//支付类型  0 支付宝 1 银联  2 VISA信用卡 3 代付 4 分期 5 自提 6 paypal 7 wechat

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
	 * - Set to PayPalConfiguration.ENVIRONMENT_SANDBOX to use your test credentials
	 * from https://developer.paypal.com
	 * 
	 * - Set to PayPalConfiguration.ENVIRONMENT_NO_NETWORK to kick the tires
	 * without communicating to PayPal's servers.
	 */
	private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_NO_NETWORK;

	// note that these credentials will differ between live & sandbox environments.
	private static final String CONFIG_CLIENT_ID = "credential from developer.paypal.com";//账号注册号之后就会有这个ID
	
	private static final String TAG = "PaymentOrderActivity";

	private static final int REQUEST_CODE_PAYMENT = 1;
	private static final int REQUEST_CODE_FUTURE_PAYMENT = 2;
	private static final int REQUEST_CODE_PROFILE_SHARING = 3;

	private static PayPalConfiguration config = new PayPalConfiguration().environment(CONFIG_ENVIRONMENT).clientId(CONFIG_CLIENT_ID);

	//-----------微信支付参数----------------//
	private IWXAPI api;
	private long timeStamp;
	private String nonceStr, packageValue; 
	private static enum LocalRetCode {
		ERR_OK, ERR_HTTP, ERR_JSON, ERR_OTHER
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment_order);
		AppManager.getInstance().addActivity(this);
		findViewById();
		init();
	}

	private void findViewById(){
		lrtLayout = (ImageView)findViewById(R.id.lrt);
		sbmtButton = (TextView)findViewById(R.id.button_smpm);
		//支付方式选择
		zfButton = (ImageView)findViewById(R.id.imageButton1_zfb);
		yhkButton = (ImageView)findViewById(R.id.imageButton2_yhk);
		paypalButton = (ImageView)findViewById(R.id.imageButton3_paypal);
		wechatButton = (ImageView)findViewById(R.id.imageButton2_weixin);
		zfbLayout = (RelativeLayout)findViewById(R.id.zfb);
		ylLayout = (RelativeLayout)findViewById(R.id.yl);
		paypalLayout = (RelativeLayout)findViewById(R.id.paypal);
		wechatLayout = (RelativeLayout)findViewById(R.id.weixin);
		//运送方式选择
		pickupLayout = (RelativeLayout)findViewById(R.id.pickup);
		pickupTextView = (TextView)findViewById(R.id.textView4);
//		addressTextView = (TextView)findViewById(R.id.addressTextView);
	}

	private void init(){
		sPreferences = getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
		newToast = new MyToast(this);
		myApplication = (MyApplication)getApplication();

		//初始化paypal
		InitPayPal();

		//获取wechat的实例对象
		api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);
		api.registerApp(Constants.APP_ID);
		
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

		if (getIntent().getStringExtra("flag") == null) {//为空，说明是正常流程进入
			nameString = getIntent().getStringExtra("name");//获取name
			priceString = getIntent().getStringExtra("price");//获取price
			introductString = getIntent().getStringExtra("introduce");//获取介绍信息
			orderid = getIntent().getStringExtra("orderId");

			pickupLayout.setOnClickListener(this);
		}else if ("order".equals(getIntent().getStringExtra("flag"))) {//从订单页面进入
			orderInfo = getIntent().getParcelableExtra("deliveryInfo");
			orderid = orderInfo.orderId;
			//此处信息，获取比较麻烦，暂时写死
			nameString = "PictureAir";
			priceString = orderInfo.orderTotalPrice + "";
			introductString = "Made by PictureAir";
		}

		needAddress = getIntent().getBooleanExtra("addressType", false);
		if (!needAddress) {//不需要地址
//			pickupTextView.setVisibility(View.GONE);
			pickupTextView.setText(R.string.noexpress);
		}else {
			pickupTextView.setText(getString(R.string.pick_up_address) + getString(R.string.disney_address));
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
	 * @param orderId  订单号
	 */
	private void pay(String orderId) {
		// TODO Auto-generated method stub
		if (0==payType) {//支付宝支付方式
			try {
				String info = AliPayUtil.getOrderInfo(orderId, nameString, introductString);
				System.out.println("info:"+info);
				// 对订单做RSA 签名
				String sign = AliPayUtil.sign(info);
				System.out.println("sign:"+sign);
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
						//设置为沙箱模式，不设置默认为线上环境
						//alipay.setSandBox(true);
						// 调用支付接口，获取支付结果
						String result = alipay.pay(payInfo);
						//						Result result = new Result(alipay.pay(orderInfo).toString());
						System.out.println("pay info="+result);
						PayResult payResult = new PayResult(result);
						//						String resultInfo = payResult.getResult();

						String resultStatus = payResult.getResultStatus();
						Message msg = new Message();
						if (TextUtils.equals(resultStatus, "9000")) {//支付成功
							msg.what = RQF_SUCCESS;
						}else if (TextUtils.equals(resultStatus, "6001")) {//用户中途取消
							msg.what = RQF_CANCEL;
						}else if (TextUtils.equals(resultStatus, "4000")) {//订单支付失败
							msg.what = RQF_ERROR;
						}else if (TextUtils.equals(resultStatus, "8000")) {//正在处理中
							msg.what = RQF_SUCCESS;
						}else if (TextUtils.equals(resultStatus, "6002")) {//网络连接出错
							msg.what = RQF_ERROR;
						}
						mHandler.sendMessage(msg);
					}
				}.start();

			} catch (Exception ex) {
				ex.printStackTrace();
				newToast.setTextAndShow(R.string.pay_failed, Common.TOAST_SHORT_TIME);
			}
		}else if (1 == payType) {
			System.out.println("yl");
			mHandler.sendEmptyMessage(RQF_SUCCESS);
		}else if (6 == payType) {
			System.out.println("paypal");
			/* 
			 * PAYMENT_INTENT_SALE will cause the payment to complete immediately.
			 * Change PAYMENT_INTENT_SALE to 
			 *   - PAYMENT_INTENT_AUTHORIZE to only authorize payment and capture funds later.
			 *   - PAYMENT_INTENT_ORDER to create a payment for authorization and capture
			 *     later via calls from your server.
			 * 
			 * Also, to include additional payment details and an item list, see getStuffToBuy() below.
			 */
			PayPalPayment thingToBuy = PaypalUtil.getStuffToBuy(PayPalPayment.PAYMENT_INTENT_SALE, nameString);
			addAppProvidedShippingAddress(thingToBuy);
			enableShippingAddressRetrieval(thingToBuy, true);
			/*
			 * See getStuffToBuy(..) for examples of some available payment options.
			 */

			Intent intent = new Intent(this, PaymentActivity.class);

			// send the same configuration for restart resiliency
			intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

			intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);

			startActivityForResult(intent, REQUEST_CODE_PAYMENT);

		}else if (payType == 7) {
			System.out.println("wechat");
			new GetAccessTokenTask().execute();
		}else {
			System.out.println("other");
		}
	}


	/*
	 * 添加收货地址到paypal
	 */
	private void addAppProvidedShippingAddress(PayPalPayment paypalPayment) {
		ShippingAddress shippingAddress = new ShippingAddress().recipientName("alieen").line1("abc")
				.city("上海").state("浦东").postalCode("0011").countryCode("US");
		paypalPayment.providedShippingAddress(shippingAddress);
	}

	/*
	 * 是否打开地址检索功能，如果true，则会从paypal账号中获取地址，如果false，不会获取地址
	 */
	private void enableShippingAddressRetrieval(PayPalPayment paypalPayment, boolean enable) {
		paypalPayment.enablePayPalShippingAddressesRetrieval(enable);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.lrt://返回
			finish();
			break;

		case R.id.button_smpm://提交支付
			sbmtButton.setEnabled(false);
			System.out.println("-------------pay on click");
			//直接调用接口
			pay(orderid);
			break;

		case R.id.zfb://选择支付宝支付
			payType = 0;
			zfButton.setImageResource(R.drawable.sele);
			yhkButton.setImageResource(R.drawable.nosele);
			paypalButton.setImageResource(R.drawable.nosele);
			wechatButton.setImageResource(R.drawable.nosele);
			System.out.println("ZFB");
			break;

		case R.id.yl://选择银联支付
			payType = 1;
			zfButton.setImageResource(R.drawable.nosele);
			yhkButton.setImageResource(R.drawable.sele);
			paypalButton.setImageResource(R.drawable.nosele);
			wechatButton.setImageResource(R.drawable.nosele);
			System.out.println("YL");
			break;

		case R.id.paypal://paypal支付
			payType = 6;
			zfButton.setImageResource(R.drawable.nosele);
			yhkButton.setImageResource(R.drawable.nosele);
			paypalButton.setImageResource(R.drawable.sele);
			wechatButton.setImageResource(R.drawable.nosele);
			System.out.println("PAYPAL");
			break;

		case R.id.weixin://wechat支付
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


	//支付成功回调函数
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			//paypal支付成功
			PaymentConfirmation confirm =
					data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
			if (confirm != null) {
				try {
					Log.i("PayPal", confirm.toJSONObject().toString(4));
					Log.i("PayPal", confirm.getPayment().toJSONObject().toString(4));
					/**
					 *  TODO: send 'confirm' (and possibly confirm.getPayment() to your server for verification
					 * or consent completion.
					 * See https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
					 * for more details.
					 *
					 * For sample mobile backend interactions, see
					 * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
					 */

				} catch (JSONException e) {
					Log.e("PayPal", "an extremely unlikely failure occurred: ", e);
				}
			}
			//			String payKey = data.getStringExtra(PayPalActivity.EXTRA_PAY_KEY);
			//			System.out.println("paypal pay success"+payKey);
			mHandler.sendEmptyMessage(RQF_SUCCESS);
		}else if (resultCode == RESULT_CANCELED) {
			//paypal取消支付
			System.out.println("paypal pay cancel");
			mHandler.sendEmptyMessage(RQF_CANCEL);
		}else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
			//paypal付款失败
			mHandler.sendEmptyMessage(RQF_ERROR);
		}else if (resultCode == 30) {//取消添加收货地址

		}
		super.onActivityResult(requestCode, resultCode, data);
	}


	//支付失败处理
	private void ErrorInPayment() {
		// TODO Auto-generated method stub
		newToast.setTextAndShow(R.string.alipay_error, Common.TOAST_SHORT_TIME);
//		myApplication.setIsBuyingPhotoInfo(null);
		myApplication.clearIsBuyingPhotoList();
		myApplication.setRefreshViewAfterBuyBlurPhoto("");
		AppManager.getInstance().killActivity(SubmitOrderActivity.class);
		AppManager.getInstance().killActivity(PreviewProductActivity.class);
//		AppManager.getInstance().killActivity(BlurActivity.class);
		AppManager.getInstance().killActivity(PreviewPhotoActivity.class);
		AppManager.getInstance().killActivity(SelectPhotoActivity.class);
		AppManager.getInstance().killActivity(MakegiftActivity.class);
		finish();
	}
	//取消操作处理
	private void CancelInPayment() {
		// TODO Auto-generated method stub
//		myApplication.setIsBuyingPhotoInfo(null);
		myApplication.clearIsBuyingPhotoList();
		myApplication.setRefreshViewAfterBuyBlurPhoto("");
		newToast.setTextAndShow(R.string.cancel_deal, Common.TOAST_SHORT_TIME);
		AppManager.getInstance().killActivity(SubmitOrderActivity.class);
		AppManager.getInstance().killActivity(PreviewProductActivity.class);
//		AppManager.getInstance().killActivity(BlurActivity.class);
		AppManager.getInstance().killActivity(SelectPhotoActivity.class);
		AppManager.getInstance().killActivity(PreviewPhotoActivity.class);
		AppManager.getInstance().killActivity(MakegiftActivity.class);
		finish();
	}
	//成功支付之后的操作
	private void SuccessAfterPayment() {
		// TODO Auto-generated method stub
		System.out.println("start finish expired activity");
		AppManager.getInstance().killActivity(SubmitOrderActivity.class);
		AppManager.getInstance().killActivity(PreviewProductActivity.class);
//		AppManager.getInstance().killActivity(BlurActivity.class);
		AppManager.getInstance().killActivity(PreviewPhotoActivity.class);
		AppManager.getInstance().killActivity(SelectPhotoActivity.class);
		AppManager.getInstance().killActivity(DetailProductActivity.class);
		AppManager.getInstance().killActivity(PPPDetailProductActivity.class);
		AppManager.getInstance().killActivity(MakegiftActivity.class);
		AppManager.getInstance().killActivity(OrderActivity.class);


	}
	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			//			Result result = new Result((String) msg.obj);
			//			System.out.println((String) msg.obj);
//			sbmtButton.setEnabled(true);
			switch (msg.what) {
			case RQF_ERROR:
				Intent intent1 = new Intent(PaymentOrderActivity.this, OrderActivity.class);
				startActivity(intent1);
				ErrorInPayment();
				break;
			case RQF_CANCEL: 
				Intent intent2 = new Intent(PaymentOrderActivity.this, OrderActivity.class);
				startActivity(intent2);
				CancelInPayment();
				break;
			case RQF_SUCCESS:
				System.out.println("RQF_PAY-----------");
				/*********测试阶段，需要做一个修改订单状态的接口************/
				RequestParams requestParams = new RequestParams();
				requestParams.put("orderId", orderid);
				requestParams.put("orderPayStatus", 2);//修改订单状态，2表示已经支付
				requestParams.put("payType", payType);
				requestParams.put("userId", sPreferences.getString(Common.USERINFO_ID, ""));
				System.out.println("orderid"+orderid+"_userId_"+sPreferences.getString(Common.USERINFO_ID, ""));
				HttpUtil.post(Common.BASE_URL+"/shopping/modifyOrderStatus", requestParams, new JsonHttpResponseHandler(){
					public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
						System.out.println(response+"result");
						try {
							JSONObject resultJsonObject = response.getJSONObject("result");
							if ("success".equals(resultJsonObject.getString("type"))) {

								if (resultJsonObject.has("pppCode")) {//判断是否购买ppp，如果购买ppp，则去ppp页面，否则去订单界面
									System.out.println(resultJsonObject.getString("pppCode"));
									if (null==resultJsonObject.getString("pppCode")||"null".equals(resultJsonObject.getString("pppCode"))) {//去订单页面
										System.out.println("------------------->buy photo");



										//获取存放的photopass信息
//										PhotoInfo photopassmap = myApplication.getIsBuyingPhotoInfo();
										Intent intent;
										//以下三种情况要回到清晰图片页面
										if (myApplication.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_VIEWORSELECTACTIVITY) ||
												myApplication.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_MYPHOTOPASS) || 
												myApplication.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_BLUR)) {
											System.out.println("flag is -------------------->" + myApplication.getRefreshViewAfterBuyBlurPhoto());
											myApplication.setPhotoIsPaid(true);
											ArrayList<PhotoInfo> photopassArrayList = new ArrayList<PhotoInfo>();
											photopassArrayList.addAll(myApplication.getIsBuyingPhotoInfoList());
											//找出购买的info，并且将购买属性改为1
											photopassArrayList.get(myApplication.getIsBuyingIndex()).isPayed = 1;
											
											intent = new Intent(PaymentOrderActivity.this, PreviewPhotoActivity.class);
											intent.putExtra("activity", "paymentorderactivity");
											intent.putExtra("position", myApplication.getIsBuyingIndex() + "");//在那个相册中的位置
											intent.putExtra("photoId", photopassArrayList.get(myApplication.getIsBuyingIndex()).photoId);
											intent.putExtra("photos", photopassArrayList);//那个相册的全部图片路径
											intent.putExtra("targetphotos", myApplication.magicPicList);
											//清空标记
											myApplication.clearIsBuyingPhotoList();

											SuccessAfterPayment();

											if (myApplication.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_VIEWORSELECTACTIVITY)) {
												myApplication.setRefreshViewAfterBuyBlurPhoto(Common.FROM_VIEWORSELECTACTIVITYANDPAYED);
											}else if (myApplication.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_MYPHOTOPASS)) {
												myApplication.setRefreshViewAfterBuyBlurPhoto(Common.FROM_MYPHOTOPASSPAYED);
											}else if (myApplication.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_BLUR)) {
												myApplication.setRefreshViewAfterBuyBlurPhoto("");
											}

										}else {
											//回到订单页面
											intent = new Intent(PaymentOrderActivity.this, OrderActivity.class);
											intent.putExtra("flag", "two");
											SuccessAfterPayment();
										}

										Editor editor = sPreferences.edit();
										editor.putBoolean(Common.NEED_FRESH, true);
										editor.commit();
										startActivity(intent);
										finish();
									}else {//去ppp页面
										System.out.println("----------------->buy ppp");
										Editor editor = sPreferences.edit();
										editor.putBoolean(Common.NEED_FRESH, true);
										editor.commit();
										myApplication.setNeedRefreshPPPList(true);
										Intent intent = new Intent(PaymentOrderActivity.this, MyPPPActivity.class);
										API.PPPlist.clear();
										SuccessAfterPayment();
										startActivity(intent);
										finish();
									}
								}else {//去订单页面
									System.out.println("----------------->buy product");
									SuccessAfterPayment();
									Intent intent = new Intent(PaymentOrderActivity.this, OrderActivity.class);
									intent.putExtra("flag", "two");
									Editor editor = sPreferences.edit();
									editor.putBoolean(Common.NEED_FRESH, true);
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
					public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
						System.out.println("fail========="+errorResponse);
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
		stopService(new Intent(this, PayPalService.class));
		AppManager.getInstance().killActivity(this);
	}

	
	private class GetAccessTokenTask extends AsyncTask<Void, Void, GetAccessTokenResult> {

		private CustomProgressDialog dialog;

		@Override
		protected void onPreExecute() {
//			ProgressDialog dialog = ProgressDialog.show(PaymentOrderActivity.this, getString(R.string.app_tip), getString(R.string.getting_access_token));
			dialog = CustomProgressDialog.show(PaymentOrderActivity.this, getString(R.string.getting_access_token), false, null);
		}

		@Override
		protected void onPostExecute(GetAccessTokenResult result) {
			if (dialog != null) {
				dialog.dismiss();
			}

			if (result.localRetCode == LocalRetCode.ERR_OK) {
//				newToast.setTextAndShow(R.string.get_access_token_succ, Common.TOAST_SHORT_TIME);
				Log.d(TAG, "onPostExecute, accessToken = " + result.accessToken);

				GetPrepayIdTask getPrepayId = new GetPrepayIdTask(result.accessToken);
				getPrepayId.execute();
			} else {
//				Toast.makeText(PaymentOrderActivity.this, getString(R.string.get_access_token_fail, result.localRetCode.name()), Toast.LENGTH_LONG).show();
			}
		}

		@Override
		protected GetAccessTokenResult doInBackground(Void... params) {
			GetAccessTokenResult result = new GetAccessTokenResult();

			String url = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
					Constants.APP_ID, Constants.APP_SECRET);
			Log.d(TAG, "get access token, url = " + url);

			byte[] buf = Util.httpGet(url);
			if (buf == null || buf.length == 0) {
				result.localRetCode = LocalRetCode.ERR_HTTP;
				return result;
			}

			String content = new String(buf);
			result.parseFrom(content);
			return result;
		}
	}

	private class GetPrepayIdTask extends AsyncTask<Void, Void, GetPrepayIdResult> {

		private CustomProgressDialog dialog;
		private String accessToken;

		public GetPrepayIdTask(String accessToken) {
			this.accessToken = accessToken;
		}

		@Override
		protected void onPreExecute() {
			dialog = CustomProgressDialog.show(PaymentOrderActivity.this, getString(R.string.getting_prepayid), false, null);
//			dialog = ProgressDialog.show(PaymentOrderActivity.this, getString(R.string.app_tip), getString(R.string.getting_prepayid));
		}

		@Override
		protected void onPostExecute(GetPrepayIdResult result) {
			if (dialog != null) {
				dialog.dismiss();
			}

			if (result.localRetCode == LocalRetCode.ERR_OK) {
//				Toast.makeText(PaymentOrderActivity.this, R.string.get_prepayid_succ, Toast.LENGTH_LONG).show();
				sendPayReq(result);
			} else {
//				Toast.makeText(PaymentOrderActivity.this, getString(R.string.get_prepayid_fail, result.localRetCode.name()), Toast.LENGTH_LONG).show();
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected GetPrepayIdResult doInBackground(Void... params) {

			String url = String.format("https://api.weixin.qq.com/pay/genprepay?access_token=%s", accessToken);
			String entity = genProductArgs();

			Log.d(TAG, "doInBackground, url = " + url);
			Log.d(TAG, "doInBackground, entity = " + entity);

			GetPrepayIdResult result = new GetPrepayIdResult();

			byte[] buf = Util.httpPost(url, entity);
			if (buf == null || buf.length == 0) {
				result.localRetCode = LocalRetCode.ERR_HTTP;
				return result;
			}

			String content = new String(buf);
			Log.d(TAG, "doInBackground, content = " + content);
			result.parseFrom(content);
			return result;
		}
	}
	private static class GetAccessTokenResult {

		private static final String TAG = "MicroMsg.SDKSample.PayActivity.GetAccessTokenResult";

		public LocalRetCode localRetCode = LocalRetCode.ERR_OTHER;
		public String accessToken;
		public int expiresIn;
		public int errCode;
		public String errMsg;

		public void parseFrom(String content) {

			if (content == null || content.length() <= 0) {
				Log.e(TAG, "parseFrom fail, content is null");
				localRetCode = LocalRetCode.ERR_JSON;
				return;
			}

			try {
				JSONObject json = new JSONObject(content);
				if (json.has("access_token")) { // success case
					accessToken = json.getString("access_token");
					expiresIn = json.getInt("expires_in");
					localRetCode = LocalRetCode.ERR_OK;
				} else {
					errCode = json.getInt("errcode");
					errMsg = json.getString("errmsg");
					localRetCode = LocalRetCode.ERR_JSON;
				}

			} catch (Exception e) {
				localRetCode = LocalRetCode.ERR_JSON;
			}
		}
	}

	private static class GetPrepayIdResult {

		private static final String TAG = "MicroMsg.SDKSample.PayActivity.GetPrepayIdResult";

		public LocalRetCode localRetCode = LocalRetCode.ERR_OTHER;
		public String prepayId;
		public int errCode;
		public String errMsg;

		public void parseFrom(String content) {

			if (content == null || content.length() <= 0) {
				Log.e(TAG, "parseFrom fail, content is null");
				localRetCode = LocalRetCode.ERR_JSON;
				return;
			}

			try {
				JSONObject json = new JSONObject(content);
				if (json.has("prepayid")) { // success case
					prepayId = json.getString("prepayid");
					localRetCode = LocalRetCode.ERR_OK;
				} else {
					localRetCode = LocalRetCode.ERR_JSON;
				}

				errCode = json.getInt("errcode");
				errMsg = json.getString("errmsg");

			} catch (Exception e) {
				localRetCode = LocalRetCode.ERR_JSON;
			}
		}
	}
	
	private String genProductArgs() {
		JSONObject json = new JSONObject();
		
		try {
			json.put("appid", Constants.APP_ID);
			String traceId = Util.getTraceId();  // traceId 由开发者自定义，可用于订单的查询与跟踪，建议根据支付用户信息生成此id
			json.put("traceid", traceId);
			nonceStr = Util.genNonceStr();
			json.put("noncestr", nonceStr);
			
			List<NameValuePair> packageParams = new LinkedList<NameValuePair>();
			packageParams.add(new BasicNameValuePair("bank_type", "WX"));//支付类型
			packageParams.add(new BasicNameValuePair("body", "安卓签名测试"));//订单详情
			packageParams.add(new BasicNameValuePair("fee_type", "1"));//费用类型
			packageParams.add(new BasicNameValuePair("input_charset", "UTF-8"));
			packageParams.add(new BasicNameValuePair("notify_url", "http://121.14.73.81:8080/agent/wxpay/payNotifyUrl.jsp"));//服务器接收当前
			packageParams.add(new BasicNameValuePair("out_trade_no", Util.genOutTradNo()));//订单号
			packageParams.add(new BasicNameValuePair("partner", "1900000109"));//商户
			packageParams.add(new BasicNameValuePair("spbill_create_ip", "196.168.1.1"));
			packageParams.add(new BasicNameValuePair("total_fee", "1"));//总费用
			packageValue = Util.genPackage(packageParams);
			
			json.put("package", packageValue);
			timeStamp = Util.genTimeStamp();
			json.put("timestamp", timeStamp);
			
			List<NameValuePair> signParams = new LinkedList<NameValuePair>();
			signParams.add(new BasicNameValuePair("appid", Constants.APP_ID));
			signParams.add(new BasicNameValuePair("appkey", Constants.APP_KEY));
			signParams.add(new BasicNameValuePair("noncestr", nonceStr));
			signParams.add(new BasicNameValuePair("package", packageValue));
			signParams.add(new BasicNameValuePair("timestamp", String.valueOf(timeStamp)));
			signParams.add(new BasicNameValuePair("traceid", traceId));
			json.put("app_signature", Util.genSign(signParams));
			
			json.put("sign_method", "sha1");
		} catch (Exception e) {
			Log.e(TAG, "genProductArgs fail, ex = " + e.getMessage());
			return null;
		}
		
		return json.toString();
	}
	
	private void sendPayReq(GetPrepayIdResult result) {

		PayReq req = new PayReq();
		req.appId = Constants.APP_ID;
		req.partnerId = Constants.PARTNER_ID;
		req.prepayId = result.prepayId;
		req.nonceStr = nonceStr;
		req.timeStamp = String.valueOf(timeStamp);
		req.packageValue = "Sign=Wxpay";//"Sign=" + packageValue;

		List<NameValuePair> signParams = new LinkedList<NameValuePair>();
		signParams.add(new BasicNameValuePair("appid", req.appId));
		signParams.add(new BasicNameValuePair("appkey", Constants.APP_KEY));
		signParams.add(new BasicNameValuePair("noncestr", req.nonceStr));
		signParams.add(new BasicNameValuePair("package", req.packageValue));
		signParams.add(new BasicNameValuePair("partnerid", req.partnerId));
		signParams.add(new BasicNameValuePair("prepayid", req.prepayId));
		signParams.add(new BasicNameValuePair("timestamp", req.timeStamp));
		req.sign = Util.genSign(signParams);
		Log.d("d", "调起支付的package串："+req.packageValue);
		// 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
		
		api.sendReq(req);
	} 
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPageEnd("PaymentOrderActivity");
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onPageStart("PaymentOrderActivity");
		MobclickAgent.onResume(this);
	}
}
