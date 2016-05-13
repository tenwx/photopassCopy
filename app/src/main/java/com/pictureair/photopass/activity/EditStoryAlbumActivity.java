package com.pictureair.photopass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.EditStoryPinnedListViewAdapter;
import com.pictureair.photopass.customDialog.CustomDialog;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.entity.PhotoItemInfo;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.widget.CustomProgressBarPop;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.MyToast;

import java.util.ArrayList;
import java.util.Collections;

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
	
	private ArrayList<PhotoInfo> albumArrayList;
	private ArrayList<PhotoInfo> originalAlbumArrayList;
	private ArrayList<PhotoInfo> photoURLlist = new ArrayList<PhotoInfo>();//选择的图片的list
	private static final String TAG = "EditStoryAlbumActivity";
	private final static int DELETEFILE = 12;
//	private final static int PROGRESSDIALOG = 0x112;
	private int photoCount = 0;
	private int currentProgress = 0;
//	private SharePop sharePop;//分享
	private int shareType = 0;
	private MyToast myToast;
	private CustomProgressDialog customProgressDialog;
	private CustomDialog customdialog;
	private boolean editMode = false;

//	private Handler handler = new Handler(){
//		public void handleMessage(android.os.Message msg) {
//			PictureAirLog.d(TAG, "photo on click");
//			Bundle bundle = msg.getData();
//			switch (bundle.getInt("flag")) {
//			case 10://取消选中的时候
//				deletefromlist(bundle);
//				break;
//
//			case 11://选中的时候，首先判断list的长度，其次判断之前有没有选中过，如果选中过，则不做任何操作，没有选中过，则选中
//				addtolist(bundle);
//				break;
//
//			default:
//				break;
//			}
//		};
//	};
	
//	Handler mhHandler = new Handler(){
//		@Override
//		public void handleMessage(Message msg) {
//			// TODO Auto-generated method stub
//			switch (msg.what) {
//			case DELETEFILE://删除图片操作
////				deleteFileDialog.setProgress(msg.arg1);
//				customProgressBarPop.setProgress(msg.arg1, photoURLlist.size());
//				if (msg.arg1 == photoURLlist.size()) {
//					System.out.println("has delete all files");
//					Iterator<PhotoInfo> iterator2 = photoURLlist.iterator();
//					while (iterator2.hasNext()) {
//						System.out.println("scan photoURLlist");
//						PhotoInfo photoInfo = (PhotoInfo) iterator2.next();
//						if (photoInfo.onLine == 0) {//本地图片，需要删除
//							System.out.println("need remove photo");
//							iterator2.remove();
//						}
//					}
//					currentProgress = 0;
//					Iterator<PhotoItemInfo> iterator = albumArrayList.iterator();
//					while (iterator.hasNext()) {
//						System.out.println("scan albumArrayList");
//						PhotoItemInfo photoItemInfo = (PhotoItemInfo) iterator.next();
//						if (photoItemInfo.list.size() == 0) {
//							System.out.println("remove albumArrayList");
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

		editBarLinearLayout.setVisibility(editMode ? View.VISIBLE : View.GONE);
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
				if (editMode) {
					return;
				}
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
		});
	}
	
//	/**
//	 * 设置列表的编辑状态
//	 * @param isEdit
//	 */
//	private void setListCheckedStatus(boolean isEdit) {
//		photoCount = 0;
//		for (int i = 0; i < albumArrayList.size(); i++) {
//			for (int j = 0; j < albumArrayList.get(i).list.size(); j++) {
//				albumArrayList.get(i).list.get(j).isSelected = 0;
//				if (isEdit) {
//					albumArrayList.get(i).list.get(j).isChecked = 1;
//				}else {
//					albumArrayList.get(i).list.get(j).isChecked = 0;
//				}
//				photoCount++;
//			}
//		}
//	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rlrt:
			finish();
			break;
