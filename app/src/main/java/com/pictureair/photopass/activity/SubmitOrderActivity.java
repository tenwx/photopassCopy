package com.pictureair.photopass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.AddressAdapter;
import com.pictureair.photopass.adapter.SubmitOrderListViewAdapter;
import com.pictureair.photopass.entity.Address;
import com.pictureair.photopass.entity.AddressJson;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.entity.InvoiceInfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.CouponTool;
import com.pictureair.photopass.util.JsonTools;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.widget.PWToast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class SubmitOrderActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = "SubmitOrderActivity";
    public static int PREVIEW_COUPON_CODE = 10000;
    public static int PREVIEW_INVOICE_CODE = 10001;
    private TextView submitButton;
    private TextView totalpriceTextView, currencyTextView, allGoodsTextView;

    private ArrayList<CartItemInfo> list;
    private ListView infoListView;
    private SubmitOrderListViewAdapter submitorderAdapter;

    private ArrayList<PhotoInfo> updatephotolist;
    private float totalprice = 0;
    private int cartCount = 0;
    private static final int CHANGE_PHOTO = 1;//修改图片
    private int payType = 0;//支付类型  0 支付宝 1 银联  2 VISA信用卡 3 代付 4 分期 5 自提 6 paypal

    private PWToast newToast;

    private JSONArray cartItemIds;
    private JSONObject cartItemId;
    private String orderId = "";
    private String currency;
    private String deliveryType = "3";//物流方式 (1和3拼接在一起的)
    private float disPrice = 0;
    private List<Address> addressList;
    private ListView transportListView;
    private AddressAdapter addressAdapter;
    private InvoiceInfo invoiceInfo;//发票信息
    private InvoiceInfo originalInvoiceInfo;//从发票页面传过来的原始发票信息
    private int curPositon = -1;//记录选择的地址


    private TextView couponCountTv, couponPriceUnitTv, couponPriceTv,invoicePriceTv,invoicePriceUnitTv,
            shopPriceUnitTv, shopPriceTv, payPriceUnitTv, payPriceTv, discountPriceUnitTv, discountPriceTv, couponSubtractTv, discountSubtractTv;

    private int couponCount = 0;//优惠券数量
    private float payPrice = 0;//优惠后总费
    private float depletePrice = 0;//优惠减免费用
    private float straightwayPreferentialPrice = 0;//优惠立减
    private float promotionPreferentialPrice = 0;//优惠抵扣
    private float preferentialPrice = 0;//优惠减免总费用
    private float invoicePay=0;//快递费用
    private float resultPrice = 0;//初始总费用

    private static final int PAY_SUCCESS = 10001;//支付成功
    private static final int PAY_FAILED = 10002;//失败

    private JSONArray couponCodes;//优惠券

    private ImageView btn_agreement;//条款按钮
    private boolean isSelecteAgreement = false;//是否选中条款

    private final Handler submitOrderHandler = new SubmitOrderHandler(this);


    private static class SubmitOrderHandler extends Handler {
        private final WeakReference<SubmitOrderActivity> mActivity;

        public SubmitOrderHandler(SubmitOrderActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivity.get() == null) {
                return;
            }
            mActivity.get().dealHandler(msg);
        }
    }

    /**
     * 处理Message
     *
     * @param msg
     */
    private void dealHandler(Message msg) {
        switch (msg.what) {
            case CHANGE_PHOTO:
                selectPhoto(msg.arg1);
                break;

            case API1.GET_COUPON_SUCCESS:
                JSONObject couponJson = (JSONObject) msg.obj;
                couponCount = couponJson.getInteger("amount");
                dismissPWProgressDialog();
                //更新界面
                updateShopPriceUI(true);

                break;
            case API1.GET_COUPON_FAILED:
                dismissPWProgressDialog();
                newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);

                break;
            case API1.PREVIEW_COUPON_SUCCESS:
                //使用优惠码成功 -- 解析数据
                JSONObject json = (JSONObject) msg.obj;
                PictureAirLog.v(TAG, "PREVIEW_COUPON_SUCCESS json： " + json);

                straightwayPreferentialPrice = Float.valueOf(json.getString("straightwayPreferentialPrice"));//优惠折扣
                promotionPreferentialPrice = Float.valueOf(json.getString("promotionPreferentialPrice"));//优惠立减
                preferentialPrice = Float.valueOf(json.getString("preferentialPrice"));//优惠减免总费用
                totalprice = Float.valueOf(json.getString("resultPrice"));//初始总费用
                payPrice = Float.valueOf(json.getString("totalPrice"));//实际支付总价

                //更新界面
                dismissPWProgressDialog();
                if (straightwayPreferentialPrice == 0) {
                    updateShopPriceUI(true);
                } else {
                    updateShopPriceUI(false);
                }
                break;
            case API1.PREVIEW_COUPON_FAILED:
                //使用优惠码失败
                PictureAirLog.v(TAG, "PREVIEW_COUPON_FAILED code：" + msg.arg1);
                dismissPWProgressDialog();
                newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);

                break;
            case API1.GET_OUTLET_ID_SUCCESS:
                //获取自提地址成功
                AddressJson addressJson = JsonTools.parseObject(msg.obj.toString(), AddressJson.class);
                if (addressJson != null && addressJson.getOutlets().size() > 0) {
                    //更新地址信息View
                    if (addressList != null) {
                        addressList.clear();
                    }
                    addressList = addressJson.getOutlets();
                    addressAdapter.refresh(addressList);
                    fixListViewHeight(transportListView);
                    //存入缓存
                    if (ACache.get(MyApplication.getInstance()).getAsString(Common.ACACHE_ADDRESS) == null || ACache.get(MyApplication.getInstance()).getAsString(Common.ACACHE_ADDRESS).equals("")) {
                        ACache.get(MyApplication.getInstance()).put(Common.ACACHE_ADDRESS, msg.obj.toString(), ACache.TIME_DAY);
                    }
                }
                break;

            case API1.GET_OUTLET_ID_FAILED:
                //获取自提地址失败
                newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);

                break;

            case API1.ADD_ORDER_SUCCESS:
                PictureAirLog.v(TAG, "ADD_ORDER_SUCCESS" + msg.obj);
                JSONObject jsonObject = (JSONObject) msg.obj;
                orderId = jsonObject.getString("orderCode");
                dismissPWProgressDialog();
                if (orderId != null && !orderId.isEmpty()) {
                    goToPayActivity(true);
                }

                break;

            case API1.ADD_ORDER_FAILED:
                PictureAirLog.e(TAG, "ADD_ORDER_FAILED cade: " + msg.arg1);
                dismissPWProgressDialog();
                newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);

                //提交订单失败，购物车数量恢复
                int currentCartCount = SPUtils.getInt(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
                SPUtils.put(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, currentCartCount + cartCount);
                break;

            case API1.UPLOAD_PHOTO_FAILED:
                newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                break;

            case API1.UPLOAD_PHOTO_SUCCESS:
                PictureAirLog.v(TAG, "UPLOAD_PHOTO_SUCCESS " + msg.obj.toString());
                JSONObject result = (JSONObject) msg.obj;
                String photoUrlString = null;
                String photoIdString = null;
                photoUrlString = result.getString("photoUrl");
                photoIdString = result.getString("photoId");
                PictureAirLog.v(TAG, photoUrlString + "_" + photoIdString);
                PhotoInfo itemInfo = updatephotolist.get(0);
                itemInfo.photoId = photoIdString;
                itemInfo.photoPathOrURL = photoUrlString;
                PictureAirLog.v(TAG, photoIdString + "{{{{" + photoUrlString);
                updatephotolist.set(0, itemInfo);
                break;

            case API1.GET_INVOICE_SUCCESS:
                invoiceInfo = originalInvoiceInfo;
                parseInvoicePay(msg);
                dismissPWProgressDialog();
                break;

            case API1.GET_INVOICE_FAILED:
                originalInvoiceInfo = null;
                dismissPWProgressDialog();
                newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);
                break;

            case PAY_SUCCESS:
                //成功跳转相应的界面


                break;
            case PAY_FAILED:
                //失败

                break;

            default:
                break;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_order);
        newToast = new PWToast(this);
        initView();

    }

    private void initView() {
        setTopLeftValueAndShow(R.drawable.back_white, true);
        setTopTitleShow(R.string.submitorder);
        totalpriceTextView = (TextView) findViewById(R.id.submitorder_textView3);
        currencyTextView = (TextView) findViewById(R.id.textView_currency);
        submitButton = (TextView) findViewById(R.id.button2_submit);
        allGoodsTextView = (TextView) findViewById(R.id.good_count);
        submitButton.setOnClickListener(this);
        list = (ArrayList<CartItemInfo>) getIntent().getSerializableExtra("orderinfo");//获取订单信息
        infoListView = (ListView) findViewById(R.id.listView_submitorder);
        currency = SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CURRENCY, Common.DEFAULT_CURRENCY);
        submitorderAdapter = new SubmitOrderListViewAdapter(this, list, currency);
        infoListView.setHeaderDividersEnabled(false);
        infoListView.setFooterDividersEnabled(false);
        infoListView.addHeaderView(initHeaderAndFooterView(true, false, null));
        infoListView.setAdapter(submitorderAdapter);
        if (list == null) {
            PictureAirLog.v(TAG, "initView list == null ");
            return;
        }
        PictureAirLog.v(TAG, "initView" + list.size());
        //封装购物车ID，提交订单使用
        cartItemIds = new JSONArray();

        for (int i = 0; i < list.size(); i++) {
            //获取总价格
            totalprice += list.get(i).getUnitPrice() * list.get(i).getQty();
            //获取购物车商品数
            cartCount += list.get(i).getQty();
            //获取购物车ID
            cartItemIds.add(list.get(i).getCartId());
            //根据商品名称，判断收货类型 0 -虚拟商品 1-实体商品（需要地址）
            if (list.get(i).getEntityType() == 0) {
                deliveryType += 3 + ",";
            } else {
                deliveryType += 1 + ",";
            }
        }
        Intent intent = getIntent();
        disPrice = intent.getFloatExtra("discountPrice", disPrice);
        PictureAirLog.v(TAG, "discountPrice：" + disPrice);
        PictureAirLog.v(TAG, "initView deliveryType：" + deliveryType);
        payPrice = totalprice - disPrice;//实际支付等于默认价格减去优惠立减价格
        //获取优惠码数量
        getCoupons();
        if (deliveryType.contains("1")) {
            //需要显示自提地址列表
            addressList = new ArrayList<>();
            infoListView.addFooterView(initHeaderAndFooterView(false, true, addressList));
            //获取地址
            getAddress();
        } else {
            //显示shop和商品优惠信息
            infoListView.addFooterView(initHeaderAndFooterView(false, false, null));
        }
        updateShopPriceUI(true);
        invoicePriceUnitTv.setText(currency);
        totalpriceTextView.setText(((int) payPrice + ""));
        currencyTextView.setText(currency);
        allGoodsTextView.setText(String.format(getString(R.string.all_goods), list.size()));

    }

    /**
     * 获取优惠信息
     */
    public void getCoupons() {
        showPWProgressDialog();
        API1.getCartItemCoupons(submitOrderHandler, cartItemIds);
    }

    /**
     * 获取收货地址列表
     */
    public void getAddress() {
        String addressByACache = ACache.get(MyApplication.getInstance()).getAsString(Common.ACACHE_ADDRESS);
        if (addressByACache != null && !addressByACache.equals("")) {
            submitOrderHandler.obtainMessage(API1.GET_OUTLET_ID_SUCCESS, addressByACache).sendToTarget();
        } else {
            API1.getOutlets(submitOrderHandler);
        }
    }

    /**
     * 添加顶部、底部视图
     *
     * @param isHeader
     * @return
     */
    public View initHeaderAndFooterView(final boolean isHeader, boolean isShowAddress, List<Address> list) {
        View view = LayoutInflater.from(this).inflate(R.layout.address_layout, null);
        ImageView transportIv = (ImageView) view.findViewById(R.id.transport_iv);
        TextView transportTv = (TextView) view.findViewById(R.id.transport_tv);
        LinearLayout transLinearLayout = (LinearLayout) view.findViewById(R.id.transport_linearlayout);
        RelativeLayout couponCountRl = (RelativeLayout) view.findViewById(R.id.coupon_count_rl);
        RelativeLayout invoiceRl = (RelativeLayout) view.findViewById(R.id.invoice_rl);
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.shop_coupon_ll);
        View lineView = view.findViewById(R.id.coupon_line);
        couponCountRl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //进入优惠券选择界面
                Intent intent = new Intent();
                intent.setClass(SubmitOrderActivity.this, CouponActivity.class);
                intent.putExtra(CouponTool.ACTIVITY_ORDER, CouponTool.ACTIVITY_ORDER);
                intent.putExtra(CouponTool.ACTIVITY_ORDER_CART_DATAS, cartItemIds.toString());
                intent.putExtra("couponCodes", couponCodes == null ? "" : couponCodes.toString());
                startActivityForResult(intent, PREVIEW_COUPON_CODE);
            }
        });
        invoiceRl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //进入发票界面
                Intent intent=new Intent(SubmitOrderActivity.this,InvoiceActivity.class);
                if(null != invoiceInfo){
                    Bundle b=new Bundle();
                    b.putParcelable("invoiceInfo",invoiceInfo);
                    intent.putExtras(b);
                }
                startActivityForResult(intent,PREVIEW_INVOICE_CODE);
            }
        });

        invoicePriceTv = (TextView) view.findViewById(R.id.invoice_price_tv);
        invoicePriceUnitTv = (TextView) view.findViewById(R.id.invoice_price_unit_tv);
        couponCountTv = (TextView) view.findViewById(R.id.coupon_count_tv);
        couponPriceUnitTv = (TextView) view.findViewById(R.id.coupon_price_unit_tv);
        couponPriceTv = (TextView) view.findViewById(R.id.coupon_price_tv);
        shopPriceUnitTv = (TextView) view.findViewById(R.id.shop_price_unit_tv);
        shopPriceTv = (TextView) view.findViewById(R.id.shop_price_tv);
        payPriceUnitTv = (TextView) view.findViewById(R.id.pay_price_unit_tv);
        payPriceTv = (TextView) view.findViewById(R.id.pay_price_tv);
        discountPriceTv = (TextView) view.findViewById(R.id.discount_price_tv);
        discountPriceUnitTv = (TextView) view.findViewById(R.id.discount_price_unit_tv);
        couponSubtractTv = (TextView) view.findViewById(R.id.coupon_subtract_tv);
        discountSubtractTv = (TextView) view.findViewById(R.id.discount_subtract_tv);

        transportListView = (ListView) view.findViewById(R.id.transport_list);
        View transportLine = (View) view.findViewById(R.id.pickup_line);

        btn_agreement = (ImageView) view.findViewById(R.id.iv_agreement);
        btn_agreement.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSelecteAgreement) {
                    isSelecteAgreement = true;
                    btn_agreement.setImageResource(R.drawable.gender_sele);
                } else {
                    isSelecteAgreement = false;
                    btn_agreement.setImageResource(R.drawable.gender_normal);
                }
            }
        });
        TextView tvAgreement = (TextView) view.findViewById(R.id.tv_agreement);
        //条款
        tvAgreement.setMovementMethod(LinkMovementMethod.getInstance());
        CharSequence text = tvAgreement.getText();
        if (text instanceof Spannable) {
            int end = text.length();
            Spannable sp = (Spannable) tvAgreement.getText();
            URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
            SpannableStringBuilder style = new SpannableStringBuilder(text);
            style.clearSpans();// should clear old spans
            for (URLSpan url : urls) {
                MyURLSpan myURLSpan = new MyURLSpan(url.getURL());
                style.setSpan(myURLSpan, sp.getSpanStart(url), sp.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }
            tvAgreement.setText(style);
        }
        if (isHeader) {
            transportIv.setImageResource(R.drawable.icon_shop);
            transportTv.setText(R.string.goods_info);
            transportListView.setVisibility(View.GONE);
            layout.setVisibility(View.GONE);
            lineView.setVisibility(View.GONE);
            transportLine.setVisibility(View.VISIBLE);

        } else {
            //判断是否是需要显示地址
            if (isShowAddress) {
                lineView.setVisibility(View.VISIBLE);
                transportIv.setImageResource(R.drawable.icon_transport);
                transportTv.setText(R.string.transport);
                transportListView.setVisibility(View.VISIBLE);
                transportLine.setVisibility(View.VISIBLE);
                addressAdapter = new AddressAdapter(this, list, new AddressAdapter.doOnClickAddressListener() {
                    @Override
                    public void doOnClickAddressListener(int position) {
                        refreshAddress(position);
                    }
                });
                transportListView.setAdapter(addressAdapter);
                transportListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        refreshAddress(position);
                    }
                });
                fixListViewHeight(transportListView);
            } else {
                lineView.setVisibility(View.GONE);
//                transportIv.setVisibility(View.GONE);
//                transportTv.setVisibility(View.GONE);
                transportLine.setVisibility(View.GONE);
                transLinearLayout.setVisibility(View.GONE);
            }

            //显示商品优惠价格
            layout.setVisibility(View.VISIBLE);
        }
        return view;
    }

    /**
     * 更新商品优惠信息
     */
    public void updateShopPriceUI(boolean isCount) {
        couponPriceUnitTv.setText(currency);
        shopPriceUnitTv.setText(currency);
        payPriceUnitTv.setText(currency);
        discountPriceUnitTv.setText(currency);
        if (isCount) {
            if (couponCount == 0) {
                couponCountTv.setText(R.string.no_coupon1);
            } else {
                couponCountTv.setText(String.format(getString(R.string.coupon_count), couponCount));
            }
        } else {
            couponCountTv.setText("-" + currency + (int) straightwayPreferentialPrice);
        }

        PictureAirLog.out("(int) straightwayPreferentialPrice " + (int) straightwayPreferentialPrice);
        if ((int) straightwayPreferentialPrice == 0) {
            couponSubtractTv.setVisibility(View.GONE);
        } else {
            couponSubtractTv.setVisibility(View.VISIBLE);
        }

        PictureAirLog.out("(int) disPrice " + (int) disPrice);

        if ((int) disPrice == 0) {
            discountSubtractTv.setVisibility(View.GONE);
        } else {
            discountSubtractTv.setVisibility(View.VISIBLE);
        }

        couponPriceTv.setText((int) straightwayPreferentialPrice + "");
        shopPriceTv.setText((int) totalprice + "");
        payPriceTv.setText(((int) payPrice + ""));
        discountPriceTv.setText((int) disPrice + "");

        totalpriceTextView.setText(((int) payPrice + ""));
    }

    /**
     * 更新选择地址
     *
     * @param position
     */
    public void refreshAddress(int position) {
        if (position == curPositon) {
            return;
        } else {
            curPositon = position;
        }
        //选择地址
        if (addressList != null && addressList.size() > 0) {
            //单选
            boolean isSelect = addressList.get(position).getIsSelect();
            if (isSelect) {
                isSelect = false;
            } else {
                isSelect = true;
            }
            for (Address address : addressList) {
                if (address == addressList.get(position)) {
                    address.setIsSelect(isSelect);
                } else {
                    address.setIsSelect(!isSelect);
                }
            }
            addressAdapter.refresh(addressList);
        }

    }

    /**
     * 展开所有item
     *
     * @param listView
     */
    public void fixListViewHeight(ListView listView) {
        // 如果没有设置数据适配器，则ListView没有子项，返回。
        ListAdapter listAdapter = listView.getAdapter();
        int totalHeight = 0;
        if (listAdapter == null) {
            return;
        }
        for (int index = 0; index < listAdapter.getCount(); index++) {
            View listViewItem = listAdapter.getView(index, null, listView);
            // 计算子项View 的宽高
            listViewItem.measure(0, 0);
            // 计算所有子项的高度和
            totalHeight += listViewItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        // listView.getDividerHeight()获取子项间分隔符的高度
        // params.height设置ListView完全显示需要的高度
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    /**
     * 价格为0时，正常跳转，无需支付，点击确认。
     */
    public void goToPayActivity(boolean isNeedPay) {
        Intent intent2 = new Intent(SubmitOrderActivity.this, PaymentOrderActivity.class);
        String orderName, orderIntroduce = null;
        if (list.size() == 1) {
            orderName = list.get(0).getProductNameAlias();
            orderIntroduce = list.get(0).getProductNameAlias() + list.get(0).getUnitPrice() + "*" + list.get(0).getQty();
        } else {
            orderName = getString(R.string.multi_goods);
            for (int i = 0; i < list.size(); i++) {
                if (i == 0) {
                    orderIntroduce = list.get(i).getProductNameAlias() + list.get(i).getUnitPrice() + "*" + list.get(i).getQty();
                } else {
                    orderIntroduce += "," + list.get(i).getProductNameAlias() + list.get(i).getUnitPrice() + "*" + list.get(i).getQty();
                }
            }
        }
        PictureAirLog.out("orderIntroduce---->" + orderIntroduce);
        intent2.putExtra("isNeedPay", isNeedPay);
        intent2.putExtra("name", orderName);
        intent2.putExtra("price", totalpriceTextView.getText().toString());
        intent2.putExtra("introduce", orderIntroduce);
        intent2.putExtra("orderId", orderId);
        intent2.putExtra("cartItemIds", cartItemIds.toString());
        intent2.putExtra("couponCodes", couponCodes != null ? couponCodes.toString() : "");
        intent2.putExtra("cartCount", cartCount);
        //传递商品类型，用于成功后返回订单
        if (deliveryType.contains("1")) {
            //实体商品
            if (curPositon < 0) {
                newToast.setTextAndShow(R.string.select_address, Common.TOAST_SHORT_TIME);
                return;
            }
            intent2.putExtra("productType", 1);
            intent2.putExtra("outletId", addressList.get(curPositon).getOutletId());
            PictureAirLog.v(TAG, "productType: " + 1);
        } else {
            //虚拟商品
            intent2.putExtra("productType", 2);
            PictureAirLog.v(TAG, "productType: " + 2);
        }
        PictureAirLog.v(TAG, "isBack: " + getIntent().getStringExtra("isBack"));
        intent2.putExtra("isBack", getIntent().getStringExtra("isBack"));
        SubmitOrderActivity.this.startActivity(intent2);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button2_submit:
                if (AppUtil.getNetWorkType(MyApplication.getInstance()) == 0) {
                    newToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
                    return;
                }

                if(!isSelecteAgreement){
                    newToast.setTextAndShow(R.string.please_agree, Common.TOAST_SHORT_TIME);
                    return;
                }

                //确认订单后 减掉购物项
                int currentCartCount = SPUtils.getInt(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
                SPUtils.put(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, currentCartCount - cartCount);

                PictureAirLog.v(TAG, "onClick" + deliveryType);
                //判断结算价格是否为0（优惠后的价格）为0，出弹窗提示，确认直接支付成功；不为0，正常支付
                if (payPrice == 0) {
                    goToPayActivity(false);
                    return;
                }

                PictureAirLog.v(TAG, "onClick" + deliveryType);
                if (orderId == null || orderId.equals("")) {
                    JSONObject invoice=null;
                    if (deliveryType.contains("1")) {
                        //获取收货地址
                        if (curPositon < 0) {
                            newToast.setTextAndShow(R.string.select_address, Common.TOAST_SHORT_TIME);
                        } else {
                            showPWProgressDialog();

                            invoice = assembleInvoiceJson();
                            API1.addOrder(cartItemIds, 1, addressList.get(curPositon).getOutletId(), "", couponCodes,invoice, null, null, submitOrderHandler);
                        }
                    } else {
                        //PP+/数码商品不需要地址，需要检查channelId和uid字段
                        String channelStr = getIntent().getStringExtra("chid");
                        String channelId = null;
                        String uid = null;

                        if (!TextUtils.isEmpty(channelStr)) {
                            channelId = channelStr.substring(0, channelStr.indexOf("&"));
                            uid = channelStr.substring(channelStr.indexOf("&") + 1, channelStr.length());
                        }
                        invoice = assembleInvoiceJson();
                        showPWProgressDialog();
                        API1.addOrder(cartItemIds, 3, "", "", couponCodes,invoice, channelId, uid, submitOrderHandler);
                    }
                } else {
                    JSONObject invoice=null;
                    Intent intent2 = new Intent(SubmitOrderActivity.this, PaymentOrderActivity.class);
                    intent2.putExtra("price", totalpriceTextView.getText().toString());
                    String orderName, orderIntroduce = null;
                    if (list.size() == 1) {
                        orderName = list.get(0).getProductNameAlias();
                        orderIntroduce = list.get(0).getProductNameAlias() + list.get(0).getUnitPrice() + "*" + list.get(0).getQty();
                    } else {
                        orderName = getString(R.string.multi_goods);
                        for (int i = 0; i < list.size(); i++) {
                            if (i == 0) {
                                orderIntroduce = list.get(i).getProductNameAlias() + list.get(i).getUnitPrice() + "*" + list.get(i).getQty();
                            } else {
                                orderIntroduce += "," + list.get(i).getProductNameAlias() + list.get(i).getUnitPrice() + "*" + list.get(i).getQty();
                            }
                        }
                    }

                    PictureAirLog.out("orderIntroduce---->" + orderIntroduce);

                    invoice = assembleInvoiceJson();
                    intent2.putExtra("name", orderName);
                    intent2.putExtra("introduce", orderIntroduce);
                    intent2.putExtra("orderId", orderId);
                    intent2.putExtra("couponCodes", couponCodes == null ? "" : couponCodes.toString());
                    intent2.putExtra("invoice",invoice);
                    //传递商品类型，用于成功后返回订单
                    if (deliveryType.contains("1")) {
                        //实体商品
                        intent2.putExtra("productType", 1);
                        PictureAirLog.v(TAG, "productType: " + 1);
                    } else {
                        //虚拟商品
                        intent2.putExtra("productType", 2);
                        PictureAirLog.v(TAG, "productType: " + 2);
                    }

                    SubmitOrderActivity.this.startActivity(intent2);
                }
                break;

            default:
                break;
        }
    }

    //封装发票的jsonObject
    public JSONObject assembleInvoiceJson(){
        if(null != invoiceInfo && invoiceInfo.isNeedInvoice()) {
            JSONObject invoice = new JSONObject();
            invoice.put("invoiceType", 0);
            invoice.put("invoiceTitle", invoiceInfo.getTitle() == InvoiceInfo.PERSONAL ? 0 : 1);
            if(invoiceInfo.getTitle() == InvoiceInfo.COMPANY)
                invoice.put("invoiceCompanyName", invoiceInfo.getCompanyName());
            invoice.put("invoiceAddressId", invoiceInfo.getAddress().getAddressId());
            return invoice;
        }
        return null;
    }

    //解析发票费用
    public void parseInvoicePay(Message msg){
        invoicePriceUnitTv.setText(currency);
        JSONObject result = (JSONObject) msg.obj;
        if(null == result)
            return;
        if(result.containsKey("logisticsFee")) {
            invoicePay = Float.valueOf(result.getString("logisticsFee"));
            invoicePriceTv.setText((int) invoicePay+"");
        }
        if(result.containsKey("totalPrice"))
            payPrice = Float.valueOf(result.getString("totalPrice"));
        payPriceTv.setText(((int) payPrice + ""));
        totalpriceTextView.setText(((int) payPrice + ""));
    }

    //选择照片
    private void selectPhoto(int requestCode) {
        Intent intent = new Intent(this, SelectPhotoActivity.class);
        intent.putExtra("activity", "cartactivity");
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        PictureAirLog.v(TAG, "onActivityResult" + "requestCode: " + requestCode + " resultCode: " + resultCode);
        if (resultCode == RESULT_OK && requestCode == PREVIEW_COUPON_CODE) {
            //选择优惠码返回 请求API使用优惠券
            showPWProgressDialog();
            couponCodes = JSONArray.parseArray(data.getExtras().getString("couponCodes"));
            couponCount = data.getExtras().getInt("couponCount", 0);
            if (couponCount == 0) {
                couponCountTv.setText(R.string.no_coupon1);
            } else {
                couponCountTv.setText(String.format(getString(R.string.coupon_count), couponCount));
            }
            boolean hasInvoiceInfo = false;
            if (null != invoiceInfo) {
                hasInvoiceInfo = invoiceInfo.isNeedInvoice();
            }
            if (cartItemIds != null && cartItemIds.size() > 0 && couponCodes != null && couponCodes.size() > 0) {
                API1.previewCoupon(submitOrderHandler, couponCodes, hasInvoiceInfo, cartItemIds);
            } else {
                //取消使用优惠券，couponCodes为空数组
                if (null != cartItemIds) {
                    API1.previewCoupon(submitOrderHandler, new JSONArray(), hasInvoiceInfo, cartItemIds);
                }
            }
        }

        if(resultCode == RESULT_OK && requestCode == PREVIEW_INVOICE_CODE){
            //TODO 发票返回结果
            originalInvoiceInfo = data.getParcelableExtra("invoiceInfo");
            if(null != originalInvoiceInfo) {
                showPWProgressDialog();
                if (couponCodes != null && couponCodes.size() > 0) {
                    API1.getCartsWithInvoice(cartItemIds, originalInvoiceInfo.isNeedInvoice(), couponCodes, submitOrderHandler);

                } else {
                    API1.getCartsWithInvoice(cartItemIds, originalInvoiceInfo.isNeedInvoice(), new JSONArray(), submitOrderHandler);

                }
            }
        }

    }

    @Override
    public void TopViewClick(View view) {
        super.TopViewClick(view);
        switch (view.getId()) {
            case R.id.topLeftView:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        submitOrderHandler.removeCallbacksAndMessages(null);
    }

    private class MyURLSpan extends ClickableSpan {
        private String mUrl;

        MyURLSpan(String url) {
            mUrl = url;
        }

        @Override
        public void onClick(View widget) {
            Intent intent = new Intent();
            intent.putExtra("key", Integer.valueOf(mUrl));
            intent.setClass(SubmitOrderActivity.this, WebViewActivity.class);
            startActivity(intent);
        }
    }
}
