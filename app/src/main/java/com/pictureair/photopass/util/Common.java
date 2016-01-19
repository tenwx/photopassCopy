package com.pictureair.photopass.util;

import android.os.Environment;

/**
 * 常量类
 */
public class Common {
    public static final String APP_NAME = "appName";
    public static final String APP_KEY = "photoPass";
    public static final String APP_SECRET = "pictureworks";
    public static final String APPLICATION_NAME = "photoPass";// app英文名字
    public static final String SHARE_APP_NAME = "PhotoPass";
    public static final String CHECK_VERSION = "/version/checkVersion";// 检查更新
    public static final String PACKGE_NAME = "com.pictureair.photopass";// 应用包名
    public static final String SMS_APPKEY = "d082b4315aed"; // SMSS，公司的账号
    public static final String SMS_APPSECRET = "0cdee8a0b0b554574a0ada56bf02c650"; // SMSS

    /**
     * 49演示服务器
     */
//    public static final String BASE_URL_TEST = "http://192.168.8.49:3006";
//    public static final String PHOTO_URL = "http://192.168.8.82:4000/";
//    public static final String ALIPAY_NOTIFY = "http://211.95.27.36:3006/api/alipayNotify";//alipay的异步通知URL
//    public static final String WECHAT_NOTIFY = "http://211.95.27.36:3006/api/weChatNotify";//wechat的异步通知URL
//    public static final String GET_UNIONPAY_TN = "/api/";//获取unionpay的tn
//    public static final String BARCODEURL = "http://140.206.125.194:3001/downloadApp.html?";
//    public final static boolean DEBUG = false;// false，不打印log
//    public final static String VERSION_CODE = "V1.1.0";//内测版

    /**
     * 8.3测试服务器
     */
    public static final String BASE_URL_TEST = "http://192.168.8.3:3006";
    public static final String PHOTO_URL = "http://192.168.8.3:4000/";
    public static final String ALIPAY_NOTIFY = "http://211.95.27.34:3006/api/alipayNotify";//alipay的异步通知URL
    public static final String WECHAT_NOTIFY = "http://211.95.27.34:3006/api/weChatNotify";//wechat的异步通知URL
    public static final String BARCODEURL = "http://140.206.125.194:3001/downloadApp.html?";
    public static final String GET_UNIONPAY_TN = "/api/";//获取unionpay的tn
    public final static boolean DEBUG = true;// true，打印log
    public final static String VERSION_CODE = "V1.2.1";//开发版


    //接口
    public static final String GET_TOKENID = "/auth/getTokenId";// 获取匿名TokenId
    public static final String GET_ALL_LOCATIONS_OF_ALBUM_GROUP = "/park/getLocationsOfAlbumGroup";//获得location信息
    public static final String IS_EXIST_PHONE = "";//判断手机号是否已经被注册
    public static final String GET_LASTEST_CONTENT = "/asset/getLatestContent";// 获取最新更新的内容
    public static final String LOGIN = "/auth/login";// 登录
    public static final String LOGOUT = "/auth/logout";// 登出
    public static final String REGISTER = "/user/register";// 注册
    public static final String MODIFYPWD = "/user/modifyUserPwd";//修改密码
    public static final String DOWNLOAD_PHOTO = "/p/downloadPhotos";//下载照片
    public static final String UPDATE_PROFILE = "/user/updateProfile";// 个人信息更新
    public static final String SET_USER_PHOTO = "/user/updateUserImage";//上传用户头像或背景图
    public static final String ADD_PHOTOPASS_TO_PHOTOPASSPLUS = "/ppp/addPhotopassToPhotopassPlus";// 绑定pp到ppp
    public static final String CHECK_CODE_AVAILABLE = "/ppp/checkCodeAvailable";//获得code类型
    public static final String ADD_CODE_TO_USER = "/user/addCodeToUser";// 将PP绑定到当前用户
    public static final String BIND_PPP_TO_USER = "/ppp/bindPPPToUser";// 将PPP绑定到当前用户
    public static final String GET_LATEST_PHOTOS = "/p/getLatestPhotos";// 获取用户最新的照片
    public static final String GET_PHOTOS_BY_CONDITIONS = "/p/getPhotosByConditions";// 获取用户的照片
    public static final String GET_VIDEO_LIST = "/video/getVideoList";//获取视频信息
    public static final String GET_LATEST_PHOTO_BY_PP = "/p/getLatestPhotosByPP";// 获取pp最新的照片
    public static final String GET_FAVORITE_LOCATIONS = "/user/getFavoriteLocations";// 获取用户收藏的location
    public static final String EDIT_FAVORITE_LOCATION = "/user/editFavoriteLocation";// 编辑收藏的location信息
    public static final String GET_PHOTO = "/p/getPhoto";// 根据照片Id获取相片信息
    public static final String ADD_COMMENT = "/p/addCpmment";// 评论照片
    public static final String SHARE_TO_SOCIAL = "/p/shareToSocial";// 分享照片
    public static final String FORGET_PWD = "/user/forgotPwd";// 忘记密码（手机号）


