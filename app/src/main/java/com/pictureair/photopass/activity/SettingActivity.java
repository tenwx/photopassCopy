package com.pictureair.photopass.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.customDialog.CustomDialog;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.service.NotificationService;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.SettingUtil;
import com.pictureair.photopass.util.UmengUtil;

import java.lang.ref.WeakReference;

/**
 * 用户功能设置
 */
public class SettingActivity extends BaseActivity implements OnClickListener {
    private SettingUtil settingUtil;
    private RelativeLayout feedback;
    //    private ImageView back;
    private Button logout;
    private TextView tvSettingLanguage;
    private MyApplication application;

    private ImageButton ibGprWifiDownload, ibWifiOnlyDownload, ibAutoUpdate; // ico
    private RelativeLayout rlGprsWifiDoenload, rlWifiOnlyDownload, rlAutoUpdate;

    // 用于显示的 按钮。
    private PictureAirDbManager pictureAirDbManager;
    private SharedPreferences sharedPreferences;

    private final Handler settingHandler = new SettingHandler(this);


    private static class SettingHandler extends Handler{
        private final WeakReference<SettingActivity> mActivity;

        public SettingHandler(SettingActivity activity){
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

    /**
     * 处理Message
     * @param msg
     */
    private void dealHandler(Message msg) {
        switch (msg.what) {
            case API1.LOGOUT_FAILED:
            case API1.LOGOUT_SUCCESS:
                SharedPreferences sp = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
                Editor editor = sp.edit();
                editor.clear();
                editor.commit();

                ACache.get(SettingActivity.this).remove(Common.TOP_GOODS);
                ACache.get(SettingActivity.this).remove(Common.ALL_GOODS);
                ACache.get(SettingActivity.this).remove(Common.BANNER_GOODS);
                ACache.get(SettingActivity.this).remove(Common.PPP_GOOD);

                application.photoPassPicList.clear();
                application.setPushPhotoCount(0);
                application.scanMagicFinish = false;
                application.fragmentStoryLastSelectedTab = 0;
                pictureAirDbManager.deleteAllInfoFromTable(Common.PHOTOPASS_INFO_TABLE);

                MyApplication.clearTokenId();

                //取消通知
                Intent intent = new Intent(SettingActivity.this, NotificationService.class);
                intent.putExtra("status", "disconnect");
                startService(intent);

                Intent i = new Intent(SettingActivity.this, LoginActivity.class);
                finish();
                startActivity(i);

                AppManager.getInstance().AppExit(SettingActivity.this);
                break;
            case API1.SOCKET_DISCONNECT_FAILED:
            case API1.SOCKET_DISCONNECT_SUCCESS:
                API1.Logout(settingHandler);
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
        onCreate(null);
    }

    private void initView() {
        setTopLeftValueAndShow(R.drawable.back_white, true);
        setTopTitleShow(R.string.setting);
        logout = (Button) findViewById(R.id.logout);
        feedback = (RelativeLayout) findViewById(R.id.sub_opinions);
//        back = (ImageView) findViewById(R.id.back);
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
//        back.setOnClickListener(this);
        tvSettingLanguage.setOnClickListener(this);
        rlGprsWifiDoenload.setOnClickListener(this);
        rlWifiOnlyDownload.setOnClickListener(this);
        rlAutoUpdate.setOnClickListener(this);
        ibGprWifiDownload.setOnClickListener(this);
        ibWifiOnlyDownload.setOnClickListener(this);
        ibAutoUpdate.setOnClickListener(this);

        pictureAirDbManager = new PictureAirDbManager(this);
        settingUtil = new SettingUtil(this);
        sharedPreferences = getSharedPreferences(Common.USERINFO_NAME,
                Context.MODE_PRIVATE);
        judgeSettingStatus();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
//            case R.id.back:
//                finish();
//                break;

            case R.id.logout:
                new CustomDialog(SettingActivity.this, R.string.comfirm_logout, R.string.button_cancel, R.string.button_ok, new CustomDialog.MyDialogInterface() {
                    @Override
                    public void yes() {
                        // TODO Auto-generated method stub // 确定退出：购买AirPass+页面. 由于失去了airPass详情的界面。故此处，跳转到了airPass＋的界面。
                        // logout 之后，清空上个用户的数据。
                        application.setLast_tab(0);   // 设置 进入 app为主页
                        //断开推送
                        API1.noticeSocketDisConnect(settingHandler);
                    }

                    @Override
                    public void no() {
                        // TODO Auto-generated method stub // 取消退出：不做操作

                    }
                });

                break;

            case R.id.sub_opinions://消息回馈按钮
                //意见反馈弹出框
                UmengUtil.startFeedbackActivity(this);

                break;

            case R.id.setting_language:
                Intent intent = new Intent();
                intent.setClass(SettingActivity.this, SettingLanguageActivity.class);
                startActivity(intent);

                break;
            case R.id.ib_gprs_wifi_download:
            case R.id.rl_gprs_wifi_download: // 4g/3g/wifi 下载
                if (settingUtil.isOnlyWifiDownload(sharedPreferences.getString(Common.USERINFO_ID, ""))) {
                    settingUtil.deleteSettingOnlyWifiStatus(sharedPreferences.getString(Common.USERINFO_ID, ""));
                } else {

                }
                judgeSettingStatus();
                break;
            case R.id.ib_wifi_only_download:
            case R.id.rl_wifi_only_download: // 仅wifi下载
                if (settingUtil.isOnlyWifiDownload(sharedPreferences.getString(Common.USERINFO_ID, ""))) {

                } else {
                    settingUtil.insertSettingOnlyWifiStatus(sharedPreferences.getString(Common.USERINFO_ID, ""));
                }
                judgeSettingStatus();
                break;
            case R.id.ib_auto_update:
            case R.id.rl_auto_update: // 自动更新
                if (settingUtil.isAutoUpdate(sharedPreferences.getString(Common.USERINFO_ID, ""))) {
                    settingUtil.deleteSettingAutoUpdateStatus(sharedPreferences.getString(Common.USERINFO_ID, ""));
                } else {
                    settingUtil.insertSettingAutoUpdateStatus(sharedPreferences.getString(Common.USERINFO_ID, ""));
                }
                judgeSettingStatus();
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
            ibAutoUpdate.setImageResource(R.drawable.sele);
        } else {
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
        settingHandler.removeCallbacksAndMessages(null);
    }
}
