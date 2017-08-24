package com.pictureair.hkdlphotopass.editPhoto.interf;

import android.os.Handler;

/**
 * Created by talon on 16/5/21.
 * 点击方法操作
 */
public interface PWEditViewListener {

    /**返回键*/
    void leftBackClik();

    /**临时保存，点击右上角勾号时触发*/
    void saveTempPhoto();

    /**上一步*/
    void lastStep();

    /**下一步*/
    void nextStep();

    void rotate();

    void rotateLfet90();

    void rotateRight90();

    /**实际保存图片*/
    void saveReallyPhoto();

    /**点击Frame按钮*/
    void frame();

    /**点击filter按钮*/
    void filter();

    /**点击sticker按钮*/
    void sticker(int mainImageHeight, int mainImageWidth); //显示饰品，为了限定滑动区域，故传参数。

    Handler getHandler();

    void locationOnResume();

    void locationOnPause();

    void showFrame(String framePath);

    void finishActivity();

    void onPwDialogClick(int which, int id);

    void onStickItemDelete();

}
