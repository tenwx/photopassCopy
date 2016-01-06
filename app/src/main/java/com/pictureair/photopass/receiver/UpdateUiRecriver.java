package com.pictureair.photopass.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.view.View;

import com.pictureair.photopass.activity.MainTabActivity;

public class UpdateUiRecriver extends BroadcastReceiver{

	Vibrator vibrator;
	@Override
	public void onReceive(Context arg0, Intent intent) {
		// TODO 更新UI
		MainTabActivity.maintabbadgeView.setVisibility(View.VISIBLE);

		//获得震动服务
		vibrator = (Vibrator)arg0.getSystemService(Context.VIBRATOR_SERVICE); 
		long[] pattern = {0, 200, 300, 200};    
		//-1表示不重复, 如果不是-1, 比如改成1, 表示从前面这个long数组的下标为1的元素开始重复.  
		vibrator.vibrate(pattern, -1); 
	}

}
