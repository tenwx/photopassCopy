package com.pictureAir;

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

import com.pictureAir.entity.GoodsInfo;
import com.pictureAir.util.API;
import com.pictureAir.util.AppManager;
import com.pictureAir.util.Common;
import com.pictureAir.util.ScreenUtil;
import com.pictureAir.widget.BannerView_Detail;
import com.pictureAir.widget.MyToast;
import com.umeng.analytics.MobclickAgent;
/**
 * 商品明细类，此页面可以加入购物车
 * @author bauer_bao
 *
 */
public class DetailProductActivity extends BaseActivity implements OnClickListener{
	//申明控件
	private ViewGroup animMaskLayout;//动画层
	private ImageView buyImg;// 这是在界面上跑的小图片
	private TextView name, promotionPrice, currencyTextView, detail;
	private ImageView returnLayout;
	private ImageView cartImageView;
	private Button buyButton;
	private Button addtocartButton;
	private TextView cartCountTextView;
	private BannerView_Detail bannerView_Detail;
	private TextView  receiveAdress;
	
	//申明实例类
	private GoodsInfo goodsInfo;
	private SharedPreferences sharedPreferences;
	private Editor editor;
	private MyToast myToast;
	
	//申明变量
	private final static String TAG = "DetailProductActivity";
	private int recordcount = 0; //记录数据库中有几条记录

	private Handler mhandler = new Handler(){
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case API.ADD_TO_CART_SUCCESS:
				editor = sharedPreferences.edit();
				editor.putInt(Common.CART_COUNT, sharedPreferences.getInt(Common.CART_COUNT, 0)+1);
				editor.commit();
				buyImg = new ImageView(DetailProductActivity.this);// buyImg是动画的图片
				buyImg.setImageResource(R.drawable.addtocart);// 设置buyImg的图片
				setAnim(buyImg);
				break;
				
			case API.ADD_TO_CART_FAILED:
				myToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
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
		initView();
	}

	private void initView() {
		AppManager.getInstance().addActivity(this);
		//find控件
		returnLayout = (ImageView)findViewById(R.id.rt);
		cartImageView = (ImageView)findViewById(R.id.button_bag);
		cartCountTextView = (TextView)findViewById(R.id.textview_cart_count);
		buyButton = (Button)findViewById(R.id.button_buy);
		addtocartButton = (Button)findViewById(R.id.button_cart);
		bannerView_Detail = (BannerView_Detail)findViewById(R.id.bannerview_product_detail);
		name = (TextView)findViewById(R.id.detail_good_name);
		detail = (TextView)findViewById(R.id.product_detail);
		promotionPrice = (TextView)findViewById(R.id.detail_promotion_price);
		currencyTextView = (TextView)findViewById(R.id.detail_currency);
//		privilegeTextView = (TextView)findViewById(R.id.detail_privilege);
//		originalPrice = (TextView)findViewById(R.id.detail_price);
		receiveAdress = (TextView)findViewById(R.id.detail_receive_address);
		
		//绑定监听
		returnLayout.setOnClickListener(this);
		cartCountTextView.setOnClickListener(this);
		cartImageView.setOnClickListener(this);
		buyButton.setOnClickListener(this);
		addtocartButton.setOnClickListener(this);
		
		//初始化数据
		myToast = new MyToast(this);
		sharedPreferences = getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
		currencyTextView.setText(sharedPreferences.getString(Common.CURRENCY, Common.DEFAULT_CURRENCY));
		goodsInfo = (GoodsInfo) getIntent().getParcelableExtra("goods");
//		goodsInfo = (GoodsInfo) getIntent().getSerializableExtra("goods");
		name.setText(goodsInfo.good_nameAlias);
		detail.setText(goodsInfo.good_detail);
		if (goodsInfo.good_promotionPrice == null) {//没有促销价
//			originalPrice.setVisibility(View.GONE);
//			privilegeTextView.setVisibility(View.GONE);
			promotionPrice.setText(goodsInfo.good_price);
		}else {//有促销价
			promotionPrice.setText(goodsInfo.good_promotionPrice);
//			String originalString = getString(R.string.original_price) + goodsInfo.good_price;
//			originalPrice.setText(originalString);
//			originalPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
		}
		if (goodsInfo.good_type == 1) {//需要自提的商品
			if (goodsInfo.good_name.equals(Common.GOOD_NAME_SINGLE_DIGITAL)) {
				receiveAdress.setVisibility(View.VISIBLE);
				receiveAdress.setText(getString(R.string.address_digital_goods));
				
			}else {
				
//				receiveAdress.setText(getString(R.string.address_goods));
				receiveAdress.setVisibility(View.GONE);
			}
			
//		}else {
//			receiveAdress.setText(getString(R.string.address_digital_goods));
		}
		bannerView_Detail.findimagepath(goodsInfo.good_previewUrls);
	}
	
	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.textview_cart_count:
		case R.id.button_bag:
			intent = new Intent(DetailProductActivity.this, CartActivity.class);
			DetailProductActivity.this.startActivity(intent);
			break;
		case R.id.button_buy://购买按钮，需要将当前商品的类型和单价存储起来
			String _id = sharedPreferences.getString(Common.USERINFO_ID, null);
			if (_id == null) {//判断是否登录
				intent = new Intent(DetailProductActivity.this,LoginActivity.class);
				intent.putExtra("activity", "detailproductactivity");
				DetailProductActivity.this.startActivity(intent);
			}else {
				intent = new Intent(DetailProductActivity.this, SelectPhotoActivity.class);
				intent.putExtra("name", goodsInfo.good_nameAlias);
				intent.putExtra("price", promotionPrice.getText().toString());
				intent.putExtra("introduce", detail.getText().toString());
				String[] urlsStrings = goodsInfo.good_previewUrls.split(",");
				intent.putExtra("productImage", urlsStrings[0]);
				intent.putExtra("activity", "detailproductactivity");
				intent.putExtra("storeid", getIntent().getStringExtra("storeid"));
				intent.putExtra("productid", goodsInfo.good_productId);
				startActivity(intent);
			}
			break;
			
