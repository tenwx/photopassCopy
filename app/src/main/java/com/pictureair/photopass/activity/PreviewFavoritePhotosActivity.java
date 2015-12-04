package com.pictureair.photopass.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.handmark.pulltorefresh.library.PullToRefreshPinnedSectionListView;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.PreviewFavoritePhotosPinnedListViewAdapter;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.PhotoItemInfo;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.NoNetWorkOrNoCountView;

import java.util.ArrayList;
import java.util.Collections;

/**
 * 编辑story中的相册页面
 * @author bauer_bao
 *
 */
public class PreviewFavoritePhotosActivity extends Activity implements OnClickListener{
	private ImageView backRelativeLayout;
	private PullToRefreshPinnedSectionListView pinnedSectionListView;
	private PreviewFavoritePhotosPinnedListViewAdapter editStoryPinnedListViewAdapter;
	
	private ArrayList<PhotoItemInfo> albumArrayList;
	private ArrayList<PhotoItemInfo> originalAlbumArrayList;
	private static final String TAG = "PreviewFavoritePhotosActivity";
	private static final int SCAN_FAVORITE_PHOTO_DONE = 1;
	private PictureAirDbManager pictureAirDbManager;
	private CustomProgressDialog dialog;
	private NoNetWorkOrNoCountView noFavoritePhoto;
	private SharedPreferences sharedPreferences;
	private boolean needReScan = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preview_love_photo);
		AppManager.getInstance().addActivity(this);
		pictureAirDbManager = new PictureAirDbManager(this);
		//find控件
		backRelativeLayout = (ImageView) findViewById(R.id.rlrt);
		pinnedSectionListView = (PullToRefreshPinnedSectionListView) findViewById(R.id.pullToRefreshPinnedSectionListView);
		pinnedSectionListView.setPullToRefreshEnabled(false);
		noFavoritePhoto = (NoNetWorkOrNoCountView)findViewById(R.id.no_favorite_photo);
		
		//绑定监听
		backRelativeLayout.setOnClickListener(this);
		
		//初始化数据
		Bundle b = getIntent().getBundleExtra("photos");
		albumArrayList = new ArrayList<PhotoItemInfo>();
		originalAlbumArrayList = b.getParcelableArrayList("photos");
		editStoryPinnedListViewAdapter = new PreviewFavoritePhotosPinnedListViewAdapter(this, albumArrayList, ((MyApplication)getApplication()).magicPicList, handler);
		pinnedSectionListView.setAdapter(editStoryPinnedListViewAdapter);
		sharedPreferences = getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
		dialog = CustomProgressDialog.show(this, getString(R.string.is_loading), false, null);
		noFavoritePhoto.setResult(0, R.string.no_favorite_photo, 0, R.drawable.no_favorite_photo, handler, false);
		
		//开线程检索所有的已经收藏了的图片
		new Thread(){
			public void run() {
				albumArrayList = pictureAirDbManager.checkLovePhotos(originalAlbumArrayList, sharedPreferences.getString(Common.USERINFO_ID, ""));
				Collections.sort(albumArrayList);
				handler.sendEmptyMessage(SCAN_FAVORITE_PHOTO_DONE);
			};
		}.start();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (needReScan) {
			needReScan = false;
			System.out.println("need rescan");
			new Thread(){
				public void run() {
					albumArrayList = pictureAirDbManager.checkLovePhotos(albumArrayList, sharedPreferences.getString(Common.USERINFO_ID, ""));
					Collections.sort(albumArrayList);
					handler.sendEmptyMessage(SCAN_FAVORITE_PHOTO_DONE);
				};
			}.start();
		}
	}
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SCAN_FAVORITE_PHOTO_DONE://检测收藏图片成功
				editStoryPinnedListViewAdapter.updateData(albumArrayList);
				if (albumArrayList.size() == 0) {//为空
					noFavoritePhoto.setVisibility(View.VISIBLE);
				}else {
					noFavoritePhoto.setVisibility(View.GONE);
				}
				
				if (dialog.isShowing()) {
					dialog.dismiss();
				}
				break;
				
			case PreviewFavoritePhotosPinnedListViewAdapter.NEED_RESCAN:
				needReScan = true;
				break;

			default:
				break;
			}
		};
	};
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		AppManager.getInstance().killActivity(this);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rlrt:
			finish();
			break;
			
		default:
			break;
		}
	}
		
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

}
