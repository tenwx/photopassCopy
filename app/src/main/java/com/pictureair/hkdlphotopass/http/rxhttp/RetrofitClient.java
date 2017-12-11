package com.pictureair.hkdlphotopass.http.rxhttp;



import com.pictureair.hkdlphotopass.http.fastjson.FastjsonConverterFactory;
import com.pictureair.hkdlphotopass.http.retrofit_progress.ProgressConverterFactory;
import com.pictureair.hkdlphotopass.http.retrofit_progress.ProgressInterceptor;
import com.pictureair.hkdlphotopass.http.retrofit_progress.ProgressListenerPool;
import com.pictureair.hkdlphotopass.util.Common;

import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;

/**
 *
 * 配置Retrofit
 * Created by pengwu on 16/7/4.
 */
public enum  RetrofitClient {
    INSTANCE;
    private OkHttpClient client;

    private Retrofit retrofit;
    private ProgressListenerPool pool;

    /**
     * 处理下载进度的网络拦截
     * */
    RetrofitClient(){
        pool = new ProgressListenerPool();
        client = new OkHttpClient.Builder()
                .addInterceptor(new ProgressInterceptor(pool))
                .connectTimeout(30, TimeUnit.SECONDS)// 连接超时时间设置
                .readTimeout(30, TimeUnit.SECONDS)// 读取超时时间设置
                .retryOnConnectionFailure(true)// 失败重试
                .connectionPool(new ConnectionPool(10, 30,TimeUnit.SECONDS))
                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl(Common.BASE_URL_TEST + "/")
                .addConverterFactory(FastjsonConverterFactory.create())
                .addConverterFactory(new ProgressConverterFactory(pool))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(client)
                .build();
    }
    public Retrofit getRetrofit(){
        return  retrofit;
    }

}
