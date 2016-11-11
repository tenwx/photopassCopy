package com.pictureair.photopass.http.rxhttp;


import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.entity.BasicResult;
import com.pictureair.photopass.http.retrofit_progress.ProgressListener;
import com.pictureair.photopass.http.retrofit_progress.annotation.DownloadProgress;
import com.pictureair.photopass.http.retrofit_progress.annotation.UploadProgress;

import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.PartMap;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;

/**
 *
 * API接口，定义请求函数和规则
 *
 * Created by pengwu on 16/7/4.
 */
public interface PhotoPassAuthApi {

    /**
     * 异步get请求，无请求参数
     *
     * @param  url 请求url 用@Url标注 与 @GET("auth/{path}")标注有区别
     * 如果使用 @GET("auth/path") 传进的路径 “/”符号会被编码成一个其他字符，@Url 标注则不会，
     * 并且如果传入的是一个完整的路径比如http://192.168.8.3:4000/media/b1b983e5cd2cfafefcb948b6bd9829746a6b4b1f539bd6077b80630377baf922764175e6657819a8758537870f3c263e
     * 则不会和retrofit初始化的baseUrl拼接。
     *  @GET("auth/path")方法使用不方便
     * */
    @GET
    Observable<BasicResult<JSONObject>> get(@Url String url);

    /**
     * 异步get请求，有请求参数
     *
     * @param  url 请求url
     * @param params 请求参数 hashmap键值对
     * */
    @GET
    Observable<BasicResult<JSONObject>> get(@Url String url, @QueryMap Map<String, Object> params);


    /**
     * 异步post请求，无请求参数
     *
     * @param  url 请求url
     * */
    @POST
    Observable<BasicResult<JSONObject>> post(@Url String url);

    /**
     * 异步post请求，有请求参数
     *
     * @param  url 请求url
     * @param params 请求参数 hashmap键值对
     * */
    @FormUrlEncoded
    @POST
    Observable<BasicResult<JSONObject>> post(@Url String url, @FieldMap Map<String, Object> params);

    /**
     * 异步delete请求，有请求参数
     *
     * @param  url 请求url
     * @param params 请求参数 hashmap键值对
     * */
    @DELETE
    Observable<BasicResult<JSONObject>> delete(@Url String url, @QueryMap Map<String, Object> params);

    /**
     * 异步put请求，有请求参数
     *
     * @param  url 请求url
     * @param params 请求参数 hashmap键值对
     * */
    @FormUrlEncoded
    @PUT
    Observable<BasicResult<JSONObject>> put(@Url String url, @FieldMap Map<String, Object> params);


    /**
     * 异步get请求，无请求参数
     *
     * @param  url 请求url
     * */
    @Streaming
    @GET
    Observable<ResponseBody> download(@Url String url);

    /**
     * 异步get请求，有请求参数
     *
     * @param  url 请求url
     * @param params 请求参数 hashmap键值对
     * */
    @Streaming
    @GET
    Observable<ResponseBody> download(@Url String url, @QueryMap Map<String, Object> params);

    @Multipart
    @POST
    Observable<BasicResult<JSONObject>> upload(@Url String url, @PartMap Map<String, RequestBody> params);
}
