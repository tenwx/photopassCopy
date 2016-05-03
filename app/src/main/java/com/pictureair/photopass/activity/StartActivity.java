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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        spApp = getSharedPreferences(Common.APP, MODE_PRIVATE);
        config = getResources().getConfiguration();
        displayMetrics = getResources().getDisplayMetrics();
        handler = new Handler(this);

        //获取手机设置的语言
        languageType = spApp.getString(Common.LANGUAGE_TYPE, Common.ENGLISH);
        if (!languageType.equals("")) {
            if (languageType.equals(Common.ENGLISH)) {
                config.locale = Locale.US;
            } else if (languageType.equals(Common.SIMPLE_CHINESE)) {
                config.locale = Locale.SIMPLIFIED_CHINESE;
            }
        } else {
            if (config.locale.getLanguage().equals(Common.SIMPLE_CHINESE)) {
                languageType = Common.SIMPLE_CHINESE;
            } else {
                languageType = Common.ENGLISH;
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

            if (code == versionCode) {// 启动app,如果不是第一次进入，则直接跳过引导页
                // 需要从服务器获取最新的照片信息，地点信息，个人用户信息，商品信息，购物车信息等等，然后存入到数据库中
                SharedPreferences sp = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
                final String _id = sp.getString(Common.USERINFO_ID, null);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = null;
                        if (_id != null) {//判断是否已经登录
                            //发送广播
                            PictureAirLog.d(TAG, "start push service");
                            intent = new Intent(StartActivity.this, MainTabActivity.class);
                        } else {
                            intent = new Intent(StartActivity.this, LoginActivity.class);
                        }
                        finish();
                        startActivity(intent);
                    }
                }, 2000);
            } else {//进入引导页
//				API1.getTokenId(this, null);
                editor = spApp.edit();
                editor.putInt(Common.APP_VERSION_CODE, versionCode);
                editor.putString(Common.APP_VERSION_NAME, info.versionName);
                editor.commit();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(StartActivity.this, WelcomeActivity.class);
                        finish();
                        startActivity(intent);
                    }
                }, 2000);
            }
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
