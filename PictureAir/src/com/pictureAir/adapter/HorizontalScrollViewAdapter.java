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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.pictureAir.R;
import com.pictureAir.entity.GoodsInfo;
import com.pictureAir.util.Common;
import com.pictureAir.util.ScreenUtil;

public class HorizontalScrollViewAdapter
{

	private Context mContext;
	private LayoutInflater mInflater;
	private ArrayList<GoodsInfo> mDatas;
	private ImageLoader imageLoader;
	private int width = 0;

	public HorizontalScrollViewAdapter(Context context, ArrayList<GoodsInfo> mDatas)
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

	public int getCount()
	{
		return mDatas.size();
	}

	public Object getItem(int position)
	{
		return mDatas.get(position);
	}

	public long getItemId(int position)
	{
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder viewHolder = null;
		if (convertView == null)
		{
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(
					R.layout.myhorizontalscrollview_item, parent, false);
			viewHolder.mImg = (ImageView) convertView
					.findViewById(R.id.id_index_gallery_item_image);
//			viewHolder.mname = (TextView) convertView
//					.findViewById(R.id.id_index_gallery_item_name);
//			viewHolder.mprice = (TextView) convertView
//					.findViewById(R.id.id_index_gallery_item_price);

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
//		viewHolder.mname.setText(mDatas.get(position).get("type").toString());
//		viewHolder.mprice.setText(mDatas.get(position).get("price").toString());
		return convertView;
	}

	private class ViewHolder
	{
		ImageView mImg;
//		TextView mname;
//		TextView mprice;
	}

}
