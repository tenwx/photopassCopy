package com.pictureair.photopass.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.MainTabActivity;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.eventbus.AsyncPayResultEvent;
import com.pictureair.photopass.eventbus.RedPointControlEvent;
import com.pictureair.photopass.eventbus.SocketEvent;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import com.pictureair.photopass.util.AppManager;
import de.greenrobot.event.EventBus;

/**
 * Created by bauer_bao on 16/4/18.
 */
public class SocketUtil {
    private Notification notification;
    private Intent intent;
    private long exitTime = 0;
    private Context mContext;
    private PictureAirDbManager pictureAirDbManager;
    private ArrayList<String> syncMessageList = new ArrayList<>();
    private MyApplication application = MyApplication.getInstance();
    private Handler handler;
    private SharedPreferences sharedPreferences;
    private String userId;
    private static final String TAG = "SocketUtil";
    public static final int SOCKET_RECEIVE_DATA = 3333;
    private static final int RED_POINT = 2222;
    private Vibrator vibrator;
    private long[] pattern = {0, 200, 300, 200};

    public SocketUtil(Context mContext, Handler handler, SharedPreferences sharedPreferences) {
        this.mContext = mContext;
        this.handler = handler;
        this.sharedPreferences = sharedPreferences;
        pictureAirDbManager = new PictureAirDbManager(mContext);
        userId = sharedPreferences.getString(Common.USERINFO_ID, null);
        //获得震动服务
        vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
    }

    private Handler redPointHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case RED_POINT:
                    EventBus.getDefault().post(new RedPointControlEvent(true));
                    //-1表示不重复, 如果不是-1, 比如改成1, 表示从前面这个long数组的下标为1的元素开始重复.
                    vibrator.vibrate(pattern, -1);
                    break;