    public static final String ADD_LIKE = "/p/addLike";// 照片点赞
    public static final String SAVE_PHOTO = "/p/savePhto";// 保存编辑后的照片
    public static final String GET_PPPS_BY_USERID = "/ppp/getPPPsByUserId";// 获取用户的ppp列表
    public static final String GET_PPS_BY_USERID = "/user/getPPsByUserId";// 获取用户的pp列表
    public static final String GET_PPPS_BY_PP_AND_DATE = "/ppp/getPPPsByPPAndDate";// 获取当前PP及日期可用的PP+
    public static final String GET_PPS_BY_PPP_AND_DATE = "/user/getPPsByPPPAndDate";// 获取当前PPP及日期可绑定的PP
    public static final String GET_USER_BY_MOBILE = "/user/getUserByMobile";// 通过手机号查询用户

    public static final String GET_STORE_BY_IP = "/api/getStoreId";//通过ip查找storeId
    public static final String GET_GOODS = "/api/goods";//获取全部商品
    public static final String GET_SINGLE_GOOD = "/api/stores/";//获取指定商品数据
    public static final String GET_CART = "/api/carts";//获取购物车信息
    public static final String ADD_TO_CART = "/api/carts";//加入购物车
    public static final String MODIFY_TO_CART = "/api/carts";//修改购物车
    public static final String DELETE_TO_CART = "/api/carts";//删除购物车
    public static final String ADD_ORDER = "/api/checkOut";//用户提交订单


    public static final String GET_TOP_GOODS = "/ms/getTop4Goods";//获取热门商品
    public static final String GET_PHOTO_PASS_PLUS = "/shopping/getDefaultPhotoPassPluss";//获取PPP商品
    public static final String REMOVE_CART = "/shopping/removeCart";//删除购物车信息
    public static final String MODIFY_CART = "/shopping/modifyCart";//修改购物车信息
    public static final String UPLOAD_PHOTOS = "/p/userUploadPhoto";//上传图片
    public static final String UPDATE_USER_IMAGE = "/user/updateUserImage";//更新用户头像或头部背景图

    public static final String ADD_ON_ORDER = "/ms/AddOnOrder";//生成订单，并且获取订单信息
    public static final String BUY_PHOTO = "/api/buyPhoto";//一键放入数码商品至购物车信息
    public static final String GET_OUTLET_ID = "/api/outlets";//获取门店地址信息

    public static final String GET_BANNER_GOODS = "/shopping/getBanners";//得到banner商品

    public static final String GET_CART_COUNT = "/shopping/getCartCount";//获取购物车的数量
    public static final String GET_ADDRESS = "/shopping/getAddresses";//获取收货地址
    public static final String DELETE_ADDRESS = "/shopping/deleteAddress";//删除收货地址
    public static final String MODIFY_ADDRESS = "/shopping/modifyAddress";//修改收货地址
    public static final String ADD_ADDRESS = "/shopping/addAddress";//添加收货地址
    public static final String GET_ALL_ORDERS = "/api/getAllOrders";//获取所有订单
    public static final String DELETE_ORDER = "/api/delOrder";//用户删除某个订单
    public static final String BIND_PPS_TO_PPP = "/ppp/bindPPsToPPP";//将pp绑定到ppp
    public static final String BIND_PP_TO_PPP_BY_PHOTOID = "/p/bindPPToPPPByPhotoId";//通过photoid将pp绑定到ppp
    public static final String GET_NEW_PHOTO_COUNT = "/p/getNewPhotoCount";// 获取用户照片是否更新

