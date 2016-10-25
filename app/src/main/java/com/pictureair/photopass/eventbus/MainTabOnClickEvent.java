package com.pictureair.photopass.eventbus;

import com.pictureair.photopass.entity.DealingInfo;

/**
 * Created by bauer_bao on 15/12/29.
 */
public class MainTabOnClickEvent implements BaseBusEvent {
    public static final int STORY_TAB_CLICK_EVENT = 1;

    public static final int SPECIAL_DEAL_EVENT = 2;

    private int eventType;

    /**
     * story tab 点击事件
     */
    private boolean storyTabClick;

    /**
     * 抢单需要的参数
     */
    private DealingInfo dealingInfo;
    private boolean showSpecialDealBar;
    private boolean specialDealBuyClick;

    /**
     * 抢单
     * @param dealingInfo
     * @param showSpecialDealBar
     * @param specialDealBuyClick
     */
    public MainTabOnClickEvent(DealingInfo dealingInfo, boolean showSpecialDealBar, boolean specialDealBuyClick) {
        eventType = SPECIAL_DEAL_EVENT;
        this.dealingInfo = dealingInfo;
        this.showSpecialDealBar = showSpecialDealBar;
        this.specialDealBuyClick = specialDealBuyClick;
    }

    /**
     * story tab 的点击事件
     * @param storyTabClick
     */
    public MainTabOnClickEvent(boolean storyTabClick) {
        eventType = STORY_TAB_CLICK_EVENT;
        this.storyTabClick = storyTabClick;
    }

    public boolean isShowSpecialDealBar() {
        return showSpecialDealBar;
    }

    public int getEventType() {
        return eventType;
    }

    public DealingInfo getDealingInfo() {
        return dealingInfo;
    }

    public boolean isStoryTabClick() {
        return storyTabClick;
    }

    public boolean isSpecialDealBuyClick() {
        return specialDealBuyClick;
    }
}
