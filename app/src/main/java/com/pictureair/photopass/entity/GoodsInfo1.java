package com.pictureair.photopass.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by milo on 15/12/13.
 * 商品信息
 */
public class GoodsInfo1 implements Serializable {
    private String goodsKey = "";//string, 商品key
    private String productId = "";//string,商品主键
    private String storeId = "";//string,商城主键
    private String name = "";//string,商品名称
    private String nameAlias = "";//string,商品别名
    private String description = "";//string,商品描述
    private int embedPhotosCount = 9;//int,商品合成所需图片数量
    private List<GoodInfoPictures> pictures = null;//商品图片信息
    private int entityType = 0; //int,商品虚拟／实体类型（0,1）
    private EmbedPhotoConfig ssConfig = null;//商品合成设置（单位：px）
    private int price = 0;//int, 商品价格
    private int sequence = 0;//int,商品序号
    public String good_SVG_Info = "";//SVG信息

    public GoodsInfo1(String goodsKey, String productId, String storeId, String name, String nameAlias, String description, int embedPhotosCount, List<GoodInfoPictures> pictures, int entityType, EmbedPhotoConfig ssConfig, int price, int sequence, String good_SVG_Info) {
        this.goodsKey = goodsKey;
        this.productId = productId;
        this.storeId = storeId;
        this.name = name;
        this.nameAlias = nameAlias;
        this.description = description;
        this.embedPhotosCount = embedPhotosCount;
        this.pictures = pictures;
        this.entityType = entityType;
        this.ssConfig = ssConfig;
        this.price = price;
        this.sequence = sequence;
        this.good_SVG_Info = good_SVG_Info;
    }

    public GoodsInfo1() {
    }

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

    public List<GoodInfoPictures> getPictures() {
        return pictures;
    }

    public void setPictures(List<GoodInfoPictures> pictures) {
        this.pictures = pictures;
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

    public String getGood_SVG_Info() {
        return good_SVG_Info;
    }

    public void setGood_SVG_Info(String good_SVG_Info) {
        this.good_SVG_Info = good_SVG_Info;
    }

    public int getEntityType() {
        return entityType;
    }

    public void setEntityType(int entityType) {
        this.entityType = entityType;
    }

    public EmbedPhotoConfig getSsConfig() {
        return ssConfig;
    }

    public void setSsConfig(EmbedPhotoConfig ssConfig) {
        this.ssConfig = ssConfig;
    }
}
