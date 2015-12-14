package com.pictureair.photopass.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by milo on 15/12/14.
 * 获取用户购物车信息
 */
public class CartItemInfoJson implements Serializable{
    private List<CartItemInfo1> items;
    private int totalCount; //number,购物车内总商品数
    private int totalPrice; //number,购物车内商品总价
    private int preferentialPrice; //number 优惠费用

    public List<CartItemInfo1> getItems() {
        return items;
    }

    public void setItems(List<CartItemInfo1> items) {
        this.items = items;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getPreferentialPrice() {
        return preferentialPrice;
    }

    public void setPreferentialPrice(int preferentialPrice) {
        this.preferentialPrice = preferentialPrice;
    }
}
