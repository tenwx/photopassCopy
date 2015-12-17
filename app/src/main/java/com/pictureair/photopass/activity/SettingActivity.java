package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.service.NotificationService;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.API;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.UmengUtil;

/**
 * 用户功能设置
 */
public class SettingActivity extends BaseActivity implements OnClickListener {
    private RelativeLayout feedback;
    private ImageView back;
    private Button logout;
    private TextView tvSettingLanguage;
    private MyApplication application;

    // 用于显示的 按钮。
    private ImageButton wifiDownload;
    private ImageButton autoDownload;
    private ImageButton autoAyncPhotoIb;

    private RelativeLayout rl_wifi_download;
    private RelativeLayout rl_auto_download;
    private RelativeLayout rl_auto_sync_photo;

    boolean isSync;//同步
    boolean isWifiDownload;//仅wifi下载

    private SharedPreferences sharedPreferences;
    private PictureAirDbManager pictureAirDbManager;
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case API1.LOGOUT_FAILED:
                case API1.LOGOUT_SUCCESS:
                    //断开推送
                    API.noticeSocketDisConnect(sharedPreferences.getString(Common.USERINFO_TOKENID, null));

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
                    //取消通知
                    Intent intent = new Intent(SettingActivity.this, NotificationService.class);
                    intent.putExtra("status", "disconnect");
                    startService(intent);

                    Intent i = new Intent(SettingActivity.this, LoginActivity.class);
                    finish();
                    startActivity(i);

                    AppManager.getInstance().AppExit(SettingActivity.this);
                    break;

                default:
                    break;
            }
        }

        ;
    };

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
        logout = (Button) findViewById(R.id.logout);
        feedback = (RelativeLayout) findViewById(R.id.sub_opinions);
        back = (ImageView) findViewById(R.id.back);
        tvSettingLanguage = (TextView) findViewById(R.id.setting_language);
        application = (MyApplication) getApplication();
        wifiDownload = (ImageButton) findViewById(R.id.wifi_download);
        autoDownload = (ImageButton) findViewById(R.id.auto_download);
        autoAyncPhotoIb = (ImageButton) findViewById(R.id.ib_auto_sync_photo);
        rl_wifi_download = (RelativeLayout) findViewById(R.id.rl_wifi_download);
        rl_auto_download = (RelativeLayout) findViewById(R.id.rl_auto_download);
        rl_auto_sync_photo = (RelativeLayout) findViewById(R.id.rl_auto_sync_photo);

        logout.setOnClickListener(this);
        feedback.setOnClickListener(this);
        back.setOnClickListener(this);
        tvSettingLanguage.setOnClickListener(this);
        rl_wifi_download.setOnClickListener(this);
        rl_auto_download.setOnClickListener(this);
        rl_auto_sync_photo.setOnClickListener(this);
        wifiDownload.setOnClickListener(this);
        autoDownload.setOnClickListener(this);
        autoAyncPhotoIb.setOnClickListener(this);

        pictureAirDbManager = new PictureAirDbManager(this);
        sharedPreferences = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
        isSync = pictureAirDbManager.checkFirstBuyPhoto(Common.SETTING_SYNC, sharedPreferences.getString(Common.USERINFO_ID, ""));
        isWifiDownload = pictureAirDbManager.checkFirstBuyPhoto(Common.SETTING_WIFI, sharedPreferences.getString(Common.USERINFO_ID, ""));

        setDownloadPhotos(isWifiDownload);
        setSyncPhotos(isSync);

    }

    /**
     * 设置下载在方式
     *
     * @param isWifiDownload 是否是wifi下载
     */
    private void setDownloadPhotos(boolean isWifiDownload) {
        if (!isWifiDownload) {
            //自动下载(2G/3G/wifi)
            autoDownload.setImageResource(R.drawable.sele);
            wifiDownload.setImageResource(R.drawable.nosele);
            pictureAirDbManager.deleteSettingStatus(Common.SETTING_WIFI, sharedPreferences.getString(Common.USERINFO_ID, ""));
        } else {
            //wifi下载
            autoDownload.setImageResource(R.drawable.nosele);
            wifiDownload.setImageResource(R.drawable.sele);
            pictureAirDbManager.insertSettingStatus(Common.SETTING_WIFI, sharedPreferences.getString(Common.USERINFO_ID, ""));
        }

    }

    /**
     * 同步更新照片
     *
     * @param isSync
     */
    private void setSyncPhotos(boolean isSync) {
        if (isSync) {
            autoAyncPhotoIb.setImageResource(R.drawable.sele);
            pictureAirDbManager.insertSettingStatus(Common.SETTING_SYNC, sharedPreferences.getString(Common.USERINFO_ID, ""));
        } else {
            autoAyncPhotoIb.setImageResource(R.drawable.nosele);
            pictureAirDbManager.deleteSettingStatus(Common.SETTING_SYNC, sharedPreferences.getString(Common.USERINFO_ID, ""));
        }

    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;

            case R.id.logout:
                // logout 之后，清空上个用户的数据。
                application.setLast_tab(0);   // 设置 进入 app为主页
                API1.Logout(handler);
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
            case R.id.rl_wifi_download:
            case R.id.wifi_download:
            case R.id.rl_auto_download:
            case R.id.auto_download:
                if (isWifiDownload) {
                    isWifiDownload = false;
                } else {
                    isWifiDownload = true;
                }
                setDownloadPhotos(isWifiDownload);
                break;
            case R.id.ib_auto_sync_photo:
            case R.id.rl_auto_sync_photo:
                //设置是否同步
                if (isSync) {
                    isSync = false;
                } else {
                    isSync = true;
                }
                setSyncPhotos(isSync);
                break;
            default:
                break;
        }

    }
}
