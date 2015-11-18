package com.pictureAir.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class BindInfo implements Parcelable {

	public String customerId; // pp号码
	public String userids; // userids。有可能多个用户。 用到的时候再去解析。
	public String bindDate; // PP 绑定的日期
	
	
	public BindInfo(){
		
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	


	@Override
	public void writeToParcel(Parcel dest, int arg1) {
		// TODO Auto-generated method stub
		dest.writeString(customerId);
		dest.writeString(userids);
		dest.writeString(bindDate);
	}

	public BindInfo(Parcel source) {
		customerId = source.readString();
		userids = source.readString();
		bindDate = source.readString();
	}

	public static final Parcelable.Creator<BindInfo> CREATOR = new Creator<BindInfo>() {

		@Override
		public BindInfo[] newArray(int size) {
			// TODO Auto-generated method stub
			return new BindInfo[size];
		}

		@Override
		public BindInfo createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new BindInfo(source);
		}
	};

}
