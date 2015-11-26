package com.pictureAir.db;

import com.pictureAir.util.Common;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabase.CursorFactory;
import net.sqlcipher.database.SQLiteOpenHelper;
import android.content.Context;

/**
 * 数据库助手类
 * 
 * 
 * 
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	public static final int VERSION = 2;// 版本号
	// private static final String DB_NAME = "ZiHao.db";// 数据库名称
	public static final String DB_NAME = 
//			SDBHelper.DB_DIR + File.separator
//			+ 
			"question.db";
	public static final String TABLE_NAME = "question";// 表名

	/**
	 * DatabaseHelper构造方法
	 * 
	 * @param context
	 * @param name
	 * @param factory
	 * @param version
	 */
	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	public DatabaseHelper(Context context) {
		// TODO Auto-generated constructor stub
		super(context, DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		createAutoTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	/**
	 * 创建数据库表
	 * 
	 * @param db
	 */
	private void createAutoTable(SQLiteDatabase db) {
		db.execSQL("create table  if not exists "
				+ TABLE_NAME
				+ "(id  INTEGER PRIMARY KEY AUTOINCREMENT,questionId INTEGER ,title varchar(255),answer varchar(255), pinyin varchar(255))");
		
//		String addsql="insert into question values(1,'如何使用乐拍通／购买照片？','你真的想知道吗？','')";
//		db.execSQL(addsql);
//		
//		String addsql1="insert into question values('乐拍通的使用有限期限？','我可以不说吗？')";
//		db.execSQL(addsql1);
//		
//		String addsql2="insert into question values('还想了解乐拍通的哪些功能？','我好像不知道！！！')";
//		db.execSQL(addsql2);
//		
//		String addsql3="insert into question values('怎么样才能拍的好看？','就不告诉你！！！')";
//		db.execSQL(addsql3);
		
	}
	
	// 封装更新数据库的sql语句
	public void exeUpdate(String sql, Object[] params) {
		SQLiteDatabase db = this.getWritableDatabase(Common.SQLCIPHER_KEY);
		db.execSQL(sql, params);
	}

	// 封装查询数据库的sql语句
	public Cursor exeQuery(String sql, String[] params) {
		SQLiteDatabase db = this.getReadableDatabase(Common.SQLCIPHER_KEY);
		return db.rawQuery(sql, params);
	}

}