package com.pictureair.photopass.http;



import com.pictureair.photopass.util.Common;

import java.util.concurrent.TimeUnit;

import converter.fastjson.FastjsonConverterFactory;
import okhttp3.OkHttpClient;
import pl.gumyns.retrofit_progress.ProgressConverterFactory;
import pl.gumyns.retrofit_progress.ProgressInterceptor;
import pl.gumyns.retrofit_progress.ProgressListenerPool;
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
    private ProgressListenerPool pool;

    /**
     * 处理下载进度的网络拦截
     * */
//    private FileDownInterceptor fileDownInterceptor;

    RetrofitClient(){
//        interceptor = new DynamicBaseUrlInterceptor();
//        fileDownInterceptor = new FileDownInterceptor();
//        builder = ProgressHelper.addProgress(null);
        pool = new ProgressListenerPool();
        client = new OkHttpClient.Builder()
//                .addInterceptor(interceptor)
//                .addNetworkInterceptor(fileDownInterceptor)
                .addInterceptor(new ProgressInterceptor(pool))
                .connectTimeout(60, TimeUnit.SECONDS)// 连接超时时间设置
                .readTimeout(20, TimeUnit.SECONDS)// 读取超时时间设置
                .retryOnConnectionFailure(true)// 失败重试
                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl(Common.BASE_URL_TEST)
                .addConverterFactory(FastjsonConverterFactory.create())
                .addConverterFactory(new ProgressConverterFactory(pool))
                .client(client)
                .build();
    }
    public Retrofit getRetrofit(){
        return  retrofit;
    }

//    public FileDownInterceptor getFileDownInterceptor() {
//        return fileDownInterceptor;
//    }

}
