package com.pictureAir;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import cn.sharesdk.facebook.b;

import com.handmark.pulltorefresh.library.PullToRefreshPinnedSectionListView;
import com.pictureAir.adapter.EditStoryPinnedListViewAdapter;
import com.pictureAir.customDialog.CustomDialog;
import com.pictureAir.entity.CartItemInfo;
import com.pictureAir.entity.PhotoInfo;
import com.pictureAir.entity.PhotoItemInfo;
import com.pictureAir.util.AppManager;
import com.pictureAir.util.Common;
import com.pictureAir.widget.MyToast;
import com.pictureAir.widget.SharePop;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 编辑story中的相册页面
 * @author bauer_bao
 *
 */
public class EditStoryAlbumActivity extends Activity implements OnClickListener{
	private ImageView backRelativeLayout;
	private TextView selectAllTextView;
	private TextView shareTextView;
	private TextView deleteTextView;
	private TextView buyTextView;
	private TextView selectDisAllTextView;
	private ProgressDialog deleteFileDialog;
	private PullToRefreshPinnedSectionListView pinnedSectionListView;
	private EditStoryPinnedListViewAdapter editStoryPinnedListViewAdapter;
	
	private ArrayList<PhotoItemInfo> albumArrayList;
	private ArrayList<PhotoItemInfo> originalAlbumArrayList;
	private ArrayList<PhotoInfo> photoURLlist = new ArrayList<PhotoInfo>();//选择的图片的list
	private static final String TAG = "EditStoryAlbumActivity";
	private final static int DELETEFILE = 12;
	private final static int PROGRESSDIALOG = 0x112;
	private int photoCount = 0;
	private int currentProgress = 0;
	private boolean isSelectAll = false;
	private SharePop sharePop;//分享
	private MyToast myToast;
	private CustomDialog customdialog;
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			Log.d(TAG, "photo on click");
			Bundle bundle = msg.getData();
			switch (bundle.getInt("flag")) {
			case 10://取消选中的时候
				deletefromlist(bundle);
				break;

			case 11://选中的时候，首先判断list的长度，其次判断之前有没有选中过，如果选中过，则不做任何操作，没有选中过，则选中
				addtolist(bundle);
				break;

			default:
				break;
			}
		};
	};
	
	Handler mhHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case DELETEFILE:
				deleteFileDialog.setProgress(msg.arg1);
				if (msg.arg1 == photoURLlist.size()) {
					System.out.println("has delete all files");
					Iterator<PhotoInfo> iterator2 = photoURLlist.iterator();
					while (iterator2.hasNext()) {
						System.out.println("scan photoURLlist");
						PhotoInfo photoInfo = (PhotoInfo) iterator2.next();
						if (photoInfo.onLine == 0) {//本地图片，需要删除
							System.out.println("need remove photo");
							iterator2.remove();
						}
					}
//					photoURLlist.clear();
					currentProgress = 0;
					Iterator<PhotoItemInfo> iterator = albumArrayList.iterator();
					while (iterator.hasNext()) {
						System.out.println("scan albumArrayList");
						PhotoItemInfo photoItemInfo = (PhotoItemInfo) iterator.next();
						if (photoItemInfo.list.size() == 0) {
							System.out.println("remove albumArrayList");
							iterator.remove();
						}
					}
					
//					for (int i = 0; i < albumArrayList.size(); i++) {
//						System.out.println("name = "+ albumArrayList.get(i).place);
//						System.out.println("shoot time = "+ albumArrayList.get(i).shootTime);
//					}
					editStoryPinnedListViewAdapter.updateData(albumArrayList);
					if (photoURLlist.size() == 0) {
						shareTextView.setEnabled(false);
						deleteTextView.setEnabled(false);
						buyTextView.setEnabled(false);
					}
					deleteFileDialog.dismiss();
					removeDialog(msg.arg2);//删除对应ID的dialog
				}
				((MyApplication)getApplication()).scanMagicFinish = false;
				break;

			default:
				break;
			}
		}
	};

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_story_photo_activity);
		AppManager.getInstance().addActivity(this);
		//find控件
		backRelativeLayout = (ImageView) findViewById(R.id.rlrt);
		selectAllTextView = (TextView) findViewById(R.id.select_all);
		shareTextView = (TextView) findViewById(R.id.select_share);
		deleteTextView = (TextView) findViewById(R.id.select_delete);
		buyTextView = (TextView) findViewById(R.id.select_makegift);
		selectDisAllTextView = (TextView) findViewById(R.id.select_disall);
		pinnedSectionListView = (PullToRefreshPinnedSectionListView) findViewById(R.id.pullToRefreshPinnedSectionListView);
		pinnedSectionListView.setPullToRefreshEnabled(false);
		
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
		albumArrayList = new ArrayList<PhotoItemInfo>();
		originalAlbumArrayList = b.getParcelableArrayList("photos");
		albumArrayList.addAll(originalAlbumArrayList);
		for (int i = 0; i < albumArrayList.size(); i++) {
			for (int j = 0; j < albumArrayList.get(i).list.size(); j++) {
				albumArrayList.get(i).list.get(j).isSelected = 0;
				albumArrayList.get(i).list.get(j).isChecked = 1;
				photoCount++;
			}
		}
		editStoryPinnedListViewAdapter = new EditStoryPinnedListViewAdapter(this, albumArrayList, handler);
		pinnedSectionListView.setAdapter(editStoryPinnedListViewAdapter);
		sharePop = new SharePop(this);
		myToast = new MyToast(this);
	}

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
			
		case R.id.select_disall:
			//全取消操作
			System.out.println("disselect all");
			isSelectAll = false;
			selectAllTextView.setVisibility(View.VISIBLE);
			selectDisAllTextView.setVisibility(View.GONE);
			editStoryPinnedListViewAdapter.startSelectPhoto(1, 0);
			System.out.println("size======"+photoURLlist.size());
			photoURLlist.clear();//每次全选，清空全部数据
			System.out.println("size======"+photoURLlist.size());
			shareTextView.setEnabled(false);
			deleteTextView.setEnabled(false);
			buyTextView.setEnabled(false);
			break;
			
		case R.id.select_all:
