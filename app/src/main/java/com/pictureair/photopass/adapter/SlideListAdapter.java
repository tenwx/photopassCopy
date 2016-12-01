package com.pictureair.photopass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.PPinfo;

import java.util.List;

/**
 * Created by pengwu on 16/11/29.
 */

public class SlideListAdapter extends BaseAdapter {

    private List<PPinfo> ppList;
    private Context mContext;
    public SlideListAdapter(Context context, List<PPinfo> ppList) {
        this.mContext = context;
        this.ppList = ppList;
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

    public void setPPlist(List<PPinfo> list) {
        this.ppList = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.slide_list_item ,null);
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
        holder.tv_date.setText(ppInfo.getShootDate());
        holder.tv_count.setText(String.format(mContext.getString(R.string.story_photo_count), ppInfo.getPhotoCount()));
        holder.tv_num.setText(ppInfo.getPpCode());

        if (ppInfo.getIsSelected() == 0) {
            holder.img.setImageResource(R.drawable.nosele);
        } else {
            holder.img.setImageResource(R.drawable.sele);
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
