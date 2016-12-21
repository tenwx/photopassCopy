package com.pictureair.photopass.greendao;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.ADLocationInfo;
import com.pictureair.photopass.entity.DiscoverLocationItemInfo;
import com.pictureair.photopass.entity.DownloadFileStatus;
import com.pictureair.photopass.entity.FirstStartInfo;
import com.pictureair.photopass.entity.FrameOrStikerInfo;
import com.pictureair.photopass.entity.JsonInfo;
import com.pictureair.photopass.entity.PPinfo;
import com.pictureair.photopass.entity.PaymentOrderInfo;
import com.pictureair.photopass.entity.PhotoDownLoadInfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.entity.ThreadInfo;
import com.pictureair.photopass.eventbus.TabIndicatorUpdateEvent;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.GlideUtil;
import com.pictureair.photopass.util.JsonUtil;
import com.pictureair.photopass.util.PictureAirLog;

import net.sqlcipher.SQLException;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.greenrobot.event.EventBus;

/**
 * 数据库操作管理封装类，以后所有的数据库操作，都在这里进行
 *
 * @author bauer_bao
 */
public class PictureAirDbManager {
    public static final long DAY_TIME = 24 * 60 * 60 * 1000;//一天的毫秒数
    public static final int CACHE_DAY = 30;//30天的有效期

    /**
     * 插入设置中的状态
     *
     * @param settingType 设置类型
     * @param userInfoId  用户ID
     */
    public static void insertSettingStatus(String settingType, String userInfoId) {
        FirstStartInfoDao firstStartInfoDao = MyApplication.getInstance().getDaoSession().getFirstStartInfoDao();
        firstStartInfoDao.insert(new FirstStartInfo(null, settingType, userInfoId));
    }

