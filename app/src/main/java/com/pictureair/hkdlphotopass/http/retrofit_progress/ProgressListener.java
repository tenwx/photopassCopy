package com.pictureair.photopass.http.retrofit_progress;

public interface ProgressListener {

   void update(long bytesRead, long contentLength);
}
