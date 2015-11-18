package com.pictureAir.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pictureAir.R;
import com.pictureAir.util.ScreenUtil;

public class FrameGridViewAdapter extends BaseAdapter{
	private ArrayList<String> arrayList;
	private Context context;
	private int count;
	private int width;
	
	public FrameGridViewAdapter(Context context,ArrayList<String> list,int count) {
		arrayList = list;
		this.count = count;
		this.context = context;
		for (int i = 0; i < arrayList.size(); i++) {
			System.out.println(arrayList.get(i));
		}
		width = ScreenUtil.getScreenWidth(context);
	}

	@Override
	public int getCount() {
		return arrayList.size();
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
			convertView = LayoutInflater.from(context).inflate(R.layout.gridview_item, null);
			holder.iv = (ImageView) convertView.findViewById(R.id.grid_item_imageview);
			holder.tx = (TextView)convertView.findViewById(R.id.grid_item_text);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		android.widget.LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width/4-5, width/3);
		holder.iv.setLayoutParams(params);
		//先加载默认的边框，之后再加载网络边框，第一个边框是默认没有边框
//		if (position<pic.length) {
//			if (position==0) {
//				holder.iv.setBackgroundResource(0);
//			}else {
//				holder.iv.setBackgroundResource(pic[position]);
//			}
//			
//		}else {
//			holder.iv.setBackgroundResource(R.drawable.ic_empty);
//		}
//		if (position==0) {//0的时候加载一张默认图片
//			
//		}else {//要判断是否超出自带图片的范围，在这里需要多加一个判断
//			
		
			holder.iv.setImageResource(Integer.valueOf(arrayList.get(position)));
//		}
		holder.tx.setVisibility(View.GONE);
		return convertView;
	}
	private class ViewHolder{
		ImageView iv;
		TextView tx;
	}
}
