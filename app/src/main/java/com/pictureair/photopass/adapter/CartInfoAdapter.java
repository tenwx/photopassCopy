package com.pictureair.photopass.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.entity.CartPhotosInfo;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.HttpUtil;
import com.pictureair.photopass.util.JsonUtil;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.DashedLineView;
import com.pictureair.photopass.widget.ListViewImageView;
import com.pictureair.photopass.widget.MyToast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * 购物车页面的ExpandableListView的适配器
 * @author bauer_bao
 *
 */
public class CartInfoAdapter extends BaseAdapter{
	private List<CartPhotosInfo> gridviewlist;
	private LayoutInflater layoutInflater;
	private ArrayList<ImageView> gridlayoutList;
	private ArrayList<CartItemInfo> goodArrayList;
	private Context context;
	private Handler handler;
	private String currency;
	private String userId;
	private ImageLoader imageLoader;
	private ArrayList<ArrayList<ImageView>> gridLayoutLists;
	private MyToast myToast;
	
	public static final int MINUSCOUNT = 0;// 减少数量
	public static final int ADDCOUNT = 1;// 增加数量
	public static final int SELECTED = 3;// 选中item
	public static final int NOSELECTED = 4;// 取消选中item
	public static final int CHANGE_PHOTO = 5;//更改照片
	private static final String TAG = "CartInfoAdapter";
	
	
	public CartInfoAdapter(Context context, String currency, ArrayList<CartItemInfo> goodArrayList, String userId, Handler handler) {
		this.context = context;
		this.goodArrayList = goodArrayList; 
		this.handler = handler;
		this.currency = currency;
		this.userId = userId;
		layoutInflater = LayoutInflater.from(context);
		imageLoader = ImageLoader.getInstance();
		gridLayoutLists = new ArrayList<ArrayList<ImageView>>();
		myToast = new MyToast(context);
	}
	
	@Override
	public int getCount() {
		return goodArrayList.size();
	}

