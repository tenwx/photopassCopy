package com.pictureair.photopass.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 整个json字段存储
 * Created by bauer_bao on 16/12/12.
 */
@Entity
public class JsonInfo {
    public static final String JSON_LOCATION_PHOTO_TYPE = "location_photo_type";
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
}
