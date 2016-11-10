package com.pictureair.photopass;

import android.annotation.TargetApi;
import android.app.Application;
import android.graphics.Typeface;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.support.multidex.MultiDex;

import com.pictureair.jni.ciphermanager.PWJniUtil;
import com.pictureair.photopass.receiver.NetBroadCastReciver;
import com.pictureair.photopass.service.NotificationService;
import com.pictureair.photopass.util.AESKeyHelper;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.CrashHandler;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.util.UmengUtil;
import com.pictureair.photopass.widget.CustomFontManager;
import com.pictureair.photopass.widget.FontResource;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * ImageLoader的配置 百度定位的配置 全局数据的共享
 */
public class MyApplication extends Application {
    private static MyApplication instance;
    private static String tokenId;
    private boolean isLogin;
    private boolean needScanPhoto = false;// 判断是否有新的照片被保存，用来扫描更新显示新保存的照片，只针对编辑图片时候的保存
    private int pushPhotoCount = 0;// 推送图片的数量，作为是否刷新的标记
    private int pushViedoCount = 0; // 推送视频的数量。
    private boolean needRefreshPPPList = false;// 记录是否需要更新ppp列表
    public boolean scanMagicFinish = false;// 记录是否已经扫面过magic相册
    public boolean needScanFavoritePhotos = false;//记录是否需要扫描收藏图片

    /**
     * 用来判断是否成功获取了有广告图的地点
     */
    private boolean getADLocationSuccess = false;

    /**
     * 主页tab需要切换的索引值
     */
    private int mainTabIndex = -1;
    private String languageType;// 记录app选择的语言
    public int fragmentStoryLastSelectedTab = 0;// 记录story页面viewpager上次的页面序号

    private String isBuyingPhotoId;
    private String isBuyingTabName;
    private String isBuyingPhotoPassCode;
    private String isBuyingPhotoShootTime;
    private String refreshViewAfterBuyBlurPhoto = "";// 记录是否购买完单张之后刷新页面
    private String buyPPPStatus = "";//记录购买ppp的状态

    public Typeface typeface;//设置默认字体用
    public Typeface typefaceBold;//设置粗字体用

