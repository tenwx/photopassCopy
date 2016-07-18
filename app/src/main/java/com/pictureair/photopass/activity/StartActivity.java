package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;

import java.util.Locale;

import static android.os.Handler.Callback;

/**
 * 开始的启动页面，如果第一次进入，则进入第一次的引导页，如果不是，则进入登录页面
 *
 * @author bauer_bao
 */
public class StartActivity extends BaseActivity implements Callback {
    private SharedPreferences spApp;
    private int code = 0;
    private String languageType;
    private Configuration config;
    private DisplayMetrics displayMetrics;
    private TextView versionTextView;
    private static final String TAG = "StartActivity";
    private Handler handler;
    private Class tarClass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        spApp = getSharedPreferences(Common.SHARED_PREFERENCE_APP, MODE_PRIVATE);
        config = getResources().getConfiguration();
        displayMetrics = getResources().getDisplayMetrics();
        handler = new Handler(this);

        //获取手机设置的语言
        languageType = spApp.getString(Common.LANGUAGE_TYPE, "");
        if (!languageType.equals("")) {//语言不为空
            if (languageType.equals(Common.ENGLISH)) {
                config.locale = Locale.US;
            } else if (languageType.equals(Common.SIMPLE_CHINESE)) {
                config.locale = Locale.SIMPLIFIED_CHINESE;
            }
        } else {//语言为空，说明第一次进入
            PictureAirLog.out("langeuange is null---->" + config.locale.getLanguage());
            PictureAirLog.out("langeuange is null---->" + config.locale);
            if (config.locale.getLanguage().equals(Common.SIMPLE_CHINESE)) {
                languageType = Common.SIMPLE_CHINESE;
                config.locale = Locale.SIMPLIFIED_CHINESE;
            } else {
                languageType = Common.ENGLISH;
                config.locale = Locale.US;
            }
        }

        versionTextView = (TextView) findViewById(R.id.start_version_code_tv);

        getResources().updateConfiguration(config, displayMetrics);
        ((MyApplication) this.getApplicationContext()).setLanguageType(languageType);
        SharedPreferences.Editor editor = spApp.edit();
        editor.putString(Common.LANGUAGE_TYPE, languageType);
        editor.commit();

        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            final int versionCode = info.versionCode;
            versionTextView.setText("V" + info.versionName);
            code = spApp.getInt(Common.APP_VERSION_CODE, 0);
            PictureAirLog.out("code=" + code + ";versioncode=" + versionCode);
            SharedPreferences sp = getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, MODE_PRIVATE);
            final String _id = sp.getString(Common.USERINFO_ID, null);
            if (_id != null) {//之前登录过，直接进入主页面
                tarClass = MainTabActivity.class;

            } else if (code == 0){//没有登陆过，sp中没有这个值，第一次安装，则进入引导页
                tarClass = WelcomeActivity.class;
                editor = spApp.edit();
                editor.putInt(Common.APP_VERSION_CODE, versionCode);
                editor.putString(Common.APP_VERSION_NAME, info.versionName);
                editor.commit();

//            } else if (code == versionCode) {//无登录过，并且不是第一次安装，并且版本一致，进入登录页面
//                tarClass = LoginActivity.class;

            } else {//无登录，也不是第一次安装，版本不一致，表示升级的版本，进入登录页面
                tarClass = LoginActivity.class;

            }

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(StartActivity.this, tarClass);
                    startActivity(intent);
                    finish();
                }
            }, 2000);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }
}
