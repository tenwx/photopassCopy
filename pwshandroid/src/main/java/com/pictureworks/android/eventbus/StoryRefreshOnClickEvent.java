package com.pictureworks.android.eventbus;

/**
 * Created by bauer_bao on 15/12/29.
 */
public class StoryRefreshOnClickEvent implements BaseBusEvent {
    private boolean storyTabClick;

    public boolean isStoryTabClick() {
        return storyTabClick;
    }

    public void setStoryTabClick(boolean storyTabClick) {
        this.storyTabClick = storyTabClick;
    }

    public StoryRefreshOnClickEvent(boolean storyTabClick) {
        this.storyTabClick = storyTabClick;
    }
}
