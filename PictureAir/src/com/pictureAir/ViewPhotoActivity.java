package com.pictureAir;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images.Media;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pictureAir.adapter.ViewPhotoGridViewAdapter;
import com.pictureAir.entity.PhotoInfo;
import com.pictureAir.util.AppManager;
import com.pictureAir.util.Common;
import com.pictureAir.widget.CustomProgressBarPop;
import com.pictureAir.widget.CustomProgressDialog;
import com.pictureAir.widget.MyToast;
import com.pictureAir.widget.SharePop;

/**
 * 预览magic的照片，可以预览大图，可以对整个相册进行编辑
 * @author bauer_bao
 *
 */

public class ViewPhotoActivity extends BaseActivity implements OnClickListener {

	private ImageView rtLayout;
	private TextView viewTextView;
	private GridView myGridView;
	private ViewPhotoGridViewAdapter viewPhotoGridViewAdapter;
	private ImageView editImageButton;
	private MyToast newToast;
	private MyApplication myApplication;
	//预览的四个相册
	private ArrayList<PhotoInfo> magicArrayList;
	private boolean sdCardExist;
	private boolean select_photo_flag = false;
//	private ProgressDialog deleteFileDialog;
	private CustomProgressDialog dialog;
	private LinearLayout selectllLayout;
	private TextView selectDisAll, selectAll, selectMakeGift, selectShare, selectDelete;

	private ArrayList<PhotoInfo> photoURLlist = new ArrayList<PhotoInfo>();//选择的图片的list
	private PhotoInfo selectPhotoItemInfo;
	private boolean resultString = false;
	private int result = 0;
	private int currentProgress = 0;

	private SharePop sharePop;//分享

	private final static int DELETEFILE = 12;
//	private final static int PROGRESSDIALOG = 0x112;

	private ScanPhotosThread scanPhotosThread;