    /**
     * 用户设置，删除数据
     *
     * @param settingType 设置类型
     * @param userInfoId  用户ID
     */
    public static void deleteSettingStatus(String settingType, String userInfoId) {
        FirstStartInfoDao firstStartInfoDao = MyApplication.getInstance().getDaoSession().getFirstStartInfoDao();
        QueryBuilder<FirstStartInfo> queryBuilder = firstStartInfoDao.queryBuilder()
                .where(FirstStartInfoDao.Properties.Event.eq(settingType), FirstStartInfoDao.Properties.UserId.eq(userInfoId));

        if (queryBuilder.count() > 0) {
            FirstStartInfo firstStartInfo = queryBuilder.build().forCurrentThread().unique();
            firstStartInfoDao.delete(firstStartInfo);
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
    public static boolean checkFirstBuyPhoto(String settingType, String userInfoId) {
        FirstStartInfoDao firstStartInfoDao = MyApplication.getInstance().getDaoSession().getFirstStartInfoDao();
        long count = firstStartInfoDao.queryBuilder()
                .where(FirstStartInfoDao.Properties.Event.eq(settingType), FirstStartInfoDao.Properties.UserId.eq(userInfoId)).count();

        return count > 0;
    }

    /**
     * 检查是不是第一次
     *
     * @param settingType
     * @param userInfoId
     */
    public static boolean checkFirstTimeStartActivity(String settingType, String userInfoId) {
        FirstStartInfoDao firstStartInfoDao = MyApplication.getInstance().getDaoSession().getFirstStartInfoDao();
        boolean result = false;
        long count = firstStartInfoDao.queryBuilder()
                .where(FirstStartInfoDao.Properties.Event.eq(settingType), FirstStartInfoDao.Properties.UserId.eq(userInfoId)).count();

        if (count == 0){
            firstStartInfoDao.insert(new FirstStartInfo(null, settingType, userInfoId));
            result = true;
        }
        return result;
    }

    /**
     * 根据pp列表获取对应的PPCodeInfo1列表
     *
     * @param ppCodeList ppCodeList
     * @param type       1 代表直接进入的 PP 页面， 2 代表是从selectPP进入，这个情况只显示模糊图
     * @return
     */
    public static ArrayList<PPinfo> getPPCodeInfo1ByPPCodeList(Context c, ArrayList<PPinfo> ppCodeList, int type) {
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
        PhotoInfoDao photoInfoDao = MyApplication.getInstance().getDaoSession().getPhotoInfoDao();
        ArrayList<HashMap<String, String>> urlList;
        ArrayList<PhotoInfo> selectPhotoItemInfos;
        HashMap<String, String> map;
        for (int i = 0; i < ppCodeList.size(); i++) {
            if (ppCodeList.get(i).getIsHidden() == 1) {
                continue;
            }
            urlList = new ArrayList<>();
            PPinfo ppInfo = ppCodeList.get(i);
            if (type == 1) {
                selectPhotoItemInfos = (ArrayList<PhotoInfo>) photoInfoDao.queryBuilder()
                        .where(PhotoInfoDao.Properties.PhotoPassCode.like("%" + ppInfo.getPpCode() + "%"))
                        .orderAsc(PhotoInfoDao.Properties.StrShootOn).build().forCurrentThread().list();
            } else {
                selectPhotoItemInfos = (ArrayList<PhotoInfo>) photoInfoDao.queryBuilder()
                        .where(PhotoInfoDao.Properties.PhotoPassCode.like("%" + ppInfo.getPpCode() + "%"),
                                PhotoInfoDao.Properties.IsPaid.eq(0),
                                PhotoInfoDao.Properties.ShootDate.eq(ppInfo.getShootDate()))
                        .orderAsc(PhotoInfoDao.Properties.StrShootOn).build().forCurrentThread().list();
            }

            for (PhotoInfo photoInfo : selectPhotoItemInfos) {
                // 获取图片路径
                map = new HashMap<>();
                map.put("url", Common.PHOTO_URL + photoInfo.getPhotoThumbnail_512());
                map.put("isVideo", photoInfo.getIsVideo() + "");
                map.put("isEnImage", photoInfo.getIsEnImage()+"");
                urlList.add(map);
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
                    map.put("isEnImage", "0");
                    urlList.add(map);
                }
            } else if (count < 12) {
                for (int j = 12 - count; j > 0; j--) {
                    map = new HashMap<>();
                    map.put("url", GlideUtil.getDrawableUrl(c, R.drawable.default_pp));
                    map.put("isVideo", "0");
                    map.put("isEnImage", "0");
                    urlList.add(map);
                }
            }
            PPinfo ppInfo1 = new PPinfo();
            ppInfo1.setPpCode(ppInfo.getPpCode());
            ppInfo1.setShootDate(ppInfo.getShootDate());
            if (ppInfo.getPhotoCount() == -1) {//需要设置本地数量
                ppInfo1.setPhotoCount(count);

            } else {
                ppInfo1.setPhotoCount(ppInfo.getPhotoCount());

            }
            ppInfo1.setUrlList(urlList);
            ppInfo1.setSelectPhotoItemInfos(selectPhotoItemInfos);
            ppInfo1.setVisiblePhotoCount(count);
            showPPCodeList.add(ppInfo1);
        }
        return showPPCodeList;
    }

    /**
     * 根据ppCode获取对应的PP照片列表
     * @param ppCode
     */
    public static ArrayList<PhotoInfo> getPhotoInfosByPPCode(String ppCode, String shootDate, ArrayList<DiscoverLocationItemInfo> locationItemInfos, String language) {
        PhotoInfoDao photoInfoDao = MyApplication.getInstance().getDaoSession().getPhotoInfoDao();
        ArrayList<PhotoInfo> selectPhotoItemInfos = (ArrayList<PhotoInfo>) photoInfoDao.queryBuilder()
                .where(PhotoInfoDao.Properties.PhotoPassCode.like("%" + ppCode + "%"), PhotoInfoDao.Properties.ShootDate.eq(shootDate))
                .orderDesc(PhotoInfoDao.Properties.ReceivedOn).build().forCurrentThread().list();

        for (PhotoInfo photoInfo: selectPhotoItemInfos) {
            // 获取图片路径
            for (int i = 0; i < locationItemInfos.size(); i++) {
                if (photoInfo.getLocationId().equals(locationItemInfos.get(i).locationId) || locationItemInfos.get(i).locationIds.contains(photoInfo.getLocationId())) {
                    if (language.equals(Common.ENGLISH)) {
                        photoInfo.setLocationName(locationItemInfos.get(i).placeENName);
                    } else if (language.equals(Common.SIMPLE_CHINESE)) {
                        photoInfo.setLocationName(locationItemInfos.get(i).placeCHName);
                    }
                    break;
                }
            }
            if (TextUtils.isEmpty(photoInfo.getLocationName())) {
                if (language.equals(Common.ENGLISH)) {
                    photoInfo.setLocationName(locationItemInfos.get(locationItemInfos.size() - 1).placeENName);
                } else if (language.equals(Common.SIMPLE_CHINESE)) {
                    photoInfo.setLocationName(locationItemInfos.get(locationItemInfos.size() - 1).placeCHName);
                }
            }
        }
        return selectPhotoItemInfos;
    }

    /**
     * 更新照片的信息
     * @param photo
     */
    public static void updatePhotoInfo(PhotoInfo photo) {
        PhotoInfoDao photoInfoDao = MyApplication.getInstance().getDaoSession().getPhotoInfoDao();
        PhotoInfo photoInfo = photoInfoDao.queryBuilder()
                .where(PhotoInfoDao.Properties.PhotoId.eq(photo.getPhotoId())).build().forCurrentThread().unique();

        if (photoInfo == null) {
            return;
        }
        photoInfo.setPhotoPassCode(photo.getPhotoPassCode());
        photoInfo.setShootDate(photo.getShootDate());
        photoInfo.setPhotoOriginalURL(photo.getPhotoOriginalURL());
        photoInfo.setPhotoThumbnail_128(photo.getPhotoThumbnail_128());
        photoInfo.setPhotoThumbnail_512(photo.getPhotoThumbnail_512());
        photoInfo.setPhotoThumbnail_1024(photo.getPhotoThumbnail_1024());
        photoInfo.setLocationId(photo.getLocationId());
        photoInfo.setStrShootOn(photo.getStrShootOn());
        photoInfo.setIsPaid(photo.getIsPaid());
        photoInfo.setShareURL(photo.getShareURL());
        photoInfo.setFileSize(photo.getFileSize());
        photoInfo.setVideoWidth(photo.getVideoWidth());
        photoInfo.setVideoHeight(photo.getVideoHeight());
        photoInfo.setIsPreset(photo.getIsPreset());
        photoInfo.setIsEnImage(photo.getIsEnImage());

        photoInfoDao.update(photoInfo);
    }

    /**
     * 更新指定照片的购买状态
     *
     * @param selectedPhotoId 指定照片ID
     */
    public static void updatePhotoBought(String selectedPhotoId, boolean isDelete) {
        PhotoInfoDao photoInfoDao = MyApplication.getInstance().getDaoSession().getPhotoInfoDao();
        PhotoInfo photoInfo = photoInfoDao.queryBuilder()
                .where(PhotoInfoDao.Properties.PhotoId.eq(selectedPhotoId)).build().forCurrentThread().unique();

        if (photoInfo == null) {
            return;
        }
        if (isDelete) {//删除操作
            photoInfoDao.delete(photoInfo);

        } else {//同步操作
            photoInfo.setIsPaid(1);
            photoInfoDao.update(photoInfo);
        }
    }

    /**
     * 更新指定照片的购买状态
     *
     * @param ppCode    pp码
     * @param shootDate 绑定时间
     */
    public static void updatePhotoBoughtByPPCodeAndDate(String ppCode, String shootDate, boolean isDelete) {
        PhotoInfoDao photoInfoDao = MyApplication.getInstance().getDaoSession().getPhotoInfoDao();
        ArrayList<PhotoInfo> photos;
        if (isDelete) {//删除操作
            photos = (ArrayList<PhotoInfo>) photoInfoDao.queryBuilder()
                    .where(PhotoInfoDao.Properties.PhotoPassCode.like("%" + ppCode + "%")).build().forCurrentThread().list();
            if (photos != null && photos.size() > 0) {
                photoInfoDao.deleteInTx(photos);
            }

        } else {//同步
            photos = (ArrayList<PhotoInfo>) photoInfoDao.queryBuilder()
                    .where(PhotoInfoDao.Properties.PhotoPassCode.like("%" + ppCode + "%"), PhotoInfoDao.Properties.ShootDate.eq(shootDate))
                    .build().forCurrentThread().list();
            if (photos == null && photos.size() == 0) {
                return;
            }
            for (PhotoInfo photo : photos) {
                photo.setIsPaid(1);
            }
            photoInfoDao.updateInTx(photos);
        }
    }

    /**
     * 删除photopassInfo中的内容
     *
     */
    public static void deleteAllInfoFromTable() {
        PhotoInfoDao photoInfoDao = MyApplication.getInstance().getDaoSession().getPhotoInfoDao();
        photoInfoDao.deleteAll();
    }

    /**
     * 按照pp卡删除photopassInfo中的内容
     *
     */
    public static void deleteAllInfoFromTable(String ppCode, String shoodDate) {
        PhotoInfoDao photoInfoDao = MyApplication.getInstance().getDaoSession().getPhotoInfoDao();
        List<PhotoInfo> list;
        QueryBuilder<PhotoInfo> queryBuilder = photoInfoDao.queryBuilder()
                .where(PhotoInfoDao.Properties.PhotoPassCode.eq(ppCode), PhotoInfoDao.Properties.ShootDate.eq(shoodDate));
        if (queryBuilder.count() > 0) {
            list = queryBuilder.build().forCurrentThread().list();
            if (list != null) {
                photoInfoDao.deleteInTx(list);
            }
        }
    }

    /**
     * 将照片插入到photoPassInfo表中
     *
     * @param responseArray
     * @param type         是否是刷新信息
     */
    public static synchronized ArrayList<PhotoInfo> insertPhotoInfoIntoPhotoPassInfo(JSONArray responseArray, int type, String language,
                                                                                     ArrayList<DiscoverLocationItemInfo> locationList) {
        PhotoInfoDao photoInfoDao = MyApplication.getInstance().getDaoSession().getPhotoInfoDao();
        ArrayList<PhotoInfo> resultArrayList = new ArrayList<>();//返回的数据列表
        ArrayList<PhotoInfo> dbPhotoList = new ArrayList<>();//插入数据库的列表
        if (responseArray.size() == 0) {
            return resultArrayList;
        }
        PictureAirLog.d("photo size -->" + responseArray.size());
        for (int i = 0; i < responseArray.size(); i++) {
            JSONObject object = responseArray.getJSONObject(i);
            PhotoInfo photo = JsonUtil.getPhoto(object);
            if (TextUtils.isEmpty(photo.getLocationId())) {
                photo.setLocationId("others");
            }

            //设置地点名称
            if (locationList != null) {
                int resultPosition = AppUtil.findPositionInLocationList(photo, locationList);
                if (resultPosition == -1) {//如果没有找到，说明是其他地点的照片
                    resultPosition = locationList.size() - 1;
                }
                if (resultPosition < 0 ) {
                    resultPosition = 0;
                }
                if (language.equals(Common.SIMPLE_CHINESE)) {
                    photo.setLocationName(locationList.get(resultPosition).placeCHName);
                } else {
                    photo.setLocationName(locationList.get(resultPosition).placeENName);
                }
            }

            if (type == API1.GET_NEW_PHOTOS || type == API1.GET_OLD_PHOTOS) {
                //1.先查询数据库是否有新的数据，如果有，则更新信息
                //2.如果没有，则插入
                PhotoInfo dbPhotoInfo = photoInfoDao.queryBuilder()
                        .where(PhotoInfoDao.Properties.PhotoId.eq(photo.getPhotoId())).build().forCurrentThread().unique();
                if (dbPhotoInfo != null) {//说明存在此数据，需要更新下数据
                    dbPhotoInfo.setPhotoPassCode(photo.getPhotoPassCode());
                    dbPhotoInfo.setShootDate(photo.getShootDate());
                    dbPhotoInfo.setPhotoOriginalURL(photo.getPhotoOriginalURL());
                    dbPhotoInfo.setPhotoThumbnail_128(photo.getPhotoThumbnail_128());
                    dbPhotoInfo.setPhotoThumbnail_512(photo.getPhotoThumbnail_512());
                    dbPhotoInfo.setPhotoThumbnail_1024(photo.getPhotoThumbnail_1024());
                    dbPhotoInfo.setLocationId(photo.getLocationId());
                    dbPhotoInfo.setStrShootOn(photo.getStrShootOn());
                    dbPhotoInfo.setIsPaid(photo.getIsPaid());
                    dbPhotoInfo.setShareURL(photo.getShareURL());
                    dbPhotoInfo.setFileSize(photo.getFileSize());
                    dbPhotoInfo.setVideoWidth(photo.getVideoWidth());
                    dbPhotoInfo.setVideoHeight(photo.getVideoHeight());
                    dbPhotoInfo.setIsPreset(photo.getIsPreset());
                    dbPhotoInfo.setIsEnImage(photo.getIsEnImage());

                    photoInfoDao.update(dbPhotoInfo);

                    photo.setIsRefreshInfo(1);
                    resultArrayList.add(photo);
                    continue;
                }
            }

            resultArrayList.add(photo);
            dbPhotoList.add(photo);
        }
        photoInfoDao.insertInTx(dbPhotoList);
        return resultArrayList;
    }

    /**
     * 删除数据库中的照片（照片表和收藏表）
     * @param list
     * @param ppCode
     */
    public static void deletePhotosFromPhotoInfoAndFavorite(ArrayList<PhotoInfo> list, String ppCode) {
        PhotoInfoDao photoInfoDao = MyApplication.getInstance().getDaoSession().getPhotoInfoDao();
        PhotoInfo photoInfo;
        try {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getPhotoPassCode().equals(ppCode)) {//只有一张卡
                    photoInfo = photoInfoDao.queryBuilder()
                            .where(PhotoInfoDao.Properties.PhotoId.eq(list.get(i).getPhotoId()), PhotoInfoDao.Properties.PhotoPassCode.eq(ppCode))
                            .build().forCurrentThread().unique();
                    if (photoInfo != null) {
                        photoInfoDao.delete(photoInfo);
                    }

                } else {//有多张卡
                    String newPPCode = list.get(i).getPhotoPassCode().replace(ppCode, "");
                    photoInfo = photoInfoDao.queryBuilder()
                            .where(PhotoInfoDao.Properties.PhotoId.eq(list.get(i).getPhotoId())).build().forCurrentThread().unique();
                    if (photoInfo == null) {
                        return;
                    }
                    photoInfo.setPhotoPassCode(newPPCode);
                    photoInfoDao.update(photoInfo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
        }
    }

    /**
     * 根据ppCode删除对应的照片
     *
     * @param position 删除的position
     * @param ppList   原始ppCode列表
     */
    public static void removePhotosFromUserByPPCode(int position, ArrayList<PPinfo> ppList) {
        /**
         * 删除步骤
         * 1.获取删除ppcode对应的所有图片
         * 2.获取删除图片对应的ppcode
         * 3.遍历pp列表中其他pp
         * 4.检查是否其他pp的code在删除图片对应的ppcode中
         * 5.如果在，则不删除，如果不在，则删除
         */
        PhotoInfoDao photoInfoDao = MyApplication.getInstance().getDaoSession().getPhotoInfoDao();
        String deletePPCode;
        boolean needDelete = true;
        ArrayList<PhotoInfo> deletePhotos = new ArrayList<>();
        //1
        deletePhotos.addAll(ppList.get(position).getSelectPhotoItemInfos());

        for (int i = 0; i < deletePhotos.size(); i++) {
            //2
            deletePPCode = deletePhotos.get(i).getPhotoPassCode();
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
                PhotoInfo photoInfo = photoInfoDao.queryBuilder()
                        .where(PhotoInfoDao.Properties.PhotoId.eq(deletePhotos.get(i).getPhotoId())).build().forCurrentThread().unique();
                if (photoInfo != null) {
                    photoInfoDao.delete(photoInfo);
                }
            } else {
                needDelete = true;
            }
        }
    }

    /**
     * 查询数据库中的图片信息
     *
     * @return
     */
    public static synchronized ArrayList<PhotoInfo> getAllPhotoFromPhotoPassInfo(boolean exceptVideo, String deleteTime) {
        ArrayList<PhotoInfo> resultArrayList;
        PhotoInfoDao photoInfoDao = MyApplication.getInstance().getDaoSession().getPhotoInfoDao();
        //根据当前时间，删除超过30天并且未支付的数据信息
        /**
         * 1.获取当前时间，以毫秒为单位
         * 2.删除数据库数据，条件1.未购买的图片，2.当前时间 - 30天的时间 > 数据库的时间
         */
        resultArrayList = (ArrayList<PhotoInfo>) photoInfoDao
                .queryRaw("WHERE T.'IS_PAID' = 0 AND T.'STR_SHOOT_ON' < datetime(?)", new String[]{deleteTime});
        PictureAirLog.d("size--> " + resultArrayList.size());
        if (resultArrayList.size() > 0) {
            photoInfoDao.deleteInTx(resultArrayList);
        }

        resultArrayList.clear();
        //删除过期的数据之后，再查询photo表的信息
        PictureAirLog.out("cursor open ---> getAllPhotoFromPhotoPassInfo");
        if (exceptVideo) {
            resultArrayList = (ArrayList<PhotoInfo>) photoInfoDao.queryBuilder()
                    .where(PhotoInfoDao.Properties.IsVideo.eq(0))
                    .orderDesc(PhotoInfoDao.Properties.StrShootOn).build().forCurrentThread().list();
        } else{
            resultArrayList = (ArrayList<PhotoInfo>) photoInfoDao.queryBuilder()
                    .orderDesc(PhotoInfoDao.Properties.StrShootOn).build().forCurrentThread().list();
        }
        PictureAirLog.out("cursor close ---> getAllPhotoFromPhotoPassInfo" + resultArrayList.size());
        return resultArrayList;
    }

    /**
     * 查询数据库中的图片信息
     *
     * @return
     */
    public static synchronized ArrayList<PhotoInfo> getAllPhotosFromPhotoPassInfoByPPcodeAndDate(String ppCode, String shootDate, String deleteTime) {
        ArrayList<PhotoInfo> resultArrayList;
        PhotoInfoDao photoInfoDao = MyApplication.getInstance().getDaoSession().getPhotoInfoDao();
        //根据当前时间，删除超过30天并且未支付的数据信息
        /**
         * 1.获取当前时间，以毫秒为单位
         * 2.删除数据库数据，条件1.未购买的图片，2.当前时间 - 30天的时间 > 数据库的时间
         */
        resultArrayList = (ArrayList<PhotoInfo>) photoInfoDao
                .queryRaw("WHERE T.'IS_PAID' = 0 AND T.'STR_SHOOT_ON' < datetime(?)", new String[]{deleteTime});
        PictureAirLog.d("size--> " + resultArrayList.size());
        if (resultArrayList.size() > 0) {
            photoInfoDao.deleteInTx(resultArrayList);
        }

        resultArrayList.clear();
        //删除过期的数据之后，再查询photo表的信息
        PictureAirLog.out("cursor open ---> getAllPhotoFromPhotoPassInfo");
        QueryBuilder<PhotoInfo> queryBuilder = photoInfoDao.queryBuilder()
                .where(PhotoInfoDao.Properties.PhotoPassCode.like("%" + ppCode + "%"),
                        PhotoInfoDao.Properties.ShootDate.eq(shootDate)).orderDesc(PhotoInfoDao.Properties.ReceivedOn);
        if (queryBuilder.count() > 0) {
            resultArrayList = (ArrayList<PhotoInfo>) queryBuilder.build().forCurrentThread().list();
        }
        PictureAirLog.out("cursor close ---> getAllPhotoFromPhotoPassInfo" + resultArrayList.size());
        return resultArrayList;
    }

    /**
     * 查询数据库中的图片信息
     *
     * @return
     */
    public static ArrayList<PhotoInfo> getPhotoFromPhotoPassInfo(String deleteTime, boolean hasBought) {
        ArrayList<PhotoInfo> resultArrayList;
        PhotoInfoDao photoInfoDao = MyApplication.getInstance().getDaoSession().getPhotoInfoDao();
        //根据当前时间，删除超过30天并且未支付的数据信息
        /**
         * 1.获取当前时间，以毫秒为单位
         * 2.删除数据库数据，条件1.未购买的图片，2.当前时间 - 30天的时间 > 数据库的时间
         */
        resultArrayList = (ArrayList<PhotoInfo>) photoInfoDao
                .queryRaw("WHERE T.'IS_PAID' = 0 AND T.'STR_SHOOT_ON' < datetime(?)", new String[]{deleteTime});
        if (resultArrayList.size() > 0) {
            photoInfoDao.deleteInTx(resultArrayList);
        }

        resultArrayList.clear();

        //删除过期的数据之后，再查询photo表的信息
        resultArrayList = (ArrayList<PhotoInfo>) photoInfoDao.queryBuilder()
                .where(PhotoInfoDao.Properties.IsPaid.eq(hasBought ? "1" : "0"))
                .orderDesc(PhotoInfoDao.Properties.StrShootOn).build().forCurrentThread().list();
        return resultArrayList;
    }

    /**
     * 判断这个video是否有原始视频链接
     * @param photoId
     * @return
     */
    public static boolean needGetLastestVideoInfoFromNetwork(String photoId) {
        PhotoInfoDao photoInfoDao = MyApplication.getInstance().getDaoSession().getPhotoInfoDao();
        PhotoInfo photoInfo = photoInfoDao.queryBuilder()
                .where(PhotoInfoDao.Properties.PhotoId.eq(photoId)).build().forCurrentThread().unique();
        return AppUtil.isOldVersionOfTheVideo(photoInfo.getPhotoOriginalURL(), photoInfo.getPhotoThumbnail_1024(), photoInfo.getPhotoThumbnail_512(), photoInfo.getPhotoThumbnail_128());
    }

    /**
     * 从数据库中查询边框和饰品信息
     *
     * @param frame 边框的话为1，饰品的话为0
     * @return
     */
    public static ArrayList<FrameOrStikerInfo> getLastContentDataFromDB(int frame) {
        FrameOrStikerInfoDao frameOrStikerInfoDao = MyApplication.getInstance().getDaoSession().getFrameOrStikerInfoDao();
        ArrayList<FrameOrStikerInfo> resultArrayList = (ArrayList<FrameOrStikerInfo>) frameOrStikerInfoDao.queryBuilder()
                .where(FrameOrStikerInfoDao.Properties.IsActive.eq(1), FrameOrStikerInfoDao.Properties.FileType.eq(frame)).build().forCurrentThread().list();
        return resultArrayList == null ? new ArrayList<FrameOrStikerInfo>() : resultArrayList;
    }

    /**
     * 插入边框和饰品
     *
     * @param jsonArray
     * @param isFrame   是不是边框
     * @throws JSONException
     */
    private static void insertFrameAndSticker(JSONArray jsonArray, boolean isFrame) throws JSONException {
        FrameOrStikerInfoDao frameOrStikerInfoDao = MyApplication.getInstance().getDaoSession().getFrameOrStikerInfoDao();
        ArrayList<FrameOrStikerInfo> frameOrStikerInfos = new ArrayList<>();
        FrameOrStikerInfo frameOrStikerInfo;

        if (jsonArray.size() > 0) {
            for (int i = 0; i < jsonArray.size(); i++) {
                //解析json
                if (isFrame) {
                    frameOrStikerInfo = JsonUtil.getFrameInfo(jsonArray.getJSONObject(i));

                } else {
                    frameOrStikerInfo = JsonUtil.getStickerInfo(jsonArray.getJSONObject(i));
                }

                if (frameOrStikerInfo.getIsActive() == 1) {//新数据
                    frameOrStikerInfo.setFileType(isFrame ? 1 : 0);
                    frameOrStikerInfos.add(frameOrStikerInfo);
                } else {//如果为0，说明需要修改以前的数据状态
                    QueryBuilder<FrameOrStikerInfo> queryBuilder = frameOrStikerInfoDao.queryBuilder()
                            .where(FrameOrStikerInfoDao.Properties.FrameName.eq(frameOrStikerInfo.getFrameName()), FrameOrStikerInfoDao.Properties.FileType.eq(isFrame ? "1" : "0"));

                    if (queryBuilder.count() > 0) {
                        FrameOrStikerInfo oldFrameOrStikerInfo = queryBuilder.build().forCurrentThread().unique();
                        oldFrameOrStikerInfo.setIsActive(0);
                        frameOrStikerInfoDao.update(oldFrameOrStikerInfo);
                    }
                }
            }
            if (frameOrStikerInfos.size() > 0) {
                frameOrStikerInfoDao.updateInTx(frameOrStikerInfos);

            }
        }
    }

    /**
     * 将边框和饰品信息解析并且写入数据库
     *
     * @param jsonObject
     */
    public static void insertFrameAndStickerIntoDB(JSONObject jsonObject) {
        if (jsonObject.containsKey("frames")) {
            insertFrameAndSticker(jsonObject.getJSONArray("frames"), true);
        }
        if (jsonObject.containsKey("cliparts")) {
            insertFrameAndSticker(jsonObject.getJSONArray("cliparts"), false);
        }
    }

    /**
     * 更新边框下载状态
     *
     * @param name  边框名字
     * @param frame 边框为1，饰品为0
     */
    public static void updateFrameAndStickerDownloadStatus(String name, int frame) {
        FrameOrStikerInfoDao frameOrStikerInfoDao = MyApplication.getInstance().getDaoSession().getFrameOrStikerInfoDao();
        QueryBuilder<FrameOrStikerInfo> queryBuilder = frameOrStikerInfoDao.queryBuilder()
                .where(FrameOrStikerInfoDao.Properties.FrameName.eq(name), FrameOrStikerInfoDao.Properties.FileType.eq(frame));
        if (queryBuilder.count() > 0) {
            FrameOrStikerInfo frameOrStikerInfo = queryBuilder.build().forCurrentThread().unique();
            frameOrStikerInfo.setIsDownload(1);
            frameOrStikerInfoDao.update(frameOrStikerInfo);
        }
    }

    /**
     * 添加已支付的订单ID
     */
    public static void insertPaymentOrderIdDB(String userId, String orderId) {
        PaymentOrderInfoDao paymentOrderInfoDao = MyApplication.getInstance().getDaoSession().getPaymentOrderInfoDao();
        paymentOrderInfoDao.insert(new PaymentOrderInfo(null, orderId, userId));
    }

    /**
     * 查询已支付订单
     *
     * @return
     */
    public static List<PaymentOrderInfo> searchPaymentOrderIdDB() {
        PaymentOrderInfoDao paymentOrderInfoDao = MyApplication.getInstance().getDaoSession().getPaymentOrderInfoDao();
        List<PaymentOrderInfo> orderInfos = paymentOrderInfoDao.queryBuilder().build().forCurrentThread().list();
        if (orderInfos == null) {
            return new ArrayList<>();
        } else {
            return orderInfos;
        }
    }

    /**
     * 删除已支付的订单ID
     */
    public static void removePaymentOrderIdDB(String orderId) {
        PaymentOrderInfoDao paymentOrderInfoDao = MyApplication.getInstance().getDaoSession().getPaymentOrderInfoDao();
        List<PaymentOrderInfo> orderInfos = paymentOrderInfoDao.queryBuilder()
                .where(PaymentOrderInfoDao.Properties.OrderId.eq(orderId)).build().forCurrentThread().list();
        if (orderInfos != null && orderInfos.size() > 0) {
            paymentOrderInfoDao.deleteInTx(orderInfos);
        }
    }

    /**
     * 插入广告地点信息
     * 1.先清除广告数据
     * 2.再插入广告数据
     *
     * @param jsonArray
     */
    public static void insertADLocations(JSONArray jsonArray) {
        ADLocationInfoDao adLocationInfoDao = MyApplication.getInstance().getDaoSession().getADLocationInfoDao();
        adLocationInfoDao.deleteAll();

        ArrayList<ADLocationInfo> adLocationInfos = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            adLocationInfos.add(JsonUtil.getAdLocationInfo(jsonArray.getJSONObject(i)));
        }

        adLocationInfoDao.insertInTx(adLocationInfos);
    }

