package com.pictureair.photopass.editPhoto.bean;

import android.graphics.Bitmap;


import java.util.List;

/**
 * Created by talon on 16/5/21.
 * 纪录每次照片编辑的步骤
 */
public class PhotoEditorInfo {

    private String photoPath;  //纪录图片的路径
    private int editType;  // 编辑照片的类型。 0 默认值代表原图。1，代表边框，2，代表 滤镜， 3 ，代表饰品，4 代表字体。
    private Bitmap frameBitmap; // 相框的路径
    private List<PhotoStikerInfo> stikerInfoList; // 饰品的对象，纪录bitmap 和 matrix
    private String filterName; // 纪录 滤镜的名字
    private int rotateAngle; //记录旋转角度。顺时针旋转角度

    public int getEditType() {
        return editType;
    }

    public void setEditType(int editType) {
        this.editType = editType;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public Bitmap getFrameBitmap() {
        return frameBitmap;
    }

    public void setFrameBitmap(Bitmap frameBitmap) {
        this.frameBitmap = frameBitmap;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public int getRotateAngle() {
        return rotateAngle;
    }

    public void setRotateAngle(int rotateAngle) {
        this.rotateAngle = rotateAngle;
    }

    public List<PhotoStikerInfo> getStikerInfoList() {
        return stikerInfoList;
    }

    public void setStikerInfoList(List<PhotoStikerInfo> stikerInfoList) {
        this.stikerInfoList = stikerInfoList;
    }

    public PhotoEditorInfo(int editType, String filterName, Bitmap frameBitmap, String photoPath, int rotateAngle, List<PhotoStikerInfo> stikerInfoList) {
        this.editType = editType;
        this.filterName = filterName;
        this.frameBitmap = frameBitmap;
        this.photoPath = photoPath;
        this.rotateAngle = rotateAngle;
        this.stikerInfoList = stikerInfoList;
    }

    public PhotoEditorInfo() {

    }

}
