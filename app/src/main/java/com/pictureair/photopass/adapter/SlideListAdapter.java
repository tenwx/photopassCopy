package com.pictureair.photopass.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.PPinfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by pengwu on 16/11/29.
 */

public class SlideListAdapter extends BaseAdapter {

    private List<PPinfo> ppList;
    private Context mContext;
    private boolean mDeleteStatus;
    public SlideListAdapter(Context context, List<PPinfo> ppList, boolean deleteStatus) {
        this.mContext = context;
        this.ppList = ppList;
        this.mDeleteStatus = deleteStatus;
    }

    @Override
    public int getCount() {
        return ppList.size();
    }

    @Override
    public Object getItem(int position) {
        return ppList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void refreshSlideList(List<PPinfo> ppList) {
        this.ppList.clear();
        this.ppList.addAll(ppList);
        notifyDataSetChanged();
    }

    public void refreshDeleteStatus(boolean status) {
        this.mDeleteStatus = status;
        notifyDataSetChanged();
    }

    public boolean getDeleteStatus() {
        return this.mDeleteStatus;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.slide_list_item ,parent, false);
            holder = new Holder();
            holder.tv_count = (TextView) convertView.findViewById(R.id.slide_list_item_count);
            holder.tv_date = (TextView) convertView.findViewById(R.id.slide_list_item_date);
            holder.tv_num = (TextView) convertView.findViewById(R.id.slide_list_item_num);
            holder.img = (ImageView) convertView.findViewById(R.id.slide_list_item_select);
            convertView.setTag(holder);
        }  else {
            holder = (Holder) convertView.getTag();
        }

        PPinfo ppInfo = ppList.get(position);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String loadTime = df.format(new Date());

        if (TextUtils.isEmpty(ppInfo.getShootDate()) && ppInfo.getPhotoCount() == 0) {
            holder.tv_date.setText(R.string.today);
        } else {
            if(ppInfo.getShootDate() != null) {
                if (loadTime.equals(ppInfo.getShootDate())) {
                    holder.tv_date.setText(R.string.today);
                } else {
                    holder.tv_date.setText(ppInfo.getShootDate());
                }
            } else {
                holder.tv_date.setText("");
            }
        }

        holder.tv_count.setText(String.format(mContext.getString(R.string.story_photo_count), ppInfo.getPhotoCount()));
        holder.tv_num.setText(ppInfo.getPpCode());

        if (!mDeleteStatus) {
            holder.img.setVisibility(View.INVISIBLE);
        } else {
            holder.img.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    class Holder {
        TextView tv_date;
        TextView tv_num;
        TextView tv_count;
        ImageView img;
    }
}
