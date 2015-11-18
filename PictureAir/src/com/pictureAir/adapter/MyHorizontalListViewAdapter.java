package com.pictureAir.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.pictureAir.R;
import com.pictureAir.entity.GoodsInfo;
import com.pictureAir.util.Common;
import com.pictureAir.util.ScreenUtil;
/**
 * 水平listview的适配器
 * @author bauer_bao
 *
 */
public class MyHorizontalListViewAdapter extends BaseAdapter {
	
	private Context mContext ;
//	private List<Map<String,Object>> mList;
	private ArrayList<GoodsInfo> mList;
	private ImageLoader imageLoader;
	private int width;
	private SharedPreferences sharedPreferences;
//	private static String IP = "http://192.168.8.204:3001";
	
	public MyHorizontalListViewAdapter(Context context ,ArrayList<GoodsInfo> list){
		this.mContext = context;
		this.mList = list;
		imageLoader = ImageLoader.getInstance();
		sharedPreferences = context.getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
	}
	
	@Override
	public int getCount() {
		return mList.size();
	}

	public ArrayList<GoodsInfo> getmList() {
		return mList;
	}

	public void setmList(ArrayList<GoodsInfo> mList) {
		this.mList = mList;
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);   
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		HolderView holderView = null;
		if(convertView == null ){
			holderView = new HolderView();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.horizontal_listview_item, parent, false);
			holderView.imageView_product =(ImageView) convertView.findViewById(R.id.imageView_product);
			holderView.textView_name = (TextView) convertView.findViewById(R.id.textView_type);
			holderView.textView_price = (TextView) convertView.findViewById(R.id.textView_money);
			holderView.currency = (TextView)convertView.findViewById(R.id.currency);
			convertView.setTag(holderView);
		}else{
			holderView = (HolderView) convertView.getTag();
		}
		
		
		Log.d("myhorizontallistviewadapter:", mList.get(position).good_previewUrls);
		String url[] = mList.get(position).good_previewUrls.split(",");
		Log.d("myhorizontallistviewadapter:", Common.BASE_URL+url[0]);
		
		width = ScreenUtil.getScreenWidth(mContext)/3-10;
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, width*3/4);
		holderView.imageView_product.setLayoutParams(params);
		
		if (url.length==0||null==url[0]||url[0].equals("")) {
			url[0]="";
			imageLoader.displayImage(url[0], holderView.imageView_product);
		}else {
			
			imageLoader.displayImage(Common.BASE_URL+url[0], holderView.imageView_product);
		}
//		holderView.imageView_product.setImageResource((Integer)(mList.get(position).get("pic")));
		holderView.textView_name.setText((String) mList.get(position).good_nameAlias);
		holderView.textView_price.setText((String) mList.get(position).good_price);
		holderView.currency.setText(sharedPreferences.getString(Common.CURRENCY, Common.DEFAULT_CURRENCY));
		return convertView;
	}
	
	class HolderView{
		TextView currency;
		ImageView imageView_product;
		TextView textView_name;
		TextView textView_price;
	}
}
