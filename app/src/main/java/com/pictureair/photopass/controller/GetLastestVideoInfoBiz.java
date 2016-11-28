package com.pictureair.photopass.controller;

import android.content.Context;
import android.os.Handler;

import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppUtil;

/**
 * Created by bauer_bao on 16/9/18.
 */
public class GetLastestVideoInfoBiz implements IGetLastestVideoInfoBiz {
    private Context context;
    private PictureAirDbManager pictureAirDbManager;
    private Handler handler;
    /**
     * 需要从网络获取最新数据
     */
    public static final int NEED_GET_NEW_INFO = 101;
    /**
     * 拿到最新的数据，并且视频已经制作完成
     */
    public static final int GET_RIGHT_VIDEO_INFO_DONE = 102;
    /**
     * 拿到最新的数据，但是视频还没有制作完成
     */
    public static final int GET_RIGHT_VIDEO_INFO_FAILED = 103;

    public GetLastestVideoInfoBiz(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    @Override
    public void needGetLastestVideoInfoFromNetByDB(String photoId) {
        if (pictureAirDbManager == null) {
            pictureAirDbManager = new PictureAirDbManager(context);
        }

        //查询数据库
        boolean result = pictureAirDbManager.needGetLastestVideoInfoFromNetwork(photoId);
        handler.obtainMessage(NEED_GET_NEW_INFO, result).sendToTarget();
    }

    @Override
    public void getLastestVideoInfoFromNet(String tokenId, String photoId, Handler handler) {
        API1.getNewPhotosInfo(tokenId, photoId, 0, handler);
    }

    @Override
    public void updateLastestVideoInfo(PhotoInfo videoInfo) {
        if (pictureAirDbManager == null) {
            pictureAirDbManager = new PictureAirDbManager(context);
        }

        pictureAirDbManager.updatePhotoInfo(videoInfo);

        //判断从网络获取的视频数据是否已经制作完成
        if (AppUtil.isOldVersionOfTheVideo(videoInfo.getPhotoOriginalURL(), videoInfo.getPhotoThumbnail_1024(), videoInfo.getPhotoThumbnail_512(), videoInfo.getPhotoThumbnail_128())) {
            //依旧不是最新的url
            handler.sendEmptyMessage(GET_RIGHT_VIDEO_INFO_FAILED);

        } else {
            //拿到了制作完成的url
            handler.sendEmptyMessage(GET_RIGHT_VIDEO_INFO_DONE);
        }
    }
}
