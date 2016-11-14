package com.pictureair.photopass.db;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.DiscoverLocationItemInfo;
import com.pictureair.photopass.entity.DownloadFileStatus;
import com.pictureair.photopass.entity.FrameOrStikerInfo;
import com.pictureair.photopass.entity.PPinfo;
import com.pictureair.photopass.entity.PhotoDownLoadInfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.entity.PhotoItemInfo;
import com.pictureair.photopass.entity.QuestionInfo;
import com.pictureair.photopass.entity.ThreadInfo;
import com.pictureair.photopass.eventbus.TabIndicatorUpdateEvent;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.GlideUtil;
import com.pictureair.photopass.util.JsonUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SPUtils;

import net.sqlcipher.Cursor;
import net.sqlcipher.SQLException;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.greenrobot.event.EventBus;

/**
 * 数据库操作管理封装类，以后所有的数据库操作，都在这里进行
 *
 * @author bauer_bao
 */
public class PictureAirDbManager {
    private static final String TAG = "PictureAirDbManager";
    private SQLiteOpenHelper photoInfoDBHelper;
    private SQLiteDatabase database;
    public static final long DAY_TIME = 24 * 60 * 60 * 1000;//一天的毫秒数
    public static final int CACHE_DAY = 30;//30天的有效期

    public PictureAirDbManager(Context context) {
        if (photoInfoDBHelper == null) {
            photoInfoDBHelper = SQLiteHelperFactory.create(context);
            DBManager.initializeInstance(photoInfoDBHelper);//初始化数据库操作类
        }
    }

