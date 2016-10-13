package com.pictureair.photopass.editPhoto.util;

/**
 * Created by talon on 16/5/21.
 * 纪录一些编辑图片的常量
 */
public class PhotoCommon {

    public static final String StickerPath = "sticker";

    public static final int EditNone = 00; //没有选择模式
    public static final int EditFrame = 11; //相框模式
    public static final int EditFilter = 22; //滤镜模式
    public static final int EditSticker = 33; //贴图模式
    public static final int EditRotate = 44; //旋转模式


//    public static final int UnableLastAndNext = 111; //不能前进和后退
//    public static final int AbleLastAndNext = 222; // 可以前进和后退
//    public static final int AbleNextUnableLast = 333; //只能前进，不能后退
//    public static final int AbleLastUnaleNext = 444; //只能后退，不能前进


    public static final int OnclickFramePosition = 1111; // 点击单个相框，常量
    public static final int OnclickStickerPosition = 2222; // 点击单个相框，常量
    public static final int OnclickFilterPosition = 3333; // 点击单个相框，常量

    public static final int DOWNLOAD_ONLINE = 9999; // 点击单个相框，常量
    public static final int INIT_DATA_FINISHED = 104;
    public static final int START_ASYNC = 105;

}
