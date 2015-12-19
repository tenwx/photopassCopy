package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
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
import com.pictureair.photopass.entity.CartItemInfo1;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.API;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.JsonTools;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.widget.CustomProgressBarPop;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.MyToast;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class SubmitOrderActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = "SubmitOrderActivity";
    private TextView submitButton;
    private ImageView llrtLayout;
    private TextView totalpriceTextView, currencyTextView, allGoodsTextView;

    private ArrayList<CartItemInfo1> list;
    private String nameString;
    private String introduceString;

    private ListView infoListView;
    private SubmitOrderListViewAdapter submitorderAdapter;

    private SharedPreferences sharedPreferences;
    private ArrayList<PhotoInfo> updatephotolist;
    private float totalprice = 0;
    private boolean needAddressGood = false;//是否有需要地址的商品
    private static final int CHANGE_PHOTO = 1;//修改图片
    private static final int DELIVERY_EXPRESS = 0;//物流
    private static final int DELIVERY_PICKUP = 1;//选择自提
    private static final int DELIVERY_NOEXPRESS = 3;//虚拟类商品无须快递
    private int payType = 0;//支付类型  0 支付宝 1 银联  2 VISA信用卡 3 代付 4 分期 5 自提 6 paypal

    private CustomProgressBarPop customProgressBarPop;
    private CustomProgressDialog customProgressDialog;

    private MyToast newToast;

    private JSONArray cartItemIds;
    private JSONObject cartItemId;
    private String orderId = "";
    private int deliveryType = 3;//物流方式
    private List<Address> addressList;//收货地址
    private Address address;


    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case CHANGE_PHOTO:
                    selectPhoto(msg.arg1);
                    break;
                case API1.GET_OUTLET_ID_SUCCESS:
                    //获取自提地址成功
                    address = JsonTools.parseObject((JSONObject) msg.obj, Address.class);
                    //存入缓存
                    MyApplication.address = address;
//                    ACache.get(MyApplication.getInstance()).put(Common.ACACHE_ADDRESS, address);
                    API1.addOrder(cartItemIds, deliveryType, "", "", mHandler);

                    break;

                case API1.GET_OUTLET_ID_FAILED:
                    //获取自提地址失败
                    API1.addOrder(cartItemIds, deliveryType, "", "", mHandler);

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
                        Editor editor = sharedPreferences.edit();
                        editor.putInt(Common.CART_COUNT, sharedPreferences.getInt(Common.CART_COUNT, 0) - count);
                        editor.commit();

                        Intent intent2 = new Intent(SubmitOrderActivity.this, PaymentOrderActivity.class);
                        intent2.putExtra("name", "PictureAir");
                        intent2.putExtra("price", totalpriceTextView.getText().toString());
                        intent2.putExtra("introduce", "Made by PictureAir");
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

                case API.UPLOAD_PHOTO_FAILED:
                    newToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
                    break;

                case API.UPLOAD_PHOTO_SUCCESS:
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
                    //创建jsonobject对象
                    final int position = msg.arg1;
//                    com.alibaba.fastjson.JSONObject cartItem = JsonUtil.CreateModifyCartItemJsonObject(updatephotolist, list.get(position / 10), list.get(position / 10).getQty());
//                    PictureAirLog.v(TAG,cartItem.toString());
                    RequestParams params = new RequestParams();
                    params.put(Common.USER_ID, sharedPreferences.getString(Common.USERINFO_ID, ""));
//                    params.put(Common.ITEM, cartItem);
//                    HttpUtil.post(Common.BASE_URL + Common.MODIFY_CART, params, new JsonHttpResponseHandler() {
//
//                        @Override
//                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                            // TODO Auto-generated method stub
//                            super.onSuccess(statusCode, headers, response);
//                            PictureAirLog.v(TAG,"modify cart with change photot ==" + response);
//                            if (response.containsKey("message")) {//添加失败
//                                newToast.setTextAndShow(R.string.uploadphotofailed, Common.TOAST_SHORT_TIME);
//                            } else {//添加成功
//                                changephoto(position, updatephotolist);
//                            }
//                            if (customProgressBarPop.isShowing()) {
//                                customProgressBarPop.dismiss();
//                            }
//                        }
//
//                        @Override
//                        public void onProgress(long bytesWritten, long totalSize) {
//                            super.onProgress(bytesWritten, totalSize);
//                            customProgressBarPop.setProgress(bytesWritten, totalSize);
//                        }
//
//                        ;
//
//                        @Override
//                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//                            super.onFailure(statusCode, headers, throwable, errorResponse);
//                            newToast.setTextAndShow(R.string.uploadphotofailed, Common.TOAST_SHORT_TIME);
//                            if (customProgressBarPop.isShowing()) {
//                                customProgressBarPop.dismiss();
//                            }
//                        }
//                    });
                    break;

                default:
                    break;
            }
        }

        ;
    };

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
        totalpriceTextView = (TextView) findViewById(R.id.submitorder_textView3);
        currencyTextView = (TextView) findViewById(R.id.textView_currency);
        submitButton = (TextView) findViewById(R.id.button2_submit);
        allGoodsTextView = (TextView) findViewById(R.id.good_count);
        submitButton.setOnClickListener(this);
        llrtLayout = (ImageView) findViewById(R.id.llrt);
        llrtLayout.setOnClickListener(this);
        customProgressBarPop = new CustomProgressBarPop(this, findViewById(R.id.submitOrderRelativeLayout), CustomProgressBarPop.TYPE_UPLOAD);
        list = (ArrayList<CartItemInfo1>) getIntent().getSerializableExtra("orderinfo");//获取订单信息
        infoListView = (ListView) findViewById(R.id.listView_submitorder);
        submitorderAdapter = new SubmitOrderListViewAdapter(this, list, sharedPreferences.getString(Common.CURRENCY, Common.DEFAULT_CURRENCY), mHandler);
