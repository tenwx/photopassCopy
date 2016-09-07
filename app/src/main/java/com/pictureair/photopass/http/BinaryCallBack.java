package com.pictureair.photopass.http;

import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ResponseCallback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by pengwu on 16/9/7.
 */
public class BinaryCallBack<T> {
    private static final String Tag = "CallTask";
    private Call<T> mCall;
    private static final int HTTP_ERROR = 401;//请求失败的错误代码

    public BinaryCallBack(Call call){
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
            httpCallback.onSuccess(response.body());
        }else if(response.errorBody() != null){
            httpCallback.onFailure(HTTP_ERROR);
        }
    }

    public void cancle(){
        if (mCall != null){
            mCall.cancel();
        }
    }
}
