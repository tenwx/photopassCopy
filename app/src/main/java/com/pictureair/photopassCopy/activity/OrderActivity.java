package com.pictureair.photopassCopy.activity;

import android.content.Intent;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopassCopy.MyApplication;
import com.pictureair.photopassCopy.R;
import com.pictureair.photopassCopy.adapter.OrderViewPagerAdapter2;
import com.pictureair.photopassCopy.entity.CartItemInfo;
import com.pictureair.photopassCopy.entity.OrderInfo;
import com.pictureair.photopassCopy.entity.OrderProductInfo;
import com.pictureair.photopassCopy.entity.PaymentOrderInfo;
import com.pictureair.photopassCopy.eventbus.OrderFragmentEvent;
import com.pictureair.photopassCopy.fragment.OrderFragment;
import com.pictureair.photopassCopy.greendao.PictureAirDbManager;
import com.pictureair.photopassCopy.http.rxhttp.RxSubscribe;
import com.pictureair.photopassCopy.util.API2;
import com.pictureair.photopassCopy.util.AppManager;
import com.pictureair.photopassCopy.util.AppUtil;
import com.pictureair.photopassCopy.util.Common;
import com.pictureair.photopassCopy.util.JsonUtil;
import com.pictureair.photopassCopy.util.PictureAirLog;
import com.pictureair.photopassCopy.util.ScreenUtil;
import com.pictureair.photopassCopy.widget.NoNetWorkOrNoCountView;
import com.pictureair.photopassCopy.widget.PWProgressDialog;
import com.pictureair.photopassCopy.widget.PWToast;
import com.trello.rxlifecycle.android.ActivityEvent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

/**
 * 订单页面，分三类，Payment，Delivery，All order
 *
 * @author bauer_bao
 */
public class OrderActivity extends BaseFragmentActivity {

    private ViewPager viewPager;
    private OrderViewPagerAdapter2 orderAdapter;
    private ImageView cursorImageView;//动画图片
    private TextView paymentOrderTextView, deliveryOrderTextView, allOrderTextView;//选项卡
    private LinearLayout lead_bar,cursor_layout;
//    private ImageView backLayout;

    //group列表信息
    private ArrayList<OrderInfo> paymentOrderArrayList;
    private ArrayList<OrderInfo> deliveryOrderArrayList;
    private ArrayList<OrderInfo> downOrderArrayList;

    private OrderInfo orderInfo;
    //child列表信息
    private ArrayList<OrderProductInfo> paymentOrderChildArrayList;
    private ArrayList<OrderProductInfo> deliveryOrderChildArrayList;
    private ArrayList<OrderProductInfo> downOrderChildArrayList;
    private ArrayList<CartItemInfo> cartItemInfo;

    private int currentIndex = 0;//viewpager当前编号
    private int screenW;//屏幕宽度

    private NoNetWorkOrNoCountView netWorkOrNoCountView;

    private static String TAG = "OrderActivity";

    private PWProgressDialog pwProgressDialog;
    private PWToast myToast;
    private List<PaymentOrderInfo> orderIds;

    private int orderType = 0;//订单类型 异步回调使用
    private String currency;
    private List<Fragment> mFragments;
    public static final int REFRESH = 0X001;
    private static final int GET_LOCAL_PEYMENT_DONE = 111;
    private boolean getLocalPaymentDone = false;
//    private SwipeRefreshLayout refreshLayout;

    private final Handler orderActivityHandler = new OrderActivityHandler(this);


    private static class OrderActivityHandler extends Handler {
        private final WeakReference<OrderActivity> mActivity;

