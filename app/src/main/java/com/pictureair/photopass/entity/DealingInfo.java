package com.pictureair.photopass.entity;

/**
 * Created by bauer_bao on 16/10/24.
 */
public class DealingInfo {

    /**
     * currTimeIntervalStart : 2016/01/01 12:00:00
     * state : -3
     * content : 快速抢购担任乐拍通一卡通，您的照片立即全部拥有，现在只需169！
     * details : 只需169元就能获得绑定至同一张迪士尼乐拍通卡内的所有数码照片！
     * title : ppp拼单活动
     * dealingUrl : /api/stores/54b4a700155da2bf0e141bbe/goods/57fa14ae079a90d84b5065d5
     * currTimeIntervalEnd : 2016/12/31 12:00:00
     */

    private String currTimeIntervalStart;
    private String currTimeIntervalEnd;
    private String title;
    private String content;
    private String details;
    private String dealingUrl;
    private int state;
    /**
     * 本地时间和服务器时间的时间差
     */
    private long timeOffset;

    public String getCurrTimeIntervalStart() {
        return currTimeIntervalStart;
    }

    public void setCurrTimeIntervalStart(String currTimeIntervalStart) {
        this.currTimeIntervalStart = currTimeIntervalStart;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDealingUrl() {
        return dealingUrl;
    }

    public void setDealingUrl(String dealingUrl) {
        this.dealingUrl = dealingUrl;
    }

    public String getCurrTimeIntervalEnd() {
        return currTimeIntervalEnd;
    }

    public void setCurrTimeIntervalEnd(String currTimeIntervalEnd) {
        this.currTimeIntervalEnd = currTimeIntervalEnd;
    }

    public long getTimeOffset() {
        return timeOffset;
    }

    public void setTimeOffset(long timeOffset) {
        this.timeOffset = timeOffset;
    }

    @Override
    public String toString() {
        return "DealingInfo{" +
                "currTimeIntervalStart='" + currTimeIntervalStart + '\'' +
                ", currTimeIntervalEnd='" + currTimeIntervalEnd + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", details='" + details + '\'' +
                ", dealingUrl='" + dealingUrl + '\'' +
                ", state=" + state +
                ", timeOffset=" + timeOffset +
                '}';
    }
}
