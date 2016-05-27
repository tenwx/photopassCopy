package com.pictureworks.android.widget;

import android.content.Context;
import android.os.Handler;

import com.pictureworks.android.entity.BaseCheckUpdate;
import com.pictureworks.android.util.API1;

/**
 * 自动更新检查类
 * @author bauer_bao
 *
 */
public class CheckUpdate implements BaseCheckUpdate{
	private static CheckUpdate checkUpdate;
	
	public static CheckUpdate getInstance() {
		if (checkUpdate == null) {
			checkUpdate = new CheckUpdate();
		}
		return checkUpdate;
	}

	@Override
	public void getTokenId(Context context, String appType, Handler handler) {
		API1.getTokenId(context, appType, handler);
	}

	@Override
	public void checkUpdate(String tokenId, Context context, Handler handler, String versionName, String currentLanguage) {
		// TODO Auto-generated method stub
		API1.checkUpdate(tokenId, context, handler, versionName, currentLanguage);
	}

	@Override
	public void downloadAPK(Context context,String downloadURL, CustomProgressBarPop customProgressBarPop, String version, Handler handler) {
		// TODO Auto-generated method stub
//		API1.downloadAPK(context,downloadURL, customProgressBarPop, version, handler);
		//启动service去下载
	}
}
