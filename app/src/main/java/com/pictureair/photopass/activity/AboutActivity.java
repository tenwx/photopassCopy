package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;


public class AboutActivity extends BaseActivity {
    private TextView logo_text;
    private TextView developTextView;
    private TextView tvTermsPolicy;
    private String versionCode;
    private String versionName;
    private String developVersion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setTopLeftValueAndShow(R.drawable.back_white, true);
        setTopTitleShow(R.string.mypage_about);
        logo_text = (TextView) findViewById(R.id.logo_text1);
        developTextView = (TextView) findViewById(R.id.develop_version_tv);
        tvTermsPolicy = (TextView) findViewById(R.id.tv_terms_policy);
        tvTermsPolicy.setMovementMethod(LinkMovementMethod.getInstance());
        CharSequence text = tvTermsPolicy.getText();
        if (text instanceof Spannable) {
            int end = text.length();
            Spannable sp = (Spannable) tvTermsPolicy.getText();
            URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
            SpannableStringBuilder style = new SpannableStringBuilder(text);
            style.clearSpans();// should clear old spans
            for (URLSpan url : urls) {
                MyURLSpan myURLSpan = new MyURLSpan(url.getURL());
                style.setSpan(myURLSpan, sp.getSpanStart(url), sp.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }
            tvTermsPolicy.setText(style);
        }

        getVersionCode();
        logo_text.setTypeface(MyApplication.getInstance().getFontBold());
        developVersion = "V" + versionName;
        developTextView.setText(developVersion);
    }

    /**
     * 获取应用的版本号
     */
    private void getVersionCode() {
        // TODO Auto-generated method stub
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            versionCode = info.versionCode + "";
            versionName = info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
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

    private class MyURLSpan extends ClickableSpan {
        private String mUrl;
        MyURLSpan(String url) {
            mUrl = url;
        }

        @Override
        public void onClick(View widget) {
            Intent intent=new Intent();
            intent.putExtra("key", Integer.valueOf(mUrl));
            intent.setClass(AboutActivity.this, WebViewActivity.class);
            startActivity(intent);
        }
    }

}
