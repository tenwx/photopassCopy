package com.pictureair.photopass.entity;

import android.graphics.Bitmap;

/**
 * Created by pengwu on 16/7/8.
 */
public class PhotoDownLoadInfo {
    private String photoId;
    private String size;
    private String shootTime;
    private String loadTime;
    private String url;
    private String previewUrl;

    public PhotoDownLoadInfo(){

    }

    public PhotoDownLoadInfo(String photoId, String size, String shootTime, String loadTime, String url, String previewUrl) {
        this.photoId = photoId;
        this.size = size;
        this.shootTime = shootTime;
        this.loadTime = loadTime;
        this.url = url;
        this.previewUrl = previewUrl;
    }

    public String getPhotoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getShootTime() {
        return shootTime;
    }

    public void setShootTime(String shootTime) {
        this.shootTime = shootTime;
    }

    public String getLoadTime() {
        return loadTime;
    }

    public void setLoadTime(String loadTime) {
        this.loadTime = loadTime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }
}
