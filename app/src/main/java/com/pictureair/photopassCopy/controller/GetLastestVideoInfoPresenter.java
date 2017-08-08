package com.pictureair.photopassCopy.controller;

import android.content.Context;

import com.pictureair.photopassCopy.entity.PhotoInfo;
import com.pictureair.photopassCopy.util.PictureAirLog;

/**
 * Created by bauer_bao on 16/9/19.
 */
public class GetLastestVideoInfoPresenter implements GetLastestVideoInfoContract.Presenter, GetLastestVideoInfoContract.ModelListener{
    private GetLastestVideoInfoContract.View view;
    private GetLastestVideoInfoContract.Model model;
    private int position;

    public GetLastestVideoInfoPresenter(Context context, GetLastestVideoInfoContract.View v) {
        view = v;
        model = new GetLastestVideoInfoBiz(context, this);
    }

    @Override
    public void videoInfoClick(String photoId, String tokenId, int position){
        PictureAirLog.out("start check the lastest video info-->" + photoId + ";postion--->" + position);
        this.position = position;
        model.getNewPhotoInfo(photoId, tokenId);
    }

    @Override
    public void getVideoInfoCompleted(int dealStatus, PhotoInfo photoInfo, boolean checkByNetwork) {
        view.getNewInfoDone(dealStatus, position, photoInfo, checkByNetwork);
    }
}
