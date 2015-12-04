package com.pictureair.photopass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pictureair.photopass.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 物流信息的适配器
 * @author bauer_bao
 *
 */
public class DeliveryInfoAdatper extends BaseAdapter{

	private ArrayList<HashMap<String, String>> list;
	private LayoutInflater inflater;
	
	public DeliveryInfoAdatper(Context context, ArrayList<HashMap<String, String>> list) {
		this.list = list;
		inflater = LayoutInflater.from(context);
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
		DeliveryHolerView deliveryHolerView = null;
		if (convertView == null) {
			deliveryHolerView = new DeliveryHolerView();
			convertView = inflater.inflate(R.layout.delivery_item, null);
			deliveryHolerView.timePointImageView = (ImageView)convertView.findViewById(R.id.delivery_time);
			deliveryHolerView.deliveryInfoTextView = (TextView)convertView.findViewById(R.id.delivery_info);
			convertView.setTag(deliveryHolerView);
		}else {
			deliveryHolerView = (DeliveryHolerView) convertView.getTag();
		}
		//初始化数据
		if (position == 0) {
			deliveryHolerView.timePointImageView.setImageResource(R.drawable.delivery_current_icon);
			deliveryHolerView.deliveryInfoTextView.setBackgroundResource(R.drawable.delivery_bg_sele);
		}else {
			deliveryHolerView.timePointImageView.setImageResource(R.drawable.delivery_time_point);
			deliveryHolerView.deliveryInfoTextView.setBackgroundResource(R.drawable.delivery_bg_nor);
		}
		deliveryHolerView.deliveryInfoTextView.setText(list.get(position).get("time") + "\n" + list.get(position).get("place"));
		return convertView;
	}
	
	private class DeliveryHolerView{
		ImageView timePointImageView;
		TextView deliveryInfoTextView;
	}

}
