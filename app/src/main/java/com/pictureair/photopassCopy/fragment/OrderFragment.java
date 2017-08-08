package com.pictureair.photopassCopy.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.pictureair.photopassCopy.MyApplication;
import com.pictureair.photopassCopy.R;
import com.pictureair.photopassCopy.activity.MainTabActivity;
import com.pictureair.photopassCopy.activity.OrderActivity;
import com.pictureair.photopassCopy.activity.OrderDetailActivity;
import com.pictureair.photopassCopy.adapter.OrderListViewAdapter;
import com.pictureair.photopassCopy.entity.OrderInfo;
import com.pictureair.photopassCopy.entity.OrderProductInfo;
import com.pictureair.photopassCopy.eventbus.BaseBusEvent;
import com.pictureair.photopassCopy.eventbus.OrderFragmentEvent;
import com.pictureair.photopassCopy.util.AppManager;
import com.pictureair.photopassCopy.util.Common;
import com.pictureair.photopassCopy.util.OrderInfoDateSortUtil;
import com.pictureair.photopassCopy.util.OrderProductDateSortUtil;
import com.pictureair.photopassCopy.util.PictureAirLog;
import com.pictureair.photopassCopy.util.ReflectionUtil;
import com.pictureair.photopassCopy.widget.NoNetWorkOrNoCountView;
import com.pictureair.photopassCopy.widget.PWToast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

/**
 * 订单
 * Created by bass on 16/4/25.
 */
public class OrderFragment extends Fragment implements OrderListViewAdapter.RemoveOrderItemListener{
    private final String TAG = "OrderFragment";
    private View view;
    private ExpandableListView orderListView;

    private ArrayList<OrderInfo> orderList;

    private List<OrderProductInfo> childlist;

    private OrderListViewAdapter allOrderAdapter;

    private String currency;

    private Context context;
    private PWToast myToast;
    private MyApplication application;
    private int tab = 0;

    private static Handler mHandler;
    private SwipeRefreshLayout refreshLayout;

