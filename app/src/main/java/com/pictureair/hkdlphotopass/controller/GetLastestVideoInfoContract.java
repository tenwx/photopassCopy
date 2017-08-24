package com.pictureair.hkdlphotopass.controller;

import com.pictureair.hkdlphotopass.entity.PhotoInfo;

/**
 * 获取最新视频照片的mvp的合并类
 * Created by bauer_bao on 17/3/13.
 */

public class GetLastestVideoInfoContract {

    public interface View {
        /**
         * 判断成功的回调
         * @param dealStatus 处理结果的状态
         * @param position 当前处理的position
         * @param photoInfo 成功处理完成的photoInfo
         * @param checkByNetwork 是否通过网络，还是通过本地判断
         */
        void getNewInfoDone(int dealStatus, int position, PhotoInfo photoInfo, boolean checkByNetwork);
    }

    public interface Presenter {
        /**
         * 视频点击
         * @param photoId
         * @param tokenId
         * @param position
         */
        void videoInfoClick(String photoId, String tokenId, int position);
    }

    public interface Model {
        /**
         * 获取最新照片数据
         * @param photoId
         * @param tokenId
         */
        void getNewPhotoInfo(String photoId, String tokenId);
    }

    public interface ModelListener {
        /**
         * 获取数据结果的回调
         * @param dealStatus
         * @param photoInfo
         * @param checkByNetwork
         */
        void getVideoInfoCompleted(int dealStatus, PhotoInfo photoInfo, boolean checkByNetwork);
    }
}
