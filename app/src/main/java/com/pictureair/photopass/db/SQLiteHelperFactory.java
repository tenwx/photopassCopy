package com.pictureair.photopass.db;


import android.content.Context;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

/**
 * SQLiteOpenHelper 工厂
 *
 * @author Ricky
 */
public class SQLiteHelperFactory {

    private static final String TAG = SQLiteHelperFactory.class.getSimpleName();

    private static SQLiteOpenHelper sqLiteOpenHelper;

    private SQLiteHelperFactory() {

    }

    public static SQLiteOpenHelper create(Context context) {
        if (sqLiteOpenHelper == null) {
            synchronized (SQLiteHelperFactory.class) {
                if (sqLiteOpenHelper == null) {
                    sqLiteOpenHelper = new PictureAirDBHelper(context.getApplicationContext());
                    //必须先调用此方法
                    SQLiteDatabase.loadLibs(context);
                }
            }
        }
        return sqLiteOpenHelper;
    }
}
