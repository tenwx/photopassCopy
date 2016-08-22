package com.pictureair.photopass.editPhoto.util;

/**
 * Created by talon on 16/5/21.
 * 纪录一些编辑图片的常量
 */
public class PhotoCommon {

    public static final String StickerPath = "rekcits";

    public static final int EditNone = 00; //没有选择模式
    public static final int EditFrame = 11; //相框模式
    public static final int EditFilter = 22; //滤镜模式
    public static final int EditSticker = 33; //贴图模式
    public static final int EditRotate = 44; //旋转模式


    public static final int UnableLastAndNext = 111; //不能前进和后退
    public static final int AbleLastAndNext = 222; // 可以前进和后退
    public static final int AbleNextUnableLast = 333; //只能前进，不能后退
    public static final int AbleLastUnaleNext = 444; //只能后退，不能前进

}
