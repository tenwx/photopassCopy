package com.pictureair.photopass.eventbus;

/**
 * Created by pengwu on 16/7/21.
 */
public class DownLoadCountUpdateEvent implements BaseBusEvent{
    private int updateCount;
    public DownLoadCountUpdateEvent(int updateCount){
        this.updateCount = updateCount;

    }
    public int getUpdateCount() {
        return updateCount;
    }
}