	private SharedPreferences sharedPreferences;
	private Editor editor;
	private SimpleDateFormat sdf;
	private CustomProgressBarPop customProgressBarPop;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_photo);
		initview();
	}

	//初始化函数
	private void initview() {
		//初始化资源
		AppManager.getInstance().addActivity(this);
		newToast = new MyToast(this);
		myApplication = (MyApplication)getApplication();
		sharedPreferences = getSharedPreferences("pictureAir", MODE_PRIVATE);
		sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);//判断sd卡是否存在 
		sharePop = new SharePop(this);
		customProgressBarPop = new CustomProgressBarPop(this, findViewById(R.id.viewSelectRelativeLayout), CustomProgressBarPop.TYPE_DELETE);
		//初始化控件
		selectllLayout = (LinearLayout)findViewById(R.id.select_tools_linearlayout);
		selectAll = (TextView)findViewById(R.id.select_all);
		selectDisAll = (TextView)findViewById(R.id.select_disall);
		selectMakeGift = (TextView)findViewById(R.id.select_makegift);
		selectShare = (TextView)findViewById(R.id.select_share);
		selectDelete = (TextView)findViewById(R.id.select_delete);
		editImageButton = (ImageView)findViewById(R.id.imageButton_edit);
		rtLayout = (ImageView)findViewById(R.id.rlrt);
		viewTextView = (TextView)findViewById(R.id.text);
		myGridView = (GridView)findViewById(R.id.gridView_all);

		//绑定监听
		selectAll.setOnClickListener(this);
		selectDisAll.setOnClickListener(this);
		selectMakeGift.setOnClickListener(this);
		selectShare.setOnClickListener(this);
		selectDelete.setOnClickListener(this);
		editImageButton.setOnClickListener(this);
		rtLayout.setOnClickListener(this);

		//初始化数据
		myApplication.scanMagicFinish = false;
		magicArrayList = new ArrayList<PhotoInfo>();
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		editImageButton.setVisibility(View.VISIBLE);
		viewTextView.setText(getString(R.string.story_tab_magic));//更改文字
		viewPhotoGridViewAdapter = new ViewPhotoGridViewAdapter(this, magicArrayList);
		myGridView.setAdapter(viewPhotoGridViewAdapter);
		myGridView.setOnItemClickListener(new PhotoSelectedListener());

		dialog = CustomProgressDialog.show(this, getString(R.string.is_loading), false, null);
		//		dialog = ProgressDialog.show(this, getString(R.string.loading___), getString(R.string.is_loading), false, false);
		//		scanPhotosThread = new ScanPhotosThread();
		//		scanPhotosThread.start();

	}

	//处理viewpager传递过来的数据
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {

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


	//获取Magic的照片
	private void ScanPhotos(ArrayList<PhotoInfo> arrayList, String filePath, String albumName){
		System.out.println("---------->scan1"+albumName);
		myApplication.magicPicList.clear();
		selectPhotoItemInfo = new PhotoInfo();
		arrayList.add(selectPhotoItemInfo);
		if (!sdCardExist) {//如果SD卡不存在
			return;
		}
		System.out.println("---------->scan2"+albumName);
		File file = new File(filePath);
		if (!file.exists()) {
			file.mkdirs();
		}
		Date date;
		File[] files  = file.listFiles();
		System.out.println("---------->scan3"+albumName);
		//		arrayList.add(selectPhotoItemInfo);
		for (int i = 0; i < files.length; i++) {
			if(files[i].getName().endsWith(".JPG")||files[i].getName().endsWith(".jpg")){
				if (files[i].length() > 0) {//扫描到文件
					selectPhotoItemInfo = new PhotoInfo();
					selectPhotoItemInfo.photoPathOrURL = files[i].getPath();
					System.out.println("magic url is =====>" + selectPhotoItemInfo.photoPathOrURL);
					selectPhotoItemInfo.lastModify = files[i].lastModified();
					date = new Date(selectPhotoItemInfo.lastModify);
					selectPhotoItemInfo.shootOn = sdf.format(date);
					selectPhotoItemInfo.shootTime = selectPhotoItemInfo.shootOn.substring(0, 10);
					selectPhotoItemInfo.isChecked = 0;
					selectPhotoItemInfo.isSelected = 0;
					selectPhotoItemInfo.showMask = 0;
					selectPhotoItemInfo.locationName = getString(R.string.magic_location);
					//					selectPhotoItemInfo.albumName = albumName;
					selectPhotoItemInfo.onLine = 0;
					selectPhotoItemInfo.isUploaded = 0;
					selectPhotoItemInfo.isPayed = 1;
					//					selectPhotoItemInfo.location = getString(R.string.story_tab_magic);
					myApplication.magicPicList.add(selectPhotoItemInfo);
				}
			}
		}
		myApplication.scanMagicFinish = true;
		arrayList.addAll(myApplication.magicPicList);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AppManager.getInstance().killActivity(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (!myApplication.scanMagicFinish) {
			//			myApplication.setneedScanPhoto(false);
			myApplication.scanMagicFinish = true;
			magicArrayList.clear();
			scanPhotosThread = new ScanPhotosThread();
			scanPhotosThread.start();
		}
	}


	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.rlrt://返回按钮
			finish();
			break;

		case R.id.imageButton_edit://点击编辑按钮
			System.out.println("edit photo");
			if (!select_photo_flag) {
				selectllLayout.setVisibility(View.VISIBLE);
				select_photo_flag = true;
				selectDisAll.setVisibility(View.GONE);
				selectAll.setVisibility(View.VISIBLE);
				selectShare.setEnabled(false);
				selectDelete.setEnabled(false);
				selectMakeGift.setEnabled(false);
				viewPhotoGridViewAdapter.startSelectPhoto(1, 0);
				//				editImageButton.setImageResource(R.drawable.edit_cancel);
				rtLayout.setVisibility(View.GONE);
			}else {
				selectllLayout.setVisibility(View.GONE);
				select_photo_flag = false;
				selectAll.setText(R.string.all);
				viewPhotoGridViewAdapter.startSelectPhoto(0, 0);
				photoURLlist.clear();//清除选择的图片
				//				editImageButton.setImageResource(R.drawable.preview_edit);
				rtLayout.setVisibility(View.VISIBLE);
			}
			break;

		case R.id.select_disall:
			//全取消操作
			System.out.println("disselect all");
			selectAll.setVisibility(View.VISIBLE);
			selectDisAll.setVisibility(View.GONE);
			selectShare.setEnabled(false);
			selectDelete.setEnabled(false);
			selectMakeGift.setEnabled(false);
			viewPhotoGridViewAdapter.startSelectPhoto(1, 0);
			System.out.println("size======"+photoURLlist.size());
			photoURLlist.clear();//每次全选，清空全部数据
			System.out.println("size======"+photoURLlist.size());

			break;

		case R.id.select_all://全选操作
			//			if (!isSelectAll) {//全选操作
			selectAll.setVisibility(View.GONE);
			selectDisAll.setVisibility(View.VISIBLE);
			selectShare.setEnabled(true);
			selectDelete.setEnabled(true);
			selectMakeGift.setEnabled(true);

			System.out.println("select all");
			viewPhotoGridViewAdapter.startSelectPhoto(1, 1);
			selectall(magicArrayList, Common.ALBUM_MAGIC);
			//			}else {}
			break;

		case R.id.select_makegift://制作礼物操作
			System.out.println("select make gift");
			if (photoURLlist.size() == 0) {//没有图片
				newToast.setTextAndShow(R.string.select_photos, Common.TOAST_SHORT_TIME);
			}else if (photoURLlist.size() == 1) {//普通商品
				System.out.println("makegift");
				intent = new Intent(this,MakegiftActivity.class);
				//				intent.putExtra("photopath", photoURLlist.get(0).photoPathOrURL);
				intent.putExtra("selectPhoto", photoURLlist.get(0));
				startActivity(intent);
			}else if (photoURLlist.size() > 1) {//相册，暂时还不开放
				newToast.setTextAndShow(R.string.share_photo_count, Common.TOAST_SHORT_TIME);
			}
			break;

		case R.id.select_share://分享操作
			//调用share的接口
			System.out.println("select share");
			if (photoURLlist.size() == 0) {//没选择图片
				newToast.setTextAndShow(R.string.select_photos, Common.TOAST_SHORT_TIME);
			}else if (photoURLlist.size() == 1) {//分享图片
				System.out.println("start share=" + photoURLlist.get(0).photoPathOrURL);
				//判断图片是本地还是网路图片
				if (photoURLlist.get(0).onLine == 1) {//网络图片
					System.out.println("网络图片");
					if (photoURLlist.get(0).isPayed == 0) {//未购买
						newToast.setTextAndShow(R.string.buythephoto, Common.TOAST_SHORT_TIME);
					}else {
						sharePop.setshareinfo(null, photoURLlist.get(0).photoPathOrURL, null,"online",mHandler);
						sharePop.showAtLocation(v, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
					}

				}else {
					System.out.println("本地图片");
					sharePop.setshareinfo(photoURLlist.get(0).photoPathOrURL, null,null, "local",mHandler);
					sharePop.showAtLocation(v, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
				}
			}else {//选择超过1张
				newToast.setTextAndShow(R.string.share_photo_count, Common.TOAST_SHORT_TIME);
			}
			break;

		case R.id.select_delete://删除操作
			if (photoURLlist.size()==0) {
				newToast.setTextAndShow(R.string.select_photos, Common.TOAST_SHORT_TIME);
				return;
			}
			//如果有删除文件的操作，应该要判断当前删除的文件是否和sharedPreferences中的存储数据一样。此处直接重置了。
			editor = sharedPreferences.edit();
			editor.putString(Common.LAST_PHOTO_URL, "");
			editor.commit();
//			showDialog(PROGRESSDIALOG);
			customProgressBarPop.show(photoURLlist.size());
			new Thread(){
				public void run() {
					doWork();
//					doWork(PROGRESSDIALOG);
				};
			}.start();
			break;

		default:
			break;
		}
	}

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			PhotoInfo photoInfo;
			for (int i = 1; i < magicArrayList.size(); i++) {
				photoInfo = magicArrayList.get(i);
				photoInfo.isChecked = 0;
				photoInfo.isSelected = 0;
			}
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
		
		
	}
	
	private class ScanPhotosThread extends Thread{

		@Override
		public void run() {
			System.out.println("------->run");
			//预览模式，需要预先加载本地的magic的图片
			ScanPhotos(magicArrayList, Common.PHOTO_SAVE_PATH, Common.ALBUM_MAGIC);
			Collections.sort(magicArrayList);
			mhHandler.sendEmptyMessage(133);
		}
	}

	//gridview点击监听
	private class PhotoSelectedListener implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

			if (position==0) {
				Intent intent = new Intent(ViewPhotoActivity.this,CameraActivity.class);
				ViewPhotoActivity.this.startActivity(intent);
			}else {
				if (magicArrayList.get(position).isChecked == 1) {//通过判断check的标记来确定是否响应选择事件还是预览事件
					System.out.println("select"+position);
					Message msg = mHandler.obtainMessage();
					int visiblePos = myGridView.getFirstVisiblePosition();
					//选择事件
					PhotoInfo info = magicArrayList.get(position);
					Bundle bundle = new Bundle();
					if (info.isSelected == 1) {//取消选择
						info.isSelected = 0;
						bundle.putInt("flag", 10);
					}else {//选择
						info.isSelected = 1;
						bundle.putInt("flag", 11);
					}
					viewPhotoGridViewAdapter.refreshView(position, myGridView.getChildAt(position-visiblePos), 1);
					bundle.putString("pathOrUrl", info.photoPathOrURL);
					bundle.putInt("position", position);
					msg.setData(bundle);
					mHandler.sendMessage(msg);
					magicArrayList.set(position, info);
				}else {
					//预览事件
					/*************************************
					 * 要判断照片的购买属性，如果已经购买，则直接显示，如果没有购买，提示购买
					 * ***************************/
					System.out.println(position+"_"+magicArrayList.get(position).photoPathOrURL);
					Intent intent = null;
					intent = new Intent(ViewPhotoActivity.this,PreviewPhotoActivity.class);
					intent.putExtra("flag", magicArrayList.get(position).onLine);//哪个相册的标记
					intent.putExtra("position", String.valueOf(position));//在那个相册中的位置
					intent.putExtra("photos", magicArrayList);//那个相册的全部图片路径
					intent.putExtra("targetphotos", magicArrayList);
					//					intent.putExtra("locationName", getString(R.string.story_tab_magic));
					intent.putExtra("activity", "viewphotoactivity");
					startActivity(intent);
				}
			}
		}
	}


	Handler mhHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case DELETEFILE:
