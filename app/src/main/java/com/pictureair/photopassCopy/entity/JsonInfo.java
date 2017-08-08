package com.pictureair.photopassCopy.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 整个json字段存储
 * Created by bauer_bao on 16/12/12.
 */
@Entity
public class JsonInfo {
    /**
     * 一卡一天的数据存储
     */
    public static final String JSON_LOCATION_PHOTO_TYPE = "location_photo_type";

    /**
     * 一卡一天内页是否需要全部刷新的标记，由卡号和时间组成：code,shootdate，如果有，说明需要全部刷新，如果没有说明不需要全部刷新
     */
    public static final String DAILY_PP_REFRESH_ALL_TYPE = "daily_pp_refresh_all_type";
    @Id
    private Long id;
    /**
     * 存储数据库的类型
     */
    private String jsonType;
    /**
     * 存储的json数据
     */
    private String jsonString;
    @Generated(hash = 1753990713)
    public JsonInfo(Long id, String jsonType, String jsonString) {
        this.id = id;
        this.jsonType = jsonType;
        this.jsonString = jsonString;
    }
    @Generated(hash = 949567778)
    public JsonInfo() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getJsonType() {
        return this.jsonType;
    }
    public void setJsonType(String jsonType) {
        this.jsonType = jsonType;
    }
    public String getJsonString() {
        return this.jsonString;
    }
    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }

    /**
     * 获取一卡一天的数据结构
     * @param ppCode
     * @param shootDate
     * @return
     */
    public static String getNeedRefreshString(String ppCode, String shootDate) {
        return ppCode + "," + shootDate;
    }

    /**
     * 是否刷新的字符串拼接
     * @param jsonStr
     * @param needRefresh
     * @return
     */
    public static String updateRefreshStr(String jsonStr, boolean needRefresh) {
        return jsonStr + (needRefresh ? ",unRefreshed" : ",Refreshed");//unRefreshed：需要刷新 : Refreshed：已经刷新过
    }
}
