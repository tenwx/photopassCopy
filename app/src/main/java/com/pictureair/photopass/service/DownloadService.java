package com.pictureair.photopass.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
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
import com.pictureair.photopass.entity.PhotoDownLoadInfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.eventbus.BaseBusEvent;
import com.pictureair.photopass.eventbus.DownLoadCountUpdateEvent;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

/**
 * 下载网络图片服务类
 * 下载网络视频服务类
 */
public class DownloadService extends Service {
    private ArrayList<PhotoInfo> photos = new ArrayList<PhotoInfo>();
    private CopyOnWriteArrayList<DownloadFileStatus> downloadList = new CopyOnWriteArrayList<DownloadFileStatus>();
    private List<DownloadFileStatus> removeCache = new Vector<DownloadFileStatus>();
    private Map<String,DownloadFileStatus> taskList = new HashMap<>();
//    private Map<String,DownloadFileStatus> cacheList = new HashMap<>();
    private int toLoadCount = 0;
    private Map<String,File> fileList = new ConcurrentHashMap<>();
    private int downed_num = 0;//实际下载照片数
    private int exist_num = 0 , scan_num = 0;//无需下载的照片数,扫描成功的照片数
    private int failed_num = 0;//下载失败的照片数
    private boolean mFinish = false;
    private boolean mIsErrorsAdd = false;
    private boolean reconnect = false;
//    private boolean clear

    private Context mContext = this;
    private NotificationManager manager;
    private Notification notification;
    private final static int FINISH_DOWNLOAD = 1;
    private final static int START_DOWNLOAD = 2;
    public final static int ADD_DOWNLOAD = 3;
    public final static int CLEAR_FAILED = 4;
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
    private CountDownLatch countDownLatch;
    private boolean mSartNotificate = false;

    @Override
    public IBinder onBind(Intent arg0) {
        return photoBind;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PictureAirLog.out("downloadService ---------> onCreate" + downed_num + "_" + failed_num);
        PictureAirLog.out("onCreate downed_num："+downed_num);
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        myToast = new PWToast(getApplicationContext());
        pictureAirDbManager = new PictureAirDbManager(getApplicationContext());
        preferences = getApplicationContext().getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, Context.MODE_PRIVATE);
        fixedThreadPool = Executors.newFixedThreadPool(1);
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
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
            int reconnect = b.getInt("reconnect",-1);
            if (!AppUtil.checkPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                myToast.setTextAndShow(R.string.permission_storage_message, Common.TOAST_SHORT_TIME);
                stopSelf();//下载服务停止
            } else if (photos != null) {
                //将新的数据放入到下载队列的末尾
                synchronized (taskList) {
                    if (reconnect > -1) {
                        if (photos.size() >0 && downloadList.size() >0) {
                            int ifOne = b.getInt("one",-1);
                            for (int i=0;i<photos.size();i++) {
                                PhotoInfo info = photos.get(i);
                                for (int j=0;j<downloadList.size();j++) {
                                    DownloadFileStatus fileStatus = downloadList.get(j);
                                    if (fileStatus != null && fileStatus.getUrl().equals(photos.get(i).photoPathOrURL) && fileStatus.status == DownloadFileStatus.DOWNLOAD_STATE_RECONNECT) {
                                        fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_WAITING;
                                        if (ifOne == 0) {
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (photos.size() >0) {
                            countDownLatch = new CountDownLatch(photos.size());
                        }
                        for (int i = 0; i < photos.size(); i++) {
                            PhotoInfo photoInfo = photos.get(i);
                            final DownloadFileStatus fileStatus = new DownloadFileStatus(photoInfo.photoPathOrURL, "0", "0", "0", photoInfo.photoId, photoInfo.isVideo,photoInfo.photoThumbnail,photoInfo.shootOn,"");
                            String fileName = AppUtil.getReallyFileName(fileStatus.getUrl(),fileStatus.isVideo());
                            PictureAirLog.out("filename=" + fileName);
                            File filedir = new File(Common.PHOTO_DOWNLOAD_PATH);
                            filedir.mkdirs();
                            final File file = new File(filedir + "/" + fileName);
                            if (!file.exists()) {
                                downloadList.add(fileStatus);
                                ++toLoadCount;
                                countDownLatch.countDown();
                            }else{
                                ++downed_num;
                                PictureAirLog.out("onStartCommand downed_num："+downed_num);
                                PictureAirLog.e("onStartCommand","file exists");
                                exist_num++;
//                                ++toLoadCount;
                                fixedThreadPool.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        long fileLength = file.length()/1000/1000;
                                        String formatLength = AppUtil.formatData(fileLength);
                                        fileStatus.setTotalSize(formatLength);
                                        String userId = preferences.getString(Common.USERINFO_ID, "");
                                        String loadTime = AppUtil.getFormatCurrentTime();
                                        pictureAirDbManager.insertLoadSuccessPhotos(userId,fileStatus,loadTime,true);
                                        countDownLatch.countDown();
                                    }
                                });
                            }
                            PictureAirLog.out("downloadlist size =" + downloadList.size());
                            PictureAirLog.out("onStartCommand downed_num："+downed_num);
                        }
                    }
                }

                if (!isDownloading) {//如果当前不在下载
                    prepareDownload();
                    isDownloading = true;
                }else{
                    if (adapterHandler != null) {
                        PictureAirLog.e("onStartCommand","isDownloading = true");
                        adapterHandler.sendEmptyMessage(DownLoadingFragment.SERVICE_LOAD_SUCCESS);
                    }
                }
            } else {
                stopSelf();//下载服务停止
            }
        } else {
            stopSelf();//下载服务停止
        }
        return START_NOT_STICKY;//被系统kill之后，不会自动复活重新启动服务
    }

