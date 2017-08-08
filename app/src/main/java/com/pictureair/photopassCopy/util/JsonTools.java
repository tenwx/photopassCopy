package com.pictureair.photopassCopy.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopassCopy.entity.HttpBaseJson;

import java.util.List;

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
    public static HttpBaseJson parseObject(String jsonStr) {
        return JSON.parseObject(jsonStr, HttpBaseJson.class);
    }

    public static HttpBaseJson parseObject(org.json.JSONObject jsonObject) {
        return JSON.parseObject(jsonObject.toString(), HttpBaseJson.class);
    }

    public static <T> T parseObject(String str, Class<T> cls) {
        return JSON.parseObject(str, cls);
    }

    public static <T> T parseObject(JSONObject jsonObject, Class<T> cls) {
        return JSON.parseObject(jsonObject.toString(), cls);
    }

    public static <T> List<T> parseArray(String str, Class<T> cls) {
        return JSON.parseArray(str, cls);
    }

    public static JSONObject parseObject(byte[] bytes) {
        return JSON.parseObject(new String(bytes));
    }

}
