package com.pictureair.photopass.activity;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.EditActivityAdapter;
import com.pictureair.photopass.editPhoto.controller.PWEditPresenter;
import com.pictureair.photopass.editPhoto.interf.PWEditViewInterface;
import com.pictureair.photopass.editPhoto.util.PhotoCommon;
import com.pictureair.photopass.editPhoto.widget.StickerView;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.LocationUtil;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.HorizontalListView;
import com.pictureair.photopass.widget.PWToast;
import com.pictureair.photopass.widget.PictureWorksDialog;

//显示的时候用压缩过的bitmap，合成的时候，用原始的bitmap
public class EditPhotoActivity extends BaseActivity implements View.OnClickListener, PWEditViewInterface, LocationUtil.OnLocationNotificationListener{
	PWEditPresenter pwEditPresenter;
	private CustomProgressDialog dialog; // Loading
	private PWToast myToast;
	private ImageView mLeftBack;
	private ImageView mLastStep,mNextStep, mMainImage, mPhotoFrame;
	private ImageButton mTempSave;
	private TextView mRotate,mLeft90,mRight90,mTitle,mReallySave,mFrame,mFilter,mSticker;
	private LinearLayout mRotetaView;
	private HorizontalListView mHorizontalListView;
	private PictureWorksDialog pictureWorksDialog;
	private StickerView mStickerView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        // 进编辑页即释放掉ImageLoader的缓存，尽量增大可用内存
		ImageLoader.getInstance().clearMemoryCache();
		setContentView(R.layout.activity_edit_photo);
		dialog = CustomProgressDialog.create(this, getString(R.string.dealing), false, null);
		myToast = new PWToast(this);
		mLeftBack = (ImageView) findViewById(R.id.edit_return);
		mLastStep = (ImageView) findViewById(R.id.btn_last_step);
		mNextStep = (ImageView) findViewById(R.id.btn_next_step);
		mMainImage = (ImageView) findViewById(R.id.main_image);
		mTempSave = (ImageButton) findViewById(R.id.ib_temp_save);
		mRotate = (TextView) findViewById(R.id.tv_edit_rotate);
		mRotetaView = (LinearLayout) findViewById(R.id.ll_rotate_bar);
		mTitle = (TextView) findViewById(R.id.tv_title);
		mLeft90 = (TextView) findViewById(R.id.tv_left90);
		mRight90 = (TextView) findViewById(R.id.tv_right90);
		mReallySave = (TextView) findViewById(R.id.tv_really_save);
		mFrame = (TextView) findViewById(R.id.tv_edit_frame);
		mHorizontalListView = (HorizontalListView) findViewById(R.id.horizontalListView);
		mFilter = (TextView) findViewById(R.id.tv_edit_filter);
		mSticker = (TextView) findViewById(R.id.tv_edit_sticker);
		mPhotoFrame = (ImageView) findViewById(R.id.iv_photoframe);

		mStickerView = (StickerView) findViewById(R.id.sticker_view);

		mLeftBack.setOnClickListener(this);
		mLastStep.setOnClickListener(this);
		mNextStep.setOnClickListener(this);
		mTempSave.setOnClickListener(this);
		mRotate.setOnClickListener(this);
		mLeft90.setOnClickListener(this);
		mRight90.setOnClickListener(this);
		mReallySave.setOnClickListener(this);
		mFrame.setOnClickListener(this);
		mFilter.setOnClickListener(this);
		mSticker.setOnClickListener(this);

