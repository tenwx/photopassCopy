package com.pictureair.photopass.util;


import com.alibaba.fastjson.JSONObject;

import java.io.File;

/**
 * Created by milo on 15/12/7.
 * 定义Http 操作回调接口
 */
public interface HttpInterface {
    /**
     * 请求成功，返回jsonObject
     *
     * @param jsonObject JSONObject
     */
    void onSuccess(JSONObject jsonObject);

    /**
     * 请求成功,返回字符
     *
     * @param result String
     */
    void onSuccess(String result);

    /**
     * 请求成功,返回byte
     *
     * @param binaryData
     */
    void onSuccess(byte[] binaryData);

    /**
     * 请求失败,返回状态码
     *
     * @param status int
     */
    void onFailure(int status);

    /**
     * 请求成功，返回文件
     *
     * @param file
     */
    void onSuccess(File file);

    /**
     * 上传进度
     *
     * @param bytesWritten long
     * @param totalSize    long
     */
    void onProgress(long bytesWritten, long totalSize);

    /**
     * 开始
     */
    void onStart();

}
