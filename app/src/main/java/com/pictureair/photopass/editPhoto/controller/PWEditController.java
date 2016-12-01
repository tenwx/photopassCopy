package com.pictureair.photopass.editPhoto.controller;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.EditActivityAdapter;
import com.pictureair.photopass.customDialog.PWDialog;
import com.pictureair.photopass.editPhoto.interf.PWEditViewInterface;
import com.pictureair.photopass.editPhoto.interf.PWEditViewListener;
import com.pictureair.photopass.editPhoto.util.PWEditUtil;
import com.pictureair.photopass.editPhoto.util.PhotoCommon;
import com.pictureair.photopass.entity.FrameOrStikerInfo;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.GlideUtil;
import com.pictureair.photopass.util.PictureAirLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by talon on 16/5/20.
 * 负责逻辑性操作
 * 第一次加载网络图片时，已经把Bitmap存入了临时目录。后退的时候，从临时目录取
 */
public class PWEditController implements PWEditViewListener, PWDialog.OnPWDialogClickListener{

    private Activity mActivity;
    private PWEditViewInterface pwEditViewInterface;
    private PWEditUtil pwEditUtil = new PWEditUtil();
    private int index; // 指示着当前在第几个位置 ！
    private int curEditType;
    private String photoPath;
    private boolean isOnLine;

    private Bitmap mMainBitmap; // 原图的Bitmap，开始操作这个Bitmap
    private int rotateAngle;// 记录旋转角度
    private PWDialog pictureWorksDialog; // 询问是否保存的对话框
    EditActivityAdapter eidtAdapter;
    int curFramePosition; // 边框的索引值

    public void onCreate(Activity activity,PWEditViewInterface pwEditViewInterface){
        this.mActivity = activity;
        photoPath = activity.getIntent().getStringExtra("photoPath");
        isOnLine = activity.getIntent().getBooleanExtra("isOnLine",false);
        this.pwEditViewInterface = pwEditViewInterface;
        loadImageFormPath(photoPath, isOnLine);  //加载图片，用ImageLoader加载，故不用新开线程。
        pwEditViewInterface.setLister(this); // 加入接口，消耗内存很小，不用开线程。
        pwEditUtil.createFolder(); //可以放在线程。（不过需要考虑线程还没执行，就执行了其他操作，造成不同步）
        index = 1;
        rotateAngle = 0;
        pwEditUtil.loadFrameList(mActivity); // 加载边框 考虑放入线程。（不过需要考虑线程还没执行，就执行了其他操作，造成不同步）
        pwEditUtil.loadFilterImgPath(mActivity); //加载滤镜图片
        pwEditUtil.loadStickerList(mActivity);
    }


    /**
     * 根据 图片地址加载图片。 仅仅在进入的时候使用一次本方法
     * 如果是网络图片，判断本地是否存在，如果是本地图片，直接加载。
     * @param photoPath
     */
    private void loadImageFormPath(String photoPath,Boolean isOnLine){
        if (isOnLine){
            loadImageOnLine(photoPath);
        }else{
            PictureAirLog.e("====","本地图片");
            loadImageOnLocal(photoPath);
            pwEditUtil.addPhotoEditorInfo(photoPath,PhotoCommon.EditNone,null,null,"",0);
        }
    }

