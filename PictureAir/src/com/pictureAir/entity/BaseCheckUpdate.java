package com.pictureAir.entity;

import com.pictureAir.widget.CustomProgressBarPop;

import android.content.Context;
import android.os.Handler;

public abstract interface BaseCheckUpdate {

	abstract void checkUpdate(Context context, Handler handler, String versionName, String currentLanguage);
	abstract void downloadAPK(String downloadURL, CustomProgressBarPop customProgressBarPop, String version, Handler handler);
}
