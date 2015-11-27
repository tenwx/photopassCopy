package com.pictureAir.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.pictureAir.R;
import com.pictureAir.entity.GoodsInfo;
import com.pictureAir.util.Common;
import com.pictureAir.util.ScreenUtil;

public class MakegiftGoodsAdapter extends BaseAdapter
{

	private Context mContext;
	private LayoutInflater mInflater;
	private ArrayList<GoodsInfo> mDatas;
	private ImageLoader imageLoader;
	private int width = 0;

	public MakegiftGoodsAdapter(Context context, ArrayList<GoodsInfo> mDatas)
	{
		this.mContext = context;
		mInflater = LayoutInflater.from(context);
		this.mDatas = mDatas;
		System.out.println("========="+mDatas.size());
		imageLoader = ImageLoader.getInstance();
	}

	public ArrayList<GoodsInfo> getmDatas() {
		return mDatas;
	}

	public void setmDatas(ArrayList<GoodsInfo> mDatas) {
		this.mDatas = mDatas;
	}
	
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder viewHolder = null;
		if (convertView == null)
		{
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.myhorizontalscrollview_item, parent, false);
			viewHolder.mImg = (ImageView) convertView.findViewById(R.id.id_index_gallery_item_image);
			convertView.setTag(viewHolder);
		} else
		{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		width = ScreenUtil.getScreenWidth(mContext)/3-10;
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, width*3/4);
		viewHolder.mImg.setLayoutParams(params);
		if (mDatas.size() != 0) {
			String[] urlStrings = mDatas.get(position).good_previewUrls.split(",");
			System.err.println("-=-=-=-=-"+urlStrings[0]);
			if (null == urlStrings[0]&&"".equals(urlStrings[0])) {

			}else {
				imageLoader.displayImage(Common.BASE_URL+urlStrings[0], viewHolder.mImg);
			}
		}
		return convertView;
	}

	private class ViewHolder
	{
		ImageView mImg;
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
