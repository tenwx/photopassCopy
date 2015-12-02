package com.pictureAir;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pictureAir.adapter.CartInfoAdapter;
import com.pictureAir.entity.CartItemInfo;
import com.pictureAir.entity.CartPhotosInfo;
import com.pictureAir.entity.PhotoInfo;
import com.pictureAir.util.API;
import com.pictureAir.util.AppManager;
import com.pictureAir.util.Common;
import com.pictureAir.util.HttpUtil;
import com.pictureAir.util.JsonUtil;
import com.pictureAir.widget.CustomProgressBarPop;
import com.pictureAir.widget.CustomProgressDialog;
import com.pictureAir.widget.MyToast;
import com.pictureAir.widget.NoNetWorkOrNoCountView;
import com.pictureAir.widget.XListViewHeader;
import com.umeng.analytics.MobclickAgent;

/**
 * 购物车页面
 * @author bauer_bao
 * 
 */
public class CartActivity extends BaseActivity implements OnClickListener {
	private ListView listView;
	private ImageView rtButton;
	private Button paymentButton;
	private TextView totalTextView, currencyTextView;
	private ImageView cartSelectAllImageView;
	private TextView editTextView;
	private LinearLayout cartPriceLinearLayout;
	private RelativeLayout bottomRelativeLayout;
	private View line;

	private ArrayList<CartItemInfo> cartInfoList;// 订单list
	private CartItemInfo cartItemInfo;

	private ArrayList<PhotoInfo> updatephotolist;

	private float totalprice = 0;

	private boolean isEdit = false;
	private boolean isDelete = false;
	private int disSelectedCount = 0;//记录已经取消选中的个数

	private CartInfoAdapter cartAdapter;

	private SharedPreferences sPreferences;

	private CustomProgressBarPop dialog;
	private CustomProgressDialog customProgressDialog;
	
	private String userId = "";

	private MyToast newToast;
	
