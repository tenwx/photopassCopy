package com.pictureair.photopass.eventbus;

/**
 * Created by bauer_bao on 16/9/23.
 * story刷新数据完成之后，通知maintab显示引导层，因为引导层要全屏覆盖
 */
public class StoryLoadCompletedEvent implements BaseBusEvent{

    public StoryLoadCompletedEvent() {
    }
}
