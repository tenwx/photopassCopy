package com.pictureair.photopass.eventbus;

/**
 * Created by bauer_bao on 16/9/23.
 * story刷新数据完成之后，通知maintab显示引导层，因为引导层要全屏覆盖
 */
public class StoryLoadCompletedEvent implements BaseBusEvent{
    private boolean showLeadView;

    public StoryLoadCompletedEvent(boolean showLeadView) {
        this.showLeadView = showLeadView;
    }

    public boolean isShowLeadView() {
        return showLeadView;
    }

    public void setShowLeadView(boolean showLeadView) {
        this.showLeadView = showLeadView;
    }
}
