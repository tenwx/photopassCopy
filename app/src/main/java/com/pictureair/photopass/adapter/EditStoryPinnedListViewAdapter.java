package com.pictureair.photopass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.util.UniversalImageLoadTool;
import com.pictureair.photopass.widget.stickygridheaders.StickyGridHeadersSimpleAdapter;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * listView的适配器,一个是悬浮的列表，一个是正常的列表，但是数据都是一样的
 * 暂时是使用listView来做，以后应该使用expandableListView处理，但是那样的话，数据处理那块需要修改
 * @author bauer_bao
 *
 */
public class EditStoryPinnedListViewAdapter extends BaseAdapter implements StickyGridHeadersSimpleAdapter{
	private ArrayList<PhotoInfo> photoList;
	private LayoutInflater layoutInflater;
	private static final int COLUMN_COUNT = 3;
	
	private Context context;
	private static final String TAG = "StoryPinnedListView";
	private boolean editMode;
	public EditStoryPinnedListViewAdapter(Context context, boolean editMode, ArrayList<PhotoInfo> photoList) {
		this.context = context;
		this.editMode = editMode;
		this.photoList = photoList;
		layoutInflater = LayoutInflater.from(context);
	}
	
	
	
	public boolean isEditMode() {
		return editMode;
	}



	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

	public void setPhotoList(ArrayList<PhotoInfo> photoList) {
		this.photoList = photoList;
		notifyDataSetChanged();
	}


	
	/**
	 * 开始选择照片
	 * @param isChecked false：取消选择状态，true：显示选择状态
	 * @param isSelected false：默认选择的图片，true：选中的时候的图片
	 */
	public void startSelectPhoto(int isChecked, int isSelected) {
		PhotoInfo selectPhotoItemInfo;
		for (int i = 0; i < photoList.size(); i++) {
				selectPhotoItemInfo = photoList.get(i);
				selectPhotoItemInfo.isChecked = isChecked;
				selectPhotoItemInfo.isSelected = isSelected;
		}
		notifyDataSetChanged();
	}
	

	@Override
	public int getCount() {
		return photoList.size();
	}

	@Override
	public Object getItem(int position) {
		return photoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			// 图片的 布局
			convertView = layoutInflater.inflate(R.layout.sticky_grid_view, parent, false);

			// 布局的 图片
			viewHolder.imageView = (ImageView) convertView.findViewById(R.id.sticky_imageView);
			viewHolder.videoImageView = (ImageView) convertView.findViewById(R.id.play_video_iv);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		ViewGroup.LayoutParams params = viewHolder.imageView.getLayoutParams();
		params.width = (ScreenUtil.getScreenWidth(context) - ScreenUtil.dip2px(context, 5 * (2))) / COLUMN_COUNT;
		params.height = params.width;
		viewHolder.imageView.setLayoutParams(params);

		if (photoList.get(position).onLine == 1) {
			if (photoList.get(position).isVideo == 1) {
				PictureAirLog.out("load video--->" + photoList.get(position).photoPathOrURL);
				UniversalImageLoadTool.loadImage(Common.PHOTO_URL + photoList.get(position).photoPathOrURL, viewHolder.imageView);
				viewHolder.videoImageView.setVisibility(View.VISIBLE);
				ViewGroup.LayoutParams params2 = viewHolder.videoImageView.getLayoutParams();
				params2.width = (ScreenUtil.getScreenWidth(context) - ScreenUtil.dip2px(context, 5 * (2))) / (2 * COLUMN_COUNT);
				params2.height = params2.width;
				viewHolder.videoImageView.setLayoutParams(params2);
			} else {
				PictureAirLog.out("load online photo--->" + photoList.get(position).photoPathOrURL);
				if (photoList.get(position).isPayed == 1) {
					UniversalImageLoadTool.loadImage(Common.PHOTO_URL + photoList.get(position).photoThumbnail_512, viewHolder.imageView);
				} else {
					UniversalImageLoadTool.loadImage(photoList.get(position).photoThumbnail, viewHolder.imageView);
				}
				viewHolder.videoImageView.setVisibility(View.GONE);
			}
		} else {
			UniversalImageLoadTool.loadImage("file://" + photoList.get(position).photoPathOrURL, viewHolder.imageView);
			viewHolder.videoImageView.setVisibility(View.GONE);
		}
		return convertView;
	}


	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		PictureAirLog.d(TAG, "section--------->" + position);
		HeaderViewHolder viewHolder = null;

