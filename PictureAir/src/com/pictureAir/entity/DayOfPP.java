package com.pictureAir.entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

public class DayOfPP implements Parcelable,Comparable<DayOfPP>{
	public String time;//pp绑定的时间
	public String ppId;//pp的id
	public int number;//pp对应的照片数量
	public int isSelected;//0，未选中，1，选中，2，不可选
	public String pp_url1;//pp的第一张图片
	public String pp_url2;//pp的第二张图片
	public String pp_url3;//pp的第三张图片
	public String pp_url4;
	public String pp_url5;
	public String pp_url6;
	public String pp_url7;
	public String pp_url8;
	public String pp_url9;
	public String pp_url10;
	public String pp_url11;
	public String pp_url12;
	
	public DayOfPP() {

	}

	public DayOfPP(Parcel source) {
		time = source.readString();
		ppId = source.readString();
		number = source.readInt();
		isSelected = source.readInt();
		pp_url1 = source.readString();
		pp_url2 = source.readString();
		pp_url3 = source.readString();
		
		pp_url4 = source.readString();
		pp_url5 = source.readString();
		pp_url6 = source.readString();
		pp_url7 = source.readString();
		pp_url8 = source.readString();
		pp_url9 = source.readString();
		pp_url10 = source.readString();
		pp_url11 = source.readString();
		pp_url12 = source.readString();
	}

	

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(time);
		dest.writeString(ppId);
		dest.writeInt(number);
		dest.writeInt(isSelected);
		dest.writeString(pp_url1);
		dest.writeString(pp_url2);
		dest.writeString(pp_url3);
		dest.writeString(pp_url4);
		dest.writeString(pp_url5);
		dest.writeString(pp_url6);
		dest.writeString(pp_url7);
		dest.writeString(pp_url8);
		dest.writeString(pp_url9);
		dest.writeString(pp_url10);
		dest.writeString(pp_url11);
		dest.writeString(pp_url12);
	}
	
	
	
   public static final Parcelable.Creator<DayOfPP> CREATOR = new Creator<DayOfPP>() {
		
		@Override
		public DayOfPP[] newArray(int size) {
			return new DayOfPP[size];
		}
		
		@Override
		public DayOfPP createFromParcel(Parcel source) {
			return new DayOfPP(source);
		}
	};
	

	
	//根据 图片
	@Override
	public int compareTo(DayOfPP another) {
		// TODO Auto-generated method stub
		if (this.number<another.number) {
			return 1;
		}else{
			return -1;
		}
		
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	
	//比较时间
//		private int compareTime(DayOfPP another, SimpleDateFormat sdf) {
//			// TODO Auto-generated method stub
//			Log.e("this", "this.time :"+this.time);
//			try {
//				if (!"".equals(this.time) && !"".equals(another.time)) {
//					System.out.println(this.time+"_"+another.time);
//					Date date1 = sdf.parse(this.time);
//					Date date2 = sdf.parse(another.time);
//					if (date1.after(date2))
//						return -1;
//					else
//						return 1;
//				} else if (!"".equals(this.time) && "".equals(another.time)) {
//					System.out.println(this.time+"_"+another.time);
//					return -1;
//				} else if ("".equals(this.time) && !"".equals(another.time)) {
//					System.out.println(this.time+"_"+another.time);
//					return 1;
//				}
//			} catch (ParseException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			return 0;
//		}
	
	
}
