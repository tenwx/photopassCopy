package com.pictureair.photopass.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.util.AppManager;

/**
 * 1、非第一次使用迪士尼视频 且没有乐拍通照片：介绍页面；
 * 
 * @author bass
 *
 */
public class EditVideoNullPhotoActivity extends BaseActivity implements
		OnClickListener {
	private static final String TAG = "EditVideoNullPhotoActivity";
	private ImageView rl_back;
	private Button btnGoToSelectPhoto;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_video_null_photo);
		initView();
		initEvent();
	}

	private void initView() {
		btnGoToSelectPhoto = (Button) findViewById(R.id.btn_goto_select);
		rl_back = (ImageView) findViewById(R.id.back1);
	}

	private void initEvent() {
		btnGoToSelectPhoto.setOnClickListener(this);
		rl_back.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_goto_select:
			//删除所有aty，只剩下mainTab页面，
			//将mainTab切换到shop Tab
			AppManager.getInstance().killOtherActivity(MainTabActivity.class);
			MainTabActivity.changeToShopTab = true;
		case R.id.back1:
			finish();
			break;
			
		default:
			break;
		}
	}
	

}
