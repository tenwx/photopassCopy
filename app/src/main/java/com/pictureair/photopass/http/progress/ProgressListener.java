package com.pictureair.photopass.http.progress;

/**
 * Created by pengwu on 16/7/5.
 */
public interface ProgressListener {

    void onProgress(long progress, long total, boolean done);
}
