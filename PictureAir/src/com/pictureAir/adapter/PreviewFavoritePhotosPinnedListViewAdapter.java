package com.pictureAir.adapter;

import java.text.ParseException;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.hb.views.PinnedSectionListView.PinnedSectionListAdapter;
import com.pictureAir.PreviewPhotoActivity;
import com.pictureAir.R;
import com.pictureAir.entity.PhotoInfo;
import com.pictureAir.entity.PhotoItemInfo;
import com.pictureAir.util.AppUtil;
import com.pictureAir.widget.NoScrollGridView;

/**
 * listView的适配器,一个是悬浮的列表，一个是正常的列表，但是数据都是一样的
 * 暂时是使用listView来做，以后应该使用expandableListView处理，但是那样的话，数据处理那块需要修改
 * @author bauer_bao
 *
 */
public class PreviewFavoritePhotosPinnedListViewAdapter extends BaseAdapter implements PinnedSectionListAdapter, SectionIndexer{
	private ArrayList<PhotoItemInfo> picList;//照片列表
	private ArrayList<PhotoInfo> magicPhotoList;
	private PhotoItemInfo [] sectionList;//section悬浮列表
	private LayoutInflater layoutInflater;
	private Context context;
	private static final int SECTION = 0;
	private static final int ITEM = 1;
	private static final String TAG = "StoryPinnedListView";
	private Handler handler;
	public static final int NEED_RESCAN = 3;
	public PreviewFavoritePhotosPinnedListViewAdapter(Context context, ArrayList<PhotoItemInfo> list, ArrayList<PhotoInfo> magicPhotoList, Handler handler) {
		picList = list;
		this.context = context;
		this.magicPhotoList = magicPhotoList;
		sectionList = (PhotoItemInfo[]) list.toArray(new PhotoItemInfo[list.size()]);
		layoutInflater = LayoutInflater.from(context);
		this.handler = handler;
	}
	
	/**
	 * 更新数据
	 * @param list
	 */
	public void updateData(ArrayList<PhotoItemInfo> list){
		picList = list;
		sectionList = (PhotoItemInfo[]) list.toArray(new PhotoItemInfo[list.size()]);
		notifyDataSetChanged();
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
				convertView = layoutInflater.inflate(R.layout.edit_album_gridview, null);
				viewHolder.storyGridView = (NoScrollGridView) convertView.findViewById(R.id.gridView_all);
				convertView.setTag(viewHolder);
			}else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			ArrayList<PhotoInfo> list = new ArrayList<PhotoInfo>();
			list.addAll(picList.get(position / 2).list);
			EditStoryPhotoGridViewAdapter editStoryPhotoGridViewAdapter = new EditStoryPhotoGridViewAdapter(context, list);
			viewHolder.storyGridView.setAdapter(editStoryPhotoGridViewAdapter);
			viewHolder.storyGridView.setOnItemClickListener(new PhotoOnClickListener(list));
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
		NoScrollGridView storyGridView;//图片布局
	}
	
	//照片点击的监听类
	private class PhotoOnClickListener implements OnItemClickListener{
		private ArrayList<PhotoInfo> photoArrayList;
		public PhotoOnClickListener(ArrayList<PhotoInfo> list) {
			photoArrayList = list;
		}
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			System.out.println("select"+position);
			//选择事件
			PhotoInfo info = photoArrayList.get(position);
			Intent i = new Intent();
			
			ArrayList<PhotoInfo> photopassArrayList = new ArrayList<PhotoInfo>();
			//需要将picList中的图片数据全部转到成photopassArrayList
			for (int j = 0; j < picList.size(); j++) {
				photopassArrayList.addAll(picList.get(j).list);
			}
			System.out.println("photopass list size is ----->"+ photopassArrayList.size());
			System.out.println("photopass list index is ----->"+ photopassArrayList.indexOf(info));
			i.setClass(context, PreviewPhotoActivity.class);
			i.putExtra("activity", "previewFavoritePhotosPinnedListViewAdapter");
			i.putExtra("position", photopassArrayList.indexOf(info)+"");//在那个相册中的位置
			i.putExtra("photoId", info.photoId);
			i.putExtra("photos", photopassArrayList);//那个相册的全部图片路径
			i.putExtra("targetphotos", magicPhotoList);
			
			handler.sendEmptyMessage(NEED_RESCAN);
			context.startActivity(i);
		}
		
	}

}
