package com.pictureair.photopass.entity;

import android.graphics.Bitmap;

import com.google.zxing.common.Comparator;

/**
 * Created by pengwu on 16/7/8.
 */
public class PhotoDownLoadInfo{
    private String photoId;
    private String size;
    private String shootTime;
    private String loadTime;
    private String url;
    private String previewUrl;
    private int isVideo;
    private String failedTime = "";
    private String status;
    public boolean isSelect;//表示是否被勾选
    public int selectPos;
    private int id;

    public PhotoDownLoadInfo(){

    }

    public PhotoDownLoadInfo(String photoId, String size, String shootTime, String loadTime, String url, String previewUrl,int isVideo,String failedTime,String status) {
        this.photoId = photoId;
        this.size = size;
        this.shootTime = shootTime;
        this.loadTime = loadTime;
        this.url = url;
        this.previewUrl = previewUrl;
        this.isVideo = isVideo;
        this.failedTime = failedTime;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getIsVideo() {
        return isVideo;
    }

    public void setIsVideo(int isVideo) {
        this.isVideo = isVideo;
    }

    public String getFailedTime() {
        return failedTime;
    }

    public void setFailedTime(String failedTime) {
        this.failedTime = failedTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
