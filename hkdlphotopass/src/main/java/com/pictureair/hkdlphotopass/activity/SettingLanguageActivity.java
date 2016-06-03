package com.pictureair.hkdlphotopass.activity;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.pictureair.hkdlphotopass.MyApplication;
import com.pictureair.hkdlphotopass.R;
import com.pictureworks.android.customDialog.CustomDialog;
import com.pictureworks.android.util.ACache;
import com.pictureworks.android.util.Common;
import com.pictureworks.android.util.PictureAirLog;
import com.pictureworks.android.widget.PictureWorksDialog;

import java.util.Locale;

public class SettingLanguageActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = "SettingLanguageActivity";
    private Configuration config;
    private DisplayMetrics dm;

    private RelativeLayout back;
    private RelativeLayout languageChinese;
    private RelativeLayout languageEnglish;
//    private RelativeLayout save;
//    private TextView saveTv;

    private ImageView chineseSeleted;
    private ImageView englishSeleted;

    private String oldLanguage = "";
    private String currentLanguage = "";   // en表示英语，zh表示简体中文。
    private SharedPreferences sharedPreferences;
    private PictureWorksDialog pictureWorksDialog;

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
//        save = (RelativeLayout) findViewById(R.id.save);
//        saveTv = (TextView) findViewById(R.id.save_tv);

        chineseSeleted = (ImageView) findViewById(R.id.chinese_imageView);
        englishSeleted = (ImageView) findViewById(R.id.english_imageView);

//        save.setOnClickListener(this);
        back.setOnClickListener(this);
        languageChinese.setOnClickListener(this);
        languageEnglish.setOnClickListener(this);

        sharedPreferences = getSharedPreferences(Common.SHARED_PREFERENCE_APP, MODE_PRIVATE);
        oldLanguage = sharedPreferences.getString(Common.LANGUAGE_TYPE, Common.ENGLISH);
        currentLanguage = oldLanguage;

        PictureAirLog.out("current language---->" + currentLanguage);
        if (currentLanguage.equals("")) {
            //获取本地数据
            if (config.locale.getLanguage().equals(Common.SIMPLE_CHINESE)) {
                currentLanguage = Common.SIMPLE_CHINESE;
            } else {
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
            case R.id.language_chinese:
                if (!currentLanguage.equals(Common.SIMPLE_CHINESE)) {
//                    updateUI(currentLanguage);
                    createDialog();
                }
                break;
            case R.id.language_english:
                if (!currentLanguage.equals(Common.ENGLISH)) {
//                    updateUI(currentLanguage);
                    createDialog();
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


    // 改变语言设置时的对话框
    private void createDialog() {
        new CustomDialog(SettingLanguageActivity.this, R.string.setting_language_msg, R.string.confirm_sync_no, R.string.confirm_sync_yes, new CustomDialog.MyDialogInterface() {
            @Override
            public void yes() {
                // TODO Auto-generated method stub // 确认语言设置之后，修改状态

                if (currentLanguage.equals(Common.SIMPLE_CHINESE)){
                    currentLanguage = Common.ENGLISH;
                }else{
                    currentLanguage = Common.SIMPLE_CHINESE;
                }
                updateUI(currentLanguage);
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
                onCreate(null);
//                finish();
            }
            @Override
            public void no() {
                // TODO Auto-generated method stub // 取消：不做操作

            }
        });
    }

}