//			if (!isSelectAll) {
			//全选操作
				isSelectAll = true;
				selectDisAllTextView.setVisibility(View.VISIBLE);
				selectAllTextView.setVisibility(View.GONE);
//				selectAllTextView.setText(R.string.disall);
				System.out.println("select all");
				editStoryPinnedListViewAdapter.startSelectPhoto(1, 1);
				selectall(originalAlbumArrayList);
				shareTextView.setEnabled(true);
				deleteTextView.setEnabled(true);
				buyTextView.setEnabled(true);
//			}else {//全取消操作
//				System.out.println("disselect all");
//				isSelectAll = false;
//				selectAllTextView.setText(R.string.all);
//				editStoryPinnedListViewAdapter.startSelectPhoto(1, 0);
//				System.out.println("size======"+photoURLlist.size());
//				photoURLlist.clear();//每次全选，清空全部数据
//				System.out.println("size======"+photoURLlist.size());
//			}
			break;
			
		case R.id.select_share:
			//调用share的接口
			System.out.println("select share");
			if (photoURLlist.size() == 0) {//没选择图片
				myToast.setTextAndShow(R.string.select_photos, Common.TOAST_SHORT_TIME);
			}else if (photoURLlist.size() == 1) {//分享图片
				System.out.println("start share=" + photoURLlist.get(0).photoPathOrURL);
				//判断图片是本地还是网路图片
				if (photoURLlist.get(0).onLine == 1) {//网络图片
					System.out.println("网络图片");
					if (photoURLlist.get(0).isPayed == 0) {//未购买
						myToast.setTextAndShow(R.string.buythephoto, Common.TOAST_SHORT_TIME);
					}else {
						sharePop.setshareinfo(null, photoURLlist.get(0).photoPathOrURL, "online");
						sharePop.showAtLocation(v, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
					}

				}else {
					System.out.println("本地图片");
					sharePop.setshareinfo(photoURLlist.get(0).photoPathOrURL, null, "local");
					sharePop.showAtLocation(v, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
				}
			}else {//选择超过1张
				myToast.setTextAndShow(R.string.share_photo_count, Common.TOAST_SHORT_TIME);
			}
			break;

		case R.id.select_makegift:
			System.out.println("select make gift");
			if (photoURLlist.size() == 0) {//没有图片
				myToast.setTextAndShow(R.string.select_photos, Common.TOAST_SHORT_TIME);
			}else if (photoURLlist.size() == 1) {//普通商品
				System.out.println("makegift");
				Intent intent = new Intent(this,MakegiftActivity.class);
				intent.putExtra("selectPhoto", photoURLlist.get(0));
				startActivity(intent);
			}else if (photoURLlist.size() > 1) {//相册，暂时还不开放
				myToast.setTextAndShow(R.string.share_photo_count, Common.TOAST_SHORT_TIME);
			}

			break;
			
		case R.id.select_delete:
			if (photoURLlist.size()==0) {
				myToast.setTextAndShow(R.string.select_photos, Common.TOAST_SHORT_TIME);
				return;
			}
			boolean hasNetWorkPhoto = false;
			boolean hasLocalPhoto = false;
			for (int i = 0; i < photoURLlist.size(); i++) {
//				if (!hasNetWorkPhoto) {
					if (photoURLlist.get(i).onLine == 1) {
						if (!hasNetWorkPhoto) {
							hasNetWorkPhoto = true;
							
						}
					}else {
						if (!hasLocalPhoto) {
							
							hasLocalPhoto = true;
						}
					}
//				}
			}
			
			if (hasNetWorkPhoto && hasLocalPhoto) {//如果有网络图片，也有本地照片，弹框提示
				//初始化dialog
				customdialog = new CustomDialog.Builder(this)
				.setMessage(getString(R.string.delete_with_photopass)) 
				.setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogOnClickListener())
				.setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogOnClickListener())
				.setCancelable(false)
				.create();
				customdialog.show();
			}else if (hasNetWorkPhoto && !hasLocalPhoto) {//只有网络图片，没有本地图片
				myToast.setTextAndShow(R.string.cannot_delete_in_PhotoPass, Common.TOAST_SHORT_TIME);
			}else if (!hasNetWorkPhoto && hasLocalPhoto) {//只有本地图片
				showDialog(PROGRESSDIALOG);
			}
			break;
			
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
			isSelectAll = false;
			selectAllTextView.setVisibility(View.VISIBLE);
			selectDisAllTextView.setVisibility(View.GONE);
			if (photoURLlist.size() == 0) {
				shareTextView.setEnabled(false);
				deleteTextView.setEnabled(false);
				buyTextView.setEnabled(false);
			}
			
