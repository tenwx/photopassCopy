package com.pictureair.photopass.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.SelectPhotoViewPagerAdapter;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.MyToast;

import java.io.File;
import java.util.ArrayList;

/**
 * 预览照片，总共有4大类，1，全部的照片，2，pictureair网络获取的图片，3，本软件拍的照片，4，已经购买的照片
 * 只能选择照片或者拍摄新的照片，不能做其他操作
 * @author bauer_bao
 *
 */

public class SelectPhotoActivity extends BaseActivity implements OnClickListener {
	//申明控件
	private ImageView rtLayout;
	private Button okButton;
	private TextView titleTextView;
	private ImageButton editImageButton;
	private TextView albumAll, albumPhotoPass, albumMagic, albumBought;
	private ImageView cursor;
	private ViewPager viewPager;
	
	private MyToast newToast;
	private MyApplication myApplication;
	//预览的四个相册
	private ArrayList<PhotoInfo> photoList = new ArrayList<PhotoInfo>();
	private ArrayList<PhotoInfo> allArrayList;
	private ArrayList<PhotoInfo> photoPassArrayList;
	private ArrayList<PhotoInfo> magicArrayList;
	private ArrayList<PhotoInfo> boughtArrayList;
	private ArrayList<View> listViews;
	private SelectPhotoViewPagerAdapter selectPhotoViewPagerAdapter;
	private String activity = null;
	private boolean sdCardExist;

	private int currentSelectedAlbum = 0;
	private ArrayList<PhotoInfo> photoURLlist = new ArrayList<PhotoInfo>();//选择的图片的list
	private PhotoInfo selectPhotoItemInfo;
	private boolean resultString = false;
	private int result = 0;
	private int photocount = 1;//需要添加的图片数量，以后要改这个数值
	private int selectedCount = 0;//记录已经选择的照片数量
	private int screenWidth;

	//每个相册扫描成功标记
	private boolean scanAllFinish = false;
	private boolean scanPhotoPassFinish = false;
	private boolean scanMagicFinish = false;
	private boolean scanBoughtFinish = false;
	
//	private ScanPhotosThread scanPhotosThread;
	
	private static final int ALBUM_ALL = 0;
	private static final int ALBUM_PHOTOPASS = 1;
	private static final int ALBUM_MAGIC = 2;
	private static final int ALBUM_BOUGHT = 3;
	private static final String TAG = "SelectPhotoActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_photo);
		initview();
	}

	//初始化函数
	private void initview() {
		//初始化资源
		newToast = new MyToast(this);
		myApplication = (MyApplication)getApplication();
		sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);//判断sd卡是否存在 
		activity = getIntent().getStringExtra("activity");
		
		//初始化控件
		okButton = (Button)findViewById(R.id.button1);
		editImageButton = (ImageButton)findViewById(R.id.imageButton_edit);
		rtLayout = (ImageView)findViewById(R.id.rlrt);
		titleTextView = (TextView)findViewById(R.id.text);
		albumAll = (TextView)findViewById(R.id.select_photo_tab_all);
		albumPhotoPass = (TextView)findViewById(R.id.select_photo_tab_photopass);
		albumMagic = (TextView)findViewById(R.id.select_photo_tab_magic);
		albumBought = (TextView)findViewById(R.id.select_photo_tab_bought);
		cursor = (ImageView)findViewById(R.id.select_photo_cursor);
		viewPager = (ViewPager)findViewById(R.id.select_photo_viewPager);

		//绑定监听
		editImageButton.setOnClickListener(this);
		rtLayout.setOnClickListener(this);
		okButton.setOnClickListener(this);
		albumAll.setOnClickListener(this);
		albumPhotoPass.setOnClickListener(this);
		albumMagic.setOnClickListener(this);
		albumBought.setOnClickListener(this);
		
		//初始化数据列表
		allArrayList = new ArrayList<PhotoInfo>();
		boughtArrayList = new ArrayList<PhotoInfo>();
		photoPassArrayList = new ArrayList<PhotoInfo>();
		magicArrayList = new ArrayList<PhotoInfo>();
		photoPassArrayList.addAll(myApplication.photoPassPicList);
		magicArrayList.addAll(myApplication.magicPicList);
		transferPhotoItemInfoToPhotoInfo();
		PhotoInfo photoInfo = new PhotoInfo();
		allArrayList.add(0, photoInfo);
		photoPassArrayList.add(0, photoInfo);
		magicArrayList.add(0, photoInfo);
		boughtArrayList.add(0, photoInfo);
		
		//初始化storyViewPage的信息
		LayoutInflater mInflater = this.getLayoutInflater();
		listViews = new ArrayList<View>();
		listViews.add(mInflater.inflate(R.layout.select_photo_gridview_all, null));
		listViews.add(mInflater.inflate(R.layout.select_photo_gridview_all, null));
		listViews.add(mInflater.inflate(R.layout.select_photo_gridview_all, null));
		listViews.add(mInflater.inflate(R.layout.select_photo_gridview_all, null));
		
		Log.d(TAG, allArrayList.size()+"_"+photoPassArrayList.size()+"_"+magicArrayList.size()+"_"+boughtArrayList.size());
		selectPhotoViewPagerAdapter = new SelectPhotoViewPagerAdapter(this, listViews, allArrayList, photoPassArrayList, magicArrayList, boughtArrayList, mHandler, 1);
		viewPager.setAdapter(selectPhotoViewPagerAdapter);
		viewPager.setCurrentItem(0);
		viewPager.setOffscreenPageLimit(3);
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());

		//初始化数据
		screenWidth = ScreenUtil.getScreenWidth(this);
		if (myApplication.needScanPhoto()) {//如果刚进入这个页面，则onresume不需要重新扫描文件，所以直接将变量设为false，以免造成oncreate和onresume同时进行，造成collection数组越界
			myApplication.setneedScanPhoto(false);
		}

		okButton.setVisibility(View.VISIBLE);
		editImageButton.setVisibility(View.GONE);
		okButton.setText(String.format(getString(R.string.hasselectedphoto), 0, photocount));
		currentSelectedAlbum = ALBUM_ALL;
