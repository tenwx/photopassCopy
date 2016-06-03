package com.pictureair.photopass.util;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.LoginActivity;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.service.NotificationService;
import com.pictureair.photopass.widget.PWToast;

import java.lang.ref.WeakReference;

/**
 * Created by milo on 15/12/29.
 * 退出程序操作
 * 1.断开推送
 * 2.清空数据
 */
public class AppExitUtil {
    private static AppExitUtil appExitUtil;
    private static PictureAirDbManager pictureAirDbManager;
    public static boolean isAppExit = false;//防止程序一直logout
    private static PWToast newToast;
    private MyHandler myHandler = new MyHandler(MyApplication.getInstance());

    public static AppExitUtil getInstance() {
        if (appExitUtil == null) {
            appExitUtil = new AppExitUtil();
            pictureAirDbManager = new PictureAirDbManager(MyApplication.getInstance());
            newToast = new PWToast(MyApplication.getInstance());
        }
        return appExitUtil;
    }

    public class MyHandler extends Handler {
        WeakReference<MyApplication> myApplication;

        MyHandler(MyApplication application) {
            myApplication = new WeakReference<>(application);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (myApplication == null) {
                return;
            }
            switch (msg.what) {
                case API1.LOGOUT_FAILED:
                case API1.LOGOUT_SUCCESS:
                    SharedPreferences sp = MyApplication.getInstance().getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, MyApplication.getInstance().MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.clear();
                    editor.commit();

                    ACache.get(MyApplication.getInstance()).remove(Common.TOP_GOODS);
                    ACache.get(MyApplication.getInstance()).remove(Common.ALL_GOODS);
                    ACache.get(MyApplication.getInstance()).remove(Common.ACACHE_ADDRESS);
                    ACache.get(MyApplication.getInstance()).remove(Common.BANNER_GOODS);
                    ACache.get(MyApplication.getInstance()).remove(Common.PPP_GOOD);
                    ACache.get(MyApplication.getInstance()).remove(Common.LOCATION_INFO);

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
                    isAppExit = true;

                    AppManager.getInstance().AppExit(MyApplication.getInstance());
                    break;
                case API1.SOCKET_DISCONNECT_FAILED:
                case API1.SOCKET_DISCONNECT_SUCCESS:
                    API1.Logout(myHandler);
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * App重新登录
     */
    public void AppReLogin() {
        //断开推送
        newToast.setTextAndShow(R.string.please_relogin, Toast.LENGTH_SHORT);
        isAppExit = true;
        API1.noticeSocketDisConnect(myHandler);
    }

    /**
     * App登出
     */
    public void AppLogout() {
        //断开推送
        isAppExit = true;
        API1.noticeSocketDisConnect(myHandler);
    }

    /**
     * app登录
     */
    public void AppLogin(){
        isAppExit = false;
    }
}
