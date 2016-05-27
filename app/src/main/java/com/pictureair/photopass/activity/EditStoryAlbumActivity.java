package com.pictureair.photopass.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images.Media;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.EditStoryPinnedListViewAdapter;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.DiscoverLocationItemInfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.UmengUtil;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.MyToast;
import com.pictureair.photopass.widget.PictureWorksDialog;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

/**
 * 编辑story中的相册页面
 * @author bauer_bao
 *
 */
public class EditStoryAlbumActivity extends BaseActivity implements OnClickListener{
	private ImageView backRelativeLayout;
	private TextView deleteTextView, titleTextView, editTextView;
	private LinearLayout editBarLinearLayout;
	private GridView pinnedSectionListView;
	private EditStoryPinnedListViewAdapter editStoryPinnedListViewAdapter;
	private RelativeLayout noCountView;
	private TextView noCountTextView;

	private ArrayList<PhotoInfo> albumArrayList;
	private ArrayList<PhotoInfo> localPhotoArrayList = new ArrayList<>();
	private ArrayList<PhotoInfo> originalAlbumArrayList;
	private ArrayList<PhotoInfo> photopassPhotoslist = new ArrayList<>();//选择的网络图片的list
	private ArrayList<PhotoInfo> localPhotoslist = new ArrayList<>();//选择的本地图片的list
	private ArrayList<DiscoverLocationItemInfo> locationList = new ArrayList<>();

	private final static int GET_PHOTOS_DONE = 13;
	private final static int START_DELETE_NETWORK_PHOTOS = 14;
	private final static int DELETE_LOCAL_PHOTOS_DONE = 15;
	private int tabIndex = 0;
	private int selectCount = 0;
	private MyToast myToast;
	private CustomProgressDialog customProgressDialog;
	private PictureAirDbManager pictureAirDbManager;
	private boolean editMode = false;
	private boolean deleteLocalPhotoDone = false;
	private boolean deleteNetPhotoDone = false;
	private boolean netWorkFailed = false;
	private String ppCode;
	private SimpleDateFormat simpleDateFormat;
	private SharedPreferences sharedPreferences;
	private PictureWorksDialog pictureWorksDialog;

	private Handler editStoryAlbumHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
				case GET_PHOTOS_DONE://获取图片成功
					if (customProgressDialog.isShowing()) {
						customProgressDialog.dismiss();
					}
					if (albumArrayList.size() == 0){
						noCountView.setVisibility(View.VISIBLE);
						switch (tabIndex) {
							case 0:
								break;

							case 1://PP
								noCountTextView.setText(R.string.no_photo_in_airpass);
								break;

							case 2://local
								noCountTextView.setText(R.string.no_photo_in_magiccam);
								break;

							case 3://bought
								noCountTextView.setText(R.string.no_photo_in_bought);
								break;

							case 4://favorite
								noCountTextView.setText(R.string.no_photo_in_favourite);
								break;

							default:
								break;
						}
					} else {//有数据
						editBarLinearLayout.setVisibility(View.VISIBLE);
					}
					editStoryPinnedListViewAdapter.notifyDataSetChanged();
					break;

				case START_DELETE_NETWORK_PHOTOS://开始删除网络图片
					JSONArray ids = new JSONArray();
					for (int i = 0; i < photopassPhotoslist.size(); i++) {
						ids.add(photopassPhotoslist.get(i).photoId);
					}
					PictureAirLog.out("ids---->" + ids);
					PictureAirLog.out("ppCode---->" + ppCode);
					API1.removePhotosFromPP(MyApplication.getTokenId(), ids, ppCode, editStoryAlbumHandler);
					break;

				case API1.DELETE_PHOTOS_FAILED://判断本地图片是否删除完毕，并且更具有没有本地图片而显示不同的提示
					//需要处理
					deleteNetPhotoDone = true;
					netWorkFailed = true;
					if (deleteLocalPhotoDone) {
						dealAfterDeleted();
					}
					break;

				case API1.DELETE_PHOTOS_SUCCESS://判断本地图片是否删除完毕
					/**
					 * 1.删除列表内的数据
					 * 2.判断本地数据是否处理完毕
					 */
					netWorkFailed = false;
					//删除本地列表数据操作
					deleteNetworkPhotos();
					deleteNetPhotoDone = true;
					selectCount -= photopassPhotoslist.size();

					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putBoolean(Common.IS_DELETED_PHOTO_FROM_PP, true);
					editor.commit();

					if (deleteLocalPhotoDone) {
						dealAfterDeleted();
					}
					break;

				case DELETE_LOCAL_PHOTOS_DONE://本地文件删除完成
					deleteLocalPhotoDone = true;
					((MyApplication)getApplication()).scanMagicFinish = false;
					selectCount -= localPhotoslist.size();
					if(deleteNetPhotoDone) {
						dealAfterDeleted();
					}
					break;

