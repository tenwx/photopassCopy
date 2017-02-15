package com.pictureair.photopass.controller;

import com.pictureair.photopass.entity.PhotoInfo;

/**
 * Created by bauer_bao on 17/2/15.
 */

public interface GetLastestVideoInfoListener {
    void getVideoInfoCompleted(int dealStatus, PhotoInfo photoInfo, boolean checkByNetwork);
}
