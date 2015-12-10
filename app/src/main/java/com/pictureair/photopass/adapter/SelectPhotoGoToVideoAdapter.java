package com.pictureair.photopass.adapter;


import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import com.pictureair.photopass.entity.PhotoInfo;

import com.pictureair.photopass.R;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.UniversalImageLoadTool;
import com.pictureair.photopass.util.ScreenUtil;
import java.util.ArrayList;

/**
 * 选择照片发给服务器做视频
 * 
 * @author bass
 *
 */
public class SelectPhotoGoToVideoAdapter extends BaseAdapter {
	// private static final String TAG = "SelectPhotoGoToVideoAdapter";
	// // private ArrayList<PhotoInfo> allList;
	// private Context context;
	// private MyToast myToast;
	// private GridView gridView;
	// private ArrayList<PhotoInfo> photoURLlist;// 已经选择了的图片列表
	//
	// public SelectPhotoGoToVideoAdapter(Context context,
	// ArrayList<PhotoInfo> photoURLlist) {
	// this.context = context;
	// this.photoURLlist = photoURLlist;
	// }
	//
	// public void setGridView(GridView gridView) {
	// this.gridView = gridView;
	// }
	//
	// /**
	// * update gridview 单条数据
	// *
	// * @param item
	// * 新数据对象
	// */
	// public void updateItemData(PhotoInfo item) {
	// Message msg = Message.obtain();
	// int ids = -1;
	// // 进行数据对比获取对应数据在list中的位置
	// for (int i = 0; i < photoURLlist.size(); i++) {
	// if (photoURLlist.get(i).photoId == item.photoId) {
	// ids = i;
	// }
	// }
	// msg.arg1 = ids;
	// // // 更新mDataList对应位置的数据
	// // mDataList.set(ids, item);
	// // handle刷新界面
	// handler.sendMessage(msg);
	// }
	//
	// private Handler handler = new Handler() {
	// public void handleMessage(android.os.Message msg) {
	// updateItem(msg.arg1);
	// };
	// };
	//
	// /**
	// * 刷新指定item
	// *
	// * @param index
	// * item在gridView中的位置
	// */
	// private void updateItem(int index) {
	// if (gridView == null) {
	// return;
	// }
	//
	// // 获取当前可以看到的item位置
	// int visiblePosition = gridView.getFirstVisiblePosition();
	// // 如添加headerview后 firstview就是hearderview
	// // 所有索引+1 取第一个view
	// // View view = listview.getChildAt(index - visiblePosition + 1);
	// // 获取点击的view
	// View view = gridView.getChildAt(index - visiblePosition);
	// // TextView txt = (TextView) view.findViewById(R.id.textView1);
	// // 获取mDataList.set(ids, item);更新的数据
	// // MyListItem data = (MyListItem) getItem(index);
	// // 重新设置界面显示数据
	// // txt.setText(data.getData());
	// }
	//
	// @Override
	// public View getView(int position, View convertView, ViewGroup parent) {
	// // TODO Auto-generated method stub
	// if (convertView == null) {
	// convertView = LayoutInflater.from(context).inflate( R.layout.grid_row,
	// null);
	// }
	// holderView.imageview_select =
	// (ImageView)convertView.findViewById(R.id.imageView_Select);
	// holderView.imageview_maskImageView =
	// (ImageView)convertView.findViewById(R.id.imageView_mask);
	// TextView txt = (TextView) convertView.findViewById(R.id.textView1);
	// txt.setText(mDataList.get(position).getData());
	//
	// return convertView;
	// }
	//
	// class HolderView {
	// ImageView imageView_photo;
	// ImageView imageview_select;
	// ImageView imageview_maskImageView;
	// }
	//
	// public ArrayList<PhotoInfo> getPhotoURLlist() {
	// return photoURLlist;
	// }
	//
	// public void setPhotoURLlist(ArrayList<PhotoInfo> photoURLlist) {
	// this.photoURLlist = photoURLlist;
	// }
	//
	// @Override
	// public int getCount() {
	// return photoURLlist.size();
	// }
	//
	// @Override
	// public Object getItem(int position) {
	// return photoURLlist.get(position);
	// }
	//
	// @Override
	// public long getItemId(int position) {
	// return position;
	// }
	//
	// }

	private LayoutInflater mInflater;
	private ArrayList<PhotoInfo> arrayList;
	private static int requestwidth;
	private Context c;
	private final static String TAG = "ViewPhotoGridViewAdapter";

	// 构造函数
	/**
	 *
	 * @param context
	 * @param arraylist
	 *            相册路径数组
	 */
	public SelectPhotoGoToVideoAdapter(Context context,
			ArrayList<PhotoInfo> arraylist) {
		mInflater = LayoutInflater.from(context);
		c = context;
		arrayList = arraylist;
		for (int i = 0; i < arrayList.size(); i++) {
			System.out.println("size = " + arrayList.size() + "_"
					+ arrayList.get(i));
		}
	}

	public void setArrayList(ArrayList<PhotoInfo> arrayList) {
		this.arrayList = arrayList;
	}

	public int getCount() {
		return arrayList.size();
	}

