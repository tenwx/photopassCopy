package com.pictureAir.entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * ppp封装类
 * @author bauer_bao
 *
 */
public class DayOfPPP implements Parcelable, Comparable<DayOfPPP>{
	public String time;//绑定的时间，有可能会空
	public String pppId;//绑定的ppp的id
	public int usedNumber;//已经使用的数量
	public int amount;//总共ppp的数量
	public String pp1,pp2,pp3;//ppp对应三个pp的号码
	public String pppBgUrl;//ppp背景图的照片url,只取第一个pp对应的第一张照片
	
	public DayOfPPP(){
		
	}
	public DayOfPPP(Parcel source){
		time = source.readString();
		pppId = source.readString();
		usedNumber = source.readInt();
		amount = source.readInt();
		pp1 = source.readString();
		pp2 = source.readString();
		pp3 = source.readString();
		pppBgUrl = source.readString();
	}
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(time);
		dest.writeString(pppId);
		dest.writeInt(usedNumber);
		dest.writeInt(amount);
		dest.writeString(pp1);
		dest.writeString(pp2);
		dest.writeString(pp3);
		dest.writeString(pppBgUrl);
	}
	public static final Parcelable.Creator<DayOfPPP> CREATOR = new Creator<DayOfPPP>() {
		
		@Override
		public DayOfPPP[] newArray(int size) {
			return new DayOfPPP[size];
		}
		
		@Override
		public DayOfPPP createFromParcel(Parcel source) {
			return new DayOfPPP(source);
		}
	};
	@Override
	public int compareTo(DayOfPPP another) {
		System.out.println("comparing-------------");
		//排序主要原则，未用完---》新的---》已用完
		//排序次要原则，时间新---》旧
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		if (this.usedNumber == this.amount) {//已经用完，排在后面
			if (this.usedNumber > another.usedNumber) {//比较上一个是否是已经用完的，如果没用完，返回-1
				return 1;
			}else {//如果已经用完，根据时间排序
				return compareTime(another, sdf);
			}
		}else{//没有用完，排在前面
			if (this.usedNumber == 0) {//如果没用过
				if (another.usedNumber == another.amount) {//如果前面的是已经用完了得，排在前面
					return -1;
				}else {
					if (this.usedNumber > another.usedNumber) {//大于前面的值，排在前面
						return compareTime(another, sdf);
					}else {//小于等于前面的值，排在后面
						return 1;
					}
					
				}
			}else {//用过
				if (another.usedNumber == another.amount) {//如果前面的是已经用完了得，排在前面
					return -1;
				}else {
					
					return compareTime(another, sdf);
				}
				
			}
//			if (this.usedNumber > another.usedNumber) {//已经使用个数多的，排在前面
//				return -1;
//			}else if (this.usedNumber < another.usedNumber) {//已经使用个数少的，排在后面
//				if (another.usedNumber == another.amount) {//如果前面的是已经用完了得，排在前面
//					return -1;
//				}else {
//					return 1;
//				}
//			}else {//数量一样的
//				return compareTime(another, sdf);
//			}
		}
	}
	//比较时间
	private int compareTime(DayOfPPP another, SimpleDateFormat sdf) {
		// TODO Auto-generated method stub
		try {
			if (!"".equals(this.time) && !"".equals(another.time)) {
				System.out.println(this.time+"_"+another.time);
				Date date1 = sdf.parse(this.time);
				Date date2 = sdf.parse(another.time);
				if (date1.after(date2))
					return -1;
				else
					return 1;
			} else if (!"".equals(this.time) && "".equals(another.time)) {
				System.out.println(this.time+"_"+another.time);
				return -1;
			} else if ("".equals(this.time) && !"".equals(another.time)) {
				System.out.println(this.time+"_"+another.time);
				return 1;
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
}