//		titleTextView.setText(getString(R.string.story_tab_bought));//更改文字
	}

	//相册切换的信息传递
	/**
	 * 存在的风险，加入还没全部加载完毕，切换到对应的相册，直接setadapter会造成数据丢失
	 * 需要set数据的时候，判断是否已经全部加载完毕。如果没有加载完毕，需要等待一会
	 */
	private Handler handler2 = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case ALBUM_ALL:
//				viewPhotoGridViewAdapter.setArrayList(photoPassArrayList);
				if (!scanAllFinish) {//扫描未完成，等待500ms
					handler2.sendEmptyMessageDelayed(ALBUM_ALL, 500);
				}
				break;

			case ALBUM_PHOTOPASS:
//				viewPhotoGridViewAdapter.setArrayList(magicArrayList);

				if (!scanPhotoPassFinish) {//扫描未完成，等待500ms
					handler2.sendEmptyMessageDelayed(ALBUM_PHOTOPASS, 500);
				}
				break;

			case ALBUM_MAGIC:
				//需要检查有没有最新的照片下载，如果有最新的下载照片，需要添加到列表中
//				viewPhotoGridViewAdapter.setArrayList(downLoadArrayList);
				if (!scanMagicFinish) {//扫描未完成，等待500ms
					handler2.sendEmptyMessageDelayed(ALBUM_MAGIC, 500);
				}
				break;

			case ALBUM_BOUGHT:
//				viewPhotoGridViewAdapter.setArrayList(otherArrayList);
				if (!scanBoughtFinish) {//扫描未完成，等待500ms
					handler2.sendEmptyMessageDelayed(ALBUM_BOUGHT, 500);
				}
				break;

			default:
				break;
			}
