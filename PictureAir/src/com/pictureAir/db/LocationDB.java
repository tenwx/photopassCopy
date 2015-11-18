package com.pictureAir.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.pictureAir.entity.DiscoverLocationItemInfo;

/**
 * @author lance 地点信息的数据库操作
 */

public class LocationDB {
	SQLiteDatabase db;
	Context context;
	PictureAirDbHelper dbHelper;

	public LocationDB(Context context) {
		this.context = context;
		dbHelper = PictureAirDbHelper.getInstance(context);
	}

	/**
	 * 打开数据库
	 */
	private void onOpen() {
		db = dbHelper.getWritableDatabase();
	}

	/**
	 * 关闭数据库
	 */
	private void onClose() {
		db.close();
	}

	public void insert(ArrayList<DiscoverLocationItemInfo> list) {
		synchronized (PictureAirDbHelper.dbLock) {
			ContentValues cv = new ContentValues();
			onOpen();
			db.beginTransaction();
			for (int i = 0; i < list.size(); i++) {
				DiscoverLocationItemInfo info = list.get(i);
				cv.put("pid", info.locationId);
				cv.put("placename", info.place);
				cv.put("url", info.placeUrl);
				cv.put("longitude", info.longitude);
				cv.put("latitude", info.latitude);
				db.insert(Field.TABLE_LOCATION, null, cv);
			}
			db.setTransactionSuccessful();
			db.endTransaction();
			onClose();
		}
	}

	public void update(ArrayList<DiscoverLocationItemInfo> list) {
		synchronized (PictureAirDbHelper.dbLock) {
			ContentValues cv = new ContentValues();
			onOpen();
			db.beginTransaction();
			for (int i = 0; i < list.size(); i++) {
				DiscoverLocationItemInfo info = list.get(i);
				cv.put("pid", info.locationId);
				cv.put("placename", info.place);
				cv.put("url", info.placeUrl);
				cv.put("longitude", info.longitude);
				cv.put("latitude", info.latitude);
				db.update(Field.TABLE_LOCATION, cv, "pid = " + info.locationId, null);
			}
			db.setTransactionSuccessful();
			db.endTransaction();
			onClose();
		}
	}

	public ArrayList<DiscoverLocationItemInfo> getAll() {
		synchronized (PictureAirDbHelper.dbLock) {
			ArrayList<DiscoverLocationItemInfo> list = new ArrayList<DiscoverLocationItemInfo>();
			String columns[] = { "pid", "placename", "url", "islove", "longitude", "latitude" };
			onOpen();
			Cursor cursor = db.query(Field.TABLE_LOCATION, columns, null, null, null, null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					DiscoverLocationItemInfo info = new DiscoverLocationItemInfo();
					info.locationId = cursor.getString(0);
					info.place = cursor.getString(1);
					info.placeUrl = cursor.getString(2);
					String longitude = cursor.getString(4);
					String latitude = cursor.getString(5);
					info.latitude = Double.valueOf(latitude);
					info.longitude = Double.valueOf(longitude);
					list.add(info);
				}
				cursor.close();
			}
			onClose();
			return list;
		}
	}

	public DiscoverLocationItemInfo getOne(int pid) {
		synchronized (PictureAirDbHelper.dbLock) {
			DiscoverLocationItemInfo info = null;
			onOpen();
			String columns[] = { "pid", "placename", "url", "isloce", "longitude", "latitude" };
			Cursor cursor = db.query(Field.TABLE_LOCATION, columns, "pid = " + pid, null, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				info = new DiscoverLocationItemInfo();
				info.locationId = cursor.getString(0);
				info.place = cursor.getString(1);
				info.placeUrl = cursor.getString(2);
				String longitude = cursor.getString(4);
				String latitude = cursor.getString(5);
				info.latitude = Double.valueOf(latitude);
				info.longitude = Double.valueOf(longitude);
			}
			cursor.close();
			onClose();
			return info;
		}
	}
}
