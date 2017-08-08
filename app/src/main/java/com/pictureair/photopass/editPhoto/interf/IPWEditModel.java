package com.pictureair.photopass.editPhoto.interf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;

import com.pictureair.photopass.editPhoto.bean.PhotoEditorInfo;
import com.pictureair.photopass.editPhoto.bean.StikerInfo;
import com.pictureair.photopass.editPhoto.filter.Filter;
import com.pictureair.photopass.editPhoto.widget.StickerItem;
import com.pictureair.photopass.entity.FrameOrStikerInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by pengwu on 16/9/18.
 */
public interface IPWEditModel {

    File getFile(String photoPath);

    Bitmap getRotateBitmap(Bitmap bit, int rotateAngle);

    Bitmap getFrameComposeBitmap(Context context, Bitmap mMainBitmap, int curFramePosition);

    void createFolder();

    String getTempPath();

    File getTempFile();

    String getReallyPath();

    void addPhotoEditorInfo(String photoPath, int editType, int framePosition, List< StikerInfo > stikerInfoList, String filterName, int rotateAngle);

    ArrayList<PhotoEditorInfo> getPhotoEditorList();

    void setPhotoEditorList(ArrayList<PhotoEditorInfo> photoEditorList);

    void loadFrameList();

    ArrayList<FrameOrStikerInfo> getFrameInfos();

    void setFrameInfos(ArrayList<FrameOrStikerInfo> frameInfos);

    void loadFilterImgPath();

    List<String> getFilterPathList();

    void setFilterPathList(List<String> filterPathList);

    void loadStickerList();

    ArrayList<FrameOrStikerInfo> getStikerInfos();

    void setStikerInfos(ArrayList<FrameOrStikerInfo> stikerInfos);

    Rect getStickerRect(int mainBitmapHeight, int mainBitmapWidth, int mainImageHeight, int mainImageWidth, Context context);

    Bitmap getStickerComposeBitmap(LinkedHashMap<Integer, StickerItem> addItems, Matrix touchMatrix, Bitmap bitmap);

    Bitmap getFilterComposeBitmap(Bitmap bitmap, Filter filter);

    Filter getSelectFilter(int position);

    ArrayList<PhotoEditorInfo> getTempEditPhotoInfoArrayList();

    void tempEditPhotoListAddList(List<PhotoEditorInfo> list);

    void tempEditPhotoListRemoveItem();

    void tempEditPhotoListAddItem();

    List<StikerInfo> getStikerInfoList();

    void getLastContentSuccess(String msg);

    void inOrOutPlace(final String locationIds, final boolean in);

    Bitmap saveFilterOther(Context context, Bitmap bitmap, int index);

    /**
     * 删除EditPhotoInfoArrayList末尾几项
     * @param index 表示删除index后面的几项
     * */
    void deleteEditPhotoInfoArrayListLastItems(int index);
}