    public static final String HIDE_PPS = "/user/hidePPs";//隐藏PP
    public static final String GET_PP_BY_DATE = "/user/getNeedUpgradePPsByDate";
    public static final String BIND_PPS_DATE_TO_PPP = "/ppp/bindPPsAndBindDatesToPPP";//将pp绑定到ppp   多了时间。
    public static final String APNS_CONNECT = "/socket/APNSConnect"; // 链接时
    public static final String APNS_DISCONNECT = "/socket/APNSDisconnect"; // 退出登陆时
    public static final String CLEAR_PHOTO_COUNT = "/socket/clearPhotoCount"; // 接受到消息时。

    public static final String ME_HELP = "/help/getHelpList";
    public static final String VIDEO_GENERATEVIDEO = "/video/generateVideo";//上传照片到服务器合成视频
    public static final String GET_SHARE_URL = "/api/getShareUrl";//获取分享用的URL
    public static final String SHARE_CALL_BACK = "/api/share";//分享回调
    public static final String GET_AD_LOCATIONS = "/park/getAdLocations";//获取广告地址
    public static final String USE_EXPERIENCE_PPP = "/ppp/useExperiencePPP";//使用体验卡绑定图片


    //user的SharedPreferences    以及   接口参数
    public static final String UUID = "UUID";
    public static final String TERMINAL = "terminal";
    public static final String APP_ID = "appID";
    public static final String USERINFO_NAME = "userInfo";
    public static final String USERINFO_ID = "_id";//api需要的userId
    public static final String USERINFO_TOKENID = "tokenId";//api需要的tokenId
    public static final String USERINFO_USER_PP = "userPP"; //用户的code
    public static final String USERINFO_USERNAME = "userName";
    public static final String USERINFO_PASSWORD = "password";
    public static final String USERINFO_NICKNAME = "name";
    public static final String USERINFO_COUNTRY = "country";
    public static final String USERINFO_HEADPHOTO = "avatarUrl";
    public static final String USERINFO_BGPHOTO = "coverHeaderImage";
    public static final String USERINFO_GENDER = "gender";
    public static final String USERINFO_BIRTHDAY = "birthday";
    public static final String USERINFO_EMAIL = "email";
    public static final String USERINFO_QQ = "qq";
    public static final String USERINFO_WECHAT = "wechat";
    public static final String USERINFO_TWITTER = "twitter";
    public static final String USERINFO_ACCOUNT = "account";
    public static final String SOCKETPUSHCONNECTED = "isConnected";
    public static final String NEW_PASSWORD = "newPwd";
    public static final String OLD_PASSWORD = "oldPwd";
    public static final String MODIFY_OR_FORGET = "opType";
    public static final String PPP_COUNT = "pppcount";
    public static final String USER_ID = "userId";
    public static final String IS_JUST_BUY = "isJustBuy";
    public static final String STORE_ID = "storeId";
    public static final String PRODUCT_ID = "productId";
    public static final String QTY = "qty";
    public static final String PRODUCT_NAME = "productName";
    public static final String PHOTO_ID = "photoId";
    public static final String PRICE = "price";
    public static final String PRODUCT_DESCRIPTION = "productDescription";
    public static final String PROMOTION_ID = "promotionId";
    public static final String SOURCE_PRICE = "sourcePrice";
    public static final String EMBEDPHOTOS = "embedPhotos";
    public static final String PHOTO_IMAGE = "productImage";
    public static final String CART_COUNT = "cartcount";
    public static final String PHOTOIDS = "photoIds";
    //	public static final String CART = "cart";
    public static final String CART_ITEM_ID = "cartItemId";
    public static final String GOODS_KEY = "goodsKey";
    public static final String CART_ITEM_IDS = "cartItemIds";
    public static final String EMBED_PHOTOS = "embedPhotos";
    public static final String ITEM = "item";
    public static final String CODE = "code";
    public static final String CODE_TYPE = "codeType";
    public static final String CUSTOMERID = "customerId";
    public static final String ADDRESS_ID = "addressId";
    public static final String ORDER_ID = "orderId";
    public static final String PAY_TYPE = "payType";
    public static final String DELIVERY_TYPE = "deliveryType";
    public static final String STORE_ADDRESS = "storeAddress";
    public static final String CURRENCY = "currency";
    public static final String IP = "ip";
    public static final String ADDRESS_INFO = "addressInfo";
    public static final String NEW_ADDRESS_INFO = "newAddressInfo";
    public static final String DEFAULT_CURRENCY = "¥";
    public static final String HEADPHOTO_PATH = "headphoto";
    public static final String BGPHOTO_PAHT = "bgphoto";

