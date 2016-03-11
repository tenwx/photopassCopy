package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.util.Common;


public class AboutActivity extends BaseActivity {
    private TextView logo_text;
    private TextView tv_versionCode;
    private TextView developTextView;
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
        getVersionCode();
        tv_versionCode.setVisibility(View.GONE);
//        tv_versionCode.setText("V" + versionName);
        if (Common.DEBUG) {//研发版本
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


}
