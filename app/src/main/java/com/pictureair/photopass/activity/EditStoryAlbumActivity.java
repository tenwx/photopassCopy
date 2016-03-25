package com.pictureair.photopass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.EditStoryPinnedListViewAdapter;
import com.pictureair.photopass.customDialog.CustomDialog;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.entity.PhotoItemInfo;
import com.pictureair.photopass.widget.CustomProgressBarPop;
import com.pictureair.photopass.widget.MyToast;

import java.util.ArrayList;

/**
 * 编辑story中的相册页面
 * @author bauer_bao
 *
 */
public class EditStoryAlbumActivity extends BaseActivity implements OnClickListener{
	private ImageView backRelativeLayout;
	private ImageView editPhotoImageView;
	private TextView selectAllTextView;
	private TextView shareTextView;
	private TextView deleteTextView;
	private TextView buyTextView;
	private TextView selectDisAllTextView;
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
	private CustomDialog customdialog;
	private boolean editMode = false;
	private ImageView footerView;//防止点击编辑的时候，listview会跳动，所以加了一个footerView，绕道解决了跳动的问题
	private int bottomBarHeight = 0;//记录底部编辑栏的高度
	
//	private Handler handler = new Handler(){
//		public void handleMessage(android.os.Message msg) {
//			Log.d(TAG, "photo on click");
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
////					deleteFileDialog.dismiss();
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
		selectAllTextView = (TextView) findViewById(R.id.select_all);
		shareTextView = (TextView) findViewById(R.id.select_share);
		deleteTextView = (TextView) findViewById(R.id.select_delete);
		buyTextView = (TextView) findViewById(R.id.select_makegift);
		selectDisAllTextView = (TextView) findViewById(R.id.select_disall);
		editBarLinearLayout = (LinearLayout) findViewById(R.id.select_tools_linearlayout);
		pinnedSectionListView = (GridView) findViewById(R.id.pullToRefreshPinnedSectionListView);
		editPhotoImageView = (ImageView) findViewById(R.id.imageButton_edit);
		//删除图片进度条
		customProgressBarPop = new CustomProgressBarPop(this, findViewById(R.id.editStoryPhotoRelativeLayout), CustomProgressBarPop.TYPE_DELETE);
		
		//绑定监听
		backRelativeLayout.setOnClickListener(this);
		selectAllTextView.setOnClickListener(this);
		shareTextView.setOnClickListener(this);
		deleteTextView.setOnClickListener(this);
		buyTextView.setOnClickListener(this);
		selectDisAllTextView.setOnClickListener(this);
		
		shareTextView.setEnabled(false);
		deleteTextView.setEnabled(false);
		buyTextView.setEnabled(false);
		selectDisAllTextView.setVisibility(View.GONE);
		selectAllTextView.setVisibility(View.VISIBLE);
		
		//初始化数据
		Bundle b = getIntent().getBundleExtra("photos");
		albumArrayList = new ArrayList<>();
		originalAlbumArrayList = b.getParcelableArrayList("photos");
		albumArrayList.addAll(originalAlbumArrayList);
//		if (getIntent().getStringExtra("mode").equals("edit")) {//编辑模式
//			editMode = true;
//			editPhotoImageView.setVisibility(View.GONE);
//		}else if (getIntent().getStringExtra("mode").equals("noedit")) {//非编辑模式
//			editMode = false;
//
//			editPhotoImageView.setOnClickListener(this);
//		}
//		setListCheckedStatus(editMode);
		editStoryPinnedListViewAdapter = new EditStoryPinnedListViewAdapter(this, editMode, albumArrayList);//
		pinnedSectionListView.setAdapter(editStoryPinnedListViewAdapter);
//		sharePop = new SharePop(this);
		myToast = new MyToast(this);
		
		footerView = new ImageView(EditStoryAlbumActivity.this);
		
		ViewTreeObserver viewTreeObserver = editBarLinearLayout.getViewTreeObserver();
		viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				// TODO Auto-generated method stub
				editBarLinearLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				bottomBarHeight = editBarLinearLayout.getHeight();
				System.out.println("editBarLinearLayout height is "+ editBarLinearLayout.getHeight());
				LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, bottomBarHeight);
				footerView.setLayoutParams(layoutParams);
				if (!editMode) {
					editBarLinearLayout.setVisibility(View.GONE);
				}else {
//					pinnedSectionListView.getRefreshableView().addFooterView(footerView);
				}
			}
		});

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
				System.out.println("select" + position);
				Intent i = new Intent();

				i.setClass(EditStoryAlbumActivity.this, PreviewPhotoActivity.class);
				i.putExtra("activity", "EditStoryAlbumActivity");
				i.putExtra("position", position);//在那个相册中的位置
				i.putExtra("photoId", albumArrayList.get(position).photoId);
				i.putExtra("photos", albumArrayList);//那个相册的全部图片路径
				i.putExtra("targetphotos", ((MyApplication) getApplication()).magicPicList);
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
			