    public static final int TOAST_SHORT_TIME = 100;
    //	public static final String TESTING_USER_ID = "54780d447eac676ae8cdcd44";
    public static final String PHOTOPASS_DESCRPITION = "This picture made by PictureAir";
    //	public static final String PHOTOPASS_NAME = "Photo";
    public static final String ppp = "PhotoPass+";//商品的ppp名称修改了，原来为PhotoPassPlus。暂时忘了这个字段是否涉及到其他问题，
    public static final String PPPCode = "PPPCode";
    public static final String bindDate = "bindDate";
    public static final String PPS = "pps";
    public static final String ppp1 = "ppp";
    //	public static final String AUTO_BIND_PP = "autobindpp";//购买ppp之后，是否需要自动绑定标记，photoid：表示需要自动绑定；其他：不需要自动绑定
    public static final String LAST_UPDATE_PHOTO_TIME = "lastUpdatePhotoTime";
    public static final String LAST_UPDATE_VIDEO_TIME = "lastUpdateVideoTime";
    public static final String LAST_UPDATE_TIME = "lastUpdateTime";
    public static final String LANGUAGE_NAME = "l";
    public static final String PP_COUNT = "ppCount";//保存pp数量的sharedpreference
    public static final String LOCATION_ID = "locationId";
    public static final String ACTION = "action";
    public static final String CLEAR_TYPE = "clearType";//判断订单还是照片推送
    public static final String SHARE_MODE = "mode";//分享的类型
    public static final String SHARE_PHOTO_ID = "ids";//分享的类型
    public static final String IS_USE_SHORT_URL = "isUseShortUrl";//分享的短链接或者长链接
    public static final String SHARE_CONTENT = "shareContent";//分享的短链接或者长链接
    public static final String SHARE_ID = "shareId";
    public static final String SHARE_PLATFORM = "platform";
    public static final String EPPP = "ePPP";
    public static final String EPPP_IDS = "ids";

    //存放sdk的目录
    public static final String ALBUM_FILE_DOWLOAD_NAME = "/Shanghai Disney PhotoPass/download";
    public static final String USER_PATH = Environment.getExternalStorageDirectory().getPath() + "/Shanghai Disney PhotoPass/user/";
    public static final String PHOTO_SAVE_PATH = Environment.getExternalStorageDirectory().getPath() + "/Shanghai Disney PhotoPass/";
    public static final String DOWNLOAD_APK_PATH = Environment.getExternalStorageDirectory().getPath() + "/Shanghai Disney PhotoPass/app/";
    public static final String PHOTO_DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getPath() + "/Shanghai Disney PhotoPass/download/";
    public static final String FRAME_DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getPath() + "/Shanghai Disney PhotoPass/download/frames/";
    public static final String TEMPPIC_PATH = Environment.getExternalStorageDirectory().getPath() + "/Shanghai Disney PhotoPass/temppic/";
    public static final String SHARE_PATH = Environment.getExternalStorageDirectory().getPath() + "/Shanghai Disney PhotoPass/share/";
    public static final String OCR_PATH = Environment.getExternalStorageDirectory().getPath() + "/Shanghai Disney PhotoPass/tessdata/";
    public static final String OCR_DATA_PATH = Environment.getExternalStorageDirectory().getPath() + "/Shanghai Disney PhotoPass/tessdata/eng.traineddata";

    //缓存的字段
    public static final String TOP_GOODS = "topgoods";
    public static final String ALL_GOODS = "allgoods";
    public static final String BANNER_GOODS = "bannergoods";
    public static final String PPP_GOOD = "pppgood";
    public static final String LOCATION_INFO = "locationInfo";
    public static final String ACACHE_ADDRESS = "address";//收货地址缓存
    //	public static final String CHANGE_CUR_TAB = "change_cur_tab";

