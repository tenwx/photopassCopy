package com.pictureair.photopassCopy.controller;

import android.content.Context;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopassCopy.entity.PhotoInfo;
import com.pictureair.photopassCopy.greendao.PictureAirDbManager;
import com.pictureair.photopassCopy.http.rxhttp.RxSubscribe;
import com.pictureair.photopassCopy.http.rxhttp.ServerException;
import com.pictureair.photopassCopy.util.API2;
import com.pictureair.photopassCopy.util.AppUtil;
import com.pictureair.photopassCopy.util.JsonUtil;
import com.pictureair.photopassCopy.util.PictureAirLog;
import com.trello.rxlifecycle.components.RxActivity;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 获取视频最新数据的逻辑
 * Created by bauer_bao on 16/9/18.
 */
public class GetLastestVideoInfoBiz implements GetLastestVideoInfoContract.Model {
    private Context context;
    private GetLastestVideoInfoContract.ModelListener listener;
    private PhotoInfo lastestVideoInfo;

    public static final int VIDEO_MAKING = 11;
    public static final int VIDEO_FINISHED = 12;
    public static final int NETWORK_ERROR = 13;

    /**
     * 不需要从网络获取最新数据，为了和服务器返回的数字码错开，因此数字比较大
     */
    public static final int NEEDNOT_GET_NEW_INFO = 11100;

    /**
     * 拿到最新的数据，并且视频已经制作完成
     */
    public static final int GET_RIGHT_VIDEO_INFO_DONE = 11102;

    /**
     * 拿到最新的数据，但是视频还没有制作完成
     */
    public static final int GET_RIGHT_VIDEO_INFO_FAILED = 11103;

    public GetLastestVideoInfoBiz(Context context, GetLastestVideoInfoContract.ModelListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    public void getNewPhotoInfo(final String photoId, final String tokenId) {
        Observable.just(photoId)
                .subscribeOn(Schedulers.io())
                .map(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        //查询数据库
                        return PictureAirDbManager.needGetLastestVideoInfoFromNetwork(s);
                    }
                })
                .flatMap(new Func1<Boolean, Observable<JSONObject>>() {
                    @Override
                    public Observable<JSONObject> call(Boolean aBoolean) {
                        PictureAirLog.out("need get the info from net ? the flag by db is " + aBoolean);
                        if (aBoolean) {//需要从网络获取
                            return API2.getNewPhotosInfo(tokenId, photoId);
                        } else {//已经是最新的，无需从网络获取
                            return Observable.error(new ServerException(NEEDNOT_GET_NEW_INFO));
                        }
                    }
                })
                .flatMap(new Func1<JSONObject, Observable<PhotoInfo>>() {
                    @Override
                    public Observable<PhotoInfo> call(JSONObject jsonObject) {
                        PictureAirLog.out("jsonobject---->" + jsonObject.toString());
                        JSONArray photos = jsonObject.getJSONArray("photos");
                        if (photos.size() > 0) {
                            PhotoInfo photoInfo = JsonUtil.getPhoto(photos.getJSONObject(0));
                            PictureAirLog.out("1024url---->" + photoInfo.getPhotoThumbnail_1024());
                            return Observable.just(photoInfo);

                        } else {
                            PictureAirLog.d("no size");
                            return Observable.error(new ServerException(401));
                        }
                    }
                })
                .map(new Func1<PhotoInfo, Integer>() {
                    @Override
                    public Integer call(PhotoInfo photoInfo) {
                        PictureAirLog.out("get the lastset video info");
                        lastestVideoInfo = photoInfo;
                        PictureAirDbManager.updatePhotoInfo(photoInfo);

                        //判断从网络获取的视频数据是否已经制作完成
                        if (AppUtil.isOldVersionOfTheVideo(photoInfo.getPhotoOriginalURL(), photoInfo.getPhotoThumbnail_1024(), photoInfo.getPhotoThumbnail_512(), photoInfo.getPhotoThumbnail_128())) {
                            //依旧不是最新的url
                            PictureAirLog.d("still not new");
                            return GET_RIGHT_VIDEO_INFO_FAILED;

                        } else {
                            //拿到了制作完成的url
                            PictureAirLog.d("got the new url");
                            return GET_RIGHT_VIDEO_INFO_DONE;
                        }
                    }
                })
                .compose(((RxActivity)context).<Integer>bindToLifecycle())
                .subscribe(new RxSubscribe<Integer>() {
                    @Override
                    public void _onNext(Integer integer) {
                        PictureAirLog.d("onnext result-->" + integer);
                        if (integer == GET_RIGHT_VIDEO_INFO_FAILED) {//视频依旧在制作中
                            listener.getVideoInfoCompleted(VIDEO_MAKING, null, true);

                        } else if (integer == GET_RIGHT_VIDEO_INFO_DONE) {//获取制作好的视频成功
                            lastestVideoInfo.setId(1L);
                            listener.getVideoInfoCompleted(VIDEO_FINISHED, lastestVideoInfo, true);
                        }
                    }

                    @Override
                    public void _onError(int status) {
                        PictureAirLog.d("onerror result-->" + status);
                        if (status == NEEDNOT_GET_NEW_INFO) {//已经是最新的数据，不需要从网络获取
                            listener.getVideoInfoCompleted(VIDEO_FINISHED, null, false);
                        } else {//网络请求失败 或者 请求到的数据为空
                            listener.getVideoInfoCompleted(NETWORK_ERROR, null, false);
                        }
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }
}
