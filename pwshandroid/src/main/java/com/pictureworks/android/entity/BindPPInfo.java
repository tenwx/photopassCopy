package com.pictureworks.android.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class BindPPInfo implements Parcelable {

	public String customerId; // pp号码
	public String userids; // userids。有可能多个用户。 用到的时候再去解析。
	public String bindDate; // PP 绑定的日期
	
	
	public BindPPInfo(){
		
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

	public BindPPInfo(Parcel source) {
		customerId = source.readString();
		userids = source.readString();
		bindDate = source.readString();
	}

	public static final Creator<BindPPInfo> CREATOR = new Creator<BindPPInfo>() {

		@Override
		public BindPPInfo[] newArray(int size) {
			// TODO Auto-generated method stub
			return new BindPPInfo[size];
		}

		@Override
		public BindPPInfo createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new BindPPInfo(source);
		}
	};

}
