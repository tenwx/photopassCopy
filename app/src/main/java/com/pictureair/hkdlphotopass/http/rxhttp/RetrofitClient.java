package com.pictureair.hkdlphotopass.http.rxhttp;



import com.pictureair.hkdlphotopass.http.fastjson.FastjsonConverterFactory;
import com.pictureair.hkdlphotopass.http.retrofit_progress.ProgressConverterFactory;
import com.pictureair.hkdlphotopass.http.retrofit_progress.ProgressInterceptor;
import com.pictureair.hkdlphotopass.http.retrofit_progress.ProgressListenerPool;
import com.pictureair.hkdlphotopass.util.Common;
import com.pictureair.hkdlphotopass.util.DelegatingSSLSocketFactory;

import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
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
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(new ProgressInterceptor(pool))
                .connectTimeout(30, TimeUnit.SECONDS)// 连接超时时间设置
                .readTimeout(30, TimeUnit.SECONDS)// 读取超时时间设置
                .retryOnConnectionFailure(true)// 失败重试
                .connectionPool(new ConnectionPool(10, 30,TimeUnit.SECONDS));
        try {
            // 自定义一个信任所有证书的TrustManager，添加SSLSocketFactory的时候要用到
            final X509TrustManager trustAllCert =
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    };
//            SSLContext sslContext = SSLContext.getInstance("SSL");
//            sslContext.init(null, new TrustManager[] {trustAllCert}, new SecureRandom());
//            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            final SSLSocketFactory sslSocketFactory = new DelegatingSSLSocketFactory(trustAllCert);
            builder.sslSocketFactory(sslSocketFactory, trustAllCert);//添加tlsv1.2的支持
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        client = builder.build();

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

    public OkHttpClient getClient() {
        return client;
    }
}
