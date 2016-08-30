package com.pictureair.photopass.editPhoto.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.editPhoto.bean.PhotoEditorInfo;
import com.pictureair.photopass.editPhoto.bean.StikerInfo;
import com.pictureair.photopass.editPhoto.filter.Amaro;
import com.pictureair.photopass.editPhoto.filter.BeautifyFilter;
import com.pictureair.photopass.editPhoto.filter.BlurFilter;
import com.pictureair.photopass.editPhoto.filter.HDRFilter;
import com.pictureair.photopass.editPhoto.filter.LomoFilter;
import com.pictureair.photopass.editPhoto.filter.OldFilter;
import com.pictureair.photopass.editPhoto.widget.StickerItem;
import com.pictureair.photopass.entity.FrameOrStikerInfo;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by talon on 16/5/20.
 */
public class PWEditUtil {

    private File desFile; //保存文件的目标目录
    private File tempFile; //保存文件的临时目录

    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    private ArrayList<PhotoEditorInfo> photoEditorList; //纪录编辑照片的步骤
    private SimpleDateFormat dateFormat;

    private String[][] framePathStr = {{"frame/frame_none.png","frame/frame_none.png","frame/frame_none.png","frame/frame_none.png"},
            {"frame/frame_h_1t.png","frame/frame_v_1t.png","frame/frame_h_1.png","frame/frame_v_1.png"},
            {"frame/frame_h_2t.png","frame/frame_v_2t.png","frame/frame_h_2.png","frame/frame_v_2.png"},
            {"frame/frame_h_3t.png","frame/frame_v_3t.png","frame/frame_h_3.png","frame/frame_v_3.png"},
            {"frame/frame_h_4t.png","frame/frame_v_4t.png","frame/frame_h_4.png","frame/frame_v_4.png"}
    };
    private ArrayList<FrameOrStikerInfo> frameInfos; //保存边框的集合。
    private List<String> filterPathList; // 保存滤镜图片路径的集合
    private ArrayList<FrameOrStikerInfo> stikerInfos;// 饰品图片路径列表

    private List<StikerInfo> stikerInfoList;