                default:
                    break;
            }
            return false;
        }
    });

    /**
     * 订单完成状态 的处理事件。
     *
     * @param message
     */
    private void eventDoneOrderPay(JSONObject message) {
        PictureAirLog.v(TAG, "eventDoneOrderPay（）: " + message);
        showNotification(mContext.getResources().getString(R.string.notifacation_new_message), mContext.getResources().getString(R.string.notifacation_order_completed_msg));
        EventBus.getDefault().post(new AsyncPayResultEvent(message));
    }

    /**
     * 升级PP＋与购买照片后发送的事件 的处理事件。
     *
     * @param updateJsonObject
     */
    private void eventUpgradedPhotos(JSONObject updateJsonObject, boolean isDelete) {
        PictureAirLog.out("upgrade photo---->" + updateJsonObject.toString());
        int socketType = -1;
        String ppCode = null, shootDate = null, photoId = null;
        //1.更新数据库
        try {
            if (syncMessageList.contains(updateJsonObject.toString() + (isDelete ? "del" : "sync"))) {//和上次的数据相同，直接返回
                PictureAirLog.out("same and return" + updateJsonObject.toString());
                return;
            } else {
                PictureAirLog.out("a new notifycation" + updateJsonObject.toString());
                syncMessageList.add(updateJsonObject.toString() + (isDelete ? "del" : "sync"));
            }
            if (updateJsonObject.has("customerId")) {//ppp升级pp
                socketType = SocketEvent.SOCKET_PHOTOPASS;
                ppCode = updateJsonObject.getString("customerId");
                shootDate = updateJsonObject.getString("shootDate");
                pictureAirDbManager.updatePhotoBoughtByPPCodeAndDate(ppCode, shootDate, isDelete);
            } else if (updateJsonObject.has("id")) {//照片购买
                socketType = SocketEvent.SOCKET_PHOTO;
                photoId = updateJsonObject.getString("id");
                pictureAirDbManager.updatePhotoBought(photoId, isDelete);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //2.如果处于story页面，则更新数据，并且刷新列表；如果不是处于story页面，则设置更新变量
        if (application.isStoryTab() && //如果处于story页面，则更新数据，并且刷新列表
                !sharedPreferences.getBoolean(Common.NEED_FRESH, false)) {//返回到故事页面会重新拉取数据，所以取反
            PictureAirLog.out("start sync bought info");

            EventBus.getDefault().post(new SocketEvent(true, socketType, ppCode, shootDate, photoId));
        } else {//如果不是处于story页面，则设置更新变量
            application.setNeedRefreshOldPhotos(true);
        }
    }

    /**
     * 下订单 的处理事件。
     *
     * @param message
     */
    private void eventCatchOrderInfoOf(JSONObject message) {
        try {
            String info = message.getString("info");
            PictureAirLog.out("接受的订单信息:" + info);
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
     *
     * @param message
     */
    private void eventSendNewPhotosCountOf(JSONObject message, String sendType) {
        int photoCount;
        try {
            photoCount = Integer.valueOf(message.getString("c"));
            if (photoCount > 0) {
                handler.obtainMessage(SOCKET_RECEIVE_DATA, sendType).sendToTarget();// photoCount大于0 的时候去清空
                if (!(AppManager.getInstance().getTopActivity() instanceof MainTabActivity)) {
                    int photoCountLocal = sharedPreferences.getInt("photoCount", 0);
                    photoCount = photoCount + photoCountLocal;
                    showNotification(mContext.getResources().getString(R.string.notifacation_new_message), mContext.getResources().getString(R.string.notifacation_new_photo));
                    SharedPreferences.Editor editor = sharedPreferences.edit();// 获取编辑器
                    editor.putInt("photoCount", photoCount);
                    editor.commit();// 提交修改
                } else {
                    redPointHandler.sendEmptyMessage(RED_POINT);
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
     *
     * @param message
     */
    private void eventVideoGenerate(JSONObject message) {

        try {
            int videoCount = message.getInt("c");
            showNotification(mContext.getResources().getString(R.string.notifacation_new_message), mContext.getResources().getString(R.string.notifacation_new_video));

            redPointHandler.sendEmptyMessage(RED_POINT);
            application.setPushViedoCount(videoCount);//设置VideoCount
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 初始化notification的数据
     *
     * @param titleStr
     * @param contentStr
     */
    private void showNotification(String titleStr, String contentStr) {
        NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (intent == null) {
            intent = new Intent(mContext, MainTabActivity.class);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        if (notification == null) {
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

    /**
     * 状态链接上之后，监听的事件。 socket on 方法
     *
     * @param envenStr        事件名
     * @param message         内容
     * @param isSocketReceive 是否是socket接收。
     */
    public void socketOn(String envenStr, JSONObject message, boolean isSocketReceive) throws JSONException {
        PictureAirLog.v(TAG, "socketOn: envenStr: " + envenStr);//注意事件名要对应
        PictureAirLog.v(TAG, "socketOn: message: " + message);
        if (envenStr.equals("doneOrderPay")) {//订单完成支付推送
            if (isSocketReceive) {
                handler.obtainMessage(SOCKET_RECEIVE_DATA, "doneOrderPay").sendToTarget();//清空推送，不能移动位置。
                message = (JSONObject) message.get("c");
            }
            eventDoneOrderPay(message);

        } else if (envenStr.equals("upgradedPhotos")) {//升级照片推送
            if (isSocketReceive) {
                handler.obtainMessage(SOCKET_RECEIVE_DATA, "upgradedPhoto").sendToTarget();//清空推送，不能移动位置。
                message = message.getJSONObject("c");
            }
            eventUpgradedPhotos(message, false);

        } else if (envenStr.equals("catchOrderInfoOf" + userId)) {//下单推送
            handler.obtainMessage(SOCKET_RECEIVE_DATA, "orderSend").sendToTarget();//清空推送，不能移动位置。
            eventCatchOrderInfoOf(message);

        } else if (envenStr.equals("sendNewPhotosCountOf" + userId)) {//新照片推送
            handler.obtainMessage(SOCKET_RECEIVE_DATA, "photoSend").sendToTarget();//清空推送，不能移动位置。
            eventSendNewPhotosCountOf(message, "photoSend");

        } else if (envenStr.equals("videoGenerate")) {//视频生成推送
            handler.obtainMessage(SOCKET_RECEIVE_DATA, "videoGenerate").sendToTarget();//清空推送，不能移动位置。
            handler.sendEmptyMessage(SOCKET_RECEIVE_DATA);
            eventVideoGenerate(message);

        } else if (envenStr.equals("delPhotos")) {//删除图片，以及删除pp对应的逻辑
            if (isSocketReceive) {
                handler.obtainMessage(SOCKET_RECEIVE_DATA, "delPhotos").sendToTarget();//清空推送，不能移动位置。
                message = (JSONObject) message.get("c");
            }
            //处理删除照片的逻辑
            eventUpgradedPhotos(message, true);
        }
    }
}
