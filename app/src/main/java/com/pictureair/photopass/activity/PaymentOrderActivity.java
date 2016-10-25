package com.pictureair.photopass.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.jni.ciphermanager.PWJniUtil;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.OrderInfo;
import com.pictureair.photopass.eventbus.AsyncPayResultEvent;
import com.pictureair.photopass.eventbus.BaseBusEvent;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.JsonUtil;
import com.pictureair.photopass.util.PayUtils;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.widget.PWToast;
import com.unionpay.UPPayAssistEx;

import java.lang.ref.WeakReference;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

public class PaymentOrderActivity extends BaseActivity implements OnClickListener {
    private TextView sbmtButton;
    private String nameString;
    private String priceString;
    private String introductString;

    private View paySv;
    private LinearLayout noPayLl;

    private RelativeLayout zfbLayout;
    private RelativeLayout ylLayout;
    private RelativeLayout visaLayout;
    private RelativeLayout wechatLayout;
    private RelativeLayout masterCardLayout;
    private RelativeLayout jcbLayout;

    private ImageView zfButton;
    private ImageView yhkButton;
    private ImageView visaButton;
    private ImageView wechatButton;
    private ImageView masterCardButton;
    private ImageView jcbButton;

    public static final int RQF_SUCCESS = 1;
    public static final int RQF_CANCEL = 2;
    public static final int RQF_ERROR = 3;
    public static final int INITPAYPAL = 4;
    public static final int RQF_UNSUCCESS = 5;
    private static final int ASYNC_PAY_SUCCESS = 6;

    private static final String TAG = "PaymentOrderActivity";

    private MyApplication myApplication;
    private PWToast newToast;
    private PictureAirDbManager pictureAirDbManager;
    private boolean paySyncResult = false;
    private org.json.JSONObject payAsyncResultJsonObject;

    private int payType = 1;// 支付类型 0 支付宝 1 银联 2 VISA信用卡 3 代付 4 分期 5 自提 6 paypal 7
    private PayUtils payUtils;

    private OrderInfo orderInfo;
    private String outletId;
    private JSONArray cartItemIds;
    private String orderid = "";
    private boolean weChatIsPaying = false;
    private int productType = 0;//商品类型 1-实体商品 2-虚拟商品
    private String isBack = "0";//用于判断是否需要返回 0- 不返回 1-返回

    private boolean needAddress = false;
    private boolean isPaying = false;

    //mMode参数解释： "00" - 启动银联正式环境 "01" - 连接银联测试环境
    private final String mMode = "00";
    private String tNCode;

    private boolean isNeedPay = true;//是否需要支付
    private JSONArray couponCodes;//优惠券
    private int cartCount = 0;//购物车数量

    private boolean asyncTimeOut = false;
    private int fromPanicBuy;

