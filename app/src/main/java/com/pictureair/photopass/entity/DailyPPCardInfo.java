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
    private String logoUrl;//logo的路径
    private int colorR, colorG, colorB;//颜色RGB
    private int activated;
    private int photoCount;
    private PhotoInfo leftPhoto;//左边地点显示图片
    private PhotoInfo rightPhoto;//右边地点显示图片

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

    public int getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(int photoCount) {
        this.photoCount = photoCount;
    }

    public PhotoInfo getLeftPhoto() {
        return leftPhoto;
    }

    public void setLeftPhoto(PhotoInfo leftPhoto) {
        this.leftPhoto = leftPhoto;
    }

    public PhotoInfo getRightPhoto() {
        return rightPhoto;
    }

    public void setRightPhoto(PhotoInfo rightPhoto) {
        this.rightPhoto = rightPhoto;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public int getColorR() {
        return colorR;
    }

    public void setColorR(int colorR) {
        this.colorR = colorR;
    }

    public int getColorG() {
        return colorG;
    }

    public void setColorG(int colorG) {
        this.colorG = colorG;
    }

    public int getColorB() {
        return colorB;
    }

    public void setColorB(int colorB) {
        this.colorB = colorB;
    }
}
