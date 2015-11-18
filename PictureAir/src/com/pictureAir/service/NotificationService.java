package com.pictureAir.service;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.pictureAir.MyApplication;
import com.pictureAir.R;
import com.pictureAir.util.AppManager;
import com.pictureAir.util.Common;

public class NotificationService extends android.app.Service {
	private int photoCount;
	private SocketIO socket;
	private SharedPreferences preferences;
//	private Editor editor;
	private String userId;
	
	
	private MyApplication application;
	

	private boolean isConnected = false;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub\
		Log.e("notification服务里面", "========onCreate=========");
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		preferences = getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
		userId = preferences.getString(Common.USERINFO_ID, null);
//		editor = preferences.edit();
		if (intent != null && intent.getStringExtra("status") != null) {//断开连接
			if ("disconnect".equals(intent.getStringExtra("status"))) {
				System.out.println("-----------disconnect-------socketio--------");
				
				socket.disconnect();
//				editor.putInt(Common.SOCKETPUSHCONNECTED, 0);
//				editor.commit();
				isConnected = false;
				stopSelf();//停止服务
				
			}
		}else {//请求连接
//			if (preferences.getInt(Common.SOCKETPUSHCONNECTED, 0) == 0) {
			if (!isConnected) {
				System.out.println("-----------connect-------socketio--------");
				application = (MyApplication) getApplication();
				showNotification();
			}
		}
		return START_STICKY;
		/*
		* START_STICKY：如果service进程被kill掉，保留service的状态为开始状态，但不保留递送的intent对象。
		* 随后系统会尝试重新创建service，由于服务状态为开始状态，所以创建服务后一定会调用onStartCommand(Intent,int,int)方法。如果在此期间没有任何启动命令被传递到service，那么参数Intent将为null。
		* START_NOT_STICKY：“非粘性的”。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统不会自动重启该服务。
		* START_REDELIVER_INTENT：重传Intent。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统会自动重启该服务，并将Intent的值传入。
		* START_STICKY_COMPATIBILITY：START_STICKY的兼容版本，但不保证服务被kill后一定能重启。
		* */
	}

