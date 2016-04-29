package com.pictureair.photopass.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.Common;

public class WebViewActivity extends BaseActivity {
    ProgressBar pb;
    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        setTopLeftValueAndShow(R.drawable.back_white, true);

        int key = getIntent().getIntExtra("key", 1);
        pb = (ProgressBar) findViewById(R.id.pb);
        pb.setMax(100);

        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.setWebChromeClient(new MyWebChromeClient());  // 所有页面在webView中显示
        webView.setWebViewClient(new MyWebViewClient()); // 监听 进度条 事件。
        initWebSettings();// 初始化webViewsetting
        if (key == 1){
            webView.loadUrl(Common.POLICY_AGREEMENT + "&lang=" + MyApplication.getInstance().getLanguageType());
            setTopTitleShow(R.string.policy); // 设置标题
        }else if(key == 2){
            webView.loadUrl(Common.TERMS_AGREEMENT + "&lang=" + MyApplication.getInstance().getLanguageType());
            setTopTitleShow(R.string.terms); // 设置标题
        }else if (key == 3){
            webView.loadUrl(String.format(Common.CONTACT_AGREEMENT, MyApplication.getTokenId(), MyApplication.getInstance().getLanguageType()));
            setTopTitleShow(R.string.mypage_opinions); // 设置标题
        }
    }

    //  监听 进度条事件。
    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            pb.setProgress(newProgress);
            if (newProgress == 100) {
                pb.setVisibility(View.GONE);
            }
            super.onProgressChanged(view, newProgress);
        }
    }


    /**
     * 初始化webViewSetting
     */
    public void initWebSettings() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setAllowFileAccess(true);// 设置可以访问网络
        webSettings.setBuiltInZoomControls(false);// 设置不支持缩放
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); // 设置

        webSettings.setSaveFormData(false);
        webSettings.setSavePassword(false);// 设置不保存密码
        webSettings.setJavaScriptEnabled(true); //
        // 开启 Application Caches 功能
        webSettings.setAppCacheEnabled(true);
        // 设置 Application Caches 缓存目录
        // webSettings.setAppCachePath(cacheDirPath);
        // 开启 DOM storage API 功能
        webSettings.setDomStorageEnabled(true);
        // 开启 database storage API 功能
        webSettings.setDatabaseEnabled(true);
        // 设置数据库缓存路径
        // webSettings.setDatabasePath(cacheDirPath);
        webSettings.setPluginState(WebSettings.PluginState.ON);

    }

    /**
     * 在WebView中而不是默认浏览器中显示页面
     */
    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView webview, String url) {
            webview.loadUrl(url);// 使用当前WebView处理跳转
            return true;// true表示此事件在此处被处理，不需要再广播
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            // 页面跳转结束后被回调
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            // 出错
            //dismissPD();
        }
    }

    @Override
    public void TopViewClick(View view) {
        super.TopViewClick(view);
        switch (view.getId()) {
            case R.id.topLeftView:
                finish();
                break;
            default:
                break;
        }
    }

}
