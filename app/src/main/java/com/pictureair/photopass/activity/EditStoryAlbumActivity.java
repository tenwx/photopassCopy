package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.EditStoryPinnedListViewAdapter;
import com.pictureair.photopass.customDialog.CustomDialog;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.DiscoverLocationItemInfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.widget.CustomProgressBarPop;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.MyToast;
import com.pictureair.photopass.widget.PictureWorksDialog;

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
	private TextView deleteTextView, titleTextView;
	private LinearLayout editBarLinearLayout;
	private CustomProgressBarPop customProgressBarPop;
	private GridView pinnedSectionListView;
	private EditStoryPinnedListViewAdapter editStoryPinnedListViewAdapter;
	private RelativeLayout noCountView;
	private TextView noCountTextView;

	private ArrayList<PhotoInfo> albumArrayList;
	private ArrayList<PhotoInfo> localPhotoArrayList = new ArrayList<>();
	private ArrayList<PhotoInfo> originalAlbumArrayList;
	private ArrayList<PhotoInfo> photoURLlist = new ArrayList<PhotoInfo>();//选择的图片的list
	private ArrayList<DiscoverLocationItemInfo> locationList = new ArrayList<>();

	private static final String TAG = "EditStoryAlbumActivity";
	private final static int DELETEFILE = 12;
	private final static int GET_PHOTOS_DONE = 13;
//	private final static int PROGRESSDIALOG = 0x112;
	private int photoCount = 0;
	private int tabIndex = 0;
	private int currentProgress = 0;
	private int selectCount = 0;
	private MyToast myToast;
	private CustomProgressDialog customProgressDialog;
	private CustomDialog customdialog;
	private PictureAirDbManager pictureAirDbManager;
	private boolean editMode = false;
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
			}
			return false;
		}
	});

//	Handler mhHandler = new Handler(){
//		@Override
//		public void handleMessage(Message msg) {
//			// TODO Auto-generated method stub
//			switch (msg.what) {
//			case DELETEFILE://删除图片操作
////				deleteFileDialog.setProgress(msg.arg1);
//				customProgressBarPop.setProgress(msg.arg1, photoURLlist.size());
//				if (msg.arg1 == photoURLlist.size()) {
//					PictureAirLog.out("has delete all files");
//					Iterator<PhotoInfo> iterator2 = photoURLlist.iterator();
//					while (iterator2.hasNext()) {
//						PictureAirLog.out("scan photoURLlist");
//						PhotoInfo photoInfo = (PhotoInfo) iterator2.next();
//						if (photoInfo.onLine == 0) {//本地图片，需要删除
//							PictureAirLog.out("need remove photo");
//							iterator2.remove();
//						}
//					}
//					currentProgress = 0;
//					Iterator<PhotoItemInfo> iterator = albumArrayList.iterator();
//					while (iterator.hasNext()) {
//						PictureAirLog.out("scan albumArrayList");
//						PhotoItemInfo photoItemInfo = (PhotoItemInfo) iterator.next();
//						if (photoItemInfo.list.size() == 0) {
//							PictureAirLog.out("remove albumArrayList");
//							iterator.remove();
//						}
//					}
//
//					editStoryPinnedListViewAdapter.updateData(albumArrayList);
//					if (photoURLlist.size() == 0) {
//						shareTextView.setEnabled(false);
//						deleteTextView.setEnabled(false);
//						buyTextView.setEnabled(false);
//					}
////					deleteFileDiaPictureAirLog.dismiss();
//					customProgressBarPop.dismiss();
////					removeDialog(msg.arg2);//删除对应ID的dialog
//				}
//				((MyApplication)getApplication()).scanMagicFinish = false;
//				break;
//
//			default:
//				break;
//			}
//		}
//	};

	
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
		//删除图片进度条
		customProgressBarPop = new CustomProgressBarPop(this, findViewById(R.id.editStoryPhotoRelativeLayout), CustomProgressBarPop.TYPE_DELETE);
		customProgressDialog = CustomProgressDialog.show(this, getString(R.string.is_loading), false, null);

		//绑定监听
		backRelativeLayout.setOnClickListener(this);
		deleteTextView.setOnClickListener(this);
		deleteTextView.setEnabled(false);

		//初始化数据
		editMode = getIntent().getStringExtra("mode").equals("edit");//编辑模式
		albumArrayList = new ArrayList<>();
		pictureAirDbManager = new PictureAirDbManager(this);
		sharedPreferences = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
		simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		customProgressDialog.show();
		if (editMode) {
			getPreviewPhotos();
		} else {
			Bundle b = getIntent().getBundleExtra("photos");
			originalAlbumArrayList = b.getParcelableArrayList("photos");
			albumArrayList.addAll(originalAlbumArrayList);
			if (customProgressDialog.isShowing()) {
				customProgressDialog.dismiss();
			}
		}

		titleTextView.setText(editMode ? R.string.edit : R.string.mypage_pp);
//		setListCheckedStatus(editMode);
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
			finish();
			break;

		case R.id.select_delete:
			if (selectCount == 0) {
				myToast.setTextAndShow(R.string.select_photos, Common.TOAST_SHORT_TIME);
				return;
			}

			if (pictureWorksDialog == null){
				pictureWorksDialog = new PictureWorksDialog(EditStoryAlbumActivity.this, null, "msg", "no", "ok", true, editStoryAlbumHandler);
			}


