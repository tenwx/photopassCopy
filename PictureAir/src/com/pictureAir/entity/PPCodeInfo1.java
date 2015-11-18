package com.pictureAir.entity;

import java.util.List;

public class PPCodeInfo1 {
	private String ppCode;//照片对应pp号码
	private int photoCount;//照片数量
	private int isUpgrade;//是否已经绑定到PP+（0否，1是）
	private String shootDate;//shootDate
	private int isHidden;//是否已经隐藏（0否，1是）
	private String location;//图片位置
	private List<String> urlList;//图片路径
	private List<PhotoInfo> selectPhotoItemInfos;//用于点进去看图片详情
	
	public List<PhotoInfo> getSelectPhotoItemInfos() {
		return selectPhotoItemInfos;
	}
	public void setSelectPhotoItemInfos(
			List<PhotoInfo> selectPhotoItemInfos) {
		this.selectPhotoItemInfos = selectPhotoItemInfos;
	}
	public String getPpCode() {
		return ppCode;
	}
	public void setPpCode(String ppCode) {
		this.ppCode = ppCode;
	}
	public int getPhotoCount() {
		return photoCount;
	}
	public void setPhotoCount(int photoCount) {
		this.photoCount = photoCount;
	}
	public int getIsUpgrade() {
		return isUpgrade;
	}
	public void setIsUpgrade(int isUpgrade) {
		this.isUpgrade = isUpgrade;
	}
	public String getShootDate() {
		return shootDate;
	}
	public void setShootDate(String shootDate) {
		this.shootDate = shootDate;
	}
	public int getIsHidden() {
		return isHidden;
	}
	public void setIsHidden(int isHidden) {
		this.isHidden = isHidden;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public List<String> getUrlList() {
		return urlList;
	}
	public void setUrlList(List<String> urlList) {
		this.urlList = urlList;
	}

}
