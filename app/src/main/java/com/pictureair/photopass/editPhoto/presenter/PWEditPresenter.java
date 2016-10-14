package com.pictureair.photopass.editPhoto.presenter;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.EditActivityAdapter;
import com.pictureair.photopass.editPhoto.filter.Filter;
import com.pictureair.photopass.editPhoto.interf.IPWEditView;
import com.pictureair.photopass.editPhoto.interf.PWEditViewListener;
import com.pictureair.photopass.editPhoto.util.BitmapUtils;
import com.pictureair.photopass.editPhoto.util.EditPhotoUtil;
import com.pictureair.photopass.editPhoto.util.PWEditUtil;
import com.pictureair.photopass.editPhoto.util.PhotoCommon;
import com.pictureair.photopass.editPhoto.widget.StickerItem;
import com.pictureair.photopass.entity.DiscoverLocationItemInfo;
import com.pictureair.photopass.entity.FrameOrStikerInfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.GlideUtil;
import com.pictureair.photopass.util.LocationUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SPUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by talon on 16/5/20.
 * 负责逻辑性操作
 * 第一次加载网络图片时，已经把Bitmap存入了临时目录。后退的时候，从临时目录取
 */
public class PWEditPresenter implements PWEditViewListener, LocationUtil.OnLocationNotificationListener{

    private IPWEditView pwEditView;
    private PWEditUtil pwEditUtil;
    private int curEditType;
    private String photoPath;
    private boolean isOnLine;
    private int imageWidth, imageHeight;// 展示图片控件 宽 高

    private Bitmap mMainBitmap; // 原图的Bitmap，开始操作这个Bitmap
    private Bitmap filterBitmap; //滤镜处理完之后的Bitmap

    private int rotateAngle;// 记录旋转角度
    EditActivityAdapter eidtAdapter;
    int curFramePosition; // 边框的索引值
    private int index = -1; // 索引。   控制图片步骤 前进后退。
    private int lastEditionPosition = 0;
    private boolean isEncrypted = false;
    private LocationUtil locationUtil;
    private ArrayList<DiscoverLocationItemInfo> locationItemInfos;
    private LoadImageTask mLoadImageTask;
    private Filter filter;
    private MyHandler mHandler;
    //对象
    private PhotoInfo photoInfo;


    public void onCreate(IPWEditView pwEditViewInterface){
        this.pwEditView = pwEditViewInterface;
        pwEditUtil = new PWEditUtil();
        pwEditUtil.createFolder(); //可以放在线程。（不过需要考虑线程还没执行，就执行了其他操作，造成不同步）
        rotateAngle = 0;
        pwEditUtil.loadFrameList(); // 加载边框 考虑放入线程。（不过需要考虑线程还没执行，就执行了其他操作，造成不同步）
        pwEditUtil.loadFilterImgPath(); //加载滤镜图片
        pwEditUtil.loadStickerList();
        imageWidth = 900;
        imageHeight = 1200;
        mHandler = new MyHandler(pwEditView.getEditPhotView());
        locationItemInfos = new ArrayList<DiscoverLocationItemInfo>();
        locationUtil = new LocationUtil(pwEditView.getEditPhotView());
        photoInfo = pwEditView.getEditPhotView().getIntent().getParcelableExtra("photo");
        photoPath = pwEditView.getEditPhotView().getIntent().getStringExtra("photoPath");
        isOnLine = pwEditView.getEditPhotView().getIntent().getBooleanExtra("isOnLine",false);
        loadImageFormPath();  //加载图片，用ImageLoader加载，故不用新开线程。
        // 获取网络饰品与边框
        API1.getLastContent(SPUtils.getString(MyApplication.getInstance(),Common.SHARED_PREFERENCE_APP,Common.GET_LAST_CONTENT_TIME,""), mHandler);
    }