    /**
     * 加载网络图片。
     * @param photoPath
     */
    private void loadImageOnLine(final String photoPath){
        if (pwEditUtil.getFile(photoPath).exists()){
            PictureAirLog.e("====","网络图片本地存在");
            loadImageOnLocal(pwEditUtil.getFile(photoPath).toString());
        }else {
            PictureAirLog.e("====", "网络图片本地不存在");
            GlideUtil.load(mActivity.getApplicationContext(), photoPath, new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                    mMainBitmap = bitmap;
                    pwEditViewInterface.showBitmap(mMainBitmap);
                    String path = pwEditUtil.getTempPath();
                    pwEditUtil.saveBitmap(mMainBitmap, path);
                    pwEditUtil.addPhotoEditorInfo(path, PhotoCommon.EditNone, null, null, "", 0);
                }
            });

        }
    }

    /**
     * 加载本地图片。
     * @param photoPath
     */
    private void loadImageOnLocal(String photoPath){
        GlideUtil.load(mActivity.getApplicationContext(), GlideUtil.getFileUrl(photoPath), new SimpleTarget<Bitmap>(){

            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                mMainBitmap = bitmap;
                pwEditViewInterface.showBitmap(mMainBitmap);
            }
        });

    }


    @Override
    public void leftBackClik() {
        pwEditViewInterface.leftBackClik();
        if (curEditType == PhotoCommon.EditFrame){
            pwEditViewInterface.hidePhotoFrame(pwEditUtil.getFrameInfos().get(0).getThumbnailPathH160());
        }else if(curEditType == PhotoCommon.EditRotate){
            loadImageOnLocal(pwEditUtil.getPhotoEditorList().get(pwEditUtil.getPhotoEditorList().size() - 1).getPhotoPath());
        }


        if (pwEditUtil.getPhotoEditorList().size() > 1){
            pwEditViewInterface.showReallySave();
        }
    }

    @Override //返回按钮，如果有操作，弹出提示框，提醒用户保存
    public void finish() {
            mActivity.finish();
    }

    @Override
    public void saveTempPhoto() {
        PictureAirLog.e("===","保存临时图片");
        new Thread(new Runnable() {
            @Override
            public void run() {
                String path = pwEditUtil.getTempPath();
                if (curEditType == PhotoCommon.EditRotate){

                }else if(curEditType == PhotoCommon.EditFrame){
                    mMainBitmap = pwEditUtil.getFrameComposeBitmap(mActivity,mMainBitmap,curFramePosition);
                }
                pwEditUtil.saveBitmap(mMainBitmap,path);

                pwEditUtil.addPhotoEditorInfo(path, curEditType, null, null, "",rotateAngle);
                rotateAngle = 0;
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        index++;
                        pwEditViewInterface.exitEditStatus();
                        pwEditViewInterface.updateLastAndNextUI(checkLastNext());
                        pwEditViewInterface.showBitmap(mMainBitmap);
                        if (pwEditUtil.getPhotoEditorList().size() > 1){
                            pwEditViewInterface.showReallySave();
                        }
                        if(curEditType == PhotoCommon.EditFrame){
                            pwEditViewInterface.hidePhotoFrame(pwEditUtil.getFrameInfos().get(0).getThumbnailPathH160());
                        }
                    }
                });
            }
        }).start();
    }

    @Override
    public void lastStep() {
        PictureAirLog.e("===","上一步");
        index--;
        GlideUtil.load(mActivity.getApplicationContext(), GlideUtil.getFileUrl(pwEditUtil.getPhotoEditorList().get(index - 1).getPhotoPath()), new SimpleTarget<Bitmap>(){

            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                mMainBitmap = bitmap;
                pwEditViewInterface.showBitmap(mMainBitmap);
                pwEditViewInterface.updateLastAndNextUI(checkLastNext());
            }
        });
    }

    @Override
    public void nextStep() {
        PictureAirLog.e("===","下一步");
        GlideUtil.load(mActivity.getApplicationContext(), GlideUtil.getFileUrl(pwEditUtil.getPhotoEditorList().get(index).getPhotoPath()), new SimpleTarget<Bitmap>(){

            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                mMainBitmap = bitmap;
                pwEditViewInterface.showBitmap(mMainBitmap);
                index++;
                pwEditViewInterface.updateLastAndNextUI(checkLastNext());
            }
        });
    }

    @Override
    public void rotate() {
        PictureAirLog.e("===","旋转图片按钮点击");
        curEditType = PhotoCommon.EditRotate;
        pwEditViewInterface.showEditView(curEditType,null);
    }

    @Override
    public void rotateLfet90() {
        rotateAngle = rotateAngle - 90;
        mMainBitmap = pwEditUtil.rotateImage(mMainBitmap,-90);
        pwEditViewInterface.showBitmap(mMainBitmap);
        pwEditViewInterface.showTempSave();
    }

    @Override
    public void rotateRight90() {
        rotateAngle = rotateAngle + 90;
        mMainBitmap = pwEditUtil.rotateImage(mMainBitmap,90);
        pwEditViewInterface.showBitmap(mMainBitmap);
        pwEditViewInterface.showTempSave();
    }

    @Override
    public void saveReallyPhoto() {
        PictureAirLog.e("===","保存图片按钮点击");
        String path = pwEditUtil.getReallyPath();
        pwEditUtil.copyFile(pwEditUtil.getPhotoEditorList().get(index-1).getPhotoPath(),path);
        pwEditUtil.scanSDcard(path,mActivity);
        pwEditUtil.deleteTempPic(Common.TEMPPIC_PATH);

        Intent intent = new Intent();
        intent.putExtra("photoUrl", path);
        mActivity.setResult(11, intent);
        finish();
    }

    @Override
    public void judgeIsShowDialog() {
        if(pwEditUtil.isNeedShowDialog()){ //弹出对话框
            if (pictureWorksDialog == null) {
                pictureWorksDialog = new PWDialog(mActivity)
                        .setPWDialogMessage(R.string.exit_hint)
                        .setPWDialogNegativeButton(R.string.button_cancel)
                        .setPWDialogPositiveButton(R.string.button_ok)
                        .setOnPWDialogClickListener(this)
                        .pwDialogCreate();
            }
            pwEditViewInterface.showIsSaveDialog(pictureWorksDialog);
        }else {
            finish();
        }
    }

    @Override //点击相框按钮进行的操作。
    public void frame() {
        curEditType = PhotoCommon.EditFrame;
        eidtAdapter = new EditActivityAdapter(mActivity,mMainBitmap, new ArrayList<String>(),curEditType, pwEditUtil.getFrameInfos(), mHandler);
        pwEditViewInterface.showEditView(curEditType,eidtAdapter);

        // 如果照片不是 4:3 。需要裁减
        if ((float) mMainBitmap.getWidth() / mMainBitmap.getHeight() == (float) 4 / 3 || (float) mMainBitmap.getWidth() / mMainBitmap.getHeight() == (float) 3 / 4) {

        } else {
            mMainBitmap = pwEditUtil.cropBitmap(mMainBitmap, 4, 3);
            pwEditViewInterface.showBitmap(mMainBitmap);
        }
    }

    @Override
    public void filter() {
        curEditType = PhotoCommon.EditFilter;
        eidtAdapter = new EditActivityAdapter(mActivity, mMainBitmap, pwEditUtil.getFilterPathList(), curEditType, new ArrayList<FrameOrStikerInfo>(), mHandler);
        pwEditViewInterface.showEditView(curEditType,eidtAdapter);
    }

    @Override
    public void sticker() {
        curEditType = PhotoCommon.EditSticker;
        eidtAdapter = new EditActivityAdapter(mActivity,mMainBitmap, new ArrayList<String>(),curEditType, pwEditUtil.getStikerInfos(), mHandler);
        pwEditViewInterface.showEditView(curEditType,eidtAdapter);
    }

    /**
     * 判断可否后退或者前进。
     * @return
     */
    private int checkLastNext(){
        if (index ==1 && pwEditUtil.getPhotoEditorList().size() == 1){
            return PhotoCommon.UnableLastAndNext;
        }else if(index == pwEditUtil.getPhotoEditorList().size()){
            return PhotoCommon.AbleLastUnaleNext;
        }else if(index ==1  && pwEditUtil.getPhotoEditorList().size() != 1){
            return PhotoCommon.AbleNextUnableLast;
        }else{
            return PhotoCommon.AbleLastAndNext;
        }
    }

    @Override
    public void onPWDialogButtonClicked(int which, int dialogId) {
        if (which == DialogInterface.BUTTON_NEGATIVE) {
            finish();
        } else if (which == DialogInterface.BUTTON_POSITIVE) {
            saveReallyPhoto();
        }
    }

    public class MyHandler extends Handler {
        WeakReference<MyApplication> myApplication;

        MyHandler(MyApplication application) {
            myApplication = new WeakReference<>(application);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (myApplication == null) {
                return;
            }
            switch (msg.what) {
                case 1111: //点击边框Item的回调
                    curEditType = PhotoCommon.EditFrame;
                    curFramePosition = msg.arg1;

                    if (curFramePosition !=0 ){
                        if (pwEditUtil.getFrameInfos().get(curFramePosition).getOnLine() == 1) { // 网络图片
                            if (mMainBitmap.getWidth() < mMainBitmap.getHeight()) {
                                pwEditViewInterface.showPhotoFrame("file://" + mActivity.getFilesDir().toString() + "/frames/frame_portrait_" +
                                                AppUtil.getReallyFileName(pwEditUtil.getFrameInfos().get(curFramePosition).getOriginalPathPortrait(),0));
                            }else{
                                pwEditViewInterface.showPhotoFrame("file://" + mActivity.getFilesDir().toString() + "/frames/frame_landscape_" +
                                                AppUtil.getReallyFileName(pwEditUtil.getFrameInfos().get(curFramePosition).getOriginalPathLandscape(),0));
                            }
                        }else {  // 本地图片
                            if (mMainBitmap.getWidth() < mMainBitmap.getHeight()) {
                                  pwEditViewInterface.showPhotoFrame(pwEditUtil.getFrameInfos().get(curFramePosition).getOriginalPathPortrait());
                            }else{
                                pwEditViewInterface.showPhotoFrame(pwEditUtil.getFrameInfos().get(curFramePosition).getOriginalPathLandscape());
                            }
                        }
                    }else{
                        pwEditViewInterface.hidePhotoFrame(pwEditUtil.getFrameInfos().get(0).getThumbnailPathH160());
                    }
                    break;

                default:
                    break;
            }
        }
    }
    private MyHandler mHandler = new MyHandler(MyApplication.getInstance());

}
