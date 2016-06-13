package com.pictureair.photopass.editPhoto.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.EditActivityAdapter;
import com.pictureair.photopass.editPhoto.interf.PWEditViewInterface;
import com.pictureair.photopass.editPhoto.interf.PWEditViewListener;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.HorizontalListView;
import com.pictureair.photopass.widget.PictureWorksDialog;

/**
 * Created by talon on 16/5/20.
 * 负责页面的绘制，不做逻辑操作。(除了判断页面是否显示)
 */
public class PWEditView implements View.OnClickListener, PWEditViewInterface{
    private CustomProgressDialog dialog; // Loading
    private ImageView mLeftBack;
    private Activity mActivity;
    private ImageView mLastStep,mNextStep, mMainImage, mPhotoFrame;
    private ImageButton mTempSave;
    protected PWEditViewListener pwEditViewListener;
    private TextView mRotate,mLeft90,mRight90,mTitle,mReallySave,mFrame,mFilter,mSticker;
    private LinearLayout mRotetaView;
    private HorizontalListView mHorizontalListView;

    public void initView(Activity activity) {
        mActivity = activity;
        activity.setContentView(R.layout.activity_edit_photo);
//        dialog = CustomProgressDialog.create(mActivity, mActivity.getString(R.string.dealing), false, null);

//        mLeftBack = (ImageView) activity.findViewById(R.id.edit_return);
//        mLastStep = (ImageView) activity.findViewById(R.id.btn_last_step);
//        mNextStep = (ImageView) activity.findViewById(R.id.btn_next_step);
//        mMainImage = (ImageView) activity.findViewById(R.id.main_image);
//        mTempSave = (ImageButton) activity.findViewById(R.id.ib_temp_save);
//        mRotate = (TextView) activity.findViewById(R.id.tv_edit_rotate);
//        mRotetaView = (LinearLayout) activity.findViewById(R.id.ll_rotate_bar);
//        mTitle = (TextView) activity.findViewById(R.id.tv_title);
//        mLeft90 = (TextView) activity.findViewById(R.id.tv_left90);
//        mRight90 = (TextView) activity.findViewById(R.id.tv_right90);
//        mReallySave = (TextView) activity.findViewById(R.id.tv_really_save);
//        mFrame = (TextView) activity.findViewById(R.id.tv_edit_frame);
//        mHorizontalListView = (HorizontalListView) activity.findViewById(R.id.horizontalListView);
//        mFilter = (TextView) activity.findViewById(R.id.tv_edit_filter);
//        mSticker = (TextView) activity.findViewById(R.id.tv_edit_sticker);
//        mPhotoFrame = (ImageView) activity.findViewById(R.id.iv_photoframe);
//
//        mLeftBack.setOnClickListener(this);
//        mLastStep.setOnClickListener(this);
//        mNextStep.setOnClickListener(this);
//        mTempSave.setOnClickListener(this);
//        mRotate.setOnClickListener(this);
//        mLeft90.setOnClickListener(this);
//        mRight90.setOnClickListener(this);
//        mReallySave.setOnClickListener(this);
//        mFrame.setOnClickListener(this);
//        mFilter.setOnClickListener(this);
//        mSticker.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
//        switch (view.getId()){
//            case R.id.edit_return: //返回按钮。
//                pwEditViewListener.leftBackClik();
//                break;
//            case R.id.btn_last_step:
//                pwEditViewListener.lastStep();
//                break;
//            case R.id.btn_next_step:
//                pwEditViewListener.nextStep();
//                break;
//            case R.id.ib_temp_save:
//                dialog.show();
//                pwEditViewListener.saveTempPhoto();
//                break;
//            case R.id.tv_really_save:
//                pwEditViewListener.saveReallyPhoto();
//                break;
//            case R.id.tv_edit_rotate:
//                pwEditViewListener.rotate();
//                break;
//            case R.id.tv_left90:
//                pwEditViewListener.rotateLfet90();
//                break;
//            case R.id.tv_right90:
//                pwEditViewListener.rotateRight90();
//                break;
//            case R.id.tv_edit_frame:
//                pwEditViewListener.frame();
//                break;
//            case R.id.tv_edit_filter:
//                pwEditViewListener.filter();
//                break;
//            case R.id.tv_edit_sticker:
//                pwEditViewListener.sticker();
//                break;
//
//        }

    }

    @Override
    public void setLister(PWEditViewListener pwEditViewListener) {
//        this.pwEditViewListener = pwEditViewListener;
    }

