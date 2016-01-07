package com.pictureair.photopass.eventbus;

/**
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
