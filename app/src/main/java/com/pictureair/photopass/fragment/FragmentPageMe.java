package com.pictureair.photopass.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.AboutActivity;
import com.pictureair.photopass.activity.BaseFragment;
import com.pictureair.photopass.activity.CouponActivity;
import com.pictureair.photopass.activity.HelpActivity;
import com.pictureair.photopass.activity.MyPPActivity;
import com.pictureair.photopass.activity.MyPPPActivity;
import com.pictureair.photopass.activity.OrderActivity;
import com.pictureair.photopass.activity.ProfileActivity;
import com.pictureair.photopass.activity.SettingActivity;
import com.pictureair.photopass.selectHeadorBg.SetHeadPhotoAct;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
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
    private TextView profileTV, orderTV, ppTV, pppTV, helpTV, settingTV, aboutTV, couponTV;
    private LinearLayout linearLayout1, linearLayout2, linearLayout3;
    private ImageView headPhoto, icon2, code_pic;
    private TextView name;// hint是条目右边的小标签，根据需要添加信息
    private SharedPreferences sp;
    private String userPP = "";//用户PP号
    private String avatarUrl = "";//用户头像url
    private boolean isCodePic = false;//是否已经生成二维码
    private boolean isShowCodePic = false;//二维码是否已经放大

    private DisplayImageOptions headOptions;

    private PullToZoomScrollViewEx scrollView;


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me, null);
        scrollView = (PullToZoomScrollViewEx) view.findViewById(R.id.scroll_view);
        View headView = LayoutInflater.from(getActivity()).inflate(R.layout.profile_head_view, null, false);
        View zoomView = LayoutInflater.from(getActivity()).inflate(R.layout.profile_zoom_view, null, false);
        View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.profile_content_view, null, false);
        scrollView.setHeaderView(headView);
        scrollView.setZoomView(zoomView);
        scrollView.setScrollContentView(contentView);

        code_pic = (ImageView) scrollView.getZoomView().findViewById(R.id.code_pic);

        headPhoto = (ImageView) scrollView.getHeaderView().findViewById(R.id.user_photo);
        name = (TextView) scrollView.getHeaderView().findViewById(R.id.user_name);

        profileTV = (TextView) scrollView.getPullRootView().findViewById(R.id.me_profile);
        orderTV = (TextView) scrollView.getPullRootView().findViewById(R.id.me_order);
        ppTV = (TextView) scrollView.getPullRootView().findViewById(R.id.me_pp);
        pppTV = (TextView) scrollView.getPullRootView().findViewById(R.id.me_ppp);
        helpTV = (TextView) scrollView.getPullRootView().findViewById(R.id.me_help);
        settingTV = (TextView) scrollView.getPullRootView().findViewById(R.id.me_setting);
        aboutTV = (TextView) scrollView.getPullRootView().findViewById(R.id.me_about);
        couponTV = (TextView) scrollView.getPullRootView().findViewById(R.id.me_coupon);
        linearLayout1 = (LinearLayout) scrollView.getPullRootView().findViewById(R.id.line1_ll);
        linearLayout2 = (LinearLayout) scrollView.getPullRootView().findViewById(R.id.line2_ll);
        linearLayout3 = (LinearLayout) scrollView.getPullRootView().findViewById(R.id.line3_ll);
        ViewGroup.LayoutParams params1 = linearLayout1.getLayoutParams();
        params1.height = ScreenUtil.getScreenHeight(getActivity()) * 186 / 1136;
        linearLayout1.setLayoutParams(params1);
        ViewGroup.LayoutParams params2 = linearLayout2.getLayoutParams();
        params2.height = ScreenUtil.getScreenHeight(getActivity()) * 186 / 1136;
        linearLayout2.setLayoutParams(params2);
        ViewGroup.LayoutParams params3 = linearLayout3.getLayoutParams();
        params3.height = ScreenUtil.getScreenHeight(getActivity()) * 186 / 1136;
        linearLayout3.setLayoutParams(params3);
        headPhoto.setOnClickListener(this);
        profileTV.setOnClickListener(this);
        orderTV.setOnClickListener(this);
        ppTV.setOnClickListener(this);
        pppTV.setOnClickListener(this);
        helpTV.setOnClickListener(this);
        settingTV.setOnClickListener(this);
        aboutTV.setOnClickListener(this);
        couponTV.setOnClickListener(this);

        code_pic.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //二维码放大
                if (!isShowCodePic) {
                    isShowCodePic = true;
                    showCodePic(code_pic, container);
                }
            }
        });

        //初始化控件
        sp = MyApplication.getInstance().getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
        userPP = sp.getString(Common.USERINFO_USER_PP, "");
        //设置头像ImageLoader参数
        headOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.default_photo)
                .showImageForEmptyUri(R.drawable.default_photo)
                .showImageOnFail(R.drawable.default_photo)
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .build();

        LinearLayout.LayoutParams localObject = new LinearLayout.LayoutParams(ScreenUtil.getScreenWidth(getActivity()),
                (int) (4.0F * (ScreenUtil.getScreenHeight(getActivity()) / 16.0F)) + ScreenUtil.dip2px(getActivity(), 35));
        scrollView.setHeaderLayoutParams(localObject);

        return view;
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        // 初始化数据
        initData();
    }

    /**
     * 更新数据
     * 用户名、图像、
     */
    private void initData() {
        isShowCodePic = false;
        isCodePic = false;
        if ("".equals(sp.getString(Common.USERINFO_NICKNAME, ""))) {
            name.setText(sp.getString(Common.USERINFO_ACCOUNT, "PhotoPass"));
        } else {
            name.setText(sp.getString(Common.USERINFO_NICKNAME, ""));
        }
        avatarUrl = sp.getString(Common.USERINFO_HEADPHOTO, null);
        setCodePic();//设置二维码
        setHeadImage();//设置头像
    }

    /**
     * 设置头像
     */
    private void setHeadImage() {
        // TODO Auto-generated method stub
        if (avatarUrl == null || avatarUrl.equals("")) {
            //如果user中的头像为空，显示默认头像
            PictureAirLog.v(TAG, "setHeadImage == null");
            headPhoto.setImageResource(R.drawable.default_photo);
        } else {//如果有数据，加载图片
            PictureAirLog.v(TAG, "setHeadImage: " + avatarUrl);
            initUserHeadImage();
        }
    }

    /**
     * 初始化头像
     */
    public void initUserHeadImage() {
        ImageLoader.getInstance().displayImage(Common.PHOTO_URL + avatarUrl, headPhoto, headOptions);
    }

    /**
     * 生成二维码
     */
    public void setCodePic() {
        if (!isCodePic) {
            if (!userPP.isEmpty()) {
                try {
                    //生成二维码
                    code_pic.setImageBitmap(AppUtil.createQRCode(userPP, ScreenUtil.getScreenWidth(getActivity()) / 5));
                    isCodePic = true;
                } catch (WriterException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

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

            case R.id.user_photo:
                // 跳转到图片选择返回一张图片更新头像
                i.setClass(MyApplication.getInstance(), SetHeadPhotoAct.class);
                startActivity(i);
                break;

            case R.id.me_profile:
                i.setClass(MyApplication.getInstance(), ProfileActivity.class);
                startActivity(i);
                break;

            case R.id.me_order:
                // 跳转到我的订单页面
                i.setClass(MyApplication.getInstance(), OrderActivity.class);
                startActivity(i);
                break;

            case R.id.me_pp:
                i.setClass(MyApplication.getInstance(), MyPPActivity.class);
                startActivity(i);
                break;

            case R.id.me_ppp:
                // 跳转到PPP页面
                i.setClass(MyApplication.getInstance(), MyPPPActivity.class);
                startActivity(i);
                break;

            case R.id.me_help:
                i.setClass(MyApplication.getInstance(), HelpActivity.class);
                startActivity(i);
                break;

            case R.id.me_setting:
                i.setClass(MyApplication.getInstance(), SettingActivity.class);
                startActivity(i);
                break;

            case R.id.me_about:
                i.setClass(MyApplication.getInstance(), AboutActivity.class);
                startActivity(i);
                break;

            case R.id.me_coupon:
                i.setClass(MyApplication.getInstance(), CouponActivity.class);
                startActivity(i);
                break;

            default:
                break;
        }
    }

    /**
     * 显示二维码大图
     * 1.动态添加大图
     * 2.添加动画
     *
     * @param viewGroup
     */
    private void showCodePic(ImageView codePicView, ViewGroup viewGroup) {
        //半透明背景
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        LinearLayout layout = new LinearLayout(viewGroup.getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundResource(R.color.black_alpha_60);
        //边框 存放二维码/文字
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.show_code_layout, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.code_pic_iv);
        try {
            imageView.setImageBitmap(AppUtil.createQRCode(userPP, ScreenUtil.getScreenWidth(getActivity()) / 5));
        } catch (WriterException e) {
            e.printStackTrace();
        }
        imageView.setLayoutParams(new LinearLayout.LayoutParams(ScreenUtil.getScreenWidth(viewGroup.getContext()) / 2, ScreenUtil.getScreenWidth(viewGroup.getContext()) / 2));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        viewParams.gravity = Gravity.CENTER;
        viewParams.setMargins(ScreenUtil.dip2px(viewGroup.getContext(), 64), 0, ScreenUtil.dip2px(viewGroup.getContext(), 64), 0);
        layout.addView(view, viewParams);

        viewGroup.addView(layout, layoutParams);
        //动画开始
        startAnimation(viewGroup, layout);
    }

    /**
     * 开始动画（从中间放大）
     *
     * @param viewGroup
     * @param layout
     */
    private void startAnimation(final ViewGroup viewGroup, final LinearLayout layout) {
        final ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setInterpolator(new LinearInterpolator());
        scaleAnimation.setRepeatCount(0);
        scaleAnimation.setFillAfter(true);
        final AnimationSet set = new AnimationSet(false);
        set.setFillAfter(true);
        set.addAnimation(scaleAnimation);
        set.setDuration(300);
        layout.startAnimation(set);
        //点击屏幕取消
        layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                scaleAnimation.cancel();
                set.cancel();
                layout.clearAnimation();
                viewGroup.removeView(layout);
                isShowCodePic = false;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}