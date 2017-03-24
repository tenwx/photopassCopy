package com.pictureair.photopass.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.pictureair.photopass.util.PictureAirLog;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 图片封装类
 *
 * @author bauer_bao
 */
@Entity
public class PhotoInfo implements Parcelable, Comparable<PhotoInfo>{

	@Id
	private Long id;//自增长id
	private String photoId;//网络图片的话就是photoId，本地图片的话，就是原始路径
	private String photoPassCode;//网络图片的PPCode
	private String shootDate;// 拍摄时间 年月日，用于页面显示，以前的shootTime: 2016-10-20
	private String photoThumbnail_128;//缩略图128尺寸路径
	private String photoThumbnail_512;//缩略图512尺寸的路径
	private String photoThumbnail_1024;//缩略图1024尺寸的路径
	private String photoOriginalURL;//图片的原始路径
	private String locationId;//照片拍摄地点
	private String strShootOn;//拍摄时间 年月日时分秒，用来数据库的排序  2016-10-20 10:06:57
	private String shareURL;//网络图片分享的URL
	private String adURL;//广告链接
	private String receivedOn;//每张图片到服务器的时间，用于刷新加载操作  2016-10-20T02:06:57.000Z
	private String exipreDate;//文件过期时间
	private int isPaid;//网络图片是否已经购买属性，1已付，0，未支付
	private int isVideo;//1是视频，0是图片
	private int isPreset; // 照片是否有模版，0，代表没有模板，1，代表有模版, 000000000000000000000000
	private int isEnImage;//是否是加密的图片 0：未加密；1：已加密
	private int isOnLine;//1线上图片，0，本地图片
	private int fileSize;//文件大小
	private int videoWidth;//视频文件宽
	private int videoHeight;//视频文件高

	@Transient
	private String locationName;//每张图片的地点名称
	@Transient
	private String failedTime;
	@Transient
	private int sectionId;//悬浮的id
	@Transient
	private int isRefreshInfo;//0：不需要刷新旧数据，1：需要刷新旧数据列表，只针对刷新的旧图片，其他均用不到
	@Transient
	private int isChecked;//图片是否被检索到，用在ViewPhotoActivity中的全选操作，1，true，0，false
	@Transient
	private int isSelected;//图片是否被选中，1，选中，0，未选中
	@Transient
	private int isUploaded;//图片是否已经上传到服务器
	@Transient
	private int currentLocationPhotoCount;//只有展示照片页才需要这个字段，每个地点对应的照片数量

