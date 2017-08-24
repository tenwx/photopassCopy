/*
 Copyright (c) 2013 Roman Truba

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
package com.pictureair.hkdlphotopass.GalleryWidget;

import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.pictureair.hkdlphotopass.R;
import com.pictureair.hkdlphotopass.entity.PhotoInfo;
import com.pictureair.hkdlphotopass.util.AppUtil;
import com.pictureair.hkdlphotopass.util.Common;
import com.pictureair.hkdlphotopass.util.PictureAirLog;

import java.io.File;
import java.util.List;

public class UrlPagerAdapter extends BasePagerAdapter {

    private int defaultType;
    private PhotoEventListener photoEventListener;
    private boolean fullScreenMode = false;

    /**
     * 卡片模式
     */
    private boolean cardMode;

    public UrlPagerAdapter(Context context,List<PhotoInfo> resources){
        super(context, resources);
        this.defaultType = 0;
    }
    public UrlPagerAdapter(Context context, List<PhotoInfo> resources, int defaultType, boolean cardMode) {
        super(context, resources);
        this.defaultType = defaultType;
        this.cardMode = cardMode;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        ((GalleryViewPager) container).mCurrentView = ((UrlTouchImageView) object).getImageView();
    }

    @Override
    public Object instantiateItem(ViewGroup collection, final int position) {
        PhotoInfo photoInfo = mResources.get(position);
        UrlTouchImageView iv = new UrlTouchImageView(mContext, photoInfo.getIsPaid(), position, cardMode);
        iv.setDefaultType(defaultType);
        if (photoInfo.getIsOnLine() == 1 && photoInfo.getIsPaid() == 1) {//网络图
            iv.setProgressImageViewVisible(true);
            if (photoInfo.getIsVideo() == 0) {//照片
                //1.获取需要显示文件的文件名
                String fileString = AppUtil.getReallyFileName(photoInfo.getPhotoThumbnail_1024(), 0);
                //2、判断文件是否存在sd卡中
                File file = new File(Common.PHOTO_DOWNLOAD_PATH + fileString);
                if (file.exists()) {//3、如果存在SD卡，则从SD卡获取图片信息
                    PictureAirLog.out("file in sd card");
                    iv.setImagePath(file.toString());

                } else {
                    PictureAirLog.v("UrlPagerAdapter", "online and ispayed : " + position);
                    iv.setUrl(photoInfo.getPhotoThumbnail_1024(), AppUtil.isEncrypted(photoInfo.getIsEnImage()));
                }
            } else {//视频
                PictureAirLog.out("show video info");
                iv.setUrl(Common.PHOTO_URL + photoInfo.getPhotoThumbnail_512(), AppUtil.isEncrypted(photoInfo.getIsEnImage()));
                iv.disableZoom();
                iv.setVideoType(photoEventListener);
            }

        } else if (photoInfo.getIsOnLine() == 0) {//本地图
            if (photoInfo.getIsVideo() == 0) {//照片
                PictureAirLog.out("url---->" + photoInfo.getPhotoOriginalURL());
                PictureAirLog.v("instantiateItem", "local photo : " + position + position);
                iv.setProgressImageViewVisible(true);
                iv.setImagePath(photoInfo.getPhotoOriginalURL());
            }else{//视频
                iv.setUrl(Common.PHOTO_URL + photoInfo.getPhotoThumbnail_512(), AppUtil.isEncrypted(photoInfo.getIsEnImage()));
                iv.disableZoom();
                iv.setVideoType(photoEventListener);
            }

        } else {//模糊图
            iv.setBlurImageUrl(photoInfo.getPhotoThumbnail_1024(), photoInfo.getPhotoId());
            iv.setProgressImageViewVisible(true);
        }
        iv.setOnPhotoEventListener(photoEventListener);
        iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        iv.setId(position);
        if (cardMode) {
            iv.setTimeText(photoInfo.getShootDate() + String.format(mContext.getString(R.string.gallery_expire_time),
                    TextUtils.isEmpty(photoInfo.getExipreDate()) ?
                            AppUtil.getNewExpiredTime(photoInfo, photoInfo.getIsPaid() == 1 ? 60 : 30) :
                            photoInfo.getExipreDate()));//如果数据库没有获取到时间，需要给他默认的时间
            iv.setFullScreenMode(fullScreenMode);
        }
        collection.addView(iv, 0);
        return iv;
    }

    public void setOnPhotoEventListener(PhotoEventListener listener) {
        photoEventListener = listener;
    }

    public void setFullScreenMode(boolean fullScreenMode) {
        this.fullScreenMode = fullScreenMode;
    }
}
