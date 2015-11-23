package com.pictureAir;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureAir.adapter.OrderProductDetailAdapter;
import com.pictureAir.entity.CartItemInfo;
import com.pictureAir.entity.OrderInfo;
import com.pictureAir.util.AppManager;
import com.pictureAir.util.Common;
import com.pictureAir.widget.NoScrollListView;
/**
 * 订单详情界面
 * @author bauer_bao
 *
 */
public class OrderDetailActivity extends BaseActivity implements OnClickListener{

	private Button deliveryButton;
	private TextView orderNumber, orderTime, payMethod, orderStatus, customer, phoneNumber, address, productPrice, shippingPrice, totalPrice, productCurrency, shipCurrency, totalCurrency;
	private NoScrollListView noScrollListView;
	private ImageView backLayout;
	private LinearLayout addressLayout;
	
	private OrderInfo orderInfo;
	private ArrayList<CartItemInfo> orderDetailArrayList;
	
	private String paymethod = null;
	private String orderstatus = null;
	private String currency = null;
	
	private SharedPreferences sharedPreferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order_detail_activity);
		initView();
	}
	
	private void initView() {
		// TODO Auto-generated method stub
		AppManager.getInstance().addActivity(this);
		deliveryButton = (Button)findViewById(R.id.order_detail_payment);
		backLayout = (ImageView)findViewById(R.id.order_detail_return);
		orderNumber = (TextView)findViewById(R.id.order_detail_number);
		orderTime = (TextView)findViewById(R.id.order_detail_time);
		payMethod = (TextView)findViewById(R.id.order_detail_paymethod);
		orderStatus = (TextView)findViewById(R.id.order_status);
		customer = (TextView)findViewById(R.id.order_customer);
		phoneNumber = (TextView)findViewById(R.id.order_phone_number);
		address = (TextView)findViewById(R.id.order_delivery_address);
		productPrice = (TextView)findViewById(R.id.order_productprice);
		productCurrency = (TextView)findViewById(R.id.order_productprice_currency);
		shipCurrency = (TextView)findViewById(R.id.order_shipping_currency);
		shippingPrice = (TextView)findViewById(R.id.order_shipping);
		totalPrice = (TextView)findViewById(R.id.order_totalprice);
		totalCurrency = (TextView)findViewById(R.id.order_totalprice_currency);
		noScrollListView = (NoScrollListView)findViewById(R.id.product_detail_listview);
		addressLayout = (LinearLayout)findViewById(R.id.order_address);
		backLayout.setOnClickListener(this);
		deliveryButton.setOnClickListener(this);
		
		getData();
		
		orderNumber.setText(orderInfo.orderNumber);
		orderTime.setText(orderInfo.orderTime.substring(0, 10));
		switch (orderInfo.orderPayMentMethod) {
		//订单支付方式  支付类型  0 支付宝 1 银联  2 VISA信用卡 3 代付 4 分期 5 自提 6 paypal
		case 0:
			paymethod = getResources().getString(R.string.zfbzf);
			break;
			
		case 1:
			paymethod = getResources().getString(R.string.yhkzf);
			break;
			
		case 6:
			paymethod = getResources().getString(R.string.paypalzf);
			break;
			
		default:
			break;
		}
		
		payMethod.setText(paymethod);
		
		switch (orderInfo.orderStatus) {
		//订单当前状态 1等待买家付款，2买家已付款（等待卖家发货），3卖家已发货（等待买家确认），4交易成功，5交易关闭,订单冻结
		case 1:
			orderstatus = getResources().getString(R.string.waitpay);
			deliveryButton.setVisibility(View.VISIBLE);
			deliveryButton.setText(R.string.pay);
			break;
			
		case 2:
			orderstatus = getResources().getString(R.string.haspay);
			deliveryButton.setVisibility(View.GONE);
//			deliveryButton.setText(R.string.delivery_details);
			break;
			
		case 3:
			orderstatus = getResources().getString(R.string.hasdelivery);
			deliveryButton.setVisibility(View.GONE);
//			deliveryButton.setText(R.string.delivery_details);
			break;
			
		case 4:
			orderstatus = getResources().getString(R.string.hassign);
			deliveryButton.setVisibility(View.GONE);
//			deliveryButton.setText(R.string.buy_again);
			break;
			
		case 5:
			orderstatus = getResources().getString(R.string.orderclose);
			deliveryButton.setVisibility(View.GONE);
//			deliveryButton.setText(R.string.buy_again);
			break;

		default:
			break;
		}
		orderStatus.setText(orderstatus);
		
		if (orderInfo.deliveryCustomer != null && !"".equals(orderInfo.deliveryCustomer)) {//有地址
			addressLayout.setVisibility(View.VISIBLE);
			customer.setText(orderInfo.deliveryCustomer);
			phoneNumber.setText(orderInfo.deliveryPhoneNumber + "  " + orderInfo.deliveryHomeNumber);
			address.setText(orderInfo.deliveryAddress);
		}else {//没有地址，隐藏
			addressLayout.setVisibility(View.GONE);
		}
		
		productPrice.setText((int)orderInfo.productPrice + "");
		shippingPrice.setText((int)orderInfo.deliveryShipping + "");
		totalPrice.setText((int)orderInfo.orderTotalPrice + "");
		productCurrency.setText(currency);
		shipCurrency.setText(currency);
		totalCurrency.setText(currency);
		noScrollListView.setAdapter(new OrderProductDetailAdapter(this, orderDetailArrayList, currency));
	}
	
	//获取初始化的数据
	private void getData() {
		Bundle bundle = getIntent().getExtras();
		orderInfo = bundle.getParcelable("groupitem");
		orderDetailArrayList = bundle.getParcelableArrayList("childitemlist");
		sharedPreferences = getSharedPreferences(Common.USERINFO_NAME,Context.MODE_PRIVATE);
		currency = sharedPreferences.getString(Common.CURRENCY, Common.DEFAULT_CURRENCY);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		AppManager.getInstance().killActivity(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.order_detail_return:
			finish();
			break;
			
		case R.id.order_detail_payment:
			Intent intent = null;
			switch (orderInfo.orderStatus) {
			case 1://1等待买家付款
				intent = new Intent(this, PaymentOrderActivity.class);
				intent.putExtra("flag", "order");
				intent.putExtra("deliveryInfo", orderInfo);
				startActivity(intent);
				finish();
				break;

//			case 2://2买家已付款（等待卖家发货），3卖家已发货（等待买家确认）
//			case 3:
//				intent = new Intent(this, DeliveryActivity.class);
//				Bundle bundle = new Bundle();
//				bundle.putParcelable("deliveryInfo", orderInfo);
//				intent.putExtras(bundle);
//				startActivity(intent);
//				break;

			case 4://4交易成功，5交易关闭,订单冻结
			case 5:
				System.out.println("buyagain");
				break;

			default:
				break;
			}
			break;

		default:
			break;
		}
	}
}
