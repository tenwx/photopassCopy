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
import com.pictureair.photopass.activity.DownloadPhotoPreviewActivity;
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

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

/**
 * 下载网络图片服务类
 * 下载网络视频服务类
 */
public class DownloadService extends Service {
    private ArrayList<PhotoInfo> photos = new ArrayList<PhotoInfo>();
    private CopyOnWriteArrayList<DownloadFileStatus> downloadList = new CopyOnWriteArrayList<DownloadFileStatus>();
    private Map<String,DownloadFileStatus> taskList = new HashMap<>();
    private AtomicInteger downed_num = new AtomicInteger(0);//实际下载照片数
    private AtomicInteger failed_num = new AtomicInteger(0);//下载失败的照片数
    private AtomicInteger repeatCount = new AtomicInteger(0);//数据库已存在，本地文件不存在时重新下载计数需要剪去重复下载的数量
    private boolean mFinish = false;
    private boolean mIsErrorsAdd = false;
//    private boolean clear

    private Context mContext = this;
    private NotificationManager manager;
    private Notification notification;
    private final static int FINISH_DOWNLOAD = 1;
    private final static int START_DOWNLOAD = 2;
    public final static int ADD_DOWNLOAD = 3;
    public final static int CLEAR_FAILED = 4;

    private boolean isDownloading = false;
    private PWToast myToast;
    private PhotoBind photoBind = new PhotoBind();
    private Handler adapterHandler;
    private PictureAirDbManager pictureAirDbManager;
    private SharedPreferences preferences;
    private AtomicInteger databasePhotoCount = new AtomicInteger(0);//未下载之前的数据库照片数量
    private ExecutorService fixedThreadPool;
    private CountDownLatch countDownLatch;
    private boolean hasPhotos = false;
    private String lastUrl = new String();
    private String userId;
    private ConcurrentHashMap<String,DownloadFileStatus> cacheList = new ConcurrentHashMap<>();

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
        preferences = getApplicationContext().getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, Context.MODE_PRIVATE);
        userId = preferences.getString(Common.USERINFO_ID, "");
        fixedThreadPool = Executors.newFixedThreadPool(1);
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
        fixedThreadPool.execute(new ChangeLoadToFailTask());
    }

    @SuppressWarnings("deprecation")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //		serviceIntent = intent;
        PictureAirLog.out("DownloadService ----------> onStartCommand");
        mFinish = false;
        fixedThreadPool.execute(new AddDownloadTask(intent));
        return START_NOT_STICKY;//被系统kill之后，不会自动复活重新启动服务
    }


    private synchronized void addTask(Intent intent){
        Bundle b = intent.getExtras();
        if (b != null) {
            photos = b.getParcelableArrayList("photos");
            int reconnect = b.getInt("reconnect",-1);
            if (!AppUtil.checkPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                myToast.setTextAndShow(R.string.permission_storage_message, Common.TOAST_SHORT_TIME);
                stopSelf();//下载服务停止
            } else if (photos != null) {
                //将新的数据放入到下载队列的末尾
                if (reconnect > -1) {//需要重连的走这个流程
//                    mSartNotificate = false;
                    if (photos.size() >0 && downloadList.size() >0) {
                        for (int i=0;i<photos.size();i++) {
                            PhotoInfo info = photos.get(i);
                            for (int j=0;j<downloadList.size();j++) {
                                DownloadFileStatus fileStatus = downloadList.get(j);
                                if (fileStatus != null && fileStatus.getUrl().equals(photos.get(i).photoPathOrURL) && fileStatus.status == DownloadFileStatus.DOWNLOAD_STATE_SELECT) {
                                    fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_WAITING;
                                }
                            }
                        }
                        int count = failed_num.get() - photos.size();
                        if (count <0) {
                            failed_num.set(0);
                        }else{
                            failed_num.set(count);
                        }
                    }
                } else {//正常下载走这个
                    PictureAirLog.e("onstartCommand","start:"+System.currentTimeMillis());
                    for (int i = 0; i < photos.size(); i++) {
                        PhotoInfo photoInfo = photos.get(i);
                        boolean photoExists = pictureAirDbManager.isExistPhoto(userId,photoInfo.photoId);
                        final DownloadFileStatus fileStatus = new DownloadFileStatus(photoInfo.photoPathOrURL, "0", "0", "0", photoInfo.photoId, photoInfo.isVideo,photoInfo.photoThumbnail,photoInfo.shootOn,"");
                        if (!photoExists){
                            pictureAirDbManager.insertPhotos(userId,fileStatus,"","load");
                            addToDownloadList(fileStatus);
                        }else{
                            String fileName = AppUtil.getReallyFileName(fileStatus.getUrl(),fileStatus.isVideo());
                            PictureAirLog.out("filename=" + fileName);
                            File filedir = new File(Common.PHOTO_DOWNLOAD_PATH);
                            filedir.mkdirs();
                            final File file = new File(filedir + "/" + fileName);
                            if (!file.exists()){
                                addToDownloadList(fileStatus);
                                repeatCount.incrementAndGet();
                            }
                        }

                        if (downloadList.size() > 0){
                            hasPhotos = true;
                        }
                        PictureAirLog.e("onstartCommand","end:"+System.currentTimeMillis());


                        PictureAirLog.out("downloadlist size =" + downloadList.size());
                        PictureAirLog.out("onStartCommand downed_num："+downed_num.get());
                    }
                }

                if (!isDownloading) {//如果当前不在下载
                    prepareDownload();
                    isDownloading = true;
                }else{
                    if (adapterHandler != null) {
                        PictureAirLog.v("onStartCommand","isDownloading = true");
                        adapterHandler.sendEmptyMessage(DownLoadingFragment.SERVICE_LOAD_SUCCESS);
                    }
                }
            } else {
                stopSelf();//下载服务停止
            }
        } else {
            stopSelf();//下载服务停止
        }
    }

    private void addToDownloadList(DownloadFileStatus fileStatus){
        if (cacheList.get(fileStatus.getPhotoId()) == null) {
            downloadList.add(fileStatus);
            cacheList.put(fileStatus.getPhotoId(),fileStatus);
        }
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
        if (hasPhotos) {
            hasPhotos = false;
            startNotification();
        }

        PictureAirLog.out("prepareDownload>>>>>>>>>>>>>read database");
        int count = pictureAirDbManager.getDownloadPhotoCount(userId, "true");
        count -= repeatCount.get();
        databasePhotoCount.set(count);
        PictureAirLog.out("prepareDownload>>>>>>>>>>databasePhotoCount:" + databasePhotoCount);
        if (!mIsErrorsAdd) {
            mIsErrorsAdd = true;
            List<PhotoDownLoadInfo> infos = pictureAirDbManager.getPhotos(userId, "false");
            if (infos != null && infos.size() > 0) {

                for (int i = 0; i < infos.size(); i++) {
                    PhotoDownLoadInfo info = infos.get(i);
                    DownloadFileStatus fileStatus = new DownloadFileStatus(info.getUrl(), "0", "0", "0", info.getPhotoId(), info.getIsVideo(), info.getPreviewUrl(), info.getShootTime(), info.getFailedTime());
                    fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_FAILURE;
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
                case START_DOWNLOAD://开始下载
                    PictureAirLog.out("downloadService----------->START_DOWNLOAD");
                    PictureAirLog.v("handleMessage","START_DOWNLOAD");
                    if (downloadList.size() > 0){
                        for (int i = 0; i < downloadList.size(); i++) {
                            DownloadFileStatus fileStatus = downloadList.get(i);
                            if (fileStatus.status == DownloadFileStatus.DOWNLOAD_STATE_WAITING) {
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
                                downed_num.set(0);
                                repeatCount.set(0);
                                failed_num.set(0);
                                isDownloading = false;
                                PictureAirLog.out("START_DOWNLOAD downed_num："+downed_num.get());
                            }
                        }
                    }else{//从数据库中查找出所有下载失败的照片无法直接下载，会从这里结束
                        PictureAirLog.out("finish download-------------->");
                        mFinish = true;
                        stopSelf();//下载服务停止
                        downed_num.set(0);
                        repeatCount.set(0);
                        failed_num.set(0);
                        isDownloading = false;
                    }

                    break;
                case FINISH_DOWNLOAD://下载结束
                    //如果下载数目一致，提示用户下载完毕，并且让service停止掉
                    PictureAirLog.out("下载完毕,共下载了" + downed_num.get() + "张照片，失败了" + failed_num.get() + "张");
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
                            setSmallIcon(R.drawable.pp_icon).setAutoCancel(true).setContentTitle(mContext.getString(R.string.app_name))
                            .setContentText(notificationDetail).setContentIntent(pendingIntent).
                                    setWhen(System.currentTimeMillis()).setTicker(notificationDetail).build();
                    notification.flags = Notification.FLAG_AUTO_CANCEL;//通知栏可以自动删除
                    notification.defaults = Notification.DEFAULT_SOUND;//默认下载完成声音

                    stopSelf();//下载服务停止
                    manager.notify(0, notification);
                    downed_num.set(0);
                    failed_num.set(0);
                    repeatCount.set(0);
                    isDownloading = false;
                    PictureAirLog.out("FINISH_DOWNLOAD downed_num："+downed_num.get());
                    if (adapterHandler != null) {
                        adapterHandler.sendEmptyMessage(DownLoadingFragment.DOWNLOAD_FINISH);
                    }
                    break;

                case API1.DOWNLOAD_PHOTO_SUCCESS://下载成功之后获取data数据，然后base64解码，然后保存。
                    PictureAirLog.out("downloadService----------->DOWNLOAD_PHOTO_SUCCESS");
                    Bundle bundle = msg.getData();
                    DownloadFileStatus fileStatus = (DownloadFileStatus) bundle.get("url");
                    byte[] results = (byte[]) bundle.get("binaryData");
                    File file = getSaveFile(fileStatus);
                    saveFile(file, results,fileStatus);
                    break;
                case API1.DOWNLOAD_PHOTO_FAILED://下载失败
                    Bundle failBundle = msg.getData();
                    final DownloadFileStatus failStatus = (DownloadFileStatus) failBundle.get("url");
                    if (!fixedThreadPool.isShutdown() && fixedThreadPool != null) {
                        fixedThreadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                PictureAirLog.out("downloadService----------->DOWNLOAD_PHOTO_FAILED");
                                failed_num.getAndIncrement();
                                if (failStatus.select == 1){
                                    failStatus.select =0;
                                }
                                taskList.remove(failStatus.getUrl());

                                pictureAirDbManager.updateLoadPhotos(userId,"false","","",failStatus.getPhotoId());
                                if (adapterHandler != null) {
                                    PictureAirLog.v("handleMessage","DOWNLOAD_PHOTO_FAILED");
                                    adapterHandler.obtainMessage(DownLoadingFragment.PHOTO_STATUS_UPDATE, failStatus).sendToTarget();
                                } else {
                                    handler.sendEmptyMessage(ADD_DOWNLOAD);
                                }
                            }
                        });
                    }
                    break;
                case ADD_DOWNLOAD://添加下载任务
                    PictureAirLog.out("downloadService----------->ADD_DOWNLOAD");
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
                                        PictureAirLog.v("handleMessage","ADD_DOWNLOAD");
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
                            PictureAirLog.out("ADD_DOWNLOAD send  FINISH_DOWNLOAD");
                            handler.sendEmptyMessage(FINISH_DOWNLOAD);
                            mFinish = true;
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
                            }
                        }
                        failed_num.set(0);
                        if (!fixedThreadPool.isShutdown() && fixedThreadPool != null) {
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

                default:
                    break;
            }
        }

    };


    private File getSaveFile(DownloadFileStatus fileStatus){
        String fileName = AppUtil.getReallyFileName(fileStatus.getUrl(),fileStatus.isVideo());
        PictureAirLog.out("filename=" + fileName);
        File filedir = new File(Common.PHOTO_DOWNLOAD_PATH);
        filedir.mkdirs();
        File file = new File(filedir + "/" + fileName);
        return file;
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
        PictureAirLog.out("downloadurl--->" + fileStatus.getUrl());
        File file = getSaveFile(fileStatus);

        // 使用友盟统计点击下载次数
        UmengUtil.onEvent(mContext, Common.EVENT_ONCLICK_DOWNLOAD);
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
                    PictureAirLog.v("downloadImgOrVideo", "调用下载照片API成功");
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
                    PictureAirLog.v("downloadImgOrVideo", "调用下载照片API失败：错误代码：" + status);
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
                    double currentSize = bytesWritten/1000d/1000d;
                    double total = totalSize/1000d/1000d;
                    String c = AppUtil.formatData(currentSize);
                    String t = AppUtil.formatData(total);
                    fileStatus.setCurrentSize(c);
                    fileStatus.setTotalSize(t);
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
                    downed_num.incrementAndGet();
                    PictureAirLog.out("saveFile()>>>>>>>>>> downed_num："+downed_num.get());
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
                        saveFileToDb(fileStatus);
                    }
                });
    }

    private synchronized void saveFileToDb(final DownloadFileStatus fileStatus){
        if (fixedThreadPool != null && !fixedThreadPool.isShutdown()) {
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    PictureAirLog.out("downloadService-----------> scan");
                    lastUrl = fileStatus.getUrl();
                    taskList.remove(fileStatus.getUrl());
                    downloadList.remove(fileStatus.getPosition());
                    final String loadTime = AppUtil.getFormatCurrentTime();
                    for (String key:taskList.keySet()) {
                        DownloadFileStatus status = taskList.get(key);
                        if (status.getPosition() > fileStatus.getPosition()) {
                            status.setPosition(status.getPosition() - 1);
                        }
                    }

                    if (adapterHandler != null) {
                        adapterHandler.obtainMessage(DownLoadingFragment.PHOTO_REMOVE,fileStatus).sendToTarget();
                        PictureAirLog.v("scan","scan");
                        adapterHandler.removeMessages(DownLoadingFragment.PHOTO_STATUS_UPDATE);
                    }else {
                        handler.sendEmptyMessageDelayed(ADD_DOWNLOAD,200);
                    }
                    pictureAirDbManager.updateLoadPhotos(userId,"true",loadTime,fileStatus.getTotalSize(),fileStatus.getPhotoId());
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        PictureAirLog.out("downloadService-----------> ondestroy");
        downloadList.clear();
//        mSartNotificate = false;
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


    public AtomicInteger getDatabasePhotoCount(){
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

    public boolean downloadListContainsFailur(){
        if (downloadList != null && downloadList.size() >0){
            for (int i = 0;i<downloadList.size(); i++){
                if (downloadList.get(i).status == DownloadFileStatus.DOWNLOAD_STATE_FAILURE){
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
                if (fileStatus.status == DownloadFileStatus.DOWNLOAD_STATE_FAILURE){
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
                    fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_FAILURE;
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

    @Subscribe
    public void onUserEvent(BaseBusEvent baseBusEvent) {
        if (baseBusEvent instanceof DownLoadCountUpdateEvent) {
            DownLoadCountUpdateEvent event = (DownLoadCountUpdateEvent)baseBusEvent;
            databasePhotoCount.set(0);
            int count = downed_num.get();
            count -= event.getUpdateCount();
            if (count < 0){
                downed_num.set(0);
            }else{
                downed_num.set(count);
            }
        }
    }

    public AtomicInteger getDowned_num(){
        return downed_num;
    }

    class ChangeLoadToFailTask implements Runnable{
        @Override
        public void run() {
            countDownLatch = new CountDownLatch(1);
            List<PhotoDownLoadInfo> loadPhotos = pictureAirDbManager.getPhotos(userId,"load");
            if (loadPhotos != null && loadPhotos.size() >0) {
                for (int i=0;i<loadPhotos.size();i++) {
                    PhotoDownLoadInfo info = loadPhotos.get(i);
                    pictureAirDbManager.updateLoadPhotos(userId,"false","","",info.getPhotoId());
                }
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
}
