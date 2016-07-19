package com.pictureair.photopass.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.loopj.android.http.RequestParams;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.DownloadFileStatus;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.fragment.DownLoadingFragment;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.HttpCallback;
import com.pictureair.photopass.util.HttpUtil1;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.UmengUtil;
import com.pictureair.photopass.widget.PWToast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 下载网络图片服务类
 * 下载网络视频服务类
 */
public class DownloadService extends Service {
    private ArrayList<PhotoInfo> photos = new ArrayList<PhotoInfo>();
    private Vector<DownloadFileStatus> downloadList = new Vector<DownloadFileStatus>();
    private List<DownloadFileStatus> removeCache = new Vector<DownloadFileStatus>();
    private Map<String,DownloadFileStatus> taskList = new HashMap<>();
    private Map<String,DownloadFileStatus> cacheList = new HashMap<>();
    private Map<String,File> fileList = new ConcurrentHashMap<>();
    private int downed_num = 0;//实际下载照片数
    private int exist_num = 0 , scan_num = 0;//无需下载的照片数,扫描成功的照片数
    private int failed_num = 0;//下载失败的照片数
    private boolean mFinish = false;

    private Context mContext = this;
    private NotificationManager manager;
    private Notification notification;
    private final static int FINISH_DOWNLOAD = 1;
    private final static int START_DOWNLOAD = 2;
    public final static int ADD_DOWNLOAD = 3;
//    public final static int SEND_TO_FRAGMENT = 4;

    private boolean isDownloading = false;
    //    private File file;  //文件
//    private String photoId;// 图片的photoId
    private PWToast myToast;
    private PhotoBind photoBind = new PhotoBind();
    private Handler adapterHandler;
    private PictureAirDbManager pictureAirDbManager;
    private SharedPreferences preferences;
    private int databasePhotoCount;//未下载之前的数据库照片数量
    private ExecutorService fixedThreadPool;

