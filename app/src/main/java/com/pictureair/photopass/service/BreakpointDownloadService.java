package com.pictureair.photopass.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.FileInfo;
import com.pictureair.photopass.entity.ThreadInfo;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.DownloadTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by bass on 16/3/8.
 * 1. 负责断点下载
 */
public class BreakpointDownloadService extends Service {
    public static final String ACTION_STRAT = "action strat";
    public static final String ACTION_STOP = "action stop";
    public static final String ACTION_UPDATE = "action update";

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
        } else if (ACTION_STOP.equals(intent.getAction())) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            if (null != mTask) {
                mTask.isPause = true;
            }
        }
        return START_NOT_STICKY;//被系统kill之后，不会自动复活重新启动服务
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_INIT:
                    FileInfo fileInfo = (FileInfo) msg.obj;
                    //启动下载任务
                    mTask = new DownloadTask(BreakpointDownloadService.this, fileInfo);
                    mTask.download();
                    break;
            }

        }
    };

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
            try {
                //连接网络文件
                URL url = new URL(fileInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000);
                conn.setRequestMethod("GET");

                int length = -1;

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
                e.printStackTrace();
            } finally {
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




}
