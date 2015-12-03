package com.pictureair.photopass.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.widget.MyToast;

import java.util.ArrayList;
import java.util.List;
/**
 * 故事页面的ViewPager适配器
 * @author bauer_bao
 *
 */
public class SelectPhotoViewPagerAdapter extends PagerAdapter{
	private List<View> listViews;
	private ArrayList<PhotoInfo> allArrayList;
	private ArrayList<PhotoInfo> photoPassArrayList;
	private ArrayList<PhotoInfo> magicArrayList;
	private ArrayList<PhotoInfo> boughtArrayList;

	private Handler handler;
	private Context context;

	private GridView allGridView, photoPassGridView, magicGridView, boughtGridView;
	private ViewPhotoGridViewAdapter allAdapter, photoPassAdapter, magicAdapter, boughtAdapter;
	private MyToast myToast;


	private int photocount = 1;//每个商品可以添加的图片数量
	private int selectedCount = 0;//已经选择了的图片数量
	private ArrayList<PhotoInfo> photoURLlist;//已经选择了的图片列表
	private static final String TAG ="SelectPhotoViewPagerAdapter";

	public SelectPhotoViewPagerAdapter(Context context, List<View> list, ArrayList<PhotoInfo> list1, ArrayList<PhotoInfo> list2, 
			ArrayList<PhotoInfo> list3, ArrayList<PhotoInfo> list4, Handler handler, int photoCount) {
		listViews = list;
		this.context = context;
		allArrayList = list1;
		photoPassArrayList = list2;
		magicArrayList = list3;
		boughtArrayList = list4;
		photoURLlist = new ArrayList<PhotoInfo>();
		myToast = new MyToast(context);
		this.handler = handler;
		this.photocount = photoCount;
	}

	public ArrayList<PhotoInfo> getPhotoURLlist() {
		return photoURLlist;
	}
	
	public void setPhotoURLlist(ArrayList<PhotoInfo> photoURLlist) {
		this.photoURLlist = photoURLlist;
	}
	
	@Override
	public void destroyItem(View container, int position, Object object) {
		((ViewPager) container).removeView(listViews.get(position));
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listViews.size();
	}

	//初始化页面
	@Override
	public Object instantiateItem(View container, int position) {
		switch (position) {
		case 0:
			allGridView = (GridView) listViews.get(position).findViewById(R.id.gridView_all);
			allAdapter = new ViewPhotoGridViewAdapter(context, allArrayList);
			allGridView.setAdapter(allAdapter);
			allGridView.setOnItemClickListener(new PhotoSelectedListener(0));
			break;

		case 1:
			photoPassGridView = (GridView) listViews.get(position).findViewById(R.id.gridView_all);
			photoPassAdapter = new ViewPhotoGridViewAdapter(context, photoPassArrayList);
			photoPassGridView.setAdapter(photoPassAdapter);
			photoPassGridView.setOnItemClickListener(new PhotoSelectedListener(1));
			break;

		case 2:
			magicGridView = (GridView) listViews.get(position).findViewById(R.id.gridView_all);
			magicAdapter = new ViewPhotoGridViewAdapter(context, magicArrayList);
			magicGridView.setAdapter(magicAdapter);
			magicGridView.setOnItemClickListener(new PhotoSelectedListener(2));
			break;

		case 3:
			boughtGridView = (GridView) listViews.get(position).findViewById(R.id.gridView_all);
			boughtAdapter = new ViewPhotoGridViewAdapter(context, boughtArrayList);
			boughtGridView.setAdapter(boughtAdapter);
			boughtGridView.setOnItemClickListener(new PhotoSelectedListener(3));
			break;

		default:
			break;
		}
		((ViewPager) container).addView(listViews.get(position));
		return listViews.get(position);
	}
	
	/**
	 * 刷新指定页面
	 * @param currentTab
	 */
	public void notifyDataSetChangedCurrentTab(int currentTab){
		switch (currentTab) {
		case 0:
			allAdapter.notifyDataSetChanged();
			break;
			
		case 1:
			photoPassAdapter.notifyDataSetChanged();
			break;
			
		case 2:
			magicAdapter.notifyDataSetChanged();
			break;
			
		case 3:
			boughtAdapter.notifyDataSetChanged();
			break;

		default:
			break;
		}
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		// TODO Auto-generated method stub
		return arg0 == arg1;
	}

	//gridview点击监听
	private class PhotoSelectedListener implements OnItemClickListener{
		private PhotoInfo info;
		private int current;
		private GridView myGridView;
		private ViewPhotoGridViewAdapter viewPhotoGridViewAdapter;
		public PhotoSelectedListener(int current) {
			this.current = current;
		}
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			//获取对应相册中的SelectPhotoItemInfo对象
			switch (current) {
			case 0:
				info = allArrayList.get(position);
				myGridView = allGridView;
				viewPhotoGridViewAdapter = allAdapter;
				break;

			case 1:
				info = photoPassArrayList.get(position);
				myGridView = photoPassGridView;
				viewPhotoGridViewAdapter = photoPassAdapter;
				break;

			case 2:
				info = magicArrayList.get(position);
				myGridView = magicGridView;
				viewPhotoGridViewAdapter = magicAdapter;
				break;

			case 3:
				info = boughtArrayList.get(position);
				myGridView = boughtGridView;
				viewPhotoGridViewAdapter = boughtAdapter;
				break;

			default:
				break;
			}

			if (position==0) {
//				Intent intent = new Intent(context,CameraActivity.class);
//				context.startActivity(intent);
			}else {

				Message msg = handler.obtainMessage();
				//获取已经选择的照片数量
				selectedCount = photoURLlist.size();
				//判断数量
				if (selectedCount >= 0) {
					System.out.println(selectedCount+"current:"+position);
					if (info.isSelected == 1){//取消选中
						if (photoURLlist.contains(info)) {//存在
							photoURLlist.remove(info);
						}
//						info.isChecked = 1;
						info.isSelected = 0;
						selectedCount--;
						System.out.println("点过了，取消选中");
						int visiblePos = myGridView.getFirstVisiblePosition();
						viewPhotoGridViewAdapter.refreshView(position, myGridView.getChildAt(position-visiblePos), 1);
					}else{
						if (selectedCount < photocount) {
//							info.isChecked = 1;
							info.isSelected = 1;
							System.out.println("没点过，选中");
							selectedCount++;
							int visiblePos = myGridView.getFirstVisiblePosition();
							viewPhotoGridViewAdapter.refreshView(position, myGridView.getChildAt(position-visiblePos), 1);
							if (photoURLlist.contains(info)) {//说明已经存在
								Log.d(TAG, "之前点过了");
								myToast.setTextAndShow(R.string.photo_selected, Common.TOAST_SHORT_TIME);
							}else {
								Log.d(TAG, "没有点过，点中");
								photoURLlist.add(info);
							}
//							notifyDataSetChanged();
						}else {
							myToast.setTextAndShow(String.format(context.getString(R.string.limit_photos), photocount), Common.TOAST_SHORT_TIME);
						}
					}
					msg.what = 111;
					msg.arg1 = selectedCount;
					handler.sendMessage(msg);//通知主UI更改信息
				}else {
					myToast.setTextAndShow(String.format(context.getString(R.string.limit_photos), photocount), Common.TOAST_SHORT_TIME);
				}
			}
		}

	}
	
//	@Override
//	public int getItemPosition(Object object) {
//		// TODO Auto-generated method stub
//		return POSITION_NONE;
//	}

}
