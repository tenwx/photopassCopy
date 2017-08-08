package com.pictureair.photopassCopy.util;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopassCopy.MyApplication;
import com.pictureair.photopassCopy.R;
import com.pictureair.photopassCopy.activity.LoginActivity;
import com.pictureair.photopassCopy.greendao.PictureAirDbManager;
import com.pictureair.photopassCopy.http.rxhttp.RxSubscribe;
import com.pictureair.photopassCopy.service.DownloadService;
import com.pictureair.photopassCopy.service.NotificationService;
import com.pictureair.photopassCopy.widget.PWToast;

import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by milo on 15/12/29.
 * 退出程序操作
 * 1.断开推送
 * 2.清空数据
 */
public class AppExitUtil {
    private static AppExitUtil appExitUtil;

    public static AppExitUtil getInstance() {
        if (appExitUtil == null) {
            appExitUtil = new AppExitUtil();
        }
        return appExitUtil;
    }

    /**
     * App重新登录
     */
    public void AppReLogin() {
        //断开推送
        PWToast.getInstance(MyApplication.getInstance()).setTextAndShow(R.string.please_relogin, Toast.LENGTH_SHORT);
        AppLogout();
    }

    /**
     * App登出
     */
    public void AppLogout() {
        //断开推送
        API2.noticeSocketDisConnect()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {

                    }

                    @Override
                    public void _onError(int status) {
                        Logout();
                    }

                    @Override
                    public void onCompleted() {
                        Logout();
                    }
                });
    }

    private void logoutProcess() {
        exit();

        Intent i = new Intent(MyApplication.getInstance(), LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MyApplication.getInstance().startActivity(i);

        AppManager.getInstance().AppLogout();
    }

    private void Logout() {
        API2.Logout()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {

                    @Override
                    public void onCompleted() {
                        logoutProcess();
                    }

                    @Override
                    public void _onNext(JSONObject jsonObject) {

                    }

                    @Override
                    public void _onError(int status) {
                        logoutProcess();
                    }
                });
    }

    public void exit() {
        SPUtils.clear(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME);

        ACache.get(MyApplication.getInstance()).remove(Common.ALL_GOODS);
        ACache.get(MyApplication.getInstance()).remove(Common.ACACHE_ADDRESS);
        ACache.get(MyApplication.getInstance()).remove(Common.SOUVENIR);

        MyApplication.getInstance().setPushPhotoCount(0);
        MyApplication.getInstance().setPushViedoCount(0);
//        MyApplication.getInstance().scanMagicFinish = false;
        MyApplication.getInstance().fragmentStoryLastSelectedTab = 0;
        PictureAirDbManager.deleteAllInfoFromTable();

        /**
         * 目前有两种状态，因此可以全部删除，如果以后有多种状态，并且有些不需要退出删除，就需要用下面的写法进行单个删除操作
         * PictureAirDbManager.deleteJsonInfosByType(JsonInfo.JSON_LOCATION_PHOTO_TYPE);//退出的时候，清除所有pp信息，一卡一天信息
         * PictureAirDbManager.deleteJsonInfosByType(JsonInfo.DAILY_PP_REFRESH_ALL_TYPE);//退出的时候，清除所有pp信息，pp刷新标记
         */
        PictureAirDbManager.deleteJsonInfos();

        MyApplication.clearTokenId();

        Installation.clearId(MyApplication.getInstance());

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
    }
}
