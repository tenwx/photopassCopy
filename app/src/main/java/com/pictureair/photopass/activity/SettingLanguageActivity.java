package com.pictureair.photopass.activity;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;

import java.util.Locale;

public class SettingLanguageActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = "SettingLanguageActivity";
    private Configuration config;
    private DisplayMetrics dm;

    private RelativeLayout back;
    private RelativeLayout languageChinese;
    private RelativeLayout languageEnglish;
    private RelativeLayout save;
    private TextView saveTv;

    private ImageView chineseSeleted;
    private ImageView englishSeleted;

    private String oldLanguage = "";
    private String currentLanguage = "";   // en表示英语，zh表示简体中文。
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PictureAirLog.v(TAG, "onCreate");
        setContentView(R.layout.activity_setting_language);
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
        saveTv = (TextView) findViewById(R.id.save_tv);

        chineseSeleted = (ImageView) findViewById(R.id.chinese_imageView);
        englishSeleted = (ImageView) findViewById(R.id.english_imageView);

        save.setOnClickListener(this);
        back.setOnClickListener(this);
        languageChinese.setOnClickListener(this);
        languageEnglish.setOnClickListener(this);

        sharedPreferences = getSharedPreferences(Common.APP, MODE_PRIVATE);
        oldLanguage = sharedPreferences.getString(Common.LANGUAGE_TYPE, Common.ENGLISH);
        currentLanguage = oldLanguage;
        if (currentLanguage.equals("")) {
            //获取本地数据
            if (config.locale.equals(Locale.SIMPLIFIED_CHINESE)) {
                currentLanguage = Common.SIMPLE_CHINESE;
            } else if (config.locale.equals(Locale.US)) {
                currentLanguage = Common.ENGLISH;
            }

            oldLanguage = currentLanguage;//使用系统默认语言
        }
        updateUI(currentLanguage);

    }

    /**
     * 更新选中
     *
     * @param cur 新语言
     */
    public void updateUI(String cur) {
        if (cur.equals(Common.SIMPLE_CHINESE)) {
            chineseSeleted.setVisibility(View.VISIBLE);
            englishSeleted.setVisibility(View.INVISIBLE);
        } else if (cur.equals(Common.ENGLISH)) {
            englishSeleted.setVisibility(View.VISIBLE);
            chineseSeleted.setVisibility(View.INVISIBLE);
        }
        if (oldLanguage.equals(cur)) {
            //确认按钮不可点击
            save.setClickable(false);
            saveTv.setTextColor(getResources().getColor(R.color.pp_gray));
        } else {
            save.setClickable(true);
            saveTv.setTextColor(getResources().getColor(R.color.white));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.back_set:
                finish();
                break;
            case R.id.save:
                //保存全局变量，和服务器同步。主要用于商品 列表的中英文切换
                if (currentLanguage.equals(Common.SIMPLE_CHINESE)) {
                    config.locale = Locale.SIMPLIFIED_CHINESE;
                    MyApplication.getInstance().setLanguageType(Common.SIMPLE_CHINESE);
                } else if (currentLanguage.equals(Common.ENGLISH)) {
                    config.locale = Locale.US;
                    MyApplication.getInstance().setLanguageType(Common.ENGLISH);
                }
                getResources().updateConfiguration(config, dm);
                //把语言写入数据库
                SharedPreferences.Editor localEditor = sharedPreferences.edit();
                localEditor.putString(Common.LANGUAGE_TYPE, currentLanguage);
                localEditor.commit();
                //清除商品
                clearCache();
                finish();
                break;
            case R.id.language_chinese:
                if (!currentLanguage.equals(Common.SIMPLE_CHINESE)) {
                    currentLanguage = Common.SIMPLE_CHINESE;
                    updateUI(currentLanguage);
                }

                break;
            case R.id.language_english:
                if (!currentLanguage.equals(Common.ENGLISH)) {
                    currentLanguage = Common.ENGLISH;
                    updateUI(currentLanguage);

                }
                break;


            default:
                break;
        }
    }

    //清除acahe框架的缓存数据
    private void clearCache() {
        ACache.get(this).remove(Common.ALL_GOODS);
        ACache.get(this).remove(Common.ACACHE_ADDRESS);
    }

}
