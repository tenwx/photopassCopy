package com.pictureair.photopass.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.util.UniversalImageLoadTool;

import java.util.ArrayList;

/**
 * 选择照片和预览照片的gridView适配器
 * 分两类，索引为0的项需要打开camera
 * @author bauer_bao
 *
 */
public class ViewPhotoGridViewAdapter extends BaseAdapter
{
	private LayoutInflater mInflater;	 
	private ArrayList<PhotoInfo> arrayList;
	private static int requestwidth;
	private Context  c;
	private final static String TAG = "ViewPhotoGridViewAdapter";
	//构造函数
	/**
	 * 
	 * @param context
	 * @param arraylist 相册路径数组
	 */
	public ViewPhotoGridViewAdapter(Context context, ArrayList<PhotoInfo> arraylist)
	{
		mInflater = LayoutInflater.from(context);
		c=context;
		arrayList = arraylist;
		for (int i = 0; i < arrayList.size(); i++) {
			System.out.println("size = "+ arrayList.size() + "_"+arrayList.get(i));
		}
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

	public long getItemId(int position)
	{
		return position;
	}

	/**
	 * 开始选择照片
	 * @param isChecked false：取消选择状态，true：显示选择状态
	 * @param isSelected false：默认选择的图片，true：选中的时候的图片
	 */
	public void startSelectPhoto(int isChecked, int isSelected) {
		PhotoInfo selectPhotoItemInfo;
		for (int i = 0; i < arrayList.size(); i++) {
			selectPhotoItemInfo = arrayList.get(i);
			selectPhotoItemInfo.isChecked = isChecked;
			selectPhotoItemInfo.isSelected = isSelected;
			if (isSelected == 0) {
				selectPhotoItemInfo.showMask = 0;//遮罩层；0:不显示；1:显示
			} else {
				selectPhotoItemInfo.showMask = 1;//遮罩层；0:不显示；1:显示
			}
		}
		notifyDataSetChanged();
	}

	/**
	 * 刷新view
	 * @param index
	 * @param view
	 * @param mode 0:选择，1：预览
	 */
	public void refreshView(int index, View view, int mode){
		HolderView holder = (HolderView)view.getTag();
		if (mode == 0) {
			if(arrayList.get(index).isChecked == 1){
				holder.imageview_select.setImageResource(R.drawable.sel2);
				holder.imageview_select.setVisibility(View.VISIBLE);
				holder.imageview_select.setBackgroundColor(Color.TRANSPARENT);
			}else{
				holder.imageview_select.setVisibility(View.INVISIBLE);
//				holder.imageview_maskImageView.setVisibility(View.INVISIBLE);
			}
		}else if (mode == 1) {
			if (arrayList.get(index).isChecked == 1) {
				if (arrayList.get(index).isSelected == 1) {//选中
					holder.imageview_select.setImageResource(R.drawable.sel2);
				}else {//未选中
					holder.imageview_select.setImageResource(R.drawable.sel3);

				}
			}
		}

		if (arrayList.get(index).showMask == 0) {//隐藏遮罩
			holder.imageview_maskImageView.setVisibility(View.GONE);
		} else {//显示遮罩
			holder.imageview_maskImageView.setVisibility(View.VISIBLE);
		}
	}


	public View getView(int position,View convertView,ViewGroup parent)
	{
//				PictureAirLog.v(TAG, "getView----------->" + arrayList.get(position).photoPathOrURL);
				PictureAirLog.v(TAG, "getView----------->" + position);
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
//		if (position==0) {
//			holderView.imageview_select.setLayoutParams(params);
//			holderView.imageview_select.setScaleType(ScaleType.CENTER);
//			holderView.imageview_maskImageView.setVisibility(View.INVISIBLE);
//			holderView.imageView_photo.setVisibility(View.INVISIBLE);
//			holderView.imageview_select.setVisibility(View.VISIBLE);
//			holderView.imageview_select.setImageResource(R.drawable.camera_in_album);
//			holderView.imageview_select.setBackgroundColor(Color.WHITE);
//		}else {
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
					holderView.imageview_select.setImageResource(R.drawable.sel3);
				}
				holderView.imageview_select.setBackgroundColor(Color.TRANSPARENT);
			} else {
				holderView.imageview_maskImageView.setVisibility(View.VISIBLE);
				holderView.imageview_select.setVisibility(View.INVISIBLE);
			}
			if (selectPhotoItemInfo.onLine == 0) {
				System.out.println("开始加载图片");
				System.out.println("加载原图---------->" + selectPhotoItemInfo.photoPathOrURL);
				UniversalImageLoadTool.loadImage("file://" + selectPhotoItemInfo.photoPathOrURL, holderView.imageView_photo);
				System.out.println("-------->原图加载完毕");
			}else if (selectPhotoItemInfo.onLine == 1) {
				if (selectPhotoItemInfo.isPayed == 1) {//如果已经购买，显示512的缩略图
					System.out.println("开始加载512图片" + selectPhotoItemInfo.photoThumbnail_512);
					UniversalImageLoadTool.loadImage(Common.PHOTO_URL + selectPhotoItemInfo.photoThumbnail_512, holderView.imageView_photo);
				}else {//反之显示128的缩略图
					System.out.println("开始加载128图片"+selectPhotoItemInfo.photoThumbnail);
					UniversalImageLoadTool.loadImage(selectPhotoItemInfo.photoThumbnail, holderView.imageView_photo);
				}
			}

		if (selectPhotoItemInfo.showMask == 0) {//隐藏遮罩
			holderView.imageview_maskImageView.setVisibility(View.GONE);
		} else {//显示遮罩
			holderView.imageview_maskImageView.setVisibility(View.VISIBLE);
		}

		return convertView;
	}
	class HolderView{
		ImageView imageView_photo;
		ImageView imageview_select;
		ImageView imageview_maskImageView;
	}

}