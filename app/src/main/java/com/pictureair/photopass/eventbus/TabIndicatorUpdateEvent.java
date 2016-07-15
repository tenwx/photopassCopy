package com.pictureair.photopass.eventbus;

/**
 * Created by pengwu on 16/7/11.
 */
public class TabIndicatorUpdateEvent implements BaseBusEvent{
    private int dataBasePhotoCount;
    /**
     * 0表示左边
     * 1表示右边
     * */
    private int whichSide;

    public TabIndicatorUpdateEvent(int count,int whichSide){
        this.dataBasePhotoCount = count;
        this.whichSide = whichSide;
    }

    public int getDataBasePhotoCount(){
        return dataBasePhotoCount;
    }

    public int getWhichSide(){
        return whichSide;
    }

}
