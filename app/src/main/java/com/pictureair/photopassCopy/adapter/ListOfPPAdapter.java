package com.pictureair.photopassCopy.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopassCopy.R;
import com.pictureair.photopassCopy.entity.PPPinfo;
import com.pictureair.photopassCopy.entity.PPinfo;
import com.pictureair.photopassCopy.entity.PhotoInfo;
import com.pictureair.photopassCopy.util.AppUtil;
import com.pictureair.photopassCopy.util.Common;
import com.pictureair.photopassCopy.util.GlideUtil;
import com.pictureair.photopassCopy.util.PictureAirLog;
import com.pictureair.photopassCopy.util.ScreenUtil;
import com.pictureair.photopassCopy.widget.PWToast;

import java.util.ArrayList;

/**
 * pp数据的适配器
 */
public class ListOfPPAdapter extends BaseAdapter {
    private ArrayList<PPinfo> arrayList;
    private Context mContext;
    private ViewHolder holder;
    private PWToast myToast;
    private Handler mHandler;
    private OnItemChildClickListener childClickListener;
    private int useNumber = 0;//已经使用的个数
    private int choice = 0;//选中的个数
    private SparseBooleanArray map;//统计被勾选的子项
    private PPPinfo pppInfo;

    public ListOfPPAdapter(ArrayList<PPinfo> list, Context mContext, Handler mHandler, PPPinfo pppInfo) {
        this.arrayList = list;
        this.mContext = mContext;
        this.mHandler = mHandler;
        myToast = new PWToast(mContext);

        this.pppInfo = pppInfo;
        map = new SparseBooleanArray();
        useNumber = pppInfo.bindInfo.size();
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // 初始化view
        holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.my_pp_list, parent, false);
            holder.ppCode = (TextView) convertView.findViewById(R.id.pp_code);
            holder.deleteMyPP = (ImageView) convertView.findViewById(R.id.delete_my_pp);
            holder.image1 = (ImageView) convertView.findViewById(R.id.pp_img1);
            holder.pp_imageLayout = (RelativeLayout) convertView.findViewById(R.id.pp_image_layout);
            holder.tvPhotoCount = (TextView) convertView.findViewById(R.id.pp_photo_count);
            holder.tvShootDate = (TextView) convertView.findViewById(R.id.tv_shoot_date);
            holder.itemLayout = (LinearLayout) convertView.findViewById(R.id.pp_item);
            holder.tvPPShowSeclect = (TextView) convertView.findViewById(R.id.pp_show_select);
            holder.videoCover = (ImageView) convertView.findViewById(R.id.pp_video_cover);
            holder.pp_card_layout = (RelativeLayout) convertView.findViewById(R.id.pp_card_layout);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        childClickListener = new OnItemChildClickListener(position);
        holder.itemLayout.setOnClickListener(childClickListener);

        holder.deleteMyPP.setVisibility(View.GONE);
        holder.img_no_check = (ImageView) convertView.findViewById(R.id.img);
        holder.img_no_check.setVisibility(View.VISIBLE);
        holder.tvPPShowSeclect.setVisibility(View.VISIBLE);
        holder.tvShootDate.setVisibility(View.VISIBLE);
        //初始化选中与否
        if (arrayList.get(position).getIsSelected() == 0) {//未选中
            holder.img_no_check.setImageResource(R.drawable.nosele);
        } else if (arrayList.get(position).getIsSelected() == 1) {//选中
            holder.img_no_check.setImageResource(R.drawable.sele);
        } else {//不可选
            holder.img_no_check.setImageResource(R.drawable.del1);
        }

        //设置布局
        int imgViewWidth = ScreenUtil.getScreenWidth(mContext)-ScreenUtil.dip2px(mContext, 10) * 2;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(imgViewWidth, imgViewWidth);
        holder.image1.setLayoutParams(params);
        holder.videoCover.setLayoutParams(params);

        RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) holder.pp_card_layout.getLayoutParams();
        params1.width = imgViewWidth;
        holder.pp_card_layout.setLayoutParams(params1);

        if (arrayList == null || arrayList.size() <= 0) {
            return convertView;
        }

        // 初始化pp码
        PPinfo ppInfo1 = arrayList.get(position);
        holder.ppCode.setText(ppInfo1.getPpCode());
        holder.tvPhotoCount.setText(String.format(mContext.getResources().getString(R.string.story_photo_count),ppInfo1.getPhotoCount()));

        //设置日期
        if (ppInfo1.getPhotoCount() == 0) {
            if (AppUtil.getFormatCurrentTime().substring(0,10).equals(ppInfo1.getShootDate())) {
                holder.tvShootDate.setText(R.string.today);
            } else {
                holder.tvShootDate.setText(ppInfo1.getShootDate());
            }
        } else {
            holder.tvShootDate.setText(ppInfo1.getShootDate());
        }

        //显示图片
        PhotoInfo photoInfo = ppInfo1.getAlbumCoverPhotoInfo();
        boolean isEncrypt;
        boolean isVideo;
        String url;
        if (photoInfo == null) {
            isEncrypt = false;
            isVideo = false;
            url = "";

        } else {
            isEncrypt = photoInfo.getIsEnImage() == 1;
            isVideo = photoInfo.getIsVideo() == 1;
            if (photoInfo.getIsPaid() == 1) {
                url = Common.PHOTO_URL + photoInfo.getPhotoThumbnail_512();

            } else {
                url = photoInfo.getPhotoThumbnail_128();

            }
        }

        holder.videoCover.setVisibility(isVideo ? View.VISIBLE : View.INVISIBLE);
        if (holder.image1.getTag(R.id.glide_image_tag) == null || !holder.image1.getTag(R.id.glide_image_tag).equals(url)) {
            GlideUtil.load(mContext, url, isEncrypt, holder.image1);
            holder.image1.setTag(R.id.glide_image_tag, url);
        }
        return convertView;
    }

    private class ViewHolder {
        TextView ppCode;// pp码
        ImageView deleteMyPP, img_no_check;//删除PP，选中按钮
        ImageView image1, videoCover;
        LinearLayout itemLayout;
        TextView tvShootDate;
        TextView tvPhotoCount, tvPPShowSeclect;
        RelativeLayout pp_imageLayout, pp_card_layout;
    }

    //点击一个item的事件
    private class OnItemChildClickListener implements OnClickListener {
        private int position;

        public OnItemChildClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if (arrayList.get(position) instanceof PPinfo) {
                PPinfo ppInfo = arrayList.get(position);
                switch (v.getId()) {
                    case R.id.img:
                    case R.id.pp_item:
                        PictureAirLog.out("adapter---size=" + map.size());
                        if (null != mHandler) {
                            Message msg = mHandler.obtainMessage();
                            if (ppInfo.getIsSelected() == 0) {
                                if (useNumber >= pppInfo.capacity) {
                                    myToast.setTextAndShow(R.string.outofrange, Common.TOAST_SHORT_TIME);
                                    break;
                                } else {
                                    ++choice;
                                    ppInfo.setIsSelected(1);
                                    ++useNumber;
                                    map.put(position, ppInfo.getIsSelected() == 1);
                                }
                            } else if (ppInfo.getIsSelected() == 2) {

                            } else {
                                --choice;
                                if (choice == 0) {
                                    for (int j = 0; j < arrayList.size(); j++) {
                                        PPinfo pp = arrayList.get(j);
                                        if (pp.getIsSelected() == 2) {
                                            pp.setIsSelected(0);
                                        }
                                    }
                                }
                                ppInfo.setIsSelected(0);
                                --useNumber;
                                map.delete(position);
                            }
                            notifyDataSetChanged();
                            msg.arg1 = useNumber;
                            msg.what = 2;
                            mHandler.sendMessage(msg);
                        }
                        break;

                    default:
                        break;
                }
            }
        }
    }

    public SparseBooleanArray getMap() {
        return map;
    }

    public void setMap(SparseBooleanArray map) {
        this.map = map;
    }


}
