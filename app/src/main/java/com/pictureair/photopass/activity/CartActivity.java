package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.CartInfoAdapter;
import com.pictureair.photopass.entity.CartItemInfo1;
import com.pictureair.photopass.entity.CartItemInfoJson;
import com.pictureair.photopass.entity.CartPhotosInfo1;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.API;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.HttpUtil;
import com.pictureair.photopass.util.JsonTools;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.widget.CustomProgressBarPop;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.MyToast;
import com.pictureair.photopass.widget.NoNetWorkOrNoCountView;
import com.pictureair.photopass.widget.XListViewHeader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * 购物车页面
 *
 * @author bauer_bao
 */
public class CartActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = "CartActivity";
    private ListView listView;
    private ImageView rtButton;
    private Button paymentButton;
    private TextView totalTextView, currencyTextView;
    private ImageView cartSelectAllImageView;
    private TextView editTextView;
    private LinearLayout cartPriceLinearLayout;
    private RelativeLayout bottomRelativeLayout;
    private View line;

    private ArrayList<CartItemInfo1> cartInfoList;// 订单list
    private CartItemInfo1 cartItemInfo;

    private ArrayList<PhotoInfo> updatephotolist;

    private float totalCount = 0;//总数量
    private float totalPrice = 0;//总价格
    private boolean isEdit = false;
    private boolean isDelete = false;
    private int disSelectedCount = 0;//记录已经取消选中的个数

    private CartInfoAdapter cartAdapter;

    private SharedPreferences sPreferences;

    private CustomProgressBarPop dialog;
    private CustomProgressDialog customProgressDialog;

    private String userId = "";

    private MyToast newToast;

    private NoNetWorkOrNoCountView netWorkOrNoCountView;
    private CartItemInfoJson cartItemInfoJson;//存放返回的数据
    private List<CartItemInfo1> deleteCartItemInfoList;//存放删除的购物车

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case API1.GET_CART_SUCCESS:
                    PictureAirLog.v(TAG, "GET_CART_SUCCESS");
                    cartItemInfoJson = JsonTools.parseObject((JSONObject) msg.obj, CartItemInfoJson.class);//CartItemInfoJson.getString()
                    if (cartItemInfoJson == null) {
                        return;
                    }
                    PictureAirLog.v(TAG, "GET_CART_SUCCESS cart size: " + cartItemInfoJson.getItems().size());
                    cartItemInfoJson = setIsSelect(cartItemInfoJson);//更新底部计算条
                    cartInfoList.addAll(cartItemInfoJson.getItems());
                    //更新购物车数量
                    Editor editor = sPreferences.edit();
                    editor.putInt(Common.CART_COUNT, cartItemInfoJson.getTotalCount());
                    editor.commit();

                    totalTextView.setText((float) cartItemInfoJson.getTotalPrice() + "");
                    paymentButton.setVisibility(View.VISIBLE);
                    paymentButton.setText(String.format(getString(R.string.go_pay), cartItemInfoJson.getItems().size()));

                    editTextView.setEnabled(true);
                    customProgressDialog.dismiss();
                    if (cartInfoList.size() == 0) {//没有数量
                        netWorkOrNoCountView.setVisibility(View.VISIBLE);
                        netWorkOrNoCountView.setResult(R.string.no_cart, R.string.want_to_buy, R.string.to_add_good, R.drawable.no_cart, handler, false);
                        bottomRelativeLayout.setVisibility(View.INVISIBLE);
                        listView.setVisibility(View.INVISIBLE);
                        line.setVisibility(View.INVISIBLE);
                        editTextView.setVisibility(View.INVISIBLE);
                    } else {//有数量
                        netWorkOrNoCountView.setVisibility(View.GONE);
                        bottomRelativeLayout.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.VISIBLE);
                        line.setVisibility(View.VISIBLE);
                    }
                    break;

                case API1.GET_CART_FAILED://请求失败
                    customProgressDialog.dismiss();
                    netWorkOrNoCountView.setVisibility(View.VISIBLE);
                    netWorkOrNoCountView.setResult(R.string.no_network, R.string.click_button_reload, R.string.reload, R.drawable.no_network, handler, true);
                    bottomRelativeLayout.setVisibility(View.INVISIBLE);
                    listView.setVisibility(View.INVISIBLE);
                    line.setVisibility(View.INVISIBLE);
                    editTextView.setVisibility(View.INVISIBLE);
                    break;

                case API1.DELETE_CART_FAILED:
                case API.UPLOAD_PHOTO_FAILED:
                    isDelete = false;
                    newToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
                    editTextView.setEnabled(true);
                    break;

                case API1.DELETE_CART_SUCCESS://删除购物车item
                    isDelete = false;
                    cartInfoList.remove(deleteCartItemInfoList.get(0));
                    //更新界面
                    cartAdapter.refresh(cartInfoList);
                    //保存购物车数量
                    editor = sPreferences.edit();
                    editor.putInt(Common.CART_COUNT, cartInfoList.size());
                    editor.commit();

                    //保存购物车数量
                    if (cartInfoList.size() == 0) {
                        paymentButton.setBackgroundResource(R.color.gray_light3);
                        cancelEdit();
                        ShowNoNetOrNoCountView();
                    }
                    break;

                case API.UPLOAD_PHOTO_SUCCESS:
                    JSONObject result = (JSONObject) msg.obj;

                    String photoUrlString = null;
                    String photoIdString = null;
                    try {
                        photoUrlString = result.getString("photoUrl");
                        photoIdString = result.getString("photoId");
                        System.out.println(photoUrlString + "_" + photoIdString);

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    PhotoInfo info = updatephotolist.get(0);
                    info.photoId = photoIdString;
                    info.photoPathOrURL = photoUrlString;
                    updatephotolist.set(0, info);
                    //创建jsonobject对象
                    final int position = msg.arg1;
//                    JSONArray cartItem = JsonUtil.addAndModifyCartItemJsonArray(updatephotolist, cartInfoList.get(position / 10), cartInfoList.get(position / 10).getQty());
                    RequestParams params = new RequestParams();
                    params.put(Common.USER_ID, userId);
//                    params.put(Common.ITEM, cartItem);

                    HttpUtil.post(Common.BASE_URL + Common.MODIFY_CART, params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, org.json.JSONObject response) {
                            // TODO Auto-generated method stub
                            super.onSuccess(statusCode, headers, response);
                            System.out.println("modify cart with change photot ==" + response);

                            if (response.has("message")) {//添加失败
                                newToast.setTextAndShow(R.string.uploadphotofailed, Common.TOAST_SHORT_TIME);
                            } else {//添加成功
                                changephoto(position, updatephotolist);
                            }
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, org.json.JSONObject errorResponse) {
                            // TODO Auto-generated method stub
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                            newToast.setTextAndShow(R.string.uploadphotofailed, Common.TOAST_SHORT_TIME);
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                        }

                        @Override
                        public void onProgress(long bytesWritten, long totalSize) {
                            super.onProgress(bytesWritten, totalSize);
                            dialog.setProgress(bytesWritten, totalSize);
                        }

                        ;
                    });
                    break;

                case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_RELOAD://noView的按钮响应重新加载点击事件
                    //重新加载购物车数据
                    System.out.println("onclick with reload");
                    customProgressDialog = CustomProgressDialog.show(CartActivity.this, getString(R.string.is_loading), false, null);
                    API.getcart(CartActivity.this, Common.BASE_URL + Common.GET_CART, userId, handler);
                    cartInfoList.clear();
                    break;

                case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_NO_RELOAD://noView的按钮响应非重新加载的点击事件
                    //去跳转到购物车
                    System.out.println("onclick with no reload");
                    //需要删除页面，保证只剩下mainTab页面，
                    AppManager.getInstance().killOtherActivity(MainTabActivity.class);
                    //同时将mainTab切换到shop Tab
                    MainTabActivity.changeToShopTab = true;

                    break;

                default:
                    break;
            }
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CartInfoAdapter.MINUSCOUNT:// 减数量
                    if (msg.arg1 == 1) {
                        totalPrice = Float.parseFloat(totalTextView.getText().toString());
                        totalPrice -= Float.parseFloat(msg.obj.toString());
                        totalTextView.setText((int) totalPrice + "");
                    }
                    //更新单个item数据
                    float price = cartInfoList.get(msg.arg2).getPrice() - Float.parseFloat(msg.obj.toString());
                    cartInfoList.get(msg.arg2).setPrice((int) price);
                    //更新总数量、总价格
                    cartItemInfoJson.setTotalCount(cartItemInfoJson.getTotalCount() - 1);
                    cartItemInfoJson.setTotalPrice(cartItemInfoJson.getTotalPrice() - (int) msg.obj);

                    Editor editor = sPreferences.edit();
                    editor.putInt(Common.CART_COUNT, sPreferences.getInt(Common.CART_COUNT, 0) - 1);
                    editor.commit();
                    break;

                case CartInfoAdapter.ADDCOUNT:// 加数量
                    if (msg.arg1 == 1) {
                        totalPrice = Float.parseFloat(totalTextView.getText().toString());
                        totalPrice += Float.parseFloat(msg.obj.toString());
                        totalTextView.setText((int) totalPrice + "");

                    }
                    //更新单个item数据
                    price = cartInfoList.get(msg.arg2).getPrice() + Float.parseFloat(msg.obj.toString());
                    cartInfoList.get(msg.arg2).setPrice((int) price);
                    //更新总数量、总价格
                    cartItemInfoJson.setTotalCount(cartItemInfoJson.getTotalCount() + 1);
                    cartItemInfoJson.setTotalPrice(cartItemInfoJson.getTotalPrice() - (int) msg.obj);

                    Editor editor1 = sPreferences.edit();
                    editor1.putInt(Common.CART_COUNT, sPreferences.getInt(Common.CART_COUNT, 0) + 1);
                    editor1.commit();
                    break;

                case CartInfoAdapter.SELECTED:// 选中item
                    totalPrice = Float.parseFloat(totalTextView.getText().toString());
                    totalPrice += Float.parseFloat(msg.obj.toString());
                    totalTextView.setText(totalPrice + "");
                    disSelectedCount--;
                    if (disSelectedCount == 0) {
                        cartSelectAllImageView.setImageResource(R.drawable.cart_select);
                        if (isEdit) {
                            paymentButton.setBackgroundResource(R.color.orange);

                        } else {//购买状态
                            if (cartItemInfoJson.getItems().size() == disSelectedCount) {//没有选中任何
                                paymentButton.setBackgroundResource(R.color.gray_light3);
                            } else {//选中
                                paymentButton.setBackgroundResource(R.color.blue);
                            }
                        }
                    } else {
                        cartSelectAllImageView.setImageResource(R.drawable.cart_not_select);
                        if (!isEdit) {
                            paymentButton.setBackgroundResource(R.color.blue);
                        } else {
                            paymentButton.setBackgroundResource(R.color.orange);
                        }
                    }

                    if (!isEdit) {
                        paymentButton.setText(String.format(getString(R.string.go_pay), cartItemInfoJson.getItems().size() - disSelectedCount));
                    }
                    break;
                case CartInfoAdapter.NOSELECTED:// 取消选中item
                    totalPrice = Float.parseFloat(totalTextView.getText().toString());
                    totalPrice -= Float.parseFloat(msg.obj.toString());
                    totalTextView.setText(totalPrice + "");
                    disSelectedCount++;
                    if (disSelectedCount == 0) {
                        cartSelectAllImageView.setImageResource(R.drawable.cart_select);
                        if (isEdit) {

                            paymentButton.setBackgroundResource(R.color.orange);
                        } else {
                            paymentButton.setBackgroundResource(R.color.gray_light3);

                        }
                    } else {
                        cartSelectAllImageView.setImageResource(R.drawable.cart_not_select);
                    }
                    if (disSelectedCount == cartItemInfoJson.getItems().size()) {
                        paymentButton.setBackgroundResource(R.color.gray_light3);
                    }
                    if (!isEdit) {
                        paymentButton.setText(String.format(getString(R.string.go_pay), cartItemInfoJson.getItems().size() - disSelectedCount));

                    }
                    break;

                case CartInfoAdapter.CHANGE_PHOTO://更换照片
                    selectPhoto(msg.arg1);
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        newToast = new MyToast(this);
        //上传进度条
        dialog = new CustomProgressBarPop(this, findViewById(R.id.cart_activity_relativeLayout), CustomProgressBarPop.TYPE_UPLOAD);
        rtButton = (ImageView) findViewById(R.id.ret_relyt);
        rtButton.setOnClickListener(this);
        paymentButton = (Button) findViewById(R.id.button3_pm);
        paymentButton.setOnClickListener(this);
        totalTextView = (TextView) findViewById(R.id.textView3);
        currencyTextView = (TextView) findViewById(R.id.textView_currency);
        cartSelectAllImageView = (ImageView) findViewById(R.id.cartSelectAllImageView);
        cartSelectAllImageView.setOnClickListener(this);
        editTextView = (TextView) findViewById(R.id.cart_edit);
        editTextView.setOnClickListener(this);
        editTextView.setEnabled(false);
        cartPriceLinearLayout = (LinearLayout) findViewById(R.id.cartPriceLinearLayout);
        cartInfoList = new ArrayList<>();
        line = findViewById(R.id.line);
        bottomRelativeLayout = (RelativeLayout) findViewById(R.id.linearLayout1);
        netWorkOrNoCountView = (NoNetWorkOrNoCountView) findViewById(R.id.noNetWorkView);

        sPreferences = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
        userId = sPreferences.getString(Common.USERINFO_ID, "");
        currencyTextView.setText(sPreferences.getString(Common.CURRENCY, Common.DEFAULT_CURRENCY));
        customProgressDialog = CustomProgressDialog.show(this, getString(R.string.is_loading), false, null);
        API1.getCarts(handler);
        totalTextView.setText((int) totalPrice + "");
        listView = (ListView) findViewById(R.id.cartListView);
        cartAdapter = new CartInfoAdapter(this, sPreferences.getString(Common.CURRENCY, Common.DEFAULT_CURRENCY), cartInfoList, userId, mHandler);
        listView.addHeaderView(new XListViewHeader(this));
        listView.setAdapter(cartAdapter);
        listView.setHeaderDividersEnabled(true);
        listView.setFooterDividersEnabled(false);
        cartItemInfoJson = new CartItemInfoJson();
    }

    /**
     * 设置是否选中
     * 根据商品类型/图片数量
     *
     * @param cartItemInfoJson
     * @return
     */
    public CartItemInfoJson setIsSelect(CartItemInfoJson cartItemInfoJson) {
        if (cartItemInfoJson.getItems() != null && cartItemInfoJson.getItems().size() > 0) {
            for (CartItemInfo1 cartItemInfo : cartItemInfoJson.getItems()) {
                //设置商品类型
                if (cartItemInfo.getProductName().equals(Common.GOOD_NAME_PPP)) {
                    //ppp
                    cartItemInfo.setCartProductType(3);
                } else if (cartItemInfo.getProductName().equals(Common.GOOD_NAME_SINGLE_DIGITAL)) {
                    //pp
                    cartItemInfo.setCartProductType(2);
                } else {
                    //other goods
                    cartItemInfo.setCartProductType(1);
                }
                List<CartPhotosInfo1> cartPhotosInfoList = cartItemInfo.getEmbedPhotos();
                if (cartPhotosInfoList != null && cartPhotosInfoList.size() > 0) {
                    cartItemInfo.setHasPhoto(true);
                } else {
                    cartItemInfo.setHasPhoto(false);
                }
                if (cartItemInfo.getCartProductType() != 1) {//如果是虚拟商品，则不需要加图片
                    cartItemInfo.setIsFullPhotos(true);
                    cartItemInfo.setHasPhoto(true);
                } else if (cartPhotosInfoList.size() < cartItemInfo.getEmbedPhotosCount()) {//如果是正常商品，判断已经加的图品数量和需要数量是否一致
                    cartItemInfo.setIsFullPhotos(false);
                } else {
                    cartItemInfo.setIsFullPhotos(true);
                    cartItemInfo.setHasPhoto(true);
                }
                cartItemInfo.setIsSelect(true);
                cartItemInfo.setShowEdit(0);
            }
        }
        return cartItemInfoJson;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        Intent intent;
        switch (v.getId()) {
            case R.id.ret_relyt:
                finish();
                break;
            case R.id.button3_pm://支付提交按钮
                if (isEdit) {
                    //需要删除操作
                    if (cartInfoList.size() == 0) {
                        newToast.setTextAndShow(R.string.no_cart, Common.TOAST_SHORT_TIME);
                        return;
                    }
                    if (isDelete) {//说明正在删除中，直接返回
                        return;
                    } else {
                        isDelete = true;
                    }
                    //获取已经选中的item
                    deleteCartItemInfoList = new ArrayList<>();
                    for (int i = 0; i < cartInfoList.size(); i++) {
                        if (cartInfoList.get(i).getIsSelect()) {
                            deleteCartItemInfoList.add(cartInfoList.get(i));
                        }
                    }
                    if (deleteCartItemInfoList == null || deleteCartItemInfoList.size() == 0) {
                        newToast.setTextAndShow(R.string.select_cart, Common.TOAST_SHORT_TIME);
                        isDelete = false;
                        return;
                    }
                    //不支持中间项删除
                    //请求删除操作
                    API1.removeCartItems(deleteCartItemInfoList.get(0).getCartId(), handler);

                } else {//支付操作
                    ArrayList<CartItemInfo1> orderinfo = new ArrayList<CartItemInfo1>();
                    for (int i = 0; i < cartInfoList.size(); i++) {//查找键值对中select为true的值，并将它放入orderinfo2中
                        if (cartInfoList.get(i).getIsSelect()) {
                            orderinfo.add(cartInfoList.get(i));
                        }
                    }
                    System.out.println("order info count = " + orderinfo.size());
                    if (0 == cartInfoList.size()) {
                        System.out.println("cartinfolist = 0");
                        newToast.setTextAndShow(R.string.selectyourcart, Common.TOAST_SHORT_TIME);
                    } else if (0 == orderinfo.size()) {
                        System.out.println("orderinfo = 0");
                        newToast.setTextAndShow(R.string.selectyourcart, Common.TOAST_SHORT_TIME);
                    } else {
                        /**********判断是否有图片没有添加*********/
                        for (int i = 0; i < orderinfo.size(); i++) {
                            if (!orderinfo.get(i).getHasPhoto()) {
                                System.out.println("have no photo");
                                newToast.setTextAndShow(R.string.addphoto, Common.TOAST_SHORT_TIME);
                                return;
                            }
                        }
                        intent = new Intent(this, SubmitOrderActivity.class);
                        intent.putExtra("orderinfo", orderinfo);
                        startActivity(intent);
                        finish();
                    }
                }
                break;

            case R.id.cart_edit:
                for (int i = 0; i < cartInfoList.size(); i++) {
                    cartInfoList.get(i).setShowEdit(isEdit ? 0 : 1);
                }
                if (isEdit) {//取消编辑状态
                    cancelEdit();
                } else {//开始编辑
                    isEdit = true;
                    editTextView.setText(R.string.ok);
                    cartPriceLinearLayout.setVisibility(View.GONE);

                    if (cartInfoList == null || cartInfoList.size() == 0 || disSelectedCount == cartItemInfoJson.getItems().size()) {
                        paymentButton.setBackgroundResource(R.color.gray_light3);
                    } else {
                        paymentButton.setBackgroundResource(R.color.orange);
                    }
                    paymentButton.setText(R.string.delete);
                    rtButton.setVisibility(View.GONE);

                }
                cartAdapter.notifyDataSetChanged();
                break;

            case R.id.cartSelectAllImageView:
                totalPrice = 0;
                for (int i = 0; i < cartInfoList.size(); i++) {
                    cartInfoList.get(i).setIsSelect((disSelectedCount == 0) ? false : true);
                    totalPrice += cartInfoList.get(i).getPrice();
                }

                cartAdapter.notifyDataSetChanged();
                if (disSelectedCount == 0) {
                    disSelectedCount = cartItemInfoJson.getItems().size();
                    cartSelectAllImageView.setImageResource(R.drawable.cart_not_select);
                    totalPrice = 0;
                    paymentButton.setBackgroundResource(R.color.gray_light3);
                } else {
                    cartSelectAllImageView.setImageResource(R.drawable.cart_select);
                    if (isEdit) {

                        paymentButton.setBackgroundResource(R.color.orange);
                    } else {
                        paymentButton.setBackgroundResource(R.color.blue);

                    }
                    disSelectedCount = 0;
                }
                totalTextView.setText(totalPrice + "");
                if (!isEdit) {
                    paymentButton.setText(String.format(getString(R.string.go_pay), cartItemInfoJson.getItems().size() - disSelectedCount));
                }
                break;

            default:
                break;
        }
    }

    /**
     * 取消编辑
     */
    private void cancelEdit() {
        // TODO Auto-generated method stub
        isEdit = false;
        editTextView.setText(R.string.edit);
        cartPriceLinearLayout.setVisibility(View.VISIBLE);
        paymentButton.setText(String.format(getString(R.string.go_pay), cartItemInfoJson.getItems().size() - disSelectedCount));
        if (cartItemInfoJson.getItems().size() == disSelectedCount) {//没有选中
            paymentButton.setBackgroundResource(R.color.gray_light3);
        } else {

            paymentButton.setBackgroundResource(R.color.blue);
        }
        rtButton.setVisibility(View.VISIBLE);
    }

    /**
     * 展示无数据的页面
     */
    private void ShowNoNetOrNoCountView() {
        bottomRelativeLayout.setVisibility(View.INVISIBLE);
        netWorkOrNoCountView.setVisibility(View.VISIBLE);
        netWorkOrNoCountView.setResult(R.string.no_cart, R.string.want_to_buy, R.string.to_add_good, R.drawable.no_cart, handler, false);
        editTextView.setVisibility(View.INVISIBLE);
    }

    //选择照片
    private void selectPhoto(int requestCode) {
        Intent intent = new Intent(CartActivity.this, SelectPhotoActivity.class);
        intent.putExtra("activity", "cartactivity");
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 20) {//先要上传图片，上传完之后调用修改cart的api，如果返回ok，则刷新界面
            updatephotolist = data.getParcelableArrayListExtra("photopath");
            if (updatephotolist.get(0).onLine == 1) {//如果是选择的PP的照片
                JSONObject object = new JSONObject();
                try {
                    object.put("photoUrl", updatephotolist.get(0).photoThumbnail_512);
                    object.put("photoId", updatephotolist.get(0).photoId);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Message msg = handler.obtainMessage();
                msg.what = API.UPLOAD_PHOTO_SUCCESS;
                msg.arg1 = requestCode;
                msg.obj = object;
                handler.sendMessage(msg);
                dialog.show(0);
            } else {
                String photourl = updatephotolist.get(0).photoPathOrURL;
                // 需要上传选择的图片
                RequestParams params = new RequestParams();
                String tokenId = sPreferences.getString(Common.USERINFO_TOKENID, null);
                try {
                    params.put("file", new File(photourl), "application/octet-stream");
                    params.put(Common.USERINFO_TOKENID, tokenId);
                    API1.SetPhoto(params, handler, requestCode, dialog);
                    dialog.show(0);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 1.找到动态添加的imageview，并替换对应的图片
     * 2.更新orderinfo的arraylist信息
     * 3.修改数据库
     *
     * @param position
     * @param photoList
     */
    private void changephoto(int position, ArrayList<PhotoInfo> photoList) {
        List<CartPhotosInfo1> oriphoto = cartInfoList.get(position / 10).getEmbedPhotos();
        CartPhotosInfo1 cartPhotosInfo = new CartPhotosInfo1();
        cartPhotosInfo.setCartPhotoUrl(photoList.get(0).photoPathOrURL);
        oriphoto.set(position % 10, cartPhotosInfo);//替换图片
        System.out.println("重新选择的图片");
        CartItemInfo1 map = cartInfoList.get(position / 10);
        map.setEmbedPhotos(oriphoto);
        map.setHasPhoto(true);
        cartInfoList.set(position / 10, map);//替换列表信息
        cartAdapter.notifyDataSetChanged();
    }
}