    @Override
    public IBinder onBind(Intent arg0) {
        return photoBind;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PictureAirLog.out("downloadService ---------> onCreate" + downed_num + "_" + failed_num);
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        myToast = new PWToast(getApplicationContext());
        pictureAirDbManager = new PictureAirDbManager(getApplicationContext());
        preferences = getApplicationContext().getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, Context.MODE_PRIVATE);
        fixedThreadPool = Executors.newFixedThreadPool(1);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //		serviceIntent = intent;
        PictureAirLog.out("DownloadService ----------> onStartCommand");
        mFinish = false;
        Bundle b = intent.getExtras();
        if (b != null) {
            photos = b.getParcelableArrayList("photos");
            int posiotion = b.getInt("posiotion",-1);
            if (!AppUtil.checkPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                myToast.setTextAndShow(R.string.permission_storage_message, Common.TOAST_SHORT_TIME);
                stopSelf();//下载服务停止
            } else if (photos != null && photos.size() > 0) {
                //将新的数据放入到下载队列的末尾
                synchronized (taskList) {
                    if (posiotion > -1) {
                        if (downloadList.size() > 0) {
                            PhotoInfo info = photos.get(0);
                            DownloadFileStatus fileStatus = new DownloadFileStatus(info.photoPathOrURL, "0", "0", "0", info.photoId, info.isVideo,info.photoThumbnail,info.shootOn);
                            for (int i = 0; i < downloadList.size();i++) {
                                if (i == posiotion) {
                                    downloadList.remove(i);
                                    downloadList.add(i, fileStatus);
                                }
                            }
                        }
                    } else {
                        for (int i = 0; i < photos.size(); i++) {
                            PhotoInfo photoInfo = photos.get(i);
                            DownloadFileStatus fileStatus = new DownloadFileStatus(photoInfo.photoPathOrURL, "0", "0", "0", photoInfo.photoId, photoInfo.isVideo,photoInfo.photoThumbnail,photoInfo.shootOn);
                            String fileName = AppUtil.getReallyFileName(fileStatus.getUrl(),fileStatus.isVideo());
                            PictureAirLog.out("filename=" + fileName);
                            File filedir = new File(Common.PHOTO_DOWNLOAD_PATH);
                            filedir.mkdirs();
                            File file = new File(filedir + "/" + fileName);
                            if (!file.exists()) {
                                if (cacheList.get(fileStatus.getUrl()) == null) {
                                    downloadList.add(fileStatus);
                                    cacheList.put(fileStatus.getUrl(), fileStatus);
                                }
                            }else{
                                ++downed_num;
                                exist_num++;
                            }
                            PictureAirLog.out("downloadlist size =" + downloadList.size());
                        }
                    }
                }
                if (!isDownloading) {//如果当前不在下载
                    prepareDownload();
                    isDownloading = true;
                }
            } else {
                stopSelf();//下载服务停止
            }
        } else {
            stopSelf();//下载服务停止
        }
        return START_NOT_STICKY;//被系统kill之后，不会自动复活重新启动服务
    }

    private void prepareDownload() {
        // TODO Auto-generated method stub
        PictureAirLog.out("DownloadService ----------> preparedownload");
        PictureAirLog.out("DownloadService ----------> before notification");
        Notification notification = new NotificationCompat.Builder(mContext).
                setSmallIcon(R.drawable.pp_icon).setAutoCancel(true).setContentTitle(mContext.getString(R.string.app_name))
                .setContentText(mContext.getString(R.string.downloading)).setWhen(System.currentTimeMillis()).setTicker(mContext.getString(R.string.downloading)).build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;//通知栏可以自动删除
        notification.defaults = Notification.DEFAULT_SOUND;//默认下载完成声音
        manager.notify(0, notification);
        PictureAirLog.out("DownloadService ----------> after notification");
        new Thread(new Runnable() {
            @Override
            public void run() {
                String userId = preferences.getString(Common.USERINFO_ID, "");
                databasePhotoCount = pictureAirDbManager.getDownloadPhotoCount(userId);
                handler.sendEmptyMessage(START_DOWNLOAD);
            }
        }).start();
    }

    //下载文件成功之后的回调函数
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case START_DOWNLOAD:
                    PictureAirLog.e("service","START_DOWNLOAD");
                    if (downloadList.size() > 0){
                            for (int i = 0; i < downloadList.size(); i++) {
                                DownloadFileStatus fileStatus = downloadList.get(i);
                                if (fileStatus.status == DownloadFileStatus.DOWNLOAD_STATE_WAITING) {
                                    if (taskList.size() < 3) {
                                        fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_DOWNLOADING;
                                        fileStatus.setPosition(i);
                                        taskList.put(fileStatus.getUrl(), fileStatus);
                                        String name = fileStatus.getUrl();
                                        name = name.substring(name.length()-10,name.length());
                                        PictureAirLog.e("service","START_DOWNLOAD name and positiong " + name + ""+fileStatus.getPosition());
                                        downLoad(fileStatus);
                                    } else {
                                        break;
                                    }
                                }
                            }
                    }else{
                        PictureAirLog.out("finish download-------------->");
                        handler.sendEmptyMessage(FINISH_DOWNLOAD);
                    }

                    break;
                case FINISH_DOWNLOAD:
                    //如果下载数目一致，提示用户下载完毕，并且让service停止掉
                    PictureAirLog.out("下载完毕,共下载了" + downed_num + "张照片，失败了" + failed_num + "张");
