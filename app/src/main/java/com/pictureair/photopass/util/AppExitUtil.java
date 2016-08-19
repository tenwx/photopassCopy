package com.pictureair.photopass.util;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.LoginActivity;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.service.NotificationService;
import com.pictureair.photopass.widget.PWToast;

/**
 * Created by milo on 15/12/29.
 * 退出程序操作
 * 1.断开推送
 * 2.清空数据
 */
public class AppExitUtil {
    private static AppExitUtil appExitUtil;
    private static PictureAirDbManager pictureAirDbManager;
    private static PWToast newToast;

    private Handler myHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case API1.LOGOUT_FAILED:
                case API1.LOGOUT_SUCCESS:
                    SPUtils.clear(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME);

                    ACache.get(MyApplication.getInstance()).remove(Common.ALL_GOODS);
                    ACache.get(MyApplication.getInstance()).remove(Common.ACACHE_ADDRESS);

                    MyApplication.getInstance().setPushPhotoCount(0);
                    MyApplication.getInstance().setPushViedoCount(0);
                    MyApplication.getInstance().scanMagicFinish = false;
                    MyApplication.getInstance().fragmentStoryLastSelectedTab = 0;
                    pictureAirDbManager.deleteAllInfoFromTable(Common.PHOTOPASS_INFO_TABLE);

                    MyApplication.clearTokenId();

                    //取消通知
                    Intent intent = new Intent(MyApplication.getInstance(), NotificationService.class);
                    intent.putExtra("status", "disconnect");
                    MyApplication.getInstance().startService(intent);

                    Intent i = new Intent(MyApplication.getInstance(), LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MyApplication.getInstance().startActivity(i);

                    AppManager.getInstance().AppExit(MyApplication.getInstance());
                    break;
                case API1.SOCKET_DISCONNECT_FAILED:
                case API1.SOCKET_DISCONNECT_SUCCESS:
                    API1.Logout(myHandler);
                    break;

                default:
                    break;
            }
            return false;
        }
    });

    public static AppExitUtil getInstance() {
        if (appExitUtil == null) {
            appExitUtil = new AppExitUtil();
            pictureAirDbManager = new PictureAirDbManager(MyApplication.getInstance());
            newToast = new PWToast(MyApplication.getInstance());
        }
        return appExitUtil;
    }

    /**
     * App重新登录
     */
    public void AppReLogin() {
        //断开推送
        newToast.setTextAndShow(R.string.please_relogin, Toast.LENGTH_SHORT);
        API1.noticeSocketDisConnect(myHandler);
    }

    /**
     * App登出
     */
    public void AppLogout() {
        //断开推送
        API1.noticeSocketDisConnect(myHandler);
    }
}
