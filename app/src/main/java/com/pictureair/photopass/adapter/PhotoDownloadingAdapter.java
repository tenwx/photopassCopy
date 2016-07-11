package com.pictureair.photopass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.PhotoInfo;

import java.util.List;

/**
 * Created by pengwu on 16/7/11.
 */
public class PhotoDownloadingAdapter extends BaseAdapter {

    private Context mContext;
    private List<PhotoInfo> photos;
    private ImageLoader imageLoader;
    public PhotoDownloadingAdapter(Context context,List<PhotoInfo> photos){
        this.mContext = context;
        this.photos = photos;
        imageLoader = ImageLoader.getInstance();
    }

    @Override
    public int getCount() {
        return photos.size();
    }

    @Override
    public Object getItem(int position) {
        return photos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder = null;
        if (convertView == null) {
            holder = new Holder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.photo_loading_item,null);
            holder.img = (ImageView) convertView.findViewById(R.id.loading_img);
            holder.tv_name = (TextView) convertView.findViewById(R.id.photo_loading_tv_name);
            holder.tv_size = (TextView) convertView.findViewById(R.id.photo_loading_tv_size);
            holder.tv_speed = (TextView) convertView.findViewById(R.id.photo_loading_tv_speed);
            holder.tv_status = (TextView) convertView.findViewById(R.id.photo_loading_tv_status);
            convertView.setTag(holder);
        }else{
            convertView.getTag();
        }

        PhotoInfo photoInfo = photos.get(position);
        if (photoInfo != null) {
            ImageAware imageAware = new ImageViewAware(holder.img);
            if (holder.img.getTag() == null || !holder.img.getTag().equals(photoInfo.photoPathOrURL)) {
                imageLoader.displayImage(photoInfo.photoPathOrURL,imageAware);
                holder.img.setTag(photoInfo.photoPathOrURL);
            }

            holder.tv_name.setText("xxxxxxxxxxx.png");
            holder.tv_size.setText("");
            holder.tv_speed.setText("");
            holder.tv_status.setText(mContext.getResources().getString(R.string.photo_download_waiting));
        }

        return convertView;
    }

    class Holder{
        ImageView img;
        TextView tv_name;
        TextView tv_size;
        TextView tv_speed;
        TextView tv_status;
    }
}
