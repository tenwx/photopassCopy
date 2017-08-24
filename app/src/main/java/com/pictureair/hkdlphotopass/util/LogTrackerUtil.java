package com.pictureair.hkdlphotopass.util;

import android.content.Context;

import com.alibaba.fastjson.JSONObject;
import com.pictureair.hkdlphotopass.http.rxhttp.RxSubscribe;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 用来记录用户的操作步骤，有利于疑难杂症的处理
 * <p>
 * 使用步骤：
 * 1.在application中初始化LogTrackerUtil.getInstance().init();
 * 2.每个关键方法进入和结束，都需要调用LogTrackerUtil.getInstance().addTrackMsg();
 * 3.在app整个退出的时候 或者 app奔溃的时候，需要调用LogTrackerUtil.getInstance().release();
 * <p>
 * 核心内容
 * 1.按天记录，单个文件过大，则新建一个文件记录
 * 2.每当list中超过规定数量的时候，才会执行写入文件的方法
 * 3.app退出的时候，会将list中剩余的部分写入文件
 * 4.app一起动的时候，需要将之前的log文件压缩成zip，并发送给服务器
 * <p>
 * <p>
 * 待完善部分：
 * 由于网络接口，后台没有，如果以后有需要的时候，只要更改上传文件的逻辑即可，代码中 T O D O 部分
 * 没有经过测试，可能代码有部分问题，等需要的时候可以测试看看
 * <p>
 * Created by bauer_bao on 17/3/16.
 */
public class LogTrackerUtil {
    /**
     * 正常的追踪日志
     */
    public static final String TRACKER = "T";

    /**
     * 警告
     */
    public static final String WARNING = "W";

    /**
     * 错误
     */
    public static final String ERROR = "E";

    private static LogTrackerUtil instance = null;

    private Context mContext;

    private ExecutorService singleThreadExecutor;

    /**
     * 当前写入的文件名
     */
    private String logFileName;

    /**
     * 需要上传的压缩包名
     */
    private String zipFileName;

    private int sameFileCount;

    private String time;

    /**
     * 用于格式化日期,作为日志文件名的一部分
     */
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 待记录的列表
     */
    private ArrayList<LogInfo> pendingLogList = new ArrayList<>();

    /**
     * 正在写入的列表
     */
    private ArrayList<LogInfo> writingLogList;

    /**
     * list中最多存的条数，超过此数量，则需要将list中的内容写到文件中去
     */
    private static final int LIMIT_LOG_COUNT = 100;

    /**
     * 1.5M
     */
    private static final int LIMIT_FILE_SIZE = 3 * 1024 * 1024 / 2;

