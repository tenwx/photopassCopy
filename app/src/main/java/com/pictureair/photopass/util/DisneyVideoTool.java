package com.pictureair.photopass.util;

import android.content.Context;
import android.content.Intent;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.activity.SelectPhotoActivity;

/**
 * 关于美拍操作
 *
 * @author bass
 */
public class DisneyVideoTool {
    private static final String TAG = "DisneyVideoTool";
    public static final String FROM_STORY = "from_story";
    public static final String DISNEY_VIDEO = "disney_video";

    /**
     * 2015-12 的需求
     * 1、第一次使用，则直接进入无视频引导页面，引导用户如何制作； 2、非第一次使用且没有乐拍通照片，则进入介绍页面；
     * 3、非第一次使用且有乐拍通照片，，则直接进入选择照片的页面，照片仅可使用乐拍通已购买的照片；
     *
     * 2016－1 的新需求（删除引导页）
     * 点击视频Icon：
     * 1、已购买的照片>=3张照片：选择照片页面（底部三个icon）
     * 2、已购买的照片< 3张照片：无照片页面  （底部三个icon）
     */
    public static void getIsOneGoToDisneyVideoPage(Context context) {
        getIsEditImageGoToVideo(context);
    }

    /**
     * 立即体验按钮： 如果有已购买照片，直接进入选择照片制作故事的页面； 如果无已购买的照片，直接进入没有乐拍通照片的页面；
     * ture：进入编辑;false:无图介绍
     */
    public static void getIsEditImageGoToVideo(Context context) {
        // 判断是否有照片,到MyApplication查询是否有已经购买的照片
        //测试
        PictureAirLog.e(TAG,"tokenId:"+ MyApplication.getTokenId());
        Intent intent = new Intent(context, SelectPhotoActivity.class);
        intent.putExtra("activity", DISNEY_VIDEO);
        intent.putExtra("photoCount", 3);
        context.startActivity(intent);
    }

}
