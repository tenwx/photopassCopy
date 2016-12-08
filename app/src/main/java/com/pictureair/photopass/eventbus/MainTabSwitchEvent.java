package com.pictureair.photopass.eventbus;

/**
 * 首页tab切换的eventbus事件
 * Created by bauer_bao on 16/3/11.
 */
public class MainTabSwitchEvent implements BaseBusEvent {
    public static final int STORY_TAB = 0;//故事页面
    public static final int DISCOVER_TAB = 1;//发现页面
    public static final int CAMERA_TAB = 2;//相机页面
//    public static final int DISNEY_STORY_TAB = 2;//我的迪士尼故事（视频）页面
    public static final int SHOP_TAB = 3;//商品页面
    public static final int ME_TAB = 4;//我的页面
    public static final int DRAGER_VIEW = 5;//侧边栏

    private int mainTabSwitchIndex;

    public MainTabSwitchEvent(int mainTabSwitchIndex) {
        this.mainTabSwitchIndex = mainTabSwitchIndex;
    }

    public int getMainTabSwitchIndex() {
        return mainTabSwitchIndex;
    }

    public void setMainTabSwitchIndex(int mainTabSwitchIndex) {
        this.mainTabSwitchIndex = mainTabSwitchIndex;
    }
}