	public Object getItem(int position) {
		return arrayList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	/**
	 * 开始选择照片
	 *
	 * false：取消选择状态，true：显示选择状态
	 */
	public void startSelectPhoto(int isChecked, int isSelected) {
		PhotoInfo selectPhotoItemInfo;
		for (int i = 1; i < arrayList.size(); i++) {
			selectPhotoItemInfo = arrayList.get(i);
			selectPhotoItemInfo.isChecked = isChecked;
			selectPhotoItemInfo.isSelected = isSelected;
		}
		notifyDataSetChanged();
	}

	/**
	 * 刷新view
	 * 
	 * @param index
	 * @param view
	 * @param mode
	 *            0:选择，1：预览
	 */
	public void refreshView(int index, View view, int mode) {
		HolderView holder = (HolderView) view.getTag();
		if (mode == 0) {
			if (arrayList.get(index).isChecked == 1) {
				holder.imageview_select.setImageResource(R.drawable.sel2);
				holder.imageview_select.setVisibility(View.VISIBLE);
				holder.imageview_select.setBackgroundColor(Color.TRANSPARENT);
				holder.imageview_maskImageView.setVisibility(View.VISIBLE);
			} else {
				holder.imageview_select.setVisibility(View.INVISIBLE);
				holder.imageview_maskImageView.setVisibility(View.INVISIBLE);
			}
		} else if (mode == 1) {
			if (arrayList.get(index).isChecked == 1) {
				if (arrayList.get(index).isSelected == 1) {// 选中
					holder.imageview_select.setImageResource(R.drawable.sel2);
				} else {// 未选中
					holder.imageview_select.setImageResource(R.drawable.sel3);

				}
			}
		}
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// Log.d(TAG, "getView----------->" +
		requestwidth = (ScreenUtil.getScreenWidth(c) - 4 * 5) / 3;// （屏宽-4*3）/3,3为间隔间距

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				requestwidth, requestwidth);
		HolderView holderView;

		if (convertView != null) {
			holderView = (HolderView) convertView.getTag();
		} else {
			holderView = new HolderView();
			convertView = mInflater.inflate(R.layout.grid_row, null);
			holderView.imageView_photo = (ImageView) convertView
					.findViewById(R.id.imageItem);
			holderView.imageview_select = (ImageView) convertView
					.findViewById(R.id.imageView_Select);
			holderView.imageview_maskImageView = (ImageView) convertView
					.findViewById(R.id.imageView_mask);
			convertView.setTag(holderView);
		}
		PhotoInfo selectPhotoItemInfo = arrayList.get(position);

		holderView.imageView_photo.setLayoutParams(params);// 设置图片显示的大小，使得每张图片显示为正方形
		holderView.imageview_maskImageView.setLayoutParams(params);// 设置蒙版的大小
		holderView.imageView_photo.setScaleType(ScaleType.CENTER_CROP);
		holderView.imageView_photo.setVisibility(View.VISIBLE);
		if (selectPhotoItemInfo.isChecked == 1) {// 如果已经有点过了
			holderView.imageview_maskImageView.setVisibility(View.VISIBLE);
			holderView.imageview_select.setVisibility(View.VISIBLE);
			System.out.println(selectPhotoItemInfo.isChecked + "_______");
			if (selectPhotoItemInfo.isSelected == 1) {
				holderView.imageview_select.setImageResource(R.drawable.sel2);
				System.out.println("isSelected------>"
						+ selectPhotoItemInfo.isSelected);
			} else {
				System.out.println("no select--》"
						+ selectPhotoItemInfo.isSelected);
				holderView.imageview_select.setImageResource(R.drawable.sel3);
			}
			holderView.imageview_select.setBackgroundColor(Color.TRANSPARENT);
		} else {
			holderView.imageview_maskImageView.setVisibility(View.INVISIBLE);
			holderView.imageview_select.setVisibility(View.INVISIBLE);
		}
		if (selectPhotoItemInfo.onLine == 0) {
			System.out.println("开始加载图片");
			System.out.println("加载原图---------->"
					+ selectPhotoItemInfo.photoPathOrURL);
			UniversalImageLoadTool.loadImage("file://"
					+ selectPhotoItemInfo.photoPathOrURL,
					holderView.imageView_photo);
			System.out.println("-------->原图加载完毕");
		} else if (selectPhotoItemInfo.onLine == 1) {
			if (selectPhotoItemInfo.isPayed == 1) {// 如果已经购买，显示512的缩略图
				System.out.println("开始加载512图片"
						+ selectPhotoItemInfo.photoThumbnail_512);
				UniversalImageLoadTool.loadImage(Common.PHOTO_URL
								+ selectPhotoItemInfo.photoThumbnail_512,
						holderView.imageView_photo);
			} else {// 反之显示128的缩略图
				System.out.println("开始加载128图片"
						+ selectPhotoItemInfo.photoThumbnail);
				UniversalImageLoadTool.loadImage(
						selectPhotoItemInfo.photoThumbnail,
						holderView.imageView_photo);
			}
		}
		return convertView;
	}

	class HolderView {
		ImageView imageView_photo;
		ImageView imageview_select;
		ImageView imageview_maskImageView;
	}

}
