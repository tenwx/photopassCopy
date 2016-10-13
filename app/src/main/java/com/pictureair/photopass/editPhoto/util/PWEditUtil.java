package com.pictureair.photopass.editPhoto.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
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
import com.pictureair.photopass.editPhoto.filter.EarlyBird;
import com.pictureair.photopass.editPhoto.filter.Filter;
import com.pictureair.photopass.editPhoto.filter.HDRFilter;
import com.pictureair.photopass.editPhoto.filter.LomoFi;
import com.pictureair.photopass.editPhoto.filter.LomoFilter;
import com.pictureair.photopass.editPhoto.filter.NormalFilter;
import com.pictureair.photopass.editPhoto.filter.OldFilter;
import com.pictureair.photopass.editPhoto.interf.IPWEditModel;
import com.pictureair.photopass.editPhoto.widget.StickerItem;
import com.pictureair.photopass.entity.FrameOrStikerInfo;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.util.ScreenUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by talon on 16/5/20.
 */
public class PWEditUtil implements IPWEditModel{

    private File desFile; //保存文件的目标目录
    private File tempFile; //保存文件的临时目录

    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    private ArrayList<PhotoEditorInfo> photoEditorList; //纪录编辑照片的步骤
    private ArrayList<PhotoEditorInfo> tempEditPhotoInfoArrayList;
    private SimpleDateFormat dateFormat;
    private boolean loadingFrame = false;

    private String[][] framePathStr = {{"frame/frame_none.png","frame/frame_none.png","frame/frame_none.png","frame/frame_none.png"},
            {"frame/frame_h_1t.png","frame/frame_v_1t.png","frame/frame_h_1.png","frame/frame_v_1.png"},
            {"frame/frame_h_2t.png","frame/frame_v_2t.png","frame/frame_h_2.png","frame/frame_v_2.png"},
            {"frame/frame_h_3t.png","frame/frame_v_3t.png","frame/frame_h_3.png","frame/frame_v_3.png"},
            {"frame/frame_h_4t.png","frame/frame_v_4t.png","frame/frame_h_4.png","frame/frame_v_4.png"}
    };


    public static final int FRAMECOUNT = 4+1;//正常frame的数量+1个frame_none
    private ArrayList<FrameOrStikerInfo> frameInfos; //保存边框的集合。
    private List<String> filterPathList; // 保存滤镜图片路径的集合
    private ArrayList<FrameOrStikerInfo> stikerInfos;// 饰品图片路径列表

    private List<StikerInfo> stikerInfoList;

    private PictureAirDbManager pictureAirDbManager;
    private ArrayList<FrameOrStikerInfo> frameFromDBInfos;//来自数据库的数据
    private ArrayList<FrameOrStikerInfo> stickerFromDBInfos;//来自数据库的数据
    public PWEditUtil() {
        photoEditorList = new ArrayList<PhotoEditorInfo>();
        tempEditPhotoInfoArrayList = new ArrayList<PhotoEditorInfo>();
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public  Bitmap getRotateBitmap(Bitmap bit, int rotateAngle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotateAngle);
        Bitmap tempBitmap = Bitmap.createBitmap(bit, 0, 0, bit.getWidth(),
                bit.getHeight(), matrix, true);
        return tempBitmap;
    }

