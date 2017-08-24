package com.pictureair.hkdlphotopass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pictureair.hkdlphotopass.R;
import com.pictureair.hkdlphotopass.adapter.OrderProductDetailAdapter;
import com.pictureair.hkdlphotopass.entity.CartItemInfo;
import com.pictureair.hkdlphotopass.entity.OrderInfo;
import com.pictureair.hkdlphotopass.util.Common;
import com.pictureair.hkdlphotopass.widget.NoScrollListView;

import java.math.BigDecimal;
import java.util.ArrayList;


/**
 * 订单详情界面
 *
 * @author bauer_bao
 */
public class OrderDetailActivity extends BaseActivity implements OnClickListener {

    private Button deliveryButton;
    private TextView orderNumber, orderTime, payMethod, orderStatus, productPrice, address, payTotalPrice, preferentialPrice, straightwayPreferentialPrice;
    private TextView invoiceShippingPriceTV;
    private OrderInfo orderInfo;
    private ArrayList<CartItemInfo> orderDetailArrayList;

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
        preferentialPrice = (TextView) findViewById(R.id.promotionPreferentialPrice_tv);
        straightwayPreferentialPrice = (TextView) findViewById(R.id.straightwayPreferentialPrice_tv);
        invoiceShippingPriceTV = (TextView) findViewById(R.id.invoice_shipping_price_tv);
        payTotalPrice = (TextView) findViewById(R.id.pay_total_price);

        address = (TextView) findViewById(R.id.order_delivery_address);

        noScrollListView = (NoScrollListView) findViewById(R.id.product_detail_listview);

        back = (ImageView) findViewById(R.id.order_detail_return);

        deliveryButton.setOnClickListener(this);
        back.setOnClickListener(this);
        getData();

        orderNumber.setText(orderInfo.orderNumber);
        orderTime.setText(orderInfo.orderTime.substring(0, 19));
        switch (orderInfo.orderPayMentMethod) {
            //订单支付方式  支付类型  0 支付宝 1 银联  2 VISA信用卡 3 代付 4 分期 5 自提 6 paypal
            case 0:
                paymethod = getResources().getString(R.string.zfbzf);
                break;

            case 1:
                paymethod = getResources().getString(R.string.yhkzf);
                break;

            case 8:
                paymethod = getResources().getString(R.string.other_ways);
                break;

            case 7:
                paymethod = getResources().getString(R.string.wxzf);

            default:
                break;
        }

        payMethod.setText(paymethod);  //支付方式
        switch (orderInfo.orderStatus) {
            //订单当前状态 -3 已退款 -2交易错误 -1交易取消 0交易处理中 1等待付款 2交易成功
            //兼容订单状态 1等待买家付款，2买家已付款（等待卖家发货），3卖家已发货（等待买家确认），4交易成功，5交易关闭,订单冻结,6,已付款 未收到推送

            case -3:
                orderstatus = getResources().getString(R.string.order_refund);
                deliveryButton.setVisibility(View.GONE);
                break;

            case -1://交易取消
                orderstatus = getResources().getString(R.string.order_cancled);//不能显示待支付,因为已不可支付
                deliveryButton.setVisibility(View.GONE);
                payMethod.setVisibility(View.INVISIBLE);
                break;

            case -2://交易错误
            case 1://等待付款
                orderstatus = getResources().getString(R.string.order_unpaid);
                deliveryButton.setVisibility(View.GONE);
                deliveryButton.setText(R.string.pay);
                deliveryButton.setClickable(true);
                payMethod.setVisibility(View.INVISIBLE);
                break;

            case 2://交易成功
            case 3:
            case 4:
            case 5:
                if (orderInfo.productEntityType == 0) {
                    //3为虚拟类商品无须快递
                    orderstatus = getResources().getString(R.string.order_completed);
                } else {
                    orderstatus = getResources().getString(R.string.order_paid);
                }
                deliveryButton.setVisibility(View.GONE);
                break;
            case 0://交易处理中
            case 6:
                orderstatus = getResources().getString(R.string.order_pending);
                deliveryButton.setVisibility(View.GONE);
                deliveryButton.setBackgroundResource(R.drawable.button_gray);
                deliveryButton.setText(R.string.pay);
                deliveryButton.setClickable(false);

                break;

            default:
                break;
        }
        orderStatus.setText(orderstatus);

        BigDecimal productDecimal = new BigDecimal(orderInfo.productPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalDecimal = new BigDecimal(orderInfo.orderTotalPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
        productPrice.setText(currency + String.valueOf(productDecimal));
        payTotalPrice.setText(currency + String.valueOf(totalDecimal));
        preferentialPrice.setText(currency + orderInfo.promotionPreferentialPrice);
        straightwayPreferentialPrice.setText(currency + orderInfo.straightwayPreferentialPrice);
        invoiceShippingPriceTV.setText(currency + orderInfo.deliveryShipping);


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
                address.setText("");
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
        orderDetailArrayList = (ArrayList<CartItemInfo>) bundle.getSerializable("childitemlist");

        currency =  Common.DEFAULT_CURRENCY;//SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CURRENCY, Common.DEFAULT_CURRENCY);
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
                String orderIntroduce = null;
                if (orderDetailArrayList.size() > 1){//>1
                    orderName = getString(R.string.multi_goods);
                    for (int i = 0; i < orderDetailArrayList.size(); i++) {
                        if (i == 0) {
                            orderIntroduce = orderDetailArrayList.get(i).getProductName() + orderDetailArrayList.get(i).getUnitPrice() + "*" + orderDetailArrayList.get(i).getQty();
                        } else {
                            orderIntroduce += "," + orderDetailArrayList.get(i).getProductName() + orderDetailArrayList.get(i).getUnitPrice() + "*" + orderDetailArrayList.get(i).getQty();
                        }
                    }
                } else {//1
                    orderName = orderDetailArrayList.get(0).getProductName();
                    orderIntroduce = orderDetailArrayList.get(0).getProductName() + orderDetailArrayList.get(0).getUnitPrice() + "*" + orderDetailArrayList.get(0).getQty();
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
