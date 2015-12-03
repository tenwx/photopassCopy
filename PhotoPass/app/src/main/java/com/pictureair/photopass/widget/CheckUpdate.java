package com.pictureair.photopass.widget;

import android.content.Context;
import android.os.Handler;

import com.pictureair.photopass.entity.BaseCheckUpdate;
import com.pictureair.photopass.util.API;

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
	public void checkUpdate(Context context, Handler handler, String versionName, String currentLanguage) {
		// TODO Auto-generated method stub
		API.checkUpdate(context, handler, versionName, currentLanguage);
	}

	@Override
	public void downloadAPK(String downloadURL, CustomProgressBarPop customProgressBarPop, String version, Handler handler) {
		// TODO Auto-generated method stub

		API.downloadAPK(downloadURL, customProgressBarPop, version, handler);
	}
}
