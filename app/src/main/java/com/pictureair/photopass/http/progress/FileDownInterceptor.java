package com.pictureair.photopass.http.progress;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Created by pengwu on 16/7/5.
 */
public class FileDownInterceptor implements Interceptor {

    private ProgressBean progressBean = new ProgressBean();
    private ProgressHandler mProgressHandler;
    private boolean downLoad;
    private final ProgressListener progressListener = new ProgressListener() {
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
    };;

    @Override
    public Response intercept(Chain chain) throws IOException {
//        if (downLoad) {
            okhttp3.Response originalResponse = chain.proceed(chain.request());
            return originalResponse.newBuilder().body(
                    new ProgressResponseBody(originalResponse.body(), progressListener))
                    .build();
//        }
//        return chain.proceed(chain.request());
    }

    public void setProgressHandler(ProgressHandler progressHandler){
        this.mProgressHandler = progressHandler;
    }

//    public boolean isDownLoad() {
//        return downLoad;
//    }
//
//    public void setDownLoad(boolean downLoad) {
//        this.downLoad = downLoad;
//    }
}
