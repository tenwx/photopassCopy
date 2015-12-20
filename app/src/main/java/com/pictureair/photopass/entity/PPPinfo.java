package com.pictureair.photopass.entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class PPPinfo implements Parcelable , Comparable<PPPinfo>{

	public String PPP_ID;//绑定的ppp的id
	public String PPPCode;   // 对应 pppId.
	public int capacity;//总共ppp的数量       //对应amount
	public int days;
	public List<BindPPInfo> bindInfo = new ArrayList<BindPPInfo>();   // bindInfo 的size 对应usedNumber
	public String ownOn;    //对应  time
	
	public String pp1,pp2,pp3;//ppp对应三个pp的号码
	public String expiredOn; //PPP的有效期。
	
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
			bindInfo = new ArrayList<BindPPInfo>();
			Parcelable[] pars = source.readParcelableArray(PhotoInfo.class.getClassLoader());
			bindInfo = Arrays.asList(Arrays.asList(pars).toArray(new BindPPInfo[pars.length]));
		}
		ownOn = source.readString();
		
		pp1 = source.readString();
		pp2 = source.readString();
		pp3 = source.readString();
		expiredOn = source.readString();
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
			dest.writeParcelableArray(bindInfo.toArray(new BindPPInfo[bindInfo.size()]), flags);
		dest.writeString(ownOn);
		
		dest.writeString(pp1);
		dest.writeString(pp2);
		dest.writeString(pp3);

		dest.writeString(expiredOn);
		
	}
	@Override
	public int compareTo(PPPinfo another) {
		// TODO Auto-generated method stub
		System.out.println("comparing-------------");
		//排序主要原则，未用完---》新的---》已用完
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		if (this.bindInfo.size() == this.capacity) {//已经用完，排在后面
			if (this.bindInfo.size() > another.bindInfo.size()) {//比较上一个是否是已经用完的，如果没用完，返回-1
				return 1;
			}else {//如果已经用完，根据时间排序
				return compareTime(another, sdf);
			}
		}else{//没有用完，排在前面
			if (this.bindInfo.size() == 0) {//如果没用过
				if (another.bindInfo.size() == another.capacity) {//如果前面的是已经用完了得，排在前面
					return -1;
				}else {
					if (this.bindInfo.size() > another.bindInfo.size()) {//大于前面的值，排在前面
						return compareTime(another, sdf);
					}else {//小于等于前面的值，排在后面
						return 1;
					}
					
				}
			}else {//用过
				if (another.bindInfo.size() == another.capacity) {//如果前面的是已经用完了得，排在前面
					return -1;
				}else {
					
					return compareTime(another, sdf);
				}
				
			}
		}
	}
	
	//比较时间
	private int compareTime(PPPinfo another, SimpleDateFormat sdf) {
		// TODO Auto-generated method stub
		try {
			if (!"".equals(this.ownOn) && !"".equals(another.ownOn)) {
				System.out.println(this.ownOn+"_"+another.ownOn);
				Date date1 = sdf.parse(this.ownOn);
				Date date2 = sdf.parse(another.ownOn);
				if (date1.after(date2))
					return -1;
				else
					return 1;
			} else if (!"".equals(this.ownOn) && "".equals(another.ownOn)) {
				System.out.println(this.ownOn+"_"+another.ownOn);
				return -1;
			} else if ("".equals(this.ownOn) && !"".equals(another.ownOn)) {
				System.out.println(this.ownOn+"_"+another.ownOn);
				return 1;
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

}
