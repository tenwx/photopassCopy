package com.pictureair.photopass.widget;

import android.content.Context;
import android.os.Handler;

import com.pictureair.photopass.entity.BaseCheckUpdate;
import com.pictureair.photopass.util.API1;

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
	public void getTokenId(Context context, Handler handler) {
		API1.getTokenId(context, handler);
	}

	@Override
	public void checkUpdate(Context context, Handler handler) {
		// TODO Auto-generated method stub
		API1.checkUpdate(context, handler);
	}
}
