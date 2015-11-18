package com.pictureAir.receiver;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver {
	
	public void onReceive(Context context, Intent intent) {

		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			
			//到   定时器的广播中。
			 Intent intent1 = new Intent();
			 intent1.setAction("com.notification.AlertManagerRecriver");
			 context.sendBroadcast(intent1);
//			 Toast.makeText(context, "开机了", 1000).show();
			 
		}
		
	}
}