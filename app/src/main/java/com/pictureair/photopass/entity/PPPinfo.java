package com.pictureair.photopass.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.pictureair.photopass.util.PictureAirLog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PPPinfo implements Parcelable , Comparable<PPPinfo>{

	public String PPP_ID;//绑定的ppp的id
	public String PPPCode;   // 对应 pppId.
	public int capacity;//总共ppp的数量       //对应amount
	public int days;
	public List<BindPPInfo> bindInfo = new ArrayList<BindPPInfo>();   // bindInfo 的size 对应usedNumber
	public String ownOn;    //对应  time
	
	public String pp1,pp2,pp3;//ppp对应三个pp的号码
	public String expiredOn; //PPP的有效期。
	public String pppCardBg;//卡片背景图片
	public int expericePPP;//体验卡,0:不是  1：是
	public int expired;//1：过期，0：未过期

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
		pppCardBg = source.readString();
		expericePPP = source.readInt();
		expired = source.readInt();
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
		dest.writeString(pppCardBg);
		dest.writeInt(expericePPP);
		dest.writeInt(expired);

	}
	@Override
	public int compareTo(PPPinfo another) {
		/**
		 * 已激活和未激活为一类，放在上面，按时间降序排列
		 * 已用完和已过期为一类，放在下面，按时间降序排序
		 */
		PictureAirLog.out("comparing-------------");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		if (this.expired == 1 || this.bindInfo.size() == this.capacity) {//已经用完，或者已经过期，排在后面
			if (another.expired == 1 || another.bindInfo.size() == another.capacity) {//上一张已用完或者已过期
				return compareTime(another, sdf);
			} else {
				return 1;
			}
		} else {//未激活或者未用完
			if (another.expired == 1 || another.bindInfo.size() == another.capacity) {//上一张已用完或者已过期
				return -1;
			} else {
				return compareTime(another, sdf);
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
