package com.pictureair.photopass.entity;

import android.content.Context;

public interface BaseCheckUpdate {

	void getTokenId(final Context context);
	void checkUpdate(final Context context);
}
