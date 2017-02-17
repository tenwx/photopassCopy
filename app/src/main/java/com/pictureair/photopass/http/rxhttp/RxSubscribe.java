package com.pictureair.photopass.http.rxhttp;

import com.pictureair.photopass.util.PictureAirLog;

import retrofit2.adapter.rxjava.HttpException;
import rx.Subscriber;

/**
 * Created by pengwu on 16/11/23.
 *
 * ServerException 是后台返回的错误状态，比如密码错误之类的
 * HttpException 是系统级的错误
 */

public abstract class RxSubscribe<T> extends Subscriber<T> {

    private static final int HTTP_ERROR = 401;//请求失败的错误代码
    private static final String TAG = "Response result";

    @Override
    public void onNext(T t) {
        _onNext(t);
    }

    @Override
    public void onError(Throwable e) {
        PictureAirLog.e(TAG, e.toString());
        if (e instanceof ServerException) {
            _onError(((ServerException) e).getState());
        } else {
            PictureAirLog.e(e.toString());
            if (e != null && e instanceof HttpException) {
                HttpException exception = (HttpException) e;
                _onError(exception.code());
            } else {
                _onError(HTTP_ERROR);
            }
        }

    }


    public abstract void _onNext(T t);

    public abstract void _onError(int status);

}
