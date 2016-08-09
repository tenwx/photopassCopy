package com.pictureair.photopass.http.progress;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * Created by pengwu on 16/7/5.
 */
public class ProgressHelper {
    private static ProgressBean progressBean = new ProgressBean();
    private static ProgressHandler mProgressHandler;
    public static Interceptor interceptor;
//    private static ProgressListener progressListener;

    public static OkHttpClient.Builder addProgress(OkHttpClient.Builder builder){
        if (builder == null){
            return new OkHttpClient.Builder();
        }

         final ProgressListener progressListener = new ProgressListener() {
            @Override
            public void onProgress(long progress, long total, boolean done) {
                Log.d("progress:",String.format("%d%% done\n",(100 * progress) / total));
                if (mProgressHandler == null){
                    return;
                }
                progressBean.setBytesRead(progress);
                progressBean.setContentLength(total);
                progressBean.setDone(done);
                mProgressHandler.sendMessage(progressBean);
            }
        };

        interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                okhttp3.Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder().body(
                        new ProgressResponseBody(originalResponse.body(), progressListener))
                        .build();
            }
        };
        //添加拦截器，自定义ResponseBody，添加下载进度
        builder.networkInterceptors().add(interceptor);
        return builder;
    }

    public static void setProgressHandler(ProgressHandler progressHandler){
        mProgressHandler = progressHandler;
    }

//    public static void setProgressListener(ProgressListener listener){
//        progressListener = listener;
//    }
}
