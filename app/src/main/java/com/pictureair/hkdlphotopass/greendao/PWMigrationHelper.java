package com.pictureair.hkdlphotopass.greendao;

import android.database.Cursor;
import android.database.SQLException;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.internal.DaoConfig;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 升级类
 * Created by bauer_bao on 16/11/29.
 */

public class PWMigrationHelper {

    public static void migrate(Database db, Class<? extends AbstractDao<?, ?>>... daoClasses) {
        //1 新建临时表
        generateTempTables(db, daoClasses);
        //2 删除旧表
        dropAllTables(db, true, daoClasses);
        //3 创建新表
        createAllTables(db, false, daoClasses);
        //4 将临时表数据写入新表，删除临时表
        restoreData(db, daoClasses);
    }

    private static void generateTempTables(Database db, Class<? extends AbstractDao<?, ?>>... daoClasses) {
        for (int i = 0; i < daoClasses.length; i++) {
            String tempTableName = null;

            try {
                DaoConfig daoConfig = new DaoConfig(db, daoClasses[i]);
                String tableName = daoConfig.tablename;
                tempTableName = daoConfig.tablename.concat("_TEMP");

                StringBuilder dropTableStringBuilder = new StringBuilder();
                dropTableStringBuilder.append("DROP TABLE IF EXISTS ").append(tempTableName).append(";");
                db.execSQL(dropTableStringBuilder.toString());

                StringBuilder insertTableStringBuilder = new StringBuilder();
                insertTableStringBuilder.append("CREATE TEMPORARY TABLE ").append(tempTableName);
                insertTableStringBuilder.append(" AS SELECT * FROM ").append(tableName).append(";");
                db.execSQL(insertTableStringBuilder.toString());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getColumnsStr(DaoConfig daoConfig) {
        if (daoConfig == null) {
            return "no columns";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < daoConfig.allColumns.length; i++) {
            builder.append(daoConfig.allColumns[i]);
            builder.append(",");
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }


    private static void dropAllTables(Database db, boolean ifExists, @NonNull Class<? extends AbstractDao<?, ?>>... daoClasses) {
        reflectMethod(db, "dropTable", ifExists, daoClasses);
    }

    private static void createAllTables(Database db, boolean ifNotExists, @NonNull Class<? extends AbstractDao<?, ?>>... daoClasses) {
        reflectMethod(db, "createTable", ifNotExists, daoClasses);
    }

    /**
     * 创建根删除都在NoteDao声明了，可以直接拿过来用
     * dao class already define the sql exec method, so just invoke it
     */
    private static void reflectMethod(Database db, String methodName, boolean isExists, @NonNull Class<? extends AbstractDao<?, ?>>... daoClasses) {
        if (daoClasses.length < 1) {
            return;
        }
        try {
            for (Class cls : daoClasses) {
                //根据方法名，找到声明的方法
                Method method = cls.getDeclaredMethod(methodName, Database.class, boolean.class);
                method.invoke(null, db, isExists);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static void restoreData(Database db, Class<? extends AbstractDao<?, ?>>... daoClasses) {
        for (int i = 0; i < daoClasses.length; i++) {
            String tempTableName = null;

            try {
                DaoConfig daoConfig = new DaoConfig(db, daoClasses[i]);
                String tableName = daoConfig.tablename;
                tempTableName = daoConfig.tablename.concat("_TEMP");
                // get all columns from tempTable, take careful to use the columns list
                List<String> columns = getColumns(db, tempTableName);
                ArrayList<String> properties = new ArrayList<>(columns.size());
                for (int j = 0; j < daoConfig.properties.length; j++) {
                    String columnName = daoConfig.properties[j].columnName;
                    if (columns.contains(columnName)) {
                        properties.add(columnName);
                    }
                }
                if (properties.size() > 0) {
                    final String columnSQL = TextUtils.join(",", properties);

                    StringBuilder insertTableStringBuilder = new StringBuilder();
                    insertTableStringBuilder.append("INSERT INTO ").append(tableName).append(" (");
                    insertTableStringBuilder.append(columnSQL);
                    insertTableStringBuilder.append(") SELECT ");
                    insertTableStringBuilder.append(columnSQL);
                    insertTableStringBuilder.append(" FROM ").append(tempTableName).append(";");
                    db.execSQL(insertTableStringBuilder.toString());
                }
                StringBuilder dropTableStringBuilder = new StringBuilder();
                dropTableStringBuilder.append("DROP TABLE ").append(tempTableName);
                db.execSQL(dropTableStringBuilder.toString());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static List<String> getColumns(Database db, String tableName) {
        List<String> columns = null;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + tableName + " limit 0", null);
            if (null != cursor && cursor.getColumnCount() > 0) {
                columns = Arrays.asList(cursor.getColumnNames());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
            if (null == columns)
                columns = new ArrayList<>();
        }
        return columns;
    }
}
