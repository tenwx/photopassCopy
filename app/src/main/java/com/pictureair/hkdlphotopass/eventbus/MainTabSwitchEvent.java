package com.pictureair.photopass.eventbus;

import com.pictureair.photopass.entity.PPinfo;

import java.util.ArrayList;

/**
 * tab事件传递给maintab数据的实体类
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
    public static final int DRAGER_VIEW_UPDATE = 6;//侧边栏更新数据

    private int mainTabSwitchIndex;
    private ArrayList<PPinfo> arrayList;

    public MainTabSwitchEvent(int mainTabSwitchIndex) {
        this.mainTabSwitchIndex = mainTabSwitchIndex;
    }

    public MainTabSwitchEvent(int mainTabSwitchIndex, ArrayList<PPinfo> arrayList) {
        this.mainTabSwitchIndex = mainTabSwitchIndex;
        this.arrayList = arrayList;
    }

    public int getMainTabSwitchIndex() {
        return mainTabSwitchIndex;
    }

    public void setMainTabSwitchIndex(int mainTabSwitchIndex) {
        this.mainTabSwitchIndex = mainTabSwitchIndex;
    }

    public ArrayList<PPinfo> getArrayList() {
        return arrayList;
    }

    public void setArrayList(ArrayList<PPinfo> arrayList) {
        this.arrayList = arrayList;
    }
}
