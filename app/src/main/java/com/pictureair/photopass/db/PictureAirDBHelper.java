package com.pictureair.photopass.db;

import android.content.Context;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabase.CursorFactory;
import net.sqlcipher.database.SQLiteOpenHelper;

/**
 * Photo页的图片信息数据库的databasehelper
 * photopass图片表，记录所有的photopass的照片数据
 * favorite表，记录所有的喜爱的地点
 * photopass条码表，记录所有的photopass信息表
 *
 * @author bauer_bao
 */
public class PictureAirDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "PhotoInfoDBHelper";
    private final String SQL_CREATE_TABLE_THREAD = "create table if not exists " + Common.THREAD_INFO + "(_id integer primary key autoincrement," +
            "thread_id integer,url text,start integer,end integer,finished integer)";

    /**
     *  _id
     *  userId      用户id
     *  image BLOB  缩略图
     *  photoname   图片名
     *  size        图片大小
     *  date        下载日期
     *  time        下载时间
     * */
    private final String SQL_CREATE_TABLE_DOWNLOAD_PHOTOS_= "create table if not exists " + Common.PHOTOS_LOAD +
                                                            "(_id integer primary key autoincrement," +
                                                            "userId text," +
                                                            "photoId text," +
                                                            "photoName text," +
                                                            "url text," +
                                                            "size text," +
                                                            "date text," +
                                                            "time text)";


    public PictureAirDBHelper(Context context) {
        this(context, Common.PHOTOPASS_INFO_NAME);
    }

    public PictureAirDBHelper(Context context, String name, CursorFactory factory,
                              int version) {
        super(context, name, factory, version);
    }

    public PictureAirDBHelper(Context context, String name) {
        this(context, name, Common.PHOTOPASS_INFO_VERSION);
    }

    public PictureAirDBHelper(Context context, String name, int version) {
        this(context, name, null, version);
    }

