package com.pictureair.hkdlphotopass.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.pictureair.hkdlphotopass.MyApplication;
import com.pictureair.hkdlphotopass.activity.SelectPhotoActivity;
import com.pictureworks.android.entity.PhotoInfo;
import com.pictureworks.android.util.Common;
import com.pictureworks.android.util.PictureAirLog;

/**
 * 关于美拍操作
 *
 * @author bass
 */
public class DisneyVideoTool {
    private static final String TAG = "DisneyVideoTool";
    private static final String IS_ONE_GO_TO_DISNEY_VIDEO = "is_one_go_to_disney_video";
    public static final String IS_BOUGHT = "is_bought";
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
        //取消下面注释，添加引导页
//        SharedPreferences sharedPreferences = context.getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
//        PictureAirDbManager pictureAirDbManager = new PictureAirDbManager(context);
//        if (pictureAirDbManager.checkFirstTimeStartActivity(
//                IS_ONE_GO_TO_DISNEY_VIDEO,
//                sharedPreferences.getString(Common.USERINFO_ID, ""))) {// 第一次进入
//            Intent intent = new Intent();
//            intent.setClass(context, IsOneGoToVideoActivity.class);
//            context.startActivity(intent);
//        } else {
// 第二次进入
            getIsEditImageGoToVideo(context);
            //测试进入播放视频
//            TestGoToVideo(context);
//        }
    }

    /**
     * 测试模拟一个数据
     */
    private static PhotoInfo getPhotoInfo() {
        PhotoInfo photoInfo = new PhotoInfo();
        photoInfo.photoId = "123";
        photoInfo.photoPathOrURL = "123.mp4";//图片的原始路径
        photoInfo.onLine = 1;//1线上图片，0，本地图片
        photoInfo.isVideo = 1;//1是视频，0是图片
        photoInfo.isLove = 1;
        return photoInfo;
    }

    /**
     * 立即体验按钮： 如果有已购买照片，直接进入选择照片制作故事的页面； 如果无已购买的照片，直接进入没有乐拍通照片的页面；
     * ture：进入编辑;false:无图介绍
     */
    public static void getIsEditImageGoToVideo(Context context) {
        // 判断是否有照片,到MyApplication查询是否有已经购买的照片
        //测试
        PictureAirLog.e(TAG,"tokenId:"+ MyApplication.getTokenId());
        SharedPreferences sp = context.getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, Context.MODE_PRIVATE);
        PictureAirLog.e(TAG,"userId:"+ sp.getString(Common.USERINFO_ID,"0") );

        Intent intent = new Intent(context, SelectPhotoActivity.class);
        intent.putExtra("activity", DISNEY_VIDEO);
        intent.putExtra("photoCount", 3);
        context.startActivity(intent);
    }

}
