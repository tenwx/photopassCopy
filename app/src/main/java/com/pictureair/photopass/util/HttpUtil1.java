package com.pictureair.photopass.util;

import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.http.ApiFactory;
import com.pictureair.photopass.http.BasicResultCallTask;
import com.pictureair.photopass.http.BinaryCallBack;
import com.pictureair.photopass.http.CallTaskManager;
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
    public static BasicResultCallTask asyncGet(final String url, final HttpCallback callback){

        PhotoPassAuthApi request = ApiFactory.INSTANCE.getPhotoPassAuthApi();


        BasicResultCallTask task = new BasicResultCallTask<JSONObject>(request.get(url, new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                callback.onProgress(bytesRead,contentLength);
            }
        }));
        task.handleResponse(new ResponseCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
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

    /**
     * 异步Get请求，有请求参数
     *
     * @param url       请求url
     * @param params    请求参数
     * @param callback  请求回调
     * */
    public static BasicResultCallTask asyncGet(final String url, Map params, final HttpCallback callback){

        PhotoPassAuthApi request = ApiFactory.INSTANCE.getPhotoPassAuthApi();

        BasicResultCallTask task = new BasicResultCallTask<JSONObject>(request.get(url, params, new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                callback.onProgress(bytesRead,contentLength);
            }
        }));
        task.handleResponse(new ResponseCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
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

    /**
     * 异步post请求，无请求参数
     *
     * @param url       请求url
     * @param callback  请求回调
     * */
    public static BasicResultCallTask asyncPost(final String url, final HttpCallback callback) {
        PhotoPassAuthApi request = ApiFactory.INSTANCE.getPhotoPassAuthApi();

        BasicResultCallTask task = new BasicResultCallTask<JSONObject>(request.post(url, new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                callback.onProgress(bytesRead,contentLength);
            }
        }));
        task.handleResponse(new ResponseCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
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

    /**
     * 异步post请求，有请求参数
     *
     * @param url       请求url
     * @param params    请求参数
     * @param callback  请求回调
     * */
    public static BasicResultCallTask asyncPost(final String url, Map params, final HttpCallback callback) {
        PhotoPassAuthApi request = ApiFactory.INSTANCE.getPhotoPassAuthApi();

        BasicResultCallTask task = new BasicResultCallTask<JSONObject>(request.post(url, params, new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                callback.onProgress(bytesRead,contentLength);
            }
        }));
        task.handleResponse(new ResponseCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
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

    /**
     * 异步delete请求，有请求参数
     *
     * @param url       请求url
     * @param params    请求参数
     * @param callback  请求回调
     * */
    public static BasicResultCallTask asyncDelete(final String url, Map params, final HttpCallback callback) {
        PhotoPassAuthApi request = ApiFactory.INSTANCE.getPhotoPassAuthApi();

        BasicResultCallTask task = new BasicResultCallTask<JSONObject>(request.delete(url, params, new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                callback.onProgress(bytesRead,contentLength);
            }
        }));
        task.handleResponse(new ResponseCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
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

    /**
     * 异步put请求，有请求参数
     *
     * @param url       请求url
     * @param params    请求参数
     * @param callback  请求回调
     * */
    public static BasicResultCallTask asyncPut(final String url, Map params, final HttpCallback callback) {
        PhotoPassAuthApi request = ApiFactory.INSTANCE.getPhotoPassAuthApi();

        BasicResultCallTask task = new BasicResultCallTask<JSONObject>(request.put(url, params, new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                callback.onProgress(bytesRead,contentLength);
            }
        }));
        task.handleResponse(new ResponseCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
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


    /**
     * 异步put请求，有请求参数
     *
     * @param url       请求url
     * @param callback  请求回调
     * */
    public static BinaryCallBack asyncDownloadBinaryData(final String url, final HttpCallback callback) {
        PhotoPassAuthApi request = ApiFactory.INSTANCE.getPhotoPassAuthApi();

        final BinaryCallBack task = new BinaryCallBack<ResponseBody>(request.download(url, new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                callback.onProgress(bytesRead,contentLength);
            }
        }));
        task.handleResponse(new ResponseCallback<ResponseBody>() {
            @Override
            public void onSuccess(ResponseBody result) {
                super.onSuccess(result);
                final ResponseBody body = result;
                new Thread() {
                    @Override
                    public void run() {
                        PictureAirLog.v(TAG, "get data from " + url + " finished");
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buff = new byte[1024];
                        int res = 0;
                        try {
                            InputStream is = body.byteStream();
                            while ((res = is.read(buff)) != -1) {
                                baos.write(buff, 0, res);
                            }
                            byte[] bytes = baos.toByteArray();
                            is.close();
                            baos.close();
                            callback.onSuccess(bytes);
                        } catch (Exception e) {
                            e.printStackTrace();
                            onFailure(401);
                        }finally {
                            if (CallTaskManager.getInstance().containsTask(task)){
                                CallTaskManager.getInstance().removeTask(task);
                                PictureAirLog.e("asyncDownloadBinaryData", "remove "+ task.toString());
                            }
                        }
                    }
                }.start();
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
    public static BinaryCallBack asyncDownloadBinaryData(final String url, Map params, final HttpCallback callback) {
        PhotoPassAuthApi request = ApiFactory.INSTANCE.getPhotoPassAuthApi();

        final BinaryCallBack task = new BinaryCallBack<ResponseBody>(request.download(url, params, new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                callback.onProgress(bytesRead,contentLength);
            }
        }));
        task.handleResponse(new ResponseCallback<ResponseBody>() {
            @Override
            public void onSuccess(ResponseBody result) {
                super.onSuccess(result);
                final ResponseBody body = result;
                   new Thread(){
                       @Override
                       public void run() {
                           PictureAirLog.v(TAG, "get data from " + url + " finished");
                           ByteArrayOutputStream baos = new ByteArrayOutputStream();
                           byte[] buff = new byte[1024];
                           int res = 0;
                           try {
                               InputStream is = body.byteStream();
                               while ((res = is.read(buff))!= -1){
                                   baos.write(buff,0,res);
                               }
                               byte[] bytes = baos.toByteArray();
                               is.close();
                               baos.close();
                               callback.onSuccess(bytes);
                           } catch (Exception e) {
                               e.printStackTrace();
                               onFailure(401);
                           }finally {
                               if (CallTaskManager.getInstance().containsTask(task)){
                                   CallTaskManager.getInstance().removeTask(task);
                               }
                           }
                       }
                   }.start();
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
    public static BasicResultCallTask asyncUpload(final String url, Map<String,RequestBody> params, final HttpCallback callback) {
        PhotoPassAuthApi request = ApiFactory.INSTANCE.getPhotoPassAuthApi();

        BasicResultCallTask task = new BasicResultCallTask<JSONObject>(request.upload(url, params, new ProgressListener() {
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