    /**
     * 插入广告地点信息
     * 1.先清除广告数据
     * 2.再插入广告数据
     *
     * @param jsonArray
     * @return 返回当前locationId的广告词
     */
    public static String insertADLocations(JSONArray jsonArray, String photoLocationId, String language) {
        ADLocationInfoDao adLocationInfoDao = MyApplication.getInstance().getDaoSession().getADLocationInfoDao();
        String result = "";
        adLocationInfoDao.deleteAll();

        ArrayList<ADLocationInfo> adLocationInfos = new ArrayList<>();
        ADLocationInfo adLocationInfo;
        for (int i = 0; i < jsonArray.size(); i++) {
            adLocationInfo = JsonUtil.getAdLocationInfo(jsonArray.getJSONObject(i));
            if (photoLocationId.equals(adLocationInfo.getLocationId())) {
                if (language.equals(Common.SIMPLE_CHINESE)) {
                    result = adLocationInfo.getDescriptionCH();
                } else {
                    result = adLocationInfo.getDescriptionEN();
                }
            }
            adLocationInfos.add(adLocationInfo);
        }

        adLocationInfoDao.insertInTx(adLocationInfos);
        return result;
    }

    /**
     * 根据locationId获取广告信息
     *
     * @param locationId
     * @param language
     * @return
     */
    public static String getADByLocationId(String locationId, String language) {
        String ad = "";
        ADLocationInfoDao adLocationInfoDao = MyApplication.getInstance().getDaoSession().getADLocationInfoDao();
        QueryBuilder<ADLocationInfo> queryBuilder = adLocationInfoDao.queryBuilder()
                .where(ADLocationInfoDao.Properties.LocationId.eq(locationId))
                .distinct();

        if (queryBuilder.count() > 0) {
            ADLocationInfo adLocationInfo = queryBuilder.build().forCurrentThread().unique();
            if (language.equals(Common.SIMPLE_CHINESE)) {
                ad = adLocationInfo.getDescriptionCH();
            } else {
                ad = adLocationInfo.getDescriptionEN();
            }
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
    public static boolean isExistsThread(String url, int threadId) {
        ThreadInfoDao threadInfoDao = MyApplication.getInstance().getDaoSession().getThreadInfoDao();
        QueryBuilder<ThreadInfo> queryBuilder = threadInfoDao.queryBuilder()
                .where(ThreadInfoDao.Properties.Url.eq(url), ThreadInfoDao.Properties.ThreadId.eq(threadId));
        return queryBuilder.count() > 0;
    }

    /**
     * 查看所有下载线程
     *
     * @return
     */
    public static boolean isExistsThread() {
        ThreadInfoDao threadInfoDao = MyApplication.getInstance().getDaoSession().getThreadInfoDao();
        QueryBuilder<ThreadInfo> queryBuilder = threadInfoDao.queryBuilder();
        return queryBuilder.count() > 0;
    }

    /**
     * 读取下载线程信息
     *
     * @param url
     */
    public static List<ThreadInfo> getTreads(String url) {
        ThreadInfoDao threadInfoDao = MyApplication.getInstance().getDaoSession().getThreadInfoDao();
        List<ThreadInfo> list = threadInfoDao.queryBuilder()
                .where(ThreadInfoDao.Properties.Url.eq(url)).build().forCurrentThread().list();
        return list == null ? new ArrayList<ThreadInfo>() : list;
    }


    /**
     * 更新下载线程
     *
     * @param url
     * @param threadId
     * @param finished
     */
    public static void updateThread(String url, int threadId, int finished) {
        ThreadInfoDao threadInfoDao = MyApplication.getInstance().getDaoSession().getThreadInfoDao();
        QueryBuilder<ThreadInfo> queryBuilder = threadInfoDao.queryBuilder()
                .where(ThreadInfoDao.Properties.Url.eq(url), ThreadInfoDao.Properties.ThreadId.eq(threadId));
        if (queryBuilder.count() > 0) {
            ThreadInfo threadInfo = queryBuilder.build().forCurrentThread().unique();
            threadInfo.setFinished(finished);
            threadInfoDao.update(threadInfo);
        }
    }


    /**
     * 删除下载线程
     *
     * @param url
     * @param threadId
     */
    public static void deleteThread(String url, int threadId) {
        ThreadInfoDao threadInfoDao = MyApplication.getInstance().getDaoSession().getThreadInfoDao();
        QueryBuilder<ThreadInfo> queryBuilder = threadInfoDao.queryBuilder()
                .where(ThreadInfoDao.Properties.Url.eq(url), ThreadInfoDao.Properties.ThreadId.eq(threadId));
        if (queryBuilder.count() > 0) {
            ThreadInfo threadInfo = queryBuilder.build().forCurrentThread().unique();
            threadInfoDao.delete(threadInfo);
        }
    }

    /**
     *
     * @param threadInfo
     */
    public static void insertThread(ThreadInfo threadInfo) {
        ThreadInfoDao threadInfoDao = MyApplication.getInstance().getDaoSession().getThreadInfoDao();
        threadInfoDao.insert(threadInfo);
    }

    /**
     * 获取对应状态的photo信息
     * @param userId
     * @param success 状态有 成功 “true”  失败 “false”  下载中 “load”
     * */
    public static List<PhotoDownLoadInfo> getPhotos(String userId, String success){
        PhotoDownLoadInfoDao photoDownLoadInfoDao = MyApplication.getInstance().getDaoSession().getPhotoDownLoadInfoDao();
        List<PhotoDownLoadInfo> photos = new ArrayList<>();

        QueryBuilder<PhotoDownLoadInfo> queryBuilder = photoDownLoadInfoDao.queryBuilder()
                .where(PhotoDownLoadInfoDao.Properties.UserId.eq(userId), PhotoDownLoadInfoDao.Properties.Status.eq(success))
                .orderDesc(PhotoDownLoadInfoDao.Properties.DownLoadTime);
        if (queryBuilder.count() > 0) {
            photos = queryBuilder.build().forCurrentThread().list();
        }
        return photos;
    }

    /**
     * 获取对应状态的photo信息
     * @param userId
     * @param success 状态有 成功 “true”  失败 “false”  下载中 “load”
     * */
    public static List<PhotoDownLoadInfo> getPhotosOrderByTime(String userId, String success){
        PhotoDownLoadInfoDao photoDownLoadInfoDao = MyApplication.getInstance().getDaoSession().getPhotoDownLoadInfoDao();
        List<PhotoDownLoadInfo> photos = new ArrayList<>();

        QueryBuilder<PhotoDownLoadInfo> queryBuilder = photoDownLoadInfoDao.queryBuilder()
                .where(PhotoDownLoadInfoDao.Properties.UserId.eq(userId), PhotoDownLoadInfoDao.Properties.Status.eq(success))
                .orderDesc(PhotoDownLoadInfoDao.Properties.DownLoadTime);
        if (queryBuilder.count() > 0) {
            photos = queryBuilder.build().forCurrentThread().list();
        }
        return photos;
    }

    /**
     * 获取对应状态的photo信息
     * @param userId
     * @param status1 状态有 成功 “true”  失败 “false”  下载中 “load” 原图上传中 upload
     * @param status2 状态有 成功 “true”  失败 “false”  下载中 “load” 原图上传中 upload
     * */
    public static List<PhotoDownLoadInfo> getPhotos(String userId, String status1, String status2){
        PhotoDownLoadInfoDao photoDownLoadInfoDao = MyApplication.getInstance().getDaoSession().getPhotoDownLoadInfoDao();
        List<PhotoDownLoadInfo> photos = new ArrayList<>();

        QueryBuilder queryBuilder = photoDownLoadInfoDao.queryBuilder();
        queryBuilder.where(PhotoDownLoadInfoDao.Properties.UserId.eq(userId),
                queryBuilder.or(PhotoDownLoadInfoDao.Properties.Status.eq(status1), PhotoDownLoadInfoDao.Properties.Status.eq(status2)))
                .orderDesc(PhotoDownLoadInfoDao.Properties.DownLoadTime);

        if (queryBuilder.count() > 0) {
            photos = queryBuilder.build().forCurrentThread().list();
        }
        return photos;
    }


    /**
     * 获取所有userId
     *
     * */
    public static List<String> getAllUsers(){
        List<String> users = new ArrayList<>();
        PhotoDownLoadInfoDao photoDownLoadInfoDao = MyApplication.getInstance().getDaoSession().getPhotoDownLoadInfoDao();
        List<PhotoDownLoadInfo> photos = new ArrayList<>();

        QueryBuilder queryBuilder = photoDownLoadInfoDao.queryBuilder();

        if (queryBuilder.count() > 0) {
            photos = queryBuilder.build().forCurrentThread().list();
        }

        for (PhotoDownLoadInfo photoDownLoadInfo : photos) {
            if (!users.contains(photoDownLoadInfo.getUserId())) {
                users.add(photoDownLoadInfo.getUserId());
            }
        }
        return users;
    }


    /**
     * 获取所有的照片信息
     * */
    public static List<PhotoDownLoadInfo> getAllPhotos(String userId){
        PhotoDownLoadInfoDao photoDownLoadInfoDao = MyApplication.getInstance().getDaoSession().getPhotoDownLoadInfoDao();
        List<PhotoDownLoadInfo> photos = new ArrayList<>();

        QueryBuilder queryBuilder = photoDownLoadInfoDao.queryBuilder()
                .where(PhotoDownLoadInfoDao.Properties.UserId.eq(userId));

        if (queryBuilder.count() > 0) {
            photos = queryBuilder.build().forCurrentThread().list();
        }
        return photos;
    }

    /**
     * 根据photid获取下载成功的图片信息
     *
     * */
    public static List<PhotoDownLoadInfo> getPhotosByPhotoId(String photoId){
        PhotoDownLoadInfoDao photoDownLoadInfoDao = MyApplication.getInstance().getDaoSession().getPhotoDownLoadInfoDao();
        List<PhotoDownLoadInfo> photos = new ArrayList<>();

        QueryBuilder queryBuilder = photoDownLoadInfoDao.queryBuilder()
                .where(PhotoDownLoadInfoDao.Properties.PhotoId.eq(photoId), PhotoDownLoadInfoDao.Properties.Status.eq("true"));

        if (queryBuilder.count() > 0) {
            photos = queryBuilder.build().forCurrentThread().list();
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
    public static synchronized void insertPhotos(String userId, CopyOnWriteArrayList<DownloadFileStatus> list, String loadTime, String status){
        PhotoDownLoadInfoDao photoDownLoadInfoDao = MyApplication.getInstance().getDaoSession().getPhotoDownLoadInfoDao();
        ArrayList<PhotoDownLoadInfo> photoDownLoadInfos = new ArrayList<>();
        PhotoDownLoadInfo photoDownLoadInfo;
        for (int i=0;i<list.size();i++) {
            DownloadFileStatus fileStatus = list.get(i);
            photoDownLoadInfo = new PhotoDownLoadInfo();
            photoDownLoadInfo.setUserId(userId);
            photoDownLoadInfo.setPhotoId(fileStatus.getPhotoId());
            photoDownLoadInfo.setUrl(fileStatus.getUrl());
            photoDownLoadInfo.setSize(fileStatus.getTotalSize());
            photoDownLoadInfo.setPreviewUrl(fileStatus.getPhotoThumbnail());
            photoDownLoadInfo.setShootTime(fileStatus.getShootOn());
            photoDownLoadInfo.setDownLoadTime(loadTime);
            photoDownLoadInfo.setIsVideo(fileStatus.getIsVideo());
            photoDownLoadInfo.setStatus(status);
            photoDownLoadInfo.setFailedTime(fileStatus.getFailedTime());
            photoDownLoadInfo.setPhotoThumbnail_512(fileStatus.getPhotoThumbnail_512());
            photoDownLoadInfo.setPhotoThumbnail_1024(fileStatus.getPhotoThumbnail_1024());
            photoDownLoadInfo.setVideoWidth(fileStatus.getVideoWidth());
            photoDownLoadInfo.setVideoHeight(fileStatus.getVideoHeight());

            photoDownLoadInfos.add(photoDownLoadInfo);
        }

        photoDownLoadInfoDao.insertInTx(photoDownLoadInfos);
    }


    public static synchronized void deletePhotos(String userId, CopyOnWriteArrayList<DownloadFileStatus> list){
        PhotoDownLoadInfoDao photoDownLoadInfoDao = MyApplication.getInstance().getDaoSession().getPhotoDownLoadInfoDao();
        List<PhotoDownLoadInfo> photoList;
        QueryBuilder queryBuilder = photoDownLoadInfoDao.queryBuilder();
        for (int i = 0; i < list.size(); i++) {
            queryBuilder.where(PhotoDownLoadInfoDao.Properties.UserId.eq(userId), PhotoDownLoadInfoDao.Properties.PhotoId.eq(list.get(i).getPhotoId()));
            if (queryBuilder.count() > 0) {
                photoList = queryBuilder.build().forCurrentThread().list();
                photoDownLoadInfoDao.deleteInTx(photoList);
            }
        }

    }

    /**
     *
     * 删除所有下载照片
     *
     * */
    public static synchronized int deleteDownloadPhoto(String userId){
        PhotoDownLoadInfoDao photoDownLoadInfoDao = MyApplication.getInstance().getDaoSession().getPhotoDownLoadInfoDao();
        List<PhotoDownLoadInfo> photoList = new ArrayList<>();
        QueryBuilder queryBuilder = photoDownLoadInfoDao.queryBuilder()
                .where(PhotoDownLoadInfoDao.Properties.UserId.eq(userId), PhotoDownLoadInfoDao.Properties.Status.eq("true"));

        if (queryBuilder.count() > 0) {
            photoList = queryBuilder.build().forCurrentThread().list();
            photoDownLoadInfoDao.deleteInTx(photoList);

        }
        return photoList.size();
    }

    /**
     *
     * 根据用户id删除所有失败照片
     *
     * */
    public static synchronized void deleteDownloadFailPhotoByUserId(String userId){
        PhotoDownLoadInfoDao photoDownLoadInfoDao = MyApplication.getInstance().getDaoSession().getPhotoDownLoadInfoDao();
        QueryBuilder queryBuilder = photoDownLoadInfoDao.queryBuilder();
        queryBuilder.where(PhotoDownLoadInfoDao.Properties.UserId.eq(userId),
                queryBuilder.or(PhotoDownLoadInfoDao.Properties.Status.eq("false"), PhotoDownLoadInfoDao.Properties.Status.eq("upload")));

        if (queryBuilder.count() > 0) {
            List<PhotoDownLoadInfo> photos = queryBuilder.build().forCurrentThread().list();
            photoDownLoadInfoDao.deleteInTx(photos);
        }
    }

    /**
     * 删除重复的照片
     *
     * */
    public static synchronized void deleteRepeatPhoto(String userId,PhotoDownLoadInfo info){
        PhotoDownLoadInfoDao photoDownLoadInfoDao = MyApplication.getInstance().getDaoSession().getPhotoDownLoadInfoDao();
        QueryBuilder<PhotoDownLoadInfo> queryBuilder = photoDownLoadInfoDao.queryBuilder()
                .where(PhotoDownLoadInfoDao.Properties.UserId.eq(userId), PhotoDownLoadInfoDao.Properties.Id.eq(info.getId()));

        if (queryBuilder.count() > 0) {
            PhotoDownLoadInfo photoDownLoadInfo = queryBuilder.build().forCurrentThread().unique();
            photoDownLoadInfoDao.delete(photoDownLoadInfo);
        }
    }

    /**
     * 根据xxx删除图片
     *
     * */
    public static synchronized void deletePhotoByPhotoId(String userId,String photoId){
        PhotoDownLoadInfoDao photoDownLoadInfoDao = MyApplication.getInstance().getDaoSession().getPhotoDownLoadInfoDao();
        QueryBuilder<PhotoDownLoadInfo> queryBuilder = photoDownLoadInfoDao.queryBuilder()
                .where(PhotoDownLoadInfoDao.Properties.UserId.eq(userId), PhotoDownLoadInfoDao.Properties.PhotoId.eq(photoId));

        if (queryBuilder.count() > 0) {
            List<PhotoDownLoadInfo> photoDownLoadInfos = queryBuilder.build().forCurrentThread().list();
            photoDownLoadInfoDao.deleteInTx(photoDownLoadInfos);
        }
    }

    /**
     * 更新状态为load的图片信息
     *
     * */
    public static synchronized void updateLoadPhotos(String userId, String status,String downloadTime,String size,String photoId,String failedTime){
        PhotoDownLoadInfoDao photoDownLoadInfoDao = MyApplication.getInstance().getDaoSession().getPhotoDownLoadInfoDao();
        QueryBuilder<PhotoDownLoadInfo> queryBuilder = photoDownLoadInfoDao.queryBuilder()
                .where(PhotoDownLoadInfoDao.Properties.UserId.eq(userId), PhotoDownLoadInfoDao.Properties.PhotoId.eq(photoId));

        if (queryBuilder.count() > 0) {
            List<PhotoDownLoadInfo> photoDownLoadInfos = queryBuilder.build().forCurrentThread().list();
            for (int i = 0; i < photoDownLoadInfos.size(); i++) {
                PhotoDownLoadInfo photoDownLoadInfo = photoDownLoadInfos.get(i);
                photoDownLoadInfo.setFailedTime(failedTime);
                photoDownLoadInfo.setStatus(status);
                photoDownLoadInfo.setDownLoadTime(downloadTime);
                photoDownLoadInfo.setSize(size);

            }
            photoDownLoadInfoDao.updateInTx(photoDownLoadInfos);
        }

        EventBus.getDefault().post(new TabIndicatorUpdateEvent(0, 0, true));
    }

    /**
     * 用列表循环更新状态为load的图片信息，较单个的更新速度更快
     *
     * */
    public static synchronized void updateLoadPhotoList(String userId, String status,String downloadTime,String size,List<PhotoDownLoadInfo> list){
        PhotoDownLoadInfoDao photoDownLoadInfoDao = MyApplication.getInstance().getDaoSession().getPhotoDownLoadInfoDao();
        QueryBuilder<PhotoDownLoadInfo> queryBuilder = photoDownLoadInfoDao.queryBuilder();

        for (int i = 0; i < list.size(); i++) {
            PhotoDownLoadInfo info = list.get(i);
            queryBuilder.where(PhotoDownLoadInfoDao.Properties.UserId.eq(userId), PhotoDownLoadInfoDao.Properties.PhotoId.eq(info.getPhotoId()));

            if (queryBuilder.count() > 0) {
                List<PhotoDownLoadInfo> photoDownLoadInfos = queryBuilder.build().forCurrentThread().list();
                for (int j = 0; j < photoDownLoadInfos.size(); j++) {
                    PhotoDownLoadInfo photoDownLoadInfo = photoDownLoadInfos.get(j);
                    photoDownLoadInfo.setFailedTime("");
                    photoDownLoadInfo.setStatus(status);
                    photoDownLoadInfo.setDownLoadTime(downloadTime);
                    photoDownLoadInfo.setSize(size);

                }
                photoDownLoadInfoDao.updateInTx(photoDownLoadInfos);
            }
        }
    }

    public static synchronized CopyOnWriteArrayList<PhotoDownLoadInfo> getExistPhoto(String userId){
        CopyOnWriteArrayList<PhotoDownLoadInfo> list = new CopyOnWriteArrayList<>();
        PhotoDownLoadInfoDao photoDownLoadInfoDao = MyApplication.getInstance().getDaoSession().getPhotoDownLoadInfoDao();
        QueryBuilder<PhotoDownLoadInfo> queryBuilder = photoDownLoadInfoDao.queryBuilder()
                .where(PhotoDownLoadInfoDao.Properties.UserId.eq(userId));

        if (queryBuilder.count() > 0) {
            list.addAll(queryBuilder.build().forCurrentThread().list());
        }
        return list;
    }

    /**
     * 更新json数据库
     * @param jsonArray
     * @param type json的类型
     */
    public static synchronized void updateJsonInfos(JSONArray jsonArray, String type) {
        JsonInfoDao jsonInfoDao = MyApplication.getInstance().getDaoSession().getJsonInfoDao();
        ArrayList<JsonInfo> list = new ArrayList<>();
        QueryBuilder<JsonInfo> queryBuilder = jsonInfoDao.queryBuilder()
                .where(JsonInfoDao.Properties.JsonType.eq(type));
        if (queryBuilder.count() > 0) {
            list.addAll(queryBuilder.build().forCurrentThread().list());
        }
        //清除旧数据
        jsonInfoDao.deleteInTx(list);

        list.clear();
        JsonInfo jsonInfo;
        //获取新数据
        for (int i = 0; i < jsonArray.size(); i++) {
            jsonInfo = new JsonInfo();
            jsonInfo.setJsonString(jsonArray.getJSONObject(i).toString());
            jsonInfo.setJsonType(type);
            list.add(jsonInfo);
        }
        jsonInfoDao.insertInTx(list);

    }

    /**
     * 更新json数据库
     * @param jsonArray
     * @param type json的类型
     */
    public static synchronized void insertRefreshPPFlag(JSONArray jsonArray, String type) {
        JsonInfoDao jsonInfoDao = MyApplication.getInstance().getDaoSession().getJsonInfoDao();
        ArrayList<JsonInfo> list = new ArrayList<>();
        JSONObject jsonObject;
        for (int i = 0; i < jsonArray.size(); i++) {
            jsonObject = jsonArray.getJSONObject(i);
            JsonInfo jsonInfo = new JsonInfo();
            jsonInfo.setJsonType(type);
            jsonInfo.setJsonString(JsonInfo.getNeedRefreshString(jsonObject.getString("code"), jsonObject.getString("bindDate")));
            list.add(jsonInfo);
        }
        jsonInfoDao.insertInTx(list);
    }

    /**
     * 获取json存储数据
     * @param type
     * @return
     */
    public static synchronized ArrayList<JsonInfo> getJsonInfos(String type) {
        JsonInfoDao jsonInfoDao = MyApplication.getInstance().getDaoSession().getJsonInfoDao();
        ArrayList<JsonInfo> list = new ArrayList<>();
        QueryBuilder<JsonInfo> queryBuilder = jsonInfoDao.queryBuilder()
                .where(JsonInfoDao.Properties.JsonType.eq(type));
        if (queryBuilder.count() > 0) {
            list.addAll(queryBuilder.build().forCurrentThread().list());
        }
        return list;
    }

    /**
     * 删除对应类型的数据
     * @param type
     * @return
     */
    public static synchronized void deleteJsonInfosByType(String type) {
        JsonInfoDao jsonInfoDao = MyApplication.getInstance().getDaoSession().getJsonInfoDao();
        ArrayList<JsonInfo> list = new ArrayList<>();
        QueryBuilder<JsonInfo> queryBuilder = jsonInfoDao.queryBuilder()
                .where(JsonInfoDao.Properties.JsonType.eq(type));
        if (queryBuilder.count() > 0) {
            list.addAll(queryBuilder.build().forCurrentThread().list());
            jsonInfoDao.deleteInTx(list);
        }
    }

    /**
     * 删除对应类型的数据
     * @return
     */
    public static synchronized void deleteJsonInfos() {
        JsonInfoDao jsonInfoDao = MyApplication.getInstance().getDaoSession().getJsonInfoDao();
        jsonInfoDao.deleteAll();
    }

    /**
     * 删除一条数据
     * @param type
     * @return
     */
    public static synchronized void deleteJsonInfosByTypeAndString(String type, String str) {
        PictureAirLog.d("delete str ---> " + str);
        JsonInfoDao jsonInfoDao = MyApplication.getInstance().getDaoSession().getJsonInfoDao();
        ArrayList<JsonInfo> list = new ArrayList<>();
        QueryBuilder<JsonInfo> queryBuilder = jsonInfoDao.queryBuilder()
                .where(JsonInfoDao.Properties.JsonType.eq(type), JsonInfoDao.Properties.JsonString.like("%" + str + "%"));
        if (queryBuilder.count() > 0) {
            list.addAll(queryBuilder.build().forCurrentThread().list());
            jsonInfoDao.deleteInTx(list);
        }
    }

    /**
     * 删除对应类型的数据
     * @param jsonString
     * @return
     */
    public static synchronized boolean isJsonInfoExist(String jsonString) {
        JsonInfoDao jsonInfoDao = MyApplication.getInstance().getDaoSession().getJsonInfoDao();
        QueryBuilder<JsonInfo> queryBuilder = jsonInfoDao.queryBuilder()
                .where(JsonInfoDao.Properties.JsonString.like("%" + jsonString + "%"));
        return queryBuilder.count() > 0;
    }
}
