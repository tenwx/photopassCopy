package com.pictureair.photopass.editPhoto.interf;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.pictureair.photopass.adapter.EditActivityAdapter;
import com.pictureair.photopass.editPhoto.widget.StickerView;
import com.pictureair.photopass.widget.PictureWorksDialog;

/**
 * Created by talon on 16/5/20.
 */
public interface IPWEditView {

    void dialogShow(); // 显示Loading

    void dialogDismiss(); // 隐藏Loading

    void showEditView(int curEditType, EditActivityAdapter editActivityAdapter); // 显示底部栏目

    void showBitmap(Bitmap bitmap);

    void exitEditStatus(); //退出编辑状态

    void showTempSave();

    void hideTempSave();

    void showReallySave();

    void hideReallySave();

    void showIsSaveDialog(); //显示是否保存的对话框

    void showPhotoFrame(); //显示用户的边框

    void hidePhotoFrame();

    void showPhotoStickerView(); //事先操作， 显示stickerView的组件

    void hidePhotoStickerView(); //隐藏饰品view，清空饰品

    void ToastShow(int stringId); //弹出Toast

    Activity getEditPhotView();//获取窗体对象

    void finishActivity();

    void onEditStatus();

    ImageView getFrameImageView();

    ImageView getMainImageView();

    StickerView getStickView();

    void enableNextBtnClick();

    void disableNextBtnClick();

    void enableBackBtnClick();

    void disableBackBtnClick();
}
