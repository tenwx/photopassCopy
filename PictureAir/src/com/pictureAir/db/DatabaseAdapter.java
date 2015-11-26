package com.pictureAir.db;

import java.util.ArrayList;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import android.content.Context;

import com.pictureAir.entity.Question;
import com.pictureAir.util.PinYin;

/**
 * 数据库操作类
 * 
 * 
 * 
 */
public class DatabaseAdapter {

	private static DatabaseManager manager;
	private static Context mContext;

	/**
	 * 获取一个操作类对象
	 * 
	 * @param context
	 * @return
	 */
	public static DatabaseAdapter getIntance(Context context) {
		DatabaseAdapter adapter = new DatabaseAdapter();
		mContext = context;
		manager = DatabaseManager.getInstance(new DatabaseHelper(mContext));
		return adapter;
	}

	/**
	 * 插入信息
	 * 
	 * @param titleArray
	 */
	public void insertInfo(ArrayList<Question> titleArray) {
		SQLiteDatabase database = manager.getWritableDatabase();
		try {
//			for (Question title : titleArray) {
//				ContentValues values = new ContentValues();
//				values.put("title", title.questionName);
//				values.put("answer", title.answer);// 讲内容转换为拼音
//				values.put("pinyin", PinYin.getPinYin(title.questionName));// 讲内容转换为拼音
//				database.insert(DatabaseHelper.TABLE_NAME, null, values);
//			}
			System.out.println("title size"+titleArray.size());
			String addsql="insert into question values(null,?,?,?,?)";
			for (int i = 0; i < titleArray.size(); i++) {
				System.out.println("inserting data into database:"+i);
				database.execSQL(addsql, new String[]{i+"", titleArray.get(i).questionName, titleArray.get(i).answer, PinYin.getPinYin(titleArray.get(i).questionName)});
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			manager.closeDatabase();
		}
	}
	
//	public void insert(List<String> titleArray) {
//		SQLiteDatabase database = manager.getWritableDatabase();
//
//		try {
//			for (String answer: titleArray) {
//				ContentValues values = new ContentValues();
//				values.put("answer", answer);
//				database.insert(DatabaseHelper.TABLE_NAME, null, values);
//			}
//		} catch (Exception e) {
//			// TODO: handle exception
//		} finally {
//			manager.closeDatabase();
//		}
//	}
	
	

	/**
	 * 查询信息
	 * 
	 * @param pinyin
	 *            // 字符串转换的拼音
	 * @return
	 */	
	
	public ArrayList<Question> findAll() {
		ArrayList<Question> resultArray = new ArrayList<Question>();
		SQLiteDatabase database = manager.getReadableDatabase();
		Cursor cursor = null;
		cursor = database.rawQuery(
				"select * from " + DatabaseHelper.TABLE_NAME, null);
		Question question = null;
		while (cursor.moveToNext()) {
			question = new Question();
			question.questionName = cursor.getString(cursor.getColumnIndex("title"));
			question.questionId = cursor.getString(cursor.getColumnIndex("questionId"));
			question.answer = cursor.getString(cursor.getColumnIndex("answer"));
			resultArray.add(question);
		}
		manager.closeDatabase();
		return resultArray;
	}

	public ArrayList<Question> queryInfo(String pinyin) {
		ArrayList<Question> resultArray = new ArrayList<Question>();
		SQLiteDatabase database = manager.getReadableDatabase();
		Cursor cursor = null;

		try {
			// 创建模糊查询的条件
			String likeStr = "'";
			for (int i = 0; i < pinyin.length(); i++) {
				if (i < pinyin.length() - 1) {
					likeStr += "%" + pinyin.charAt(i);
				} else {
					likeStr += "%" + pinyin.charAt(i) + "%'";
				}
			}

			cursor = database.rawQuery("select * from "
					+ DatabaseHelper.TABLE_NAME + " where pinyin like "
					+ likeStr, null);
			Question question = null;
			while (cursor.moveToNext()) {
				question = new Question();
				question.questionName = cursor.getString(cursor.getColumnIndex("title"));
				question.questionId = "questionId";
				question.answer = "answer";
				resultArray.add(question);
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.toString();
		} finally {
			manager.closeDatabase();
		}

		return resultArray;
	}

	/**
	 * 删除表中的所有数据
	 */
	public void deleteAll() {
		SQLiteDatabase database = manager.getWritableDatabase();

		try {
			database.delete(DatabaseHelper.TABLE_NAME, null, null);
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			manager.closeDatabase();
		}
	}
}