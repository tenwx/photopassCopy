package com.pictureair.photopass.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.customDialog.PWDialog;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.util.AppExitUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SettingUtil;
import com.pictureair.photopass.widget.CustomProgressDialog;

import java.lang.ref.WeakReference;

/**
 * @author talon
 *         用户功能设置
 */
public class SettingActivity extends BaseActivity implements OnClickListener, PWDialog.OnPWDialogClickListener {
    private SettingUtil settingUtil;
    private RelativeLayout feedback;
    private Button logout;
    private TextView tvSettingLanguage,tvAbout;
    private MyApplication application;
    private ImageView backBtn;

    private ImageButton ibGprWifiDownload, ibWifiOnlyDownload; // ico
    private RelativeLayout rlGprsWifiDoenload, rlWifiOnlyDownload;

    // 用于显示的 按钮。
    private SharedPreferences sharedPreferences;
    private SharedPreferences appSharedPreferences;
    private String currentLanguage;
    private final String TAG = "SettingActivity";
    private static final int LOGOUT_DIALOG = 111;

    private CustomProgressDialog customProgressDialog; // loading 框
    private PWDialog pwDialog;

    private final Handler settingHandler = new HelpHandler(this);


    private static class HelpHandler extends Handler{
        private final WeakReference<SettingActivity> mActivity;

        public HelpHandler(SettingActivity activity){
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivity.get() == null) {
                return;
            }
            mActivity.get().dealHandler(msg);
        }
    }

    private void dealHandler(Message msg) {
        if (null != customProgressDialog && customProgressDialog.isShowing()) {
            customProgressDialog.dismiss();
        }

        switch (msg.what){
            case 1:
                ibGprWifiDownload.setImageResource(R.drawable.nosele);
                ibWifiOnlyDownload.setImageResource(R.drawable.sele);
                break;
            case 2:
                ibGprWifiDownload.setImageResource(R.drawable.sele);
                ibWifiOnlyDownload.setImageResource(R.drawable.nosele);
                break;
            case 3:
//                isAutoUpdate = true;
//                ibAutoUpdate.setImageResource(R.drawable.sele);
                break;
            case 4:
//                isAutoUpdate = false;
//                ibAutoUpdate.setImageResource(R.drawable.nosele);
                break;

        }
    }

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
        tvAbout = (TextView) findViewById(R.id.setting_about);
        application = (MyApplication) getApplication();

        ibGprWifiDownload = (ImageButton) findViewById(R.id.ib_gprs_wifi_download);
        ibWifiOnlyDownload = (ImageButton) findViewById(R.id.ib_wifi_only_download);
//        ibAutoUpdate = (ImageButton) findViewById(R.id.ib_auto_update);

        rlGprsWifiDoenload = (RelativeLayout) findViewById(R.id.rl_gprs_wifi_download);
        rlWifiOnlyDownload = (RelativeLayout) findViewById(R.id.rl_wifi_only_download);
//        rlAutoUpdate = (RelativeLayout) findViewById(R.id.rl_auto_update);
        logout.setTypeface(MyApplication.getInstance().getFontBold());
        tvSettingLanguage.setTypeface(MyApplication.getInstance().getFontBold());
        tvAbout.setTypeface(MyApplication.getInstance().getFontBold());
        ((TextView) findViewById(R.id.tv_feedback)).setTypeface(MyApplication.getInstance().getFontBold());
        ((TextView) findViewById(R.id.tv_download)).setTypeface(MyApplication.getInstance().getFontBold());
//        ((TextView) findViewById(R.id.tv_update_photo)).setTypeface(MyApplication.getInstance().getFontBold());

        logout.setOnClickListener(this);
        feedback.setOnClickListener(this);
        backBtn.setOnClickListener(this);
        tvSettingLanguage.setOnClickListener(this);
        tvAbout.setOnClickListener(this);
        rlGprsWifiDoenload.setOnClickListener(this);
        rlWifiOnlyDownload.setOnClickListener(this);
//        rlAutoUpdate.setOnClickListener(this);
        ibGprWifiDownload.setOnClickListener(this);
        ibWifiOnlyDownload.setOnClickListener(this);
//        ibAutoUpdate.setOnClickListener(this);

        settingUtil = new SettingUtil(new PictureAirDbManager(this));
        sharedPreferences = getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, Context.MODE_PRIVATE);
        appSharedPreferences = getSharedPreferences(Common.SHARED_PREFERENCE_APP, MODE_PRIVATE);
        currentLanguage = appSharedPreferences.getString(Common.LANGUAGE_TYPE, Common.ENGLISH);
        customProgressDialog = CustomProgressDialog.show(this, this.getString(R.string.is_loading), true, null);
        pwDialog = new PWDialog(this)
                .setOnPWDialogClickListener(this)
                .pwDialogCreate();
        new Thread() {
            @Override
            public void run() {
                judgeSettingStatus();
            }
        }.start();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;

            case R.id.logout:
                pwDialog.setPWDialogId(LOGOUT_DIALOG)
                        .setPWDialogMessage(R.string.comfirm_logout)
                        .setPWDialogNegativeButton(R.string.button_cancel)
                        .setPWDialogPositiveButton(R.string.button_ok)
                        .pwDilogShow();

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
//            case R.id.ib_auto_update:
//            case R.id.rl_auto_update: // 自动更新。  选择框提示。
//                if (isAutoUpdate == true){ //如果是自动更新
//                    ibAutoUpdate.setImageResource(R.drawable.nosele);
//                    isAutoUpdate = false;
//                    new Thread() {
//                        @Override
//                        public void run() {
//                            settingUtil.deleteSettingAutoUpdateStatus(sharedPreferences.getString(Common.USERINFO_ID, ""));
//                        }
//                    }.start();
//                }else{
//                    new CustomDialog(SettingActivity.this, R.string.confirm_sync_msg, R.string.confirm_sync_no, R.string.confirm_sync_yes, new CustomDialog.MyDialogInterface() {
//                        @Override
//                        public void yes() {
//                            // TODO Auto-generated method stub // 确认同步更新后，修改更新设置状态
//                            ibAutoUpdate.setImageResource(R.drawable.sele);
//                            isAutoUpdate = true;
//                            new Thread() {
//                                @Override
//                                public void run() {
//                                    settingUtil.insertSettingAutoUpdateStatus(sharedPreferences.getString(Common.USERINFO_ID, ""));
//                                }
//                            }.start();
//                        }
//                        @Override
//                        public void no() {
//                            // TODO Auto-generated method stub // 取消：不做操作
//
//                        }
//                    });
//                }
//                break;
            case R.id.setting_about:
                Intent i = new Intent(MyApplication.getInstance(),AboutActivity.class);
                startActivity(i);
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
            settingHandler.sendEmptyMessage(1);
        } else {
            settingHandler.sendEmptyMessage(2);
        }

        if (settingUtil.isAutoUpdate(sharedPreferences.getString(Common.USERINFO_ID, ""))) {
            settingHandler.sendEmptyMessage(3);
        } else {
            settingHandler.sendEmptyMessage(4);
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

    @Override
    public void onPWDialogButtonClicked(int which, int dialogId) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            switch (dialogId) {
                case LOGOUT_DIALOG:
                    // logout 之后，清空上个用户的数据。
                    application.setMainTabIndex(0);   // 设置 进入 app为主页
                    //断开推送
                    AppExitUtil.getInstance().AppLogout();
                    break;

                default:
                    break;
            }
        }
    }
}
