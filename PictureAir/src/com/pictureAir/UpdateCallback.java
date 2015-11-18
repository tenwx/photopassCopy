package com.pictureAir;

import android.view.View;

/**
 * 定位的回调接口
 * @author bauer_bao
 *
 */
public interface UpdateCallback {
	//开始定位函数，由继承类去实现
	public void startLocation(int position, View view);
}