//			boolean hasNetWorkPhoto = false;
//			boolean hasLocalPhoto = false;
//			for (int i = 0; i < photoURLlist.size(); i++) {
//				if (photoURLlist.get(i).onLine == 1) {
//					if (!hasNetWorkPhoto) {
//						hasNetWorkPhoto = true;
//					}
//				}else {
//					if (!hasLocalPhoto) {
//						hasLocalPhoto = true;
//					}
//				}
//			}
//
//			if (hasNetWorkPhoto && hasLocalPhoto) {//如果有网络图片，也有本地照片，弹框提示
//				//初始化dialog
//				customdialog = new CustomDialog.Builder(this)
//				.setMessage(getString(R.string.delete_with_photopass))
//				.setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogOnClickListener())
//				.setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogOnClickListener())
//				.setCancelable(false)
//				.create();
//				customdialog.show();
//			}else if (hasNetWorkPhoto && !hasLocalPhoto) {//只有网络图片，没有本地图片
//				myToast.setTextAndShow(R.string.cannot_delete_in_PhotoPass, Common.TOAST_SHORT_TIME);
//			}else if (!hasNetWorkPhoto && hasLocalPhoto) {//只有本地图片
////				showDialog(PROGRESSDIALOG);
//				customProgressBarPop.show(photoURLlist.size());
//				new Thread(){
//					public void run() {
//						doWork();
////						doWork(PROGRESSDIALOG);
//					};
//				}.start();
//			}
			break;

		default:
			break;
		}
	}
	
		/**
		 * 删除文件的操作，完成比较耗时的操作
		 */
//		private void doWork() {
//			File file;
//			Message message;
//			boolean hasFound = false;
//			ArrayList<PhotoInfo> list;
//			for (int i = 0; i < photoURLlist.size(); i++) {
//
//				//删除图片在arraylist中对应的项
//				if (photoURLlist.get(i).onLine == 0) {//本地图片
//					//删除contentpridiver表中的数据
//					PictureAirLog.out("需要删除的文件为"+photoURLlist.get(i).photoPathOrURL);
//					String params[] = new String[]{photoURLlist.get(i).photoPathOrURL};
//					//删除Media数据库中的对应图片信息
//					PictureAirLog.out("删除Media表中的对应数据");
//					getContentResolver().delete(Media.EXTERNAL_CONTENT_URI, Media.DATA+" like ?", params);
//
////					PictureAirLog.out(",需要删除的索引值----->"+photoURLlist.get(i).index.toString());
//					PictureAirLog.out("arraylist需要移除的文件是"+photoURLlist.get(i).photoPathOrURL);
//					for (int j = 0; j < albumArrayList.size(); j++) {
//						list = new ArrayList<PhotoInfo>();
//						list.addAll(albumArrayList.get(j).list);
//						Iterator<PhotoInfo> iterator = list.iterator();
//						while (iterator.hasNext()) {
//							PhotoInfo photoInfo = iterator.next();
//							if (photoInfo.photoPathOrURL.equals(photoURLlist.get(i).photoPathOrURL)) {
//								iterator.remove();
//								hasFound = true;
//								break;
//							}
//						}
//						if (hasFound) {
//							hasFound = false;
//							albumArrayList.get(j).list = list;
//							break;
//						}else {
//
//						}
//					}
//					//获取需要删除的文件
//					file = new File(photoURLlist.get(i).photoPathOrURL);
//					//删除文件
//					if (file.exists()) {
//						PictureAirLog.out("开始删除文件"+photoURLlist.get(i).photoPathOrURL);
//						//删除文件
//						file.delete();
//						PictureAirLog.out("the file has been deleted");
//					}
//				}
//				currentProgress++;
//				message = mhHandler.obtainMessage();
//				message.what = DELETEFILE;
//				message.arg1 = currentProgress;
////				message.arg2 = id;
////				message.obj = photoURLlist.get(i).photoPathOrURL;
//				mhHandler.sendMessage(message);
//			}
//			PictureAirLog.out("notify");
//		}

//		private class DialogOnClickListener implements DialogInterface.OnClickListener{
//
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				// TODO Auto-generated method stub
//				switch (which) {
//				case DialogInterface.BUTTON_POSITIVE:
//					PictureAirLog.out("ok");
////					showDialog(PROGRESSDIALOG);
//					customProgressBarPop.show(photoURLlist.size());
//					new Thread(){
//						public void run() {
//							doWork();
////							doWork(PROGRESSDIALOG);
//						};
//					}.start();
//					break;
//
//				case DialogInterface.BUTTON_NEGATIVE:
//					PictureAirLog.out("no");
//
//					break;
//
//				default:
//					break;
//				}
//				diaPictureAirLog.dismiss();
//			}
//
//		}

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
						locationList.addAll(AppUtil.getLocation(ACache.get(EditStoryAlbumActivity.this).getAsString(Common.LOCATION_INFO)));
						try {
							albumArrayList.addAll(AppUtil.getSortedAllPhotos(EditStoryAlbumActivity.this, locationList, localPhotoArrayList,
									pictureAirDbManager, simpleDateFormat.format(new Date(cacheTime)),
									simpleDateFormat, MyApplication.getInstance().getLanguageType()));
						} catch (ParseException e) {
							e.printStackTrace();
						}
						break;

					case 1://photopass
						locationList.addAll(AppUtil.getLocation(ACache.get(EditStoryAlbumActivity.this).getAsString(Common.LOCATION_INFO)));
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
						locationList.addAll(AppUtil.getLocation(ACache.get(EditStoryAlbumActivity.this).getAsString(Common.LOCATION_INFO)));
						try {
							albumArrayList.addAll(AppUtil.getSortedPhotoPassPhotos(locationList, pictureAirDbManager,
									simpleDateFormat.format(new Date(cacheTime)), simpleDateFormat, MyApplication.getInstance().getLanguageType(), true));
						} catch (ParseException e) {
							e.printStackTrace();
						}
						break;

					case 4://favourite
						locationList.addAll(AppUtil.getLocation(ACache.get(EditStoryAlbumActivity.this).getAsString(Common.LOCATION_INFO)));
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