    /**
     * 获取合成边框后的Bitmap
     * @param mMainBitmap
     * @param curFramePosition
     * @return
     */
    @Override
    public Bitmap getFrameComposeBitmap(Bitmap mMainBitmap, int curFramePosition){

        // 如果照片不是 4:3 。需要裁减
        if ((float) mMainBitmap.getWidth() / mMainBitmap.getHeight() == (float) 4 / 3 || (float) mMainBitmap.getWidth() / mMainBitmap.getHeight() == (float) 3 / 4) {

        } else {
            mMainBitmap = EditPhotoUtil.cropBitmap(mMainBitmap, 4, 3);
        }

        Bitmap heBitmap = Bitmap.createBitmap(mMainBitmap.getWidth(), mMainBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Bitmap frameBitmap;
        String loadPhotoUrl;
        if (mMainBitmap.getWidth()<mMainBitmap.getHeight()) {
            if(frameInfos.get(curFramePosition).onLine == 1){
                frameBitmap = imageLoader.loadImageSync("file://" + MyApplication.getInstance().getFilesDir().toString() + "/frames/frame_portrait_" + AppUtil.getReallyFileName(frameInfos.get(curFramePosition).frameOriginalPathPortrait,0));
            }else{
                frameBitmap = imageLoader.loadImageSync(frameInfos.get(curFramePosition).frameOriginalPathPortrait);
            }
        }else{
            if(frameInfos.get(curFramePosition).onLine == 1){
                frameBitmap = imageLoader.loadImageSync("file://" + MyApplication.getInstance().getFilesDir().toString() + "/frames/frame_landscape_" + AppUtil.getReallyFileName(frameInfos.get(curFramePosition).frameOriginalPathLandscape,0));
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
    @Override
    public void createFolder(){
        desFile = new File(Common.PHOTO_SAVE_PATH);
        if (!desFile.isDirectory()) {
            desFile.mkdirs();// 创建根目录文件夹
        }
        EditPhotoUtil.deleteTempPic(Common.TEMPPIC_PATH); //每次进入清空temp文件夹。
        tempFile = new File(Common.TEMPPIC_PATH);
        if (!tempFile.isDirectory()) {
            tempFile.mkdirs();// 创建根目录文件夹
        }
    }

    /**
     * 获取临时保存的路径
     * @return
     */
    @Override
    public String getTempPath(){
        if (dateFormat == null){
            dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
        }
       return tempFile + "/" + dateFormat.format(new Date()) + ".jpg";
    }

    @Override
    public File getTempFile(){
        return  tempFile;
    }

    /**
     * 获取真正保存的路径
     * @return
     */
    @Override
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
    @Override
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

    @Override
    public ArrayList<PhotoEditorInfo> getPhotoEditorList() {
        return photoEditorList;
    }

    @Override
    public void setPhotoEditorList(ArrayList<PhotoEditorInfo> photoEditorList) {
        this.photoEditorList = photoEditorList;
    }

    /**
     * 获取边框代码
     */
    @Override
    public void loadFrameList(){
        frameInfos.clear();

        for (int i=0; i<framePathStr.length; i++) {
            FrameOrStikerInfo frameInfo = new FrameOrStikerInfo();
            frameInfo.frameThumbnailPathH160 = ImageDownloader.Scheme.ASSETS.wrap(framePathStr[i][0]);
            frameInfo.frameThumbnailPathV160 = ImageDownloader.Scheme.ASSETS.wrap(framePathStr[i][1]);
            frameInfo.frameOriginalPathLandscape = ImageDownloader.Scheme.ASSETS.wrap(framePathStr[i][2]);
            frameInfo.frameOriginalPathPortrait = ImageDownloader.Scheme.ASSETS.wrap(framePathStr[i][3]);
            frameInfos.add(frameInfo);
        }
    }

    @Override
    public ArrayList<FrameOrStikerInfo> getFrameInfos() {
        return frameInfos;
    }

    @Override
    public void setFrameInfos(ArrayList<FrameOrStikerInfo> frameInfos) {
        this.frameInfos = frameInfos;
    }

    /**
     * 加载滤镜图片
     */
    @Override
    public void loadFilterImgPath(){
        filterPathList.clear();
        filterPathList.add("filter/original.png");
        filterPathList.add("filter/filter1.png");
        filterPathList.add("filter/filter2.png");
        filterPathList.add("filter/filter3.png");
        filterPathList.add("filter/filter4.png");
        filterPathList.add("filter/filter5.png");
        filterPathList.add("filter/filter6.png");
    }

    @Override
    public List<String> getFilterPathList() {
        return filterPathList;
    }

    @Override
    public void setFilterPathList(List<String> filterPathList) {
        this.filterPathList = filterPathList;
    }

    /**
     * 加载饰品
     */
    @Override
    public void loadStickerList(){
        stikerInfos.clear();
        FrameOrStikerInfo frameOrStikerInfo;
        try {
            String[] files =MyApplication.getInstance().getResources().getAssets()
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

    @Override
    public ArrayList<FrameOrStikerInfo> getStikerInfos() {
        return stikerInfos;
    }

    @Override
    public void setStikerInfos(ArrayList<FrameOrStikerInfo> stikerInfos) {
        this.stikerInfos = stikerInfos;
    }

    @Override
    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    @Override
    public DisplayImageOptions getOptions() {
        return options;
    }

    /**
     * 获取饰品可滑动的范围
     * 计算出 图片真正显示的坐标。
     */
    @Override
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
    @Override
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
     * @return  滤镜比较特殊，饰品与相框不需要滤镜效果。故应将原图先处理滤镜效果，再叠加之前图片应用的效果。
     */
    @Override
    public Bitmap getFilterComposeBitmap(Bitmap bitmap, Filter filter){

        if (bitmap == null || bitmap.isRecycled()){ // 解决使用部分滤镜崩溃
            return null;
        }
        if (filter instanceof LomoFi) {
            bitmap = ((LomoFi) filter).transform(bitmap);
        } else if (filter instanceof EarlyBird) {
            bitmap = ((EarlyBird) filter).transform(bitmap,
                    MyApplication.getInstance().getResources());
        } else if (filter instanceof Amaro) {
            bitmap = ((Amaro) filter).transform(bitmap);
        } else if (filter instanceof NormalFilter) {
            bitmap = ((NormalFilter) filter).transform(bitmap);
        } else if (filter instanceof LomoFilter) {
            bitmap = ((LomoFilter) filter).transform(bitmap);
        } else if (filter instanceof BeautifyFilter) {
            bitmap = ((BeautifyFilter) filter).transform(bitmap);
        } else if (filter instanceof HDRFilter) {
            bitmap = ((HDRFilter) filter).transform(bitmap);
        } else if (filter instanceof OldFilter) {
            bitmap = ((OldFilter) filter).transform(bitmap);
        } else if (filter instanceof BlurFilter) {
            bitmap = ((BlurFilter) filter).transform(bitmap);
        }
        bitmap = saveFilterOther(bitmap); //保存其他步骤
        return bitmap;
    }

    @Override
    public Filter getSelectFilter(int position){
        Filter filter = null;
        switch (position) {
            case 0:
                filter = new NormalFilter();
                break;
            case 1:
                filter = new LomoFilter();
                break;
            case 2:
                // 流年效果
                filter = new Amaro();
                break;
            case 3:
                // 自然美肤效果
                filter = new BeautifyFilter();
                break;
            case 4:
                // HDR 效果
                filter = new HDRFilter();
                break;
            case 5:
                // 自然美肤效果
                filter = new BlurFilter();
                break;
            case 6:
                // 怀旧效果
                filter = new OldFilter();
                break;
            default:
                break;
        }
        return filter;
    }

    /**
     * 保存除了滤镜之外的所有步骤
     * @param bitmap
     * @return
     */
    private Bitmap saveFilterOther(Bitmap bitmap) {
        if (tempEditPhotoInfoArrayList.size() == 1){

        }else {
            for (int i = 0; i < tempEditPhotoInfoArrayList.size(); i++) {
                if (getPhotoEditorList().get(i).getEditType() == PhotoCommon.EditFrame) {
                    bitmap = getFrameComposeBitmap(bitmap, tempEditPhotoInfoArrayList.get(i).getFramePosition());
                }
                if (getPhotoEditorList().get(i).getEditType() == PhotoCommon.EditSticker) {
                    bitmap = saveStiker(bitmap, tempEditPhotoInfoArrayList.get(i).getStikerInfoList());
                }
                if (getPhotoEditorList().get(i).getEditType() == PhotoCommon.EditRotate) {
                    bitmap = getRotateBitmap(bitmap, tempEditPhotoInfoArrayList.get(i).getRotateAngle());
                }
            }
        }
        return bitmap;
    }

    @Override
    public ArrayList<PhotoEditorInfo> getTempEditPhotoInfoArrayList(){
        return tempEditPhotoInfoArrayList;
    }

    @Override
    public void tempEditPhotoListAddList(List<PhotoEditorInfo> list){
        if (tempEditPhotoInfoArrayList != null){
            tempEditPhotoInfoArrayList.addAll(list);
        }
    }

    @Override
    public void tempEditPhotoListRemoveItem(){
        tempEditPhotoInfoArrayList.remove(photoEditorList.get(photoEditorList.size()-1));
    }

    @Override
    public void tempEditPhotoListAddItem(){
        tempEditPhotoInfoArrayList.add(photoEditorList.get(photoEditorList.size()-1));
    }

    @Override
    public List<StikerInfo> getStikerInfoList() {
        return stikerInfoList;
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
     * 获取网络边框并处理数据。
     * 原方法，未改变
     * @param msg
     */
    @Override
    public void getLastContentSuccess(String msg){
        try {
            com.alibaba.fastjson.JSONObject resultJsonObject = com.alibaba.fastjson.JSONObject.parseObject(msg);
            if (resultJsonObject.containsKey("assets")) {
                pictureAirDbManager.insertFrameAndStickerIntoDB(resultJsonObject.getJSONObject("assets"));
            }
            if (resultJsonObject.containsKey("time")) {
                SPUtils.put(MyApplication.getInstance(), Common.SHARED_PREFERENCE_APP, Common.GET_LAST_CONTENT_TIME, resultJsonObject.getString("time"));
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
    @Override
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
}
