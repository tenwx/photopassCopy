package com.pictureair.photopass.util;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pictureair.photopass.entity.BaseJson;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cz.msebera.android.httpclient.Header;


/**
 * Created by milo on 15/12/5.
 * Http 帮助类 ：处理数据请求、文件上传、下载
 * 参考官网：https://loopj.com/android-async-http/
 */
public class HttpUtil1 extends HttpCallback {
    private static AsyncHttpClient asyncHttpClient;//异步处理网络请求
    private static ExecutorService threadPool;//线程重用，减少线程开销
    private static String BASE_URL;


    static {
        if (threadPool == null) {
            threadPool = Executors.newScheduledThreadPool(6);//线程池最大线程连接数.
        }
        if (asyncHttpClient == null) {
            asyncHttpClient = new AsyncHttpClient();
            asyncHttpClient.setMaxConnections(10);// 设置最大连接数
            asyncHttpClient.setMaxRetriesAndTimeout(2, 2 * 1000);//设置的重试次数、重试间隔
            asyncHttpClient.setTimeout(5 * 1000);//// 设置获取连接的最大等待时间
            asyncHttpClient.setConnectTimeout(10 * 1000);//设置连接超时时间
            asyncHttpClient.setResponseTimeout(20 * 1000);//设置响应超时时间
            asyncHttpClient.setThreadPool(threadPool);//设置线程池，方便线程管理，重用
        }

    }

    /**
     * 获取绝对路径
     *
     * @param relativeUrl 相对路径
     * @return 绝对路径
     */
    public static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;


    }

    /**
     * 设置BASE URL
     *
     * @param baseUrl 请求base url
     * @return
     */
    public static String setBaseUrl(String baseUrl) {
        BASE_URL = baseUrl;
        return baseUrl;
    }


    /**
     * 异步get请求
     *
     * @param url          请求url
     * @param httpCallback 请求回调
     */
    public static void asyncGet(final String url, final HttpCallback httpCallback) {
        asyncHttpClient.get(getAbsoluteUrl(url), new BaseJsonHttpResponseHandler<BaseJson>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, BaseJson baseJson) {
                // called when response HTTP status is "200 OK"
                if (baseJson == null) {
                    return;
                }
                if (baseJson.getStatus() == 200) {
                    httpCallback.onSuccess(baseJson.getResult());
                } else {
                    //失败返回错误码
                    httpCallback.onFailure(baseJson.getStatus());
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, BaseJson errorResponse) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                httpCallback.onFailure(statusCode);

            }

            @Override
            protected BaseJson parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                //解析响应
                return null;
            }
        });
    }

    /**
     * 异步get请求
     *
     * @param url          请求url
     * @param params       请求参数
     * @param httpCallback 请求回调
     */
    public static void asyncGet(final String url, RequestParams params, final HttpCallback httpCallback) {
        asyncHttpClient.get(getAbsoluteUrl(url), params, new BaseJsonHttpResponseHandler<BaseJson>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, BaseJson baseJson) {
                // called when response HTTP status is "200 OK"
                if (baseJson == null) {
                    return;
                }
                if (baseJson.getStatus() == 200) {

                    httpCallback.onSuccess(baseJson.getResult());
                } else {
                    //失败返回错误码
                    httpCallback.onFailure(baseJson.getStatus());
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, BaseJson errorResponse) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                httpCallback.onFailure(statusCode);

            }

            @Override
            protected BaseJson parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                //解析响应
                return null;
            }
        });
    }

    /**
     * 异步post请求
     *
     * @param url          请求url
     * @param httpCallback 请求回调
     */
    public static void asyncPost(final String url, final HttpCallback httpCallback) {
        asyncHttpClient.post(getAbsoluteUrl(url), new BaseJsonHttpResponseHandler<BaseJson>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, BaseJson baseJson) {
                // called when response HTTP status is "200 OK"
                //获取服务器返回内容,并解析.
                if (baseJson == null) {
                    return;
                }
                if (baseJson.getStatus() == 200) {
                    //成功,返回内容
                    httpCallback.onSuccess(baseJson.getResult());
                } else {
                    //失败返回错误码
                    httpCallback.onFailure(baseJson.getStatus());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, BaseJson errorResponse) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                httpCallback.onFailure(statusCode);

            }

            @Override
            protected BaseJson parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }


    /**
     * 异步post请求
     *
     * @param url          请求url
     * @param params       请求参数
     * @param httpCallback 请求回调
     */
    public static void asyncPost(final String url, RequestParams params, final HttpCallback httpCallback) {
        asyncHttpClient.post(getAbsoluteUrl(url), params, new BaseJsonHttpResponseHandler<BaseJson>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, BaseJson baseJson) {
                // called when response HTTP status is "200 OK"
                //获取服务器返回内容,并解析.
                if (baseJson == null) {
                    return;
                }
                if (baseJson.getStatus() == 200) {
                    //成功,返回内容
                    httpCallback.onSuccess(baseJson.getResult());
                } else {
                    //失败返回错误码
                    httpCallback.onFailure(baseJson.getStatus());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, BaseJson errorResponse) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                httpCallback.onFailure(statusCode);

            }

            @Override
            protected BaseJson parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }


    /**
     * 异步上传文件
     *
     * @param url           请求url
     * @param requestParams 文件(InputStream、File、byte array;具体参照官网)
     * @param context       上下文
     * @param httpCallback  请求回调
     */
    public static void asynUploadFile(String url, RequestParams requestParams, Context context, final HttpCallback httpCallback) {

        asyncHttpClient.post(getAbsoluteUrl(url), requestParams, new FileAsyncHttpResponseHandler(context) {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                //上传失败
                httpCallback.onFailure(statusCode);

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, File file) {
                //上传成功
                httpCallback.onSuccess(file);

            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                //上传进度
                httpCallback.onProgress(bytesWritten, totalSize);
            }

        });
    }


    /**
     * 异步下载文件
     *
     * @param url          请求url
     * @param context      上下文
     * @param httpCallback 请求回调
     */
    public static void asynDownloadFile(String url, Context context, final HttpCallback httpCallback) {
        asyncHttpClient.get(getAbsoluteUrl(url), new FileAsyncHttpResponseHandler(context) {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                httpCallback.onFailure(statusCode);

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, File file) {
                // Do something with the file
                httpCallback.onSuccess(file);

            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                httpCallback.onProgress(bytesWritten, totalSize);

            }
        });
    }


    /**
     * 异步下载文件
     *
     * @param url          请求url
     * @param context      上下文
     * @param params       请求参数
     * @param httpCallback 请求回调
     */
    public static void asynDownloadFile(String url, Context context, RequestParams params, final HttpCallback httpCallback) {
        asyncHttpClient.get(getAbsoluteUrl(url), params, new FileAsyncHttpResponseHandler(context) {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                httpCallback.onFailure(statusCode);

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, File file) {
                // Do something with the file
                httpCallback.onSuccess(file);

            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                httpCallback.onProgress(bytesWritten, totalSize);

            }
        });
    }

}