    /**
     * 根据 图片地址加载图片。 仅仅在进入的时候使用一次本方法
     * 如果是网络图片，判断本地是否存在，如果是本地图片，直接加载。
     */
    private void loadImageFormPath(){
        if (photoInfo.onLine == 1){
            PictureAirLog.v("====","网络图片");
            photoPath = photoInfo.photoThumbnail_1024;
            isOnLine = true;
            isEncrypted = AppUtil.isEncrypted(photoInfo.isEncrypted);
            loadImageOnLine();
        }else{
            PictureAirLog.v("====","本地图片");
            photoPath = photoInfo.photoPathOrURL;
            isOnLine = false;
            loadImageOnLocal(photoPath, true);
        }
        pwEditUtil.addPhotoEditorInfo(photoPath, PhotoCommon.EditNone, curFramePosition, null, "",rotateAngle);
    }

    /**
     * 加载网络图片。
     * 需要使用三级缓存，1.判断SD卡是否存在，2.判断缓存中是否存在，3.从网上下载
     */
    private void loadImageOnLine(){
        if (pwEditUtil.getFile(photoPath).exists()){
            PictureAirLog.d("====","网络图片本地存在");
            loadImageOnLocal(pwEditUtil.getFile(photoPath).toString(), true);
        }else{
            PictureAirLog.d("====","网络图片本地不存在");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    PictureAirLog.d("===","photoPath:"+photoPath);
                    mHandler.sendEmptyMessage(PhotoCommon.DOWNLOAD_ONLINE);
                }
            }).start();
        }
    }

    /**
     * 加载本地图片。
     */
    private void loadImageOnLocal(String photoPath, boolean isInitload){
        if (mLoadImageTask != null) {
            mLoadImageTask.cancel(true);
        }
        mLoadImageTask = new LoadImageTask(isInitload);
        mLoadImageTask.execute(photoPath);
    }

    private final class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private boolean isInitLoad;
        public LoadImageTask(boolean isInitLoad) {
            this.isInitLoad = isInitLoad;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = BitmapUtils.loadImageByPath(params[0], imageWidth, imageHeight);
            if(AppUtil.getExifOrientation(params[0])!=0){ // 修改图片显示方向问题。
                bitmap = AppUtil.rotaingImageView(AppUtil.getExifOrientation(params[0]),bitmap);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (mMainBitmap != null) {
                mMainBitmap.recycle();
                mMainBitmap = null;
                System.gc();
            }
            mMainBitmap = result;
            if (isInitLoad) {
                if (mMainBitmap != null) {
                    pwEditView.showBitmap(mMainBitmap);
                }
                if (null != mHandler) {
                    mHandler.sendEmptyMessage(PhotoCommon.INIT_DATA_FINISHED);
                }
            } else {
                mHandler.sendEmptyMessage(PhotoCommon.START_ASYNC);
            }
        }
    }

    /**
     * 执行滤镜任务
     *
     * @author talon
     *
     */
    private final class ExcuteFilterTask extends
            AsyncTask<Bitmap, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Bitmap... params) {
            if (params[0] == null || params[0].isRecycled()) {
                return null;
            }
            Bitmap tempBitmap = pwEditUtil.getFilterComposeBitmap(mMainBitmap,filter);
            filterBitmap = pwEditUtil.saveFilterOther(pwEditView.getEditPhotView(), tempBitmap); //保存其他步骤
            return filterBitmap;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            pwEditView.dialogDismiss();
        }

        @Override
        protected void onCancelled(Bitmap result) {
            super.onCancelled(result);
            pwEditView.dialogDismiss();
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (result != null) {
                pwEditView.showBitmap(result);
            }
            pwEditView.dialogDismiss();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pwEditView.dialogShow();
        }
    }

    @Override
    public void leftBackClik() {
        if (curEditType == PhotoCommon.EditFrame){
            pwEditView.hidePhotoFrame();
            cancelFrameEdition();
        }

        if (curEditType == PhotoCommon.EditSticker){
            pwEditView.hidePhotoStickerView();
        }

        if (curEditType == PhotoCommon.EditFilter){
            if (filterBitmap != null){
                filterBitmap = null;
            }

            if (pwEditUtil.getPhotoEditorList().size() == 1){
                if (isOnLine) {
                    loadImageOnLine();
                }else{
                    loadImageOnLocal(photoPath, true);
                }
            }else{
                loadImageOnLocal(pwEditUtil.getPhotoEditorList().get(pwEditUtil.getPhotoEditorList().size()-1).getPhotoPath(), true);
            }
        }

        if (curEditType == PhotoCommon.EditRotate){
            if (pwEditUtil.getPhotoEditorList().size() == 1){
                if (isOnLine) {
                    loadImageOnLine();
                }else{
                    loadImageOnLocal(photoPath, true);
                }
            }else{
                loadImageOnLocal(pwEditUtil.getPhotoEditorList().get(pwEditUtil.getPhotoEditorList().size()-1).getPhotoPath(), true);
            }
        }

        pwEditView.exitEditStatus();

        if (pwEditUtil.getPhotoEditorList().size() > 1){
            pwEditView.showReallySave();
        }
    }

    @Override
    public void saveTempPhoto() {
        PictureAirLog.d("===","保存临时图片");
        if (index == 0) {
            pwEditUtil.getPhotoEditorList().clear();
            pwEditUtil.addPhotoEditorInfo(photoPath, 0, 0, null, "", 0);
        }
        if (!AppUtil.checkPermission(MyApplication.getInstance(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) { //检查权限
            pwEditView.ToastShow(R.string.permission_storage_message);
            return;
        }
        SaveStickersTask task = new SaveStickersTask();
        if (curEditType == PhotoCommon.EditRotate || curEditType == PhotoCommon.EditFrame || curEditType == PhotoCommon.EditSticker){
            task.execute(mMainBitmap);
        }else if(curEditType == PhotoCommon.EditFilter){
            task.execute(filterBitmap);
        }
    }

    //保存贴图 滤镜 的异步方法。
    /**
     * 保存贴图任务
     *
     * @author panyi
     *
     */

    private final class SaveStickersTask extends
            AsyncTask<Bitmap, Void, Bitmap> {
        private LinkedHashMap<Integer, StickerItem> bank;
        private Matrix matrix;
        @Override
        protected Bitmap doInBackground(Bitmap... params) {
            // System.out.println("保存贴图!");
            if (params[0] == null || params[0].isRecycled()) {
                return null;
            }
            String url = pwEditUtil.getTempPath();
            if (curEditType == PhotoCommon.EditFilter) {//滤镜
                EditPhotoUtil.saveBitmap(params[0], url);
                pwEditUtil.addPhotoEditorInfo(url, curEditType, 0, null, "",0);
                index = pwEditUtil.getPhotoEditorList().size() - 1;
                return params[0];
            }else if(curEditType == PhotoCommon.EditSticker){//饰品
                Bitmap resultBit = pwEditUtil.getStickerComposeBitmap(bank, matrix, params[0]);
                EditPhotoUtil.saveBitmap(resultBit, url);
                pwEditUtil.addPhotoEditorInfo(url, curEditType, 0,  pwEditUtil.getStikerInfoList(), "",0);
                index = pwEditUtil.getPhotoEditorList().size() - 1;
                return resultBit;
            }else if(curEditType == PhotoCommon.EditRotate){
                Bitmap resultBit = Bitmap.createBitmap(params[0]).copy(
                        Bitmap.Config.ARGB_8888, true);
                EditPhotoUtil.saveBitmap(resultBit, url);
                pwEditUtil.addPhotoEditorInfo(url, curEditType, 0, null, "",rotateAngle);
                rotateAngle = 0; //设置完之后恢复状态。
                index = pwEditUtil.getPhotoEditorList().size() - 1;
                return resultBit;
            }else if(curEditType == PhotoCommon.EditFrame){
                Bitmap heBitmap = pwEditUtil.getFrameComposeBitmap(pwEditView.getEditPhotView(), params[0],curFramePosition);
                EditPhotoUtil.saveBitmap(heBitmap, url);
                pwEditUtil.addPhotoEditorInfo(url, curEditType, curFramePosition, null, "",0);
                index = pwEditUtil.getPhotoEditorList().size() - 1;
                return heBitmap;
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            pwEditView.dialogDismiss();
        }

        @Override
        protected void onCancelled(Bitmap result) {
            super.onCancelled(result);
            pwEditView.dialogDismiss();
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            pwEditUtil.getTempEditPhotoInfoArrayList().clear();
            pwEditUtil.tempEditPhotoListAddList(pwEditUtil.getPhotoEditorList());
            pwEditView.getStickView().clear();
            pwEditView.hidePhotoFrame();
            changeMainBitmap(result);
            pwEditView.exitEditStatus();
            checkLastNext();
            pwEditView.showBitmap(mMainBitmap);
            pwEditView.dialogDismiss();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pwEditView.dialogShow();
            if (curEditType == PhotoCommon.EditSticker){
                bank = pwEditView.getStickView().getBank();
                matrix = pwEditView.getMainImageView().getImageMatrix();
            }
        }
    }

    /**
     * 切换底图Bitmap
     *
     * @param newBit
     */
    public void changeMainBitmap(Bitmap newBit) {
        if (mMainBitmap != null) {
            if (!mMainBitmap.isRecycled()) {// 回收
                mMainBitmap.recycle();
            }
            mMainBitmap = newBit;
        } else {
            mMainBitmap = newBit;
        }// end if
        pwEditView.showBitmap(mMainBitmap);
    }

    @Override
    public void lastStep() {
        PictureAirLog.d("===","上一步");
        if (index == -1) {
            index = pwEditUtil.getPhotoEditorList().size()-1;
        }
        if (index >= 1) {
            index--;
        }

        if (pwEditUtil.getPhotoEditorList().size()-2 >= 0){
            if (index == 0) {
                if (isOnLine) {
                    loadImageOnLine();
                    pwEditUtil.tempEditPhotoListRemoveItem();
                }else{
                    loadImageOnLocal(pwEditUtil.getPhotoEditorList().get(index).getPhotoPath(), true);
                    pwEditUtil.tempEditPhotoListRemoveItem();
                }
            }else{
                loadImageOnLocal(pwEditUtil.getPhotoEditorList().get(index).getPhotoPath(), true);
                pwEditUtil.tempEditPhotoListRemoveItem();
            }
        }

        checkLastNext();
    }

    @Override
    public void nextStep() {
        PictureAirLog.d("===","下一步");
        if (index == -1) {
            index = pwEditUtil.getPhotoEditorList().size()-1;
        }

        if (pwEditUtil.getPhotoEditorList().size() > index +1) {
            index++;
            loadImageOnLocal(pwEditUtil.getPhotoEditorList().get(index).getPhotoPath(), true);
            pwEditUtil.tempEditPhotoListAddItem();
        }
        checkLastNext();
    }

    @Override
    public void rotate() {
        PictureAirLog.d("===","旋转图片按钮点击");
        curEditType = PhotoCommon.EditRotate;
        pwEditView.showEditView(curEditType,null);
        pwEditView.onEditStatus();
    }

    @Override
    public void rotateLfet90() {
        rotateAngle = rotateAngle - 90;
        mMainBitmap = pwEditUtil.getRotateBitmap(mMainBitmap,-90);
        pwEditView.showBitmap(mMainBitmap);
        pwEditView.showTempSave();
    }

    @Override
    public void rotateRight90() {
        rotateAngle = rotateAngle + 90;
        mMainBitmap = pwEditUtil.getRotateBitmap(mMainBitmap,90);
        pwEditView.showBitmap(mMainBitmap);
        pwEditView.showTempSave();
    }

    @Override
    public void saveReallyPhoto() {
        PictureAirLog.d("===","保存图片按钮点击");
        if (!AppUtil.checkPermission(MyApplication.getInstance().getApplicationContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE)) {//检查权限
            pwEditView.ToastShow(R.string.permission_storage_message);
            return;
        }
        final String path = pwEditUtil.getReallyPath();
        if (index == 0 && isOnLine) { //如果是网络图片，并且 index ＝ 0 的时候，就没有保存到临时文件目录的文件，故保存Bitmap
            pwEditView.dialogShow();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    EditPhotoUtil.saveBitmap(mMainBitmap , path);
                    scan(path);
                    mHandler.sendEmptyMessage(PhotoCommon.INIT_DATA_FINISHED);
                }
            }).start();
        }else{
            EditPhotoUtil.copyFile(pwEditUtil.getPhotoEditorList().get(index).getPhotoPath(),path);
            scan(path);
        }
    }

    // 扫描SD卡
    private void scan(final String file) {
        // TODO Auto-generated method stub
        MediaScannerConnection.scanFile(pwEditView.getEditPhotView(), new String[] { file }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String arg0, Uri arg1) {
                        // TODO Auto-generated method stub
                        SPUtils.put(MyApplication.getInstance(), Common.SHARED_PREFERENCE_APP, Common.LAST_PHOTO_URL, file);
                        // 可以添加一些返回的数据过去，还有扫描最好放在返回去之后。
                        Intent intent = new Intent();
                        intent.putExtra("photoUrl", file);
                        pwEditView.getEditPhotView().setResult(11, intent);
                        pwEditView.finishActivity();
                    }
                });
    }

    @Override //点击相框按钮进行的操作。
    public void frame() {
        lastEditionPosition = 0;
        curEditType = PhotoCommon.EditFrame;
        eidtAdapter = new EditActivityAdapter(MyApplication.getInstance(),mMainBitmap, new ArrayList<String>(),curEditType, pwEditUtil.getFrameInfos(), mHandler);
        pwEditView.showEditView(curEditType,eidtAdapter);
        pwEditView.onEditStatus();
    }

    @Override
    public void filter() {
        lastEditionPosition = 0;
        curEditType = PhotoCommon.EditFilter;
        eidtAdapter = new EditActivityAdapter(MyApplication.getInstance(), mMainBitmap, pwEditUtil.getFilterPathList(), curEditType, new ArrayList<FrameOrStikerInfo>(), mHandler);
        pwEditView.showEditView(curEditType,eidtAdapter);
        pwEditView.onEditStatus();
    }

    @Override
    public void sticker(int mainImageHeight, int mainImageWidth) {
        curEditType = PhotoCommon.EditSticker;
        pwEditView.getStickView().setRec(pwEditUtil.getStickerRect(mMainBitmap.getHeight(), mMainBitmap.getWidth(), mainImageHeight, mainImageWidth, MyApplication.getInstance()));
        pwEditView.showPhotoStickerView();//事先让StickerView显示
        eidtAdapter = new EditActivityAdapter(MyApplication.getInstance(),mMainBitmap, new ArrayList<String>(),curEditType, pwEditUtil.getStikerInfos(), mHandler);
        pwEditView.showEditView(curEditType,eidtAdapter);
        pwEditView.onEditStatus();
    }

    @Override
    public void inOrOutPlace(String locationIds, boolean in) {
        pwEditUtil.inOrOutPlace(locationIds, in);
    }

    @Override
    public Handler getHandler() {
        return mHandler;
    }

    @Override
    public void locationOnResume() {
        if (locationItemInfos.size() == 0) {//说明不存在，需要获取所有的location地点信息
            locationItemInfos.addAll(AppUtil.getLocation(MyApplication.getInstance(), ACache.get(MyApplication.getInstance()).getAsString(Common.DISCOVER_LOCATION), false));

            locationUtil.setLocationItemInfos(locationItemInfos, this);
        }
        locationUtil.startLocation();
    }

    @Override
    public void locationOnPause() {
        locationUtil.stopLocation();
    }

    @Override
    public void showFrame(String framePath) {
        GlideUtil.loadWithNoPlaceHolder(pwEditView.getEditPhotView(), framePath, new SimpleTarget<Bitmap>() {
            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                mHandler.sendEmptyMessage(PhotoCommon.INIT_DATA_FINISHED);
            }

            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                pwEditView.getFrameImageView().setImageBitmap(bitmap);
                pwEditView.showPhotoFrame();
                mHandler.sendEmptyMessage(PhotoCommon.INIT_DATA_FINISHED);
            }

            @Override
            public void onLoadCleared(Drawable placeholder) {
                mHandler.sendEmptyMessage(PhotoCommon.INIT_DATA_FINISHED);
            }

            @Override
            public void onStop() {
                mHandler.sendEmptyMessage(PhotoCommon.INIT_DATA_FINISHED);
            }

            @Override
            public void onDestroy() {
                mHandler.sendEmptyMessage(PhotoCommon.INIT_DATA_FINISHED);
            }
        });

    }

    @Override
    public void finishActivity() {
        File tempFile = pwEditUtil.getTempFile();
        // 判断 是否 需要保存图片
        if (tempFile != null && tempFile.exists() && tempFile.isDirectory() && tempFile.list().length > 0 && index != 0) {
            // 提示是否需要保存图片。
            pwEditView.showIsSaveDialog();
        } else {
            pwEditView.finishActivity();
        }
    }

    @Override
    public void onPwDialogClick(int which, int id) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                saveReallyPhoto();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                pwEditView.finishActivity();
                break;
            default:
                break;
        }
    }

    /**
     * 判断可否后退或者前进。
     * @return
     */
    private void checkLastNext(){
        if (index == -1) {
            index = pwEditUtil.getPhotoEditorList().size() - 1;
        }
        if (index == pwEditUtil.getPhotoEditorList().size() - 1) {
            pwEditView.disableNextBtnClick();
        } else {
            pwEditView.enableNextBtnClick();
        }
        if (index == 0) {
            pwEditView.disableBackBtnClick();
            pwEditView.hideReallySave();
        } else {
            pwEditView.enableBackBtnClick();
            pwEditView.showReallySave();
        }
    }


    public class MyHandler extends Handler {
        WeakReference<Activity> mAactivity;

        MyHandler(Activity activity) {
            mAactivity = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mAactivity == null) {
                return;
            }
            switch (msg.what) {
                case PhotoCommon.OnclickFramePosition: //点击边框Item的回调
                    curEditType = PhotoCommon.EditFrame;
                    if (msg.arg1 == lastEditionPosition){
                        PictureAirLog.out("same with last edition");
                        break;
                    }else{
                        PictureAirLog.out("not same with last edition");
                        lastEditionPosition = msg.arg1;
                    }
                    curFramePosition = msg.arg1;

                    if (curFramePosition == 0){
                        cancelFrameEdition();
                    }else{
                        // 判断 如果图片是 4:3 就不要去裁减。
                        if ((float) mMainBitmap.getWidth() / mMainBitmap.getHeight() == (float) 4 / 3
                                || (float) mMainBitmap.getWidth() / mMainBitmap.getHeight() == (float) 3 / 4) {

                        } else {
                            mMainBitmap = EditPhotoUtil.cropBitmap(mMainBitmap, 4, 3);
                            pwEditView.showBitmap(mMainBitmap);
                        }
                    }
                    if (curEditType != 0) {
                        pwEditView.showTempSave();
                    } else {
                        pwEditView.hideTempSave();
                    }
                    pwEditView.dialogShow();
                    loadFrame(curFramePosition);
                    break;

                case PhotoCommon.OnclickStickerPosition:
                    curEditType = PhotoCommon.EditSticker;
                    int stickerPosition = msg.arg1;
                    pwEditView.showTempSave();
                    String stickerUrl = "";
                    if (pwEditUtil.getStikerInfos().get(stickerPosition).onLine == 1){
                        stickerUrl = Common.PHOTO_URL + pwEditUtil.getStikerInfos().get(stickerPosition).frameOriginalPathPortrait;
                    }else{
                        stickerUrl = pwEditUtil.getStikerInfos().get(stickerPosition).frameOriginalPathPortrait;
                    }
                    GlideUtil.load(pwEditView.getEditPhotView(), stickerUrl, new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                            pwEditView.getStickView().addBitImage(bitmap);
                        }
                    });
                    break;
                case PhotoCommon.OnclickFilterPosition:
                    curEditType = PhotoCommon.EditFilter;
                    final int filterPosition = msg.arg1;
                    if (filterPosition == 0){
                        pwEditView.hideTempSave();
                    }else{
                        pwEditView.showTempSave();
                    }
                    if (lastEditionPosition == filterPosition){
                        PictureAirLog.out("same with last edition");
                        return;
                    }else{
                        PictureAirLog.out("not same with last edition");
                        lastEditionPosition = filterPosition;
                    }
                    filter = pwEditUtil.getSelectFilter(filterPosition);
                    if (isOnLine){
                        String fileString = AppUtil.getReallyFileName(pwEditUtil.getPhotoEditorList().get(0).getPhotoPath(),0);
                        File file = new File(Common.PHOTO_DOWNLOAD_PATH+fileString);
                        if (file.exists()){
                            loadImageOnLocal(file.toString(), false);
                        }else {
                            GlideUtil.load(pwEditView.getEditPhotView(), pwEditUtil.getPhotoEditorList().get(0).getPhotoPath(), new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                                    mMainBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                                    mHandler.sendEmptyMessage(PhotoCommon.START_ASYNC);
                                }
                            });
                        }
                    }else{
                        loadImageOnLocal(pwEditUtil.getPhotoEditorList().get(0).getPhotoPath(), false);
                    }



                    break;

                case API1.GET_LAST_CONTENT_SUCCESS://获取更新信息成功。
                    pwEditUtil.getLastContentSuccess(msg.obj.toString());
                    break;

                case API1.GET_LAST_CONTENT_FAILED://获取更新信息失败，不做操作

                    break;

                case PhotoCommon.DOWNLOAD_ONLINE:
                    GlideUtil.load(pwEditView.getEditPhotView(), photoPath, isEncrypted, new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                            mMainBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                            if (mMainBitmap != null){
                                pwEditView.showBitmap(mMainBitmap);
                                pwEditView.dialogDismiss();
                            }else{
                                //加载图片出错，换张图片试试
                                pwEditView.ToastShow(R.string.load_photo_error);
                            }
                        }
                    });
                    break;

                case PhotoCommon.INIT_DATA_FINISHED:
                    pwEditView.dialogDismiss();
                    break;
                case PhotoCommon.START_ASYNC:
                    ExcuteFilterTask excuteFilterTask = new ExcuteFilterTask();
                    excuteFilterTask.execute(mMainBitmap);
                    break;
                default:
                    break;
            }
        }
    }

    private void cancelFrameEdition() {
        //恢复到没有裁减的状态。
        if (pwEditUtil.getPhotoEditorList().size() == 1 || index == 0){ //代表最初的图片。
            if (isOnLine) {
                loadImageOnLine();
            }else{
                loadImageOnLocal(photoPath, true);
            }
        }else{ // 如果 pathList不仅仅存在 一个。说明本地都存在。 恢复到前一个
            loadImageOnLocal(pwEditUtil.getPhotoEditorList().get(index).getPhotoPath(), true);
        }
    }

    private void loadFrame(int position){
        if (position != 0) {
            if (pwEditUtil.getFrameInfos().get(position).onLine == 1) { // 网络图片
                // 判断宽高，加载不同的边框。加载预览边框
                if (mMainBitmap.getWidth() < mMainBitmap.getHeight()) {
                    showFrame("file://" + MyApplication.getInstance().getFilesDir().toString() + "/frames/frame_portrait_" +
                            AppUtil.getReallyFileName(pwEditUtil.getFrameInfos().get(position).frameOriginalPathPortrait, 0));
                } else {
                    showFrame("file://" + MyApplication.getInstance().getFilesDir().toString() + "/frames/frame_landscape_" +
                            AppUtil.getReallyFileName(pwEditUtil.getFrameInfos().get(position).frameOriginalPathLandscape, 0));
                }
            } else { // 本地图片
                if (mMainBitmap.getWidth() < mMainBitmap.getHeight()) {
                    showFrame(pwEditUtil.getFrameInfos().get(position).frameOriginalPathPortrait);
                } else {
                    showFrame(pwEditUtil.getFrameInfos().get(position).frameOriginalPathLandscape);
                }
            }

        }else{
            pwEditView.hidePhotoFrame();
            mHandler.sendEmptyMessage(PhotoCommon.INIT_DATA_FINISHED);
        }
    }




}
