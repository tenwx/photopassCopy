package com.pictureair.photopass.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.Base64;
import com.loopj.android.http.RequestParams;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.HttpCallback;
import com.pictureair.photopass.util.HttpUtil1;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.util.UmengUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 下载网络图片服务类
 * 下载网络视频服务类
 */
public class DownloadService extends Service {
    private ArrayList<PhotoInfo> photos = new ArrayList<PhotoInfo>();
    private ArrayList<PhotoInfo> downloadList = new ArrayList<PhotoInfo>();
    private int downed_num = 0;//实际下载照片数
    private int exist_num = 0 , scan_num = 0;//无需下载的照片数,扫描成功的照片数
    private int failed_num = 0;//下载失败的照片数

    private Context mContext = this;
    private NotificationManager manager;
    private Notification notification;
    private final static int FINISH_DOWNLOAD = 1;
    private final static int START_DOWNLOAD = 2;

    private boolean isDownloading = false;
    private File file;  //文件
    private String photoId;// 图片的photoId

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("downloadService ---------> onCreate" + downed_num + "_" + failed_num);
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //		serviceIntent = intent;
        System.out.println("DownloadService ----------> onStartCommand");
        Bundle b = intent.getExtras();
        photos = b.getParcelableArrayList("photos");
        //将新的数据放入到下载队列的末尾
        for (int i = 0; i < photos.size(); i++) {
            downloadList.add(photos.get(i));
            System.out.println("downloadlist size =" + downloadList.size());
        }
        if (!isDownloading) {//如果当前不在下载
            prepareDownload();
            isDownloading = true;
        }

