package com.pictureair.photopass.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.DisneyVideoTool;
import com.pictureair.photopass.util.PictureAirLog;

import java.util.ArrayList;
import java.util.List;

/**
 * 1、第一次使用，则直接进入无视频引导页面，引导用户如何制作；
 *
 * @author bass
 */
public class IsOneGoToVideoActivity extends BaseActivity implements
        OnClickListener, OnPageChangeListener {
    private static final String TAG = "IsOneGoToVideoActivity";

    private Context context;
    private LayoutInflater inflater;
    private ViewPager mViewPager;
    private List<View> mList;
    private Button btnStart;
    private View view1, view2;
//    private ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acticity_is_one_goto_video);
        context = this;
        initView();
        initData();
        initEvent();
    }

    private void initView() {
        setTopTitleShow(R.string.my_disney_story);
        setTopLeftValueAndShow(R.drawable.back_white,true);
        mViewPager = (ViewPager) findViewById(R.id.vP_is_one_video);
    }

    @Override
    public void TopViewClick(View view) {
        super.TopViewClick(view);
        switch (view.getId()) {
            case R.id.topLeftView:
                finish();
                break;
            default:
                break;
        }
    }

    private void initData() {
        inflater = LayoutInflater.from(context);
        mList = new ArrayList<View>();
//		view1 = inflater.inflate(R.layout.viewpager_disney_video1, null);
//		view1.setBackgroundResource(R.drawable.loding_1);

        view2 = inflater.inflate(R.layout.viewpager_disney_video1, null);
        ((TextView)view2.findViewById(R.id.tv_video1)).setTypeface(MyApplication.getInstance().getFontBold());
        view2.setBackgroundResource(R.drawable.img_disneyvideo1);
//        ivBack = (ImageView) findViewById(R.id.iv_back);
//        ivBack.setOnClickListener(this);
        btnStart = (Button) view2.findViewById(R.id.btn_start);
        btnStart.setTypeface(MyApplication.getInstance().getFontBold());
        btnStart.setVisibility(View.VISIBLE);

//		mList.add(view1);
        mList.add(view2);
    }

    private void initEvent() {
        btnStart.setOnClickListener(this);
        mViewPager.setAdapter(pagerAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_start:
                // 如果有已购买照片，直接进入选择照片制作故事的页面；
                // 如果无已购买的照片，直接进入没有乐拍通照片的页面；
                DisneyVideoTool.getIsEditImageGoToVideo(context);
            default:
                break;
        }
    }

    private PagerAdapter pagerAdapter = new PagerAdapter() {

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mList.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mList.get(position));
            return mList.get(position);
        }
    };

    @Override
    public void onPageScrollStateChanged(int arg0) {
        PictureAirLog.e(TAG, "onPageScrollStateChanged:" + arg0);
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    @Override
    public void onPageSelected(int arg0) {
    }


}