        public OrderActivityHandler(OrderActivity activity) {
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

            case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_RELOAD://noView的按钮响应重新加载点击事件
                //重新加载购物车数据
                if (AppUtil.getNetWorkType(MyApplication.getInstance()) == 0) {
                    goneTop();
                    myToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
                    break;
                }
                showTop();
                showProgressDialog();
                getData();
                break;

            case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_NO_RELOAD://noView的按钮响应非重新加载的点击事件
                //去跳转到商品页面
                //需要删除页面，保证只剩下mainTab页面，
                AppManager.getInstance().killOtherActivity(MainTabActivity.class);
                //同时将mainTab切换到shop Tab
                ((MyApplication) getApplication()).setMainTabIndex(3);

                break;
            case REFRESH:
                //重新加载购物车数据
                if (AppUtil.getNetWorkType(MyApplication.getInstance()) == 0) {
                    goneTop();
                    myToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
                    break;
                }
                showTop();
                getData();
                break;


            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        orderType = getIntent().getIntExtra("orderType", 0);
        PictureAirLog.v(TAG, "orderType： " + orderType);
        initView();
        PictureAirLog.out("finish--->oncreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

    }

    //初始化
    private void initView() {
        PictureAirLog.out("initview");
        showProgressDialog();
        //从网络获取数据
        mFragments = new ArrayList<>();
        getData();
        currency =  Common.DEFAULT_CURRENCY;//SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CURRENCY, Common.DEFAULT_CURRENCY);
        //获取订单接口
        // 显示进度条。
        netWorkOrNoCountView = (NoNetWorkOrNoCountView) findViewById(R.id.nonetwork_view);
        setTopTitleShow(R.string.my_order);
        setTopLeftValueAndShow(R.drawable.back_blue, true);
        paymentOrderTextView = (TextView) findViewById(R.id.order_payment);
        deliveryOrderTextView = (TextView) findViewById(R.id.order_delivery);
        allOrderTextView = (TextView) findViewById(R.id.order_all);
        viewPager = (ViewPager) findViewById(R.id.order_vPager);
        cursorImageView = (ImageView) findViewById(R.id.cursor);
        lead_bar = (LinearLayout) findViewById(R.id.lead_bar);
        cursor_layout = (LinearLayout) findViewById(R.id.cursor_layout);

        paymentOrderTextView.setTextColor(ContextCompat.getColor(this, R.color.white));
        deliveryOrderTextView.setTextColor(ContextCompat.getColor(this, R.color.pp_dark_blue));
        allOrderTextView.setTextColor(ContextCompat.getColor(this, R.color.pp_dark_blue));

        screenW = ScreenUtil.getScreenWidth(this);// 获取分辨率宽度
        Matrix matrix = new Matrix();
        matrix.postTranslate(0, 0);
        cursorImageView.setImageMatrix(matrix);// 设置动画初始位置
        initData();
        //获取本地已付款为收到推送的order
        getLocalPaymentOrder();
    }

    /**
     * 初始化数据
     */
    public void initData() {
        PictureAirLog.out("init data---->");
        //初始化expandablelistview需要的数据
        paymentOrderArrayList = new ArrayList<>();
        deliveryOrderArrayList = new ArrayList<>();
        downOrderArrayList = new ArrayList<>();
        paymentOrderChildArrayList = new ArrayList<>();
        deliveryOrderChildArrayList = new ArrayList<>();
        downOrderChildArrayList = new ArrayList<>();
        orderIds = new ArrayList<>();

        viewPager.addOnPageChangeListener(new MyOnPageChangeListener());
        paymentOrderTextView.setOnClickListener(new viewPagerOnClickListener(0));
        deliveryOrderTextView.setOnClickListener(new viewPagerOnClickListener(1));
        allOrderTextView.setOnClickListener(new viewPagerOnClickListener(2));

        myToast = new PWToast(this);
    }

    /**
     * 从网络上获取信息
     */
    public void getData() {

        API2.getOrderInfo()
                .map(new Func1<JSONObject, JSONObject>() {
                    @Override
                    public JSONObject call(JSONObject jsonObject) {
                        PictureAirLog.e("order map",Looper.myLooper() == Looper.getMainLooper()?"true":"false");
                        while (!getLocalPaymentDone);
                        return jsonObject;

                    }
                })
                .compose(this.<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        showTop();
                        PictureAirLog.d(TAG, "get success----");

                        netWorkOrNoCountView.setVisibility(View.INVISIBLE);
                        paymentOrderArrayList.clear();
                        paymentOrderChildArrayList.clear();
                        deliveryOrderArrayList.clear();
                        deliveryOrderChildArrayList.clear();
                        downOrderArrayList.clear();
                        downOrderChildArrayList.clear();
                        //解析数据
                        JSONArray allOrdersArray = jsonObject.getJSONArray("orders");//得到所有的订单信息
                        PictureAirLog.v(TAG, "orderInfo" + allOrdersArray.toString());
                        for (int i = 0; i < allOrdersArray.size(); i++) {
                            JSONObject orderJsonObject = allOrdersArray.getJSONObject(i);//得到单个订单信息
                            orderInfo = JsonUtil.getOrderGroupInfo(orderJsonObject);//获取group信息
                            cartItemInfo = JsonUtil.getOrderChildInfo(orderJsonObject);//获取child信息
                            PictureAirLog.v(TAG, "cartItemInfo size = " + cartItemInfo.size());

                            for (int j = 0; j < cartItemInfo.size(); j++) {
                                if (cartItemInfo.get(j).getCartProductType() == 1) {
                                    orderInfo.productEntityType = 1;
                                    break;
                                } else {
                                    orderInfo.productEntityType = 0;
                                }
                            }

                            OrderProductInfo orderProductInfo = new OrderProductInfo();
                            orderProductInfo.setOrderTime(orderInfo.orderTime);
                            orderProductInfo.setCartItemInfos(cartItemInfo);
                            PictureAirLog.v(TAG, "orderInfo orderId:" + orderInfo.orderNumber);
                            if (orderInfo.orderStatus == -3) {
                                downOrderArrayList.add(orderInfo);
                                downOrderChildArrayList.add(orderProductInfo);
                            } else if (orderInfo.orderStatus > -3 && orderInfo.orderStatus < 1) {
                                if (orderInfo.orderStatus == -1) {
                                    continue;
                                } else {
                                    paymentOrderArrayList.add(orderInfo);
                                    paymentOrderChildArrayList.add(orderProductInfo);
                                }
                            } else if (orderInfo.orderStatus == 1) {//1等待买家付款
                                if (orderIds != null && orderIds.size() > 0) {
                                    for (PaymentOrderInfo paymentOrderInfo : orderIds) {
                                        //判断orderId是否相同，且状态是否为1（未付款）
                                        if (paymentOrderInfo.getOrderId().equals(orderInfo.orderNumber + "")) {
                                            orderInfo.orderStatus = 6;
                                            break;
                                        }
                                    }
                                }
                                paymentOrderArrayList.add(orderInfo);
                                paymentOrderChildArrayList.add(orderProductInfo);
                            } else if (orderInfo.orderStatus >= 2) {//2买家已付款（等待卖家发货），3卖家已发货（等待买家确认）
                                if (orderInfo.productEntityType == 0) {
                                    //0为虚拟商品
                                    downOrderArrayList.add(orderInfo);
                                    downOrderChildArrayList.add(orderProductInfo);
                                } else {
                                    //需要买家自提
                                    deliveryOrderArrayList.add(orderInfo);
                                    deliveryOrderChildArrayList.add(orderProductInfo);
                                }
                            }
                        }

                    }

                    @Override
                    public void _onError(int status) {
                        OrderFragmentEvent orderFragmentEvent = new OrderFragmentEvent();
                        orderFragmentEvent.setRequest(1);
                        EventBus.getDefault().post(orderFragmentEvent);

                        hideProgressDialog();
                        goneTop();
                        netWorkOrNoCountView.setVisibility(View.VISIBLE);
                        netWorkOrNoCountView.setResult(R.string.no_network, R.string.click_button_reload, R.string.reload, R.drawable.no_network, orderActivityHandler, true);
                        viewPager.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onCompleted() {

                        if (null == mFragments || mFragments.size() == 0) {
                            if (null != orderActivityHandler
                                    && null != paymentOrderArrayList
                                    && null != deliveryOrderArrayList
                                    && null != downOrderArrayList
                                    && null != paymentOrderChildArrayList
                                    && null != deliveryOrderChildArrayList
                                    && null != downOrderChildArrayList) {
                                mFragments.add(OrderFragment.getInstance(orderActivityHandler, paymentOrderArrayList, paymentOrderChildArrayList, currency, 0));
                                mFragments.add(OrderFragment.getInstance(orderActivityHandler, deliveryOrderArrayList, deliveryOrderChildArrayList, currency, 1));
                                mFragments.add(OrderFragment.getInstance(orderActivityHandler, downOrderArrayList, downOrderChildArrayList, currency, 2));
                            }
                        }

                        if (null == orderAdapter) {
                            orderAdapter = new OrderViewPagerAdapter2(getSupportFragmentManager(), mFragments);
                            viewPager.setAdapter(orderAdapter);
                            viewPager.setVisibility(View.VISIBLE);
                            viewPager.setOffscreenPageLimit(3);
                            viewPager.setCurrentItem(orderType);
                        }
                        EventBus.getDefault().post(new OrderFragmentEvent(paymentOrderArrayList, paymentOrderChildArrayList, currency, 0));
                        EventBus.getDefault().post(new OrderFragmentEvent(deliveryOrderArrayList, deliveryOrderChildArrayList, currency, 1));
                        EventBus.getDefault().post(new OrderFragmentEvent(downOrderArrayList, downOrderChildArrayList, currency, 2));
                        hideProgressDialog();
                    }
                });
    }