				case DialogInterface.BUTTON_POSITIVE://对话框点击确认按钮
					UmengUtil.onEvent(EditStoryAlbumActivity.this,Common.EVENT_ONCLICK_DEL_PHOTO); //统计点删除的事件。（友盟）
					if (!customProgressDialog.isShowing()){
						customProgressDialog.show();
					}
					new Thread() {
						public void run() {
							photopassPhotoslist.clear();
							localPhotoslist.clear();
							deleteLocalPhotoDone = false;
							deleteNetPhotoDone = false;
							for (int i = 0; i < albumArrayList.size(); i++) {
								if (albumArrayList.get(i).isSelected == 1) {//选中的照片
									if (albumArrayList.get(i).onLine == 1) {//网络照片
										photopassPhotoslist.add(albumArrayList.get(i));
									} else {
										localPhotoslist.add(albumArrayList.get(i));
									}
								}
							}

							if (photopassPhotoslist.size() > 0) {
								editStoryAlbumHandler.sendEmptyMessage(START_DELETE_NETWORK_PHOTOS);
							} else {
								deleteNetPhotoDone = true;
							}

							if (localPhotoslist.size() > 0) {
								deleteLocalPhotos();
							} else {
								deleteLocalPhotoDone = true;
							}
						}
					}.start();
					break;

