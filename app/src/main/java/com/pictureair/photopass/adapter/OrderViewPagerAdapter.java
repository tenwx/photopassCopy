package com.pictureair.photopass.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.MainTabActivity;
import com.pictureair.photopass.activity.OrderDetailActivity;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.entity.OrderInfo;
import com.pictureair.photopass.util.API;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.widget.NoNetWorkOrNoCountView;

import java.util.ArrayList;
import java.util.List;
/**
 * 订单页面ViewPager的适配器
 * @author bauer_bao
 *
 */
public class OrderViewPagerAdapter extends PagerAdapter{
	private List<View> listViews;
	private ExpandableListView orderListView1;
	private ExpandableListView orderListView2;
	private ExpandableListView orderListView3;
	
	private ArrayList<OrderInfo> paymentOrderList;
	private ArrayList<OrderInfo> deliveryOrderList;
	private ArrayList<OrderInfo> allOrderList;
	private ArrayList<ArrayList<CartItemInfo>> paymentChildlist;
	private ArrayList<ArrayList<CartItemInfo>> deliveryChildlist;
	private ArrayList<ArrayList<CartItemInfo>> allChildlist;
	private OrderListViewAdapter paymentOrderAdapter;
	private OrderListViewAdapter deliveryOrderAdapter;
	private OrderListViewAdapter allOrderAdapter;
	
	private String currency;
	
	private Context context;
//	private NoNetWorkOrNoCountView netWorkOrNoCountView;
	
