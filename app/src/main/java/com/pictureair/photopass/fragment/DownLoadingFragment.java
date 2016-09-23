package com.pictureair.photopass.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
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
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.BaseFragment;
import com.pictureair.photopass.activity.LoadManageActivity;
import com.pictureair.photopass.activity.MyPPActivity;
import com.pictureair.photopass.adapter.PhotoDownloadingAdapter;
import com.pictureair.photopass.entity.DownloadFileStatus;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.eventbus.TabIndicatorUpdateEvent;
import com.pictureair.photopass.service.DownloadService;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.widget.PWToast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import de.greenrobot.event.EventBus;

/**
 * Created by pengwu on 16/7/8.
 */
public class DownLoadingFragment extends BaseFragment implements View.OnClickListener,AdapterView.OnItemClickListener {

    private ListView lv_loading;
    private DownloadService downloadService;
    private CopyOnWriteArrayList<DownloadFileStatus> downloadList;
    public static final int PHOTO_STATUS_UPDATE = 2222;
    public static final int PHOTO_REMOVE = 3333;
    public static final int SERVICE_LOAD_SUCCESS = 4444;
    public static final int DOWNLOAD_FINISH = 5555;
    public static final int REMOVE_FAILED_PHOTOS = 6666;
    public static final int PHOTO_ALL_SELECT = 7777;
    public static final int BIND_INIT = 8888;
    private RelativeLayout rl_loading;
    private RelativeLayout ll_loading;
    private Button button;
    private PhotoDownloadingAdapter adapter;
    private PWToast myToast;
    private LinearLayout ll_popup;
    private Animation animationIn;
    private Animation animationOut;
    private TextView tv_selectAll;
    private TextView tv_reconnect;
    private TextView tv_delete;
    private CopyOnWriteArrayList<PhotoInfo> selectPhotos;
    private boolean selectAll;
    private boolean isEdit;


    private Handler adapterHandler;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (adapter == null || downloadList.size() ==0 || downloadList == null) return;
        if (downloadList.size() > position){
            DownloadFileStatus fileStatus = downloadList.get(position);
            if (isEdit) {//编辑状态下，数据保存列表
                if (fileStatus != null && fileStatus.status == DownloadFileStatus.DOWNLOAD_STATE_SELECT) {
                    if (fileStatus.select == 0) {
                        PhotoInfo info = newPhotoInfo(fileStatus, position);
                        fileStatus.select = 1;
                        adapter.setList(downloadList);
                        adapter.notifyDataSetChanged();
                        selectPhotos.add(info);
                    } else {
                        fileStatus.select = 0;
                        adapter.setList(downloadList);
                        adapter.notifyDataSetChanged();
                        removePhotoInfo(fileStatus, position);
                    }
                    enableReconnectDeleteButton();
                    ifSelectAllWhenItemClick();
                }
            }else{//非编辑状态下点击item 直接下载
                DownloadFileStatus reconnecFile = downloadList.get(position);
                if (reconnecFile != null && (reconnecFile.status == DownloadFileStatus.DOWNLOAD_STATE_FAILURE
                        || reconnecFile.status == DownloadFileStatus.DOWNLOAD_STATE_UPLOADING)){
                    reconnecFile.status = DownloadFileStatus.DOWNLOAD_STATE_RECONNECT;
                    reconnecFile.setCurrentSize("0");
                    reconnecFile.setLoadSpeed("0");
                    reconnecFile.setTotalSize("0");
                    Object tag = view.getTag();
                    if (tag instanceof PhotoDownloadingAdapter.Holder){
                        PhotoDownloadingAdapter.Holder holder = (PhotoDownloadingAdapter.Holder)tag;
                        if (holder != null){
                            holder.tv_size.setText("0MB/0MB");
                            holder.tv_speed.setText("0KB/S");
                            holder.tv_status.setText(MyApplication.getInstance().getString(R.string.photo_download_reconnect));
                            holder.img_status.mCanDraw = false;
                            holder.img_status.setImageResource(R.drawable.photo_status_reconnect);
                        }
                    }
                    ArrayList<PhotoInfo> photos = new ArrayList<>();
                    PhotoInfo info = new PhotoInfo();
                    info.isVideo = reconnecFile.isVideo();
                    info.photoPathOrURL = reconnecFile.getUrl();
                    info.photoId = reconnecFile.getPhotoId();
                    info.shootOn = reconnecFile.getShootOn();
                    info.failedTime = reconnecFile.getFailedTime();
                    photos.add(info);
                    Intent intent = new Intent(MyApplication.getInstance(),DownloadService.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("photos",photos);
                    bundle.putInt("reconnect",1);
                    bundle.putInt("prepareDownloadCount",photos.size());
                    intent.putExtras(bundle);
                    MyApplication.getInstance().startService(intent);
                }
            }
        }
    }

