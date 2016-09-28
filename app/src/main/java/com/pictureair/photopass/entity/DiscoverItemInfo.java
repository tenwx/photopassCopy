package com.pictureair.photopass.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by pengwu on 16/9/27.
 */
public class DiscoverItemInfo {

    /**刷新的时间*/
    public String updateTime ;

    public int imageId;
    /**主视图图片链接*/
    public String mainViewUrl;
    /**主视图文案*/
    public String mainViewText;
    /**跳转链接*/
    public String jumpUrl;
    /**子视图的信息*/
    public List<DiscoverListItemInfo> itemList;


    public class DiscoverListItemInfo {
        /**子视图的图片链接*/
        public String itemImageUrl;
        /**子视图的文案*/
        public String itemText;
        /**子视图跳转链接*/
        public String itemJumpUrl;

        public int itemImageId;
    }

}
