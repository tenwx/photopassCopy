package com.pictureair.hkdlphotopass.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.pictureair.hkdlphotopass.MyApplication;
import com.pictureair.hkdlphotopass.R;
import com.pictureair.hkdlphotopass.activity.BaseFragment;
import com.pictureair.hkdlphotopass.activity.DownloadPhotoPreviewActivity;
import com.pictureair.hkdlphotopass.activity.LoadManageActivity;
import com.pictureair.hkdlphotopass.activity.MainTabActivity;
import com.pictureair.hkdlphotopass.adapter.PhotoLoadSuccessAdapter;
import com.pictureair.hkdlphotopass.greendao.PictureAirDbManager;
import com.pictureair.hkdlphotopass.entity.PhotoDownLoadInfo;
import com.pictureair.hkdlphotopass.eventbus.TabIndicatorUpdateEvent;
import com.pictureair.hkdlphotopass.util.Common;
import com.pictureair.hkdlphotopass.util.PictureAirLog;
import com.pictureair.hkdlphotopass.util.SPUtils;
import com.pictureair.hkdlphotopass.widget.PWToast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;

/**
 * 下载管理－下载成功
 * Created by pengwu on 16/7/8.
 */
public class LoadSuccessFragment extends BaseFragment implements View.OnClickListener,AdapterView.OnItemClickListener{

    private ListView lv_success;
    private String userId = "";
    private final Handler photoLoadSuccessHandler= new PhotoLoadSuccessHandler(this);
    public static final int LOAD_FROM_DATABASE = 1111;
    public static final int DELETE_SUCCESS = 2233;
    public static final int GET_PHOTO_BACKGROUND = 3344;
    public static final int RELOAD_DATABASE = 4455;
    private List<PhotoDownLoadInfo> photos;
    private ScrollView rl_load_success;
    private RelativeLayout ll_load_success;
    private Button button_toload;
    private PhotoLoadSuccessAdapter adapter;
    private ExecutorService executorService;
    private LinearLayout ll_pop;
    private Animation animationIn;
    private Animation animationOut;
    private CopyOnWriteArrayList<PhotoDownLoadInfo> selectPhotos;
    private TextView tv_selectAll;
    private TextView tv_delete;
    private boolean selectAll;
    private PWToast myToast;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (photos == null || photos.size() == 0 || adapter == null) {
            return;
        }
        if (!adapter.isSelect()) {
            Intent intent = new Intent(MyApplication.getInstance(), DownloadPhotoPreviewActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("position", position);
            intent.putExtras(bundle);
            startActivity(intent);
        }else{
            if (photos.size() > position){
                PhotoDownLoadInfo info = photos.get(position);
                info.setSelectPos(position);
                if (!info.isSelect()){
                    info.setSelect(true);
                    adapter.setPhotos(photos);
                    adapter.notifyDataSetChanged();
                    selectPhotos.add(info);
                }else{
                    info.setSelect(false);
                    adapter.setPhotos(photos);
                    adapter.notifyDataSetChanged();
                    removeInfo(position);
                }
                enableReconnectDeleteButton();
                ifSelectAllWhenItemClick();
            }
        }
    }

    private void ifSelectAllWhenItemClick(){
        if (selectPhotos.size() == photos.size()){
            selecAllButtonStatusChange(true);
        }else{
            selecAllButtonStatusChange(false);
        }
    }

    private void enableReconnectDeleteButton(){
        if (selectPhotos.size() > 0) {
            tv_delete.setEnabled(true);
        }else{
            tv_delete.setEnabled(false);
        }
    }

    private void removeInfo(int pos){
        if (selectPhotos.size() >0 ) {
            Iterator<PhotoDownLoadInfo> iterator = selectPhotos.iterator();
            while (iterator.hasNext()) {
                PhotoDownLoadInfo info = iterator.next();
                if (info.getSelectPos() == pos) {
                    selectPhotos.remove(info);
                }
            }
        }
    }

    private static class PhotoLoadSuccessHandler extends Handler{
        private final WeakReference<LoadSuccessFragment> mActivity;