    /**
     * 是否点击全选按钮
     * */
    private void ifSelectAllWhenItemClick(){
        if (selectPhotos.size() == downloadList.size()){
            selecAllButtonStatusChange(true);
        }else{
            selecAllButtonStatusChange(false);
        }
    }

    private void enableReconnectDeleteButton(){
        if (selectPhotos.size() >0){
            tv_reconnect.setEnabled(true);
            tv_delete.setEnabled(true);
        }else{
            tv_reconnect.setEnabled(false);
            tv_delete.setEnabled(false);
        }
    }

    private PhotoInfo newPhotoInfo(DownloadFileStatus fileStatus,int position){
        PhotoInfo info = new PhotoInfo();
        info.isVideo = fileStatus.isVideo();
        info.photoPathOrURL = fileStatus.getUrl();
        info.photoId = fileStatus.getPhotoId();
        info.shootOn = fileStatus.getShootOn();
        info.failedTime = fileStatus.getFailedTime();
        info.isSelected = position;
        return info;
    }

    private void removePhotoInfo(DownloadFileStatus fileStatus,int pos){
        if (selectPhotos.size() >0 ) {
            Iterator<PhotoInfo> iterator = selectPhotos.iterator();
            while (iterator.hasNext()) {
                PhotoInfo info = iterator.next();
                if (info.isSelected == pos) {
                    selectPhotos.remove(info);
                }
            }
        }
    }

    public static class AdapterHandler extends Handler{
        private final WeakReference<DownLoadingFragment> mActivity;

