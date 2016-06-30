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
package com.pictureair.photopass.GalleryWidget;

import android.content.Context;
import android.view.ViewGroup;

import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.PictureAirLog;

import java.util.List;

public class UrlPagerAdapter extends BasePagerAdapter {

    public UrlPagerAdapter(Context context, List<PhotoInfo> resources) {
        super(context, resources);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        ((GalleryViewPager) container).mCurrentView = ((UrlTouchImageView) object).getImageView();
    }

    @Override
    public Object instantiateItem(ViewGroup collection, final int position) {
        final UrlTouchImageView iv = new UrlTouchImageView(mContext);
        if (mResources.get(position).onLine == 1 && mResources.get(position).isPayed == 1) {
            PictureAirLog.v("UrlPagerAdapter", "online and ispayed : " + position);
            iv.setProgressImageViewVisible(true);
            iv.setUrl(mResources.get(position).photoThumbnail_1024);
        } else if (mResources.get(position).onLine == 0) {

            PictureAirLog.v("instantiateItem", "local photo : " + position + position);
            iv.setProgressImageViewVisible(true);
            iv.setImagePath(mResources.get(position).photoPathOrURL);
        } else {
            iv.setProgressImageViewVisible(false);
        }
        iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        collection.addView(iv, 0);
        return iv;
    }
}
