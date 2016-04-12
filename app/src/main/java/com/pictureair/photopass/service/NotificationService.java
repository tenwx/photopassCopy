package com.pictureair.photopass.service;

import android.content.Intent;
import android.os.IBinder;

/**
 * 推送的服务。
 *
 * @author talon
 */
public class NotificationService extends android.app.Service {
    private final String TAG = "NotificationService";
    NotificationServiceHelp notificationServiceHelp;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub\
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        notificationServiceHelp = new NotificationServiceHelp(this);
        if (notificationServiceHelp.isRequireDisconnect(intent)){ //是否接受到断开的信号。
            notificationServiceHelp.disconnectSocket(); //断开socket
            stopSelf();//停止服务
        }else{
            notificationServiceHelp.connectSocket(); // 开启推送。
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

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        notificationServiceHelp.destoryService();
    }

}
