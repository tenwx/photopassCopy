package com.pictureair.photopass.editPhoto.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;

import com.pictureair.photopass.activity.EditPhotoActivity;
import com.pictureair.photopass.editPhoto.bean.PhotoEditorInfo;
import com.pictureair.photopass.editPhoto.bean.PhotoStikerInfo;
import com.pictureair.photopass.entity.FrameOrStikerInfo;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.GlideUtil;
import com.pictureair.photopass.util.PWMediaScanner;
import com.pictureair.photopass.util.PictureAirLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by talon on 16/5/20.
 */
public class PWEditUtil {

    private File desFile; //保存文件的目标目录
    private File tempFile; //保存文件的临时目录

    private ArrayList<PhotoEditorInfo> photoEditorList; //纪录编辑照片的步骤
    private SimpleDateFormat dateFormat;

    private ArrayList<FrameOrStikerInfo> frameInfos; //保存边框的集合。
    private List<String> filterPathList; // 保存滤镜图片路径的集合
    private ArrayList<FrameOrStikerInfo> stikerInfos;// 饰品图片路径列表
    public PWEditUtil() {
        photoEditorList = new ArrayList<>();
        frameInfos = new ArrayList<>();
        filterPathList = new ArrayList<>();
        stikerInfos = new ArrayList<>();
    }

    /**
     * 图片是否在本地存在
     * @return
     */
    public File getFile(String photoPath){
        photoPath = AppUtil.getReallyFileName(photoPath,0);
        File file = new File(Common.PHOTO_DOWNLOAD_PATH + photoPath);
        return file;
    }

    /**
     * 图片旋转
     *
     * @param bit     旋转原图像
     * @param degrees 旋转度数
     * @return 旋转之后的图像
     */
    public  Bitmap rotateImage(Bitmap bit, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        Bitmap tempBitmap = Bitmap.createBitmap(bit, 0, 0, bit.getWidth(),
                bit.getHeight(), matrix, true);
        return tempBitmap;
    }

    /**
     * 保存Bitmap图片到指定文件
     * @param bm
     * @param filePath
     */
    public void saveBitmap(Bitmap bm, String filePath) {
        File f = new File(filePath);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /**
     * 获取合成边框后的Bitmap
     * @param mContext
     * @param mMainBitmap
     * @param curFramePosition
     * @return
     */
    public Bitmap getFrameComposeBitmap(Context mContext, Bitmap mMainBitmap, int curFramePosition){
        Bitmap heBitmap = Bitmap.createBitmap(mMainBitmap.getWidth(), mMainBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Bitmap frameBitmap;
        String photoUrl;
        if (mMainBitmap.getWidth() < mMainBitmap.getHeight()) {
            if(frameInfos.get(curFramePosition).onLine == 1){
                photoUrl = "file://" + mContext.getFilesDir().toString() + "/frames/frame_portrait_" + AppUtil.getReallyFileName(frameInfos.get(curFramePosition).frameOriginalPathPortrait,0);
            }else{
                photoUrl = frameInfos.get(curFramePosition).frameOriginalPathPortrait;
            }
        }else{
            if(frameInfos.get(curFramePosition).onLine == 1){
                photoUrl = "file://" + mContext.getFilesDir().toString() + "/frames/frame_landscape_" + AppUtil.getReallyFileName(frameInfos.get(curFramePosition).frameOriginalPathLandscape,0);
            }else{
                photoUrl = frameInfos.get(curFramePosition).frameOriginalPathLandscape;
            }
        }
        Canvas canvas = new Canvas(heBitmap);
        Paint point = new Paint();
        point.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OVER));
        canvas.drawBitmap(mMainBitmap, 0, 0, point);

        frameBitmap = GlideUtil.load(mContext, photoUrl, mMainBitmap.getWidth(), mMainBitmap.getHeight());
        if (frameBitmap != null) {
            Matrix matrix2 = new Matrix();
            matrix2.postScale((float) mMainBitmap.getWidth() / (frameBitmap.getWidth()),
                    (float) mMainBitmap.getHeight() / (frameBitmap.getHeight()));

            frameBitmap = Bitmap.createBitmap(frameBitmap, 0, 0, frameBitmap.getWidth(), frameBitmap.getHeight(), matrix2, true);
            canvas.drawBitmap(frameBitmap, 0,0, point);
            matrix2.reset();
            frameBitmap.recycle();
        }
        return heBitmap;
    }


