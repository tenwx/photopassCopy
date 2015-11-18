package com.pictureAir.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class PPinfo implements Parcelable {

	public String customerId;//pp号
	public String shootdate;//pp时间
	public int photocount;//pp对应数量
	
	public static final Parcelable.Creator<PPinfo> CREATOR = new Creator<PPinfo>() {
		
		@Override
		public PPinfo[] newArray(int size) {
			// TODO Auto-generated method stub
			return new PPinfo[size];
		}
		
		@Override
		public PPinfo createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new PPinfo(source);
		}
	};
	
	public PPinfo(){
		
	}
	
	public PPinfo(Parcel source){
		customerId = source.readString();
		shootdate = source.readString();
		photocount = source.readInt();
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(customerId);
		dest.writeString(shootdate);
		dest.writeInt(photocount);
		
	}

}
