package com.pictureair.photopass.service;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;

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
    private Context mContext;
    private static SocketIO socket;
    private boolean isConnected = false; // socket是否链接的状态。（ 如果判断socket 是否为空，这个变量是不是可以不要 ）
    private SocketUtil socketUtil;

    private Handler notificationHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SOCKET_CONNECT_SUCCESS: // 链接成功
                    API1.noticeSocketConnect();
                    break;

                case SocketUtil.SOCKET_RECEIVE_DATA: // 接受到信息之后。清空服务器消息。PhotoPass上需要清空四个：照片，订单，视频，upgradedPhoto
                    if (msg.obj != null) {
                        API1.clearSocketCachePhotoCount(msg.obj.toString());
                    }
                    break;

                default:
                    break;
            }
            return false;
        }
    });

    public NotificationServiceHelp(Context context) {
        mContext = context;
        socketUtil = new SocketUtil(mContext, notificationHandler);
    }

    /**
     * 是否接受到断开的信号。
     *
     * @param intent
     * @return
     */
    public boolean isRequireDisconnect(Intent intent) {
        if (intent != null && intent.getStringExtra("status") != null) {// 要求断开的情况
            if ("disconnect".equals(intent.getStringExtra("status"))) {
                return true;
            } else {
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
     * 请求链接 socket
     * 启用 socket
     */
    public void connectSocket() {
        if (!isConnected) {
            dealSocket();
        }
    }

    /**
     * notification 中的destory方法。removeCallbacksAndMessages
     */
    public void destoryService() {
        notificationHandler.removeCallbacksAndMessages(null);
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
//                            PictureAirLog.d("  ====  arg2", " :" + arg2.toString());
//                            PictureAirLog.d("===on===", "Server triggered event '" + event + "'");
                            try {
                                socketUtil.socketOn(event.toString(), (JSONObject) arg2[0], true);
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
}
