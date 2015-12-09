package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.Common;

import java.util.Locale;

public class SettingLanguageActivity extends BaseActivity implements OnClickListener {
    private Configuration config;
    private DisplayMetrics dm;

    private RelativeLayout back;
    private RelativeLayout languageChinese;
    private RelativeLayout languageEnglish;
    private RelativeLayout save;

    private ImageView chineseSeleted;
    private ImageView englishSeleted;

    String languageType = Common.SIMPLE_CHINESE;   // 0表示英语，1表示简体中文。

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_language);
        AppManager.getInstance().addActivity(this);
        initView();
    }

    private void initView() {
        // 修改语言需要的配置信息
        config = getResources().getConfiguration();
        dm = getResources().getDisplayMetrics();

        back = (RelativeLayout) findViewById(R.id.back_set);
        languageChinese = (RelativeLayout) findViewById(R.id.language_chinese);
        languageEnglish = (RelativeLayout) findViewById(R.id.language_english);
        save = (RelativeLayout) findViewById(R.id.save);

        chineseSeleted = (ImageView) findViewById(R.id.chinese_imageView);
        englishSeleted = (ImageView) findViewById(R.id.english_imageView);

        if (config.locale.equals(Locale.SIMPLIFIED_CHINESE)) {
            chineseSeleted.setVisibility(View.VISIBLE);
            englishSeleted.setVisibility(View.INVISIBLE);
        } else if (config.locale.equals(Locale.US)) {
            englishSeleted.setVisibility(View.VISIBLE);
            chineseSeleted.setVisibility(View.INVISIBLE);
        }

        save.setOnClickListener(this);
        back.setOnClickListener(this);
        languageChinese.setOnClickListener(this);
        languageEnglish.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.back_set:
                finish();
                break;
            case R.id.save:
                MainTabActivity.instances.finish();

                //保存全局变量，和服务器同步。主要用于商品 列表的中英文切换
                if (languageType.equals(Common.SIMPLE_CHINESE)) {
                    MyApplication.getInstance().setLanguageType(Common.SIMPLE_CHINESE);
                } else if (languageType.equals(Common.ENGLISH)) {
                    MyApplication.getInstance().setLanguageType(Common.ENGLISH);
                }
                //保存 设置好的语言类型。
                getResources().updateConfiguration(config, dm);

                //把语言写入数据库
                SharedPreferences settingLanguage = this.getSharedPreferences(
                        Common.APP, MODE_PRIVATE);
                Editor localEditor = settingLanguage.edit();
                localEditor.putString(Common.LANGUAGE_TYPE, languageType);
                localEditor.apply();

                AppManager.getInstance().killAllActivity();
                startActivity(new Intent(this, SettingActivity.class));

                break;
            case R.id.language_chinese:
                languageType = Common.SIMPLE_CHINESE;
                config.locale = Locale.SIMPLIFIED_CHINESE;
                chineseSeleted.setVisibility(View.VISIBLE);
                englishSeleted.setVisibility(View.INVISIBLE);
                break;
            case R.id.language_english:
                languageType = Common.ENGLISH;
                config.locale = Locale.US;
                englishSeleted.setVisibility(View.VISIBLE);
                chineseSeleted.setVisibility(View.INVISIBLE);
                break;


            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        AppManager.getInstance().killActivity(this);
    }
}