	@Override
	public Object getItem(int position) {
		return goodArrayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

//	public ArrayList<CartItemInfo> getGoodArrayList() {
//		return goodArrayList;
//	}

//	public void setGoodArrayList(ArrayList<CartItemInfo> goodArrayList) {
//		this.goodArrayList = goodArrayList;
//	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = layoutInflater.inflate(R.layout.cart_listview_item, null);
			viewHolder.selectedImageView = (ImageView) convertView.findViewById(R.id.cartSelectImageView);
			viewHolder.cartGoodImageView = (ImageView) convertView.findViewById(R.id.cartProductImageView);
			viewHolder.cartGoodNameTextView = (TextView) convertView.findViewById(R.id.cartProductName);
			viewHolder.cartCurrencyTextView = (TextView) convertView.findViewById(R.id.cartCurrency);
			viewHolder.cartGoodPriceTextView = (TextView) convertView.findViewById(R.id.cartPrice);
			viewHolder.cartGoodProductQuentityTextView = (TextView) convertView.findViewById(R.id.cartCount);
			viewHolder.cartReduceImageView = (ListViewImageView) convertView.findViewById(R.id.cartRemoveImageView);
			viewHolder.cartAddImageView = (ListViewImageView) convertView.findViewById(R.id.cartAddImageView);
			viewHolder.cartGoodCountTextView = (TextView) convertView.findViewById(R.id.cartProductCountTextView);
			viewHolder.cartGoodPhotosGridLayout = (GridLayout) convertView.findViewById(R.id.cartPhotoGridLayout);
			viewHolder.editBarLayout = (LinearLayout) convertView.findViewById(R.id.cartEditBar);
			viewHolder.cartLineImageView = (DashedLineView) convertView.findViewById(R.id.cartLine1);
			viewHolder.hideImageView = (ImageView) convertView.findViewById(R.id.hideView);
			convertView.setTag(viewHolder);
		}else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		//初始化数据
		if (goodArrayList.get(position).isSelect) {
			viewHolder.selectedImageView.setImageResource(R.drawable.cart_select);
		}else {
			viewHolder.selectedImageView.setImageResource(R.drawable.cart_not_select);
		}
		gridviewlist = goodArrayList.get(position).cart_photoUrls;
		gridlayoutList = new ArrayList<ImageView>();
		//设置商品图片
		if (Common.GOOD_NAME_SINGLE_DIGITAL.equals(goodArrayList.get(position).cart_productName)) {//照片商品
			Log.d(TAG, "picture product");
			String url = null;
			if (goodArrayList.get(position).cart_productImageUrl.contains("http")) {
				url = goodArrayList.get(position).cart_productImageUrl;
			}else {
				url = Common.PHOTO_URL + goodArrayList.get(position).cart_productImageUrl;
			}
			/*****temp code*****/
			if (url.contains("productImage/gift-singleDigital.jpg")) {
				System.out.println(url);
				url = url.replace("4000", "3001");
				System.out.println(url);
			}
			/*****temp code*****/
			imageLoader.displayImage(url, viewHolder.cartGoodImageView);
			viewHolder.cartGoodPhotosGridLayout.setVisibility(View.GONE);
			viewHolder.cartLineImageView.setVisibility(View.GONE);
			viewHolder.hideImageView.setVisibility(View.GONE);
			
		}else if (Common.ppp.equals(goodArrayList.get(position).cart_productName)) {//ppp商品
			Log.d(TAG, "photopassplus product");
			imageLoader.displayImage(Common.BASE_URL+goodArrayList.get(position).cart_productImageUrl, viewHolder.cartGoodImageView);
			viewHolder.cartGoodPhotosGridLayout.setVisibility(View.GONE);
			viewHolder.cartLineImageView.setVisibility(View.GONE);
			viewHolder.hideImageView.setVisibility(View.GONE);
		}else {//其他商品
			Log.d(TAG, "other product");
			imageLoader.displayImage(Common.BASE_URL+goodArrayList.get(position).cart_productImageUrl, viewHolder.cartGoodImageView);
			viewHolder.cartLineImageView.setVisibility(View.VISIBLE);
			viewHolder.cartGoodPhotosGridLayout.setVisibility(View.VISIBLE);
			viewHolder.hideImageView.setVisibility(View.INVISIBLE);
			viewHolder.cartGoodPhotosGridLayout.removeAllViews();
			if (0==gridviewlist.size()) {//如果照片数量为0
				Log.d(TAG, "gridview list is 0");
				ImageView imageView = new ImageView(context);
				GridLayout.LayoutParams params = new GridLayout.LayoutParams();
				params.width = (ScreenUtil.getScreenWidth(context) - ScreenUtil.dip2px(context, 25)) / 4;
				params.height = params.width;
				imageView.setLayoutParams(params);
				imageView.setImageResource(R.drawable.empty);
				imageView.setScaleType(ScaleType.CENTER_CROP);
				imageView.setId(position*10);//给添加的imageview添加id
				gridlayoutList.add(imageView);
				//imageview设置监听
				imageView.setOnClickListener(new PhotoOnClickListener());
				viewHolder.cartGoodPhotosGridLayout.addView(imageView,params);
				TextView textView = new TextView(context);
				GridLayout.LayoutParams params2 = new GridLayout.LayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				textView.setLayoutParams(params2);
				textView.setText(R.string.add_photo);
				textView.setTextColor(Color.WHITE);
				textView.setBackgroundColor(context.getResources().getColor(R.color.orange));
				viewHolder.cartGoodPhotosGridLayout.addView(textView,params2);
				
			}else {//有照片数量
				Log.d(TAG, "gridView is not null");
				for (int i = 0; i < gridviewlist.size(); i++) {
					ImageView imageView = new ImageView(context);
					GridLayout.LayoutParams params = new GridLayout.LayoutParams();
					params.width =  (ScreenUtil.getScreenWidth(context) - ScreenUtil.dip2px(context, 25)) / 4;
					params.height = params.width;
					imageView.setLayoutParams(params);
					Log.d(TAG, gridviewlist.get(i).cart_photoUrl);
					imageView.setScaleType(ScaleType.CENTER_CROP);
					imageView.setId(position*10+i);//给添加的imageview添加id
					gridlayoutList.add(imageView);
					//imageview设置监听
					imageView.setOnClickListener(new PhotoOnClickListener());
					viewHolder.cartGoodPhotosGridLayout.addView(imageView,params);
					if (gridviewlist.get(i).cart_photoUrl.equals("")||gridviewlist.get(i).cart_photoUrl.equals("null")) {
						imageView.setImageResource(R.drawable.empty);
						
						TextView textView = new TextView(context);
						GridLayout.LayoutParams params2 = new GridLayout.LayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
						textView.setLayoutParams(params2);
						textView.setText(R.string.add_photo);
						textView.setTextColor(Color.WHITE);
						textView.setBackgroundColor(context.getResources().getColor(R.color.orange));
						viewHolder.cartGoodPhotosGridLayout.addView(textView,params2);
					}else {
						System.out.println(Common.PHOTO_URL+gridviewlist.get(i).cart_photoUrl.trim());//"http://192.168.8.133:3001/"
						imageLoader.displayImage(Common.PHOTO_URL+gridviewlist.get(i).cart_photoUrl.trim(), imageView);
					}
				}
			}
			
		}
		gridLayoutLists.add(gridlayoutList);
		viewHolder.cartGoodCountTextView.setText(goodArrayList.get(position).cart_quantity+"");
		viewHolder.cartCurrencyTextView.setText(currency);
		viewHolder.cartGoodProductQuentityTextView.setText("x" + goodArrayList.get(position).cart_quantity+"");
		viewHolder.cartGoodPriceTextView.setText((int)goodArrayList.get(position).cart_originalPrice+"");
		viewHolder.cartGoodNameTextView.setText(goodArrayList.get(position).cart_productName);
		viewHolder.cartAddImageView.setOnClickListener(new ChangeCountOnclick(viewHolder, gridviewlist, goodArrayList.get(position), position));
		viewHolder.cartReduceImageView.setOnClickListener(new ChangeCountOnclick(viewHolder, gridviewlist, goodArrayList.get(position), position));
		if (goodArrayList.get(position).show_edit == 1) {
			viewHolder.editBarLayout.setVisibility(View.VISIBLE);
			viewHolder.cartGoodNameTextView.setVisibility(View.GONE);
		}else {
			viewHolder.editBarLayout.setVisibility(View.GONE);
			viewHolder.cartGoodNameTextView.setVisibility(View.VISIBLE);
		}
		viewHolder.selectedImageView.setOnClickListener(new SelectOnClick(viewHolder, goodArrayList.get(position)));
		return convertView;
	}
	
	private class ViewHolder{
		ImageView selectedImageView;//商品选择按钮
		ImageView cartGoodImageView;//商品预览图
		TextView cartGoodNameTextView;//商品名称
		TextView cartCurrencyTextView;//币种
		TextView cartGoodPriceTextView;//商品单价
		TextView cartGoodProductQuentityTextView;//商品数量
		ListViewImageView cartReduceImageView;//减少数量
		ListViewImageView cartAddImageView;//增加数量
		TextView cartGoodCountTextView;//商品数量（编辑数量的时候的数量）
		GridLayout cartGoodPhotosGridLayout;//商品携带图片的控件
		LinearLayout editBarLayout;//编辑数量
		DashedLineView cartLineImageView;//线
		ImageView hideImageView;//占位置的一个View
	}
	
	private class PhotoOnClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			Log.d(TAG, "photo click "+v.getId()/10+"_"+v.getId()%10);
			Message message = handler.obtainMessage();
			message.what = CHANGE_PHOTO;
			message.arg1 = v.getId();
			handler.sendMessage(message);
		}
	}
	
	//选择按键监听， 取消选中的时候，需要将对应列的价格减去，同时更新总的订单信息
	private class SelectOnClick implements OnClickListener{
		private CartItemInfo cartItemInfo;
		private ViewHolder viewHolder;

		public SelectOnClick(ViewHolder holderView, CartItemInfo cartItemInfo) {
			this.viewHolder = holderView;
			this.cartItemInfo = cartItemInfo;
		}

		@Override
		public void onClick(View v) {
			Log.d(TAG, "set isSelected");
			Message message = handler.obtainMessage();
			if (cartItemInfo.isSelect) {//取消选中
				viewHolder.selectedImageView.setImageResource(R.drawable.cart_not_select);
				cartItemInfo.isSelect = false;
				message.what = NOSELECTED;
			} else {
				viewHolder.selectedImageView.setImageResource(R.drawable.cart_select);
				cartItemInfo.isSelect = true;
				message.what = SELECTED;
			}
			message.obj = cartItemInfo.cart_originalPrice * cartItemInfo.cart_quantity;
			handler.sendMessage(message);
		}
	}
	
	//修改购物车数量的监听
	private class ChangeCountOnclick implements OnClickListener{
		private ViewHolder holderView;
		private List<CartPhotosInfo> arraylist;
		private boolean ishandle = false;
		private CartItemInfo cartItemInfo;
		private int position;
		public ChangeCountOnclick(ViewHolder holderView, List<CartPhotosInfo> arraylist, CartItemInfo cartItemInfo, int position) {
			// TODO Auto-generated constructor stub
			this.holderView = holderView;
			this.arraylist = arraylist;
			this.cartItemInfo = cartItemInfo;
			this.position = position;
		}
		@Override
		public void onClick(View v) {
			if (!ishandle) {//如果已经在处理中，则忽略响应，反之，进行处理
				System.out.println("start change count");
				if (cartItemInfo.cart_productType == 2) {//如果是pp不允许添加或者减少数量
					myToast.setTextAndShow(R.string.cannot_change_count, Common.TOAST_SHORT_TIME);
					return;
				}
				ishandle = true;
				int count = Integer.parseInt(holderView.cartGoodCountTextView.getText().toString());
				boolean addcount = false;//true 代表添加操作，false代表减少操作
				if (v.getId() == holderView.cartAddImageView.getId()) {//添加按钮
					System.out.println("add item count");
					count++;
					addcount = true;
				}else if (v.getId() == holderView.cartReduceImageView.getId()) {//减少按钮
					System.out.println("remove");
					if (count > 1) {// 判断数量是否小于1件，如果小于1，则不让更改
						count--;
						addcount = false;
					} else {
						System.out.println("not ok");
						myToast.setTextAndShow(R.string.cannot_reduce, Common.TOAST_SHORT_TIME);
						ishandle = false;
						return;
					}
				}
				//创建jsonobject对象
				com.alibaba.fastjson.JSONObject cartItem = JsonUtil.CreateModifyCartItemJsonObject(null, cartItemInfo, count);
				System.out.println(cartItem.toString());
				final boolean addOrminus = addcount;
				final int cart_item_count = count;
				RequestParams params = new RequestParams();
				params.put(Common.USER_ID, userId);
				params.put(Common.ITEM, cartItem);
				HttpUtil.post(Common.BASE_URL+Common.MODIFY_CART, params, new JsonHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
						// TODO Auto-generated method stub
						super.onSuccess(statusCode, headers, response);
						System.out.println("modify cart=="+response);
						ishandle = false;
						if (response.has("message")) {//添加失败

						}else {//添加成功

							holderView.cartGoodCountTextView.setText(String.valueOf(cart_item_count));
							holderView.cartGoodProductQuentityTextView.setText("x" + String.valueOf(cart_item_count));
							for (int i = 0; i < arraylist.size(); i++) {
								CartPhotosInfo map = arraylist.get(i);
								map.cart_photoCount = String.valueOf(cart_item_count);
								arraylist.set(i, map);
							}
							cartItemInfo.cart_photoUrls = arraylist;
							cartItemInfo.cart_quantity = cart_item_count;
							Message message = handler.obtainMessage();
							if (addOrminus) {
								message.what = ADDCOUNT;
							}else {
								message.what = MINUSCOUNT;
							}
							if (cartItemInfo.isSelect) {
								message.arg1 = 1;
							}else {
								message.arg1 = 0;
							}
							message.arg2 = position;
							System.out.println("传出来的价格"+cartItemInfo.cart_originalPrice);
							message.obj = cartItemInfo.cart_originalPrice;
							handler.sendMessage(message);
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
						// TODO Auto-generated method stub
						super.onFailure(statusCode, headers, throwable, errorResponse);
						ishandle = false;
					}
				});
			}else {
				System.out.println("is still change count");
			}
		}
	}

}
