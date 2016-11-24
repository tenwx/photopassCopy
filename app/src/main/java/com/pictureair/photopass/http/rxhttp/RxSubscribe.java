package com.pictureair.photopass.http.rxhttp;

import com.pictureair.photopass.util.PictureAirLog;

import rx.Subscriber;

/**
 * Created by pengwu on 16/11/23.
 */

public abstract class RxSubscribe<T> extends Subscriber<T> {

    private static final int HTTP_ERROR = 401;//请求失败的错误代码

    @Override
    public void onNext(T t) {
        _onNext(t);
    }

    @Override
    public void onError(Throwable e) {
        if (e instanceof ServerException) {
            _onError(((ServerException) e).getState());
        } else {
            PictureAirLog.e(e.toString());
            _onError(HTTP_ERROR);
        }

    }


    public abstract void _onNext(T t);

    public abstract void _onError(int status);

}
