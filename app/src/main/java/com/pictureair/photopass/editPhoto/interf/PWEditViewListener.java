package com.pictureair.photopass.editPhoto.interf;


import android.graphics.Matrix;

import com.pictureair.photopass.editPhoto.StickerItem;

import java.util.LinkedHashMap;

/**
 * Created by talon on 16/5/21.
 * 点击方法操作
 */
public interface PWEditViewListener {

    void leftBackClik();

    void finish();

    void saveTempPhoto(LinkedHashMap<Integer, StickerItem> addItems, Matrix touchMatrix);

    void lastStep();

    void nextStep();

    void rotate();

    void rotateLfet90();

    void rotateRight90();

    void saveReallyPhoto();

    void judgeIsShowDialog();

    void frame();

    void filter();

    void sticker(int mainImageHeight, int mainImageWidth); //显示饰品，为了限定滑动区域，故传参数。

}
