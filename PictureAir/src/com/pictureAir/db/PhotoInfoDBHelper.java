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
public class PhotoInfoDBHelper extends SQLiteOpenHelper {
	
	private static final int VERSION = 1;
	private static final String TAG = "PhotoInfoDBHelper";
	
	public PhotoInfoDBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}
	public PhotoInfoDBHelper(Context context,String name){
		this(context,name,VERSION);
	}
	public PhotoInfoDBHelper(Context context,String name,int version){
		this(context, name,null,version);
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
		 * photoId
		 * userId
		 * isLove
		 */
		db.execSQL("create table if not exists "+Common.FAVORITE_INFO_TABLE+"(_id integer primary key autoincrement, photoId varchar(30), userId varchar(30), isLove varchar(2))");
		
		/**
		 * 创建photopass表，有记录所有的photopass信息
		 * _id
		 * photopass
		 * userId
		 * bindDate 
		 * picCount
		 * deleteState 记录是否被本地删除过，Y删除过和N么有删除过
		 */
		db.execSQL("create table if not exists "+Common.PHOTOPASS_CODE_INFO_TABLE+"(_id integer primary key autoincrement, photopass varchar(20), " +
				"userId varchar(30), bindDate varchar(30), picCount varchar(4), deleteState varchar(1))");
		
		/**
		 * 创建firststart表，记录所有页面第一次进入的记录
		 * _id
		 * activity
		 * userId
		 */
		db.execSQL("create table if not exists "+Common.FIRST_START_ACTIVITY_INFO_TABLE+"(_id integer primary key autoincrement, activity varchar(40), userId varchar(30))");
		
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
