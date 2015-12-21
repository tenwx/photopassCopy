package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.OrderViewPagerAdapter;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.entity.OrderInfo;
import com.pictureair.photopass.util.API;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.JsonUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.MyToast;
import com.pictureair.photopass.widget.NoNetWorkOrNoCountView;

import java.util.ArrayList;

/**
 * 订单页面，分三类，Payment，Delivery，All order
 *
 * @author bauer_bao
 */
public class OrderActivity extends BaseActivity implements OnClickListener {

    private ViewPager viewPager;
    private OrderViewPagerAdapter orderAdapter;
    private ArrayList<View> listViews;
    private ImageView cursorImageView;//动画图片
    private TextView paymentOrderTextView, deliveryOrderTextView, allOrderTextView;//选项卡
    private ImageView backLayout;

    //group列表信息
    private ArrayList<OrderInfo> paymentOrderArrayList;
    private ArrayList<OrderInfo> deliveryOrderArrayList;
    private ArrayList<OrderInfo> allOrderArrayList;
    private ArrayList<OrderInfo> downOrderArrayList;

    private OrderInfo orderInfo;
    //child列表信息
    private ArrayList<ArrayList<CartItemInfo>> paymentOrderChildArrayList;
    private ArrayList<ArrayList<CartItemInfo>> deliveryOrderChildArrayList;
    private ArrayList<ArrayList<CartItemInfo>> allOrderChildArrayList;
    private ArrayList<ArrayList<CartItemInfo>> downOrderChildArrayList;
    private ArrayList<CartItemInfo> cartItemInfo;

    private SharedPreferences sharedPreferences;
    private int currentIndex = 0;//viewpager当前编号
    private int screenW;//屏幕宽度

    private NoNetWorkOrNoCountView netWorkOrNoCountView;

    private static String TAG = "OrderActivity";

    private CustomProgressDialog customProgressDialog;
    private MyToast myToast;

    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case API1.GET_ORDER_SUCCESS:
                    Log.d(TAG, "get success----");
                    viewPager.setVisibility(View.VISIBLE);
                    netWorkOrNoCountView.setVisibility(View.INVISIBLE);
                    customProgressDialog.dismiss();
                    paymentOrderArrayList.clear();
                    paymentOrderChildArrayList.clear();
                    deliveryOrderArrayList.clear();
                    deliveryOrderChildArrayList.clear();
                    allOrderArrayList.clear();
                    allOrderChildArrayList.clear();
                    downOrderArrayList.clear();
                    downOrderChildArrayList.clear();
                    //解析数据
                    JSONObject resultJsonObject = (JSONObject) msg.obj;
                    JSONArray allOrdersArray = resultJsonObject.getJSONArray("orders");//得到所有的订单信息
                    for (int i = 0; i < allOrdersArray.size(); i++) {
                        JSONObject orderJsonObject = allOrdersArray.getJSONObject(i);//得到单个订单信息
                        orderInfo = JsonUtil.getOrderGroupInfo(orderJsonObject);//获取group信息
                        cartItemInfo = JsonUtil.getOrderChildInfo(orderJsonObject);//获取child信息
                        PictureAirLog.v(TAG, "child size = " + cartItemInfo.size());

                        if (orderInfo.orderStatus == 1) {//1等待买家付款
                            paymentOrderArrayList.add(orderInfo);
                            paymentOrderChildArrayList.add(cartItemInfo);
                        } else if (orderInfo.orderStatus == 2 || orderInfo.orderStatus == 3) {//2买家已付款（等待卖家发货），3卖家已发货（等待买家确认）
                            deliveryOrderArrayList.add(orderInfo);
                            deliveryOrderChildArrayList.add(cartItemInfo);
                        } else if (orderInfo.orderStatus == 4 || orderInfo.orderStatus == 5) {
                            downOrderArrayList.add(orderInfo);
                            downOrderChildArrayList.add(cartItemInfo);
                        }
                        allOrderArrayList.add(orderInfo);
                        allOrderChildArrayList.add(cartItemInfo);
                    }
                    orderAdapter = new OrderViewPagerAdapter(OrderActivity.this, listViews, paymentOrderArrayList, deliveryOrderArrayList, downOrderArrayList,
                            paymentOrderChildArrayList, deliveryOrderChildArrayList, downOrderChildArrayList, sharedPreferences.getString(Common.CURRENCY, Common.DEFAULT_CURRENCY));
                    viewPager.setAdapter(orderAdapter);
                    if (getIntent().getStringExtra("flag") != null) {
                        viewPager.setCurrentItem(1);
                    } else {
                        viewPager.setCurrentItem(0);

                    }
                    orderAdapter.expandGropu(0);//因为异步回调，所以第一次需要在此处设置展开

                    if (allOrderArrayList.size() == 0) {
                        customProgressDialog.dismiss();
                    }
                    break;

                case API1.GET_ORDER_FAILED:
//				toast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
                    customProgressDialog.dismiss();
                    netWorkOrNoCountView.setVisibility(View.VISIBLE);
                    netWorkOrNoCountView.setResult(R.string.no_network, R.string.click_button_reload, R.string.reload, R.drawable.no_network, handler, true);
                    viewPager.setVisibility(View.INVISIBLE);
                    break;
                case API.DELETE_ORDER_SUCCESS:
//				int deletePosition = msg.arg1;
                    allOrderArrayList.remove(0);
                    allOrderChildArrayList.remove(0);

                    deliveryOrderArrayList.remove(0);
                    deliveryOrderChildArrayList.remove(0);

                    orderAdapter.notifyDataSetChanged();
                    break;