//    public static PictureAirDBHelper getInstance(Context context) {
//        if (photoInfoDBHelper == null) {
//            photoInfoDBHelper = new PictureAirDBHelper(context, Common.PHOTOPASS_INFO_NAME);
//        }
//        return photoInfoDBHelper;
//    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        PictureAirLog.out("create sqlite-------->");
        // TODO Auto-generated method stub
        /**
         * 创建photo表
         * _id
         * photoId
         * photoCode ppCode 图片所属pp码
         * shootTime 拍摄时间
         * originalUrl 原图
         * previewUrl 128缩略图
         * previewUrl_512 512缩略图
         * previewUrl_1024 1024缩略图
         * locationId 地点id
         * shootOn 拍摄时间
         * isLove 是否收藏，目前没有用到
         * isPay 是否已经付费
         * locationName 地点名称
         * locationCountry 国家地点
         * shareURL  分享URL
         * isVideo   是否是视频
         * fileSize  文件的大小
         * videoWidth 视频宽
         * videoHeight 视频高
         */
        db.execSQL("create table if not exists " + Common.PHOTOPASS_INFO_TABLE +
                        "(_id integer primary key autoincrement, " +
                        "photoId text," +
                        "photoCode text," +
                        "shootTime text, " +
                        "originalUrl text, " +
                        "previewUrl text, " +
                        "previewUrl_512 text, " +
                        "previewUrl_1024 text," +
                        "locationId text, " +
                        "shootOn text, " +
                        "isLove integer, " +
                        "isPay integer, " +
                        "locationName text, " +
                        "locationCountry text, " +
                        "shareURL text, " +
                        "isVideo integer, " +
                        "fileSize integer, " +
                        "videoWidth integer, " +
                        "videoHeight integer, " +
                        "isHasPreset integer)"
        );

        /**
         * 创建图片收藏表，用来记录已收藏的信息
         * 目前退出登录都会重新写photo表，并且本地图片没有存放到photo表，所以暂时需要重新开表记录收藏图片，以后修改了photo表的逻辑之后，这两张表可以合并
         * _id
         * userId
         * photoId
         * photoCode ppCode 图片所属pp码
         * shootTime 拍摄时间
         * originalUrl 原图
         * previewUrl 128缩略图
         * previewUrl_512 512缩略图
         * previewUrl_1024 1024缩略图
         * locationId 地点id
         * shootOn 拍摄时间
         * isLove 是否收藏，目前没有用到
         * isPay 是否已经付费
         * locationName 地点名称
         * locationCountry 国家地点
         * shareURL  分享URL
         * isVideo   是否是视频
         * fileSize  文件的大小
         * videoWidth 视频宽
         * videoHeight 视频高
         * isOnLine 本地还是网络图片
         */
        db.execSQL("create table if not exists " + Common.FAVORITE_INFO_TABLE +
                        "(_id integer primary key autoincrement, " +
                        "userId text, " +
                        "photoId text, " +
                        "photoCode text, " +
                        "shootTime text, " +
                        "originalUrl text, " +
                        "previewUrl text, " +
                        "previewUrl_512 text, " +
                        "previewUrl_1024 text," +
                        "locationId text, " +
                        "shootOn text, " +
                        "isLove integer, " +
                        "isPay integer, " +
                        "locationName text, " +
                        "locationCountry text, " +
                        "shareURL text, " +
                        "isVideo integer, " +
                        "fileSize integer, " +
                        "videoWidth integer, " +
                        "videoHeight integer, " +
                        "isOnLine integer," +
                        "isHasPreset integer)"
        );

        /**
         * 创建firststart表，记录所有页面第一次进入的记录
         * _id
         * activity
         * userId
         */
        db.execSQL("create table if not exists " + Common.FIRST_START_ACTIVITY_INFO_TABLE +
                "(_id integer primary key autoincrement, " +
                "activity text, " +
                "userId text)");

        /**
         * 创建help页面的问题表
         * questionId 问题ID
         * question 问题
         * answer 问题答案
         * pinyin 拼音自动检索字段
         *
         */
        db.execSQL("create table if not exists " + Common.HELP_QUESTION_TABLE
                + "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "questionId INTEGER, " +
                "question text, " +
                "answer text, " +
                "pinyin text)");

        /**
         * 创建边框以及饰品的表
         * _id
         * frameName 名称
         * originalPathLandscape 横版原图
         * originalPathPortrait 竖版原图
         * thumbnailPathLandscape400 400缩略图横版
         * thumbnailPathPortrait400 400缩略图竖版
         * thumbnailPath160 160竖版缩略图
         * locationId 地点
         * isActive 是否激活状态
         * onLine 是否是网络数据
         * isDownload 是否已经下载过
         * fileSize 文件总共大小
         * fileType 文件类型  1：边框，0：饰品
         */
        db.execSQL("create table if not exists " + Common.FRAME_STICKER_TABLES
                + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "frameName text, " +
                "originalPathLandscape text, " +
                "originalPathPortrait text, " +
                "thumbnailPathLandscape400 text, " +
                "thumbnailPathPortrait400 text, " +
                "thumbnailPathH160 text," +
                "thumbnailPathV160 text, " +
                "locationId text, " +
                "isActive integer, " +
                "onLine integer, " +
                "isDownload integer, " +
                "fileSize integer, " +
                "fileType integer)");


        /**
         * 订单支付结果
         * userId
         * orderId
         */
        db.execSQL("create table if not exists " + Common.PAYMENT_ORDER
                + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "userId text, " +
                "orderId text)");

        /**
         * 广告地点信息
         * locationId 地点id
         * descriptionCH 中文广告
         * descriptionEN 英文广告
         */
        db.execSQL("create table if not exists " + Common.AD_LOCATION
                + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "locationId text, " +
                "descriptionCH text, " +
                "descriptionEN text)");

        /**
         * 线程表
         */
        db.execSQL(SQL_CREATE_TABLE_THREAD);

        /**
         * 已下载图片表
         * */
        db.execSQL(SQL_CREATE_TABLE_DOWNLOAD_PHOTOS_);

    }

    //如果数据库的版本号不一致，会执行onUpgrade函数
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        PictureAirLog.d(TAG, "update Database");
        switch (newVersion) {
            case 2://版本号为2的更新包

                break;

            default:
                break;
        }
    }


}
