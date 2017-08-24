package com.pictureair.hkdlphotopass.activity;

import android.content.DialogInterface;
import android.content.Intent;
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

import com.pictureair.hkdlphotopass.MyApplication;
import com.pictureair.hkdlphotopass.R;
import com.pictureair.hkdlphotopass.customDialog.PWDialog;
import com.pictureair.hkdlphotopass.util.AppExitUtil;
import com.pictureair.hkdlphotopass.util.Common;
import com.pictureair.hkdlphotopass.util.PictureAirLog;
import com.pictureair.hkdlphotopass.util.SPUtils;
import com.pictureair.hkdlphotopass.util.SettingUtil;

import java.lang.ref.WeakReference;

/**
 * 用户功能设置
 * @author talon
 */
public class SettingActivity extends BaseActivity implements OnClickListener, PWDialog.OnPWDialogClickListener {
    private SettingUtil settingUtil;
    private Button logout;
    private TextView tvSettingLanguage, tvAbout;
    private MyApplication application;
    private ImageView backBtn;

    private ImageButton ibGprWifiDownload, ibWifiOnlyDownload; // ico
    private RelativeLayout rlGprsWifiDoenload, rlWifiOnlyDownload;

    // 用于显示的 按钮。
    private String currentLanguage;
    private String userId;
    private final String TAG = "SettingActivity";
    private static final int LOGOUT_DIALOG = 111;

    private PWDialog pwDialog;

    private final Handler settingHandler = new HelpHandler(this);

    private static class HelpHandler extends Handler {
        private final WeakReference<SettingActivity> mActivity;

        public HelpHandler(SettingActivity activity) {
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
        dismissPWProgressDialog();
        switch (msg.what) {
            case 1:
                ibGprWifiDownload.setImageResource(R.drawable.nosele);
                ibWifiOnlyDownload.setImageResource(R.drawable.sele);
                break;

            case 2:
                ibGprWifiDownload.setImageResource(R.drawable.sele);
                ibWifiOnlyDownload.setImageResource(R.drawable.nosele);
                break;

            default:
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
        backBtn = (ImageView) findViewById(R.id.back);
        tvSettingLanguage = (TextView) findViewById(R.id.setting_language);
        tvAbout = (TextView) findViewById(R.id.setting_about);
        application = (MyApplication) getApplication();

        ibGprWifiDownload = (ImageButton) findViewById(R.id.ib_gprs_wifi_download);
        ibWifiOnlyDownload = (ImageButton) findViewById(R.id.ib_wifi_only_download);

        rlGprsWifiDoenload = (RelativeLayout) findViewById(R.id.rl_gprs_wifi_download);
        rlWifiOnlyDownload = (RelativeLayout) findViewById(R.id.rl_wifi_only_download);

        logout.setOnClickListener(this);
        backBtn.setOnClickListener(this);
        tvSettingLanguage.setOnClickListener(this);
        tvAbout.setOnClickListener(this);
        rlGprsWifiDoenload.setOnClickListener(this);
        rlWifiOnlyDownload.setOnClickListener(this);
        ibGprWifiDownload.setOnClickListener(this);
        ibWifiOnlyDownload.setOnClickListener(this);

        settingUtil = new SettingUtil();
        userId = SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ID, "");
        currentLanguage = SPUtils.getString(this, Common.SHARED_PREFERENCE_APP, Common.LANGUAGE_TYPE, Common.ENGLISH);
        showPWProgressDialog(true);
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
                        if (settingUtil.isOnlyWifiDownload(userId)) {
                            settingUtil.deleteSettingOnlyWifiStatus(userId);
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
                        if (settingUtil.isOnlyWifiDownload(userId)) {
                        } else {
                            settingUtil.insertSettingOnlyWifiStatus(userId);
                        }
                    }
                }.start();
                break;

            case R.id.setting_about:
                Intent i = new Intent(MyApplication.getInstance(), AboutActivity.class);
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
        if (settingUtil.isOnlyWifiDownload(userId)) {
            settingHandler.sendEmptyMessage(1);
        } else {
            settingHandler.sendEmptyMessage(2);
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
                    showPWProgressDialog();
                    application.setMainTabIndex(-1);   // 设置 进入 app为主页
                    //断开推送
                    AppExitUtil.getInstance().AppLogout();
                    break;

                default:
                    break;
            }
        }
    }
}
