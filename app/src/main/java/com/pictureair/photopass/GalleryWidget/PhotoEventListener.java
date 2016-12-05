package com.pictureair.photopass.GalleryWidget;

/**
 * Created by bauer_bao on 16/9/12.
 */
public interface PhotoEventListener {
    /**
     * 视频点击
     * @param position
     */
    void videoClick(int position);

    /**
     * 模糊图的购买
     * @param position
     */
    void buyClick(int position);

    /**
     * 图片的长按点击，只针对清晰图片
     * @param position
     */
    void longClick(int position);
}