	@Override
	public int compareTo(PhotoInfo another) {
		//此处为通用的排序方法
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		PictureAirLog.out("sort photoItemInfo------->");
		try {
			if (this.strShootOn != null && another.strShootOn != null) {
				PictureAirLog.out("this---->" + this.strShootOn + "another----->"+ another.strShootOn);
				Date date1 = sdf.parse(this.strShootOn);
				Date date2 = sdf.parse(another.strShootOn);
				return date1.compareTo(date2) * -1;
			} else if (this.strShootOn != null && another.strShootOn == null) {
				return -1;
			} else if (this.strShootOn == null && another.strShootOn != null) {
				return 1;
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	public static final Parcelable.Creator<PhotoInfo> CREATOR = new Creator<PhotoInfo>() {

		@Override
		public PhotoInfo[] newArray(int size) {
			return new PhotoInfo[size];
		}

		@Override
		public PhotoInfo createFromParcel(Parcel source) {
			return new PhotoInfo(source);
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(photoId);
		dest.writeString(photoPassCode);
		dest.writeString(shootDate);
		dest.writeString(photoThumbnail_128);
		dest.writeString(photoThumbnail_512);
		dest.writeString(photoThumbnail_1024);
		dest.writeString(photoOriginalURL);
		dest.writeString(locationId);
		dest.writeString(strShootOn);
		dest.writeString(shareURL);
		dest.writeString(adURL);
		dest.writeString(locationName);
		dest.writeString(receivedOn);
		dest.writeString(exipreDate);
		dest.writeString(failedTime);
		dest.writeInt(isPaid);
		dest.writeInt(isVideo);
		dest.writeInt(isPreset);
		dest.writeInt(isEnImage);
		dest.writeInt(isOnLine);
		dest.writeInt(fileSize);
		dest.writeInt(videoWidth);
		dest.writeInt(videoHeight);
		dest.writeInt(sectionId);
		dest.writeInt(isRefreshInfo);
		dest.writeInt(isChecked);
		dest.writeInt(isSelected);
		dest.writeInt(isUploaded);
		dest.writeInt(currentLocationPhotoCount);
	}

	@Override
	public String toString() {
		return "PhotoInfo{" +
				"id=" + id +
				", photoId='" + photoId + '\'' +
				", photoPassCode='" + photoPassCode + '\'' +
				", shootDate='" + shootDate + '\'' +
				", photoThumbnail_128='" + photoThumbnail_128 + '\'' +
				", photoThumbnail_512='" + photoThumbnail_512 + '\'' +
				", photoThumbnail_1024='" + photoThumbnail_1024 + '\'' +
				", photoOriginalURL='" + photoOriginalURL + '\'' +
				", locationId='" + locationId + '\'' +
				", strShootOn='" + strShootOn + '\'' +
				", shareURL='" + shareURL + '\'' +
				", adURL='" + adURL + '\'' +
				", isPaid=" + isPaid +
				", isVideo=" + isVideo +
				", isPreset=" + isPreset +
				", isEnImage=" + isEnImage +
				", isOnLine=" + isOnLine +
				", fileSize=" + fileSize +
				", videoWidth=" + videoWidth +
				", videoHeight=" + videoHeight +
				", exipreDate=" + exipreDate +
				", locationName='" + locationName + '\'' +
				", receivedOn='" + receivedOn + '\'' +
				", failedTime='" + failedTime + '\'' +
				", sectionId=" + sectionId +
				", isRefreshInfo=" + isRefreshInfo +
				", isChecked=" + isChecked +
				", isSelected=" + isSelected +
				", isUploaded=" + isUploaded +
				", currentLocationPhotoCount=" + currentLocationPhotoCount +
				'}';
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public String getReceivedOn() {
		return receivedOn;
	}

	public void setReceivedOn(String receivedOn) {
		this.receivedOn = receivedOn;
	}

	public String getFailedTime() {
		return failedTime;
	}

	public void setFailedTime(String failedTime) {
		this.failedTime = failedTime;
	}

	public int getSectionId() {
		return sectionId;
	}

	public void setSectionId(int sectionId) {
		this.sectionId = sectionId;
	}

	public int getIsRefreshInfo() {
		return isRefreshInfo;
	}

	public void setIsRefreshInfo(int isRefreshInfo) {
		this.isRefreshInfo = isRefreshInfo;
	}

	public int getIsChecked() {
		return isChecked;
	}

	public void setIsChecked(int isChecked) {
		this.isChecked = isChecked;
	}

	public int getIsSelected() {
		return isSelected;
	}

	public void setIsSelected(int isSelected) {
		this.isSelected = isSelected;
	}

	public int getIsUploaded() {
		return isUploaded;
	}

	public void setIsUploaded(int isUploaded) {
		this.isUploaded = isUploaded;
	}

	public int getCurrentLocationPhotoCount() {
		return currentLocationPhotoCount;
	}

	public void setCurrentLocationPhotoCount(int currentLocationPhotoCount) {
		this.currentLocationPhotoCount = currentLocationPhotoCount;
	}

	public Long getId() {
					return this.id;
	}

	public void setId(Long id) {
					this.id = id;
	}

	public String getPhotoId() {
					return this.photoId;
	}

	public void setPhotoId(String photoId) {
					this.photoId = photoId;
	}

	public String getPhotoPassCode() {
					return this.photoPassCode;
	}

	public void setPhotoPassCode(String photoPassCode) {
					this.photoPassCode = photoPassCode;
	}

	public String getShootDate() {
					return this.shootDate;
	}

	public void setShootDate(String shootDate) {
					this.shootDate = shootDate;
	}

	public String getPhotoThumbnail_128() {
					return this.photoThumbnail_128;
	}

	public void setPhotoThumbnail_128(String photoThumbnail_128) {
					this.photoThumbnail_128 = photoThumbnail_128;
	}

	public String getPhotoThumbnail_512() {
					return this.photoThumbnail_512;
	}

	public void setPhotoThumbnail_512(String photoThumbnail_512) {
					this.photoThumbnail_512 = photoThumbnail_512;
	}

	public String getPhotoThumbnail_1024() {
					return this.photoThumbnail_1024;
	}

	public void setPhotoThumbnail_1024(String photoThumbnail_1024) {
					this.photoThumbnail_1024 = photoThumbnail_1024;
	}

	public String getPhotoOriginalURL() {
					return this.photoOriginalURL;
	}

	public void setPhotoOriginalURL(String photoOriginalURL) {
					this.photoOriginalURL = photoOriginalURL;
	}

	public String getLocationId() {
					return this.locationId;
	}

	public void setLocationId(String locationId) {
					this.locationId = locationId;
	}

	public String getStrShootOn() {
					return this.strShootOn;
	}

	public void setStrShootOn(String strShootOn) {
					this.strShootOn = strShootOn;
	}

	public String getShareURL() {
					return this.shareURL;
	}

	public void setShareURL(String shareURL) {
					this.shareURL = shareURL;
	}

	public String getAdURL() {
					return this.adURL;
	}

	public void setAdURL(String adURL) {
					this.adURL = adURL;
	}

	public int getIsPaid() {
					return this.isPaid;
	}

	public void setIsPaid(int isPaid) {
					this.isPaid = isPaid;
	}

	public int getIsVideo() {
					return this.isVideo;
	}

	public void setIsVideo(int isVideo) {
					this.isVideo = isVideo;
	}

	public int getIsPreset() {
					return this.isPreset;
	}

	public void setIsPreset(int isPreset) {
					this.isPreset = isPreset;
	}

	public int getIsEnImage() {
					return this.isEnImage;
	}

	public void setIsEnImage(int isEnImage) {
					this.isEnImage = isEnImage;
	}

	public int getIsOnLine() {
					return this.isOnLine;
	}

	public void setIsOnLine(int isOnLine) {
					this.isOnLine = isOnLine;
	}

	public int getFileSize() {
					return this.fileSize;
	}

	public void setFileSize(int fileSize) {
					this.fileSize = fileSize;
	}

	public int getVideoWidth() {
					return this.videoWidth;
	}

	public void setVideoWidth(int videoWidth) {
					this.videoWidth = videoWidth;
	}

	public int getVideoHeight() {
					return this.videoHeight;
	}

	public void setVideoHeight(int videoHeight) {
					this.videoHeight = videoHeight;
	}

	public String getExipreDate() {
		return this.exipreDate;
	}

	public void setExipreDate(String exipreDate) {
		this.exipreDate = exipreDate;
	}

	private PhotoInfo(Parcel source) {
		id = source.readLong();
		photoId = source.readString();
		photoPassCode = source.readString();
		shootDate = source.readString();
		photoThumbnail_128 = source.readString();
		photoThumbnail_512 = source.readString();
		photoThumbnail_1024 = source.readString();
		photoOriginalURL = source.readString();
		locationId = source.readString();
		strShootOn = source.readString();
		shareURL = source.readString();
		adURL = source.readString();
		locationName = source.readString();
		receivedOn = source.readString();
		exipreDate = source.readString();
		failedTime = source.readString();
		isPaid = source.readInt();
		isVideo = source.readInt();
		isPreset = source.readInt();
		isEnImage = source.readInt();
		isOnLine = source.readInt();
		fileSize = source.readInt();
		videoWidth = source.readInt();
		videoHeight = source.readInt();
		sectionId = source.readInt();
		isRefreshInfo = source.readInt();
		isChecked = source.readInt();
		isSelected = source.readInt();
		isUploaded = source.readInt();
		currentLocationPhotoCount = source.readInt();
	}

	@Generated(hash = 133317568)
	public PhotoInfo(Long id, String photoId, String photoPassCode, String shootDate,
			String photoThumbnail_128, String photoThumbnail_512, String photoThumbnail_1024,
			String photoOriginalURL, String locationId, String strShootOn, String shareURL,
			String adURL, String receivedOn, String exipreDate, int isPaid, int isVideo,
			int isPreset, int isEnImage, int isOnLine, int fileSize, int videoWidth,
			int videoHeight) {
		this.id = id;
		this.photoId = photoId;
		this.photoPassCode = photoPassCode;
		this.shootDate = shootDate;
		this.photoThumbnail_128 = photoThumbnail_128;
		this.photoThumbnail_512 = photoThumbnail_512;
		this.photoThumbnail_1024 = photoThumbnail_1024;
		this.photoOriginalURL = photoOriginalURL;
		this.locationId = locationId;
		this.strShootOn = strShootOn;
		this.shareURL = shareURL;
		this.adURL = adURL;
		this.receivedOn = receivedOn;
		this.exipreDate = exipreDate;
		this.isPaid = isPaid;
		this.isVideo = isVideo;
		this.isPreset = isPreset;
		this.isEnImage = isEnImage;
		this.isOnLine = isOnLine;
		this.fileSize = fileSize;
		this.videoWidth = videoWidth;
		this.videoHeight = videoHeight;
	}

	@Generated(hash = 2143356537)
	public PhotoInfo() {
	}
}
