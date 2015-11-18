package com.pictureAir.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlertManagerRecriver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {

		// 改用socketIO就不用重复发送广播。所以没必要用AlertMAnager。
		Intent intent1 = new Intent(context, com.pictureAir.service.NotificationService.class);
		context.startService(intent1);
	}

}
