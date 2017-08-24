package com.pictureair.hkdlphotopass.http.rxhttp;

/**
 * Created by milo on 15/12/7.
 * 定义Http 操作回调接口
 */
public interface HttpInterface {

    /**
     * 上传进度
     *
     * @param bytesWritten long
     * @param totalSize    long
     */
    void onProgress(long bytesWritten, long totalSize);

    void doOnSubscribe();
}
