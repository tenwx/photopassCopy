package com.pictureAir.adapter;

import java.util.ArrayList;
import java.util.List;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.paypal.android.sdk.ca;
import com.pictureAir.R;
import com.pictureAir.SubmitOrderActivity;
import com.pictureAir.entity.CartItemInfo;
import com.pictureAir.entity.CartPhotosInfo;
import com.pictureAir.util.Common;
import com.pictureAir.util.ScreenUtil;
import com.pictureAir.widget.BadgeView;
import com.pictureAir.widget.DashedLineView;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class SubmitOrderListViewAdapter extends BaseAdapter{
	private ArrayList<CartItemInfo> arrayList;
	private Context context;
	private LayoutInflater layoutInflater;
	private ImageLoader imageLoader;
	private ArrayList<ImageView> imageViews;
	private List<CartPhotosInfo> gridviewlist;
	private ArrayList<ArrayList<ImageView>> gridLayoutLists;
	private Handler handler;
	private String currency;
	private static final String TAG = "SubmitOrderListViewAdapter";
	public SubmitOrderListViewAdapter(Context context, ArrayList<CartItemInfo> list, String currency, Handler handler) {
		this.context = context;
		this.handler = handler;
		this.currency = currency;
		arrayList = list;
		layoutInflater = LayoutInflater.from(context);
		imageLoader = ImageLoader.getInstance();
		gridLayoutLists = new ArrayList<ArrayList<ImageView>>();
	}
	
	@Override
	public int getCount() {
		return arrayList.size();
	}

	@Override
	public Object getItem(int position) {
		return arrayList.get(position);
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
			convertView = layoutInflater.inflate(R.layout.submitorder_listview_item, null);
			viewHolder.goodImageView = (ImageView) convertView.findViewById(R.id.imageView_pd);
			viewHolder.goodNameTextView = (TextView) convertView.findViewById(R.id.textView_name);
			viewHolder.currencyTextView = (TextView) convertView.findViewById(R.id.textView_currency);
			viewHolder.goodPriceTextView = (TextView) convertView.findViewById(R.id.textView_pr);
			viewHolder.goodQuentityTextView = (TextView) convertView.findViewById(R.id.editText_count1);
			viewHolder.goodPhotosGridLayout = (GridLayout) convertView.findViewById(R.id.gridView_cartphoto);
			viewHolder.goodRelativeLayout = (RelativeLayout) convertView.findViewById(R.id.submitOrderRelativeLayout);
			viewHolder.goodDashedLineView = (DashedLineView) convertView.findViewById(R.id.submitLine1);
			viewHolder.showOrHidePhotoImageView = (ImageView) convertView.findViewById(R.id.showOrHeight);
			convertView.setTag(viewHolder);
		}else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		gridviewlist = arrayList.get(position).cart_photoUrls;
		imageViews = new ArrayList<ImageView>();
		
		//初始化数据
		if (Common.PHOTOPASS_NAME.equals(arrayList.get(position).cart_productName)) {//照片产品
			Log.d(TAG, "pp product");
			if (arrayList.get(position).cart_productImageUrl.contains("http")) {
				imageLoader.displayImage(arrayList.get(position).cart_productImageUrl, viewHolder.goodImageView);
			}else {
				imageLoader.displayImage(Common.PHOTO_URL + arrayList.get(position).cart_productImageUrl, viewHolder.goodImageView);
			}
			viewHolder.goodPhotosGridLayout.setVisibility(View.GONE);
			viewHolder.goodDashedLineView.setVisibility(View.GONE);
			viewHolder.showOrHidePhotoImageView.setVisibility(View.GONE);
		}else if (Common.ppp.equals(arrayList.get(position).cart_productName)) {//ppp产品
			Log.d(TAG, "ppp product");
			imageLoader.displayImage(Common.BASE_URL+arrayList.get(position).cart_productImageUrl, viewHolder.goodImageView);
			viewHolder.goodPhotosGridLayout.setVisibility(View.GONE);
			viewHolder.goodDashedLineView.setVisibility(View.GONE);
			viewHolder.showOrHidePhotoImageView.setVisibility(View.GONE);
		}else {//为其他产品
			Log.d(TAG, "other procut");
			imageLoader.displayImage(Common.BASE_URL + arrayList.get(position).cart_productImageUrl, viewHolder.goodImageView);
			
			//设置gridlayout,先清除所有的view，再重新添加view
			viewHolder.goodPhotosGridLayout.setVisibility(View.VISIBLE);
			viewHolder.goodPhotosGridLayout.removeAllViews();
			
			for (int i = 0; i < gridviewlist.size(); i++) {
				ImageView imageView = new ImageView(context);
				GridLayout.LayoutParams params = new GridLayout.LayoutParams();
				params.width = (ScreenUtil.getScreenWidth(context) - ScreenUtil.dip2px(context, 25)) / 4;
				params.height = params.width;
				imageView.setLayoutParams(params);
				if (gridviewlist.size()==0||gridviewlist.get(i).cart_photoUrl.equals("")){
					imageView.setImageResource(R.drawable.empty);
				}else {
					imageLoader.displayImage(Common.PHOTO_URL+gridviewlist.get(i).cart_photoUrl, imageView);
				}
				imageView.setScaleType(ScaleType.CENTER_CROP);
				imageView.setId(position*10+i);//给添加的imageview添加id
				imageViews.add(imageView);
				//imageview设置监听
				imageView.setOnClickListener(new SubmitOrderOnClickListener(viewHolder,arrayList.get(position)));
				viewHolder.goodPhotosGridLayout.addView(imageView,params);
			}
			if (arrayList.get(position).showPhotos == 1) {
				viewHolder.goodPhotosGridLayout.setVisibility(View.VISIBLE);
				viewHolder.goodDashedLineView.setVisibility(View.VISIBLE);
				viewHolder.showOrHidePhotoImageView.setVisibility(View.VISIBLE);
				viewHolder.showOrHidePhotoImageView.setImageResource(R.drawable.good_hide_photo);
			}else {
				viewHolder.showOrHidePhotoImageView.setImageResource(R.drawable.good_show_photo);
				viewHolder.showOrHidePhotoImageView.setVisibility(View.VISIBLE);
				viewHolder.goodPhotosGridLayout.setVisibility(View.GONE);
				viewHolder.goodDashedLineView.setVisibility(View.GONE);
			}
		}
		gridLayoutLists.add(imageViews);
		
		viewHolder.goodQuentityTextView.setText("x"+arrayList.get(position).cart_quantity);
		viewHolder.currencyTextView.setText(currency);
		viewHolder.goodPriceTextView.setText((int)arrayList.get(position).cart_originalPrice+"");
		viewHolder.goodNameTextView.setText(arrayList.get(position).cart_productName);
		viewHolder.goodRelativeLayout.setOnClickListener(new SubmitOrderOnClickListener(viewHolder, arrayList.get(position)));
		return convertView;
	}
	
	private class ViewHolder{
		ImageView goodImageView;
		TextView goodNameTextView;
		TextView currencyTextView;
		TextView goodPriceTextView;
		TextView goodQuentityTextView;
		GridLayout goodPhotosGridLayout;
		RelativeLayout goodRelativeLayout;
		DashedLineView goodDashedLineView;
		ImageView showOrHidePhotoImageView;
	}
	
	private class SubmitOrderOnClickListener implements OnClickListener{
		private ViewHolder viewHolder;
		private CartItemInfo cartItemInfo;
		public SubmitOrderOnClickListener(ViewHolder viewHolder, CartItemInfo cartItemInfo) {
			this.cartItemInfo = cartItemInfo;
			this.viewHolder = viewHolder;
		}
		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.submitOrderRelativeLayout) {
				Log.d(TAG, "expand photos");
				if (cartItemInfo.cart_productType == 1) {
					if (cartItemInfo.showPhotos == 1) {
						viewHolder.goodPhotosGridLayout.setVisibility(View.GONE);
						viewHolder.goodDashedLineView.setVisibility(View.GONE);
						viewHolder.showOrHidePhotoImageView.setImageResource(R.drawable.good_show_photo);
						cartItemInfo.showPhotos = 0;
					} else {
						viewHolder.goodPhotosGridLayout.setVisibility(View.VISIBLE);
						viewHolder.goodDashedLineView.setVisibility(View.VISIBLE);
						viewHolder.showOrHidePhotoImageView.setImageResource(R.drawable.good_hide_photo);
						cartItemInfo.showPhotos = 1;
					}
					
				}
			}else {
				Log.d(TAG, "photo click "+v.getId()/10+"_"+v.getId()%10);
				Message message = handler.obtainMessage();
				message.what = 1;
				message.arg1 = v.getId();
				handler.sendMessage(message);
				
			}
		}
	}

}
