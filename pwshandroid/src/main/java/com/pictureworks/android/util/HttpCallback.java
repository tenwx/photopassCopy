package com.pictureworks.android.util;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.File;

/**
 * Created by milo on 15/12/7.
 * 创建Http请求辅助类
 * 注意：这里必须实现接口里面的所有方法，不然后面调用就会自动实现接口里面没有实现的方法。
 */
public abstract class HttpCallback implements HttpInterface{
    //实现所有方法，方便后面任意调用其中某一个


    @Override
    public void onSuccess(JSONArray jsonArray) {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onSuccess(JSONObject jsonObject) {

    }

    @Override
    public void onSuccess(String result) {

    }

    @Override
    public void onSuccess(byte[] binaryData) {

    }

    @Override
    public void onSuccess(File file) {

    }

    @Override
    public void onFailure(int status) {

    }

    @Override
    public void onProgress(long bytesWritten, long totalSize) {

    }
}
