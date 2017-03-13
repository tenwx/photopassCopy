package cn.udesk.activity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import cn.udesk.R;
import cn.udesk.config.UdekConfigUtil;
import cn.udesk.config.UdeskConfig;
import cn.udesk.widget.KeyBoardUtil;
import cn.udesk.widget.UdeskTitleBar;

public class UdeskHelperArticleActivity extends Activity {

    private UdeskTitleBar mTitlebar;
    private View udeskLoading;
    private WebView udeskWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.udesk_articleactivity_view);
        settingTitlebar();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        udeskLoading = findViewById(R.id.udesk_loading);
        udeskWebView = (WebView) findViewById(R.id.udesk_help_content_webview);
        String language = getIntent().getStringExtra("language");
        getArticlesContentJsonApiById(language);

    }

    /**
     * titlebar 的设置
     */
    private void settingTitlebar() {
        mTitlebar = (UdeskTitleBar) findViewById(R.id.udesktitlebar);
        if (mTitlebar != null) {
            UdekConfigUtil.setUITextColor(UdeskConfig.udeskTitlebarTextLeftRightResId, mTitlebar.getLeftTextView(), mTitlebar.getRightTextView());
            UdekConfigUtil.setUIbgDrawable(UdeskConfig.udeskTitlebarBgResId, mTitlebar.getRootView());
            if (UdeskConfig.DEFAULT != UdeskConfig.udeskbackArrowIconResId) {
                mTitlebar.getUdeskBackImg().setImageResource(UdeskConfig.udeskbackArrowIconResId);
            }
            mTitlebar.setLeftTextSequence(getString(R.string.udesk_navi_helper_title_main));
            mTitlebar.setLeftLinearVis(View.VISIBLE);
            mTitlebar.setLeftViewClick(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    // 通过文章的ID，获取文章的具体内容
    private void getArticlesContentJsonApiById(String language) {
        udeskLoading.setVisibility(View.VISIBLE);

        udeskWebView.setHorizontalScrollBarEnabled(false);
        udeskWebView.setVerticalScrollBarEnabled(false);

        WebSettings settings = udeskWebView.getSettings();

        settings.setSupportZoom(true);

        settings.setJavaScriptEnabled(true);
        settings.setSavePassword(false);

        settings.setAllowFileAccess(true);// 设置可以访问网络
        settings.setBuiltInZoomControls(false);// 设置不支持缩放
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); // 设置

        settings.setSaveFormData(false);
        // 开启 Application Caches 功能
        settings.setAppCacheEnabled(true);
        // 设置 Application Caches 缓存目录
        // webSettings.setAppCachePath(cacheDirPath);
        // 开启 DOM storage API 功能
        settings.setDomStorageEnabled(true);
        // 开启 database storage API 功能
        settings.setDatabaseEnabled(true);
        // 设置数据库缓存路径
        // webSettings.setDatabasePath(cacheDirPath);
        settings.setPluginState(WebSettings.PluginState.ON);

        //不使用缓存
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        udeskWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                udeskLoading.setVisibility(View.GONE);
            }
        });

        udeskWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    // 网页加载完成
                    udeskLoading.setVisibility(View.GONE);
                } else {
                    // 加载中
                    udeskLoading.setVisibility(View.VISIBLE);
                }
            }

        });
//        System.out.println("load url ----> " + UdeskConfig.BaseUrl + language);
        udeskWebView.loadUrl(UdeskConfig.BaseUrl + language);//en
    }

    @Override
    protected void onDestroy() {
        KeyBoardUtil.fixFocusedViewLeak(this);
        super.onDestroy();
    }
}
