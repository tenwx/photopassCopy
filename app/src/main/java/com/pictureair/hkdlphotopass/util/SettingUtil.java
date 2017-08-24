package com.pictureair.hkdlphotopass.util;

import com.pictureair.hkdlphotopass.greendao.PictureAirDbManager;


/**
 * 我的 - 设置 中的管理类。
 *  1,2,3,4 种设置。弹窗。
 * 
 * @author talon
 * 
 */
public class SettingUtil {
    private final String TAG = "SettingUtil";

	public SettingUtil() {
		super();
	}
	
	//     tips 1 , 网络下载模式流程。 start
	/**
	 * 获取 是否仅wifi下载
	 *  默认是 3g/4g/wifi 下载
	 * @return
	 */
	public boolean isOnlyWifiDownload(String userInfoId) {
		return PictureAirDbManager.checkFirstBuyPhoto(Common.SETTING_ONLY_WIFI, userInfoId);// 是否仅wifi下载。
	}
	
	/**
	 * 删除 "仅wifi" 设置中的字段。
	 * 
	 * @param userInfoId
	 */
	public void deleteSettingOnlyWifiStatus(String userInfoId) {
		PictureAirDbManager.deleteSettingStatus(Common.SETTING_ONLY_WIFI, userInfoId);
	}

	/**
	 * 增加 "仅wifi" 设置中的字段。
	 * 
	 * @param userInfoId
	 */
	public void insertSettingOnlyWifiStatus(String userInfoId) {
		PictureAirDbManager.insertSettingStatus(Common.SETTING_ONLY_WIFI, userInfoId);
	}

   //  tips 2 , 推广AirPass+ 弹窗流程。 start
	/**
	 * 如果airPass中的照片首次到达了十张。 字段存在，就不是首次，字段不存在，首次。
	 * @param userInfoId
	 * @return
	 */
	public boolean isFirstPP10(String userInfoId){
		return !(PictureAirDbManager.checkFirstBuyPhoto(Common.SETTING_FIRST_PP10, userInfoId));
	}
	
	/**
	 * 增加 " 第一次到10 张图片字段。" 的字段。
	 * 
	 * @param userInfoId
	 */
	public void insertSettingFirstPP10Status(String userInfoId) {
		PictureAirDbManager.insertSettingStatus(Common.SETTING_FIRST_PP10, userInfoId);
	}

}
