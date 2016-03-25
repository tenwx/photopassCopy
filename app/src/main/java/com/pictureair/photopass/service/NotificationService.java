package com.pictureair.photopass.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.MainTabActivity;
import com.pictureair.photopass.activity.PaymentOrderActivity;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.eventbus.SocketEvent;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;

import cn.smssdk.gui.AppManager;
import de.greenrobot.event.EventBus;
import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

/**
 * 推送的服务。
 *
 * @author talon
 */
public class NotificationService extends android.app.Service {
    private final String TAG = "NotificationService";
    private final int SOCKET_CONNECT_SUCCESS = 1111;
    private final int SOCKET_RECEIVE_DATA = 3333;
    private int photoCount;
    private SocketIO socket;
    private SharedPreferences preferences;
    private String userId;

    private MyApplication application;

    private boolean isConnected = false;
    private String sendType;

    private PictureAirDbManager pictureAirDbManager;

    private String syncMessage = "";

    private final Handler notificationHandler = new NotificationHandler(this);

    private static class NotificationHandler extends Handler {
        private final WeakReference<NotificationService> mService;

        public NotificationHandler(NotificationService service) {
            mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mService.get() == null) {
                return;
            }
            mService.get().dealHandler(msg);
        }
    }

    /**
     * 处理Message
     *
     * @param msg
     */
    private void dealHandler(Message msg) {
        switch (msg.what) {
            case SOCKET_CONNECT_SUCCESS: // 链接成功
                API1.noticeSocketConnect();
                break;
            case SOCKET_RECEIVE_DATA: // 接受到信息之后。清空服务器消息。PhotoPass上需要清空四个：照片，订单，视频，upgradedPhoto
                API1.clearSocketCachePhotoCount(sendType);
                break;

            default:
                break;
        }
    }

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
        preferences = getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
        userId = preferences.getString(Common.USERINFO_ID, null);
        pictureAirDbManager = new PictureAirDbManager(this);
        if (intent != null && intent.getStringExtra("status") != null) {//断开连接
            if ("disconnect".equals(intent.getStringExtra("status"))) {
                if (socket != null) {
                    socket.disconnect();
                    isConnected = false;
                }
                stopSelf();//停止服务
            }
        } else {//请求连接
            if (!isConnected) {
                application = (MyApplication) getApplication();
                dealSocket();
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

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        notificationHandler.removeCallbacksAndMessages(null);
    }


    /**
     * socket的回调。
     */
    public void dealSocket() {
        new Thread() {
            public void run() {
                try {
                    socket = new SocketIO(Common.BASE_URL_TEST);
                    socket.connect(new IOCallback() {
                        @Override
                        public void onMessage(JSONObject json, IOAcknowledge arg1) {
                            try {
                                PictureAirLog.d(TAG, "Server said json:" + json.toString(2));
                                PictureAirLog.d(TAG, "IOAcknowledge:" + arg1.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onMessage(String data, IOAcknowledge arg1) {
                            PictureAirLog.d(TAG, "Server said data: " + data);
                            PictureAirLog.d(TAG, "arg1:" + arg1.toString());
                        }

                        @Override
                        public void onError(
                                SocketIOException socketIOException) {
                            // TODO Auto-generated method stub
                            PictureAirLog.d(TAG, "an Error occured：" + socketIOException.toString());
                            socketIOException.printStackTrace();
                            isConnected = false;
                            socket.reconnect();  //出错的情况，让socket重新链接。
                        }

                        @Override
                        public void onDisconnect() {
                            // TODO Auto-generated method stub
                            PictureAirLog.d(TAG, "Connection terminated");
                            isConnected = false;
                        }

                        @Override
                        public void onConnect() {
                            // TODO Auto-generated method stub
                            socket.emit("getNewPhotosCountOfUser", preferences.getString(Common.USERINFO_TOKENID, null));
                            isConnected = true;
                            PictureAirLog.d(TAG, "Connection established");
                            notificationHandler.sendEmptyMessage(SOCKET_CONNECT_SUCCESS);
                        }

                        @Override
                        public void on(String event, IOAcknowledge arg1, Object... arg2) {
                            // TODO Auto-generated method stub
                            PictureAirLog.d("  ====  arg2", " :" + arg2);
                            PictureAirLog.d("===on===", "Server triggered event '" + event + "'");
                            /**
                             * 接收到  订单完成  事件
                             */
                            if (event.toString().equals("doneOrderPay")) {
                                sendType = "doneOrderPay";
                                notificationHandler.sendEmptyMessage(SOCKET_RECEIVE_DATA);// 清空服务器消息。
                                JSONObject message = (JSONObject) arg2[0];
                                try {
                                    message = (JSONObject) message.get("c");
                                    PictureAirLog.d(TAG, "message： " + message);
                                    PaymentOrderActivity.resultJsonObject = message;
//									String orderId = message.getString("orderId");
                                    showNotification(getResources().getString(R.string.notifacation_new_message), getResources().getString(R.string.notifacation_order_completed_msg));
                                } catch (JSONException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }


                            /**
                             *  解决问题：购买照片与升级PP＋,同一用户不同设备不同步的问题。
                             *  升级PP＋与购买照片后发送的事件，记录下来，并且保存在application中。
                             *  升级PP+后的值：{"c":{"customerId":"DPPPTV9BH3U4Z2WS","shootDate":"2015-12-18"}}
                             *  购买照片过后的返回值：{"c":{"id":"*******"}
                             */
                            if (event.toString().equals("upgradedPhotos")) {
                                sendType = "upgradedPhoto";
                                notificationHandler.sendEmptyMessage(SOCKET_RECEIVE_DATA);
                                try {
                                    sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                JSONObject message = (JSONObject) arg2[0];
                                PictureAirLog.e("推送", "收到推送：" + message.toString());
                                int socketType = -1;
                                String ppCode = null, shootDate = null, photoId = null;
                                //1.更新数据库
                                try {
                                    JSONObject updateJsonObject = message.getJSONObject("c");
                                    if (updateJsonObject.toString().equals(syncMessage)) {//和上次的数据相同，直接返回
                                        return;
                                    } else {
                                        syncMessage = updateJsonObject.toString();
                                    }
                                    if (updateJsonObject.has("customerId")) {//ppp升级pp
                                        socketType = SocketEvent.SOCKET_PHOTOPASS;
                                        ppCode = updateJsonObject.getString("customerId");
                                        shootDate = updateJsonObject.getString("shootDate");
                                        pictureAirDbManager.updatePhotoBoughtByPPCodeAndDate(ppCode, shootDate);
                                    } else if (updateJsonObject.has("id")) {//照片购买
                                        socketType = SocketEvent.SOCKET_PHOTO;
                                        photoId = updateJsonObject.getString("id");
                                        pictureAirDbManager.updatePhotoBought(photoId);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                //2.如果处于story页面，则更新数据，并且刷新列表；如果不是处于story页面，则设置更新变量
                                if (application.isStoryTab() && //如果处于story页面，则更新数据，并且刷新列表
                                        !preferences.getBoolean(Common.NEED_FRESH, false)) {//返回到故事页面会重新拉取数据，所以取反
                                    PictureAirLog.out("start sync bought info");
                                    EventBus.getDefault().post(new SocketEvent(true, socketType, ppCode, shootDate, photoId));
                                } else {//如果不是处于story页面，则设置更新变量
                                    application.setNeedRefreshOldPhotos(true);
                                }
                            }

                            /**
                             * 接收到  下订单  事件
                             */
                            if (event.toString().equals("catchOrderInfoOf" + userId)) {
                                sendType = "orderSend";
                                notificationHandler.sendEmptyMessage(SOCKET_RECEIVE_DATA);
                                JSONObject message = (JSONObject) arg2[0];
                                try {
                                    String info = message.getString("info");
                                    PictureAirLog.d(TAG, "接受的订单信息:" + info);
                                    showNotification(getResources().getString(R.string.notifacation_new_message), getResources().getString(R.string.notifacation_order_submit_msg));
                                } catch (NumberFormatException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }

                            /**
                             * 接收到  收到照片  事件
                             */
                            if (event.toString().equals("sendNewPhotosCountOf" + userId)) {
                                JSONObject message = (JSONObject) arg2[0];
                                try {
                                    photoCount = Integer.valueOf(message.getString("c"));
                                    if (photoCount > 0) {
                                        sendType = "photoSend";
                                        notificationHandler.sendEmptyMessage(SOCKET_RECEIVE_DATA);

                                        if (!(AppManager.getInstance().getTopActivity() instanceof MainTabActivity)) {
                                            int photoCountLocal = preferences.getInt("photoCount", 0);
                                            photoCount = photoCount + photoCountLocal;
                                            showNotification(getResources().getString(R.string.notifacation_new_message), getResources().getString(R.string.notifacation_new_photo));
                                            Editor editor = preferences.edit();// 获取编辑器
                                            editor.putInt("photoCount", photoCount);
                                            editor.commit();// 提交修改
                                        } else {
                                            Intent intent = new Intent();// 创建Intent对象
                                            intent.setAction("com.receiver.UpdateUiRecriver");
                                            intent.putExtra("photoCount", photoCount);
                                            sendBroadcast(intent);
                                        }

                                        application.setPushPhotoCount(photoCount);
                                    }

                                } catch (JSONException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }


                            /**
                             * 接收到  视频  事件
                             */
                            if (event.toString().equals("videoGenerate")) {
                                sendType = "videoGenerate";
                                notificationHandler.sendEmptyMessage(SOCKET_RECEIVE_DATA);

                                JSONObject message = (JSONObject) arg2[0];
                                try {
                                    int videoCount = message.getInt("c");
                                    showNotification(getResources().getString(R.string.notifacation_new_message), getResources().getString(R.string.notifacation_new_video));

                                    Intent intent1 = new Intent();// 创建Intent对象
                                    intent1.setAction("com.receiver.UpdateUiRecriver");
                                    sendBroadcast(intent1);

                                    application.setPushViedoCount(videoCount);//设置VideoCount
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    });


                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 初始化notification的数据。
     */
    private void showNotification(String titleStr, String contentStr) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        Intent intent = new Intent(getApplicationContext(),
                MainTabActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(NotificationService.this).
                setSmallIcon(R.drawable.pp_icon).setAutoCancel(true).setContentTitle(titleStr).
                setContentIntent(pendingIntent)
                .setContentText(contentStr).setWhen(System.currentTimeMillis()).setTicker(titleStr).build();

        notification.flags = Notification.FLAG_AUTO_CANCEL;//通知栏可以自动删除
        notification.defaults = Notification.DEFAULT_ALL;//默认下载完成声音

        manager.notify(0, notification);
    }

}
