package com.pictureair.photopassCopy.entity;

import java.io.Serializable;

/**
 * Created by bauer_bao on 16/10/24.
 */
public class DealingInfo implements Serializable {

    /**
     * currTimeIntervalStart : 2016/01/01 12:00:00
     * state : -3
     * content : 快速抢购担任乐拍通一卡通，您的照片立即全部拥有，现在只需169！
     * details : 只需169元就能获得绑定至同一张迪士尼乐拍通卡内的所有数码照片！
     * title : ppp拼单活动
     * dealingUrl : /api/stores/54b4a700155da2bf0e141bbe/goods/57fa14ae079a90d84b5065d5
     * currTimeIntervalEnd : 2016/12/31 12:00:00
     * currTime : 2016/10/25 01:16:44
     */

    /**
     * 活动最早开始时间
     * */
    private String participateTimeIntervalStart;

    /**
     *  活动最晚结束时间
     * */

    private String participateTimeIntervalEnd;

    /**
     * 当前最接近的活动开始时间
     * */
    private String currTimeIntervalStart;

    /**
     * 当前最接近的活动结束时间
     * */
    private String currTimeIntervalEnd;

    /**
     * 活动状态  1 活动正常开启;   0 活动已关闭;   -1 活动已过期; -2活动未开始;
     * -3非活动时间;  -4活动暂停;  -5用户已参加过;
     * */
    private int state;

    /**活动标题*/
    private String title;

    /**
     * 活动副标题
     * */
    private String content;

    /**
     * 活动内容详情
     * */
    private String details;

    /**
     * 多动内容详情链接
     * */
    private String dealingUrl;

    /**
     * 唯一id
     * */
    private String key;

    /**
     * 是否参加过
     * */
    private Boolean  participated;

    /**
     * 是否可参加
     * */
    private Boolean isPossible;

    /**
     * 总参加活动次数
     * */
    private int totalCount;

    /**
     * 剩余次数,-1为限
     * */
    private int lave;

    /**
     * 服务器当前时间
     * */
    private String currTime;

    /**
     * 本地时间和服务器时间的时间差 ： timeOffset = 本地时间 - 服务器时间
     */
    private long timeOffset;

    public DealingInfo() {

    }

    public DealingInfo(String participateTimeIntervalEnd, String currTimeIntervalStart, Boolean isPossible, int state, String content, int lave, String details, String participateTimeIntervalStart, String title, String key, String currTime, int totalCount, String dealingUrl, String currTimeIntervalEnd, Boolean participated, long timeOffset) {
        this.participateTimeIntervalEnd = participateTimeIntervalEnd;
        this.currTimeIntervalStart = currTimeIntervalStart;
        this.isPossible = isPossible;
        this.state = state;
        this.content = content;
        this.lave = lave;
        this.details = details;
        this.participateTimeIntervalStart = participateTimeIntervalStart;
        this.title = title;
        this.key = key;
        this.currTime = currTime;
        this.totalCount = totalCount;
        this.dealingUrl = dealingUrl;
        this.currTimeIntervalEnd = currTimeIntervalEnd;
        this.participated = participated;
        this.timeOffset = timeOffset;
    }

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

    public String getParticipateTimeIntervalEnd() {
        return participateTimeIntervalEnd;
    }

    public void setParticipateTimeIntervalEnd(String participateTimeIntervalEnd) {
        this.participateTimeIntervalEnd = participateTimeIntervalEnd;
    }

    public int getLave() {
        return lave;
    }

    public void setLave(int lave) {
        this.lave = lave;
    }

    public String getParticipateTimeIntervalStart() {
        return participateTimeIntervalStart;
    }

    public void setParticipateTimeIntervalStart(String participateTimeIntervalStart) {
        this.participateTimeIntervalStart = participateTimeIntervalStart;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCurrTime() {
        return currTime;
    }

    public void setCurrTime(String currTime) {
        this.currTime = currTime;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public Boolean getParticipated() {
        return participated;
    }

    public void setParticipated(Boolean participated) {
        this.participated = participated;
    }

    public Boolean getPossible() {
        return isPossible;
    }

    public void setPossible(Boolean possible) {
        isPossible = possible;
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
                ", currTime='" + currTime + '\'' +
                ", state=" + state +
                ", timeOffset=" + timeOffset +
                ", participateTimeIntervalEnd='" + participateTimeIntervalEnd + '\'' +
                ", isPossible=" + isPossible +
                ", lave=" + lave +
                ", details='" + details + '\'' +
                ", participateTimeIntervalStart='" + participateTimeIntervalStart + '\'' +
                ", key='" + key + '\'' +
                ", currTime='" + currTime + '\'' +
                ", totalCount=" + totalCount +
                ", participated=" + participated +
                '}';
    }
}
