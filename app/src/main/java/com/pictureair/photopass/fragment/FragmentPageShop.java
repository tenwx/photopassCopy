package com.pictureair.photopass.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.BaseFragment;
import com.pictureair.photopass.activity.CartActivity;
import com.pictureair.photopass.activity.DetailProductActivity;
import com.pictureair.photopass.activity.PPPDetailProductActivity;
import com.pictureair.photopass.adapter.ShopGoodListViewAdapter;
import com.pictureair.photopass.entity.AddressJson;
import com.pictureair.photopass.entity.GoodsInfo1;
import com.pictureair.photopass.entity.GoodsInfoJson;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.JsonTools;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.UniversalImageLoadTool;
import cn.smssdk.gui.CustomProgressDialog;
import com.pictureair.photopass.widget.MyToast;
import com.pictureair.photopass.widget.NoNetWorkOrNoCountView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * shop类
 * 显示全部商品，调用全部商品API
 *
 * @author bauer_bao
 */
public class FragmentPageShop extends BaseFragment implements OnClickListener {
    private static final String TAG = "FragmentPageShop";
    //申明控件
    private ImageView shoppingBag;
    private TextView cartCountTextView;
    private ListView xListView;
    private NoNetWorkOrNoCountView noNetWorkOrNoCountView;
    private CustomProgressDialog customProgressDialog;

    //申明变量
    private int cartCount = 0; // 记录数据库中有几条记录
    private String currency = "";//货币种类

    //申明实例类
    private List<GoodsInfo1> allGoodsList;//全部商品
    private ShopGoodListViewAdapter shopGoodListViewAdapter;
    private int i;

    //申明其他
    private SharedPreferences sharedPreferences;
    private MyToast newToast;


    private final Handler fragmentPageShopHandler = new FragmentPageShopHandler(this);

    private static class FragmentPageShopHandler extends Handler{
        private final WeakReference<FragmentPageShop> mActivity;

        public FragmentPageShopHandler(FragmentPageShop activity){
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
            case API1.GET_GOODS_SUCCESS://成功获取商品
                customProgressDialog.dismiss();
                allGoodsList.clear();
                PictureAirLog.v(TAG, "GET_GOODS_SUCCESS");
                GoodsInfoJson goodsInfoJson = JsonTools.parseObject(msg.obj.toString(), GoodsInfoJson.class);//GoodsInfoJson.getString()
                if (goodsInfoJson != null && goodsInfoJson.getGoods().size() > 0) {
                    allGoodsList = goodsInfoJson.getGoods();
                    PictureAirLog.v(TAG, "goods size: " + allGoodsList.size());
                    noNetWorkOrNoCountView.setVisibility(View.GONE);
                    //更新界面
                    shopGoodListViewAdapter.refresh(allGoodsList);
                    //将数据保存到缓存中
                    if (ACache.get(MyApplication.getInstance()).getAsString(Common.ALL_GOODS) == null || ACache.get(MyApplication.getInstance()).getAsString(Common.ALL_GOODS).equals("")) {
                        ACache.get(MyApplication.getInstance()).put(Common.ALL_GOODS, msg.obj.toString(), ACache.GOODS_ADDRESS_ACACHE_TIME);
                    }
                }

                //获取收货地址列表
                String addressByACache = ACache.get(MyApplication.getInstance()).getAsString(Common.ACACHE_ADDRESS);
                PictureAirLog.v(TAG, "initData: addressByACache: " + addressByACache);
                if (addressByACache == null || addressByACache.equals("")) {
                    API1.getOutlets(fragmentPageShopHandler);
                }
                break;

            case API1.GET_GOODS_FAILED://获取商品失败
                //显示重新加载界面
                customProgressDialog.dismiss();
                showNetWorkView();
                break;

            case API1.GET_OUTLET_ID_SUCCESS:
                //获取自提地址成功
                customProgressDialog.dismiss();
                AddressJson addressJson = JsonTools.parseObject((JSONObject) msg.obj, AddressJson.class);
                if (addressJson != null && addressJson.getOutlets().size() > 0) {
                    //存入缓存
                    if (ACache.get(MyApplication.getInstance()).getAsString(Common.ACACHE_ADDRESS) == null || ACache.get(MyApplication.getInstance()).getAsString(Common.ACACHE_ADDRESS).equals("")) {
                        ACache.get(MyApplication.getInstance()).put(Common.ACACHE_ADDRESS, msg.obj.toString(), ACache.GOODS_ADDRESS_ACACHE_TIME);
                    }
                }
                break;

            case API1.GET_OUTLET_ID_FAILED:
                //获取自提地址失败
                customProgressDialog.dismiss();
                break;

            case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_RELOAD://noView的按钮响应重新加载点击事件
                //重新加载购物车数据
                if (AppUtil.getNetWorkType(MyApplication.getInstance()) == 0) {
                    newToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
                    return;
                }
                PictureAirLog.v(TAG, "onclick with reload");
                customProgressDialog = CustomProgressDialog.show(getActivity(), getString(R.string.is_loading), false, null);
                //重新加载数据
                initData();
                break;

            default:
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop, null);
        newToast = new MyToast(MyApplication.getInstance());
        //找控件
        shoppingBag = (ImageView) view.findViewById(R.id.frag3_cart);
        cartCountTextView = (TextView) view.findViewById(R.id.textview_cart_count);
        xListView = (ListView) view.findViewById(R.id.shopListView);
        noNetWorkOrNoCountView = (NoNetWorkOrNoCountView) view.findViewById(R.id.shopNoNetWorkView);