    @Override
    public void showEditView(int curEditType, EditActivityAdapter editActivityAdapter) {
//        if (curEditType == PhotoCommon.EditRotate){
//            mTitle.setText(R.string.rotate);
//            mRotetaView.setVisibility(View.VISIBLE);
//        }else if(curEditType == PhotoCommon.EditFrame){
//            mTitle.setText(R.string.frames);
//            mHorizontalListView.setVisibility(View.VISIBLE);
//            mHorizontalListView.setAdapter(editActivityAdapter);
//        }else if (curEditType == PhotoCommon.EditFilter){
//            mTitle.setText(R.string.magicbrush);
//            mHorizontalListView.setVisibility(View.VISIBLE);
//            mHorizontalListView.setAdapter(editActivityAdapter);
//        }else if(curEditType == PhotoCommon.EditSticker){
//            mTitle.setText(R.string.decoration);
//            mHorizontalListView.setVisibility(View.VISIBLE);
//            mHorizontalListView.setAdapter(editActivityAdapter);
//        }
//        onEditStatus();
    }

    @Override // 只要是加载了Bitmap，说明进行了编辑，就显示保存按钮。
    public void showBitmap(Bitmap bitmap) {
//        mMainImage.setImageBitmap(bitmap);
    }

    @Override
    public void exitEditStatus() {
//        if (dialog.isShowing()){
//            dialog.dismiss();
//        }
//        if (mRotetaView.isShown()){
//            mRotetaView.setVisibility(View.GONE);
//        }
//        if (mHorizontalListView.isShown()){
//            mHorizontalListView.setVisibility(View.GONE);
//        }
//        mTitle.setVisibility(View.GONE);
//        mTempSave.setVisibility(View.GONE);
////        mReallySave.setVisibility(View.VISIBLE);
////        edittoolsbar.setVisibility(View.VISIBLE);
//        //字体的编辑条消失。
//        mNextStep.setVisibility(View.VISIBLE);
//        mLastStep.setVisibility(View.VISIBLE);
    }

    @Override
    public void updateLastAndNextUI(int lastNextStatus) {
//        if (lastNextStatus == PhotoCommon.UnableLastAndNext){
//            mNextStep.setImageResource(R.drawable.forward1);
//            mNextStep.setClickable(false);
//            mLastStep.setImageResource(R.drawable.cancel1);
//            mLastStep.setClickable(false);
//        }else if(lastNextStatus == PhotoCommon.AbleLastAndNext){
//            mNextStep.setImageResource(R.drawable.forward);
//            mNextStep.setClickable(true);
//            mLastStep.setImageResource(R.drawable.cancel);
//            mLastStep.setClickable(true);
//        }else if(lastNextStatus == PhotoCommon.AbleLastUnaleNext){
//            mNextStep.setImageResource(R.drawable.forward1);
//            mNextStep.setClickable(false);
//            mLastStep.setImageResource(R.drawable.cancel);
//            mLastStep.setClickable(true);
//        }else if(lastNextStatus == PhotoCommon.AbleNextUnableLast){
//            mNextStep.setImageResource(R.drawable.forward);
//            mNextStep.setClickable(true);
//            mLastStep.setImageResource(R.drawable.cancel1);
//            mLastStep.setClickable(false);
//        }
    }

    @Override
    public void showTempSave() {
//        mTempSave.setVisibility(View.VISIBLE);
    }

    @Override
    public void showReallySave() {
//        mReallySave.setVisibility(View.VISIBLE);
    }

    @Override
    public void leftBackClik() {
//        if (mRotetaView.isShown() || mHorizontalListView.isShown()){
//            exitEditStatus();
//        }else{
//            pwEditViewListener.judgeIsShowDialog();
//        }
    }

    @Override
    public void showIsSaveDialog(PictureWorksDialog pictureWorksDialog) {
//        pictureWorksDialog.show();
    }

    @Override
    public void showPhotoFrame(ImageLoader imageLoader, DisplayImageOptions options, String framePath) {
//        if(!mPhotoFrame.isShown()){
//            mPhotoFrame.setVisibility(View.VISIBLE);
//        }
//        if(!mTempSave.isShown()){
//            mTempSave.setVisibility(View.VISIBLE);
//        }
//        imageLoader.displayImage(framePath, mPhotoFrame, options);
    }

    @Override //隐藏之后显示一个透明层，解决 边框切换闪烁的bug
    public void hidePhotoFrame(ImageLoader imageLoader, DisplayImageOptions options,String framePath) {
//        imageLoader.displayImage(framePath, mPhotoFrame, options);
//        if(mTempSave.isShown()){
//            mTempSave.setVisibility(View.GONE);
//        }
//        if(mPhotoFrame.isShown()){
//            mPhotoFrame.setVisibility(View.GONE);
//        }
    }

    /**
     * 进入编辑状态
     */
    private void onEditStatus(){
//        if(mReallySave.isShown()){
//            mReallySave.setVisibility(View.GONE);
//        }
//        mTitle.setVisibility(View.VISIBLE);
//        mLastStep.setVisibility(View.GONE);
//        mNextStep.setVisibility(View.GONE);
    }

}