		if (convertView == null) {
			viewHolder = new HeaderViewHolder();
			convertView = layoutInflater.inflate(R.layout.story_pinned_listview_section, null);
			viewHolder.storyTimeTextView = (TextView) convertView.findViewById(R.id.section_time);
			viewHolder.storyAddressNameTextView = (TextView) convertView.findViewById(R.id.section_location_place);
			viewHolder.storyCountryTextView = (TextView) convertView.findViewById(R.id.section_location_country);
			convertView.setTag(viewHolder);
		}else {
			viewHolder = (HeaderViewHolder) convertView.getTag();
		}

		if (photoList.get(position).onLine == 0) {
			viewHolder.storyAddressNameTextView.setText(R.string.story_tab_magic);
		}else {
			String place = photoList.get(position).locationName;
			if (place == null || place.equals("null")) {
				place = "";
			}
			viewHolder.storyAddressNameTextView.setText(place);
		}

		String country = photoList.get(position).locationCountry;

		if (country != null && !country.equals("") && !country.equals("null")) {

			viewHolder.storyCountryTextView.setText(country);
			viewHolder.storyCountryTextView.setVisibility(View.VISIBLE);
		}else {
			viewHolder.storyCountryTextView.setVisibility(View.GONE);
		}

		try {
			viewHolder.storyTimeTextView.setText(AppUtil.dateToSmartDate(photoList.get(position).shootOn, context));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return convertView;
	}


	@Override
	public long getHeaderId(int position) {
		// TODO Auto-generated method stub
		return photoList.get(position).sectionId;
	}


	private class ViewHolder{
		private ImageView imageView;//图片布局
		private ImageView videoImageView;
	}

	private class HeaderViewHolder {
		private TextView storyTimeTextView;
		private TextView storyAddressNameTextView;
		private TextView storyCountryTextView;
	}

//	//照片点击的监听类
//	private class PhotoOnClickListener implements OnItemClickListener{
//		private ViewHolder viewHolder;
//		private ArrayList<PhotoInfo> photoArrayList;
//		private Handler handler;
//		private EditStoryPhotoGridViewAdapter editStoryPhotoGridViewAdapter;
//		public PhotoOnClickListener(ViewHolder viewHolder, ArrayList<PhotoInfo> list, Handler handler, EditStoryPhotoGridViewAdapter editStoryPhotoGridViewAdapter) {
//			this.viewHolder = viewHolder;
//			photoArrayList = list;
//			this.handler = handler;
//			this.editStoryPhotoGridViewAdapter = editStoryPhotoGridViewAdapter;
//		}
//
//		@Override
//		public void onItemClick(AdapterView<?> parent, View view, int position,
//				long id) {
//
//			PhotoInfo info = photoArrayList.get(position);
//			if (editMode) {
//				System.out.println("select"+position);
//				Message msg = handler.obtainMessage();
//				int visiblePos = viewHolder.storyGridView.getFirstVisiblePosition();
//				//选择事件
//				Bundle bundle = new Bundle();
//				if (info.isSelected == 1) {//取消选择
//					info.isSelected = 0;
//					bundle.putInt("flag", 10);
//				}else {//选择
//					info.isSelected = 1;
//					bundle.putInt("flag", 11);
//				}
//				editStoryPhotoGridViewAdapter.refreshView(position, viewHolder.storyGridView.getChildAt(position-visiblePos), 1);
////				bundle.putString("pathOrUrl", info.photoPathOrURL);
//				bundle.putParcelable("photo", info);
////				bundle.putInt("position", position);
//				msg.setData(bundle);
//				handler.sendMessage(msg);
////				photoArrayList.set(position, info);
//
//			}else {//非编辑模式，需要预览大图
//				Intent i = new Intent();
//				ArrayList<PhotoInfo> photopassArrayList = new ArrayList<PhotoInfo>();
//				//需要将picList中的图片数据全部转到成photopassArrayList
//				for (int j = 0; j < picList.size(); j++) {
//					photopassArrayList.addAll(picList.get(j).list);
//				}
//				System.out.println("photopass list size is ----->"+ photopassArrayList.size());
//				System.out.println("photopass list index is ----->"+ photopassArrayList.indexOf(info));
//				i.setClass(context, PreviewPhotoActivity.class);
//				i.putExtra("activity", "storyPinnedListViewAdapter");
//				i.putExtra("position", photopassArrayList.indexOf(info)+"");//在那个相册中的位置
//				i.putExtra("photoId", info.photoId);
//				i.putExtra("photos", photopassArrayList);//那个相册的全部图片路径
//				i.putExtra("targetphotos", magicPhotoList);
//
//				context.startActivity(i);
//
//			}
//
//		}
//
//	}

}
