package com.pictureair.photopass.entity;

/**
 * 首页一天PP卡的封装类
 * Created by bauer_bao on 16/12/12.
 */
public class DailyPPCardInfo {
    private int sectionId;
    private String ppCode;
    private String shootDate;//年月日
    private String shootOn;//年月日时分秒
    private int activated;
    private int photoCount;
    private PhotoInfo albumCoverPhoto;//最旧的一个对象，作为封面
    private PhotoInfo locationPhoto;//作为每个地点的显示图片

    public DailyPPCardInfo() {
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public String getPpCode() {
        return ppCode;
    }

    public void setPpCode(String ppCode) {
        this.ppCode = ppCode;
    }

    public String getShootDate() {
        return shootDate;
    }

    public void setShootDate(String shootDate) {
        this.shootDate = shootDate;
    }

    public String getShootOn() {
        return shootOn;
    }

    public void setShootOn(String shootOn) {
        this.shootOn = shootOn;
    }

    public int getActivated() {
        return activated;
    }

    public void setActivated(int activated) {
        this.activated = activated;
    }

    public PhotoInfo getAlbumCoverPhoto() {
        return albumCoverPhoto;
    }

    public void setAlbumCoverPhoto(PhotoInfo albumCoverPhoto) {
        this.albumCoverPhoto = albumCoverPhoto;
    }

    public PhotoInfo getLocationPhoto() {
        return locationPhoto;
    }

    public void setLocationPhoto(PhotoInfo locationPhoto) {
        this.locationPhoto = locationPhoto;
    }

    public int getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(int photoCount) {
        this.photoCount = photoCount;
    }
}
