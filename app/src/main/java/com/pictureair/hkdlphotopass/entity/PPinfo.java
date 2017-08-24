package com.pictureair.hkdlphotopass.entity;

import java.util.List;

public class PPinfo {
    private String ppCode;//照片对应pp号码
    private int photoCount;//照片数量
    private String shootDate;//拍摄日期
    private int visiblePhotoCount;//已经加载过的本地照片数量
    private int isHidden;//是否已经隐藏（0否，1是）
    private String location;//图片位置
    private List<PhotoInfo> selectPhotoItemInfos;//用于点进去看图片详情
    private int isSelected;//0，未选中，1，选中，2，不可选    DayOfPP里面
    private PhotoInfo albumCoverPhotoInfo;

    public String getPpCode() {
        return ppCode;
    }

    public void setPpCode(String ppCode) {
        this.ppCode = ppCode;
    }

    public int getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(int photoCount) {
        this.photoCount = photoCount;
    }

    public int getVisiblePhotoCount() {
        return visiblePhotoCount;
    }

    public void setVisiblePhotoCount(int visiblePhotoCount) {
        this.visiblePhotoCount = visiblePhotoCount;
    }

    public String getShootDate() {
        return shootDate;
    }

    public void setShootDate(String shootDate) {
        this.shootDate = shootDate;
    }

    public int getIsHidden() {
        return isHidden;
    }

    public void setIsHidden(int isHidden) {
        this.isHidden = isHidden;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<PhotoInfo> getSelectPhotoItemInfos() {
        return selectPhotoItemInfos;
    }

    public void setSelectPhotoItemInfos(List<PhotoInfo> selectPhotoItemInfos) {
        this.selectPhotoItemInfos = selectPhotoItemInfos;
    }

    public int getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(int isSelected) {
        this.isSelected = isSelected;
    }

    public PPinfo() {
        super();
    }

    public PhotoInfo getAlbumCoverPhotoInfo() {
        return albumCoverPhotoInfo;
    }

    public void setAlbumCoverPhotoInfo(PhotoInfo albumCoverPhotoInfo) {
        this.albumCoverPhotoInfo = albumCoverPhotoInfo;
    }
}
