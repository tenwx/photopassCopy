package com.pictureair.photopass.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.DownloadPhotoPreviewActivity;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.DownloadFileStatus;
import com.pictureair.photopass.entity.PhotoDownLoadInfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.fragment.DownLoadingFragment;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PWMediaScanner;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.util.UmengUtil;
import com.pictureair.photopass.widget.PWToast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 下载网络图片服务类
 * 下载网络视频服务类
 *  7.0适配 Notification 显示不正确
 */
public class DownloadService extends Service {
    private ArrayList<PhotoInfo> photos = new ArrayList<PhotoInfo>();
    private CopyOnWriteArrayList<DownloadFileStatus> downloadList = new CopyOnWriteArrayList<DownloadFileStatus>();
    private ConcurrentHashMap<String,DownloadFileStatus> taskList = new ConcurrentHashMap<>();
    private AtomicInteger downed_num = new AtomicInteger(0);//实际下载照片数
    private AtomicInteger failed_num = new AtomicInteger(0);//下载失败的照片数
    private CopyOnWriteArrayList<DownloadFileStatus> tempList = new CopyOnWriteArrayList<>();
    private boolean mFinish = false;
    private boolean mIsErrorsAdd = false;

    private Context mContext = this;
    private NotificationManager manager;
    private Notification notification;
    private final static int FINISH_DOWNLOAD = 1;
    private final static int START_DOWNLOAD = 2;
    public final static int ADD_DOWNLOAD = 3;
    public final static int CLEAR_FAILED = 4;
    public final static int PREPARE_DOWNLOAD = 5;
    public final static int NO_PERMISSION = 6;

    private boolean isDownloading = false;
    private PWToast myToast;
    private PhotoBind photoBind = new PhotoBind();
    private Handler adapterHandler;
    private PictureAirDbManager pictureAirDbManager;
    private ExecutorService fixedThreadPool;
    private CountDownLatch countDownLatch;
    private boolean hasPhotos = false;
    private String lastUrl = new String();
    private String userId;
//    private ConcurrentHashMap<String,DownloadFileStatus> cacheList = new ConcurrentHashMap<>();
    private CopyOnWriteArrayList<DownloadFileStatus> deleteList = new CopyOnWriteArrayList<>();
    private int prepareDownloadCount;
    private AtomicInteger processCount = new AtomicInteger(0);
    private boolean logout;

    @Override
    public IBinder onBind(Intent arg0) {
        return photoBind;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PictureAirLog.out("downloadService ---------> onCreate" + downed_num.get() + "_" + failed_num.get());
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        myToast = new PWToast(getApplicationContext());
        pictureAirDbManager = new PictureAirDbManager(getApplicationContext());
        fixedThreadPool = Executors.newFixedThreadPool(1);
        userId = SPUtils.getString(getApplicationContext(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ID, "");
        fixedThreadPool.execute(new ChangeLoadToFailTask());
    }

    @SuppressWarnings("deprecation")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PictureAirLog.out("DownloadService ----------> onStartCommand");
        mFinish = false;
        if (intent != null) {
            fixedThreadPool.execute(new AddDownloadTask(intent));
        }else{
            forceFinish();
            stopSelf();
        }
        return START_NOT_STICKY;//被系统kill之后，不会自动复活重新启动服务
    }


