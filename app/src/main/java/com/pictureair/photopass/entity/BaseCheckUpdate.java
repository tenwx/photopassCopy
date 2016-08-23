package com.pictureair.photopass.entity;

import android.content.Context;
import android.os.Handler;

public interface BaseCheckUpdate {

	void getTokenId(Context context, Handler handler);
	void checkUpdate(Context context, Handler handler);
}