	private NoNetWorkOrNoCountView netWorkOrNoCountView;

	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case API.GET_CART_SUCCESS:
				try {
					System.out.println(msg.obj);
					JSONObject object = (JSONObject) msg.obj;
					String favorablepriceString = null;//优惠价格
					int totalcount = 0;//总数
					double totalprice = 0;//总价
					JSONObject shopcart;
					JSONArray shopitemArray = new JSONArray();
					if ("null".equals(object.getString("shopCart"))) {
						return;
					}
					shopcart = object.getJSONObject("shopCart");

					favorablepriceString = shopcart.getString("preferentialPrice");//优惠价格
					totalcount = shopcart.getInt("totalCount");//总数
					totalprice = shopcart.getDouble("totalPrice");//总价
					shopitemArray = shopcart.getJSONArray("items");
					System.out.println(favorablepriceString+"_"+totalcount+"_"+totalprice);
					JSONObject itemObject;//cart中的每个item
					int cartcount = 0;
					for (int i = 0; i < shopitemArray.length(); i++) {
						itemObject = (JSONObject) shopitemArray.get(i);
						cartItemInfo = new CartItemInfo();
						cartItemInfo = JsonUtil.getCartItemInfo(itemObject);
						cartcount += cartItemInfo.cart_quantity;
						cartInfoList.add(cartItemInfo);
					}
					totalTextView.setText((int)totalprice + "");
					paymentButton.setVisibility(View.VISIBLE);
					paymentButton.setText(String.format(getString(R.string.go_pay), cartInfoList.size()));
					//更新购物车数量
					Editor editor = sPreferences.edit();
					editor.putInt(Common.CART_COUNT, cartcount);
					editor.commit();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				editTextView.setEnabled(true);
				customProgressDialog.dismiss();
				if (cartInfoList.size() == 0) {//没有数量
					netWorkOrNoCountView.setVisibility(View.VISIBLE);
					netWorkOrNoCountView.setResult(R.string.no_cart, R.string.want_to_buy, R.string.to_add_good, R.drawable.no_cart, handler, false);
					bottomRelativeLayout.setVisibility(View.INVISIBLE);
					listView.setVisibility(View.INVISIBLE);
					line.setVisibility(View.INVISIBLE);
					editTextView.setVisibility(View.INVISIBLE);
				}else {//有数量
					netWorkOrNoCountView.setVisibility(View.GONE);
					bottomRelativeLayout.setVisibility(View.VISIBLE);
					listView.setVisibility(View.VISIBLE);
					line.setVisibility(View.VISIBLE);
				}
				break;
				
			case API.GET_CART_FAILED://请求失败
				customProgressDialog.dismiss();
				netWorkOrNoCountView.setVisibility(View.VISIBLE);
				netWorkOrNoCountView.setResult(R.string.no_network, R.string.click_button_reload, R.string.reload, R.drawable.no_network, handler, true);
				bottomRelativeLayout.setVisibility(View.INVISIBLE);
				listView.setVisibility(View.INVISIBLE);
				line.setVisibility(View.INVISIBLE);
				editTextView.setVisibility(View.INVISIBLE);
				break;
				
			case API.DELETE_CART_FAILED:
			case API.UPLOAD_PHOTO_FAILED:
				isDelete = false;
				newToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
				editTextView.setEnabled(true);
				break;

			case API.DELETE_CART_SUCCESS://删除购物车item
				isDelete = false;
				JSONObject resultjsonobject = (JSONObject) msg.obj;
				JSONObject statusJsonObject;
				try {
					statusJsonObject = resultjsonobject.getJSONObject("result");
					if ("success".equals(statusJsonObject.getString("status"))) {
						System.out.println(cartInfoList.get(msg.arg1).cart_id);
						System.out.println("after delete "+ cartInfoList.get(msg.arg1).cart_quantity);
						totalprice = 0;
						int count = 0;
						Iterator<CartItemInfo> iterator = cartInfoList.iterator();
						while (iterator.hasNext()) {
							CartItemInfo cartItemInfo = iterator.next();
							if (cartItemInfo.isSelect) {
								iterator.remove();
							}else {
								count += cartItemInfo.cart_quantity;
							}
						}
						totalTextView.setText((int)totalprice + "");
						cartAdapter.notifyDataSetChanged();
						Editor editor = sPreferences.edit();
						editor.putInt(Common.CART_COUNT, count);
						editor.commit();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (cartInfoList.size() == 0) {
					paymentButton.setBackgroundResource(R.color.gray_light3);
					cancelEdit();
					ShowNoNetOrNoCountView();
				}
				break;
				
			case API.UPLOAD_PHOTO_SUCCESS:
				System.out.println("upload photo success ======>"+msg.obj);
				JSONObject result = (JSONObject) msg.obj;
				String photoUrlString = null;
				String photoIdString = null;
				try {
					photoUrlString = result.getString("photoUrl");
					photoIdString = result.getString("photoId");
					System.out.println(photoUrlString+"_"+photoIdString);

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				PhotoInfo info = updatephotolist.get(0);
				info.photoId = photoIdString;
				info.photoPathOrURL = photoUrlString;
				System.out.println(photoIdString+"{{{{"+photoUrlString);
				updatephotolist.set(0, info);
				//创建jsonobject对象
				final int position = msg.arg1;
				JSONObject cartItem = JsonUtil.CreateModifyCartItemJsonObject(updatephotolist, cartInfoList.get(position/10), cartInfoList.get(position/10).cart_quantity);
				System.out.println(cartItem.toString());
				RequestParams params = new RequestParams();
				params.put(Common.USER_ID, userId);
				params.put(Common.ITEM, cartItem);
				HttpUtil.post(Common.BASE_URL+Common.MODIFY_CART, params, new JsonHttpResponseHandler() {
					@Override
					public void onStart() {
						System.out.println("start chang count------->");
					};
					@Override
					public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
						// TODO Auto-generated method stub
						super.onSuccess(statusCode, headers, response);
						System.out.println("modify cart with change photot =="+response);

						if (response.has("message")) {//添加失败
							newToast.setTextAndShow(R.string.uploadphotofailed, Common.TOAST_SHORT_TIME);
						}else {//添加成功
							changephoto(position, updatephotolist);
						}
						if (dialog.isShowing()) {
							dialog.dismiss();
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
						// TODO Auto-generated method stub
						super.onFailure(statusCode, headers, throwable, errorResponse);
						newToast.setTextAndShow(R.string.uploadphotofailed, Common.TOAST_SHORT_TIME);
						if (dialog.isShowing()) {
							dialog.dismiss();
						}
					}
					
					@Override
					public void onProgress(int bytesWritten, int totalSize) {
						super.onProgress(bytesWritten, totalSize);
						dialog.setProgress(bytesWritten, totalSize);
					};
				});
				break;
				
			case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_RELOAD://noView的按钮响应重新加载点击事件
				//重新加载购物车数据
				System.out.println("onclick with reload");
				customProgressDialog = CustomProgressDialog.show(CartActivity.this, getString(R.string.is_loading), false, null);
				API.getcart(CartActivity.this,Common.BASE_URL+Common.GET_CART, userId, handler);
				cartInfoList.clear();
				break;
				
			case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_NO_RELOAD://noView的按钮响应非重新加载的点击事件
				//去跳转到购物车
				System.out.println("onclick with no reload");
				//需要删除页面，保证只剩下mainTab页面，
				AppManager.getInstance().killOtherActivity(MainTabActivity.class);
				//同时将mainTab切换到shop Tab
				MainTabActivity.changeToShopTab = true;
				
				break;
				
			default:
				break;
			}
		}
	};

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CartInfoAdapter.MINUSCOUNT:// 减数量
				if (msg.arg1 == 1) {
					totalprice = Float.parseFloat(totalTextView.getText().toString());
					totalprice -= Float.parseFloat(msg.obj.toString());
					totalTextView.setText((int)totalprice + "");
				}
				cartInfoList.get(msg.arg2).cart_promotionPrice -= Float.parseFloat(msg.obj.toString());
				Editor editor = sPreferences.edit();
				editor.putInt(Common.CART_COUNT, sPreferences.getInt(Common.CART_COUNT, 0)-1);
				editor.commit();
				break;

			case CartInfoAdapter.ADDCOUNT:// 加数量
				if (msg.arg1 == 1) {
					totalprice = Float.parseFloat(totalTextView.getText().toString());
					totalprice += Float.parseFloat(msg.obj.toString());
					totalTextView.setText((int)totalprice + "");
				}
				cartInfoList.get(msg.arg2).cart_promotionPrice += Float.parseFloat(msg.obj.toString());
				Editor editor1 = sPreferences.edit();
				editor1.putInt(Common.CART_COUNT, sPreferences.getInt(Common.CART_COUNT, 0)+1);
				editor1.commit();
				break;

			case CartInfoAdapter.SELECTED:// 选中item
				System.out.println("selected");
				totalprice = Float.parseFloat(totalTextView.getText().toString());
				totalprice += Float.parseFloat(msg.obj.toString());
				totalTextView.setText((int)totalprice + "");
				disSelectedCount--;
				if (disSelectedCount == 0) {
					cartSelectAllImageView.setImageResource(R.drawable.cart_select);
					if (isEdit) {
						paymentButton.setBackgroundResource(R.color.orange);
						
					}else {//购买状态
						if (cartInfoList.size() == disSelectedCount) {//没有选中任何
							paymentButton.setBackgroundResource(R.color.gray_light3);
							
						}else {//选中
							paymentButton.setBackgroundResource(R.color.blue);
							
						}
					}
				}else {
					cartSelectAllImageView.setImageResource(R.drawable.cart_not_select);
					if (!isEdit) {
						
					paymentButton.setBackgroundResource(R.color.blue);
					}else {
						
						paymentButton.setBackgroundResource(R.color.orange);
					}
				}
				
				
				if (!isEdit) {

					paymentButton.setText(String.format(getString(R.string.go_pay), cartInfoList.size() - disSelectedCount));
				}
				break;
			case CartInfoAdapter.NOSELECTED:// 取消选中item
				System.out.println("noselected");
				totalprice = Float.parseFloat(totalTextView.getText().toString());
				totalprice -= Float.parseFloat(msg.obj.toString());
				totalTextView.setText((int)totalprice + "");
				disSelectedCount++;
				if (disSelectedCount == 0) {
					cartSelectAllImageView.setImageResource(R.drawable.cart_select);
					if (isEdit) {
						
						paymentButton.setBackgroundResource(R.color.orange);
					}else {
						paymentButton.setBackgroundResource(R.color.gray_light3);
						
					}
				}else {
					cartSelectAllImageView.setImageResource(R.drawable.cart_not_select);
				}
				if (disSelectedCount == cartInfoList.size()) {
					paymentButton.setBackgroundResource(R.color.gray_light3);
				}
				if (!isEdit) {
					paymentButton.setText(String.format(getString(R.string.go_pay), cartInfoList.size() - disSelectedCount));

				}
				break;

			case CartInfoAdapter.CHANGE_PHOTO://更换照片
				selectPhoto(msg.arg1);
				break;

			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cart);
		AppManager.getInstance().addActivity(this);
		newToast = new MyToast(this);
		//上传进度条
		dialog = new CustomProgressBarPop(this, findViewById(R.id.cart_activity_relativeLayout), CustomProgressBarPop.TYPE_UPLOAD);
		rtButton = (ImageView) findViewById(R.id.ret_relyt);
		rtButton.setOnClickListener(this);
		paymentButton = (Button) findViewById(R.id.button3_pm);
		paymentButton.setOnClickListener(this);
		totalTextView = (TextView) findViewById(R.id.textView3);
		currencyTextView = (TextView)findViewById(R.id.textView_currency);
		cartSelectAllImageView = (ImageView) findViewById(R.id.cartSelectAllImageView);
		cartSelectAllImageView.setOnClickListener(this);
		editTextView = (TextView) findViewById(R.id.cart_edit);
		editTextView.setOnClickListener(this);
		editTextView.setEnabled(false);
		cartPriceLinearLayout = (LinearLayout) findViewById(R.id.cartPriceLinearLayout);
		cartInfoList = new ArrayList<CartItemInfo>();
		line = (View) findViewById(R.id.line);
		bottomRelativeLayout = (RelativeLayout)findViewById(R.id.linearLayout1);
		netWorkOrNoCountView = (NoNetWorkOrNoCountView) findViewById(R.id.noNetWorkView);

