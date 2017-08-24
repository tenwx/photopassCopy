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
import com.pictureair.photopass.entity.DownloadFileStatus;
import com.pictureair.photopass.entity.PhotoDownLoadInfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.fragment.DownLoadingFragment;
import com.pictureair.photopass.greendao.PictureAirDbManager;
import com.pictureair.photopass.http.rxhttp.HttpCallback;
import com.pictureair.photopass.http.rxhttp.RxSubscribe;
import com.pictureair.photopass.util.API2;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.JsonUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.util.UmengUtil;
import com.pictureair.photopass.widget.PWToast;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.ResponseBody;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Action0;
import rx.functions.Func1;

/**
 * 下载网络图片服务类
 * 下载网络视频服务类
 *  文件断点下载，下载过程中文件名没有后缀名，下载完成后才会添加后缀名
 *  图片文件的命名如果过长会使用MD5修改，视频文件直接使用MD5命名
 */
public class DownloadService extends Service {
    private ArrayList<PhotoInfo> photos = new ArrayList<>();
    private CopyOnWriteArrayList<DownloadFileStatus> downloadList = new CopyOnWriteArrayList<>();
    private ConcurrentHashMap<String,DownloadFileStatus> taskList = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,Subscription> subMap = new ConcurrentHashMap<>();
    private AtomicInteger downed_num = new AtomicInteger(0);//实际下载照片数
    private AtomicInteger failed_num = new AtomicInteger(0);//下载失败的照片数
    /**
     * 请求下载的数据，数据库中不存在，放在此列表中，后续加入数据库
     * */
    private CopyOnWriteArrayList<DownloadFileStatus> tempList = new CopyOnWriteArrayList<>();
    private boolean mFinish = false;
    private boolean mIsErrorsAdd = false;

    private Context mContext = this;
    private NotificationManager manager;
    private final static int FINISH_DOWNLOAD = 1;
    private final static int START_DOWNLOAD = 2;
    public final static int ADD_DOWNLOAD = 3;
    public final static int CLEAR_FAILED = 4;
    public final static int PREPARE_DOWNLOAD = 5;
    public final static int NO_PERMISSION = 6;

    private boolean isDownloading = false;
    private PWToast myToast;
    private PhotoBind photoBind;
    private Handler adapterHandler;
    private ExecutorService fixedThreadPool;
    private CountDownLatch countDownLatch;
    private boolean hasPhotos = false;
    private String lastUrl;
    private String userId;
    /**
     * 数据库中存在但是没有本地图片则存放在此列表中
     * */
    private CopyOnWriteArrayList<DownloadFileStatus> deleteList = new CopyOnWriteArrayList<>();
    private int prepareDownloadCount;
    private AtomicInteger processCount = new AtomicInteger(0);
    private boolean logout;
    private Handler handler = new DownloadHandler(this);
    //下载
    public static final int DOWNLOAD_PHOTO_SUCCESS = 6041;
    public static final int DOWNLOAD_PHOTO_FAILED = 6040;
    public final static int DOWNLOAD_PHOTO_GET_URL_SUCCESS = 6042;
    private File filedir;

