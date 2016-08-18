package com.pictureair.photopass.util;

import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.entity.BasicResult;
import com.pictureair.photopass.http.ApiFactory;
import com.pictureair.photopass.http.CallTask;
import com.pictureair.photopass.http.PhotoPassAuthApi;
import com.pictureair.photopass.http.retrofit_progress.ProgressListener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * Created by pengwu on 16/6/29.
 */
public class HttpUtil1 {
    private static final String TAG = "HttpUtil1";

    /**
     * 异步Get请求，无请求参数
     *
     * @param url       请求url
     * @param callback  请求回调
     * */
    public static CallTask asyncGet(final String url,final HttpCallback callback){

        PhotoPassAuthApi request = ApiFactory.INSTANCE.getPhotoPassAuthApi();


        CallTask task = new CallTask<BasicResult<JSONObject>>(request.get(url, new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                callback.onProgress(bytesRead,contentLength);
            }
        }));
        task.handleResponse(new ResponseCallback<BasicResult<JSONObject>>() {
            @Override
            public void onSuccess(BasicResult<JSONObject> result) {
                PictureAirLog.v(TAG, "get data from " + url + " finished");
                callback.onSuccess(result.getResult());
            }

            @Override
            public void onFailure(int code) {
                callback.onFailure(code);
            }
        });
        return task;
    }

    /**
     * 异步Get请求，有请求参数
     *
     * @param url       请求url
     * @param params    请求参数
     * @param callback  请求回调
     * */
    public static CallTask asyncGet(final String url,Map params,final HttpCallback callback){

        PhotoPassAuthApi request = ApiFactory.INSTANCE.getPhotoPassAuthApi();

        CallTask task = new CallTask<BasicResult<JSONObject>>(request.get(url, params, new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                callback.onProgress(bytesRead,contentLength);
            }
        }));
        task.handleResponse(new ResponseCallback<BasicResult<JSONObject>>() {
            @Override
            public void onSuccess(BasicResult<JSONObject> result) {
                PictureAirLog.v(TAG, "get data from " + url + " finished");
                callback.onSuccess(result.getResult());
            }

            @Override
            public void onFailure(int code) {
                callback.onFailure(code);
            }
        });
        return task;
    }

    /**
     * 异步post请求，无请求参数
     *
     * @param url       请求url
     * @param callback  请求回调
     * */
    public static CallTask asyncPost(final String url,final HttpCallback callback) {
        PhotoPassAuthApi request = ApiFactory.INSTANCE.getPhotoPassAuthApi();

        CallTask task = new CallTask<BasicResult<JSONObject>>(request.post(url, new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                callback.onProgress(bytesRead,contentLength);
            }
        }));
        task.handleResponse(new ResponseCallback<BasicResult<JSONObject>>() {
            @Override
            public void onSuccess(BasicResult<JSONObject> result) {
                PictureAirLog.v(TAG, "get data from " + url + " finished");
                callback.onSuccess(result.getResult());
            }

            @Override
            public void onFailure(int code) {
                callback.onFailure(code);
            }
        });
        return task;
    }

    /**
     * 异步post请求，有请求参数
     *
     * @param url       请求url
     * @param params    请求参数
     * @param callback  请求回调
     * */
    public static CallTask asyncPost(final String url,Map params,final HttpCallback callback) {
        PhotoPassAuthApi request = ApiFactory.INSTANCE.getPhotoPassAuthApi();

        CallTask task = new CallTask<BasicResult<JSONObject>>(request.post(url, params, new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                callback.onProgress(bytesRead,contentLength);
            }
        }));
        task.handleResponse(new ResponseCallback<BasicResult<JSONObject>>() {
            @Override
            public void onSuccess(BasicResult<JSONObject> result) {
                PictureAirLog.v(TAG, "get data from " + url + " finished");
                callback.onSuccess(result.getResult());
            }

            @Override
            public void onFailure(int code) {
                callback.onFailure(code);
            }
        });
        return task;
    }

    /**
     * 异步delete请求，有请求参数
     *
     * @param url       请求url
     * @param params    请求参数
     * @param callback  请求回调
     * */
    public static CallTask asyncDelete(final String url,Map params,final HttpCallback callback) {
        PhotoPassAuthApi request = ApiFactory.INSTANCE.getPhotoPassAuthApi();

        CallTask task = new CallTask<BasicResult<JSONObject>>(request.delete(url, params, new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                callback.onProgress(bytesRead,contentLength);
            }
        }));
        task.handleResponse(new ResponseCallback<BasicResult<JSONObject>>() {
            @Override
            public void onSuccess(BasicResult<JSONObject> result) {
                PictureAirLog.v(TAG, "get data from " + url + " finished");
                callback.onSuccess(result.getResult());
            }

            @Override
            public void onFailure(int code) {
                callback.onFailure(code);
            }
        });
        return task;
    }

    /**
     * 异步put请求，有请求参数
     *
     * @param url       请求url
     * @param params    请求参数
     * @param callback  请求回调
     * */
    public static CallTask asyncPut(final String url,Map params,final HttpCallback callback) {
        PhotoPassAuthApi request = ApiFactory.INSTANCE.getPhotoPassAuthApi();

        CallTask task = new CallTask<BasicResult<JSONObject>>(request.put(url, params, new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                callback.onProgress(bytesRead,contentLength);
            }
        }));
        task.handleResponse(new ResponseCallback<BasicResult<JSONObject>>() {
            @Override
            public void onSuccess(BasicResult<JSONObject> result) {
                PictureAirLog.v(TAG, "get data from " + url + " finished");
                callback.onSuccess(result.getResult());
            }

            @Override
            public void onFailure(int code) {
                callback.onFailure(code);
            }
        });
        return task;
    }


    /**
     * 异步put请求，有请求参数
     *
     * @param url       请求url
     * @param callback  请求回调
     * */
    public static CallTask asyncDownloadBinaryData(final String url,final HttpCallback callback) {
        PhotoPassAuthApi request = ApiFactory.INSTANCE.getPhotoPassAuthApi();

        CallTask task = new CallTask<ResponseBody>(request.download(url, new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                callback.onProgress(bytesRead,contentLength);
            }
        }));
        task.handleResponse(new ResponseCallback<ResponseBody>() {
            @Override
            public void onSuccess(ResponseBody result) {
                super.onSuccess(result);
                PictureAirLog.v(TAG, "get data from " + url + " finished");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buff = new byte[1024];
                int res = 0;
                try {
                    InputStream is = result.byteStream();
                    while ((res = is.read(buff))!= -1){
                        baos.write(buff,0,res);
                    }
                    byte[] bytes = baos.toByteArray();
                    is.close();
                    baos.close();
                    callback.onSuccess(bytes);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int code) {
                callback.onFailure(code);
            }
        });
        return task;
    }

    /**
     * 异步put请求，有请求参数
     *
     * @param url       请求url
     * @param params    请求参数
     * @param callback  请求回调
     * */
    public static CallTask asyncDownloadBinaryData(final String url,Map params,final HttpCallback callback) {
        PhotoPassAuthApi request = ApiFactory.INSTANCE.getPhotoPassAuthApi();

        CallTask task = new CallTask<ResponseBody>(request.download(url, params, new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                callback.onProgress(bytesRead,contentLength);
            }
        }));
        task.handleResponse(new ResponseCallback<ResponseBody>() {
            @Override
            public void onSuccess(ResponseBody result) {
                super.onSuccess(result);
                PictureAirLog.v(TAG, "get data from " + url + " finished");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buff = new byte[1024];
                int res = 0;
                try {
                    InputStream is = result.byteStream();
                    while ((res = is.read(buff))!= -1){
                        baos.write(buff,0,res);
                    }
                    byte[] bytes = baos.toByteArray();
                    is.close();
                    baos.close();
                    callback.onSuccess(bytes);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int code) {
                callback.onFailure(code);
            }
        });
        return task;
    }

    /**
     * 异步put请求，有请求参数
     *
     * @param url       请求url
     * @param params    请求参数
     * @param callback  请求回调
     * */
    public static CallTask asyncUpload(final String url, Map<String,RequestBody> params, final HttpCallback callback) {
        PhotoPassAuthApi request = ApiFactory.INSTANCE.getPhotoPassAuthApi();

        CallTask task = new CallTask<JSONObject>(request.upload(url, params, new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                callback.onProgress(bytesRead,contentLength);
            }
        }));
        task.handleResponse(new ResponseCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                super.onSuccess(result);
                PictureAirLog.v(TAG, "get data from " + url + " finished");
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(int code) {
                callback.onFailure(code);
            }
        });
        return task;
    }
}
