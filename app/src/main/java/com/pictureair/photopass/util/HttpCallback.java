package com.pictureair.photopass.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.File;

/**
 * Created by pengwu on 16/8/5.
 */
public abstract class HttpCallback implements HttpInterface {

    @Override
    public void onSuccess(JSONObject jsonObject){

    }

    @Override
    public void onSuccess(JSONArray jsonArray){

    }

    @Override
    public void onSuccess(File file){

    }

    @Override
    public void onSuccess(String result){

    }
    @Override
    public void onSuccess(byte[] binaryData){

    }

    @Override
    public void onFailure(int status){

    }

    @Override
    public void onProgress(long bytesWritten, long totalSize) {

    }
}
