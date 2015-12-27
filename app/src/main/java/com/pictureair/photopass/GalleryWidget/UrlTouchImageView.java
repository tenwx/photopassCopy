/*
 Copyright (c) 2012 Roman Truba

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial
 portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.pictureair.photopass.GalleryWidget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

import com.loopj.android.http.BinaryHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.pictureair.photopass.GalleryWidget.InputStreamWrapper.InputStreamProgressListener;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.AESKeyHelper;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.HttpUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

import cz.msebera.android.httpclient.Header;


public class UrlTouchImageView extends RelativeLayout {
    //	protected ProgressBar mProgressBar;
    protected TouchImageView mImageView;
    protected ImageView progressImageView;

    protected Context mContext;

    protected File dirfile;

    private Bitmap bitmap;
    private ImageLoader imageLoader;

    private static final int LOAD_FILE_DONE = 1;

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case LOAD_FILE_DONE:
                    if (bitmap != null) {
                        PictureAirLog.out("LOAD_FILE_DONE: " + "bitmap != null");
                        mImageView.setScaleType(ScaleType.MATRIX);
                        mImageView.setImageBitmap(bitmap);
                    } else {
                        PictureAirLog.out("LOAD_FILE_DONE: " + "bitmap == null");
                        mImageView.setScaleType(ScaleType.CENTER);
                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_failed);
                        mImageView.setImageBitmap(bitmap);
                    }
                    progressImageView.setImageResource(R.drawable.loading_12);
                    //			 mProgressBar.setProgress(100);
                    mImageView.setVisibility(VISIBLE);
                    progressImageView.setVisibility(GONE);
                    break;

                default:
                    break;
            }
        }

        ;
    };

    public UrlTouchImageView(Context ctx) {
        super(ctx);
        mContext = ctx;
        init();

    }

    public UrlTouchImageView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        mContext = ctx;
        init();
    }

    public TouchImageView getImageView() {
        return mImageView;
    }

    protected void init() {
        mImageView = new TouchImageView(mContext);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mImageView.setLayoutParams(params);
        this.addView(mImageView);
        mImageView.setVisibility(GONE);
        imageLoader = ImageLoader.getInstance();

        progressImageView = new ImageView(mContext);
        //		mProgressBar = new ProgressBar(mContext, null, android.R.attr.progressBarStyleHorizontal);
        params = new LayoutParams(ScreenUtil.getScreenWidth(mContext) / 3, ScreenUtil.getScreenWidth(mContext) / 3);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        //		params.setMargins(30, 0, 30, 0);
        //		mProgressBar.setLayoutParams(params);
        //		mProgressBar.setIndeterminate(false);
        //		mProgressBar.setMax(100);
        progressImageView.setLayoutParams(params);
        progressImageView.setImageResource(R.drawable.loading_0);
        this.addView(progressImageView);
    }

    /**
     * 设置图片的url
     *
     * @param imageUrl 网络图片路径
     * @param photoId  photoId
     */
    public void setUrl(String imageUrl, String photoId) {
        /**
         * 一个三级缓存的操纵
         * 1.判断文件是否已经存在于sd卡中
         * 2.判断文件是否在缓存中
         * 3.从网络获取，获取的途中，需要将文件存放于缓存中
         */
        //1.获取需要显示文件的文件名
        String fileString = ScreenUtil.getReallyFileName(imageUrl);
        //2、判断文件是否存在sd卡中
        File file = new File(Common.PHOTO_DOWNLOAD_PATH + fileString);
        if (file.exists()) {//3、如果存在SD卡，则从SD卡获取图片信息
            PictureAirLog.out("file in sd card");
//			final File file2 = file;
            //使用imageloader加载图片
            imageLoader.loadImage("file://" + file.toString(), new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view,
                                              Bitmap loadedImage) {
                    // TODO Auto-generated method stub
                    super.onLoadingComplete(imageUri, view, loadedImage);
                    bitmap = loadedImage;
                    handler.sendEmptyMessage(LOAD_FILE_DONE);

                }
            });
