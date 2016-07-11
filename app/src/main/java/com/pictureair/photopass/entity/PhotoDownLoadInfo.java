package com.pictureair.photopass.entity;

import android.graphics.Bitmap;

/**
 * Created by pengwu on 16/7/8.
 */
public class PhotoDownLoadInfo {
    private String photoId;
    private String name;
    private String size;
    private String date;
    private String time;
    private String url;

    public PhotoDownLoadInfo(){

    }

    public PhotoDownLoadInfo(String photoId, String name, String size, String date, String time,String url) {
        this.photoId = photoId;
        this.name = name;
        this.size = size;
        this.date = date;
        this.time = time;
        this.url = url;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPhotoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
