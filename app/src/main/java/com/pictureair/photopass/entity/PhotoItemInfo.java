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

public class PhotoItemInfo implements Parcelable, Comparable<PhotoItemInfo> {
	public String locationId;// 地点ID
	public String locationIds;//地点ID集合
	public String shootTime;// 拍摄时间 年月日
	public String shootOn;//拍摄时间 年月日时分秒
	public String place;// 拍摄地点
	public List<PhotoInfo> list = new ArrayList<PhotoInfo>();//图片列表
	public String placeUrl;// 地点图片url
	public int islove;// 地点是否收藏,0：未收藏；1：收藏
	public double latitude;
	public double longitude;
	
	public static final Parcelable.Creator<PhotoItemInfo> CREATOR = new Creator<PhotoItemInfo>() {

		@Override
		public PhotoItemInfo[] newArray(int size) {
			// TODO Auto-generated method stub
			return new PhotoItemInfo[size];
		}

		@Override
		public PhotoItemInfo createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new PhotoItemInfo(source);
		}
	};

	public PhotoItemInfo() {

	}

	private int length = 0;

	private PhotoItemInfo(Parcel source) {
		locationId = source.readString();
		locationIds = source.readString();
		shootTime = source.readString();
		shootOn = source.readString();
		place = source.readString();
		length = source.readInt();
		if (length > 0) {
			list = new ArrayList<PhotoInfo>();
			Parcelable[] pars = source.readParcelableArray(PhotoInfo.class.getClassLoader());
			list = Arrays.asList(Arrays.asList(pars).toArray(new PhotoInfo[pars.length]));
		}
		placeUrl = source.readString();
		islove = source.readInt();
		latitude = source.readDouble();
		longitude = source.readDouble();
//		length = source.readInt();
//		if (length > 0) {
//			gps = new String[length];
//			source.readStringArray(gps);
//		}
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(locationId);
		dest.writeString(locationIds);
		dest.writeString(shootTime);
		dest.writeString(shootOn);

		dest.writeString(place);

		if (list == null)
			dest.writeInt(0);
		else
			dest.writeInt(list.size());

		if (list != null)
			dest.writeParcelableArray(list.toArray(new PhotoInfo[list.size()]), flags);

		dest.writeString(placeUrl);

		dest.writeInt(islove);

		dest.writeDouble(latitude);
		
		dest.writeDouble(longitude);

	}

	@Override
	public int compareTo(PhotoItemInfo another) {
		// TODO Auto-generated method stub
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		PictureAirLog.out("sort photoItemInfo------->");
		try {
			if (this.shootOn != null && another.shootOn != null) {
				PictureAirLog.out("this---->" + this.shootOn + "another----->"+ another.shootOn);
				Date date1 = sdf.parse(this.shootOn);
				Date date2 = sdf.parse(another.shootOn);
				if (date1.after(date2))
					return -1;
				else
					return 1;
			} else if (this.shootOn != null && another.shootOn == null) {
				return -1;
			} else if (this.shootOn == null && another.shootOn != null) {
				return 1;
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;
	}

}
