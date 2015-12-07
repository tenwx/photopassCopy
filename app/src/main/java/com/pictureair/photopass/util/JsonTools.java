package com.pictureair.photopass.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.entity.BaseJson;

/**
 * Created by milo on 15/12/7.
 * 封装json处理类，方便日后维护
 */
public class JsonTools {

    /**
     * 字符串转对象
     *
     * @param jsonStr 字符串
     * @return
     */
    public static BaseJson parseObject(String jsonStr) {
        return JSON.parseObject(jsonStr, BaseJson.class);
    }

    public static BaseJson parseObject(org.json.JSONObject jsonObject) {
        return JSON.parseObject(jsonObject.toString(), BaseJson.class);
    }


    public static <T> T parseObject(JSONObject jsonObject, Class<T> cls) {
        return JSON.parseObject(jsonObject.toString(), cls);
    }


    public static JSONObject parseObject(byte[] bytes) {
        return JSON.parseObject(new String(bytes));
    }




}
