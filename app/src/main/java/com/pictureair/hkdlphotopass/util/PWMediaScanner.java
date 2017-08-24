package com.pictureair.hkdlphotopass.util;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;


/**
 * Created by tech-beyondren on 16/10/8.
 */
public class PWMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {
    private MediaScannerConnection msc;
    private String path, mimeType;
    private ScannerListener mListener;

    public interface ScannerListener{
        void OnScannerFinish(String path);
    }

    public PWMediaScanner (Context context, String file, String mimeType, ScannerListener listener){
        mListener = listener;
        path = file;
        this.mimeType = mimeType;
        msc = new MediaScannerConnection(context, this);
        msc.connect();
    }

    @Override
    public void onMediaScannerConnected() {
        msc.scanFile(path, mimeType);
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        msc.disconnect();
        mListener.OnScannerFinish(path);
    }
}
