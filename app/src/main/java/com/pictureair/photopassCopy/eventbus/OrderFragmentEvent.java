package com.pictureair.photopassCopy.eventbus;


import com.pictureair.photopassCopy.entity.OrderInfo;
import com.pictureair.photopassCopy.entity.OrderProductInfo;

import java.util.ArrayList;

/**
 * Created by bass on 16/4/25.
 */
public class OrderFragmentEvent implements BaseBusEvent {

    private ArrayList<OrderInfo> orderInfoArrayList = new ArrayList<>();
    private ArrayList<OrderProductInfo> orderChildlist = new ArrayList<>();
    private String currency;
    private int tab;
    private int request = 0;//请求类型

    public OrderFragmentEvent(ArrayList<OrderInfo> orderInfoArrayList, ArrayList<OrderProductInfo> orderChildlist, String currency, int tab) {
        this.orderChildlist.clear();
        this.orderInfoArrayList.clear();
        this.orderInfoArrayList.addAll(orderInfoArrayList);
        this.orderChildlist.addAll(orderChildlist);
        this.currency = currency;
        this.tab = tab;
    }

    public OrderFragmentEvent() {
    }

    public int getRequest() {
        return request;
    }

    public void setRequest(int request) {
        this.request = request;
    }

    public ArrayList<OrderInfo> getOrderInfos() {
        return orderInfoArrayList;
    }

    public void setOrderInfos(ArrayList<OrderInfo> orderInfoArrayList) {
        this.orderInfoArrayList = orderInfoArrayList;
    }

    public ArrayList<OrderProductInfo> getOrderChildlist() {
        return orderChildlist;
    }

    public void setOrderChildlist(ArrayList<OrderProductInfo> orderChildlist) {
        this.orderChildlist = orderChildlist;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getTab() {
        return tab;
    }

    public void setTab(int tab) {
        this.tab = tab;
    }
}
