package com.pictureAir.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class PPCodeInfo implements Parcelable{
	public String ppCode;//照片对应pp号码
	public String onePreviewURL;//其中一张的原始路径
	public String oneThumbnail_128;//128的缩略图
	public String oneThumbnail_512;//512的缩略图
	public int photoCount;//照片数量
	public int isUpgrade;//是否已经升级
	
	public String shootDate;//照片拍摄日期
	public int isHidden;//是否已经隐藏（0否，1是）
	
	public static final Parcelable.Creator<PPCodeInfo> CREATOR = new Creator<PPCodeInfo>() {

		@Override
		public PPCodeInfo[] newArray(int size) {
			// TODO Auto-generated method stub
			return new PPCodeInfo[size];
		}

		@Override
		public PPCodeInfo createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new PPCodeInfo(source);
		}
	};
	
	public PPCodeInfo(){
		
	}
	
	public PPCodeInfo(Parcel source){
		ppCode = source.readString();
		onePreviewURL = source.readString();
		oneThumbnail_128 = source.readString();
		oneThumbnail_512 = source.readString();
		photoCount = source.readInt();
		isUpgrade = source.readInt();
		
		shootDate = source.readString();
		isHidden = source.readInt();
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(ppCode);
		dest.writeString(onePreviewURL);
		dest.writeString(oneThumbnail_128);
		dest.writeString(oneThumbnail_512);
		dest.writeInt(photoCount);
		dest.writeInt(isUpgrade);
		
		dest.writeString(shootDate);
		dest.writeInt(isHidden);
	}

	@Override
	public String toString() {
		return "PPCodeInfo [ppCode=" + ppCode + ", onePreviewURL="
				+ onePreviewURL + ", oneThumbnail_128=" + oneThumbnail_128
				+ ", oneThumbnail_512=" + oneThumbnail_512 + ", photoCount="
				+ photoCount + ", isUpgrade=" + isUpgrade + "]";
	}
}
	