        //初始化数据
        sharedPreferences = getActivity().getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
        cartCount = sharedPreferences.getInt(Common.CART_COUNT, 0);//获取购物车数量
        currency = sharedPreferences.getString(Common.CURRENCY, Common.DEFAULT_CURRENCY);//获取币种
        //设置购物车数量
        if (cartCount <= 0) {
            cartCountTextView.setVisibility(View.INVISIBLE);
        } else {
            cartCountTextView.setVisibility(View.VISIBLE);
            cartCountTextView.setText(cartCount + "");
        }
        allGoodsList = new ArrayList<>();//初始化商品列表
        customProgressDialog = CustomProgressDialog.show(getActivity(), getActivity().getString(R.string.is_loading), false, null);
        shopGoodListViewAdapter = new ShopGoodListViewAdapter(allGoodsList, getActivity(), currency);
        xListView.setAdapter(shopGoodListViewAdapter);
        //绑定监听
        shoppingBag.setOnClickListener(this);
        cartCountTextView.setOnClickListener(this);
        xListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent;
                if (allGoodsList == null || allGoodsList.get(position) == null) {
                    return;
                }
                //根据类型判断 为0,虚拟商品（PP+、Digital Photo）
                if (allGoodsList.get(position).getEntityType() == 0 && allGoodsList.get(position).getName().equals(Common.GOOD_NAME_PPP)) {
                    intent = new Intent(getActivity(), PPPDetailProductActivity.class);
                    intent.putExtra("goods", allGoodsList.get(position));
                } else {
                    intent = new Intent(getActivity(), DetailProductActivity.class);
                    //从第二张开始显示
                    intent.putExtra("goods", allGoodsList.get(position));
                }
                FragmentPageShop.this.startActivity(intent);
            }
        });
        xListView.setOnScrollListener(new PauseOnScrollListener(UniversalImageLoadTool.getImageLoader(), true, true));
        initData();//初始化数据
        return view;
    }

    /**
     * 初始化数据
     */
    public void initData() {
        //从缓层中获取数据
        String goodsByACache = ACache.get(getActivity()).getAsString(Common.ALL_GOODS);
        PictureAirLog.v(TAG, "initData: goodsByACache: " + goodsByACache);
        if (goodsByACache != null && !goodsByACache.equals("")) {
            fragmentPageShopHandler.obtainMessage(API1.GET_GOODS_SUCCESS, goodsByACache).sendToTarget();
        } else {
            //从网络获取商品,先检查网络
            if (AppUtil.getNetWorkType(MyApplication.getInstance()) != 0) {
                API1.getGoods(fragmentPageShopHandler);
            } else {
                showNetWorkView();
            }
        }

    }

    /**
     * 重新加载View
     */
    public void showNetWorkView() {
        customProgressDialog.dismiss();
        noNetWorkOrNoCountView.setVisibility(View.VISIBLE);
        noNetWorkOrNoCountView.setResult(R.string.no_network, R.string.click_button_reload, R.string.reload, R.drawable.no_network, fragmentPageShopHandler, true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.textview_cart_count:
            case R.id.frag3_cart:
                if (AppUtil.getNetWorkType(MyApplication.getInstance()) == 0) {
                    newToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
                    return;
                }
                Intent intent = new Intent(getActivity(), CartActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        cartCount = sharedPreferences.getInt(Common.CART_COUNT, 0);
        if (cartCount > 0) {
            cartCountTextView.setVisibility(View.VISIBLE);
            cartCountTextView.setText(cartCount + "");
        } else {
            cartCountTextView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentPageShopHandler.removeCallbacksAndMessages(null);
        if (customProgressDialog != null && customProgressDialog.isShowing()) {
            customProgressDialog.dismiss();
        }
    }
}