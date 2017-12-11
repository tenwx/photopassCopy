package com.pictureair.hkdlphotopass.service;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.pictureair.hkdlphotopass.MyApplication;
import com.pictureair.hkdlphotopass.http.rxhttp.RxSubscribe;
import com.pictureair.hkdlphotopass.util.API2;
import com.pictureair.hkdlphotopass.util.Common;
import com.pictureair.hkdlphotopass.util.PictureAirLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;
import rx.Subscription;

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
    private Subscription subscription1, subscription2;

    private Handler notificationHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SOCKET_CONNECT_SUCCESS: // 链接成功
                    subscription1 = API2.noticeSocketConnect()
                            .subscribe(new RxSubscribe<com.alibaba.fastjson.JSONObject>() {
                                @Override
                                public void _onNext(com.alibaba.fastjson.JSONObject jsonObject) {
                                    PictureAirLog.i("SOCKET_CONNECT_SUCCESS", jsonObject.toJSONString());
                                }

                                @Override
                                public void _onError(int status) {
                                    unsubscribe();
                                    PictureAirLog.v(TAG, "noticeSocketConnect 链接失败,状态码：" + status);
                                }

                                @Override
                                public void onCompleted() {
                                    unsubscribe();
                                    PictureAirLog.v(TAG, "noticeSocketConnect 链接成功");
                                }
                            });
                    break;

                case SocketUtil.SOCKET_RECEIVE_DATA: // 接受到信息之后。清空服务器消息。PhotoPass上需要清空四个：照片，订单，视频，upgradedPhoto
                    if (msg.obj != null) {
                        subscription2 = API2.clearSocketCachePhotoCount(msg.obj.toString())
                                .subscribe(new RxSubscribe<com.alibaba.fastjson.JSONObject>() {
                                    @Override
                                    public void _onNext(com.alibaba.fastjson.JSONObject jsonObject) {
                                        PictureAirLog.i("SOCKET_RECEIVE_DATA", jsonObject.toJSONString());
                                    }

                                    @Override
                                    public void _onError(int status) {
                                        unsubscribe();
                                        PictureAirLog.v(TAG, "clearSocketCachePhotoCount 收到推送 清空服务器消息失败,状态码：" + status);
                                    }

                                    @Override
                                    public void onCompleted() {
                                        unsubscribe();
                                        PictureAirLog.v(TAG, "clearSocketCachePhotoCount 收到推送 清空服务器消息成功");
                                    }
                                });
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
            return "disconnect".equals(intent.getStringExtra("status"));
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
        if (subscription1 != null && !subscription1.isUnsubscribed()) {
            subscription1.unsubscribe();
            PictureAirLog.d("service desotry---> 1:" + subscription1.isUnsubscribed());
        }
        if (subscription2 != null && !subscription2.isUnsubscribed()) {
            subscription2.unsubscribe();
            PictureAirLog.d("service desotry---> 2:" + subscription2.isUnsubscribed());
        }
    }

    /**
     * 监听socket的方法
     */
    private void dealSocket() {
        new Thread() {
            public void run() {
                try {
                    socket = new SocketIO(Common.BASE_URL_SOCKET);
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
                            PictureAirLog.d("  ====  arg2", " :" + arg2.toString());
                            PictureAirLog.d("===on===", "Server triggered event '" + event + "'");
                            try {
                                socketUtil.socketOn(event, (JSONObject) arg2[0], true);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });


                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }
}
