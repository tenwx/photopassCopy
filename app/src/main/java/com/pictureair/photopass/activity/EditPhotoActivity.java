package com.pictureair.photopass.activity;


import android.os.Bundle;
import android.view.KeyEvent;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.pictureair.photopass.editPhoto.controller.PWEditController;
import com.pictureair.photopass.editPhoto.view.PWEditView;

//显示的时候用压缩过的bitmap，合成的时候，用原始的bitmap
public class EditPhotoActivity extends BaseActivity{
	PWEditController pwEditController;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        // 进编辑页即释放掉ImageLoader的缓存，尽量增大可用内存
		ImageLoader.getInstance().clearMemoryCache();

		PWEditView pwEditView = new PWEditView();
		pwEditView.initView(this);

		pwEditController = new PWEditController();
		pwEditController.onCreate(this,pwEditView);

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
			pwEditController.leftBackClik();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
