package com.pictureair.hkdlphotopass.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by milo on 15/12/14.
 * 获取用户购物车信息
 */
public class CartItemInfoJson implements Serializable {
    private List<CartItemInfo> items = null;
    private int totalCount = 0; //number,购物车内总商品数
    private double totalPrice = 0; //number,购物车内商品总价
    private double preferentialPrice = 0; //number 优惠费用

    public CartItemInfoJson(List<CartItemInfo> items, int totalCount, int totalPrice, int preferentialPrice) {
        this.items = items;
        this.totalCount = totalCount;
        this.totalPrice = totalPrice;
        this.preferentialPrice = preferentialPrice;
    }

    public CartItemInfoJson() {
    }

    public List<CartItemInfo> getItems() {
        return items;
    }

    public void setItems(List<CartItemInfo> items) {
        this.items = items;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public double getPreferentialPrice() {
        return preferentialPrice;
    }

    public void setPreferentialPrice(double preferentialPrice) {
        this.preferentialPrice = preferentialPrice;
    }
}
