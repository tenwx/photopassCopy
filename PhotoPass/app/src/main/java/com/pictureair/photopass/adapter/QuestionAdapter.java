package com.pictureair.photopass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.QuestionInfo;

import java.util.ArrayList;

/**
 * ListView适配器
 * 
 * 
 * 
 */
public class QuestionAdapter extends BaseAdapter {

	private ArrayList<QuestionInfo> mTitleArray;// 问题列表
	private LayoutInflater inflater = null;
	private Context mContext;

	/**
	 * Adapter构造方法
	 * 
	 * @param titleArray
	 */
	public QuestionAdapter(Context context, ArrayList<QuestionInfo> titleArray) {
		// TODO Auto-generated constructor stub
		this.mTitleArray = titleArray;
		this.mContext = context;
		inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	/**
	 * 获取总数
	 */
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mTitleArray.size();
	}

	/**
	 * 获取Item内容
	 */
	@Override
	public QuestionInfo getItem(int position) {
		// TODO Auto-generated method stub
		return mTitleArray.get(position);
	}

	/**
	 * 获取Item的ID
	 */
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;

		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.list_item_layout, null);
			holder.titleTv = (TextView) convertView.findViewById(R.id.title_tv);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// 设置
		holder.titleTv.setText(mTitleArray.get(position).questionName);
		return convertView;
	}

	static class ViewHolder {
		TextView titleTv;
	}

	/**
	 * 刷新数据
	 * 
	 * @param array
	 */
	public void refreshData(ArrayList<QuestionInfo> array) {
		this.mTitleArray = array;
		notifyDataSetChanged();
	}

}