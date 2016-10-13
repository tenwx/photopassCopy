package com.pictureair.photopass.activity;


import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.EditActivityAdapter;
import com.pictureair.photopass.editPhoto.presenter.PWEditPresenter;
import com.pictureair.photopass.editPhoto.interf.IPWEditView;
import com.pictureair.photopass.editPhoto.util.PhotoCommon;
import com.pictureair.photopass.editPhoto.widget.StickerView;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.HorizontalListView;
import com.pictureair.photopass.widget.PWToast;
import com.pictureair.photopass.widget.PictureWorksDialog;

//显示的时候用压缩过的bitmap，合成的时候，用原始的bitmap
public class EditPhotoActivity extends BaseActivity implements View.OnClickListener, IPWEditView {
	PWEditPresenter pwEditPresenter;
	private CustomProgressDialog dialog; // Loading
	private PWToast myToast;
	private ImageView mLeftBack,back;
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
		back = (ImageView) findViewById(R.id.edit_return);
		mLeftBack = (ImageView) findViewById(R.id.btn_left_back);
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

		back.setOnClickListener(this);
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
		dialogShow();
		pwEditPresenter = new PWEditPresenter();
		pwEditPresenter.onCreate(this);
		pictureWorksDialog = new PictureWorksDialog(this, null, getString(R.string.exit_hint), getString(R.string.button_cancel), getString(R.string.button_ok), true, pwEditPresenter.getHandler());

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		pwEditPresenter.locationOnResume();

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		pwEditPresenter.locationOnPause();
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
				pwEditPresenter.finishActivity();
				break;
			case R.id.btn_left_back:
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
				pwEditPresenter.saveTempPhoto();
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
	}

	@Override // 只要是加载了Bitmap，说明进行了编辑，就显示保存按钮。
	public void showBitmap(Bitmap bitmap) {
		mMainImage.setImageBitmap(bitmap);
	}

	@Override
	public void showTempSave() {
		if (!mTempSave.isShown()) {
			mTempSave.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void hideTempSave() {
		if(mTempSave.isShown()){
			mTempSave.setVisibility(View.GONE);
		}
	}

	@Override
	public void showReallySave() {
		if (!mReallySave.isShown()) {
			mReallySave.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void hideReallySave() {
		if (mReallySave.isShown()) {
			mReallySave.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void showIsSaveDialog() {
		pictureWorksDialog.show();
	}

	@Override
	public void showPhotoFrame() {
		if(!mPhotoFrame.isShown()){
			mPhotoFrame.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void hidePhotoFrame() {
		if(mPhotoFrame.isShown()){
			mPhotoFrame.setVisibility(View.GONE);
		}
	}

	@Override 
	public void showPhotoStickerView() {
		if (!mStickerView.isShown()){
			mStickerView.setVisibility(View.VISIBLE);
		}
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
	@Override
	public void onEditStatus(){
		if(mReallySave.isShown()){
			mReallySave.setVisibility(View.GONE);
		}
		mTitle.setVisibility(View.VISIBLE);
		mLastStep.setVisibility(View.GONE);
		mNextStep.setVisibility(View.GONE);
		back.setVisibility(View.GONE);
		mLeftBack.setVisibility(View.VISIBLE);
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
		//字体的编辑条消失。
		mNextStep.setVisibility(View.VISIBLE);
		mLastStep.setVisibility(View.VISIBLE);
		mLeftBack.setVisibility(View.GONE);
		back.setVisibility(View.VISIBLE);
	}

	@Override
	public ImageView getFrameImageView() {
		return mPhotoFrame;
	}

	@Override
	public ImageView getMainImageView() {
		return mMainImage;
	}

	@Override
	public StickerView getStickView() {
		return mStickerView;
	}

	@Override
	public void enableNextBtnClick() {
		mNextStep.setImageResource(R.drawable.forward);
		mNextStep.setClickable(true);
	}

	@Override
	public void disableNextBtnClick() {
		mNextStep.setImageResource(R.drawable.forward1);
		mNextStep.setClickable(false);
	}


	@Override
	public void enableBackBtnClick() {
		mLastStep.setImageResource(R.drawable.cancel);
		mLastStep.setClickable(true);
	}

	@Override
	public void disableBackBtnClick() {
		mLastStep.setImageResource(R.drawable.cancel1);
		mLastStep.setClickable(false);
	}

	/**
	 * 返回按钮
	 */
	public void onBackKeyDown(){
		if (mRotetaView.isShown() || mHorizontalListView.isShown()){
			pwEditPresenter.leftBackClik();
		}else{
			pwEditPresenter.finishActivity();
		}
	}

	@Override
	public void finishActivity() {
		this.finish();
	}
}
