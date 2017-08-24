package com.pictureair.hkdlphotopass.entity;

import java.io.Serializable;

public class CartPhotosInfo implements Serializable {
    private String photoId = "";//购物车添加图片的URL
    private String photoUrl = "";//照片url地址
    private String cartPhotoCount = "";//购物车对应的数量
    private String cartPhotoLocalAlbum = "";//购物车对应照片在本地属于的相册名称
    private int isEncrypted;

    public CartPhotosInfo() {
    }

    public CartPhotosInfo(String photoId, String photoUrl, String cartPhotoCount, String cartPhotoLocalAlbum, int isEncrypted) {
        this.photoId = photoId;
        this.photoUrl = photoUrl;
        this.cartPhotoCount = cartPhotoCount;
        this.cartPhotoLocalAlbum = cartPhotoLocalAlbum;
        this.isEncrypted = isEncrypted;
    }

    public String getPhotoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getCartPhotoCount() {
        return cartPhotoCount;
    }

    public void setCartPhotoCount(String cartPhotoCount) {
        this.cartPhotoCount = cartPhotoCount;
    }

    public String getCartPhotoLocalAlbum() {
        return cartPhotoLocalAlbum;
    }

    public void setCartPhotoLocalAlbum(String cartPhotoLocalAlbum) {
        this.cartPhotoLocalAlbum = cartPhotoLocalAlbum;
    }

    public int getIsEncrypted() {
        return isEncrypted;
    }

    public void setIsEncrypted(int isEncrypted) {
        this.isEncrypted = isEncrypted;
    }
}