    private final Handler paymentOrderHandler = new PaymentOrderHandler(this);

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            PictureAirLog.v(TAG, "run");
            //手动拉取信息
            if (payAsyncResultJsonObject == null) {//没有收到推送，需要手动获取
                asyncTimeOut = true;
                API1.getSocketData(paymentOrderHandler);
            }
        }
    };

    private static class PaymentOrderHandler extends Handler {
        private final WeakReference<PaymentOrderActivity> mActivity;

        public PaymentOrderHandler(PaymentOrderActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivity.get() == null) {
                return;
            }
            mActivity.get().dealHandler(msg);
        }
    }

    /**
     * 处理Message
     *
     * @param msg
     */
    private void dealHandler(Message msg) {
        switch (msg.what) {
            case RQF_ERROR:
                PictureAirLog.v(TAG, "RQF_ERROR");
                Intent intent1 = new Intent(PaymentOrderActivity.this, OrderActivity.class);
                startActivity(intent1);
                ErrorInPayment();
                break;

            case RQF_CANCEL:
                PictureAirLog.v(TAG, "RQF_CANCEL");
                //从模糊照片单张购买、PP+购买 回到之前的预览界面
                CancelInPayment(true);
                break;

            case RQF_UNSUCCESS:
                PictureAirLog.v(TAG, "RQF_UNSUCCESS");
                //从模糊照片单张购买、PP+购买 回到之前的预览界面
                CancelInPayment(false);
                break;

            case RQF_SUCCESS:
            case API1.ADD_ORDER_SUCCESS:
                PictureAirLog.v(TAG, "RQF_SUCCESS orderid: " + orderid);
                //支付成功后：出现等待弹窗，5秒后进入订单页面。其中接收推送，若没有推送则将订单ID写入数据库，状态为灰色不可点击
                showPWProgressDialog();
                paySyncResult = true;
                paymentOrderHandler.postDelayed(runnable, 5000);

                break;

            case ASYNC_PAY_SUCCESS:
                PictureAirLog.v(TAG, "ASYNC_PAY_SUCCESS: " + payAsyncResultJsonObject);
                if (paySyncResult) {//很有可能先收到异步通知，然后才返回同步通知，所以要做判断
                    paySyncResult = false;
                    dealData(payAsyncResultJsonObject);
                } else {
                    paymentOrderHandler.sendEmptyMessageDelayed(ASYNC_PAY_SUCCESS, 500);
                }
                break;

            case API1.UNIONPAY_GET_TN_SUCCESS://获取银联TN成功
                PictureAirLog.v(TAG, "UNIONPAY_GET_TN_SUCCESS: ");
                dismissPWProgressDialog();
                PictureAirLog.v(TAG, "msg.obj: " + msg.obj);
                if (msg.obj == null || (msg.obj).toString().length() == 0) {
                    PictureAirLog.v(TAG, "msg.obj: " + msg.obj);
                    paymentOrderHandler.sendEmptyMessage(RQF_ERROR);
                } else {
                    //获得TN号成功 -- 解析数据
                    PictureAirLog.v(TAG, "msg.obj: " + msg.obj);
                    JSONObject result = (JSONObject) msg.obj;
                    PictureAirLog.v(TAG, "UNIONPAY_GET_TN_SUCCESS:  result： " + result);
                    tNCode = result.getString("tn");//
                    UPPayAssistEx.startPay(PaymentOrderActivity.this, null, null, tNCode, mMode);
                    PictureAirLog.v(TAG, "tNCode: " + tNCode);
                }
                break;

            case API1.GET_SOCKET_DATA_SUCCESS:
                //获取推送成功，后面逻辑按照之前走
                PictureAirLog.v(TAG, "GET_SOCKET_DATA_SUCCESS: ");
                JSONObject jsonObject = (JSONObject) msg.obj;
                boolean isSuccess = false;
                if (jsonObject.size() > 0) {
                    isSuccess = JsonUtil.dealGetSocketData(PaymentOrderActivity.this, jsonObject.toString(), false, orderid);
                }
                if (!isSuccess) {
                    dismissPWProgressDialog();
                    if (payAsyncResultJsonObject == null) {
                        pictureAirDbManager.insertPaymentOrderIdDB(SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ID, ""), orderid);
                    }
                    SuccessAfterPayment();
                    finish();
                }

                break;
            case API1.GET_SOCKET_DATA_FAILED:
                //获取推送失败
                dismissPWProgressDialog();
                PictureAirLog.v(TAG, "GET_SOCKET_DATA_FAILED: " + msg.arg1);
                if (payAsyncResultJsonObject == null) {
                    pictureAirDbManager.insertPaymentOrderIdDB(SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ID, ""), orderid);
                }
                SuccessAfterPayment();
                finish();

                break;

            case API1.UNIONPAY_GET_TN_FAILED://获取银联TN失败
                PictureAirLog.v(TAG, "UNIONPAY_GET_TN_FAILED: ");
                dismissPWProgressDialog();
                newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                paymentOrderHandler.sendEmptyMessage(RQF_ERROR);
                break;

            case API1.ADD_ORDER_FAILED:
                PictureAirLog.e(TAG, "ADD_ORDER_FAILED cade: " + msg.arg1);
                dismissPWProgressDialog();
                newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);
                Intent errorIntent = new Intent(PaymentOrderActivity.this, OrderActivity.class);
                startActivity(errorIntent);
                ErrorInPayment();
                break;

            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_order);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        findViewById();
        init();
    }

    private void findViewById() {
        setTopTitleShow(R.string.pay);
        setTopLeftValueAndShow(R.drawable.back_white, true);
        paySv = findViewById(R.id.pay_sv);
        noPayLl = (LinearLayout) findViewById(R.id.no_pay_ll);
        sbmtButton = (TextView) findViewById(R.id.button_smpm);
        sbmtButton.setTypeface(MyApplication.getInstance().getFontBold());
        // 支付方式选择
        zfButton = (ImageView) findViewById(R.id.imageButton1_zfb);
        yhkButton = (ImageView) findViewById(R.id.imageButton2_yhk);
        visaButton = (ImageView) findViewById(R.id.imageButton_payvisa);
        masterCardButton = (ImageView) findViewById(R.id.imageButton_paymc);
        jcbButton = (ImageView) findViewById(R.id.imageButton_payjcb);
        wechatButton = (ImageView) findViewById(R.id.imageButton2_weixin);
        zfbLayout = (RelativeLayout) findViewById(R.id.zfb);
        ylLayout = (RelativeLayout) findViewById(R.id.yl);
        visaLayout = (RelativeLayout) findViewById(R.id.paytype_visa);
        masterCardLayout = (RelativeLayout) findViewById(R.id.paytype_mc);
        jcbLayout = (RelativeLayout) findViewById(R.id.paytype_jcb);
        wechatLayout = (RelativeLayout) findViewById(R.id.weixin);
        pictureAirDbManager = new PictureAirDbManager(MyApplication.getInstance());
    }

    private void init() {
        newToast = new PWToast(this);
        myApplication = (MyApplication) getApplication();

        sbmtButton.setOnClickListener(this);
        yhkButton.setImageResource(R.drawable.sele);
        zfButton.setImageResource(R.drawable.nosele);
        visaButton.setImageResource(R.drawable.nosele);
        masterCardButton.setImageResource(R.drawable.nosele);
        jcbButton.setImageResource(R.drawable.nosele);
        wechatButton.setImageResource(R.drawable.nosele);

        zfbLayout.setOnClickListener(this);
        ylLayout.setOnClickListener(this);
        visaLayout.setOnClickListener(this);
        masterCardLayout.setOnClickListener(this);
        jcbLayout.setOnClickListener(this);
        wechatLayout.setOnClickListener(this);

        if (getIntent().getStringExtra("flag") == null) {
            // 为空，说明是正常流程进入
            PictureAirLog.v(TAG, "为空，说明是正常流程进入");
            isNeedPay = getIntent().getBooleanExtra("isNeedPay", true);
            nameString = getIntent().getStringExtra("name");// 获取name
            priceString = getIntent().getStringExtra("price");// 获取price
            introductString = getIntent().getStringExtra("introduce");// 获取介绍信息
            orderid = getIntent().getStringExtra("orderId");
            outletId = getIntent().getStringExtra("outletId");
            cartItemIds = JSONArray.parseArray(getIntent().getStringExtra("cartItemIds"));
            String couponCodesStr = getIntent().getStringExtra("couponCodes");
            couponCodes = !TextUtils.isEmpty(couponCodesStr) ? JSONArray.parseArray(getIntent().getStringExtra("couponCodes")) : null;
            cartCount = getIntent().getIntExtra("cartCount", 0);
            fromPanicBuy = getIntent().getIntExtra("fromPanicBuy", 0);

        } else if ("order".equals(getIntent().getStringExtra("flag"))) {
            // 从订单页面进入
            PictureAirLog.v(TAG, "从订单页面进入");
            orderInfo = getIntent().getParcelableExtra("deliveryInfo");
            orderid = orderInfo.orderNumber;
            // 此处信息，获取比较麻烦，暂时写死
            priceString = orderInfo.orderTotalPrice + "";
//            nameString = "PictureAir";
//            introductString = "Made by PictureAir";
            nameString = getIntent().getStringExtra("name");// 获取name
            introductString = getIntent().getStringExtra("introduce");// 获取介绍信息

        }
        PictureAirLog.v(TAG, "isBack: " + getIntent().getStringExtra("isBack"));
        productType = getIntent().getIntExtra("productType", 0);
        isBack = getIntent().getStringExtra("isBack");
        nameString = AppUtil.ReplaceString(nameString);
        PictureAirLog.v(TAG, "name: " + nameString + " orderid： " + orderid + "priceString: " + priceString);
//		needAddress = getIntent().getBooleanExtra("addressType", false);
//		if (!needAddress) {// 不需要地址
//			// pickupTextView.setVisibility(View.GONE);
//			pickupTextView.setText(R.string.noexpress);
//		} else {
//			pickupTextView.setText(getString(R.string.pick_up_address)
//					+ getString(R.string.disney_address));
//		}

        //判断是否需要支付
        if (isNeedPay) {
            paySv.setVisibility(View.VISIBLE);
            noPayLl.setVisibility(View.GONE);
            sbmtButton.setText(R.string.submitpayment);
        } else {
            paySv.setVisibility(View.GONE);
            noPayLl.setVisibility(View.VISIBLE);
            sbmtButton.setText(R.string.no_pay_success);
        }

    }

    /**
     * 提交订单（无需付钱的情况）
     */
    public void checkOut() {
        showPWProgressDialog();
        if (cartItemIds != null) {
            if (productType == 1) {
                //获取收货地址
                API1.addOrder(cartItemIds, 1, outletId, "", couponCodes,null, null, null, paymentOrderHandler);

            } else {
                //PP+/数码商品不需要地址
                API1.addOrder(cartItemIds, 3, "", "", couponCodes,null, null, null, paymentOrderHandler);

            }
        }
    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.button_smpm:// 提交支付
                if (AppUtil.getNetWorkType(MyApplication.getInstance()) == 0) {
                    newToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
                    return;
                }
                sbmtButton.setEnabled(false);
                PictureAirLog.v(TAG, "-------------pay on click");

                if (isNeedPay) {
                    // 直接调用接口，正常流程
                    pay(orderid);
                } else {
                    //直接提交订单，等待推送
                    checkOut();
                }

                break;

            case R.id.zfb:// 选择支付宝支付
                payType = 0;
                zfButton.setImageResource(R.drawable.sele);
                yhkButton.setImageResource(R.drawable.nosele);
                visaButton.setImageResource(R.drawable.nosele);
                masterCardButton.setImageResource(R.drawable.nosele);
                jcbButton.setImageResource(R.drawable.nosele);
                wechatButton.setImageResource(R.drawable.nosele);
                PictureAirLog.v(TAG, "ZFB");
                break;

            case R.id.yl:// 选择银联支付
                payType = 1;
                zfButton.setImageResource(R.drawable.nosele);
                yhkButton.setImageResource(R.drawable.sele);
                visaButton.setImageResource(R.drawable.nosele);
                masterCardButton.setImageResource(R.drawable.nosele);
                jcbButton.setImageResource(R.drawable.nosele);
                wechatButton.setImageResource(R.drawable.nosele);
                PictureAirLog.v(TAG, "YL");
                break;

            case R.id.paytype_visa:// paypal支付
                payType = 6;
                zfButton.setImageResource(R.drawable.nosele);
                yhkButton.setImageResource(R.drawable.nosele);
                visaButton.setImageResource(R.drawable.sele);
                masterCardButton.setImageResource(R.drawable.nosele);
                jcbButton.setImageResource(R.drawable.nosele);
                wechatButton.setImageResource(R.drawable.nosele);
                PictureAirLog.v(TAG, "PAYPAL");
                break;
            case R.id.paytype_mc:// paypal支付
                payType = 6;
                zfButton.setImageResource(R.drawable.nosele);
                yhkButton.setImageResource(R.drawable.nosele);
                visaButton.setImageResource(R.drawable.nosele);
                masterCardButton.setImageResource(R.drawable.sele);
                jcbButton.setImageResource(R.drawable.nosele);
                wechatButton.setImageResource(R.drawable.nosele);
                PictureAirLog.v(TAG, "PAYPAL");
                break;
            case R.id.paytype_jcb:// paypal支付
                payType = 6;
                zfButton.setImageResource(R.drawable.nosele);
                yhkButton.setImageResource(R.drawable.nosele);
                visaButton.setImageResource(R.drawable.nosele);
                masterCardButton.setImageResource(R.drawable.nosele);
                jcbButton.setImageResource(R.drawable.sele);
                wechatButton.setImageResource(R.drawable.nosele);
                PictureAirLog.v(TAG, "PAYPAL");
                break;

            case R.id.weixin:// wechat支付
                payType = 7;
                zfButton.setImageResource(R.drawable.nosele);
                yhkButton.setImageResource(R.drawable.nosele);
                visaButton.setImageResource(R.drawable.nosele);
                masterCardButton.setImageResource(R.drawable.nosele);
                jcbButton.setImageResource(R.drawable.nosele);
                wechatButton.setImageResource(R.drawable.sele);
                PictureAirLog.v(TAG, "WECHAT");
                break;

            default:
                break;
        }
    }

    /**
     * 支付函数
     *
     * @param orderId 订单号
     */
    private void pay(String orderId) {
        // TODO Auto-generated method stub
        payUtils = new PayUtils(this, paymentOrderHandler, orderId, nameString, introductString, priceString, PWJniUtil.getAESKey(Common.APP_TYPE_SHDRPP, 0));
        if (0 == payType) {// 支付宝支付方式
            try {
                payUtils.aliPay();

            } catch (Exception ex) {
                ex.printStackTrace();
                newToast.setTextAndShow(R.string.pay_failed, Common.TOAST_SHORT_TIME);
            }
        } else if (1 == payType) {
            PictureAirLog.v(TAG, "yl");
            if (!AppUtil.checkPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE)) {//没有权限
                newToast.setTextAndShow(R.string.permission_read_phone_state_message, Common.TOAST_SHORT_TIME);
                sbmtButton.setEnabled(true);
            } else {
                showPWProgressDialog();
                PictureAirLog.out("========orderId" + orderId);
                API1.getUnionPayTN(orderId, paymentOrderHandler);
            }
        } else if (6 == payType) {
            PictureAirLog.v(TAG, "paypal");
            Intent intent = new Intent(PaymentOrderActivity.this, WebViewActivity.class);
            intent.putExtra("key", 4);
            intent.putExtra("orderId", orderId);
            startActivityForResult(intent, 3333);
        } else if (payType == 7) {
            weChatIsPaying = true;
            PictureAirLog.v(TAG, "wechat");
            if (payUtils.isWechatInstalled()) {
                try {
                    // 调起微信支付
                    payUtils.wxPay();
                } catch (Exception e) {
                    e.printStackTrace();
                    newToast.setTextAndShow(R.string.pay_failed, Common.TOAST_SHORT_TIME);
                }
            } else {
                newToast.setTextAndShow(R.string.install_wechat_first, Common.TOAST_SHORT_TIME);
                sbmtButton.setEnabled(true);
            }
        } else {
            PictureAirLog.v(TAG, "other");
        }
    }

    // 支付失败处理
    private void ErrorInPayment() {
        // TODO Auto-generated method stub
        newToast.setTextAndShow(R.string.alipay_error, Common.TOAST_SHORT_TIME);
        // myApplication.setIsBuyingPhotoInfo(null);
        myApplication.clearIsBuyingPhotoList();
        myApplication.setRefreshViewAfterBuyBlurPhoto("");
        myApplication.setBuyPPPStatus("");
        AppManager.getInstance().killActivity(SubmitOrderActivity.class);
        AppManager.getInstance().killActivity(PreviewProductActivity.class);
        AppManager.getInstance().killActivity(SelectPhotoActivity.class);
        AppManager.getInstance().killActivity(PreviewPhotoActivity.class);
        AppManager.getInstance().killActivity(MakegiftActivity.class);
        AppManager.getInstance().killActivity(PPPDetailProductActivity.class);
        AppManager.getInstance().killActivity(DetailProductActivity.class);
        AppManager.getInstance().killActivity(ADVideoDetailProductActivity.class);
        finish();
    }

    // 取消操作处理
    private void CancelInPayment(boolean isCancel) {
        // TODO Auto-generated method stub
        // myApplication.setIsBuyingPhotoInfo(null);
        newToast.setTextAndShow(isCancel ? R.string.cancel_deal : R.string.pay_unsuccesss, Common.TOAST_SHORT_TIME);
        myApplication.clearIsBuyingPhotoList();
        myApplication.setRefreshViewAfterBuyBlurPhoto("");
        myApplication.setBuyPPPStatus("");
        if (isBack != null && !isBack.isEmpty() && isBack.equals("1")) {
            //返回到上一个界面
            AppManager.getInstance().killActivity(SubmitOrderActivity.class);
        } else {
            //进入订单界面
            AppManager.getInstance().killActivity(SubmitOrderActivity.class);
            AppManager.getInstance().killActivity(PreviewProductActivity.class);
            AppManager.getInstance().killActivity(SelectPhotoActivity.class);
            AppManager.getInstance().killActivity(PreviewPhotoActivity.class);
            AppManager.getInstance().killActivity(MakegiftActivity.class);
            AppManager.getInstance().killActivity(PPPDetailProductActivity.class);
            AppManager.getInstance().killActivity(DetailProductActivity.class);
            AppManager.getInstance().killActivity(ADVideoDetailProductActivity.class);
            if (fromPanicBuy == 1) {
                AppManager.getInstance().killActivity(PanicBuyActivity.class);
                API1.updateDealingOrder(paymentOrderHandler, orderid, "");
            } else {
                Intent intent2 = new Intent(PaymentOrderActivity.this, OrderActivity.class);
                startActivity(intent2);
            }
        }
        finish();
    }

    // 成功支付之后的操作
    private void SuccessAfterPayment() {
        // TODO Auto-generated method stub
        PictureAirLog.v(TAG, "start finish expired activity");
        AppManager.getInstance().killActivity(SubmitOrderActivity.class);
        AppManager.getInstance().killActivity(PreviewProductActivity.class);
        AppManager.getInstance().killActivity(PreviewPhotoActivity.class);
        AppManager.getInstance().killActivity(SelectPhotoActivity.class);
        AppManager.getInstance().killActivity(DetailProductActivity.class);
        AppManager.getInstance().killActivity(PPPDetailProductActivity.class);
        AppManager.getInstance().killActivity(MakegiftActivity.class);
        AppManager.getInstance().killActivity(OrderActivity.class);
        AppManager.getInstance().killActivity(ADVideoDetailProductActivity.class);
        AppManager.getInstance().killActivity(PanicBuyActivity.class);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        PictureAirLog.e(TAG, "requestCode" + requestCode);
//        if (requestCode == 10) {
//            //银联返回值处理
//            payUtils.unDealResult();
//        }

        if (data == null) {
            return;
        }

        // ipaylink 支付回调。
        if (requestCode == 3333 && resultCode == 111) {
            int payType = data.getIntExtra("payType", -2); //0: 支付成功 ， -1: 支付取消 ， -2: 支付失败
            switch (payType) {
                case 0:
                    paymentOrderHandler.sendEmptyMessage(PaymentOrderActivity.RQF_SUCCESS);
                    break;
                case -1:
                    paymentOrderHandler.sendEmptyMessage(PaymentOrderActivity.RQF_CANCEL);
                    break;
                case -2:
                    paymentOrderHandler.sendEmptyMessage(PaymentOrderActivity.RQF_UNSUCCESS);
                    break;
            }
            return;
        }

        if (requestCode == 10) {
            //支付控件返回字符串:success、fail、cancel 分别代表支付成功，支付失败，支付取消
            String str = data.getExtras().getString("pay_result");
            PictureAirLog.e(TAG, "str" + str);
            if (str.equalsIgnoreCase("success")) {
                // 支付成功后，extra中如果存在result_data，取出校验
                // result_data结构见c）result_data参数说明
                paymentOrderHandler.sendEmptyMessage(PaymentOrderActivity.RQF_SUCCESS);
//                if (data.hasExtra("result_data")) {
//                    String result = data.getExtras().getString("result_data");
//                    PictureAirLog.e(TAG, "result" + result);
//                    JSONObject resultJson = JSONObject.parseObject(result);
//                    PictureAirLog.e(TAG, "resultJson" + resultJson);
//                    String sign = resultJson.getString("sign");
//                    PictureAirLog.e(TAG, "sign" + sign);
//                    String dataOrg = resultJson.getString("data");
////                    // 验签证书同后台验签证书
////                    // 此处的verify，商户需送去商户后台做验签
////                    boolean ret = UnionpayRSAUtil.verify(dataOrg, sign, mMode);
////
//////                    PictureAirLog.e(TAG,"========" + ret);
////
////                    if (ret) {
////                        // 验证通过后，显示支付结果
////                    paymentOrderHandler.sendEmptyMessage(PaymentOrderActivity.RQF_SUCCESS);
////                    } else {
////                        // 验证不通过后的处理
////                        // 建议通过商户后台查询支付结果
////                        paymentOrderHandler.sendEmptyMessage(PaymentOrderActivity.RQF_ERROR);
////                    }
//                } else {
//                    // 未收到签名信息
//                    // 建议通过商户后台查询支付结果
//                    paymentOrderHandler.sendEmptyMessage(PaymentOrderActivity.RQF_SUCCESS);
//                }
            } else if (str.equalsIgnoreCase("fail")) {
                paymentOrderHandler.sendEmptyMessage(PaymentOrderActivity.RQF_ERROR);
            } else if (str.equalsIgnoreCase("cancel")) {
                paymentOrderHandler.sendEmptyMessage(PaymentOrderActivity.RQF_CANCEL);
            }
        }
    }

    /**
     * 处理收到推送的数据
     *
     * @param resultJsonObject
     */
    public void dealData(org.json.JSONObject resultJsonObject) {
        PictureAirLog.v(TAG, "dealData()");
        Intent intent;
        if (resultJsonObject.has("pppCode")) {// ppp
            // product
            PictureAirLog.v(TAG, "----------------->buy ppp");
            myApplication.setNeedRefreshPPPList(true);
            if (myApplication.getBuyPPPStatus().equals(Common.FROM_AD_ACTIVITY)) {
                myApplication.setBuyPPPStatus(Common.FROM_AD_ACTIVITY_PAYED);

            } else if (myApplication.getBuyPPPStatus().equals(Common.FROM_PREVIEW_PPP_ACTIVITY)) {
                myApplication.setBuyPPPStatus(Common.FROM_PREVIEW_PPP_ACTIVITY_PAYED);

            }
            intent = new Intent(PaymentOrderActivity.this, MyPPPActivity.class);
            API1.PPPlist.clear();

        } else {
            // 以下两种情况，进入图片清晰页面
            PictureAirLog.v(TAG, "get refresh view after buy blur photo---->" + myApplication.getRefreshViewAfterBuyBlurPhoto());
            // 以下三种情况要回到清晰图片页面
            if (myApplication.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_VIEWORSELECTACTIVITY)
                    || myApplication.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_MYPHOTOPASS)
                    || myApplication.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_PREVIEW_PHOTO_ACTIVITY)) {
                PictureAirLog.v("flag is -------------------->", myApplication.getRefreshViewAfterBuyBlurPhoto());
                PictureAirLog.v("photoId---->", myApplication.getIsBuyingPhotoId());
                String tab = myApplication.getIsBuyingTabName();
                String photoId = myApplication.getIsBuyingPhotoId();
                PictureAirLog.out("tabname---->" + tab + "photoid--->" + photoId);

                intent = new Intent(PaymentOrderActivity.this, PreviewPhotoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("position", -1);
                bundle.putString("tab", tab);
                bundle.putString("photoId", photoId);
                intent.putExtra("bundle", bundle);

                // 清空标记
                myApplication.clearIsBuyingPhotoList();

                if (myApplication.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_VIEWORSELECTACTIVITY)) {
                    myApplication.setRefreshViewAfterBuyBlurPhoto(Common.FROM_VIEWORSELECTACTIVITYANDPAYED);
                } else if (myApplication.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_MYPHOTOPASS)) {
                    myApplication.setRefreshViewAfterBuyBlurPhoto(Common.FROM_MYPHOTOPASSPAYED);
                } else if (myApplication.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_PREVIEW_PHOTO_ACTIVITY)) {
                    myApplication.setRefreshViewAfterBuyBlurPhoto("");
                }

            } else {
                // 回到订单页面
                PictureAirLog.v(TAG, "----------------->回到订单页面 productType： " + productType);
                intent = new Intent(PaymentOrderActivity.this, OrderActivity.class);
                intent.putExtra("orderType", productType);
            }
        }
