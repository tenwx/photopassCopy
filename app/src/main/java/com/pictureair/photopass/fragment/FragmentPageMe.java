package com.pictureair.photopass.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.BaseFragment;
import com.pictureair.photopass.activity.CouponActivity;
import com.pictureair.photopass.activity.HelpActivity;
import com.pictureair.photopass.activity.LoadManageActivity;
import com.pictureair.photopass.activity.MyPPPActivity;
import com.pictureair.photopass.activity.OpinionsActivity;
import com.pictureair.photopass.activity.OrderActivity;
import com.pictureair.photopass.activity.ProfileActivity;
import com.pictureair.photopass.activity.SettingActivity;
import com.pictureair.photopass.activity.WebViewActivity;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.CouponTool;
import com.pictureair.photopass.util.GlideUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.pulltozoomview.PullToZoomScrollViewEx;

/**
 * 我的界面
 * 处理用户信息、订单、乐拍通、乐拍通一卡通、设置、帮助、关于
 *
 * @author bauer_bao
 */
public class FragmentPageMe extends BaseFragment implements OnClickListener {
    private static final String TAG = "FragmentPageMe";
    private RelativeLayout orderTV,  pppTV, helpTV,  couponTV, opinionsTV, customerTV, downloadTV;
    private ImageView headPhoto, headSet;
    private TextView name;// hint是条目右边的小标签，根据需要添加信息
    private String avatarUrl = "";//用户头像url

    private boolean hasHidden = false;

    private PullToZoomScrollViewEx scrollView;


    private Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        activity = getActivity();
        View view = inflater.inflate(R.layout.fragment_me, null);
        scrollView = (PullToZoomScrollViewEx) view.findViewById(R.id.scroll_view);
        View headView = LayoutInflater.from(activity).inflate(R.layout.profile_head_view, null, false);
        View zoomView = LayoutInflater.from(activity).inflate(R.layout.profile_zoom_view, null, false);
        View contentView = LayoutInflater.from(activity).inflate(R.layout.profile_content_view, null, false);
        scrollView.setHeaderView(headView);
        scrollView.setZoomView(zoomView);
        scrollView.setScrollContentView(contentView);


        headPhoto = (ImageView) scrollView.getHeaderView().findViewById(R.id.user_photo);
        name = (TextView) scrollView.getHeaderView().findViewById(R.id.user_name);
        headSet = (ImageView) scrollView.getHeaderView().findViewById(R.id.user_set);
        scrollView.getHeaderView().findViewById(R.id.user_head_layout).setOnClickListener(this);

        orderTV = (RelativeLayout) scrollView.getPullRootView().findViewById(R.id.me_order);
        pppTV = (RelativeLayout) scrollView.getPullRootView().findViewById(R.id.me_ppp);
        helpTV = (RelativeLayout) scrollView.getPullRootView().findViewById(R.id.me_help);
        couponTV = (RelativeLayout) scrollView.getPullRootView().findViewById(R.id.me_coupon);
        opinionsTV = (RelativeLayout) scrollView.getPullRootView().findViewById(R.id.me_opinions);
        customerTV = (RelativeLayout) scrollView.getPullRootView().findViewById(R.id.me_customer);
        downloadTV = (RelativeLayout) scrollView.getPullRootView().findViewById(R.id.me_download);

        orderTV.setOnClickListener(this);
        pppTV.setOnClickListener(this);
        helpTV.setOnClickListener(this);
        couponTV.setOnClickListener(this);
        opinionsTV.setOnClickListener(this);
        headSet.setOnClickListener(this);
        customerTV.setOnClickListener(this);
        downloadTV.setOnClickListener(this);

        LinearLayout.LayoutParams localObject = new LinearLayout.LayoutParams(ScreenUtil.getScreenWidth(activity),
                (int) (4.0F * (ScreenUtil.getScreenHeight(activity) / 16.0F)) + ScreenUtil.dip2px(activity, 35));
        scrollView.setHeaderLayoutParams(localObject);

        return view;
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (!hasHidden) {
            PictureAirLog.out("truely resume----->me");
            // 初始化数据
            initData();
        } else {
            PictureAirLog.out("fake resume----->me");
        }
    }

    /**
     * 更新数据
     * 用户名、图像、
     */
    private void initData() {
        if ("".equals(SPUtils.getString(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_NICKNAME, ""))) {
            name.setText(SPUtils.getString(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ACCOUNT, "PhotoPass"));
        } else {
            name.setText(SPUtils.getString(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_NICKNAME, ""));
        }
        avatarUrl = SPUtils.getString(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_HEADPHOTO, null);
        GlideUtil.load(activity, Common.PHOTO_URL + avatarUrl, R.drawable.default_photo, R.drawable.default_photo, System.currentTimeMillis() + "", headPhoto);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        Intent i = new Intent();
        switch (v.getId()) {
//		case R.id.backgroud_img:
//			Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
//			getAlbum.setType("image/*");
//			startActivityForResult(getAlbum, 1);
//			break;

//            case R.id.user_photo:
//                // 跳转到图片选择返回一张图片更新头像
//                i.setClass(MyApplication.getInstance(), SetHeadPhotoAct.class);
//                startActivity(i);
//                break;

//            case R.id.me_profile:
//                i.setClass(MyApplication.getInstance(), ProfileActivity.class);
//                startActivity(i);
//                break;

            case R.id.me_order:
                // 跳转到我的订单页面
                i.setClass(MyApplication.getInstance(), OrderActivity.class);
                startActivity(i);
                break;

//            case R.id.me_pp:
//                i.setClass(MyApplication.getInstance(), MyPPActivity.class);
//                startActivity(i);
//                break;

            case R.id.me_ppp:
                // 跳转到PPP页面
                i.setClass(MyApplication.getInstance(), MyPPPActivity.class);
                startActivity(i);
                break;

            case R.id.me_help:
                i.setClass(MyApplication.getInstance(), HelpActivity.class);
                startActivity(i);
                break;

//            case R.id.me_setting:
//                i.setClass(MyApplication.getInstance(), SettingActivity.class);
//                startActivity(i);
//                break;

            case R.id.me_download:
                i.setClass(MyApplication.getInstance(), LoadManageActivity.class);
                startActivity(i);
                break;

            case R.id.me_coupon:
                i.setClass(MyApplication.getInstance(), CouponActivity.class);
                i.putExtra(CouponTool.ACTIVITY_ME, CouponTool.ACTIVITY_ME);
                i.setClass(MyApplication.getInstance(), CouponActivity.class);
                startActivity(i);
                break;

            case R.id.me_opinions:
                //意见反馈弹出框
                PictureAirLog.v(TAG, "me_opinions");
//                UmengUtil.startFeedbackActivity(context);
                i.setClass(MyApplication.getInstance(), WebViewActivity.class);
                i.putExtra("key",3);
                startActivity(i);

                break;

            case R.id.me_customer:

                i.setClass(MyApplication.getInstance(), OpinionsActivity.class);
                startActivity(i);

                break;

            case R.id.user_set:
                i.setClass(MyApplication.getInstance(), SettingActivity.class);
                startActivity(i);
                break;

            case R.id.user_head_layout:
                i.setClass(MyApplication.getInstance(), ProfileActivity.class);
                startActivity(i);
                break;

            default:
                break;
        }
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        hasHidden = hidden;
        PictureAirLog.out("onHiddenChanged---->me" + hidden);
    }
}