package com.pictureair.photopass.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by milo on 15/12/13.
 * 商品信息
 */
public class GoodsInfo1 implements Serializable {
    private String goodsKey;//string, 商品key
    private String productId;//string,商品主键
    private String storeId;//string,商城主键
    private String name;//string,商品名称
    private String nameAlias;//string,商品别名
    private String description;//string,商品描述
    private int embedPhotosCount;//int,商品合成所需图片数量
    private List<GoodInfoPictures> pictures;//商品图片信息
    private int entityType; //int,商品虚拟／实体类型（0,1）
    private EmbedPhotoConfig ssConfig;//商品合成设置（单位：px）
    private int price;//int, 商品价格
    private int sequence;//int,商品序号
    public String good_SVG_Info;//SVG信息

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
