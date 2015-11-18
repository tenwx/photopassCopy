package com.pictureAir.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class PictureAirDbHelper extends SQLiteOpenHelper{

    private final static String DB_NAME = "pictureAir";
    
    private final static int DATABASE_VERSION = 1;
    
    // single instance
    public static final Object dbLock = new Object();

    private static PictureAirDbHelper mPictureAirDbHelper = null;
    private Context context;
    private int oldVersion;
    
    public synchronized static PictureAirDbHelper getInstance(Context context) {
        if (mPictureAirDbHelper == null) {
        	mPictureAirDbHelper = new PictureAirDbHelper(context, DB_NAME, null, DATABASE_VERSION);
        }
        return mPictureAirDbHelper;
    }
    
    private PictureAirDbHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        synchronized (dbLock) {
            createTables(db);
        }
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

	
	private void createTables(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		createLocationDB(db);
	}
	
	
	private void createLocationDB(SQLiteDatabase db){
		System.out.println("create location db============");
		 StringBuilder sb = new StringBuilder();
	        sb.append("create table if not exists " + Field.TABLE_LOCATION + "(");
	        sb.append("id  integer primary key not null, ");
	        sb.append("pid integer,");
	        sb.append("plcaename varchar(50),");
	        sb.append("url varchar(50),");
	        sb.append("islove integer,");
	        sb.append("longitude varchar(50),");
	        sb.append("latitude varchar(50)");
	        sb.append(")");
	        db.execSQL(sb.toString());
	}
}
