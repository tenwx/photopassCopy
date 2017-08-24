package com.pictureair.hkdlphotopass.http.rxhttp;

import com.pictureair.hkdlphotopass.util.PictureAirLog;

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

        //如果需要打印详细error信息，取消注释如下代码
//        Writer writer = new StringWriter();
//        PrintWriter printWriter = new PrintWriter(writer);
//        e.printStackTrace(printWriter);
//        Throwable cause = e.getCause();
//        while (cause != null) {
//            cause.printStackTrace(printWriter);
//            cause = cause.getCause();
//        }
//        printWriter.close();
//        String result = writer.toString();
//        PictureAirLog.d("e--->" + result);

        if (e instanceof ServerException) {
            _onError(((ServerException) e).getState());
        } else {
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