    private synchronized void addTask(Intent intent){
        Bundle b = intent.getExtras();
        if (b != null) {
            photos = b.getParcelableArrayList("photos");
            prepareDownloadCount = b.getInt("prepareDownloadCount",0);
            PictureAirLog.out("addTask start prepareDownloadCount size =" + prepareDownloadCount);
            int reconnect = b.getInt("reconnect",-1);
            logout = b.getBoolean("logout",false);
            if (logout) {
                //如果在下载，先取消下载项，然后组织handler的传递，来关闭下载
                API1.cancelAllRequest();
                stopSelf();
                return;
            }else if (!AppUtil.checkPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                handler.sendEmptyMessage(NO_PERMISSION);
                return;
            } else if (photos != null) {
                //将新的数据放入到下载队列的末尾
                if (reconnect > -1) {//需要重连的走这个流程
                    if (photos.size() >0 && downloadList.size() >0) {
                        for (int i=0;i<photos.size();i++) {
                            PhotoInfo info = photos.get(i);
                            for (int j=0;j<downloadList.size();j++) {
                                DownloadFileStatus fileStatus = downloadList.get(j);
                                if (fileStatus != null && fileStatus.getPhotoId().equals(photos.get(i).photoId) && fileStatus.status == DownloadFileStatus.DOWNLOAD_STATE_SELECT) {
                                    fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_WAITING;
                                    processCount.incrementAndGet();
                                }else if(fileStatus != null && fileStatus.getPhotoId().equals(photos.get(i).photoId) && fileStatus.status == DownloadFileStatus.DOWNLOAD_STATE_RECONNECT){
                                    fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_WAITING;
                                    processCount.incrementAndGet();
                                }
                            }
                        }
                        int count = failed_num.get() - photos.size();
                        if (count <0) {
                            failed_num.set(0);
                        }else{
                            failed_num.set(count);
                        }
                        if (downloadList.size() > 0){
                            hasPhotos = true;
                        }
                    }
                } else {//正常下载走这个
                    CopyOnWriteArrayList<PhotoDownLoadInfo> infos = pictureAirDbManager.getExistPhoto(userId);
                    if (photos.size() >0) {
                        for (int i = 0; i < photos.size(); i++) {
                            PhotoInfo photoInfo = photos.get(i);
                            DownloadFileStatus fileStatus = null;
                            if (photoInfo.isVideo == 0) {
                                fileStatus = new DownloadFileStatus(photoInfo.photoThumbnail_1024, photoInfo.photoThumbnail_512, photoInfo.photoThumbnail_1024,
                                        photoInfo.photoPathOrURL, "0", "0", "0", photoInfo.photoId, photoInfo.isVideo, photoInfo.photoThumbnail, photoInfo.shootOn, "",
                                        photoInfo.videoWidth, photoInfo.videoHeight);
                            }else{
                                fileStatus = new DownloadFileStatus(photoInfo.photoPathOrURL, photoInfo.photoThumbnail_512, photoInfo.photoThumbnail_1024,
                                        photoInfo.photoPathOrURL, "0", "0", "0", photoInfo.photoId, photoInfo.isVideo, photoInfo.photoThumbnail, photoInfo.shootOn, "",
                                        photoInfo.videoWidth, photoInfo.videoHeight);
                            }
                            boolean existPhoto = false;
                            for (int j = 0; j < infos.size(); j++) {

                                PhotoDownLoadInfo info = infos.get(j);
                                if (info.getPhotoId().equalsIgnoreCase(fileStatus.getPhotoId())) {
                                    String fileName = "";
                                    File filedir = new File(Common.PHOTO_DOWNLOAD_PATH);
                                    filedir.mkdirs();
                                    if (TextUtils.isEmpty(info.getFailedTime())) {//过长的文件名的处理
                                        fileName = filedir + "/" + AppUtil.getReallyFileName(fileStatus.getUrl(), fileStatus.isVideo());
                                    } else {
                                        fileName = info.getFailedTime();
                                    }
                                    PictureAirLog.out("filename=" + fileName);

                                    File file = new File(fileName);
                                    if ("true".equalsIgnoreCase(info.getStatus())) {
                                        if (!file.exists()) {
                                            addToDownloadList(fileStatus);
                                            deleteList.add(fileStatus);
                                            processCount.incrementAndGet();
                                            tempList.add(fileStatus);
                                        } else {
                                            processCount.incrementAndGet();
                                        }
                                    } else {
                                        processCount.incrementAndGet();
                                    }
                                    existPhoto = true;
                                    break;
                                }
                            }
                            if (!existPhoto) {
                                addToDownloadList(fileStatus);
                                processCount.incrementAndGet();
                                tempList.add(fileStatus);
                            } else {
                                existPhoto = false;
                            }
                        }
                        if (downloadList.size() > 0){
                            hasPhotos = true;
                        }

                        if (deleteList.size() >0){
                            pictureAirDbManager.deletePhotos(userId,deleteList);
                            deleteList.clear();
                        }

                        if (tempList.size() >0){
                            pictureAirDbManager.insertPhotos(userId,tempList,"","load");
                            tempList.clear();
                        }

                    }
                }
                PictureAirLog.out("addTask processCount size =" + processCount.get());
                if (processCount.get() == prepareDownloadCount){
                    handler.sendEmptyMessage(PREPARE_DOWNLOAD);
                }

            } else {
                forceFinish();
                stopSelf();//下载服务停止
            }
        } else {
            forceFinish();
            stopSelf();//下载服务停止
        }
    }

