package com.pictureair.photopass.util;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pictureair.photopass.entity.HttpBaseJson;

import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cz.msebera.android.httpclient.Header;


/**
 * Created by milo on 15/12/5.
 * Http 帮助类 ：处理数据请求、文件上传、下载
 * 参考官网：https://loopj.com/android-async-http/
 */
public class HttpUtil1{
    private static AsyncHttpClient asyncHttpClient;//异步处理网络请求
    private static ExecutorService threadPool;//线程重用，减少线程开销
    private static String BASE_URL = "http://192.168.8.3:3006";
    private static final String TAG = "HttpUtil1";


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
     * @return 返回baseURL
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

        asyncHttpClient.get(url, new BaseJsonHttpResponseHandler<HttpBaseJson>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, HttpBaseJson httpBaseJson) {
httpCallback.onStart();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, HttpBaseJson errorResponse) {

            }

            @Override
            protected HttpBaseJson parseResponse(String rawJsonData, boolean isFailure) throws Throwable {

                return JsonTools.parseObject(rawJsonData);
            }
        });


        asyncHttpClient.get(getAbsoluteUrl(url), new BaseJsonHttpResponseHandler<HttpBaseJson>() {

            @Override
            public void onStart() {
                super.onStart();
                httpCallback.onStart();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, HttpBaseJson response) {
                HttpBaseJson httpBaseJson = JsonTools.parseObject(rawJsonResponse);
                // called when response HTTP status is "200 OK"
                if (httpBaseJson != null) {
                    if (httpBaseJson.getStatus() == 200) {
                        httpCallback.onSuccess(httpBaseJson.getResult().toString());
                        httpCallback.onSuccess((JSONObject) httpBaseJson.getResult());
                    } else {
                        //失败返回错误码
                        httpCallback.onFailure(httpBaseJson.getStatus());
                    }
                }


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, HttpBaseJson errorResponse) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                httpCallback.onFailure(statusCode);

            }

            @Override
            protected HttpBaseJson parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                //必须解析rawJsonData并返回。不然onSuccess 接收到的是null
                return JsonTools.parseObject(rawJsonData);
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
        asyncHttpClient.get(getAbsoluteUrl(url), params, new BaseJsonHttpResponseHandler<HttpBaseJson>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, HttpBaseJson response) {
                HttpBaseJson httpBaseJson = JsonTools.parseObject(rawJsonResponse);
                // called when response HTTP status is "200 OK"
                if (httpBaseJson == null) {
                    if (httpBaseJson.getStatus() == 200) {
                        httpCallback.onSuccess(httpBaseJson.getResult().toString());
                        httpCallback.onSuccess((JSONObject) httpBaseJson.getResult());
                    } else {
                        //失败返回错误码
                        httpCallback.onFailure(httpBaseJson.getStatus());
                    }
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, HttpBaseJson errorResponse) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                httpCallback.onFailure(statusCode);

            }

            @Override
            protected HttpBaseJson parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                //必须解析rawJsonData并返回。不然onSuccess 接收到的是null
                return JsonTools.parseObject(rawJsonData);
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
        asyncHttpClient.post(getAbsoluteUrl(url), new BaseJsonHttpResponseHandler<HttpBaseJson>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, HttpBaseJson httpBaseJson) {
                // called when response HTTP status is "200 OK"
                //获取服务器返回内容,并解析.
                if (httpBaseJson == null) {
                    if (httpBaseJson.getStatus() == 200) {
                        //成功,返回内容
                        httpCallback.onSuccess(httpBaseJson.getResult().toString());
                        httpCallback.onSuccess((JSONObject) httpBaseJson.getResult());
                    } else {
                        //失败返回错误码
                        httpCallback.onFailure(httpBaseJson.getStatus());
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, HttpBaseJson errorResponse) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                httpCallback.onFailure(statusCode);

            }

            @Override
            protected HttpBaseJson parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                //必须解析rawJsonData并返回。不然onSuccess 接收到的是null
                return JsonTools.parseObject(rawJsonData);
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
        PictureAirLog.v(TAG, "asyncPost" + "url: " + getAbsoluteUrl(url));
        asyncHttpClient.post(getAbsoluteUrl(url), params, new BaseJsonHttpResponseHandler<HttpBaseJson>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, HttpBaseJson httpBaseJson) {
                PictureAirLog.v(TAG, "onSuccess");
                if (httpBaseJson != null) {
                    PictureAirLog.v(TAG, "parseResponse baseJson: " + httpBaseJson.getStatus() + "---" + httpBaseJson.getMsg() + "---" + httpBaseJson.getResult());
                    if (httpBaseJson.getStatus() == 200) {
                        //成功,返回内容
                        httpCallback.onSuccess(httpBaseJson.getResult().toString());
                        httpCallback.onSuccess((JSONObject) httpBaseJson.getResult());
                    } else {
                        //失败返回错误码
                        httpCallback.onFailure(httpBaseJson.getStatus());
                    }
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, HttpBaseJson errorResponse) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                PictureAirLog.v(TAG, "onFailure statusCode: " + statusCode);
            }

            @Override
            protected HttpBaseJson parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                PictureAirLog.v(TAG, "parseResponse rawJsonData: " + rawJsonData);
                //必须解析rawJsonData并返回。不然onSuccess 接收到的是null
                return JsonTools.parseObject(rawJsonData);
            }
        });
    }


    /**
     * 异步上传文件
     *
     * @param url           请求url
     * @param requestParams 文件(InputStream、File、byte array;具体参照官网)
     * @param httpCallback  请求回调
     */
    public static void asynUploadFile(String url, RequestParams requestParams, final HttpCallback httpCallback) {
        asyncHttpClient.post(getAbsoluteUrl(url), requestParams, new BaseJsonHttpResponseHandler<HttpBaseJson>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, HttpBaseJson httpBaseJson) {
                //上传成功
                if (httpBaseJson != null) {
                    if (httpBaseJson.getStatus() == 200) {
                        //成功,返回内容
                        httpCallback.onSuccess(httpBaseJson.getResult().toString());
                        httpCallback.onSuccess((JSONObject) httpBaseJson.getResult());
                    } else {
                        //失败返回错误码
                        httpCallback.onFailure(httpBaseJson.getStatus());
                    }
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, HttpBaseJson errorResponse) {
                //上传失败
                httpCallback.onFailure(statusCode);

            }

            @Override
            protected HttpBaseJson parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return JsonTools.parseObject(rawJsonData);
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
     * @param httpCallback 请求回调 - file
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

        asyncHttpClient.post(url, params, new BinaryHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] binaryData) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error) {

            }
        });
    }


    /**
     * 异步下载文件
     *
     * @param url          请求url
     * @param httpCallback 请求回调 - byte
     */
    public static void asynDownloadBinaryData(String url, final HttpCallback httpCallback) {
        asyncHttpClient.get(url, new BinaryHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] binaryData) {
                httpCallback.onSuccess(binaryData);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error) {
                httpCallback.onFailure(statusCode);
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
     * @param params       请求参数
     * @param httpCallback 请求回调 - byte
     */
    public static void asynDownloadBinaryData(String url, RequestParams params, final HttpCallback httpCallback) {
        asyncHttpClient.post(url, params, new BinaryHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] binaryData) {
                httpCallback.onSuccess(binaryData);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error) {
                httpCallback.onFailure(statusCode);
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                httpCallback.onProgress(bytesWritten, totalSize);

            }
        });
    }



}
