package com.pictureair.photopassCopy.greendao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.database.Database;

/**
 * Created by bauer_bao on 16/11/29.
 */

public class PWDBHelper extends DaoMaster.OpenHelper {
    public PWDBHelper(Context context, String name) {
        super(context, name);
    }

    public PWDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
    }
}
