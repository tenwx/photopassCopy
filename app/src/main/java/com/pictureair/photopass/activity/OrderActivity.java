package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.entity.OrderInfo;
import com.pictureair.photopass.entity.OrderProductInfo;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单页面，分三类，Payment，Delivery，All order
 *
 * @author bauer_bao
 */
public class OrderActivity extends BaseActivity {

    private ViewPager viewPager;
    private OrderViewPagerAdapter orderAdapter;
    private ArrayList<View> listViews;
    private ImageView cursorImageView;//动画图片
    private TextView paymentOrderTextView, deliveryOrderTextView, allOrderTextView;//选项卡
//    private ImageView backLayout;

    //group列表信息
    private ArrayList<OrderInfo> paymentOrderArrayList;
    private ArrayList<OrderInfo> deliveryOrderArrayList;
    private ArrayList<OrderInfo> allOrderArrayList;
    private ArrayList<OrderInfo> downOrderArrayList;

    private OrderInfo orderInfo;
    //child列表信息
    private List<OrderProductInfo> paymentOrderChildArrayList;
    private List<OrderProductInfo> deliveryOrderChildArrayList;
    private List<OrderProductInfo> allOrderChildArrayList;
    private List<OrderProductInfo> downOrderChildArrayList;
    private ArrayList<CartItemInfo> cartItemInfo;

    private SharedPreferences sharedPreferences;
    private int currentIndex = 0;//viewpager当前编号
    private int screenW;//屏幕宽度

    private NoNetWorkOrNoCountView netWorkOrNoCountView;

    private static String TAG = "OrderActivity";

    private CustomProgressDialog customProgressDialog;
    private MyToast myToast;
    private PictureAirDbManager pictureAirDbManager;
    private List<String> orderIds;

    private final Handler orderActivityHandler = new OrderActivityHandler(this);


    private static class OrderActivityHandler extends Handler{
        private final WeakReference<OrderActivity> mActivity;

        public OrderActivityHandler(OrderActivity activity){
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
                    PictureAirLog.v(TAG, "cartItemInfo size = " + cartItemInfo.size());

                    OrderProductInfo orderProductInfo = new OrderProductInfo();
                    orderProductInfo.setOrderTime(orderInfo.orderTime);
                    orderProductInfo.setCartItemInfos(cartItemInfo);
                    PictureAirLog.v(TAG, "orderInfo orderId:" + orderInfo.orderId);

                    if (orderInfo.orderStatus == 1) {//1等待买家付款
                        if (orderIds != null && orderIds.size() > 0) {
                            for (String orderId : orderIds) {
                                //判断orderId是否相同，且状态是否为1（未付款）
                                if (orderId.equals(orderInfo.orderId + "")) {
                                    orderInfo.orderStatus = 6;
                                    break;
                                }
                            }
                        }
                        paymentOrderArrayList.add(orderInfo);
                        paymentOrderChildArrayList.add(orderProductInfo);
                    } else if (orderInfo.orderStatus == 2 || orderInfo.orderStatus == 3) {//2买家已付款（等待卖家发货），3卖家已发货（等待买家确认）
                        deliveryOrderArrayList.add(orderInfo);
                        deliveryOrderChildArrayList.add(orderProductInfo);
                    } else if (orderInfo.orderStatus == 4 || orderInfo.orderStatus == 5) {
                        downOrderArrayList.add(orderInfo);
                        downOrderChildArrayList.add(orderProductInfo);
                    }
                    allOrderArrayList.add(orderInfo);
                    allOrderChildArrayList.add(orderProductInfo);
                }

                orderAdapter = new OrderViewPagerAdapter(OrderActivity.this, listViews, paymentOrderArrayList, deliveryOrderArrayList, downOrderArrayList,
                        paymentOrderChildArrayList, deliveryOrderChildArrayList, downOrderChildArrayList, sharedPreferences.getString(Common.CURRENCY, Common.DEFAULT_CURRENCY));
                viewPager.setAdapter(orderAdapter);
                viewPager.setCurrentItem(0);
                orderAdapter.expandGropu(0);//因为异步回调，所以第一次需要在此处设置展开

