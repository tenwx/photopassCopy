package com.pictureworks.android.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.pictureair.jni.ciphermanager.PWJniUtil;
import com.pictureworks.android.R;
import com.pictureworks.android.db.PictureAirDbManager;
import com.pictureworks.android.widget.MyToast;


/**
 * Created by milo on 15/12/29.
 * 退出程序操作
 * 1.断开推送
 * 2.清空数据
 */
public class AppExitUtil {
    private static AppExitUtil appExitUtil;
    private static PictureAirDbManager pictureAirDbManager;
    private static boolean isAppExit = false;//防止程序一直logout
    private static MyToast newToast;
    private static Context mContext;
    private static LogoutListener listener;
    private Handler myHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case API1.LOGOUT_FAILED:
                case API1.LOGOUT_SUCCESS:
                    SharedPreferences sp = mContext.getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.clear();
                    editor.commit();

                    ACache.get(mContext).remove(Common.TOP_GOODS);
                    ACache.get(mContext).remove(Common.ALL_GOODS);
                    ACache.get(mContext).remove(Common.ACACHE_ADDRESS);
                    ACache.get(mContext).remove(Common.BANNER_GOODS);
                    ACache.get(mContext).remove(Common.PPP_GOOD);
                    ACache.get(mContext).remove(Common.LOCATION_INFO);

                    pictureAirDbManager.deleteAllInfoFromTable(Common.PHOTOPASS_INFO_TABLE);

                    isAppExit = true;

                    listener.send(API1.LOGOUT_SUCCESS);
                    break;
                case API1.SOCKET_DISCONNECT_FAILED:
                case API1.SOCKET_DISCONNECT_SUCCESS:
                    API1.Logout(mContext.getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, Context.MODE_PRIVATE).getString(Common.USERINFO_TOKENID, null),myHandler);
                    break;

                default:
                    break;
            }
            return false;
        }
    });

    public AppExitUtil (Context context, String appType, LogoutListener listener) {
        pictureAirDbManager = new PictureAirDbManager(context, PWJniUtil.getSqlCipherKey(appType));
        newToast = new MyToast(context);
        mContext = context;
        this.listener = listener;
    }

    public static AppExitUtil getInstance() {
        if (appExitUtil == null) {
            appExitUtil = new AppExitUtil();
        }
        return appExitUtil;
    }

    public AppExitUtil () {
    }

    /**
     * App重新登录
     */
    public void AppReLogin() {
        //断开推送
        newToast.setTextAndShow(R.string.please_relogin, Toast.LENGTH_SHORT);
        isAppExit = true;
        API1.noticeSocketDisConnect(mContext.getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, Context.MODE_PRIVATE).getString(Common.USERINFO_TOKENID, null),myHandler);
    }

    /**
     * App登出
     */
    public void AppLogout() {
        //断开推送
        isAppExit = true;
        API1.noticeSocketDisConnect(mContext.getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, Context.MODE_PRIVATE).getString(Common.USERINFO_TOKENID, null), myHandler);
    }

    /**
     * app登录
     */
    public void AppLogin(){
        isAppExit = false;
    }


    public boolean isAppExit(){
        return isAppExit;
    }

    /**
     * 定义接口
     */
    public interface LogoutListener{
        void send(int what);
    }
}
