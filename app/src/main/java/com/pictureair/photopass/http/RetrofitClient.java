package com.pictureair.photopass.http;



import com.pictureair.photopass.http.progress.FileDownInterceptor;
import com.pictureair.photopass.http.progress.ProgressHelper;
import com.pictureair.photopass.util.Common;

import java.util.concurrent.TimeUnit;

import converter.fastjson.FastjsonConverterFactory;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 *
 * 配置Retrofit
 * Created by pengwu on 16/7/4.
 */
public enum  RetrofitClient {
    INSTANCE;
    private OkHttpClient client;
    private OkHttpClient.Builder builder;

    /**
     * 网络请求拦截，切换baserul的时候使用
     * */
    private DynamicBaseUrlInterceptor interceptor;
    private Retrofit retrofit;

    /**
     * 处理下载进度的网络拦截
     * */
    private FileDownInterceptor fileDownInterceptor;

    RetrofitClient(){
        interceptor = new DynamicBaseUrlInterceptor();
        fileDownInterceptor = new FileDownInterceptor();
        builder = ProgressHelper.addProgress(null);
        client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addNetworkInterceptor(fileDownInterceptor)
                .connectTimeout(60, TimeUnit.SECONDS)// 连接超时时间设置
                .readTimeout(20, TimeUnit.SECONDS)// 读取超时时间设置
                .retryOnConnectionFailure(true)// 失败重试
                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl(Common.BASE_URL_TEST)
                .addConverterFactory(FastjsonConverterFactory.create())
                .client(client)
                .build();
    }
    public Retrofit getRetrofit(){
        return  retrofit;
    }

    public FileDownInterceptor getFileDownInterceptor() {
        return fileDownInterceptor;
    }

}
