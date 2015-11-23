package com.pictureAir;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureAir.GalleryWidget.GalleryViewPager;
import com.pictureAir.GalleryWidget.UrlPagerAdapter;
import com.pictureAir.entity.PhotoInfo;
import com.pictureAir.service.DownloadService;
import com.pictureAir.util.AppManager;
import com.pictureAir.util.Common;
import com.pictureAir.widget.MyToast;
import com.pictureAir.widget.SharePop;
/**
 * 预览图片，可以进行编辑，分享，下载和制作礼物的操作
 * @author bauer_bao
 *
 */
@SuppressLint({ "FloatMath", "NewApi" }) public class PreviewPhotoActivity extends BaseActivity implements OnClickListener{
	//工具条
	private TextView editButton;
	private TextView shareButton;
	private TextView downloadButton;
	private TextView makegiftButton;
	
	private TextView locationTextView;
	
	private GalleryViewPager mViewPager;
	private ImageView returnImageView;

	private MyToast newToast;
	private SharePop sharePop;
	private MyApplication myApplication;
	
	//图片显示框架
	private ArrayList<PhotoInfo> photolist;
	private ArrayList<PhotoInfo> targetphotolist;
	private int position;
	private int flag;
	
	//图片布局宽高
	public static int fraWidth;
	public static int fraHeight;
	
