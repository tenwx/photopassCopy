package com.pictureair.photopass.entity;

import java.io.Serializable;

public class CartPhotosInfo1 implements Serializable{
	public String cartPhotoUrl;//购物车添加图片的URL
	public String cartPhotoId;//购物车添加图片的ID
	public String cartPhotoCount;//购物车对应的数量
	public String cartPhotoLocalAlbum;//购物车对应照片在本地属于的相册名称

	public String getCartPhotoUrl() {
		return cartPhotoUrl;
	}

	public void setCartPhotoUrl(String cartPhotoUrl) {
		this.cartPhotoUrl = cartPhotoUrl;
	}

	public String getCartPhotoId() {
		return cartPhotoId;
	}

	public void setCartPhotoId(String cartPhotoId) {
		this.cartPhotoId = cartPhotoId;
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
}