    private void addToDownloadList(DownloadFileStatus fileStatus){
        downloadList.add(fileStatus);
    }

    private void startNotification(){
        Notification notification = new NotificationCompat.Builder(mContext).
                setSmallIcon(AppUtil.getNotificationIcon()).setAutoCancel(true).setContentTitle(mContext.getString(R.string.app_name))
                .setContentText(mContext.getString(R.string.downloading)).setWhen(System.currentTimeMillis()).setTicker(mContext.getString(R.string.downloading)).build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;//通知栏可以自动删除
        manager.notify(0, notification);
    }

    private void cancelNotification(){
        if (manager != null) {
            manager.cancel(0);
        }
    }

    private void prepareDownload() {
        // TODO Auto-generated method stub
        PictureAirLog.out("DownloadService ----------> preparedownload");
        if (hasPhotos) {
            hasPhotos = false;
            startNotification();
        }
        if (!mIsErrorsAdd) {
            mIsErrorsAdd = true;
            List<PhotoDownLoadInfo> infos = pictureAirDbManager.getPhotos(userId, "false", "upload");
            if (infos != null && infos.size() > 0) {

                for (int i = 0; i < infos.size(); i++) {
                    PhotoDownLoadInfo info = infos.get(i);
                    DownloadFileStatus fileStatus = new DownloadFileStatus(info.getUrl(), "","","","0", "0", "0",
                            info.getPhotoId(), info.getIsVideo(), info.getPreviewUrl(), info.getShootTime(), "", info.getVideoWidth(),info.getVideoHeight());
                    if ("false".equals(info.getStatus())) {
                        fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_FAILURE;
                    } else {
                        fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_UPLOADING;
                    }
                    addToDownloadList(fileStatus);
                }
            }
        }
        if (adapterHandler != null) {
            adapterHandler.sendEmptyMessage(DownLoadingFragment.SERVICE_LOAD_SUCCESS);
        } else {
            handler.sendEmptyMessage(START_DOWNLOAD);
        }
    }

