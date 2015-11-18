package com.pictureAir.adapter;

import java.util.List;

import com.pictureAir.R;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class FontAdapter extends BaseAdapter {
	private Context context;
	private List<String> list;
	private List<Typeface>  strings;

	public FontAdapter(Context context, List<String> list, List<Typeface> strings) {
		this.context = context;
		this.list = list;
		this.strings = strings;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		GridViewList gridViewList = null;
		if (convertView == null) {
			gridViewList = new GridViewList();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.font_list, parent, false);
			gridViewList.textView = (TextView) convertView
					.findViewById(R.id.fontView);
			convertView.setTag(gridViewList);
		} else {
			gridViewList = (GridViewList) convertView.getTag();
		}
		gridViewList.textView.setText(list.get(position));
		gridViewList.textView.setTypeface(strings.get(position));
		gridViewList.textView.setGravity(Gravity.CENTER);
		return convertView;
	}

	class GridViewList {
		TextView textView;
	}
}
