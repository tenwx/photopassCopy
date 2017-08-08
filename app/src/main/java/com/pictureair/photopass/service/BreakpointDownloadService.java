package com.pictureair.photopass.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.pictureair.photopass.entity.FileInfo;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.DownloadTask;
import com.pictureair.photopass.util.PictureAirLog;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by bass on 16/3/8.
 * 1. 负责断点下载
 */
public class BreakpointDownloadService extends Service {
    public static final String ACTION_STRAT = "action strat";
    public static final String ACTION_UPDATE = "action update";
    public static final int SERVICE_STOP = 2;

    public static final int MSG_INIT = 0;
    private DownloadTask mTask = null;

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_STRAT.equals(intent.getAction())) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            //启动初始化线程
            if (null != mTask) {
                mTask.isPause = false;
            }
            new InitThread(fileInfo).start();
        }
        return START_NOT_STICKY;//被系统kill之后，不会自动复活重新启动服务
    }

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT:
                    FileInfo fileInfo = (FileInfo) msg.obj;
                    //启动下载任务
                    mTask = new DownloadTask(BreakpointDownloadService.this, fileInfo,mHandler);
                    mTask.download();
                    break;

                case SERVICE_STOP:
                    stopSelf();//下载服务停止
                    break;
            }
            return false;
        }
    });

    /**
     * 初始化线程，不做下载，只获取文件的长度用
     */
    class InitThread extends Thread {
        private FileInfo fileInfo;

        public InitThread(FileInfo fileInfo) {
            this.fileInfo = fileInfo;
        }

        @Override
        public void run() {
//            super.run();
            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            int length = -1;
            try {
                //连接网络文件
                URL url = new URL(fileInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000);
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() == 200) {
                    //获得文件长度
                    length = conn.getContentLength();
                }

                if (length <= 0) {
                    return;
                }
                //在本地创建文件
                File dir = new File(Common.DOWNLOAD_APK_PATH);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                File file = new File(dir, fileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");//随即访问文件，可在文件任意一个位置访问,//"rwd"表示可读可写可删
                //设置文件长度
                raf.setLength(length);
                fileInfo.setLength(length);
                mHandler.obtainMessage(MSG_INIT, fileInfo).sendToTarget();

            } catch (Exception e) {
                PictureAirLog.out("download failed");
                e.printStackTrace();
                Intent intent = new Intent(BreakpointDownloadService.ACTION_UPDATE);
                intent.putExtra("onFailure", true);
                sendBroadcast(intent);
            } finally {
                PictureAirLog.out("finally failed");
                if (length <= 0) {
                    Intent intent = new Intent(BreakpointDownloadService.ACTION_UPDATE);
                    intent.putExtra("onFailure", true);
                    sendBroadcast(intent);
                }
                PictureAirLog.out("get length finally----->");
                try {
                    //关闭流
                    if (null != raf) {
                        raf.close();
                    }
                    if (null != conn) {
                        conn.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        /**
         * 断开服务时如果正在下载，需要保存下载记录
          */
        PictureAirLog.out(BreakpointDownloadService.class.getSimpleName() + " ---ondestroy");
        super.onDestroy();
    }
}
