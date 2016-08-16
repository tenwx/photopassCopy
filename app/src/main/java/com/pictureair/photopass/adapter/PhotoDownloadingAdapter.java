package com.pictureair.photopass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.DownloadFileStatus;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.GlideUtil;
import com.pictureair.photopass.widget.CircleProgressImage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by pengwu on 16/7/11.
 */
public class PhotoDownloadingAdapter extends BaseAdapter {

    private Context mContext;
    private CopyOnWriteArrayList<DownloadFileStatus> photos;
    private CopyOnWriteArrayList<PhotoInfo> selectPhotos;

    public PhotoDownloadingAdapter(Context context,CopyOnWriteArrayList<DownloadFileStatus> photos){
        this.mContext = context;
        this.photos = photos;
        selectPhotos = new CopyOnWriteArrayList<>();
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
            holder.rl = (RelativeLayout) convertView.findViewById(R.id.rl_photo_loading);
            holder.img = (ImageView) convertView.findViewById(R.id.loading_img);
            holder.tv_shootTime = (TextView) convertView.findViewById(R.id.photo_loading_tv_take_time);
            holder.tv_size = (TextView) convertView.findViewById(R.id.photo_loading_tv_size);
            holder.tv_speed = (TextView) convertView.findViewById(R.id.photo_loading_tv_speed);
            holder.tv_status = (TextView) convertView.findViewById(R.id.photo_loading_tv_status);
            holder.img_status = (CircleProgressImage) convertView.findViewById(R.id.photo_loading_img_status);
            convertView.setTag(holder);
        }else{
            holder = (Holder) convertView.getTag();
        }

        DownloadFileStatus fileStatus = photos.get(position);
        holder.tv_shootTime.setText(fileStatus.getShootOn());
        if (fileStatus != null) {
            try {
                if (holder.img.getTag(R.id.glide_image_tag) == null || !holder.img.getTag(R.id.glide_image_tag).equals(fileStatus.getPhotoThumbnail())) {
                    GlideUtil.load(mContext, fileStatus.getPhotoThumbnail(), holder.img);
                    holder.img.setTag(R.id.glide_image_tag, fileStatus.getPhotoThumbnail());
                }

                switch (fileStatus.status) {
                    case DownloadFileStatus.DOWNLOAD_STATE_WAITING:
                        holder.tv_size.setText("0MB/0MB");
                        holder.tv_speed.setText("0KB/S");
                        holder.tv_status.setText(mContext.getString(R.string.photo_download_waiting));
                        holder.img_status.setImageResource(R.drawable.photo_status_wait);
                        holder.img_status.mCanDraw = false;
                        holder.img_status.setProgress(0);
                        break;
                    case DownloadFileStatus.DOWNLOAD_STATE_FINISH:
                        holder.tv_size.setText(fileStatus.getCurrentSize()+"MB/"+fileStatus.getTotalSize()+"MB");
                        holder.tv_speed.setText(fileStatus.getLoadSpeed()+"KB/S");
                        holder.tv_status.setText(mContext.getString(R.string.photo_download_loading));
                        holder.img_status.setImageResource(R.drawable.photo_status_load);
                        holder.img_status.mCanDraw = true;
                        if (Float.valueOf(fileStatus.getTotalSize()) == 0){
                            holder.img_status.setProgress(0);
                        }else {
                            int pro = (int) ((Float.valueOf(fileStatus.getCurrentSize()) / Float.valueOf(fileStatus.getTotalSize())) * 100);
                            holder.img_status.setProgress(pro);
                        }
                        break;
                    case DownloadFileStatus.DOWNLOAD_STATE_DOWNLOADING:
                        holder.tv_size.setText(fileStatus.getCurrentSize()+"MB/"+fileStatus.getTotalSize()+"MB");
                        holder.tv_speed.setText(fileStatus.getLoadSpeed()+"KB/S");
                        holder.tv_status.setText(mContext.getString(R.string.photo_download_loading));
                        holder.img_status.setImageResource(R.drawable.photo_status_load);
                        holder.img_status.mCanDraw = true;
                        if (Float.valueOf(fileStatus.getTotalSize()) == 0){
                            holder.img_status.setProgress(0);
                        }else {
                            int pro = (int) ((Float.valueOf(fileStatus.getCurrentSize()) / Float.valueOf(fileStatus.getTotalSize())) * 100);
                            holder.img_status.setProgress(pro);
                        }
                        break;
                    case DownloadFileStatus.DOWNLOAD_STATE_FAILURE:
                        holder.tv_size.setText(fileStatus.getCurrentSize()+"MB/"+fileStatus.getTotalSize()+"MB");
                        holder.tv_speed.setText("0KB/S");
                        holder.tv_status.setText(mContext.getString(R.string.photo_download_failed));
                        holder.img_status.setImageResource(R.drawable.photo_status_error);
                        holder.img_status.mCanDraw = false;
                        holder.img_status.setProgress(0);
                        break;

                    case DownloadFileStatus.DOWNLOAD_STATE_RECONNECT:
                        holder.tv_size.setText(fileStatus.getCurrentSize()+"MB/"+fileStatus.getTotalSize()+"MB");
                        holder.tv_speed.setText("0KB/S");
                        holder.tv_status.setText(mContext.getString(R.string.photo_download_reconnect));
                        holder.img_status.setImageResource(R.drawable.photo_status_reconnect);
                        holder.img_status.mCanDraw = false;
                        holder.img_status.setProgress(0);
                        break;
                    case DownloadFileStatus.DOWNLOAD_STATE_SELECT:
                        holder.tv_size.setText(fileStatus.getCurrentSize()+"MB/"+fileStatus.getTotalSize()+"MB");
                        holder.tv_speed.setText("0KB/S");
                        holder.tv_status.setText(mContext.getString(R.string.photo_download_wait_select));
                        if (fileStatus.select == 0) {
                            holder.img_status.setImageResource(R.drawable.nosele);
                        }else{
                            holder.img_status.setImageResource(R.drawable.sele);
                        }
                        holder.img_status.mCanDraw = false;
                        holder.img_status.setProgress(0);
                        break;
                    default:
                        break;
                }
            }catch (Exception e){

            }
        }


        return convertView;
    }

    public class Holder{
        public RelativeLayout rl;
        public ImageView img;
        public TextView tv_shootTime;
        public TextView tv_size;
        public TextView tv_speed;
        public TextView tv_status;
        public CircleProgressImage img_status;
    }

    public void setList(CopyOnWriteArrayList<DownloadFileStatus> photos){
        this.photos = photos;
    }

    public List getList(){
        return photos;
    }
}
