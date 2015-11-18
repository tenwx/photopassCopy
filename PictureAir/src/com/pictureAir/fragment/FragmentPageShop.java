package com.pictureAir.fragment;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.pictureAir.CartActivity;
import com.pictureAir.DetailProductActivity;
import com.pictureAir.MyApplication;
import com.pictureAir.PPPDetailProductActivity;
import com.pictureAir.R;
import com.pictureAir.adapter.ShopGoodListViewAdapter;
import com.pictureAir.entity.GoodsInfo;
import com.pictureAir.util.ACache;
import com.pictureAir.util.API;
import com.pictureAir.util.Common;
import com.pictureAir.util.UniversalImageLoadTool;
import com.pictureAir.widget.MyToast;
import com.pictureAir.widget.XListView;
import com.pictureAir.widget.XListView.IXListViewListener;

/**
 * shop类
 * 显示全部商品，调用全部商品API
 * @author bauer_bao
 * 
 */
public class FragmentPageShop extends Fragment implements OnClickListener{
	//申明控件
	private ImageView shoppingBag;
	private TextView cartCountTextView;
	private ListView xListView;
	
	//申明变量
	private static String TAG = "FragmentPage3";
	private int cartCount = 0; // 记录数据库中有几条记录
	private String storeIdString = "";
	private String currency = "";//货币种类
	
	//申明实例类
	private ArrayList<GoodsInfo> allGoodsList;//全部商品
	private GoodsInfo goodsInfo;
	private ShopGoodListViewAdapter shopGoodListViewAdapter;
	
	//申明其他
	private MyToast myToast;
	private SharedPreferences sharedPreferences;
	private ACache aCache;

	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case API.GET_ALL_GOODS_SUCCESS://成功获取商品
				Log.d(TAG, "get all goods success");
				allGoodsList.clear();
				JSONObject getallgoodsobj = (JSONObject) msg.obj;
				if (null == getallgoodsobj) {
					Log.d(TAG, "no any goods");
				} else {
					Log.d(TAG, "deal the all goods info");
					aCache.put(Common.ALL_GOODS, getallgoodsobj);
					try {
						String resultString = getallgoodsobj.getString("products").toString();
						JSONArray jsonArray = new JSONArray(resultString);
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject message = (JSONObject)jsonArray.get(i);//获取解析文字
							JSONArray urlsArray = new JSONArray(message.getString("imageURLS"));
							String url = "";
							for (int j = 0; j < urlsArray.length(); j++) {
								if (j==0) {
									url = urlsArray.get(j).toString();
								}else {
									url += ","+(String)urlsArray.get(j);
								}
							}
							goodsInfo = new GoodsInfo();
							goodsInfo.good_name = message.getString("name");
							goodsInfo.good_nameAlias = message.getString("nameAlias");
							goodsInfo.good_previewUrls = url;
							goodsInfo.good_price = message.getString("productPrice");
//							goodsInfo.good_detail = getString(R.string.goods_intro);
							goodsInfo.good_detail = message.getString("description");
							goodsInfo.good_productId = message.getString("productId");
							goodsInfo.good_promotionPrice = message.getString("promotionPrice");
							if (Common.PHOTOPASSPLUS.equals(goodsInfo.good_name)) {//区分商品
								//如果是PPP
								goodsInfo.good_embedPhotoCount = 0;
								goodsInfo.good_type = 3;
							}else {
								//其他商品
								goodsInfo.good_embedPhotoCount = 1;
								goodsInfo.good_type = 1;
							}
							allGoodsList.add(goodsInfo);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				shopGoodListViewAdapter.notifyDataSetChanged();
//				onLoad();
				break;
				
			case API.GET_ALL_GOODS_FAILED://获取商品失败
				myToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
//				onLoad();
				break;
				
			default:
				break;
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_shop, null);
		
		//找控件
		shoppingBag = (ImageView) view.findViewById(R.id.frag3_cart);
		cartCountTextView = (TextView) view.findViewById(R.id.textview_cart_count);
		xListView = (ListView) view.findViewById(R.id.shopListView);
		
		//申明类
		myToast = new MyToast(getActivity());
		aCache = ACache.get(getActivity());

		//初始化数据
		sharedPreferences = getActivity().getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
		cartCount = sharedPreferences.getInt(Common.CART_COUNT, 0);//获取购物车数量
		storeIdString = sharedPreferences.getString(Common.STORE_ID, "");//获取storeId
		currency = sharedPreferences.getString(Common.CURRENCY, Common.DEFAULT_CURRENCY);//获取币种
		//设置购物车数量
		if (cartCount<=0) {
			cartCountTextView.setVisibility(View.INVISIBLE);
		}else {
			cartCountTextView.setVisibility(View.VISIBLE);
			cartCountTextView.setText(cartCount + "");
		}
		allGoodsList = new ArrayList<GoodsInfo>();//初始化商品列表
		API.getAllGoods(mHandler, storeIdString, ((MyApplication)getActivity().getApplication()).getLanguageType());
		shopGoodListViewAdapter = new ShopGoodListViewAdapter(allGoodsList, getActivity(), currency);
		xListView.setAdapter(shopGoodListViewAdapter);
//		xListView.setPullLoadEnable(false);
//		xListView.setPullRefreshEnable(false);
//		xListView.setXListViewListener(this);

		//绑定监听
		shoppingBag.setOnClickListener(this);
		cartCountTextView.setOnClickListener(this);
		xListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
//				position--;
				Intent intent = null;
				if (Common.ppp.equals(allGoodsList.get(position).good_name)) {
					intent = new Intent(getActivity(), PPPDetailProductActivity.class);
					intent.putExtra("showComment", "Y");
				}else {
					intent = new Intent(getActivity(), DetailProductActivity.class);
					intent.putExtra("storeid", storeIdString);
					intent.putExtra("goods", allGoodsList.get(position));
				}
				FragmentPageShop.this.startActivity(intent);
			}
		});
		xListView.setOnScrollListener(new PauseOnScrollListener(UniversalImageLoadTool.getImageLoader(), true, true));
		return view;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.textview_cart_count:
		case R.id.frag3_cart:
			Intent intent = new Intent(getActivity(), CartActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		cartCount = sharedPreferences.getInt(Common.CART_COUNT, 0);
		if (cartCount<=0) {
			cartCountTextView.setVisibility(View.INVISIBLE);
		}else {
			cartCountTextView.setVisibility(View.VISIBLE);
			cartCountTextView.setText(cartCount + "");
		}
	}

//	//下拉刷新
//	@Override
//	public void onRefresh() {
//		Log.d(TAG, "start refresh------->");
//		API.getAllGoods(mHandler, storeIdString, ((MyApplication)getActivity().getApplication()).getLanguageType());
//	}
//
//	@Override
//	public void onLoadMore() {
//		
//	}
//	
//	//结束刷新
//	private void onLoad() {
//		xListView.stopRefresh();
//	}
}