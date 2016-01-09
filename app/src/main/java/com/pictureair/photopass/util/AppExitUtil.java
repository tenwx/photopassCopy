package com.pictureair.photopass.util;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.activity.LoginActivity;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.service.NotificationService;

/**
 * Created by milo on 15/12/29.
 * 退出程序操作
 * 1.断开推送
 * 2.清空数据
 */
public class AppExitUtil {
    private static AppExitUtil appExitUtil;
    private static PictureAirDbManager pictureAirDbManager;

    public static AppExitUtil getInstance() {
        if (appExitUtil == null) {
            appExitUtil = new AppExitUtil();
            pictureAirDbManager = new PictureAirDbManager(MyApplication.getInstance());
        }
        return appExitUtil;
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case API1.LOGOUT_FAILED:
                case API1.LOGOUT_SUCCESS:
                    SharedPreferences sp = MyApplication.getInstance().getSharedPreferences(Common.USERINFO_NAME, MyApplication.getInstance().MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.clear();
                    editor.commit();

                    ACache.get(MyApplication.getInstance()).remove(Common.TOP_GOODS);
                    ACache.get(MyApplication.getInstance()).remove(Common.ALL_GOODS);
                    ACache.get(MyApplication.getInstance()).remove(Common.ACACHE_ADDRESS);
                    ACache.get(MyApplication.getInstance()).remove(Common.BANNER_GOODS);
                    ACache.get(MyApplication.getInstance()).remove(Common.PPP_GOOD);
                    ACache.get(MyApplication.getInstance()).remove(Common.LOCATION_INFO);

                    MyApplication.getInstance().photoPassPicList.clear();
                    MyApplication.getInstance().setPushPhotoCount(0);
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
                    API1.Logout(handler);
                    break;

                default:
                    break;
            }
        }

        ;
    };

    /**
     * App重新登录
     */
    public void AppReLogin() {
        //断开推送
        API1.noticeSocketDisConnect(handler);
    }

    /**
     * App登出
     */
    public void AppLogout() {
        //断开推送
        API1.noticeSocketDisConnect(handler);
    }
}
