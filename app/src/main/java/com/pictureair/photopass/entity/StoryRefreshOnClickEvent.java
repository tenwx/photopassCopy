package com.pictureair.photopass.entity;

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
