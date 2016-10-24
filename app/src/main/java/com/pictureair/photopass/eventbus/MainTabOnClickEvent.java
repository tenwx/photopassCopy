package com.pictureair.photopass.eventbus;

import com.pictureair.photopass.entity.DealingInfo;

/**
 * Created by bauer_bao on 15/12/29.
 */
public class MainTabOnClickEvent implements BaseBusEvent {
    private boolean storyTabClick;

    private DealingInfo dealingInfo;

    private boolean showSpecialDealBar;

    private boolean specialDealBuyClick;

    public MainTabOnClickEvent(boolean storyTabClick, DealingInfo dealingInfo, boolean showSpecialDealBar, boolean specialDealBuyClick) {
        this.storyTabClick = storyTabClick;
        this.dealingInfo = dealingInfo;
        this.showSpecialDealBar = showSpecialDealBar;
        this.specialDealBuyClick = specialDealBuyClick;
    }

    public boolean isShowSpecialDealBar() {
        return showSpecialDealBar;
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
