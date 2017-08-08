package com.pictureair.photopassCopy.http.rxhttp;

import com.pictureair.photopassCopy.MyApplication;
import com.pictureair.photopassCopy.activity.LoginActivity;
import com.pictureair.photopassCopy.entity.BasicResult;
import com.pictureair.photopassCopy.util.AppExitUtil;
import com.pictureair.photopassCopy.util.AppManager;
import com.pictureair.photopassCopy.util.Common;
import com.pictureair.photopassCopy.util.PictureAirLog;
import com.pictureair.photopassCopy.util.SPUtils;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by pengwu on 16/11/23.
 */

public class RxHelper {

    private static final int HTTP_ERROR = 401;//请求失败的错误代码

    public static <T>Observable.Transformer<BasicResult<T>, T> handleResult() {
        return new Observable.Transformer<BasicResult<T>, T>() {
            @Override
            public Observable<T> call(Observable<BasicResult<T>> basicResultObservable) {
                return basicResultObservable.flatMap(new Func1<BasicResult<T>, Observable<T>>() {
                    @Override
                    public Observable<T> call(BasicResult<T> response) {

                        if (response != null) {
                            if (response.getStatus() == 200){
                                return createData(response.getResult());
                            } else {
                                PictureAirLog.d("network error--> " + response.toString());
                                switch (response.getStatus()) {
                                    case 6035://Current certification has expired, please login again
                                    case 6079://Current certification has expired, please login again
                                    case 6080://token已经过期
                                    case 6074://get token error
                                    case 6075://set token error
                                    case 6151://query token error
                                    case 6153://未授权
                                    case 6034://please login
                                    case 5030://not login
                                    case 5011://not login
                                        boolean isLogin = SPUtils.getBoolean(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ISLOGIN, false);
                                        if (isLogin || !AppManager.getInstance().checkActivity(LoginActivity.class)){//通过登录标记得到的已登录状态，或者 通过activity判断的已登录状态
                                            //如果在登录状态，需要退出操作
                                            SPUtils.remove(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ISLOGIN);
                                            AppExitUtil.getInstance().AppReLogin();
                                        } else {
                                            //没有登录
                                            return Observable.error(new ServerException(response.getStatus()));
                                        }
                                        break;
                                    default:
                                        return Observable.error(new ServerException(response.getStatus()));
                                }
                            }
                        }
                        return Observable.error(new ServerException(HTTP_ERROR));
                    }
                });
            }
        };
    }


    /**
     * 创建成功的数据
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> Observable<T> createData(final T data) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                try {
                    subscriber.onNext(data);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });

    }
}
