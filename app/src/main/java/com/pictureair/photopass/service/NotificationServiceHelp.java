package com.pictureair.photopass.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.MainTabActivity;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.eventbus.AsyncPayResultEvent;
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
 * NotificationService 的辅助类
 * Created by talon on 16/4/11.
 */
public class NotificationServiceHelp {
    private final String TAG = "NotificationServiceHelp";
    private final int SOCKET_CONNECT_SUCCESS = 1111;
    private final int SOCKET_RECEIVE_DATA = 3333;
    private Context mContext;
    private SharedPreferences preferences;
    private String userId;
    private PictureAirDbManager pictureAirDbManager;
    private SocketIO socket;
    private MyApplication application = MyApplication.getInstance();
    private boolean isConnected = false; // socket是否链接的状态。（ 如果判断socket 是否为空，这个变量是不是可以不要 ）
    private String sendType; // 受到的socket 事件名，用于接受之后清空相应的消息。
    private String syncMessage = "";

    private Handler notificationHandler = new NotificationHandler(application) ;

    private class NotificationHandler extends Handler {
        private WeakReference<MyApplication> myApplication;

        public NotificationHandler(MyApplication application) {
            myApplication = new WeakReference<>(application);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (myApplication == null) {
                return;
            }
            dealHandler(msg);
        }
    }

    public NotificationServiceHelp(Context context) {
        mContext = context;
        preferences = mContext.getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
        userId = preferences.getString(Common.USERINFO_ID, null);
        pictureAirDbManager = new PictureAirDbManager(mContext);
    }


    /**
     *  是否接受到断开的信号。
     * @param intent
     * @return
     */
    public boolean isRequireDisconnect(Intent intent){
        if (intent != null && intent.getStringExtra("status") != null) {// 要求断开的情况
            if ("disconnect".equals(intent.getStringExtra("status"))) {
                return true;
            }else{
                return false;
            }
        } else {// 要求链接的情况
            return false;
        }
    }


    /**
     * 断开 socket 方法
     */
    public void disconnectSocket() {
        if (socket != null) {
            socket.disconnect();
            isConnected = false;
        }
    }

    /**
     *  请求链接 socket
     *  启用 socket
     */
    public void connectSocket(){
        if (!isConnected) {
            dealSocket();
        }
    }

