package com.pictureair.photopass.controller;

import com.pictureair.photopass.entity.PhotoInfo;

/**
 * Created by bauer_bao on 16/9/19.
 */
public interface IGetLastestVideoInfoView {
    /**
     * 判断成功的回调
     * @param dealStatus 处理结果的状态
     * @param position 当前处理的position
     * @param photoInfo 成功处理完成的photoInfo
     * @param checkByNetwork 是否通过网络，还是通过本地判断
     */
    void getNewInfoDone(int dealStatus, int position, PhotoInfo photoInfo, boolean checkByNetwork);
}