        public AdapterHandler(DownLoadingFragment activity){
            mActivity = new WeakReference<>(activity);
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

    private void dealHandler(Message msg) {
        switch (msg.what) {
            case BIND_INIT:
                if (downloadService != null) {
                    downloadList = downloadService.getDownloadList();
                    downloadService.setAdapterhandler(adapterHandler);
                    if (AppUtil.checkPermission(MyApplication.getInstance(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        showPWProgressDialog();
                    }
                    if (downloadList != null && downloadList.size() >0) {
                        ll_loading.setVisibility(View.VISIBLE);
                        rl_loading.setVisibility(View.GONE);
                        adapter = new PhotoDownloadingAdapter(getContext(), downloadList);
                        lv_loading.setAdapter(adapter);
                    }
                    Intent intent = new Intent(MyApplication.getInstance(), DownloadService.class);
                    ArrayList<PhotoInfo> photos = new ArrayList<>();
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("photos", photos);
                    intent.putExtras(bundle);
                    MyApplication.getInstance().startService(intent);
                }
                break;
            case PHOTO_STATUS_UPDATE://更新listview
                DownloadFileStatus fileStatus = (DownloadFileStatus)msg.obj;
                updateView();
                if (fileStatus != null) {
                    if (fileStatus.status == DownloadFileStatus.DOWNLOAD_STATE_FAILURE
                            || fileStatus.status == DownloadFileStatus.DOWNLOAD_STATE_UPLOADING){
                        downloadService.sendAddDownLoadMessage();
                    }
                }
                break;
            case PHOTO_REMOVE://下载成功后删除
                DownloadFileStatus status = (DownloadFileStatus)msg.obj;
                if (downloadList != null) {
                    downloadList = downloadService.getDownloadList();
                    PictureAirLog.v("PHOTO_REMOVE","downloadList.size: "+downloadList.size());
                    if (adapter != null) {
                        adapter.setList(downloadList);
                        adapter.notifyDataSetChanged();
                    }else{
                        adapter = new PhotoDownloadingAdapter(getContext(),downloadList);
                        lv_loading.setAdapter(adapter);
                    }
                    EventBus.getDefault().post(new TabIndicatorUpdateEvent(downloadList.size(),0,false));
                    downloadService.sendAddDownLoadMessage();
                }
                break;

            case SERVICE_LOAD_SUCCESS://DownloadService 绑定成功，更新页面，设置下载数量
                Activity activity = getActivity();
                if (activity != null && activity instanceof LoadManageActivity){
                    LoadManageActivity loadManageActivity = (LoadManageActivity)activity;
                    loadManageActivity.successFragmentLoading();
                }
                if (adapter == null) {
                    downloadList = downloadService.getDownloadList();
                    if (downloadList != null && downloadList.size() >0) {
                        ll_loading.setVisibility(View.VISIBLE);
                        rl_loading.setVisibility(View.GONE);
                        adapter = new PhotoDownloadingAdapter(getContext(),downloadList);
                        lv_loading.setAdapter(adapter);
                    }else{
                        ll_loading.setVisibility(View.GONE);
                        rl_loading.setVisibility(View.VISIBLE);
                    }
                }else{
                    downloadList = downloadService.getDownloadList();
                    adapter.setList(downloadList);
                    adapter.notifyDataSetChanged();
                }
                EventBus.getDefault().post(new TabIndicatorUpdateEvent(downloadList.size(), 0,false));
                activityClick();
                downloadService.startDownload();
                dismissPWProgressDialog();
                break;
            case DOWNLOAD_FINISH://下载完成，此时downloadservice中已没有可下载任务，页面显示没有下载中的照片
                CopyOnWriteArrayList<DownloadFileStatus> list = downloadService.getDownloadList();
                if (list == null || list.size() == 0) {
                    ll_loading.setVisibility(View.GONE);
                    rl_loading.setVisibility(View.VISIBLE);
                }
                dismissPWProgressDialog();
                break;

            case REMOVE_FAILED_PHOTOS://表示已删除所有下载失败的照片，更新页面
                downloadList = downloadService.getDownloadList();
                if (downloadList != null && downloadList.size() > 0) {
                    adapter.setList(downloadList);
                    adapter.notifyDataSetChanged();
                }else{
                    ll_loading.setVisibility(View.GONE);
                    rl_loading.setVisibility(View.VISIBLE);

                }
                EventBus.getDefault().post(new TabIndicatorUpdateEvent(downloadList.size(),0,false));
                activityClick();
                dismissPWProgressDialog();
                break;

            case PHOTO_ALL_SELECT:
                updateView();
                if (selectAll){
                    setSelectPhotos();
                }else{
                    selectPhotos.clear();
                }
                enableReconnectDeleteButton();
                dismissPWProgressDialog();
                break;
            default:
                dismissPWProgressDialog();
                break;
        }
    }

    private void setSelectPhotos(){
        if (downloadList!= null && downloadList.size() >0) {
            if (selectPhotos.size() >0) selectPhotos.clear();
            for (int i = 0; i < downloadList.size(); i++) {
                DownloadFileStatus fileStatus = downloadList.get(i);
                PhotoInfo info = newPhotoInfo(fileStatus,i);
                selectPhotos.add(info);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_downloading,null);
        lv_loading = (ListView) view.findViewById(R.id.lv_downloading);
        rl_loading = (RelativeLayout) view.findViewById(R.id.rl_downloading);
        button = (Button) view.findViewById(R.id.downloading_btn_toload);
        ll_loading = (RelativeLayout) view.findViewById(R.id.rl_list_downloading);
        ll_popup = (LinearLayout) view.findViewById(R.id.poplayout_downloading);
        tv_selectAll = (TextView) view.findViewById(R.id.tv_downloading_select_all);
        tv_reconnect = (TextView) view.findViewById(R.id.tv_downloading_reconnect);
        tv_delete = (TextView) view.findViewById(R.id.tv_downloading_delete);
        tv_reconnect.setEnabled(false);
        tv_delete.setEnabled(false);
        tv_selectAll.setOnClickListener(this);
        tv_reconnect.setOnClickListener(this);
        tv_delete.setOnClickListener(this);
        selectPhotos = new CopyOnWriteArrayList<>();
        animationIn = AnimationUtils.loadAnimation(MyApplication.getInstance(),R.anim.push_bottom_in);
        animationOut= AnimationUtils.loadAnimation(MyApplication.getInstance(),R.anim.push_bottom_out);
        animationOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ll_popup.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        ll_popup.setVisibility(View.GONE);
        myToast = new PWToast(MyApplication.getInstance());
        ll_loading.setVisibility(View.GONE);
        rl_loading.setVisibility(View.GONE);
        button.setOnClickListener(this);
        lv_loading.setOnItemClickListener(this);
        bindService();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DownloadService.PhotoBind photoBind = (DownloadService.PhotoBind)service;
            downloadService = photoBind.getService();
            if (adapterHandler == null){
                adapterHandler =  new AdapterHandler(DownLoadingFragment.this);
            }
            adapterHandler.sendEmptyMessage(BIND_INIT);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.downloading_btn_toload:
                Intent intent = new Intent();
                intent.setClass(MyApplication.getInstance(), MyPPActivity.class);
                startActivity(intent);
                break;

            case R.id.tv_downloading_select_all:
                if (downloadService == null) return;
                if (!selectAll){
                    selecAllButtonStatusChange(true);
                    downloadService.setDownloadListSelectOrNot(1);
                }else{
                    selecAllButtonStatusChange(false);
                    downloadService.setDownloadListSelectOrNot(0);
                }

                break;

            case R.id.tv_downloading_reconnect:
                if (selectPhotos.size() == 0) return;
                showPWProgressDialog();
                ArrayList<PhotoInfo> list = new ArrayList<>();
                Intent intent1 = new Intent(MyApplication.getInstance(), DownloadService.class);
                for (int i=0;i<selectPhotos.size();i++){
                    PhotoInfo info = selectPhotos.get(i);
                    list.add(info);
                    if (i != 0 && (i % 50 == 0) && (i != selectPhotos.size() - 1) && list.size() > 0) {//如果全部扔过去，超出intent传递的限制，报错，因此分批扔过去，每次扔50个
                        //开始将图片加入下载队列
                        Bundle bundle = new Bundle();
                        bundle.putParcelableArrayList("photos", list);
                        bundle.putInt("prepareDownloadCount",selectPhotos.size());
                        bundle.putInt("reconnect",1);
                        intent1.putExtras(bundle);
                        MyApplication.getInstance().startService(intent1);
                        list.clear();
                    }

                }

                if (list.size() > 0) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("photos", list);
                    bundle.putInt("prepareDownloadCount", selectPhotos.size());
                    bundle.putInt("reconnect", 1);
                    intent1.putExtras(bundle);
                    MyApplication.getInstance().startService(intent1);
                }
                break;

            case R.id.tv_downloading_delete:
                if (downloadService == null || selectPhotos.size() == 0) return;
                showPWProgressDialog();
                if (selectAll){
                    downloadService.sendClearFailedMsg();
                }else {
                    downloadService.deleteSelecItems();
                }
                break;
            default:
                break;
        }
    }

    private void selecAllButtonStatusChange(boolean all) {
        Drawable top;
        if (all){
            top = getResources().getDrawable(R.drawable.edit_album_disall_button);
            tv_selectAll.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
            tv_selectAll.setText(R.string.edit_story_disall);
            selectAll = true;
        }else{
            top = getResources().getDrawable(R.drawable.edit_album_all_button);
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

    private void bindService(){
        Intent intent = new Intent(getContext(),DownloadService.class);
        getContext().bindService(intent,conn,Context.BIND_AUTO_CREATE);
    }

    private void unBind(){
        getContext().unbindService(conn);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (downloadService != null){
            downloadService.setAdapterhandler(null);
            unBind();
        }
        if (adapterHandler != null){
            adapterHandler.removeCallbacksAndMessages(null);
        }
    }

    private void updateView(){
        downloadList = downloadService.getDownloadList();
        if (adapter != null) {
            adapter.setList(downloadList);
            adapter.notifyDataSetChanged();
        } else {
            adapter = new PhotoDownloadingAdapter(getContext(),downloadList);
            lv_loading.setAdapter(adapter);
        }

    }

    public boolean changeToSelectState(){
        if (downloadService != null) {
            if (downloadService.downloadListContainsFailur()){
                downloadService.updateDownloadList();
                isEdit = true;
                ll_popup.setVisibility(View.VISIBLE);
                ll_popup.startAnimation(animationIn);
                return true;
            }
        }
        return false;
    }

    public void changeToNormalState(){
        if (downloadService != null) {
            isEdit = false;
            downloadService.reverseDownloadList();
            selectPhotos.clear();
            enableReconnectDeleteButton();
            Drawable top = getResources().getDrawable(R.drawable.edit_album_all_button);
            tv_selectAll.setCompoundDrawablesWithIntrinsicBounds(null,top,null,null);
            tv_selectAll.setText(R.string.edit_story_all);
            selectAll=false;
            ll_popup.startAnimation(animationOut);
            return;
        }
    }

    public boolean getIsDownloading(){
        if (downloadService != null && downloadService.isDownloading()){
            return true;
        }
        return false;
    }
}
