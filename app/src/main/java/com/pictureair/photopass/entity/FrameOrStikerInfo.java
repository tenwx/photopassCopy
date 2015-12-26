package com.pictureair.photopass.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 边框封装类
 * @author bauer_bao
 *
 */
public class FrameOrStikerInfo implements Parcelable{
	public String frameName;//边框名字
	public String frameOriginalPathLandscape;//边框水平原图
	public String frameOriginalPathPortrait;//边框竖直原图，也表示饰品的原图
	public String frameThumbnailPathLandscape400;//边框水平400缩略图
	public String frameThumbnailPathPortrait400;//边框竖直400缩略图
	public String frameThumbnailPathH160;//边框横160缩略图，也表示饰品的缩略图
	public String frameThumbnailPathV160;//边框垂直160缩略图
	public String locationId;//边框对应的locationId
	public int isActive;//边框是否可用
	public int onLine;//本地还是网络图片，1：网络，0：本地
	public int isDownload;//是否已经下载，1：已下载，0：未下载
	public int fileSize;//文件大小
//	public int needShow;//边框是否需要显示，因为地点变化，边框也会变化
	
	public static final Parcelable.Creator<FrameOrStikerInfo> CREATOR = new Creator<FrameOrStikerInfo>() {

		@Override
		public FrameOrStikerInfo[] newArray(int size) {
			return new FrameOrStikerInfo[size];
		}

		@Override
		public FrameOrStikerInfo createFromParcel(Parcel source) {
			return new FrameOrStikerInfo(source);
		}
	};

	public FrameOrStikerInfo() {

	}

	private FrameOrStikerInfo(Parcel source) {
		frameName = source.readString();
		frameOriginalPathLandscape = source.readString();
		frameOriginalPathPortrait = source.readString();
		frameThumbnailPathLandscape400 = source.readString();
		frameThumbnailPathPortrait400 = source.readString();
		frameThumbnailPathH160 = source.readString();
		frameThumbnailPathV160 = source.readString();
		locationId = source.readString();
		isActive = source.readInt();
		onLine = source.readInt();
		isDownload = source.readInt();
		fileSize = source.readInt();
//		needShow = source.readInt();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(frameName);
		dest.writeString(frameOriginalPathLandscape);
		dest.writeString(frameOriginalPathPortrait);
		dest.writeString(frameThumbnailPathLandscape400);
		dest.writeString(frameThumbnailPathPortrait400);
		dest.writeString(frameThumbnailPathH160);
		dest.writeString(frameThumbnailPathV160);
		dest.writeString(locationId);
		dest.writeInt(isActive);
		dest.writeInt(onLine);
		dest.writeInt(isDownload);
		dest.writeInt(fileSize);
//		dest.writeInt(needShow);
	}
}
