package com.pictureair.photopass.activity;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.customDialog.PWDialog;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SPUtils;

import java.util.Locale;

public class SettingLanguageActivity extends BaseActivity implements OnClickListener, PWDialog.OnPWDialogClickListener {
    private static final String TAG = "SettingLanguageActivity";
    private Configuration config;

    private RelativeLayout back;
    private RelativeLayout languageChinese;
    private RelativeLayout languageEnglish;

    private ImageView chineseSeleted;
    private ImageView englishSeleted;

    private String oldLanguage = "";
    private String currentLanguage = "";   // en表示英语，zh表示简体中文。
    private PWDialog pictureWorksDialog;

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

        back = (RelativeLayout) findViewById(R.id.back_set);
        languageChinese = (RelativeLayout) findViewById(R.id.language_chinese);
        languageEnglish = (RelativeLayout) findViewById(R.id.language_english);

        chineseSeleted = (ImageView) findViewById(R.id.chinese_imageView);
        englishSeleted = (ImageView) findViewById(R.id.english_imageView);

        back.setOnClickListener(this);
        languageChinese.setOnClickListener(this);
        languageEnglish.setOnClickListener(this);

        oldLanguage = SPUtils.getString(this, Common.SHARED_PREFERENCE_APP, Common.LANGUAGE_TYPE, Common.ENGLISH);
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
        if (pictureWorksDialog == null) {
            pictureWorksDialog = new PWDialog(SettingLanguageActivity.this)
                    .setOnPWDialogClickListener(this)
                    .pwDialogCreate();
        }
        //修改语言之后，需要重新设置下文字，不然还是原来的语言
        pictureWorksDialog.setPWDialogMessage(R.string.setting_language_msg)
                .setPWDialogNegativeButton(R.string.confirm_sync_no)
                .setPWDialogPositiveButton(R.string.confirm_sync_yes)
                .pwDilogShow();
    }

    @Override
    public void onPWDialogButtonClicked(int which, int dialogId) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            if (currentLanguage.equals(Common.SIMPLE_CHINESE)) {
                currentLanguage = Common.ENGLISH;
            } else {
                currentLanguage = Common.SIMPLE_CHINESE;
            }
            updateUI(currentLanguage);
            if (currentLanguage.equals(Common.SIMPLE_CHINESE)) {
                if (Build.VERSION.SDK_INT < 24) {
                    config.locale = Locale.SIMPLIFIED_CHINESE;
                } else {
                    config.setLocale(Locale.SIMPLIFIED_CHINESE);
                }
                MyApplication.getInstance().setLanguageType(Common.SIMPLE_CHINESE);
            } else if (currentLanguage.equals(Common.ENGLISH)) {
                if (Build.VERSION.SDK_INT < 24) {
                    config.locale = Locale.US;
                } else {
                    config.setLocale(Locale.US);
                }
                MyApplication.getInstance().setLanguageType(Common.ENGLISH);
            }
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
            //把语言写入数据库
            SPUtils.put(this, Common.SHARED_PREFERENCE_APP, Common.LANGUAGE_TYPE, currentLanguage);
            //清除商品
            clearCache();
            onCreate(null);
        }
    }
}
