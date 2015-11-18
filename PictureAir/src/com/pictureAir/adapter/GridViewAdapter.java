package com.pictureAir.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.pictureAir.R;

public class GridViewAdapter extends BaseAdapter {

	private ArrayList<Bitmap> adapterList;
	private Context mContext;
	private LayoutInflater inflater;

	public GridViewAdapter(Context context, ArrayList<Bitmap> dataList) {
		mContext = context;
		inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		adapterList = dataList;

	}

	@Override
	public int getCount() {

		return adapterList.size();
	}

	@Override
	public Object getItem(int position) {

		return position;
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.pop_grid_item, null);
			holder.iv = (ImageView) convertView.findViewById(R.id.grid_item_iv);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.iv.setImageBitmap(adapterList.get(position));
		return convertView;
	}
		private class ViewHolder{
			ImageView iv;
		}
}