	private boolean isEdited = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.previewphoto_activity);
		AppManager.getInstance().addActivity(this);
		init();
	}

	private void init() {
		// TODO Auto-generated method stub
		newToast = new MyToast(this);
		returnImageView = (ImageView)findViewById(R.id.button1_shop_rt);

		locationTextView = (TextView)findViewById(R.id.preview_location);
		editButton = (TextView)findViewById(R.id.preview_edit);
		shareButton = (TextView)findViewById(R.id.preview_share);
		downloadButton = (TextView)findViewById(R.id.preview_download);
		makegiftButton = (TextView)findViewById(R.id.preview_makegift);
		
		myApplication = (MyApplication) getApplication();
		
		returnImageView.setOnClickListener(this);
		editButton.setOnClickListener(this);
		shareButton.setOnClickListener(this);
		downloadButton.setOnClickListener(this);
		makegiftButton.setOnClickListener(this);

		System.out.println("photoid ===== "+getIntent().getStringExtra("photoId"));

		//获取intent传递过来的信息
		photolist = (ArrayList<PhotoInfo>)getIntent().getSerializableExtra("photos");//获取图片路径list
		targetphotolist = (ArrayList<PhotoInfo>)getIntent().getSerializableExtra("targetphotos");
		position = Integer.valueOf(getIntent().getStringExtra("position"));
		flag = getIntent().getIntExtra("flag", 0);
		//如果是从viewphotoactivity界面进来，因为第一项为拍照按钮，所以这里的数据都要删除第一项
		if ("viewphotoactivity".equals(getIntent().getStringExtra("activity"))) {
			photolist.remove(0);
			targetphotolist.remove(0);
			position--;
		}else {//如果是其他界面进来，则只删除目标list中的第一项
			if (targetphotolist != null && targetphotolist.size() > 0) {
				targetphotolist.remove(0);
			}
			System.out.println("need not to reduce or delete");
		}
		
		mViewPager = (GalleryViewPager)findViewById(R.id.viewer);
//		if(flag.equals(Common.ALBUM_PHOTOPASS)){
			//网络图片
			UrlPagerAdapter pagerAdapter = new UrlPagerAdapter(this, photolist);
//	        mViewPager.setAdapter(pagerAdapter);
//		}else{
            //本地图片
//			FilePagerAdapter pagerAdapter = new FilePagerAdapter(this, photolist);
			mViewPager.setOffscreenPageLimit(3);
	        mViewPager.setAdapter(pagerAdapter);
//		}
		mViewPager.setCurrentItem(position, true);
//		locationTextView.setText(getIntent().getStringExtra("locationName"));
		locationTextView.setText(getString(R.string.story_tab_magic));
//		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
//			
//			@Override
//			public void onPageSelected(int arg0) {
//				// TODO Auto-generated method stub
//			}
//			
//			@Override
//			public void onPageScrolled(int arg0, float arg1, int arg2) {
//				// TODO Auto-generated method stub
//				
//			}
//			
//			@Override
//			public void onPageScrollStateChanged(int arg0) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
//		locationTextView.setText(photolist.get(position).location);
		sharePop = new SharePop(this);
	}
	@SuppressLint("ClickableViewAccessibility") @Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.button1_shop_rt:
			finish();
			break;
			
		case R.id.preview_edit://编辑
			Intent intent2 = new Intent(this, EditPhotoActivity.class);
			if (isEdited) {//已经编辑过，取targetlist中的值
//				intent2.putExtra("photoURL", targetphotolist.get(mViewPager.getCurrentItem()).photoPathOrURL);
//				intent2.putExtra("album", flag);
//				intent2.putExtra("id", targetphotolist.get(mViewPager.getCurrentItem()).photoId);
				intent2.putExtra("photo", targetphotolist.get(mViewPager.getCurrentItem()));
			}else {//没有编辑，取正常的值
//				intent2.putExtra("photoURL", photolist.get(mViewPager.getCurrentItem()).photoPathOrURL);
//				intent2.putExtra("album", getIntent().getStringExtra("flag"));
//				intent2.putExtra("id", photolist.get(mViewPager.getCurrentItem()).photoId);
				intent2.putExtra("photo", photolist.get(mViewPager.getCurrentItem()));
			}
			startActivityForResult(intent2, 1);
			break;
		case R.id.preview_share:
			System.out.println("start share=" + photolist.get(mViewPager.getCurrentItem()).photoPathOrURL);
			if (isEdited) {//编辑后
				sharePop.setshareinfo(targetphotolist.get(mViewPager.getCurrentItem()).photoPathOrURL, null, "local");
			}else {//编辑前
				//判断图片是本地还是网路图片
				if (getIntent().getIntExtra("flag", 0) == 1) {//网络图片
					sharePop.setshareinfo(null, photolist.get(mViewPager.getCurrentItem()).photoPathOrURL, "online");
				}else {
					sharePop.setshareinfo(photolist.get(mViewPager.getCurrentItem()).photoPathOrURL, null, "local");
				}
				
			}
			sharePop.showAtLocation(v, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
			break;
			
		case R.id.preview_download://下载,如果不是pp的照片，提示不需要下载，如果是pp的照片，并且没有支付，提示购买，如果已经购买，如果没有下载，则下载，否则提示已经下载
			if (isEdited) {//编辑后
				newToast.setTextAndShow(R.string.neednotdownload, Common.TOAST_SHORT_TIME);
			}else {//编辑前
				if (getIntent().getIntExtra("flag", 0) == 1) {//是pp的照片
					ArrayList<PhotoInfo> list = new ArrayList<PhotoInfo>();
//					PhotoInfo photoInfo = new PhotoInfo();
//					photoInfo.photoPathOrURL = photolist.get(mViewPager.getCurrentItem()).photoPathOrURL;
////					photoInfo.photoId = 
					list.add(photolist.get(mViewPager.getCurrentItem()));
					Intent service = new Intent(this, DownloadService.class);
					Bundle bundle = new Bundle();
					bundle.putParcelableArrayList("photos", list);
					service.putExtras(bundle);
					startService(service);
					System.out.println("start");
				}else {
					newToast.setTextAndShow(R.string.neednotdownload, Common.TOAST_SHORT_TIME);
				}
				
			}
			break;
			
		case R.id.preview_makegift:
			System.out.println("makegift");
			Intent intent = new Intent(this,MakegiftActivity.class);
			//判断是否已经被编辑过
			if (isEdited) {//已经被编辑过，那么取得是targetList中的值
				intent.putExtra("selectPhoto", targetphotolist.get(mViewPager.getCurrentItem()));
			}else {//没有编辑过，直接获取之前的值
//				if (Common.ALBUM_PHOTOPASS.equals(flag)) {
//					intent.putExtra("thumbnailURL", photolist.get(mViewPager.getCurrentItem()).photoThumbnail_512);
//					intent.putExtra("photoId", getIntent().getStringExtra("photoId"));
//					intent.putExtra("albums", flag);
//				}else {
//					intent.putExtra("photopath", photolist.get(mViewPager.getCurrentItem()).photoPathOrURL);
//					intent.putExtra("id", getIntent().getStringExtra("id"));
//					intent.putExtra("albums", flag);
//				}
				intent.putExtra("selectPhoto", photolist.get(mViewPager.getCurrentItem()));
			}
			startActivity(intent);

			break;
			
		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1) {
			if (resultCode == 11) {
				//保存完图片的处理
				System.out.println("save success");
				//1.获取新图片的数据
				PhotoInfo selectPhotoItemInfo = new PhotoInfo();
				selectPhotoItemInfo.photoPathOrURL = data.getStringExtra("photoUrl");
				//2.将新图片插入到targetList中
				targetphotolist.add(0, selectPhotoItemInfo);
				//3.修改viewPager中的值为targetList
				mViewPager.setAdapter(new UrlPagerAdapter(this, targetphotolist));
				mViewPager.setCurrentItem(0, true);
				isEdited = true;
				myApplication.setneedScanPhoto(true);
				myApplication.scanMagicFinish = false;
				flag = 0;
			}
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		AppManager.getInstance().killActivity(this);
	}
}