package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.networkbench.agent.impl.NBSAppAgent;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.PhotoDownLoadInfo;
import com.pictureair.photopass.service.DownloadService;
import com.pictureair.photopass.service.NotificationService;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PhotoDownLoadInfoSortUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SPUtils;

import java.util.Collections;
import java.util.List;

import static android.os.Handler.Callback;

/**
 * 开始的启动页面，如果第一次进入，则进入第一次的引导页，如果不是，则进入登录页面
 *
 * @author bauer_bao
 */
public class StartActivity extends BaseActivity implements Callback {
    private int code = 0;
    private String _id;
    private TextView versionTextView;
    private Handler handler;
    private Class tarClass;
    private PictureAirDbManager pictureAirDbManager;
    private static final int UPDATE_SUCCESS = 1111;
    private long updateTime = 0;
    private long curTime = 0;
    private LinearLayout ll_update;
    private ImageView img_update;
    private AnimationDrawable spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        //初始化听云统计，必须放入工程的入口Startactivity
        NBSAppAgent.setLicenseKey(Common.TINGYUN_KEY).withLocationServiceEnabled(true).start(this.getApplicationContext());

        ll_update = (LinearLayout) findViewById(R.id.ll_update);
        img_update = (ImageView) findViewById(R.id.img_update);
        spinner = (AnimationDrawable) img_update.getBackground();
        handler = new Handler(this);
        versionTextView = (TextView) findViewById(R.id.start_version_code_tv);
        pictureAirDbManager = new PictureAirDbManager(getApplicationContext());
        _id = SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ID, null);
        boolean update =  SPUtils.getBoolean(this, Common.SHARED_PREFERENCE_APP, Common.REMOVE_REPEATE_PHOTO, false);
        updateTime = System.currentTimeMillis();
        if (!update) {
            ll_update.setVisibility(View.VISIBLE);
            spinner.start();
            new RemoveRepeatPhotoTask().start();
        }else {
            handler.obtainMessage(UPDATE_SUCCESS).sendToTarget();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case UPDATE_SUCCESS:
                curTime = System.currentTimeMillis();
                long between = curTime - updateTime;
                PictureAirLog.e("startactivity between", String.valueOf(between));
                if (between < 2000) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            goToNextActivity();
                        }
                    }, 2000 - between);
                } else {
                    goToNextActivity();
                }
                break;
        }
        return false;
    }

    /**
     * 需要在开始跳转的时候，才去判断需要跳转哪个页面。不然会造成，已经判断好跳转的页面，在2s的等待时间内，登录过期，进入重新登录页面之后，2s事件触发，又进入了之前得到的页面
     */
    private void goToNextActivity(){
        PictureAirLog.d("go to next activity");
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            int versionCode = info.versionCode;
            versionTextView.setText("V" + info.versionName);
            code = SPUtils.getInt(this, Common.SHARED_PREFERENCE_APP, Common.APP_VERSION_CODE, 0);
            PictureAirLog.out("code=" + code + ";versioncode=" + versionCode);

            boolean isLogin = SPUtils.getBoolean(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ISLOGIN, false);

            if (_id != null && isLogin) {//之前登录过
                if (Common.NEED_RELOGIN && SPUtils.getInt(this, Common.SHARED_PREFERENCE_APP, Common.APP_NEED_RELOGIN, 0) < versionCode) {//升级版本之后检查是否需要重新登录
                    SPUtils.clear(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME);

                    ACache.get(MyApplication.getInstance()).remove(Common.ALL_GOODS);
                    ACache.get(MyApplication.getInstance()).remove(Common.ACACHE_ADDRESS);

                    MyApplication.getInstance().setPushPhotoCount(0);
                    MyApplication.getInstance().setPushViedoCount(0);
                    MyApplication.getInstance().scanMagicFinish = false;
                    MyApplication.getInstance().fragmentStoryLastSelectedTab = 0;
                    pictureAirDbManager.deleteAllInfoFromTable();

                    MyApplication.clearTokenId();

                    //取消通知
                    Intent intent = new Intent(MyApplication.getInstance(), NotificationService.class);
                    intent.putExtra("status", "disconnect");
                    MyApplication.getInstance().startService(intent);

                    //关闭下载
                    Intent intent1 = new Intent(MyApplication.getInstance(), DownloadService.class);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("logout",true);
                    intent1.putExtras(bundle);
                    MyApplication.getInstance().startService(intent1);

                    SPUtils.put(this, Common.SHARED_PREFERENCE_APP, Common.APP_NEED_RELOGIN, versionCode);
                    tarClass = LoginActivity.class;

                } else {//直接进入主页面
                    tarClass = MainTabActivity.class;
                }

            } else if (code == 0){//没有登陆过，sp中没有这个值，第一次安装，则进入引导页
                tarClass = WelcomeActivity.class;

            } else {//无登录，也不是第一次安装，版本不一致，表示升级的版本，进入登录页面
                tarClass = LoginActivity.class;

            }

            if (code < versionCode) {//更新版本号
                SPUtils.put(this, Common.SHARED_PREFERENCE_APP, Common.APP_VERSION_CODE, versionCode);
                SPUtils.put(this, Common.SHARED_PREFERENCE_APP, Common.APP_VERSION_NAME, info.versionName);

            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(StartActivity.this, tarClass);
        startActivity(intent);
        finish();
    }


    class RemoveRepeatPhotoTask extends Thread{
        @Override
        public void run() {
            try {
                List<String> users = pictureAirDbManager.getAllUsers();
                if (users.size() >0) {
                    for (int k = 0;k<users.size();k++) {
                        String userId = users.get(k);
                        List<PhotoDownLoadInfo> list = pictureAirDbManager.getAllPhotos(userId);
                        Collections.sort(list, new PhotoDownLoadInfoSortUtil());
                        if (list != null && list.size() > 0) {
                            int len = list.size();
                            for (int i = 0; i < len - 1; i++) {
                                PhotoDownLoadInfo infoi = list.get(i);
                                for (int j = i + 1; j < len; j++) {
                                    PhotoDownLoadInfo infoj = list.get(j);
                                    if (infoi.getPhotoId().equalsIgnoreCase(infoj.getPhotoId())) {
                                        pictureAirDbManager.deleteRepeatPhoto(userId, infoj);
                                        list.remove(j);
                                        j--;
                                        len--;
                                    }
                                }
                            }
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            SPUtils.put(MyApplication.getInstance(), Common.SHARED_PREFERENCE_APP, Common.REMOVE_REPEATE_PHOTO, true);
            handler.obtainMessage(UPDATE_SUCCESS).sendToTarget();

        }
    }
}
