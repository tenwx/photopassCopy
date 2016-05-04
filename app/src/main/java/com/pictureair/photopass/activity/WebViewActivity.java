package com.pictureair.photopass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.jni.keygenerator.PWJniUtil;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.widget.CustomWebView;

public class WebViewActivity extends BaseActivity {
    CustomWebView webView;
    int key;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        setTopLeftValueAndShow(R.drawable.back_white, true);
        key = getIntent().getIntExtra("key", 1);
        webView = (CustomWebView) findViewById(R.id.webView);
        if (key == 1){
//            webView.start(Common.POLICY_AGREEMENT + "&lang=" + MyApplication.getInstance().getLanguageType());
            webView.start(String.format(Common.POLICY_AGREEMENT, (MyApplication.getInstance().getLanguageType().equals("en") ? "en/" : "")));
            setTopTitleShow(R.string.policy); // 设置标题
        }else if(key == 2){
            webView.start(Common.TERMS_AGREEMENT + "&lang=" + MyApplication.getInstance().getLanguageType());
            setTopTitleShow(R.string.terms); // 设置标题
        }else if (key == 3){
//            webView.start(Common.CONTACT_AGREEMENT + "&lang=" + MyApplication.getInstance().getLanguageType());
            webView.start(String.format(Common.CONTACT_AGREEMENT, MyApplication.getTokenId(), MyApplication.getInstance().getLanguageType()));
            setTopTitleShow(R.string.mypage_opinions); // 设置标题
        }else if(key == 4){  //海外支付的链接。
            String orderId = getIntent().getStringExtra("orderId");
            String currency = "CNY";
            String appID = AppUtil.md5(PWJniUtil.getAPPKey(Common.APP_TYPE_SHDRPP) + PWJniUtil.getAppSecret(Common.APP_TYPE_SHDRPP));
            String language = MyApplication.getInstance().getLanguageType();
            if (MyApplication.getInstance().getLanguageType().equals("zh")){
                language = "cn";
            }
            webView.start(Common.BASE_URL_TEST + Common.IPAY_LINK + "?tokenId="+MyApplication.getTokenId()+"&orderId="+orderId+"&language=" + language +"&currency="+currency + "&appID="+appID);
            setTopTitleShow(R.string.paypalzf); // 设置标题
            webView.addJavascriptInterface(this, "someThing");
        }
    }

    @Override
    public void TopViewClick(View view) {
        super.TopViewClick(view);
        switch (view.getId()) {
            case R.id.topLeftView:
                if (key == 4){
                    sendBroadcast(-1);
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
        if (obj.containsKey("status")){
            int code = obj.getIntValue("status");
            if (code == 200){
                sendBroadcast(0); // 支付成功
            }else{
                sendBroadcast(-2);
            }
        }
        this.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //返回键
        if (key == 4) {
            sendBroadcast(-1);
        }
        finish();
    }

    /**
     * 发送广播。 0: 支付成功 ， -1: 支付取消 ， -2: 支付失败
     * @param payType
     */
    private void sendBroadcast(int payType){
        Intent intent = new Intent("com.payment.action");
        intent.putExtra("payType",payType);
        sendBroadcast(intent);
    }

}
