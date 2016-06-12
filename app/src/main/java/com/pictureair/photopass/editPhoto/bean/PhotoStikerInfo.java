package com.pictureair.photopass.editPhoto.bean;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.pictureair.photopass.editPhoto.StickerItem;

import java.util.LinkedHashMap;

/**
 * Created by talon on 16/5/21.
 * 辅助 EditPhotoInfo 类，纪录每次编辑时 保存的多个饰品。
 */
public class PhotoStikerInfo {
    private LinkedHashMap<Integer, StickerItem> addItems;
    private Matrix touchMatrix;


    public LinkedHashMap<Integer, StickerItem> getAddItems() {
        return addItems;
    }

    public void setAddItems(LinkedHashMap<Integer, StickerItem> addItems) {
        this.addItems = addItems;
    }

    public Matrix getTouchMatrix() {
        return touchMatrix;
    }

    public void setTouchMatrix(Matrix touchMatrix) {
        this.touchMatrix = touchMatrix;
    }

    public PhotoStikerInfo(LinkedHashMap<Integer, StickerItem> addItems, Matrix touchMatrix) {
        this.addItems = addItems;
        this.touchMatrix = touchMatrix;
    }

    public PhotoStikerInfo() {
    }
}