    /**
     * 进入编辑图片时，确保有以下目录。
     */
    public void createFolder(){
        desFile = new File(Common.PHOTO_SAVE_PATH);
        if (!desFile.isDirectory()) {
            desFile.mkdirs();// 创建根目录文件夹
        }
        deleteTempPic(Common.TEMPPIC_PATH); //每次进入清空temp文件夹。
        tempFile = new File(Common.TEMPPIC_PATH);
        if (!tempFile.isDirectory()) {
            tempFile.mkdirs();// 创建根目录文件夹
        }
    }

    public void deleteTempPic(String path) {
        File file = new File(path);
        DeleteFile(file);
    }

    private void DeleteFile(File file) {
        if (file.exists() == false) {
            return;
        } else {
            if (file.isFile()) {
                file.delete();
                return;
            }
            if (file.isDirectory()) {
                File[] childFile = file.listFiles();
                if (childFile == null || childFile.length == 0) {
                    file.delete();
                    return;
                }
                for (File f : childFile) {
                    DeleteFile(f);
                }
                // file.delete();
            }
        }
    }

    /**
     * 获取临时保存的路径
     * @return
     */
    public String getTempPath(){
        if (dateFormat == null){
            dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
        }
       return tempFile + "/" + dateFormat.format(new Date()) + ".jpg";
    }

