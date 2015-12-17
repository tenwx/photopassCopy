package com.pictureair.photopass.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.WriterException;
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

import java.io.File;

/**
 * 我的界面
 * 处理用户信息、订单、乐拍通、乐拍通+、设置、帮助、关于
 *
 * @author bauer_bao
 */
public class FragmentPageMe extends BaseFragment implements OnClickListener {
    private static final String TAG = "FragmentPageMe";
    private RelativeLayout rl_myprofile, rl_myorder, rl_pp, rl_ppp, rl_help, rl_setting, rl_about;
    private ImageView headPhoto, icon2, code_pic;
    private TextView name;// hint是条目右边的小标签，根据需要添加信息
    private SharedPreferences sp;
    private Long originalHeadLastModifyTime;
    private File file;
    private String userPP = "";//用户PP号
    private String avatarUrl = "";//用户头像url
    private boolean isCodePic = false;//是否已经生成二维码

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

        //初始化控件
        sp = MyApplication.getInstance().getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
        userPP = sp.getString(Common.USERINFO_USER_PP, "");
        file = new File(Common.USER_PATH + Common.HEADPHOTO_PATH);

        return view;
    }

    /**
     * 更新数据
     * 用户名、图像、
     */
    private void initData() {
        name.setText(sp.getString(Common.USERINFO_NICKNAME, "photoPass"));
        avatarUrl = sp.getString(Common.USERINFO_HEADPHOTO, "");
        setCodePic();
        setHeadImage();
    }


    /**
     * 生成二维码
     */
    public void setCodePic() {
        if (isCodePic) {
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

    /**
     * 设置头像
     */
    private void setHeadImage() {
        // TODO Auto-generated method stub
        if (avatarUrl.isEmpty()) {
            //如果user中的头像为空，显示默认头像
            PictureAirLog.v(TAG, "null" + avatarUrl);
            headPhoto.setImageDrawable(getResources().getDrawable(R.drawable.default_photo));
            originalHeadLastModifyTime = 0L;
        } else {//如果有数据，加载图片
            Bitmap bm1 = null;
            Options options = new Options();
            options.inSampleSize = 2;
            bm1 = BitmapFactory.decodeFile(Common.USER_PATH + Common.HEADPHOTO_PATH, options);

            originalHeadLastModifyTime = file.lastModified();
            if (bm1 != null) {
                headPhoto.setImageBitmap(bm1);
            } else {
                headPhoto.setImageDrawable(getResources().getDrawable(R.drawable.default_photo));
            }

        }
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        // 初始化数据
        initData();
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