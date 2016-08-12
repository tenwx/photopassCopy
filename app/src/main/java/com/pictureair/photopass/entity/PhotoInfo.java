package com.pictureair.photopass.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.pictureair.photopass.util.PictureAirLog;

/**
 * 图片封装类
 * @author bauer_bao
 *
 */
public class PhotoInfo implements Parcelable, Comparable<PhotoInfo>{
//	public String albumName;//所属相册名称
	public String photoId;//网络图片的话就是photoId
	public String photoPathOrURL;//图片的原始路径
	public String photoThumbnail;//缩略图路径
	public String photoThumbnail_512;//缩略图512尺寸的路径
	public String photoThumbnail_1024;//缩略图1024尺寸的路径
	public String photoPassCode;//网络图片的PPCode
	public int isPayed;//网络图片是否已经购买属性，1已付，0，未支付
	public int isChecked;//图片是否被检索到，用在ViewPhotoActivity中的全选操作，1，true，0，false
	public int isSelected;//图片是否被选中，1，选中，0，未选中
	public int showMask;//mask底层是否显示，1，显示，0，不显示
	public String index;//被选中的索引值
	public int isUploaded;//图片是否已经上传，1，已经上传，0，未上传
	public String shootTime;// 拍摄时间 年月日，用于页面显示
	public String locationId;//拍摄地点
	public String shootOn;//拍摄时间 年月日时分秒，用来数据库的排序
	public long lastModify;//文件最后修改的时间
	public int isLove;//是否收藏 0：未收藏；1：已收藏
	public int onLine;//1线上图片，0，本地图片
	public int sectionId;//悬浮的id
	public String shareURL;//网络图片分享的URL
	public String locationCountry;//景点的国家城市
	public String locationName;//每张图片的地点名称
	public int isVideo;//1是视频，0是图片
	public int fileSize;//文件大小
	public int videoWidth;//视频文件宽
	public int videoHeight;//视频文件高

	public int isHasPreset; // 照片是否有模版，0，代表没有模板，1，代表有模版
	public String failedTime;
	public int isEncrypted;//是否加密 0：未加密；1：已加密
	public int selectPos;//被勾选的位置

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

	public PhotoInfo() {

	}

	private PhotoInfo(Parcel source) {
//		albumName = source.readString();
		photoId = source.readString();
		photoPathOrURL = source.readString();
		photoThumbnail = source.readString();
		photoThumbnail_512 = source.readString();
		photoThumbnail_1024 = source.readString();
		photoPassCode = source.readString();
		isPayed = source.readInt();
		isChecked = source.readInt();
		isSelected = source.readInt();
		showMask = source.readInt();
		index = source.readString();
		isUploaded = source.readInt();
		shootTime = source.readString();
		locationId = source.readString();
		shootOn = source.readString();
		lastModify = source.readLong();
		isLove = source.readInt();
		onLine = source.readInt();
		sectionId = source.readInt();
		shareURL = source.readString();
		locationCountry = source.readString();
		locationName = source.readString();
		isVideo = source.readInt();
		fileSize = source.readInt();
		videoWidth = source.readInt();
		videoHeight = source.readInt();
		isHasPreset = source.readInt();
		failedTime = source.readString();
		isEncrypted = source.readInt();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
//		dest.writeString(albumName);
		dest.writeString(photoId);
		dest.writeString(photoPathOrURL);
		dest.writeString(photoThumbnail);
		dest.writeString(photoThumbnail_512);
		dest.writeString(photoThumbnail_1024);
		dest.writeString(photoPassCode);
		dest.writeInt(isPayed);
		dest.writeInt(isChecked);
		dest.writeInt(isSelected);
		dest.writeInt(showMask);
		dest.writeString(index);
		dest.writeInt(isUploaded);
		dest.writeString(shootTime);
		dest.writeString(locationId);
		dest.writeString(shootOn);
		dest.writeLong(lastModify);
		dest.writeInt(isLove);
		dest.writeInt(onLine);
		dest.writeInt(sectionId);
		dest.writeString(shareURL);
		dest.writeString(locationCountry);
		dest.writeString(locationName);
		dest.writeInt(isVideo);
		dest.writeInt(fileSize);
		dest.writeInt(videoWidth);
		dest.writeInt(videoHeight);
		dest.writeInt(isHasPreset);
		dest.writeString(failedTime);
		dest.writeInt(isEncrypted);
	}

	@Override
	public int compareTo(PhotoInfo another) {
		if (another.lastModify == 0 && this.lastModify != 0) {
			PictureAirLog.out("----->1");
			return 1;//排后面
		}else if (this.lastModify > another.lastModify) {
			PictureAirLog.out("----->2");
			return -1;//排前面
		}else if (this.lastModify < another.lastModify) {
			PictureAirLog.out("----->3");
			return 1;//排后面
		}else {
			PictureAirLog.out("----->4");
		}
		
		return 0;
	}
}
