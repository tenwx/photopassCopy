package com.pictureair.photopass.editPhoto.interf;


import android.graphics.Matrix;
import android.os.Handler;

import com.pictureair.photopass.editPhoto.widget.StickerItem;

import java.util.LinkedHashMap;

/**
 * Created by talon on 16/5/21.
 * 点击方法操作
 */
public interface PWEditViewListener {

    void leftBackClik();

    void saveTempPhoto();

    void lastStep();

    void nextStep();

    void rotate();

    void rotateLfet90();

    void rotateRight90();

    void saveReallyPhoto();

    void frame();

    void filter();

    void sticker(int mainImageHeight, int mainImageWidth); //显示饰品，为了限定滑动区域，故传参数。

    Handler getHandler();

    void locationOnResume();

    void locationOnPause();

    void showFrame(String framePath);

    void finishActivity();

}