    @Override
    public IBinder onBind(Intent arg0) {
        return photoBind;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        photoBind = new PhotoBind(this);
        PictureAirLog.out("downloadService ---------> onCreate" + downed_num.get() + "_" + failed_num.get());
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        myToast = new PWToast(getApplicationContext());
        fixedThreadPool = Executors.newFixedThreadPool(1);
        userId = SPUtils.getString(getApplicationContext(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ID, "");
        filedir = new File(Common.PHOTO_DOWNLOAD_PATH);
        if (!filedir.exists()) {
            filedir.mkdirs();
        }
        fixedThreadPool.execute(new ChangeLoadToFailTask());
    }

    @SuppressWarnings("deprecation")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PictureAirLog.out("DownloadService ----------> onStartCommand");
        mFinish = false;
        if (intent != null) {
            if (fixedThreadPool != null && !fixedThreadPool.isShutdown() ) {
                fixedThreadPool.execute(new AddDownloadTask(intent));
            }
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
                //如果在下载，先取消下载项，然后阻止handler的传递，来关闭下载
                unSubScribeAll();
                stopSelf();
            }else if (!AppUtil.checkPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                handler.sendEmptyMessage(NO_PERMISSION);
            } else if (photos != null) {
                //将新的数据放入到下载队列的末尾
                if (reconnect > -1) {//需要重连的走这个流程
                    if (photos.size() >0 && downloadList.size() >0) {
                        for (int i=0;i<photos.size();i++) {
                            PhotoInfo info = photos.get(i);
                            for (int j=0;j<downloadList.size();j++) {
                                DownloadFileStatus fileStatus = downloadList.get(j);
                                if (fileStatus != null && fileStatus.getPhotoId().equals(photos.get(i).getPhotoId()) && fileStatus.status == DownloadFileStatus.DOWNLOAD_STATE_SELECT) {
                                    fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_WAITING;
                                    processCount.incrementAndGet();
                                }else if(fileStatus != null && fileStatus.getPhotoId().equals(photos.get(i).getPhotoId()) && fileStatus.status == DownloadFileStatus.DOWNLOAD_STATE_RECONNECT){
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
                    CopyOnWriteArrayList<PhotoDownLoadInfo> infos = PictureAirDbManager.getExistPhoto(userId);
                    if (photos.size() >0) {
                        for (int i = 0; i < photos.size(); i++) {
                            PhotoInfo photoInfo = photos.get(i);
                            DownloadFileStatus fileStatus = null;
                            if (photoInfo.getIsVideo() == 0) {
                                fileStatus = new DownloadFileStatus(photoInfo.getPhotoThumbnail_1024(), photoInfo.getPhotoThumbnail_512(), photoInfo.getPhotoThumbnail_1024(),
                                        photoInfo.getPhotoOriginalURL(), 0, 0, "0", photoInfo.getPhotoId(), photoInfo.getIsVideo(), photoInfo.getPhotoThumbnail_128(),
                                        photoInfo.getStrShootOn(), "", photoInfo.getVideoWidth(), photoInfo.getVideoHeight());
                            }else{
                                fileStatus = new DownloadFileStatus(photoInfo.getPhotoOriginalURL(), photoInfo.getPhotoThumbnail_512(), photoInfo.getPhotoThumbnail_1024(),
                                        photoInfo.getPhotoOriginalURL(), 0, 0, "0", photoInfo.getPhotoId(), photoInfo.getIsVideo(), photoInfo.getPhotoThumbnail_128(),
                                        photoInfo.getStrShootOn(), "", photoInfo.getVideoWidth(), photoInfo.getVideoHeight());
                            }
                            boolean existPhoto = false;
                            for (int j = 0; j < infos.size(); j++) {

                                PhotoDownLoadInfo info = infos.get(j);
                                //请求下载的图片如果在数据库中存在，则判断本地文件中是否有照片（照片的可能因为名字过长而使用MD5转换），如果本地照片存在则跳过，不存在则加到下载队列
                                if (info.getPhotoId().equalsIgnoreCase(fileStatus.getPhotoId())) {

                                    if ("true".equalsIgnoreCase(info.getStatus())) {
                                        String fileName = "";
                                        if (TextUtils.isEmpty(info.getFailedTime())) {//判断数据库中文件是否存在，文件名可能过长从而用MD5转换并保存在FailedTime中
                                            fileName = filedir + "/" + AppUtil.getReallyFileName(fileStatus.getUrl(), fileStatus.isVideo());
                                        } else {
                                            fileName = info.getFailedTime();
                                        }
                                        PictureAirLog.out("filename=" + fileName);

                                        File file = new File(fileName);
                                        if (!file.exists()) {//下载的图片存在数据中但是本地却没有照片的情况下加入到deleteList中，后续将数据库对应数据清除
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
                            PictureAirDbManager.deletePhotos(userId,deleteList);
                            deleteList.clear();
                        }

                        if (tempList.size() >0){
                            PictureAirDbManager.insertPhotos(userId,tempList,"","load");
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
        Notification notification = new NotificationCompat.Builder(MyApplication.getInstance()).
                setSmallIcon(AppUtil.getNotificationIcon()).setAutoCancel(true).setContentTitle(MyApplication.getInstance().getString(R.string.app_name))
                .setContentText(MyApplication.getInstance().getString(R.string.downloading)).setWhen(System.currentTimeMillis()).setTicker(MyApplication.getInstance().getString(R.string.downloading)).build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;//通知栏可以自动删除
        manager.notify(0, notification);
    }

    private void cancelNotification(){
        if (manager != null) {
            manager.cancel(0);
        }
    }

    private static class DownloadHandler extends Handler {
        private WeakReference<DownloadService> serviceWeakRef;

        public DownloadHandler(DownloadService downloadService) {
            this.serviceWeakRef = new WeakReference<>(downloadService);
        }

        @Override
        public void handleMessage(Message msg) {
            DownloadService downloadService = serviceWeakRef.get();
            if (downloadService != null) {
                downloadService.dealwithMessage(msg);
            }
        }
    }

    /**
     * 弹出通知，获取下载失败的数据
     * */
    private void prepareDownload() {
        // TODO Auto-generated method stub
        PictureAirLog.out("DownloadService ----------> preparedownload");
        if (hasPhotos) {
            hasPhotos = false;
            startNotification();
        }
        if (!mIsErrorsAdd) {
            mIsErrorsAdd = true;
            List<PhotoDownLoadInfo> infos = PictureAirDbManager.getPhotos(userId, "false", "upload");
            if (infos != null && infos.size() > 0) {

                for (int i = 0; i < infos.size(); i++) {
                    PhotoDownLoadInfo info = infos.get(i);
                    DownloadFileStatus fileStatus = new DownloadFileStatus(info.getUrl(), "", "", "", info.getReadLength(), info.getSize(), "0",
                            info.getPhotoId(), info.getIsVideo(), info.getPreviewUrl(), info.getShootTime(), info.getFailedTime(), info.getVideoWidth(),info.getVideoHeight());
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

    public void dealwithMessage(Message msg) {

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
                                    fileExistMsg.what = DOWNLOAD_PHOTO_SUCCESS;
                                    Bundle bundle = new Bundle();
                                    fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_FINISH;
                                    bundle.putParcelable("url",fileStatus);
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
                String notificationDetail = String.format(MyApplication.getInstance().getString(R.string.download_detail1), downed_num.get());
                if (failed_num.get() >0) {
                    notificationDetail = String.format(MyApplication.getInstance().getString(R.string.download_detail2), downed_num.get(), failed_num.get());
                }
                Intent intent = new Intent(mContext, DownloadPhotoPreviewActivity.class);
                Bundle bundle1 = new Bundle();
                bundle1.putInt("position",-1);
                bundle1.putString("path",lastUrl);
                intent.putExtras(bundle1);
                PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                Notification notification = new NotificationCompat.Builder(MyApplication.getInstance()).
                        setSmallIcon(AppUtil.getNotificationIcon()).setAutoCancel(true).setContentTitle(MyApplication.getInstance().getString(R.string.app_name))
                        .setContentText(notificationDetail).setContentIntent(pendingIntent).
                                setWhen(System.currentTimeMillis()).setTicker(notificationDetail).build();
                notification.flags = Notification.FLAG_AUTO_CANCEL;//通知栏可以自动删除
                notification.defaults = Notification.DEFAULT_SOUND;//默认下载完成声音

                manager.notify(0, notification);
                downed_num.set(0);
                failed_num.set(0);
                isDownloading = false;
                if (adapterHandler != null) {
                    adapterHandler.sendEmptyMessage(DownLoadingFragment.DOWNLOAD_FINISH);
                }
                break;

            case DOWNLOAD_PHOTO_SUCCESS://下载成功之后获取data数据，然后base64解码，然后保存。
                PictureAirLog.out("downloadService----------->DOWNLOAD_PHOTO_SUCCESS");
                if (adapterHandler != null) {
                    adapterHandler.sendEmptyMessage(DownLoadingFragment.PHOTO_STATUS_UPDATE);
                }
                Bundle bundle = msg.getData();
                DownloadFileStatus fileStatus = (DownloadFileStatus) bundle.get("url");
                saveFile(fileStatus);
                break;
            case DOWNLOAD_PHOTO_FAILED://下载失败
                Bundle failBundle = msg.getData();
                final DownloadFileStatus failStatus = (DownloadFileStatus) failBundle.get("url");
                final int errCode = (int) failBundle.get("status");
                unSubScribeDownload(failStatus);
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
                                if (taskList.containsKey(failStatus.getPhotoId())) {
                                    taskList.remove(failStatus.getPhotoId());
                                    if (failStatus.isVideo() == 0 && failStatus.status == DownloadFileStatus.DOWNLOAD_STATE_UPLOADING) {
                                        PictureAirLog.out("downloadService----------->DOWNLOAD_PHOTO_FAILED errCode == 404 or url empty");
                                        PictureAirDbManager.updateLoadPhotos(userId, "upload", "", failStatus.getTotalSize(), failStatus.getCurrentSize(), failStatus.getPhotoId(), failStatus.getFailedTime());
                                    } else {
                                        PictureAirDbManager.updateLoadPhotos(userId, "false", "", failStatus.getTotalSize(), failStatus.getCurrentSize(), failStatus.getPhotoId(), failStatus.getFailedTime());
                                    }
                                    if (adapterHandler != null) {
                                        adapterHandler.obtainMessage(DownLoadingFragment.PHOTO_STATUS_UPDATE, failStatus).sendToTarget();
                                    } else {
                                        handler.sendEmptyMessage(ADD_DOWNLOAD);
                                    }
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
                            if (taskList.size() < 3) {//同时下载总数不超过3个
                                status.status = DownloadFileStatus.DOWNLOAD_STATE_DOWNLOADING;
                                status.setPosition(i);
                                PictureAirLog.out("ADD_DOWNLOAD photoid and position " + status.getPhotoId() +" "+status.getPosition());
                                taskList.put(status.getPhotoId(), status);
                                if (fileExists(status)) {
                                    Message fileExistMsg2 =  handler.obtainMessage();
                                    fileExistMsg2.what = DOWNLOAD_PHOTO_SUCCESS;
                                    Bundle bundle2 = new Bundle();
                                    status.status = DownloadFileStatus.DOWNLOAD_STATE_FINISH;
                                    bundle2.putParcelable("url",status);
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
                                PictureAirDbManager.deleteDownloadFailPhotoByUserId(userId);
                                if (adapterHandler != null) {
                                    adapterHandler.sendEmptyMessage(DownLoadingFragment.REMOVE_FAILED_PHOTOS);
                                }
                            }
                        });
                    }
                }
                break;

            case DOWNLOAD_PHOTO_GET_URL_SUCCESS://更新url
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

    /**
     * 下载保存本地
     * */
    private void writeCache(ResponseBody responseBody, File file, DownloadFileStatus info) throws IOException{
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();

        RandomAccessFile randomAccessFile = null;

        try {
            randomAccessFile = new RandomAccessFile(file, "rwd");
            info.setFailedTime("");
        } catch (Exception e) {
            if (e.getMessage().contains("ENAMETOOLONG")) {//如果是文件过长错误，需要重新保存
                String path = file.getAbsolutePath();
                path = path.substring(Common.PHOTO_DOWNLOAD_PATH.length(),path.length());
                file = new File(Common.PHOTO_DOWNLOAD_PATH+"/"+AppUtil.md5(path));
                PictureAirLog.e("saveFile ENAMETOOLONG",file.getAbsolutePath());
                randomAccessFile = new RandomAccessFile(file, "rwd");
                info.setFailedTime(file.getAbsolutePath());
            }
        }

        randomAccessFile.seek(info.getCurrentSize());
        byte[] buffer = new byte[1024*8];
        int len;
        int record = 0;
        while ((len = responseBody.byteStream().read(buffer)) != -1) {
            randomAccessFile.write(buffer, 0, len);
            record += len;
            PictureAirLog.out("writeCache current length: "+record);
        }

        responseBody.byteStream().close();
        if (responseBody.byteStream() != null) {
            responseBody.byteStream().close();
        }
        if (randomAccessFile != null) {
            randomAccessFile.close();
        }
    }

    private void shutThreadAndStopService() {
        cancelNotification();
        stopSelf();
    }

    /**
     * 获取有后缀名的文件
     * */
    private File getSaveFile(DownloadFileStatus fileStatus){
        String fileName = AppUtil.getReallyFileName(fileStatus.getUrl(),fileStatus.isVideo());
        PictureAirLog.out("filename=" + fileName);
        return new File(filedir + "/" + fileName);
    }

    /**
     * 获取没有后缀名的文件
     * */
    private File getSaveFileWithoutSuffix(DownloadFileStatus fileStatus) {
        String fileName = AppUtil.getReallyFileNameWithoutSuffix(fileStatus.getUrl(),fileStatus.isVideo());
        PictureAirLog.out("filename WithoutSuffix=" + fileName);
        return new File(filedir + "/" + fileName);
    }

    /**
     * 用于判断文件是否存在，文件名在下载过程中（没有下载完成），图片没有后缀名，下载成功后才添加后缀名
     * 文件如果存在直接发送DOWNLOADFINISH，在DOWNLOADFINISH中保存到数据库
     * */
    private boolean fileExists(DownloadFileStatus fileStatus) {
        //有后缀名的文件
        String fileName = AppUtil.getReallyFileName(fileStatus.getUrl(),fileStatus.isVideo());
        //没有后缀名的文件
        String fileNameWithoutSuffix = AppUtil.getReallyFileNameWithoutSuffix(fileStatus.getUrl(), fileStatus.isVideo());
        File file = new File(filedir + "/" + fileName);
        File fileWithoutSuffix = new File(filedir + "/" + fileNameWithoutSuffix);
        //如果图片文件已经创建，并出现了文件名过长的问题，在重连时需要查看本地是否有MD5命名的文件名，视频文件不会出现文件名过长，不需要查看
        if (fileStatus.isVideo() == 0) {

            String md5FileName = AppUtil.md5(fileName)+".jpg";
            File md5File = new File(filedir + "/" + md5FileName);

            if (file.exists() || md5File.exists()) {//正常命名文件或MD5命名文件存在，则已下载
                long totalSize = file.length();
                fileStatus.setTotalSize(totalSize);
                fileStatus.setCurrentSize(totalSize);
                return true;
            }

            String md5FileNameWithoutSuffix = AppUtil.md5(fileNameWithoutSuffix);
            File md5FileWithoutSuffix = new File(filedir + "/" + md5FileNameWithoutSuffix);
            if (!fileWithoutSuffix.exists() && !md5FileWithoutSuffix.exists()) {//未下载完成的图片没有后缀，确认无后缀文件是否存在，存在则把下载大小添加到filestatus中
                fileStatus.setCurrentSize(0);
                fileStatus.setTotalSize(0);
            } else {
                if (fileWithoutSuffix.exists()) {
                    fileStatus.setCurrentSize(fileWithoutSuffix.length());
                } else if (md5FileWithoutSuffix.exists()) {
                    fileStatus.setCurrentSize(md5FileWithoutSuffix.length());
                }
            }
            return false;
        } else {
            if (file.exists()) {
                long totalSize = file.length();
                fileStatus.setTotalSize(totalSize);
                fileStatus.setCurrentSize(totalSize);
                return true;
            }

            if (!fileWithoutSuffix.exists()) {
                fileStatus.setCurrentSize(0);
                fileStatus.setTotalSize(0);
            } else {
                fileStatus.setCurrentSize(fileWithoutSuffix.length());
            }
            return false;
        }
    }

    private void getNewUrl(DownloadFileStatus fileStatus){
        if (AppUtil.isOldVersionOfTheVideo(fileStatus.getOriginalUrl(), fileStatus.getPhotoThumbnail_1024(), fileStatus.getPhotoThumbnail_512(), fileStatus.getPhotoThumbnail())){
            PictureAirLog.out("getNewUrl----------->  requestNewUrl");
            getPhotosInfo(false, fileStatus);
        }else{
            PictureAirLog.out("getNewUrl----------->  original Url");
            getPhotosInfo(true, fileStatus);
        }
    }

    private void getPhotosInfo(final boolean hasOriginUrl, final DownloadFileStatus fileStatus) {

        Map<String, Object> params = new HashMap<>();

        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.ISDOWNLOAD, true);
        //有原图是字符串downloadPhotoIds，没有原图是传ids ，jsonArray
        if (hasOriginUrl) {
            params.put(Common.DOWNLOAD_PHOTO_IDS, fileStatus.getPhotoId());
        } else {
            JSONArray ids = new JSONArray();
            ids.add(fileStatus.getPhotoId());
            params.put(Common.EPPP_IDS, ids.toString());
        }
        API2.getPhotosInfo(params)
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        PictureAirLog.e("getPhotoinfo", "unsubscribe");
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        PictureAirLog.out("jsonobject---->" + jsonObject.toString());
                        JSONArray photos = jsonObject.getJSONArray("photos");
                        if (!hasOriginUrl) {
                            if (photos.size() > 0) {
                                PhotoInfo photoInfo = JsonUtil.getPhoto(photos.getJSONObject(0), "");
                                fileStatus.setNewUrl(photoInfo.getPhotoOriginalURL());
                                if (!TextUtils.isEmpty(fileStatus.getNewUrl())) {
                                    handler.obtainMessage(DOWNLOAD_PHOTO_GET_URL_SUCCESS, fileStatus).sendToTarget();
                                } else {
                                    sendNoDataMsg(fileStatus);
                                }
                            } else {
                                sendNoDataMsg(fileStatus);
                            }
                        } else {
                            fileStatus.setNewUrl(fileStatus.getOriginalUrl());
                            handler.obtainMessage(DOWNLOAD_PHOTO_GET_URL_SUCCESS, fileStatus).sendToTarget();
                        }
                    }

                    @Override
                    public void _onError(int status) {
                        if (hasOriginUrl) {//如果有原图链接的情况直接下载
                            fileStatus.setNewUrl(fileStatus.getOriginalUrl());
                            handler.obtainMessage(DOWNLOAD_PHOTO_GET_URL_SUCCESS, fileStatus).sendToTarget();
                        } else {
                            Message msg = handler.obtainMessage();
                            msg.what = DOWNLOAD_PHOTO_FAILED;
                            Bundle bundle = new Bundle();
                            fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_FAILURE;
                            bundle.putParcelable("url", fileStatus);
                            bundle.putInt("status", 401);
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                        }
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    private void sendNoDataMsg (DownloadFileStatus fileStatus) {
        Message msg = handler.obtainMessage();
        msg.what = DOWNLOAD_PHOTO_FAILED;
        Bundle bundle = new Bundle();
        if (fileStatus.isVideo() == 0) {
            fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_UPLOADING;
        } else {
            fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_FAILURE;
        }
        bundle.putParcelable("url", fileStatus);
        bundle.putInt("status", 404);
        msg.setData(bundle);
        handler.sendMessage(msg);
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
        downloadPhotosWithUrl(fileStatus.getCurrentSize(), fileStatus);
    }


    private void downloadPhotosWithUrl(long length, final DownloadFileStatus fileStatus) {
        Subscription subscription = API2.continueDownload(length, fileStatus.getNewUrl(), new HttpCallback() {
            long startTime = System.currentTimeMillis();
            long lastTime = startTime;
            long lastDownloadSize = fileStatus.getCurrentSize();

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                fileStatus.setCurrentSize(lastDownloadSize + bytesWritten);
                fileStatus.setTotalSize(lastDownloadSize + totalSize);

                PictureAirLog.d("write length: "+bytesWritten + "--------" + "total length: "+ totalSize);

                long currentTime = System.currentTimeMillis();
                float usedTime = (currentTime-lastTime)/1000f;
                float keepTime = (currentTime-startTime)/1000f;
                if (usedTime > 0.2) {
                    lastTime = currentTime;
                    double downSpeed = (bytesWritten / 1000d) / keepTime;
                    String ds = AppUtil.formatData(downSpeed);
                    fileStatus.setLoadSpeed(ds);
                    if (adapterHandler != null) {
                        adapterHandler.sendEmptyMessage(DownLoadingFragment.PHOTO_STATUS_UPDATE);
                    }
                }
            }
        }).map(new Func1<ResponseBody, DownloadFileStatus>() {

            @Override
            public DownloadFileStatus call(ResponseBody responseBody) {
                //缓存文件到本地
                try {
                    writeCache(responseBody, getSaveFileWithoutSuffix(fileStatus), fileStatus);
                    responseBody.close();
                    return fileStatus;
                } catch (Exception e) {
                    PictureAirLog.e("writecache exception", "hasException");
                    //需要关闭reponsebody,否则内存泄漏
                    responseBody.close();
                    throw Exceptions.propagate(e);
                }
            }
        }).subscribe(new RxSubscribe<DownloadFileStatus>() {
            @Override
            public void _onNext(DownloadFileStatus downloadFileStatus) {
            }

            @Override
            public void _onError(int status) {
                PictureAirLog.e("DownloadService","下载失败 失败码:"+status);
                Message msg =  handler.obtainMessage();
                msg.what = DOWNLOAD_PHOTO_FAILED;
                Bundle bundle = new Bundle();
                //下载状态为404表示后台没有对应文件会显示"图片上传中"
                //416的错误表示断点超出范围，当文件下载完成却意外退出时需要重新下载，此时断点续传会报错，需要特殊处理
                if (status != 404 && status != 416) {
                    fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_FAILURE;
                } else if (status == 416){
                    if (fileStatus.getCurrentSize() == fileStatus.getTotalSize()) {
                        downloadSuccess(fileStatus);
                        return;
                    }
                } else {
                    if (fileStatus.isVideo() == 0) {
                        fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_UPLOADING;
                    } else {
                        fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_FAILURE;
                    }
                }
                bundle.putParcelable("url",fileStatus);
                bundle.putInt("status",status);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }

            @Override
            public void onCompleted() {
                PictureAirLog.out("下载成功");
                downloadSuccess(fileStatus);
            }
        });
        subMap.put(fileStatus.getNewUrl(), subscription);
    }

    /**
     * 下载成功后重命名
     * */
    private void downloadSuccess(DownloadFileStatus fileStatus) {
        if (TextUtils.isEmpty(fileStatus.getFailedTime())) {//区分文件名是否被MD5过
            renameFile(getSaveFileWithoutSuffix(fileStatus), getSaveFile(fileStatus));
        } else {
            File fileNew = getSaveFile(fileStatus);
            String path = fileNew.getAbsolutePath();
            path = path.substring(Common.PHOTO_DOWNLOAD_PATH.length(),path.length());
            fileNew = new File(Common.PHOTO_DOWNLOAD_PATH+"/"+AppUtil.md5(path)+".jpg");
            File fileOld = new File(fileStatus.getFailedTime());
            renameFile(fileOld, fileNew);
            fileStatus.setFailedTime(fileNew.getAbsolutePath());
        }
        UmengUtil.onEvent(mContext, Common.EVENT_DOWNLOAD_FINISH);
        Message msg =  handler.obtainMessage();
        msg.what = DOWNLOAD_PHOTO_SUCCESS;
        Bundle bundle = new Bundle();
        fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_FINISH;
        bundle.putParcelable("url",fileStatus);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    /**
     * 下载完成后给文件名添加后缀
     * */
    private void renameFile(File fileOld, File fileNew) {
        if (fileOld == null || fileNew == null) return;
        boolean res = fileOld.renameTo(fileNew);
        PictureAirLog.d("rename file",res ? "success" : "false");
    }

    /**
     * 保存文件到SDcard
     *
     */
    private void saveFile(DownloadFileStatus fileStatus) {
        // TODO Auto-generated method stub
        if (!TextUtils.isEmpty(fileStatus.getFailedTime())) {
            AppUtil.fileScan(mContext, fileStatus.getFailedTime());
        } else {
            AppUtil.fileScan(mContext, filedir + "/" + AppUtil.getReallyFileName(fileStatus.getUrl(), fileStatus.isVideo()));
        }
        unSubScribeDownload(fileStatus);
        downed_num.incrementAndGet();
        UmengUtil.onEvent(mContext, Common.EVENT_DOWNLOAD_FINISH);
        saveFileToDb(fileStatus);

    }

    private void unSubScribeDownload(DownloadFileStatus fileStatus) {
        if (fileStatus.getNewUrl() != null) {
            if (subMap.containsKey(fileStatus.getNewUrl())) {
                Subscription subscription = subMap.get(fileStatus.getNewUrl());
                if (!subscription.isUnsubscribed()) subscription.unsubscribe();
                subMap.remove(fileStatus.getNewUrl());
            }
        }
    }

    /**
     * 取消下载
     * */
    public void unSubScribeAll() {
        if (!isDownloading) return;

        for (Subscription subscription : subMap.values()) {
            if (subscription != null) {
                subscription.unsubscribe();
            }
        }
        subMap.clear();

        if (fixedThreadPool != null && !fixedThreadPool.isShutdown() ) {
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (taskList != null && taskList.size() > 0) {
                            for (DownloadFileStatus fileStatus : taskList.values()) {
                                if (fileStatus != null) {
                                    failed_num.getAndIncrement();
                                    if (fileStatus.select == 1) {
                                        fileStatus.select = 0;
                                    }
                                    taskList.remove(fileStatus.getPhotoId());
                                    PictureAirDbManager.updateLoadPhotos(userId, "load", "", fileStatus.getTotalSize(), fileStatus.getCurrentSize(), fileStatus.getPhotoId(), fileStatus.getFailedTime());
                                }
                            }
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (fixedThreadPool != null && !fixedThreadPool.isShutdown()) {
                        PictureAirLog.out("DownloadService -----------> logout");
                        fixedThreadPool.shutdownNow();
                    }
                }
            });
        }

    }

    private void saveFileToDb(final DownloadFileStatus fileStatus){
        if (fixedThreadPool != null && !fixedThreadPool.isShutdown()) {
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try{
                        lastUrl = fileStatus.getPhotoId();
                        if (taskList.containsKey(fileStatus.getPhotoId())) {
                            taskList.remove(fileStatus.getPhotoId());
                            if (downloadList.size() > 0) {
                                downloadList.remove(fileStatus.getPosition());
                            }
                            final String loadTime = AppUtil.getFormatCurrentTime();
                            for (String key : taskList.keySet()) {
                                DownloadFileStatus status = taskList.get(key);
                                if (status.getPosition() > fileStatus.getPosition()) {
                                    status.setPosition(status.getPosition() - 1);
                                }
                            }

                            PictureAirDbManager.updateLoadPhotos(userId, "true", loadTime, fileStatus.getTotalSize(), fileStatus.getCurrentSize(), fileStatus.getPhotoId(), fileStatus.getFailedTime());
                            if (adapterHandler != null) {
                                adapterHandler.obtainMessage(DownLoadingFragment.PHOTO_REMOVE, fileStatus).sendToTarget();
                            } else {
                                handler.sendEmptyMessageDelayed(ADD_DOWNLOAD, 200);
                            }
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
        photoBind = null;
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        PictureAirLog.out("downloadService-----------> onUnbind");
        return super.onUnbind(intent);
    }

    public static class PhotoBind extends Binder{
        private WeakReference<DownloadService> serviceWeakRef;
        public PhotoBind(DownloadService downloadService) {
            serviceWeakRef = new WeakReference<>(downloadService);
        }

        public DownloadService getService(){
            if (serviceWeakRef != null && serviceWeakRef.get() != null) {
                return serviceWeakRef.get();
            }
            return null;
        }
    }

    public CopyOnWriteArrayList<DownloadFileStatus> getDownloadList(){
        return  new CopyOnWriteArrayList<>(downloadList);
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

    /**
     * 判断是否存在下载失败的条目
     * */
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

    /**把下载失败的项，更改成待选中状态*/
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

    /**
     * 设置是条目是否被选中
     * */
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

    /**
     * 翻转选中为失败状态
     * */
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

    /**删除选中项*/
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
                                PictureAirDbManager.deletePhotoByPhotoId(userId,deleteStatus.getPhotoId());
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

    /**
     *
     * 在下载初始化的时候，把以前下载等待的数据更新为下载失败
     * */
    class ChangeLoadToFailTask implements Runnable{
        @Override
        public void run() {
            countDownLatch = new CountDownLatch(1);
            List<PhotoDownLoadInfo> loadPhotos = PictureAirDbManager.getPhotos(userId,"load");

            if (loadPhotos != null && loadPhotos.size() >0) {
                PictureAirDbManager.updateLoadPhotoList(userId,"false","",loadPhotos);
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

    /**
     * 显示notification，加载下载失败的数据
     * */
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
