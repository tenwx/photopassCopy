package com.pictureair.photopass.activity;

import android.content.Intent;
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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.CartInfoAdapter;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.entity.CartItemInfoJson;
import com.pictureair.photopass.entity.CartPhotosInfo;
import com.pictureair.photopass.entity.GoodsInfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.http.rxhttp.RxSubscribe;
import com.pictureair.photopass.util.API2;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.JsonTools;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.widget.NoNetWorkOrNoCountView;
import com.pictureair.photopass.widget.PWToast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

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
    private TextView totalTextView, currencyTextView, discountPriceTv, discountCurrencyTv, tvCurrency;
    private ImageView cartSelectAllImageView;
    private TextView editTextView;
    private LinearLayout cartPriceLinearLayout, discountPriceLinearLayout;
    private RelativeLayout bottomRelativeLayout;
    private View line;

    private ArrayList<CartItemInfo> cartInfoList;// 订单list

    private ArrayList<PhotoInfo> updatephotolist;

    private float totalPrice = 0;//总价格
    private float discountPrice = 0;//优惠费用
    private boolean isEdit = false;
    private boolean isDelete = false;
    private int disSelectedCount = 0;//记录已经取消选中的个数

    private CartInfoAdapter cartAdapter;

    private String userId = "";
    private PWToast newToast;

    private NoNetWorkOrNoCountView netWorkOrNoCountView;
    private CartItemInfoJson cartItemInfoJson;//存放返回的数据
    private ArrayList<CartItemInfo> selectCartInfoList;//存放勾选的购物车
    private List<CartItemInfo> deleteCartItemInfoList;//存放删除的购物车

    private final Handler cartHandler = new CartHandler(this);


    private static class CartHandler extends Handler {
        private final WeakReference<CartActivity> mActivity;

        public CartHandler(CartActivity activity) {
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
        int currentCartCounts;
        switch (msg.what) {
            case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_RELOAD://noView的按钮响应重新加载点击事件
                //重新加载购物车数据
                PictureAirLog.v(TAG, "onclick with reload");
                showPWProgressDialog();
                getCarts(null);
                cartInfoList.clear();
                break;

            case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_NO_RELOAD://noView的按钮响应非重新加载的点击事件
                //去跳转到购物车
                PictureAirLog.v(TAG, "onclick with no reload");
                //需要删除页面，保证只剩下mainTab页面，
                AppManager.getInstance().killOtherActivity(MainTabActivity.class);
                //同时将mainTab切换到shop Tab
                ((MyApplication) getApplication()).setMainTabIndex(3);

                break;

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

                currentCartCounts = SPUtils.getInt(CartActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
                SPUtils.put(CartActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, currentCartCounts - 1);
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

                currentCartCounts = SPUtils.getInt(CartActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
                SPUtils.put(CartActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, currentCartCounts + 1);
                break;

            case CartInfoAdapter.SELECTED:// 选中item
                disSelectedCount--;
                updatePayStateUI();//更新底部支付栏UI
                if (!isEdit) {
                    //非编辑状态不需要请求优惠信息
                    getDiscountPrice();
                }
                break;
            case CartInfoAdapter.NOSELECTED:// 取消选中item
                disSelectedCount++;
                updatePayStateUI();//更新底部支付栏UI
                if (!isEdit) {
                    getDiscountPrice();
                }
                break;

            case CartInfoAdapter.CHANGE_PHOTO://更换照片
                selectPhoto(msg.arg1);
                break;

            default:
                break;
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        newToast = new PWToast(this);
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
        discountPriceLinearLayout = (LinearLayout) findViewById(R.id.discountPriceLinearLayout);
        discountPriceTv = (TextView) findViewById(R.id.discount_price_tv);
        discountCurrencyTv = (TextView) findViewById(R.id.discount_currency_tv);

        cartInfoList = new ArrayList<>();
        line = findViewById(R.id.line);
        bottomRelativeLayout = (RelativeLayout) findViewById(R.id.linearLayout1);
        netWorkOrNoCountView = (NoNetWorkOrNoCountView) findViewById(R.id.noNetWorkView);

        String currency = Common.DEFAULT_CURRENCY;//SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CURRENCY, Common.DEFAULT_CURRENCY);
        userId = SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ID, "");
        currencyTextView.setText(currency);
        discountCurrencyTv.setText("-" + currency);
        showPWProgressDialog();
        getCarts(null);
        totalTextView.setText((int) totalPrice + "");
        listView = (ListView) findViewById(R.id.cartListView);
        cartAdapter = new CartInfoAdapter(this, currency, cartInfoList, userId, cartHandler);
        listView.setAdapter(cartAdapter);
        cartItemInfoJson = new CartItemInfoJson();
        selectCartInfoList = new ArrayList<>();
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
            for (CartItemInfo cartItemInfo : cartItemInfoJson.getItems()) {
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
                List<CartPhotosInfo> cartPhotosInfoList = cartItemInfo.getEmbedPhotos();
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


    /**
     * 更新支付底部栏UI
     */
    public void updatePayStateUI() {
        if (disSelectedCount == 0) {
            //全部选中
            cartSelectAllImageView.setImageResource(R.drawable.cart_select);
            paymentButton.setBackgroundResource(R.color.pp_blue);
            if (isEdit) {
                //编辑
                discountPriceLinearLayout.setVisibility(View.GONE);
            }
        } else if (cartItemInfoJson.getItems().size() == disSelectedCount) {
            //全部取消
            discountPriceLinearLayout.setVisibility(View.GONE);
            paymentButton.setBackgroundResource(R.color.gray_light3);
        } else {
            //部分选中
            cartSelectAllImageView.setImageResource(R.drawable.cart_not_select);
            paymentButton.setBackgroundResource(R.color.pp_blue);
            if (isEdit) {
                discountPriceLinearLayout.setVisibility(View.GONE);
            }
        }
        if (isEdit) {
            paymentButton.setText(R.string.delete);
        } else {
            paymentButton.setText(String.format(getString(R.string.go_pay), cartItemInfoJson.getItems().size() - disSelectedCount));
        }
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
                //检查网络
                if (AppUtil.getNetWorkType(MyApplication.getInstance()) == AppUtil.NETWORKTYPE_INVALID) {
                    newToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
                    return;
                }
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

                    showPWProgressDialog();

                    PictureAirLog.v(TAG, "removeCartItems delete size: " + deleteCartItemInfoList.size());
                    JSONArray jsonArray = new JSONArray(deleteCartItemInfoList.size());
                    //构建数组
                    for (int i = 0; i < deleteCartItemInfoList.size(); i++) {
                        jsonArray.add(deleteCartItemInfoList.get(i).getCartId());
                    }
                    PictureAirLog.v(TAG, "removeCartItems deletes length: " + jsonArray.size());
                    //请求删除操作
                    removeCartItems(jsonArray);

                } else {//支付操作
                    //获取选中的购物项
                    selectCartInfoList.clear();
                    for (int i = 0; i < cartInfoList.size(); i++) {//查找键值对中select为true的值，并将它放入orderinfo2中
                        if (cartInfoList.get(i).getIsSelect()) {
                            selectCartInfoList.add(cartInfoList.get(i));
                        }
                    }
                    //判断购物车是否为空
                    if (cartInfoList == null || cartInfoList.size() < 0) {
                        ShowNoNetOrNoCountView();
                        return;
                    }
                    if (selectCartInfoList.size() > 0) {
                        PictureAirLog.v(TAG, "selectCartInfoList size: " + selectCartInfoList.size());
                        //判断是否有图片没有添加
                        for (CartItemInfo cartItemInfo : selectCartInfoList) {
                            //PP+不需要图片
                            if (cartItemInfo.getCartProductType() != 3) {
                                if (cartItemInfo.getEmbedPhotos() == null || cartItemInfo.getEmbedPhotos().size() <= 0) {
                                    PictureAirLog.v(TAG, "no photo");
                                    newToast.setTextAndShow(R.string.addphoto, Common.TOAST_SHORT_TIME);
                                    return;
                                } else {
                                    for (CartPhotosInfo cartPhotosInfo : cartItemInfo.getEmbedPhotos()) {
                                        if (cartPhotosInfo == null || cartPhotosInfo.getPhotoId() == null || cartPhotosInfo.getPhotoUrl() == null) {
                                            PictureAirLog.v(TAG, "no photo");
                                            newToast.setTextAndShow(R.string.addphoto, Common.TOAST_SHORT_TIME);
                                            return;
                                        }

                                    }
                                }
                            }
                        }
                        intent = new Intent(this, SubmitOrderActivity.class);
                        intent.putExtra("orderinfo", selectCartInfoList);
                        intent.putExtra("discountPrice", discountPrice);
                        PictureAirLog.out("discount=========" + discountPrice);
                        startActivity(intent);
                        finish();
                    } else {
                        PictureAirLog.v(TAG, "selectCartInfoList = 0");
                        newToast.setTextAndShow(R.string.selectyourcart, Common.TOAST_SHORT_TIME);
                    }
                }
                break;

            case R.id.cart_edit:
                if (null != cartInfoList) {
                    for (int i = 0; i < cartInfoList.size(); i++) {
                        cartInfoList.get(i).setShowEdit(isEdit ? 0 : 1);
                    }
                }
                if (isEdit) {//取消编辑状态
                    cancelEdit();
                } else {//开始编辑
                    isEdit = true;
                    editTextView.setText(R.string.ok);
                    cartPriceLinearLayout.setVisibility(View.GONE);
                    discountPriceLinearLayout.setVisibility(View.GONE);

                    if (cartInfoList == null || cartInfoList.size() == 0 || disSelectedCount == cartItemInfoJson.getItems().size()) {
                        paymentButton.setBackgroundResource(R.color.gray_light3);
                    } else {
                        paymentButton.setBackgroundResource(R.color.pp_blue);
                    }
                    paymentButton.setText(R.string.delete);
//                    rtButton.setVisibility(View.INVISIBLE);
                }
                cartAdapter.notifyDataSetChanged();
                break;

            case R.id.cartSelectAllImageView:
                totalPrice = 0;
                for (int i = 0; i < cartInfoList.size(); i++) {
                    cartInfoList.get(i).setIsSelect(disSelectedCount != 0);
                    totalPrice += cartInfoList.get(i).getPrice();
                }
                cartAdapter.notifyDataSetChanged();

                if (disSelectedCount == 0) {
                    //全部取消
                    disSelectedCount = cartItemInfoJson.getItems().size();
                    cartSelectAllImageView.setImageResource(R.drawable.cart_not_select);
                    totalPrice = 0;
                    totalTextView.setText((int) totalPrice + "");
                    discountPriceLinearLayout.setVisibility(View.GONE);
                    if (isEdit) {
                        paymentButton.setBackgroundResource(R.color.gray_light3);
                        paymentButton.setText(R.string.delete);
                    } else {
                        paymentButton.setBackgroundResource(R.color.gray_light3);
                        paymentButton.setText(String.format(getString(R.string.go_pay), cartItemInfoJson.getItems().size() - disSelectedCount));
                    }

                } else {
                    //全部选中
                    cartSelectAllImageView.setImageResource(R.drawable.cart_select);
                    disSelectedCount = 0;
                    paymentButton.setBackgroundResource(R.color.pp_blue);
                    if (isEdit) {
                        paymentButton.setText(R.string.delete);
                    } else {
                        //非编辑状态不需要请求优惠信息
                        getDiscountPrice();
                    }
                }

                break;

            default:
                break;
        }

    }

    /**
     * 请求优惠信息
     */
    public void getDiscountPrice() {
        if (cartInfoList == null) {
            return;
        }
        selectCartInfoList.clear();
        for (int i = 0; i < cartInfoList.size(); i++) {
            if (cartInfoList.get(i).getIsSelect()) {
                selectCartInfoList.add(cartInfoList.get(i));
            }
        }
        JSONArray jsonArray = new JSONArray(selectCartInfoList.size());
        //构建数组
        for (int i = 0; i < selectCartInfoList.size(); i++) {
            jsonArray.add(selectCartInfoList.get(i).getCartId());
        }
        PictureAirLog.out("jsonArray" + jsonArray);
        showPWProgressDialog();
        getCarts(jsonArray);
    }

    /**
     * 删除购物车
     * @param jsonArray
     */
    private void removeCartItems(JSONArray jsonArray){
        API2.removeCartItems(jsonArray)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<JSONObject>bindToLifecycle())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        isDelete = false;
                        //删除删除项
                        cartInfoList.removeAll(deleteCartItemInfoList);
                        cartItemInfoJson.setItems(cartInfoList);
                        //获取删除的商品数
                        int deleteCount = 0;
                        for (CartItemInfo cartItemInfo : deleteCartItemInfoList) {
                            deleteCount += cartItemInfo.getQty();
                        }
                        //清空数据
                        deleteCartItemInfoList.clear();
                        //更新界面
                        cartAdapter.refresh(cartInfoList);
                        //保存购物车数量
                        int currentCartCounts = SPUtils.getInt(CartActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
                        SPUtils.put(CartActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, currentCartCounts - deleteCount);
                        paymentButton.setBackgroundResource(R.color.gray_light3);
                        if (cartInfoList.size() == 0) {
                            cancelEdit();
                            ShowNoNetOrNoCountView();

                        } else {
                            dismissPWProgressDialog();

                        }
                    }

                    @Override
                    public void _onError(int status) {
                        dismissPWProgressDialog();

                        isDelete = false;
                        newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                        editTextView.setEnabled(true);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    /**
     * 获取购物车
     * @param jsonArray
     */
    private void getCarts(final JSONArray jsonArray) {
        API2.getCarts(jsonArray)
                .map(new Func1<JSONObject, CartItemInfoJson>() {
                    @Override
                    public CartItemInfoJson call(JSONObject jsonObject) {
                        PictureAirLog.v(TAG, "GET_CART_SUCCESS obg: " + jsonObject.toString());
                        return JsonTools.parseObject(jsonObject, CartItemInfoJson.class);//CartItemInfoJson.getString()
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<CartItemInfoJson>bindToLifecycle())
                .subscribe(new RxSubscribe<CartItemInfoJson>() {
                    @Override
                    public void _onNext(CartItemInfoJson cartItemInfo) {
                        if (cartItemInfo != null && cartItemInfo.getItems() != null && cartItemInfo.getItems().size() > 0) {
                            PictureAirLog.v(TAG, "GET_CART_SUCCESS cart size: " + cartItemInfo.getItems().size());
                            //初始化请求返回
                            if (jsonArray == null) {
                                //初始化
                                cartItemInfoJson = cartItemInfo;
                                cartInfoList.addAll(cartItemInfoJson.getItems());
                                selectCartInfoList.addAll(cartItemInfoJson.getItems());//默认第一次全部选中
                                cartItemInfoJson = setIsSelect(cartItemInfoJson);//更新每个item是否选中
                                editTextView.setVisibility(View.VISIBLE);
                                editTextView.setEnabled(true);
                                netWorkOrNoCountView.setVisibility(View.GONE);
                                bottomRelativeLayout.setVisibility(View.VISIBLE);
                                listView.setVisibility(View.VISIBLE);
                                line.setVisibility(View.VISIBLE);

                                //保存购物车数量
                                SPUtils.put(CartActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, cartItemInfoJson.getTotalCount());
                            } else {
                                cartItemInfoJson.setPreferentialPrice(cartItemInfo.getPreferentialPrice());
                                cartItemInfoJson.setTotalPrice(cartItemInfo.getTotalPrice());
                            }
                            discountPrice = cartItemInfoJson.getPreferentialPrice();
                            discountPriceTv.setText((int) discountPrice + "");

                            paymentButton.setVisibility(View.VISIBLE);
                            cartPriceLinearLayout.setVisibility(View.VISIBLE);

                            if (disSelectedCount == cartItemInfoJson.getItems().size()) {
                                totalTextView.setText(0 + "");
                            } else {
                                totalTextView.setText(cartItemInfoJson.getTotalPrice() + "");
                            }

                            if (isEdit) {
                                paymentButton.setBackgroundResource(R.color.pp_blue);
                            } else {

                                if (disSelectedCount < cartItemInfoJson.getItems().size()) {
                                    //显示价格
                                    discountPriceLinearLayout.setVisibility(discountPrice == 0 ? View.GONE : View.VISIBLE);
                                    paymentButton.setBackgroundResource(R.color.pp_blue);
                                } else {
                                    paymentButton.setBackgroundResource(R.color.gray_light3);
                                }
                                //设置数量
                                paymentButton.setText(String.format(getString(R.string.go_pay), cartItemInfoJson.getItems().size() - disSelectedCount));
                            }

                        } else {
                            //保存购物车数量
                            SPUtils.put(CartActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
                            ShowNoNetOrNoCountView();
                        }
                        dismissPWProgressDialog();
                    }

                    @Override
                    public void _onError(int status) {
                        dismissPWProgressDialog();
                        netWorkOrNoCountView.setVisibility(View.VISIBLE);
                        netWorkOrNoCountView.setResult(R.string.no_network, R.string.click_button_reload, R.string.reload, R.drawable.no_network, cartHandler, true);
                        bottomRelativeLayout.setVisibility(View.INVISIBLE);
                        listView.setVisibility(View.INVISIBLE);
                        line.setVisibility(View.INVISIBLE);
                        editTextView.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }
    /**
     * 取消编辑
     */
    private void cancelEdit() {
        // TODO Auto-generated method stub
        isEdit = false;
        editTextView.setText(R.string.edit);
        rtButton.setVisibility(View.VISIBLE);
        // 请求优惠信息
        getDiscountPrice();
    }

    /**
     * 展示无数据的页面
     */
    private void ShowNoNetOrNoCountView() {
        bottomRelativeLayout.setVisibility(View.INVISIBLE);
        netWorkOrNoCountView.setVisibility(View.VISIBLE);
        netWorkOrNoCountView.setResult(R.string.no_cart, 0, R.string.to_add_good, R.drawable.no_cart, cartHandler, false);
        editTextView.setVisibility(View.INVISIBLE);
    }

    //选择照片
    private void selectPhoto(int requestCode) {
        if (cartInfoList == null || cartInfoList.size() <= 0) {
            return;
        }
        Intent intent = new Intent(CartActivity.this, SelectPhotoActivity.class);
        GoodsInfo goodsInfo = new GoodsInfo();
        goodsInfo.setName(cartInfoList.get(requestCode / 10).getProductName());
        goodsInfo.setEmbedPhotosCount(cartInfoList.get(requestCode / 10).getEmbedPhotosCount());
        goodsInfo.setIsAllowBuy(cartInfoList.get(requestCode / 10).getIsAllowBuy());
        intent.putExtra("goodsInfo", goodsInfo);
        intent.putExtra("activity", "cartactivity");
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 20) {//先要上传图片，上传完之后调用修改cart的api，如果返回ok，则刷新界面
            updatephotolist = data.getParcelableArrayListExtra("photopath");
            if (updatephotolist == null || updatephotolist.size() <= 0) {
                return;
            }
            PictureAirLog.v(TAG, "onActivityResult requestCode: " + requestCode);
            PictureAirLog.v(TAG, "onActivityResult photoPathOrURL size : " + updatephotolist.size());
            JSONArray jsonArray = new JSONArray();
            for (PhotoInfo info : updatephotolist) {
                PictureAirLog.v(TAG, "onActivityResult info url: " + info.getPhotoThumbnail_128());
                if (info.getIsOnLine() == 1) {//如果是选择的PP的照片
                    JSONObject object = new JSONObject();
                    object.put("photoId", info.getPhotoId());
                    jsonArray.add(object);
                } else {
                    //目前合成图片均为pp上的图片
                }
            }
            changeCartPhotos(requestCode, jsonArray);
        }
    }

    /**
     * 修改购物项中的照片
     * @param position
     * @param jsonArray
     */
    private void changeCartPhotos(final int position, JSONArray jsonArray) {
        showPWProgressDialog();
        PictureAirLog.v(TAG, "embedPhotos: " + jsonArray);
        CartItemInfo cartItemInfo = cartInfoList.get(position / 10);
        API2.modifyCart(cartItemInfo.getCartId(), cartItemInfo.getGoodsKey(), cartItemInfo.getQty(), jsonArray)
                .map(new Func1<JSONObject, List<CartPhotosInfo>>() {
                    @Override
                    public List<CartPhotosInfo> call(JSONObject jsonObject) {
                        List<CartPhotosInfo> oriphoto = new ArrayList<>();//获取指定购物车的图片集合
                        for (PhotoInfo photoInfo : updatephotolist) {
                            //构建购物车图片对象
                            PictureAirLog.v(TAG, "update url: " + photoInfo.getPhotoOriginalURL());
                            CartPhotosInfo cartPhotosInfo = new CartPhotosInfo();
                            cartPhotosInfo.setPhotoUrl(photoInfo.getPhotoThumbnail_128());//缩略图
                            cartPhotosInfo.setPhotoId(photoInfo.getPhotoId());
                            cartPhotosInfo.setIsEncrypted(photoInfo.getIsEnImage());
                            oriphoto.add(cartPhotosInfo);
                        }
                        return oriphoto;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<List<CartPhotosInfo>>bindToLifecycle())
                .subscribe(new RxSubscribe<List<CartPhotosInfo>>() {
                    @Override
                    public void _onNext(List<CartPhotosInfo> list) {
                        PictureAirLog.v(TAG, "MODIFY_CART_SUCCESS: " + "uodate cart");
                        dismissPWProgressDialog();

                        /**
                         * 1.找到动态添加的imageview，并替换对应的图片
                         * 2.更新orderinfo的arraylist信息
                         * 3.修改数据库
                         */
                        //获取指定购物车项
                        cartInfoList.get(position / 10).setEmbedPhotos(list);
                        cartInfoList.get(position / 10).setHasPhoto(true);
                        cartAdapter.refresh(cartInfoList);
                    }

                    @Override
                    public void _onError(int status) {
                        dismissPWProgressDialog();
                        newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cartHandler.removeCallbacksAndMessages(null);
    }
}
