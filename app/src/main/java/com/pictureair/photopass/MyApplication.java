package com.pictureair.photopass;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.pictureair.jni.ciphermanager.PWJniUtil;
import com.pictureair.photopass.util.AESKeyHelper;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.CrashHandler;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.UmengUtil;
import com.pictureair.photopass.widget.CustomFontManager;
import com.pictureair.photopass.widget.FontResource;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

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
    //    public ArrayList<PhotoInfo> photoPassPicList;// 所有的从服务器返回的photopass图片的信息
//    public ArrayList<PhotoInfo> photoPassVideoList;// 所有的从服务器返回的photopass图片的信息
//    public ArrayList<PhotoInfo> magicPicList;// 所有的使用magic相机拍出来的图片的信息
//    public ArrayList<PhotoItemInfo> boughtPicList;// 所有已经购买的图片的信息
//    public ArrayList<PhotoItemInfo> allPicList;// 所有的图片信息
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
    private int mainTabIndex = 0;
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

    private boolean needRefreshOldPhotos;//不同设备之间同步，是否需要刷新之前未购买的图片
    public Typeface typeface;//设置默认字体用
    public Typeface typefaceBold;//设置粗字体用

    private boolean isStoryTab = true;//记录是否是处于storyTab


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
        // 初始化友盟
        UmengUtil.initUmeng();
        initImageLoader(getApplicationContext());
        codeList = new ArrayList<HashMap<String, String>>();
        PictureAirLog.out("application on create--->");
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

    /**
     * 判断启动的进程是否为主进程，防止因为百度定位的独立进程启动导致onCreate被执行两次
     *
     * @param context
     * @return String
     */
    private String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }


    /**
     * 初始化imageLoader
     *
     * @param context
     */
    private static void initImageLoader(Context context) {
        // File parent = new
        // File(Environment.getExternalStorageDirectory().getPath() +
        // "/pictureAir/cache/image2/");
        // if (!parent.exists()) {
        // parent.mkdirs();
        // }
        // File cacheDir = StorageUtils.getOwnCacheDirectory(context,
        // "/pictureAir/cache/images/");
        File cacheDir = StorageUtils.getCacheDirectory(context);
        ;
        // displayImage(...) call if no options will be passed to this method
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .considerExifParams(true)
                .// 考虑图片的exif属性
                // showStubImage(imageRes)//图片下载期间显示的图片
                // .showImageForEmptyUri(R.drawable.ic_empty) //
                // 设置图片Uri为空或是错误的时候显示的图片
                        showImageOnLoading(R.drawable.ic_loading)
                .showImageOnFail(R.drawable.ic_failed)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context)
                .memoryCacheExtraOptions(1000, 1000)
                // max width, max height，即保存的每个缓存文件的最大长宽
                .threadPoolSize(4)
                // 线程池内加载的数量
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCacheSize(5 * 1024 * 1024)
                // memoryCache(...)和memoryCacheSize(...)这两个参数会互相覆盖，所以在ImageLoaderConfiguration中使用一个就好了
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                // 将保存的时候的URI名称用MD5 加密
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                // .diskCacheFileCount(100)
                // 缓存的文件数量
                // .diskCache(new UnlimitedDiscCache(cacheDir))
                // 自定义缓存路径
                // UnlimitedDiskCache 不限制缓存大小（默认）
                // TotalSizeLimitedDiskCache (设置总缓存大小，超过时删除最久之前的缓存)
                // FileCountLimitedDiskCache
                // (设置总缓存文件数量，当到达警戒值时，删除最久之前的缓存。如果文件的大小都一样的时候，可以使用该模式)
                // LimitedAgeDiskCache (不限制缓存大小，但是设置缓存时间，到期后删除)
                .diskCacheSize(30 * 1024 * 1024)
                // 50m本地缓存
                .defaultDisplayImageOptions(defaultOptions)
                .imageDownloader(
                        new BaseImageDownloader(context, 5 * 1000, 30 * 1000)) // connectTimeout
                .build();// 开始构建
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
        // ImageLoader.getInstance().clearDiskCache();
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
     * 得到时候需要刷新原有的数据
     *
     * @return
     */
    public boolean isNeedRefreshOldPhotos() {
        return needRefreshOldPhotos;
    }

    /**
     * 设置是否需要刷新原有的数据
     *
     * @param needRefreshOldPhotos
     */
    public void setNeedRefreshOldPhotos(boolean needRefreshOldPhotos) {
        this.needRefreshOldPhotos = needRefreshOldPhotos;
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
