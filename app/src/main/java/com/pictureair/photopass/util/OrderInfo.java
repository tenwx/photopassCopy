package com.pictureair.photopass.util;

import android.os.Parcel;
import android.os.Parcelable;

public class OrderInfo implements Parcelable {
	public int ID;// 订单编号
	public String name;// 收货人名称
	public String phone;// 收货人电话
	public String landline;// 收货人座机
	public String province;// 省份
	public String city;// 城市
	public String country;// 县区
	public String address;// 详细地址
	public String isSelected; // 选择状态 0 -未选中 1-- 选中
	public String china;// 中国
	public String other;// 其他

	public OrderInfo() {

	}

	/**
	 * 序列化实体类
	 */
	public static final Creator<OrderInfo> CREATOR = new Creator<OrderInfo>() {

		@Override
		public OrderInfo createFromParcel(Parcel source) {
			return new OrderInfo(source);
		}

		@Override
		public OrderInfo[] newArray(int size) {
			return new OrderInfo[size];
		}
	};

	private OrderInfo(Parcel source) {
		ID = source.readInt();
		name = source.readString();
		phone = source.readString();
		landline = source.readString();
		province = source.readString();
		city = source.readString();
		country = source.readString();
		address = source.readString();
		isSelected = source.readString();
		china = source.readString();
		other = source.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(ID);
		dest.writeString(name);
		dest.writeString(phone);
		dest.writeString(landline);
		dest.writeString(province);
		dest.writeString(city);
		dest.writeString(country);
		dest.writeString(address);
		dest.writeString(isSelected);
		dest.writeString(china);
		dest.writeString(other);
	}
}