//			viewPhotoGridViewAdapter.setFlagString(selectedAlbum);
//			System.out.println("-------->notify");
//			viewPhotoGridViewAdapter.notifyDataSetChanged();
		};
	};

	//处理viewpager传递过来的数据
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {

//			Bundle bundle = msg.getData();
			switch (msg.what) {
//			switch (bundle.getInt("flag")) {
//			case 10://取消选中的时候
//				deletefromlist(bundle);
//				break;
//
//			case 11://选中的时候，首先判断list的长度，其次判断之前有没有选中过，如果选中过，则不做任何操作，没有选中过，则选中
//				addtolist(bundle);
//				break;
//
//			case 12:
////				viewPhotoGridViewAdapter.notifyDataSetChanged();
//				break;
				
			case 111:
//				if (msg.arg1!=0) {
					okButton.setText(String.format(getString(R.string.hasselectedphoto), msg.arg1, photocount));//更新button
//				}else {
//					okButton.setText(String.format(getString(R.string.selectedphoto), msg.arg1, photocount));//更新button
//				}
				break;
				
			default:
				break;
			}
		};
	};
	
	/**
	 * 将photoItemInfo的列表转成photoInfo的列表
	 */
	private void transferPhotoItemInfoToPhotoInfo() {
		for (int i = 0; i < myApplication.allPicList.size(); i++) {
			for (int j = 0; j < myApplication.allPicList.get(i).list.size(); j++) {
				allArrayList.add(myApplication.allPicList.get(i).list.get(j));
				myApplication.allPicList.get(i).list.get(j).isChecked = 1;
				myApplication.allPicList.get(i).list.get(j).isSelected = 0;
			}
		}
		
		for (int i = 0; i < myApplication.boughtPicList.size(); i++) {
			for (int j = 0; j < myApplication.boughtPicList.get(i).list.size(); j++) {
				boughtArrayList.add(myApplication.boughtPicList.get(i).list.get(j));
				myApplication.boughtPicList.get(i).list.get(j).isChecked = 1;
				myApplication.boughtPicList.get(i).list.get(j).isSelected = 0;
			}
		}
		
		for (int i = 0; i < myApplication.photoPassPicList.size(); i++) {
			myApplication.photoPassPicList.get(i).isChecked = 1;
			myApplication.photoPassPicList.get(i).isSelected = 0;
		}
		
		for (int i = 0; i < myApplication.magicPicList.size(); i++) {
			myApplication.magicPicList.get(i).isChecked = 1;
			myApplication.magicPicList.get(i).isSelected = 0;
		}

	}

	//获取photopass的照片,获取网络图片的url数组，并且转换为arraylist
	private void ScanPhotoPassPhotos(){
		System.out.println("---------->scan photopass");
		selectPhotoItemInfo = new PhotoInfo();
//		selectPhotoItemInfo.albumName = "";
		selectPhotoItemInfo.photoId = "";
		photoPassArrayList.add(selectPhotoItemInfo);

		photoList = ((MyApplication)getApplication()).photoPassPicList;
//		for (int l = 0; l < photoList.size(); l++) {//遍历所要添加的图片list
//			SelectPhotoItemInfo info = photoList.get(l);
//			selectPhotoItemInfo = new SelectPhotoItemInfo();
//			selectPhotoItemInfo.albumName = Common.ALBUM_PHOTOPASS;
//			selectPhotoItemInfo.photoId = info._id;//photoid，支付的时候要用的到
//			selectPhotoItemInfo.photoPathOrURL = info.originalUrl;
//			selectPhotoItemInfo.photoThumbnail = info.previewUrl;
//			selectPhotoItemInfo.photoThumbnail_512 = info.previewURL_512;
//			selectPhotoItemInfo.photoThumbnail_1024 = info.previewURL_1024;
//			selectPhotoItemInfo.isPayed = info.isPay + "";
//			selectPhotoItemInfo.photopassCode = info.photoCode;
//			selectPhotoItemInfo.isChecked = "false";
//			selectPhotoItemInfo.isSelected = "false";
//			selectPhotoItemInfo.showMask = "false";
//			photoPassArrayList.add(selectPhotoItemInfo);
//		}
		photoPassArrayList = photoList;
		if (photoPassArrayList.size()==0) {
			newToast.setTextAndShow(R.string.nophoto_in_PhotoPass, Common.TOAST_SHORT_TIME);
		}
		scanPhotoPassFinish = true;
	}

	//获取Magic的照片