    /**
     * 获取单例
     *
     * @param context
     * @return
     */
    public static LogTrackerUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (LogTrackerUtil.class) {
                if (instance == null) {
                    instance = new LogTrackerUtil(context);
                }
            }
        }
        return instance;
    }

    public LogTrackerUtil(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * 需要在application中初始化
     */
    public void init() {
        sameFileCount = getSameLogFileCount();
        singleThreadExecutor = Executors.newSingleThreadExecutor();
        /**
         * 1.将之前的log文件压缩（并非是zip文件，也可以把zip也一起压缩，这样可以保证每次上传log文件，一直只有一个文件）
         * 2.上传至服务器（可能有多个zip文件）
         * 3.如果成功，把对应文件删除
         * 4.如果失败，下次开启的时候接着上传
         */

        zipFileName = time + System.currentTimeMillis() + ".zip";
        Observable.just(zipFileName)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<String, Observable<JSONObject>>() {
                    @Override
                    public Observable<JSONObject> call(String s) {
                        //1.压缩文件
                        ArrayList<String> logList = getZipLogFiles();
                        ZipUtil.zip(s, logList);
                        //1-1.将压缩之前的文件删除，防止重复压缩
                        for (String logFile : logList) {
                            File file = new File(logFile);
                            if (file.exists()) {
                                file.delete();
                            }
                        }

                        //2. 只要使用项目中的网络请求框架即可，因为目前服务器没有对应的接口，没有上传的实际代码
                        // TODO: 17/7/20
                        //http.uploadlog(s);
                        return null;
                    }
                })
                .compose(((RxAppCompatActivity) mContext).<JSONObject>bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        //3.上传成功，删除log文件
                        File dir = new File(zipFileName);
                        if (dir.exists()) {
                            dir.delete();
                        }
                    }

                    @Override
                    public void _onError(int status) {
                        //4.上传失败，不做任何处理
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    /**
     * 添加要记录的日志
     *
     * @param logLevel
     * @param msg
     */
    public void addTrackMsg(String logLevel, String tag, String msg) {
        PictureAirLog.d(tag, msg);
        LogInfo logInfo = new LogInfo();
        logInfo.setLogLevel(logLevel);
        logInfo.setLogTag(tag);
        logInfo.setLogMsg(msg);
        logInfo.setLogTime(System.currentTimeMillis());
        pendingLogList.add(logInfo);

        if (pendingLogList.size() > LIMIT_LOG_COUNT) {//超过数量，需要开始写入文件
            writingLogList = new ArrayList<>();
            writingLogList.addAll(pendingLogList);
            pendingLogList.clear();
            writeToFile(false, writingLogList);
        }
    }

    /**
     * 记录日志到文件
     *
     * @param shutDownNow 是否需要处理完之后关闭
     */
    private void writeToFile(final boolean shutDownNow, ArrayList<LogInfo> list) {
        if (singleThreadExecutor != null && !singleThreadExecutor.isShutdown()) {
            singleThreadExecutor.execute(new RunnableTask(shutDownNow, list));
        }
    }

    /**
     * 结束的时候，需要释放资源，并且之前需要将list中剩余的数据写入文件中
     * 1.记录日志
     * 2.释放资源
     * <p>
     * 在app整个退出的时候，或者app奔溃的时候，需要做这一步操作
     */
    public void release() {
        writingLogList = new ArrayList<>();
        writingLogList.addAll(pendingLogList);
        pendingLogList.clear();
        writeToFile(true, writingLogList);
    }

    private class RunnableTask implements Runnable {
        private boolean shutDown;
        private ArrayList<LogInfo> list;

        public RunnableTask(boolean shutDown, ArrayList<LogInfo> list) {
            this.shutDown = shutDown;
            this.list = list;
        }

        @Override
        public void run() {
            //每次写入的时候，都要重新获取log的文件名，防止写的时候，文件超过了指定大小
            getLogFileName();

            /**
             * 循环list列表，依次将数据写入
             */
            try {
                FileOutputStream fos = new FileOutputStream(Common.LOG_PATH + logFileName);
                for (LogInfo log : list) {
                    fos.write(log.toString().getBytes());
                }
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (shutDown) {
                logFileName = "";
                sameFileCount = 0;
                pendingLogList.clear();
                writingLogList.clear();
                if (singleThreadExecutor != null && !singleThreadExecutor.isShutdown()) {
                    singleThreadExecutor.shutdownNow();
                }
            }
        }
    }

    /**
     * 判断原文件是否超过规定大小
     */
    private void getLogFileName() {
        if (sameFileCount != 0) {//说明文件存在，取最新的一个文件
            logFileName = time + "(" + sameFileCount + ").log";
            File file = new File(Common.LOG_PATH + logFileName);
            if (file.length() > LIMIT_FILE_SIZE) {//文件大小已经超过规定大小，需要新建文件，否则，直接使用
                sameFileCount++;
                //新建文件
                logFileName = time + "(" + sameFileCount + ").log";
                createNewFile(logFileName);
            }

        } else {//说明文件不存在，创建新的文件
            sameFileCount++;
            logFileName = time + "(" + sameFileCount + ").log";
            createNewFile(logFileName);
        }
    }

    /**
     * 压缩之前的log
     */
    private ArrayList<String> getZipLogFiles() {
        ArrayList<String> resultList = new ArrayList<>();
        File dir = new File(Common.LOG_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        for (String fileStr : dir.list()) {//遍历文件夹，获取不是同一天的文件
            if (!fileStr.contains(time)) {
                resultList.add(fileStr);
            }
        }
        return resultList;
    }

    /**
     * 创建新的文件
     *
     * @param fileName
     */
    private void createNewFile(String fileName) {
        Map<String, String> infos;
        FileOutputStream fos;
        try {
            //收集设备参数信息
            infos = AppUtil.collectDeviceInfo(mContext);
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : infos.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                sb.append(key).append("=").append(value).append("\n");
            }
            fos = new FileOutputStream(Common.LOG_PATH + fileName);
            fos.write(sb.toString().getBytes());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取同一天的log文件数量
     *
     * @return
     */
    private int getSameLogFileCount() {
        File dir = new File(Common.LOG_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        time = formatter.format(new Date());
        ArrayList<String> oldFileList = new ArrayList<>();
        for (String fileStr : dir.list()) {//遍历文件夹，获取同一天的文件
            if (fileStr.contains(time)) {
                oldFileList.add(fileStr);
            }
        }
        return oldFileList.size();
    }

    /**
     * log实体类
     */
    public class LogInfo {
        /**
         * log等级，有T,W,E
         */
        private String logLevel;

        /**
         * log记录当前时间，以毫秒为单位
         */
        private long logTime;

        /**
         * 当前执行的方法
         */
        private String logTag;

        /**
         * 具体备注
         */
        private String logMsg;

        public LogInfo() {
        }

        public String getLogLevel() {
            return logLevel;
        }

        public void setLogLevel(String logLevel) {
            this.logLevel = logLevel;
        }

        public String getLogMsg() {
            return logMsg;
        }

        public void setLogMsg(String logMsg) {
            this.logMsg = logMsg;
        }

        public long getLogTime() {
            return logTime;
        }

        public void setLogTime(long logTime) {
            this.logTime = logTime;
        }

        public String getLogTag() {
            return logTag;
        }

        public void setLogTag(String logTag) {
            this.logTag = logTag;
        }

        @Override
        public String toString() {
            return "LogInfo{" +
                    "logTime=" + logTime +
                    ", logLevel='" + logLevel + '\'' +
                    ", logTag='" + logTag + '\'' +
                    ", logMsg='" + logMsg + '\'' +
                    '}' + "\n";
        }
    }

}