		sPreferences = getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
		userId = sPreferences.getString(Common.USERINFO_ID, "");
		currencyTextView.setText(sPreferences.getString(Common.CURRENCY, Common.DEFAULT_CURRENCY));
		customProgressDialog = CustomProgressDialog.show(this, getString(R.string.is_loading), false, null);
		API.getcart(this,Common.BASE_URL+Common.GET_CART, userId, handler);
		totalTextView.setText((int)totalprice + "");
		listView = (ListView) findViewById(R.id.cartListView);
		cartAdapter = new CartInfoAdapter(this, sPreferences.getString(Common.CURRENCY, Common.DEFAULT_CURRENCY), cartInfoList, userId, mHandler);
		listView.addHeaderView(new XListViewHeader(this));
		listView.setAdapter(cartAdapter);
		listView.setHeaderDividersEnabled(true);
		listView.setFooterDividersEnabled(false);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent;
		switch (v.getId()) {
		case R.id.ret_relyt:
			finish();
			break;
		case R.id.button3_pm://支付提交按钮
			if (isEdit) {//需要删除操作
				if (cartInfoList.size() == 0) {
					newToast.setTextAndShow(R.string.no_cart, Common.TOAST_SHORT_TIME);
					return;
				}
				if (isDelete) {//说明正在删除中，直接返回
					return;
				}else {
					isDelete = true;
				}
				System.out.println("delete cart");
				JSONArray cartitemidsArray = new JSONArray();
				for (int i = 0; i < cartInfoList.size(); i++) {
					if (cartInfoList.get(i).isSelect) {
						cartitemidsArray.put(cartInfoList.get(i).cart_id);
					}
				}
				if (cartitemidsArray.length() == 0) {
					newToast.setTextAndShow(R.string.select_cart, Common.TOAST_SHORT_TIME);
					isDelete = false;
					return;
				}
				API.deletecart(CartActivity.this, Common.BASE_URL+Common.REMOVE_CART, userId, cartitemidsArray, handler);

			}else {//支付操作
				ArrayList<CartItemInfo> orderinfo = new ArrayList<CartItemInfo>();
				for (int i = 0; i < cartInfoList.size(); i++) {//查找键值对中select为true的值，并将它放入orderinfo2中
					if (cartInfoList.get(i).isSelect) {
						orderinfo.add(cartInfoList.get(i));
					}
				}
				System.out.println("order info count = "+orderinfo.size());
				if (0==cartInfoList.size()) {
					//				Toast.makeText(CartActivity.this, R.string.selectyourcart, Common.TOAST_SHORT_TIME).show();
					System.out.println("cartinfolist = 0");
					newToast.setTextAndShow( R.string.selectyourcart, Common.TOAST_SHORT_TIME);
				}else if (0==orderinfo.size()) {
					System.out.println("orderinfo = 0");
					//				Toast.makeText(CartActivity.this, R.string.selectyourcart, Common.TOAST_SHORT_TIME).show();
					newToast.setTextAndShow( R.string.selectyourcart, Common.TOAST_SHORT_TIME);
				}else {
					/**********判断是否有图片没有添加*********/
					for (int i = 0; i < orderinfo.size(); i++) {
						if (!orderinfo.get(i).hasPhoto) {
							//						Toast.makeText(CartActivity.this, R.string.selectyourcart, Common.TOAST_SHORT_TIME).show();
							System.out.println("have no photo");
							newToast.setTextAndShow( R.string.addphoto, Common.TOAST_SHORT_TIME);
							return;
						}
					}
					intent = new Intent(this, SubmitOrderActivity.class);
					intent.putParcelableArrayListExtra("orderinfo", orderinfo);
					startActivity(intent);
					finish();
				}
			}
			break;
			
		case R.id.cart_edit:
			for (int i = 0; i < cartInfoList.size(); i++) {
				cartInfoList.get(i).show_edit = isEdit ? 0 : 1;
			}
			if (isEdit) {//取消编辑状态
				cancelEdit();
			}else {//开始编辑
				isEdit = true;
				editTextView.setText(R.string.ok);
				cartPriceLinearLayout.setVisibility(View.GONE);
				if (cartInfoList.size() == 0 || disSelectedCount == cartInfoList.size()) {
					paymentButton.setBackgroundResource(R.color.gray_light3);
				}else {
					paymentButton.setBackgroundResource(R.color.orange);
				}
				paymentButton.setText(R.string.delete);
				rtButton.setVisibility(View.GONE);
				
			}
			cartAdapter.notifyDataSetChanged();
			break;

		case R.id.cartSelectAllImageView:
			totalprice = 0;
			for (int i = 0; i < cartInfoList.size(); i++) {
				cartInfoList.get(i).isSelect = (disSelectedCount == 0) ? false : true;
				totalprice += cartInfoList.get(i).cart_promotionPrice;
			}
			cartAdapter.notifyDataSetChanged();
			if (disSelectedCount == 0) {
				disSelectedCount = cartInfoList.size();
				cartSelectAllImageView.setImageResource(R.drawable.cart_not_select);
				totalprice = 0;
				paymentButton.setBackgroundResource(R.color.gray_light3);
			}else {
				cartSelectAllImageView.setImageResource(R.drawable.cart_select);
				if (isEdit) {
					
					paymentButton.setBackgroundResource(R.color.orange);
				}else {
					paymentButton.setBackgroundResource(R.color.blue);
					
				}
				disSelectedCount = 0;
			}
			totalTextView.setText((int)totalprice + "");
			if (!isEdit) {

				paymentButton.setText(String.format(getString(R.string.go_pay), cartInfoList.size() - disSelectedCount));
			}
			break;

		default:
			break;
		}
	}
	
	/**
	 * 取消编辑
	 */
	private void cancelEdit() {
		// TODO Auto-generated method stub
		isEdit = false;
		editTextView.setText(R.string.edit);
		cartPriceLinearLayout.setVisibility(View.VISIBLE);
		paymentButton.setText(String.format(getString(R.string.go_pay), cartInfoList.size() - disSelectedCount));
		if (cartInfoList.size() == disSelectedCount) {//没有选中
			
			paymentButton.setBackgroundResource(R.color.gray_light3);
		}else {
			
			paymentButton.setBackgroundResource(R.color.blue);
		}
		rtButton.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 展示无数据的页面
	 */
	private void ShowNoNetOrNoCountView() {
		bottomRelativeLayout.setVisibility(View.INVISIBLE);
		netWorkOrNoCountView.setVisibility(View.VISIBLE);
		netWorkOrNoCountView.setResult(R.string.no_cart, R.string.want_to_buy, R.string.to_add_good, R.drawable.no_cart, handler, false);
		editTextView.setVisibility(View.INVISIBLE);
	}

	//选择照片
	private void selectPhoto(int requestCode) {
		Intent intent = new Intent(CartActivity.this,SelectPhotoActivity.class);
		intent.putExtra("activity", "cartactivity");
		startActivityForResult(intent, requestCode);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 20) {//先要上传图片，上传完之后调用修改cart的api，如果返回ok，则刷新界面
			updatephotolist = (ArrayList<PhotoInfo>)data.getSerializableExtra("photopath");
			if (updatephotolist.get(0).onLine == 1) {//如果是选择的PP的照片
				JSONObject object = new JSONObject();
				try {
					object.put("photoUrl", updatephotolist.get(0).photoThumbnail_512);
					object.put("photoId", updatephotolist.get(0).photoId);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Message msg = handler.obtainMessage();
				msg.what = API.UPLOAD_PHOTO_SUCCESS;
				msg.arg1 = requestCode;
				msg.obj = object;
				handler.sendMessage(msg);
				dialog.show(0);
			}else {
				String photourl = updatephotolist.get(0).photoPathOrURL;
				// 需要上传选择的图片
				StringBuffer sb = new StringBuffer();
				sb.append(Common.BASE_URL).append(Common.UPLOAD_PHOTOS);
				RequestParams params = new RequestParams();
				String tokenId = sPreferences.getString(Common.USERINFO_TOKENID, null);
				System.out.println("上传的图片URL"+photourl);
				try {
					params.put("file", new File(photourl),"application/octet-stream");
					params.put(Common.USERINFO_TOKENID, tokenId);
					API.SetPhoto(sb.toString(), params, handler,requestCode, dialog);
					dialog.show(0);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 1.找到动态添加的imageview，并替换对应的图片
	 * 2.更新orderinfo的arraylist信息
	 * 3.修改数据库
	 * @param position
	 * @param photoList
	 */
	private void changephoto(int position, ArrayList<PhotoInfo> photoList) {
		List<CartPhotosInfo> oriphoto = cartInfoList.get(position/10).cart_photoUrls;
		System.out.println("oriphoto size = "+ oriphoto.size());
		CartPhotosInfo cartPhotosInfo = new CartPhotosInfo();
		cartPhotosInfo.cart_photoUrl = photoList.get(0).photoPathOrURL;
		oriphoto.set(position%10, cartPhotosInfo);//替换图片
		System.out.println("重新选择的图片");
		CartItemInfo map = cartInfoList.get(position/10);
		map.cart_photoUrls = oriphoto;
		map.hasPhoto = true;
		cartInfoList.set(position/10, map);//替换列表信息
		cartAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		AppManager.getInstance().killActivity(this);
	}

	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		System.out.println("test pause");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		System.out.println("test resume");
	}
	
	
}
