package com.pictureair.photopass.adapter;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.DiscoverItemInfo;

import java.util.List;

/**
 * Created by pengwu on 16/9/27.
 */
public class DiscoverListItemAdapter extends BaseAdapter{

    private Context mContext;
    private List<DiscoverItemInfo.DiscoverListItemInfo> list;

    public DiscoverListItemAdapter(Context context, List<DiscoverItemInfo.DiscoverListItemInfo> list) {
        this.mContext = context;
        this.list = list;
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
        Holder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.discover_listview_item2, null);
            holder = new Holder();
            holder.itemImage = (ImageView) convertView.findViewById(R.id.discover_item_image1);
            holder.itemText = (TextView) convertView.findViewById(R.id.discover_item_tv1);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        DiscoverItemInfo.DiscoverListItemInfo itemInfo = list.get(position);
        if (holder.itemImage.getTag(R.id.glide_image_tag) == null || !holder.itemImage.getTag(R.id.glide_image_tag).equals(itemInfo.itemImageUrl)) {
            holder.itemImage.setImageResource(itemInfo.itemImageId);
            holder.itemImage.setTag(R.id.glide_image_tag,itemInfo.itemImageUrl);
        }
        holder.itemText.setText(itemInfo.itemText);
        return convertView;
    }

    private class Holder {
        ImageView itemImage;
        TextView itemText;
    }
}
