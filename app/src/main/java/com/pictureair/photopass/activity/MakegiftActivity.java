package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.MakegiftGoodsAdapter;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.entity.CartPhotosInfo;
import com.pictureair.photopass.entity.GoodsInfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.API;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.BannerView_PreviewCompositeProduct;
import com.pictureair.photopass.widget.CustomProgressBarPop;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.MyToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class MakegiftActivity extends BaseActivity implements OnClickListener{
	//选择商品的horizontalscrollview的popupwindow
	private GridView goodsGridView;
	private MakegiftGoodsAdapter mAdapter;
	private ArrayList<GoodsInfo> allList;
	private GoodsInfo goodsInfo;

	private ImageView returnLayout;
	private ImageView cartButton;
	private ImageView selectButton;
	private Button buyButton;
	private Button addtocartButton;
	private TextView cartcountTextView, currencytextview;

	private ViewGroup anim_mask_layout;//动画层
	private ImageView buyImg;// 这是在界面上跑的小图片
	private SharedPreferences sp;
	private String tokenId;
	private Editor editor;
	private boolean isbuynow = false;
	private BannerView_PreviewCompositeProduct bannerView_Makegift;

	private String photourl;
	private String idString;
	private ArrayList<PhotoInfo> photoList;
	private ArrayList<CartPhotosInfo> photoListAfterUpload;//图片上传之后的list
	//选择商品popupwindow
	private PopupWindow selproductPopupWindow;
	private View selproductView_popwindow;

	private ImageView addphotoButton;

	private int count = 1;//图片的数量
	private int upload_index = 0;
	private TextView priceTextView;
	private TextView introduceTextView;
	private int recordcount;

	private CustomProgressDialog progressDialog;
	private CustomProgressBarPop progressBarPop;

	private String productname = "cup";
	private String productOriginalName = "cup";
	private String productIdString = "";
	private String productImageUrlString = "";
	
	private MyToast newToast;
	
	private int previewViewWidth;
	private int previewViewHeight;
	
	private final static int WAIT_DRAW_FINISH = 111;

	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case API.UPLOAD_PHOTO_SUCCESS:
				System.out.println(msg.obj.toString()+"uploadphotosuccess");
				if (!"start".equals(msg.obj.toString())) {//说明是调用接口之后返回的数据，需要更新photoId和photoURL
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
					
					PhotoInfo info = photoList.get(upload_index-1);
					info.isUploaded = 1;
					info.photoId = photoIdString;
					info.photoPathOrURL = photoUrlString;
					photoList.set(upload_index-1, info);
				}
				if (upload_index<photoList.size()) {
					if (photoList.get(upload_index).onLine == 0) {//需要将图片上传
						if (photoList.get(upload_index).isUploaded == 1) {//已经上传过了
							System.out.println("has already uploaded");
							PhotoInfo selectPhotoItemInfo = photoList.get(upload_index);
							selectPhotoItemInfo.photoId = photoList.get(upload_index).photoId;
							selectPhotoItemInfo.photoPathOrURL = photoList.get(upload_index).photoPathOrURL;
							photoList.set(upload_index, selectPhotoItemInfo);
							Message message = handler.obtainMessage();
							message.what = API.UPLOAD_PHOTO_SUCCESS;
							message.obj = "start";
							handler.sendMessage(message);
						}else {//还没有上传
							System.out.println("not uploaded, starting upload");
							String photourl = photoList.get(upload_index).photoPathOrURL;
							System.out.println("上传的图片URL"+photourl);
							// 需要上传选择的图片
							RequestParams params = new RequestParams();
							try {
								params.put("file", new File(photourl),"application/octet-stream");
								params.put(Common.USERINFO_TOKENID, tokenId);
								System.out.println(tokenId+"tokenid");
								API.SetPhoto(Common.BASE_URL+Common.UPLOAD_PHOTOS, params, handler,upload_index, progressBarPop);
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}else {//服务器上获取的图片，只需要将photoid获取就行
						PhotoInfo info = photoList.get(upload_index);
						info.photoId = photoList.get(upload_index).photoId;
						info.photoPathOrURL = photoList.get(upload_index).photoThumbnail_512;
						photoList.set(upload_index, info);
						Message message = handler.obtainMessage();
						message.what = API.UPLOAD_PHOTO_SUCCESS;
						message.obj = "start";
						handler.sendMessage(message);
					}
					upload_index++;
				}else {//开始加入购物车
					upload_index = 0;
					//编辑传入照片的信息
					JSONArray embedphotos = new JSONArray();//放入图片的json数组
					try {
						JSONObject embedphoto = new JSONObject();
						JSONArray photoids = new JSONArray();//放入图片的图片id数组
						for (int i = 0; i < photoList.size(); i++) {
							JSONObject photoid = new JSONObject();
							photoid.put("photoId", photoList.get(i).photoId);
							photoid.put("photoUrl", photoList.get(i).photoPathOrURL);
							photoids.put(photoid);
						}
						embedphoto.put("photosIds", photoids);
						embedphoto.put("svg", "");
						embedphotos.put(embedphoto);

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println(embedphotos.toString());
					String [] urlStrings = productImageUrlString.split(",");
					System.out.println(urlStrings[0]+"_"+sp.getString(Common.STORE_ID, null));
					API.addtocart(sp.getString(Common.USERINFO_ID, ""), sp.getString(Common.STORE_ID, null), productIdString, 1, productname, "", 
							Double.valueOf(priceTextView.getText().toString()),urlStrings[0],introduceTextView.getText().toString(),
							"", Double.valueOf(priceTextView.getText().toString()), handler, embedphotos,isbuynow);
				}
				break;
				
			case API.GET_ALL_GOODS_SUCCESS:
				JSONObject getallgoodsobj = (JSONObject) msg.obj;
				if (null == getallgoodsobj) {
					System.out.println("null");
				} else {
					String nameString = null;
					String nameAliasString = null;
					String detail = null;
					String price = null;
					String productid = null;
					ACache.get(MakegiftActivity.this).put(Common.ALL_GOODS, getallgoodsobj);
					try {
						String resultString = getallgoodsobj.getString("products").toString();
						JSONArray jsonArray = new JSONArray(resultString);
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject message = (JSONObject)jsonArray.get(i);//获取解析文字
							nameString = message.getString("name");//提取内容
							nameAliasString = message.getString("nameAlias");
							detail = message.getString("description");//提取内容
							price = message.getString("productPrice");
							productid = message.getString("productId");
							JSONArray urlsArray = new JSONArray(message.getString("imageURLS"));
							String url = "";
							for (int j = 0; j < urlsArray.length(); j++) {
								if (j==0) {
									url = urlsArray.get(j).toString();
								}else {
									url += ","+(String)urlsArray.get(j);
								}
								System.out.println("getallgoods"+j+nameString+"_"+detail+"_"+url);
							}
							goodsInfo = new GoodsInfo();
							goodsInfo.good_name = nameString;
							goodsInfo.good_nameAlias = nameAliasString;
							goodsInfo.good_previewUrls = url;
							goodsInfo.good_price = price;
							goodsInfo.good_detail = detail;
							goodsInfo.good_productId = productid;
							if (Common.GOOD_NAME_PPP.equals(nameString)) {//区分商品
								//如果是PPP
								goodsInfo.good_embedPhotoCount = 0;
								goodsInfo.good_type = 3;
							}else {
								//其他商品
								goodsInfo.good_embedPhotoCount = 1;
								goodsInfo.good_type = 1;
							}
							if (!Common.GOOD_NAME_PPP.equals(nameString)) {
								allList.add(goodsInfo);
							}
						}
						productname = allList.get(0).good_nameAlias;
						productOriginalName = allList.get(0).good_name;
						productIdString = allList.get(0).good_productId;
						productImageUrlString = allList.get(0).good_previewUrls;
						String[] urlStrings = productImageUrlString.split(",");
						priceTextView.setText(allList.get(0).good_price);
						introduceTextView.setText(allList.get(0).good_detail);
//						if (previewViewWidth == 0 || previewViewHeight == 0) {
						//onCreate还没执行完，需要等待
							System.out.println("-------->waiting.....");
							Message message = handler.obtainMessage();
							message.what = WAIT_DRAW_FINISH;
							message.obj = urlStrings[0];
							handler.sendMessageDelayed(message, 500);
//						}else {
//							System.out.println("-------->not waiting.....");
//							
//						}
						
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				if (progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				break;
				
			case API.ADD_TO_CART_SUCCESS:
				if (progressBarPop.isShowing()) {
					progressBarPop.dismiss();
				}
				JSONObject addcart = (JSONObject) msg.obj;
				System.out.println("addtocart=="+addcart);
				String itemidString = "";
				try {
					itemidString = addcart.getString("cartItemId");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				editor = sp.edit();
				editor.putInt(Common.CART_COUNT, sp.getInt(Common.CART_COUNT, 0)+1);
				editor.commit();
				if (isbuynow) {//获取订单信息，传送到下一界面
					Intent intent = new Intent(MakegiftActivity.this, SubmitOrderActivity.class);
					ArrayList<CartItemInfo> orderinfoArrayList = new ArrayList<CartItemInfo>();
					String [] urlStrings = productImageUrlString.split(",");
					CartItemInfo cartItemInfo = new CartItemInfo();
					cartItemInfo.cart_productName = productname;
					cartItemInfo.cart_originalPrice = Double.valueOf(priceTextView.getText().toString());
					cartItemInfo.cart_productIntroduce = introduceTextView.getText().toString();
					cartItemInfo.cart_quantity = 1;
					cartItemInfo.cart_productType = 1;
					cartItemInfo.cart_id = itemidString;
					cartItemInfo.cart_storeId = sp.getString(Common.STORE_ID, null);
					cartItemInfo.cart_productImageUrl = urlStrings[0];
					cartItemInfo.cart_productId = productIdString;
					CartPhotosInfo cartPhotosInfo;
					photoListAfterUpload.clear();
					for (int i = 0; i < photoList.size(); i++) {
						cartPhotosInfo = new CartPhotosInfo();
						cartPhotosInfo.cart_photoUrl = photoList.get(i).photoPathOrURL;
						cartPhotosInfo.cart_photoId = photoList.get(i).photoId;
						photoListAfterUpload.add(cartPhotosInfo);
					}
					cartItemInfo.cart_photoUrls = photoListAfterUpload;
					orderinfoArrayList.add(cartItemInfo);
					intent.putParcelableArrayListExtra("orderinfo", orderinfoArrayList);
					intent.putExtra("activity", "previewproduct");
					MakegiftActivity.this.startActivity(intent);
				}else {
					buyImg = new ImageView(MakegiftActivity.this);// buyImg是动画的图片
					buyImg.setImageResource(R.drawable.addtocart);// 设置buyImg的图片
					setAnim(buyImg);// 开始执行动画
				}
				break;
				
			case API.UPLOAD_PHOTO_FAILED:
				if (progressBarPop.isShowing()) {
					progressBarPop.dismiss();
				}
				upload_index = 0;
				newToast.setTextAndShow(R.string.upload_failed, Common.TOAST_SHORT_TIME);
				break;
				
			case API.GET_ALL_GOODS_FAILED:
				if (progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				newToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
				break;
				
			case API.ADD_TO_CART_FAILED:
				if (progressBarPop.isShowing()) {
					progressBarPop.dismiss();
				}
				upload_index = 0;
				newToast.setTextAndShow(R.string.upload_failed, Common.TOAST_SHORT_TIME);
				break;
				
			case WAIT_DRAW_FINISH:
				//此处，如果数据已经返回，但是控件还没有画好的话，会显示不出来。需要做判断
				if (previewViewWidth != 0 && previewViewHeight != 0) {//onCreate已经执行完，显示图片
					System.out.println("--------->ok");
					setProductImage(productOriginalName, msg.obj.toString());
				}else {//onCreate还没执行完，需要等待
					System.out.println("---------->not ok, waiting.....");
					handler.sendMessageDelayed(msg, 500);
				}
				break;
				
			default:
				break;
				
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_makegift);
		init();
	}
	
	private void init() {
		sp = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
		
		tokenId = sp.getString(Common.USERINFO_TOKENID, null);
		newToast = new MyToast(this);
		currencytextview = (TextView)findViewById(R.id.textView2);
		currencytextview.setText(sp.getString(Common.CURRENCY, Common.DEFAULT_CURRENCY));
		priceTextView = (TextView)findViewById(R.id.textview_productprice);
		introduceTextView = (TextView)findViewById(R.id.product_detail);
		addphotoButton = (ImageView)findViewById(R.id.addimage);
		addphotoButton.setOnClickListener(this);
		returnLayout = (ImageView)findViewById(R.id.rt);
		returnLayout.setOnClickListener(this);
		cartButton = (ImageView)findViewById(R.id.button_cart);
		selectButton = (ImageView)findViewById(R.id.button_selectproduct);
		buyButton = (Button)findViewById(R.id.button_buy);
		addtocartButton = (Button)findViewById(R.id.button_addtocart);
		cartButton.setOnClickListener(this);
		progressBarPop = new CustomProgressBarPop(this, findViewById(R.id.makegift_relativate),CustomProgressBarPop.TYPE_UPLOAD);
		selectButton.setOnClickListener(this);
		buyButton.setOnClickListener(this);
		addtocartButton.setOnClickListener(this);
		cartcountTextView = (TextView)findViewById(R.id.textview_cart_count);
		cartcountTextView.setOnClickListener(this);
		progressDialog = CustomProgressDialog.show(this, getString(R.string.is_loading), true, null);
//		dialog = ProgressDialog.show(this, getString(R.string.loading___), getString(R.string.photo_is_uploading), true, true);
//		dialog.show();
		allList = new ArrayList<GoodsInfo>();
		if (ACache.get(this).getAsJSONObject(Common.ALL_GOODS)==null) {
			API.getAllGoods(handler, sp.getString(Common.STORE_ID, null), ((MyApplication)getApplication()).getLanguageType());
		}else {
			Message message = handler.obtainMessage();
			message.what = API.GET_ALL_GOODS_SUCCESS;
			message.obj = ACache.get(this).getAsJSONObject(Common.ALL_GOODS);
			handler.sendMessage(message);
		}
		recordcount = sp.getInt(Common.CART_COUNT, 0);
		if (recordcount<=0) {
			cartcountTextView.setVisibility(View.INVISIBLE);
		}else {
			cartcountTextView.setVisibility(View.VISIBLE);
			cartcountTextView.setText(recordcount+"");
		}
		photoList = new ArrayList<PhotoInfo>();
		photoListAfterUpload  = new ArrayList<CartPhotosInfo>();
		PhotoInfo itemInfo = new PhotoInfo();
		itemInfo = getIntent().getParcelableExtra("selectPhoto");
//		if (Common.ALBUM_PHOTOPASS.equals(getIntent().getStringExtra("albums"))) {//网络图片
//			photourl = getIntent().getStringExtra("thumbnailURL");//获取照片路径
//			idString = getIntent().getStringExtra("photoId");
//			itemInfo.photoThumbnail_512 = photourl;
//			itemInfo.photoId = idString;
//			itemInfo.albumName = Common.ALBUM_PHOTOPASS;
//			System.out.println(photourl+"____"+idString);
//		}else {//本地图片
//			photourl = getIntent().getStringExtra("photopath");//获取照片路径
////			idString = getIntent().getStringExtra("id");
//			itemInfo.photoPathOrURL = photourl;
//			itemInfo.albumName = "notphotopass";
//			System.out.println(photourl+"____"+idString);
//		}
		photoList.add(itemInfo);
		bannerView_Makegift = (BannerView_PreviewCompositeProduct)findViewById(R.id.bannerview_makegift_detail);
		ViewTreeObserver viewTreeObserver = bannerView_Makegift.getViewTreeObserver();
		viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				bannerView_Makegift.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				previewViewWidth = bannerView_Makegift.getWidth();
				previewViewHeight = bannerView_Makegift.getHeight();
			}
		});
		//选择商品的popupwindow
		selproductView_popwindow = getLayoutInflater().inflate(R.layout.popupwindow_selectproduct, null);
		selproductPopupWindow = new PopupWindow(selproductView_popwindow, android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
		selproductPopupWindow.setFocusable(true);//设置能够获得焦点
		selproductPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//此代码和上一条代码两者结合，实现能够点击popupwindow外面将popupwindow关闭
		goodsGridView = (GridView)selproductView_popwindow.findViewById(R.id.id_horizontalScrollView);
		mAdapter = new MakegiftGoodsAdapter(MakegiftActivity.this, allList);
		
		
		goodsGridView.setAdapter(mAdapter);
//		goodsGridView.setNumColumns(allList.size());
//		goodsGridView.setColumnWidth(ScreenUtil.getScreenWidth(this) / allList.size());
		goodsGridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
		goodsGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				if (allList.get(position).good_name.equals("Album")) {
					System.out.println("album");
					buyButton.setText("Buy ("+count+")");
					addphotoButton.setVisibility(View.VISIBLE);
				}else {
					if (addphotoButton.isShown()) {
						addphotoButton.setVisibility(View.INVISIBLE);
					}
				}
				productname = allList.get(position).good_nameAlias;
				productOriginalName = allList.get(position).good_name;
				productIdString = allList.get(position).good_productId;
				productImageUrlString = allList.get(position).good_previewUrls;
				priceTextView.setText(allList.get(position).good_price);
				introduceTextView.setText(allList.get(position).good_detail);
				String[] urlStrings = productImageUrlString.split(",");
				
				setProductImage(productOriginalName, urlStrings[0]);
				if (selproductPopupWindow.isShowing()) {
					selproductPopupWindow.dismiss();
				}
			}
		});
		
		
	}
	
	/**
	 * 设置makeGift的背景预览图
	 * @param productName 商品名字
	 * @param productURL 商品预览图的背景图片URL
	 */
	private void setProductImage(String productName, String productURL) {
		System.out.println("------->"+productURL);
		if (productName.equals("canvas")) {
			//1.画布，商品宽 355
			//		 商品高 258
			//       左边留白 20
			//		 上边留白 12
			//		 预览图片宽 355-20-19 = 316
			//		 预览图片高 258-12-19 = 227
			bannerView_Makegift.changeimagepath(photoList, productURL, previewViewWidth, previewViewHeight, 355, 258, 20, 12, 316, 227, 0, 0, 0, "canvas");//设置bannerview的图片
//		}else if (productName.equals("iphone5Case")) {
//			//2.手机后盖，商品宽 480
//			//		 商品高 946
//			//       左边留白 0
//			//		 上边留白 0
//			//		 预览图片宽 480
//			//		 预览图片高 946
//			bannerView_Makegift.changeimagepath(photoList, productURL, previewViewWidth, previewViewHeight, 480, 946, 0, 0, 480, 946, 0, R.drawable.iphone_case_mask_bottom, R.drawable.iphone_case_mask_top, "iphone5Case");//设置bannerview的图片
		}else if (productName.equals(Common.GOOD_NAME_SINGLE_DIGITAL)) {
			//3.数码商品，商品宽 300
			//		 商品高 217
			//       左边留白 22
			//		 上边留白 26
			//		 预览图片宽 300-22-21 = 257
			//		 预览图片高 217-26-25 = 166
			bannerView_Makegift.changeimagepath(photoList, productURL, previewViewWidth, previewViewHeight, 300, 217, 22, 26, 257, 166, 0, 0, 0, Common.GOOD_NAME_SINGLE_DIGITAL);//设置bannerview的图片
		}else if (productName.equals("4R Print")) {
			//4.4r相框，商品宽 180
			//		 商品高 120
			//       左边留白 7
			//		 上边留白 7
			//		 预览图片宽 180-7-7 = 166
			//		 预览图片高 120-7-7 = 106
			bannerView_Makegift.changeimagepath(photoList, productURL, previewViewWidth, previewViewHeight, 180, 120, 7, 7, 166, 106, 0, 0, 0, "4R Print");//设置bannerview的图片
		}else if (productName.equals(Common.GOOD_NAME_6R)||productName.equals(Common.GOOD_NAME_COOK)||productName.equals(Common.GOOD_NAME_TSHIRT)) {
			//5.6r相框，商品宽 240
			//		 商品高 180
			//       左边留白 10
			//		 上边留白 14
			//		 预览图片宽 240-10-10 = 220
			//		 预览图片高 180-14-14 = 152
			bannerView_Makegift.changeimagepath(photoList, productURL, previewViewWidth, previewViewHeight, 240, 180, 10, 14, 220, 152, 0, 0, 0, productName);//设置bannerview的图片
		}else if (productName.equals("keyChain")) {
			//6.钥匙圈，商品宽 205
			//		 商品高 89
			//       左边留白 88
			//		 上边留白 18
			//		 预览图片宽 205-88-21 = 205 - 109 = 96
			//		 预览图片高 89-18-16 = 55
			bannerView_Makegift.changeimagepath(photoList, productURL, previewViewWidth, previewViewHeight, 205, 89, 88, 18, 96, 55, 0.15f, 0, 0, "keyChain");//设置bannerview的图片
//		}else if (productName.equals("mug")) {
//			//7.杯子，商品宽 185
//			//		 商品高 160
//			//       左边留白 10
//			//		 上边留白 12
//			//		 预览图片宽 185-10-61 = 114
//			//		 预览图片高 160-12-34 = 114
//			bannerView_Makegift.changeimagepath(photoList, productURL, previewViewWidth, previewViewHeight, 185, 160, 10, 12, 114, 114, 0, 0, R.drawable.mug_mask_top, "mug");//设置bannerview的图片
		}
	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		recordcount = sp.getInt(Common.CART_COUNT, 0);
		if (recordcount<=0) {
			cartcountTextView.setVisibility(View.INVISIBLE);
		}else {
			cartcountTextView.setVisibility(View.VISIBLE);
			cartcountTextView.setText(recordcount+"");
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent;
		switch (v.getId()) {
		case R.id.rt://返回按钮
			finish();
			break;
			
		case R.id.textview_cart_count:
		case R.id.button_cart:
			intent = new Intent(this,CartActivity.class);
			startActivity(intent);
			break;
			
		case R.id.button_selectproduct:
			System.out.println("选择商品");
			mAdapter.notifyDataSetChanged();
			goodsGridView.setNumColumns(allList.size());
			goodsGridView.setColumnWidth(ScreenUtil.getScreenWidth(this) / allList.size());
			selproductPopupWindow.showAsDropDown(findViewById(R.id.makegift_titlebar));
			break;
			
		case R.id.button_buy:
			if (null !=sp.getString(Common.USERINFO_ID, null)) {
				Message message = handler.obtainMessage();
				message.what = API.UPLOAD_PHOTO_SUCCESS;
				isbuynow = true;//buy now
				message.obj = "start";
				handler.sendMessage(message);
				progressBarPop.show(0);
			}else {
				intent = new Intent(this, LoginActivity.class);
				startActivity(intent);
			}
			break;
			
		case R.id.button_addtocart://加入购物车因为还有个上传图片的过程，先上传图片，获取photo返回数据之后再调用购物车API。所以整个动画流程中，有一部分需要进度条
			if (null !=sp.getString(Common.USERINFO_ID, null)) {
				Message message = handler.obtainMessage();
				message.what = API.UPLOAD_PHOTO_SUCCESS;
				isbuynow = false;//add to cart
				message.obj = "start";
				handler.sendMessage(message);
				progressBarPop.show(0);
			}else {
				intent = new Intent(this, LoginActivity.class);
				startActivity(intent);
			}
			break;
			
		case R.id.addimage://添加图片按钮
			System.out.println("addimage");
			intent = new Intent(this,SelectPhotoActivity.class);
			intent.putExtra("photopath", photoList);
			intent.putExtra("activity", "makegiftactivity");
			startActivityForResult(intent, 1);
			break;
			
		default:
			break;
			
		}
	}

	//回调函数处理新添加的图片
	@SuppressWarnings("unchecked")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == 20) {//成功返回
			//do update operation here
			ArrayList<PhotoInfo> list = (ArrayList<PhotoInfo>)data.getSerializableExtra("photopath");
			PhotoInfo info;
			for (int i = 0; i < list.size(); i++) {
				info = new PhotoInfo();
				info.photoPathOrURL = list.get(i).photoPathOrURL;
//				info.Id = list.get(i).Id;
				photoList.add(info);
				System.out.println("i="+i);
			}
			System.out.println("photolist:"+photoList.size());
			addphotoButton.setVisibility(View.INVISIBLE);

			buyButton.setText("Buy ("+photoList.size()+")");
			//设置显示的图片，但是暂时没有这个商品，可以留着以后写
//			bannerView_Makegift.findimagepath(photoList);//设置bannerview的图片
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/*
	 * 设置动画
	 */
	private void setAnim(final View v) {
		anim_mask_layout = null;
		anim_mask_layout = createAnimLayout();
		int[] start_location = new int[2];// 一个整型数组，用来存储按钮的在屏幕的X、Y坐标
		start_location[0] = ScreenUtil.getScreenWidth(this)/2-80;//减去的值和图片大小有关系
		start_location[1] = ScreenUtil.getScreenHeight(this)/2-76;
		// 将组件添加到我们的动画层上
		final View view = addViewToAnimLayout(anim_mask_layout, v,start_location);
		int[] end_location = new int[2];
		cartcountTextView.getLocationInWindow(end_location);
		// 计算位移
		final int endX = end_location[0] - start_location[0];
		final int endY = end_location[1] - start_location[1];

		ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		scaleAnimation.setInterpolator(new LinearInterpolator());//匀速
		scaleAnimation.setRepeatCount(0);//不重复
		scaleAnimation.setFillAfter(true);//停在最后动画
		AnimationSet set = new AnimationSet(false);
		set.setFillAfter(false);
		set.addAnimation(scaleAnimation);
		set.setDuration(500);//动画整个时间
		view.startAnimation(set);//开始动画
		set.setAnimationListener(new AnimationListener() {
			// 动画的开始
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
			}

			// 动画的结束
			@Override
			public void onAnimationEnd(Animation animation) {
				//x轴的路径动画，匀速
				TranslateAnimation translateAnimationX = new TranslateAnimation(0,
						endX, 0, 0);
				translateAnimationX.setInterpolator(new LinearInterpolator());
				translateAnimationX.setRepeatCount(0);// 动画重复执行的次数
				//y轴的路径动画，加速
				TranslateAnimation translateAnimationY = new TranslateAnimation(0, 0,
						0, endY);
				translateAnimationY.setInterpolator(new AccelerateInterpolator());
				translateAnimationY.setRepeatCount(0);// 动画重复执行的次数
				ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.1f, 1.0f, 0.1f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
				AnimationSet set2 = new AnimationSet(false);
				//要先添加形状的，后添加位移的，不然动画效果不能达到要求
				set2.addAnimation(scaleAnimation);
				set2.addAnimation(translateAnimationY);
				set2.addAnimation(translateAnimationX);

				set2.setFillAfter(false);
				set2.setStartOffset(200);//等待时间
				set2.setDuration(800);// 动画的执行时间
				view.startAnimation(set2);
				set2.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationEnd(Animation animation) {
						// TODO Auto-generated method stub
						v.setVisibility(View.GONE);//控件消失
						int i = sp.getInt(Common.CART_COUNT, 0);
						if (i<=0) {
							cartcountTextView.setVisibility(View.INVISIBLE);
						}else {
							cartcountTextView.setVisibility(View.VISIBLE);
							cartcountTextView.setText(i+"");
						}
					}
				});
			}
		});
	}
	
	private ViewGroup createAnimLayout() {
		ViewGroup rootView = (ViewGroup) this.getWindow().getDecorView();
		LinearLayout animLayout = new LinearLayout(this);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		animLayout.setLayoutParams(lp);
		animLayout.setBackgroundResource(android.R.color.transparent);
		rootView.addView(animLayout);
		return animLayout;
	}

	private View addViewToAnimLayout(ViewGroup vg, final View view,
			int[] location) {
		int x = location[0];
		int y = location[1];
		vg.addView(view);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.leftMargin = x;
		lp.topMargin = y;
		view.setLayoutParams(lp);
		return view;
	}
	

}
