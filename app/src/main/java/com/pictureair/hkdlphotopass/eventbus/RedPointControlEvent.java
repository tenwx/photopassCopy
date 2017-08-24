package com.pictureair.hkdlphotopass.eventbus;

/**
 * 红点控制事件
 * Created by bauer_bao on 16/1/7.
 */
public class RedPointControlEvent implements BaseBusEvent{
    private boolean showRedPoint;

    public RedPointControlEvent(boolean showRedPoint) {
        this.showRedPoint = showRedPoint;
    }

    public boolean isShowRedPoint() {
        return showRedPoint;
    }

    public void setShowRedPoint(boolean showRedPoint) {
        this.showRedPoint = showRedPoint;
    }
}