//
//		case R.id.select_delete:
//			if (photoURLlist.size()==0) {
//				myToast.setTextAndShow(R.string.select_photos, Common.TOAST_SHORT_TIME);
//				return;
//			}
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
//			break;

		default:
			break;
		}
	}
	
	//从list中删除，首先遍历整个list，如果找到目标，则将resultstring设为true，如果找到目标，则将目标删除。
		/**
		 * 删除列表中的项
		 * 1.遍历整个list
		 * 2.如果找到目标，将resultstring设为true
		 * 3.遍历结束之后，将找到的目标删除
		 * 4.更新button的数值
		 * 5.更新适配器
		 * @param bundle
		 */
		private void deletefromlist(Bundle bundle) {
			PictureAirLog.d(TAG, "删除列表中的项");
			PhotoInfo info = bundle.getParcelable("photo");
			if (photoURLlist.contains(info)) {
				PictureAirLog.d(TAG, "找到删除项");
				photoURLlist.remove(info);
			}else {
				PictureAirLog.d(TAG, "找不到删除项");
				
			}
			if (photoURLlist.size() == 0) {
				deleteTextView.setEnabled(false);
			}
			
		}

		//添加到list，首先遍历list，如果能够找到目标，则说明之前已经添加过，所以不需要做什么，如果没有找到目标，则添加到list中
		/**
		 * 添加选择的图片到列表
		 * 1.遍历列表
		 * 2.如果找到目标，说明之前已经添加过，所以不需要任何操作
		 * 3.如果没有找到目标，添加到列表中
		 * 4.通知适配器更新
		 * @param b
		 */
		private void addtolist(Bundle b) {
			PictureAirLog.d(TAG, "添加到已选择的列表");
			PhotoInfo info = b.getParcelable("photo");
			if (photoURLlist.contains(info)) {
				PictureAirLog.out("之前点过了");
			}else {

				PictureAirLog.out("之前没有点过了");
				photoURLlist.add(info);//加入到list中
			}

			//判断是否已经全部选中，如果是，则将标记改为true
			if (photoURLlist.size()==photoCount) {
			}
			if (photoURLlist.size() > 0) {//如果有
				deleteTextView.setEnabled(true);
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
//					System.out.println("需要删除的文件为"+photoURLlist.get(i).photoPathOrURL);
//					String params[] = new String[]{photoURLlist.get(i).photoPathOrURL};
//					//删除Media数据库中的对应图片信息
//					System.out.println("删除Media表中的对应数据");
//					getContentResolver().delete(Media.EXTERNAL_CONTENT_URI, Media.DATA+" like ?", params);
//
////					System.out.println(",需要删除的索引值----->"+photoURLlist.get(i).index.toString());
//					System.out.println("arraylist需要移除的文件是"+photoURLlist.get(i).photoPathOrURL);
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
//						System.out.println("开始删除文件"+photoURLlist.get(i).photoPathOrURL);
//						//删除文件
//						file.delete();
//						System.out.println("the file has been deleted");
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
//			System.out.println("notify");
//		}

		/**
		 * 全选操作
		 * @param arraylist，传入对应的arraylist
		 */
		private void selectall(ArrayList<PhotoItemInfo> arraylist) {
			PictureAirLog.d(TAG, "select all");
			PictureAirLog.out(photoURLlist.size() + "");
			photoURLlist.clear();//每次全选，清空全部数据
			PictureAirLog.out(photoURLlist.size() + "");
			for (int i = 0; i < arraylist.size(); i++) {
				photoURLlist.addAll(arraylist.get(i).list);
			}
			PictureAirLog.out(photoURLlist.size() + "");
		}
		
//		private class DialogOnClickListener implements DialogInterface.OnClickListener{
//
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				// TODO Auto-generated method stub
//				switch (which) {
//				case DialogInterface.BUTTON_POSITIVE:
//					System.out.println("ok");
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
//					System.out.println("no");
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
				int tabIndex = getIntent().getIntExtra("tab", 0);
				long cacheTime = System.currentTimeMillis() - PictureAirDbManager.CACHE_DAY * PictureAirDbManager.DAY_TIME;
				switch (tabIndex) {
					case 0://all
						albumArrayList.addAll(AppUtil.getLocalPhotos(EditStoryAlbumActivity.this, Common.PHOTO_SAVE_PATH, Common.ALBUM_MAGIC));
						Collections.sort(albumArrayList);




						break;

					case 1://photopass
						break;

					case 2://local
						albumArrayList.addAll(AppUtil.getLocalPhotos(EditStoryAlbumActivity.this, Common.PHOTO_SAVE_PATH, Common.ALBUM_MAGIC));
						Collections.sort(albumArrayList);
						break;

					case 3://bought
						break;

					case 4://favourite
						break;

					default:
						break;
				}






//				if (tabName.equals("all")) {//获取全部照片
//					locationList.addAll(AppUtil.getLocation(ACache.get(PreviewPhotoActivity.this).getAsString(Common.LOCATION_INFO)));
//					try {
//						photolist.addAll(AppUtil.getSortedAllPhotos(PreviewPhotoActivity.this, locationList, targetphotolist,
//								pictureAirDbManager, simpleDateFormat.format(new Date(cacheTime)),
//								simpleDateFormat, MyApplication.getInstance().getLanguageType()));
//					} catch (ParseException e) {
//						e.printStackTrace();
//					}
//
//				} else if (tabName.equals("photopass")) {//获取pp图片
//					getLocation();
//					try {
//						photolist.addAll(AppUtil.getSortedPhotoPassPhotos(locationList, pictureAirDbManager,
//								simpleDateFormat.format(new Date(cacheTime)), simpleDateFormat, MyApplication.getInstance().getLanguageType(), false));
//					} catch (ParseException e) {
//						e.printStackTrace();
//					}
//
//				} else if (tabName.equals("local")) {//获取本地图片
//					photolist.addAll(targetphotolist);
//
//				} else if (tabName.equals("bought")) {//获取已经购买的图片
//					getLocation();
//					try {
//						photolist.addAll(AppUtil.getSortedPhotoPassPhotos(locationList, pictureAirDbManager,
//								simpleDateFormat.format(new Date(cacheTime)), simpleDateFormat, MyApplication.getInstance().getLanguageType(), true));
//					} catch (ParseException e) {
//						e.printStackTrace();
//					}
//
//				} else if (tabName.equals("favourite")) {//获取收藏图片
//					photolist.addAll(AppUtil.insterSortFavouritePhotos(
//							pictureAirDbManager.getFavoritePhotoInfoListFromDB(sharedPreferences.getString(Common.USERINFO_ID, ""), simpleDateFormat.format(new Date(cacheTime)), locationList, MyApplication.getInstance().getLanguageType())));
//
//				} else {//获取列表图片
//					ArrayList<PhotoInfo> temp = bundle.getParcelableArrayList("photos");//获取图片路径list
//					photolist.addAll(temp);
//				}
//
//				if (currentPosition == -1) {//购买图片后返回
//					String photoId = bundle.getString("photoId");
//					for (int i = 0; i < photolist.size(); i++) {
//						if (photolist.get(i).photoId.equals(photoId)){
//							photolist.get(i).isPayed = 1;
//							currentPosition = i;
//							break;
//						}
//					}
//				}
//
//				if (currentPosition == -2) {//绑定PP后返回
//					String ppsStr = bundle.getString("ppsStr");
//					refreshPP(photolist,ppsStr);
//					currentPosition = sharedPreferences.getInt("currentPosition",0);
//				}
//
//				if (currentPosition < 0) {
//					currentPosition = 0;
//				}
//				PhotoInfo currentPhotoInfo = photolist.get(currentPosition);
//
//				Iterator<PhotoInfo> photoInfoIterator = photolist.iterator();
//				while (photoInfoIterator.hasNext()) {
//					PhotoInfo info = photoInfoIterator.next();
//					if (info.isVideo == 1) {
//						photoInfoIterator.remove();
//					}
//				}
//				currentPosition = photolist.indexOf(currentPhotoInfo);
//				previewPhotoHandler.sendEmptyMessage(7);
			}
		}.start();
	}
}
