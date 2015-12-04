package com.pictureair.photopass.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class CartItemInfo implements Parcelable{
	public String cart_productName;//商品名字
	public String cart_productId;//商品ID
	public int cart_productType;//商品类型 1:正常商品；2:照片类型商品；3:PP+商品
	public double cart_originalPrice;//商品原价
	public double cart_promotionPrice;//商品现价
	public String cart_productIntroduce;//商品介绍
	public String cart_productImageUrl;//商品预览图URL
	public int cart_quantity;//商品数量
	public String cart_id;//购物车对应的ID
	public String cart_storeId;//购物车对应的StoreId
	public boolean isSelect;//商品是否应该被选中
	public boolean hasPhoto;//商品有没有添加图片
	public boolean isFullPhotos;//商品是否加满照片
	public int cart_embedPhotoCount;//最多可以加的图片数量
	public int show_edit;//1，开始编辑数量，0，不需要编辑数量
	public int showPhotos;//1,展示照片，0，不展示照片
	public List<CartPhotosInfo> cart_photoUrls;//商品添加图片的详情列表
	
	public static final Parcelable.Creator<CartItemInfo> CREATOR = new Creator<CartItemInfo>() {

		@Override
		public CartItemInfo[] newArray(int size) {
			// TODO Auto-generated method stub
			return new CartItemInfo[size];
		}

		@Override
		public CartItemInfo createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new CartItemInfo(source);
		}
	};

	public CartItemInfo() {

	}

	private int length = 0;

	private CartItemInfo(Parcel source) {
		cart_productName = source.readString();
		cart_productId = source.readString();
		cart_productType = source.readInt();
		cart_originalPrice = source.readDouble();
		cart_promotionPrice = source.readDouble();
		cart_productIntroduce = source.readString();
		cart_productImageUrl = source.readString();
		cart_quantity = source.readInt();
		cart_id = source.readString();
		cart_storeId = source.readString();
		cart_embedPhotoCount = source.readInt();
		show_edit = source.readInt();
		showPhotos = source.readInt();
		isSelect = (source.readInt() == 1)?true:false;
		hasPhoto = (source.readInt() == 1)?true:false;
		isFullPhotos = (source.readInt() == 1)?true:false;
		
		length = source.readInt();
		if (length > 0) {
			cart_photoUrls = new ArrayList<CartPhotosInfo>();
			Parcelable[] parcelables = source.readParcelableArray(CartPhotosInfo.class.getClassLoader());
			cart_photoUrls = Arrays.asList(Arrays.asList(parcelables).toArray(new CartPhotosInfo[parcelables.length]));
		}
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(cart_productName);
		dest.writeString(cart_productId);
		dest.writeInt(cart_productType);
		dest.writeDouble(cart_originalPrice);
		dest.writeDouble(cart_promotionPrice);
		dest.writeString(cart_productIntroduce);
		dest.writeString(cart_productImageUrl);
		dest.writeInt(cart_quantity);
		dest.writeString(cart_id);
		dest.writeString(cart_storeId);
		dest.writeInt(cart_embedPhotoCount);
		dest.writeInt(show_edit);
		dest.writeInt(showPhotos);
		dest.writeInt(isSelect?1:0);
		dest.writeInt(hasPhoto?1:0);
		dest.writeInt(isFullPhotos?1:0);

		if (cart_photoUrls == null)
			dest.writeInt(0);
		else
			dest.writeInt(cart_photoUrls.size());

		if (cart_photoUrls != null)
			dest.writeParcelableArray(cart_photoUrls.toArray(new CartPhotosInfo[cart_photoUrls.size()]), flags);

	}
}