//                    PictureAirLog.e("service","FINISH_DOWNLOAD");
                    String notificationDetail = String.format(mContext.getString(R.string.download_detail1), downed_num);
                    if (failed_num >0) {
                        notificationDetail = String.format(mContext.getString(R.string.download_detail2), downed_num, failed_num);
                    }
                    Notification notification = new NotificationCompat.Builder(mContext).
                            setSmallIcon(R.drawable.pp_icon).setAutoCancel(true).setContentTitle(mContext.getString(R.string.app_name))
                            .setContentText(notificationDetail).
                                    setWhen(System.currentTimeMillis()).setTicker(notificationDetail).build();
                    notification.flags = Notification.FLAG_AUTO_CANCEL;//通知栏可以自动删除
                    notification.defaults = Notification.DEFAULT_SOUND;//默认下载完成声音

                    stopSelf();//下载服务停止
                    manager.notify(0, notification);
                    downed_num = 0;
                    failed_num = 0;
                    isDownloading = false;
                    cacheList.clear();
                    break;

                case API1.DOWNLOAD_PHOTO_SUCCESS://下载成功之后获取data数据，然后base64解码，然后保存。
                    PictureAirLog.e("service","DOWNLOAD_PHOTO_SUCCESS");
                    Bundle bundle = msg.getData();
                    DownloadFileStatus fileStatus = (DownloadFileStatus) bundle.get("url");
                    byte[] results = (byte[]) bundle.get("binaryData");
                    File file = fileList.get(fileStatus.getUrl());
                    saveFile(file, results,fileStatus);
                    break;
                case API1.DOWNLOAD_PHOTO_FAILED:
                    PictureAirLog.e("service","DOWNLOAD_PHOTO_FAILED");
                    synchronized (taskList){
                        ++failed_num;
                        Bundle failBundle = msg.getData();
                        DownloadFileStatus failStatus = (DownloadFileStatus) failBundle.get("url");
                        taskList.remove(failStatus.getUrl());
                        if (adapterHandler != null) {
//                            adapterHandler.sendEmptyMessage(DownLoadingFragment.PHOTO_STATUS_UPDATE);
                            adapterHandler.obtainMessage(DownLoadingFragment.PHOTO_STATUS_UPDATE,failStatus).sendToTarget();
                        }else{
                            handler.sendEmptyMessage(ADD_DOWNLOAD);
                        }
                    }
                    break;
                case ADD_DOWNLOAD:
                    PictureAirLog.e("service","ADD_DOWNLOAD");
                    synchronized (taskList) {
                        if (downloadList.size() > 0) {
                            for (int i = 0; i < downloadList.size(); i++) {
                                DownloadFileStatus status = downloadList.get(i);
                                if (status.status == DownloadFileStatus.DOWNLOAD_STATE_WAITING) {
                                    if (taskList.size() < 3) {
                                        status.status = DownloadFileStatus.DOWNLOAD_STATE_DOWNLOADING;
                                        status.setPosition(i);
                                        String name = status.getUrl();
                                        name = name.substring(name.length()-10,name.length());
                                        PictureAirLog.e("service","ADD_DOWNLOAD name and positiong " + name + ""+status.getPosition());
                                        taskList.put(status.getUrl(), status);
                                        downLoad(status);
                                    } else {
                                        break;
                                    }
                                }

                                if (i == downloadList.size() -1 && taskList.size() == 0 && !mFinish){
                                    handler.sendEmptyMessage(FINISH_DOWNLOAD);
                                    mFinish = true;
                                }
                            }
                        } else {
                            //结束
//                            PictureAirLog.e("service", "ADD_DOWNLOAD download all");
                            if (taskList.size() == 0 && !mFinish) {
                                PictureAirLog.e("service", "ADD_DOWNLOAD send  FINISH_DOWNLOAD");
                                handler.sendEmptyMessage(FINISH_DOWNLOAD);
                                mFinish = true;
                            }
                        }
                    }
                    break;

                default:
                    break;
            }
        }

    };


    /**
     * 下载文件方法
     * 1.判断本地存不存在此文件，如果存在，直接结束
     * 2.判断缓存是否存在此文件，如果存在，从缓存中下载图片，并且保存到SDcard
     * 3.如果缓存不存在此文件，调用API下载图片，并且保存到SDcard
     *
     * @param fileStatus
     */
    private void downLoad(DownloadFileStatus fileStatus) {
        PictureAirLog.out("downloadurl--->" + fileStatus.getUrl());
        String fileName = AppUtil.getReallyFileName(fileStatus.getUrl(),fileStatus.isVideo());
        PictureAirLog.out("filename=" + fileName);
        File filedir = new File(Common.PHOTO_DOWNLOAD_PATH);
        filedir.mkdirs();
        File file = new File(filedir + "/" + fileName);

        if (!file.exists()) {
            // 使用友盟统计点击下载次数
            UmengUtil.onEvent(mContext, Common.EVENT_ONCLICK_DOWNLOAD);
            fileList.put(fileStatus.getUrl(), file);
            downloadImgOrVideo(file, fileStatus);
//            PictureAirLog.e("service","downLoad");
        }
    }

    /**
     * 判断下载视频还是 图片
     */
    private void downloadImgOrVideo(final File file, final DownloadFileStatus fileStatus) {//int isVideo,String photoId,String url
        if (fileStatus.isVideo() == 0) {//photo
//            PictureAirLog.e("service","downloadImgOrVideo photo");
            API1.downLoadPhotos(handler, fileStatus,adapterHandler);
        } else {//video
            String downloadURL = Common.PHOTO_URL + fileStatus.getUrl();
            RequestParams params = new RequestParams();
            params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
            params.put(Common.PHOTOIDS, fileStatus.getPhotoId());
            HttpUtil1.asyncDownloadBinaryData(downloadURL, params, new HttpCallback() {
                long startTime;
                long lastTime;
                @Override
                public void onSuccess(byte[] binaryData) {
                    super.onSuccess(binaryData);
                    PictureAirLog.v("asynDownloadFile onSuccess", "binaryData size: " + binaryData.length);
                    Message msg =  handler.obtainMessage();
                    msg.what = API1.DOWNLOAD_PHOTO_SUCCESS;
                    Bundle bundle = new Bundle();
                    fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_FINISH;
                    bundle.putParcelable("url",fileStatus);
                    bundle.putByteArray("binaryData",binaryData);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }

                @Override
                public void onFailure(int status) {
                    super.onFailure(status);
                    PictureAirLog.v("asynDownloadFile", " onFailure status :" + status);
//                  PictureAirLog.e(TAG, "调用下载照片API失败：错误代码：" + status);
//                  handler.obtainMessage(DOWNLOAD_PHOTO_FAILED, status, 0).sendToTarget();
                    Message msg =  handler.obtainMessage();
                    msg.what = API1.DOWNLOAD_PHOTO_FAILED;
                    Bundle bundle = new Bundle();
                    fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_FAILURE;
                    bundle.putParcelable("url",fileStatus);
                    bundle.putInt("status",status);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }

                @Override
                public void onProgress(long bytesWritten, long totalSize) {
                    super.onProgress(bytesWritten, totalSize);
                    float currentSize = bytesWritten/1000f/1000f;
                    float total = totalSize/1000f/1000f;
                    DecimalFormat decimalFormat=new DecimalFormat(".00");
                    String c = decimalFormat.format(currentSize);
                    if (c.indexOf(".") == 0){
                        c = 0+c;
                    }
                    String t = decimalFormat.format(total);
                    if (t.indexOf(".") == 0) {
                        t=0+t;
                    }
                    fileStatus.setCurrentSize(c);
                    fileStatus.setTotalSize(t);
                    long currentTime = System.currentTimeMillis();
                    float usedTime = (currentTime-lastTime)/1000f;
                    float keepTime = (currentTime-startTime)/1000f;
//                  PictureAirLog.e(TAG, "onProgress usedTime "+usedTime);
                    if (usedTime > 0.1) {
                        lastTime = currentTime;
                        float downSpeed = (bytesWritten / 1000f) / keepTime;
                        String ds = decimalFormat.format(downSpeed);
                        if (ds.indexOf(".") == 0) {
                            ds = 0+ds;
                        }
                        fileStatus.setLoadSpeed(ds);
                        if (adapterHandler != null) {
                            adapterHandler.sendEmptyMessage(DownLoadingFragment.PHOTO_STATUS_UPDATE);
//                          PictureAirLog.e(TAG, "onProgress sendToTarget");
                        }
                    }
                }

                @Override
                public void onStart() {
                    super.onStart();
                    startTime = System.currentTimeMillis();
                    lastTime = startTime;
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
    private void saveFile(File file, byte[] data,DownloadFileStatus fileStatus) {
        // TODO Auto-generated method stub
        BufferedOutputStream stream = null;
        try {
//            PictureAirLog.e("service","saveFile");
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
                    scan(file.toString(),fileStatus);
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
    private void scan(final String file,final DownloadFileStatus fileStatus) {
        // TODO Auto-generated method stub
        MediaScannerConnection.scanFile(this, new String[]{file}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        scan_num++;
//                            PictureAirLog.e("service","fixedThreadPool start");
                        taskList.remove(fileStatus.getUrl());
                        fileList.remove(fileStatus.getUrl());
                        downloadList.remove(fileStatus.getPosition());
//                            PictureAirLog.e("LIST_REMOVE_ITEM","downloadList.size: "+downloadList.size());
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        final String loadTime = df.format(new Date());
                        final String userId = preferences.getString(Common.USERINFO_ID, "");
//                               PictureAirLog.e("service","scan downloadList remove "+fileStatus.getPosition() );
                        for (String key:taskList.keySet()) {
                            DownloadFileStatus status = taskList.get(key);
                            String name = status.getUrl();
                            name = name.substring(name.length()-10,name.length());
                            PictureAirLog.e("service","fixedThreadPool start name and positiong " + name + ""+status.getPosition());
                            if (status.getPosition() > fileStatus.getPosition()) {
                                status.setPosition(status.getPosition() - 1);
                            }
                            PictureAirLog.e("service","fixedThreadPool end name and positiong " + name + ""+status.getPosition());
                        }

                        if (adapterHandler != null) {
                            adapterHandler.obtainMessage(DownLoadingFragment.PHOTO_REMOVE,fileStatus).sendToTarget();
                            adapterHandler.removeMessages(DownLoadingFragment.PHOTO_STATUS_UPDATE);
//                                  PictureAirLog.e("service","adapterHandler sendmessage ");
                        }else {
                            handler.sendEmptyMessageDelayed(ADD_DOWNLOAD,200);
                        }
//                            PictureAirLog.e("service","fixedThreadPool end");
                        fixedThreadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                pictureAirDbManager.writeLoadSuccessPhotos(userId,fileStatus.getPhotoId(),fileStatus.getUrl(),fileStatus.getTotalSize(),fileStatus.getPhotoThumbnail(),fileStatus.getShootOn(),loadTime);
                            }
                        });
                    }
                });
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        PictureAirLog.out("downloadService-----------> ondestroy");
        fixedThreadPool.shutdown();
        super.onDestroy();
    }

    public class PhotoBind extends Binder{
        public DownloadService getService(){
            return DownloadService.this;
        }
    }

    public Vector<DownloadFileStatus> getDownloadList(){
        return  new Vector<DownloadFileStatus>(downloadList);
    }

    public void setAdapterhandler(Handler handler){
        this.adapterHandler = handler;
    }

    public Handler getHandler(){
        return handler;
    }

    public void sendAddDownLoadMessage(){
        handler.sendEmptyMessageDelayed(ADD_DOWNLOAD,200);
    }

    public void sendFinish(){
        handler.sendEmptyMessage(FINISH_DOWNLOAD);
    }

    public int getDatabasePhotoCount(){
        return databasePhotoCount;
    }

    public int getCacheListSize(){
        return cacheList.size();
    }

}
