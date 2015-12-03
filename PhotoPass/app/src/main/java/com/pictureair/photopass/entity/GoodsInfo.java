package com.pictureair.photopass.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class GoodsInfo implements Parcelable {
	public String good_name;//商品名字，根据这个字段进行比较
	public String good_nameAlias;//商品别名，用来在UI上显示
	public String good_price;//商品价格
	public String good_previewUrls;//商品预览图
	public String good_detail;//商品详情
	public String good_productId;//商品ID
	public String good_promotionPrice;//商品促销价
	public int good_type;//商品类型，1:正常商品；2:照片类型商品；3:PP+商品
	public int good_embedPhotoCount;//最多放置svg图片的数量
	public String good_SVG_Info;//SVG信息
	
	public static final Parcelable.Creator<GoodsInfo> CREATOR = new Creator<GoodsInfo>() {

		@Override
		public GoodsInfo[] newArray(int size) {
			// TODO Auto-generated method stub
			return new GoodsInfo[size];
		}

		@Override
		public GoodsInfo createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new GoodsInfo(source);
		}
	};

	public GoodsInfo() {

	}

	private GoodsInfo(Parcel source) {
		good_name = source.readString();
		good_nameAlias = source.readString();
		good_price = source.readString();
		good_previewUrls = source.readString();
		good_detail = source.readString();
		good_productId = source.readString();
		good_promotionPrice = source.readString();
		good_SVG_Info = source.readString();
		good_type = source.readInt();
		good_embedPhotoCount = source.readInt();
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(good_name);
		dest.writeString(good_nameAlias);
		dest.writeString(good_price);
		dest.writeString(good_previewUrls);
		dest.writeString(good_detail);
		dest.writeString(good_productId);
		dest.writeString(good_promotionPrice);
		dest.writeString(good_SVG_Info);
		dest.writeInt(good_type);
		dest.writeInt(good_embedPhotoCount);
	}

}
