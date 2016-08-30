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

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.PhotoDownLoadInfo;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PhotoDownLoadInfoSortUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.util.ScreenUtil;

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
    private static final String TAG = "StartActivity";
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
        ScreenUtil.setFullScreen(this);
        setContentView(R.layout.activity_start);
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
                try {
                    PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
                    int versionCode = info.versionCode;
                    versionTextView.setText("V" + info.versionName);
                    code = SPUtils.getInt(this, Common.SHARED_PREFERENCE_APP, Common.APP_VERSION_CODE, 0);
                    PictureAirLog.out("code=" + code + ";versioncode=" + versionCode);

                    if (_id != null) {//之前登录过，直接进入主页面
                        tarClass = MainTabActivity.class;

                    } else if (code == 0){//没有登陆过，sp中没有这个值，第一次安装，则进入引导页
                        tarClass = WelcomeActivity.class;
                        SPUtils.put(this, Common.SHARED_PREFERENCE_APP, Common.APP_VERSION_CODE, versionCode);
                        SPUtils.put(this, Common.SHARED_PREFERENCE_APP, Common.APP_VERSION_NAME, info.versionName);

//                  } else if (code == versionCode) {//无登录过，并且不是第一次安装，并且版本一致，进入登录页面
//                      tarClass = LoginActivity.class;

                    } else {//无登录，也不是第一次安装，版本不一致，表示升级的版本，进入登录页面
                        tarClass = LoginActivity.class;

                    }
                    curTime = System.currentTimeMillis();
                    long between = curTime-updateTime;
                    PictureAirLog.e("startactivity between",String.valueOf(between));
                    if (between < 2000){
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                goToNextActivity();
                            }
                        }, 2000-between);
                    }else{
                        goToNextActivity();
                    }
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
                break;
        }
        return false;
    }

    private void goToNextActivity(){
        Intent intent = new Intent(StartActivity.this, tarClass);
        finish();
        startActivity(intent);
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