                break;

            case API1.GET_ORDER_FAILED:
//				toast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
                customProgressDialog.dismiss();
                netWorkOrNoCountView.setVisibility(View.VISIBLE);
                netWorkOrNoCountView.setResult(R.string.no_network, R.string.click_button_reload, R.string.reload, R.drawable.no_network, orderActivityHandler, true);
                viewPager.setVisibility(View.INVISIBLE);
                break;
            case API1.DELETE_ORDER_SUCCESS:
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
                API1.getOrderInfo(orderActivityHandler);
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        initView();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    //初始化
    private void initView() {
        //从网络获取数据
        API1.getOrderInfo(orderActivityHandler);
        //获取本地已付款为收到推送的order
        getLocalPaymentOrder();
        sharedPreferences = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
        //获取订单接口
        // 显示进度条。
        customProgressDialog = CustomProgressDialog.show(OrderActivity.this, getString(R.string.is_loading), false, null);
        netWorkOrNoCountView = (NoNetWorkOrNoCountView) findViewById(R.id.nonetwork_view);
        setTopTitleShow(R.string.my_order);
        setTopLeftValueAndShow(R.drawable.back_white, true);
        paymentOrderTextView = (TextView) findViewById(R.id.order_payment);
        deliveryOrderTextView = (TextView) findViewById(R.id.order_delivery);
        allOrderTextView = (TextView) findViewById(R.id.order_all);
        viewPager = (ViewPager) findViewById(R.id.order_vPager);
        cursorImageView = (ImageView) findViewById(R.id.cursor);

        paymentOrderTextView.setTextColor(getResources().getColor(R.color.blue));
        deliveryOrderTextView.setTextColor(getResources().getColor(R.color.gray));
        allOrderTextView.setTextColor(getResources().getColor(R.color.gray));

        screenW = ScreenUtil.getScreenWidth(this);// 获取分辨率宽度
        Matrix matrix = new Matrix();
        matrix.postTranslate(0, 0);
        cursorImageView.setImageMatrix(matrix);// 设置动画初始位置
        initData();
    }

    /**
     * 初始化数据
     */
    public void initData() {
        listViews = new ArrayList<>();
        //初始化expandablelistview需要的数据
        paymentOrderArrayList = new ArrayList<>();
        deliveryOrderArrayList = new ArrayList<>();
        allOrderArrayList = new ArrayList<>();
        downOrderArrayList = new ArrayList<>();
        paymentOrderChildArrayList = new ArrayList<>();
        deliveryOrderChildArrayList = new ArrayList<>();
        allOrderChildArrayList = new ArrayList<>();
        downOrderChildArrayList = new ArrayList<>();
        orderIds = new ArrayList<>();

        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
        paymentOrderTextView.setOnClickListener(new viewPagerOnClickListener(0));
        deliveryOrderTextView.setOnClickListener(new viewPagerOnClickListener(1));
        allOrderTextView.setOnClickListener(new viewPagerOnClickListener(2));

        LayoutInflater mInflater = getLayoutInflater();
        listViews.add(mInflater.inflate(R.layout.order_list, null));
        listViews.add(mInflater.inflate(R.layout.order_list, null));
        listViews.add(mInflater.inflate(R.layout.order_list, null));

        myToast = new MyToast(this);
        pictureAirDbManager = new PictureAirDbManager(MyApplication.getInstance());


    }

    /**
     * 获取本地已付款为收到推送的order
     */
    public void getLocalPaymentOrder() {
        orderActivityHandler.post(new Runnable() {
            @Override
            public void run() {
                orderIds = pictureAirDbManager.searchPaymentOrderIdDB();
                PictureAirLog.v(TAG, "getLocalPaymentOrder orderIds:" + orderIds.size());
            }
        });
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
    public void TopViewClick(View view) {
        super.TopViewClick(view);
        switch (view.getId()) {
            case R.id.topLeftView://返回按钮


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
    protected void onDestroy() {
        super.onDestroy();
        orderActivityHandler.removeCallbacksAndMessages(null);
    }
}
