package com.pictureair.photopass.editPhoto.interf;

import android.graphics.Bitmap;

import com.pictureair.photopass.adapter.EditActivityAdapter;
import com.pictureair.photopass.customDialog.PWDialog;

/**
 * Created by talon on 16/5/20.
 */
public interface PWEditViewInterface {

    void setLister(PWEditViewListener pwEditViewListener);

    void showEditView(int curEditType,EditActivityAdapter editActivityAdapter); // 显示底部栏目

    void showBitmap(Bitmap bitmap);

    void exitEditStatus(); //退出编辑状态

    void updateLastAndNextUI(int lastNextStatus); //更新 前进后退UI

    void showTempSave();

    void showReallySave();

    void leftBackClik();

    void showIsSaveDialog(PWDialog pwDialog); //显示是否保存的对话框

    void showPhotoFrame(String framePath); //显示用户的边框

    void hidePhotoFrame(String framePath);

}
