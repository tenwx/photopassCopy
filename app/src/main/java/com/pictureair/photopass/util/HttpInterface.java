package com.pictureair.photopass.util;

import org.json.JSONObject;

import java.io.File;

/**
 * Created by milo on 15/12/7.
 * 定义Http 操作回调接口
 */
public interface HttpInterface {
    void onSuccess(JSONObject jsonObject);//请求成功，返回jsonObject
    void onSuccess(String result);//请求成功,返回字符

    void onFailure(int status);//请求失败,返回状态码

    void onSuccess(File file);//请求成功，返回文件

    void onProgress(long bytesWritten, long totalSize);//上传进度

}
