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

import com.pictureair.photopass.BuildConfig;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.Common;


public class AboutActivity extends BaseActivity {
    private TextView logo_text;
    private TextView tv_versionCode;
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
        logo_text = (TextView) findViewById(R.id.logo_text);
        tv_versionCode = (TextView) findViewById(R.id.versionCode);
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
        tv_versionCode.setVisibility(View.GONE);
//        tv_versionCode.setText("V" + versionName);
        if (BuildConfig.LOG_DEBUG) {//研发版本
            developVersion = getString(R.string.develop_version) + Common.VERSION_CODE;
        } else {//内测版本
            developVersion = getString(R.string.testing_version) + Common.VERSION_CODE;
        }
        developTextView.setText(developVersion);
//		logo_text.setText((Html.fromHtml("我"+ "<font color='#ffa300'><big>"+ "(迪)"+ "</big></font>" + "故事里有您更精"+ "<font color='#ffa300'><big>"+ "(彩)"+ "</big></font>")));
        logo_text.setText(R.string.about_tips);
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
