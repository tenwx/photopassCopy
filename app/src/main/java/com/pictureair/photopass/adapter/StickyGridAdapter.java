package com.pictureair.photopass.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.customDialog.CustomDialog;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.service.DownloadService;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.util.UmengUtil;
import com.pictureair.photopass.util.UniversalImageLoadTool;
import com.pictureair.photopass.widget.stickygridheaders.StickyGridHeadersSimpleAdapter;

import java.text.ParseException;
import java.util.ArrayList;

import cn.smssdk.gui.MyToast;


public class StickyGridAdapter extends BaseAdapter implements StickyGridHeadersSimpleAdapter {
    private ArrayList<PhotoInfo> list;
    private Context context;
    private LayoutInflater layoutInflater;
    private MyToast myToast;
    private CustomDialog customDialog;
    private static final int COLUMN_COUNT = 3;

    public StickyGridAdapter(Context context, ArrayList<PhotoInfo> list) {
        this.context = context;
        this.list = list;
        layoutInflater = LayoutInflater.from(context);
        myToast = new MyToast(context);
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
        System.out.println("get View------->" + position + "___" + list.size());
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

        if (list.get(position).onLine == 1) {
            if (list.get(position).isVideo == 1) {
                PictureAirLog.out("load video--->" + list.get(position).photoPathOrURL);
                UniversalImageLoadTool.loadImage(Common.PHOTO_URL + list.get(position).photoPathOrURL, mViewHolder.mImageView);
                mViewHolder.videoImageView.setVisibility(View.VISIBLE);
                LayoutParams params2 = mViewHolder.videoImageView.getLayoutParams();
                params2.width = (ScreenUtil.getScreenWidth(context) - ScreenUtil.dip2px(context, 5 * (2))) / (2 * COLUMN_COUNT);
                params2.height = params2.width;
                mViewHolder.videoImageView.setLayoutParams(params2);
            } else {
                PictureAirLog.out("load online photo--->" + list.get(position).photoPathOrURL);
                UniversalImageLoadTool.loadImage(list.get(position).photoThumbnail, mViewHolder.mImageView);
                mViewHolder.videoImageView.setVisibility(View.GONE);
            }
        } else {
            UniversalImageLoadTool.loadImage("file://" + list.get(position).photoPathOrURL, mViewHolder.mImageView);
            mViewHolder.videoImageView.setVisibility(View.GONE);
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
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (HeaderViewHolder) convertView.getTag();
        }

        try {
            viewHolder.storyTimeTextView.setText(AppUtil.dateToSmartDate(list.get(position).shootTime, context));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (list.get(position).onLine == 0) {
            PictureAirLog.out("getview---->local photo");
            viewHolder.storyAddressNameTextView.setText(R.string.magic_location);
        } else {
            if (list.get(position).isVideo == 1) {
                PictureAirLog.out("getview---->video");

                viewHolder.storyAddressNameTextView.setText(R.string.video_location);
            } else {

                PictureAirLog.out("getview---->photo online");
                String place = list.get(position).locationName;
                if (place == null || place.equals("null")) {
                    place = "";
                }
                viewHolder.storyAddressNameTextView.setText(place);
            }
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

    public class ViewHolder {
        public ImageView mImageView;
        public ImageView videoImageView;
    }

    public class HeaderViewHolder {
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

    /**
     * 下载监听
     *
     * @author bauer_bao
     */
    private class DownloadAllListener implements OnClickListener {

        private long headerId;

        public DownloadAllListener(long headerId) {
            this.headerId = headerId;
        }

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch (AppUtil.getNetWorkType(context)) {
                case AppUtil.NETWORKTYPE_INVALID://无网络
                    myToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
                    break;

                case AppUtil.NETWORKTYPE_MOBILE://流量
                    customDialog = new CustomDialog.Builder(context)
                            .setMessage(context.getString(R.string.dialog_download_message))
                            .setNegativeButton(context.getString(R.string.button_cancel), new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    // TODO Auto-generated method stub
                                    customDialog.dismiss();
                                }
                            })
                            .setPositiveButton(context.getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    // TODO Auto-generated method stub
                                    startDownloadPhotos(headerId);
                                    customDialog.dismiss();
                                }
                            })
                            .setCancelable(false)
                            .create();
                    customDialog.show();
                    break;

                case AppUtil.NETWORKTYPE_WIFI://wifi
                    startDownloadPhotos(headerId);
                    break;

                default:
                    break;
            }

        }
    }

    /**
     * 开始下载图片
     *
     * @param headerId
     */
    private void startDownloadPhotos(long headerId) {
        Common.DOWNLOAD_TYPE = Common.ALL_PHOTOS_DOWNLOAD;
        UmengUtil.onEvent(context, Common.ALL_PHOTOS_DOWNLOAD);
        ArrayList<PhotoInfo> downloadList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (headerId == list.get(i).sectionId) {

                downloadList.add(list.get(i));
            }
        }
        //直接下载
        Intent intent = new Intent(context, DownloadService.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("photos", downloadList);
        intent.putExtras(bundle);
        context.startService(intent);
    }

}
