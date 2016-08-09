package com.pictureair.photopass.http;

import android.text.TextUtils;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 请求拦截，可以动态修改baseUrl
 * Created by pengwu on 16/7/4.
 */
public class DynamicBaseUrlInterceptor implements Interceptor {
    private volatile String host;

    /**
     * 调用该函数设置baseUrl，下次请求调用intercept修改url
     * */
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        if (!TextUtils.isEmpty(host)) {
            HttpUrl newUrl = originalRequest.url().newBuilder()
                    .host(host)
                    .build();
            originalRequest = originalRequest.newBuilder()
                    .url(newUrl)
                    .build();
        }

        return chain.proceed(originalRequest);
    }
}
