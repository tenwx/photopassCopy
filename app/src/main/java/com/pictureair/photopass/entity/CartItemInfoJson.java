package com.pictureair.photopass.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by milo on 15/12/14.
 * 获取用户购物车信息
 */
public class CartItemInfoJson implements Serializable {
    private List<CartItemInfo1> items = null;
    private int totalCount = 0; //number,购物车内总商品数
    private int totalPrice = 0; //number,购物车内商品总价
    private int preferentialPrice = 0; //number 优惠费用

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

    public static String getString() {
        String dataStr = "{\n" +
                "  \"items\": [\n" +
                "    {\n" +
                "      \"cartId\": \"566ec48a648d9e5016db1ee5\",\n" +
                "      \"productName\": \"PhotoPass+\",\n" +
                "      \"storeId\": \"54b4a700155da2bf0e141bbe\",\n" +
                "      \"description\": \"PictureWorks 公司自营产品,用于合作的园区的photoPass升级\",\n" +
                "      \"unitPrice\": 369,\n" +
                "      \"price\": 369,\n" +
                "      \"qty\": 1,\n" +
                "      \"embedPhotos\": []\n" +
                "    },\n" +
                "    {\n" +
                "      \"cartId\": \"566f85338fb5a0a40603bd18\",\n" +
                "      \"productName\": \"Single Digital\",\n" +
                "      \"storeId\": \"54b4a700155da2bf0e141bbe\",\n" +
                "      \"description\": \"数码照片文件\",\n" +
                "      \"unitPrice\": 59,\n" +
                "      \"price\": 59,\n" +
                "      \"qty\": 1,\n" +
                "      \"embedPhotos\": []\n" +
                "    }\n" +
                "  ],\n" +
                "  \"totalCount\": 2,\n" +
                "  \"totalPrice\": 428,\n" +
                "  \"preferentialPrice\": 0\n" +
                "}";

        return dataStr;
    }

}
