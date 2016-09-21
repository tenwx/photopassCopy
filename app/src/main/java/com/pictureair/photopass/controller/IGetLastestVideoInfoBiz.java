package com.pictureair.photopass.controller;

import android.os.Handler;

import com.pictureair.photopass.entity.PhotoInfo;

/**
 * Created by bauer_bao on 16/9/18.
 */
public interface IGetLastestVideoInfoBiz {
    void needGetLastestVideoInfoFromNetByDB(String photoId);

    void getLastestVideoInfoFromNet(String tokenId, String photoId, Handler handler);

    void updateLastestVideoInfo(PhotoInfo videoInfo);
}
