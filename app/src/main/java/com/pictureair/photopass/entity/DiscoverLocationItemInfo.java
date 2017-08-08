package com.pictureair.photopass.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * discover页面每个item的对象的封装类
 * @author bauer_bao
 *
 */
public class DiscoverLocationItemInfo implements Parcelable{
	public String locationId;//地点ID
	public String locationIds;//小地点的ID
	public String placeENName;//地点英文名称
	public String placeCHName;//地点中文名称
	public String placeDetailENIntroduce;//地点介绍：英文
	public String placeDetailCHIntroduce;//地点介绍：中文
	public String placeUrl;//地点图片url
	public String popularity;//人气
	public int islove;//地点是否收藏,0：未收藏；1：收藏
	public int showDetail;//地点详情是否显示,0：未显示；1：显示
	public double latitude;//地点纬度
	public double longitude;//经度
	public int isShow;//是否显示，1，显示，0，不显示
	
	public static final Parcelable.Creator<DiscoverLocationItemInfo> CREATOR = new Creator<DiscoverLocationItemInfo>() {

		@Override
		public DiscoverLocationItemInfo[] newArray(int size) {
			return new DiscoverLocationItemInfo[size];
		}

		@Override
		public DiscoverLocationItemInfo createFromParcel(Parcel source) {
			return new DiscoverLocationItemInfo(source);
		}
	};

	public DiscoverLocationItemInfo() {

	}

	private DiscoverLocationItemInfo(Parcel source) {
		locationId = source.readString();
		locationIds = source.readString();
		placeENName = source.readString();
		placeCHName = source.readString();
		placeDetailENIntroduce = source.readString();
		placeDetailCHIntroduce = source.readString();
		placeUrl = source.readString();
		popularity = source.readString();
		islove = source.readInt();
		showDetail = source.readInt();
		latitude = source.readDouble();
		longitude = source.readDouble();
		isShow = source.readInt();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(locationId);
		dest.writeString(locationIds);
		dest.writeString(placeENName);
		dest.writeString(placeCHName);
		dest.writeString(placeDetailENIntroduce);
		dest.writeString(placeDetailCHIntroduce);
		dest.writeString(placeUrl);
		dest.writeString(popularity);
		dest.writeInt(islove);
		dest.writeInt(showDetail);
		dest.writeDouble(latitude);
		dest.writeDouble(longitude);
		dest.writeInt(isShow);
	}

}
