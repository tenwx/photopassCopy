package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.loopj.android.http.RequestParams;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.AddressAdapter;
import com.pictureair.photopass.adapter.SubmitOrderListViewAdapter;
import com.pictureair.photopass.entity.Address;
import com.pictureair.photopass.entity.AddressJson;
import com.pictureair.photopass.entity.CartItemInfo1;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.JsonTools;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.widget.CustomProgressBarPop;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.MyToast;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class SubmitOrderActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = "SubmitOrderActivity";
    private TextView submitButton;
    private TextView totalpriceTextView, currencyTextView, allGoodsTextView;

    private ArrayList<CartItemInfo1> list;
    private ListView infoListView;
    private SubmitOrderListViewAdapter submitorderAdapter;

    private SharedPreferences sharedPreferences;
    private ArrayList<PhotoInfo> updatephotolist;
    private float totalprice = 0;
    private boolean needAddressGood = false;//是否有需要地址的商品
    private static final int CHANGE_PHOTO = 1;//修改图片
    private int payType = 0;//支付类型  0 支付宝 1 银联  2 VISA信用卡 3 代付 4 分期 5 自提 6 paypal

    private CustomProgressBarPop customProgressBarPop;
    private CustomProgressDialog customProgressDialog;

    private MyToast newToast;

    private JSONArray cartItemIds;
    private JSONObject cartItemId;
    private String orderId = "";
    private String deliveryType = "3";//物流方式 (1和3拼接在一起的)
    private List<Address> addressList;
    private ListView transportListView;
    private AddressAdapter addressAdapter;
    private int curPositon = -1;//记录选择的地址

    private final Handler submitOrderHandler = new SubmitOrderHandler(this);


    private static class SubmitOrderHandler extends Handler{
        private final WeakReference<SubmitOrderActivity> mActivity;

        public SubmitOrderHandler(SubmitOrderActivity activity){
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
     * @param msg
     */
    private void dealHandler(Message msg) {
        switch (msg.what) {
            case CHANGE_PHOTO:
                selectPhoto(msg.arg1);
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
                    if (ACache.get(MyApplication.getInstance()).getAsString(Common.ACACHE_ADDRESS) != null && !ACache.get(MyApplication.getInstance()).getAsString(Common.ACACHE_ADDRESS).equals("")) {
                        ACache.get(MyApplication.getInstance()).put(Common.ACACHE_ADDRESS, msg.obj.toString(), ACache.GOODS_ADDRESS_ACACHE_TIME);
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
                orderId = jsonObject.getString("orderId");
                customProgressDialog.dismiss();
                if (orderId != null && !orderId.isEmpty()) {
                    //一旦成功，购物车已经被服务器删除，此处需要修改购物车数量
                    int count = 0;
                    for (int i = 0; i < list.size(); i++) {
                        count += list.get(i).getQty();
                    }
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(Common.CART_COUNT, sharedPreferences.getInt(Common.CART_COUNT, 0) - count);
                    editor.commit();

                    Intent intent2 = new Intent(SubmitOrderActivity.this, PaymentOrderActivity.class);
                    String orderName, orderIntroduce;
                    if (list.size() == 1) {
                        orderName = list.get(0).getProductName();
                        orderIntroduce = list.get(0).getDescription();
                    } else {
                        orderName = getString(R.string.multi_goods);
                        orderIntroduce = getString(R.string.multi_goods);
                    }
                    intent2.putExtra("name", orderName);
                    intent2.putExtra("price", totalpriceTextView.getText().toString());
                    intent2.putExtra("introduce", orderIntroduce);
                    intent2.putExtra("orderId", orderId);
//                        intent2.putExtra("addressType", needAddressGood);
                    SubmitOrderActivity.this.startActivity(intent2);
                }

                break;

            case API1.ADD_ORDER_FAILED:
                PictureAirLog.e(TAG, "ADD_ORDER_FAILED cade: " + msg.arg1);
                customProgressDialog.dismiss();
                newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);
                break;

            case API1.UPLOAD_PHOTO_FAILED:
                newToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
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

            default:
                break;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_submit_order);
        newToast = new MyToast(this);
        initView();

    }

    private void initView() {
        sharedPreferences = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);

        setTopLeftValueAndShow(R.drawable.back_white, true);
        setTopTitleShow(R.string.submitorder);
        totalpriceTextView = (TextView) findViewById(R.id.submitorder_textView3);
        currencyTextView = (TextView) findViewById(R.id.textView_currency);
        submitButton = (TextView) findViewById(R.id.button2_submit);
        allGoodsTextView = (TextView) findViewById(R.id.good_count);
        submitButton.setOnClickListener(this);
        customProgressBarPop = new CustomProgressBarPop(this, findViewById(R.id.submitOrderRelativeLayout), CustomProgressBarPop.TYPE_UPLOAD);
        list = (ArrayList<CartItemInfo1>) getIntent().getSerializableExtra("orderinfo");//获取订单信息
        infoListView = (ListView) findViewById(R.id.listView_submitorder);
        submitorderAdapter = new SubmitOrderListViewAdapter(this, list, sharedPreferences.getString(Common.CURRENCY, Common.DEFAULT_CURRENCY), submitOrderHandler);
//        infoListView.addHeaderView(new XListViewHeader(this));
        infoListView.setHeaderDividersEnabled(false);
        infoListView.setFooterDividersEnabled(false);
        infoListView.addHeaderView(initHeaderAndFooterView(true, null));
        infoListView.setAdapter(submitorderAdapter);
        if (list == null || list.size() < 0) {
            PictureAirLog.v(TAG, "initView list == null ");
            return;
        }
        PictureAirLog.v(TAG, "initView" + list.size());
        //封装购物车ID，提交订单使用
        cartItemIds = new JSONArray();

        for (int i = 0; i < list.size(); i++) {
            //获取总价格
            totalprice += list.get(i).getUnitPrice() * list.get(i).getQty();
            //获取购物车ID
            cartItemIds.add(list.get(i).getCartId());
            //根据商品名称，判断收货类型 0 -虚拟商品 1-实体商品（需要地址）
            if (list.get(i).getEntityType() == 0) {
                deliveryType += 3 + ",";
            } else {
                deliveryType += 1 + ",";
            }
        }
        PictureAirLog.v(TAG, "initView deliveryType：" + deliveryType);
        if (deliveryType.contains("1")) {
            //需要显示自提地址列表
            addressList = new ArrayList<>();
            infoListView.addFooterView(initHeaderAndFooterView(false, addressList));
            //获取地址
            getAddress();
        }
        totalpriceTextView.setText((int) totalprice + "");
        currencyTextView.setText(sharedPreferences.getString(Common.CURRENCY, Common.DEFAULT_CURRENCY));
        allGoodsTextView.setText(String.format(getString(R.string.all_goods), list.size()));

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
    public View initHeaderAndFooterView(final boolean isHeader, List<Address> list) {
        View view = LayoutInflater.from(this).inflate(R.layout.address_layout, null);
        ImageView transportIv = (ImageView) view.findViewById(R.id.transport_iv);
        TextView transportTv = (TextView) view.findViewById(R.id.transport_tv);
        transportListView = (ListView) view.findViewById(R.id.transport_list);
        if (isHeader) {
            transportIv.setImageResource(R.drawable.icon_shop);
            transportTv.setText(R.string.goods_info);
            transportListView.setVisibility(View.GONE);
        } else {
            transportIv.setImageResource(R.drawable.icon_transport);
            transportTv.setText(R.string.transport);
            transportListView.setVisibility(View.VISIBLE);
            addressAdapter = new AddressAdapter(this, list, new AddressAdapter.doOnClickAddressListener() {
                @Override
                public void doOnClickAddressListener(int position) {
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
            });
            transportListView.setAdapter(addressAdapter);
            fixListViewHeight(transportListView);
        }
        return view;
    }

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button2_submit:
                if (AppUtil.getNetWorkType(MyApplication.getInstance()) == 0) {
                    newToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
                    return;
                }
                PictureAirLog.v(TAG, "onClick" + deliveryType);
                if (orderId == null || orderId.equals("")) {
                    if (deliveryType.contains("1")) {
                        //获取收货地址
                        if (curPositon < 0) {
                            newToast.setTextAndShow(R.string.select_address, Common.TOAST_SHORT_TIME);
                        } else {
                            customProgressDialog = CustomProgressDialog.show(this, getString(R.string.is_loading), false, null);
                            API1.addOrder(cartItemIds, 1, addressList.get(curPositon).getOutletId(), "", submitOrderHandler);
                        }
                    } else {
                        //PP+/数码商品不需要地址
                        customProgressDialog = CustomProgressDialog.show(this, getString(R.string.is_loading), false, null);
                        API1.addOrder(cartItemIds, 3, "", "", submitOrderHandler);
                    }
                } else {
                    Intent intent2 = new Intent(SubmitOrderActivity.this, PaymentOrderActivity.class);
                    intent2.putExtra("price", totalpriceTextView.getText().toString());
                    String orderName, orderIntroduce;
                    if (list.size() == 1) {
                        orderName = list.get(0).getProductName();
                        orderIntroduce = list.get(0).getDescription();
                    } else {
                        orderName = getString(R.string.multi_goods);
                        orderIntroduce = getString(R.string.multi_goods);
                    }

                    intent2.putExtra("name", orderName);
                    intent2.putExtra("introduce", orderIntroduce);
                    intent2.putExtra("orderId", orderId);
//                    intent2.putExtra("addressType", needAddressGood);
                    SubmitOrderActivity.this.startActivity(intent2);
                }
                break;

            default:
                break;
        }
    }

    //选择照片
    private void selectPhoto(int requestCode) {
        Intent intent = new Intent(this, SelectPhotoActivity1.class);
        intent.putExtra("activity", "cartactivity");
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 20) {//先要上传图片，上传完之后调用修改cart的api，如果返回ok，则刷新界面
            updatephotolist = (ArrayList<PhotoInfo>) data.getSerializableExtra("photopath");
            if (updatephotolist.get(0).onLine == 1) {//如果是选择的PP的照片
                JSONObject object = new JSONObject();
                object.put("photoUrl", updatephotolist.get(0).photoThumbnail_512);
                object.put("photoId", updatephotolist.get(0).photoId);
                Message msg = submitOrderHandler.obtainMessage();
                msg.what = API1.UPLOAD_PHOTO_SUCCESS;
                msg.arg1 = requestCode;
                msg.obj = object;
                submitOrderHandler.sendMessage(msg);
//				dialog = ProgressDialog.show(this, getString(R.string.loading___), getString(R.string.photo_is_uploading), true, true);
                customProgressBarPop.show(0);
            } else {
                String photourl = updatephotolist.get(0).photoPathOrURL;
                // 需要上传选择的图片
                RequestParams params = new RequestParams();
                String tokenId = sharedPreferences.getString(Common.USERINFO_TOKENID, null);
                PictureAirLog.v(TAG, "上传的图片URL" + photourl);
                try {
                    params.put("file", new File(photourl), "application/octet-stream");
                    params.put(Common.USERINFO_TOKENID, tokenId);
                    API1.SetPhoto(params, submitOrderHandler, requestCode, customProgressBarPop);
//					dialog = ProgressDialog.show(this, getString(R.string.loading___), getString(R.string.photo_is_uploading), true, true);
                    customProgressBarPop.show(0);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
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
}
