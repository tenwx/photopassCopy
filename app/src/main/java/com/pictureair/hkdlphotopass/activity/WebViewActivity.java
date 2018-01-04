package com.pictureair.hkdlphotopass.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.hkdlphotopass.util.PictureAirLog;
import com.pictureair.jni.ciphermanager.PWJniUtil;
import com.pictureair.hkdlphotopass.MyApplication;
import com.pictureair.hkdlphotopass.R;
import com.pictureair.hkdlphotopass.util.AppUtil;
import com.pictureair.hkdlphotopass.util.Common;
import com.pictureair.hkdlphotopass.widget.CustomWebView;
import com.pictureair.hkdlphotopass.widget.NoNetWorkOrNoCountView;
import com.pictureair.hkdlphotopass.widget.PWToast;

import java.lang.ref.WeakReference;

/**
 * 网页
 */
public class WebViewActivity extends BaseActivity implements CustomWebView.MyWebviewImp {
    private CustomWebView webView;
    private NoNetWorkOrNoCountView netWorkOrNoCountView;
    private PWToast myToast;
    private int key;

    private final Handler myHandler = new MyHandler(this);
    private String orderId;
    private String ccPayType;
    private String language;


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
                showPWProgressDialog(true);
                netWorkOrNoCountView.setVisibility(View.GONE);
                getData();
                break;

            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        setTopLeftValueAndShow(R.drawable.back_blue, true);
        key = getIntent().getIntExtra("key", 1);
        webView = (CustomWebView) findViewById(R.id.webView);
        netWorkOrNoCountView = (NoNetWorkOrNoCountView) findViewById(R.id.nonetwork_view);
        myToast = new PWToast(this);
        webView.setMyWebViewImp(this);
        getData();
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
//                super.onReceivedSslError(view, handler, error);
                final AlertDialog.Builder builder = new AlertDialog.Builder(WebViewActivity.this);
                builder.setTitle(R.string.web_ssl_error_tips);
                builder.setMessage(" ");
                builder.setPositiveButton(R.string.cancel1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.cancel();
                    }
                });
                builder.setNegativeButton(R.string.web_tips_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.proceed();
                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }
        });
    }

    private void getData() {
        webView.setVisibility(View.GONE);
            language = MyApplication.getInstance().getLanguageType();

        if (key == 1) {
            webView.start(AppUtil.getPolicyUrl(language));
            setTopTitleShow(R.string.policy); // 设置标题
        } else if (key == 2) {
            webView.start(Common.TERMS_AGREEMENT + "&lang=" + AppUtil.getLanguageY(language));
            setTopTitleShow(R.string.terms); // 设置标题
        } else if (key == 3) {
            String we = String.format(Common.CONTACT_AGREEMENT, MyApplication.getTokenId(), AppUtil.getLanguageY(language));
            PictureAirLog.i("we", we);
            webView.start(String.format(Common.CONTACT_AGREEMENT, MyApplication.getTokenId(), AppUtil.getLanguageY(language)));
            setTopTitleShow(R.string.mypage_opinions); // 设置标题
        } else if (key == 4) {  //海外支付的链接。
            String orderId = getIntent().getStringExtra("orderId");
            String currency = "HKD";
            String appID = AppUtil.md5(PWJniUtil.getAPPKey(Common.APP_TYPE_SHDRPP) + PWJniUtil.getAppSecret(Common.APP_TYPE_SHDRPP));
            String language = MyApplication.getInstance().getLanguageType();
            if (language.equals(Common.SIMPLE_CHINESE)) {
                language = "cn";
            }
            webView.start(Common.BASE_URL_TEST + Common.IPAY_LINK + "?tokenId=" + MyApplication.getTokenId() + "&orderId=" + orderId + "&language=" + language + "&currency=" + currency + "&appID=" + appID);
            setTopTitleShow(R.string.paypalzf); // 设置标题
            webView.addJavascriptInterface(this, "someThing");
        } else if (key == 5) {//使用条款
            webView.start(Common.TERMS_OF_USE + (MyApplication.getInstance().getLanguageType().equals("en") ? "english/" : "chinese-simplified/"));
            setTopTitleShow(R.string.terms_of_use); // 设置标题
        } else if (key == 6) {//常见问题
            webView.start(Common.FAQ + AppUtil.getLanguageY(language));
            setTopTitleShow(R.string.mypage_help); // 设置标题
        } else if (key == 7) {  //paydollar的链接。
            orderId = getIntent().getStringExtra("orderId");
            ccPayType = getIntent().getStringExtra("ccPayType");
            PictureAirLog.out("ccPayType" + ccPayType);
            PictureAirLog.out("======  orderId =====" + orderId);
            int payType = 2;
            webView.start(Common.BASE_URL_TEST + Common.PAY_DOLLAR + "?tokenId=" + MyApplication.getTokenId() + "&orderId=" + orderId + "&language=" + language + "&payType=" + payType + "&ccPayType=" + ccPayType);
            PictureAirLog.out("url =====" + Common.BASE_URL_TEST + Common.PAY_DOLLAR + "?tokenId=" + MyApplication.getTokenId() + "&orderId=" + orderId + "&language=" + language + "&payType=" + payType + "&ccPayType=" + ccPayType);
            setTopTitleShow(R.string.paydollar); // 设置标题
            webView.addJavascriptInterface(this, "someThing");
        }
    }

    @Override
    public void TopViewClick(View view) {
        super.TopViewClick(view);
        switch (view.getId()) {
            case R.id.topLeftView:
                if (key == 7) {
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
        PictureAirLog.json("paydollar", obj.toJSONString());
        if (obj.containsKey("status")) {
            int code = obj.getIntValue("status");
//            if (code == 200) {
//                sendActivityResult(0); // 支付成功
//            } else {
//                sendActivityResult(-2);
//            }
            sendActivityResult(code);
        }
        this.finish();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        //返回键
        if (key == 7) {
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
        showPWProgressDialog(true);
    }

    @Override
    public void loadFinish() {
        webView.setVisibility(View.VISIBLE);
        dismissPWProgressDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myHandler.removeCallbacksAndMessages(null);
    }
}