				default:
					break;
			}
			return false;
		}
	});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_story_photo);

		//find控件
		backRelativeLayout = (ImageView) findViewById(R.id.rlrt);
		deleteTextView = (TextView) findViewById(R.id.select_delete);
		editBarLinearLayout = (LinearLayout) findViewById(R.id.select_tools_linearlayout);
		pinnedSectionListView = (GridView) findViewById(R.id.pullToRefreshPinnedSectionListView);
		titleTextView = (TextView) findViewById(R.id.text);
		noCountView = (RelativeLayout) findViewById(R.id.no_photo_relativelayout);
		noCountTextView = (TextView) findViewById(R.id.no_photo_textView);
		editTextView = (TextView) findViewById(R.id.pp_photos_edit);
		//删除图片进度条
		customProgressDialog = CustomProgressDialog.create(this, getString(R.string.is_loading), false, null);

		//绑定监听
		backRelativeLayout.setOnClickListener(this);
		deleteTextView.setOnClickListener(this);
		deleteTextView.setEnabled(false);
		editTextView.setOnClickListener(this);

		//初始化数据
		albumArrayList = new ArrayList<>();
		pictureAirDbManager = new PictureAirDbManager(this);
		sharedPreferences = getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, MODE_PRIVATE);
		simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ppCode = getIntent().getStringExtra("ppCode");
		Bundle b = getIntent().getBundleExtra("photos");
		originalAlbumArrayList = b.getParcelableArrayList("photos");
		albumArrayList.addAll(originalAlbumArrayList);

		editStoryPinnedListViewAdapter = new EditStoryPinnedListViewAdapter(this, editMode, albumArrayList);//
		pinnedSectionListView.setAdapter(editStoryPinnedListViewAdapter);
		myToast = new MyToast(this);
		
		pinnedSectionListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
										   int position, long id) {
				return true;
			}
		});

		pinnedSectionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				if (editMode) {//编辑模式，需要选中效果
					itemOnClick(position, view);
				} else {//预览模式，点击进入大图预览
					PictureAirLog.out("select" + position);
					Intent i = new Intent();
					i.setClass(EditStoryAlbumActivity.this, PreviewPhotoActivity.class);
					Bundle bundle = new Bundle();
					bundle.putInt("position", position);
					bundle.putString("tab", "other");
					bundle.putParcelableArrayList("photos", albumArrayList);
					i.putExtra("bundle", bundle);
					startActivity(i);
				}
			}
		});
	}

	/**
	 * 选中或取消处理
	 * @param position
	 * @param view
     */
	private void itemOnClick(int position, View view) {
		PhotoInfo info = albumArrayList.get(position);
		PictureAirLog.out("select" + position);
		EditStoryPinnedListViewAdapter.ViewHolder viewHolder = (EditStoryPinnedListViewAdapter.ViewHolder) view.getTag();
		//选择事件
		if (info.isSelected == 1) {//取消选择
			selectCount--;
			info.isSelected = 0;
			info.showMask = 0;
			viewHolder.selectImageView.setImageResource(R.drawable.sel3);
			viewHolder.maskImageView.setVisibility(View.GONE);
		} else {//选择
			selectCount++;
			info.isSelected = 1;
			info.showMask = 1;
			viewHolder.selectImageView.setImageResource(R.drawable.sel2);
			viewHolder.maskImageView.setVisibility(View.VISIBLE);
		}
		if (selectCount == 0) {
			deleteTextView.setEnabled(false);
		} else {
			deleteTextView.setEnabled(true);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.rlrt:
				returnBack();
				break;

			case R.id.select_delete:
				if (selectCount == 0) {
					myToast.setTextAndShow(R.string.select_photos, Common.TOAST_SHORT_TIME);
					return;
				}

				if (pictureWorksDialog == null) {
					pictureWorksDialog = new PictureWorksDialog(EditStoryAlbumActivity.this, null,
							getString(R.string.start_delete), getString(R.string.button_cancel), getString(R.string.reset_pwd_ok), true, editStoryAlbumHandler);
				}
				pictureWorksDialog.show();
				break;

			case R.id.pp_photos_edit:
				UmengUtil.onEvent(EditStoryAlbumActivity.this,Common.EVENT_ONCLICK_EDIT_PHOTO); //统计点 编辑时候的事件（友盟）
				editMode = true;
				editStoryPinnedListViewAdapter.setEditMode(editMode);
				editBarLinearLayout.setVisibility(View.VISIBLE);
				titleTextView.setText(R.string.edit_story_album);
				deleteTextView.setEnabled(false);
				editTextView.setVisibility(View.GONE);
				break;

			default:
				break;
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			returnBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void returnBack() {
		if (editMode) {
			editMode = false;
			editBarLinearLayout.setVisibility(View.GONE);
			editStoryPinnedListViewAdapter.setEditMode(editMode);
			titleTextView.setText(R.string.mypage_pp);
			if (selectCount > 0) {
				for (int i = 0; i < albumArrayList.size(); i++) {
					if (albumArrayList.get(i).isSelected == 1) {
						albumArrayList.get(i).isSelected = 0;
						albumArrayList.get(i).showMask = 0;
					}
				}
				selectCount = 0;
			}
			editTextView.setVisibility(View.VISIBLE);
		} else {
			finish();
		}
	}

	/**
	 * 删除本地列表的数据
	 */
	private void deleteNetworkPhotos() {
		/**
		 * 1.删除数据库的操作（照片表和收藏表都要删除），同时需要判断是否输入多张PP卡
		 * 2.删除本地列表操作
		 */
		pictureAirDbManager.deletePhotosFromPhotoInfoAndFavorite(photopassPhotoslist, ppCode + ",");

		for (int i = 0; i < photopassPhotoslist.size(); i++) {
			Iterator<PhotoInfo> iterator = albumArrayList.iterator();
			while (iterator.hasNext()) {
				PhotoInfo photoInfo = iterator.next();
				if (photoInfo.photoPathOrURL.equals(photopassPhotoslist.get(i).photoPathOrURL)) {
					iterator.remove();
					break;
				}
			}
		}
	}

	/**
	 * 删除文件的操作，完成比较耗时的操作
	 */
	private void deleteLocalPhotos() {
		File file;
		for (int i = 0; i < localPhotoslist.size(); i++) {
			//删除contentpridiver表中的数据
			PictureAirLog.out("需要删除的文件为" + localPhotoslist.get(i).photoPathOrURL);
			String params[] = new String[]{localPhotoslist.get(i).photoPathOrURL};
			//删除Media数据库中的对应图片信息
			PictureAirLog.out("删除Media表中的对应数据");
			getContentResolver().delete(Media.EXTERNAL_CONTENT_URI, Media.DATA + " like ?", params);

			//获取需要删除的文件
			file = new File(localPhotoslist.get(i).photoPathOrURL);
			//删除文件
			if (file.exists()) {
				PictureAirLog.out("开始删除文件" + localPhotoslist.get(i).photoPathOrURL);
				//删除文件
				file.delete();
				PictureAirLog.out("the file has been deleted");
			}

			PictureAirLog.out("arraylist需要移除的文件是" + localPhotoslist.get(i).photoPathOrURL);
			Iterator<PhotoInfo> iterator = albumArrayList.iterator();
			while (iterator.hasNext()) {
				PhotoInfo photoInfo = iterator.next();
				if (photoInfo.photoPathOrURL.equals(localPhotoslist.get(i).photoPathOrURL)) {
					iterator.remove();
					break;
				}
			}
		}
		editStoryAlbumHandler.sendEmptyMessage(DELETE_LOCAL_PHOTOS_DONE);
	}

	/**
	 * 删除之后的处理
	 */
	private void dealAfterDeleted() {
		if (selectCount == 0) {
			deleteTextView.setEnabled(false);
		}
		editStoryPinnedListViewAdapter.notifyDataSetChanged();
		if (photopassPhotoslist.size() > 0 && localPhotoslist.size() > 0) {//如果既有本地图片，又有网络图片
			if (netWorkFailed) {//网络图片删除失败
				//需要弹出提示，这种提示需要特别处理，暂时还没有处理
//				myToast.setTextAndShow("本地文件删除成功，网络图片删除失败", Common.TOAST_SHORT_TIME);
			}
		} else if (photopassPhotoslist.size() > 0 && localPhotoslist.size() == 0) {
			if (netWorkFailed) {//网络图片删除失败
				myToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
			}
		}

		if (albumArrayList.size() == 0) {//全部删除，需要显示无图页面
			editMode = false;//需要取消编辑模式
			noCountView.setVisibility(View.VISIBLE);
			noCountTextView.setText(R.string.no_photo_in_airpass);
		}

		if (customProgressDialog.isShowing()) {
			customProgressDialog.dismiss();
		}
	}

	/**
	 * 获取预览图片
	 */
	private void getPreviewPhotos() {
		new Thread() {
			@Override
			public void run() {
				super.run();
				tabIndex = getIntent().getIntExtra("tab", 0);
				long cacheTime = System.currentTimeMillis() - PictureAirDbManager.CACHE_DAY * PictureAirDbManager.DAY_TIME;
				switch (tabIndex) {
					case 0://all
						localPhotoArrayList.addAll(AppUtil.getLocalPhotos(EditStoryAlbumActivity.this, Common.PHOTO_SAVE_PATH, Common.ALBUM_MAGIC));
						Collections.sort(localPhotoArrayList);
						locationList.addAll(AppUtil.getLocation(EditStoryAlbumActivity.this, ACache.get(EditStoryAlbumActivity.this).getAsString(Common.LOCATION_INFO), true));
						try {
							albumArrayList.addAll(AppUtil.getSortedAllPhotos(EditStoryAlbumActivity.this, locationList, localPhotoArrayList,
									pictureAirDbManager, simpleDateFormat.format(new Date(cacheTime)),
									simpleDateFormat, MyApplication.getInstance().getLanguageType()));
						} catch (ParseException e) {
							e.printStackTrace();
						}
						break;

					case 1://photopass
						locationList.addAll(AppUtil.getLocation(EditStoryAlbumActivity.this, ACache.get(EditStoryAlbumActivity.this).getAsString(Common.LOCATION_INFO), true));
						try {
							albumArrayList.addAll(AppUtil.getSortedPhotoPassPhotos(locationList, pictureAirDbManager,
									simpleDateFormat.format(new Date(cacheTime)), simpleDateFormat, MyApplication.getInstance().getLanguageType(), false));
						} catch (ParseException e) {
							e.printStackTrace();
						}
						break;

					case 2://local
						ArrayList<PhotoInfo> localList = AppUtil.getLocalPhotos(EditStoryAlbumActivity.this, Common.PHOTO_SAVE_PATH, Common.ALBUM_MAGIC);
						Collections.sort(localList);
						try {
							localPhotoArrayList.addAll(AppUtil.startSortForPinnedListView(AppUtil.getMagicItemInfoList(EditStoryAlbumActivity.this, simpleDateFormat, localList)));
						} catch (ParseException e) {
							e.printStackTrace();
						}
						albumArrayList.addAll(localPhotoArrayList);
						break;

					case 3://bought
						locationList.addAll(AppUtil.getLocation(EditStoryAlbumActivity.this, ACache.get(EditStoryAlbumActivity.this).getAsString(Common.LOCATION_INFO), true));
						try {
							albumArrayList.addAll(AppUtil.getSortedPhotoPassPhotos(locationList, pictureAirDbManager,
									simpleDateFormat.format(new Date(cacheTime)), simpleDateFormat, MyApplication.getInstance().getLanguageType(), true));
						} catch (ParseException e) {
							e.printStackTrace();
						}
						break;

					case 4://favourite
						locationList.addAll(AppUtil.getLocation(EditStoryAlbumActivity.this, ACache.get(EditStoryAlbumActivity.this).getAsString(Common.LOCATION_INFO), true));
						albumArrayList.addAll(AppUtil.insterSortFavouritePhotos(
								pictureAirDbManager.getFavoritePhotoInfoListFromDB(EditStoryAlbumActivity.this, sharedPreferences.getString(Common.USERINFO_ID, ""), simpleDateFormat.format(new Date(cacheTime)), locationList, MyApplication.getInstance().getLanguageType())));
						break;

					default:
						break;
				}

				Iterator<PhotoInfo> photoInfoIterator = albumArrayList.iterator();
				while (photoInfoIterator.hasNext()) {
					PhotoInfo info = photoInfoIterator.next();
					if (info.isVideo == 1) {
						photoInfoIterator.remove();
					}
				}
				editStoryAlbumHandler.sendEmptyMessage(GET_PHOTOS_DONE);
			}
		}.start();
	}
}
