package com.pictureair.hkdlphotopass.entity;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by milo on 15/12/24.
 * 订单商品信息
 */
public class OrderProductInfo implements Serializable {
    private ArrayList<CartItemInfo> cartItemInfos;
    private String orderTime;

    public OrderProductInfo() {
    }

    public OrderProductInfo(ArrayList<CartItemInfo> cartItemInfos, String orderTime) {
        this.cartItemInfos = cartItemInfos;
        this.orderTime = orderTime;
    }

    public ArrayList<CartItemInfo> getCartItemInfos() {
        return cartItemInfos;
    }

    public void setCartItemInfos(ArrayList<CartItemInfo> cartItemInfos) {
        this.cartItemInfos = cartItemInfos;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }
}
