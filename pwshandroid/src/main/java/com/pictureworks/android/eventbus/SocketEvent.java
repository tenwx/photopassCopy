package com.pictureworks.android.eventbus;

/**
 * Created by bauer_bao on 16/1/6.
 */
public class SocketEvent implements BaseBusEvent {
    /**
     * 正常的推送消息
     */
    public static final int SOCKET_NORMAL = 0;
    /**
     * 购买照片的推送消息
     */
    public static final int SOCKET_PHOTO = 1;
    /**
     * 升级pp的推送消息
     */
    public static final int SOCKET_PHOTOPASS = 2;
    /**
     * 成功收到推送消息
     */
    private boolean receiveSocket;
    /**
     * 推送消息的类别
     */
    private int type;
    /**
     * ppcode
     */
    private String customerId;
    /**
     * 绑定时间
     */
    private String shootDate;
    /**
     * photoId
     */
    private String photoId;

    public SocketEvent(boolean receiveSocket, int type, String customerId, String shootDate, String photoId) {
        this.receiveSocket = receiveSocket;
        this.type = type;
        this.customerId = customerId;
        this.shootDate = shootDate;
        this.photoId = photoId;
    }

    public boolean isReceiveSocket() {
        return receiveSocket;
    }

    public int getType() {
        return type;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getShootDate() {
        return shootDate;
    }

    public String getPhotoId() {
        return photoId;
    }
}
