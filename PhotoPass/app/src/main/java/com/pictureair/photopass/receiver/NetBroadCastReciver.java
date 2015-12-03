package com.pictureair.photopass.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.pictureair.photopass.service.NotificationService;
import com.pictureair.photopass.util.Common;

/**
 * 网络连接的监听
 * @author talon
 *
 */
public class NetBroadCastReciver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		SharedPreferences sp =context.getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);

		if (!sp.getString(Common.USERINFO_TOKENID, "").equals("")) {  
			// 没有退出的时候
			//如果是在开启wifi连接和有网络状态下
			if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
//				ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
				Intent intent1;
				if (NetworkInfo.State.CONNECTED == info.getState()) {
					// 连接状态
					Log.e(" ＝＝＝＝＝ ", "有网络连接");
					intent1 = new Intent(context,
							com.pictureair.photopass.service.NotificationService.class);
					context.startService(intent1);
				} else {
					Log.e(" ＝＝＝＝＝ ", "无网络连接");
					intent1 = new Intent(context, NotificationService.class);
					intent1.putExtra("status", "disconnect");
					context.startService(intent1);
				}
			}

		} else {
			//退出之后 不用访问。
		}

	}

}
