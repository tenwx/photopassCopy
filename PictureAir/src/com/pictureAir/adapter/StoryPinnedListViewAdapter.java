package com.pictureAir.adapter;

import java.text.ParseException;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.hb.views.PinnedSectionListView.PinnedSectionListAdapter;
import com.pictureAir.PreviewPhotoActivity;
import com.pictureAir.R;
import com.pictureAir.entity.PhotoInfo;
import com.pictureAir.entity.PhotoItemInfo;
import com.pictureAir.util.AppUtil;
import com.pictureAir.util.Common;
import com.pictureAir.util.ScreenUtil;
import com.pictureAir.util.UniversalImageLoadTool;

/**
 * listView的适配器,一个是悬浮的列表，一个是正常的列表，但是数据都是一样的
 * 暂时是使用listView来做，以后应该使用expandableListView处理，但是那样的话，数据处理那块需要修改
 * @author bauer_bao
 *
 */
public class StoryPinnedListViewAdapter extends BaseAdapter implements PinnedSectionListAdapter, SectionIndexer{
	private ArrayList<PhotoItemInfo> picList;//照片列表
	private ArrayList<PhotoInfo> magicPhotoInfoList;
	private PhotoItemInfo [] sectionList;//section悬浮列表
	private LayoutInflater layoutInflater;
	private Context context;
	private static final int SECTION = 0;
	private static final int ITEM = 1;
	private static final String TAG = "StoryPinnedListView";
	
	public StoryPinnedListViewAdapter(Context context, ArrayList<PhotoItemInfo> list, ArrayList<PhotoInfo> magicList) {
		picList = list;
		magicPhotoInfoList = magicList;
		this.context = context;
		sectionList = (PhotoItemInfo[]) list.toArray(new PhotoItemInfo[list.size()]);
		layoutInflater = LayoutInflater.from(context);
		
	}
	
	@Override
	public int getViewTypeCount() {
		return 2;
	}
	
	@Override
	public int getItemViewType(int position) {
		int type = SECTION;
		if (position % 2 == 0) {//0代表是悬浮的title部分
			type = SECTION;
		}else {//1代表是图片部分
			type = ITEM;
		}
		return type;
	}
	
	@Override
	public int getCount() {
		return picList.size() * 2;
	}

	@Override
	public Object getItem(int position) {
		//如果0，返回section，如果1，返回pic
		return (position % 2 == 0) ? sectionList[position] : picList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder viewHolder = null;
		if (getItemViewType(position) == SECTION) {//使用第一个布局（section部分）
			Log.d(TAG, "section--------->" + position);
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = layoutInflater.inflate(R.layout.story_pinned_listview_section, null);
				viewHolder.storyTimeTextView = (TextView) convertView.findViewById(R.id.section_time);
				viewHolder.storyAddressNameTextView = (TextView) convertView.findViewById(R.id.section_location_place);
				convertView.setTag(viewHolder);
			}else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			try {
				viewHolder.storyTimeTextView.setText(AppUtil.dateToSmartDate(sectionList[(position + 1) / 2].shootTime, context));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			viewHolder.storyAddressNameTextView.setText(sectionList[(position + 1) / 2].place);
		}else {//使用第二个布局（item部分）
			Log.d(TAG, "item------->" + position);
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = layoutInflater.inflate(R.layout.story_pinned_listview_item, null);
				viewHolder.picGridLayout = (GridLayout) convertView.findViewById(R.id.item_gridLayout);
				convertView.setTag(viewHolder);
			}else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			//初始化图片列表
			viewHolder.picGridLayout.removeAllViews();
			for (int j = 0; j < picList.get(position / 2).list.size(); j++) {
				ImageView imageView = new ImageView(context);
				GridLayout.LayoutParams params = new GridLayout.LayoutParams();
				params.width = (ScreenUtil.getScreenWidth(context) - ScreenUtil.dip2px(context, 20)) / 3;
				params.height = params.width;
				if (j % 3 != 0) {
					params.leftMargin = ScreenUtil.dip2px(context, 5);
				}
				if (j / 3 > 0) {//除了第一行，都要添加上边距
					params.topMargin = ScreenUtil.dip2px(context, 5);
				}
				imageView.setLayoutParams(params);
				if (picList.get(position / 2).list.get(j).onLine == 1) {//网络图片
					if (picList.get(position / 2).list.get(j).isPayed == 1) {//如果已经购买，显示512的缩略图
						System.out.println("开始加载512图片" + picList.get(position / 2).list.get(j).photoThumbnail_512);
						UniversalImageLoadTool.loadImage(Common.PHOTO_URL + picList.get(position / 2).list.get(j).photoThumbnail_512, imageView);
					}else {//反之显示128的缩略图
						Log.d("cartactivity load network photo", picList.get(position / 2).list.get(j).photoThumbnail);
						UniversalImageLoadTool.loadImage(picList.get(position / 2).list.get(j).photoThumbnail, imageView);
					}
					
					
				}else {
					Log.d("cartactivity load local photo", picList.get(position / 2).list.get(j).photoPathOrURL);
					
					UniversalImageLoadTool.loadImage("file://" + picList.get(position / 2).list.get(j).photoPathOrURL, imageView);
					
				}
				imageView.setScaleType(ScaleType.CENTER_CROP);
				imageView.setId(position*10+j);//给添加的imageview添加id
				//imageview设置监听
				imageView.setOnClickListener(new PhotoOnClickListener(picList.get(position / 2).list.get(j)));
				viewHolder.picGridLayout.addView(imageView,params);
				
			}
		}
		return convertView;
	}

	@Override
	public boolean isItemViewTypePinned(int viewType) {
		return viewType == SECTION;
	}
	
	

	@Override
	public Object[] getSections() {
		return sectionList;
	}

	//得到当前section的position
	@Override
	public int getPositionForSection(int sectionIndex) {
		return sectionIndex;
	}

	//得到当前position的section
	@Override
	public int getSectionForPosition(int position) {
		return position;
	}
	
	private class ViewHolder{
		TextView storyTimeTextView;//时间
		TextView storyAddressNameTextView;//地点
		GridLayout picGridLayout;//图片布局
	}
	
	//照片点击的监听类
	private class PhotoOnClickListener implements OnClickListener{
		private PhotoInfo info;
		public PhotoOnClickListener(PhotoInfo info) {
			this.info = info;
		}
		@Override
		public void onClick(View v) {
			Log.d(TAG, "photo on " + v.getId() / 10 +" row "+ v.getId() % 10 +" column");
			Intent i = new Intent();
			ArrayList<PhotoInfo> photopassArrayList = new ArrayList<PhotoInfo>();
			//需要将picList中的图片数据全部转到成photopassArrayList
			for (int j = 0; j < picList.size(); j++) {
				photopassArrayList.addAll(picList.get(j).list);
			}
			System.out.println("photopass list size is ----->"+ photopassArrayList.size());
			System.out.println("photopass list index is ----->"+ photopassArrayList.indexOf(info));
			i.setClass(context, PreviewPhotoActivity.class);
			i.putExtra("activity", "storyPinnedListViewAdapter");
			i.putExtra("position", photopassArrayList.indexOf(info)+"");//在那个相册中的位置
			i.putExtra("photoId", info.photoId);
			i.putExtra("photos", photopassArrayList);//那个相册的全部图片路径
			i.putExtra("targetphotos", magicPhotoInfoList);

			context.startActivity(i);
		}
		
	}

}