    private NoNetWorkOrNoCountView netWorkOrNoCountView;


    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_NO_RELOAD://noView的按钮响应非重新加载的点击事件
                    //去跳转到商品页面
                    //需要删除页面，保证只剩下mainTab页面，
                    AppManager.getInstance().killOtherActivity(MainTabActivity.class);
                    //同时将mainTab切换到shop Tab
                    application.setMainTabIndex(3);
                    break;
            }
            return false;
        }
    });

    public OrderFragment() {
    }

    public static OrderFragment getInstance(Handler handler, ArrayList<OrderInfo> orderInfos3,
                                            List<OrderProductInfo> orderChildlist3, String currency, int tab
    ) {
        mHandler = handler;
        OrderFragment orderFragment = new OrderFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("orderList", orderInfos3);
        bundle.putSerializable("orderChildList", (Serializable) orderChildlist3);

        bundle.putString("currency", currency);
        bundle.putInt("tab", tab);

        orderFragment.setArguments(bundle);
        return orderFragment;
    }

    @Override
    public void onAttach(Context context) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        if (getArguments() != null) {
            orderList = getArguments().getParcelableArrayList("orderList");
            Collections.sort(orderList, new OrderInfoDateSortUtil());

            childlist = (List<OrderProductInfo>) getArguments().getSerializable("orderChildList");
            Collections.sort(childlist, new OrderProductDateSortUtil());

            currency = getArguments().getString("currency");
            tab = getArguments().getInt("tab");
        }
        application = (MyApplication) getActivity().getApplication();
        this.myToast = new PWToast(context);
        this.context = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.order_list, container, false);
        }
        netWorkOrNoCountView = (NoNetWorkOrNoCountView) view.findViewById(R.id.nonetwork);

        orderListView = (ExpandableListView) view.findViewById(R.id.order_viewpager_listview1);

        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_layout);
        refreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        refreshLayout.setEnabled(true);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                PictureAirLog.out("start refresh");
                refreshLayout.setEnabled(false);
                mHandler.sendEmptyMessage(OrderActivity.REFRESH);
            }
        });

        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parent.removeView(view);
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        allOrderAdapter = new OrderListViewAdapter(context, orderList, childlist, currency, handler, tab);
        allOrderAdapter.setRemoveOrderItemListener(this);
        orderListView.setGroupIndicator(null);
        orderListView.setAdapter(allOrderAdapter);
        orderListView.setOnGroupClickListener(new GroupOnClick(tab));
        orderListView.setOnChildClickListener(new ChildOnClick(tab));

    }

    //expandablelistview展开child项
    public void expandGropu() {
        for (int i = 0; i < orderList.size(); i++) {
            if (orderListView != null) {
                orderListView.expandGroup(i);
            }
        }
    }

    @Override
    public void removeOrderSuccess(OrderInfo orderInfo, OrderProductInfo orderProductInfo) {
        orderList.remove(orderInfo);
        childlist.remove(orderProductInfo);
        if (allOrderAdapter != null) {
            allOrderAdapter.notifyDataSetChanged();
        }

        if (orderList == null || orderList.size() == 0) {//显示空图
            showNetWorkOrNoCount();
        }
    }

    @Override
    public void removeOrderFailed(int status) {
        myToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), status), Common.TOAST_SHORT_TIME);
    }

    //group点击事件监听
    private class GroupOnClick implements ExpandableListView.OnGroupClickListener {
        private int index;

        public GroupOnClick(int index) {
            this.index = index;
        }

        @Override
        public boolean onGroupClick(ExpandableListView parent, View v,
                                    int groupPosition, long id) {
            startNewActivity(index, groupPosition);
            return true;
        }
    }

    //child的点击事件
    private class ChildOnClick implements ExpandableListView.OnChildClickListener {

        private int index;

        public ChildOnClick(int index) {
            this.index = index;
        }

        @Override
        public boolean onChildClick(ExpandableListView parent, View v,
                                    int groupPosition, int childPosition, long id) {
            startNewActivity(index, groupPosition);
            return true;
        }

    }

    //跳转到订单详情界面
    private void startNewActivity(int index, int groupPosition) {
        // TODO Auto-generated method stub
        Intent intent = new Intent(context, OrderDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("groupitem", orderList.get(groupPosition));
        bundle.putSerializable("childitemlist",  childlist.get(groupPosition).getCartItemInfos());

        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public void onResume() {
        PictureAirLog.out(TAG + "orderFragment----------------->");
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        PictureAirLog.out(TAG + "orderFragment onDetach----------------->");

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe
    public void onUserEvent(BaseBusEvent baseBusEvent) {
        if (baseBusEvent instanceof OrderFragmentEvent) {
            OrderFragmentEvent orderFragmentEvent = (OrderFragmentEvent) baseBusEvent;
            PictureAirLog.out(TAG + "orderFragment onUserEvent----------------->" + orderFragmentEvent.getTab() + "----" + tab);

            switch (orderFragmentEvent.getRequest()) {
                case 1:
                    if (refreshLayout.isRefreshing()) {
                        refreshLayout.setEnabled(true);
                        refreshLayout.setRefreshing(false);
                    }
                    EventBus.getDefault().removeStickyEvent(orderFragmentEvent);

                    break;

                case 0:
                    if (null != refreshLayout && refreshLayout.isRefreshing()) {
                        refreshLayout.setEnabled(true);
                        refreshLayout.setRefreshing(false);
                    }
                    currency = orderFragmentEvent.getCurrency();

                    if (orderFragmentEvent.getTab() == tab) {
                        orderList.clear();
                        orderList.addAll(orderFragmentEvent.getOrderInfos());
                        Collections.sort(orderList, new OrderInfoDateSortUtil());

                        childlist.clear();
                        childlist.addAll(orderFragmentEvent.getOrderChildlist());
                        Collections.sort(childlist, new OrderProductDateSortUtil());

                        if (null != allOrderAdapter) {
                            allOrderAdapter.notifyDataSetChanged();
                        }

                        if (orderList == null || orderList.size() == 0) {
                            showNetWorkOrNoCount();
                        } else {
                            expandGropu();
                        }
                        EventBus.getDefault().removeStickyEvent(orderFragmentEvent);
                    }
                    break;

                default:
                    break;

            }
        }
    }

    private void showNetWorkOrNoCount(){
            orderListView.setVisibility(View.GONE);
            refreshLayout.setVisibility(View.GONE);
            netWorkOrNoCountView.setResult(R.string.order_empty_resultString, R.string.want_to_buy, R.string.order_empty_buttonString, R.drawable.no_order_data, handler, false);
            netWorkOrNoCountView.setVisibility(View.VISIBLE);
    }
}