                case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_RELOAD://noView的按钮响应重新加载点击事件
                    //重新加载购物车数据
                    if (AppUtil.getNetWorkType(MyApplication.getInstance()) == 0) {
                        myToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
                        return;
                    }
                    customProgressDialog = CustomProgressDialog.show(OrderActivity.this, getString(R.string.is_loading), false, null);
                    customProgressDialog.show();
                    API1.getOrderInfo(handler);
                    break;

                case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_NO_RELOAD://noView的按钮响应非重新加载的点击事件
                    //去跳转到商品页面
                    //需要删除页面，保证只剩下mainTab页面，
                    AppManager.getInstance().killOtherActivity(MainTabActivity.class);
                    //同时将mainTab切换到shop Tab
                    MainTabActivity.changeToShopTab = true;

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
        setContentView(R.layout.activity_order);
        initView();
    }

    //初始化
    private void initView() {
        sharedPreferences = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
        //获取订单接口
        // 显示进度条。
        customProgressDialog = CustomProgressDialog.show(OrderActivity.this, getString(R.string.is_loading), false, null);
        API1.getOrderInfo(handler);

        netWorkOrNoCountView = (NoNetWorkOrNoCountView) findViewById(R.id.nonetwork_view);

        paymentOrderTextView = (TextView) findViewById(R.id.order_payment);
        deliveryOrderTextView = (TextView) findViewById(R.id.order_delivery);
        allOrderTextView = (TextView) findViewById(R.id.order_all);
        viewPager = (ViewPager) findViewById(R.id.order_vPager);
        cursorImageView = (ImageView) findViewById(R.id.cursor);
        backLayout = (ImageView) findViewById(R.id.order_return);

        screenW = ScreenUtil.getScreenWidth(this);// 获取分辨率宽度
        Matrix matrix = new Matrix();
        matrix.postTranslate(0, 0);
        cursorImageView.setImageMatrix(matrix);// 设置动画初始位置

        listViews = new ArrayList<View>();
        //初始化expandablelistview需要的数据
        paymentOrderArrayList = new ArrayList<OrderInfo>();
        deliveryOrderArrayList = new ArrayList<OrderInfo>();
        allOrderArrayList = new ArrayList<OrderInfo>();
        downOrderArrayList = new ArrayList<OrderInfo>();
        paymentOrderChildArrayList = new ArrayList<ArrayList<CartItemInfo>>();
        deliveryOrderChildArrayList = new ArrayList<ArrayList<CartItemInfo>>();
        allOrderChildArrayList = new ArrayList<ArrayList<CartItemInfo>>();
        downOrderChildArrayList = new ArrayList<ArrayList<CartItemInfo>>();

        LayoutInflater mInflater = getLayoutInflater();
        listViews.add(mInflater.inflate(R.layout.order_list, null));
        listViews.add(mInflater.inflate(R.layout.order_list, null));
        listViews.add(mInflater.inflate(R.layout.order_list, null));

        paymentOrderTextView.setTextColor(getResources().getColor(R.color.blue));
        deliveryOrderTextView.setTextColor(getResources().getColor(R.color.gray));
        allOrderTextView.setTextColor(getResources().getColor(R.color.gray));
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());

        paymentOrderTextView.setOnClickListener(new viewPagerOnClickListener(0));
        deliveryOrderTextView.setOnClickListener(new viewPagerOnClickListener(1));
        allOrderTextView.setOnClickListener(new viewPagerOnClickListener(2));
        backLayout.setOnClickListener(this);

        myToast = new MyToast(this);
    }


    //选项卡点击事件监听
    public class viewPagerOnClickListener implements OnClickListener {
        private int index = 0;

        public viewPagerOnClickListener(int i) {
            // TODO Auto-generated constructor stub
            index = i;
        }

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            viewPager.setCurrentItem(index);
        }

    }

    /**
     * 页卡切换监听
     */
    public class MyOnPageChangeListener implements OnPageChangeListener {
        int one = screenW / 3;//偏移量

        @Override
        public void onPageSelected(int arg0) {
            Animation animation = new TranslateAnimation(one * currentIndex, one * arg0, 0, 0);
            currentIndex = arg0;
            animation.setFillAfter(true);
            animation.setDuration(300);
            cursorImageView.startAnimation(animation);
            //将expandablelistview展开
            orderAdapter.expandGropu(arg0);
            switch (arg0) {
                case 0:
                    paymentOrderTextView.setTextColor(getResources().getColor(R.color.blue));
                    deliveryOrderTextView.setTextColor(getResources().getColor(R.color.gray));
                    allOrderTextView.setTextColor(getResources().getColor(R.color.gray));

                    break;

                case 1:
                    paymentOrderTextView.setTextColor(getResources().getColor(R.color.gray));
                    deliveryOrderTextView.setTextColor(getResources().getColor(R.color.blue));
                    allOrderTextView.setTextColor(getResources().getColor(R.color.gray));
                    break;

                case 2:
                    paymentOrderTextView.setTextColor(getResources().getColor(R.color.gray));
                    deliveryOrderTextView.setTextColor(getResources().getColor(R.color.gray));
                    allOrderTextView.setTextColor(getResources().getColor(R.color.blue));
                    break;

                default:
                    break;
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.order_return://返回按钮
                doBack();
                break;

            default:
                break;
        }
    }

    //退出app进行的判断，判断是否是栈中的唯一一个app，如果是，启动主页
    private void doBack() {
        // TODO Auto-generated method stub
        if (AppManager.getInstance().getActivityCount() == 1) {//一个activity的时候
            Intent intent = new Intent(this, MainTabActivity.class);
            startActivity(intent);
        }
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            doBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
