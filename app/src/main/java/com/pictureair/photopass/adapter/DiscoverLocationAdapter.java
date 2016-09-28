package com.pictureair.photopass.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.WebViewActivity;
import com.pictureair.photopass.entity.DiscoverItemInfo;
import java.util.List;

/**
 * discover页面location的适配器
 *
 * @author bauer_bao
 */
public class DiscoverLocationAdapter extends BaseAdapter {


    private Context context;
    private List<DiscoverItemInfo> itemList;

    public DiscoverLocationAdapter(Context context, List<DiscoverItemInfo> list) {
        this.context = context;
        this.itemList = list;
    }


    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.discover_listview_item,null);
            holder = new Holder();
            holder.discover_image = (ImageView) convertView.findViewById(R.id.discover_image);
            holder.discover_tv = (TextView) convertView.findViewById(R.id.discover_tv);
            holder.time = (TextView) convertView.findViewById(R.id.discover_tv_time);
            holder.itemListView = (ListView) convertView.findViewById(R.id.discover_item_list);
            holder.layout = (RelativeLayout) convertView.findViewById(R.id.discover_layout);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        DiscoverItemInfo info = itemList.get(position);
        holder.time.setText(info.updateTime);
        if (holder.discover_image.getTag(R.id.glide_image_tag) == null || !holder.discover_image.getTag(R.id.glide_image_tag).equals(info.mainViewUrl)) {
            holder.discover_image.setImageResource(info.imageId);
            holder.discover_image.setTag(R.id.glide_image_tag,info.mainViewUrl);
        }
        holder.discover_tv.setText(info.mainViewText);
        DiscoverListItemAdapter adapter = new DiscoverListItemAdapter(context,info.itemList);
        holder.itemListView.setAdapter(adapter);
        setListViewHeightBasedOnChildren(holder.itemListView);
        holder.layout.setOnClickListener(new OnItemClickListener(position,info));
        holder.itemListView.setOnItemClickListener(new OnListItemClickListener(info.itemList));
        return convertView;
    }

    private void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    private class Holder {
        TextView time;
        ImageView discover_image;
        TextView discover_tv;
        ListView itemListView;
        RelativeLayout layout;
    }

    private class OnItemClickListener implements OnClickListener {
        private int mPosiotion;
        private DiscoverItemInfo info;
        public OnItemClickListener(int posiotion,  DiscoverItemInfo info) {
            this.mPosiotion = posiotion;
            this.info = info;
        }
        @Override
        public void onClick(View v) {
            Intent i = new Intent();
            i.setClass(context, WebViewActivity.class);
            i.putExtra("key",3);
            context.startActivity(i);
        }
    }

    private class OnItemLongClickListener implements View.OnLongClickListener {
        private int mPosiotion;
        public OnItemLongClickListener(int posiotion) {
            this.mPosiotion = posiotion;
        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }
    }

    private class OnListItemClickListener implements AdapterView.OnItemClickListener {
        private List<DiscoverItemInfo.DiscoverListItemInfo> list;
        public OnListItemClickListener(List<DiscoverItemInfo.DiscoverListItemInfo> list) {
            this.list = list;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent i = new Intent();
            i.setClass(context, WebViewActivity.class);
            i.putExtra("key",3);
            context.startActivity(i);
        }
    }

    private class OnListItemLongClickListener implements AdapterView.OnItemLongClickListener {
        private List<DiscoverItemInfo.DiscoverListItemInfo> list;
        public OnListItemLongClickListener(List<DiscoverItemInfo.DiscoverListItemInfo> list) {
            this.list = list;
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            return false;
        }
    }


}