		case R.id.button_cart://加入购物车，会有动画效果
			if (sharedPreferences.getString(Common.USERINFO_ID, null) == null) {
				intent = new Intent(DetailProductActivity.this,LoginActivity.class);
				intent.putExtra("activity", "detailproductactivity");
				DetailProductActivity.this.startActivity(intent);
			}else {
				addtocart();
			}
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
	
	/**
	 * 更新购物车数量
	 */
	private void updateCartCount() {
		// TODO Auto-generated method stub
		recordcount = sharedPreferences.getInt(Common.CART_COUNT, 0);
		if (recordcount<=0) {
			cartCountTextView.setVisibility(View.INVISIBLE);
		}else {
			cartCountTextView.setVisibility(View.VISIBLE);
			cartCountTextView.setText(recordcount+"");
		}
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
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		updateCartCount();
	}

	private void addtocart() {
		String[] urlsStrings = goodsInfo.good_previewUrls.split(",");
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
			e.printStackTrace();
		}
		Log.d(TAG, embedphotos.toString());
		API.addtocart(sharedPreferences.getString(Common.USERINFO_ID, ""), getIntent().getStringExtra("storeid"), goodsInfo.good_productId, 1, 
				name.getText().toString(), "", Double.valueOf(promotionPrice.getText().toString()), urlsStrings[0], detail.getText().toString(), 
				"", Double.valueOf(promotionPrice.getText().toString()), mhandler, null, false);
	}

	private void setAnim(final View v) {
		animMaskLayout = null;
		animMaskLayout = createAnimLayout();
		int[] start_location = new int[2];// 一个整型数组，用来存储按钮的在屏幕的X、Y坐标
		start_location[0] = ScreenUtil.getScreenWidth(DetailProductActivity.this)/2-80;//减去的值和图片大小有关系
		start_location[1] = ScreenUtil.getScreenHeight(DetailProductActivity.this)/2-76;
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
				ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.1f, 1.0f, 0.1f, 
						Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
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
		AppManager.getInstance().killActivity(this);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	
	
	
}
