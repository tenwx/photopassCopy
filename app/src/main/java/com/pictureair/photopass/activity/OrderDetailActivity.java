package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.OrderProductDetailAdapter;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.entity.OrderInfo;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.widget.NoScrollListView;

import java.util.ArrayList;


/**
 * 订单详情界面
 *
 * @author bauer_bao
 */
public class OrderDetailActivity extends BaseActivity implements OnClickListener {

    private Button deliveryButton;
    private TextView orderNumber, orderTime, payMethod, orderStatus, productPrice, address, payTotalPrice;
    private OrderInfo orderInfo;
    private ArrayList<CartItemInfo> orderDetailArrayList;
    private SharedPreferences sharedPreferences;
    private LinearLayout deliveryInfo;

    private ImageView back;

    private String paymethod = null; //支付方式
    private String orderstatus = null; //订单状态
    private String currency = null;

    private NoScrollListView noScrollListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        initView();
    }

    private void initView() {
        deliveryButton = (Button) findViewById(R.id.order_detail_payment);
        orderNumber = (TextView) findViewById(R.id.order_detail_number);
        orderTime = (TextView) findViewById(R.id.order_detail_time);
        payMethod = (TextView) findViewById(R.id.order_detail_paymethod);
        orderStatus = (TextView) findViewById(R.id.order_status);
        productPrice = (TextView) findViewById(R.id.order_productprice);
        payTotalPrice = (TextView) findViewById(R.id.pay_total_price);

        address = (TextView) findViewById(R.id.order_delivery_address);

        noScrollListView = (NoScrollListView) findViewById(R.id.product_detail_listview);

        back = (ImageView) findViewById(R.id.order_detail_return);
        deliveryInfo = (LinearLayout) findViewById(R.id.deliveryInfo);

        deliveryButton.setOnClickListener(this);
        back.setOnClickListener(this);
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

        payMethod.setText(paymethod);  //支付方式
        switch (orderInfo.orderStatus) {
            //订单当前状态 1等待买家付款，2买家已付款（等待卖家发货），3卖家已发货（等待买家确认），4交易成功，5交易关闭,订单冻结,6,已付款 未收到推送
            case 1:
                orderstatus = getResources().getString(R.string.waitpay);
                deliveryButton.setVisibility(View.VISIBLE);
                deliveryButton.setText(R.string.pay);
                deliveryButton.setClickable(true);
                break;

            case 2:
            case 3:
            case 4:
            case 5:
                orderstatus = getResources().getString(R.string.order_paid);
                deliveryButton.setVisibility(View.GONE);
                break;
            case 6:
                orderstatus = getResources().getString(R.string.order_pending);
                deliveryButton.setVisibility(View.VISIBLE);
                deliveryButton.setBackgroundResource(R.drawable.button_gray);
                deliveryButton.setText(R.string.pay);
                deliveryButton.setClickable(false);

                break;

            default:
                break;
        }
        orderStatus.setText(orderstatus);

        productPrice.setText((int) orderInfo.orderTotalPrice + "");
        payTotalPrice.setText((int) orderInfo.orderTotalPrice + "");

        switch (orderInfo.deliveryMethod) {
            case 1:
                //自提、
                if (orderInfo.deliveryAddress != null && !orderInfo.deliveryAddress.equals("")) {
                    address.setText(orderInfo.deliveryAddress);
                }
                break;
            case 3:
                //虚拟类商品无须快递,
//                deliveryInfo.setVisibility(View.GONE);
                address.setText(getString(R.string.address_digital_goods));
                break;

            default:
                break;
        }

        noScrollListView.setAdapter(new OrderProductDetailAdapter(this, orderDetailArrayList, currency));
    }


    // 获取初始化的数据
    private void getData() {
        Bundle bundle = getIntent().getExtras();
        orderInfo = bundle.getParcelable("groupitem");
        orderDetailArrayList = bundle.getParcelableArrayList("childitemlist");
        sharedPreferences = getSharedPreferences(Common.USERINFO_NAME,
                MODE_PRIVATE);
        currency = sharedPreferences.getString(Common.CURRENCY,
                Common.DEFAULT_CURRENCY);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.order_detail_return:
                finish();
                break;
            case R.id.order_detail_payment:
                Intent intent = new Intent(this, PaymentOrderActivity.class);
                intent.putExtra("flag", "order");
                intent.putExtra("deliveryInfo", orderInfo);
                String orderName;
                String orderIntroduce;
                if (orderDetailArrayList.size() > 1){//>1
                    orderName = getString(R.string.multi_goods);
                    orderIntroduce = getString(R.string.multi_goods);
                } else {//1
                    orderName = orderDetailArrayList.get(0).cart_productName;
                    orderIntroduce = orderDetailArrayList.get(0).cart_productIntroduce;
                }
                intent.putExtra("name", orderName);
                intent.putExtra("introduce", orderIntroduce);
                startActivity(intent);
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }
}
