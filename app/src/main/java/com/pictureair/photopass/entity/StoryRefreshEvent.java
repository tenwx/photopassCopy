package com.pictureair.photopass.entity;

/**
 * Created by bauer_bao on 15/12/26.
 */
public class StoryRefreshEvent implements BaseBusEvent {
    public static final int START_REFRESH = 11;
    public static final int STOP_REFRESH = 12;
    private int tab;
    private int refreshStatus;

    public int getRefreshStatus() {
        return refreshStatus;
    }

    public void setRefreshStatus(int refreshStatus) {
        this.refreshStatus = refreshStatus;
    }

    public StoryRefreshEvent(int tab, int refreshStatus) {

        this.tab = tab;
        this.refreshStatus = refreshStatus;
    }

    public int getTab() {
        return tab;
    }

    public void setTab(int tab) {
        this.tab = tab;
    }
}
