package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.OrderInfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PayUtils;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.widget.MyToast;
import com.unionpay.UPPayAssistEx;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import cn.smssdk.gui.AppManager;
import cn.smssdk.gui.CustomProgressDialog;

public class PaymentOrderActivity extends BaseActivity implements OnClickListener {
    private TextView sbmtButton;
    private String nameString;
    private String priceString;
    private String introductString;

    private View paySv;
    private LinearLayout noPayLl;

    private RelativeLayout zfbLayout;
    private RelativeLayout ylLayout;
    private RelativeLayout paypalLayout;
    private RelativeLayout wechatLayout;

    private ImageView zfButton;
    private ImageView yhkButton;
    private ImageView paypalButton;
    private ImageView wechatButton;

    public static final int RQF_SUCCESS = 1;
    public static final int RQF_CANCEL = 2;
    public static final int RQF_ERROR = 3;
    public static final int INITPAYPAL = 4;
    public static final int RQF_UNSUCCESS = 5;

    private static final String TAG = "PaymentOrderActivity";

    private MyApplication myApplication;
    private SharedPreferences sPreferences;
    private MyToast newToast;
    private PictureAirDbManager pictureAirDbManager;
    public static org.json.JSONObject resultJsonObject;
    private CustomProgressDialog customProgressDialog;

    private int payType;// 支付类型 0 支付宝 1 银联 2 VISA信用卡 3 代付 4 分期 5 自提 6 paypal 7
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
    private final String mMode = "01";

    private boolean isNeedPay = true;//是否需要支付

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
                CancelInPayment(true);
                break;

            case RQF_UNSUCCESS:
                PictureAirLog.v(TAG, "RQF_UNSUCCESS");
                //从模糊照片单张购买、PP+购买 回到之前的预览界面
                CancelInPayment(false);
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
                    UPPayAssistEx.startPay(PaymentOrderActivity.this, null, null, msg.obj.toString(), mMode);
                }
                break;

            case API1.UNIONPAY_GET_TN_FAILED://获取银联TN失败
                if (customProgressDialog.isShowing()) {
                    customProgressDialog.dismiss();
                }
                newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                paymentOrderHandler.sendEmptyMessage(RQF_ERROR);
                break;

            case API1.ADD_ORDER_SUCCESS:
                PictureAirLog.v(TAG, "ADD_ORDER_SUCCESS" + msg.obj);
                JSONObject jsonObject = (JSONObject) msg.obj;
                //确认订单成功，等待接收推送
                paymentOrderHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        startTimer();
                        getData();
                    }
                });

                break;

            case API1.ADD_ORDER_FAILED:
                PictureAirLog.e(TAG, "ADD_ORDER_FAILED cade: " + msg.arg1);
                customProgressDialog.dismiss();
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
            isNeedPay = getIntent().getBooleanExtra("isNeedPay", true);
            nameString = getIntent().getStringExtra("name");// 获取name
            priceString = getIntent().getStringExtra("price");// 获取price
            introductString = getIntent().getStringExtra("introduce");// 获取介绍信息
            orderid = getIntent().getStringExtra("orderId");
            outletId = getIntent().getStringExtra("outletId");
            cartItemIds = JSONArray.parseArray(getIntent().getStringExtra("cartItemIds"));

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

        //判断是否需要支付
        if (isNeedPay) {
            paySv.setVisibility(View.VISIBLE);
            noPayLl.setVisibility(View.GONE);
        } else {
            paySv.setVisibility(View.GONE);
            noPayLl.setVisibility(View.VISIBLE);
        }

    }

    /**
     * 提交订单（无需付钱的情况）
     */
    public void checkOut() {
        customProgressDialog = CustomProgressDialog.show(this, getString(R.string.is_loading), false, null);
        if (cartItemIds != null) {
            if (productType == 1) {
                //获取收货地址
                API1.addOrder(cartItemIds, 1, outletId, "", paymentOrderHandler);
            } else {
                //PP+/数码商品不需要地址
                API1.addOrder(cartItemIds, 3, "", "", paymentOrderHandler);
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

    /**
     * 支付函数
     *
     * @param orderId 订单号
     */
    private void pay(String orderId) {
        // TODO Auto-generated method stub
        payUtils = new PayUtils(this, paymentOrderHandler, orderId, nameString, introductString, priceString);
        if (0 == payType) {// 支付宝支付方式
            try {
                payUtils.aliPay();

            } catch (Exception ex) {
                ex.printStackTrace();
                newToast.setTextAndShow(R.string.pay_failed, Common.TOAST_SHORT_TIME);
            }
        } else if (1 == payType) {
            PictureAirLog.v(TAG, "yl");
            customProgressDialog = CustomProgressDialog.show(PaymentOrderActivity.this, getString(R.string.is_loading), false, null);
            API1.getUnionPayTN(paymentOrderHandler);
        } else if (6 == payType) {
            PictureAirLog.v(TAG, "paypal");

        } else if (payType == 7) {
            weChatIsPaying = true;
            PictureAirLog.v(TAG, "wechat");
            try {
                // 调起微信支付
                payUtils.wxPay();
            } catch (Exception e) {
                e.printStackTrace();
                newToast.setTextAndShow(R.string.pay_failed, Common.TOAST_SHORT_TIME);
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
    private void CancelInPayment(boolean isCancel) {
        // TODO Auto-generated method stub
        // myApplication.setIsBuyingPhotoInfo(null);
        newToast.setTextAndShow(isCancel ? R.string.cancel_deal : R.string.pay_unsuccesss, Common.TOAST_SHORT_TIME);
        myApplication.clearIsBuyingPhotoList();
        myApplication.setRefreshViewAfterBuyBlurPhoto("");
        if (isBack != null && !isBack.isEmpty() && isBack.equals("1")) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 10) {
            //银联返回值处理
            payUtils.unDealResult();
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
            if (customProgressDialog.isShowing()) {
                customProgressDialog.dismiss();
            }
            if (resultJsonObject == null) {
                PictureAirLog.v(TAG, "onFinish resultJsonObject == null");
                pictureAirDbManager.insertPaymentOrderIdDB(sPreferences.getString(Common.USERINFO_ID, ""), orderid);
                SuccessAfterPayment();
                finish();
            } else {
                PictureAirLog.v(TAG, "onFinish productType： " + productType);
                dealData(resultJsonObject);
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
            int resultCode = sPreferences.getInt(Common.WECHAT_PAY_STATUS, -3);
            //处理微信返回值，
            payUtils.wxDealResult(resultCode);
            weChatIsPaying = false;
            SharedPreferences.Editor editor = sPreferences.edit();
            editor.putInt(Common.WECHAT_PAY_STATUS, -3);
            editor.commit();
        }
    }

    @Override
    public void TopViewClick(View view) {
        super.TopViewClick(view);
        switch (view.getId()) {
            case R.id.topLeftView:
                //返回键
                PictureAirLog.v(TAG, "TopViewClick topLeftView");
                CancelInPayment(true);
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
        CancelInPayment(true);
    }
}
