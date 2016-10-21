package com.pictureair.photopass.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.BaseLazyFragment;
import com.pictureair.photopass.activity.CouponActivity;
import com.pictureair.photopass.activity.HelpActivity;
import com.pictureair.photopass.activity.LoadManageActivity;
import com.pictureair.photopass.activity.MyPPActivity;
import com.pictureair.photopass.activity.MyPPPActivity;
import com.pictureair.photopass.activity.OrderActivity;
import com.pictureair.photopass.activity.ProfileActivity;
import com.pictureair.photopass.activity.SettingActivity;
import com.pictureair.photopass.activity.WebViewActivity;
import com.pictureair.photopass.util.AppUtil;
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
public class FragmentPageMe extends BaseLazyFragment implements OnClickListener {
    private static final String TAG = "FragmentPageMe";
    private TextView profileTV, orderTV, ppTV, pppTV, helpTV, settingTV, downLoadTV, couponTV,opinionsTV;
    private LinearLayout linearLayout1, linearLayout2, linearLayout3;
    private LinearLayout layout;
    private ImageView headPhoto, code_pic;     
    private View view;
    private ViewGroup container;
    private TextView name;// hint是条目右边的小标签，根据需要添加信息
    private String userPPCode = "";//用户PP号
    private String qrCodeUrl = "";
    private String avatarUrl = "";//用户头像url
    private boolean isCodePic = false;//是否已经生成二维码
    private boolean isShowCodePic = false;//二维码是否已经放大

    private boolean isPrepared, isInited;

    private PullToZoomScrollViewEx scrollView;

    private ScaleAnimation scaleAnimation;
    private AnimationSet set;

