package com.pictureair.photopass.eventbus;

/**
 * 下载管理事件
 * Created by pengwu on 16/7/11.
 */
public class TabIndicatorUpdateEvent implements BaseBusEvent{
    private int dataBasePhotoCount;
    /**
     * 0表示左边
     * 1表示右边
     * */
    private int whichSide;

    private boolean databaseUpdate = false;
    public TabIndicatorUpdateEvent(int count,int whichSide,boolean databaseUpdate){
        this.dataBasePhotoCount = count;
        this.whichSide = whichSide;
        this.databaseUpdate = databaseUpdate;
    }

    public int getDataBasePhotoCount(){
        return dataBasePhotoCount;
    }

    public int getWhichSide(){
        return whichSide;
    }

    public boolean isDatabaseUpdate(){
        return databaseUpdate;
    }

}