    private boolean isStoryTab = true;//记录是否是处于storyTab

    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private NetBroadCastReciver netBroadCastReciver;


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);//让app支持dex多文件
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (CustomFontManager.IS_CUSOTM_FONT) {
            CalligraphyConfig.initDefault(CustomFontManager.CUSOTM_FONT_NAME, R.attr.fontPath);
            FontResource.getInstance().initFout(this);
            typeface = FontResource.getInstance().loadingFout(this);
            typefaceBold = FontResource.getInstance().loadingBoldFout(this);
        }
        if (!BuildConfig.LOG_DEBUG) {
            CrashHandler handler = CrashHandler.getInstance();
            handler.init(getApplicationContext());
        }
        instance = this;
        registerConnectDector();
        // 初始化友盟
        UmengUtil.initUmeng();
        PictureAirLog.out("application on create--->");
    }

    /**
     * 获取用户标识
     *
     * @return tokenId
     */
    public static String getTokenId() {
        if (tokenId == null) {
            tokenId = AESKeyHelper.decryptString(SPUtils.getString(MyApplication.getInstance().getApplicationContext(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_TOKENID, null),
                    PWJniUtil.getAESKey(Common.APP_TYPE_SHDRPP, 0));
        }
        return tokenId;
    }

    /**
     * 退出登录的时候，需要清空tokenId
     */
    public static void clearTokenId() {
        tokenId = null;
    }

    /**
     * 获取全局Context
     *
     * @return
     */
    public static MyApplication getInstance() {
        return instance;
    }

    /**
     * 判断当前状态是否为登录
     *
     * @return
     */
    public boolean isLogin() {
        return isLogin;
    }

    /**
     * 设置登录状态
     *
     * @param isLogin
     */
    public void setLogin(boolean isLogin) {
        this.isLogin = isLogin;
    }

    /**
     * 判断是否有新的照片被保存
     *
     * @return
     */
    public boolean needScanPhoto() {
        return needScanPhoto;
    }

    /**
     * 设置有新的照片被保存
     *
     * @param needScanPhoto
     */
    public void setneedScanPhoto(boolean needScanPhoto) {
        this.needScanPhoto = needScanPhoto;
    }

    /**
     * 设置推送图片的数量
     *
     * @param count
     */
    public void setPushPhotoCount(int count) {
        PictureAirLog.out("application---set photo count---->" + count);
        this.pushPhotoCount = count;
    }

    /**
     * 获取推送图片的数量
     *
     * @return
     */
    public int getPushPhotoCount() {
        return pushPhotoCount;
    }

    /**
     * 获取是否需要刷新ppplist的状态
     *
     * @return
     */
    public boolean getNeedRefreshPPPList() {
        return needRefreshPPPList;
    }

    /**
     * 设置是否需要刷新ppplist
     *
     * @param state
     */
    public void setNeedRefreshPPPList(boolean state) {
        this.needRefreshPPPList = state;
    }

    /**
     * 获取设置的语言信息
     *
     * @return
     */
    public String getLanguageType() {

        if (languageType == null || languageType.equals("")) {
            languageType = SPUtils.getString(this, Common.SHARED_PREFERENCE_APP, Common.LANGUAGE_TYPE, Common.ENGLISH);
        }
        PictureAirLog.out("language---->" + languageType);
        return languageType;

    }

    /**
     * 设置语言信息
     *
     * @param languageType
     */
    public void setLanguageType(String languageType) {

        this.languageType = languageType;

    }

    /**
     * 记录当前正在购买的photopass的图片信息
     */
    public void setIsBuyingPhotoInfo(String photoId, String tabName, String isBuyingPhotoPassCode, String isBuyingPhotoShootTime) {
        this.isBuyingPhotoId = photoId;
        this.isBuyingTabName = tabName;
        this.isBuyingPhotoPassCode = isBuyingPhotoPassCode;
        this.isBuyingPhotoShootTime = isBuyingPhotoShootTime;
    }

    /**
     * 获取当前正在购买的photopass的photoId
     *
     * @return
     */
    public String getIsBuyingPhotoId() {
        return isBuyingPhotoId;
    }

    /**
     * 获取当前正在购买的photopass的tabName
     *
     * @return
     */
    public String getIsBuyingTabName() {
        return isBuyingTabName;
    }

    /**
     * 获取当前正在购买的photopass的code
     * @return
     */
    public String getIsBuyingPhotoPassCode() {
        return isBuyingPhotoPassCode;
    }

    /**
     * 获取当前正在购买的photopass的对应时间
     * @return
     */
    public String getIsBuyingPhotoShootTime() {
        return isBuyingPhotoShootTime;
    }

    /**
     * 清空正在购买的图片信息
     */
    public void clearIsBuyingPhotoList() {
        isBuyingPhotoId = null;
        isBuyingTabName = null;
        isBuyingPhotoPassCode = null;
        isBuyingPhotoShootTime = null;
    }


    /**
     * 设置对应的值
     *
     * @param refreshViewAfterBuyBlurPhoto
     */
    public void setRefreshViewAfterBuyBlurPhoto(String refreshViewAfterBuyBlurPhoto) {
        this.refreshViewAfterBuyBlurPhoto = refreshViewAfterBuyBlurPhoto;
    }

    /**
     * 获取是否刷新的值
     *
     * @return
     */
    public String getRefreshViewAfterBuyBlurPhoto() {
        return refreshViewAfterBuyBlurPhoto;
    }

    /**
     * 获取当前购买ppp的状态
     * @return
     */
    public String getBuyPPPStatus() {
        return buyPPPStatus;
    }

    /**
     * 设置当前购买ppp的状态
     * @param buyPPPStatus
     */
    public void setBuyPPPStatus(String buyPPPStatus) {
        this.buyPPPStatus = buyPPPStatus;
    }

    /**
     * 获取是否处于storyTab页面
     *
     * @return
     */
    public boolean isStoryTab() {
        return isStoryTab;
    }

    /**
     * 设置是否处于storyTab页面
     *
     * @param isStoryTab
     */
    public void setIsStoryTab(boolean isStoryTab) {
        this.isStoryTab = isStoryTab;
    }

    /**
     * 获取视频推送的数量
     *
     * @return
     */
    public int getPushViedoCount() {
        return pushViedoCount;
    }

    /**
     * 设置视频推送的数量
     *
     * @return
     */
    public void setPushViedoCount(int pushViedoCount) {
        this.pushViedoCount = pushViedoCount;
    }

    /**
     * 获取需要切换的值
     *
     * @return
     */
    public int getMainTabIndex() {
        return mainTabIndex;
    }

    /**
     * 设置需要切换的值
     *
     * @param mainTabIndex
     */
    public void setMainTabIndex(int mainTabIndex) {
        this.mainTabIndex = mainTabIndex;
    }

    /**
     * 获取是否成功拿到广告地点信息
     *
     * @return
     */
    public boolean isGetADLocationSuccess() {
        return getADLocationSuccess;
    }

    /**
     * 设置是否成功拿到广告地点信息
     *
     * @param getADLocationSuccess
     */
    public void setGetADLocationSuccess(boolean getADLocationSuccess) {
        this.getADLocationSuccess = getADLocationSuccess;
    }

    /**
     * 读取粗字体
     */
    public Typeface getFontBold() {
        if (getLanguageType() != null && getLanguageType().equals(Common.SIMPLE_CHINESE)) {//如果简体中文，使用默认字体
            return Typeface.DEFAULT_BOLD;
        }
        if (null == typefaceBold) {
            typefaceBold = Typeface.createFromAsset(getAssets(), CustomFontManager.CUSOTM_FONT_BOLD_NAME);
        }
        return typefaceBold;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void registerNetWorkRequest() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkRequest.Builder builder = new NetworkRequest.Builder();

        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED);
        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);

        NetworkRequest networkRequest = builder.build();

        networkCallback = new ConnectivityManager.NetworkCallback() {

            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                PictureAirLog.d(" ＝＝＝＝＝ ", "有网络连接");
                Intent intent = new Intent(MyApplication.getInstance(),
                        com.pictureair.photopass.service.NotificationService.class);
                MyApplication.getInstance().startService(intent);
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                PictureAirLog.d(" ＝＝＝＝＝ ", "无网络连接");
                Intent intent = new Intent(MyApplication.getInstance(), NotificationService.class);
                intent.putExtra("status", "disconnect");
                MyApplication.getInstance().startService(intent);
            }
        };
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void unRegisterNetWorkRequest() {
        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    private void registerConnectivityReceiver() {
        netBroadCastReciver = new NetBroadCastReciver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netBroadCastReciver, filter);
    }

    private void unRegisterConnectivityReceiver() {
        if (netBroadCastReciver != null) {
            unregisterReceiver(netBroadCastReciver);
        }
    }

    private void registerConnectDector() {
        if (Build.VERSION.SDK_INT > 21) {
            registerNetWorkRequest();
        } else {
            registerConnectivityReceiver();
        }
    }

    private void unregisterConnectDector() {
        if (Build.VERSION.SDK_INT > 21) {
            unRegisterNetWorkRequest();
        } else {
            unRegisterConnectivityReceiver();
        }
    }

    @Override
    public void onTerminate() {
        unregisterConnectDector();
        super.onTerminate();
    }

    @Override
    public Resources getResources() {
        Resources resources =  super.getResources();
        Configuration oriCOnfig = resources.getConfiguration();
        oriCOnfig.fontScale = 1f;
        resources.updateConfiguration(oriCOnfig, resources.getDisplayMetrics());
        return resources;
    }
}