    //photo sqlite数据库字段
    public static final String PHOTOPASS_INFO_NAME = "photopass_info.db3";
    public static final String PHOTOPASS_INFO_TABLE = "photopassinfo";
    public static final String FAVORITE_INFO_TABLE = "favoriteinfo";
    public static final String PHOTOPASS_CODE_INFO_TABLE = "photopass_code";
    public static final String FIRST_START_ACTIVITY_INFO_TABLE = "first_start_activity";
    public static final String HELP_QUESTION_TABLE = "help_question";
    public static final String FRAME_STICKER_TABLES = "frame_sticker";
    public static final int PHOTOPASS_INFO_VERSION = 1;
    public static final String AES_ENCRYPTION_KEY = "80ab912ad5392557";//(pictureAir-android)
    public static final String SQLCIPHER_KEY = "d8b8a0ad80ab912ad53925576cd10676";//(pictureAir-android)
    public static final String PAYMENT_ORDER = "payment_order";//存放已经支付但未收到支付完成推送的订单
    public static final String AD_LOCATION = "ad_location";


    //相册名称字段
    public static final String ALBUM_ALL = "All";
    public static final String ALBUM_PHOTOPASS = "PhotoPass";
    public static final String ALBUM_MAGIC = "Magic";
    public static final String ALBUM_BOUGHT = "Bought";

    //图片保存文件夹名字字段
    public static final String ALBUM_FILE_PICTURE_NAME = "photopass";


    //其他字段
    //	public static final String PHOTOPASSPLUS = "PhotoPass+";//商品的ppp名称修改了，原来为PhotoPassPlus。暂时忘了这个字段是否涉及到其他问题，

    //	public static final String BARCODEURL = "http://192.168.8.3:3001/downloadApp.html?";

    //app的SharePreferences
    public static final String APP = "app";
    public static final String BLUR_LEAD = "blurlead";
    public static final String APP_VERSION_CODE = "version_code";
    public static final String DEFAULT_ADDRESS = "default_address";
    public static final String NEED_FRESH = "need_fresh";
    public static final String LANGUAGE_TYPE = "languageType";
    public static final String LAST_PHOTO_URL = "LastPhotoUrl";
    public static final String GET_LAST_CONTENT_TIME = "lastContentTime";
    public static final String LANGUAGE = "language";

    //app的语言
    public static final String ENGLISH = "en";//英语
    public static final String SIMPLE_CHINESE = "zh";//简体中文

    //购买单张回来是否更新View的字段
    public static final String FROM_VIEWORSELECTACTIVITY = "fromViewOrSelect";
    public static final String FROM_VIEWORSELECTACTIVITYANDPAYED = "fromViewOrSelectBuy";
    public static final String FROM_MYPHOTOPASS = "fromMyPhotoPass";
    public static final String FROM_MYPHOTOPASSPAYED = "fromMyPhotoPassBuy";
    public static final String FROM_PREVIEW_PHOTO_ACTIVITY = "fromPreviewPhotoActivity";
    public static final String FROM_PREVIEW_PHOTO_ACTIVITY_PAY = "fromPreviewPhotoActivityBuy";
    //修改个人信息，判断是从哪里跳转去修改页面。
    public static final String USERINFOTYPE = "userinfotype";

    public static final int NICKNAMETYPE = 1;
    public static final int EMAILTYPE = 2;
    public static final int QQTYPE = 3;

    public static final String GOOD_NAME_PPP = "PhotoPass+";
    public static final String GOOD_NAME_SINGLE_DIGITAL = "Digital Photo";
    public static final String GOOD_NAME_6R = "6R Print";
    public static final String GOOD_NAME_TSHIRT = "Duffy Bear Personalised T-Shirt";
    public static final String GOOD_NAME_COOK = "Your personal cookies gift";

    // 友盟参数
    public static final String UMENG_CHANNEL = "360";// 打包渠道
    //		public static final String CHANNEL = "tencent";// 渠道
//		public static final String CHANNEL = "baidu";// 渠道
//		public static final String CHANNEL = "PICTUREAIRCOM_HOME";// 渠道
//		public static final String CHANNEL = "PICTUREAIRCOM_PHOTO";// 渠道

    // 友盟自定义事件

    // 纪录点击的是  单张下载 还是 全部下载。
    public static String DOWNLOAD_TYPE = Common.ONE_PHOTO_DOWNLOAD;
    public static final String ONE_PHOTO_DOWNLOAD = "OnePhotoDownload";
    public static final String ONE_PHOTO_DOWNLOAD_SUCCESS = "OnePhotoDownloadSuccess";

