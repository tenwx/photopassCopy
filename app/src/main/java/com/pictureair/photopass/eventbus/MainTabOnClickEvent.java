package com.pictureair.photopass.eventbus;

/**
 * Created by bauer_bao on 15/12/29.
 */
public class MainTabOnClickEvent implements BaseBusEvent {
    private boolean storyTabClick;

    private boolean showSpecialDealBar;

    public MainTabOnClickEvent(boolean storyTabClick, boolean showSpecialDealBar) {
        this.storyTabClick = storyTabClick;
        this.showSpecialDealBar = showSpecialDealBar;
    }

    public boolean isShowSpecialDealBar() {
        return showSpecialDealBar;
    }

    public boolean isStoryTabClick() {
        return storyTabClick;
    }
}
