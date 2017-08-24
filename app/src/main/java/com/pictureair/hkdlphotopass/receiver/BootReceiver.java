package com.pictureair.photopass.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
	
	public void onReceive(Context context, Intent intent) {

		if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			
			//到   定时器的广播中。
			 Intent intent1 = new Intent();
			 intent1.setAction("com.notification.AlertManagerRecriver");
			 context.sendBroadcast(intent1);
//			 Toast.makeText(context, "开机了", 1000).show();
			 
		}
		
	}
}