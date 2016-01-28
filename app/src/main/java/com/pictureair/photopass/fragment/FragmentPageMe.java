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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.AboutActivity;
import com.pictureair.photopass.activity.BaseFragment;
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

/**
 * 我的界面
 * 处理用户信息、订单、乐拍通、乐拍通一卡通、设置、帮助、关于
 *
 * @author bauer_bao
 */
public class FragmentPageMe extends BaseFragment implements OnClickListener {
    private static final String TAG = "FragmentPageMe";
    private RelativeLayout rl_myprofile, rl_myorder, rl_pp, rl_ppp, rl_help, rl_setting, rl_about;
    private ImageView headPhoto, icon2, code_pic;
    private TextView name;// hint是条目右边的小标签，根据需要添加信息
    private SharedPreferences sp;
    private String userPP = "";//用户PP号
    private String avatarUrl = "";//用户头像url
    private boolean isCodePic = false;//是否已经生成二维码
    private boolean isShowCodePic = false;//二维码是否已经放大

    private DisplayImageOptions headOptions;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me, null);
        code_pic = (ImageView) view.findViewById(R.id.code_pic);
        headPhoto = (ImageView) view.findViewById(R.id.user_photo);
        name = (TextView) view.findViewById(R.id.user_name);
        rl_myprofile = (RelativeLayout) view.findViewById(R.id.rl_myprofile);
        rl_myorder = (RelativeLayout) view.findViewById(R.id.rl_myorder);
        rl_pp = (RelativeLayout) view.findViewById(R.id.rl_pp);
        rl_ppp = (RelativeLayout) view.findViewById(R.id.rl_ppp);
        rl_help = (RelativeLayout) view.findViewById(R.id.rl_help);
        rl_setting = (RelativeLayout) view.findViewById(R.id.rl_setting);
        rl_about = (RelativeLayout) view.findViewById(R.id.rl_about);

        headPhoto.setOnClickListener(this);
        rl_myprofile.setOnClickListener(this);
        rl_myorder.setOnClickListener(this);
        rl_pp.setOnClickListener(this);
        rl_ppp.setOnClickListener(this);
        rl_help.setOnClickListener(this);
        rl_setting.setOnClickListener(this);
        rl_about.setOnClickListener(this);
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
//			getActivity().finish();
                break;
            case R.id.rl_myprofile:
                i.setClass(MyApplication.getInstance(), ProfileActivity.class);
                startActivity(i);
                break;
            case R.id.rl_myorder:
                // 跳转到我的订单页面
                i.setClass(MyApplication.getInstance(), OrderActivity.class);
                startActivity(i);
                break;
            case R.id.rl_pp:
                i.setClass(MyApplication.getInstance(), MyPPActivity.class);
                startActivity(i);
                break;
            case R.id.rl_ppp:
                // 跳转到PPP页面
                i.setClass(MyApplication.getInstance(), MyPPPActivity.class);
                startActivity(i);
                break;
            case R.id.rl_help:
                i.setClass(MyApplication.getInstance(), HelpActivity.class);
                startActivity(i);
                break;
            case R.id.rl_setting:
                i.setClass(MyApplication.getInstance(), SettingActivity.class);
                startActivity(i);
                break;
            case R.id.rl_about:
                i.setClass(MyApplication.getInstance(), AboutActivity.class);
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
        layout.setBackgroundResource(R.color.white_alpha_90);
        //二维码
        ImageView imageView = new ImageView(viewGroup.getContext());
        try {
            imageView.setImageBitmap(AppUtil.createQRCode(userPP, ScreenUtil.getScreenWidth(getActivity()) / 5));
        } catch (WriterException e) {
            e.printStackTrace();
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(3 * codePicView.getWidth(), 3 * codePicView.getHeight());
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setBackgroundResource(R.color.white);
        layout.addView(imageView, params);
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
        set.setDuration(500);
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
//        AppManager.getInstance().killActivity(getActivity());
    }
}