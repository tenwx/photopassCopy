package com.pictureair.photopass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
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

public class StickyGridAdapter extends BaseAdapter implements StickyGridHeadersSimpleAdapter {
    private ArrayList<PhotoInfo> list;
    private Context context;
    private LayoutInflater layoutInflater;
    private static final int COLUMN_COUNT = 3;

    public StickyGridAdapter(Context context, ArrayList<PhotoInfo> list) {
        this.context = context;
        this.list = list;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PictureAirLog.out("get View------->" + position + "___" + list.size());
        ViewHolder mViewHolder;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            // 图片的 布局
            convertView = layoutInflater.inflate(R.layout.sticky_grid_view, parent, false);

            // 布局的 图片
            mViewHolder.mImageView = (ImageView) convertView.findViewById(R.id.sticky_imageView);
            mViewHolder.videoImageView = (ImageView) convertView.findViewById(R.id.play_video_iv);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        LayoutParams params = mViewHolder.mImageView.getLayoutParams();
        params.width = (ScreenUtil.getScreenWidth(context) - ScreenUtil.dip2px(context, 5 * (2))) / COLUMN_COUNT;
        params.height = params.width;
        mViewHolder.mImageView.setLayoutParams(params);

        String photoUrl;
        if (list.get(position).onLine == 1) {
            if (list.get(position).isVideo == 1) {
                PictureAirLog.out("load video--->" + list.get(position).photoThumbnail_512);
                photoUrl = Common.PHOTO_URL + list.get(position).photoThumbnail_512;
                mViewHolder.videoImageView.setVisibility(View.VISIBLE);
                LayoutParams params2 = mViewHolder.videoImageView.getLayoutParams();
                params2.width = (ScreenUtil.getScreenWidth(context) - ScreenUtil.dip2px(context, 5 * (2))) / (4 * COLUMN_COUNT);
                params2.height = params2.width;
                mViewHolder.videoImageView.setLayoutParams(params2);
            } else {
                PictureAirLog.out("load online photo--->" + list.get(position).photoPathOrURL);
                if (list.get(position).isPayed == 1) {
                    photoUrl = Common.PHOTO_URL + list.get(position).photoThumbnail_512;
                } else {
                    photoUrl = list.get(position).photoThumbnail;
                }
                mViewHolder.videoImageView.setVisibility(View.GONE);
            }
        } else {
            photoUrl = "file://" + list.get(position).photoPathOrURL;
            mViewHolder.videoImageView.setVisibility(View.GONE);
        }

        if (mViewHolder.mImageView.getTag(R.id.glide_image_tag) == null || !mViewHolder.mImageView.getTag(R.id.glide_image_tag).equals(photoUrl)) {//加载图片
            GlideUtil.load(context, photoUrl, AppUtil.isEncrypted(list.get(position).isEncrypted), mViewHolder.mImageView);
            mViewHolder.mImageView.setTag(R.id.glide_image_tag, photoUrl);
        }

        return convertView;
    }

    @Override
    public View getHeaderView(int position, View convertView,
                              ViewGroup parent) {
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
        } else {
            viewHolder = (HeaderViewHolder) convertView.getTag();
        }

        String headerTime = list.get(position).shootOn;
        for (int i = position; i >= 0; i--) {
            if (list.get(position).sectionId == list.get(i).sectionId) {//当前的
                headerTime = list.get(i).shootOn;
            } else {
                break;
            }
        }

        try {
            viewHolder.storyTimeTextView.setText(AppUtil.dateToSmartDate(headerTime, context));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (list.get(position).onLine == 0) {
//            PictureAirLog.out("getview---->local photo");
            viewHolder.storyAddressNameTextView.setText(R.string.story_tab_magic);
        } else {
//                PictureAirLog.out("getview---->photo online");
            String place = list.get(position).locationName;
            if (place == null || place.equals("null")) {
                place = "";
            }
            viewHolder.storyAddressNameTextView.setText(place);
        }

        String country = list.get(position).locationCountry;

        if (country != null && !country.equals("") && !country.equals("null")) {

            viewHolder.storyCountryTextView.setText(country);
            viewHolder.storyCountryTextView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.storyCountryTextView.setVisibility(View.GONE);
        }
        return convertView;
    }

    private class ViewHolder {
        public ImageView mImageView;
        public ImageView videoImageView;
    }

    private class HeaderViewHolder {
        public TextView storyTimeTextView;
        public TextView storyAddressNameTextView;
        public TextView storyCountryTextView;

    }

    /**
     * 获取HeaderId, 只要HeaderId不相等就添加一个Header
     */

    @Override
    public long getHeaderId(int position) {
        return list.get(position).sectionId;
    }
}
