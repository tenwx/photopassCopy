package com.pictureair.photopass;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.pictureair.photopass.activity.CrashHandler;
import com.pictureair.photopass.entity.PPinfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.entity.PhotoItemInfo;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.UmengUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * ImageLoader的配置 百度定位的配置 全局数据的共享
 */
public class MyApplication extends Application {
    // public LocationClient mLocationClient;
    // public BDLocation mLocation;
    private static MyApplication instance;
    private static SharedPreferences userInfosharedPreferences;
    private boolean isLogin;
    private boolean needScanPhoto = false;// 判断是否有新的照片被保存，用来扫描更新显示新保存的照片，只针对编辑图片时候的保存
    private int pushPhotoCount = 0;// 推送图片的数量，作为是否刷新的标记
    private int pushViedoCount = 0; // 推送视频的数量。
    private ArrayList<HashMap<String, String>> codeList;// 记录登录之前扫描的pp或者ppp
    public ArrayList<PhotoInfo> photoPassPicList;// 所有的从服务器返回的photopass图片的信息
    public ArrayList<PhotoInfo> photoPassVideoList;// 所有的从服务器返回的photopass图片的信息
    public ArrayList<PhotoInfo> magicPicList;// 所有的使用magic相机拍出来的图片的信息
    public ArrayList<PhotoItemInfo> boughtPicList;// 所有已经购买的图片的信息
    public ArrayList<PhotoItemInfo> allPicList;// 所有的图片信息
    public ArrayList<PPinfo> photoPassCodeList;// 保存所有的pp信息
    // private boolean locationIsRunning = false;//当前定位是否在运行之中
    private boolean needRefreshPPPList = false;// 记录是否需要更新ppp列表
    public boolean scanMagicFinish = false;// 记录是否已经扫面过magic相册
    private String languageType;// 记录app选择的语言
    public int fragmentStoryLastSelectedTab = 0;// 记录story页面viewpager上次的页面序号

    private int last_tab = 0;

    private ArrayList<PhotoInfo> isBuyingPhotoInfoList;// 记录正在购买的全部照片信息
    private int isBuyingIndex;// 记录正在购买的图片的索引值
    // private HashMap<String, Boolean> isBuyingPhotoFromAlbumHashMap = new
    // HashMap<String,
    // Boolean>();//记录是否从相册页面购买的单张照片（此处的相册有两处，第一次ViewOrSelectActivity页面，第二处是LocationPhotosAct）
    private String refreshViewAfterBuyBlurPhoto = "";// 记录是否购买完单张之后刷新页面
    private boolean photoIsPaid; // 购买照片之后，返回photo页面。
    // onCreate方法不建议写耗时的操作

    // 是否开启debug模式，如果为true，打印log，如果为false，不打印log
    public final static boolean DEBUG = true;
    private String upgradedPhotosMessage; // 接收升级或者购买单张照片时服务器传过来的值。

    @Override
    public void onCreate() {
        super.onCreate();
        if (!DEBUG) {
            CrashHandler handler = CrashHandler.getInstance();
            handler.init(getApplicationContext());
        }
        instance = this;
        userInfosharedPreferences = this.getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
        // 初始化友盟
        UmengUtil.initUmeng();
        if (getCurProcessName(getApplicationContext()).equals(
                Common.PACKGE_NAME)) {
            System.err.println("application on create----->");
            initImageLoader(getApplicationContext());
            codeList = new ArrayList<HashMap<String, String>>();
            photoPassPicList = new ArrayList<PhotoInfo>();
            photoPassVideoList = new ArrayList<>();
            allPicList = new ArrayList<PhotoItemInfo>();
            magicPicList = new ArrayList<PhotoInfo>();
            boughtPicList = new ArrayList<PhotoItemInfo>();
            photoPassCodeList = new ArrayList<PPinfo>();
        } else {
            System.err.println("application not on create------>");
        }
    }

