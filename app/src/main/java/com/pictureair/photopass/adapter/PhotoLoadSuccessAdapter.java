package com.pictureair.photopass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.PhotoDownLoadInfo;
import com.pictureair.photopass.util.GlideUtil;

import java.util.List;

/**
 * Created by pengwu on 16/7/8.
 */
public class PhotoLoadSuccessAdapter extends BaseAdapter {

    private List<PhotoDownLoadInfo> photos;
    private Context mContext;
    private boolean select;
    public PhotoLoadSuccessAdapter(Context context,List<PhotoDownLoadInfo> photos){
        this.mContext = context;
        this.photos = photos;
        this.select = false;
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
            holder.videoMask = (ImageView)convertView.findViewById(R.id.load_success_video);
            holder.tv_shootTime = (TextView) convertView.findViewById(R.id.load_success_shoottime);
            holder.tv_size = (TextView) convertView.findViewById(R.id.load_success_size);
            holder.tv_loadTime = (TextView) convertView.findViewById(R.id.load_success_time);
            holder.tv_status = (TextView) convertView.findViewById(R.id.load_success_status);
            holder.img_select = (ImageView) convertView.findViewById(R.id.loading_success_img_select);
            convertView.setTag(holder);
        }else{
            holder = (Holder) convertView.getTag();
        }
        PhotoDownLoadInfo info = photos.get(position);
        if (info != null) {

            if (info.getIsVideo() == 0){
                holder.videoMask.setVisibility(View.GONE);
            }else{
                holder.videoMask.setVisibility(View.VISIBLE);
            }
            String previewUrl = info.getPreviewUrl();
            if (holder.img.getTag(R.id.glide_image_tag) == null || !holder.img.getTag(R.id.glide_image_tag).equals(previewUrl)){
                GlideUtil.load(mContext, previewUrl, holder.img);
                holder.img.setTag(R.id.glide_image_tag, previewUrl);
            }
            holder.tv_shootTime.setText(info.getShootTime());
            holder.tv_size.setText(info.getSize()+"MB");
            holder.tv_loadTime.setText(info.getDownLoadTime());
            if (!select){
                holder.img_select.setVisibility(View.GONE);
            }else{
                holder.img_select.setVisibility(View.VISIBLE);
                if (!info.isSelect()){
                    holder.img_select.setImageResource(R.drawable.nosele);
                }else{
                    holder.img_select.setImageResource(R.drawable.sele);
                }
            }
        }
        return convertView;
    }

    public class Holder{
        public ImageView img;
        public ImageView videoMask;
        public TextView tv_shootTime;
        public TextView tv_size;
        public TextView tv_loadTime;
        public TextView tv_status;
        public ImageView img_select;
    }

    public void setPhotos(List<PhotoDownLoadInfo> list){
        this.photos = list;
    }

    public List<PhotoDownLoadInfo> getPhotos(){
        return photos;
    }
    public void setSelect(boolean select){
        this.select = select;
        notifyDataSetChanged();
    }

    public boolean isSelect(){
        return select;
    }
}