    /**
     * 显示菊花
     */
    private void showProgressDialog() {
        if (null == pwProgressDialog) {
            pwProgressDialog = new PWProgressDialog(this)
                    .setPWProgressDialogMessage(R.string.is_loading)
                    .pwProgressDialogCreate();
        }
        pwProgressDialog.pwProgressDialogShow();
    }

    /**
     * 隐藏菊花
     */
    private void hideProgressDialog() {
        if (null != pwProgressDialog) {
            pwProgressDialog.pwProgressDialogDismiss();
        }
    }

    /**
     * 获取本地已付款为收到推送的order
     */
    public void getLocalPaymentOrder() {
        PictureAirLog.out("get local payment order");
        new Thread() {
            @Override
            public void run() {
                super.run();
                PictureAirLog.out("runk------>");
                orderIds = PictureAirDbManager.searchPaymentOrderIdDB();
                PictureAirLog.v(TAG, "getLocalPaymentOrder orderIds:" + orderIds.size());
                getLocalPaymentDone = true;
            }
        }.start();
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
        public void onPageSelected(final int arg0) {
            Animation animation = new TranslateAnimation(one * currentIndex, one * arg0, 0, 0);
            currentIndex = arg0;
            animation.setFillAfter(true);
            animation.setDuration(300);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    paymentOrderTextView.setTextColor(ContextCompat.getColor(OrderActivity.this, R.color.pp_dark_blue));
                    deliveryOrderTextView.setTextColor(ContextCompat.getColor(OrderActivity.this, R.color.pp_dark_blue));
                    allOrderTextView.setTextColor(ContextCompat.getColor(OrderActivity.this, R.color.pp_dark_blue));
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    switch (arg0) {
                        case 0:
                            paymentOrderTextView.setTextColor(ContextCompat.getColor(OrderActivity.this, R.color.white));
                            deliveryOrderTextView.setTextColor(ContextCompat.getColor(OrderActivity.this, R.color.pp_dark_blue));
                            allOrderTextView.setTextColor(ContextCompat.getColor(OrderActivity.this, R.color.pp_dark_blue));

                            break;

                        case 1:
                            paymentOrderTextView.setTextColor(ContextCompat.getColor(OrderActivity.this, R.color.pp_dark_blue));
                            deliveryOrderTextView.setTextColor(ContextCompat.getColor(OrderActivity.this, R.color.white));
                            allOrderTextView.setTextColor(ContextCompat.getColor(OrderActivity.this, R.color.pp_dark_blue));
                            break;

                        case 2:
                            paymentOrderTextView.setTextColor(ContextCompat.getColor(OrderActivity.this, R.color.pp_dark_blue));
                            deliveryOrderTextView.setTextColor(ContextCompat.getColor(OrderActivity.this, R.color.pp_dark_blue));
                            allOrderTextView.setTextColor(ContextCompat.getColor(OrderActivity.this, R.color.white));
                            break;

                        default:
                            break;
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            cursorImageView.startAnimation(animation);
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
        hideProgressDialog();
    }

    /**
     * 显示头部
     */
    private void showTop(){
        cursor_layout.setVisibility(View.VISIBLE);
        lead_bar.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏头部
     */
    private void goneTop(){
        cursor_layout.setVisibility(View.GONE);
        lead_bar.setVisibility(View.GONE);
    }
}
