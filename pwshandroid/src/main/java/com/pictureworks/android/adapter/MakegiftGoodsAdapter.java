package com.pictureworks.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.pictureworks.android.R;
import com.pictureworks.android.entity.GoodsInfo;
import com.pictureworks.android.util.PictureAirLog;

import java.util.List;

public class MakegiftGoodsAdapter extends BaseAdapter
{

	private Context mContext;
	private LayoutInflater mInflater;
	private List<GoodsInfo> mDatas;
	private ImageLoader imageLoader;
	private int width = 0;

	public MakegiftGoodsAdapter(Context context, List<GoodsInfo> mDatas)
	{
		this.mContext = context;
		mInflater = LayoutInflater.from(context);
		this.mDatas = mDatas;
		PictureAirLog.out("========="+mDatas.size());
		imageLoader = ImageLoader.getInstance();
	}

	public List<GoodsInfo> getmDatas() {
		return mDatas;
	}

	public void setmDatas(List<GoodsInfo> mDatas) {
		this.mDatas = mDatas;
	}
	
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder viewHolder = null;
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
//		width = ScreenUtil.getScreenWidth(mContext)/3-10;
//		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, width*3/4);
//		viewHolder.mImg.setLayoutParams(params);
//		if (mDatas.size() != 0) {
//			if (null == mDatas.get(position).getPictures().get(0).getUrl()&&"".equals(mDatas.get(position).getPictures().get(0).getUrl())) {
//
//			}else {
//				imageLoader.displayImage(Common.BASE_URL + mDatas.get(position).getPictures().get(0).getUrl(), viewHolder.mImg);
//			}
//		}
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
