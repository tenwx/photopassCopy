package com.pictureair.photopass.eventbus;


import com.pictureair.photopass.entity.OrderInfo;
import com.pictureair.photopass.entity.OrderProductInfo;

import java.util.ArrayList;

/**
 * Created by bass on 16/4/25.
 */
public class OrderFragmentEvent implements BaseBusEvent {

    private ArrayList<OrderInfo> orderInfos1;
    private ArrayList<OrderInfo> orderInfos2;
    private ArrayList<OrderInfo> orderInfos3;
    private ArrayList<OrderProductInfo> orderChildlist1;
    private ArrayList<OrderProductInfo> orderChildlist2;
    private ArrayList<OrderProductInfo> orderChildlist3;
    private String currency;
    private int tab;
    private int request = 0;//请求类型

    public int getRequest() {
        return request;
    }
    public void setRequest(int request) {
        this.request = request;
    }

    public ArrayList<OrderInfo> getOrderInfos1() {
        return orderInfos1;
    }

    public void setOrderInfos1(ArrayList<OrderInfo> orderInfos1) {
        this.orderInfos1 = orderInfos1;
    }

    public ArrayList<OrderInfo> getOrderInfos2() {
        return orderInfos2;
    }

    public void setOrderInfos2(ArrayList<OrderInfo> orderInfos2) {
        this.orderInfos2 = orderInfos2;
    }

    public ArrayList<OrderInfo> getOrderInfos3() {
        return orderInfos3;
    }

    public void setOrderInfos3(ArrayList<OrderInfo> orderInfos3) {
        this.orderInfos3 = orderInfos3;
    }

    public ArrayList<OrderProductInfo> getOrderChildlist1() {
        return orderChildlist1;
    }

    public void setOrderChildlist1(ArrayList<OrderProductInfo> orderChildlist1) {
        this.orderChildlist1 = orderChildlist1;
    }

    public ArrayList<OrderProductInfo> getOrderChildlist2() {
        return orderChildlist2;
    }

    public void setOrderChildlist2(ArrayList<OrderProductInfo> orderChildlist2) {
        this.orderChildlist2 = orderChildlist2;
    }

    public ArrayList<OrderProductInfo> getOrderChildlist3() {
        return orderChildlist3;
    }

    public void setOrderChildlist3(ArrayList<OrderProductInfo> orderChildlist3) {
        this.orderChildlist3 = orderChildlist3;
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
