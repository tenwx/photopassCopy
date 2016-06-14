package com.pictureair.photopass.entity;

import android.content.Context;
import android.os.Handler;

import com.pictureair.photopass.widget.CustomProgressBarPop;

public abstract interface BaseCheckUpdate {

	abstract void getTokenId(Context context, Handler handler);
	abstract void checkUpdate(Context context, Handler handler);
	abstract void downloadAPK(Context context,String downloadURL, CustomProgressBarPop customProgressBarPop, String version, Handler handler);
}
