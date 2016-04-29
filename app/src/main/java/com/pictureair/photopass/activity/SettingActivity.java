package com.pictureair.photopass.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.customDialog.CustomDialog;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.util.AppExitUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SettingUtil;

/**
 * @author talon
 *         用户功能设置
 */
public class SettingActivity extends BaseActivity implements OnClickListener {
    private SettingUtil settingUtil;
    private RelativeLayout feedback;
    //    private ImageView back;
    private Button logout;
    private TextView tvSettingLanguage;
    private MyApplication application;
    private ImageView backBtn;

    private ImageButton ibGprWifiDownload, ibWifiOnlyDownload, ibAutoUpdate; // ico
    private RelativeLayout rlGprsWifiDoenload, rlWifiOnlyDownload, rlAutoUpdate;

    // 用于显示的 按钮。
    private SharedPreferences sharedPreferences;
    private SharedPreferences appSharedPreferences;
    private String currentLanguage;
    private final String TAG = "SettingActivity";

    //纪录是否自动更新的变量。
    private boolean isAutoUpdate = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentLanguage != null && !currentLanguage.equals(MyApplication.getInstance().getLanguageType())) {
            PictureAirLog.v(TAG, "onResume onCreate(null)");
            onCreate(null);
        }
    }

    private void initView() {
        logout = (Button) findViewById(R.id.logout);
        feedback = (RelativeLayout) findViewById(R.id.sub_opinions);
        backBtn = (ImageView) findViewById(R.id.back);
        tvSettingLanguage = (TextView) findViewById(R.id.setting_language);
        application = (MyApplication) getApplication();

        ibGprWifiDownload = (ImageButton) findViewById(R.id.ib_gprs_wifi_download);
        ibWifiOnlyDownload = (ImageButton) findViewById(R.id.ib_wifi_only_download);
        ibAutoUpdate = (ImageButton) findViewById(R.id.ib_auto_update);

        rlGprsWifiDoenload = (RelativeLayout) findViewById(R.id.rl_gprs_wifi_download);
        rlWifiOnlyDownload = (RelativeLayout) findViewById(R.id.rl_wifi_only_download);
        rlAutoUpdate = (RelativeLayout) findViewById(R.id.rl_auto_update);
        logout.setTypeface(MyApplication.getInstance().getFontBold());
        tvSettingLanguage.setTypeface(MyApplication.getInstance().getFontBold());
        ((TextView) findViewById(R.id.tv_feedback)).setTypeface(MyApplication.getInstance().getFontBold());
        ((TextView) findViewById(R.id.tv_download)).setTypeface(MyApplication.getInstance().getFontBold());
        ((TextView) findViewById(R.id.tv_update_photo)).setTypeface(MyApplication.getInstance().getFontBold());

        logout.setOnClickListener(this);
        feedback.setOnClickListener(this);
        backBtn.setOnClickListener(this);
        tvSettingLanguage.setOnClickListener(this);
        rlGprsWifiDoenload.setOnClickListener(this);
        rlWifiOnlyDownload.setOnClickListener(this);
        rlAutoUpdate.setOnClickListener(this);
        ibGprWifiDownload.setOnClickListener(this);
        ibWifiOnlyDownload.setOnClickListener(this);
        ibAutoUpdate.setOnClickListener(this);

        settingUtil = new SettingUtil(new PictureAirDbManager(this));
        sharedPreferences = getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
        appSharedPreferences = getSharedPreferences(Common.APP, MODE_PRIVATE);
        currentLanguage = appSharedPreferences.getString(Common.LANGUAGE_TYPE, "");
        judgeSettingStatus();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;

            case R.id.logout:
                new CustomDialog(SettingActivity.this, R.string.comfirm_logout, R.string.button_cancel, R.string.button_ok, new CustomDialog.MyDialogInterface() {
                    @Override
                    public void yes() {
                        // TODO Auto-generated method stub // 确定退出：购买AirPass+页面. 由于失去了airPass详情的界面。故此处，跳转到了airPass＋的界面。
                        // logout 之后，清空上个用户的数据。
                        application.setMainTabIndex(0);   // 设置 进入 app为主页
                        //断开推送
                        AppExitUtil.getInstance().AppLogout();
                    }

                    @Override
                    public void no() {
                        // TODO Auto-generated method stub // 取消退出：不做操作

                    }
                });

                break;

            case R.id.setting_language:
                Intent intent = new Intent();
                intent.setClass(SettingActivity.this, SettingLanguageActivity.class);
                startActivity(intent);

                break;
            case R.id.ib_gprs_wifi_download:
            case R.id.rl_gprs_wifi_download: // 4g/3g/wifi 下载

                ibGprWifiDownload.setImageResource(R.drawable.sele);
                ibWifiOnlyDownload.setImageResource(R.drawable.nosele);

                new Thread() {
                    @Override
                    public void run() {
                        if (settingUtil.isOnlyWifiDownload(sharedPreferences.getString(Common.USERINFO_ID, ""))) {
                            settingUtil.deleteSettingOnlyWifiStatus(sharedPreferences.getString(Common.USERINFO_ID, ""));
                        } else {

                        }
                    }
                }.start();
                break;
            case R.id.ib_wifi_only_download:
            case R.id.rl_wifi_only_download: // 仅wifi下载
                ibGprWifiDownload.setImageResource(R.drawable.nosele);
                ibWifiOnlyDownload.setImageResource(R.drawable.sele);

                new Thread() {
                    @Override
                    public void run() {
                        if (settingUtil.isOnlyWifiDownload(sharedPreferences.getString(Common.USERINFO_ID, ""))) {
                        } else {
                            settingUtil.insertSettingOnlyWifiStatus(sharedPreferences.getString(Common.USERINFO_ID, ""));
                        }
                    }
                }.start();
                break;
            case R.id.ib_auto_update:
            case R.id.rl_auto_update: // 自动更新。  选择框提示。
                if (isAutoUpdate == true){ //如果是自动更新
                    ibAutoUpdate.setImageResource(R.drawable.nosele);
                    isAutoUpdate = false;
                    new Thread() {
                        @Override
                        public void run() {
                            settingUtil.deleteSettingAutoUpdateStatus(sharedPreferences.getString(Common.USERINFO_ID, ""));
                        }
                    }.start();
                }else{
                    new CustomDialog(SettingActivity.this, R.string.confirm_sync_msg, R.string.confirm_sync_no, R.string.confirm_sync_yes, new CustomDialog.MyDialogInterface() {
                        @Override
                        public void yes() {
                            // TODO Auto-generated method stub // 确认同步更新后，修改更新设置状态
                            ibAutoUpdate.setImageResource(R.drawable.sele);
                            isAutoUpdate = true;
                            new Thread() {
                                @Override
                                public void run() {
                                    settingUtil.insertSettingAutoUpdateStatus(sharedPreferences.getString(Common.USERINFO_ID, ""));
                                }
                            }.start();
                        }
                        @Override
                        public void no() {
                            // TODO Auto-generated method stub // 取消：不做操作

                        }
                    });
                }
                break;
            default:
                break;
        }
    }

    /**
     * 判断当前的设置模式，并且作出相应的视图。
     */
    private void judgeSettingStatus() {
        if (settingUtil.isOnlyWifiDownload(sharedPreferences.getString(Common.USERINFO_ID, ""))) {
            ibGprWifiDownload.setImageResource(R.drawable.nosele);
            ibWifiOnlyDownload.setImageResource(R.drawable.sele);
        } else {
            ibGprWifiDownload.setImageResource(R.drawable.sele);
            ibWifiOnlyDownload.setImageResource(R.drawable.nosele);
        }

        if (settingUtil.isAutoUpdate(sharedPreferences.getString(Common.USERINFO_ID, ""))) {
            isAutoUpdate = true;
            ibAutoUpdate.setImageResource(R.drawable.sele);
        } else {
            isAutoUpdate = false;
            ibAutoUpdate.setImageResource(R.drawable.nosele);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
