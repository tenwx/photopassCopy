package com.pictureair.photopass.util;

import android.content.Context;

import com.pictureair.photopass.db.PictureAirDbManager;


/**
 * 我的 - 设置 中的管理类。
 *  1,2,3,4 种设置。弹窗。
 * 
 * @author talon
 * 
 */
public class SettingUtil {
    private final String TAG = "SettingUtil";
	private PictureAirDbManager pictureAirDbManager;

	public SettingUtil(Context context) {
		super();
		pictureAirDbManager = new PictureAirDbManager(context);
	}
	
	//     tips 1 , 网络下载模式流程。 start
	/**
	 * 获取 是否仅wifi下载
	 *  默认是 3g/4g/wifi 下载
	 * @return
	 */
	public boolean isOnlyWifiDownload(String userInfoId) {
		PictureAirLog.e(TAG, ": ==>> "+pictureAirDbManager.checkFirstBuyPhoto(Common.SETTING_ONLY_WIFI,
				userInfoId));
		return pictureAirDbManager.checkFirstBuyPhoto(Common.SETTING_ONLY_WIFI,
				userInfoId);// 是否仅wifi下载。
	}
	
	/**
	 * 删除 "仅wifi" 设置中的字段。
	 * 
	 * @param userInfoId
	 */
	public void deleteSettingOnlyWifiStatus(String userInfoId) {
		pictureAirDbManager.deleteSettingStatus(Common.SETTING_ONLY_WIFI,
				userInfoId);
	}

	/**
	 * 增加 "仅wifi" 设置中的字段。
	 * 
	 * @param userInfoId
	 */
	public void insertSettingOnlyWifiStatus(String userInfoId) {
		pictureAirDbManager.insertSettingStatus(Common.SETTING_ONLY_WIFI,
				userInfoId);
	}
	
   //  tips 1 , 网络下载模式流程。 end
	
	
	
	
   //  tips 2 , 推广AirPass+ 弹窗流程。 start
	/**
	 * 如果airPass中的照片首次到达了十张。 字段存在，就不是首次，字段不存在，首次。
	 * @param userInfoId
	 * @return
	 */
	public boolean isFirstPP10(String userInfoId){
		return !(pictureAirDbManager.checkFirstBuyPhoto(Common.SETTING_FIRST_PP10,
				userInfoId));
	}
	
	/**
	 * 增加 " 第一次到10 张图片字段。" 的字段。
	 * 
	 * @param userInfoId
	 */
	public void insertSettingFirstPP10Status(String userInfoId) {
		pictureAirDbManager.insertSettingStatus(Common.SETTING_FIRST_PP10,
				userInfoId);
	}
   //  tips 2 , 推广AirPass+ 弹窗流程。 end
	
	
	
	
   //  tips 3 , 同步更新流程。 Start
	/**
	 * 获取 是否第一次购买单张图片。
	 * 如果字段存在,就不是第一次提示同步。如果不存在，就是第一次
	 * @return
	 */
	public boolean isFirstTipsSyns(String userInfoId){
		return !(pictureAirDbManager.checkFirstBuyPhoto(Common.SETTING_FIRST_TIPS_SYNS,
				userInfoId));
	}
	
	/**
	 * 增加字段，代表不是第一次了。
	 * @return
	 */
	public void insertSettingFirstTipsSynsStatus(String userInfoId) {
		pictureAirDbManager.insertSettingStatus(Common.SETTING_FIRST_TIPS_SYNS,
				userInfoId);
	}
	
	/**
	 * 获取 是否自动更新
	 * 
	 * @return
	 */
	public boolean isAutoUpdate(String userInfoId) {
		return pictureAirDbManager.checkFirstBuyPhoto(
				Common.SETTING_AUTO_UPDATE, userInfoId); // 是否自动更新
	}
	
	/**
	 * 删除 "自动更新" 设置中的字段。
	 * 
	 * @param userInfoId
	 */
	public void deleteSettingAutoUpdateStatus(String userInfoId) {
		pictureAirDbManager.deleteSettingStatus(Common.SETTING_AUTO_UPDATE,
				userInfoId);
	}

	/**
	 * 增加 "自动更新" 设置中的字段。
	 * 
	 * @param userInfoId
	 */
	public void insertSettingAutoUpdateStatus(String userInfoId) {
		pictureAirDbManager.insertSettingStatus(Common.SETTING_AUTO_UPDATE,
				userInfoId);
	}
	
	
   //  tips 3 , 同步更新流程。  end


	
	

}
