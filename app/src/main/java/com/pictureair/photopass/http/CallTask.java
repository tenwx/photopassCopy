package com.pictureair.photopass.http;

import android.util.Log;

import com.pictureair.photopass.util.AppExitUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ResponseCallback;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by pengwu on 16/7/5.
 */
public class CallTask<T>{
    private static final String Tag = "CallTask";
    private Call<T> mCall;
    private static final int HTTP_ERROR = 401;//请求失败的错误代码

    public CallTask(Call call){
        this.mCall = call;
    }

    public void handleResponse(final ResponseCallback httpCallback) {
        mCall.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (!call.isCanceled()) {
                    getAPISuccess(response, httpCallback);
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable throwable) {
                PictureAirLog.e(Tag, throwable.toString());
                if (!call.isCanceled()){
                    httpCallback.onFailure(HTTP_ERROR);
                }
            }
        });
    }

    private static void getAPISuccess(Response response, ResponseCallback httpCallback){
        if (response.isSuccessful() && response.errorBody() == null) {
            int status = response.code();
            if (response.code() == 200) {
                httpCallback.onSuccess(response.body());
            } else {
                //失败返回错误码
                switch (response.code()) {
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
                        if (AppExitUtil.isAppExit){
                            httpCallback.onFailure(response.code());
                        }else {
                            AppExitUtil.getInstance().AppReLogin();
                        }
                        break;
                    default:
                        httpCallback.onFailure(response.code());
                        break;
                }
            }
        }else if(response.errorBody() != null){
            httpCallback.onFailure(HTTP_ERROR);
        }
    }

    public void Cancle(){
        if (mCall != null){
            mCall.cancel();
        }
    }

}
