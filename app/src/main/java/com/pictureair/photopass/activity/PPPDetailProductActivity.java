package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.API;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.BannerView_Detail;
import com.pictureair.photopass.widget.MyToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
/**
 * PP+商品明细类
 * @author bauer_bao
 *
 */
public class PPPDetailProductActivity extends BaseActivity implements OnClickListener{
	//申明控件
	private ViewGroup animMaskLayout;//动画层
	private ImageView buyImg;// 这是在界面上跑的小图片
	private TextView nameTextView;
	private TextView promotionPriceTextView;
	private TextView detailTextView;
	private TextView currencyTextView;
	private ImageView returnLayout;
	private ImageView cartImageView;
	private Button buyButton;
	private Button addToCartButton;
	private TextView cartCountTextView;
	private BannerView_Detail bannerViewDetail;
//	private TextView privilegeTextView;
//	private TextView originalTextView;
	private TextView shopAddressTextView;

	//申明变量
	private final static String TAG = "PPPDetailProductAct";
	private int recordCount = 0; //记录数据库中有几条记录
	private String storeIdString = null;
	private String PPPProductId = null;
	private String NameAliasString = null;
	private String PPPNameString = null;
	private String PPPDetail = null;
	private double PPPPrice = 0;
	private int promotionPrice = 0;
	private String PPPUrl = "";
	private String currencyString = "";
	private boolean isBuyNow = false;

	//申明其他类
	private SharedPreferences sharedPreferences;
	private Editor editor;
	private ACache aCache;
	private MyToast myToast;

