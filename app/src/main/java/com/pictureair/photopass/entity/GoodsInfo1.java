package com.pictureair.photopass.entity;

import java.util.List;

/**
 * Created by milo on 15/12/13.
 * 商品信息
 */
public class GoodsInfo1 {
    private String goodsKey;//string, 商品key
    private String productId;//string,商品主键
    private String storeId;//string,商城主键
    private String name;//string,商品名称
    private String nameAlias;//string,商品别名
    private String description;//string,商品描述
    private int embedPhotosCount;//int,商品合成所需图片数量
    private List<GoodInfoPrictures> prictures;//商品图片信息
    private int price;//int, 商品价格
    private int sequence;//int,商品序号

    public String getGoodsKey() {
        return goodsKey;
    }

    public void setGoodsKey(String goodsKey) {
        this.goodsKey = goodsKey;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameAlias() {
        return nameAlias;
    }

    public void setNameAlias(String nameAlias) {
        this.nameAlias = nameAlias;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getEmbedPhotosCount() {
        return embedPhotosCount;
    }

    public void setEmbedPhotosCount(int embedPhotosCount) {
        this.embedPhotosCount = embedPhotosCount;
    }

    public List<GoodInfoPrictures> getPrictures() {
        return prictures;
    }

    public void setPrictures(List<GoodInfoPrictures> prictures) {
        this.prictures = prictures;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
}
