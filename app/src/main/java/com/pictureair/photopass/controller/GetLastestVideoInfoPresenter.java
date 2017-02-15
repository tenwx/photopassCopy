package com.pictureair.photopass.controller;

import android.content.Context;

import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.PictureAirLog;

/**
 * Created by bauer_bao on 16/9/19.
 */
public class GetLastestVideoInfoPresenter implements GetLastestVideoInfoListener{
    private IGetLastestVideoInfoView iGetLastestVideoInfoView;
    private IGetLastestVideoInfoBiz iGetLastestVideoInfoBiz;
    private int position;

    public GetLastestVideoInfoPresenter(Context context, IGetLastestVideoInfoView iGetLastestVideoInfoView) {
        this.iGetLastestVideoInfoView = iGetLastestVideoInfoView;
        iGetLastestVideoInfoBiz = new GetLastestVideoInfoBiz(context, this);
    }

    public void videoInfoClick(String photoId, String tokenId, int position){
        PictureAirLog.out("start check the lastest video info-->" + photoId + ";postion--->" + position);
        this.position = position;
        iGetLastestVideoInfoBiz.getNewPhotoInfo(photoId, tokenId);
    }

    @Override
    public void getVideoInfoCompleted(int dealStatus, PhotoInfo photoInfo, boolean checkByNetwork) {
        iGetLastestVideoInfoView.getNewInfoDone(dealStatus, position, photoInfo, checkByNetwork);
    }
}