		pwEditPresenter = new PWEditPresenter();
		pwEditPresenter.onCreate(this);
		pictureWorksDialog = new PictureWorksDialog(this, null, getString(R.string.exit_hint), getString(R.string.button_cancel), getString(R.string.button_ok), true, pwEditPresenter.getHandler());

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			onBackKeyDown();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.edit_return: //返回按钮。
				pwEditPresenter.leftBackClik();
				break;
			case R.id.btn_last_step:
				pwEditPresenter.lastStep();
				break;
			case R.id.btn_next_step:
				pwEditPresenter.nextStep();
				break;
			case R.id.ib_temp_save:
				dialog.show();
				pwEditPresenter.saveTempPhoto(mStickerView.getBank(), mMainImage.getImageMatrix());
				break;
			case R.id.tv_really_save:
				pwEditPresenter.saveReallyPhoto();
				break;
			case R.id.tv_edit_rotate:
				pwEditPresenter.rotate();
				break;
			case R.id.tv_left90:
				pwEditPresenter.rotateLfet90();
				break;
			case R.id.tv_right90:
				pwEditPresenter.rotateRight90();
				break;
			case R.id.tv_edit_frame:
				pwEditPresenter.frame();
				break;
			case R.id.tv_edit_filter:
				pwEditPresenter.filter();
				break;
			case R.id.tv_edit_sticker:
				pwEditPresenter.sticker(mMainImage.getHeight(), mMainImage.getWidth());
				break;
		}
	}

	@Override
	public void dialogShow() {
		if (!dialog.isShowing()){
			dialog.show();
		}
	}

	@Override
	public void dialogDismiss() {
		if (dialog.isShowing()){
			dialog.dismiss();
		}
	}

	@Override
	public void showEditView(int curEditType, EditActivityAdapter editActivityAdapter) {
		if (curEditType == PhotoCommon.EditRotate){
			mTitle.setText(R.string.rotate);
			mRotetaView.setVisibility(View.VISIBLE);
		}else if(curEditType == PhotoCommon.EditFrame){
			mTitle.setText(R.string.frames);
			mHorizontalListView.setVisibility(View.VISIBLE);
			mHorizontalListView.setAdapter(editActivityAdapter);
		}else if (curEditType == PhotoCommon.EditFilter){
			mTitle.setText(R.string.magicbrush);
			mHorizontalListView.setVisibility(View.VISIBLE);
			mHorizontalListView.setAdapter(editActivityAdapter);
		}else if(curEditType == PhotoCommon.EditSticker){
			mTitle.setText(R.string.decoration);
			mHorizontalListView.setVisibility(View.VISIBLE);
			mHorizontalListView.setAdapter(editActivityAdapter);
		}
		onEditStatus();
	}

	@Override // 只要是加载了Bitmap，说明进行了编辑，就显示保存按钮。
	public void showBitmap(Bitmap bitmap) {
		if(dialog.isShowing()){
			dialog.dismiss();
		}
		mMainImage.setImageBitmap(bitmap);
	}

	@Override
	public void exitEditStatus() {
		if (dialog.isShowing()){
			dialog.dismiss();
		}
		if (mRotetaView.isShown()){
			mRotetaView.setVisibility(View.GONE);
		}
		if (mHorizontalListView.isShown()){
			mHorizontalListView.setVisibility(View.GONE);
		}
		mTitle.setVisibility(View.GONE);
		mTempSave.setVisibility(View.GONE);
//        mReallySave.setVisibility(View.VISIBLE);
//        edittoolsbar.setVisibility(View.VISIBLE);
		//字体的编辑条消失。
		mNextStep.setVisibility(View.VISIBLE);
		mLastStep.setVisibility(View.VISIBLE);
	}

	@Override
	public void updateLastAndNextUI(int lastNextStatus) {
		if (lastNextStatus == PhotoCommon.UnableLastAndNext){
			mNextStep.setImageResource(R.drawable.forward1);
			mNextStep.setClickable(false);
			mLastStep.setImageResource(R.drawable.cancel1);
			mLastStep.setClickable(false);
		}else if(lastNextStatus == PhotoCommon.AbleLastAndNext){
			mNextStep.setImageResource(R.drawable.forward);
			mNextStep.setClickable(true);
			mLastStep.setImageResource(R.drawable.cancel);
			mLastStep.setClickable(true);
		}else if(lastNextStatus == PhotoCommon.AbleLastUnaleNext){
			mNextStep.setImageResource(R.drawable.forward1);
			mNextStep.setClickable(false);
			mLastStep.setImageResource(R.drawable.cancel);
			mLastStep.setClickable(true);
		}else if(lastNextStatus == PhotoCommon.AbleNextUnableLast){
			mNextStep.setImageResource(R.drawable.forward);
			mNextStep.setClickable(true);
			mLastStep.setImageResource(R.drawable.cancel1);
			mLastStep.setClickable(false);
		}
	}

	@Override
	public void showTempSave() {
		mTempSave.setVisibility(View.VISIBLE);
	}

	@Override
	public void hideTempSave() {
		if(mTempSave.isShown()){
			mTempSave.setVisibility(View.GONE);
		}
	}

	@Override
	public void showReallySave() {
		mReallySave.setVisibility(View.VISIBLE);
	}

	@Override
	public void leftBackClik() {
		if (mRotetaView.isShown() || mHorizontalListView.isShown()){
			exitEditStatus();
		}else{
			pwEditPresenter.judgeIsShowDialog();
		}
	}

	@Override
	public void showIsSaveDialog() {
		pictureWorksDialog.show();
	}

	@Override
	public void showPhotoFrame(ImageLoader imageLoader, DisplayImageOptions options, String framePath) {
		if(!mPhotoFrame.isShown()){
			mPhotoFrame.setVisibility(View.VISIBLE);
		}
		if(!mTempSave.isShown()){
			mTempSave.setVisibility(View.VISIBLE);
		}
		imageLoader.displayImage(framePath, mPhotoFrame, options);
	}

	@Override //隐藏之后显示一个透明层，解决 边框切换闪烁的bug
	public void hidePhotoFrame(ImageLoader imageLoader, DisplayImageOptions options, String framePath) {
		imageLoader.displayImage(framePath, mPhotoFrame, options);
		if(mPhotoFrame.isShown()){
			mPhotoFrame.setVisibility(View.GONE);
		}
	}

	@Override // 显示stickerView 并且 设置可滑动的矩形范围
	public void showPhotoStickerView() {
		if (!mStickerView.isShown()){
			mStickerView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void setPhotoStickerRec(Rect rect) {
		mStickerView.setRec(rect);
	}

	@Override
	public void showPhotoSticker(ImageLoader imageLoader, String stickerPath) {
		mStickerView.addBitImage(imageLoader.loadImageSync(stickerPath)); //本地饰品，所以可以用 同步加载的方法
	}

	@Override
	public void hidePhotoStickerView() {
		if (mStickerView.isShown()) {
			mStickerView.clear();
			mStickerView.setVisibility(View.GONE);
		}
	}

	@Override
	public void ToastShow(int StringId) {
		myToast.setTextAndShow(StringId, Common.TOAST_SHORT_TIME);
	}

	@Override
	public Activity getEditPhotView() {
		return this;
	}

	/**
	 * 进入编辑状态
	 */
	private void onEditStatus(){
		if(mReallySave.isShown()){
			mReallySave.setVisibility(View.GONE);
		}
		mTitle.setVisibility(View.VISIBLE);
		mLastStep.setVisibility(View.GONE);
		mNextStep.setVisibility(View.GONE);
	}

	/**
	 * 返回按钮
	 */
	public void onBackKeyDown(){
		pwEditPresenter.leftBackClik();
	}


	@Override
	public void inOrOutPlace(String locationIds, boolean in) {
		pwEditPresenter.inOrOutPlace(locationIds, in);
	}

	@Override
	public void finishActivity() {
		this.finish();
	}
}