	private Handler mhandler = new Handler(){
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case API.GET_PHOTOPASSPLUS_SUCCESS://获取ppp商品详情成功
				JSONObject getpppgoodsobj = null;
				try {
					getpppgoodsobj = new JSONObject(msg.obj.toString());
					currencyString = getpppgoodsobj.getString("currency");
					JSONObject resJsonObject = getpppgoodsobj.getJSONObject("products");
					PPPDetail = resJsonObject.getString("description");//提取内容
					PPPNameString = resJsonObject.getString("name");//提取内容
					storeIdString = resJsonObject.getString("storeId");
					PPPPrice = resJsonObject.getInt("productPrice");
					PPPProductId = resJsonObject.getString("productId");
					NameAliasString = resJsonObject.getString("nameAlias");
					promotionPrice = resJsonObject.getInt("promotionPrice");
					JSONArray urlsArray = new JSONArray(resJsonObject.getString("imageURLS"));

					for (int j = 0; j < urlsArray.length(); j++) {
						if (j==0) {

							PPPUrl = urlsArray.get(j).toString();
						}else {
							PPPUrl += ","+(String)urlsArray.get(j);
						}
					}
					nameTextView.setText(NameAliasString);
					bannerViewDetail.findimagepath(PPPUrl);
					detailTextView.setText(PPPDetail);
					currencyTextView.setText(currencyString);
					if (promotionPrice == (int)PPPPrice) {//没有促销价
//						originalTextView.setVisibility(View.GONE);
//						privilegeTextView.setVisibility(View.GONE);
						promotionPriceTextView.setText((int)PPPPrice + "");
					}else {//有促销价
						promotionPriceTextView.setText(promotionPrice+"");
						String originalString = getString(R.string.original_price) + (int)PPPPrice;
//						originalTextView.setText(originalString);
//						originalTextView.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
					}
					editor = sharedPreferences.edit();
					editor.putString(Common.CURRENCY, currencyString);
					editor.putString(Common.STORE_ID, storeIdString);
					editor.commit();
					shopAddressTextView.setVisibility(View.GONE);
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
				break;
				
			case API.GET_PHOTOPASSPLUS_FAILED:
			case API.ADD_TO_CART_FAILED:
				myToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
				break;

			case API.ADD_TO_CART_SUCCESS:
				JSONObject addcart = (JSONObject) msg.obj;
				editor = sharedPreferences.edit();
				editor.putInt(Common.CART_COUNT, sharedPreferences.getInt(Common.CART_COUNT, 0)+1);
				editor.commit();
				String itemidString = "";
				try {
					itemidString = addcart.getString("cartItemId");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (isBuyNow) {
					Intent intent = new Intent(PPPDetailProductActivity.this, SubmitOrderActivity.class);
					ArrayList<CartItemInfo> orderinfoArrayList = new ArrayList<CartItemInfo>();
					CartItemInfo cartItemInfo = new CartItemInfo();
					cartItemInfo.cart_productName = PPPNameString;
					cartItemInfo.cart_originalPrice = PPPPrice;
					cartItemInfo.cart_photoUrls = null;
					cartItemInfo.cart_productIntroduce = PPPDetail;
					cartItemInfo.cart_quantity = 1;
					cartItemInfo.cart_id = itemidString;
					cartItemInfo.cart_storeId = storeIdString;
					String[] urlStrings = PPPUrl.split(",");
					cartItemInfo.cart_productImageUrl = urlStrings[0];
					cartItemInfo.cart_productId = PPPProductId;
					cartItemInfo.cart_productType = 3;
					orderinfoArrayList.add(cartItemInfo);
					intent.putParcelableArrayListExtra("orderinfo", orderinfoArrayList);
					PPPDetailProductActivity.this.startActivity(intent);
				}else {
					buyImg = new ImageView(PPPDetailProductActivity.this);// buyImg是动画的图片
					buyImg.setImageResource(R.drawable.addtocart);// 设置buyImg的图片
					setAnim(buyImg);
				}
				break;

			default:
				break;
			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_product);
		//初始化控件
		returnLayout = (ImageView)findViewById(R.id.rt);
		cartImageView = (ImageView)findViewById(R.id.button_bag);
		cartCountTextView = (TextView)findViewById(R.id.textview_cart_count);
		nameTextView = (TextView)findViewById(R.id.detail_good_name);
		bannerViewDetail = (BannerView_Detail)findViewById(R.id.bannerview_product_detail);
		detailTextView = (TextView)findViewById(R.id.product_detail);
		currencyTextView = (TextView)findViewById(R.id.detail_currency);
		promotionPriceTextView = (TextView)findViewById(R.id.detail_promotion_price);
//		privilegeTextView = (TextView)findViewById(R.id.detail_privilege);
//		originalTextView = (TextView)findViewById(R.id.detail_price);
		shopAddressTextView = (TextView)findViewById(R.id.detail_receive_address);
		buyButton = (Button)findViewById(R.id.button_buy);
		addToCartButton = (Button)findViewById(R.id.button_cart);

		//绑定监听
		returnLayout.setOnClickListener(this);
		cartCountTextView.setOnClickListener(this);
		cartImageView.setOnClickListener(this);
		buyButton.setOnClickListener(this);
		addToCartButton.setOnClickListener(this);
		
		//初始数据
		myToast = new MyToast(this);
		sharedPreferences = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
		aCache = ACache.get(this);
		if (aCache.getAsString(Common.PPP_GOOD) == null) {//如果缓存为空，从服务器获取
			Log.d(TAG, "ppp is null");
			API.getPPP(PPPDetailProductActivity.this, "140.206.125.195", mhandler, ((MyApplication)getApplication()).getLanguageType());
		}else {//如果在缓存中
			Log.d(TAG, "ppp not null");
			Message message = mhandler.obtainMessage();
			message.what = API.GET_PHOTOPASSPLUS_SUCCESS;
			message.obj = aCache.getAsString(Common.PPP_GOOD);
			mhandler.sendMessage(message);
		}
		buyButton.setText(R.string.buy_good);
	}


	@Override
	protected void onResume() {
		super.onResume();
		recordCount = sharedPreferences.getInt(Common.CART_COUNT, 0);
		if (recordCount<=0) {
			cartCountTextView.setVisibility(View.INVISIBLE);
		}else {
			cartCountTextView.setVisibility(View.VISIBLE);
			cartCountTextView.setText(recordCount+"");
		}
	}

	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.textview_cart_count:
		case R.id.button_bag:
			intent = new Intent(this, CartActivity.class);
			startActivity(intent);
			break;

		case R.id.button_buy://购买按钮，需要将当前商品的类型和单价存储起来
			isBuyNow = true;
			addtocart();
			break;
			
		case R.id.button_cart://加入购物车，会有动画效果
			//如果没有登录，先提示登录
			isBuyNow = false;
			addtocart();
			break;
			
		case R.id.rt:
			goBack();
			break;

		default:
			break;
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	//返回操作
	private void goBack() {
		if (AppManager.getInstance().checkActivity(MainTabActivity.class)) {//说明有这个界面
			finish();
		}else {//没有这个页面
			Intent intent = new Intent(this, MainTabActivity.class);
			startActivity(intent);
			finish();
		}
	}

	private void addtocart() {
		//调用getShop() API
		Log.d(TAG, "add to cart");
		//编辑传入照片的信息
		JSONArray embedphotos = new JSONArray();//放入图片的json数组
		JSONObject embedphoto = new JSONObject();
		JSONArray photoids = new JSONArray();//放入图片的图片id数组
		JSONObject photoid = new JSONObject();
		try {
			//如果是多张照片，此处需要for循环处理
			photoid.put("photoId", "");
			photoid.put("photoUrl", "");
			photoids.put(photoid);
			embedphoto.put("photosIds", photoids);
			embedphoto.put("svg", "");
			embedphotos.put(embedphoto);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d(TAG, embedphotos.toString());
		String[] photoImage = PPPUrl.split(",");
		API.addtocart(sharedPreferences.getString(Common.USERINFO_ID, ""), storeIdString, PPPProductId, 1,
				PPPNameString, "", PPPPrice, photoImage[0], PPPDetail, "", PPPPrice, mhandler, embedphotos, isBuyNow);

	}

	private void setAnim(final View v) {
		animMaskLayout = null;
		animMaskLayout = createAnimLayout();
		int[] start_location = new int[2];// 一个整型数组，用来存储按钮的在屏幕的X、Y坐标
		start_location[0] = ScreenUtil.getScreenWidth(PPPDetailProductActivity.this)/2-80;//减去的值和图片大小有关系
		start_location[1] = ScreenUtil.getScreenHeight(PPPDetailProductActivity.this)/2-76;
		// 将组件添加到我们的动画层上
		final View view = addViewToAnimLayout(animMaskLayout, v,start_location);
		int[] end_location = new int[2];
		cartCountTextView.getLocationInWindow(end_location);
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

					}

					@Override
					public void onAnimationRepeat(Animation animation) {

					}

					@Override
					public void onAnimationEnd(Animation animation) {
						v.setVisibility(View.GONE);//控件消失
						int i = sharedPreferences.getInt(Common.CART_COUNT, 0);
						if (i<=0) {
							cartCountTextView.setVisibility(View.INVISIBLE);
						}else {
							cartCountTextView.setVisibility(View.VISIBLE);
							cartCountTextView.setText(i+"");
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

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}


	
}
