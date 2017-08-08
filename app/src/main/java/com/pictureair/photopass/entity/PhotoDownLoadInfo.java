package com.pictureair.photopass.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by pengwu on 16/7/8.
 */

@Entity
public class PhotoDownLoadInfo {
    @Id
    private Long id;//自增长id
    private String photoId;
    private String url;
    private long size;
    private String previewUrl;
    private String shootTime;
    private String downLoadTime;
    private String failedTime = "";
    private String userId;
    private String status;
    private String photoThumbnail_512;
    private String photoThumbnail_1024;
    private int videoWidth;
    private int videoHeight;
    private int isVideo;
    private long readLength;//断点下载使用字段，表示当前下载的点

    @Transient
    private boolean isSelect;//表示是否被勾选
    @Transient
    private int selectPos;

    @Generated(hash = 430359003)
    public PhotoDownLoadInfo(Long id, String photoId, String url, long size, String previewUrl,
            String shootTime, String downLoadTime, String failedTime, String userId, String status,
            String photoThumbnail_512, String photoThumbnail_1024, int videoWidth, int videoHeight,
            int isVideo, long readLength) {
        this.id = id;
        this.photoId = photoId;
        this.url = url;
        this.size = size;
        this.previewUrl = previewUrl;
        this.shootTime = shootTime;
        this.downLoadTime = downLoadTime;
        this.failedTime = failedTime;
        this.userId = userId;
        this.status = status;
        this.photoThumbnail_512 = photoThumbnail_512;
        this.photoThumbnail_1024 = photoThumbnail_1024;
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        this.isVideo = isVideo;
        this.readLength = readLength;
    }

    @Generated(hash = 73030779)
    public PhotoDownLoadInfo() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhotoId() {
        return this.photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPreviewUrl() {
        return this.previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getShootTime() {
        return this.shootTime;
    }

    public void setShootTime(String shootTime) {
        this.shootTime = shootTime;
    }

    public String getDownLoadTime() {
        return this.downLoadTime;
    }

    public void setDownLoadTime(String downLoadTime) {
        this.downLoadTime = downLoadTime;
    }

    public String getFailedTime() {
        return this.failedTime;
    }

    public void setFailedTime(String failedTime) {
        this.failedTime = failedTime;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhotoThumbnail_512() {
        return this.photoThumbnail_512;
    }

    public void setPhotoThumbnail_512(String photoThumbnail_512) {
        this.photoThumbnail_512 = photoThumbnail_512;
    }

    public String getPhotoThumbnail_1024() {
        return this.photoThumbnail_1024;
    }

    public void setPhotoThumbnail_1024(String photoThumbnail_1024) {
        this.photoThumbnail_1024 = photoThumbnail_1024;
    }

    public int getVideoWidth() {
        return this.videoWidth;
    }

    public void setVideoWidth(int videoWidth) {
        this.videoWidth = videoWidth;
    }

    public int getVideoHeight() {
        return this.videoHeight;
    }

    public void setVideoHeight(int videoHeight) {
        this.videoHeight = videoHeight;
    }

    public int getIsVideo() {
        return this.isVideo;
    }

    public void setIsVideo(int isVideo) {
        this.isVideo = isVideo;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public int getSelectPos() {
        return selectPos;
    }

    public void setSelectPos(int selectPos) {
        this.selectPos = selectPos;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getSize() {
        return size;
    }

    public long getReadLength() {
        return readLength;
    }

    public void setReadLength(long readLength) {
        this.readLength = readLength;
    }
}