//		case R.id.select_disall:
//			//全取消操作
//			System.out.println("disselect all");
//			selectAllTextView.setVisibility(View.VISIBLE);
//			selectDisAllTextView.setVisibility(View.GONE);
//			editStoryPinnedListViewAdapter.startSelectPhoto(1, 0);
//			System.out.println("size======"+photoURLlist.size());
//			photoURLlist.clear();//每次全选，清空全部数据
//			System.out.println("size======"+photoURLlist.size());
//			shareTextView.setEnabled(false);
//			deleteTextView.setEnabled(false);
//			buyTextView.setEnabled(false);
//			break;
//
//		case R.id.select_all:
//			//全选操作
//			selectDisAllTextView.setVisibility(View.VISIBLE);
//			selectAllTextView.setVisibility(View.GONE);
//			System.out.println("select all");
//			editStoryPinnedListViewAdapter.startSelectPhoto(1, 1);
//			selectall(originalAlbumArrayList);
//			shareTextView.setEnabled(true);
//			deleteTextView.setEnabled(true);
//			buyTextView.setEnabled(true);
//			break;
//
//		case R.id.select_share:
//			//调用share的接口
//			System.out.println("select share");
//			if (photoURLlist.size() == 0) {//没选择图片
//				myToast.setTextAndShow(R.string.select_photos, Common.TOAST_SHORT_TIME);
//			}else if (photoURLlist.size() == 1) {//分享图片
//				System.out.println("start share=" + photoURLlist.get(0).photoPathOrURL);
//				//判断图片是本地还是网路图片
//				if (photoURLlist.get(0).onLine == 1) {//网络图片
//					System.out.println("网络图片");
//					if (photoURLlist.get(0).isPayed == 0) {//未购买
//						myToast.setTextAndShow(R.string.buythephoto, Common.TOAST_SHORT_TIME);
//					}else {
//						sharePop.setshareinfo(null, photoURLlist.get(0).photoPathOrURL,null, "online",mhHandler);
//						sharePop.showAtLocation(v, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
//					}
//
//				}else {
//					System.out.println("本地图片");
//					sharePop.setshareinfo(photoURLlist.get(0).photoPathOrURL, null, null,"local",mhHandler);
//					sharePop.showAtLocation(v, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
//				}
//			}else {//选择超过1张
//				myToast.setTextAndShow(R.string.share_photo_count, Common.TOAST_SHORT_TIME);
//			}
//			break;
//
//		case R.id.select_makegift:
//			System.out.println("select make gift");
//			if (photoURLlist.size() == 0) {//没有图片
//				myToast.setTextAndShow(R.string.select_photos, Common.TOAST_SHORT_TIME);
//			}else if (photoURLlist.size() == 1) {//普通商品
//				System.out.println("makegift");
//				Intent intent = new Intent(this,MakegiftActivity.class);
//				intent.putExtra("selectPhoto", photoURLlist.get(0));
//				startActivity(intent);
//			}else if (photoURLlist.size() > 1) {//相册，暂时还不开放
//				myToast.setTextAndShow(R.string.share_photo_count, Common.TOAST_SHORT_TIME);
//			}
//
//			break;
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
//
//		case R.id.imageButton_edit://编辑操作
//			if (!editMode) {//开始编辑操作
//				editMode = true;
//				editBarLinearLayout.setVisibility(View.VISIBLE);
//				backRelativeLayout.setVisibility(View.GONE);
////				pinnedSectionListView.getRefreshableView().addFooterView(footerView);
//				setListCheckedStatus(editMode);
//			}else {//取消编辑操作
//				editMode = false;
//				editBarLinearLayout.setVisibility(View.GONE);
//				backRelativeLayout.setVisibility(View.VISIBLE);
////				pinnedSectionListView.getRefreshableView().removeFooterView(footerView);
//				setListCheckedStatus(editMode);
//				System.out.println("select photo count is "+ photoURLlist.size());
//				photoURLlist.clear();
//				System.out.println("select photo count is "+ photoURLlist.size());
//			}
//			editStoryPinnedListViewAdapter.setEditMode(editMode);
//			editStoryPinnedListViewAdapter.notifyDataSetChanged();
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
			Log.d(TAG, "删除列表中的项");
			PhotoInfo info = bundle.getParcelable("photo");
			if (photoURLlist.contains(info)) {
				Log.d(TAG, "找到删除项");
				photoURLlist.remove(info);
			}else {
				Log.d(TAG, "找不到删除项");
				
			}
			selectAllTextView.setVisibility(View.VISIBLE);
			selectDisAllTextView.setVisibility(View.GONE);
			if (photoURLlist.size() == 0) {
				shareTextView.setEnabled(false);
				deleteTextView.setEnabled(false);
				buyTextView.setEnabled(false);
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
			Log.d(TAG, "添加到已选择的列表");
			PhotoInfo info = b.getParcelable("photo");
			if (photoURLlist.contains(info)) {
				System.out.println("之前点过了");
			}else {
				
				System.out.println("之前没有点过了");
				photoURLlist.add(info);//加入到list中
			}

			//判断是否已经全部选中，如果是，则将标记改为true
			if (photoURLlist.size()==photoCount) {
				selectAllTextView.setVisibility(View.GONE);
				selectDisAllTextView.setVisibility(View.VISIBLE);
			}
			if (photoURLlist.size() > 0) {//如果有
				shareTextView.setEnabled(true);
				deleteTextView.setEnabled(true);
				buyTextView.setEnabled(true);
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
			Log.d(TAG, "select all");
			System.out.println(photoURLlist.size());
			photoURLlist.clear();//每次全选，清空全部数据
			System.out.println(photoURLlist.size());
			for (int i = 0; i < arraylist.size(); i++) {
				photoURLlist.addAll(arraylist.get(i).list);
			}
			System.out.println(photoURLlist.size());
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
//				dialog.dismiss();
//			}
//
//		}
}