    /**
     * 收藏或者取消收藏图片
     *
     * @param photoInfo 网络图片的id
     * @param userId
     * @param setLove   是收藏还是取消收藏操作
     */
    public void setPictureLove(PhotoInfo photoInfo, String userId, boolean setLove) {
        database = DBManager.getInstance().writData();
        database.beginTransaction();
        Cursor cursor = null;
        try {
            if (setLove) {//添加收藏，因为收藏按钮是异步执行，所以添加前需要检查是否已经收藏过
                PictureAirLog.d(TAG, "start add___" + database + "___" + photoInfoDBHelper);

                PictureAirLog.out("cursor open---> checklovephoto");
                if (photoInfo.onLine == 1) {
                    cursor = database.rawQuery("select * from " + Common.FAVORITE_INFO_TABLE +
                                    " where photoId = ? and userId = ?",
                            new String[]{photoInfo.photoId, userId});

                } else {
                    cursor = database.rawQuery("select * from " + Common.FAVORITE_INFO_TABLE +
                                    " where userId = ? and originalUrl = ?",
                            new String[]{userId, photoInfo.photoPathOrURL});

                }

                if (cursor.getCount() == 0) {//之前没有添加过
                    database.execSQL("insert into " + Common.FAVORITE_INFO_TABLE + " values(null,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                            new String[]{userId, photoInfo.photoId, photoInfo.photoPassCode, photoInfo.shootTime,
                                    photoInfo.photoPathOrURL, photoInfo.photoThumbnail, photoInfo.photoThumbnail_512,
                                    photoInfo.photoThumbnail_1024, photoInfo.locationId, photoInfo.shootOn, photoInfo.isLove + "",
                                    photoInfo.isPayed + "", photoInfo.locationName, photoInfo.locationCountry,
                                    photoInfo.shareURL, photoInfo.isVideo + "", photoInfo.fileSize + "",
                                    photoInfo.videoWidth + "", photoInfo.videoHeight + "", photoInfo.onLine + "",
                                    photoInfo.isHasPreset + "", photoInfo.isEncrypted + "", photoInfo.adURL});
                }

            } else {//取消收藏

                if (photoInfo.onLine == 1) {
                    database.execSQL("delete from " + Common.FAVORITE_INFO_TABLE + " where photoId = ? and userId = ? and originalUrl = ?",
                            new String[]{photoInfo.photoId, userId, photoInfo.photoPathOrURL});
                } else {
                    database.execSQL("delete from " + Common.FAVORITE_INFO_TABLE + " where userId = ? and originalUrl = ?",
                            new String[]{userId, photoInfo.photoPathOrURL});
                }

            }
            database.setTransactionSuccessful();

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            database.endTransaction();
            DBManager.getInstance().closeDatabase();
        }
    }

    /**
     * 检查图片是否为收藏图片
     *
     * @param photoInfo
     * @param userId
     */
    public boolean checkLovePhoto(PhotoInfo photoInfo, String userId) {
        boolean result = false;
        Cursor cursor = null;
        try {
            database = DBManager.getInstance().readData();
            PictureAirLog.out("cursor open---> checklovephoto");
            if (photoInfo.onLine == 1) {

                cursor = database.rawQuery("select * from " + Common.FAVORITE_INFO_TABLE +
                                " where photoId = ? and userId = ?",
                        new String[]{photoInfo.photoId, userId});
            } else {
                cursor = database.rawQuery("select * from " + Common.FAVORITE_INFO_TABLE +
                                " where userId = ? and originalUrl = ?",
                        new String[]{userId, photoInfo.photoPathOrURL});

            }
            result = (cursor.getCount() > 0) ? true : false;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                PictureAirLog.out("cursor close---> check love photo");
                cursor.close();
            }
            DBManager.getInstance().closeDatabase();
        }
        return result;
    }

    /**
     * 检查图片列表是否为收藏图片，较于checkLovePhoto，可以减少database开关的次数，从而提高效率
     *
     * @param originalPhotosArrayList
     * @param userId
     */
    public ArrayList<PhotoItemInfo> checkLovePhotos(ArrayList<PhotoItemInfo> originalPhotosArrayList, String userId) {
        ArrayList<PhotoItemInfo> resultArrayList = new ArrayList<PhotoItemInfo>();
        PhotoItemInfo photoItemInfo;
        ArrayList<PhotoInfo> photoInfos;
        PhotoInfo photoInfo;
        Cursor cursor = null;
        try {
            database = DBManager.getInstance().writData();
            for (int i = 0; i < originalPhotosArrayList.size(); i++) {
                photoItemInfo = new PhotoItemInfo();
                photoItemInfo.locationId = originalPhotosArrayList.get(i).locationId;
                photoItemInfo.locationIds = originalPhotosArrayList.get(i).locationIds;
                photoItemInfo.shootTime = originalPhotosArrayList.get(i).shootTime;
                photoItemInfo.shootOn = originalPhotosArrayList.get(i).shootOn;
                photoItemInfo.place = originalPhotosArrayList.get(i).place;
                photoItemInfo.list = originalPhotosArrayList.get(i).list;
                photoItemInfo.placeUrl = originalPhotosArrayList.get(i).placeUrl;
                photoItemInfo.islove = originalPhotosArrayList.get(i).islove;
                photoItemInfo.latitude = originalPhotosArrayList.get(i).latitude;
                photoItemInfo.longitude = originalPhotosArrayList.get(i).longitude;

                photoInfos = new ArrayList<>();
                photoInfos.addAll(photoItemInfo.list);
                Iterator<PhotoInfo> iterator = photoInfos.iterator();
                while (iterator.hasNext()) {
                    photoInfo = iterator.next();
                    //检查是否是收藏图片
                    if (photoInfo.photoId == null) {
                        photoInfo.photoId = "";
                    }
                    if (photoInfo.photoPathOrURL == null) {
                        photoInfo.photoPathOrURL = "";
                    }
                    PictureAirLog.out("cursor open ---> check love photos");
                    cursor = database.rawQuery("select * from " + Common.FAVORITE_INFO_TABLE + " where photoId = ? and userId = ? and originalUrl = ?",
                            new String[]{(photoInfo.photoId != null) ? photoInfo.photoId : "", userId,
                                    (photoInfo.photoPathOrURL != null) ? photoInfo.photoPathOrURL : ""});
                    if (cursor.getCount() > 0) {//是收藏的图片
                        photoInfo.isLove = 1;
                    } else {//不是收藏图片
                        iterator.remove();
                    }
                    if (cursor != null) {
                        PictureAirLog.out("cursor close ---> check love photos");
                        cursor.close();
                    }
                }
                if (photoInfos.size() > 0) {//有收藏图片
                    photoItemInfo.list = photoInfos;
                    resultArrayList.add(photoItemInfo);
                    PictureAirLog.d(TAG, "photo size " + photoItemInfo.list.size());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                PictureAirLog.out("cursor close ---> check love photos");
                cursor.close();
            }
            DBManager.getInstance().closeDatabase();
        }
        return resultArrayList;
    }


    /**
     * 通过userId获取降序排列的已收藏图片列表
     *
     * @param userId
     * @return
     */
    public ArrayList<PhotoInfo> getFavoritePhotoInfoListFromDB(Context context, String userId, String deleteTime, ArrayList<DiscoverLocationItemInfo> locationItemInfos, String language) {
        ArrayList<PhotoInfo> resultArrayList = new ArrayList<>();
        Cursor cursor = null;
        Cursor cursor1 = null;
        try {
            database = DBManager.getInstance().writData();
            PictureAirLog.out("cursor open ---> getFavoritePhotoInfoListFromDB");

            //根据当前时间，删除超过30天并且未支付的数据信息
            /**
             * 1.获取当前时间，以毫秒为单位
             * 2.删除数据库数据，条件1.未购买的图片，2.当前时间 - 30天的时间 > 数据库的时间
             */
            database.execSQL("delete from " + Common.FAVORITE_INFO_TABLE + " where isPay = 0 and shootOn < datetime(?)", new String[]{deleteTime});

            cursor = database.rawQuery("select * from " + Common.FAVORITE_INFO_TABLE + " where userId = ? order by shootOn desc", new String[]{userId});
            PhotoInfo photoInfo;
            File file;
            while (cursor.moveToNext()) {
                photoInfo = new PhotoInfo();
                photoInfo.photoId = cursor.getString(cursor.getColumnIndex("photoId"));
                photoInfo.photoPassCode = cursor.getString(cursor.getColumnIndex("photoCode"));
                photoInfo.shootTime = cursor.getString(cursor.getColumnIndex("shootTime"));
                photoInfo.photoPathOrURL = cursor.getString(cursor.getColumnIndex("originalUrl"));
                photoInfo.photoThumbnail = cursor.getString(cursor.getColumnIndex("previewUrl"));
                photoInfo.photoThumbnail_512 = cursor.getString(cursor.getColumnIndex("previewUrl_512"));
                photoInfo.photoThumbnail_1024 = cursor.getString(cursor.getColumnIndex("previewUrl_1024"));
                photoInfo.locationId = cursor.getString(cursor.getColumnIndex("locationId"));
                photoInfo.shootOn = cursor.getString(cursor.getColumnIndex("shootOn"));
                photoInfo.isLove = Integer.valueOf(cursor.getString(cursor.getColumnIndex("isLove")));
                photoInfo.isPayed = Integer.valueOf(cursor.getString(cursor.getColumnIndex("isPay")));
                photoInfo.locationCountry = cursor.getString(cursor.getColumnIndex("locationCountry"));
                photoInfo.shareURL = cursor.getString(cursor.getColumnIndex("shareURL"));
                photoInfo.isVideo = Integer.valueOf(cursor.getString(cursor.getColumnIndex("isVideo")));
                photoInfo.fileSize = Integer.valueOf(cursor.getString(cursor.getColumnIndex("fileSize")));
                photoInfo.videoWidth = Integer.valueOf(cursor.getString(cursor.getColumnIndex("videoWidth")));
                photoInfo.videoHeight = Integer.valueOf(cursor.getString(cursor.getColumnIndex("videoHeight")));
                photoInfo.onLine = Integer.valueOf(cursor.getString(cursor.getColumnIndex("isOnLine")));
                photoInfo.isHasPreset = Integer.valueOf(cursor.getString(cursor.getColumnIndex("isHasPreset")));
                photoInfo.isEncrypted = Integer.valueOf(cursor.getString(cursor.getColumnIndex("enImg")));
                photoInfo.adURL = cursor.getString(cursor.getColumnIndex("adURL"));
                if (photoInfo.isPayed == 0) {//如果为0，检查photo表是否是已经购买状态
                    cursor1 = database.rawQuery("select * from " + Common.PHOTOPASS_INFO_TABLE + " where photoId = ? and isPay = ?", new String[]{photoInfo.photoId, "1"});
                    if (cursor1.getCount() > 0) {
                        photoInfo.isPayed = 1;
                    }
                    if (cursor1 != null) {
                        cursor1.close();
                    }
                }
                if (photoInfo.onLine == 0) {//本地图片，检查是否存在
                    PictureAirLog.out("local photos");
                    file = new File(photoInfo.photoPathOrURL);
                    if (!file.exists()) {
                        continue;
                    } else {
                        photoInfo.locationName = context.getString(R.string.story_tab_magic);
                    }
                } else {//网络图片
                    PictureAirLog.out("network photos");
                    for (int i = 0; i < locationItemInfos.size(); i++) {
                        PictureAirLog.out("find favorite location---->");
                        if (photoInfo.locationId.equals(locationItemInfos.get(i).locationId) || locationItemInfos.get(i).locationIds.contains(photoInfo.locationId)) {
                            PictureAirLog.out("found favorite location---->");
                            if (language.equals(Common.ENGLISH)) {
                                PictureAirLog.out("found favorite endligh location---->");
                                photoInfo.locationName = locationItemInfos.get(i).placeENName;
                            } else if (language.equals(Common.SIMPLE_CHINESE)) {
                                PictureAirLog.out("found favorite chinese location---->");
                                photoInfo.locationName = locationItemInfos.get(i).placeCHName;
                            }
                            break;
                        }
                    }
                    if (TextUtils.isEmpty(photoInfo.locationName)) {
                        if (language.equals(Common.ENGLISH)) {
                            PictureAirLog.out("found favorite endligh location---->");
                            photoInfo.locationName = locationItemInfos.get(locationItemInfos.size() - 1).placeENName;
                        } else if (language.equals(Common.SIMPLE_CHINESE)) {
                            PictureAirLog.out("found favorite chinese location---->");
                            photoInfo.locationName = locationItemInfos.get(locationItemInfos.size() - 1).placeCHName;
                        }
                    }
                }
                resultArrayList.add(photoInfo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            PictureAirLog.out("cursor close ---> getFavoritePhotoInfoListFromDB");
            if (cursor != null)
                cursor.close();
            if (cursor1 != null) {
                cursor1.close();
            }
            DBManager.getInstance().closeDatabase();
        }

        return resultArrayList;
    }


    /**
     * 插入问题答案信息
     *
     * @param questionArrayList
     */
//    public void insertIntoQuestionTable(ArrayList<QuestionInfo> questionArrayList) {
//        database = DBManager.getInstance().writData();
//        database.beginTransaction();
//        try {
//            database.delete(Common.HELP_QUESTION_TABLE, null, null);
//
//            System.out.println("title size" + questionArrayList.size());
//            String addsql = "insert into " + Common.HELP_QUESTION_TABLE + " values(null,?,?,?,?)";
//            for (int i = 0; i < questionArrayList.size(); i++) {
//                System.out.println("inserting data into database:" + i);
//                database.execSQL(addsql, new String[]{i + "", questionArrayList.get(i).questionName, questionArrayList.get(i).answer, PinYin.getPinYin(questionArrayList.get(i).questionName)});
//            }
//            database.setTransactionSuccessful();
//        } catch (Exception e) {
//            // TODO: handle exception
//        } finally {
//            database.endTransaction();
//            DBManager.getInstance().closeDatabase();
//        }
//    }

    /**
     * 在问题答案表中查询所有信息
     *
     * @return
     */
    public ArrayList<QuestionInfo> findAllQuestions() {
        ArrayList<QuestionInfo> resultArray = new ArrayList<QuestionInfo>();
        Cursor cursor = null;
        try {
            database = DBManager.getInstance().writData();
            PictureAirLog.out("cursor open ---> findAllQuestions");
            cursor = database.rawQuery("select * from " + Common.HELP_QUESTION_TABLE, null);
            QuestionInfo question = null;
            //			cursor.moveToFirst();
            while (cursor.moveToNext()) {
                question = new QuestionInfo();
                question.questionId = cursor.getString(cursor.getColumnIndex("questionId"));
                question.questionName = cursor.getString(cursor.getColumnIndex("question"));
                question.answer = cursor.getString(cursor.getColumnIndex("answer"));
                resultArray.add(question);
            }
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            if (cursor != null) {
                PictureAirLog.out("cursor close ---> findAllQuestions");
                cursor.close();
            }
            DBManager.getInstance().closeDatabase();
        }
        return resultArray;
    }

    /**
     * 查询问题表中的单个记录
     *
     * @param pinyin
     * @return
     */
    public ArrayList<QuestionInfo> queryQuestionInfo(String pinyin) {
        ArrayList<QuestionInfo> resultArray = new ArrayList<QuestionInfo>();
        database = DBManager.getInstance().writData();
        Cursor cursor = null;
        try {
            // 创建模糊查询的条件
            String likeStr = "'";
            String[] words = pinyin.split(" ");

            for (int i = 0; i < words.length; i++) {
                if (i < words.length - 1) {
                    likeStr += "%" + words[i];
                } else {
                    likeStr += "%" + words[i] + "%'";
                }
            }
            PictureAirLog.out("cursor open ---> queryQuestionInfo");
            cursor = database.rawQuery("select * from "
                    + Common.HELP_QUESTION_TABLE + " where pinyin like " + likeStr, null);
            QuestionInfo question = null;
            //			cursor.moveToFirst();
            while (cursor.moveToNext()) {
                question = new QuestionInfo();
                question.questionName = cursor.getString(cursor.getColumnIndex("question"));
                question.questionId = cursor.getString(cursor.getColumnIndex("questionId"));
                question.answer = cursor.getString(cursor.getColumnIndex("answer"));
                resultArray.add(question);
            }
            PictureAirLog.out("-------find the question and the size is " + resultArray.size());

        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            if (cursor != null) {
                PictureAirLog.out("cursor close ---> queryQuestionInfo");
                cursor.close();
            }
            DBManager.getInstance().closeDatabase();
        }

        return resultArray;
    }


    /**
     * 插入设置中的状态
     *
     * @param settingType 设置类型
     * @param userInfoId  用户ID
     */
    public void insertSettingStatus(String settingType, String userInfoId) {
        database = DBManager.getInstance().writData();
        database.beginTransaction();
        try {
            database.execSQL("insert into " + Common.FIRST_START_ACTIVITY_INFO_TABLE + " values(null,?,?)", new String[]{settingType, userInfoId});
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            DBManager.getInstance().closeDatabase();
        }
    }

    /**
     * 用户设置，删除数据
     *
     * @param settingType 设置类型
     * @param userInfoId  用户ID
     */
    public void deleteSettingStatus(String settingType, String userInfoId) {
        database = DBManager.getInstance().writData();
        database.beginTransaction();
        try {
            database.execSQL("delete from " + Common.FIRST_START_ACTIVITY_INFO_TABLE + " where activity = ? and userId = ?", new String[]{settingType, userInfoId});
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            DBManager.getInstance().closeDatabase();
        }
    }


    /**
     * 判断字段在数据库中是否存在
     * 有设置 就设置，没有设置就删除掉
     *
     * @param settingType 设置类型
     * @param userInfoId  用户ID
     * @return
     */
    public boolean checkFirstBuyPhoto(String settingType, String userInfoId) {
        Cursor cursor = null;
        boolean result = false;
        try {
            database = DBManager.getInstance().writData();
            PictureAirLog.out("cursor open ---> checkFirstBuyPhoto");
            cursor = database.rawQuery("select * from " + Common.FIRST_START_ACTIVITY_INFO_TABLE + " where activity = ? and userId = ?", new String[]{settingType, userInfoId});
            result = (cursor.getCount() > 0) ? true : false;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                PictureAirLog.out("cursor close ---> checkFirstBuyPhoto");
                cursor.close();
            }
            DBManager.getInstance().closeDatabase();
        }
        return result;
    }

    /**
     * 通过photoCode和shootOn查找数据库中的所有符合条件的照片
     *
     * @param photoCode
     * @param shootOn
     * @return
     */
    public ArrayList<PhotoInfo> getPhotoUrlByPhotoIDAndShootOn(String photoCode, String shootOn) {
        ArrayList<PhotoInfo> photoInfos = new ArrayList<PhotoInfo>();
        Cursor cursor = null;
        try {
            database = DBManager.getInstance().writData();
            PictureAirLog.out("cursor open ---> getPhotoUrlByPhotoIDAndShootOn");
            cursor = database.rawQuery("select * from " + Common.PHOTOPASS_INFO_TABLE + " where photoCode like ? and shootTime=? order by shootOn", new String[]{"%" + photoCode + "%", shootOn});

            PictureAirLog.d("查出来的数据。", "cursor.getCount(); ;" + cursor.getCount());
            if (cursor.moveToFirst()) {
                do {
                    PhotoInfo photoInfo = new PhotoInfo();
                    photoInfo.photoId = cursor.getString(cursor.getColumnIndex("photoId"));
                    photoInfo.photoPathOrURL = Common.PHOTO_URL + cursor.getString(cursor.getColumnIndex("originalUrl"));
                    photoInfos.add(photoInfo);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                PictureAirLog.out("cursor close ---> getPhotoUrlByPhotoIDAndShootOn");
                cursor.close();
            }
            DBManager.getInstance().closeDatabase();
        }
        return photoInfos;
    }

    /**
     * 根据pp列表获取对应的PPCodeInfo1列表
     *
     * @param ppCodeList ppCodeList
     * @param type       1 代表直接进入的 PP 页面， 2 代表是从selectPP进入，这个情况只显示模糊图
     * @return
     */
    public ArrayList<PPinfo> getPPCodeInfo1ByPPCodeList(Context c, ArrayList<PPinfo> ppCodeList, int type) {
        ArrayList<PPinfo> showPPCodeList = new ArrayList<PPinfo>();
        //获取需要显示的PP(去掉重复、隐藏的) (new add 选择PP+界面直接解析)
        if (type == 1) {
            for (int j = 0; j < ppCodeList.size(); j++) {
                if (j + 1 < ppCodeList.size() && ppCodeList.get(j).getPpCode().equals(ppCodeList.get(j + 1).getPpCode())) {
                    ppCodeList.remove(j);
                }
            }
        } else {

        }
        Cursor cursor = null;
        try {
            database = DBManager.getInstance().writData();
            ArrayList<HashMap<String, String>> urlList;
            ArrayList<PhotoInfo> selectPhotoItemInfos;
            HashMap<String, String> map;
            for (int i = 0; i < ppCodeList.size(); i++) {
                if (ppCodeList.get(i).getIsHidden() == 1) {
                    continue;
                }
                urlList = new ArrayList<>();
                selectPhotoItemInfos = new ArrayList<>();
                PPinfo ppInfo = ppCodeList.get(i);
                PictureAirLog.out("cursor open ---> getPPCodeInfo1ByPPCodeList" + cursor);
                if (type == 1) {
                    cursor = database.rawQuery("select * from " + Common.PHOTOPASS_INFO_TABLE
                            + " where photoCode like ? order by shootOn desc", new String[]{"%" + ppInfo.getPpCode() + "%"});
                    PictureAirLog.d("cursor cursor cursor ", "cursor :" + cursor.getCount());
                } else {
                    cursor = database.rawQuery("select * from " + Common.PHOTOPASS_INFO_TABLE +
                            " where photoCode like ? and isPay = 0 and shootTime = ? order by shootOn", new String[]{"%" + ppInfo.getPpCode() + "%", ppInfo.getShootDate()});
                }

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        // 获取图片路径
                        map = new HashMap<>();
                        map.put("url", cursor.getString(cursor.getColumnIndex("previewUrl")));
                        map.put("isVideo", cursor.getInt(cursor.getColumnIndex("isVideo")) + "");
                        urlList.add(map);
                        PhotoInfo sInfo = AppUtil.getPhotoInfoFromCursor(cursor);
                        selectPhotoItemInfos.add(sInfo);
                    } while (cursor.moveToNext());

                }
                if (type == 2) {
                    Collections.reverse(urlList);
                }

                int count = urlList.size();
                if (count < 6) {//不满6或者12的，需要补全
                    for (int j = 6 - count; j > 0; j--) {
                        map = new HashMap<>();
                        map.put("url", GlideUtil.getDrawableUrl(c, R.drawable.default_pp));
                        map.put("isVideo", "0");
                        urlList.add(map);
                    }
                } else if (count < 12) {
                    for (int j = 12 - count; j > 0; j--) {
                        map = new HashMap<>();
                        map.put("url", GlideUtil.getDrawableUrl(c, R.drawable.default_pp));
                        map.put("isVideo", "0");
                        urlList.add(map);
                    }
                }
                PPinfo ppInfo1 = new PPinfo();
                ppInfo1.setPpCode(ppInfo.getPpCode());
                ppInfo1.setShootDate(ppInfo.getShootDate());
                ppInfo1.setUrlList(urlList);
                ppInfo1.setSelectPhotoItemInfos(selectPhotoItemInfos);
                ppInfo1.setPhotoCount(count);
                showPPCodeList.add(ppInfo1);
                if (cursor != null) {
                    PictureAirLog.out("cursor close ---> getPPCodeInfo1ByPPCodeList");
                    cursor.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 处理完了，通知处理之后的信息
            if (cursor != null) {
                PictureAirLog.out("cursor close ---> getPPCodeInfo1ByPPCodeList");
                cursor.close();
            }
            DBManager.getInstance().closeDatabase();
        }
        return showPPCodeList;
    }

    /**
     * 根据ppCode获取对应的PP照片列表
     * @param ppCode
     */
    public ArrayList<PhotoInfo> getPhotoInfosByPPCode(String ppCode, ArrayList<DiscoverLocationItemInfo> locationItemInfos, String language) {
        Cursor cursor = null;
        ArrayList<PhotoInfo> selectPhotoItemInfos = new ArrayList<>();
        try {
            database = DBManager.getInstance().writData();
            cursor = database.rawQuery("select * from " + Common.PHOTOPASS_INFO_TABLE
                    + " where photoCode like ? order by shootOn desc", new String[]{"%" + ppCode + "%"});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // 获取图片路径
                    PhotoInfo photoInfo = AppUtil.getPhotoInfoFromCursor(cursor);
                    for (int i = 0; i < locationItemInfos.size(); i++) {
                        if (photoInfo.locationId.equals(locationItemInfos.get(i).locationId) || locationItemInfos.get(i).locationIds.contains(photoInfo.locationId)) {
                            if (language.equals(Common.ENGLISH)) {
                                photoInfo.locationName = locationItemInfos.get(i).placeENName;
                            } else if (language.equals(Common.SIMPLE_CHINESE)) {
                                photoInfo.locationName = locationItemInfos.get(i).placeCHName;
                            }
                            break;
                        }
                    }
                    if (TextUtils.isEmpty(photoInfo.locationName)) {
                        if (language.equals(Common.ENGLISH)) {
                            photoInfo.locationName = locationItemInfos.get(locationItemInfos.size() - 1).placeENName;
                        } else if (language.equals(Common.SIMPLE_CHINESE)) {
                            photoInfo.locationName = locationItemInfos.get(locationItemInfos.size() - 1).placeCHName;
                        }
                    }
                    selectPhotoItemInfos.add(photoInfo);
                } while (cursor.moveToNext());

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 处理完了，通知处理之后的信息
            if (cursor != null) {
                PictureAirLog.out("cursor close ---> getPPCodeInfo1ByPPCodeList");
                cursor.close();
            }
            DBManager.getInstance().closeDatabase();
        }
        return selectPhotoItemInfos;
    }

    /**
     * 更新照片的信息
     * @param photo
     */
    public void updatePhotoInfo(PhotoInfo photo) {
        database = DBManager.getInstance().writData();
        try {
            database.execSQL("update " + Common.PHOTOPASS_INFO_TABLE + " set photoCode = ?, " +
                            "shootTime = ?, originalUrl = ?, previewUrl = ?, previewUrl_512 = ?, " +
                            "previewUrl_1024 = ?, locationId = ?, shootOn = ?, " +
                            "isPay = ?, locationName = ?, locationCountry = ?, shareURL = ?," +
                            "fileSize = ?, videoWidth = ?, videoHeight = ?, isHasPreset = ?, enImg = ? where photoId = ?",
                    new String[]{photo.photoPassCode, photo.shootTime, photo.photoPathOrURL,
                            photo.photoThumbnail, photo.photoThumbnail_512, photo.photoThumbnail_1024,
                            photo.locationId, photo.shootOn, photo.isPayed + "", photo.locationName,
                            photo.locationCountry, photo.shareURL, photo.fileSize + "",
                            photo.videoWidth + "", photo.videoHeight + "", photo.isHasPreset + "", photo.isEncrypted + "", photo.photoId});

            database.execSQL("update " + Common.FAVORITE_INFO_TABLE + " set photoCode = ?, " +
                            "shootTime = ?, originalUrl = ?, previewUrl = ?, previewUrl_512 = ?, " +
                            "previewUrl_1024 = ?, locationId = ?, shootOn = ?, " +
                            "isPay = ?, locationName = ?, locationCountry = ?, shareURL = ?," +
                            "fileSize = ?, videoWidth = ?, videoHeight = ?, isHasPreset = ?, enImg = ? where photoId = ?",
                    new String[]{photo.photoPassCode, photo.shootTime, photo.photoPathOrURL,
                            photo.photoThumbnail, photo.photoThumbnail_512, photo.photoThumbnail_1024,
                            photo.locationId, photo.shootOn, photo.isPayed + "", photo.locationName,
                            photo.locationCountry, photo.shareURL,  photo.fileSize + "",
                            photo.videoWidth + "", photo.videoHeight + "", photo.isHasPreset + "", photo.isEncrypted + "", photo.photoId});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.getInstance().closeDatabase();
        }
    }

    /**
     * 更新指定照片的购买状态
     *
     * @param selectedPhotoId 指定照片ID
     */
    public void updatePhotoBought(String selectedPhotoId, boolean isDelete) {
        database = DBManager.getInstance().writData();
        try {
            if (isDelete) {//删除操作
                database.execSQL("delete from " + Common.PHOTOPASS_INFO_TABLE + " where photoId = ?", new String[]{selectedPhotoId});
                database.execSQL("delete from " + Common.FAVORITE_INFO_TABLE + " where photoId = ?", new String[]{selectedPhotoId});
            } else {//同步操作
                database.execSQL("update " + Common.PHOTOPASS_INFO_TABLE + " set isPay = 1 where photoId = ?", new String[]{selectedPhotoId});
                database.execSQL("update " + Common.FAVORITE_INFO_TABLE + " set isPay = 1 where photoId = ?", new String[]{selectedPhotoId});
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.getInstance().closeDatabase();
        }
    }

    /**
     * 更新指定照片的购买状态
     *
     * @param ppCode    pp码
     * @param shootDate 绑定时间
     */
    public void updatePhotoBoughtByPPCodeAndDate(String ppCode, String shootDate, boolean isDelete) {
        database = DBManager.getInstance().writData();
        database.beginTransaction();
        try {
            if (isDelete) {//删除操作
                database.execSQL("delete from " + Common.PHOTOPASS_INFO_TABLE + " where photoCode like ?", new String[]{"%" + ppCode + "%"});
                database.execSQL("delete from " + Common.FAVORITE_INFO_TABLE + " where photoCode like ?", new String[]{"%" + ppCode + "%"});
            } else {//同步
                database.execSQL("update " + Common.PHOTOPASS_INFO_TABLE + " set isPay = 1 where photoCode like ? and shootTime = ?", new String[]{"%" + ppCode + "%", shootDate});
                database.execSQL("update " + Common.FAVORITE_INFO_TABLE + " set isPay = 1 where photoCode like ? and shootTime = ?", new String[]{"%" + ppCode + "%", shootDate});
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            DBManager.getInstance().closeDatabase();
        }
    }

    /**
     * 检查是不是第一次
     *
     * @param activity
     * @param userID
     */
    public boolean checkFirstTimeStartActivity(String activity, String userID) {
        boolean result = false;
        Cursor cursor = null;
        try {
            //第一次进入，判断数据库中是否存在当前UserId，如果不存在，则第一次进入
            database = DBManager.getInstance().writData();
            PictureAirLog.out("cursor open ---> checkFirstTimeStartActivity");
            cursor = database.rawQuery("select * from " + Common.FIRST_START_ACTIVITY_INFO_TABLE + " where activity = ? and userId = ?", new String[]{activity, userID});
            if (cursor.getCount() == 0) {//说明没有数据，则为第一次进入
                database.execSQL("insert into " + Common.FIRST_START_ACTIVITY_INFO_TABLE + " values(null,?,?)", new String[]{activity, userID});
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                PictureAirLog.out("cursor close ---> checkFirstTimeStartActivity");
                cursor.close();
            }
            DBManager.getInstance().closeDatabase();
        }
        return result;
    }

    /**
     * 删除photopassInfo中的内容
     *
     * @param tableName 需要清空的表的名字
     */
    public void deleteAllInfoFromTable(String tableName) {
        database = DBManager.getInstance().writData();
        try {
            database.execSQL("delete from " + tableName);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.getInstance().closeDatabase();
        }
    }

    /**
     * 将照片插入到photoPassInfo表中
     *
     * @param responseArray
     * @param type         是否是刷新信息
     */
    public synchronized ArrayList<PhotoInfo> insertPhotoInfoIntoPhotoPassInfo(JSONArray responseArray, int type) {
        ArrayList<PhotoInfo> resultArrayList = new ArrayList<PhotoInfo>();
        if (responseArray.size() == 0) {
            return resultArrayList;
        }
        database = DBManager.getInstance().writData();
        database.beginTransaction();
        Cursor cursor = null;
        try {
            for (int i = 0; i < responseArray.size(); i++) {
                JSONObject object = responseArray.getJSONObject(i);
                PhotoInfo photo = JsonUtil.getPhoto(object);
                if (photo.locationId == null || photo.locationId.equals("")) {
                    photo.locationId = "others";
                }

                if (type == API1.GET_DEFAULT_PHOTOS) {
                    if (i == 0) {//记录最新的值
                        SPUtils.put(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.LAST_UPDATE_TOP_PHOTO, photo.photoId);
                    } else if (i == responseArray.size() - 1) {//记录最后一个值
                        SPUtils.put(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.LAST_UPDATE_BOTTOM_PHOTO, photo.photoId);
                    }
                } else if (type == API1.GET_NEW_PHOTOS) {
                        SPUtils.put(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.LAST_UPDATE_TOP_PHOTO, photo.photoId);

                } else if (type == API1.GET_OLD_PHOTOS) {
                        SPUtils.put(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.LAST_UPDATE_BOTTOM_PHOTO, photo.photoId);

                }


                if (type == API1.GET_NEW_PHOTOS) {
                    //1.先查询数据库是否有新的数据，如果有，则更新信息
                    //2.如果没有，则插入
                    PictureAirLog.out("cursor open ---> insertPhotoInfoIntoPhotoPassInfo");
                    cursor = database.rawQuery("select * from " + Common.PHOTOPASS_INFO_TABLE + " where photoId = ?", new String[]{photo.photoId});
                    if (cursor.getCount() > 0) {//说明存在此数据，需要更新下数据
                        database.execSQL("update " + Common.PHOTOPASS_INFO_TABLE + " set photoCode = ?, " +
                                "shootTime = ?, originalUrl = ?, previewUrl = ?, previewUrl_512 = ?, " +
                                "previewUrl_1024 = ?, locationId = ?, shootOn = ?, " +
                                "isPay = ?, locationName = ?, locationCountry = ?, shareURL = ?," +
                                "fileSize = ?, videoWidth = ?, videoHeight = ?, isHasPreset = ?, enImg = ? where photoId = ?",
                                new String[]{photo.photoPassCode, photo.shootTime, photo.photoPathOrURL,
                                        photo.photoThumbnail, photo.photoThumbnail_512, photo.photoThumbnail_1024,
                                        photo.locationId, photo.shootOn, photo.isPayed + "", photo.locationName,
                                        photo.locationCountry, photo.shareURL, photo.fileSize + "",
                                        photo.videoWidth + "", photo.videoHeight + "", photo.isHasPreset + "", photo.isEncrypted + "", photo.photoId});

                        database.execSQL("update " + Common.FAVORITE_INFO_TABLE + " set photoCode = ?, " +
                                "shootTime = ?, originalUrl = ?, previewUrl = ?, previewUrl_512 = ?, " +
                                "previewUrl_1024 = ?, locationId = ?, shootOn = ?, " +
                                "isPay = ?, locationName = ?, locationCountry = ?, shareURL = ?," +
                                "fileSize = ?, videoWidth = ?, videoHeight = ?, isHasPreset = ?, enImg = ? where photoId = ?",
                                new String[]{photo.photoPassCode, photo.shootTime, photo.photoPathOrURL,
                                        photo.photoThumbnail, photo.photoThumbnail_512, photo.photoThumbnail_1024,
                                        photo.locationId, photo.shootOn, photo.isPayed + "", photo.locationName,
                                        photo.locationCountry, photo.shareURL,  photo.fileSize + "",
                                        photo.videoWidth + "", photo.videoHeight + "", photo.isHasPreset + "", photo.isEncrypted + "", photo.photoId});

                        photo.isRefreshInfo = 1;
                        cursor.close();
                        resultArrayList.add(photo);
                        continue;
                    } else {
                        cursor.close();
                    }
                }

                resultArrayList.add(photo);
                //将数据插入到数据库
                database.execSQL("insert into " + Common.PHOTOPASS_INFO_TABLE + " values(null,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", new String[]{
                        photo.photoId, photo.photoPassCode, photo.shootTime, photo.photoPathOrURL,
                        photo.photoThumbnail, photo.photoThumbnail_512, photo.photoThumbnail_1024,
                        photo.locationId, photo.shootOn, 0 + "", photo.isPayed + "", photo.locationName,
                        photo.locationCountry, photo.shareURL, photo.isVideo + "", photo.fileSize + "",
                        photo.videoWidth + "", photo.videoHeight + "", photo.isHasPreset + "", photo.isEncrypted + "", photo.adURL});
            }

            database.setTransactionSuccessful();
        } catch (JSONException e1) {
            e1.printStackTrace();
        } finally {
            if (cursor != null) {
                PictureAirLog.out("cursor close ---> insertPhotoInfoIntoPhotoPassInfo");
                cursor.close();
            }
            database.endTransaction();
            DBManager.getInstance().closeDatabase();
        }
        return resultArrayList;
    }

    /**
     * 删除数据库中的照片（照片表和收藏表）
     * @param list
     * @param ppCode
     */
    public void deletePhotosFromPhotoInfoAndFavorite(ArrayList<PhotoInfo> list, String ppCode) {
        database = DBManager.getInstance().writData();
        database.beginTransaction();
        try {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).photoPassCode.equals(ppCode)) {//只有一张卡
                    database.execSQL("delete from " + Common.PHOTOPASS_INFO_TABLE + " where photoId = ? and photoCode = ?", new String[]{list.get(i).photoId, ppCode});
                    database.execSQL("delete from " + Common.FAVORITE_INFO_TABLE + " where photoId = ? and photoCode = ?", new String[]{list.get(i).photoId, ppCode});
                } else {//有多张卡
                    String newPPCode = list.get(i).photoPassCode.replace(ppCode, "");
                    database.execSQL("update " + Common.PHOTOPASS_INFO_TABLE + " set photoCode = ? where photoId = ?", new String[]{newPPCode, list.get(i).photoId});
                    database.execSQL("update " + Common.FAVORITE_INFO_TABLE + " set photoCode = ? where photoId = ?", new String[]{newPPCode, list.get(i).photoId});
                }
            }
            database.setTransactionSuccessful();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            DBManager.getInstance().closeDatabase();
        }
    }

    /**
     * 根据ppCode删除对应的照片
     *
     * @param position 删除的position
     * @param ppList   原始ppCode列表
     */
    public void removePhotosFromUserByPPCode(int position, ArrayList<PPinfo> ppList) {
        /**
         * 删除步骤
         * 1.获取删除ppcode对应的所有图片
         * 2.获取删除图片对应的ppcode
         * 3.遍历pp列表中其他pp
         * 4.检查是否其他pp的code在删除图片对应的ppcode中
         * 5.如果在，则不删除，如果不在，则删除
         */
        database = DBManager.getInstance().writData();
        database.beginTransaction();
        String deletePPCode;
        boolean needDelete = true;
        ArrayList<PhotoInfo> deletePhotos = new ArrayList<>();
        //1
        deletePhotos.addAll(ppList.get(position).getSelectPhotoItemInfos());

        try {
            for (int i = 0; i < deletePhotos.size(); i++) {
                //2
                deletePPCode = deletePhotos.get(i).photoPassCode;
                PictureAirLog.out("deletePPCode--->" + deletePPCode);
                //3
                for (int j = 0; j < ppList.size(); j++) {
                    if (j == position) {
                        continue;
                    }

                    //4
                    if (deletePPCode.contains(ppList.get(j).getPpCode())) {
                        needDelete = false;
                        break;
                    }
                }

                //5
                if (needDelete) {//需要删除
                    database.execSQL("delete from " + Common.PHOTOPASS_INFO_TABLE + " where photoId = ?", new String[]{deletePhotos.get(i).photoId});
                    database.execSQL("delete from " + Common.FAVORITE_INFO_TABLE + " where photoId = ?", new String[]{deletePhotos.get(i).photoId});
                } else {
                    needDelete = true;
                }
            }

            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            DBManager.getInstance().closeDatabase();
        }
    }

    /**
     * 查询数据库中的图片信息
     *
     * @return
     */
    public synchronized ArrayList<PhotoInfo> getAllPhotoFromPhotoPassInfo(boolean exceptVideo, String deleteTime) {
        ArrayList<PhotoInfo> resultArrayList = new ArrayList<PhotoInfo>();
        database = DBManager.getInstance().writData();
        //根据当前时间，删除超过30天并且未支付的数据信息
        /**
         * 1.获取当前时间，以毫秒为单位
         * 2.删除数据库数据，条件1.未购买的图片，2.当前时间 - 30天的时间 > 数据库的时间
         */
        database.execSQL("delete from " + Common.PHOTOPASS_INFO_TABLE + " where isPay = 0 and shootOn < datetime(?)", new String[]{deleteTime});

        //删除过期的数据之后，再查询photo表的信息
        PictureAirLog.out("cursor open ---> getAllPhotoFromPhotoPassInfo");
        Cursor cursor;
        if (exceptVideo) {
            cursor = database.rawQuery("select * from " + Common.PHOTOPASS_INFO_TABLE + " where isVideo = 0 order by shootOn desc", null);
        } else{
            cursor = database.rawQuery("select * from " + Common.PHOTOPASS_INFO_TABLE + " order by shootOn desc", null);
        }
        PhotoInfo photoInfo;
        if (cursor.moveToFirst()) {//判断是否photo数据
            do {
                photoInfo = AppUtil.getPhotoInfoFromCursor(cursor);
                resultArrayList.add(photoInfo);
            } while (cursor.moveToNext());
        }
        PictureAirLog.out("cursor close ---> getAllPhotoFromPhotoPassInfo");
        cursor.close();
        DBManager.getInstance().closeDatabase();
        return resultArrayList;
    }

    /**
     * 查询数据库中的图片信息
     *
     * @return
     */
    public ArrayList<PhotoInfo> getPhotoFromPhotoPassInfo(String deleteTime, boolean hasBought) {
        ArrayList<PhotoInfo> resultArrayList = new ArrayList<PhotoInfo>();
        database = DBManager.getInstance().writData();
        //根据当前时间，删除超过30天并且未支付的数据信息
        /**
         * 1.获取当前时间，以毫秒为单位
         * 2.删除数据库数据，条件1.未购买的图片，2.当前时间 - 30天的时间 > 数据库的时间
         */
        database.execSQL("delete from " + Common.PHOTOPASS_INFO_TABLE + " where isPay = 0 and shootOn < datetime(?)", new String[]{deleteTime});

        //删除过期的数据之后，再查询photo表的信息
        Cursor cursor = database.rawQuery("select * from " + Common.PHOTOPASS_INFO_TABLE + " where isPay = ? order by shootOn desc", new String[]{hasBought ? "1" : "0"});
        PhotoInfo photoInfo;
        if (cursor.moveToFirst()) {//判断是否photo数据
            do {
                photoInfo = AppUtil.getPhotoInfoFromCursor(cursor);
                resultArrayList.add(photoInfo);
            } while (cursor.moveToNext());
        }
        cursor.close();
        DBManager.getInstance().closeDatabase();
        return resultArrayList;
    }

    /**
     * 判断这个video是否有原始视频链接
     * @param photoId
     * @return
     */
    public boolean needGetLastestVideoInfoFromNetwork(String photoId) {
        database = DBManager.getInstance().writData();
        Cursor cursor = null;
        PhotoInfo photoInfo = null;
        try {
            cursor = database.rawQuery("select * from " + Common.PHOTOPASS_INFO_TABLE + " where photoId = ?", new String[]{photoId});
            if (cursor.moveToFirst()) {//判断是否photo数据
                photoInfo = AppUtil.getPhotoInfoFromCursor(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            DBManager.getInstance().closeDatabase();
        }

        return AppUtil.isOldVersionOfTheVideo(photoInfo.photoPathOrURL, photoInfo.photoThumbnail_1024, photoInfo.photoThumbnail_512, photoInfo.photoThumbnail);
    }

    /**
     * 从数据库中查询边框和饰品信息
     *
     * @param frame 边框的话为1，饰品的话为0
     * @return
     */
    public ArrayList<FrameOrStikerInfo> getLastContentDataFromDB(int frame) {
        ArrayList<FrameOrStikerInfo> resultArrayList = new ArrayList<FrameOrStikerInfo>();
        database = DBManager.getInstance().writData();
        PictureAirLog.out("cursor open ---> getLastContentDataFromDB");
        Cursor cursor = database.rawQuery("select * from " + Common.FRAME_STICKER_TABLES + " where isActive = ? and fileType = ?", new String[]{"1", frame + ""});
        FrameOrStikerInfo frameInfo;
        if (cursor.moveToFirst()) {//判断是否有数据
            do {
                frameInfo = new FrameOrStikerInfo();
                frameInfo.frameName = cursor.getString(cursor.getColumnIndex("frameName"));
                frameInfo.frameOriginalPathLandscape = cursor.getString(cursor.getColumnIndex("originalPathLandscape"));
                frameInfo.frameOriginalPathPortrait = cursor.getString(cursor.getColumnIndex("originalPathPortrait"));
                frameInfo.frameThumbnailPathLandscape400 = cursor.getString(cursor.getColumnIndex("thumbnailPathLandscape400"));
                frameInfo.frameThumbnailPathPortrait400 = cursor.getString(cursor.getColumnIndex("thumbnailPathPortrait400"));
                frameInfo.frameThumbnailPathH160 = cursor.getString(cursor.getColumnIndex("thumbnailPathH160"));
                frameInfo.frameThumbnailPathV160 = cursor.getString(cursor.getColumnIndex("thumbnailPathV160"));
                frameInfo.locationId = cursor.getString(cursor.getColumnIndex("locationId"));
                frameInfo.isActive = cursor.getInt(cursor.getColumnIndex("isActive"));
                frameInfo.onLine = cursor.getInt(cursor.getColumnIndex("onLine"));
                frameInfo.isDownload = cursor.getInt(cursor.getColumnIndex("isDownload"));
                frameInfo.fileSize = cursor.getInt(cursor.getColumnIndex("fileSize"));
                resultArrayList.add(frameInfo);
            } while (cursor.moveToNext());
        }
        PictureAirLog.out("cursor close ---> getLastContentDataFromDB");
        cursor.close();
        DBManager.getInstance().closeDatabase();
        return resultArrayList;
    }

    /**
     * 插入边框和饰品
     *
     * @param jsonArray
     * @param isFrame   是不是边框
     * @throws JSONException
     */
    private void insertFrameAndSticker(JSONArray jsonArray, boolean isFrame) throws JSONException {
        FrameOrStikerInfo frameInfo = null;
        try {
            if (jsonArray.size() > 0) {
                PictureAirLog.d(TAG, "frames or sticker length is " + jsonArray.size());
                //开始解析数据，并且将数据写入数据库
                for (int i = 0; i < jsonArray.size(); i++) {
                    //解析json
                    if (isFrame) {
                        frameInfo = JsonUtil.getFrameInfo(jsonArray.getJSONObject(i));

                    } else {
                        frameInfo = JsonUtil.getStickerInfo(jsonArray.getJSONObject(i));
                    }
                    //插入数据
                    if (frameInfo.isActive == 1) {
                        database.execSQL("insert into " + Common.FRAME_STICKER_TABLES + " values(null,?,?,?,?,?,?,?,?,?,?,?,?,?)", new String[]{
                                frameInfo.frameName, frameInfo.frameOriginalPathLandscape, frameInfo.frameOriginalPathPortrait, frameInfo.frameThumbnailPathLandscape400
                                , frameInfo.frameThumbnailPathPortrait400, frameInfo.frameThumbnailPathH160, frameInfo.frameThumbnailPathV160, frameInfo.locationId, frameInfo.isActive + "", frameInfo.onLine + "",
                                frameInfo.isDownload + "", frameInfo.fileSize + "", isFrame ? "1" : "0"});//测试代码，需要修改。
                    } else {//如果为0，说明需要修改以前的数据状态
                        //根据边框或者饰品名字修改使用状态
                        database.execSQL("update " + Common.FRAME_STICKER_TABLES + " set isActive = 0 where frameName = ? and fileType = ?", new String[]{frameInfo.frameName, isFrame ? "1" : "0"});
                    }
                }
            } else {
                PictureAirLog.d(TAG, "has no any frames or stickers");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将边框和饰品信息解析并且写入数据库
     *
     * @param jsonObject
     */
    public void insertFrameAndStickerIntoDB(JSONObject jsonObject) {
        database = DBManager.getInstance().writData();
        database.beginTransaction();
        try {
            if (jsonObject.containsKey("frames")) {
                insertFrameAndSticker(jsonObject.getJSONArray("frames"), true);
            }
            if (jsonObject.containsKey("cliparts")) {
                insertFrameAndSticker(jsonObject.getJSONArray("cliparts"), false);
            }
            database.setTransactionSuccessful();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            database.endTransaction();
            DBManager.getInstance().closeDatabase();
        }
    }

    /**
     * 更新边框下载状态
     *
     * @param name  边框名字
     * @param frame 边框为1，饰品为0
     */
    public void updateFrameAndStickerDownloadStatus(String name, int frame) {
        database = DBManager.getInstance().writData();
        database.beginTransaction();
        try {
            database.execSQL("update " + Common.FRAME_STICKER_TABLES + " set isDownload = 1 where frameName = ? and fileType = ?", new String[]{name, frame + ""});
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            DBManager.getInstance().closeDatabase();
        }
    }

    /**
     * 添加已支付的订单ID
     */
    public void insertPaymentOrderIdDB(String userId, String orderId) {
        database = DBManager.getInstance().writData();
        database.beginTransaction();
        try {
            database.execSQL("insert into " + Common.PAYMENT_ORDER + " values(null,?,?)", new String[]{userId, orderId});
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            DBManager.getInstance().closeDatabase();
        }

    }

    /**
     * 查询已支付订单
     *
     * @return
     */
    public List<String> searchPaymentOrderIdDB() {
        List<String> orderIds = new ArrayList<>();
        Cursor cursor = null;
        try {
            database = DBManager.getInstance().readData();
            PictureAirLog.out("cursor open ---> searchPaymentOrderIdDB");
            cursor = database.query(Common.PAYMENT_ORDER, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    orderIds.add(cursor.getString(cursor.getColumnIndex("orderId")));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                PictureAirLog.out("cursor close ---> searchPaymentOrderIdDB");
                cursor.close();
            }
            DBManager.getInstance().closeDatabase();
        }
        return orderIds;
    }

    /**
     * 删除已支付的订单ID
     */
    public void removePaymentOrderIdDB(String orderId) {
        try {
            database = DBManager.getInstance().writData();
            database.execSQL("delete from " + Common.PAYMENT_ORDER + " where orderId = ? ", new String[]{orderId});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.getInstance().closeDatabase();
        }

    }

    /**
     * 插入广告地点信息
     * 1.先清除广告数据
     * 2.再插入广告数据
     *
     * @param jsonArray
     */
    public void insertADLocations(JSONArray jsonArray) {
        database = DBManager.getInstance().writData();
        database.beginTransaction();
        try {
            database.execSQL("delete from " + Common.AD_LOCATION);
            JSONObject jsonObject;
            String locationId, adCH, adEN;
            JSONObject adJsonObject;
            for (int i = 0; i < jsonArray.size(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                locationId = jsonObject.getString("locationId");
                adJsonObject = jsonObject.getJSONObject("adWords");
                adCH = adJsonObject.getString("CN");
                adEN = adJsonObject.getString("EN");
                database.execSQL("insert into " + Common.AD_LOCATION + " values(null,?,?,?)",
                        new String[]{locationId, adCH, adEN});
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            DBManager.getInstance().closeDatabase();
        }
    }

    /**
     * 插入广告地点信息
     * 1.先清除广告数据
     * 2.再插入广告数据
     *
     * @param jsonArray
     */
    public String insertADLocations(JSONArray jsonArray, String photoLocationId, String language) {
        String result = "";
        database = DBManager.getInstance().writData();
        database.beginTransaction();
        try {
            database.execSQL("delete from " + Common.AD_LOCATION);
            JSONObject jsonObject;
            String locationId, adCH, adEN;
            JSONObject adJsonObject;
            for (int i = 0; i < jsonArray.size(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                locationId = jsonObject.getString("locationId");
                adJsonObject = jsonObject.getJSONObject("adWords");
                adCH = adJsonObject.getString("CN");
                adEN = adJsonObject.getString("EN");
                database.execSQL("insert into " + Common.AD_LOCATION + " values(null,?,?,?)",
                        new String[]{locationId, adCH, adEN});
                if (photoLocationId.equals(locationId)) {
                    if (language.equals(Common.SIMPLE_CHINESE)) {
                        result = adCH;
                    } else {
                        result = adEN;
                    }
                }
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            DBManager.getInstance().closeDatabase();
        }
        return result;
    }

    /**
     * 根据locationId获取广告信息
     *
     * @param locationId
     * @param language
     * @return
     */
    public String getADByLocationId(String locationId, String language) {
        String ad = "";
        Cursor cursor = null;
        try {
            database = DBManager.getInstance().writData();
            PictureAirLog.out("cursor open ---> getADByLocationId");
            cursor = database.rawQuery("select * from " + Common.AD_LOCATION + " where locationId = ?", new String[]{locationId});
            if (cursor.moveToFirst()) {
                if (language.equals(Common.SIMPLE_CHINESE)) {
                    ad = cursor.getString(cursor.getColumnIndex("descriptionCH"));
                } else {
                    ad = cursor.getString(cursor.getColumnIndex("descriptionEN"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                PictureAirLog.out("cursor close ---> getADByLocationId");
                cursor.close();
            }
            DBManager.getInstance().closeDatabase();
        }
        return ad;
    }


    /**
     * * 是否存在下载线程信息
     *
     * @param url
     * @param threadId
     * @return
     */
    public boolean isExistsThread(String url, int threadId) {
        Cursor cursor = null;
        boolean exists = false;
        try {
            database = DBManager.getInstance().writData();
            cursor = database.rawQuery("select * from " + Common.THREAD_INFO + " where url = ? and thread_id = ?", new String[]{url, threadId + ""});
            exists = cursor.moveToNext();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            DBManager.getInstance().closeDatabase();
        }
        return exists;
    }

    /**
     * 查看所有下载线程
     *
     * @return
     */
    public boolean isExistsThread() {
        Cursor cursor = null;
        boolean exists = false;
        try {
            database = DBManager.getInstance().writData();
            cursor = database.rawQuery("select * from " + Common.THREAD_INFO,null);
            exists = cursor.moveToNext();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            DBManager.getInstance().closeDatabase();
        }
        return exists;
    }

    /**
     * 读取下载线程信息
     *
     * @param url
     */
    public List<ThreadInfo> getTreads(String url) {
        List<ThreadInfo> list = new ArrayList<ThreadInfo>();
        Cursor cursor = null;
        try {
            database = DBManager.getInstance().writData();
            cursor = database.rawQuery("select * from " + Common.THREAD_INFO + " where url = ?", new String[]{url});
            while (cursor.moveToNext()) {
                ThreadInfo threadInfo = new ThreadInfo();
                threadInfo.setId(cursor.getInt(cursor.getColumnIndex("thread_id")));
                threadInfo.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                threadInfo.setFinished(cursor.getInt(cursor.getColumnIndex("finished")));
                threadInfo.setEnd(cursor.getInt(cursor.getColumnIndex("end")));
                threadInfo.setStart(cursor.getInt(cursor.getColumnIndex("start")));
                list.add(threadInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            DBManager.getInstance().closeDatabase();
        }
        return list;
    }


    /**
     * 更新下载线程
     *
     * @param url
     * @param threadId
     * @param finished
     */
    public void updateThread(String url, int threadId, long finished) {
        database = DBManager.getInstance().writData();
        try {
            database.execSQL("update " + Common.THREAD_INFO + " set finished = ? where url = ? and thread_id = ?",
                    new Object[]{finished, url, threadId});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.getInstance().closeDatabase();
        }
    }


    /**
     * 删除下载线程
     *
     * @param url
     * @param threadId
     */
    public void deleteThread(String url, int threadId) {
        database = DBManager.getInstance().writData();
        try {
            database.execSQL("delete from " + Common.THREAD_INFO + " where url = ? and thread_id = ?",
                    new Object[]{url, threadId});
        }catch (Exception e){
            PictureAirLog.e(TAG, "删除失败：" + e.getMessage());
        }finally {
            DBManager.getInstance().closeDatabase();
        }
    }

    /**
     *
     * @param threadInfo
     */
    public void insertThread(ThreadInfo threadInfo) {
        database = DBManager.getInstance().writData();
        database.beginTransaction();
        try {
            database.execSQL("insert into " + Common.THREAD_INFO + "(thread_id,url,start,end,finished) values(?,?,?,?,?)",
                    new Object[]{threadInfo.getId(), threadInfo.getUrl(), threadInfo.getStart(), threadInfo.getEnd(), threadInfo.getFinished()});
            database.setTransactionSuccessful();

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        } finally {
            database.endTransaction();
            DBManager.getInstance().closeDatabase();
        }
    }

    /**
     * 获取对应状态的photo信息
     * @param userId
     * @param success 状态有 成功 “true”  失败 “false”  下载中 “load”
     * */
    public List<PhotoDownLoadInfo> getPhotos(String userId,String  success){
        List<PhotoDownLoadInfo> photos = new ArrayList<>();
        database = DBManager.getInstance().readData();
        PictureAirLog.out("cursor open ---> getPhotos");
        Cursor cursor = database.rawQuery("select * from " + Common.PHOTOS_LOAD + " where userId = ? and success = ? order by downloadTime", new String[]{userId,success});
        try {
            if (cursor.moveToFirst()) {//判断是否photo数据
                do {
                    PhotoDownLoadInfo photoInfo = new PhotoDownLoadInfo();
                    photoInfo.setPhotoId(cursor.getString(cursor.getColumnIndex("photoId")));
                    photoInfo.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                    photoInfo.setSize(cursor.getString(cursor.getColumnIndex("size")));
                    photoInfo.setPreviewUrl(cursor.getString(cursor.getColumnIndex("previewUrl")));
                    photoInfo.setShootTime(cursor.getString(cursor.getColumnIndex("shootTime")));
                    photoInfo.setLoadTime(cursor.getString(cursor.getColumnIndex("downloadTime")));
                    photoInfo.setIsVideo(cursor.getInt(cursor.getColumnIndex("isVideo")));
                    photoInfo.setFailedTime(cursor.getString(cursor.getColumnIndex("failedTime")));
                    photoInfo.setPhotoThumbnail_512(cursor.getString(cursor.getColumnIndex("photoThumbnail_512")));
                    photoInfo.setPhotoThumbnail_1024(cursor.getString(cursor.getColumnIndex("photoThumbnail_1024")));
                    photoInfo.setVideoWidth(cursor.getInt(cursor.getColumnIndex("videoWidth")));
                    photoInfo.setVideoHeight(cursor.getInt(cursor.getColumnIndex("videoHeight")));
                    photos.add(photoInfo);
                } while (cursor.moveToNext());
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (cursor != null) {
                cursor.close();
            }
            DBManager.getInstance().closeDatabase();
        }
        return photos;
    }

    /**
     * 获取对应状态的photo信息
     * @param userId
     * @param status1 状态有 成功 “true”  失败 “false”  下载中 “load” 原图上传中 upload
     * @param status2 状态有 成功 “true”  失败 “false”  下载中 “load” 原图上传中 upload
     * */
    public List<PhotoDownLoadInfo> getPhotos(String userId, String status1, String status2){
        List<PhotoDownLoadInfo> photos = new ArrayList<>();
        database = DBManager.getInstance().readData();
        PictureAirLog.out("cursor open ---> getPhotos");
        Cursor cursor = database.rawQuery("select * from " + Common.PHOTOS_LOAD + " where userId = ? and success = ? or success = ? order by downloadTime", new String[]{userId,status1,status2});
        try {
            if (cursor.moveToFirst()) {//判断是否photo数据
                do {
                    PhotoDownLoadInfo photoInfo = new PhotoDownLoadInfo();
                    photoInfo.setPhotoId(cursor.getString(cursor.getColumnIndex("photoId")));
                    photoInfo.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                    photoInfo.setSize(cursor.getString(cursor.getColumnIndex("size")));
                    photoInfo.setPreviewUrl(cursor.getString(cursor.getColumnIndex("previewUrl")));
                    photoInfo.setShootTime(cursor.getString(cursor.getColumnIndex("shootTime")));
                    photoInfo.setLoadTime(cursor.getString(cursor.getColumnIndex("downloadTime")));
                    photoInfo.setIsVideo(cursor.getInt(cursor.getColumnIndex("isVideo")));
                    photoInfo.setFailedTime(cursor.getString(cursor.getColumnIndex("failedTime")));
                    photoInfo.setPhotoThumbnail_512(cursor.getString(cursor.getColumnIndex("photoThumbnail_512")));
                    photoInfo.setPhotoThumbnail_1024(cursor.getString(cursor.getColumnIndex("photoThumbnail_1024")));
                    photoInfo.setVideoWidth(cursor.getInt(cursor.getColumnIndex("videoWidth")));
                    photoInfo.setVideoHeight(cursor.getInt(cursor.getColumnIndex("videoHeight")));
                    photoInfo.setStatus(cursor.getString(cursor.getColumnIndex("success")));
                    photos.add(photoInfo);
                } while (cursor.moveToNext());
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (cursor != null) {
                cursor.close();
            }
            DBManager.getInstance().closeDatabase();
        }
        return photos;
    }

    /**
     * 获取对应状态的photo信息
     * @param userId
     * @param success 状态有 成功 “true”  失败 “false”  下载中 “load”
     * */
    public List<PhotoDownLoadInfo> getPhotosOrderByTime(String userId,String  success){
        List<PhotoDownLoadInfo> photos = new ArrayList<>();
        database = DBManager.getInstance().readData();
        PictureAirLog.out("cursor open ---> getPhotosOrderByTime");
        Cursor cursor = database.rawQuery("select * from " + Common.PHOTOS_LOAD + " where userId = ? and success = ? order by downloadTime desc", new String[]{userId,success});
        try {
            if (cursor.moveToFirst()) {//判断是否photo数据
                do {
                    PhotoDownLoadInfo photoInfo = new PhotoDownLoadInfo();
                    photoInfo.setPhotoId(cursor.getString(cursor.getColumnIndex("photoId")));
                    photoInfo.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                    photoInfo.setSize(cursor.getString(cursor.getColumnIndex("size")));
                    photoInfo.setPreviewUrl(cursor.getString(cursor.getColumnIndex("previewUrl")));
                    photoInfo.setShootTime(cursor.getString(cursor.getColumnIndex("shootTime")));
                    photoInfo.setLoadTime(cursor.getString(cursor.getColumnIndex("downloadTime")));
                    photoInfo.setIsVideo(cursor.getInt(cursor.getColumnIndex("isVideo")));
                    photoInfo.setFailedTime(cursor.getString(cursor.getColumnIndex("failedTime")));
                    photoInfo.setPhotoThumbnail_512(cursor.getString(cursor.getColumnIndex("photoThumbnail_512")));
                    photoInfo.setPhotoThumbnail_1024(cursor.getString(cursor.getColumnIndex("photoThumbnail_1024")));
                    photoInfo.setVideoWidth(cursor.getInt(cursor.getColumnIndex("videoWidth")));
                    photoInfo.setVideoHeight(cursor.getInt(cursor.getColumnIndex("videoHeight")));
                    photos.add(photoInfo);
                } while (cursor.moveToNext());
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (cursor != null) {
                cursor.close();
            }
            DBManager.getInstance().closeDatabase();
        }
        return photos;
    }


    /**
     * 获取所有userId
     *
     * */
    public List<String> getAllUsers(){
        List<String> users = new ArrayList<>();
        database = DBManager.getInstance().readData();
        Cursor cursor = database.rawQuery("select distinct userId from " + Common.PHOTOS_LOAD, null);
        try {
            if (cursor.moveToFirst()) {//判断是否photo数据
                do {
                    String user = cursor.getString(cursor.getColumnIndex("userId"));
                    users.add(user);
                } while (cursor.moveToNext());
            }
            PictureAirLog.out("cursor close ---> getAllUsers");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (cursor != null) {
                cursor.close();
            }
            DBManager.getInstance().closeDatabase();
        }
        return users;
    }


    /**
     * 获取所有的照片信息
     * */
    public List<PhotoDownLoadInfo> getAllPhotos(String userId){
        List<PhotoDownLoadInfo> photos = new ArrayList<>();
        database = DBManager.getInstance().readData();
        PictureAirLog.out("cursor open ---> getAllPhotos");
        Cursor cursor = database.rawQuery("select * from " + Common.PHOTOS_LOAD + " where userId = ?", new String[]{userId});
        try {
            if (cursor.moveToFirst()) {//判断是否photo数据
                do {
                    PhotoDownLoadInfo photoInfo = new PhotoDownLoadInfo();
                    photoInfo.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                    photoInfo.setPhotoId(cursor.getString(cursor.getColumnIndex("photoId")));
                    photoInfo.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                    photoInfo.setSize(cursor.getString(cursor.getColumnIndex("size")));
                    photoInfo.setPreviewUrl(cursor.getString(cursor.getColumnIndex("previewUrl")));
                    photoInfo.setShootTime(cursor.getString(cursor.getColumnIndex("shootTime")));
                    photoInfo.setLoadTime(cursor.getString(cursor.getColumnIndex("downloadTime")));
                    photoInfo.setIsVideo(cursor.getInt(cursor.getColumnIndex("isVideo")));
                    photoInfo.setFailedTime(cursor.getString(cursor.getColumnIndex("failedTime")));
                    photoInfo.setStatus(cursor.getString(cursor.getColumnIndex("success")));
                    photoInfo.setPhotoThumbnail_512(cursor.getString(cursor.getColumnIndex("photoThumbnail_512")));
                    photoInfo.setPhotoThumbnail_1024(cursor.getString(cursor.getColumnIndex("photoThumbnail_1024")));
                    photoInfo.setVideoWidth(cursor.getInt(cursor.getColumnIndex("videoWidth")));
                    photoInfo.setVideoHeight(cursor.getInt(cursor.getColumnIndex("videoHeight")));
                    photos.add(photoInfo);
                } while (cursor.moveToNext());
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (cursor != null) {
                cursor.close();
            }
            DBManager.getInstance().closeDatabase();
        }
        return photos;
    }

    /**
     * 根据photid获取下载成功的图片信息
     *
     * */
    public List<PhotoDownLoadInfo> getPhotosByPhotoId(String photoId){
        List<PhotoDownLoadInfo> photos = new ArrayList<>();
        database = DBManager.getInstance().readData();
        PictureAirLog.out("cursor open ---> getPhotosByPhotoId");
        Cursor cursor = database.rawQuery("select * from " + Common.PHOTOS_LOAD + " where photoId = ? and success = 'true'", new String[]{photoId});
        try {
            if (cursor.moveToFirst()) {//判断是否photo数据
                do {
                    PhotoDownLoadInfo photoInfo = new PhotoDownLoadInfo();
                    photoInfo.setPhotoId(cursor.getString(cursor.getColumnIndex("photoId")));
                    photoInfo.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                    photoInfo.setSize(cursor.getString(cursor.getColumnIndex("size")));
                    photoInfo.setPreviewUrl(cursor.getString(cursor.getColumnIndex("previewUrl")));
                    photoInfo.setShootTime(cursor.getString(cursor.getColumnIndex("shootTime")));
                    photoInfo.setLoadTime(cursor.getString(cursor.getColumnIndex("downloadTime")));
                    photoInfo.setIsVideo(cursor.getInt(cursor.getColumnIndex("isVideo")));
                    photoInfo.setFailedTime(cursor.getString(cursor.getColumnIndex("failedTime")));
                    photoInfo.setPhotoThumbnail_512(cursor.getString(cursor.getColumnIndex("photoThumbnail_512")));
                    photoInfo.setPhotoThumbnail_1024(cursor.getString(cursor.getColumnIndex("photoThumbnail_1024")));
                    photoInfo.setVideoWidth(cursor.getInt(cursor.getColumnIndex("videoWidth")));
                    photoInfo.setVideoHeight(cursor.getInt(cursor.getColumnIndex("videoHeight")));
                    photos.add(photoInfo);
                } while (cursor.moveToNext());
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (cursor != null) {
                cursor.close();
            }
            DBManager.getInstance().closeDatabase();
        }
        return photos;
    }

    /**
     *
     * 插入下载图片信息
     *
     * @param loadTime 下载完成时间
     * @param status 下载状态
     *
     * */
    public synchronized void insertPhotos(String userId, CopyOnWriteArrayList<DownloadFileStatus> list, String loadTime, String status){

        database = DBManager.getInstance().writData();
        database.beginTransaction();

        try {
            for (int i=0;i<list.size();i++) {
                DownloadFileStatus fileStatus = list.get(i);
                ContentValues values = new ContentValues();
                values.put("userId", userId);
                values.put("photoId", fileStatus.getPhotoId());
                values.put("url", fileStatus.getUrl());
                values.put("size", fileStatus.getTotalSize());
                values.put("previewUrl", fileStatus.getPhotoThumbnail());
                values.put("shootTime", fileStatus.getShootOn());
                values.put("downloadTime", loadTime);
                values.put("isVideo", fileStatus.getIsVideo());
                values.put("success", status);
                values.put("failedTime", fileStatus.getFailedTime());
                values.put("photoThumbnail_512", fileStatus.getPhotoThumbnail_512());
                values.put("photoThumbnail_1024", fileStatus.getPhotoThumbnail_1024());
                values.put("videoWidth", fileStatus.getVideoWidth());
                values.put("videoHeight", fileStatus.getVideoHeight());
                database.insert(Common.PHOTOS_LOAD, "", values);
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            DBManager.getInstance().closeDatabase();
        }
    }


    public synchronized void deletePhotos(String userId, CopyOnWriteArrayList<DownloadFileStatus> list){
        database = DBManager.getInstance().writData();
        database.beginTransaction();
        try {
            for (int i=0;i<list.size();i++) {
                DownloadFileStatus fileStatus = list.get(i);
                database.delete(Common.PHOTOS_LOAD,"userId = ? and photoId=?",new String[]{userId,fileStatus.getPhotoId()});
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            DBManager.getInstance().closeDatabase();
        }

    }

    /**
     *
     * 删除所有下载照片
     *
     * */
    public synchronized int deleteDownloadPhoto(String userId){
        database = DBManager.getInstance().writData();
        database.beginTransaction();
        int res = 0;
        try {
            res = database.delete(Common.PHOTOS_LOAD,"userId = ? and success=?",new String[]{userId,"true"});
            PictureAirLog.e("deleteDownloadPhoto","count:" + res);
            database.setTransactionSuccessful();
        }catch (Exception e){
            PictureAirLog.e(TAG, "删除失败：" + e.getMessage());
        }finally {
            database.endTransaction();
            DBManager.getInstance().closeDatabase();
            return res;
        }
    }

    /**
     *
     * 根据用户id删除所有失败照片
     *
     * */
    public synchronized void deleteDownloadFailPhotoByUserId(String userId){
        database = DBManager.getInstance().writData();
        database.beginTransaction();
        int res;
        try {
            res = database.delete(Common.PHOTOS_LOAD,"userId = ? and success = ? or success = ?",new String[]{userId,"false","upload"});
            PictureAirLog.e("deleteDownloadFailPhotoByUserId","count:" + res);
            database.setTransactionSuccessful();
        }catch (Exception e){
            PictureAirLog.e(TAG, "删除失败：" + e.getMessage());
        }finally {
            database.endTransaction();
            DBManager.getInstance().closeDatabase();
        }
    }

    /**
     * 删除重复的照片
     *
     * */
    public synchronized void deleteRepeatPhoto(String userId,PhotoDownLoadInfo info){
        database = DBManager.getInstance().writData();
        database.beginTransaction();
        int res = 0;
        try {
            res = database.delete(Common.PHOTOS_LOAD,"userId = ? and _id=?",new String[]{userId,String.valueOf(info.getId())});
            PictureAirLog.e("deleteRepeatPhoto","count:" + res);
            database.setTransactionSuccessful();
        }catch (Exception e){
            PictureAirLog.e(TAG, "删除失败：" + e.getMessage());
        }finally {
            database.endTransaction();
            DBManager.getInstance().closeDatabase();
        }
    }

    /**
     * 根据xxx删除图片
     *
     * */
    public synchronized void deletePhotoByPhotoId(String userId,String photoId){
        database = DBManager.getInstance().writData();
        database.beginTransaction();
        int res = 0;
        try {
            res = database.delete(Common.PHOTOS_LOAD,"userId = ? and photoId = ?",new String[]{userId,photoId});
            PictureAirLog.e("deletePhotoByPhotoId","count:" + res);
            database.setTransactionSuccessful();
        }catch (Exception e){
            PictureAirLog.e(TAG, "删除失败：" + e.getMessage());
        }finally {
            database.endTransaction();
            DBManager.getInstance().closeDatabase();
        }
    }

    /**
     * 更新状态为load的图片信息
     *
     * */
    public synchronized void updateLoadPhotos(String userId, String status,String downloadTime,String size,String photoId,String failedTime){

        database = DBManager.getInstance().writData();
        database.beginTransaction();
        PictureAirLog.out("cursor open ---> updateLoadPhotos");
        try {
            database.execSQL("update " + Common.PHOTOS_LOAD + " set failedTime = ?,success=?,downloadTime=?,size=? where userId = ? and photoId=?", new Object[]{failedTime, status, downloadTime, size, userId, photoId});
            database.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            database.endTransaction();
            DBManager.getInstance().closeDatabase();
            EventBus.getDefault().post(new TabIndicatorUpdateEvent(0,0,true));
        }
    }

    /**
     * 用列表循环更新状态为load的图片信息，较单个的更新速度更快
     *
     * */
    public synchronized void updateLoadPhotoList(String userId, String status,String downloadTime,String size,List<PhotoDownLoadInfo> list){

        database = DBManager.getInstance().writData();
        database.beginTransaction();
        PictureAirLog.out("cursor open ---> updateLoadPhotoList");
        try {
            for (int i=0;i<list.size();i++) {
                PhotoDownLoadInfo info = list.get(i);
                database.execSQL("update " + Common.PHOTOS_LOAD + " set failedTime = ?,success=?,downloadTime=?,size=? where userId = ? and photoId=?", new Object[]{"", status, downloadTime, size, userId, info.getPhotoId()});
            }
            database.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            database.endTransaction();
            DBManager.getInstance().closeDatabase();
        }
    }

    public synchronized CopyOnWriteArrayList<PhotoDownLoadInfo> getExistPhoto(String userId){
        int count = 0;
        PhotoDownLoadInfo photoInfo = null;
        CopyOnWriteArrayList<PhotoDownLoadInfo> list = new CopyOnWriteArrayList<>();
        database = DBManager.getInstance().readData();
        PictureAirLog.out("cursor open ---> getExistPhoto");
        Cursor cursor = database.rawQuery("select * from " + Common.PHOTOS_LOAD + " where userId = ?", new String[]{userId});
        try {
            if (cursor.moveToFirst()) {//判断是否photo数据
                count = cursor.getInt(0);
                do {
                    photoInfo = new PhotoDownLoadInfo();
                    photoInfo.setPhotoId(cursor.getString(cursor.getColumnIndex("photoId")));
                    photoInfo.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                    photoInfo.setSize(cursor.getString(cursor.getColumnIndex("size")));
                    photoInfo.setPreviewUrl(cursor.getString(cursor.getColumnIndex("previewUrl")));
                    photoInfo.setShootTime(cursor.getString(cursor.getColumnIndex("shootTime")));
                    photoInfo.setLoadTime(cursor.getString(cursor.getColumnIndex("downloadTime")));
                    photoInfo.setIsVideo(cursor.getInt(cursor.getColumnIndex("isVideo")));
                    photoInfo.setFailedTime(cursor.getString(cursor.getColumnIndex("failedTime")));
                    photoInfo.setStatus(cursor.getString(cursor.getColumnIndex("success")));
                    photoInfo.setPhotoThumbnail_512(cursor.getString(cursor.getColumnIndex("photoThumbnail_512")));
                    photoInfo.setPhotoThumbnail_1024(cursor.getString(cursor.getColumnIndex("photoThumbnail_1024")));
                    photoInfo.setVideoWidth(cursor.getInt(cursor.getColumnIndex("videoWidth")));
                    photoInfo.setVideoHeight(cursor.getInt(cursor.getColumnIndex("videoHeight")));

                    list.add(photoInfo);
                }while (cursor.moveToNext());
            }
            PictureAirLog.out("ExistPhoto --->count: "+count);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (cursor != null) {
                cursor.close();
            }
            DBManager.getInstance().closeDatabase();
        }
        return list;
    }
}