//				deleteFileDialog.setProgress(msg.arg1);
				customProgressBarPop.setProgress(msg.arg1, photoURLlist.size());
				if (msg.arg1 == photoURLlist.size()) {
					photoURLlist.clear();
					currentProgress = 0;
					viewPhotoGridViewAdapter.notifyDataSetChanged();
					customProgressBarPop.dismiss();
//					deleteFileDialog.dismiss();
//					removeDialog(msg.arg2);//删除对应ID的dialog
					selectShare.setEnabled(false);
					selectDelete.setEnabled(false);
					selectMakeGift.setEnabled(false);
					myApplication.scanMagicFinish = false;
				}
				break;

			case 133:
				viewPhotoGridViewAdapter.notifyDataSetChanged();
				if (dialog.isShowing()) {
					dialog.dismiss();
				}
				break;

			default:
				break;
			}
		}
	};


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
		System.out.println("position="+bundle.getInt("position"));
		for (int i = 0; i < photoURLlist.size(); i++) {
			if (bundle.getString("pathOrUrl").equals(photoURLlist.get(i).photoPathOrURL)) {//如果找到之后立刻删除，会对list的长度有影响，for循环会报错
				result = i;
				resultString = true;
			}
		}
		if (resultString) {
			photoURLlist.remove(result);
			result = 0;
			resultString = false;
		}
		selectAll.setVisibility(View.VISIBLE);
		selectDisAll.setVisibility(View.GONE);
		if (photoURLlist.size() == 0) {
			selectShare.setEnabled(false);
			selectDelete.setEnabled(false);
			selectMakeGift.setEnabled(false);
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
		for (int j = 0; j < photoURLlist.size(); j++) {
			if (b.getString("pathOrUrl").equals(photoURLlist.get(j).photoPathOrURL)) {
				result = j;
				resultString = true;
			}
		}
		if (resultString) {
			System.out.println("之前点过了");
			result = 0;//清零
			resultString = false;
		}else {//同样，如果直接在循环中加入的话，会对for循环产生影响
			System.out.println("position"+b.getInt("position"));
			System.out.println("path"+b.getString("pathOrUrl"));
			PhotoInfo itemInfo = new PhotoInfo();
			//			itemInfo.albumName = Common.ALBUM_MAGIC;//所在的图册
			itemInfo.photoPathOrURL = b.getString("pathOrUrl");//图片对应的原始路径
			itemInfo.index = b.getInt("position")+"";//对应于那个相册中的index索引
			photoURLlist.add(itemInfo);//加入到list中
			result = 0;//清零
			//判断是否已经全部选中，如果是，则将标记改为true
			System.out.println("photoURLList"+ photoURLlist.size() + "magicarraylist is "+ magicArrayList.size());
			if (photoURLlist.size()==magicArrayList.size()-1) {
				selectAll.setVisibility(View.GONE);
				selectDisAll.setVisibility(View.VISIBLE);
			}
			if (photoURLlist.size() > 0) {
				selectShare.setEnabled(true);
				selectDelete.setEnabled(true);
				selectMakeGift.setEnabled(true);
			}

		}
	}

	//创建dialog
