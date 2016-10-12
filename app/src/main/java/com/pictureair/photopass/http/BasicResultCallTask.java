package com.pictureair.photopass.http;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.entity.BasicResult;
import com.pictureair.photopass.util.AppExitUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ResponseCallback;
import com.pictureair.photopass.util.SPUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by pengwu on 16/7/5.
 */
public class BasicResultCallTask<T> extends ACallTask{
    private static final String Tag = "BasicResultCallTask";
    private Call<BasicResult<T>> mCall;
    private static final int HTTP_ERROR = 401;//请求失败的错误代码

    public BasicResultCallTask(Call call){
        this.mCall = call;
    }

    public void handleResponse(final ResponseCallback httpCallback) {
        CallTaskManager.getInstance().addTask(this);
        mCall.enqueue(new Callback<BasicResult<T>>() {
            @Override
            public void onResponse(Call<BasicResult<T>> call, Response<BasicResult<T>> response) {
                if (CallTaskManager.getInstance().containsTask(BasicResultCallTask.this)){
                    CallTaskManager.getInstance().removeTask(BasicResultCallTask.this);
                }
                if (response.isSuccessful() && response.errorBody() == null) {
                    getAPISuccess(response.body(), httpCallback);
                }else if (response.errorBody() != null){
                    httpCallback.onFailure(HTTP_ERROR);
                }
            }

            @Override
            public void onFailure(Call<BasicResult<T>> call, Throwable throwable) {
                PictureAirLog.e(Tag, throwable.toString());
                if (CallTaskManager.getInstance().containsTask(BasicResultCallTask.this)){
                    CallTaskManager.getInstance().removeTask(BasicResultCallTask.this);
                }
                if (!call.isCanceled()){
                    httpCallback.onFailure(HTTP_ERROR);
                }
            }
        });
    }

    private static void getAPISuccess(BasicResult response, ResponseCallback httpCallback){
        if (response != null){
            if (response.getStatus() == 200){
                httpCallback.onSuccess(response.getResult());
            }else{
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
                        if (!isLogin){//没有登录
                            httpCallback.onFailure(response.getStatus());
                        } else {//如果在登录状态，需要退出操作
                            SPUtils.remove(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ISLOGIN);
                            AppExitUtil.getInstance().AppReLogin();
                        }
                        break;
                    default:
                        httpCallback.onFailure(response.getStatus());
                        break;
                }
            }
        }
    }

    @Override
    public void cancle(){
        if (mCall != null){
            mCall.cancel();
        }
    }

}