//	private void ScanPhotos(ArrayList<PhotoInfo> arrayList, String filePath, String albumName){
//		System.out.println("---------->scan"+albumName);
//		selectPhotoItemInfo = new PhotoInfo();
//		selectPhotoItemInfo.albumName = "";
//		selectPhotoItemInfo.photoId = "";
//		selectPhotoItemInfo.lastModify = 0;
//		arrayList.add(selectPhotoItemInfo);
//		if (!sdCardExist) {//如果SD卡不存在
//			scanMagicFinish = true;
//			return;
//		}
//		File file = new File(filePath);
//		if (!file.exists()) {
//			file.mkdirs();
//		}
//		File[] files  = file.listFiles();
//		for (int i = 0; i < files.length; i++) {
//			if(files[i].getName().endsWith(".JPG")||files[i].getName().endsWith(".jpg")){
//				if (files[i].length() > 0) {//扫描到文件
//					selectPhotoItemInfo = new PhotoInfo();
//					selectPhotoItemInfo.photoPathOrURL = files[i].getPath();
//					selectPhotoItemInfo.lastModify = files[i].lastModified();
//					selectPhotoItemInfo.isChecked = 0;
//					selectPhotoItemInfo.isSelected = 0;
//					selectPhotoItemInfo.showMask = 0;
//					selectPhotoItemInfo.albumName = albumName;
//					arrayList.add(selectPhotoItemInfo);
//				}
//			}
//		}
//	}

	//检查是否有最新的照片
	private void checkNewPhotos(final String filePath, final ArrayList<PhotoInfo> arrayList, final String albumName) {
		new Thread(){
			public void run() {
				if (!sdCardExist) {//如果SD卡不存在
					return;
				}
				File file = new File(filePath);
				if (!file.exists()) {
					file.mkdirs();
				}
				File[] files  = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					if(files[i].getName().endsWith(".JPG")||files[i].getName().endsWith(".jpg")){
						if (files[i].length() > 0) {//扫描到有效文件
							if (arrayList.size() > 1) {//原来有下载的文件，要和第二个位置的时间进行比较，并且插在第二个位置即可
								if (files[i].lastModified() > arrayList.get(1).lastModify) {//有新的照片
									selectPhotoItemInfo = new PhotoInfo();
									selectPhotoItemInfo.photoPathOrURL = files[i].getPath();
									selectPhotoItemInfo.lastModify = files[i].lastModified();
									selectPhotoItemInfo.isChecked = 0;
									selectPhotoItemInfo.isSelected = 0;
									selectPhotoItemInfo.showMask = 0;
//									selectPhotoItemInfo.albumName = albumName;
									arrayList.add(1, selectPhotoItemInfo);
								}else {//没有新的照片

								}
							}else {//原来没有最新的，直接添加在后面即可
								selectPhotoItemInfo = new PhotoInfo();
								selectPhotoItemInfo.photoPathOrURL = files[i].getPath();
								selectPhotoItemInfo.lastModify = files[i].lastModified();
								selectPhotoItemInfo.isChecked = 0;
								selectPhotoItemInfo.isSelected = 0;
								selectPhotoItemInfo.showMask = 0;
//								selectPhotoItemInfo.albumName = albumName;
								arrayList.add(selectPhotoItemInfo);
							}
						}
					}
				}
				Bundle bundle = new Bundle();
				bundle.putInt("flag", 12);
				Message message = mHandler.obtainMessage();
				message.setData(bundle);
				mHandler.sendMessage(message);
			};
		}.start();
	}

	//获取其他相册的照片
