package com.pictureair.hkdlphotopass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.JavascriptInterface;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.hkdlphotopass.MyApplication;
import com.pictureair.hkdlphotopass.R;
import com.pictureworks.android.util.AppUtil;
import com.pictureworks.android.util.Common;
import com.pictureworks.android.widget.CustomProgressDialog;
import com.pictureworks.android.widget.CustomWebView;
import com.pictureworks.android.widget.MyToast;
import com.pictureworks.android.widget.NoNetWorkOrNoCountView;
import com.pictureair.jni.ciphermanager.PWJniUtil;

import java.lang.ref.WeakReference;

public class WebViewActivity extends BaseActivity implements CustomWebView.MyWebviewImp {
    private CustomWebView webView;
    private NoNetWorkOrNoCountView netWorkOrNoCountView;
    private MyToast myToast;
    private CustomProgressDialog customProgressDialog;


    private final Handler myHandler = new MyHandler(this);


    private static class MyHandler extends Handler {
        private final WeakReference<WebViewActivity> mActivity;

        public MyHandler(WebViewActivity activity) {
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

    private void dealHandler(Message msg) {
        switch (msg.what) {
            case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_RELOAD://noView的按钮响应重新加载点击事件
                if (AppUtil.getNetWorkType(MyApplication.getInstance()) == 0) {
                    myToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
                    break;
                }
                showDialog();
                netWorkOrNoCountView.setVisibility(View.GONE);
                getData();
                break;

            default:
                break;
        }
    }

    int key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        setTopLeftValueAndShow(R.drawable.back_white, true);
        key = getIntent().getIntExtra("key", 1);
        webView = (CustomWebView) findViewById(R.id.webView);
        netWorkOrNoCountView = (NoNetWorkOrNoCountView) findViewById(R.id.nonetwork_view);
        myToast = new MyToast(this);
        webView.setMyWebViewImp(this);
        getData();
    }

    private void getData() {
        webView.setVisibility(View.GONE);
        if (key == 1) {
//            webView.start(Common.POLICY_AGREEMENT + "&lang=" + MyApplication.getInstance().getLanguageType());
            webView.start(String.format(Common.POLICY_AGREEMENT, (MyApplication.getInstance().getLanguageType().equals("en") ? "en/" : "")));
            setTopTitleShow(R.string.policy); // 设置标题
        } else if (key == 2) {
            webView.start(Common.TERMS_AGREEMENT + "&lang=" + MyApplication.getInstance().getLanguageType());
            setTopTitleShow(R.string.terms); // 设置标题
        } else if (key == 3) {
//            webView.start(Common.CONTACT_AGREEMENT + "&lang=" + MyApplication.getInstance().getLanguageType());
            webView.start(String.format(Common.CONTACT_AGREEMENT, MyApplication.getTokenId(), MyApplication.getInstance().getLanguageType()));
            setTopTitleShow(R.string.mypage_opinions); // 设置标题
        } else if (key == 4) {  //海外支付的链接。
            String orderId = getIntent().getStringExtra("orderId");
            String currency = "CNY";
            String appID = AppUtil.md5(PWJniUtil.getAPPKey(Common.APP_TYPE_SHDRPP) + PWJniUtil.getAppSecret(Common.APP_TYPE_SHDRPP));
            String language = MyApplication.getInstance().getLanguageType();
            if (MyApplication.getInstance().getLanguageType().equals("zh")) {
                language = "cn";
            }
            webView.start(Common.BASE_URL_TEST + Common.IPAY_LINK + "?tokenId=" + MyApplication.getTokenId() + "&orderId=" + orderId + "&language=" + language + "&currency=" + currency + "&appID=" + appID);
            setTopTitleShow(R.string.paypalzf); // 设置标题
            webView.addJavascriptInterface(this, "someThing");
        }
    }

    @Override
    public void TopViewClick(View view) {
        super.TopViewClick(view);
        switch (view.getId()) {
            case R.id.topLeftView:
                if (key == 4) {
                    sendActivityResult(-1);
                }
                finish();
                break;
            default:
                break;
        }
    }

    @JavascriptInterface
    public void setSmething(String some) {
        JSONObject obj = (JSONObject) JSONArray.parse(some);
        if (obj.containsKey("status")) {
            int code = obj.getIntValue("status");
            if (code == 200) {
                sendActivityResult(0); // 支付成功
            } else {
                sendActivityResult(-2);
            }
        }
        this.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //返回键
        if (key == 4) {
            sendActivityResult(-1);
        }
        finish();
    }

    /**
     * 发送广播。 0: 支付成功 ， -1: 支付取消 ， -2: 支付失败
     *
     * @param payType
     */
    private void sendActivityResult(int payType) {
//        Intent intent = new Intent("com.payment.action");
//        intent.putExtra("payType", payType);
//        sendBroadcast(intent);

        Intent intent = new Intent(WebViewActivity.this, PaymentOrderActivity.class);
        intent.putExtra("payType", payType);
        setResult(111,intent);

    }

    @Override
    public void webViewFailedToLoad() {
        webView.setVisibility(View.GONE);
        netWorkOrNoCountView.setVisibility(View.VISIBLE);
        netWorkOrNoCountView.setResult(R.string.no_network, R.string.click_button_reload, R.string.reload, R.drawable.no_network, myHandler, true);
    }

    @Override
    public void loading() {
        webView.setVisibility(View.GONE);
        showDialog();
    }

    @Override
    public void loadFinish() {
        webView.setVisibility(View.VISIBLE);
        goneDialog();
    }

    /**
     * GONE DIALOG
     */
    private void goneDialog(){
        if (null != customProgressDialog && customProgressDialog.isShowing()) {
            customProgressDialog.dismiss();
        }
    }

    /**
     * SHOW DIALOG
     */
    private void showDialog(){
        if (null == customProgressDialog) {
            customProgressDialog = CustomProgressDialog.show(WebViewActivity.this, getString(R.string.is_loading), false, null);
        }
        if (!customProgressDialog.isShowing()){
            customProgressDialog.show();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myHandler.removeCallbacksAndMessages(null);
    }
}