//	@Override
//	@Deprecated
//	public void onStart(Intent intent, int startId) {
//		preferences = getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
//		userId = preferences.getString(Common.USERINFO_ID, null);
//		if (intent.getStringExtra("status") != null) {//断开连接
//			if ("disconnect".equals(intent.getStringExtra("status"))) {
//				System.out.println("-----------disconnect-------socketio--------");
//				
//				socket.disconnect();
//				isConnected = false;
//				
//			}
//		}else {//请求连接
//			if (!isConnected) {
//				System.out.println("-----------connect-------socketio--------");
//				application = (MyApplication) getApplication();
//				showNotification();
//			}
//		}
//
//		super.onStart(intent, startId);
//	}

	private boolean isTopActivity() {
		boolean isTop = false;
		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
		if (cn.getClassName().contains("MainTabActivity")) {
			isTop = true;
		}
		return isTop;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		System.out.println("===========service destroy");
	}

	public void showNotification() {
		new Thread() {
			public void run() {
				Log.e("===userId=====", "=============:+"+userId);
				try {
					socket = new SocketIO(Common.BASE_URL);

					socket.connect(new IOCallback() {

						@Override
						public void onMessage(JSONObject json,
								IOAcknowledge arg1) {
							// TODO Auto-generated method stub
							try {
								Log.e("======", "Server said json:"
										+ json.toString(2));
								Log.e("======",
										"IOAcknowledge:"
												+ arg1.toString());
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

						@Override
						public void onMessage(String data,
								IOAcknowledge arg1) {
							// TODO Auto-generated method stub
							Log.e("======", "Server said data: " + data);
							Log.e("======", "arg1:" + arg1.toString());
						}

						@Override
						public void onError(
								SocketIOException socketIOException) {
							// TODO Auto-generated method stub
							Log.e("======", "an Error occured："
									+ socketIOException.toString());
							socketIOException.printStackTrace();
//							editor.putInt(Common.SOCKETPUSHCONNECTED, 0);
//							editor.commit();
							isConnected = false;
							socket.reconnect();
						}

						@Override
						public void onDisconnect() {
							// TODO Auto-generated method stub
							Log.e("======", "Connection terminated");
//							editor.putInt(Common.SOCKETPUSHCONNECTED, 0);
//							editor.commit();
							isConnected = false;
						}

						@Override
						public void onConnect() {
							// TODO Auto-generated method stub
							socket.emit("getNewPhotosCountOfUser",userId);
//							editor.putInt(Common.SOCKETPUSHCONNECTED, 1);
//							editor.commit();
							isConnected = true;
							Log.e("======", "Connection established");
						}

						@Override
						public void on(String event,
								IOAcknowledge arg1, Object... arg2) {
							// TODO Auto-generated method stub
							Log.e("  ====  arg2", " :"+arg2);
							Log.e("===on===","Server triggered event '" + event + "'");
							if (event.toString().equals("catchOrderInfoOf"+userId)) {
//								Log.e("   订单事件中 ", " === ");
								JSONObject message = (JSONObject) arg2[0];
								try {
									String info = message.getString("info");
									
									Log.e(" 接受的订单信息 ", " === "+info);
									
									NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
									
									
									Intent intent = new Intent(getApplicationContext(),
											com.pictureAir.MainTabActivity.class);
									
									PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,intent, 0);
									Notification notification = new NotificationCompat.Builder(NotificationService.this).
								        	setSmallIcon(R.drawable.pp_icon).setAutoCancel(true).setContentTitle("New Order").
								        	setContentIntent(pendingIntent)
								        	.setContentText(info).setWhen(System.currentTimeMillis()).setTicker("New Message").build();
//										PendingIntent pi = PendingIntent.getActivity(MainActivity.this, 0, new Intent(MainActivity.this,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT);
//										notificationBuilder.setContentIntent(pi);
										
										notification.flags = Notification.FLAG_AUTO_CANCEL;//通知栏可以自动删除
										notification.defaults = Notification.DEFAULT_ALL;//默认下载完成声音
//										notificationManager.notify(1, notification);
									
									
									
									
									
//									Notification notification = new Notification(R.drawable.pp_icon, "New Message",System.currentTimeMillis());
//									notification.flags |= Notification.FLAG_AUTO_CANCEL; // 点击之后自动清除
//									notification.defaults = Notification.DEFAULT_ALL;
//									notification.setLatestEventInfo(NotificationService.this, "New Order", info, pendingIntent);
									manager.notify(0, notification);
									
									Log.e("=====", "执行");
								} catch (NumberFormatException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							
							
							
							
							
							if (event.toString().equals("sendNewPhotosCountOf"+userId)) {
								JSONObject message = (JSONObject) arg2[0];
								try {
									//									Log.e("===============","==:"+ message.getString("photoCount"));
									photoCount = Integer.valueOf(message.getString("c"));
									System.out.println("receive photo count =====" + photoCount);
									if (photoCount > 0) {
										
//										System.out.println("photocount ========== > 0");

										if (!isTopActivity()) {
//											System.out.println("not top activity");
//											Log.e("推送", "要求推送");
											int photoCountLocal = preferences.getInt("photoCount", 0);
//											Log.e("photoCountLocal ", "photoCountLocal:"+photoCountLocal);
											photoCount = photoCount + photoCountLocal;
											NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
											
											Intent intent = new Intent(getApplicationContext(),
													com.pictureAir.MainTabActivity.class);
											
											PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,intent, 0);
											Notification notification = new NotificationCompat.Builder(NotificationService.this).
										        	setSmallIcon(R.drawable.pp_icon).setAutoCancel(true).setContentTitle("New Message").
										        	setContentIntent(pendingIntent)
										        	.setContentText("You have new photos").setWhen(System.currentTimeMillis()).setTicker("New Message").build();
												
												notification.flags = Notification.FLAG_AUTO_CANCEL;//通知栏可以自动删除
												notification.defaults = Notification.DEFAULT_ALL;//默认下载完成声音
											manager.notify(0, notification);
											
											Editor editor = preferences.edit();// 获取编辑器
											editor.putInt("photoCount",photoCount);
											editor.commit();// 提交修改
										}else {
//											System.out.println("top activity");
											Intent intent = new Intent();// 创建Intent对象
											intent.setAction("com.receiver.UpdateUiRecriver");
											intent.putExtra("photoCount", photoCount);
											sendBroadcast(intent);
										}
										
										application.setPushPhotoCount(photoCount);

//									}else {
//										System.out.println("photo count = 0");
									}

								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					});


				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
		}.start();
	}
}