    public static final String ALL_PHOTOS_DOWNLOAD = "allPhotosDownload";
    public static final String ALL_PHOTOS_DOWNLOAD_SUCCESS = "allPhotosDownloadSuccess";
    public static final String EVENT_CONTAIN_PICTURE_PEOPLES = "event_contain_picture_peoples";// 有图片的总人数
    public static final String EVENT_TOTAL_PICTURES = "event_total_pictures";// 图片总数
    public static final String EVENT_ONCLICK_DOWNLOAD = "event_onclick_dwonload";// 点击下载
    public static final String EVENT_DOWNLOAD_FINISH = "event_dwonload_finish";// 下载成功
    public static final String EVENT_ONCLICK_SHARE_QQ = "event_onclick_share_qq";// 点击qq分享
    public static final String EVENT_SHARE_QQ_FINISH = "event_share_qq_finish";// qq分享成功


    public static final String EVENT_ONCLICK_SHARE_QQZONE = "event_onclick_share_qqzone";// 点击qqzone分享
    public static final String EVENT_SHARE_QQZONE_FINISH = "event_share_qqzone_finish";// qqzone分享成功


    public static final String EVENT_ONCLICK_SHARE_WECHAT = "event_onclick_share_wechat";// 点击微信好友分享
    public static final String EVENT_SHARE_WECHAT_FINISH = "event_share_wechat_finish";// 微信好友分享成功

    public static final String EVENT_ONCLICK_SHARE_WECHAT_MOMENTS = "event_onclick_share_wechat_moments";// 点击微信朋友圈分享
    public static final String EVENT_SHARE_WECHAT_MOMENTS_FINISH = "event_share_wechat_moments_finish";// 微信朋友圈分享成功


    public static final String EVENT_ONCLICK_SHARE_SINA_WEIBO = "event_onclick_share_sina_weibo";// 点击新浪微博分享
    public static final String EVENT_SHARE_SINA_WEIBO_FINISH = "event_share_sina_weibo_finish";// 新浪微博分享成功

    public static final String EVENT_ONCLICK_SHARE_TWITTER = "event_onclick_share_twitter";// 点击Twitter分享
    public static final String EVENT_SHARE_TWITTER_FINISH = "event_share_twitter_finish";// Twitter分享成功

    public static final String EVENT_ONCLICK_SHARE_FACEBOOK = "event_onclick_share_facebook";// 点击facebook分享
    public static final String EVENT_SHARE_FACEBOOK_FINISH = "event_share_facebook_finish";// facebook分享成功

    public static final String ALL_PHOTO_COUNT = "AllPhotoCount";

    public static final String HAVE_PHOTO_USERS_COUNT = "HavePhotoUsersCount";

    // 友盟sharedPreferences

    public static final String IS_HAVE_PIC = "IS_HAVE_PIC";// 是否包含图片
    public static final String PICTURE_COUNT = "PICTURE_COUNT";// 用户图片总数（PhotoPass）

    // 分享参数
    public static final int PREVIEW_SHARE = 203;


    // 保存设置。
    public static final String SETTING_WIFI = "onlyWifi";
    public static final String SETTING_SYNC = "sync";
    public static final String SETTING_NOT_FIRST_BUY_ONE_PHOTO = "notFirstGoBuyOnePhoto";

    public static final String SETTING_NOT_FIRST_PP10 = "notFirstpp10";

    // 纪录 MainTab 中 tab 的高度。
    public static int TAB_HEIGHT = 116; //默认116，vivo手机上是这么多。 在MainTab中跳转 Camera时 会有重新赋值。

    //保存设置中的字段  start
    public static final String SETTING_ONLY_WIFI = "onlyWifi";  // 仅wifi的下载模式， 如果存在，是
    public static final String SETTING_AUTO_UPDATE = "autoUpdate"; // 自动更新，如果字段存在，是
    //保存设置中的字段   end
    // tips 需要的字段  start
    public static final String SETTING_FIRST_TIPS_SYNS = "firstTipsSyns"; // 如果字段存在,就不是第一次提示同步。如果不存在，就是第一次
    public static final String SETTING_FIRST_PP10 = "firstpp10"; // 是否第一次，AirPass中的照片到达十张，就提示购买 AirPass。
    public static final int CART_HEIGHT = 158;
    public static final int CART_WIDTH = 174;

    public static final int BOTTOM_BAR_HEIGHT = 65; // dp 单位

}