//        SharedPreferences.Editor editor = sPreferences.edit();
//        editor.putBoolean(Common.NEED_FRESH, true);
//        editor.commit();
        dismissPWProgressDialog();
        SuccessAfterPayment();
        startActivity(intent);
        finish();
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        paymentOrderHandler.removeCallbacksAndMessages(null);
//        unregisterReceiver(broadcastReceiver); // 解除广播
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
        if (weChatIsPaying) {
            int resultCode = SPUtils.getInt(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.WECHAT_PAY_STATUS, -3);
            //处理微信返回值，
            payUtils.wxDealResult(resultCode);
            weChatIsPaying = false;
            SPUtils.put(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.WECHAT_PAY_STATUS, -3);
        }
    }

    @Override
    public void TopViewClick(View view) {
        super.TopViewClick(view);
        switch (view.getId()) {
            case R.id.topLeftView:
                backToLastActivity();

                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backToLastActivity();
    }

    private void backToLastActivity() {
        //返回键
        if (isNeedPay) {
            PictureAirLog.v(TAG, "TopViewClick onBackPressed");
            CancelInPayment(true);
        } else {
            //0元支付  购物车数量恢复
            int currentCartCount = SPUtils.getInt(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
            SPUtils.put(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, currentCartCount + cartCount);
            finish();
        }
    }

    @Subscribe
    public void onUserEvent(BaseBusEvent baseBusEvent) {
        PictureAirLog.v(TAG, "onUserEvent（）");
        if (baseBusEvent instanceof AsyncPayResultEvent) {
            AsyncPayResultEvent asyncPayResultEvent = (AsyncPayResultEvent) baseBusEvent;
            PictureAirLog.out("get asyncPayResultEvent----->" + asyncPayResultEvent.getAsyncPayResult());

            if (!asyncTimeOut) {//如果没有超时，需要处理
                payAsyncResultJsonObject = asyncPayResultEvent.getAsyncPayResult();
                //接受到推送之后，先将handler清空，然后再执行新的任务
                paymentOrderHandler.removeCallbacks(runnable);

                paymentOrderHandler.sendEmptyMessage(ASYNC_PAY_SUCCESS);
            }

            //刷新列表
            EventBus.getDefault().removeStickyEvent(asyncPayResultEvent);
        }
    }

}
