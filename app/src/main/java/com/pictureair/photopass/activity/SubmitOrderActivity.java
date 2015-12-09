package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.SubmitOrderListViewAdapter;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.entity.CartPhotosInfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.API;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.HttpUtil;
import com.pictureair.photopass.util.JsonUtil;
import com.pictureair.photopass.widget.CustomProgressBarPop;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.MyToast;
import com.pictureair.photopass.widget.XListViewHeader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class SubmitOrderActivity extends BaseActivity implements OnClickListener{
	private TextView submitButton;
	private ImageView llrtLayout;
	private TextView totalpriceTextView,currencyTextView, allGoodsTextView;

	private ArrayList<CartItemInfo> list;
	private String nameString;
	private String introduceString;

	private ListView infoListView;
	private SubmitOrderListViewAdapter submitorderAdapter;

	private SharedPreferences sharedPreferences;
	private ArrayList<PhotoInfo> updatephotolist;
	private float totalprice = 0;
	private boolean needAddressGood = false;//是否有需要地址的商品
	private static final int CHANGE_PHOTO = 1;//修改图片
//	private static final int DELIVERY_EXPRESS = 0;//物流
	private static final int DELIVERY_PICKUP = 1;//选择自提   
	private static final int DELIVERY_NOEXPRESS = 3;//虚拟类商品无须快递
	private int payType = 0;//支付类型  0 支付宝 1 银联  2 VISA信用卡 3 代付 4 分期 5 自提 6 paypal
//	private ProgressDialog dialog;

	private CustomProgressBarPop customProgressBarPop;
	private CustomProgressDialog customProgressDialog;
	
	private MyToast newToast;
	
	private JSONArray cartItemIds;
	private JSONObject cartItemId;
	private String orderid = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_submit_order);
		newToast = new MyToast(this);
		initview();

	}
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case CHANGE_PHOTO:
				selectPhoto(msg.arg1);
				break;
				
			case API.GET_ORDER_NO_SUCCESS:
				System.out.println("ok"+msg.obj);
				//一旦成功，购物车已经被服务器删除，此处需要修改购物车数量
				int count = 0;
				for (int i = 0; i < list.size(); i++) {
					System.out.println("size="+list.size());
					String s = String.valueOf(list.get(i).cart_quantity);
					count += Integer.parseInt(s); 
					System.out.println("count = "+count);
				}
				Editor editor = sharedPreferences.edit();
				editor.putInt(Common.CART_COUNT, sharedPreferences.getInt(Common.CART_COUNT, 0)-count);
				editor.commit();
				orderid = msg.obj.toString();
				customProgressDialog.dismiss();
				Intent intent2 = new Intent(SubmitOrderActivity.this,PaymentOrderActivity.class);
				intent2.putExtra("name", nameString);
				intent2.putExtra("price", totalpriceTextView.getText().toString());
				intent2.putExtra("introduce", introduceString);
				intent2.putExtra("orderId", orderid);
				intent2.putExtra("addressType", needAddressGood);
				SubmitOrderActivity.this.startActivity(intent2);

				break;

			case API.GET_ORDER_NO_FAILED:
				customProgressDialog.dismiss();
				newToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
				break;
				
			case API.UPLOAD_PHOTO_FAILED:
				newToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
				break;
			
			case API.UPLOAD_PHOTO_SUCCESS:
				System.out.println(msg.obj);
				JSONObject result = (JSONObject) msg.obj;
				String photoUrlString = null;
				String photoIdString = null;
				try {
					photoUrlString = result.getString("photoUrl");
					photoIdString = result.getString("photoId");
					System.out.println(photoUrlString+"_"+photoIdString);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				PhotoInfo itemInfo = updatephotolist.get(0);
				itemInfo.photoId = photoIdString;
				itemInfo.photoPathOrURL = photoUrlString;
				System.out.println(photoIdString+"{{{{"+photoUrlString);
				updatephotolist.set(0, itemInfo);
				//创建jsonobject对象
				final int position = msg.arg1;
				JSONObject cartItem = JsonUtil.CreateModifyCartItemJsonObject(updatephotolist, list.get(position/10), list.get(position/10).cart_quantity);
				System.out.println(cartItem.toString());
				RequestParams params = new RequestParams();
				params.put(Common.USER_ID, sharedPreferences.getString(Common.USERINFO_ID, ""));
				params.put(Common.ITEM, cartItem);
				HttpUtil.post(Common.BASE_URL+Common.MODIFY_CART, params, new JsonHttpResponseHandler() {

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
						if (customProgressBarPop.isShowing()) {
							customProgressBarPop.dismiss();
						}
					}

					@Override
					public void onProgress(long bytesWritten, long totalSize) {
						super.onProgress(bytesWritten, totalSize);
						customProgressBarPop.setProgress(bytesWritten, totalSize);
					};
					
					@Override
					public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
						super.onFailure(statusCode, headers, throwable, errorResponse);
						newToast.setTextAndShow(R.string.uploadphotofailed, Common.TOAST_SHORT_TIME);
						if (customProgressBarPop.isShowing()) {
							customProgressBarPop.dismiss();
						}
					}
				});
				break;
				
			default:
				break;
			}
		};
	};
	private void initview() {
		sharedPreferences = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
		totalpriceTextView = (TextView)findViewById(R.id.submitorder_textView3);
		currencyTextView = (TextView)findViewById(R.id.textView_currency);
		submitButton = (TextView)findViewById(R.id.button2_submit);
		allGoodsTextView = (TextView)findViewById(R.id.good_count);
		submitButton.setOnClickListener(this);
		llrtLayout = (ImageView)findViewById(R.id.llrt);
		llrtLayout.setOnClickListener(this);
		customProgressBarPop = new CustomProgressBarPop(this, findViewById(R.id.submitOrderRelativeLayout), CustomProgressBarPop.TYPE_UPLOAD);
		list = getIntent().getParcelableArrayListExtra("orderinfo");//获取照片路径
		infoListView = (ListView)findViewById(R.id.listView_submitorder);
		submitorderAdapter = new SubmitOrderListViewAdapter(this, list, sharedPreferences.getString(Common.CURRENCY, Common.DEFAULT_CURRENCY), mHandler);
		infoListView.addHeaderView(new XListViewHeader(this));
		infoListView.setAdapter(submitorderAdapter);
		infoListView.setHeaderDividersEnabled(true);
		infoListView.setFooterDividersEnabled(false);
		cartItemIds = new JSONArray();
		for (int i = 0; i < list.size(); i++) {
			if (i==0) {
				nameString = list.get(i).cart_productName;
				introduceString = list.get(i).cart_productIntroduce;
			}else {
				nameString += ","+list.get(i).cart_productName;
				introduceString += "," + list.get(i).cart_productIntroduce;
			}
			totalprice += list.get(i).cart_originalPrice * list.get(i).cart_quantity;
			list.get(i).showPhotos = 0;
			cartItemId = new JSONObject();
			try {
				cartItemId.put("cartItemId", list.get(i).cart_id);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			cartItemIds.put(cartItemId);
			if (!needAddressGood) {
				if (list.get(i).cart_productType == 1) {
					needAddressGood = true;
				}
			}
		}
		totalpriceTextView.setText((int)totalprice+"");
		currencyTextView.setText(sharedPreferences.getString(Common.CURRENCY, Common.DEFAULT_CURRENCY));
		allGoodsTextView.setText(String.format(getString(R.string.all_goods), list.size()));
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button2_submit:
			if (orderid.equals("")) {
//				dialog = ProgressDialog.show(this, getString(R.string.loading___), getString(R.string.is_loading), false, false);
				customProgressDialog = CustomProgressDialog.show(this, getString(R.string.is_loading), false, null);
				int deliveryType = DELIVERY_PICKUP;
				if (nameString.equals(Common.GOOD_NAME_PPP) || nameString.equals(Common.GOOD_NAME_SINGLE_DIGITAL)) {//ppp, pp
					deliveryType = DELIVERY_NOEXPRESS;
				}else {//other goods
					deliveryType = DELIVERY_PICKUP;
				}
				API.addOnOrder(this, mHandler, sharedPreferences.getString(Common.USERINFO_ID, ""), null, payType, getString(R.string.disney_address), cartItemIds, deliveryType);
				
			}else {
				Intent intent2 = new Intent(SubmitOrderActivity.this,PaymentOrderActivity.class);
				intent2.putExtra("name", nameString);
				intent2.putExtra("price", totalpriceTextView.getText().toString());
				intent2.putExtra("introduce", introduceString);
				intent2.putExtra("orderId", orderid);
				intent2.putExtra("addressType", needAddressGood);
				SubmitOrderActivity.this.startActivity(intent2);
			}
			break;
			
		case R.id.llrt:
			finish();
			break;
			
		default:
			break;
		}
	}

	//选择照片
	private void selectPhoto(int requestCode) {
		Intent intent = new Intent(this,SelectPhotoActivity.class);
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
					e.printStackTrace();
				}
				Message msg = mHandler.obtainMessage();
				msg.what = API.UPLOAD_PHOTO_SUCCESS;
				msg.arg1 = requestCode;
				msg.obj = object;
				mHandler.sendMessage(msg);