//        infoListView.addHeaderView(new XListViewHeader(this));
        infoListView.setAdapter(submitorderAdapter);
        infoListView.setHeaderDividersEnabled(false);
        infoListView.setFooterDividersEnabled(false);
        infoListView.addHeaderView(initHeaderAndFooterView(true));
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
            //根据商品名称，判断收货类型
            if (list.get(i).getProductName().equals(Common.GOOD_NAME_TSHIRT)) {
                deliveryType = 1;
                break;

            } else if (list.get(i).getProductName().equals(Common.GOOD_NAME_6R)) {
                deliveryType = 1;
                break;

            } else if (list.get(i).getProductName().equals(Common.GOOD_NAME_COOK)) {
                deliveryType = 1;
                break;

            }
        }
        PictureAirLog.v(TAG, "initView deliveryType：" + deliveryType);
        if (deliveryType == 1) {
            infoListView.addFooterView(initHeaderAndFooterView(false));
        }
        totalpriceTextView.setText((int) totalprice + "");
        currencyTextView.setText(sharedPreferences.getString(Common.CURRENCY, Common.DEFAULT_CURRENCY));
        allGoodsTextView.setText(String.format(getString(R.string.all_goods), list.size()));

    }

    /**
     * 添加顶部、底部视图
     *
     * @param isHeader
     * @return
     */
    public View initHeaderAndFooterView(boolean isHeader) {
        View view = LayoutInflater.from(this).inflate(R.layout.address_layout, null);
        ImageView transportIv = (ImageView) view.findViewById(R.id.transport_iv);
        TextView transportTv = (TextView) view.findViewById(R.id.transport_tv);
        ListView transportList = (ListView) view.findViewById(R.id.transport_list);
        if (isHeader) {
            transportIv.setImageResource(R.drawable.icon_shop);
            transportTv.setText(R.string.goods_info);
            transportList.setVisibility(View.GONE);
        } else {
            transportIv.setImageResource(R.drawable.icon_transport);
            transportTv.setText(R.string.transport);
            transportList.setVisibility(View.VISIBLE);
            addressList = new ArrayList<>();
            address = new Address();
            address.setAddress("自提地址。。。");
            address.setIsSelect(true);
            addressList.add(address);
            AddressAdapter addressAdapter = new AddressAdapter(this, addressList);
            transportList.setAdapter(addressAdapter);
        }

        return view;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button2_submit:
                if (orderId.equals("")) {
                    customProgressDialog = CustomProgressDialog.show(this, getString(R.string.is_loading), false, null);
                    PictureAirLog.v(TAG, "onClick nameString" + nameString);
                    if (deliveryType == 1) {
                        //获取收货地址
                        if (MyApplication.address == null) {
                            API1.getOutlets(mHandler);
                        } else {
                            address = MyApplication.address;
                            API1.addOrder(cartItemIds, deliveryType, address.getOutletId(), "", mHandler);
                        }
                    } else {
                        API1.addOrder(cartItemIds, 3, "", "", mHandler);
                    }
                } else {
                    Intent intent2 = new Intent(SubmitOrderActivity.this, PaymentOrderActivity.class);
                    intent2.putExtra("name", "PictureAir");
                    intent2.putExtra("price", totalpriceTextView.getText().toString());
                    intent2.putExtra("introduce", "Made by PictureAir");
                    intent2.putExtra("orderId", orderId);
//                    intent2.putExtra("addressType", needAddressGood);
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
        Intent intent = new Intent(this, SelectPhotoActivity.class);
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
                Message msg = mHandler.obtainMessage();
                msg.what = API.UPLOAD_PHOTO_SUCCESS;
                msg.arg1 = requestCode;
                msg.obj = object;
                mHandler.sendMessage(msg);
//				dialog = ProgressDialog.show(this, getString(R.string.loading___), getString(R.string.photo_is_uploading), true, true);
                customProgressBarPop.show(0);
            } else {
                String photourl = updatephotolist.get(0).photoPathOrURL;
                // 需要上传选择的图片
                StringBuffer sb = new StringBuffer();
                sb.append(Common.BASE_URL).append(Common.UPLOAD_PHOTOS);
                RequestParams params = new RequestParams();
                String tokenId = sharedPreferences.getString(Common.USERINFO_TOKENID, null);
                PictureAirLog.v(TAG, "上传的图片URL" + photourl);
                try {
                    params.put("file", new File(photourl), "application/octet-stream");
                    params.put(Common.USERINFO_TOKENID, tokenId);
                    API.SetPhoto(sb.toString(), params, mHandler, requestCode, customProgressBarPop);
//					dialog = ProgressDialog.show(this, getString(R.string.loading___), getString(R.string.photo_is_uploading), true, true);
                    customProgressBarPop.show(0);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
