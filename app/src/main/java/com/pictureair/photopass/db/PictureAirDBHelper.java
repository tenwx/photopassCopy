package com.pictureair.photopass.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.pictureair.photopass.util.Common;

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
    private static PictureAirDBHelper photoInfoDBHelper;

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

    public static PictureAirDBHelper getInstance(Context context) {
        if (photoInfoDBHelper == null) {
            photoInfoDBHelper = new PictureAirDBHelper(context, Common.PHOTOPASS_INFO_NAME);
        }
        return photoInfoDBHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
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
                "photoId varchar(30)," +
                "photoCode varchar(180)," +
                "shootTime varchar(12), " +
                "originalUrl varchar(100), " +
                "previewUrl varchar(100), " +
                "previewUrl_512 varchar(100), " +
                "previewUrl_1024 varchar(100)," +
                "locationId varchar(10), " +
                "shootOn varchar(30), " +
                "isLove integer, " +
                "isPay integer, " +
                "locationName varchar(100), " +
                "locationCountry varchar(100), " +
                "shareURL varchar(100), " +
                "isVideo integer, " +
                "fileSize integer, " +
                "videoWidth integer, " +
                "videoHeight integer)");

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
                "userId varchar(30), " +
                "photoId varchar(30), " +
                "photoCode varchar(180), " +
                "shootTime varchar(12), " +
                "originalUrl varchar(100), " +
                "previewUrl varchar(100), " +
                "previewUrl_512 varchar(100), " +
                "previewUrl_1024 varchar(100)," +
                "locationId varchar(10), " +
                "shootOn varchar(30), " +
                "isLove integer, " +
                "isPay integer, " +
                "locationName varchar(100), " +
                "locationCountry varchar(100), " +
                "shareURL varchar(100), " +
                "isVideo integer, " +
                "fileSize integer, " +
                "videoWidth integer, " +
                "videoHeight integer, " +
                "isOnLine integer)");


        /**
         * 创建firststart表，记录所有页面第一次进入的记录
         * _id
         * activity
         * userId
         */
        db.execSQL("create table if not exists " + Common.FIRST_START_ACTIVITY_INFO_TABLE +
                "(_id integer primary key autoincrement, " +
                "activity varchar(40), " +
                "userId varchar(30))");

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
                "question varchar(255), " +
                "answer varchar(255), " +
                "pinyin varchar(255))");

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
                "frameName varchar(40), " +
                "originalPathLandscape varchar(100), " +
                "originalPathPortrait varchar(100)" +
                ", thumbnailPathLandscape400 varchar(100), " +
                "thumbnailPathPortrait400 varchar(100), " +
                "thumbnailPath160 varchar(100), " +
                "locationId varchar(20), " +
                "isActive integer" +
                ", onLine integer, " +
                "isDownload integer, " +
                "fileSize integer, " +
                "fileType integer)");

        db.execSQL("create table if not exists " + Common.PAYMENT_ORDER
                + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "userId varchar(255), " +
                "orderId varchar(255))");

    }

    //如果数据库的版本号不一致，会执行onUpgrade函数
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "update Database");
        switch (newVersion) {
            case 2://版本号为2的更新包

                break;

            default:
                break;
        }
    }


}
