package com.pictureair.photopass.controller;

import android.content.Context;
import android.os.Handler;

import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.API1;

/**
 * Created by bauer_bao on 16/9/18.
 */
public class GetLastestVideoInfoBiz implements IGetLastestVideoInfoBiz {
    private Context context;
    private PictureAirDbManager pictureAirDbManager;
    private Handler handler;
    public static final int NEED_GET_NEW_INFO = 101;
    public static final int UPDATE_VIDEO_INFO = 102;

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
        handler.sendEmptyMessage(UPDATE_VIDEO_INFO);
    }
}
