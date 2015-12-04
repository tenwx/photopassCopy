package com.pictureair.photopass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.pictureair.photopass.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class GridAdapter extends BaseAdapter {

	ArrayList<HashMap<String, String>> gridviewList;
	Context context;
	private LayoutInflater mInflater;
	ImageLoader imageLoader;
//	public ArrayList<HolderView2> list = null;
	public ArrayList<HashMap<String, String>> getGridviewList() {
		return gridviewList;
	}

	public void setGridviewList(ArrayList<HashMap<String, String>> gridviewList) {
		this.gridviewList = gridviewList;
	}

	public GridAdapter(Context c, ArrayList<HashMap<String, String>> list) {
		gridviewList = list;
		context = c;
		mInflater = LayoutInflater.from(c);
		imageLoader = ImageLoader.getInstance();
		for (int i = 0; i < gridviewList.size(); i++) {
			System.out.println(gridviewList.get(i).get("position"));
			// System.out.println(gridviewList.get(i).get("count"));
		}
		System.out.println("adapter");
	}

	@Override
	public View getView(int position, View convertView2, ViewGroup parent) {
		// TODO Auto-generated method stub
		HolderView2 holderView2 = null;
		String urlString = gridviewList.get(position).get("position");
		System.out.println(position + "_" + gridviewList.size());
//		list = new ArrayList<GridAdapter.HolderView2>();
		for (int i = 0; i < gridviewList.size(); i++) {
			System.out.println(gridviewList.get(i).get("position"));
		}
		if (convertView2 == null) {
			holderView2 = new HolderView2();
			convertView2 = mInflater.inflate(R.layout.cart_grid_item, null);
			holderView2.imageView = (ImageView) convertView2
					.findViewById(R.id.imagecartItem);
			holderView2.countTextView = (TextView) convertView2
					.findViewById(R.id.textView1_sl);
			convertView2.setTag(holderView2);
		} else {
			holderView2 = (HolderView2) convertView2.getTag();
		}
//		list.add(holderView2);
		if (gridviewList.size()==0||urlString.equals(""))
		{//如果长度为0，或者为空，显示默认图片
			holderView2.imageView.setImageResource(R.drawable.empty);
		}else {
			System.out.println("show photo");
			File imageFile = new
					File(gridviewList.get(position).get("position"));
			if (imageFile.exists()&&imageFile.length()!=0) {
				System.out.println("存在文件");
				String id = gridviewList.get(position).get("id");
				System.out.println("id:"+id);
				imageLoader.displayImage("file://"+gridviewList.get(position).get("position"), holderView2.imageView);
//				BitmapCompress bitmapCompress = new BitmapCompress(context,
//						holderView2.imageView);
//				bitmapCompress.execute(id);
			}
		}
		holderView2.countTextView.setText(String.valueOf(gridviewList.get(position).get("count")));
		holderView2.imageView.setScaleType(ScaleType.CENTER_CROP);//按比例扩大图片的size居中显示，使得图片长(宽)等于或大于View的长(宽)
		return convertView2;
	}

	//	public void setData() {
	//		System.out.println(3);
	//		for (int i = 0; i < gridviewList.size(); i++) {
	//			HolderView2 holderView2 = list.get(i);
	//			 if (gridviewList.size()==0||gridviewList.get(i).get("position").equals(""))
	//			 {//如果长度为0，或者为空，显示默认图片
	//			 holderView2.imageView.setImageResource(R.drawable.empty);
	//			 }else {
	//			 System.out.println("show photo");
	//			 File imageFile = new
	//			 File(gridviewList.get(i).get("position"));
	//			 if (imageFile.exists()&&imageFile.length()!=0) {
	//			 System.out.println("存在文件");
	//			 String id = gridviewList.get(i).get("id");
	//			 System.out.println("id:"+id);
	//			 BitmapCompress bitmapCompress = new BitmapCompress(context,
	//			 holderView2.imageView);
	//			 bitmapCompress.execute(id);
	//			 }
	//			 }
	//			 holderView2.countTextView.setText(String.valueOf(gridviewList.get(i).get("count")));
	//			 holderView2.imageView.setScaleType(ScaleType.CENTER_CROP);//按比例扩大图片的size居中显示，使得图片长(宽)等于或大于View的长(宽)
	//		}
	//	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return gridviewList.size() > 0 ? gridviewList.get(position) : position;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return gridviewList.size() > 0 ? gridviewList.size() : 1;
	}

	public class HolderView2 {
		ImageView imageView;// 图片
		TextView countTextView;// 图片右上角的数量
	}

}
