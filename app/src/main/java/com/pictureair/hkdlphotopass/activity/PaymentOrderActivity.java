package com.pictureair.hkdlphotopass.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
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
import com.payeco.android.plugin.PayecoPluginLoadingActivity;
import com.pictureair.hkdlphotopass.eventbus.BuySingleDigital;
import com.pictureair.jni.ciphermanager.PWJniUtil;
import com.pictureair.hkdlphotopass.MyApplication;
import com.pictureair.hkdlphotopass.R;
import com.pictureair.hkdlphotopass.entity.OrderInfo;
import com.pictureair.hkdlphotopass.eventbus.AsyncPayResultEvent;
import com.pictureair.hkdlphotopass.eventbus.BaseBusEvent;
import com.pictureair.hkdlphotopass.greendao.PictureAirDbManager;
import com.pictureair.hkdlphotopass.http.rxhttp.RxSubscribe;
import com.pictureair.hkdlphotopass.util.API2;
import com.pictureair.hkdlphotopass.util.AppManager;
import com.pictureair.hkdlphotopass.util.AppUtil;
import com.pictureair.hkdlphotopass.util.Common;
import com.pictureair.hkdlphotopass.util.JsonUtil;
import com.pictureair.hkdlphotopass.util.PayUtils;
import com.pictureair.hkdlphotopass.util.PictureAirLog;
import com.pictureair.hkdlphotopass.util.ReflectionUtil;
import com.pictureair.hkdlphotopass.util.SPUtils;
import com.pictureair.hkdlphotopass.widget.PWToast;
import com.trello.rxlifecycle.android.ActivityEvent;
import com.unionpay.UPPayAssistEx;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import rx.android.schedulers.AndroidSchedulers;