    private void startNotification(){
        Notification notification = new NotificationCompat.Builder(mContext).
                setSmallIcon(R.drawable.pp_icon).setAutoCancel(true).setContentTitle(mContext.getString(R.string.app_name))
                .setContentText(mContext.getString(R.string.downloading)).setWhen(System.currentTimeMillis()).setTicker(mContext.getString(R.string.downloading)).build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;//通知栏可以自动删除
        manager.notify(0, notification);
    }

    private void prepareDownload() {
        // TODO Auto-generated method stub
        PictureAirLog.out("DownloadService ----------> preparedownload");
        fixedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (countDownLatch != null){
                        countDownLatch.await();
                    }
                    String userId = preferences.getString(Common.USERINFO_ID, "");

                    PictureAirLog.out("prepareDownload>>>>>>>>>>>>>read database");
                    databasePhotoCount = pictureAirDbManager.getDownloadPhotoCount(userId,true);
                    databasePhotoCount -= downed_num;
                    PictureAirLog.out("prepareDownload>>>>>>>>>>databasePhotoCount:" +databasePhotoCount);
                    if (!mIsErrorsAdd){
                        mIsErrorsAdd = true;
                        List<PhotoDownLoadInfo> infos = pictureAirDbManager.getPhotos(userId,false);
                        if (infos != null && infos.size() >0) {

                            for (int i = 0;i<infos.size();i++) {
                                PhotoDownLoadInfo info = infos.get(i);
                                DownloadFileStatus fileStatus = new DownloadFileStatus(info.getUrl(), "0", "0", "0", info.getPhotoId(), info.getIsVideo(),info.getPreviewUrl(),info.getShootTime(),info.getFailedTime());
                                fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_FAILURE;
                                downloadList.add(fileStatus);
                                ++toLoadCount;

                            }
                        }
                    }
                    if (adapterHandler != null) {
                        adapterHandler.sendEmptyMessage(DownLoadingFragment.SERVICE_LOAD_SUCCESS);
                    }else {
                        handler.sendEmptyMessage(START_DOWNLOAD);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //下载文件成功之后的回调函数
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case START_DOWNLOAD:
                    PictureAirLog.out("downloadService----------->START_DOWNLOAD");
                    PictureAirLog.e("handleMessage","START_DOWNLOAD");
                    if (downloadList.size() > 0){
                        for (int i = 0; i < downloadList.size(); i++) {
                            DownloadFileStatus fileStatus = downloadList.get(i);
                            if (fileStatus.status == DownloadFileStatus.DOWNLOAD_STATE_WAITING) {
                                if (!mSartNotificate){
                                    mSartNotificate = true;
                                    startNotification();
                                }
                                if (taskList.size() < 3) {
                                    fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_DOWNLOADING;
                                    fileStatus.setPosition(i);
                                    taskList.put(fileStatus.getUrl(), fileStatus);
                                    PictureAirLog.out("START_DOWNLOAD photoid and positiong " + fileStatus.getPhotoId() +" "+fileStatus.getPosition());
                                    downLoad(fileStatus);
                                } else {
                                    break;
                                }
                            }
                            if (i == downloadList.size() -1 && taskList.size() == 0 && !mFinish){
                                mFinish = true;
                                stopSelf();//下载服务停止
                                downed_num = 0;
                                failed_num = 0;
                                isDownloading = false;
                                PictureAirLog.out("START_DOWNLOAD downed_num："+downed_num);
                            }
                        }
                    }else{
                        PictureAirLog.out("finish download-------------->");
                        mFinish = true;
                        stopSelf();//下载服务停止
                        downed_num = 0;
                        failed_num = 0;
                        isDownloading = false;
                    }

                    break;
                case FINISH_DOWNLOAD://下载结束
                    failed_num = downloadList.size();
                    //如果下载数目一致，提示用户下载完毕，并且让service停止掉
                    PictureAirLog.out("下载完毕,共下载了" + downed_num + "张照片，失败了" + failed_num + "张");
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
                    PictureAirLog.out("FINISH_DOWNLOAD downed_num："+downed_num);
                    if (adapterHandler != null) {
                        adapterHandler.sendEmptyMessage(DownLoadingFragment.DOWNLOAD_FINISH);
                    }
                    break;

                case API1.DOWNLOAD_PHOTO_SUCCESS://下载成功之后获取data数据，然后base64解码，然后保存。
                    PictureAirLog.out("downloadService----------->DOWNLOAD_PHOTO_SUCCESS");
                    Bundle bundle = msg.getData();
                    DownloadFileStatus fileStatus = (DownloadFileStatus) bundle.get("url");
                    byte[] results = (byte[]) bundle.get("binaryData");
                    File file = fileList.get(fileStatus.getUrl());
                    saveFile(file, results,fileStatus);
                    break;
                case API1.DOWNLOAD_PHOTO_FAILED://下载失败
                    PictureAirLog.out("downloadService----------->DOWNLOAD_PHOTO_FAILED");
                    synchronized (taskList) {
                        Bundle failBundle = msg.getData();
                        final DownloadFileStatus failStatus = (DownloadFileStatus) failBundle.get("url");
                        taskList.remove(failStatus.getUrl());
                        if (adapterHandler != null) {
                            PictureAirLog.e("handleMessage","DOWNLOAD_PHOTO_FAILED");
                            adapterHandler.obtainMessage(DownLoadingFragment.PHOTO_STATUS_UPDATE, failStatus).sendToTarget();
                        } else {
                            handler.sendEmptyMessage(ADD_DOWNLOAD);
                        }
                        final String userId = preferences.getString(Common.USERINFO_ID, "");
                        fixedThreadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                pictureAirDbManager.updateOrInsertFailedPhotos(userId, failStatus);
                            }
                        });
                    }
                    break;
                case ADD_DOWNLOAD://添加下载任务
                    PictureAirLog.out("downloadService----------->ADD_DOWNLOAD");
                    synchronized (taskList) {
                        if (downloadList.size() > 0) {
                            for (int i = 0; i < downloadList.size(); i++) {
                                DownloadFileStatus status = downloadList.get(i);
                                if (status.status == DownloadFileStatus.DOWNLOAD_STATE_WAITING) {
                                    if (taskList.size() < 3) {
                                        status.status = DownloadFileStatus.DOWNLOAD_STATE_DOWNLOADING;
                                        status.setPosition(i);
                                        PictureAirLog.out("ADD_DOWNLOAD photoid and positiong " + status.getPhotoId() +" "+status.getPosition());
                                        taskList.put(status.getUrl(), status);
                                        downLoad(status);
                                        if (adapterHandler != null) {
                                            PictureAirLog.e("handleMessage","ADD_DOWNLOAD");
                                            adapterHandler.sendEmptyMessage(DownLoadingFragment.PHOTO_STATUS_UPDATE);
                                        }
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
                            if (taskList.size() == 0 && !mFinish) {
                                PictureAirLog.out("ADD_DOWNLOAD send  FINISH_DOWNLOAD");
                                handler.sendEmptyMessage(FINISH_DOWNLOAD);
                                mFinish = true;
                            }
                        }
                    }
                    break;
                case CLEAR_FAILED://清除下载失败文件
                    PictureAirLog.out("downloadService----------->CLEAR_FAILED");
                    if (downloadList.size() > 0){
                        Iterator<DownloadFileStatus> iterator = downloadList.iterator();
                        while (iterator.hasNext()) {
                            DownloadFileStatus deleteStatus = iterator.next();
                            if (deleteStatus.status == DownloadFileStatus.DOWNLOAD_STATE_FAILURE) {
                                downloadList.remove(deleteStatus);
                                --toLoadCount;
                            }
                        }
                        fixedThreadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                String userId = preferences.getString(Common.USERINFO_ID, "");
                                pictureAirDbManager.deleteDownloadFailPhoto(userId);
                                if (adapterHandler != null) {
                                    adapterHandler.sendEmptyMessage(DownLoadingFragment.REMOVE_FAILED_PHOTOS);
                                }
                            }
                        });
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
    private void downLoad(final DownloadFileStatus fileStatus) {
        PictureAirLog.out("downloadurl--->" + fileStatus.getUrl());
        String fileName = AppUtil.getReallyFileName(fileStatus.getUrl(),fileStatus.isVideo());
        PictureAirLog.out("filename=" + fileName);
        File filedir = new File(Common.PHOTO_DOWNLOAD_PATH);
        filedir.mkdirs();
        File file = new File(filedir + "/" + fileName);

        // 使用友盟统计点击下载次数
        UmengUtil.onEvent(mContext, Common.EVENT_ONCLICK_DOWNLOAD);
        fileList.put(fileStatus.getUrl(), file);
        downloadImgOrVideo(file, fileStatus);
    }

    /**
     * 判断下载视频还是 图片
     */
    private void downloadImgOrVideo(final File file, final DownloadFileStatus fileStatus) {//int isVideo,String photoId,String url
        if (fileStatus.isVideo() == 0) {//photo
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
                    PictureAirLog.e("downloadImgOrVideo", "调用下载照片API成功");
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
                    PictureAirLog.e("downloadImgOrVideo", "调用下载照片API失败：错误代码：" + status);
                    Message msg =  handler.obtainMessage();
                    msg.what = API1.DOWNLOAD_PHOTO_FAILED;
                    Bundle bundle = new Bundle();
                    fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_FAILURE;
                    if (TextUtils.isEmpty(fileStatus.getFailedTime())) {
                        String failedTime = AppUtil.getFormatCurrentTime();
                        fileStatus.setFailedTime(failedTime);
                    }
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
                    if (usedTime > 0.2) {
                        lastTime = currentTime;
                        float downSpeed = (bytesWritten / 1000f) / keepTime;
                        String ds = decimalFormat.format(downSpeed);
                        if (ds.indexOf(".") == 0) {
                            ds = 0+ds;
                        }
                        fileStatus.setLoadSpeed(ds);
                        if (adapterHandler != null) {
                            adapterHandler.sendEmptyMessage(DownLoadingFragment.PHOTO_STATUS_UPDATE);
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
            PictureAirLog.out("downloadService-----------> saveFile");
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
                    PictureAirLog.out("saveFile()>>>>>>>>>> downed_num："+downed_num);
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
                        PictureAirLog.out("downloadService-----------> scan");
                        scan_num++;
                        taskList.remove(fileStatus.getUrl());
                        fileList.remove(fileStatus.getUrl());
                        downloadList.remove(fileStatus.getPosition());
                        final String loadTime = AppUtil.getFormatCurrentTime();
                        final String userId = preferences.getString(Common.USERINFO_ID, "");
                        for (String key:taskList.keySet()) {
                            DownloadFileStatus status = taskList.get(key);
                            if (status.getPosition() > fileStatus.getPosition()) {
                                status.setPosition(status.getPosition() - 1);
                            }
                        }

                        if (adapterHandler != null) {
                            adapterHandler.obtainMessage(DownLoadingFragment.PHOTO_REMOVE,fileStatus).sendToTarget();
                            PictureAirLog.e("scan","scan");
                            adapterHandler.removeMessages(DownLoadingFragment.PHOTO_STATUS_UPDATE);
                        }else {
                            handler.sendEmptyMessageDelayed(ADD_DOWNLOAD,200);
                        }
                        fixedThreadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                if (TextUtils.isEmpty(fileStatus.getFailedTime())) {
                                    PictureAirLog.out("scanfile() >>>>>>> insertLoadSuccessPhotos");
                                    pictureAirDbManager.insertLoadSuccessPhotos(userId, fileStatus, loadTime, true);
                                } else{
                                    PictureAirLog.out("scanfile() >>>>>>> updateFailedPhotos");
                                    pictureAirDbManager.updateFailedPhotos(userId,fileStatus,loadTime);
                                }
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
        downloadList.clear();
        toLoadCount = 0;
        mSartNotificate = false;
        if (EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        PictureAirLog.out("downloadService-----------> onUnbind");
        return super.onUnbind(intent);
    }

    public class PhotoBind extends Binder{
        public DownloadService getService(){
            return DownloadService.this;
        }
    }

    public CopyOnWriteArrayList<DownloadFileStatus> getDownloadList(){
        return  new CopyOnWriteArrayList<DownloadFileStatus>(downloadList);
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


    public int getDatabasePhotoCount(){
        return databasePhotoCount;
    }


    public void startDownload(){
        handler.sendEmptyMessage(START_DOWNLOAD);
    }

    public void sendClearFailedMsg(){
        handler.sendEmptyMessage(CLEAR_FAILED);
    }

    public boolean isDownloading (){
        return isDownloading;
    }

    @Subscribe
    public void onUserEvent(BaseBusEvent baseBusEvent) {
        if (baseBusEvent instanceof DownLoadCountUpdateEvent) {
            DownLoadCountUpdateEvent event = (DownLoadCountUpdateEvent)baseBusEvent;
            databasePhotoCount = event.getUpdateCount();
            downed_num = 0;
        }
    }

    public int getDowned_num(){
        return downed_num;
    }

}
