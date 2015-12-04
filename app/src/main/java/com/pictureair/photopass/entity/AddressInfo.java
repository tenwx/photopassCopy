package com.pictureair.photopass.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class AddressInfo implements Parcelable {
	public String address_username;//用户姓名
	public String address_telephone;//手机号
	public String address_homephone;//固定电话
	public String address_id;//地址ID
	public String address_country;//国家
	public String address_detail;//地址详情
	
	public static final Parcelable.Creator<AddressInfo> CREATOR = new Creator<AddressInfo>() {

		@Override
		public AddressInfo[] newArray(int size) {
			// TODO Auto-generated method stub
			return new AddressInfo[size];
		}

		@Override
		public AddressInfo createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new AddressInfo(source);
		}
	};

	public AddressInfo() {

	}

	private AddressInfo(Parcel source) {
		address_username = source.readString();
		address_telephone = source.readString();
		address_homephone = source.readString();
		address_id = source.readString();
		address_country = source.readString();
		address_detail = source.readString();
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(address_username);
		dest.writeString(address_telephone);
		dest.writeString(address_homephone);
		dest.writeString(address_id);
		dest.writeString(address_country);
		dest.writeString(address_detail);
	}

}