//	@Override
//	@Deprecated
//	protected Dialog onCreateDialog(int id, Bundle args) {
//		switch (id) {
//		case PROGRESSDIALOG:
//			System.out.println("onCreateDialog------->"+photoURLlist.size());
//			deleteFileDialog = new ProgressDialog(ViewPhotoActivity.this);
//			deleteFileDialog.setTitle("Deleting");
//			deleteFileDialog.setCancelable(false);
//			deleteFileDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//			break;
//
//		default:
//			break;
//		}
//		return deleteFileDialog;
//	}
	//创建完dialog之后会调用的方法
//	@Override
//	@Deprecated
//	protected void onPrepareDialog(final int id, Dialog dialog) {
//		// TODO Auto-generated method stub
//		super.onPrepareDialog(id, dialog);
//		System.out.println("onPrepareDialog------->"+photoURLlist.size());
//		deleteFileDialog.setMax(photoURLlist.size());
//		deleteFileDialog.setProgress(0);
//		switch (id) {
//		case PROGRESSDIALOG:
//			new Thread(){
//				public void run() {
//					doWork(id);
//				};
//			}.start();
//			break;
//
//		default:
//			break;
//		}
//	}

	/**
	 * 删除文件的操作，完成比较耗时的操作
	 */
	private void doWork() {
		File file;
		Message message; 
		for (int i = 0; i < photoURLlist.size(); i++) {
			//删除contentpridiver表中的数据
			System.out.println("需要删除的文件为"+photoURLlist.get(i).photoPathOrURL + ",需要删除的索引值为"+photoURLlist.get(i).index);
			String params[] = new String[]{photoURLlist.get(i).photoPathOrURL};
			//删除Media数据库中的对应图片信息
			System.out.println("删除Media表中的对应数据");
			getContentResolver().delete(Media.EXTERNAL_CONTENT_URI, Media.DATA+" like ?", params);

			//删除图片在arraylist中对应的项
			System.out.println("删除前的列表长度为"+magicArrayList.size()+",需要删除的索引值----->"+photoURLlist.get(i).index.toString());
			System.out.println("arraylist需要移除的文件是"+magicArrayList.get(Integer.parseInt(photoURLlist.get(i).index.toString())).photoPathOrURL);
			magicArrayList.remove(Integer.parseInt(photoURLlist.get(i).index.toString()));//删除列表中的对应的信息
			System.out.println("删除后的列表长度为"+magicArrayList.size());
			//修改列表其他项的索引值
			for (int j = i+1; j < photoURLlist.size(); j++) {
				//遍历后几项，如果当前的索引值小于后面的，则将后面的序号减1
				int temp = Integer.parseInt(photoURLlist.get(j).index);
				if (Integer.parseInt(photoURLlist.get(i).index) < temp) {
					temp--;
					photoURLlist.get(j).index = temp + "";
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
			currentProgress++;
			message = mhHandler.obtainMessage();
			message.what = DELETEFILE;
			message.arg1 = currentProgress;
//			message.arg2 = id;
			message.obj = photoURLlist.get(i).photoPathOrURL;
			mhHandler.sendMessage(message);
		}
		System.out.println("notify");
		System.out.println("cleared");
	}

	/**
	 * 全选操作
	 * @param arraylist，传入对应的arraylist
	 */
	private void selectall(ArrayList<PhotoInfo> arraylist,String album) {
		PhotoInfo selectedInfo;
		System.out.println(photoURLlist.size());
		photoURLlist.clear();//每次全选，清空全部数据
		System.out.println(photoURLlist.size());
		for (int i = 1; i < arraylist.size(); i++) {//因为每个arraylist中的第一项为空，所以直接从1开始
			selectedInfo = new PhotoInfo();
			//			selectedInfo.albumName = album;
			selectedInfo.photoPathOrURL = arraylist.get(i).photoPathOrURL;
			selectedInfo.index = i+"";
			photoURLlist.add(selectedInfo);//加入到list中
		}
	}


}
