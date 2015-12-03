package com.pictureair.photopass.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.GoodsInfo;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.util.UniversalImageLoadTool;

import java.util.ArrayList;

/**
 * shop页商品列表的适配器
 * @author bauer_bao
 *
 */
public class ShopGoodListViewAdapter extends BaseAdapter{
	private ArrayList<GoodsInfo> goodList;
	private LayoutInflater layoutInflater;
	private String currency;
	private final static String TAG = "ShopGoodListViewAdapter";
	private int width = 0;
	private DisplayImageOptions options;
	
	public ShopGoodListViewAdapter(ArrayList<GoodsInfo> list, Context c, String currency) {
		goodList = list;
		layoutInflater = LayoutInflater.from(c);
		this.currency = currency;
		width = ScreenUtil.getScreenWidth(c) - ScreenUtil.dip2px(c, 10);
		options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_discover_loading).
				showImageOnFail(R.drawable.ic_discover_failed).cacheInMemory(true).cacheOnDisk(true).build();
		Log.d(TAG, "good list size is " + goodList.size());
	}
	
	@Override
	public int getCount() {
		return goodList.size();
	}

	@Override
	public Object getItem(int position) {
		return goodList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = layoutInflater.inflate(R.layout.good_listview_item, null);
			viewHolder.goodNameAlias = (TextView) convertView.findViewById(R.id.good_name);
//			viewHolder.goodSupriseTextView = (TextView) convertView.findViewById(R.id.good_surprise);
			viewHolder.goodPrice = (TextView) convertView.findViewById(R.id.good_price);
			viewHolder.goodCurrency = (TextView) convertView.findViewById(R.id.good_currency);
			viewHolder.goodDetailIntroduce = (TextView) convertView.findViewById(R.id.goodDetailIntroduce);
			viewHolder.goodImageView = (ImageView) convertView.findViewById(R.id.goodImageView);
			convertView.setTag(viewHolder);
		}else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		LayoutParams layoutParams = viewHolder.goodImageView.getLayoutParams();
		layoutParams.width = width;
		layoutParams.height = width / 2;
//		viewHolder.goodImageView.setLayoutParams(layoutParams);
		//初始化数据
		viewHolder.goodNameAlias.setText(goodList.get(position).good_nameAlias);
		if (goodList.get(position).good_promotionPrice == null) {//促销价为空
//			viewHolder.goodSupriseTextView.setVisibility(View.INVISIBLE);
			viewHolder.goodPrice.setText(goodList.get(position).good_price);
		}else {//有促销价
//			viewHolder.goodSupriseTextView.setVisibility(View.VISIBLE);
			viewHolder.goodPrice.setText(goodList.get(position).good_promotionPrice);
		}
		viewHolder.goodCurrency.setText(currency);
		viewHolder.goodDetailIntroduce.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad");
//		viewHolder.goodDetailIntroduce.setText(R.string.goods_intro);
//		viewHolder.goodDetailIntroduce.setText(goodList.get(position).good_detail);
		System.out.println("good info=========>"+goodList.get(position).good_previewUrls);
		String url[] = goodList.get(position).good_previewUrls.split(",");
		Log.d(TAG, "photo url:" + Common.BASE_URL+url[0]);
		
		if (url.length==0||null==url[0]||url[0].equals("")) {
			System.out.println("----> url is null");
			url[0]="";
			UniversalImageLoadTool.loadDiscoverImage(url[0], viewHolder.goodImageView, options);
			viewHolder.goodImageView.setTag("null");
		}else {
			System.out.println("url is "+ url[0]);
			UniversalImageLoadTool.loadDiscoverImage(Common.BASE_URL+url[0], viewHolder.goodImageView, options);
		}
		return convertView;
	}
	
	private class ViewHolder{
		TextView goodNameAlias;//商品的别名
//		TextView goodSupriseTextView;//商品的惊爆价
		TextView goodPrice;//商品的价格
		TextView goodCurrency;//商品的币种
		ImageView goodImageView;//商品的预览图片
		TextView goodDetailIntroduce;//商品的详细介绍
	}

}
