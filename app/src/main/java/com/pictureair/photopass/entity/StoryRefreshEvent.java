package com.pictureair.photopass.entity;

/**
 * Created by bauer_bao on 15/12/26.
 */
public class StoryRefreshEvent implements BaseBusEvent {
    private int tab;

    public StoryRefreshEvent(int tab) {
        this.tab = tab;
    }

    public int getTab() {
        return tab;
    }

    public void setTab(int tab) {
        this.tab = tab;
    }
}
