package com.pictureair.photopass.adapter;

import android.content.Context;
import android.media.Image;
import android.support.v4.app.NavUtils;
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
import com.pictureair.photopass.entity.PhotoDownLoadInfo;

import java.util.List;

/**
 * Created by pengwu on 16/7/8.
 */
public class PhotoLoadSuccessAdapter extends BaseAdapter {

    private List<PhotoDownLoadInfo> photos;
    private Context mContext;
    private ImageLoader imageLoader;
    public PhotoLoadSuccessAdapter(Context context,List<PhotoDownLoadInfo> photos){
        this.mContext = context;
        this.photos = photos;
        imageLoader.getInstance();
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
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.photo_load_success_item,null);
            holder = new Holder();
            holder.img = (ImageView)convertView.findViewById(R.id.load_success_img);
            holder.tv_name = (TextView) convertView.findViewById(R.id.load_success_name);
            holder.tv_size = (TextView) convertView.findViewById(R.id.load_success_size);
            holder.tv_date = (TextView) convertView.findViewById(R.id.load_success_date);
            holder.tv_time = (TextView) convertView.findViewById(R.id.load_success_time);
            convertView.setTag(holder);
        }else{
            holder = (Holder) convertView.getTag();
        }
        PhotoDownLoadInfo info = photos.get(position);
        if (info != null) {
            ImageAware imageAware = new ImageViewAware(holder.img);
            String url = info.getUrl();
            if (holder.img.getTag() == null || !holder.img.getTag().equals(url)){
                imageLoader.displayImage(url,imageAware);
                holder.img.setTag(url);
            }
            holder.tv_name.setText(info.getName());
            holder.tv_size.setText(info.getSize());
            holder.tv_date.setText(info.getDate());
            holder.tv_time.setText(info.getTime());
        }
        return convertView;
    }

    class Holder{
        ImageView img;
        TextView tv_name;
        TextView tv_size;
        TextView tv_date;
        TextView tv_time;
    }
}