//				dialog = ProgressDialog.show(this, getString(R.string.loading___), getString(R.string.photo_is_uploading), true, true);
				customProgressBarPop.show(0);
			}else {
				String photourl = updatephotolist.get(0).photoPathOrURL;
				// 需要上传选择的图片
				StringBuffer sb = new StringBuffer();
				sb.append(Common.BASE_URL).append(Common.UPLOAD_PHOTOS);
				RequestParams params = new RequestParams();
				String tokenId = sharedPreferences.getString(Common.USERINFO_TOKENID, null);
				System.out.println("上传的图片URL"+photourl);
				try {
					params.put("file", new File(photourl),"application/octet-stream");
					params.put(Common.USERINFO_TOKENID, tokenId);
					API.SetPhoto(sb.toString(), params, mHandler,requestCode, customProgressBarPop);
//					dialog = ProgressDialog.show(this, getString(R.string.loading___), getString(R.string.photo_is_uploading), true, true);
					customProgressBarPop.show(0);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void changephoto(int position, ArrayList<PhotoInfo> photoList) {
		List<CartPhotosInfo> oriphoto = list.get(position/10).cart_photoUrls;
		CartPhotosInfo hashMap = new CartPhotosInfo();
//		hashMap.cart_photo_local_album = photoList.get(0).albumName;
		hashMap.cart_photoUrl = photoList.get(0).photoPathOrURL;
		oriphoto.set(position%10, hashMap);//替换图片
		System.out.println("重新选择的图片");
		list.get(position/10).cart_photoUrls = oriphoto;
		submitorderAdapter.notifyDataSetChanged();
	}

}
