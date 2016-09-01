package com.pictureair.photopass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.GlideUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.stickygridheaders.StickyGridHeadersSimpleAdapter;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * @author bauer_bao
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

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
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
		PictureAirLog.out("ppcode---->" + photoList.get(position).photoPassCode);
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			// 图片的 布局
			convertView = layoutInflater.inflate(R.layout.sticky_grid_view, parent, false);

			// 布局的 图片
			viewHolder.imageView = (ImageView) convertView.findViewById(R.id.sticky_imageView);
			viewHolder.videoImageView = (ImageView) convertView.findViewById(R.id.play_video_iv);
			viewHolder.maskImageView = (ImageView) convertView.findViewById(R.id.imageView_mask);
			viewHolder.selectImageView = (ImageView) convertView.findViewById(R.id.imageView_Select);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		ViewGroup.LayoutParams params = viewHolder.imageView.getLayoutParams();
		params.width = (ScreenUtil.getScreenWidth(context) - ScreenUtil.dip2px(context, 5 * (2))) / COLUMN_COUNT;
		params.height = params.width;
		viewHolder.imageView.setLayoutParams(params);
		viewHolder.maskImageView.setLayoutParams(params);
		String photoUrl;
		if (photoList.get(position).onLine == 1) {
			if (photoList.get(position).isVideo == 1) {
				PictureAirLog.out("load video--->" + photoList.get(position).photoThumbnail_512);
				photoUrl = Common.PHOTO_URL + photoList.get(position).photoThumbnail_512;
				viewHolder.videoImageView.setVisibility(View.VISIBLE);
				ViewGroup.LayoutParams params2 = viewHolder.videoImageView.getLayoutParams();
				params2.width = (ScreenUtil.getScreenWidth(context) - ScreenUtil.dip2px(context, 5 * (2))) / (2 * COLUMN_COUNT);
				params2.height = params2.width;
				viewHolder.videoImageView.setLayoutParams(params2);
			} else {
				PictureAirLog.out("load online photo--->" + photoList.get(position).photoPathOrURL);
				if (photoList.get(position).isPayed == 1) {
					photoUrl = Common.PHOTO_URL + photoList.get(position).photoThumbnail_512;
				} else {
					photoUrl = photoList.get(position).photoThumbnail;
				}
				viewHolder.videoImageView.setVisibility(View.GONE);
			}
		} else {
			photoUrl = "file://" + photoList.get(position).photoPathOrURL;
			viewHolder.videoImageView.setVisibility(View.GONE);
		}
		if (viewHolder.imageView.getTag(R.id.glide_image_tag) == null || !viewHolder.imageView.getTag(R.id.glide_image_tag).equals(photoUrl)) {//加载图片
			GlideUtil.load(context, photoUrl, AppUtil.isEncrypted(photoList.get(position).isEncrypted), viewHolder.imageView);
			viewHolder.imageView.setTag(R.id.glide_image_tag, photoUrl);
		}
		if (editMode) {
			viewHolder.selectImageView.setVisibility(View.VISIBLE);
			if (photoList.get(position).isSelected == 1) {
				viewHolder.selectImageView.setImageResource(R.drawable.sel2);
			} else {
				viewHolder.selectImageView.setImageResource(R.drawable.sel3);
			}

			viewHolder.maskImageView.setVisibility(photoList.get(position).showMask == 1 ? View.VISIBLE : View.GONE);
		} else {
			viewHolder.selectImageView.setVisibility(View.GONE);
			viewHolder.maskImageView.setVisibility(View.GONE);
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
			viewHolder.storyCountryTextView.setTypeface(MyApplication.getInstance().getFontBold());
			viewHolder.storyAddressNameTextView.setTypeface(MyApplication.getInstance().getFontBold());
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
		return photoList.get(position).sectionId;
	}


	public class ViewHolder{
		private ImageView imageView;//图片布局
		private ImageView videoImageView;
		public ImageView maskImageView;
		public ImageView selectImageView;
	}

	private class HeaderViewHolder {
		private TextView storyTimeTextView;
		private TextView storyAddressNameTextView;
		private TextView storyCountryTextView;
	}

}