//			new Thread(){
//				public void run() {
//					
//					bitmap = BitmapFactory.decodeFile(file2.toString());
//				};
//			}.start();


        } else {//4、如果SD卡不存在，判断是否在缓存中
            PictureAirLog.out("file not in sd card");
            dirfile = new File(mContext.getCacheDir() + "/" + photoId);
            if (dirfile.exists()) {//5、如果缓存存在，则从缓存中获取图片信息
                PictureAirLog.out("file in cache");
                new Thread(){
                    public void run() {
                        byte[] arg2 = null;
                        try {
                            arg2 = AESKeyHelper.decrypt(dirfile.toString(), Common.AES_ENCRYPTION_KEY);
                        } catch (InvalidKeyException | NoSuchAlgorithmException
                                | NoSuchPaddingException | IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        Options options = new Options();
                        options.inSampleSize = 2;//缩小为原来的1/2
                        bitmap = BitmapFactory.decodeByteArray(arg2, 0, arg2.length, options);
                        handler.sendEmptyMessage(LOAD_FILE_DONE);
                    };
                }.start();

            } else {//6.如果缓存不存在，从网络获取图片信息，
                PictureAirLog.out("file not in cache and load from network");
                //				new ImageLoadTask().execute(imageUrl);
                loadImage(imageUrl);
            }
        }
    }


    public void setScaleType(ScaleType scaleType) {
        mImageView.setScaleType(scaleType);
    }

    private void loadImage(String url) {
        // TODO Auto-generated method stub

        HttpUtil.get(url, new BinaryHttpResponseHandler() {
            @Override
            public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(arg2, 0, arg2.length);
                //7.将网络获取的图片信息存放到缓存
                BufferedOutputStream stream = null;
                try {
                    System.out.println(dirfile.toString());
                    FileOutputStream fsStream = new FileOutputStream(dirfile);
                    stream = new BufferedOutputStream(fsStream);
                    stream.write(arg2);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    try {
                        if (stream != null) {
                            stream.flush();
                            stream.close();
                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    System.out.println("done");
                }

                if (bitmap != null) {
                    mImageView.setScaleType(ScaleType.MATRIX);
                    mImageView.setImageBitmap(bitmap);
                } else {
                    mImageView.setScaleType(ScaleType.CENTER);
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_failed);
                    mImageView.setImageBitmap(bitmap);
                }
                mImageView.setVisibility(VISIBLE);
                progressImageView.setVisibility(GONE);
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                System.out.println(arg3.toString());
                mImageView.setScaleType(ScaleType.CENTER);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_failed);
                mImageView.setImageBitmap(bitmap);
                mImageView.setVisibility(VISIBLE);
                progressImageView.setVisibility(GONE);
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                super.onProgress(bytesWritten, totalSize);
                progressImageView.setImageResource(getImageResource(bytesWritten * 100 / totalSize));
            }

        });
    }

    /**
     * 设置本地图片的路径
     *
     * @param imagePath 本地路径
     */
    public void setImagePath(String imagePath) {
        new ImageLoadTask().execute(imagePath);
    }

    public class ImageLoadTask extends AsyncTask<String, Integer, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            String path = strings[0];
            Bitmap bm = null;
            try {
                File file = new File(path);
                FileInputStream fis = new FileInputStream(file);
                InputStreamWrapper bis = new InputStreamWrapper(fis, 8192, file.length());
                bis.setProgressListener(new InputStreamProgressListener() {
                    @Override
                    public void onProgress(float progressValue, long bytesLoaded,
                                           long bytesTotal) {
                        publishProgress((int) (progressValue * 100));
                    }
                });
                bm = BitmapFactory.decodeStream(bis);
                System.out.println("bitmap size w" + bm.getWidth() + "_" + bm.getHeight());
                int width = bm.getWidth();
                int height = bm.getHeight();
                System.out.println("bitmap size w" + width + "_" + height);
                bis.close();

                Matrix m = new Matrix();
                m.reset();
                //如果图片过大，不能显示，要么把硬件加速关闭，要么缩小预览尺寸
                if (width >= height && (width > 2400 || height > 1800)) {//如果是图片是横着的，
                    m.postScale((float) 2400 / width, (float) 2400 / width);
                    bm = Bitmap.createBitmap(bm, 0, 0, width, height, m, true);
                    System.out.println("------> need zoom");
                } else if (width < height && (width > 1800 || height > 2400)) {//如果图片是竖着的
                    m.postScale((float) 1800 / height, (float) 1800 / height);
                    bm = Bitmap.createBitmap(bm, 0, 0, width, height, m, true);
                    System.out.println("------> need zoom");
                } else {

                    System.out.println("------> need not zoom");
                }
                System.out.println("bitmap size w" + bm.getWidth() + "_" + bm.getHeight());
                System.out.println("--------> load success");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (AppUtil.getExifOrientation(path) != 0) {
                bm = AppUtil.rotaingImageView(AppUtil.getExifOrientation(path), bm);
            }
            return bm;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap == null) {
                mImageView.setScaleType(ScaleType.CENTER);
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_failed);
                mImageView.setImageBitmap(bitmap);
            } else {
                mImageView.setScaleType(ScaleType.MATRIX);
                mImageView.setImageBitmap(bitmap);
            }

            mImageView.setVisibility(VISIBLE);
            progressImageView.setVisibility(GONE);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressImageView.setImageResource(getImageResource(values[0]));
            //			 mProgressBar.setProgress(values[0]);
        }
    }

    /**
     * 根据当前的下载percent来显示对应的图片，实现下载进度条的功能
     *
     * @param currentProgress
     * @return
     */
    private int getImageResource(long currentProgress) {
        int result = R.drawable.loading_0;
        if (currentProgress >= 0 && currentProgress <= 8) {
            result = R.drawable.loading_0;
        } else if (currentProgress > 8 && currentProgress <= 16) {
            result = R.drawable.loading_1;
        } else if (currentProgress > 16 && currentProgress <= 25) {
            result = R.drawable.loading_2;
        } else if (currentProgress > 25 && currentProgress <= 33) {
            result = R.drawable.loading_3;
        } else if (currentProgress > 33 && currentProgress <= 41) {
            result = R.drawable.loading_4;
        } else if (currentProgress > 41 && currentProgress <= 50) {
            result = R.drawable.loading_5;
        } else if (currentProgress > 50 && currentProgress <= 58) {
            result = R.drawable.loading_6;
        } else if (currentProgress > 58 && currentProgress <= 66) {
            result = R.drawable.loading_7;
        } else if (currentProgress > 66 && currentProgress <= 75) {
            result = R.drawable.loading_8;
        } else if (currentProgress > 75 && currentProgress <= 83) {
            result = R.drawable.loading_9;
        } else if (currentProgress > 83 && currentProgress <= 91) {
            result = R.drawable.loading_10;
        } else if (currentProgress > 91 && currentProgress < 100) {
            result = R.drawable.loading_11;
        } else {
            result = R.drawable.loading_12;
        }

        return result;
    }
}