        public PhotoLoadSuccessHandler(LoadSuccessFragment fragment){
            mActivity = new WeakReference<>(fragment);
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
                        adapter = new PhotoLoadSuccessAdapter(MyApplication.getInstance(), photos);
                        lv_success.setAdapter(adapter);
                        EventBus.getDefault().post(new TabIndicatorUpdateEvent(photos.size(), 1,false));
                    }else{
                        rl_load_success.setVisibility(View.VISIBLE);
                        ll_load_success.setVisibility(View.GONE);
                        EventBus.getDefault().post(new TabIndicatorUpdateEvent(0, 1,false));
                    }
                }
                dismissPWProgressDialog();
                break;
            case DELETE_SUCCESS:
                rl_load_success.setVisibility(View.VISIBLE);
                ll_load_success.setVisibility(View.GONE);
                int delCount = (int)msg.obj;
                EventBus.getDefault().post(new TabIndicatorUpdateEvent(0, 1,false));
                dismissPWProgressDialog();
                break;
            case GET_PHOTO_BACKGROUND:
                if (msg.obj != null) {
                    photos = (List<PhotoDownLoadInfo>)(msg.obj);
                    if (adapter != null){
                        if (photos != null && photos.size() >0) {
                            ll_load_success.setVisibility(View.VISIBLE);
                            rl_load_success.setVisibility(View.GONE);
                            adapter.setPhotos(photos);
                            adapter.notifyDataSetChanged();
                            EventBus.getDefault().post(new TabIndicatorUpdateEvent(photos.size(), 1, false));
                        }else{
                            rl_load_success.setVisibility(View.VISIBLE);
                            ll_load_success.setVisibility(View.GONE);
                            EventBus.getDefault().post(new TabIndicatorUpdateEvent(0, 1,false));
                        }
                    }else{
                        if (photos != null && photos.size()>0) {
                            ll_load_success.setVisibility(View.VISIBLE);
                            rl_load_success.setVisibility(View.GONE);
                            adapter = new PhotoLoadSuccessAdapter(MyApplication.getInstance(), photos);
                            lv_success.setAdapter(adapter);
                            EventBus.getDefault().post(new TabIndicatorUpdateEvent(photos.size(), 1,false));
                        }else{
                            rl_load_success.setVisibility(View.VISIBLE);
                            ll_load_success.setVisibility(View.GONE);
                            EventBus.getDefault().post(new TabIndicatorUpdateEvent(0, 1,false));
                        }

                    }
                }
                break;
            case RELOAD_DATABASE:
                if (msg.obj != null) {
                    photos = (List<PhotoDownLoadInfo>)(msg.obj);
                    if (photos != null && photos.size() >0) {
                        ll_load_success.setVisibility(View.VISIBLE);
                        rl_load_success.setVisibility(View.GONE);
                        adapter = new PhotoLoadSuccessAdapter(MyApplication.getInstance(), photos);
                        lv_success.setAdapter(adapter);
                        EventBus.getDefault().post(new TabIndicatorUpdateEvent(photos.size(), 1,false));
                    }else{
                        rl_load_success.setVisibility(View.VISIBLE);
                        ll_load_success.setVisibility(View.GONE);
                        EventBus.getDefault().post(new TabIndicatorUpdateEvent(0, 1,false));
                    }
                }
                activityClick();
                dismissPWProgressDialog();
                break;
            default:
                break;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_loadsuccess, container, false);
        lv_success = (ListView) view.findViewById(R.id.lv_load_success);
        rl_load_success = (ScrollView) view.findViewById(R.id.rl_load_success);
        ll_load_success = (RelativeLayout) view.findViewById(R.id.ll_load_success);
        button_toload = (Button) view.findViewById(R.id.load_success_btn_toload);
        rl_load_success.setVisibility(View.GONE);
        ll_load_success.setVisibility(View.GONE);
        button_toload.setOnClickListener(this);
        lv_success.setOnItemClickListener(this);
        ll_pop = (LinearLayout) view.findViewById(R.id.poplayout_load_success);
        tv_selectAll = (TextView) view.findViewById(R.id.tv_load_success_select_all);
        tv_delete = (TextView) view.findViewById(R.id.tv_load_success_delete);
        tv_delete.setEnabled(false);
        tv_selectAll.setOnClickListener(this);
        tv_delete.setOnClickListener(this);
        animationIn = AnimationUtils.loadAnimation(MyApplication.getInstance(),R.anim.push_bottom_in);
        animationOut= AnimationUtils.loadAnimation(MyApplication.getInstance(),R.anim.push_bottom_out);
        animationOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ll_pop.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        ll_pop.setVisibility(View.GONE);
        if (TextUtils.isEmpty(userId)) {
            userId = SPUtils.getString(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ID, "");
        }
        executorService = Executors.newFixedThreadPool(1);
        selectPhotos = new CopyOnWriteArrayList<>();
        myToast = new PWToast(MyApplication.getInstance());
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.load_success_btn_toload:
                MyApplication.getInstance().setMainTabIndex(0);
                Intent i = new Intent();
                i.setClass(MyApplication.getInstance(), MainTabActivity.class);
                startActivity(i);
                break;
            case R.id.tv_load_success_select_all:
                if (photos == null || photos.size() ==0 || adapter == null) return;
                if (!selectAll){
                    selecAllButtonStatusChange(true);
                    setPhotosSelect();
                }else{
                    selecAllButtonStatusChange(false);
                    reversePhotoSelect();
                }
                enableReconnectDeleteButton();
                break;

            case R.id.tv_load_success_delete:
                if (selectPhotos.size() == 0) return;
                if (executorService != null && !executorService.isShutdown()){
                    showPWProgressDialog();
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
//                            isLoading = true;
                            if (selectAll){
                                PictureAirDbManager.deleteDownloadPhoto(userId);
                                loadPhotos(RELOAD_DATABASE);
                            }else {
                                removeSelectPhotosFromDB();
                                loadPhotos(RELOAD_DATABASE);
                            }
                        }
                    });
                }

                break;
            default:
                break;
        }
    }

    private void selecAllButtonStatusChange(boolean all) {
        Drawable top;
        if (all){
            top = ContextCompat.getDrawable(getContext(), R.drawable.edit_album_disall_button);
            tv_selectAll.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
            tv_selectAll.setText(R.string.edit_story_disall);
            selectAll = true;
        }else{
            top = ContextCompat.getDrawable(getContext(), R.drawable.edit_album_all_button);
            tv_selectAll.setCompoundDrawablesWithIntrinsicBounds(null,top,null,null);
            tv_selectAll.setText(R.string.edit_story_all);
            selectAll=false;
        }
    }

    private void activityClick(){
        Activity activity = getActivity();
        if (activity != null && activity instanceof LoadManageActivity){
            LoadManageActivity loadManageActivity = (LoadManageActivity)activity;
            loadManageActivity.onClick(R.id.load_manage_cancle);
        }
    }

    private void removeSelectPhotosFromDB(){
        if (selectPhotos.size() >0){
            for (int i=0;i<selectPhotos.size();i++){
                String photoId = selectPhotos.get(i).getPhotoId();
                PictureAirLog.e("removeSelectPhotosFromDB","photoId:"+photoId);
                PictureAirDbManager.deletePhotoByPhotoId(userId,photoId);
            }
        }
    }

    private void setPhotosSelect(){
        if (selectPhotos.size() >0) selectPhotos.clear();
        for (int i=0;i<photos.size();i++){
            PhotoDownLoadInfo info = photos.get(i);
            info.setSelect(true);
            info.setSelectPos(i);
            selectPhotos.add(info);
        }

        adapter.setPhotos(photos);
        adapter.notifyDataSetChanged();
    }

    private void reversePhotoSelect(){
        for (int i=0;i<photos.size();i++){
            PhotoDownLoadInfo info = photos.get(i);
            info.setSelect(false);
            info.setSelectPos(0);
        }
        adapter.setPhotos(photos);
        adapter.notifyDataSetChanged();
        if (selectPhotos != null) {
            selectPhotos.clear();
        }
    }

    public void getDataBackground(){
        if (executorService != null) {
            if (!executorService.isShutdown()) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        loadPhotos(GET_PHOTO_BACKGROUND);
                    }
                });
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void loadPhotos(int what){
        try{
            List<PhotoDownLoadInfo> photos = new ArrayList<>();
            if (!TextUtils.isEmpty(userId)) {
                photos = PictureAirDbManager.getPhotosOrderByTime(userId, "true");
            }
            if (photoLoadSuccessHandler != null){
                photoLoadSuccessHandler.obtainMessage(what, photos).sendToTarget();
            }
        }catch (Exception e){
            e.printStackTrace();

        }
    }

    public boolean changeToSelectState(){
        if (adapter != null){
            if (photos.size() == 0 || adapter.getPhotos().size() == 0){
                return false;
            }
            adapter.setSelect(true);
            ll_pop.setVisibility(View.VISIBLE);
            ll_pop.startAnimation(animationIn);
            return true;
        }
        return false;
    }

    public void changeToNormalState(){
        if (adapter != null){
            clearSelectStatus();
            adapter.setSelect(false);
            selectPhotos.clear();
            enableReconnectDeleteButton();
            Drawable top = ContextCompat.getDrawable(getContext(), R.drawable.edit_album_all_button);
            tv_selectAll.setCompoundDrawablesWithIntrinsicBounds(null,top,null,null);
            tv_selectAll.setText(R.string.edit_story_all);
            selectAll=false;
            ll_pop.startAnimation(animationOut);
        }
    }

    private void clearSelectStatus(){
        if (photos.size() >0) {
            for (int i = 0; i < photos.size(); i++) {
                PhotoDownLoadInfo info = photos.get(i);
                info.setSelect(false);
                info.setSelectPos(0);
            }
            adapter.setPhotos(photos);
        }
    }

    public void initView(){
        if (executorService != null) {
            if (!executorService.isShutdown()) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        loadPhotos(LOAD_FROM_DATABASE);
                    }
                });
            }
        }
    }
}
