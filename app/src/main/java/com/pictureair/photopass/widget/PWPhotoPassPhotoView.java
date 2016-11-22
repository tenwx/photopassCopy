package com.pictureair.photopass.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.util.GlideUtil;
import com.pictureair.photopass.util.ScreenUtil;

import java.util.HashMap;

/**
 * Created by bauer_bao on 16/9/1.
 *
 * pp管理页中，每个框就是一个自定义控件
 */
public class PWPhotoPassPhotoView extends RelativeLayout {
    private ImageView photoIV, frontPlayIV;
    private TextView countTV;

    private Context context;

    public PWPhotoPassPhotoView(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public PWPhotoPassPhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    private void initView() {
        inflate(context, R.layout.photopass_manager_unit_view, this);

        photoIV = (ImageView) findViewById(R.id.pp_manager_photo_iv);
        frontPlayIV = (ImageView) findViewById(R.id.pp_manager_video_iv);
        countTV = (TextView) findViewById(R.id.pp_manager_photo_count_tv);
    }

    public void initData(HashMap<String, String> map, int position, int photoCount) {
        if (photoIV.getTag(R.id.glide_image_tag) == null || !photoIV.getTag(R.id.glide_image_tag).equals(map.get("url"))) {
            GlideUtil.load(context, map.get("url"), photoIV);
            photoIV.setTag(R.id.glide_image_tag, map.get("url"));
        }

        if (map.get("isVideo").equals("1")) {
            ViewGroup.LayoutParams layoutParams = frontPlayIV.getLayoutParams();
            layoutParams.width = ScreenUtil.getScreenWidth(context) / 15;
            layoutParams.height = layoutParams.width;
            frontPlayIV.setVisibility(VISIBLE);
        } else {
            frontPlayIV.setVisibility(INVISIBLE);
        }

//        if (photoCount >= 12 && position == 11) {
//            countTV.setVisibility(View.VISIBLE);
//            countTV.setText(photoCount + "");
//
//        } else {
//            countTV.setVisibility(View.GONE);
//        }
        countTV.setVisibility(View.GONE);
    }
}
