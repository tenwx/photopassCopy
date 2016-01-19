package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.alipay.PayResult;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.OrderInfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AliPayUtil;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import cn.smssdk.gui.CustomProgressDialog;
import com.pictureair.photopass.widget.MyToast;
import com.pictureair.photopass.wxpay.Constants;
import com.pictureair.photopass.wxpay.MD5;
import com.pictureair.photopass.wxpay.Util;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.unionpay.UPPayAssistEx;
import com.unionpay.uppay.PayActivity;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PaymentOrderActivity extends BaseActivity implements
        OnClickListener {
    private TextView sbmtButton;
    private String nameString;
    private String priceString;
    private String introductString;

    private RelativeLayout zfbLayout;
    private RelativeLayout ylLayout;
    private RelativeLayout paypalLayout;
    private RelativeLayout wechatLayout;

    private ImageView zfButton;
    private ImageView yhkButton;
    private ImageView paypalButton;
    private ImageView wechatButton;

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

    private static final String TAG = "PaymentOrderActivity";

    /*****************************************************************
     * mMode参数解释： "00" - 启动银联正式环境 "01" - 连接银联测试环境
     *****************************************************************/
    private final String mMode = "01";

    // -----------微信支付参数----------------//
    PayReq req;
    final IWXAPI msgApi = WXAPIFactory.createWXAPI(this, null);
    Map<String, String> resultunifiedorder;
    StringBuffer sb;


    private PictureAirDbManager pictureAirDbManager;
    public static org.json.JSONObject resultJsonObject;
    private CustomProgressDialog customProgressDialog;
    private int productType = 0;//商品类型 1-实体商品 2-虚拟商品
    private String isBack = "0";//用于判断是否需要返回 0- 不返回 1-返回

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_order);
        findViewById();
        init();
    }

    private void findViewById() {
        setTopTitleShow(R.string.pay);
        setTopLeftValueAndShow(R.drawable.back_white, true);
        sbmtButton = (TextView) findViewById(R.id.button_smpm);
        sbmtButton.setTypeface(MyApplication.getInstance().getFontBold());
        // 支付方式选择
        zfButton = (ImageView) findViewById(R.id.imageButton1_zfb);
        yhkButton = (ImageView) findViewById(R.id.imageButton2_yhk);
        paypalButton = (ImageView) findViewById(R.id.imageButton3_paypal);
        wechatButton = (ImageView) findViewById(R.id.imageButton2_weixin);
        zfbLayout = (RelativeLayout) findViewById(R.id.zfb);
        ylLayout = (RelativeLayout) findViewById(R.id.yl);
        paypalLayout = (RelativeLayout) findViewById(R.id.paypal);
        wechatLayout = (RelativeLayout) findViewById(R.id.weixin);
        pictureAirDbManager = new PictureAirDbManager(MyApplication.getInstance());

    }

    private void init() {
        sPreferences = getSharedPreferences(Common.USERINFO_NAME,
                MODE_PRIVATE);
        newToast = new MyToast(this);
        myApplication = (MyApplication) getApplication();

        // 获取wechat的实例对象
        req = new PayReq();
        sb = new StringBuffer();

        msgApi.registerApp(Constants.APP_ID);

//		lrtLayout.setOnClickListener(this);
        sbmtButton.setOnClickListener(this);
        zfButton.setImageResource(R.drawable.sele);
        yhkButton.setImageResource(R.drawable.nosele);
        paypalButton.setImageResource(R.drawable.nosele);
        wechatButton.setImageResource(R.drawable.nosele);

        zfbLayout.setOnClickListener(this);
        ylLayout.setOnClickListener(this);
        paypalLayout.setOnClickListener(this);
        wechatLayout.setOnClickListener(this);

        if (getIntent().getStringExtra("flag") == null) {
            // 为空，说明是正常流程进入
            PictureAirLog.v(TAG, "为空，说明是正常流程进入");
            nameString = getIntent().getStringExtra("name");// 获取name
            priceString = getIntent().getStringExtra("price");// 获取price
            introductString = getIntent().getStringExtra("introduce");// 获取介绍信息
            orderid = getIntent().getStringExtra("orderId");

        } else if ("order".equals(getIntent().getStringExtra("flag"))) {
            // 从订单页面进入
            PictureAirLog.v(TAG, "从订单页面进入");
            orderInfo = getIntent().getParcelableExtra("deliveryInfo");
            orderid = orderInfo.orderId;
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
    }


    /**
     * 支付函数
     *
     * @param orderId 订单号
     */
    private void pay(String orderId) {
        // TODO Auto-generated method stub
        if (0 == payType) {// 支付宝支付方式
            try {
                String info = AliPayUtil.getOrderInfo(orderId, nameString,
                        introductString);
                PictureAirLog.v(TAG, "info:" + info);
                // 对订单做RSA 签名
                String sign = AliPayUtil.sign(info);
                PictureAirLog.v(TAG, "sign:" + sign);
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
                        PictureAirLog.v(TAG, "pay info=" + result);
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
                        paymentOrderHandler.sendMessage(msg);
                    }
                }.start();

            } catch (Exception ex) {
                ex.printStackTrace();
                newToast.setTextAndShow(R.string.pay_failed,
                        Common.TOAST_SHORT_TIME);
            }
        } else if (1 == payType) {
            PictureAirLog.v(TAG, "yl");
            customProgressDialog = CustomProgressDialog.show(PaymentOrderActivity.this,
                    getString(R.string.please_wait), false, null);
            API1.getUnionPayTN(paymentOrderHandler);
        } else if (6 == payType) {
            PictureAirLog.v(TAG, "paypal");

        } else if (payType == 7) {
            PictureAirLog.v(TAG, "wechat");
            // 调起微信支付
            // 生成prepay_id
            GetPrepayIdTask getPrepayId = new GetPrepayIdTask();
            getPrepayId.execute();
        } else {
            PictureAirLog.v(TAG, "other");
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
                // 直接调用接口
                pay(orderid);
                break;

            case R.id.zfb:// 选择支付宝支付
                payType = 0;
                zfButton.setImageResource(R.drawable.sele);
                yhkButton.setImageResource(R.drawable.nosele);
                paypalButton.setImageResource(R.drawable.nosele);
                wechatButton.setImageResource(R.drawable.nosele);
                PictureAirLog.v(TAG, "ZFB");
                break;

            case R.id.yl:// 选择银联支付
                payType = 1;
                zfButton.setImageResource(R.drawable.nosele);
                yhkButton.setImageResource(R.drawable.sele);
                paypalButton.setImageResource(R.drawable.nosele);
                wechatButton.setImageResource(R.drawable.nosele);
                PictureAirLog.v(TAG, "YL");
                break;

            case R.id.paypal:// paypal支付
                payType = 6;
                zfButton.setImageResource(R.drawable.nosele);
                yhkButton.setImageResource(R.drawable.nosele);
                paypalButton.setImageResource(R.drawable.sele);
                wechatButton.setImageResource(R.drawable.nosele);
                PictureAirLog.v(TAG, "PAYPAL");
                break;

            case R.id.weixin:// wechat支付
                payType = 7;
                zfButton.setImageResource(R.drawable.nosele);
                yhkButton.setImageResource(R.drawable.nosele);
                paypalButton.setImageResource(R.drawable.nosele);
                wechatButton.setImageResource(R.drawable.sele);
                PictureAirLog.v(TAG, "WECHAT");
                break;

            default:
                break;
        }
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
        AppManager.getInstance().killActivity(SelectPhotoActivity.class);
        AppManager.getInstance().killActivity(PreviewPhotoActivity.class);
        AppManager.getInstance().killActivity(MakegiftActivity.class);
        AppManager.getInstance().killActivity(DetailProductActivity.class);
        finish();
    }

    // 取消操作处理
    private void CancelInPayment() {
        // TODO Auto-generated method stub
        // myApplication.setIsBuyingPhotoInfo(null);
        newToast.setTextAndShow(R.string.cancel_deal, Common.TOAST_SHORT_TIME);
        myApplication.clearIsBuyingPhotoList();
        myApplication.setRefreshViewAfterBuyBlurPhoto("");
        if (isBack!= null && !isBack.isEmpty() && isBack.equals("1")) {
            //返回到上一个界面
            AppManager.getInstance().killActivity(SubmitOrderActivity.class);
        } else {
            //进入订单界面
            AppManager.getInstance().killActivity(SubmitOrderActivity.class);
            AppManager.getInstance().killActivity(PreviewProductActivity.class);
            // AppManager.getInstance().killActivity(BlurActivity.class);
            AppManager.getInstance().killActivity(SelectPhotoActivity.class);
            AppManager.getInstance().killActivity(PreviewPhotoActivity.class);
            AppManager.getInstance().killActivity(MakegiftActivity.class);

            AppManager.getInstance().killActivity(DetailProductActivity.class);
            Intent intent2 = new Intent(PaymentOrderActivity.this, OrderActivity.class);
            startActivity(intent2);
        }
//        newToast.setTextAndShow(R.string.cancel_deal, Common.TOAST_SHORT_TIME);
//        AppManager.getInstance().killActivity(SubmitOrderActivity.class);
//        AppManager.getInstance().killActivity(PreviewProductActivity.class);
//        // AppManager.getInstance().killActivity(BlurActivity.class);
//        AppManager.getInstance().killActivity(SelectPhotoActivity1.class);
//        AppManager.getInstance().killActivity(PreviewPhotoActivity.class);
//        AppManager.getInstance().killActivity(MakegiftActivity.class);
//        AppManager.getInstance().killActivity(DetailProductActivity.class);

        finish();
    }

    // 成功支付之后的操作
    private void SuccessAfterPayment() {
        // TODO Auto-generated method stub
        PictureAirLog.v(TAG, "start finish expired activity");
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

    private final Handler paymentOrderHandler = new PaymentOrderHandler(this);


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
                CancelInPayment();
                break;
            case RQF_SUCCESS:
                PictureAirLog.v(TAG, "RQF_SUCCESS orderid: " + orderid);
                //支付成功后：出现等待弹窗，5秒后进入订单页面。其中接收推送，若没有推送则将订单ID写入数据库，状态为灰色不可点击
                customProgressDialog = CustomProgressDialog.show(PaymentOrderActivity.this, getString(R.string.is_loading), false, null);
                paymentOrderHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        startTimer();
                        getData();
                    }
                });
                break;

            case API1.UNIONPAY_GET_TN_SUCCESS://获取银联TN成功
                if (customProgressDialog.isShowing()) {
                    customProgressDialog.dismiss();
                }
                if (msg.obj == null || ((String) msg.obj).length() == 0) {
                    paymentOrderHandler.sendEmptyMessage(RQF_ERROR);
                } else {
                    UPPayAssistEx.startPayByJAR(PaymentOrderActivity.this, PayActivity.class, null, null, msg.obj.toString(), mMode);
                }
                break;

            case API1.UNIONPAY_GET_TN_FAILED://获取银联TN失败
                if (customProgressDialog.isShowing()) {
                    customProgressDialog.dismiss();
                }
                newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                paymentOrderHandler.sendEmptyMessage(RQF_ERROR);
                break;

            default:
                break;
        }
    }


    /**
     * 获取推送消息，跳转相应界面
     */
    public void startTimer() {
        PictureAirLog.v(TAG, "satrtTimer ");
        if (countDownTimer != null) {
            countDownTimer.start();
        }
    }

    CountDownTimer countDownTimer = new CountDownTimer(5000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {//支付超时或者失败
            PictureAirLog.v(TAG, "onFinish ");
            if (resultJsonObject == null) {
                if (customProgressDialog.isShowing()) {
                    customProgressDialog.dismiss();
                }
                PictureAirLog.v(TAG, "onFinish resultJsonObject == null");
                pictureAirDbManager.insertPaymentOrderIdDB(sPreferences.getString(Common.USERINFO_ID, ""), orderid);

                SuccessAfterPayment();
                finish();
            }
        }
    };


    /**
     * 获取推送消息，跳转相应界面
     */
    public void getData() {
        PictureAirLog.v(TAG, "getData ");
        while (resultJsonObject != null) {
            PictureAirLog.v(TAG, "onTick resultJsonObject: " + resultJsonObject.toString());
            dealData(resultJsonObject);
            break;
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
                myApplication.setPhotoIsPaid(true);
                ArrayList<PhotoInfo> photopassArrayList = new ArrayList<PhotoInfo>();
                photopassArrayList.addAll(myApplication.getIsBuyingPhotoInfoList());
                // 找出购买的info，并且将购买属性改为1
                photopassArrayList.get(myApplication.getIsBuyingIndex()).isPayed = 1;
                PictureAirLog.v("position--->", myApplication.getIsBuyingIndex() + "");
                PictureAirLog.v("photoId---->", photopassArrayList.get(myApplication.getIsBuyingIndex()).photoId);

                intent = new Intent(PaymentOrderActivity.this, PreviewPhotoActivity.class);
                intent.putExtra("activity", "paymentorderactivity");
                intent.putExtra("position", myApplication.getIsBuyingIndex());// 在那个相册中的位置
                intent.putExtra("photoId", photopassArrayList.get(myApplication.getIsBuyingIndex()).photoId);
                intent.putExtra("photos", photopassArrayList);// 那个相册的全部图片路径
                intent.putExtra("targetphotos", myApplication.magicPicList);

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
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.putBoolean(Common.NEED_FRESH, true);
        editor.commit();
        if (customProgressDialog.isShowing()) {
            customProgressDialog.dismiss();
        }
        SuccessAfterPayment();
        startActivity(intent);
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        finish();
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        paymentOrderHandler.removeCallbacksAndMessages(null);
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

        String packageSign = MD5.getMessageDigest(sb.toString().getBytes()).toUpperCase();
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
        String appSign = MD5.getMessageDigest(sb.toString().getBytes()).toUpperCase();
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

    private class GetPrepayIdTask extends AsyncTask<Void, Void, Map<String, String>> {

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
                    Common.WECHAT_NOTIFY));// 通知地址:接收微信支付异步通知回调地址
            packageParams.add(new BasicNameValuePair("out_trade_no",
                    orderid));// 商户订单号:商户系统内部的订单号,32个字符内、可包含字母
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

    @Override
    public void TopViewClick(View view) {
        super.TopViewClick(view);
        switch (view.getId()) {
            case R.id.topLeftView:
                //返回键
                PictureAirLog.v(TAG, "TopViewClick topLeftView");
//                Intent intent2 = new Intent(PaymentOrderActivity.this, OrderActivity.class);
//                startActivity(intent2);
                CancelInPayment();
//                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //返回键
        PictureAirLog.v(TAG, "TopViewClick onBackPressed");
//        Intent intent2 = new Intent(PaymentOrderActivity.this, OrderActivity.class);
//        startActivity(intent2);
        CancelInPayment();
//        finish();
    }
}
