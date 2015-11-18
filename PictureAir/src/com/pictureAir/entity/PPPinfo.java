package com.pictureAir.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class PPPinfo implements Parcelable {

	public String PPP_ID;//绑定的ppp的id
	public String PPPCode;
	public int capacity;//总共ppp的数量
	public int days;
//	public String time;//绑定的时间，有可能会空
//	public int usedNumber;//已经使用的数量
//	public String pp1,pp2,pp3;//ppp对应三个pp的号码
	public List<BindInfo> bindInfo = new ArrayList<BindInfo>();
	public String ownOn;
	
	public static final Parcelable.Creator<PPPinfo> CREATOR = new Creator<PPPinfo>() {
		
		@Override
		public PPPinfo[] newArray(int size) {
			// TODO Auto-generated method stub
			return new PPPinfo[size];
		}
		
		@Override
		public PPPinfo createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new PPPinfo(source);
		}
	};
	
	public PPPinfo(){
		
	}
	
	private int length = 0;
	public PPPinfo(Parcel source){
		PPP_ID = source.readString();
		PPPCode = source.readString();
		capacity = source.readInt();
		days = source.readInt();
		
		length = source.readInt();
		if (length > 0) {
			bindInfo = new ArrayList<BindInfo>();
			Parcelable[] pars = source.readParcelableArray(PhotoInfo.class.getClassLoader());
			bindInfo = Arrays.asList(Arrays.asList(pars).toArray(new BindInfo[pars.length]));
		}
		ownOn = source.readString();
//		bindDates = source.readString();
//		PPlist = source.readString();
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(PPP_ID);
		dest.writeString(PPPCode);
		dest.writeInt(capacity);
		dest.writeInt(days);
		
		if (bindInfo == null)
			dest.writeInt(0);
		else
			dest.writeInt(bindInfo.size());

		if (bindInfo != null)
			dest.writeParcelableArray(bindInfo.toArray(new BindInfo[bindInfo.size()]), flags);
//		dest.writeString(bindDates);
//		dest.writeString(PPlist);
		dest.writeString(ownOn);
		
	}

}