    /**
     * 判断是否需要弹出对话框。
     * @return
     */
    public boolean isNeedShowDialog() {
        tempFile = new File(Common.TEMPPIC_PATH);
        if (tempFile != null) {
            if (tempFile.exists() && tempFile.isDirectory()) {
                if (tempFile.list().length > 0) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 获取真正保存的路径
     * @return
     */
    public String getReallyPath(){
        if (dateFormat == null){
            dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
        }
        return desFile + "/" + dateFormat.format(new Date()) + ".jpg";
    }

    public void saveReallyPhoto(){

    }


    /**
     * 纪录每次编辑的步骤
     * @param photoPath
     * @param editType
     * @param frameBitmap
     * @param stikerInfoList
     * @param filterName
     * @param rotateAngle
     */
    public void addPhotoEditorInfo(String photoPath, int editType, Bitmap frameBitmap, List<PhotoStikerInfo> stikerInfoList, String filterName, int rotateAngle){
        PhotoEditorInfo photoEditorInfo = new PhotoEditorInfo();
        photoEditorInfo.setPhotoPath(photoPath);
        photoEditorInfo.setEditType(editType);
        if (frameBitmap != null){
            photoEditorInfo.setFrameBitmap(frameBitmap);
        }

        if (stikerInfoList != null){
            photoEditorInfo.setStikerInfoList(stikerInfoList);
        }
        photoEditorInfo.setFilterName(filterName);
        photoEditorInfo.setRotateAngle(rotateAngle);

        photoEditorList.add(photoEditorInfo);
    }

    /**
     * 移动文件
     * @param oldPath String 原文件路径
     * @param newPath String 复制后路径
     */
    public void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    PictureAirLog.out(bytesum + "");
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            PictureAirLog.out("复制单个文件操作出错");
            e.printStackTrace();
        }
    }

    /**
     * 扫描SD卡
     * @param photoPath
     */
    public void scanSDcard(final String photoPath, final Context context) {
        // TODO Auto-generated method stub
        new PWMediaScanner(context, photoPath, null, new PWMediaScanner.ScannerListener() {
            @Override
            public void OnScannerFinish() {
                // 可以添加一些返回的数据过去，还有扫描最好放在返回去之后。

            }
        });
    }

    public ArrayList<PhotoEditorInfo> getPhotoEditorList() {
        return photoEditorList;
    }

    public void setPhotoEditorList(ArrayList<PhotoEditorInfo> photoEditorList) {
        this.photoEditorList = photoEditorList;
    }


    /**
     * 获取边框代码
     */
    public void loadFrameList(Context context){
        frameInfos.clear();

        try {
            String[] files = context.getResources().getAssets().list(EditPhotoActivity.FRAMEPATH);
            for (int i=0; i < EditPhotoActivity.FRAMECOUNT; i++){
                FrameOrStikerInfo frameInfo = new FrameOrStikerInfo();
                if (i == 0){
                    frameInfo.frameThumbnailPathH160 = GlideUtil.getAssetUrl(EditPhotoActivity.FRAMEPATH + File.separator + files[i]);
                    frameInfo.frameThumbnailPathV160 = GlideUtil.getAssetUrl(EditPhotoActivity.FRAMEPATH + File.separator + files[i]);
                    frameInfo.frameOriginalPathLandscape = GlideUtil.getAssetUrl(EditPhotoActivity.FRAMEPATH + File.separator + files[i]);
                    frameInfo.frameOriginalPathPortrait = GlideUtil.getAssetUrl(EditPhotoActivity.FRAMEPATH + File.separator + files[i]);
                }else{
                    int index = (i - 1) * 4;
                    frameInfo.frameOriginalPathLandscape = GlideUtil.getAssetUrl(EditPhotoActivity.FRAMEPATH + File.separator + files[index+1]);
                    frameInfo.frameThumbnailPathH160 = GlideUtil.getAssetUrl(EditPhotoActivity.FRAMEPATH + File.separator + files[index+2]);
                    frameInfo.frameOriginalPathPortrait = GlideUtil.getAssetUrl(EditPhotoActivity.FRAMEPATH + File.separator + files[index+3]);
                    frameInfo.frameThumbnailPathV160 = GlideUtil.getAssetUrl(EditPhotoActivity.FRAMEPATH + File.separator + files[index+4]);
                }
                frameInfos.add(frameInfo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<FrameOrStikerInfo> getFrameInfos() {
        return frameInfos;
    }

    public void setFrameInfos(ArrayList<FrameOrStikerInfo> frameInfos) {
        this.frameInfos = frameInfos;
    }

    /**
     * 加载滤镜图片
     */
    public void loadFilterImgPath(Context context){
        filterPathList.clear();
        try {
            String[] files = context.getResources().getAssets().list(EditPhotoActivity.FILTERPATH);
            for (String name : files){
                filterPathList.add(GlideUtil.getAssetUrl(EditPhotoActivity.FILTERPATH + File.separator + name));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getFilterPathList() {
        return filterPathList;
    }

    public void setFilterPathList(List<String> filterPathList) {
        this.filterPathList = filterPathList;
    }

    /**
     * 加载饰品
     */
    public void loadStickerList(Context context){
        stikerInfos.clear();
        FrameOrStikerInfo frameOrStikerInfo;
        try {
            String[] files =context.getResources().getAssets()
                    .list(PhotoCommon.StickerPath);
            for (String name : files) {
                frameOrStikerInfo = new FrameOrStikerInfo();
                frameOrStikerInfo.frameOriginalPathPortrait = "assets://" + PhotoCommon.StickerPath + File.separator + name;
                frameOrStikerInfo.locationId = "common";
                frameOrStikerInfo.isActive = 1;
                frameOrStikerInfo.onLine = 0;
                stikerInfos.add(frameOrStikerInfo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<FrameOrStikerInfo> getStikerInfos() {
        return stikerInfos;
    }

    public void setStikerInfos(ArrayList<FrameOrStikerInfo> stikerInfos) {
        this.stikerInfos = stikerInfos;
    }


    /**
     * 按照一定的宽高比例裁剪图片
     *
     * @param bitmap
     * @param num1   长边的比例
     * @param num2   短边的比例
     * @return
     */
    public static Bitmap cropBitmap(Bitmap bitmap, int num1, int num2) {
        if (bitmap == null) {
            return null;
        }
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();

        int retX, retY;
        int nw, nh;
        if (w > h) {
            if (h > w * num2 / num1) {
                nw = w;
                nh = w * num2 / num1;
                retX = 0;
                retY = (h - nh) / 2;
            } else {
                nw = h * num1 / num2;
                nh = h;
                retX = (w - nw) / 2;
                retY = 0;
            }
        } else {
            if (w > h * num2 / num1) {
                nh = h;
                nw = h * num2 / num1;
                retY = 0;
                retX = (w - nw) / 2;
            } else {
                nh = w * num1 / num2;
                nw = w;
                retY = (h - nh) / 2;
                retX = 0;
            }
        }
        Bitmap bmp = Bitmap.createBitmap(bitmap, retX, retY, nw, nh, null,
                false);
        return bmp;
    }
}
