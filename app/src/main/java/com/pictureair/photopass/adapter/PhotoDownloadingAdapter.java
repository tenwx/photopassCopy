package com.pictureair.photopass.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.DownloadFileStatus;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.service.DownloadService;
import com.pictureair.photopass.widget.CircleProgressImage;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by pengwu on 16/7/11.
 */
public class PhotoDownloadingAdapter extends BaseAdapter {

    private Context mContext;
    private CopyOnWriteArrayList<DownloadFileStatus> photos;
    private ListView listView;
    private OnItemClickListener childClickListener;

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
            ImageAware imageAware = new ImageViewAware(holder.img);
            if (holder.img.getTag() == null || !holder.img.getTag().equals(fileStatus.getPhotoThumbnail())) {
                ImageLoader.getInstance().displayImage(fileStatus.getPhotoThumbnail(),imageAware);
                holder.img.setTag(fileStatus.getPhotoThumbnail());
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
                default:
                    break;
            }

            childClickListener = new OnItemClickListener(position,holder);
            holder.rl.setOnClickListener(childClickListener);

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

    private class OnItemClickListener implements View.OnClickListener{
        private int position;
        private Holder holder;
        public OnItemClickListener(int position,Holder holder){
            this.position = position;
            this.holder = holder;
        }

        @Override
        public void onClick(View v) {
            DownloadFileStatus fileStatus = photos.get(position);
            if (fileStatus!= null && fileStatus.status == DownloadFileStatus.DOWNLOAD_STATE_FAILURE){
                fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_RECONNECT;
                fileStatus.setCurrentSize("0");
                fileStatus.setLoadSpeed("0");
                fileStatus.setTotalSize("0");

                holder.tv_size.setText("0MB/0MB");
                holder.tv_speed.setText("0KB/S");
                holder.tv_status.setText(mContext.getString(R.string.photo_download_reconnect));
                holder.img_status.setImageResource(R.drawable.photo_status_reconnect);
                holder.img_status.mCanDraw = false;
                ArrayList<PhotoInfo> photos = new ArrayList<>();
                PhotoInfo info = new PhotoInfo();
                info.isVideo = fileStatus.isVideo();
                info.photoPathOrURL = fileStatus.getUrl();
                info.photoId = fileStatus.getPhotoId();
                info.shootOn = fileStatus.getShootOn();
                info.failedTime = fileStatus.getFailedTime();

                photos.add(info);
                Intent intent = new Intent(mContext, DownloadService.class);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("photos", photos);
                bundle.putInt("reconnect",1);
                bundle.putInt("one",0);
                intent.putExtras(bundle);
                mContext.startService(intent);
            }
        }
    }
}
