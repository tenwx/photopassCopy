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
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.pictureair.jni.ciphermanager.PWJniUtil;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.AESKeyHelper;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.BlurUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.GlideUtil;
import com.pictureair.photopass.util.HttpCallback;
import com.pictureair.photopass.util.HttpUtil1;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.util.ScreenUtil;

import java.io.File;

public class UrlTouchImageView extends RelativeLayout implements TouchImageView.OnTouchClearListener {
    protected TouchImageView mImageView;
    protected ImageView progressImageView, videoPlayImageView, touchClearImageView;

    protected Context mContext;

    private int defaultType;

    //模糊
    private File dirFile;
    private Bitmap oriClearBmp = null;// 原清晰图
    private Bitmap oriBlurBmp = null;// 原模糊图
    private Bitmap maskBmp = null;// 遮罩层
    private Bitmap touchClearBmp = null;// touch clear 圈
    private int touchClearRadius = 0;//圈的半径
    private float lastScale = 0;//记录上次的缩放尺寸
    private Bitmap currentBmp = null;//当前的imageview的清晰图

    private PhotoEventListener photoEventListener;

    private static final int LOAD_FILE_FAILED = 1;
    private static final int LOAD_FROM_LOCAL = 444;
    private static final int GET_BMP_COMPLTETION = 555;
    private static final String TAG = UrlTouchImageView.class.getSimpleName();

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_FILE_FAILED://加载失败
                    Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), (defaultType == 0) ? R.drawable.ic_failed : R.drawable.preview_error);
                    mImageView.setVisibility(VISIBLE);
                    mImageView.setScaleType(ScaleType.CENTER);
                    mImageView.setImageBitmap(bitmap);//如果这里直接用setImageResource，导致没有左右滑动
                    progressImageView.setVisibility(GONE);
                    break;

                case GET_BMP_COMPLTETION:
                    //添加模糊
                    if (null != oriClearBmp) {
                        showBlurView();
                    } else {
                        handler.sendEmptyMessage(LOAD_FILE_FAILED);
                    }
                    break;

                case LOAD_FROM_LOCAL:
                    new Thread() {
                        @Override
                        public void run() {
                            byte[] arg2 = null;
                            try {
                                arg2 = AESKeyHelper.decrypt(dirFile.toString(), PWJniUtil.getAESKey(Common.APP_TYPE_SHDRPP, Common.OFFSET));
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            if (null != arg2)
                                oriClearBmp = BitmapFactory.decodeByteArray(arg2, 0, arg2.length);

                            createBlurBmp();

                            handler.sendEmptyMessage(GET_BMP_COMPLTETION);
                            super.run();
                        }
                    }.start();
                    break;

                default:
                    break;
            }
            return false;
        }
    });

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
        //设置图片
        mImageView = new TouchImageView(mContext);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mImageView.setLayoutParams(params);
        this.addView(mImageView);
        mImageView.setVisibility(GONE);

        //设置进度条
        progressImageView = new ImageView(mContext);

        int screenW = ScreenUtil.getPortraitScreenWidth(mContext);
        params = new LayoutParams(screenW / 3, screenW / 3);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressImageView.setLayoutParams(params);
        GlideUtil.load(mContext, GlideUtil.getDrawableUrl(mContext, R.drawable.loading_0), progressImageView);
        this.addView(progressImageView);

        //设置视频播放按钮
        videoPlayImageView = new ImageView(mContext);
        params = new LayoutParams(screenW / 6, screenW / 6);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        videoPlayImageView.setLayoutParams(params);
        videoPlayImageView.setImageResource(R.drawable.play);
        this.addView(videoPlayImageView);
        videoPlayImageView.setVisibility(GONE);
    }

    /**
     * 显示视频图标，并且需要回调点击事件
     */
    public void setVideoType(final int position, final PhotoEventListener photoEventListener) {
        videoPlayImageView.setVisibility(VISIBLE);
        videoPlayImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PictureAirLog.out("start play video ---> onclick" + position);
                photoEventListener.videoClick(position);
            }
        });
    }

    /**
     * 设置监听
     * @param photoEventListener
     */
    public void setOnPhotoEventListener(PhotoEventListener photoEventListener) {
        this.photoEventListener = photoEventListener;
    }

    public void disableZoom() {
        mImageView.disableZoom();
    }

    /**
     * 设置图片的url
     *
     * @param imageUrl 网络图片路径
     */
    public void setUrl(String imageUrl, boolean isEncrypted) {
        //使用imageloader加载图片
        GlideUtil.load(mContext, imageUrl, isEncrypted, new SimpleTarget<Bitmap>() {
            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                super.onLoadFailed(e, errorDrawable);
                handler.sendEmptyMessage(LOAD_FILE_FAILED);
            }

            @Override
            public void onResourceReady(Bitmap loadedImage, GlideAnimation<? super Bitmap> glideAnimation) {
                GlideUtil.load(mContext, GlideUtil.getDrawableUrl(mContext, getImageResource(100)), progressImageView);
                mImageView.setVisibility(VISIBLE);
                mImageView.setScaleType(ScaleType.MATRIX);
                mImageView.setImageBitmap(loadedImage);
                progressImageView.setVisibility(GONE);
            }
        });
    }

    /**
     * 设置本地图片的路径
     *
     * @param imagePath 本地路径
     */
    public void setImagePath(String imagePath) {
        //为什么设置800，800，本地图片尽量不需要去加载原图，容易OOM，因此只要加载特定尺寸即可。
        //测试发现，800，800，不是最终的bitmap大小，而是和原图相比缩小了1倍，需要继续验证。
        GlideUtil.load(mContext, GlideUtil.getFileUrl(imagePath), 800, 800, defaultType == 1,//defauleType为1，说明从下载页面进入，此时不需要缓存机制
                new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String s, Target<Bitmap> target, boolean b) {
                        handler.sendEmptyMessage(LOAD_FILE_FAILED);//此处必须抛出去，不然图片无法显示
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap bitmap, String s, Target<Bitmap> target, boolean b, boolean b1) {
                        PictureAirLog.out("width--->" + bitmap.getWidth() + "===" + bitmap.getHeight());
                        GlideUtil.load(mContext, GlideUtil.getDrawableUrl(mContext, getImageResource(100)), progressImageView);
                        progressImageView.setVisibility(GONE);
                        mImageView.setScaleType(ScaleType.MATRIX);
                        mImageView.setVisibility(VISIBLE);
                        return false;
                    }
                }, mImageView);
    }

    /**
     * 设置blur url
     * @param blurImageUrl
     */
    public void setBlurImageUrl(String blurImageUrl, String photoId) {
        dirFile = new File(getContext().getApplicationContext().getCacheDir() + "/" + photoId + Common.OFFSET);//创建一个以ID为名字的文件，放入到app缓存文件下

        PictureAirLog.v(TAG, dirFile.toString());
        PictureAirLog.v(TAG, "photo URL ------->" + blurImageUrl);
        if (dirFile.exists()) {//如果文件存在
            PictureAirLog.v(TAG, "file exists");
            handler.sendEmptyMessageDelayed(LOAD_FROM_LOCAL, 200);
        } else {//如果文件不存在，下载文件到缓存
            PictureAirLog.v(TAG, "file is not exist");
            HttpUtil1.asyncDownloadBinaryData(blurImageUrl, new HttpCallback() {
                @Override
                public void onSuccess(byte[] binaryData) {
                    super.onSuccess(binaryData);
                    byte[] data = AppUtil.getRealByte(binaryData);
                    if (data == null) {
                        data = binaryData;
                    }
                    try {
                        AESKeyHelper.encrypt(data, dirFile.toString(), PWJniUtil.getAESKey(Common.APP_TYPE_SHDRPP, Common.OFFSET));
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    oriClearBmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                    createBlurBmp();
                    handler.sendEmptyMessage(GET_BMP_COMPLTETION);
                }

                @Override
                public void onFailure(int status) {
                    super.onFailure(status);
                    handler.sendEmptyMessage(GET_BMP_COMPLTETION);
                }
            });
        }
    }

    /**
     * 创建模糊图
     */
    private void createBlurBmp() {
        if (oriClearBmp != null) {
            PictureAirLog.v(TAG, "ori clear bitmap" + oriClearBmp.getWidth() + "----" + oriClearBmp.getHeight());
            maskBmp = BitmapFactory.decodeResource(getResources(), R.drawable.round_meitu_1);
            oriBlurBmp = BlurUtil.blur(oriClearBmp);//添加模糊度
        }
    }

    /**
     * 根据照片的购买情况确定布局和显示模式
     */
    private void showBlurView() {
        GlideUtil.load(mContext, GlideUtil.getDrawableUrl(mContext, getImageResource(100)), progressImageView);
        mImageView.setVisibility(VISIBLE);
        mImageView.setScaleType(ScaleType.MATRIX);
        mImageView.setImageBitmap(oriBlurBmp);
        mImageView.setOnTouchClearListener(this);
        mImageView.setDrawingCacheEnabled(true);
        setProgressImageViewVisible(false);

        touchClearImageView = new ImageView(mContext);
        this.addView(touchClearImageView);
        touchClearImageView.setVisibility(GONE);
        touchClearRadius = ScreenUtil.dip2px(mContext, 45);
    }

    /**
     * 设置进度条可见
     * @param visible
     */
    public void setProgressImageViewVisible(boolean visible) {
        progressImageView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setScaleType(ScaleType scaleType) {
        mImageView.setScaleType(scaleType);
    }

    /**
     * 根据当前的下载percent来显示对应的图片，实现下载进度条的功能
     *
     * @param currentProgress
     * @return
     */
    private int getImageResource(long currentProgress) {
        return ReflectionUtil.getDrawableId(mContext, "loading_" + (currentProgress / 8));
    }

    public void setDefaultType(int defaultType) {
        this.defaultType = defaultType;
    }

    @Override
    public void onTouchClear(float positionX, float positionY, int matrixX, int matrixY, float scale, boolean hasReset, boolean visible) {
        photoEventListener.touchClear(visible);
        if (visible) {
            if (lastScale != scale || hasReset) {//缩放尺寸不一样，或者旋转过手机图片被重置过，需要重新获取对应的bitmap
                PictureAirLog.out("touch need new bmp");
                lastScale = scale;
                currentBmp = BlurUtil.zoomClearBmp(oriClearBmp, scale, mImageView.getWidth(), mImageView.getHeight());
            }

            if (currentBmp == null) return;

            Point point = BlurUtil.getStartCropPoint(positionX, positionY, currentBmp.getWidth(), currentBmp.getHeight(), touchClearRadius, matrixX, matrixY);

            touchClearBmp = Bitmap.createBitmap(currentBmp, (point.x - matrixX), (point.y - matrixY), 2 * touchClearRadius, 2 * touchClearRadius);
            touchClearBmp = BlurUtil.doMask(touchClearBmp, maskBmp);
            if (!touchClearImageView.isShown()) {
                touchClearImageView.setVisibility(VISIBLE);
            }
            touchClearImageView.setX(point.x);
            touchClearImageView.setY(point.y);
            touchClearImageView.setImageBitmap(touchClearBmp);
        } else {
            touchClearImageView.setVisibility(GONE);
        }
    }
}
