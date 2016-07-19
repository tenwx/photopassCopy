package com.pictureair.photopass.fragment;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.BaseFragment;
import com.pictureair.photopass.activity.LoadManageActivity;
import com.pictureair.photopass.activity.MyPPActivity;
import com.pictureair.photopass.adapter.PhotoLoadSuccessAdapter;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.PhotoDownLoadInfo;
import com.pictureair.photopass.eventbus.BaseBusEvent;
import com.pictureair.photopass.eventbus.TabIndicatorUpdateEvent;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.widget.CustomProgressDialog;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

/**
 * Created by pengwu on 16/7/8.
 */
public class LoadSuccessFragment extends BaseFragment implements View.OnClickListener{

    private ListView lv_success;
    PhotoLoadSuccessAdapter photoLoadSuccessAdapter;
    private CustomProgressDialog dialog;
    private boolean isLoading;
    private PictureAirDbManager pictureAirDbManager;
    private SharedPreferences sPreferences;
    private String userId = "";
    private final Handler photoLoadSuccessHandler= new PhotoLoadSuccessHandler(this);
    public static final int LOAD_FROM_DATABASE = 1111;
    public static final int DELETE_SUCCESS = 2233;
    List<PhotoDownLoadInfo> photos;
    private RelativeLayout rl_load_success;
    private LinearLayout ll_load_success;
    private Button button_toload;
    private Button btn_clear;


    private static class PhotoLoadSuccessHandler extends Handler{
        private final WeakReference<LoadSuccessFragment> mActivity;

        public PhotoLoadSuccessHandler(LoadSuccessFragment fragment){
            mActivity = new WeakReference<LoadSuccessFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivity.get() == null) {
                return;
            }
            mActivity.get().dealHandler(msg);
        }
    }

    private void dealHandler(Message msg){
        switch (msg.what){
            case LOAD_FROM_DATABASE:
                if (msg.obj != null) {
                    photos = (List<PhotoDownLoadInfo>)(msg.obj);
                    if (photos != null && photos.size() >0) {
                        ll_load_success.setVisibility(View.VISIBLE);
                        rl_load_success.setVisibility(View.GONE);
                        PhotoLoadSuccessAdapter adapter = new PhotoLoadSuccessAdapter(getContext(), photos);
                        lv_success.setAdapter(adapter);
                        EventBus.getDefault().post(new TabIndicatorUpdateEvent(photos.size(), 1));
                    }else{
                        rl_load_success.setVisibility(View.VISIBLE);
                        ll_load_success.setVisibility(View.GONE);
                        EventBus.getDefault().post(new TabIndicatorUpdateEvent(0, 1));
                    }
                }
                isLoading = false;
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                break;
            case DELETE_SUCCESS:
                rl_load_success.setVisibility(View.VISIBLE);
                ll_load_success.setVisibility(View.GONE);
                EventBus.getDefault().post(new TabIndicatorUpdateEvent(0, 1));
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                break;
            default:
                break;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_loadsuccess,null);
        lv_success = (ListView) view.findViewById(R.id.lv_load_success);
        rl_load_success = (RelativeLayout) view.findViewById(R.id.rl_load_success);
        ll_load_success = (LinearLayout) view.findViewById(R.id.ll_load_success);
        button_toload = (Button) view.findViewById(R.id.load_success_btn_toload);
        btn_clear = (Button) view.findViewById(R.id.load_success_clear);
        rl_load_success.setVisibility(View.GONE);
        ll_load_success.setVisibility(View.GONE);
        button_toload.setOnClickListener(this);
        btn_clear.setOnClickListener(this);
        dialog = CustomProgressDialog.create(getContext(), getString(R.string.is_loading), false, null);
        lv_success.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (photos == null || photos.size() == 0) {
                    return;
                }
                PhotoDownLoadInfo downLoadInfo = photos.get(position);
                String fileName = AppUtil.getReallyFileName(downLoadInfo.getUrl(),0);
                PictureAirLog.out("filename=" + fileName);
                String loadUrl = Common.PHOTO_DOWNLOAD_PATH+fileName;
                File photo = new File(loadUrl);
                if (photo.exists()) {
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(photo), "image/*");
                    startActivity(intent);
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        pictureAirDbManager = new PictureAirDbManager(getContext());
        sPreferences = getContext().getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, Context.MODE_PRIVATE);
        if (TextUtils.isEmpty(userId)) {
            userId = sPreferences.getString(Common.USERINFO_ID, "");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isLoading) {
            isLoading = true;
            if (dialog != null && !dialog.isShowing()) {
                dialog.show();
            }
            new Thread(){
                @Override
                public void run() {
                    List<PhotoDownLoadInfo> photos = pictureAirDbManager.getLoadSuccessPhotos(userId);
                    photoLoadSuccessHandler.obtainMessage(LOAD_FROM_DATABASE,photos).sendToTarget();
                }
            }.start();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.load_success_btn_toload:
                Intent i = new Intent();
                i.setClass(MyApplication.getInstance(), MyPPActivity.class);
                startActivity(i);
                getActivity().finish();
                break;
            case R.id.load_success_clear:
                if (dialog != null && !dialog.isShowing()) {
                    dialog.show();
                }
                new Thread(){
                    @Override
                    public void run() {
                        pictureAirDbManager.deleteDownloadPhoto(userId);
                        photoLoadSuccessHandler.sendEmptyMessage(DELETE_SUCCESS);
                    }
                }.start();

                break;
            default:
                break;
        }
    }

    public void updateList(){
        isLoading = true;
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
        new Thread(){
            @Override
            public void run() {
                List<PhotoDownLoadInfo> photos = pictureAirDbManager.getLoadSuccessPhotos(userId);
                photoLoadSuccessHandler.obtainMessage(LOAD_FROM_DATABASE,photos).sendToTarget();
            }
        }.start();
    }

    public void getDataBackground(){
        isLoading = true;
        new Thread(){
            @Override
            public void run() {
                List<PhotoDownLoadInfo> photos = pictureAirDbManager.getLoadSuccessPhotos(userId);
                photoLoadSuccessHandler.obtainMessage(LOAD_FROM_DATABASE,photos).sendToTarget();
            }
        }.start();
    }

}
