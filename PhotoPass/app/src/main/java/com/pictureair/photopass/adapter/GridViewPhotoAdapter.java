package com.pictureair.photopass.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.ScreenUtil;

import java.util.ArrayList;

@SuppressLint("NewApi") public class GridViewPhotoAdapter extends BaseAdapter
{
	private LayoutInflater mInflater;	 
	private ArrayList<PhotoInfo> arrayList;
	private String flagString;
	static int requestwidth;
	private ImageLoader imageLoader;
	private Context  c;
	//构造函数
	/**
	 * 
	 * @param context
	 * @param arraylist 相册路径数组
	 * @param gridView
	 * @param flag 每个相册的标记
	 */
	public GridViewPhotoAdapter(Context context, ArrayList<PhotoInfo> arraylist, GridView gridView, String flag)
	{
		mInflater = LayoutInflater.from(context);
		c=context;
		arrayList = arraylist;
		flagString = flag;
		imageLoader = ImageLoader.getInstance();
	}

	public void setArrayList(ArrayList<PhotoInfo> arrayList) {
		this.arrayList = arrayList;
	}
	public int getCount()
	{
		return arrayList.size();
	}

	public Object getItem(int position)
	{
		return arrayList.get(position);
	}
	public void setFlagString(String flagString) {
		this.flagString = flagString;
	}
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		int type = 0;
		if (position==0) {
			type = 1;
		}else {
			type = 0;
		}
		return type;
	}

	public View getView(int position,View convertView,ViewGroup parent)
	{
		requestwidth = (ScreenUtil.getScreenWidth(c)-4*5)/3;//（屏宽-4*3）/3,3为间隔间距
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(requestwidth, requestwidth);


		HolderView holderView;

		if(convertView != null)
		{
			holderView = (HolderView) convertView.getTag();
		}else {
			holderView = new HolderView();
			convertView = mInflater.inflate(R.layout.grid_row, null);
			holderView.imageView_photo = (ImageView) convertView.findViewById(R.id.imageItem);
			holderView.imageview_select = (ImageView)convertView.findViewById(R.id.imageView_Select);
			holderView.imageview_maskImageView = (ImageView)convertView.findViewById(R.id.imageView_mask);
			convertView.setTag(holderView);
		}
		PhotoInfo selectPhotoItemInfo = arrayList.get(position);
		String urlString = selectPhotoItemInfo.photoThumbnail;//获取到缩略图路径
		//			Log.d("loadphoto", urlString);
		if (position==0) {
			holderView.imageview_select.setLayoutParams(params);
			holderView.imageview_select.setScaleType(ScaleType.CENTER);
			holderView.imageview_maskImageView.setVisibility(View.INVISIBLE);
			holderView.imageView_photo.setVisibility(View.INVISIBLE);
			holderView.imageview_select.setVisibility(View.VISIBLE);
			holderView.imageview_select.setImageResource(R.drawable.camera_in_album);
			holderView.imageview_select.setBackgroundColor(Color.WHITE);
		}else {
			holderView.imageView_photo.setLayoutParams(params);//设置图片显示的大小，使得每张图片显示为正方形
			holderView.imageview_maskImageView.setLayoutParams(params);//设置蒙版的大小
			holderView.imageView_photo.setScaleType(ScaleType.CENTER_CROP);
			holderView.imageView_photo.setVisibility(View.VISIBLE);
			if (selectPhotoItemInfo.isChecked == 1) {//如果已经有点过了
				holderView.imageview_maskImageView.setVisibility(View.VISIBLE);
				holderView.imageview_select.setVisibility(View.VISIBLE);
				System.out.println(selectPhotoItemInfo.isChecked + "_______");
				if (selectPhotoItemInfo.isSelected == 1) {
					holderView.imageview_select.setImageResource(R.drawable.sel2);
					System.out.println("isSelected------>" + selectPhotoItemInfo.isSelected);
				}else {
					System.out.println("no select--》"+selectPhotoItemInfo.isSelected);
					holderView.imageview_select.setImageResource(R.drawable.sel1);
				}
				holderView.imageview_select.setBackgroundColor(Color.TRANSPARENT);
			} else {
				holderView.imageview_maskImageView.setVisibility(View.INVISIBLE);
				holderView.imageview_select.setVisibility(View.INVISIBLE);
			}
			if (flagString.equals(Common.ALBUM_MAGIC)) {
				System.out.println(flagString+"开始加载图片"+urlString);
				if (urlString.equals("")) {//如果路径为空，则表明拍完照片或者编辑之后，缩略图没有生成，所以加载原图
					System.out.println("加载原图---------->"+selectPhotoItemInfo.photoPathOrURL);
					//						UniversalImageLoadTool.loadImage("file://"+selectPhotoItemInfo.photoPathOrURL, holderView.imageView_photo);
					imageLoader.displayImage("file://"+selectPhotoItemInfo.photoPathOrURL, holderView.imageView_photo);
//					holderView.imageView_photo.setImageBitmap(getImageThumbnail(selectPhotoItemInfo.photoPathOrURL));
					System.out.println("-------->原图加载完毕");
				}else {
					System.out.println("加载缩略图");
					//						UniversalImageLoadTool.loadImage("file://"+urlString, holderView.imageView_photo);
					imageLoader.displayImage("file://"+urlString, holderView.imageView_photo);
				}
			}else if (flagString.equals(Common.ALBUM_PHOTOPASS)) {
				if ("1".equals(selectPhotoItemInfo.isPayed)) {//如果已经购买，显示512的缩略图
					System.out.println(flagString+"开始加载512图片" + selectPhotoItemInfo.photoThumbnail_512);
					//						UniversalImageLoadTool.loadImage(Common.PHOTO_URL + selectPhotoItemInfo.photoThumbnail_512, holderView.imageView_photo);
					imageLoader.displayImage(Common.PHOTO_URL + selectPhotoItemInfo.photoThumbnail_512, holderView.imageView_photo);

				}else {//反之显示128的缩略图

					System.out.println(flagString+"开始加载128图片"+urlString);
					//						UniversalImageLoadTool.loadImage(urlString, holderView.imageView_photo);
					imageLoader.displayImage(urlString, holderView.imageView_photo);
				}
			}
		}
		return convertView;
	}
	class HolderView{
		ImageView imageView_photo;
		ImageView imageview_select;
		ImageView imageview_maskImageView;
	}

	private Bitmap getImageThumbnail(String imagePath) {  

		Bitmap bitmap = null;  

		BitmapFactory.Options options = new BitmapFactory.Options();  

		options.inJustDecodeBounds = true;  

		// 获取这个图片的宽和高，注意此处的bitmap为null  

		bitmap = BitmapFactory.decodeFile(imagePath, options);  

		options.inJustDecodeBounds = false; // 设为 false  

		// 计算缩放比  

		int h = options.outHeight;  

		int w = options.outWidth;  

		int beWidth = w / requestwidth;  

		int beHeight = h / requestwidth;  

		int be = 1;  

		if (beWidth < beHeight) {  

			be = beWidth;  

		} else {  

			be = beHeight;  

		}  

		if (be <= 0) {  

			be = 1;  

		}  

		options.inSampleSize = be;  

		// 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false  

		bitmap = BitmapFactory.decodeFile(imagePath, options);  

		// 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象  

		bitmap = ThumbnailUtils.extractThumbnail(bitmap, requestwidth, requestwidth,  

				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);  

		return bitmap;  

	}

}