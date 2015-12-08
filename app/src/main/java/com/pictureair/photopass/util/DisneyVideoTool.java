package com.pictureair.photopass.util;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.pictureair.photopass.activity.EditVideoNullPhotoActivity;
import com.pictureair.photopass.activity.IsOneGoToVideoActivity;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.activity.SelectPhotoGoToVideoActivity;
import com.pictureair.photopass.activity.VideoPlayerActivity;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.widget.VideoPlayerView;

/**
 * 关于美拍操作
 *
 * @author bass
 */
public class DisneyVideoTool {
    private static final String TAG = "DisneyVideoTool";
    private static final String IS_ONE_GO_TO_DISNEY_VIDEO = "is_one_go_to_disney_video";
    private static Context context = MyApplication.getInstance().getApplicationContext();
//    public static final String DISNEY_VIDEO_SELECT = "disney_video_select";
//    public static final String TEST_MP4_URL = "http://192.168.8.3:3006/test.mp4";//测试链接

    /**
     * 1、第一次使用，则直接进入无视频引导页面，引导用户如何制作； 2、非第一次使用且没有乐拍通照片，则进入介绍页面；
     * 3、非第一次使用且有乐拍通照片，，则直接进入选择照片的页面，照片仅可使用乐拍通已购买的照片；
     */
    public static void getIsOneGoToDisneyVideoPage(Context context) {
        SharedPreferences sharedPreferences = null;
        PictureAirDbManager pictureAirDbManager = null;
        sharedPreferences = context.getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
        pictureAirDbManager = new PictureAirDbManager(context);
        if (pictureAirDbManager.checkFirstTimeStartActivity(
                IS_ONE_GO_TO_DISNEY_VIDEO,
                sharedPreferences.getString(Common.USERINFO_ID, ""))) {// 第一次进入
            Intent intent = new Intent();
            intent.setClass(context, IsOneGoToVideoActivity.class);
            context.startActivity(intent);
        } else {// 第二次进入
            getIsEditImageGoToVideo(context);
        }
        sharedPreferences = null;
        pictureAirDbManager = null;
    }

    /**
     * 立即体验按钮： 如果有已购买照片，直接进入选择照片制作故事的页面； 如果无已购买的照片，直接进入没有乐拍通照片的页面；
     */
    public static void getIsEditImageGoToVideo(Context context) {
        // 判断是否有照片,到MyApplication查询是否有已经购买的照片
        boolean b = true;
        if (b) {// 有：进入编辑
//			Intent intent = new Intent(context, SelectPhotoGoToVideoActivity.class);
//			context.startActivity(intent);
            //测试进入播放视频
            Intent intent = new Intent(context, VideoPlayerActivity.class);
            context.startActivity(intent);
        } else {// 无:进入无照片介绍页面
            context.startActivity(new Intent(context,
                    EditVideoNullPhotoActivity.class));
        }
    }

    /**
     * 将选择的照片上传到服务器 合成视频
     */
    public static boolean photoSendServer(List<PhotoInfo> listPhoto) {
        SharedPreferences sharedPreferences = null;
        sharedPreferences = context.getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
        if (null == listPhoto) {
            PictureAirLog.e(TAG, "listPhoto空的");
            return false;
        } else {
            String userid = sharedPreferences.getString(Common.USERINFO_ID, "");
            String[] photos = new String[listPhoto.size()];
            //此处上传照片id到服务器
            Toast.makeText(context, "userid:" + userid + "\n" + "选择：" + listPhoto.size() + "张。测试:发送到服务器",Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    /**
     * 下载视频
     */
    public static boolean downloadDisneyVideo() {

        return true;
    }

}