/**
 * 支付页面，目前是  银联  支付宝   微信   ipaylinks 4种支付方式
 */
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
    private RelativeLayout masterCardLayout;
    private RelativeLayout jcbLayout;
    private RelativeLayout americanexpressLayout;

    private ImageView zfButton;
    private ImageView ylButton;
    private ImageView visaButton;
    private ImageView masterCardButton;
    private ImageView jcbButton;
    private ImageView americanexpressButton;

    public static final int RQF_SUCCESS = 1;
    public static final int RQF_CANCEL = 2;
    public static final int RQF_ERROR = 3;
    public static final int INITPAYPAL = 4;
    public static final int RQF_UNSUCCESS = 5;
    private static final int ASYNC_PAY_SUCCESS = 6;

    private static final String TAG = "PaymentOrderActivity";

    private MyApplication myApplication;
    private PWToast newToast;
    private boolean paySyncResult = false;
    private org.json.JSONObject payAsyncResultJsonObject;

    private int payType = 2;// 支付类型 0 支付宝 1 银联 2 VISA信用卡 3 代付 4 分期 5 自提 6 paypal 7
    private String ccPayType = "VISA";
    private PayUtils payUtils;
    //广播地址，用于接收易联支付插件支付完成之后回调客户端
    private final static String BROADCAST_PAY_END = "com.pictureair.hkdlphotopass.broadcast";
    /**
     * @Fields payecoPayBroadcastReceiver : 易联支付插件广播
     */
    private BroadcastReceiver payecoPayBroadcastReceiver;

    private OrderInfo orderInfo;
    private String outletId;
    private JSONArray cartItemIds;
    private String orderid = "";
    private String orderIdInBackend = "";
    private boolean weChatIsPaying = false;
    private int productType = 0;//商品类型 1-实体商品 2-虚拟商品
    private String isBack = "0";//用于判断是否需要返回 0- 不返回 1-返回

    private boolean needAddress = false;
    private boolean isPaying = false;

    //mMode参数解释： "00" - 启动银联正式环境 "01" - 连接银联测试环境
    private final String mMode = "00";
    private String tNCode;
    private String orderCode;

    private boolean isNeedPay = true;//是否需要支付
    private JSONArray couponCodes;//优惠券
    private int cartCount = 0;//购物车数量

    private boolean asyncTimeOut = false;
    private int fromPanicBuy;
    private String dealingKey;

    private final Handler paymentOrderHandler = new PaymentOrderHandler(this);

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            PictureAirLog.v(TAG, "run");
            //手动拉取信息
            if (payAsyncResultJsonObject == null) {//没有收到推送，需要手动获取
                asyncTimeOut = true;
                getSocketData();
            }
        }
    };


    private void getSocketData() {
        API2.getSocketData()
                .compose(this.<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        PictureAirLog.v(TAG, "GET_SOCKET_DATA_SUCCESS: " + jsonObject.toJSONString());
                        boolean isSuccess = false;
                        if (jsonObject.size() > 0) {
                            isSuccess = JsonUtil.dealGetSocketData(PaymentOrderActivity.this, jsonObject.toString(), false, orderIdInBackend);
                        }
                        if (!isSuccess) {
                            dismissPWProgressDialog();
                            if (payAsyncResultJsonObject == null) {
                                PictureAirDbManager.insertPaymentOrderIdDB(SPUtils.getString(PaymentOrderActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ID, ""), orderid);
                            }
                            SuccessAfterPayment();
                            finish();
                        }
                    }

                    @Override
                    public void _onError(int status) {
                        dismissPWProgressDialog();
                        PictureAirLog.v(TAG, "GET_SOCKET_DATA_FAILED: " + status);
                        if (payAsyncResultJsonObject == null) {
                            PictureAirDbManager.insertPaymentOrderIdDB(SPUtils.getString(PaymentOrderActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ID, ""), orderid);
                        }
                        SuccessAfterPayment();
                        finish();
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

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
                if (fromPanicBuy == 0) {
                    Intent intent1 = new Intent(PaymentOrderActivity.this, OrderActivity.class);
                    startActivity(intent1);
                }
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
                rqf_success();

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

            case PayUtils.SHOW_DIALOG:
                showPWProgressDialog();
                break;

            case PayUtils.DISMISS_DIALOG:
                dismissPWProgressDialog();
                break;

            default:
                break;
        }
    }

    private void rqf_success() {
        PictureAirLog.v(TAG, "RQF_SUCCESS orderid: " + orderid);
        //支付成功后：出现等待弹窗，5秒后进入订单页面。其中接收推送，若没有推送则将订单ID写入数据库，状态为灰色不可点击
        showPWProgressDialog();
        paySyncResult = true;
        paymentOrderHandler.postDelayed(runnable, 5000);
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
        setTopLeftValueAndShow(R.drawable.back_blue, true);
        paySv = findViewById(R.id.pay_sv);
        noPayLl = (LinearLayout) findViewById(R.id.no_pay_ll);
        sbmtButton = (TextView) findViewById(R.id.button_smpm);
        sbmtButton.setTypeface(MyApplication.getInstance().getFontBold());
        // 支付方式选择
        visaButton = (ImageView) findViewById(R.id.imageButton_visa);
        masterCardButton = (ImageView) findViewById(R.id.imageButton_mastercard);
        americanexpressButton = (ImageView) findViewById(R.id.imageButton_american_express);
        jcbButton = (ImageView) findViewById(R.id.imageButton_jcb);
        ylButton = (ImageView) findViewById(R.id.imageButton_union_pay);
        zfButton = (ImageView) findViewById(R.id.imageButton_aliPay);

        visaLayout = (RelativeLayout) findViewById(R.id.visa);
        masterCardLayout = (RelativeLayout) findViewById(R.id.mastercard);
        americanexpressLayout = (RelativeLayout) findViewById(R.id.american_express);
        jcbLayout = (RelativeLayout) findViewById(R.id.jcb);
        ylLayout = (RelativeLayout) findViewById(R.id.union_pay);
        zfbLayout = (RelativeLayout) findViewById(R.id.aliPay);


    }

    private void init() {
        //初始化支付结果广播接收器
        initPayecoPayBroadcastReceiver();

        //注册支付结果广播接收器
        registerPayecoPayBroadcastReceiver();

        newToast = new PWToast(this);
        myApplication = (MyApplication) getApplication();

        sbmtButton.setOnClickListener(this);
        visaButton.setImageResource(R.drawable.sele);
        masterCardButton.setImageResource(R.drawable.nosele);
        americanexpressButton.setImageResource(R.drawable.nosele);
        jcbButton.setImageResource(R.drawable.nosele);
        ylButton.setImageResource(R.drawable.nosele);
        zfButton.setImageResource(R.drawable.nosele);

        visaLayout.setOnClickListener(this);
        masterCardLayout.setOnClickListener(this);
        americanexpressLayout.setOnClickListener(this);
        jcbLayout.setOnClickListener(this);
        ylLayout.setOnClickListener(this);
        zfbLayout.setOnClickListener(this);

        if (getIntent().getStringExtra("flag") == null) {
            // 为空，说明是正常流程进入
            PictureAirLog.v(TAG, "为空，说明是正常流程进入");
            isNeedPay = getIntent().getBooleanExtra("isNeedPay", true);
            nameString = getIntent().getStringExtra("name");// 获取name
            priceString = getIntent().getStringExtra("price");// 获取price
            introductString = getIntent().getStringExtra("introduce");// 获取介绍信息
            orderid = getIntent().getStringExtra("orderId");
            orderIdInBackend = getIntent().getStringExtra("orderIdInBackend");
            outletId = getIntent().getStringExtra("outletId");
            cartItemIds = JSONArray.parseArray(getIntent().getStringExtra("cartItemIds"));
            String couponCodesStr = getIntent().getStringExtra("couponCodes");
            couponCodes = !TextUtils.isEmpty(couponCodesStr) ? JSONArray.parseArray(getIntent().getStringExtra("couponCodes")) : null;
            cartCount = getIntent().getIntExtra("cartCount", 0);
            fromPanicBuy = getIntent().getIntExtra("fromPanicBuy", 0);
            dealingKey = getIntent().getStringExtra("dealingKey");

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
                addOrder(cartItemIds, 1, outletId, "", couponCodes, null, null, null);
            } else {
                //PP+/数码商品不需要地址
                addOrder(cartItemIds, 3, "", "", couponCodes, null, null, null);
            }
        }
    }

    private void addOrder(JSONArray pJsonArray, int pDeliveryType, String pOutletId, String pAddressId,
                          JSONArray pCouponCodes, JSONObject pInvoice,
                          String pChannelId, String pUid) {
        API2.addOrder(pJsonArray, pDeliveryType, pOutletId, pAddressId, pCouponCodes, pInvoice, pChannelId, pUid)
                .compose(this.<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        rqf_success();
                    }

                    @Override
                    public void _onError(int status) {
                        PictureAirLog.e(TAG, "ADD_ORDER_FAILED cade: " + status);
                        dismissPWProgressDialog();
                        newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), status), Common.TOAST_SHORT_TIME);
                        Intent errorIntent = new Intent(PaymentOrderActivity.this, OrderActivity.class);
                        startActivity(errorIntent);
                        ErrorInPayment();
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
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

            case R.id.visa:// paydollar支付 --  visa
                payType = 2;
                ccPayType = "VISA";
                visaButton.setImageResource(R.drawable.sele);
                masterCardButton.setImageResource(R.drawable.nosele);
                americanexpressButton.setImageResource(R.drawable.nosele);
                jcbButton.setImageResource(R.drawable.nosele);
                ylButton.setImageResource(R.drawable.nosele);
                zfButton.setImageResource(R.drawable.nosele);
                PictureAirLog.v(TAG, payType + "--" + ccPayType);
                break;

            case R.id.mastercard:// paydollar支付 -- mastercard
                payType = 2;
                ccPayType = "Master";
                visaButton.setImageResource(R.drawable.nosele);
                masterCardButton.setImageResource(R.drawable.sele);
                americanexpressButton.setImageResource(R.drawable.nosele);
                jcbButton.setImageResource(R.drawable.nosele);
                ylButton.setImageResource(R.drawable.nosele);
                zfButton.setImageResource(R.drawable.nosele);
                PictureAirLog.v(TAG, payType + "--" + ccPayType);
                break;

            case R.id.american_express:// paydollar支付 -- american_express
                payType = 2;
                ccPayType = "AMEX";
                visaButton.setImageResource(R.drawable.nosele);
                masterCardButton.setImageResource(R.drawable.nosele);
                americanexpressButton.setImageResource(R.drawable.sele);
                jcbButton.setImageResource(R.drawable.nosele);
                ylButton.setImageResource(R.drawable.nosele);
                zfButton.setImageResource(R.drawable.nosele);
                PictureAirLog.v(TAG, payType + "--" + ccPayType);
                break;

            case R.id.jcb:// paydollar支付 -- jcb
                payType = 2;
                ccPayType = "JCB";
                visaButton.setImageResource(R.drawable.nosele);
                masterCardButton.setImageResource(R.drawable.nosele);
                americanexpressButton.setImageResource(R.drawable.nosele);
                jcbButton.setImageResource(R.drawable.sele);
                ylButton.setImageResource(R.drawable.nosele);
                zfButton.setImageResource(R.drawable.nosele);
                PictureAirLog.v(TAG, payType + "--" + ccPayType);
                break;

            case R.id.union_pay://payeco支付
                payType = 3;
                visaButton.setImageResource(R.drawable.nosele);
                masterCardButton.setImageResource(R.drawable.nosele);
                americanexpressButton.setImageResource(R.drawable.nosele);
                jcbButton.setImageResource(R.drawable.nosele);
                ylButton.setImageResource(R.drawable.sele);
                zfButton.setImageResource(R.drawable.nosele);
                PictureAirLog.v(TAG, payType + "--" + ccPayType);
                break;

            case R.id.aliPay:// 选择支付宝支付
                payType = 0;
                visaButton.setImageResource(R.drawable.nosele);
                masterCardButton.setImageResource(R.drawable.nosele);
                americanexpressButton.setImageResource(R.drawable.nosele);
                jcbButton.setImageResource(R.drawable.nosele);
                ylButton.setImageResource(R.drawable.nosele);
                zfButton.setImageResource(R.drawable.sele);
                PictureAirLog.v(TAG, payType + "--" + ccPayType);
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
                getUnionPayTN(orderId);
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
        } else if (payType == 2) {
            PictureAirLog.v(TAG, "paydollar");
            Intent intent = new Intent(PaymentOrderActivity.this, WebViewActivity.class);
            intent.putExtra("key", 7);
            intent.putExtra("orderId", orderId);
            intent.putExtra("ccPayType", ccPayType);
            PictureAirLog.out("======  orderId =====" + orderId);
            PictureAirLog.out("======  ccPayType =====" + ccPayType);
            startActivityForResult(intent, 4444);
        } else if (payType == 3) {
            PictureAirLog.v(TAG, "payeco");
            if (!AppUtil.checkPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE)) {//没有权限
                newToast.setTextAndShow(R.string.permission_read_phone_state_message, Common.TOAST_SHORT_TIME);
                sbmtButton.setEnabled(true);
            } else {
                showPWProgressDialog();
//                API1.getPayecoInfo(paymentOrderHandler, orderId, 3);
                getPayecoInfo(orderId);
            }
        } else {
            PictureAirLog.v(TAG, "other");
        }
    }

    private void getPayecoInfo(String orderId) {
        API2.getPayecoInfo(orderId, 3)
                .compose(this.<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        PictureAirLog.e(TAG, "GET_PAYECO_INFO_SUCCESSS: " + jsonObject);
                        dismissPWProgressDialog();
                        PictureAirLog.v(TAG, "msg.obj: " + jsonObject);
                        if (jsonObject == null || jsonObject.toString().length() == 0) {
                            paymentOrderHandler.sendEmptyMessage(RQF_ERROR);
                        } else {
                            //获得支付信息成功 -- 解析数据
                            PictureAirLog.v(TAG, "msg.obj: " + jsonObject);
                            JSONObject result = (JSONObject) jsonObject;
                            PictureAirLog.v(TAG, "GET_PAYECO_INFO_SUCCESSS:  result： " + result);
                            if (result.getString("RetCode") == null || !"0000".equals(result.getString("RetCode"))) {
                                paymentOrderHandler.sendEmptyMessage(RQF_ERROR);
                                return;
                            }

                            orderCode = result.getString("MerchOrderId");
                            //组织参数用于跳转至易联支付插件，示例如下
                            //{
                            //	"Version": "2.0.0",
                            //	"MerchOrderId": "1408006824547",
                            //	"MerchantId": "302020000058",
                            //	"Amount": "5.00",
                            //	"TradeTime": "20140814170024",
                            //	"OrderId": "302014081400038872",
                            //	"Sign": "QBOiI4xl1CgWNHt+8KTyVR2c9bAGNMMkXTHsYhJrmr9QPuHhRe1CiPGu+beOiayQTGGigTJEzUm23q0lAnDoXcnmwt7bsyG+UOwl3m9OKUd8o+SP741OOJxXHK884OXWuygMXkczK+TvYhNv/RLYKgAVSG6qN0lmsc2lek+cxqo="
                            //}

                            result.remove("RetCode");//RetCode参数不需要传递给易联支付插件
                            result.remove("RetMsg");//RetMsg参数不需要传递给易联支付插件

                            String upPayReqString = result.toString();
                            PictureAirLog.i("test", "请求易联支付插件，参数：" + upPayReqString);

                            //跳转至易联支付插件
                            Intent intent = new Intent(PaymentOrderActivity.this, PayecoPluginLoadingActivity.class);
                            intent.putExtra("upPay.Req", upPayReqString);
                            intent.putExtra("Broadcast", BROADCAST_PAY_END); //广播接收地址
                            intent.putExtra("Environment", "01"); // 00: 测试环境, 01: 生产环境
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void _onError(int status) {
                        PictureAirLog.v(TAG, "UNIONPAY_GET_TN_FAILED: ");
                        dismissPWProgressDialog();
                        newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                        paymentOrderHandler.sendEmptyMessage(RQF_ERROR);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    private void getUnionPayTN(String orderId) {
        API2.getUnionPayTN(orderId)
                .compose(this.<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        PictureAirLog.v(TAG, "UNIONPAY_GET_TN_SUCCESS: ");
                        dismissPWProgressDialog();
                        if (jsonObject == null || jsonObject.toString().length() == 0) {
                            PictureAirLog.v(TAG, "msg.obj: " + jsonObject);
                            paymentOrderHandler.sendEmptyMessage(RQF_ERROR);
                        } else {
                            //获得TN号成功 -- 解析数据
                            PictureAirLog.v(TAG, "UNIONPAY_GET_TN_SUCCESS:  result： " + jsonObject);
                            tNCode = jsonObject.getString("tn");//
                            UPPayAssistEx.startPay(PaymentOrderActivity.this, null, null, tNCode, mMode);
                            PictureAirLog.v(TAG, "tNCode: " + tNCode);
                        }
                    }

                    @Override
                    public void _onError(int status) {
                        PictureAirLog.v(TAG, "UNIONPAY_GET_TN_FAILED: ");
                        dismissPWProgressDialog();
                        newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                        paymentOrderHandler.sendEmptyMessage(RQF_ERROR);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
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
        if (fromPanicBuy == 1) {
            AppManager.getInstance().killActivity(PanicBuyActivity.class);
            showPWProgressDialog();
            updateDealingOrder(orderid, dealingKey);
        } else {
            finish();
        }
    }

    private void updateDealingOrder(String orderId, String dealingKey) {
        API2.updateDealingOrder(orderId, dealingKey)
                .compose(this.<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        PictureAirLog.json(jsonObject.toString());
                        dismissPWProgressDialog();
                        finish();
                    }

                    @Override
                    public void _onError(int status) {
                        dismissPWProgressDialog();
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
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
            finish();
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
                showPWProgressDialog();
                updateDealingOrder(orderid, dealingKey);
            } else {
                Intent intent2 = new Intent(PaymentOrderActivity.this, OrderActivity.class);
                startActivity(intent2);
                finish();
            }
        }

    }

    // 成功支付之后的操作
    private void SuccessAfterPayment() {
        // TODO Auto-generated method stub
        PictureAirLog.v(TAG, "start finish expired activity");
        AppManager.getInstance().killActivity(SubmitOrderActivity.class);
        AppManager.getInstance().killActivity(PreviewProductActivity.class);
//        AppManager.getInstance().killActivity(PreviewPhotoActivity.class);
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
            String str = data.getExtras().getString("pay_result", "fail");
            PictureAirLog.e(TAG, "str" + str);
            if (str.equalsIgnoreCase("success")) {
                // 支付成功后，extra中如果存在result_data，取出校验
                // result_data结构见c）result_data参数说明
                paymentOrderHandler.sendEmptyMessage(PaymentOrderActivity.RQF_SUCCESS);
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
        PictureAirLog.v(TAG, "dealData()" + resultJsonObject.toString());
        Intent intent = null;
        try {
            if (resultJsonObject.has("pppCode") &&
                    resultJsonObject.getJSONArray("pppCode") != null
                    && resultJsonObject.getJSONArray("pppCode").length() != 0) {// ppp
                // product
                PictureAirLog.v(TAG, "----------------->buy ppp");
                myApplication.setNeedRefreshPPPList(true);
                if (myApplication.getBuyPPPStatus().equals(Common.FROM_AD_ACTIVITY)) {
                    myApplication.setBuyPPPStatus(Common.FROM_AD_ACTIVITY_PAYED);

                } else if (myApplication.getBuyPPPStatus().equals(Common.FROM_PREVIEW_PPP_ACTIVITY)) {
                    myApplication.setBuyPPPStatus(Common.FROM_PREVIEW_PPP_ACTIVITY_PAYED);

                }
                intent = new Intent(PaymentOrderActivity.this, MyPPPActivity.class);//只能购买一日通
                intent.putExtra("dailyppp", true);
                API2.PPPlist.clear();
                startActivity(intent);
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

//                    intent = new Intent(PaymentOrderActivity.this, PreviewPhotoActivity.class);
//                    Bundle bundle = new Bundle();
//                    bundle.putInt("position", -1);
//                    bundle.putString("tab", tab);
//                    bundle.putString("photoId", photoId);
//                    bundle.putString("ppCode", myApplication.getIsBuyingPhotoPassCode());
//                    bundle.putString("shootDate", myApplication.getIsBuyingPhotoShootTime());
//                    intent.putExtra("bundle", bundle);
                    EventBus.getDefault().post(new BuySingleDigital(0));

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
                    startActivity(intent);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        SharedPreferences.Editor editor = sPreferences.edit();
//        editor.putBoolean(Common.NEED_FRESH, true);
//        editor.commit();
        dismissPWProgressDialog();
        SuccessAfterPayment();
//        startActivity(intent);
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
        unRegisterPayecoPayBroadcastReceiver();
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
//        super.onBackPressed();
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

            if (asyncTimeOut) {//如果没有超时，需要处理
                payAsyncResultJsonObject = asyncPayResultEvent.getAsyncPayResult();
                //接受到推送之后，先将handler清空，然后再执行新的任务
                paymentOrderHandler.removeCallbacks(runnable);

                paymentOrderHandler.sendEmptyMessage(ASYNC_PAY_SUCCESS);
            }

            //刷新列表
            EventBus.getDefault().removeStickyEvent(asyncPayResultEvent);
        }
    }

    /**
     * @Title registerPayecoPayBroadcastReceiver
     * @Description 注册广播接收器
     */
    private void registerPayecoPayBroadcastReceiver() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_PAY_END);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(payecoPayBroadcastReceiver, filter);
    }

    /**
     * @Title unRegisterPayecoPayBroadcastReceiver
     * @Description 注销广播接收器
     */
    private void unRegisterPayecoPayBroadcastReceiver() {

        if (payecoPayBroadcastReceiver != null) {
            unregisterReceiver(payecoPayBroadcastReceiver);
            payecoPayBroadcastReceiver = null;
        }
    }

    //初始化支付结果广播接收器
    private void initPayecoPayBroadcastReceiver() {
        payecoPayBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                showPWProgressDialog();

                //接收易联支付插件的广播回调
                String action = intent.getAction();
                if (!BROADCAST_PAY_END.equals(action)) {
                    PictureAirLog.e("test", "接收到广播，但与注册的名称不一致[" + action + "]");
                    return;
                }

                //商户的业务处理
                String result = intent.getExtras().getString("upPay.Rsp");
                PictureAirLog.i("test", "接收到广播内容：" + result);

                final String notifyParams = result;

                //查詢訂單狀態
                getOrderStatus();

                String mPayResult = result;

                try {
                    org.json.JSONObject json = new org.json.JSONObject(mPayResult);
                    PictureAirLog.e("json =======", "json" + json);

                    //返回支付状态
                    if (json.has("respCode")) {

                        String respCode = json.getString("respCode");
                        if ("W101".equals(respCode)) { //W101订单未支付，用户主动退出插件
                            paymentOrderHandler.sendEmptyMessage(PaymentOrderActivity.RQF_CANCEL);
                        }

                        if (!"0000".equals(respCode)) { //非0000，订单支付响应异常
                            String respDesc = json.getString("respDesc");
//                            paymentOrderHandler.sendEmptyMessage(PaymentOrderActivity.RQF_ERROR);
                        }
                    }

                    if (json.has("Status")) {
                        if ("01".equals(json.getString("Status"))) {
                            //status = "未支付";
                            paymentOrderHandler.sendEmptyMessage(PaymentOrderActivity.RQF_CANCEL);
                        }
                        if ("02".equals(json.getString("Status"))) {
                            //status = "已支付";
                            paymentOrderHandler.sendEmptyMessage(PaymentOrderActivity.RQF_SUCCESS);
                        }
                        if ("05".equals(json.getString("Status"))) {
                            //status = "已作废";
                            paymentOrderHandler.sendEmptyMessage(PaymentOrderActivity.RQF_CANCEL);
                        }
                        if ("10".equals(json.getString("Status"))) {
                            //status = "调账-支付成功";
                            paymentOrderHandler.sendEmptyMessage(PaymentOrderActivity.RQF_SUCCESS);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };
    }

    private void getOrderStatus() {
        API2.checkOrderStatus(orderCode)
                .compose(this.<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        String result = jsonObject.toJSONString();
                        dismissPWProgressDialog();

                        if (result == null) {
                            PictureAirLog.e("test", "通知失败！");
                            return;
                        }

                        PictureAirLog.i("test", "响应数据：" + result);

                        try {
                            //解析响应数据
                            org.json.JSONObject json = new org.json.JSONObject(result);

                            //校验返回结果
                            if (!json.has("RetMsg")) {
                                newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                                PictureAirLog.e("test", "返回数据有误:" + result);
                                return;
                            }
                        } catch (JSONException e) {
                            PictureAirLog.e("test", "解析处理失败！");
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void _onError(int status) {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });

    }

}