	private Handler handler = new Handler() {
	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case API.DELETE_ORDER_SUCCESS:
			 // 移除第几个position。
			 Bundle b = msg.getData();
			 OrderInfo groupInfo = b.getParcelable("group");
			 ArrayList<CartItemInfo> childInfo = b.getParcelableArrayList("child");
			 //删除全部订单 中的对象
			 allOrderList.remove(groupInfo);
			 allChildlist.remove(childInfo);
			 // 删除Delivery中的对象
			 deliveryOrderList.remove(groupInfo);
			 deliveryChildlist.remove(childInfo);
			 //通知适配器刷新
			 deliveryOrderAdapter.notifyDataSetChanged();
			 allOrderAdapter.notifyDataSetChanged();
			 break;
			 
		case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_NO_RELOAD://noView的按钮响应非重新加载的点击事件
			//去跳转到商品页面
			//需要删除页面，保证只剩下mainTab页面，
			AppManager.getInstance().killOtherActivity(MainTabActivity.class);
			//同时将mainTab切换到shop Tab
			MainTabActivity.changeToShopTab = true;
			break;
		}
	}
};

	public OrderViewPagerAdapter(Context context, List<View> list, ArrayList<OrderInfo> orderInfos1, 
			ArrayList<OrderInfo> orderInfos2, ArrayList<OrderInfo> orderInfos3, 
			ArrayList<ArrayList<CartItemInfo>> orderChildlist1, ArrayList<ArrayList<CartItemInfo>> orderChildlist2,
			ArrayList<ArrayList<CartItemInfo>> orderChildlist3, String currency) {
		listViews = list;
		paymentOrderList = orderInfos1;
		deliveryOrderList = orderInfos2;
		allOrderList = orderInfos3;
		this.currency = currency;
		this.context = context;
		paymentChildlist = orderChildlist1;
		deliveryChildlist = orderChildlist2;
		allChildlist = orderChildlist3;
	}

	@Override
	public void destroyItem(View container, int position, Object object) {
		((ViewPager) container).removeView(listViews.get(position));
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listViews.size();
	}

	//初始化页面
	@Override
	public Object instantiateItem(View container, int position) {
		NoNetWorkOrNoCountView netWorkOrNoCountView = (NoNetWorkOrNoCountView) listViews.get(position).findViewById(R.id.nonetwork);
		switch (position) {
		case 0:
			
			System.out.println("instantiateItem----0");
			orderListView1 = (ExpandableListView)listViews.get(position).findViewById(R.id.order_viewpager_listview1);
			paymentOrderAdapter = new OrderListViewAdapter(context, paymentOrderList, paymentChildlist, currency, handler); // 
			orderListView1.setGroupIndicator(null);
			orderListView1.setAdapter(paymentOrderAdapter);
			orderListView1.setOnGroupClickListener(new GroupOnClick(0));
			orderListView1.setOnChildClickListener(new ChildOnClick(0));
			
			
			if (paymentOrderList.size() == 0) {
				orderListView1.setVisibility(View.GONE);
				netWorkOrNoCountView.setResult(R.string.order_empty_resultString, R.string.want_to_buy, R.string.order_empty_buttonString, R.drawable.no_order_data, handler, false);
				netWorkOrNoCountView.setVisibility(View.VISIBLE);
			}
			break;
			
		case 1:
			System.out.println("instantiateItem----1");
           
			orderListView2 = (ExpandableListView)listViews.get(position).findViewById(R.id.order_viewpager_listview1);
			deliveryOrderAdapter = new OrderListViewAdapter(context, deliveryOrderList, deliveryChildlist, currency, handler);
			orderListView2.setGroupIndicator(null);
			orderListView2.setAdapter(deliveryOrderAdapter);
			orderListView2.setOnGroupClickListener(new GroupOnClick(1));
			orderListView2.setOnChildClickListener(new ChildOnClick(1));
			
			 if (deliveryOrderList.size() == 0) {
					orderListView2.setVisibility(View.GONE);
					netWorkOrNoCountView.setResult(R.string.order_empty_resultString, R.string.want_to_buy, R.string.order_empty_buttonString, R.drawable.no_order_data, handler, false);
					netWorkOrNoCountView.setVisibility(View.VISIBLE);
				}
			break;
			
		case 2:
			System.out.println("instantiateItem----2");
			orderListView3 = (ExpandableListView)listViews.get(position).findViewById(R.id.order_viewpager_listview1);
			allOrderAdapter = new OrderListViewAdapter(context, allOrderList, allChildlist, currency, handler);
			orderListView3.setGroupIndicator(null);
			orderListView3.setAdapter(allOrderAdapter);
			orderListView3.setOnGroupClickListener(new GroupOnClick(2));
			orderListView3.setOnChildClickListener(new ChildOnClick(2));
			
			if (allOrderList.size() == 0) {
				orderListView3.setVisibility(View.GONE);
				netWorkOrNoCountView.setResult(R.string.order_empty_resultString, R.string.want_to_buy, R.string.order_empty_buttonString, R.drawable.no_order_data, handler, false);
				netWorkOrNoCountView.setVisibility(View.VISIBLE);
			}
			break;
			
		default:
			break;
		}
		((ViewPager) container).addView(listViews.get(position));
		return listViews.get(position);
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		// TODO Auto-generated method stub
		return arg0 == arg1;
	}
	
	//expandablelistview展开child项
	public void expandGropu(int current) {
		switch (current) {
		case 0:
			for (int i = 0; i < paymentOrderList.size(); i++) {
				orderListView1.expandGroup(i);
			}
			break;
		case 1:
			for (int i = 0; i < deliveryOrderList.size(); i++) {
				orderListView2.expandGroup(i);
			}
			break;
			
		case 2:
			for (int i = 0; i < allOrderList.size(); i++) {
				orderListView3.expandGroup(i);
			}
			break;
			
		default:
			break;
		}
	}
	
	//group点击事件监听
	private class GroupOnClick implements OnGroupClickListener{
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
	private class ChildOnClick implements OnChildClickListener{

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
		switch (index) {
		case 0:
			bundle.putParcelable("groupitem", paymentOrderList.get(groupPosition));//传递对象
			bundle.putParcelableArrayList("childitemlist", paymentChildlist.get(groupPosition));//传递list
			break;

		case 1:
			bundle.putParcelable("groupitem", deliveryOrderList.get(groupPosition));
			bundle.putParcelableArrayList("childitemlist", deliveryChildlist.get(groupPosition));
			break;
			
		case 2:
			bundle.putParcelable("groupitem", allOrderList.get(groupPosition));
			bundle.putParcelableArrayList("childitemlist", allChildlist.get(groupPosition));
			break;
			
		default:
			break;
		}
		intent.putExtras(bundle);
		context.startActivity(intent);
	}

//	@Override
//	public int getItemPosition(Object object) {
//		// TODO Auto-generated method stub
//		return POSITION_NONE;
//	}
	
	
	

}
