package com.pictureair.photopass.http;

import com.pictureair.photopass.util.PictureAirLog;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 管理下载中的task
 * Created by pengwu on 16/10/11.
 */

public class CallTaskManager {

    private static CallTaskManager instance;
    private CopyOnWriteArrayList<ACallTask> callList;
    private static final String TAG = "CallTaskManager";

    private CallTaskManager() {
        callList = new CopyOnWriteArrayList<ACallTask>();
    }

    public static CallTaskManager getInstance() {
        if (instance == null) {
            instance = new CallTaskManager();
        }
        return instance;
    }

    public void addTask(ACallTask task) {
        if (callList == null || task == null) return;
        PictureAirLog.v(TAG,"addTask(): "+task.toString());
        callList.add(task);

    }

    public void removeTask(ACallTask task) {
        if (callList == null || task == null) return;
        PictureAirLog.v(TAG,"removeTask(): "+task.toString());
        callList.remove(task);
    }

    public boolean containsTask(ACallTask task) {
        if (callList == null || callList.size() == 0 || task == null) return false;
        PictureAirLog.v(TAG,"containsTask(): "+task.toString());
        return callList.contains(task);
    }

    public void cancleAllTask() {
        if (callList == null || callList.size() == 0) return;
        Iterator<ACallTask> iterator = callList.iterator();
        while (iterator.hasNext()) {
            ACallTask task = iterator.next();
            PictureAirLog.v(TAG,"cancleAllTask(): "+task.toString());
            if (task != null) {
                task.cancle();
            }
        }
    }

    public void clearAllTask() {
        if (callList == null) return;
        callList.clear();
    }
}
