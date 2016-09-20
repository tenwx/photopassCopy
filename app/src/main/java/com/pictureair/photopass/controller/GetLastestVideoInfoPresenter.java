package com.pictureair.photopass.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.API1;

/**
 * Created by bauer_bao on 16/9/19.
 */
public class GetLastestVideoInfoPresenter {
    private IGetLastestVideoInfoView iGetLastestVideoInfoView;
    private IGetLastestVideoInfoBiz iGetLastestVideoInfoBiz;
    private String tokenId;
    private String photoId;
    private int position;

    public static final int VIDEO_MAKING = 11;
    public static final int VIDEO_FINISHED = 12;
    public static final int NETWORK_ERROR = 13;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case API1.GET_NEW_PHOTOS_INFO_SUCCESS://获取最新数据成功，需要将最新数据替换数据库
                    final PhotoInfo photoInfo = (PhotoInfo) msg.obj;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            iGetLastestVideoInfoBiz.updateLastestVideoInfo(photoInfo);
                        }
                    }).start();

                    break;

                case API1.GET_NEW_PHOTOS_INFO_FAILED:
                    iGetLastestVideoInfoView.getNewInfoDone(NETWORK_ERROR, position);
                    break;

                case GetLastestVideoInfoBiz.NEED_GET_NEW_INFO:
                    if ((boolean) msg.obj) {//需要从网络获取
                        iGetLastestVideoInfoBiz.getLastestVideoInfoFromNet(tokenId, photoId, handler);
                    } else {//已经是最新的，无需从网络获取
                        iGetLastestVideoInfoView.getNewInfoDone(VIDEO_FINISHED, position);
                    }
                    break;

                case GetLastestVideoInfoBiz.UPDATE_VIDEO_INFO://更新图片成功
                    iGetLastestVideoInfoView.getNewInfoDone(VIDEO_FINISHED, position);
                    break;

                default:
                    break;
            }
            return false;
        }
    });

    public GetLastestVideoInfoPresenter(Context context, IGetLastestVideoInfoView iGetLastestVideoInfoView, String tokenId) {
        this.tokenId = tokenId;
        this.iGetLastestVideoInfoView = iGetLastestVideoInfoView;
        iGetLastestVideoInfoBiz = new GetLastestVideoInfoBiz(context, handler);
    }

    public void videoInfoClick(String str, int position){
        this.photoId = str;
        this.position = position;
        new Thread(new Runnable() {
            @Override
            public void run() {
                iGetLastestVideoInfoBiz.needGetLastestVideoInfoFromNetByDB(photoId);
            }
        }).start();
    }
}