    //下载文件成功之后的回调函数
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {

                case PREPARE_DOWNLOAD://准备下载
                    if (fixedThreadPool != null && !fixedThreadPool.isShutdown() ) {
                        fixedThreadPool.execute(new PrepareDownloadTask());
                    }
                    break;
                case START_DOWNLOAD://开始下载
                    PictureAirLog.out("downloadService----------->START_DOWNLOAD");
                    if (downloadList.size() > 0){
                        for (int i = 0; i < downloadList.size(); i++) {
                            DownloadFileStatus fileStatus = downloadList.get(i);
                            if (fileStatus.status == DownloadFileStatus.DOWNLOAD_STATE_WAITING) {
                                if (taskList.size() < 3) {
                                    fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_DOWNLOADING;
                                    fileStatus.setPosition(i);
                                    taskList.put(fileStatus.getPhotoId(), fileStatus);
                                    PictureAirLog.out("START_DOWNLOAD photoid and position " + fileStatus.getPhotoId() +" "+fileStatus.getPosition());
                                    if (fileExists(fileStatus)) {
                                        Message fileExistMsg =  handler.obtainMessage();
                                        fileExistMsg.what = API1.DOWNLOAD_PHOTO_SUCCESS;
                                        Bundle bundle = new Bundle();
                                        fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_FINISH;
                                        bundle.putParcelable("url",fileStatus);
                                        bundle.putByteArray("binaryData",null);
                                        fileExistMsg.setData(bundle);
                                        handler.sendMessage(fileExistMsg);
                                    } else {
                                        getNewUrl(fileStatus);
                                    }
                                } else {
                                    break;
                                }
                            }
                            if (i == downloadList.size() -1 && taskList.size() == 0 ){
                                downed_num.set(0);
                                failed_num.set(0);
                                isDownloading = false;
                                PictureAirLog.out("START_DOWNLOAD downed_num："+downed_num.get());
                            }
                        }
                    }else{//从数据库中查找出所有下载失败的照片无法直接下载，会从这里结束
                        PictureAirLog.out("START_DOWNLOAD finish download-------------->");
                        downed_num.set(0);
                        failed_num.set(0);
                        isDownloading = false;
                    }

                    break;
                case FINISH_DOWNLOAD://下载结束
                    //如果下载数目一致，提示用户下载完毕，并且让service停止掉
                    PictureAirLog.out("下载完毕,共下载了" + downed_num.get() + "个文件，失败了" + failed_num.get() + "个");
                    String notificationDetail = String.format(mContext.getString(R.string.download_detail1), downed_num.get());
                    if (failed_num.get() >0) {
                        notificationDetail = String.format(mContext.getString(R.string.download_detail2), downed_num.get(), failed_num.get());
                    }
                    Intent intent = new Intent(mContext, DownloadPhotoPreviewActivity.class);
                    Bundle bundle1 = new Bundle();
                    bundle1.putInt("position",-1);
                    bundle1.putString("path",lastUrl);
                    intent.putExtras(bundle1);
                    PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    Notification notification = new NotificationCompat.Builder(mContext).
                            setSmallIcon(AppUtil.getNotificationIcon()).setAutoCancel(true).setContentTitle(mContext.getString(R.string.app_name))
                            .setContentText(notificationDetail).setContentIntent(pendingIntent).
                                    setWhen(System.currentTimeMillis()).setTicker(notificationDetail).build();
                    notification.flags = Notification.FLAG_AUTO_CANCEL;//通知栏可以自动删除
                    notification.defaults = Notification.DEFAULT_SOUND;//默认下载完成声音

                    stopSelf();//下载服务停止
                    manager.notify(0, notification);
                    downed_num.set(0);
                    failed_num.set(0);
                    isDownloading = false;
                    if (adapterHandler != null) {
                        adapterHandler.sendEmptyMessage(DownLoadingFragment.DOWNLOAD_FINISH);
                    }
                    break;

                case API1.DOWNLOAD_PHOTO_SUCCESS://下载成功之后获取data数据，然后base64解码，然后保存。
                    PictureAirLog.out("downloadService----------->DOWNLOAD_PHOTO_SUCCESS");
                    if (logout) {
                        shutThreadAndStopService();
                        break;
                    }
                    if (adapterHandler != null) {
                        adapterHandler.sendEmptyMessage(DownLoadingFragment.PHOTO_STATUS_UPDATE);
                    }
                    Bundle bundle = msg.getData();
                    DownloadFileStatus fileStatus = (DownloadFileStatus) bundle.get("url");
                    byte[] results = (byte[]) bundle.get("binaryData");
                    File file = getSaveFile(fileStatus);
                    saveFile(file, results,fileStatus);
                    break;
                case API1.DOWNLOAD_PHOTO_FAILED://下载失败
                    if (logout) {
                        shutThreadAndStopService();
                        break;
                    }
                    Bundle failBundle = msg.getData();
                    final DownloadFileStatus failStatus = (DownloadFileStatus) failBundle.get("url");
                    final int errCode = (int) failBundle.get("status");
                    if (fixedThreadPool != null && !fixedThreadPool.isShutdown()) {
                        fixedThreadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    PictureAirLog.out("downloadService----------->DOWNLOAD_PHOTO_FAILED");
                                    failed_num.getAndIncrement();
                                    if (failStatus.select == 1){
                                        failStatus.select =0;
                                    }
                                    taskList.remove(failStatus.getPhotoId());
                                    if (failStatus.isVideo() == 0 && failStatus.status == DownloadFileStatus.DOWNLOAD_STATE_UPLOADING) {
                                        PictureAirLog.out("downloadService----------->DOWNLOAD_PHOTO_FAILED errCode == 404 or url empty");
                                        pictureAirDbManager.updateLoadPhotos(userId, "upload", "", "", failStatus.getPhotoId(), "");
                                    }else {
                                        pictureAirDbManager.updateLoadPhotos(userId, "false", "", "", failStatus.getPhotoId(), "");
                                    }
                                    if (adapterHandler != null) {
                                        adapterHandler.obtainMessage(DownLoadingFragment.PHOTO_STATUS_UPDATE, failStatus).sendToTarget();
                                    } else {
                                        handler.sendEmptyMessage(ADD_DOWNLOAD);
                                    }
                                }catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    break;
                case ADD_DOWNLOAD://添加下载任务
                    PictureAirLog.out("downloadService----------->ADD_DOWNLOAD");
                    if (logout) {
                        shutThreadAndStopService();
                        break;
                    }
                    if (downloadList.size() > 0) {
                        for (int i = 0; i < downloadList.size(); i++) {
                            DownloadFileStatus status = downloadList.get(i);
                            if (status.status == DownloadFileStatus.DOWNLOAD_STATE_WAITING) {
                                if (taskList.size() < 3) {
                                    status.status = DownloadFileStatus.DOWNLOAD_STATE_DOWNLOADING;
                                    status.setPosition(i);
                                    PictureAirLog.out("ADD_DOWNLOAD photoid and position " + status.getPhotoId() +" "+status.getPosition());
                                    taskList.put(status.getPhotoId(), status);
                                    if (fileExists(status)) {
                                        Message fileExistMsg2 =  handler.obtainMessage();
                                        fileExistMsg2.what = API1.DOWNLOAD_PHOTO_SUCCESS;
                                        Bundle bundle2 = new Bundle();
                                        status.status = DownloadFileStatus.DOWNLOAD_STATE_FINISH;
                                        bundle2.putParcelable("url",status);
                                        bundle2.putByteArray("binaryData",null);
                                        fileExistMsg2.setData(bundle2);
                                        handler.sendMessage(fileExistMsg2);
                                    } else {
                                        getNewUrl(status);
                                    }
                                    if (adapterHandler != null) {
                                        adapterHandler.sendEmptyMessage(DownLoadingFragment.PHOTO_STATUS_UPDATE);
                                    }
                                } else {
                                    break;
                                }
                            }
                            if (i == downloadList.size() -1 && taskList.size() == 0 && !mFinish){//当下载队列中都是下载错误的数据时，控制结束下载
                                handler.sendEmptyMessage(FINISH_DOWNLOAD);
                                mFinish = true;
                            }
                        }
                    } else {
                        //结束
                        if (taskList.size() == 0 && !mFinish) {
                            PictureAirLog.out("ADD_DOWNLOAD finish download-------------->");
                            handler.sendEmptyMessage(FINISH_DOWNLOAD);
                            mFinish = true;
                        }
                    }
                    break;
                case CLEAR_FAILED://清除下载失败文件
                    if (downloadList.size() > 0){
                        Iterator<DownloadFileStatus> iterator = downloadList.iterator();
                        while (iterator.hasNext()) {
                            DownloadFileStatus deleteStatus = iterator.next();
                            if (deleteStatus.status == DownloadFileStatus.DOWNLOAD_STATE_SELECT) {
                                downloadList.remove(deleteStatus);
                            }
                        }
                        failed_num.set(0);
                        if (fixedThreadPool != null && !fixedThreadPool.isShutdown() ) {
                            fixedThreadPool.execute(new Runnable() {
                                @Override
                                public void run() {
                                    pictureAirDbManager.deleteDownloadFailPhotoByUserId(userId);
                                    if (adapterHandler != null) {
                                        adapterHandler.sendEmptyMessage(DownLoadingFragment.REMOVE_FAILED_PHOTOS);
                                    }
                                }
                            });
                        }
                    }
                    break;

                case API1.DOWNLOAD_PHOTO_GET_URL_SUCCESS://更新url
                    if (logout) {
                        shutThreadAndStopService();
                        break;
                    }
                    PictureAirLog.out("downloadService-----------> GET_URL_SUCCESS");
                    DownloadFileStatus newUrlStatus = (DownloadFileStatus)msg.obj;
                    downLoad(newUrlStatus);
                    if (adapterHandler != null) {
                        adapterHandler.sendEmptyMessage(DownLoadingFragment.PHOTO_STATUS_UPDATE);
                    }
                    break;
                case NO_PERMISSION:
                    myToast.setTextAndShow(R.string.permission_storage_message, Common.TOAST_SHORT_TIME);
                    stopSelf();//下载服务停止
                    break;
                default:
                    break;
            }
        }

    };

    private void shutThreadAndStopService() {
        cancelNotification();
        if (fixedThreadPool != null && !fixedThreadPool.isShutdown()) {
            PictureAirLog.out("DownloadService -----------> logout");
            fixedThreadPool.shutdownNow();
        }
        stopSelf();
    }

    private File getSaveFile(DownloadFileStatus fileStatus){
        String fileName = AppUtil.getReallyFileName(fileStatus.getUrl(),fileStatus.isVideo());
        PictureAirLog.out("filename=" + fileName);
        File filedir = new File(Common.PHOTO_DOWNLOAD_PATH);
        filedir.mkdirs();
        File file = new File(filedir + "/" + fileName);
        return file;
    }

    private boolean fileExists(DownloadFileStatus fileStatus) {
        String fileName = AppUtil.getReallyFileName(fileStatus.getUrl(),fileStatus.isVideo());
        File filedir = new File(Common.PHOTO_DOWNLOAD_PATH);
        File file = new File(filedir + "/" + fileName);
        if (file.exists()) {
            String totalSize = AppUtil.formatData(file.length() / 1000d / 1000d);
            fileStatus.setTotalSize(totalSize);
            return true;
        }
        return false;
    }

    private void getNewUrl(DownloadFileStatus fileStatus){
        if (AppUtil.isOldVersionOfTheVideo(fileStatus.getOriginalUrl(), fileStatus.getPhotoThumbnail_1024(), fileStatus.getPhotoThumbnail_512(), fileStatus.getPhotoThumbnail())){
            PictureAirLog.out("getNewUrl----------->  requestNewUrl");
            API1.getPhotosInfo(MyApplication.getTokenId(), handler, false, fileStatus);
        }else{
            PictureAirLog.out("getNewUrl----------->  original Url");
            API1.getPhotosInfo(MyApplication.getTokenId(), handler, true, fileStatus);
        }
    }

    /**
     * 下载文件方法
     * 1.判断本地存不存在此文件，如果存在，直接结束
     * 2.判断缓存是否存在此文件，如果存在，从缓存中下载图片，并且保存到SDcard
     * 3.如果缓存不存在此文件，调用API下载图片，并且保存到SDcard
     *
     * @param fileStatus
     */
    private void downLoad(final DownloadFileStatus fileStatus) {
        // 使用友盟统计点击下载次数
        UmengUtil.onEvent(mContext, Common.EVENT_ONCLICK_DOWNLOAD);
//            API1.downLoadPhotos(handler, fileStatus,adapterHandler);
        API1.downLoadPhotosWithUrl(handler, fileStatus,adapterHandler);
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
            if (!file.exists()) {
                PictureAirLog.out("downloadService-----------> saveFile");
                file.createNewFile();
                FileOutputStream fsStream = new FileOutputStream(file);
                stream = new BufferedOutputStream(fsStream);
                stream.write(data);
                fileStatus.setFailedTime("");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            if (e.getMessage().contains("ENAMETOOLONG")){//如果是文件过长错误，需要重新保存
                try {
                    String path = file.getAbsolutePath();
                    path = path.substring(Common.PHOTO_DOWNLOAD_PATH.length(),path.length());
                    file = new File(Common.PHOTO_DOWNLOAD_PATH+"/"+AppUtil.md5(path)+".jpg");
                    PictureAirLog.e("saveFile ENAMETOOLONG",file.getAbsolutePath());
                    file.createNewFile();
                    FileOutputStream fsStream = new FileOutputStream(file);
                    stream = new BufferedOutputStream(fsStream);
                    stream.write(data);
                    fileStatus.setFailedTime(file.getAbsolutePath());
                }catch (Exception e1){
                    e1.printStackTrace();
                }
            }else {
                e.printStackTrace();
            }
        } finally {
            try {
                if (stream != null) {
                    stream.flush();
                    stream.close();
                    downed_num.incrementAndGet();
                    // 使用友盟统计下载成功次数
                    UmengUtil.onEvent(mContext, Common.EVENT_DOWNLOAD_FINISH);
                    scan(file.toString(),fileStatus);
                } else if (stream == null && file.exists()) {
                    downed_num.incrementAndGet();
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
    private void scan(String file,final DownloadFileStatus fileStatus) {
        // TODO Auto-generated method stub
        new PWMediaScanner(this, file, null, new PWMediaScanner.ScannerListener() {
            @Override
            public void OnScannerFinish() {
                saveFileToDb(fileStatus);

            }
        });
    }

    private synchronized void saveFileToDb(final DownloadFileStatus fileStatus){
        if (fixedThreadPool != null && !fixedThreadPool.isShutdown()) {
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try{
                        lastUrl = fileStatus.getPhotoId();
                        taskList.remove(fileStatus.getPhotoId());
                        if (downloadList.size() > 0) {
                            downloadList.remove(fileStatus.getPosition());
                        }
                        final String loadTime = AppUtil.getFormatCurrentTime();
                        for (String key:taskList.keySet()) {
                            DownloadFileStatus status = taskList.get(key);
                            if (status.getPosition() > fileStatus.getPosition()) {
                                status.setPosition(status.getPosition() - 1);
                            }
                        }

                        pictureAirDbManager.updateLoadPhotos(userId,"true",loadTime,fileStatus.getTotalSize(),fileStatus.getPhotoId(),fileStatus.getFailedTime());
                        if (adapterHandler != null) {
                            adapterHandler.obtainMessage(DownLoadingFragment.PHOTO_REMOVE,fileStatus).sendToTarget();
                        }else {
                            handler.sendEmptyMessageDelayed(ADD_DOWNLOAD,200);
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        PictureAirLog.out("downloadService-----------> ondestroy");
        downloadList.clear();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
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

    public void startDownload(){
        handler.sendEmptyMessage(START_DOWNLOAD);
    }

    public void sendClearFailedMsg(){
        handler.sendEmptyMessage(CLEAR_FAILED);
    }

    public boolean isDownloading (){
        return isDownloading;
    }

    private void forceFinish(){
        if (adapterHandler != null){
            adapterHandler.sendEmptyMessage(DownLoadingFragment.DOWNLOAD_FINISH);
        }
    }

    public boolean downloadListContainsFailur(){
        if (downloadList != null && downloadList.size() >0){
            for (int i = 0;i<downloadList.size(); i++){
                if (downloadList.get(i).status == DownloadFileStatus.DOWNLOAD_STATE_FAILURE
                        || downloadList.get(i).status == DownloadFileStatus.DOWNLOAD_STATE_UPLOADING){
                    return true;
                }
            }
        }
        return false;
    }

    public void updateDownloadList(){
        if (downloadList!= null && downloadList.size() >0) {
            for (int i = 0; i < downloadList.size(); i++) {
                DownloadFileStatus fileStatus = downloadList.get(i);
                if (fileStatus.status == DownloadFileStatus.DOWNLOAD_STATE_FAILURE
                        || fileStatus.status == DownloadFileStatus.DOWNLOAD_STATE_UPLOADING){
                    fileStatus.lastStatus = fileStatus.status;
                    fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_SELECT;
                }
            }
            if (adapterHandler != null){
                adapterHandler.sendEmptyMessage(DownLoadingFragment.PHOTO_STATUS_UPDATE);
            }
        }
    }

    public void setDownloadListSelectOrNot(int select){
        if (downloadList!= null && downloadList.size() >0) {
            for (int i = 0; i < downloadList.size(); i++) {
                DownloadFileStatus fileStatus = downloadList.get(i);
                if (fileStatus.status == DownloadFileStatus.DOWNLOAD_STATE_SELECT){
                    fileStatus.select = select;
                }
            }
            if (adapterHandler != null){
                adapterHandler.sendEmptyMessage(DownLoadingFragment.PHOTO_ALL_SELECT);
            }
        }
    }

    public void reverseDownloadList(){
        if (downloadList!= null && downloadList.size() >0) {
            for (int i = 0; i < downloadList.size(); i++) {
                DownloadFileStatus fileStatus = downloadList.get(i);
                if (fileStatus.status == DownloadFileStatus.DOWNLOAD_STATE_SELECT){
                    fileStatus.status =fileStatus.lastStatus;
                    fileStatus.lastStatus = 0;
                    fileStatus.select = 0;
                }
            }
            if (adapterHandler != null){
                adapterHandler.sendEmptyMessage(DownLoadingFragment.PHOTO_STATUS_UPDATE);
            }
        }
    }

    public void deleteSelecItems(){
        if (fixedThreadPool != null && !fixedThreadPool.isShutdown()) {
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    if (downloadList != null && downloadList.size() > 0) {

                        Iterator<DownloadFileStatus> iterator = downloadList.iterator();
                        while (iterator.hasNext()) {
                            DownloadFileStatus deleteStatus = iterator.next();
                            if (deleteStatus.status == DownloadFileStatus.DOWNLOAD_STATE_SELECT && deleteStatus.select == 1) {
                                pictureAirDbManager.deletePhotoByPhotoId(userId,deleteStatus.getPhotoId());
                                downloadList.remove(deleteStatus);
                            }
                        }

                        if (adapterHandler != null) {
                            adapterHandler.sendEmptyMessage(DownLoadingFragment.REMOVE_FAILED_PHOTOS);
                        }
                    }
                }
            });
        }
    }

    class ChangeLoadToFailTask implements Runnable{
        @Override
        public void run() {
            countDownLatch = new CountDownLatch(1);
            List<PhotoDownLoadInfo> loadPhotos = pictureAirDbManager.getPhotos(userId,"load");

            if (loadPhotos != null && loadPhotos.size() >0) {
                pictureAirDbManager.updateLoadPhotoList(userId,"false","","",loadPhotos);
            }
            countDownLatch.countDown();
        }
    }

    class AddDownloadTask implements Runnable{
        private Intent intent;
        public AddDownloadTask(Intent intent){
            this.intent = intent;
        }
        @Override
        public void run() {
            if (countDownLatch != null) {
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            addTask(intent);
        }
    }

    class PrepareDownloadTask implements Runnable{
        @Override
        public void run() {
            if (!isDownloading) {//如果当前不在下载
                prepareDownload();
                isDownloading = true;
            }else{
                if (adapterHandler != null) {
                    adapterHandler.sendEmptyMessage(DownLoadingFragment.SERVICE_LOAD_SUCCESS);
                }

            }
            processCount.set(0);
            prepareDownloadCount = 0;
        }
    }
}
