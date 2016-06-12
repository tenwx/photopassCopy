package com.pictureair.photopass.editPhoto.interf;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.pictureair.photopass.adapter.EditActivityAdapter;
import com.pictureair.photopass.widget.PictureWorksDialog;

/**
 * Created by talon on 16/5/20.
 */
public interface PWEditViewInterface {

    void dialogShow(); // 显示Loading

    void dialogDismiss(); // 隐藏Loading

    void setLister(PWEditViewListener pwEditViewListener);

    void showEditView(int curEditType,EditActivityAdapter editActivityAdapter); // 显示底部栏目

    void showBitmap(Bitmap bitmap);

    void exitEditStatus(); //退出编辑状态

    void updateLastAndNextUI(int lastNextStatus); //更新 前进后退UI

    void showTempSave();

    void hideTempSave();

    void showReallySave();

    void leftBackClik();

    void showIsSaveDialog(PictureWorksDialog pictureWorksDialog); //显示是否保存的对话框

    void showPhotoFrame(ImageLoader imageLoader, DisplayImageOptions options, String framePath); //显示用户的边框

    void hidePhotoFrame(ImageLoader imageLoader, DisplayImageOptions options,String framePath);

    void showPhotoStickerView(); //事先操作， 显示stickerView的组件

    void setPhotoStickerRec(Rect rect); //设置饰品可滑动的区域

    void showPhotoSticker(ImageLoader imageLoader, String stickerPath);  //

    void hidePhotoStickerView(); //隐藏饰品view，清空饰品

    void ToastShow(int stringId); //弹出Toast

}