        return START_NOT_STICKY;//被系统kill之后，不会自动复活重新启动服务
    }

    private void prepareDownload() {
        // TODO Auto-generated method stub
        System.out.println("DownloadService ----------> preparedownload");
        System.out.println("DownloadService ----------> before notification");
        notification = new Notification(R.drawable.pp_icon, mContext.getString(R.string.downloading), System.currentTimeMillis());
        notification.flags = Notification.FLAG_NO_CLEAR;
        notification.setLatestEventInfo(mContext, mContext.getString(R.string.app_name), mContext.getString(R.string.downloading), null);
        manager.notify(0, notification);
        System.out.println("DownloadService ----------> after notification");
        handler.sendEmptyMessage(START_DOWNLOAD);
    }

    //下载文件成功之后的回调函数
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case START_DOWNLOAD:
                    if (downloadList.size() > 0) {//开始下载
                        System.out.println("start download----------------->");
                        photoId = downloadList.get(0).photoId;
                        downLoad(downloadList.get(0).photoPathOrURL, downloadList.get(0).photoId, downloadList.get(0).isVideo);
                    } else {//说明列表已经全部下载完,要对完成的结果进行处理
                        System.out.println("finish download-------------->");
                        handler.sendEmptyMessage(FINISH_DOWNLOAD);
                    }
                    break;
                case FINISH_DOWNLOAD:
                    //如果下载数目一致，提示用户下载完毕，并且让service停止掉
                    System.out.println("下载完毕,共下载了" + downed_num + "张照片，失败了" + failed_num + "张");
                    String notificationDetail = String.format(mContext.getString(R.string.download_detail1), downed_num);
                    if (failed_num >0) {
                        notificationDetail = String.format(mContext.getString(R.string.download_detail2), downed_num, failed_num);
                    }
                    notification = new Notification(R.drawable.pp_icon, mContext.getString(R.string.finish_download), System.currentTimeMillis());
                    //						Intent intentBack = new Intent(mContext, SelectPhotoActivity.class);
                    //						PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intentBack, PendingIntent.FLAG_UPDATE_CURRENT);
                    //						notification.setLatestEventInfo(mContext, "下载完毕", "共" + downed_num + "张", pIntent);
                    notification.setLatestEventInfo(mContext, mContext.getString(R.string.app_name), notificationDetail, null);
                    notification.flags = Notification.FLAG_AUTO_CANCEL;//通知栏可以自动删除
                    notification.defaults = Notification.DEFAULT_SOUND;//默认下载完成声音
                    //					scan(msg.obj.toString());
                    stopSelf();//下载服务停止
                    manager.notify(0, notification);
                    downed_num = 0;
                    failed_num = 0;
                    isDownloading = false;
                    break;

                case API1.DOWNLOAD_PHOTO_SUCCESS://下载成功之后获取data数据，然后base64解码，然后保存。
                    JSONObject objectSuccess;
                    objectSuccess = JSONObject.parseObject(msg.obj.toString());
                    saveFile(file, Base64.decodeFast(objectSuccess.getString("data")));
                    break;
                case API1.DOWNLOAD_PHOTO_FAILED:
                    ++failed_num;
                    downloadList.remove(0);
                    handler.sendEmptyMessage(START_DOWNLOAD);
                    break;


                default:
                    break;
            }
        }

        ;
    };

    /**
     * 下载文件方法
     * 1.判断本地存不存在此文件，如果存在，直接结束
     * 2.判断缓存是否存在此文件，如果存在，从缓存中下载图片，并且保存到SDcard
     * 3.如果缓存不存在此文件，调用API下载图片，并且保存到SDcard
     *
     * @param originalUrl 需要下载文件的原始路径
     * @param id          对应文件的id
     */
    private void downLoad(String originalUrl, String id, int isVideo) {
        String fileName = ScreenUtil.getReallyFileName(originalUrl);
        System.out.println("filename=" + fileName);
        File filedir = new File(Common.PHOTO_DOWNLOAD_PATH);
        filedir.mkdirs();
        file = new File(filedir + "/" + fileName);
        if (!file.exists()) {
            // 使用友盟统计点击下载次数
            UmengUtil.onEvent(mContext, Common.EVENT_ONCLICK_DOWNLOAD);
            downloadImgOrVideo(file, isVideo);
        } else {
            System.out.println("file exist");
            ++downed_num;
            exist_num++;
            downloadList.remove(0);
            handler.sendEmptyMessage(START_DOWNLOAD);
            //			sendMsg(file);
        }
    }

    /**
     * 判断下载视频还是 图片
     */
    private void downloadImgOrVideo(final File file, int isVideo) {
        if (isVideo == 0) {//photo
            API1.downLoadPhotos(handler, photoId);
        } else {//video
            String downloadURL = Common.PHOTO_URL + downloadList.get(0).photoPathOrURL;
            RequestParams params = new RequestParams();
            params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
            params.put(Common.PHOTOIDS, photoId);
            HttpUtil1.asyncDownloadBinaryData(downloadURL, params, new HttpCallback() {
                @Override
                public void onSuccess(byte[] binaryData) {
                    super.onSuccess(binaryData);
                    PictureAirLog.v("asynDownloadFile onSuccess", "binaryData size: " + binaryData.length);
                    saveFile(file, binaryData);
                }

                @Override
                public void onFailure(int status) {
                    super.onFailure(status);
                    PictureAirLog.v("asynDownloadFile", " onFailure status :" + status);
                    ++failed_num;
                    downloadList.remove(0);
                    handler.sendEmptyMessage(START_DOWNLOAD);
                }
            });
        }
    }
    /**
     * 保存文件到SDcard
     *
     * @param file 保存的目标文件名
     * @param data 文件的data数据
     */
    private void saveFile(File file, byte[] data) {
        // TODO Auto-generated method stub
        BufferedOutputStream stream = null;
        try {
            file.createNewFile();
            FileOutputStream fsStream = new FileOutputStream(file);
            stream = new BufferedOutputStream(fsStream);
            stream.write(data);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (stream != null) {
                    stream.flush();
                    stream.close();
                    ++downed_num;
                    // 使用友盟统计下载成功次数
                    UmengUtil.onEvent(mContext, Common.EVENT_DOWNLOAD_FINISH);
                    scan(file.toString());
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
    /**
     * 扫描文件
     *
     * @param file
     */
    private void scan(final String file) {
        // TODO Auto-generated method stub
        MediaScannerConnection.scanFile(this, new String[]{file}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        System.out.println("okdsffads");
                        //				stopSelf();//下载服务停止
                        //				stopService(serviceIntent);
                        scan_num++;
                        downloadList.remove(0);
                        handler.sendEmptyMessage(START_DOWNLOAD);
                    }
                });
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        System.out.println("downloadService-----------> ondestroy");
        super.onDestroy();
    }

}
