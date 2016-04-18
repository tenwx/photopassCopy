package com.pictureair.photopass.db;

import android.content.Context;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;
import com.pictureair.photopass.entity.FrameOrStikerInfo;
import com.pictureair.photopass.entity.PPinfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.entity.PhotoItemInfo;
import com.pictureair.photopass.entity.QuestionInfo;
import com.pictureair.photopass.entity.ThreadInfo;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.JsonUtil;
import com.pictureair.photopass.util.PictureAirLog;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
//            photoInfoDBHelper = PictureAirDBHelper.getInstance(context);
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
        try {
            if (setLove) {//添加收藏
                PictureAirLog.d(TAG, "start add___" + database + "___" + photoInfoDBHelper);
                database.execSQL("insert into " + Common.FAVORITE_INFO_TABLE + " values(null,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        new String[]{userId, photoInfo.photoId, photoInfo.photoPassCode, photoInfo.shootTime,
                                photoInfo.photoPathOrURL, photoInfo.photoThumbnail, photoInfo.photoThumbnail_512,
                                photoInfo.photoThumbnail_1024, photoInfo.locationId, photoInfo.shootOn, photoInfo.isLove + "",
                                photoInfo.isPayed + "", photoInfo.locationName, photoInfo.locationCountry,
                                photoInfo.shareURL, photoInfo.isVideo + "", photoInfo.fileSize + "",
                                photoInfo.videoWidth + "", photoInfo.videoHeight + "", photoInfo.onLine + "", photoInfo.isHasPreset + ""});
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
                                " where photoId = ? and userId = ? and originalUrl = ?",
                        new String[]{photoInfo.photoId, userId, photoInfo.photoPathOrURL});
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
    public ArrayList<PhotoInfo> getFavoritePhotoInfoListFromDB(String userId, String deleteTime) {
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
                photoInfo.locationName = cursor.getString(cursor.getColumnIndex("locationName"));
                photoInfo.locationCountry = cursor.getString(cursor.getColumnIndex("locationCountry"));
                photoInfo.shareURL = cursor.getString(cursor.getColumnIndex("shareURL"));
                photoInfo.isVideo = Integer.valueOf(cursor.getString(cursor.getColumnIndex("isVideo")));
                photoInfo.fileSize = Integer.valueOf(cursor.getString(cursor.getColumnIndex("fileSize")));
                photoInfo.videoWidth = Integer.valueOf(cursor.getString(cursor.getColumnIndex("videoWidth")));
                photoInfo.videoHeight = Integer.valueOf(cursor.getString(cursor.getColumnIndex("videoHeight")));
                photoInfo.onLine = Integer.valueOf(cursor.getString(cursor.getColumnIndex("isOnLine")));
                photoInfo.isHasPreset = Integer.valueOf(cursor.getString(cursor.getColumnIndex("isHasPreset")));
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
                    file = new File(photoInfo.photoPathOrURL);
                    if (!file.exists()) {
                        continue;
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
     * @param type       1 代表直接进入的 PP 页面， 2 代表是从selectPP进入的
     * @return
     */
    public ArrayList<PPinfo> getPPCodeInfo1ByPPCodeList(ArrayList<PPinfo> ppCodeList, int type) {
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
            ArrayList<String> urlList;
            ArrayList<PhotoInfo> selectPhotoItemInfos;
            for (int i = 0; i < ppCodeList.size(); i++) {
                if (ppCodeList.get(i).getIsHidden() == 1) {
                    continue;
                }
                urlList = new ArrayList<String>();
                selectPhotoItemInfos = new ArrayList<PhotoInfo>();
                PPinfo ppInfo = ppCodeList.get(i);
                PictureAirLog.out("cursor open ---> getPPCodeInfo1ByPPCodeList" + cursor);
                if (type == 1) {
                    cursor = database.rawQuery("select * from " + Common.PHOTOPASS_INFO_TABLE
                            + " where photoCode like ? order by shootOn desc", new String[]{"%" + ppInfo.getPpCode() + "%"});
                    PictureAirLog.d("cursor cursor cursor ", "cursor :" + cursor.getCount());
                } else {
                    cursor = database.rawQuery("select * from " + Common.PHOTOPASS_INFO_TABLE +
                            " where photoCode like ? and shootTime=? order by shootOn", new String[]{"%" + ppInfo.getPpCode() + "%", ppInfo.getShootDate()});
                }

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        // 获取图片路径
                        urlList.add(cursor.getString(cursor.getColumnIndex("previewUrl")));
                        PhotoInfo sInfo = new PhotoInfo();
                        sInfo.photoId = cursor.getString(cursor.getColumnIndex("photoId"));
                        sInfo.photoPathOrURL = cursor.getString(cursor.getColumnIndex("originalUrl"));
                        sInfo.photoThumbnail = cursor.getString(cursor.getColumnIndex("previewUrl"));
                        sInfo.photoThumbnail_512 = cursor.getString(cursor.getColumnIndex("previewUrl_512"));
                        sInfo.photoThumbnail_1024 = cursor.getString(cursor.getColumnIndex("previewUrl_1024"));
                        sInfo.photoPassCode = cursor.getString(cursor.getColumnIndex("photoCode"));
                        sInfo.locationId = cursor.getString(cursor.getColumnIndex("locationId"));
                        sInfo.shootOn = cursor.getString(cursor.getColumnIndex("shootOn"));
                        sInfo.shootTime = cursor.getString(cursor.getColumnIndex("shootTime"));
                        sInfo.isPayed = Integer.valueOf(cursor.getString(cursor.getColumnIndex("isPay")));
                        sInfo.isVideo = Integer.valueOf(cursor.getString(cursor.getColumnIndex("isVideo")));
                        sInfo.onLine = 1;
                        sInfo.isChecked = 0;
                        sInfo.isSelected = 0;
                        sInfo.isUploaded = 0;
                        sInfo.showMask = 0;
                        sInfo.lastModify = 0l;
                        sInfo.index = "";
                        sInfo.isHasPreset = Integer.valueOf(cursor.getString(cursor.getColumnIndex("isHasPreset")));
                        selectPhotoItemInfos.add(sInfo);
                    } while (cursor.moveToNext());

                }
                if (type == 2) {
                    Collections.reverse(urlList);
                }
                PPinfo ppInfo1 = new PPinfo();
                ppInfo1.setPpCode(ppInfo.getPpCode());
                ppInfo1.setShootDate(ppInfo.getShootDate());
                ppInfo1.setUrlList(urlList);
                ppInfo1.setSelectPhotoItemInfos(selectPhotoItemInfos);
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
     * 更新指定照片的购买状态
     *
     * @param selectedPhotoId 指定照片ID
     */
    public void updatePhotoBought(String selectedPhotoId) {
        database = DBManager.getInstance().writData();
        Cursor cursor = null;
        String photoUrl;
        try {
            cursor = database.rawQuery("select * from " + Common.PHOTOPASS_INFO_TABLE
                    + " where photoId = ?", new String[]{selectedPhotoId});

            PictureAirLog.out("cursor count---->" + cursor.getCount());
            if (cursor.moveToFirst()) {
                photoUrl = cursor.getString(cursor.getColumnIndex("originalUrl"));
                PictureAirLog.out("photourl---->" + photoUrl);
                MemoryCacheUtils.removeFromCache(photoUrl, ImageLoader.getInstance().getMemoryCache());
                DiskCacheUtils.removeFromCache(photoUrl, ImageLoader.getInstance().getDiskCache());

                photoUrl = cursor.getString(cursor.getColumnIndex("previewUrl"));
                PictureAirLog.out("photourl---->" + photoUrl);
                MemoryCacheUtils.removeFromCache(photoUrl, ImageLoader.getInstance().getMemoryCache());
                DiskCacheUtils.removeFromCache(photoUrl, ImageLoader.getInstance().getDiskCache());

                photoUrl = cursor.getString(cursor.getColumnIndex("previewUrl_512"));
                PictureAirLog.out("photourl---->" + photoUrl);
                MemoryCacheUtils.removeFromCache(photoUrl, ImageLoader.getInstance().getMemoryCache());
                DiskCacheUtils.removeFromCache(photoUrl, ImageLoader.getInstance().getDiskCache());

                photoUrl = cursor.getString(cursor.getColumnIndex("previewUrl_1024"));
                PictureAirLog.out("photourl---->" + photoUrl);
                MemoryCacheUtils.removeFromCache(photoUrl, ImageLoader.getInstance().getMemoryCache());
                DiskCacheUtils.removeFromCache(photoUrl, ImageLoader.getInstance().getDiskCache());
            }

            database.execSQL("update " + Common.PHOTOPASS_INFO_TABLE + " set isPay = 1 where photoId = ?", new String[]{selectedPhotoId});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            DBManager.getInstance().closeDatabase();
        }
    }

    /**
     * 更新指定照片的购买状态
     *
     * @param ppCode    pp码
     * @param shootDate 绑定时间
     */
    public void updatePhotoBoughtByPPCodeAndDate(String ppCode, String shootDate) {
        database = DBManager.getInstance().writData();
        database.beginTransaction();
        try {
            database.execSQL("update " + Common.PHOTOPASS_INFO_TABLE + " set isPay = 1 where photoCode like ? and shootTime = ?", new String[]{"%" + ppCode + "%", shootDate});
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
     * 删除photopassInfo中的内容
     *
     * @param tableName 需要清空的表的名字
     * @param isVideo   是不是视频数据
     */
    public void deleteAllInfoFromTable(String tableName, boolean isVideo) {
        database = DBManager.getInstance().writData();
        try {
            database.execSQL("delete from " + tableName + " where isVideo = ?", new String[]{isVideo ? "1" : "0"});
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
     * @param isVideo       是否是视频信息
     * @param isAll         是否是刷新信息
     */
    public synchronized ArrayList<PhotoInfo> insertPhotoInfoIntoPhotoPassInfo(JSONArray responseArray, boolean isVideo, boolean isAll) {
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
                PhotoInfo photo = isVideo ? JsonUtil.getVideoInfo(object) : JsonUtil.getPhoto(object);
                if (!isVideo) {
                    if (photo.locationId == null || photo.locationId.equals("")) {
                        continue;
                    }
                }

                if (!isAll) {
                    //1.先查询数据库是否有新的数据，如果有，则忽略信息
                    //2.如果没有，则插入
                    PictureAirLog.out("cursor open ---> insertPhotoInfoIntoPhotoPassInfo");
                    cursor = database.rawQuery("select * from " + Common.PHOTOPASS_INFO_TABLE + " where photoId = ?", new String[]{photo.photoId});
                    if (cursor.getCount() > 0) {//说明存在此数据
                        cursor.close();
                        continue;
                    } else {
                        cursor.close();
                    }
                }

                resultArrayList.add(photo);
                //将数据插入到数据库
                database.execSQL("insert into " + Common.PHOTOPASS_INFO_TABLE + " values(null,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", new String[]{
                        photo.photoId, photo.photoPassCode, photo.shootTime, photo.photoPathOrURL,
                        photo.photoThumbnail, photo.photoThumbnail_512, photo.photoThumbnail_1024,
                        photo.locationId, photo.shootOn, 0 + "", photo.isPayed + "", photo.locationName,
                        photo.locationCountry, photo.shareURL, photo.isVideo + "", photo.fileSize + "",
                        photo.videoWidth + "", photo.videoHeight + "", photo.isHasPreset + ""});
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
    public synchronized ArrayList<PhotoInfo> getAllPhotoFromPhotoPassInfo(boolean isVideo, String deleteTime) {
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
        Cursor cursor = database.rawQuery("select * from " + Common.PHOTOPASS_INFO_TABLE + " where isVideo = ? order by shootOn desc", new String[]{isVideo ? "1" : "0"});
        PhotoInfo photoInfo;
        if (cursor.moveToFirst()) {//判断是否photo数据
            do {
                photoInfo = new PhotoInfo();
                PictureAirLog.out("load data from database = " + cursor.getInt(0));
                photoInfo.photoId = cursor.getString(1);//photoId
                photoInfo.photoPassCode = cursor.getString(2);//photopassCode
                photoInfo.shootTime = cursor.getString(3);//shootTime
                photoInfo.photoPathOrURL = cursor.getString(4);//originalUrl
                photoInfo.photoThumbnail = cursor.getString(5);//previewUrl
                photoInfo.photoThumbnail_512 = cursor.getString(6);//previewUrl_512
                photoInfo.photoThumbnail_1024 = cursor.getString(7);//previewUrl_1024
                photoInfo.locationId = cursor.getString(8);//locationId
                photoInfo.shootOn = cursor.getString(9);//shootOn
                photoInfo.isLove = cursor.getInt(10);//islove
                photoInfo.isPayed = cursor.getInt(11);//ispay
                photoInfo.locationName = cursor.getString(12);//locationName
                photoInfo.locationCountry = cursor.getString(13);//locationCountry
                photoInfo.shareURL = cursor.getString(14);//shareURL
                photoInfo.isVideo = cursor.getInt(15);//isVideo
                photoInfo.fileSize = cursor.getInt(16);//fileSize
                photoInfo.videoWidth = cursor.getInt(17);//videoWidth
                photoInfo.videoHeight = cursor.getInt(18);//videoHeight
                photoInfo.onLine = 1;
                photoInfo.isChecked = 0;
                photoInfo.isSelected = 0;
                photoInfo.isUploaded = 0;
                photoInfo.showMask = 0;
                photoInfo.lastModify = 0l;
                photoInfo.index = "";
                photoInfo.isHasPreset = cursor.getInt(19); // isHasPreset
                resultArrayList.add(photoInfo);
            } while (cursor.moveToNext());
        }
        PictureAirLog.out("cursor close ---> getAllPhotoFromPhotoPassInfo");
        cursor.close();
        DBManager.getInstance().closeDatabase();
        return resultArrayList;
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
        database.execSQL("update " + Common.THREAD_INFO + " set finished = ? where url = ? and thread_id = ?",
                new Object[]{finished, url, threadId});
        DBManager.getInstance().closeDatabase();
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

}