    private PictureAirDbManager pictureAirDbManager;
    private ArrayList<FrameOrStikerInfo> frameFromDBInfos;//来自数据库的数据
    private ArrayList<FrameOrStikerInfo> stickerFromDBInfos;//来自数据库的数据
    public PWEditUtil() {
        photoEditorList = new ArrayList<PhotoEditorInfo>();
        frameInfos = new ArrayList<FrameOrStikerInfo>();
        filterPathList = new ArrayList<String>();
        stikerInfos = new ArrayList<FrameOrStikerInfo>();
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder().cacheInMemory(true).build();
        pictureAirDbManager = new PictureAirDbManager(MyApplication.getInstance());
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
     * 获取网络图片的 Bitmap
     * @param photoPath
     * @return  如果网络图片链接打不开，就返回为空。上一级判断。
     */
    public Bitmap getOnLineBitampFormPath(String photoPath){
        try {
            return imageLoader.loadImageSync(photoPath);
        }catch (Exception e){
            return null;
        }
    }

    /**
     * 获取本地图片的 Bitmap
     * @param photoPath
     * @return
     */
    public Bitmap getLocalBitampFormPath(String photoPath){
        return imageLoader.loadImageSync("file:///" + photoPath);
    }

    /**
     * 图片旋转
     *
     * @param bit     旋转原图像
     * @param rotateAngle 旋转度数
     * @return 旋转之后的图像
     */
    public  Bitmap getRotateBitmap(Bitmap bit, int rotateAngle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotateAngle);
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

        // 如果照片不是 4:3 。需要裁减
        if ((float) mMainBitmap.getWidth() / mMainBitmap.getHeight() == (float) 4 / 3 || (float) mMainBitmap.getWidth() / mMainBitmap.getHeight() == (float) 3 / 4) {

        } else {
            mMainBitmap = cropBitmap(mMainBitmap, 4, 3);
        }

        Bitmap heBitmap = Bitmap.createBitmap(mMainBitmap.getWidth(), mMainBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Bitmap frameBitmap;
        if (mMainBitmap.getWidth()<mMainBitmap.getHeight()) {
            if(frameInfos.get(curFramePosition).onLine == 1){
                frameBitmap = imageLoader.loadImageSync("file://" + mContext.getFilesDir().toString() + "/frames/frame_portrait_" + AppUtil.getReallyFileName(frameInfos.get(curFramePosition).frameOriginalPathPortrait,0));
            }else{
                frameBitmap = imageLoader.loadImageSync(frameInfos.get(curFramePosition).frameOriginalPathPortrait);
            }
        }else{
            if(frameInfos.get(curFramePosition).onLine == 1){
                frameBitmap = imageLoader.loadImageSync("file://" + mContext.getFilesDir().toString() + "/frames/frame_landscape_" + AppUtil.getReallyFileName(frameInfos.get(curFramePosition).frameOriginalPathLandscape,0));
            }else{
                frameBitmap = imageLoader.loadImageSync(frameInfos.get(curFramePosition).frameOriginalPathLandscape);
            }
        }

        Canvas canvas = new Canvas(heBitmap);
        Paint point = new Paint();
        point.setXfermode(new PorterDuffXfermode(
                android.graphics.PorterDuff.Mode.SRC_OVER));
        Matrix matrix2 = new Matrix();
        matrix2.postScale(
                (float) mMainBitmap.getWidth() / (frameBitmap.getWidth()),
                (float) mMainBitmap.getHeight() / (frameBitmap.getHeight()));

        frameBitmap = Bitmap.createBitmap(frameBitmap, 0, 0,
                frameBitmap.getWidth(), frameBitmap.getHeight(),
                matrix2, true);

        canvas.drawBitmap(mMainBitmap, 0, 0, point);
//				canvas.drawBitmap(frameBitmap, matrix2, point);
        canvas.drawBitmap(frameBitmap, 0,0, point);
        matrix2.reset();
        frameBitmap.recycle();
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
     * @param isOnLine
     * @return
     */
    public boolean isNeedShowDialog(boolean isOnLine) {
        int count = 0;  //本地照片不会加载到 temp 目录下，网络图片会。
        if (isOnLine){
            count = 1;
        }

        tempFile = new File(Common.TEMPPIC_PATH);
        if (tempFile != null) {
            if (tempFile.exists() && tempFile.isDirectory()) {
                if (tempFile.list().length > count) {
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

    /**
     * 纪录每次编辑的步骤
     * @param photoPath
     * @param editType
     * @param framePosition
     * @param stikerInfoList
     * @param filterName
     * @param rotateAngle
     */
    public void addPhotoEditorInfo(String photoPath, int editType, int framePosition, List< StikerInfo > stikerInfoList, String filterName, int rotateAngle){
        PhotoEditorInfo photoEditorInfo = new PhotoEditorInfo();
        photoEditorInfo.setPhotoPath(photoPath);
        photoEditorInfo.setEditType(editType);
        photoEditorInfo.setFramePosition(framePosition);

        photoEditorInfo.setStikerInfoList(stikerInfoList);

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
        MediaScannerConnection.scanFile(context, new String[] { photoPath }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String arg0, Uri arg1) {
                        // TODO Auto-generated method stub
//                        editor = sharedPreferences.edit();
//                        editor.putString(Common.LAST_PHOTO_URL, file);
//                        editor.commit();
                        // 可以添加一些返回的数据过去，还有扫描最好放在返回去之后。
//                        Intent intent = new Intent();
//                        intent.putExtra("photoUrl", photoPath);
//                        context.setResult(11, intent);
//                        PictureAirLog.out("set result--------->");
//                        finish();
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
    public void loadFrameList(){
        for (int i=0; i<framePathStr.length; i++){
            FrameOrStikerInfo frameInfo = new FrameOrStikerInfo();
            frameInfo.frameThumbnailPathH160 = ImageDownloader.Scheme.ASSETS.wrap(framePathStr[i][0]);
            frameInfo.frameThumbnailPathV160 = ImageDownloader.Scheme.ASSETS.wrap(framePathStr[i][1]);
            frameInfo.frameOriginalPathLandscape = ImageDownloader.Scheme.ASSETS.wrap(framePathStr[i][2]);
            frameInfo.frameOriginalPathPortrait = ImageDownloader.Scheme.ASSETS.wrap(framePathStr[i][3]);
            frameInfos.add(frameInfo);
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
    public void loadFilterImgPath(){
        filterPathList.add("filter/original.png");
        filterPathList.add("filter/filter1.png");
        filterPathList.add("filter/filter2.png");
        filterPathList.add("filter/filter3.png");
        filterPathList.add("filter/filter4.png");
        filterPathList.add("filter/filter5.png");
        filterPathList.add("filter/filter6.png");
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
        PictureAirLog.d("====","stikerInfos:"+stikerInfos.size());
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
    public Bitmap cropBitmap(Bitmap bitmap, int num1, int num2) {
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

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public DisplayImageOptions getOptions() {
        return options;
    }


    /**
     * 获取饰品可滑动的范围
     * 计算出 图片真正显示的坐标。
     */
    public Rect getStickerRect(int mainBitmapHeight, int mainBitmapWidth, int mainImageHeight, int mainImageWidth, Context context){
        int displayBitmapHeight,displayBitmapWidth;
        if (mainBitmapHeight / (float)mainBitmapWidth > mainImageHeight / (float)mainImageWidth) {//左右会留白
            displayBitmapHeight = mainImageHeight;//displayBitmapHeight : ? = bitmapReallyHeight : bitmapReallyWidth
            displayBitmapWidth = (int) (displayBitmapHeight * mainBitmapWidth / (float) mainBitmapHeight);
        }else {//上下留白
            displayBitmapWidth = mainImageWidth;
            displayBitmapHeight = (int) (displayBitmapWidth * mainBitmapHeight / (float)mainBitmapWidth);
        }
        int leftTopX = (ScreenUtil.getScreenWidth(context) - displayBitmapWidth) / 2;
        int rightBottomX = leftTopX + displayBitmapWidth;
        //leftTopY = 图片上边距＋imageview.getY
        //图片上边距 ＝ （imageview的高 － 图片显示在imageview上的高）／ 2
        int leftTopY = (mainImageHeight - displayBitmapHeight) / 2;
        int rightBottomY = leftTopY + displayBitmapHeight;
        Rect rec = new Rect(leftTopX,leftTopY,rightBottomX,rightBottomY);
        return rec;
    }

    /**
     * 获取 饰品压缩的Bitmap
     * @param addItems
     * @param touchMatrix
     * @param bitmap
     * @return
     */
    public Bitmap getStickerComposeBitmap(LinkedHashMap<Integer, StickerItem> addItems, Matrix touchMatrix, Bitmap bitmap){
        stikerInfoList = new ArrayList<StikerInfo>();

        Bitmap resultBit = Bitmap.createBitmap(bitmap).copy(
                Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(resultBit);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));  //抗锯齿
        float[] data = new float[9];
        touchMatrix.getValues(data);// 底部图片变化记录矩阵原始数据
        Matrix3 cal = new Matrix3(data);// 辅助矩阵计算类
        Matrix3 inverseMatrix = cal.inverseMatrix();// 计算逆矩阵
        Matrix m = new Matrix();
        m.setValues(inverseMatrix.getValues());
        for (Integer id : addItems.keySet()) {
            StickerItem item = addItems.get(id);
            item.matrix.postConcat(m);// 乘以底部图片变化矩阵
            canvas.drawBitmap(item.bitmap, item.matrix, null);
            stikerInfoList.add(new StikerInfo(item.bitmap, item.matrix)); //添加进去
        }
        return resultBit;
    }

    /**
     * 获取滤镜Bitmap
     * @param bitmap
     * @param position
     * @return  滤镜比较特殊，饰品与相框不需要滤镜效果。故应将原图先处理滤镜效果，再叠加之前图片应用的效果。
     */
    public Bitmap getFilterComposeBitmap(Context mContext, Bitmap bitmap, int position, int backStep){
        if (bitmap == null || bitmap.isRecycled()){ // 解决使用部分滤镜崩溃
            return null;
        }
        switch (position){
            case 0:
                break;
            case 1:
                bitmap = new LomoFilter().transform(bitmap);
                break;
            case 2:
                bitmap = new Amaro().transform(bitmap);
                break;
            case 3:
                bitmap = new BeautifyFilter().transform(bitmap);
                break;
            case 4:
                bitmap = new HDRFilter().transform(bitmap);
                break;
            case 5:
                bitmap = new BlurFilter().transform(bitmap);
                break;
            case 6:
                bitmap = new OldFilter().transform(bitmap);
                break;
            default:
                break;
        }
        bitmap = saveFilterOther(mContext, bitmap, backStep); //保存其他步骤
        return bitmap;
    }

    /**
     * 保存除了滤镜之外的所有步骤
     * @param bitmap
     * @return
     */
    private Bitmap saveFilterOther(Context mContext, Bitmap bitmap, int backStep) {
        for (int i = 0; i < getPhotoEditorList().size() - backStep; i++){
            if (getPhotoEditorList().get(i).getEditType() == PhotoCommon.EditFrame){
                bitmap = getFrameComposeBitmap(mContext, bitmap, getPhotoEditorList().get(i).getFramePosition());
            }
            if (getPhotoEditorList().get(i).getEditType() == PhotoCommon.EditSticker){
                bitmap = saveStiker(bitmap, getPhotoEditorList().get(i).getStikerInfoList());
            }
            if (getPhotoEditorList().get(i).getEditType() == PhotoCommon.EditRotate){
                bitmap = getRotateBitmap(bitmap, getPhotoEditorList().get(i).getRotateAngle());
            }
        }
        return bitmap;
    }

    public List<StikerInfo> getStikerInfoList() {
        return stikerInfoList;
    }

    public void setStikerInfoList(List<StikerInfo> stikerInfoList) {
        this.stikerInfoList = stikerInfoList;
    }

    /**
     * 保存 饰品
     * @param bitmap
     * @return
     */
    private Bitmap saveStiker(Bitmap bitmap, List<StikerInfo> stikerInfoList) {
        Bitmap resultBit = Bitmap.createBitmap(bitmap.copy(Bitmap.Config.ARGB_8888, true));
        for (int i = 0; i < stikerInfoList.size(); i++) {
            Canvas canvas = new Canvas(resultBit);
            canvas.drawBitmap(stikerInfoList.get(i).getStickerBitmap(), stikerInfoList.get(i).getStickerMatrix(), null);
        }
        return resultBit;
    }

    /**
     * 动态设置margin
     * @param v
     * @param l
     * @param t
     * @param r
     * @param b
     */
    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }


    /**
     * 获取网络边框并处理数据。
     * 原方法，未改变
     * @param msg
     */
    public void getLastContentSuccess(String msg){
        try {
            com.alibaba.fastjson.JSONObject resultJsonObject = com.alibaba.fastjson.JSONObject.parseObject(msg);
            if (resultJsonObject.containsKey("assets")) {
                pictureAirDbManager.insertFrameAndStickerIntoDB(resultJsonObject.getJSONObject("assets"));
            }
            if (resultJsonObject.containsKey("time")) {
                PWEditSPUtil.setValue(MyApplication.getInstance(),Common.GET_LAST_CONTENT_TIME,resultJsonObject.getString("time"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //写入数据库之后，再从数据库拿数据
        frameFromDBInfos = pictureAirDbManager.getLastContentDataFromDB(1);
        for (int i = 0; i < frameFromDBInfos.size(); i++) {
            if (frameFromDBInfos.get(i).locationId.equals("common")) {//通用边框
                frameInfos.add(frameFromDBInfos.get(i));
            }
        }
        //从数据库获取饰品信息
        stickerFromDBInfos = pictureAirDbManager.getLastContentDataFromDB(0);
        for (int j = 0; j < stickerFromDBInfos.size(); j++) {
            if (stickerFromDBInfos.get(j).locationId.equals("common")) {//通用饰品
                stikerInfos.add(stickerFromDBInfos.get(j));
            }
        }
    }

    /**
     * 根据地点显示边框或者饰品。
     * 进入或离开地点
     * 原方法，未改变
     */
    private boolean loadingFrame = false;
    public void inOrOutPlace(final String locationIds, final boolean in){
        new Thread(){
            public void run() {
                while (!loadingFrame) {//等待边框处理完毕
                    if (frameFromDBInfos != null && stickerFromDBInfos != null) {
                        loadingFrame = true;
                    }
                }
                //1.根据locationIds来判断需要显示或者隐藏的边框
//				frameInfos.addAll(frameFromDBInfos);
                for (int i = 0; i < frameFromDBInfos.size(); i++) {
                    PictureAirLog.out("locationIds:"+locationIds+":locationId:"+frameFromDBInfos.get(i).locationId);
                    if (locationIds.contains(frameFromDBInfos.get(i).locationId)) {//有属于特定地点的边框
                        if (in) {//进入
                            frameInfos.add(frameFromDBInfos.get(i));
                        }else {//离开
                            frameInfos.remove(frameFromDBInfos.get(i));
                        }
                    }
                }
//				stikerInfos.addAll(stickerFromDBInfos);
                for (int j = 0; j < stickerFromDBInfos.size(); j++) {
                    PictureAirLog.out("locationIds:"+locationIds+":locationId:"+stickerFromDBInfos.get(j).locationId);
                    if (locationIds.contains(stickerFromDBInfos.get(j).locationId)) {//有属于特定地点的边框
                        if (in) {//进入
                            stikerInfos.add(stickerFromDBInfos.get(j));
                        }else {//离开
                            stikerInfos.remove(stickerFromDBInfos.get(j));
                        }
                    }
                }
            };
        }.start();
    }

}
