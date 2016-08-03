package com.pictureair.photopass;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.util.DisplayMetrics;

import com.pictureair.jni.ciphermanager.PWJniUtil;
import com.pictureair.photopass.util.AESKeyHelper;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.CrashHandler;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.UmengUtil;
import com.pictureair.photopass.widget.CustomFontManager;
import com.pictureair.photopass.widget.FontResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * ImageLoader的配置 百度定位的配置 全局数据的共享
 */
public class MyApplication extends Application {
    private static MyApplication instance;
    private static SharedPreferences userInfosharedPreferences, appSP;
    private static String tokenId;
    private boolean isLogin;
    private boolean needScanPhoto = false;// 判断是否有新的照片被保存，用来扫描更新显示新保存的照片，只针对编辑图片时候的保存
    private int pushPhotoCount = 0;// 推送图片的数量，作为是否刷新的标记
    private int pushViedoCount = 0; // 推送视频的数量。
    private ArrayList<HashMap<String, String>> codeList;// 记录登录之前扫描的pp或者ppp
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
    // private HashMap<String, Boolean> isBuyingPhotoFromAlbumHashMap = new
    // HashMap<String,
    // Boolean>();//记录是否从相册页面购买的单张照片（此处的相册有两处，第一次ViewOrSelectActivity页面，第二处是LocationPhotosAct）
    private String refreshViewAfterBuyBlurPhoto = "";// 记录是否购买完单张之后刷新页面
    private boolean photoIsPaid; // 购买照片之后，返回photo页面。
    // onCreate方法不建议写耗时的操作

//    private boolean needRefreshOldPhotos;//不同设备之间同步，是否需要刷新之前未购买的图片
    public Typeface typeface;//设置默认字体用
    public Typeface typefaceBold;//设置粗字体用

    private boolean isStoryTab = true;//记录是否是处于storyTab

    private Configuration config;
    private DisplayMetrics displayMetrics;


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
        userInfosharedPreferences = this.getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, Context.MODE_PRIVATE);
        appSP = this.getSharedPreferences(Common.SHARED_PREFERENCE_APP, Context.MODE_PRIVATE);
        initLanguage();
        // 初始化友盟
        UmengUtil.initUmeng();
        codeList = new ArrayList<>();
        PictureAirLog.out("application on create--->");
    }

    private void initLanguage(){
        config = getResources().getConfiguration();
        displayMetrics = getResources().getDisplayMetrics();
        //获取手机设置的语言
        languageType = appSP.getString(Common.LANGUAGE_TYPE, "");
        if (!languageType.equals("")) {//语言不为空
            if (languageType.equals(Common.ENGLISH)) {
                config.locale = Locale.US;
            } else if (languageType.equals(Common.SIMPLE_CHINESE)) {
                config.locale = Locale.SIMPLIFIED_CHINESE;
            }
        } else {//语言为空，说明第一次进入
            PictureAirLog.out("langeuange is null---->" + config.locale.getLanguage());
            PictureAirLog.out("langeuange is null---->" + config.locale);
            if (config.locale.getLanguage().equals(Common.SIMPLE_CHINESE)) {
                languageType = Common.SIMPLE_CHINESE;
                config.locale = Locale.SIMPLIFIED_CHINESE;
            } else {
                languageType = Common.ENGLISH;
                config.locale = Locale.US;
            }
        }
        getResources().updateConfiguration(config, displayMetrics);
        SharedPreferences.Editor editor = appSP.edit();
        editor.putString(Common.LANGUAGE_TYPE, languageType);
        editor.commit();
    }

    /**
     * 获取用户标识
     *
     * @return tokenId
     */
    public static String getTokenId() {
        if (tokenId == null) {
            tokenId = AESKeyHelper.decryptString(userInfosharedPreferences.getString(Common.USERINFO_TOKENID, null), PWJniUtil.getAESKey(Common.APP_TYPE_SHDRPP, 0));
        }
        return tokenId;
    }

    /**
     * 退出登录的时候，需要清空tokenId
     */
    public static void clearTokenId() {
        tokenId = null;
    }

    /*
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
     * 获取登录前扫描pp或者ppp数据的个数
     *
     * @return
     */
    public int getCodeListSize() {
        return codeList.size();
    }

    /**
     * 获取已扫描的对象
     *
     * @param position
     * @return
     */
    public HashMap<String, String> getCodeListItem(int position) {
        return codeList.get(position);

    }

    /**
     * 添加已扫描的卡的信息
     *
     * @param obj
     */
    public void addObject2CodeList(HashMap<String, String> obj) {
        codeList.add(obj);
    }

    /**
     * 清除存放已扫描卡得列表
     */
    public void clearCodeList() {
        codeList.clear();
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
            languageType = appSP.getString(Common.LANGUAGE_TYPE, Common.ENGLISH);
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
    public void setIsBuyingPhotoInfo(String photoId, String tabName) {
        this.isBuyingPhotoId = photoId;
        this.isBuyingTabName = tabName;
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
     * 清空正在购买的图片信息
     */
    public void clearIsBuyingPhotoList() {
        isBuyingPhotoId = null;
        isBuyingTabName = null;
    }


    /**
     * 设置对应的值
     *
     * @param refreshViewAfterBuyBlurPhoto
     */
    public void setRefreshViewAfterBuyBlurPhoto(
            String refreshViewAfterBuyBlurPhoto) {
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
     * 获取图片是否已经购买
     *
     * @return
     */
    public boolean isPhotoIsPaid() {
        return photoIsPaid;
    }

    /**
     * 设置图片是否已经购买
     *
     * @param photoIsPaid
     */
    public void setPhotoIsPaid(boolean photoIsPaid) {
        this.photoIsPaid = photoIsPaid;
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
        if (getLanguageType() != null && getLanguageType().equals("cn")) {
            return Typeface.DEFAULT_BOLD;
        }
        if (null == typefaceBold) {
            typefaceBold = Typeface.createFromAsset(getAssets(), CustomFontManager.CUSOTM_FONT_BOLD_NAME);
        }
        return typefaceBold;
    }
}