//	private void ScanOtherPhotos(){
//		System.out.println("-------->scan other");
//		selectPhotoItemInfo = new SelectPhotoItemInfo();
//		selectPhotoItemInfo.Id = "";
//		selectPhotoItemInfo.photoPathOrURL = "";
//		selectPhotoItemInfo.photoThumbnail = "";
//		otherArrayList.add(selectPhotoItemInfo);
//		if (!sdCardExist) {//如果SD卡不存在
//			scanOtherFinish = true;
//			return;
//		}
//		ContentResolver testcr = getContentResolver();
//		String[] projection = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
//		Cursor cur = testcr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//				projection, null, null, MediaStore.Images.Media._ID+" DESC");//查找media中全部的图片路径
//		for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
//			int _idColumn = cur.getColumnIndex(Thumbnails._ID);
//			int dataColumn = cur.getColumnIndex(Thumbnails.DATA);
//			int _id = cur.getInt(_idColumn);
//			String image_path = cur.getString(dataColumn);
//			if ((image_path.endsWith(".jpeg") || image_path.endsWith(".jpg") || 
//					image_path.endsWith(".JPG")|| image_path.endsWith(".JPEG"))&&(new File(image_path).length()!=0)){//筛选后缀名
//				selectPhotoItemInfo = new SelectPhotoItemInfo();
//				selectPhotoItemInfo.Id = String.valueOf(_id);
//				selectPhotoItemInfo.photoPathOrURL = image_path;
//				selectPhotoItemInfo.isChecked = "false";
//				selectPhotoItemInfo.isSelected = "false";
//				selectPhotoItemInfo.showMask = "false";
//				if (image_path.contains(Common.ALBUM_FILE_DOWLOAD_NAME)) {//不做操作
//
//				}else if (image_path.contains(Common.ALBUM_FILE_PICTURE_NAME)) {//不做操作
//
//				}else {//other的照片
//					selectPhotoItemInfo.albumName = Common.ALBUM_OTHERS;
//					otherArrayList.add(selectPhotoItemInfo);
//				}
//			}
//		}
//		cur.close();
//		scanOtherFinish = true;
//	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (myApplication.needScanPhoto()) {//需要刷新
			System.out.println("need scan photo--------------");
			checkNewPhotos(Common.PHOTO_SAVE_PATH, magicArrayList, Common.ALBUM_MAGIC);
			myApplication.setneedScanPhoto(false);
		}else {
			System.out.println("has new edit photo false");
		}
	}


	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.rlrt://返回按钮
			finish();
			break;

		case R.id.button1://选择确定按钮
			photoURLlist = selectPhotoViewPagerAdapter.getPhotoURLlist();
			if (photoURLlist.size()==0) {
				newToast.setTextAndShow(R.string.select_photos, Common.TOAST_SHORT_TIME);
			}else if (photoURLlist.size()<photocount) {
				newToast.setTextAndShow(R.string.select_photos, Common.TOAST_SHORT_TIME);
			}else {
				if (activity.equals("detailproductactivity")||activity.equals("previewproductactivity")) {//从详细商品界面进入
					intent= new Intent(this, PreviewProductActivity.class);
					intent.putExtra("name", getIntent().getStringExtra("name"));
					intent.putExtra("price", getIntent().getStringExtra("price"));
					intent.putExtra("introduce", getIntent().getStringExtra("introduce"));
					intent.putExtra("productImage", getIntent().getStringExtra("productImage"));
					intent.putExtra("photopath", photoURLlist);
					intent.putExtra("storeid", getIntent().getStringExtra("storeid"));
					intent.putExtra("productid", getIntent().getStringExtra("productid"));
					startActivity(intent);
				}else if (activity.equals("makegiftactivity"))  {//从其他界面进来，返回新选择的照片
					intent = new Intent();
					intent.putExtra("photopath", photoURLlist);
					setResult(20,intent);
					finish();
				}else if (activity.equals("submitorderactivity"))  {//从其他界面进来，返回新选择的照片
					intent = new Intent();
					intent.putExtra("photopath", photoURLlist);
					setResult(20,intent);
					finish();
				}else if (activity.equals("cartactivity")) {
					intent = new Intent();
					intent.putExtra("photopath", photoURLlist);
					setResult(20,intent);
					finish();
				}
			}
			break;

		case R.id.select_photo_tab_all:
			viewPager.setCurrentItem(ALBUM_ALL);
			break;
			
		case R.id.select_photo_tab_photopass:
			viewPager.setCurrentItem(ALBUM_PHOTOPASS);
			break;
		
		case R.id.select_photo_tab_magic:
			viewPager.setCurrentItem(ALBUM_MAGIC);
			break;
		
		case R.id.select_photo_tab_bought:
			viewPager.setCurrentItem(ALBUM_BOUGHT);
			break;
			
		default:
			break;
		}
	}

	/**
	 * 页卡切换监听
	 */
	private class MyOnPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageSelected(int arg0) {
			Animation animation = new TranslateAnimation(screenWidth / 4 * currentSelectedAlbum, screenWidth / 4 * arg0, 0, 0);
			System.out.println("-------->pageselected "+ arg0);
			currentSelectedAlbum = arg0;
			animation.setFillAfter(true);
			animation.setDuration(300);
			cursor.startAnimation(animation);
			selectPhotoViewPagerAdapter.notifyDataSetChangedCurrentTab(arg0);
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}
	
