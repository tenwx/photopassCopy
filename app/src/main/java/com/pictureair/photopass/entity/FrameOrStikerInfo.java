package com.pictureair.photopass.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 边框封装类
 * @author bauer_bao
 *
 */
@Entity
public class FrameOrStikerInfo implements Parcelable{
	@Id
	private Long id;//自增长id
	private String frameName;//边框名字
	private String originalPathLandscape;//边框水平原图
	private String originalPathPortrait;//边框竖直原图，也表示饰品的原图
	private String thumbnailPathLandscape400;//边框水平400缩略图
	private String thumbnailPathPortrait400;//边框竖直400缩略图
	private String thumbnailPathH160;//边框横160缩略图，也表示饰品的缩略图
	private String thumbnailPathV160;//边框垂直160缩略图
	private String locationId;//边框对应的locationId
	private int isActive;//边框是否可用
	private int onLine;//本地还是网络图片，1：网络，0：本地
	private int isDownload;//是否已经下载，1：已下载，0：未下载
	private int fileSize;//文件大小
	private int fileType;

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
		id = source.readLong();
		frameName = source.readString();
		originalPathLandscape = source.readString();
		originalPathPortrait = source.readString();
		thumbnailPathLandscape400 = source.readString();
		thumbnailPathPortrait400 = source.readString();
		thumbnailPathH160 = source.readString();
		thumbnailPathV160 = source.readString();
		locationId = source.readString();
		isActive = source.readInt();
		onLine = source.readInt();
		isDownload = source.readInt();
		fileSize = source.readInt();
		fileType = source.readInt();
	}

	@Generated(hash = 1722241396)
	public FrameOrStikerInfo(Long id, String frameName, String originalPathLandscape,
									String originalPathPortrait, String thumbnailPathLandscape400,
									String thumbnailPathPortrait400, String thumbnailPathH160, String thumbnailPathV160,
									String locationId, int isActive, int onLine, int isDownload, int fileSize, int fileType) {
					this.id = id;
					this.frameName = frameName;
					this.originalPathLandscape = originalPathLandscape;
					this.originalPathPortrait = originalPathPortrait;
					this.thumbnailPathLandscape400 = thumbnailPathLandscape400;
					this.thumbnailPathPortrait400 = thumbnailPathPortrait400;
					this.thumbnailPathH160 = thumbnailPathH160;
					this.thumbnailPathV160 = thumbnailPathV160;
					this.locationId = locationId;
					this.isActive = isActive;
					this.onLine = onLine;
					this.isDownload = isDownload;
					this.fileSize = fileSize;
					this.fileType = fileType;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(frameName);
		dest.writeString(originalPathLandscape);
		dest.writeString(originalPathPortrait);
		dest.writeString(thumbnailPathLandscape400);
		dest.writeString(thumbnailPathPortrait400);
		dest.writeString(thumbnailPathH160);
		dest.writeString(thumbnailPathV160);
		dest.writeString(locationId);
		dest.writeInt(isActive);
		dest.writeInt(onLine);
		dest.writeInt(isDownload);
		dest.writeInt(fileSize);
		dest.writeInt(fileType);
	}

	public Long getId() {
					return this.id;
	}

	public void setId(Long id) {
					this.id = id;
	}

	public String getFrameName() {
					return this.frameName;
	}

	public void setFrameName(String frameName) {
					this.frameName = frameName;
	}

	public String getOriginalPathLandscape() {
					return this.originalPathLandscape;
	}

	public void setOriginalPathLandscape(String originalPathLandscape) {
					this.originalPathLandscape = originalPathLandscape;
	}

	public String getOriginalPathPortrait() {
					return this.originalPathPortrait;
	}

	public void setOriginalPathPortrait(String originalPathPortrait) {
					this.originalPathPortrait = originalPathPortrait;
	}

	public String getThumbnailPathLandscape400() {
					return this.thumbnailPathLandscape400;
	}

	public void setThumbnailPathLandscape400(String thumbnailPathLandscape400) {
					this.thumbnailPathLandscape400 = thumbnailPathLandscape400;
	}

	public String getThumbnailPathPortrait400() {
					return this.thumbnailPathPortrait400;
	}

	public void setThumbnailPathPortrait400(String thumbnailPathPortrait400) {
					this.thumbnailPathPortrait400 = thumbnailPathPortrait400;
	}

	public String getThumbnailPathH160() {
					return this.thumbnailPathH160;
	}

	public void setThumbnailPathH160(String thumbnailPathH160) {
					this.thumbnailPathH160 = thumbnailPathH160;
	}

	public String getThumbnailPathV160() {
					return this.thumbnailPathV160;
	}

	public void setThumbnailPathV160(String thumbnailPathV160) {
					this.thumbnailPathV160 = thumbnailPathV160;
	}

	public String getLocationId() {
					return this.locationId;
	}

	public void setLocationId(String locationId) {
					this.locationId = locationId;
	}

	public int getIsActive() {
					return this.isActive;
	}

	public void setIsActive(int isActive) {
					this.isActive = isActive;
	}

	public int getOnLine() {
					return this.onLine;
	}

	public void setOnLine(int onLine) {
					this.onLine = onLine;
	}

	public int getIsDownload() {
					return this.isDownload;
	}

	public void setIsDownload(int isDownload) {
					this.isDownload = isDownload;
	}

	public int getFileSize() {
					return this.fileSize;
	}

	public void setFileSize(int fileSize) {
					this.fileSize = fileSize;
	}

	public int getFileType() {
					return this.fileType;
	}

	public void setFileType(int fileType) {
					this.fileType = fileType;
	}
}
