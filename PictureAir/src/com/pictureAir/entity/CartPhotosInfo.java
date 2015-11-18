package com.pictureAir.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class CartPhotosInfo implements Parcelable{
	public String cart_photoUrl;//购物车添加图片的URL
	public String cart_photoId;//购物车添加图片的ID
	public String cart_photoCount;//购物车对应的数量
	public String cart_photo_local_album;//购物车对应照片在本地属于的相册名称

	public static final Parcelable.Creator<CartPhotosInfo> CREATOR = new Creator<CartPhotosInfo>() {

		@Override
		public CartPhotosInfo[] newArray(int size) {
			// TODO Auto-generated method stub
			return new CartPhotosInfo[size];
		}

		@Override
		public CartPhotosInfo createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new CartPhotosInfo(source);
		}
	};

	public CartPhotosInfo() {

	}

	private CartPhotosInfo(Parcel source) {
		cart_photoUrl = source.readString();
		cart_photoId = source.readString();
		cart_photoCount = source.readString();
		cart_photo_local_album = source.readString();
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(cart_photoUrl);
		dest.writeString(cart_photoId);
		dest.writeString(cart_photoCount);
		dest.writeString(cart_photo_local_album);
	}
}
