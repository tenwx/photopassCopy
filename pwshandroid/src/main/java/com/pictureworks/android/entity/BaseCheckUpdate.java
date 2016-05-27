package com.pictureworks.android.entity;

import android.content.Context;
import android.os.Handler;

import com.pictureworks.android.widget.CustomProgressBarPop;

public abstract interface BaseCheckUpdate {

	abstract void getTokenId(Context context, String appType, Handler handler);
	abstract void checkUpdate(String tokenId, Context context, Handler handler, String versionName, String currentLanguage);
	abstract void downloadAPK(Context context, String downloadURL, CustomProgressBarPop customProgressBarPop, String version, Handler handler);
}