//	private class ScanPhotosThread extends Thread{
//
//		@Override
//		public void run() {
//			System.out.println("------->run");
//			ScanPhotoPassPhotos();
//			//此处需要传递值出去，先将扫描完的显示出来
//			handler2.sendEmptyMessage(ALBUM_PHOTOPASS);
//
//			//其他的在后台继续执行
//			if (!scanPhotoPassFinish) {
//				ScanPhotoPassPhotos();
//			}
//			if (!scanMagicFinish) {
//				ScanPhotos(magicArrayList, Common.PHOTO_SAVE_PATH, Common.ALBUM_MAGIC);
//
//
//				Collections.sort(magicArrayList);
//				scanMagicFinish = true;
//
//			}
//			if (!scanAllFinish) {
//				//				ScanDownloadPhotos();
////				ScanPhotos(downLoadArrayList, Common.PHOTO_DOWNLOAD_PATH, Common.ALBUM_DOWNLOAD);
//
////				Collections.sort(downLoadArrayList);
////				scanDownloadFinish = true;
//
//			}
////			if (!scanOtherFinish) {
////				ScanOtherPhotos();
////			}
//		}
//	}

	


	Handler mhHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 1:
				System.out.println("ok");
//				viewPhotoGridViewAdapter.notifyDataSetChanged();
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
		//检查已添加的list中有没有相同的数据
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
		if (photoURLlist.size()!=0) {
			okButton.setText(String.format(getString(R.string.hasselectedphoto), photoURLlist.size(), photocount));//更新button
			okButton.setTextColor(Color.WHITE);
		}else {
			okButton.setText(String.format(getString(R.string.selectedphoto), photoURLlist.size(), photocount));//更新button
			okButton.setTextColor(this.getResources().getColor(R.color.gray_light));
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
		if (photoURLlist.size()<=8) {
			for (int j = 0; j < photoURLlist.size(); j++) {
				if (b.getString("pathOrUrl").equals(photoURLlist.get(j).photoPathOrURL)) {
					result = j;
					resultString = true;
				}
			}
			if (resultString) {
				System.out.println("之前点过了");
				newToast.setTextAndShow(R.string.photo_selected, Common.TOAST_SHORT_TIME);
				result = 0;
				resultString = false;
			}else {//同样，如果直接在循环中加入的话，会对for循环产生影响
				System.out.println("position"+b.getInt("position"));
				System.out.println("path"+b.getString("pathOrUrl"));
				PhotoInfo selectedInfo = new PhotoInfo();
//				switch (currentSelectedAlbum) {
//				case ALBUM_ALL:
//					selectedInfo.albumName = Common.ALBUM_ALL;//所在的图册
//					break;
//					
//				case ALBUM_PHOTOPASS:
//					selectedInfo.albumName = Common.ALBUM_PHOTOPASS;//所在的图册
//					break;
//
//				case ALBUM_MAGIC:
//					selectedInfo.albumName = Common.ALBUM_MAGIC;//所在的图册
//					break;
//
//				case ALBUM_BOUGHT:
//					selectedInfo.albumName = Common.ALBUM_BOUGHT;//所在的图册
//					break;
//
//				default:
//					break;
//				}
				selectedInfo.photoPathOrURL = b.getString("pathOrUrl");//图片路径
				if (currentSelectedAlbum == ALBUM_PHOTOPASS) {
					selectedInfo.photoThumbnail_512 = photoPassArrayList.get(b.getInt("position")).photoThumbnail_512;//图片的缩略图,只有PP有，其他相册没有
					selectedInfo.photoId = photoPassArrayList.get(b.getInt("position")).photoId;
				}else {
//					selectedInfo.Id = b.getString("Id");//缩略图表中对应的id，此项PP中没有
				}
				photoURLlist.add(selectedInfo);//加入到list中
				okButton.setText(String.format(getString(R.string.hasselectedphoto), photoURLlist.size(), photocount));//更新button
				okButton.setTextColor(Color.WHITE);
				result = 0;
			}
			//				mPagerAdapter.notifyDataSetChanged();
		}else {
			newToast.setTextAndShow(String.format(getString(R.string.limit_photos), photocount), Common.TOAST_SHORT_TIME);
		}
	}


}