    /**
     * 获取用户标识
     *
     * @return tokenId
     */
    public static String getTokenId() {
        String tokenId = userInfosharedPreferences.getString(Common.USERINFO_TOKENID, null);
        return tokenId;
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

    // public void InitLocation() {
    // SDKInitializer.initialize(getApplicationContext());
    // mLocationClient = new LocationClient(getApplicationContext());
    // LocationClientOption option = new LocationClientOption();
    // mLocationClient.registerLocationListener(new BDLocationListener() {
    // @Override
    // // TODO Auto-generated method stub
    // public void onReceiveLocation(BDLocation location) {
    // if (location == null) {
    // return;
    // }
    // mLocation = location;
    // Log.d("百度定位", location.getLongitude()+"+++++++"+location.getLatitude());
    // /**
    // * 61 ： GPS定位结果 62 ： 扫描整合定位依据失败。此时定位结果无效。 63 ：
    // * 网络异常，没有成功向服务器发起请求。此时定位结果无效。 65 ： 定位缓存的结果。 66 ：
    // * 离线定位结果。通过requestOfflineLocaiton调用时对应的返回结果 67 ：
    // * 离线定位失败。通过requestOfflineLocaiton调用时对应的返回结果 68 ：
    // * 网络连接失败时，查找本地离线定位时对应的返回结果 161： 表示网络定位结果 162~167： 服务端定位失败
    // * 502：key参数错误 505：key不存在或者非法 601：key服务被开发者自己禁用 602：key mcode不匹配
    // * 501～700：key验证失败
    // */
    // }
    // });
    // option.setLocationMode(LocationMode.Hight_Accuracy); // 高精度定位
    // option.setCoorType("bd09ll");
    // option.setScanSpan(2000);
    // option.setOpenGps(true);
    // option.setProdName("PhotoPass");
    // option.setNeedDeviceDirect(false); // 不需要方向
    // option.setIsNeedAddress(true);
    // mLocationClient.setLocOption(option);
    // // mLocationClient.start();
    // // if (mLocationClient.isStarted()) {
    // // System.out.println("请求定位");
    // // mLocationClient.requestLocation();
    // // }else {
    // // System.out.println("不请求定位");
    // // }
    // }

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
                .showImageOnFail(R.drawable.ic_failed).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context)
                .memoryCacheExtraOptions(1000, 1000)
                        // max width, max height，即保存的每个缓存文件的最大长宽
                .threadPoolSize(4)
                        // 线程池内加载的数量
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCacheSize(2 * 1024 * 1024)
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
                .diskCacheSize(50 * 1024 * 1024)
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
        if (instance == null) {
            instance = new MyApplication();
        }
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
     *
     * @param isBuyingPhotoInfoList
     * @param isBuyingIndex
     */
    public void setIsBuyingPhotoInfo(
            ArrayList<PhotoInfo> isBuyingPhotoInfoList, int isBuyingIndex) {
        this.isBuyingPhotoInfoList = isBuyingPhotoInfoList;
        this.isBuyingIndex = isBuyingIndex;
    }

    /**
     * 获取当前正在购买的photopass的图片信息
     *
     * @return
     */
    public ArrayList<PhotoInfo> getIsBuyingPhotoInfoList() {
        return isBuyingPhotoInfoList;
    }

    /**
     * 清空正在购买的图片信息
     */
    public void clearIsBuyingPhotoList() {
        if (isBuyingPhotoInfoList != null) {
            isBuyingPhotoInfoList.clear();
        }
        isBuyingIndex = -1;
    }

    /**
     * 获取当前正在购买的图片索引值
     *
     * @return
     */
    public int getIsBuyingIndex() {
        return isBuyingIndex;
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
     * 获取mainTab上一个页面
     *
     * @return
     */
    public int getLast_tab() {
        return last_tab;
    }

    /**
     * 设置mainTab上一个页面
     *
     * @param last_tab
     */
    public void setLast_tab(int last_tab) {
        this.last_tab = last_tab;
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
     * 获取视频推送的数量
     * @return
     */
    public int getPushViedoCount() {
        return pushViedoCount;
    }

    /**
     * 设置视频推送的数量
     * @return
     */
    public void setPushViedoCount(int pushViedoCount) {
        this.pushViedoCount = pushViedoCount;
    }

    public String getUpgradedPhotosMessage() {
        return upgradedPhotosMessage;
    }

    /**
     * 购买照片或者升级PP+后，设置服务器传过来的值。通过该值判断。
     * 升级PP+后的值：{"c":{"customerId":"DPPPTV9BH3U4Z2WS","shootDate":"2015-12-18"}}
     * 购买照片过后的返回值：{"c":{"id":"*******"}  具体需要测试后才知道。
     * @param upgradedPhotosMessage
     */
    public void setUpgradedPhotosMessage(String upgradedPhotosMessage) {
        this.upgradedPhotosMessage = upgradedPhotosMessage;
    }
}
