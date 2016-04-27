package com.pictureair.photopass.widget;

import android.content.Context;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by bass on 16/4/22.
 */
public class CustomWebView extends WebView {
    private MyWebviewImp myWebviewImp;//webview的接口

    private boolean isDefaultWeb = true;//默认使用webview打开网页，false为用第三方浏览器打开网页
    private boolean isSupportJavaScript = true;//是否支持javascript
    private boolean isShowHorizontalScrollBarEnabled = false;//水平滚动条隐藏
    private boolean isShowVerticalScrollBarEnabled = false;//竖直滚动条隐藏
    private boolean isCache = false;//默认不使用缓存

    public CustomWebView(Context context) {
        super(context);
    }

    public CustomWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void start(String url) {
        this.setHorizontalScrollBarEnabled(isShowHorizontalScrollBarEnabled);
        this.setVerticalScrollBarEnabled(isShowVerticalScrollBarEnabled);

        WebSettings settings = this.getSettings();
        settings.setJavaScriptEnabled(isSupportJavaScript);
        settings.setSavePassword(false);
        if (isCache){//使用缓存
            settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }else{//不使用缓存
            settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        }
        setWebChromeClient();
        setWebViewClient();
        loadUrl(url);
    }

    /**
     * 是否使用缓存
     */
    public CustomWebView setCache(boolean isCache) {
        this.isCache = isCache;
        return this;
    }

    /**
     * 设置是否用webview启动网页，默认true
     * false为用第三方浏览器启动
     *
     * @param isDefaultWeb
     * @return
     */
    public CustomWebView setDefaultWeb(boolean isDefaultWeb) {
        this.isDefaultWeb = isDefaultWeb;
        return this;
    }

    /**
     * 设置是否使用javascriot
     */
    public CustomWebView setSupportJavaScript(boolean isSupportJavaScript) {
        this.isSupportJavaScript = isSupportJavaScript;
        return this;
    }

    /**
     * 是否显示滚动条
     */
    public CustomWebView setShowScrollBarEnabled(boolean isShow){
        this.isShowHorizontalScrollBarEnabled = isShow;
        this.isShowVerticalScrollBarEnabled = isShow;
        return this;
    }

    private void setWebViewClient() {
        this.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return isDefaultWeb;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                myWebviewImp.webViewFailedToLoad();
            }
        });
    }

    private void setWebChromeClient(){
        this.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    // 网页加载完成
                    myWebviewImp.loadFinish();
                } else {
                    // 加载中
                    myWebviewImp.loading();
                }
            }

        });
    }

    /**
     * 对外接口
     * @param myWebviewImp
     * @return
     */
    public CustomWebView setMyWebViewImp(MyWebviewImp myWebviewImp){
        this.myWebviewImp = myWebviewImp;
        return this;
    }

    public interface MyWebviewImp{
        void webViewFailedToLoad();//加载失败
        void loading();//加载中
        void loadFinish();//加载完成
    }
}
