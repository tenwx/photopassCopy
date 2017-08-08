package com.pictureair.photopassCopy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopassCopy.R;
import com.pictureair.photopassCopy.entity.DownloadFileStatus;
import com.pictureair.photopassCopy.util.AppUtil;
import com.pictureair.photopassCopy.util.GlideUtil;
import com.pictureair.photopassCopy.widget.CircleProgressImage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 图片下载
 * Created by pengwu on 16/7/11.
 */
public class PhotoDownloadingAdapter extends BaseAdapter {

    private Context mContext;
    private CopyOnWriteArrayList<DownloadFileStatus> photos;

    public PhotoDownloadingAdapter(Context context,CopyOnWriteArrayList<DownloadFileStatus> photos){
        this.mContext = context;
        this.photos = photos;
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.photo_loading_item,parent, false);
            holder.rl = (RelativeLayout) convertView.findViewById(R.id.rl_photo_loading);
            holder.img = (ImageView) convertView.findViewById(R.id.loading_img);
            holder.videoMask = (ImageView) convertView.findViewById(R.id.loading_video);
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
                if (fileStatus.isVideo() == 0){
                    holder.videoMask.setVisibility(View.GONE);
                }else{
                    holder.videoMask.setVisibility(View.VISIBLE);
                }

                if (holder.img.getTag(R.id.glide_image_tag) == null || !holder.img.getTag(R.id.glide_image_tag).equals(fileStatus.getPhotoThumbnail())) {
                    GlideUtil.load(mContext, fileStatus.getPhotoThumbnail(), holder.img);
                    holder.img.setTag(R.id.glide_image_tag, fileStatus.getPhotoThumbnail());
                }

                double currentSize = fileStatus.getCurrentSize()/1000d/1000d;
                double total = fileStatus.getTotalSize()/1000d/1000d;
                String c = AppUtil.formatData(currentSize);
                String t = AppUtil.formatData(total);

                switch (fileStatus.status) {
                    case DownloadFileStatus.DOWNLOAD_STATE_WAITING:
                        holder.tv_size.setText("0.00MB/0.00MB");
                        holder.tv_speed.setText("0KB/S");
                        holder.tv_status.setText(mContext.getString(R.string.photo_download_waiting));
                        holder.img_status.setImageResource(R.drawable.photo_status_wait);
                        holder.img_status.mCanDraw = false;
                        holder.img_status.setProgress(0);
                        break;
                    case DownloadFileStatus.DOWNLOAD_STATE_FINISH:
                        holder.tv_size.setText(c + "MB/" + t + "MB");
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
                        holder.tv_size.setText(c + "MB/" + t + "MB");
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
                        holder.tv_size.setText(c + "MB/" + t + "MB");
                        holder.tv_speed.setText("0KB/S");
                        holder.tv_status.setText(mContext.getString(R.string.photo_download_failed));
                        holder.img_status.setImageResource(R.drawable.photo_status_error);
                        holder.img_status.mCanDraw = false;
                        holder.img_status.setProgress(0);
                        break;

                    case DownloadFileStatus.DOWNLOAD_STATE_RECONNECT:
                        holder.tv_size.setText(c + "MB/"+ t +"MB");
                        holder.tv_speed.setText("0KB/S");
                        holder.tv_status.setText(mContext.getString(R.string.photo_download_reconnect));
                        holder.img_status.setImageResource(R.drawable.photo_status_reconnect);
                        holder.img_status.mCanDraw = false;
                        holder.img_status.setProgress(0);
                        break;
                    case DownloadFileStatus.DOWNLOAD_STATE_SELECT:
                        holder.tv_size.setText(c + "MB/" + t + "MB");
                        holder.tv_speed.setText("0KB/S");
                        holder.tv_status.setText(mContext.getString(R.string.photo_download_failed));
                        if (fileStatus.select == 0) {
                            holder.img_status.setImageResource(R.drawable.nosele);
                        }else{
                            holder.img_status.setImageResource(R.drawable.sele);
                        }
                        holder.img_status.mCanDraw = false;
                        holder.img_status.setProgress(0);
                        break;
                    case DownloadFileStatus.DOWNLOAD_STATE_UPLOADING:
                        holder.tv_size.setText(c + "MB/" + t + "MB");
                        holder.tv_speed.setText("0KB/S");
                        holder.tv_status.setText(mContext.getString(R.string.photo_download_upload));
                        holder.img_status.setImageResource(R.drawable.photo_status_error);
                        holder.img_status.mCanDraw = false;
                        holder.img_status.setProgress(0);
                        break;
                    default:
                        break;
                }
            }catch (Exception e){
                e.printStackTrace();

            }
        }


        return convertView;
    }

    public class Holder{
        public RelativeLayout rl;
        public ImageView img;
        public ImageView videoMask;
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
