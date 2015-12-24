package com.pictureair.photopass.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by milo on 15/12/14.
 * 购物车实体类
 */
public class CartItemInfo1 implements Serializable{
    private String cartId = ""; //string,购物项主键
    private String storeId = ""; //string,商城编号
    private String goodsKey = ""; //string,商品项key
    private String productName = ""; //string,商品名称
    private String productNameAlias = ""; //商品名称别称
    private String description = ""; //string,商品描述
    private int embedPhotosCount = 0; //number,商品合成所需照片数
    private int unitPrice = 0; //number,商品单价
    private int qty = 0; //number,商品数量
    private int price = 0; //number,商品项价格
    private String[] pictures = null; //array<string>商品宣传图地址数组
    private List<CartPhotosInfo1> embedPhotos = null;//购物项内配备的照片数据
    private boolean isSelect = false;//商品是否应该被选中
    private boolean hasPhoto = false;//商品有没有添加图片
    private int showEdit = 0;//1，开始编辑数量，0，不需要编辑数量
    private int showPhotos = 0;//1,展示照片，0，不展示照片
    private boolean isFullPhotos = false;//商品是否加满照片
    private int cartProductType = 3;//商品类型 1:正常商品；2:照片类型商品；3:PP+商品
    private String cartProductImageUrl = "";//自己添加，购物车商品预览图

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getGoodsKey() {
        return goodsKey;
    }

    public void setGoodsKey(String goodsKey) {
        this.goodsKey = goodsKey;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductNameAlias() {
        return productNameAlias;
    }

    public void setProductNameAlias(String productNameAlias) {
        this.productNameAlias = productNameAlias;
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

    public int getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(int unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String[] getPictures() {
        return pictures;
    }

    public void setPictures(String[] pictures) {
        this.pictures = pictures;
    }

    public boolean getIsSelect() {
        return isSelect;
    }

    public void setIsSelect(boolean isSelect) {
        this.isSelect = isSelect;
    }

    public boolean getHasPhoto() {
        return hasPhoto;
    }

    public void setHasPhoto(boolean hasPhoto) {
        this.hasPhoto = hasPhoto;
    }

    public int getShowEdit() {
        return showEdit;
    }

    public void setShowEdit(int showEdit) {
        this.showEdit = showEdit;
    }

    public List<CartPhotosInfo1> getEmbedPhotos() {
        return embedPhotos;
    }

    public void setEmbedPhotos(List<CartPhotosInfo1> embedPhotos) {
        this.embedPhotos = embedPhotos;
    }

    public int getShowPhotos() {
        return showPhotos;
    }

    public void setShowPhotos(int showPhotos) {
        this.showPhotos = showPhotos;
    }

    public boolean isFullPhotos() {
        return isFullPhotos;
    }

    public void setIsFullPhotos(boolean isFullPhotos) {
        this.isFullPhotos = isFullPhotos;
    }

    public int getCartProductType() {
        return cartProductType;
    }

    public void setCartProductType(int cartProductType) {
        this.cartProductType = cartProductType;
    }

    public String getCartProductImageUrl() {
        return cartProductImageUrl;
    }

    public void setCartProductImageUrl(String cartProductImageUrl) {
        this.cartProductImageUrl = cartProductImageUrl;
    }
}