//			for (int i = 0; i < photoURLlist.size(); i++) {
//				if (bundle.getString("pathOrUrl").equals(photoURLlist.get(i).photoPathOrURL)) {//如果找到之后立刻删除，会对list的长度有影响，for循环会报错
//					result = i;
//					resultString = true;
//				}
//			}
//			if (resultString) {
//				photoURLlist.remove(result);
//				result = 0;
//				resultString = false;
//			}
//			isSelectAll = false;
//			selectall.setText(R.string.all);
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
				isSelectAll = true;
				selectAllTextView.setVisibility(View.GONE);
				selectDisAllTextView.setVisibility(View.VISIBLE);
//				selectAllTextView.setText(R.string.disall);
			}
			if (photoURLlist.size() > 0) {//如果有
				shareTextView.setEnabled(true);
				deleteTextView.setEnabled(true);
				buyTextView.setEnabled(true);
			}
			
		}
		
		//创建dialog
		@Override
		@Deprecated
		protected Dialog onCreateDialog(int id, Bundle args) {
			switch (id) {
			case PROGRESSDIALOG:
				System.out.println("onCreateDialog------->"+photoURLlist.size());
				deleteFileDialog = new ProgressDialog(EditStoryAlbumActivity.this);
				deleteFileDialog.setTitle("Deleting");
				deleteFileDialog.setCancelable(false);
				deleteFileDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				break;

			default:
				break;
			}
			return deleteFileDialog;
		}
		//创建完dialog之后会调用的方法
		@Override
		@Deprecated
		protected void onPrepareDialog(final int id, Dialog dialog) {
			// TODO Auto-generated method stub
			super.onPrepareDialog(id, dialog);
			System.out.println("onPrepareDialog------->"+photoURLlist.size());
			deleteFileDialog.setMax(photoURLlist.size());
			deleteFileDialog.setProgress(0);
			switch (id) {
			case PROGRESSDIALOG:
				new Thread(){
					public void run() {
						doWork(id);
					};
				}.start();
				break;

			default:
				break;
			}
		}

		/**
		 * 删除文件的操作，完成比较耗时的操作
		 */
		private void doWork(int id) {
			File file;
			Message message; 
			boolean hasFound = false;
			ArrayList<PhotoInfo> list;
			for (int i = 0; i < photoURLlist.size(); i++) {
				
				//删除图片在arraylist中对应的项
				if (photoURLlist.get(i).onLine == 0) {//本地图片
					//删除contentpridiver表中的数据
					System.out.println("需要删除的文件为"+photoURLlist.get(i).photoPathOrURL);
					String params[] = new String[]{photoURLlist.get(i).photoPathOrURL};
					//删除Media数据库中的对应图片信息
					System.out.println("删除Media表中的对应数据");
					getContentResolver().delete(Media.EXTERNAL_CONTENT_URI, Media.DATA+" like ?", params);
					
					
					
//					System.out.println(",需要删除的索引值----->"+photoURLlist.get(i).index.toString());
					System.out.println("arraylist需要移除的文件是"+photoURLlist.get(i).photoPathOrURL);
					for (int j = 0; j < albumArrayList.size(); j++) {
						list = new ArrayList<PhotoInfo>();
						list.addAll(albumArrayList.get(j).list);
						Iterator<PhotoInfo> iterator = list.iterator();
						while (iterator.hasNext()) {
							PhotoInfo photoInfo = iterator.next();
							if (photoInfo.photoPathOrURL.equals(photoURLlist.get(i).photoPathOrURL)) {
								iterator.remove();
								hasFound = true;
								break;
							}
						}
						if (hasFound) {
							hasFound = false;
							albumArrayList.get(j).list = list;
							break;
						}else {
							
						}
					}
					//获取需要删除的文件
					file = new File(photoURLlist.get(i).photoPathOrURL);
					//删除文件
					if (file.exists()) {
						System.out.println("开始删除文件"+photoURLlist.get(i).photoPathOrURL);
						//删除文件
						file.delete();
						System.out.println("the file has been deleted");
					}
				}
				currentProgress++;
				message = mhHandler.obtainMessage();
				message.what = DELETEFILE;
				message.arg1 = currentProgress;
				message.arg2 = id;
//				message.obj = photoURLlist.get(i).photoPathOrURL;
				mhHandler.sendMessage(message);
			}
			System.out.println("notify");
		}

		/**
		 * 全选操作
		 * @param arraylist，传入对应的arraylist
		 */
		private void selectall(ArrayList<PhotoItemInfo> arraylist) {
			Log.d(TAG, "select all");
//			PhotoInfo selectedInfo;
			System.out.println(photoURLlist.size());
			photoURLlist.clear();//每次全选，清空全部数据
			System.out.println(photoURLlist.size());
			for (int i = 0; i < arraylist.size(); i++) {
				photoURLlist.addAll(arraylist.get(i).list);
//				selectedInfo = new PhotoInfo();
//				selectedInfo.albumName = album;
//				selectedInfo.photoPathOrURL = arraylist.get(i).photoPathOrURL;
//				selectedInfo.index = i+"";
//				photoURLlist.add(selectedInfo);//加入到list中
			}
			System.out.println(photoURLlist.size());
		}
		
		private class DialogOnClickListener implements DialogInterface.OnClickListener{

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					System.out.println("ok");
					showDialog(PROGRESSDIALOG);
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					System.out.println("no");
					
					break;
					
				default:
					break;
				}
				dialog.dismiss();
			}
			
		}
}
