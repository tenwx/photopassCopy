package com.pictureworks.android.db;


import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by milo on 16/2/29.
 * 多线程安全并发操作数据库
 * 1.synchronized  安全性
 * 2.AtomicInteger 并发性
 */
public class DBManager {
    private static AtomicInteger mOpenCounter = new AtomicInteger();
    private static DBManager dbManager;
    private static SQLiteOpenHelper dbHelper;
    private static SQLiteDatabase db;

    public static synchronized void initializeInstance(SQLiteOpenHelper helper) {
        if (dbManager == null) {
            dbManager = new DBManager();
            dbHelper = helper;
        }
    }


    /**
     * 获取DB
     *
     * @return
     */
    public static synchronized DBManager getInstance() {
        if (dbManager == null) {
            throw new IllegalStateException(DBManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return dbManager;
    }

    /**
     * 写入数据DB
     *
     * @return
     */
    public synchronized SQLiteDatabase writData(String key) {
        if (mOpenCounter.incrementAndGet() == 1) {
            // Opening new database
            db = dbHelper.getWritableDatabase(key);
        }
        return db;
    }

    /**
     * 读取数据DB
     *
     * @return
     */
    public synchronized SQLiteDatabase readData(String key) {
        if (mOpenCounter.incrementAndGet() == 1) {
            // Opening new database
            //db = dbHelper.getReadableDatabase();
            //API原文：This will be the same object returned by getWritableDatabase() unless some problem, such as a full disk, requires the database to be opened read-only.
            db = dbHelper.getWritableDatabase(key);
        }
        return db;
    }


    /**
     * 关闭DB
     */
    public synchronized void closeDatabase() {
        if (mOpenCounter.decrementAndGet() == 0 && db != null) {
            // Closing database
            db.close();
        }
    }

    /**
     * 删除DB
     */
    public synchronized void deleteDatabase(String DBName) {
        if (db != null) {
            // delete database
            db.delete(DBName, null, null);
        }
    }

    /**
     * 删除所有DB
     */
    public synchronized void deleteAllDatabase() {
        if (db != null) {
            // delete all database
            db.delete("", null, null);
        }
    }
}
