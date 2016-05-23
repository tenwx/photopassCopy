package com.pictureair.photopass.editPhoto.bean;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by talon on 16/5/21.
 * 辅助 EditPhotoInfo 类，纪录每次编辑时 保存的多个饰品。
 */
public class PhotoStikerInfo {

    private Bitmap stickerBitmap;
    private Matrix stickerMatrix;

    public Matrix getStickerMatrix() {
        return stickerMatrix;
    }

    public void setStickerMatrix(Matrix stickerMatrix) {
        this.stickerMatrix = stickerMatrix;
    }

    public Bitmap getStickerBitmap() {
        return stickerBitmap;
    }

    public void setStickerBitmap(Bitmap stickerBitmap) {
        this.stickerBitmap = stickerBitmap;
    }

    public PhotoStikerInfo(Bitmap stickerBitmap, Matrix stickerMatrix) {
        this.stickerBitmap = stickerBitmap;
        this.stickerMatrix = stickerMatrix;
    }

}
