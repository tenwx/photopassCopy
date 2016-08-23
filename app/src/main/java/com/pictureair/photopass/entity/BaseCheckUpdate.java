package com.pictureair.photopass.entity;

import android.content.Context;
import android.os.Handler;

import com.pictureair.photopass.widget.CustomProgressBarPop;

public interface BaseCheckUpdate {

	void getTokenId(Context context, Handler handler);
	void checkUpdate(Context context, Handler handler);
	void downloadAPK(Context context,String downloadURL, CustomProgressBarPop customProgressBarPop, String version, Handler handler);
}