    /**
     * notification 中的destory方法。removeCallbacksAndMessages
     */
    public void destoryService(){
        notificationHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 订单完成状态 的处理事件。
     * @param message
     */
    private void eventDoneOrderPay(JSONObject message) {
        showNotification(mContext.getResources().getString(R.string.notifacation_new_message), mContext.getResources().getString(R.string.notifacation_order_completed_msg));
        EventBus.getDefault().post(new AsyncPayResultEvent(message));
    }

    /**
     * 升级PP＋与购买照片后发送的事件 的处理事件。
     * @param updateJsonObject
     */
    private void eventUpgradedPhotos(JSONObject updateJsonObject) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int socketType = -1;
        String ppCode = null, shootDate = null, photoId = null;
        //1.更新数据库
        try {
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
     * 下订单 的处理事件。
     * @param message
     */
    private void eventCatchOrderInfoOf(JSONObject message){

        try {
            String info = message.getString("info");
            PictureAirLog.d(TAG, "接受的订单信息:" + info);
            showNotification(mContext.getResources().getString(R.string.notifacation_new_message), mContext.getResources().getString(R.string.notifacation_order_submit_msg));
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 收到新照片 的处理事件。
     * @param message
     */
    private void eventSendNewPhotosCountOf(JSONObject message){
        int photoCount;
        try {
            photoCount = Integer.valueOf(message.getString("c"));
            if (photoCount > 0) {
                notificationHandler.sendEmptyMessage(SOCKET_RECEIVE_DATA); // photoCount大于0 的时候去清空
                if (!(AppManager.getInstance().getTopActivity() instanceof MainTabActivity)) {
                    int photoCountLocal = preferences.getInt("photoCount", 0);
                    photoCount = photoCount + photoCountLocal;
                    showNotification(mContext.getResources().getString(R.string.notifacation_new_message), mContext.getResources().getString(R.string.notifacation_new_photo));
                    SharedPreferences.Editor editor = preferences.edit();// 获取编辑器
                    editor.putInt("photoCount", photoCount);
                    editor.commit();// 提交修改
                } else {
                    Intent intent = new Intent();// 创建Intent对象
                    intent.setAction("com.receiver.UpdateUiRecriver");
                    intent.putExtra("photoCount", photoCount);
                    mContext.sendBroadcast(intent);
                }

                application.setPushPhotoCount(photoCount);
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 收到新视频 的处理事件。
     * @param message
     */
    private void eventVideoGenerate(JSONObject message){

        try {
            int videoCount = message.getInt("c");
            showNotification(mContext.getResources().getString(R.string.notifacation_new_message), mContext.getResources().getString(R.string.notifacation_new_video));

            Intent intent = new Intent();// 创建Intent对象
            intent.setAction("com.receiver.UpdateUiRecriver");
            mContext.sendBroadcast(intent);
            application.setPushViedoCount(videoCount);//设置VideoCount
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 状态链接上之后，监听的事件。 socket on 方法
     * @param envenStr
     * @param message
     * @param isSocketReceive   是否是socket接收。
     */
    public void socketOn(String envenStr, JSONObject message, boolean isSocketReceive) throws JSONException {
        if (envenStr.equals("doneOrderPay")){
            if (isSocketReceive){
                sendType = "doneOrderPay";
                notificationHandler.sendEmptyMessage(SOCKET_RECEIVE_DATA); //清空推送，不能移动位置。
                message = (JSONObject) message.get("c");
            }
            eventDoneOrderPay(message);
        }else if(envenStr.equals("upgradedPhotos")){
            if (isSocketReceive){
                sendType = "upgradedPhoto";
                notificationHandler.sendEmptyMessage(SOCKET_RECEIVE_DATA);
                message = message.getJSONObject("c");
            }
            eventUpgradedPhotos(message);
        }else if(envenStr.equals("catchOrderInfoOf" + userId)){
            sendType = "orderSend";
            notificationHandler.sendEmptyMessage(SOCKET_RECEIVE_DATA);
            eventCatchOrderInfoOf(message);
        }else if(envenStr.equals("sendNewPhotosCountOf" + userId)){
            sendType = "photoSend";
            eventSendNewPhotosCountOf(message);
        }else if(envenStr.equals("videoGenerate")){
            sendType = "videoGenerate";
            notificationHandler.sendEmptyMessage(SOCKET_RECEIVE_DATA);
            eventVideoGenerate(message);
        }

    }


    /**
     * 监听socket的方法
     */
    private void dealSocket() {
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
                            socket.emit("getNewPhotosCountOfUser", MyApplication.getTokenId());
                            isConnected = true;
                            PictureAirLog.d(TAG, "Connection established");
                            notificationHandler.sendEmptyMessage(SOCKET_CONNECT_SUCCESS);
                        }

                        @Override
                        public void on(String event, IOAcknowledge arg1, Object... arg2) {
                            // TODO Auto-generated method stub
                            PictureAirLog.d("  ====  arg2", " :" + arg2);
                            PictureAirLog.d("===on===", "Server triggered event '" + event + "'");

                            try {
                                socketOn(event.toString(),(JSONObject) arg2[0],true);
                            } catch (JSONException e) {
                                e.printStackTrace();
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


    private Notification notification ;
    private Intent intent;
    private long exitTime = 0;
    /**
     * 初始化notification的数据
     * @param titleStr
     * @param contentStr
     */
    private void showNotification(String titleStr, String contentStr) {
        NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (intent == null){
            intent = new Intent(mContext, MainTabActivity.class);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        if (notification == null){
            notification = new NotificationCompat.Builder(mContext).
                    setSmallIcon(R.drawable.pp_icon).setAutoCancel(true).setContentTitle(titleStr).
                    setContentIntent(pendingIntent)
                    .setContentText(contentStr).setWhen(System.currentTimeMillis()).setTicker(titleStr).build();
        }
        notification.flags = Notification.FLAG_AUTO_CANCEL;//通知栏可以自动删除
        notification.defaults = Notification.DEFAULT_ALL;//默认下载完成声音

        if ((System.currentTimeMillis() - exitTime) > 2000) {  // 两秒之内避免重复推送。
            manager.notify(0, notification);
            exitTime = System.currentTimeMillis();
        }

    }


}
