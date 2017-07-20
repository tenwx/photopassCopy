package com.pictureair.photopass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.GoodsInfo;
import com.pictureair.photopass.util.PictureAirLog;

import java.util.List;

/**
 * 制作礼物的商品列表
 */
public class MakegiftGoodsAdapter extends BaseAdapter{
	private Context mContext;
	private LayoutInflater mInflater;
	private List<GoodsInfo> mDatas;

	public MakegiftGoodsAdapter(Context context, List<GoodsInfo> mDatas)
	{
		this.mContext = context;
		mInflater = LayoutInflater.from(context);
		this.mDatas = mDatas;
		PictureAirLog.out("========="+mDatas.size());
	}

	public List<GoodsInfo> getmDatas() {
		return mDatas;
	}

	public void setmDatas(List<GoodsInfo> mDatas) {
		this.mDatas = mDatas;
	}
	
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder viewHolder;
		if (convertView == null)
		{
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.myhorizontalscrollview_item, parent, false);
			viewHolder.mImg = (TextView) convertView.findViewById(R.id.id_index_gallery_item_name);
			convertView.setTag(viewHolder);
		} else
		{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.mImg.setText(mDatas.get(position).getNameAlias());
		return convertView;
	}

	private class ViewHolder
	{
		TextView mImg;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDatas.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
}