    private Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            activity = getActivity();
            view = inflater.inflate(R.layout.fragment_me, null);
            this.container = container;
            isPrepared = true;
            onLazyLoad();
        }
        return view;
    }

    @Override
    protected void onLazyLoad() {
        if (!isPrepared || !isVisible || isInited) {
            return;
        }
        isInited = true;
        scrollView = (PullToZoomScrollViewEx) view.findViewById(R.id.scroll_view);
        View headView = LayoutInflater.from(activity).inflate(R.layout.profile_head_view, null, false);
        View zoomView = LayoutInflater.from(activity).inflate(R.layout.profile_zoom_view, null, false);
        View contentView = LayoutInflater.from(activity).inflate(R.layout.profile_content_view, null, false);
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
        downLoadTV = (TextView) scrollView.getPullRootView().findViewById(R.id.me_download);
        couponTV = (TextView) scrollView.getPullRootView().findViewById(R.id.me_coupon);
        opinionsTV = (TextView) scrollView.getPullRootView().findViewById(R.id.me_opinions);

        linearLayout1 = (LinearLayout) scrollView.getPullRootView().findViewById(R.id.line1_ll);
        linearLayout2 = (LinearLayout) scrollView.getPullRootView().findViewById(R.id.line2_ll);
        linearLayout3 = (LinearLayout) scrollView.getPullRootView().findViewById(R.id.line3_ll);
        ViewGroup.LayoutParams params1 = linearLayout1.getLayoutParams();
        params1.height = ScreenUtil.getScreenHeight(activity) * 186 / 1136;
        linearLayout1.setLayoutParams(params1);
        ViewGroup.LayoutParams params2 = linearLayout2.getLayoutParams();
        params2.height = ScreenUtil.getScreenHeight(activity) * 186 / 1136;
        linearLayout2.setLayoutParams(params2);
        ViewGroup.LayoutParams params3 = linearLayout3.getLayoutParams();
        params3.height = ScreenUtil.getScreenHeight(activity) * 186 / 1136;
        linearLayout3.setLayoutParams(params3);
        profileTV.setOnClickListener(this);
        orderTV.setOnClickListener(this);
        ppTV.setOnClickListener(this);
        pppTV.setOnClickListener(this);
        helpTV.setOnClickListener(this);
        settingTV.setOnClickListener(this);
        downLoadTV.setOnClickListener(this);
        couponTV.setOnClickListener(this);
        opinionsTV.setOnClickListener(this);

        code_pic.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PictureAirLog.out("qrcode on click" + userPPCode + " " + isShowCodePic);
                //二维码放大
                if (!isShowCodePic && !TextUtils.isEmpty(userPPCode)) {
                    isShowCodePic = true;
                    showCodePic(container);
                }
            }
        });

        //初始化控件
        userPPCode = SPUtils.getString(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_USER_PP, "");
        qrCodeUrl = Common.BARCODEURL + userPPCode;

        LinearLayout.LayoutParams localObject = new LinearLayout.LayoutParams(ScreenUtil.getScreenWidth(activity),
                (int) (4.0F * (ScreenUtil.getScreenHeight(activity) / 16.0F)) + ScreenUtil.dip2px(activity, 35));
        scrollView.setHeaderLayoutParams(localObject);

        onResume();//第一次resume在activity运行的时候就执行了，导致真正到了me页面之后，第一次的resume不会执行
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (isPrepared && isVisible) {
            PictureAirLog.out("truely resume----->me");
            // 初始化数据
            initData();
        } else {
            PictureAirLog.out("fake resume----->me");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isPrepared && isVisible) {
            PictureAirLog.out("FragmentPageMe" + "  ==onPause");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        if (isPrepared && isVisible) {
            PictureAirLog.out("FragmentPageMe" + "  ==onVisible");
        }
    }

    @Override
    protected void onInvisible() {
        super.onInvisible();
        if (isPrepared && !isVisible) {//隐藏发现页面
            PictureAirLog.out("FragmentPageMe" + "  ==onInVisible");
            PictureAirLog.out("hide me---->");
            if (layout != null) {//如果二维码框现实中，则关闭
                layout.performClick();
            }
        }
    }

    /**
     * 更新数据
     * 用户名、图像、
     */
    private void initData() {
        isShowCodePic = false;
        isCodePic = false;
        if ("".equals(SPUtils.getString(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_NICKNAME, ""))) {
            name.setText(SPUtils.getString(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ACCOUNT, "PhotoPass"));
        } else {
            name.setText(SPUtils.getString(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_NICKNAME, ""));
        }
        avatarUrl = SPUtils.getString(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_HEADPHOTO, null);
        setCodePic();//设置二维码
        GlideUtil.load(activity, Common.PHOTO_URL + avatarUrl, R.drawable.default_photo, R.drawable.default_photo, System.currentTimeMillis() + "", headPhoto);
    }

    /**
     * 生成二维码
     */
    public void setCodePic() {
        if (!isCodePic) {
            if (!qrCodeUrl.isEmpty()) {
                try {
                    //生成二维码
                    code_pic.setImageBitmap(AppUtil.createQRCode(qrCodeUrl, ScreenUtil.getScreenWidth(activity) / 5));
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
                i.setClass(MyApplication.getInstance(), WebViewActivity.class);
                i.putExtra("key",3);
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
    private void showCodePic(ViewGroup viewGroup) {
        PictureAirLog.out("showCodePic");
        //半透明背景
        if (layout == null) {
            initPicCodeView(viewGroup);
        }

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        viewGroup.addView(layout, layoutParams);
        //动画开始
        layout.startAnimation(set);
    }

    private void initPicCodeView(final ViewGroup viewGroup) {
        PictureAirLog.out("initPicCodeView");
        layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundResource(R.color.black_alpha_60);
        //边框 存放二维码/文字
        View view = LayoutInflater.from(activity).inflate(R.layout.show_code_layout, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.code_pic_iv);
        TextView textView = (TextView) view.findViewById(R.id.code_tv);
        //初始化ppp号码
        String ppCode = userPPCode.substring(0, 4);
        for (int i = 0; i < 3; i++) {//4-7，8-11，12-15
            ppCode += "-" + userPPCode.substring(4 * i + 4, 4 * i + 8);
        }
        textView.setText(ppCode);
        try {
            imageView.setImageBitmap(AppUtil.createQRCode(qrCodeUrl, ScreenUtil.getScreenWidth(activity) / 2));
        } catch (WriterException e) {
            e.printStackTrace();
        }
        ViewGroup.LayoutParams layoutParams1 = imageView.getLayoutParams();
        layoutParams1.width = ScreenUtil.getScreenWidth(activity) / 2;
        layoutParams1.height = ScreenUtil.getScreenWidth(activity) / 2;
        imageView.setLayoutParams(layoutParams1);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        viewParams.gravity = Gravity.CENTER;
        viewParams.setMargins(ScreenUtil.dip2px(activity, 40), 0, ScreenUtil.dip2px(activity, 40), 0);
        layout.addView(view, viewParams);

        scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setInterpolator(new LinearInterpolator());
        scaleAnimation.setRepeatCount(0);
        scaleAnimation.setFillAfter(true);

        set = new AnimationSet(false);
        set.setFillAfter(true);
        set.addAnimation(scaleAnimation);
        set.setDuration(300);

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
}