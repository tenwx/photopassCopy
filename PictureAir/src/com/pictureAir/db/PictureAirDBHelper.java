package com.pictureAir.db;

import com.pictureAir.util.Common;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;
/**
 * Photo页的图片信息数据库的databasehelper
 * photopass图片表，记录所有的photopass的照片数据
 * favorite表，记录所有的喜爱的地点
 * photopass条码表，记录所有的photopass信息表
 * @author bauer_bao
 *
 */
public class PictureAirDBHelper extends SQLiteOpenHelper {
	
	private static final String TAG = "PhotoInfoDBHelper";
	private static PictureAirDBHelper photoInfoDBHelper;
	
	public PictureAirDBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}
	
	public PictureAirDBHelper(Context context,String name){
		this(context,name,Common.PHOTOPASS_INFO_VERSION);
	}
	
	public PictureAirDBHelper(Context context,String name,int version){
		this(context, name,null,version);
	}
	
	public static PictureAirDBHelper getInstance(Context context){
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
		 * photoCode ppCode
		 * shootTime
		 * originalUrl
		 * previewUrl
		 * previewUrl_512
		 * previewUrl_1024
		 * location
		 * shootOn
		 * isLove
		 * isPay
		 */
		db.execSQL("create table if not exists "+Common.PHOTOPASS_INFO_TABLE+"(_id integer primary key autoincrement, photoId varchar(30),photoCode varchar(180),shootTime varchar(12), " +
				"originalUrl varchar(100), previewUrl varchar(100), previewUrl_512 varchar(100), previewUrl_1024 varchar(100)," +
				"location varchar(10), shootOn varchar(30), isLove varchar(2), isPay varchar(2))");
		
		/**
		 * 创建图片favorite表
		 * _id
		 * photoId 网络照片ID
		 * userId 用户ID
		 * photoPath 本地照片路径
		 */
		db.execSQL("create table if not exists "+Common.FAVORITE_INFO_TABLE+"(_id integer primary key autoincrement, photoId varchar(30), userId varchar(30), photoPath varchar(100))");
		
//		/**
//		 * 创建photopass表，有记录所有的photopass信息
//		 * _id
//		 * photopass
//		 * userId
//		 * bindDate 
//		 * picCount
//		 * deleteState 记录是否被本地删除过，Y删除过和N么有删除过
//		 */
//		db.execSQL("create table if not exists "+Common.PHOTOPASS_CODE_INFO_TABLE+"(_id integer primary key autoincrement, photopass varchar(20), " +
//				"userId varchar(30), bindDate varchar(30), picCount varchar(4), deleteState varchar(1))");
		
		/**
		 * 创建firststart表，记录所有页面第一次进入的记录
		 * _id
		 * activity
		 * userId
		 */
		db.execSQL("create table if not exists "+Common.FIRST_START_ACTIVITY_INFO_TABLE+"(_id integer primary key autoincrement, activity varchar(40), userId varchar(30))");
		
		/**
		 * 创建help页面的问题表
		 * questionId 问题ID
		 * question 问题
		 * answer 问题答案
		 * pinyin 拼音自动检索字段 
		 * 
		 */
		db.execSQL("create table if not exists " + Common.HELP_QUESTION_TABLE
				+ "(id INTEGER PRIMARY KEY AUTOINCREMENT, questionId INTEGER, question varchar(255), answer varchar(255), pinyin varchar(255))");
		
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
				+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT, frameName varchar(40), originalPathLandscape varchar(100), originalPathPortrait varchar(100)" +
				", thumbnailPathLandscape400 varchar(100), thumbnailPathPortrait400 varchar(100), thumbnailPath160 varchar(100), locationId varchar(20), isActive integer" +
				", onLine integer, isDownload integer, fileSize integer, fileType integer)");
		
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
